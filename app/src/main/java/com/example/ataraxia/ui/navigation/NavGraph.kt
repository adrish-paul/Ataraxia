package com.example.ataraxia.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ataraxia.data.local.AtaraxiaDatabase
import com.example.ataraxia.data.preferences.SanctuaryPreferences
import com.example.ataraxia.data.repository.JournalRepository
import com.example.ataraxia.data.repository.SessionRepository
import com.example.ataraxia.ui.components.FloatingBottomNavigation
import com.example.ataraxia.ui.screens.BreatheScreen
import com.example.ataraxia.ui.screens.FocusScreen
import com.example.ataraxia.ui.screens.HomeScreen
import com.example.ataraxia.ui.screens.JournalScreen
import com.example.ataraxia.ui.screens.MeScreen
import com.example.ataraxia.ui.screens.PinLockScreen
import com.example.ataraxia.ui.screens.SplashScreen
import com.example.ataraxia.ui.screens.WelcomeSetupScreen
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color
import com.example.ataraxia.R
import com.example.ataraxia.ui.theme.AtaraxiaTheme
import com.example.ataraxia.ui.theme.DesignTokens
import com.example.ataraxia.ui.theme.AtaraxiaThemeMode
import com.example.ataraxia.viewmodel.BreatheViewModel
import com.example.ataraxia.viewmodel.FocusViewModel
import com.example.ataraxia.viewmodel.JournalViewModel
import com.example.ataraxia.viewmodel.MainViewModel

@Composable
fun AtaraxiaNavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Initialize data structures at the root Composable scope
    val database = remember { AtaraxiaDatabase.getDatabase(context) }
    val preferences = remember { SanctuaryPreferences(context) }

    val journalRepository = remember { JournalRepository(database.journalDao()) }
    val sessionRepository = remember { SessionRepository(database.breatheDao(), database.focusDao()) }

    val mainViewModel = remember { MainViewModel(preferences, journalRepository, sessionRepository) }
    val journalViewModel = remember { JournalViewModel(journalRepository) }
    val breatheViewModel = remember { BreatheViewModel(sessionRepository) }
    val focusViewModel = remember { FocusViewModel(sessionRepository) }

    // Read state flows reactively
    val themeMode by mainViewModel.themeMode.collectAsState()
    val userName by mainViewModel.username.collectAsState()
    val onboardingCompleted by mainViewModel.onboardingCompleted.collectAsState()
    val appLockEnabled by mainViewModel.appLockEnabled.collectAsState()
    val appPin by mainViewModel.appPin.collectAsState()
    val profileImage by mainViewModel.profileImage.collectAsState()
    val isFirstLaunch by mainViewModel.isFirstLaunch.collectAsState()

    val reflections by journalViewModel.allEntries.collectAsState()
    val breatheSecs by breatheViewModel.totalDurationSeconds.collectAsState()
    val focusMins by focusViewModel.totalDurationMinutes.collectAsState()

    // Track active sub-screen overlay composition states
    var isJournalWriting by remember { mutableStateOf(false) }
    var isBreatheActive by remember { mutableStateOf(false) }
    var isFocusActive by remember { mutableStateOf(false) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = (currentRoute in listOf(
        Screen.Home.route,
        Screen.Journal.route,
        Screen.Breathe.route,
        Screen.Focus.route,
        Screen.Me.route
    )) && !isJournalWriting && !isBreatheActive && !isFocusActive

    // Index mapping for horizontal navigation layout direction
    val routes = listOf(
        Screen.Home.route,
        Screen.Journal.route,
        Screen.Breathe.route,
        Screen.Focus.route,
        Screen.Me.route
    )

    // Premium EaseOutCubic easing curve matching requested specifications
    val EaseOutCubic = CubicBezierEasing(0.215f, 0.610f, 0.355f, 1.0f)
    val animDuration = 300

    val navigateToTab: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.startDestinationId) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    AtaraxiaTheme(themeMode = themeMode) {
        val bgImage = when (themeMode) {
            AtaraxiaThemeMode.AURORA -> painterResource(id = R.drawable.aurora_bg)
            AtaraxiaThemeMode.SAKURA -> painterResource(id = R.drawable.sakura_bg)
            else -> null
        }
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            if (bgImage != null) {
                Image(
                    painter = bgImage,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DesignTokens.AppBackground)
                )
            }
            NavHost(
                navController = navController,
                startDestination = Screen.Splash.route,
                modifier = Modifier.fillMaxSize(),
                enterTransition = {
                    val initialIndex = routes.indexOf(initialState.destination.route)
                    val targetIndex = routes.indexOf(targetState.destination.route)
                    if (initialIndex != -1 && targetIndex != -1) {
                        if (targetIndex > initialIndex) {
                            // Moving Right: Enter from right, fade 0.9 -> 1.0, scale 0.98 -> 1.0
                            slideInHorizontally(
                                initialOffsetX = { it },
                                animationSpec = tween(animDuration, easing = EaseOutCubic)
                            ) + fadeIn(
                                initialAlpha = 0.9f,
                                animationSpec = tween(animDuration, easing = EaseOutCubic)
                            ) + scaleIn(
                                initialScale = 0.98f,
                                animationSpec = tween(animDuration, easing = EaseOutCubic)
                            )
                        } else {
                            // Moving Left: Enter from left, fade 0.9 -> 1.0, scale 0.98 -> 1.0
                            slideInHorizontally(
                                initialOffsetX = { -it },
                                animationSpec = tween(animDuration, easing = EaseOutCubic)
                            ) + fadeIn(
                                initialAlpha = 0.9f,
                                animationSpec = tween(animDuration, easing = EaseOutCubic)
                            ) + scaleIn(
                                initialScale = 0.98f,
                                animationSpec = tween(animDuration, easing = EaseOutCubic)
                            )
                        }
                    } else {
                        fadeIn(animationSpec = tween(animDuration))
                    }
                },
                exitTransition = {
                    val initialIndex = routes.indexOf(initialState.destination.route)
                    val targetIndex = routes.indexOf(targetState.destination.route)
                    if (initialIndex != -1 && targetIndex != -1) {
                        if (targetIndex > initialIndex) {
                            // Moving Right: Exit to left, fade 1.0 -> 0.9, scale 1.0 -> 0.98
                            slideOutHorizontally(
                                targetOffsetX = { -it },
                                animationSpec = tween(animDuration, easing = EaseOutCubic)
                            ) + fadeOut(
                                targetAlpha = 0.9f,
                                animationSpec = tween(animDuration, easing = EaseOutCubic)
                            ) + scaleOut(
                                targetScale = 0.98f,
                                animationSpec = tween(animDuration, easing = EaseOutCubic)
                            )
                        } else {
                            // Moving Left: Exit to right, fade 1.0 -> 0.9, scale 1.0 -> 0.98
                            slideOutHorizontally(
                                targetOffsetX = { it },
                                animationSpec = tween(animDuration, easing = EaseOutCubic)
                            ) + fadeOut(
                                targetAlpha = 0.9f,
                                animationSpec = tween(animDuration, easing = EaseOutCubic)
                            ) + scaleOut(
                                targetScale = 0.98f,
                                animationSpec = tween(animDuration, easing = EaseOutCubic)
                            )
                        }
                    } else {
                        fadeOut(animationSpec = tween(animDuration))
                    }
                },
                popEnterTransition = {
                    val initialIndex = routes.indexOf(initialState.destination.route)
                    val targetIndex = routes.indexOf(targetState.destination.route)
                    if (initialIndex != -1 && targetIndex != -1) {
                        if (targetIndex > initialIndex) {
                            slideInHorizontally(
                                initialOffsetX = { it },
                                animationSpec = tween(animDuration, easing = EaseOutCubic)
                            ) + fadeIn(
                                initialAlpha = 0.9f,
                                animationSpec = tween(animDuration, easing = EaseOutCubic)
                            ) + scaleIn(
                                initialScale = 0.98f,
                                animationSpec = tween(animDuration, easing = EaseOutCubic)
                            )
                        } else {
                            slideInHorizontally(
                                initialOffsetX = { -it },
                                animationSpec = tween(animDuration, easing = EaseOutCubic)
                            ) + fadeIn(
                                initialAlpha = 0.9f,
                                animationSpec = tween(animDuration, easing = EaseOutCubic)
                            ) + scaleIn(
                                initialScale = 0.98f,
                                animationSpec = tween(animDuration, easing = EaseOutCubic)
                            )
                        }
                    } else {
                        fadeIn(animationSpec = tween(animDuration))
                    }
                },
                popExitTransition = {
                    val initialIndex = routes.indexOf(initialState.destination.route)
                    val targetIndex = routes.indexOf(targetState.destination.route)
                    if (initialIndex != -1 && targetIndex != -1) {
                        if (targetIndex > initialIndex) {
                            slideOutHorizontally(
                                targetOffsetX = { -it },
                                animationSpec = tween(animDuration, easing = EaseOutCubic)
                            ) + fadeOut(
                                targetAlpha = 0.9f,
                                animationSpec = tween(animDuration, easing = EaseOutCubic)
                            ) + scaleOut(
                                targetScale = 0.98f,
                                animationSpec = tween(animDuration, easing = EaseOutCubic)
                            )
                        } else {
                            slideOutHorizontally(
                                targetOffsetX = { it },
                                animationSpec = tween(animDuration, easing = EaseOutCubic)
                            ) + fadeOut(
                                targetAlpha = 0.9f,
                                animationSpec = tween(animDuration, easing = EaseOutCubic)
                            ) + scaleOut(
                                targetScale = 0.98f,
                                animationSpec = tween(animDuration, easing = EaseOutCubic)
                            )
                        }
                    } else {
                        fadeOut(animationSpec = tween(animDuration))
                    }
                }
            ) {
                composable(Screen.Splash.route) {
                    SplashScreen(
                        onNavigateNext = {
                            if (onboardingCompleted) {
                                if (appLockEnabled && appPin.isNotEmpty()) {
                                    navController.navigate(Screen.PinLock.route) {
                                        popUpTo(Screen.Splash.route) { inclusive = true }
                                    }
                                } else {
                                    navigateToTab(Screen.Home.route)
                                }
                            } else {
                                navController.navigate(Screen.Welcome.route) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            }
                        }
                    )
                }

                composable(Screen.Welcome.route) {
                    WelcomeSetupScreen(
                        onGetStarted = { name ->
                            mainViewModel.completeOnboarding(name)
                            navigateToTab(Screen.Home.route)
                        }
                    )
                }

                composable(Screen.PinLock.route) {
                    PinLockScreen(
                        correctPin = appPin,
                        onUnlockSuccess = {
                            navigateToTab(Screen.Home.route)
                        }
                    )
                }

                composable(Screen.Home.route) {
                    HomeScreen(
                        name = userName,
                        profileImage = profileImage,
                        isFirstLaunch = isFirstLaunch,
                        currentThemeMode = themeMode,
                        onFirstLaunchCompleted = { mainViewModel.completeFirstLaunch() },
                        onNavigateToProfile = { navigateToTab(Screen.Me.route) },
                        onQuickBreathe = { navigateToTab(Screen.Breathe.route) },
                        onQuickJournal = { navigateToTab(Screen.Journal.route) },
                        onQuickFocus = { navigateToTab(Screen.Focus.route) }
                    )
                }

                composable(Screen.Journal.route) {
                    JournalScreen(
                        name = userName,
                        profileImage = profileImage,
                        entries = reflections,
                        onAddEntry = { title, content, mood, weatherContext, isFavorite, tags, imagePath, voicePath ->
                            journalViewModel.addEntry(
                                title = title,
                                content = content,
                                mood = mood,
                                weatherContext = weatherContext,
                                isFavorite = isFavorite,
                                tags = tags,
                                imagePath = imagePath,
                                voicePath = voicePath
                            )
                        },
                        onToggleFavorite = { journalViewModel.toggleFavorite(it) },
                        onDeleteEntry = { journalViewModel.deleteEntry(it) },
                        onWritingModeChanged = { isJournalWriting = it },
                        onNavigateToProfile = { navigateToTab(Screen.Me.route) }
                    )
                }

                composable(Screen.Breathe.route) {
                    BreatheScreen(
                        name = userName,
                        profileImage = profileImage,
                        viewModel = breatheViewModel,
                        onSessionActiveChanged = { isBreatheActive = it },
                        onNavigateToProfile = { navigateToTab(Screen.Me.route) }
                    )
                }

                composable(Screen.Focus.route) {
                    FocusScreen(
                        name = userName,
                        profileImage = profileImage,
                        viewModel = focusViewModel,
                        onSpaceActiveChanged = { isFocusActive = it },
                        onNavigateToProfile = { navigateToTab(Screen.Me.route) }
                    )
                }

                composable(Screen.Me.route) {
                    MeScreen(
                        name = userName,
                        profileImage = profileImage,
                        onProfileImageChange = { mainViewModel.updateProfileImage(it) },
                        currentThemeMode = themeMode,
                        reflectionCount = reflections.size,
                        breatheSeconds = breatheSecs,
                        focusMinutes = focusMins,
                        appLockEnabled = appLockEnabled,
                        onAppLockToggle = { enabled, pin ->
                            mainViewModel.updateAppLockEnabled(enabled)
                            mainViewModel.updateAppPin(pin)
                        },
                        onNameChange = { newName ->
                            mainViewModel.updateUsername(newName)
                        },
                        onThemeChange = { mode -> mainViewModel.updateThemeMode(mode) },
                        onClearData = {
                            mainViewModel.clearAllData()
                            navController.navigate(Screen.Splash.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            }

            // Animate sliding the bottom bar in and out of the view
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(600, easing = EaseOutCubic)
                ) + fadeIn(animationSpec = tween(600)),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(600, easing = EaseOutCubic)
                ) + fadeOut(animationSpec = tween(600)),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                FloatingBottomNavigation(
                    currentRoute = currentRoute ?: Screen.Home.route,
                    onNavigate = { route -> navigateToTab(route) }
                )
            }
        }
    }
}
