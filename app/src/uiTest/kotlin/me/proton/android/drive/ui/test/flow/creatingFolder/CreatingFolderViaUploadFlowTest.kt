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

package me.proton.android.drive.ui.test.flow.creatingFolder

import androidx.test.rule.GrantPermissionRule
import dagger.hilt.android.testing.HiltAndroidTest
import me.proton.android.drive.ui.robot.CreateFolderRobot
import me.proton.android.drive.ui.robot.FilesTabRobot
import me.proton.android.drive.ui.robot.LauncherRobot
import me.proton.android.drive.ui.rules.ExternalFilesRule
import me.proton.android.drive.ui.rules.UserLoginRule
import me.proton.android.drive.ui.test.EmptyBaseTest
import me.proton.android.drive.utils.getRandomString
import me.proton.core.test.quark.data.User
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class CreatingFolderViaUploadFlowTest : EmptyBaseTest() {
    private val testUser = User(name = "proton_drive_${getRandomString(15)}")

    @get:Rule(order = 1)
    val userLoginRule: UserLoginRule = UserLoginRule(testUser, quarkCommands = quarkRule.quarkCommands)

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
    )

    @get:Rule
    val externalFilesRule = ExternalFilesRule()

    @Test
    fun createFolderWhenUploadingViaThirdPartyApp() {
        val randomFolderName = getRandomString()

        val file = externalFilesRule.create1BFile("empty.txt")

        LauncherRobot.uploadTo(file)
            .verify {
                robotDisplayed()
                assertEmptyFolder() // TODO: Enable create folder button when folder is loaded
            }.clickCreateFolder()

        CreateFolderRobot
            .typeFolderName(randomFolderName)
            .clickCreate()
        FilesTabRobot
            .dismissFolderCreateSuccessGrowler(randomFolderName, FilesTabRobot)
            .verify {
                itemIsDisplayed(randomFolderName)
            }
    }

}
