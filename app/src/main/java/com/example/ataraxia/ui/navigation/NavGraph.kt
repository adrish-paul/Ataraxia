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
import com.example.ataraxia.ui.components.FloatingBottomNavigation
import com.example.ataraxia.features.breathe.presentation.BreatheScreen
import com.example.ataraxia.features.breathe.presentation.BreatheViewModel
import com.example.ataraxia.features.focus.presentation.FocusScreen
import com.example.ataraxia.features.home.presentation.HomeScreen
import com.example.ataraxia.features.journal.presentation.JournalScreen
import com.example.ataraxia.features.me.presentation.MeScreen
import com.example.ataraxia.features.auth.presentation.PinLockScreen
import com.example.ataraxia.features.auth.presentation.SplashScreen
import com.example.ataraxia.features.auth.presentation.WelcomeSetupScreen
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color
import com.example.ataraxia.R
import com.example.ataraxia.ui.theme.AtaraxiaTheme
import com.example.ataraxia.ui.theme.AtaraxiaThemeMode
import com.example.ataraxia.ui.theme.DeepIndigo
import com.example.ataraxia.ui.theme.WarmIvory
import androidx.compose.runtime.CompositionLocalProvider
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import com.example.ataraxia.ui.theme.LocalHazeState
import com.example.ataraxia.features.focus.presentation.FocusViewModel
import com.example.ataraxia.features.journal.presentation.JournalViewModel
import com.example.ataraxia.viewmodel.MainViewModel

@Composable
fun AtaraxiaNavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current

    val factory = remember { com.example.ataraxia.core.di.AtaraxiaViewModelFactory(
        (context.applicationContext as com.example.ataraxia.core.di.AtaraxiaApplication).container
    ) }

    val mainViewModel: MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
    val journalViewModel: JournalViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
    val breatheViewModel: BreatheViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
    val focusViewModel: FocusViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)

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

    // Scroll-to-top keys: increment when user taps a tab they're already on
    var homeScrollKey by remember { mutableStateOf(0) }
    var journalScrollKey by remember { mutableStateOf(0) }
    var breatheScrollKey by remember { mutableStateOf(0) }
    var focusScrollKey by remember { mutableStateOf(0) }
    var meScrollKey by remember { mutableStateOf(0) }

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
        if (currentRoute == route) {
            // Already on this tab — scroll to top instead of re-navigating
            when (route) {
                Screen.Home.route    -> homeScrollKey++
                Screen.Journal.route -> journalScrollKey++
                Screen.Breathe.route -> breatheScrollKey++
                Screen.Focus.route   -> focusScrollKey++
                Screen.Me.route      -> meScrollKey++
            }
        } else if (route == Screen.Home.route) {
            // Navigating to Home: pop everything back to Home
            navController.navigate(route) {
                popUpTo(Screen.Home.route) { inclusive = false }
                launchSingleTop = true
            }
        } else {
            // Navigating to a non-Home tab: back stack is Home -> <tab>
            navController.navigate(route) {
                popUpTo(Screen.Home.route) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    val hazeState = remember { HazeState() }

    CompositionLocalProvider(LocalHazeState provides hazeState) {
        AtaraxiaTheme(themeMode = themeMode) {
            val bgImage = when (themeMode) {
                AtaraxiaThemeMode.AURORA -> painterResource(id = R.drawable.aurora_bg)
                AtaraxiaThemeMode.SAKURA -> painterResource(id = R.drawable.sakura_bg)
                AtaraxiaThemeMode.COSMOS -> painterResource(id = R.drawable.cosmos_bg)
                AtaraxiaThemeMode.LIGHT -> painterResource(id = R.drawable.serene_bg)
                AtaraxiaThemeMode.FOREST -> painterResource(id = R.drawable.forest_bg)
                AtaraxiaThemeMode.AQUA -> painterResource(id = R.drawable.aqua_bg)
            }
            val scrimColor = when (themeMode) {
                AtaraxiaThemeMode.LIGHT -> WarmIvory.copy(alpha = 0.5f)
                AtaraxiaThemeMode.SAKURA -> Color(0xFFFCF5F7).copy(alpha = 0.45f)
                AtaraxiaThemeMode.AQUA -> Color(0xFFF2F7FA).copy(alpha = 0.45f)
                AtaraxiaThemeMode.AURORA -> DeepIndigo.copy(alpha = 0.65f)
                AtaraxiaThemeMode.COSMOS -> Color.Black.copy(alpha = 0.7f)
                AtaraxiaThemeMode.FOREST -> Color(0xFF0F1412).copy(alpha = 0.65f)
            }
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .hazeSource(state = hazeState)
                ) {
                    Image(
                        painter = bgImage,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(scrimColor)
                    )
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
                                        popUpTo(0) { inclusive = true }
                                    }
                                } else {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            } else {
                                navController.navigate(Screen.Welcome.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }
                    )
                }

                composable(Screen.Welcome.route) {
                    WelcomeSetupScreen(
                        onGetStarted = { name ->
                            mainViewModel.completeOnboarding(name)
                            navController.navigate(Screen.Home.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.PinLock.route) {
                    PinLockScreen(
                        correctPin = appPin,
                        onUnlockSuccess = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }


                composable(Screen.Home.route) {
                    HomeScreen(
                        name = userName,
                        isFirstLaunch = isFirstLaunch,
                        currentThemeMode = themeMode,
                        onFirstLaunchCompleted = { mainViewModel.completeFirstLaunch() },
                        onQuickBreathe = { navigateToTab(Screen.Breathe.route) },
                        onQuickJournal = { navigateToTab(Screen.Journal.route) },
                        onQuickFocus = { navigateToTab(Screen.Focus.route) },
                        scrollToTopKey = homeScrollKey
                    )
                }

                composable(Screen.Journal.route) {
                    JournalScreen(
                        viewModel = journalViewModel,
                        onWritingModeChanged = { isJournalWriting = it },
                        scrollToTopKey = journalScrollKey
                    )
                }

                composable(Screen.Breathe.route) {
                    BreatheScreen(
                        viewModel = breatheViewModel,
                        onSessionActiveChanged = { isBreatheActive = it },
                        scrollToTopKey = breatheScrollKey
                    )
                }

                composable(Screen.Focus.route) {
                    FocusScreen(
                        viewModel = focusViewModel,
                        onSpaceActiveChanged = { isFocusActive = it },
                        scrollToTopKey = focusScrollKey
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
                        },
                        scrollToTopKey = meScrollKey
                    )
                }
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
}
