/*
 * Copyright (c) 2023-2024 Proton AG.
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

package me.proton.core.drive.backup.domain.usecase

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import me.proton.core.drive.backup.data.manager.BackupPermissionsManagerImpl
import me.proton.core.drive.backup.data.repository.BackupConfigurationRepositoryImpl
import me.proton.core.drive.backup.data.repository.BackupErrorRepositoryImpl
import me.proton.core.drive.backup.data.repository.BackupFileRepositoryImpl
import me.proton.core.drive.backup.data.repository.BackupFolderRepositoryImpl
import me.proton.core.drive.backup.domain.entity.BackupFile
import me.proton.core.drive.backup.domain.entity.BackupFileState
import me.proton.core.drive.backup.domain.entity.BackupFolder
import me.proton.core.drive.backup.domain.entity.BackupPermissions
import me.proton.core.drive.backup.domain.entity.BackupState
import me.proton.core.drive.backup.domain.entity.BackupStatus
import me.proton.core.drive.backup.domain.entity.BucketEntry
import me.proton.core.drive.backup.domain.manager.BackupPermissionsManager
import me.proton.core.drive.backup.domain.manager.StubbedBackupConnectivityManager
import me.proton.core.drive.backup.domain.manager.StubbedBackupManager
import me.proton.core.drive.backup.domain.repository.BucketRepository
import me.proton.core.drive.base.domain.entity.TimestampS
import me.proton.core.drive.base.domain.extension.bytes
import me.proton.core.drive.base.domain.provider.ConfigurationProvider
import me.proton.core.drive.db.test.DriveDatabaseRule
import me.proton.core.drive.db.test.NoNetworkConfigurationProvider
import me.proton.core.drive.db.test.myDrive
import me.proton.core.drive.link.domain.entity.FolderId
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GetBackupStateTest {

    @get:Rule
    val database = DriveDatabaseRule()
    private lateinit var folderId: FolderId

    private val appContext = ApplicationProvider.getApplicationContext<Application>()
    private lateinit var addFolder: AddFolder
    private lateinit var deleteFolders: DeleteFolders
    private lateinit var setFiles: SetFiles
    private lateinit var markAsCompleted: MarkAsCompleted
    private lateinit var markAsFailed: MarkAsFailed
    private lateinit var backupState: Flow<BackupState>

    private lateinit var backupManager: StubbedBackupManager
    private val permissionsManager: BackupPermissionsManager =
        BackupPermissionsManagerImpl(appContext)
    private val connectivityManager = StubbedBackupConnectivityManager

    private var bucketEntries = listOf(BucketEntry(0, "Camera"))

    @Before
    fun setUp() = runTest {
        folderId = database.myDrive {}
        val folderRepository = BackupFolderRepositoryImpl(database.db)
        val fileRepository = BackupFileRepositoryImpl(database.db)
        val errorRepository = BackupErrorRepositoryImpl(database.db)

        addFolder = AddFolder(folderRepository)
        deleteFolders = DeleteFolders(folderRepository)
        setFiles = SetFiles(fileRepository)
        markAsCompleted = MarkAsCompleted(fileRepository)
        markAsFailed = MarkAsFailed(fileRepository)
        backupManager = StubbedBackupManager(folderRepository)
        addFolder = AddFolder(folderRepository)
        deleteFolders = DeleteFolders(folderRepository)
        permissionsManager.onPermissionChanged(BackupPermissions.Granted)

        val getBackupState = GetBackupState(
            getBackupStatus = GetBackupStatus(fileRepository),
            backupManager = backupManager,
            permissionsManager = permissionsManager,
            connectivityManager = connectivityManager,
            getErrors = GetErrors(errorRepository, NoNetworkConfigurationProvider.instance),
            getAllBuckets = GetAllBuckets(object : BucketRepository {
                override suspend fun getAll(): List<BucketEntry> = bucketEntries
            }, permissionsManager),
            configurationProvider = object : ConfigurationProvider {
                override val host = ""
                override val baseUrl = ""
                override val appVersionHeader = ""
                override val backupDefaultBucketName = "Camera"
            },
            getConfiguration = GetConfiguration(BackupConfigurationRepositoryImpl(database.db)),
        )

        backupState = getBackupState(folderId)
    }

    @Test
    fun `blank backup state`() = runTest {
        bucketEntries = emptyList()

        assertEquals(
            BackupState(
                isBackupEnabled = false,
                hasDefaultFolder = false,
                backupStatus = null,
            ),
            backupState.first(),
        )
    }

    @Test
    fun `blank backup state with folder`() = runTest {
        database.myDrive {}

        assertEquals(
            BackupState(
                isBackupEnabled = false,
                hasDefaultFolder = true,
                backupStatus = null,
            ),
            backupState.first(),
        )
    }

    @Test
    fun `running backup state`() = runTest {
        addFolder(BackupFolder(0, folderId)).getOrThrow()

        assertEquals(
            BackupState(
                isBackupEnabled = true,
                hasDefaultFolder = true,
                backupStatus = BackupStatus.Complete(totalBackupPhotos = 0),
            ),
            backupState.first(),
        )
    }

    @Test
    fun `uploading backup state`() = runTest {
        addFolder(BackupFolder(0, folderId)).getOrThrow()

        setFiles(
            listOf(
                backupFile("uri1"),
                backupFile("uri2"),
                backupFile("uri3"),
            )
        ).getOrThrow()
        markAsCompleted(folderId, "uri1").getOrThrow()


        assertEquals(
            BackupState(
                isBackupEnabled = true,
                hasDefaultFolder = true,
                backupStatus = BackupStatus.InProgress(
                    totalBackupPhotos = 3,
                    pendingBackupPhotos = 2,
                )
            ),
            backupState.first(),
        )
    }
    @Test
    fun failure() = runTest {
        addFolder(BackupFolder(0, folderId)).getOrThrow()

        setFiles(
            listOf(
                backupFile("uri1"),
                backupFile("uri2"),
                backupFile("uri3"),
            )
        ).getOrThrow()
        markAsFailed(folderId, "uri1").getOrThrow()

        assertEquals(
            BackupState(
                isBackupEnabled = true,
                hasDefaultFolder = true,
                backupStatus = BackupStatus.InProgress(
                    totalBackupPhotos = 3,
                    pendingBackupPhotos = 2,
                )
            ),
            backupState.first(),
        )
    }

    @Test
    fun uncompleted() = runTest {
        addFolder(BackupFolder(0, folderId)).getOrThrow()

        setFiles(
            listOf(
                backupFile("uri1"),
                backupFile("uri2"),
                backupFile("uri3"),
            )
        ).getOrThrow()
        markAsCompleted(folderId, "uri1").getOrThrow()
        markAsCompleted(folderId, "uri2").getOrThrow()
        markAsFailed(folderId, "uri3").getOrThrow()

        assertEquals(
            BackupState(
                isBackupEnabled = true,
                hasDefaultFolder = true,
                backupStatus = BackupStatus.Uncompleted(
                    totalBackupPhotos = 3,
                    failedBackupPhotos = 1,
                )
            ),
            backupState.first(),
        )
    }

    @Test
    fun `stopped backup state`() = runTest {

        addFolder(BackupFolder(0, folderId)).getOrThrow()
        deleteFolders(folderId).getOrThrow()

        assertEquals(
            BackupState(
                isBackupEnabled = false,
                hasDefaultFolder = true,
                backupStatus = null,
            ),
            backupState.first(),
        )
    }

    private fun backupFile(uriString: String) = BackupFile(
        bucketId = 0,
        folderId = folderId,
        uriString = uriString,
        mimeType = "",
        name = "",
        hash = "",
        size = 0.bytes,
        state = BackupFileState.IDLE,
        date = TimestampS(0),
    )
}
