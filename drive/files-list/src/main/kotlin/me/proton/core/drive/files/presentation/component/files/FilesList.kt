/*
 * Copyright (c) 2021-2023 Proton AG.
 * This file is part of Proton Core.
 *
 * Proton Core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Core.  If not, see <https://www.gnu.org/licenses/>.
 */
package me.proton.core.drive.files.presentation.component.files

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.compose.itemKey
import me.proton.core.compose.component.DeferredCircularProgressIndicator
import me.proton.core.compose.component.ErrorPadding
import me.proton.core.compose.component.ProtonErrorMessage
import me.proton.core.compose.component.ProtonErrorMessageWithAction
import me.proton.core.compose.component.ProtonSolidButton
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonDimens.DefaultSpacing
import me.proton.core.compose.theme.ProtonDimens.ExtraSmallSpacing
import me.proton.core.compose.theme.ProtonDimens.MediumSpacing
import me.proton.core.compose.theme.ProtonDimens.SmallSpacing
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultWeak
import me.proton.core.compose.theme.headline
import me.proton.core.drive.base.presentation.extension.conditional
import me.proton.core.drive.base.presentation.extension.isLandscape
import me.proton.core.drive.base.presentation.extension.isPortrait
import me.proton.core.drive.drivelink.domain.entity.DriveLink
import me.proton.core.drive.files.presentation.component.LazyColumnItems
import me.proton.core.util.kotlin.exhaustive
import kotlin.math.ceil
import me.proton.core.drive.base.presentation.R as BasePresentation
import me.proton.core.drive.i18n.R as I18N

@Composable
fun FilesListLoading(modifier: Modifier = Modifier) {
    DeferredCircularProgressIndicator(modifier)
}

fun LazyListScope.FilesListLoading(modifier: Modifier = Modifier) {
    item {
        me.proton.core.drive.files.presentation.component.files.FilesListLoading(modifier.fillParentMaxSize())
    }
}

@Composable
fun FilesListEmpty(
    @DrawableRes imageResId: Int,
    @StringRes titleResId: Int,
    @StringRes descriptionResId: Int,
    @StringRes actionResId: Int,
    modifier: Modifier = Modifier,
    onAction: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(painter = painterResource(id = imageResId), contentDescription = null)

                Text(
                    text = stringResource(id = titleResId),
                    style = ProtonTheme.typography.headline.copy(textAlign = TextAlign.Center),
                    modifier = Modifier.padding(
                        top = SmallSpacing,
                        start = DefaultSpacing,
                        end = DefaultSpacing,
                    )
                )
                if (descriptionResId != 0) {
                    Text(
                        text = stringResource(id = descriptionResId),
                        style = ProtonTheme.typography.defaultWeak.copy(textAlign = TextAlign.Center),
                        modifier = Modifier.padding(
                            top = ExtraSmallSpacing,
                            start = DefaultSpacing,
                            end = DefaultSpacing,
                        )
                    )
                }
            }
        }
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            if (actionResId != 0) {
                ProtonSolidButton(
                    onClick = onAction,
                    modifier = Modifier
                        .conditional(isPortrait) {
                            this
                                .padding(all = MediumSpacing)
                                .fillMaxWidth()
                        }
                        .conditional(isLandscape) {
                            this
                                .padding(all = SmallSpacing)
                                .widthIn(min = ButtonMinWidth)
                        }
                        .heightIn(min = ProtonDimens.ListItemHeight),
                ) {
                    Text(
                        text = stringResource(id = actionResId),
                        modifier = Modifier.padding(horizontal = DefaultSpacing)
                    )
                }
            }
        }
    }
}


fun LazyListScope.FilesListEmpty(
    @DrawableRes imageResId: Int,
    @StringRes titleResId: Int,
    @StringRes descriptionResId: Int,
    @StringRes actionResId: Int,
    modifier: Modifier = Modifier,
    onAction: () -> Unit,
) {
    item {
        me.proton.core.drive.files.presentation.component.files.FilesListEmpty(
            imageResId = imageResId,
            titleResId = titleResId,
            descriptionResId = descriptionResId,
            actionResId = actionResId,
            onAction = onAction,
            modifier = modifier.fillParentMaxSize()
        )
    }
}

@Composable
fun FilesListError(
    message: String,
    @StringRes actionResId: Int?,
    modifier: Modifier = Modifier,
    onAction: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(ErrorPadding),
        contentAlignment = Alignment.BottomCenter
    ) {
        if (actionResId != null) {
            ProtonErrorMessageWithAction(
                errorMessage = message,
                action = stringResource(id = actionResId),
                onAction = onAction
            )
        } else {
            ProtonErrorMessage(errorMessage = message)
        }
    }
}

fun LazyListScope.FilesListError(
    message: String,
    @StringRes actionResId: Int?,
    modifier: Modifier = Modifier,
    onAction: () -> Unit,
) {
    item {
        me.proton.core.drive.files.presentation.component.files.FilesListError(
            message = message,
            actionResId = actionResId,
            modifier = modifier.fillParentMaxSize(),
            onAction = onAction
        )
    }
}

fun LazyListScope.FilesListContent(
    driveLinks: LazyColumnItems,
    onItemsIndexed: @Composable LazyItemScope.(DriveLink) -> Unit,
) {
    when (driveLinks) {
        is LazyColumnItems.PagingItems -> items(
            count = driveLinks.value.itemCount,
            key = driveLinks.value.itemKey { driveLink -> driveLink.id.id },
        ) { index ->
            driveLinks.value[index]?.let { driveLink ->
                onItemsIndexed(driveLink)
            }
        }
        is LazyColumnItems.ListItems -> items(
            items = driveLinks.value,
            key = { driveLink -> driveLink.id.id },
        ) { driveLink ->
            onItemsIndexed(driveLink)
        }
    }.exhaustive
}

fun LazyListScope.FilesGridContent(
    driveLinks: LazyColumnItems,
    itemsPerRow: Int,
    onItemsIndexed: @Composable RowScope.(DriveLink) -> Unit,
) {
    require(itemsPerRow > 0) { "itemsPerRow must be > 0, value passed $itemsPerRow" }
    items(ceil(driveLinks.size.toFloat() / itemsPerRow).toInt()) { row ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ExtraSmallSpacing),
            horizontalArrangement = Arrangement.spacedBy(ExtraSmallSpacing),
        ) {
            repeat(itemsPerRow) { repeat ->
                val driveLink = driveLinks[row * itemsPerRow + repeat]
                if (driveLink != null) {
                    onItemsIndexed(driveLink)
                } else {
                    Spacer(modifier = Modifier.weight(1F))
                }
            }
        }
    }
}

@Preview(
    name = "Empty My files in light mode",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Empty My files in dark mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun PreviewMyFilesEmpty() {
    ProtonTheme {
        Surface {
            FilesListEmpty(
                imageResId = BasePresentation.drawable.empty_folder_daynight,
                titleResId = I18N.string.title_empty_my_files,
                descriptionResId = I18N.string.description_empty_my_files,
                actionResId = I18N.string.action_empty_files_add_files,
                onAction = {}
            )
        }
    }
}

@Preview(
    name = "Empty folder in light mode",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Empty folder in dark mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun PreviewFolderEmpty() {
    ProtonTheme {
        Surface {
            FilesListEmpty(
                imageResId = BasePresentation.drawable.empty_folder_daynight,
                titleResId = I18N.string.title_empty_folder,
                descriptionResId = I18N.string.description_empty_folder,
                actionResId = I18N.string.action_empty_files_add_files,
                onAction = {}
            )
        }
    }
}

@Preview(
    name = "Empty trash in light mode",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Empty trash in dark mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun PreviewTrashEmpty() {
    ProtonTheme {
        Surface {
            FilesListEmpty(
                imageResId = BasePresentation.drawable.empty_trash_daynight,
                titleResId = I18N.string.title_empty_trash,
                descriptionResId = I18N.string.description_empty_trash,
                actionResId = 0,
                onAction = {}
            )
        }
    }
}

@Preview(
    name = "Empty shared in light mode",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Empty shared in dark mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun PreviewSharedEmpty() {
    ProtonTheme {
        Surface {
            FilesListEmpty(
                imageResId = BasePresentation.drawable.empty_shared_by_me_daynight,
                titleResId = I18N.string.title_empty_shared,
                descriptionResId = I18N.string.description_empty_shared,
                actionResId = 0,
                onAction = {}
            )
        }
    }
}

@Preview(
    name = "Empty available offline in light mode",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Empty available offline in dark mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun PreviewAvailableOfflineEmpty() {
    ProtonTheme {
        Surface {
            FilesListEmpty(
                imageResId = BasePresentation.drawable.empty_offline_daynight,
                titleResId = I18N.string.title_empty_offline_available,
                descriptionResId = I18N.string.description_empty_offline_available,
                actionResId = 0,
                onAction = {}
            )
        }
    }
}

private val ButtonMinWidth = 300.dp
