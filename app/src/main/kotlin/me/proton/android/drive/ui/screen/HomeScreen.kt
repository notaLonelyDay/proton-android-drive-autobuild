/*
 * Copyright (c) 2023 Proton AG.
 * This file is part of Proton Drive.
 *
 * Proton Drive is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Drive is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Drive.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.android.drive.ui.screen

import android.os.Bundle
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import me.proton.android.drive.ui.navigation.HomeNavGraph
import me.proton.android.drive.ui.navigation.PagerType
import me.proton.android.drive.ui.navigation.Screen
import me.proton.android.drive.ui.options.OptionsFilter
import me.proton.android.drive.ui.provider.setLocalSnackbarPadding
import me.proton.android.drive.ui.viewevent.HomeViewEvent
import me.proton.android.drive.ui.viewmodel.HomeViewModel
import me.proton.android.drive.ui.viewstate.HomeViewState
import me.proton.android.drive.ui.viewstate.rememberHomeScaffoldState
import me.proton.core.compose.component.DeferredCircularProgressIndicator
import me.proton.core.compose.component.ProtonSnackbarHost
import me.proton.core.compose.component.ProtonSnackbarHostState
import me.proton.core.compose.component.ProtonSnackbarType
import me.proton.core.compose.component.bottomsheet.ModalBottomSheetViewState
import me.proton.core.compose.flow.rememberFlowWithLifecycle
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.domain.entity.UserId
import me.proton.core.drive.base.presentation.component.BottomNavigation
import me.proton.core.drive.base.presentation.component.ModalBottomSheet
import me.proton.core.drive.link.domain.entity.FileId
import me.proton.core.drive.link.domain.entity.FolderId
import me.proton.core.drive.link.domain.entity.LinkId
import me.proton.core.drive.link.selection.domain.entity.SelectionId
import me.proton.core.drive.navigationdrawer.presentation.NavigationDrawer
import me.proton.core.drive.sorting.domain.entity.Sorting

@Composable
@ExperimentalAnimationApi
@ExperimentalCoroutinesApi
fun HomeScreen(
    userId: UserId,
    homeNavController: NavHostController,
    deepLinkBaseUrl: String,
    startDestination: String,
    arguments: Bundle,
    navigateToBugReport: () -> Unit,
    onDrawerStateChanged: (Boolean) -> Unit,
    navigateToSigningOut: () -> Unit,
    navigateToTrash: () -> Unit,
    navigateToOffline: () -> Unit,
    navigateToPreview: (fileId: FileId, pagerType: PagerType, optionsFilter: OptionsFilter) -> Unit,
    navigateToSorting: (sorting: Sorting) -> Unit,
    navigateToSettings: () -> Unit,
    navigateToFileOrFolderOptions: (linkId: LinkId, optionsFilter: OptionsFilter) -> Unit,
    navigateToMultipleFileOrFolderOptions: (selectionId: SelectionId, optionsFilter: OptionsFilter) -> Unit,
    navigateToParentFolderOptions: (folderId: FolderId) -> Unit,
    navigateToSubscription: () -> Unit,
    navigateToPhotosIssues: (FolderId) -> Unit,
    navigateToBackupSettings: () -> Unit,
    navigateToPhotosPermissionRationale: () -> Unit,
    modifier: Modifier = Modifier,
) {
    setLocalSnackbarPadding(BottomNavigationHeight)
    val homeViewModel = hiltViewModel<HomeViewModel>()
    DisposableEffect(homeNavController) {
        homeViewModel.setCurrentDestination(startDestination)
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            homeViewModel.setCurrentDestination(destination.route ?: startDestination)
        }
        homeNavController.addOnDestinationChangedListener(listener)
        onDispose {
            homeNavController.removeOnDestinationChangedListener(listener)
        }
    }
    val viewState by rememberFlowWithLifecycle(flow = homeViewModel.viewState)
        .collectAsState(initial = null)
    viewState?.let { currentViewState ->
        Home(
            homeNavController = homeNavController,
            deepLinkBaseUrl = deepLinkBaseUrl,
            startDestination = startDestination,
            onDrawerStateChanged = onDrawerStateChanged,
            navigateToPreview = navigateToPreview,
            navigateToSorting = navigateToSorting,
            navigateToFileOrFolderOptions = navigateToFileOrFolderOptions,
            navigateToMultipleFileOrFolderOptions = navigateToMultipleFileOrFolderOptions,
            navigateToParentFolderOptions = navigateToParentFolderOptions,
            navigateToPhotosPermissionRationale = navigateToPhotosPermissionRationale,
            navigateToSubscription = navigateToSubscription,
            navigateToPhotosIssues = navigateToPhotosIssues,
            navigateToBackupSettings = navigateToBackupSettings,
            arguments = arguments,
            viewState = currentViewState,
            viewEvent = homeViewModel.viewEvent(
                navigateToSigningOut = navigateToSigningOut,
                navigateToTrash = navigateToTrash,
                navigateToTab = { route ->
                    homeNavController.navigate(route) {
                        popUpTo(Screen.Files.route) { inclusive = route == Screen.Files(userId) }
                        launchSingleTop = true
                    }
                },
                navigateToOffline = navigateToOffline,
                navigateToSettings = navigateToSettings,
                navigateToBugReport = navigateToBugReport,
                navigateToSubscription = navigateToSubscription,
            ),
            modifier = modifier
                .navigationBarsPadding()
                .testTag(HomeScreenTestTag.screen),
        )
    } ?: DeferredCircularProgressIndicator(modifier)
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
@ExperimentalCoroutinesApi
@ExperimentalAnimationApi
internal fun Home(
    homeNavController: NavHostController,
    deepLinkBaseUrl: String,
    startDestination: String,
    onDrawerStateChanged: (Boolean) -> Unit,
    navigateToPreview: (fileId: FileId, pagerType: PagerType, optionsFilter: OptionsFilter) -> Unit,
    navigateToSorting: (sorting: Sorting) -> Unit,
    navigateToFileOrFolderOptions: (linkId: LinkId, optionsFilter: OptionsFilter) -> Unit,
    navigateToMultipleFileOrFolderOptions: (selectionId: SelectionId, optionsFilter: OptionsFilter) -> Unit,
    navigateToParentFolderOptions: (folderId: FolderId) -> Unit,
    arguments: Bundle,
    viewState: HomeViewState,
    viewEvent: HomeViewEvent,
    modifier: Modifier = Modifier,
    navigateToPhotosPermissionRationale: () -> Unit,
    navigateToSubscription: () -> Unit,
    navigateToPhotosIssues: (FolderId) -> Unit,
    navigateToBackupSettings: () -> Unit,
) {
    val homeScaffoldState = rememberHomeScaffoldState()
    val isDrawerOpen = with(homeScaffoldState.scaffoldState.drawerState) {
        (isOpen && !isAnimationRunning) || (isClosed && isAnimationRunning)
    }
    LaunchedEffect(isDrawerOpen) {
        onDrawerStateChanged(isDrawerOpen)
    }
    val drawerGesturesEnabled by homeScaffoldState.drawerGesturesEnabled
    ModalBottomSheet(
        sheetState = homeScaffoldState.modalBottomSheetContentState.sheetState,
        sheetContent = homeScaffoldState.modalBottomSheetContentState.sheetContent.value,
        viewState = remember { ModalBottomSheetViewState() },
    ) {
        var beforeActionGesturesEnabled by remember { mutableStateOf(drawerGesturesEnabled) }
        Scaffold(
            modifier = modifier,
            scaffoldState = homeScaffoldState.scaffoldState,
            drawerShape = RoundedCornerShape(0.dp),
            drawerContent = {
                NavigationDrawer(
                    drawerState = homeScaffoldState.scaffoldState.drawerState,
                    viewState = viewState.navigationDrawerViewState,
                    viewEvent = viewEvent.navigationDrawerViewEvent,
                    modifier = Modifier
                        .testTag(HomeScreenTestTag.sidebar)
                        .statusBarsPadding()
                        .navigationBarsPadding(),
                    onCloseOnActionStarted = {
                        beforeActionGesturesEnabled = drawerGesturesEnabled
                        // tapping outside of drawer closes it, once action is selected we
                        // prevent gestures so that cancellation of action is not possible
                        homeScaffoldState.drawerGesturesEnabled.value = false
                    },
                    onCloseOnActionCompleted = {
                        // restore gestures as were before action
                        homeScaffoldState.drawerGesturesEnabled.value = beforeActionGesturesEnabled
                    },
                )
            },
            topBar = {
                Box(
                    modifier = Modifier
                        .statusBarsPadding()
                ) {
                    homeScaffoldState.topAppBar.value()
                }
            },
            drawerGesturesEnabled = drawerGesturesEnabled,
            drawerScrimColor = ProtonTheme.colors.blenderNorm,
            bottomBar = {
                AnimatedVisibility(
                    visible = homeScaffoldState.bottomNavigationEnabled.value,
                    enter = slideInVertically(initialOffsetY = { fullHeight: Int -> fullHeight }),
                    exit = slideOutVertically(targetOffsetY = { fullHeight: Int -> fullHeight }),
                ) {
                    BottomNavigation(
                        modifier = Modifier
                            .testTag(HomeScreenTestTag.bottomBar),
                        selectedTab = viewState.selectedTab,
                        tabs = viewState.tabs,
                        onSelectedTab = { tab -> viewEvent.onTab(tab) },
                    )
                }
            },
            snackbarHost = { snackbarHostState ->
                ProtonSnackbarHost(
                    ProtonSnackbarHostState(
                        snackbarHostState,
                        ProtonSnackbarType.ERROR
                    )
                )
            }
        ) { contentPadding ->
            Box(Modifier
                .padding(contentPadding)
            ) {
                HomeNavGraph(
                    homeNavController,
                    deepLinkBaseUrl,
                    arguments,
                    startDestination,
                    homeScaffoldState,
                    navigateToPreview,
                    navigateToSorting,
                    navigateToFileOrFolderOptions,
                    navigateToMultipleFileOrFolderOptions,
                    navigateToParentFolderOptions,
                    navigateToPhotosPermissionRationale,
                    navigateToSubscription,
                    navigateToPhotosIssues,
                    navigateToBackupSettings,
                )
            }
        }
    }
}

private val BottomNavigationHeight = 56.dp

object HomeScreenTestTag {
    const val screen = "home screen"
    const val sidebar = "home sidebar"
    const val bottomBar = "home bottom bar"
}
