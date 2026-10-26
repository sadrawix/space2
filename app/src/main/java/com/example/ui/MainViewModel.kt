package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.AppDatabase
import com.example.data.model.CloneItem
import com.example.data.prefs.PreferencesManager
import com.example.data.repository.CloneRepository
import com.example.engine.CloneEngineManager
import com.example.engine.CloneableApp
import com.example.engine.EngineOption
import com.example.engine.EngineType
import com.example.engine.WorkProfileState
import com.example.engine.WorkProfiles
import com.example.engine.engineOptions
import com.example.plan.LocalVipProvider
import com.example.plan.PinHasher
import com.example.plan.PlanPolicy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class Screen { HOME, SETTINGS, BATTERY_HELPER, ONBOARDING }

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = CloneRepository(database.cloneDao())
    val prefs = PreferencesManager(application)
    val engineManager = CloneEngineManager(application)
    private val vipProvider = LocalVipProvider(prefs)

    // ---------- Data ----------
    val allClones: StateFlow<List<CloneItem>> = repository.allClones.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val cloneCount: StateFlow<Int> = repository.cloneCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    // ---------- Preferences ----------
    val isVip: StateFlow<Boolean> = vipProvider.isVip
    val isSpaceLockEnabled = prefs.isSpaceLockEnabled
    val spacePin = prefs.spacePin
    val preferredEngine = prefs.preferredEngine
    val language = prefs.language
    val hasCompletedOnboarding = prefs.hasCompletedOnboarding

    // ---------- UI state ----------
    private val _isSpaceUnlocked = MutableStateFlow(!prefs.isSpaceLockEnabled.value)
    val isSpaceUnlocked = _isSpaceUnlocked.asStateFlow()

    private val _currentScreen = MutableStateFlow(
        if (prefs.hasCompletedOnboarding.value) Screen.HOME else Screen.ONBOARDING
    )
    val currentScreen = _currentScreen.asStateFlow()

    private val _engineInfo =
        MutableStateFlow(engineManager.selectActiveEngine(prefs.preferredEngine.value))
    val engineInfo = _engineInfo.asStateFlow()

    private val _workProfileState = MutableStateFlow(WorkProfiles.state(application))
    /** Drive the Home badge from this: READY = green, NEEDS_SETUP = yellow, UNSUPPORTED = gray. */
    val workProfileState = _workProfileState.asStateFlow()

    private val _engineOptions = MutableStateFlow<List<EngineOption>>(engineOptions(application))
    val engineOptionsList = _engineOptions.asStateFlow()

    /** UI observes this and shows a dialog that calls CloneAdminReceiver.startProvisioning(activity). */
    private val _needsProfileSetup = MutableStateFlow(false)
    val needsProfileSetup = _needsProfileSetup.asStateFlow()

    private val _installedApps = MutableStateFlow<List<CloneableApp>>(emptyList())
    val installedApps = _installedApps.asStateFlow()

    private val _isLoadingApps = MutableStateFlow(false)
    val isLoadingApps = _isLoadingApps.asStateFlow()

    private val _selectedPackages = MutableStateFlow<Set<String>>(emptySet())
    val selectedPackages = _selectedPackages.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _isAddSheetVisible = MutableStateFlow(false)
    val isAddSheetVisible = _isAddSheetVisible.asStateFlow()

    private val _selectedCloneForDetail = MutableStateFlow<CloneItem?>(null)
    val selectedCloneForDetail = _selectedCloneForDetail.asStateFlow()

    private val _selectedCloneForPin = MutableStateFlow<CloneItem?>(null)
    val selectedCloneForPin = _selectedCloneForPin.asStateFlow()

    private val _isVipStoreVisible = MutableStateFlow(false)
    val isVipStoreVisible = _isVipStoreVisible.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage = _toastMessage.asStateFlow()

    init {
        refreshEngine()
    }

    // ---------- Navigation ----------
    fun navigateTo(screen: Screen) { _currentScreen.value = screen }

    fun completeOnboarding() {
        prefs.setCompletedOnboarding(true)
        _currentScreen.value = Screen.HOME
    }

    // ---------- Engine ----------
    fun refreshEngine() {
        val app = getApplication<Application>()
        _engineInfo.value = engineManager.selectActiveEngine(prefs.preferredEngine.value)
        _workProfileState.value = WorkProfiles.state(app)
        _engineOptions.value = engineOptions(app)
    }

    fun setPreferredEngine(engine: String) {
        prefs.setPreferredEngine(engine)
        refreshEngine()
    }

    fun dismissProfileSetup() { _needsProfileSetup.value = false }

    // ---------- VIP (debug only until billing exists) ----------
    fun setVipForTesting(vip: Boolean) {
        if (BuildConfig.DEBUG) prefs.setVip(vip)
    }

    fun setVip(vip: Boolean) = setVipForTesting(vip)

    fun setLanguage(lang: String) = prefs.setLanguage(lang)

    // ---------- Space lock (PIN stored as hash) ----------
    fun setSpaceLock(enabled: Boolean, pin: String) {
        prefs.setSpaceLock(enabled, if (enabled) PinHasher.hash(pin) else "")
        if (!enabled) _isSpaceUnlocked.value = true
    }

    private fun pinMatches(entered: String): Boolean =
        PinHasher.hash(entered) == prefs.spacePin.value

    fun unlockSpace(enteredPin: String): Boolean {
        if (pinMatches(enteredPin)) {
            _isSpaceUnlocked.value = true
            return true
        }
        return false
    }

    fun lockSpace() {
        if (prefs.isSpaceLockEnabled.value) _isSpaceUnlocked.value = false
    }

    // ---------- Sheets ----------
    fun showAddSheet() {
        _selectedPackages.value = emptySet()
        _searchQuery.value = ""
        _isAddSheetVisible.value = true
        loadInstalledApps()
    }

    fun hideAddSheet() {
        _isAddSheetVisible.value = false
        _selectedPackages.value = emptySet()
    }

    fun showVipStore(show: Boolean) { _isVipStoreVisible.value = show }
    fun showCloneDetail(clone: CloneItem?) { _selectedCloneForDetail.value = clone }
    fun showClonePinPrompt(clone: CloneItem?) { _selectedCloneForPin.value = clone }
    fun setSearchQuery(query: String) { _searchQuery.value = query }

    // ---------- Plan limits (free = 3 apps, VIP = unlimited) ----------
    private fun clonedAppsCount(): Int =
        allClones.value.map { it.packageName }.toSet().size

    fun remainingSlots(): Int =
        if (isVip.value) Int.MAX_VALUE
        else (PlanPolicy.FREE_MAX_APPS - clonedAppsCount()).coerceAtLeast(0)

    /** Text for the sheet header, e.g. "2 / 3" for free users, "2" for VIP. */
    fun counterText(): String {
        val selected = _selectedPackages.value.size
        return if (isVip.value) "$selected"
        else "${selected + clonedAppsCount()} / ${PlanPolicy.FREE_MAX_APPS}"
    }

    fun togglePackageSelection(packageName: String) {
        val current = _selectedPackages.value.toMutableSet()
        if (current.remove(packageName)) {
            _selectedPackages.value = current
            return
        }
        if (!isVip.value && current.size >= remainingSlots()) {
            _toastMessage.value =
                "Free plan: max ${PlanPolicy.FREE_MAX_APPS} apps. VIP (unlimited) is coming soon."
            _isVipStoreVisible.value = true
            return
        }
        current.add(packageName)
        _selectedPackages.value = current
    }

    // ---------- Installed apps ----------
    fun loadInstalledApps() {
        viewModelScope.launch {
            _isLoadingApps.value = true
            try {
                val engine = _engineInfo.value.activeEngine
                val apps = engine.listCloneableApps(getApplication())
                val clonedPkgs = allClones.value.map { it.packageName }.toSet()
                _installedApps.value = apps.map { app ->
                    app.copy(isAlreadyCloned = clonedPkgs.contains(app.packageName))
                }
            } catch (e: Exception) {
                // No fake apps. Show the real error instead.
                _installedApps.value = emptyList()
                _toastMessage.value = "Cannot list apps: ${e.localizedMessage}"
            } finally {
                _isLoadingApps.value = false
            }
        }
    }

    // ---------- Create clones ----------
    fun createClonesForSelected() {
        val selected = _selectedPackages.value
        if (selected.isEmpty()) return

        val engine = _engineInfo.value.activeEngine

        // 1) The engine must really be usable. No silent fallbacks.
        if (engine.engineType == EngineType.WORK_PROFILE) {
            when (WorkProfiles.state(getApplication())) {
                WorkProfileState.UNSUPPORTED -> {
                    _toastMessage.value =
                        "This phone does not support Work Profile. Try OEM Dual Apps."
                    return
                }
                WorkProfileState.NEEDS_SETUP -> {
                    _toastMessage.value = "Create the Work Profile first"
                    _needsProfileSetup.value = true
                    return
                }
                WorkProfileState.READY -> Unit
            }
        }

        // 2) Plan limit, checked again here (never trust the UI alone).
        val alreadyCloned = allClones.value.map { it.packageName }.toSet()
        val newPackages = selected - alreadyCloned
        if (!isVip.value && newPackages.size > remainingSlots()) {
            _toastMessage.value = "Free plan limit reached (${PlanPolicy.FREE_MAX_APPS} apps)."
            _isVipStoreVisible.value = true
            return
        }

        viewModelScope.launch {
            val appsMap = _installedApps.value.associateBy { it.packageName }
            var ok = 0
            var failed = 0
            var lastError: String? = null

            for (pkg in selected) {
                // 3) Respect the engine's per-app copy limit.
                val used = allClones.value.count {
                    it.packageName == pkg && it.engineType == engine.engineType.name
                }
                if (used >= engine.maxInstances(pkg)) {
                    failed++
                    lastError = "Max copies reached for this app"
                    continue
                }

                val appName = appsMap[pkg]?.appName ?: pkg.substringAfterLast('.')
                engine.createClone(getApplication(), pkg, appName)
                    .onSuccess { item ->
                        val index = used + 2
                        repository.insertClone(
                            item.copy(instanceIndex = index, displayName = "$appName $index")
                        )
                        ok++
                    }
                    .onFailure { err ->
                        failed++
                        lastError = err.localizedMessage
                    }
            }

            _selectedPackages.value = emptySet()
            if (ok > 0) _isAddSheetVisible.value = false
            _toastMessage.value = when {
                failed == 0 -> "Added $ok app(s) to Multiclonix Space"
                ok == 0 -> "Clone failed: ${lastError ?: "unknown error"}"
                else -> "Added $ok, failed $failed: ${lastError ?: ""}"
            }
        }
    }

    // ---------- Launch ----------
    fun launchClone(context: Context, clone: CloneItem) {
        if (clone.isLocked) {
            _selectedCloneForPin.value = clone
            return
        }
        performLaunch(context, clone)
    }

    fun unlockAndLaunchClone(context: Context, clone: CloneItem, enteredPin: String): Boolean {
        if (pinMatches(enteredPin)) {
            _selectedCloneForPin.value = null
            performLaunch(context, clone)
            return true
        }
        return false
    }

    private fun performLaunch(context: Context, clone: CloneItem) {
        viewModelScope.launch {
            val engine = _engineInfo.value.activeEngine
            engine.launchClone(context, clone)
                .onSuccess { repository.updateLastLaunched(clone.id) }
                .onFailure { err ->
                    _toastMessage.value =
                        err.localizedMessage ?: "Clone is not isolated. Set up Work Profile first."
                }
        }
    }

    // ---------- Edit / delete ----------
    fun updateClone(clone: CloneItem) {
        viewModelScope.launch {
            repository.updateClone(clone)
            _selectedCloneForDetail.value = null
            _toastMessage.value = "Clone updated"
        }
    }

    fun deleteClone(id: Long) {
        viewModelScope.launch {
            repository.deleteClone(id)
            _selectedCloneForDetail.value = null
            _toastMessage.value = "Clone removed from Space"
        }
    }

    fun clearToast() { _toastMessage.value = null }
}
