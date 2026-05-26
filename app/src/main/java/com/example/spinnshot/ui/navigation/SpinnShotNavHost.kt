package com.example.spinnshot.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.spinnshot.ui.GameViewModel
import com.example.spinnshot.ui.agevalidation.AgeValidationScreen
import com.example.spinnshot.ui.game.GameScreen
import com.example.spinnshot.ui.onboarding.OnboardingScreen
import com.example.spinnshot.ui.question.QuestionScreen
import com.example.spinnshot.ui.result.ResultScreen
import com.example.spinnshot.ui.setup.CategoriesScreen
import com.example.spinnshot.ui.setup.GameModeScreen
import com.example.spinnshot.ui.setup.PlayersScreen
import com.example.spinnshot.ui.setup.RoundsScreen

@Composable
fun SpinnShotNavHost(
    navController: NavHostController = rememberNavController(),
    viewModel: GameViewModel = viewModel()
) {
    val game by viewModel.game.collectAsStateWithLifecycle()
    val resolution by viewModel.resolution.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = Routes.ONBOARDING
    ) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(onFinished = { navController.navigate(Routes.AGE) })
        }
        composable(Routes.AGE) {
            AgeValidationScreen(
                viewModel = viewModel,
                onValidated = { navController.navigate(Routes.SETUP_CATEGORIES) }
            )
        }
        composable(Routes.SETUP_CATEGORIES) {
            CategoriesScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNext = { navController.navigate(Routes.SETUP_MODE) }
            )
        }
        composable(Routes.SETUP_MODE) {
            GameModeScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNext = { navController.navigate(Routes.SETUP_PLAYERS) }
            )
        }
        composable(Routes.SETUP_PLAYERS) {
            PlayersScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNext = { navController.navigate(Routes.SETUP_ROUNDS) }
            )
        }
        composable(Routes.SETUP_ROUNDS) {
            RoundsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onStart = {
                    viewModel.startGame()
                    navController.navigate(Routes.GAME) {
                        popUpTo(Routes.SETUP_CATEGORIES) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.GAME) {
            GameScreen(
                viewModel = viewModel,
                onAskQuestion = { navController.navigate(Routes.QUESTION) },
                onTerminate = {
                    viewModel.resetToNewGame()
                    navController.navigate(Routes.SETUP_CATEGORIES) {
                        popUpTo(Routes.ONBOARDING) { inclusive = false }
                    }
                },
                onFinished = { navController.navigate(Routes.RESULT) }
            )
            // Auto-navigate to result when game is finished.
            if (game?.finished == true && resolution != null) {
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    navController.navigate(Routes.RESULT) {
                        popUpTo(Routes.GAME) { inclusive = true }
                    }
                }
            }
        }
        composable(Routes.QUESTION) {
            QuestionScreen(
                viewModel = viewModel,
                onComplete = {
                    if (viewModel.game.value?.finished == true) {
                        navController.navigate(Routes.RESULT) {
                            popUpTo(Routes.GAME) { inclusive = true }
                        }
                    } else {
                        navController.popBackStack(Routes.GAME, inclusive = false)
                    }
                }
            )
        }
        composable(Routes.RESULT) {
            ResultScreen(
                viewModel = viewModel,
                onNewGame = {
                    viewModel.resetToNewGame()
                    navController.navigate(Routes.SETUP_CATEGORIES) {
                        popUpTo(Routes.ONBOARDING) { inclusive = false }
                    }
                }
            )
        }
    }
}
