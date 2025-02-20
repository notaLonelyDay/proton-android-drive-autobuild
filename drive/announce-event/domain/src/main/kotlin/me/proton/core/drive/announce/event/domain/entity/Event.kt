/*
 * Copyright (c) 2022-2024 Proton AG.
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
package me.proton.core.drive.announce.event.domain.entity

import kotlinx.serialization.Serializable
import me.proton.core.drive.base.domain.entity.Bytes
import me.proton.core.drive.base.domain.entity.Percentage
import me.proton.core.drive.base.domain.entity.TimestampMs
import me.proton.core.drive.link.domain.entity.FolderId

@Serializable
sealed class Event {
    abstract val id: String
    abstract val occurredAt: TimestampMs

    @Serializable
    data class StorageFull(val needed: Bytes) : Event() {
        override val id: String = "$EVENT_ID_PREFIX${this.javaClass.simpleName.uppercase()}_1"
        override val occurredAt: TimestampMs = TimestampMs(System.currentTimeMillis())
    }

    @Serializable
    data class Upload(
        val state: UploadState,
        val uploadFileLinkId: Long,
        val percentage: Percentage,
        val shouldShow: Boolean,
        val reason: Reason? = null,
    ) : Event() {
        override val id: String =
            "$EVENT_ID_PREFIX${this.javaClass.simpleName.uppercase()}_$uploadFileLinkId"
        override val occurredAt: TimestampMs = TimestampMs(System.currentTimeMillis())

        enum class UploadState {
            NEW_UPLOAD,
            UPLOADING,
            UPLOAD_COMPLETE,
            UPLOAD_FAILED,
            UPLOAD_CANCELLED,
        }

        enum class Reason {
            ERROR_OTHER,
            ERROR_PERMISSIONS,
            ERROR_DRIVE_STORAGE,
            ERROR_LOCAL_STORAGE,
            ERROR_NOT_ALLOWED,
        }

    }

    @Serializable
    data class Backup(
        val folderId: FolderId,
        val state: BackupState,
        val totalBackupPhotos: Int,
        val pendingBackupPhotos: Int,
    ) : Event() {
        override val id: String = "$EVENT_ID_PREFIX${this.javaClass.simpleName.uppercase()}"
        override val occurredAt: TimestampMs = TimestampMs(System.currentTimeMillis())

        enum class BackupState {
            IN_PROGRESS,
            COMPLETE,
            FAILED,
            FAILED_CONNECTIVITY,
            FAILED_WIFI_CONNECTIVITY,
            FAILED_PERMISSION,
            FAILED_LOCAL_STORAGE,
            FAILED_DRIVE_STORAGE,
            FAILED_PHOTOS_UPLOAD_NOT_ALLOWED,
            PAUSED_DISABLED,
            UNCOMPLETED,
        }
    }

    data class BackupEnabled(val folderId: FolderId) : Event() {
        override val id: String = "$EVENT_ID_PREFIX${this.javaClass.simpleName.uppercase()}_1"
        override val occurredAt: TimestampMs = TimestampMs(System.currentTimeMillis())
    }

    data class BackupDisabled(val folderId: FolderId) : Event() {
        override val id: String = "$EVENT_ID_PREFIX${this.javaClass.simpleName.uppercase()}_1"
        override val occurredAt: TimestampMs = TimestampMs(System.currentTimeMillis())
    }

    data class BackupStarted(val folderId: FolderId) : Event() {
        override val id: String = "$EVENT_ID_PREFIX${this.javaClass.simpleName.uppercase()}_1"
        override val occurredAt: TimestampMs = TimestampMs(System.currentTimeMillis())
    }

    data class BackupStopped(val folderId: FolderId, val state: Backup.BackupState) : Event() {
        override val id: String = "$EVENT_ID_PREFIX${this.javaClass.simpleName.uppercase()}_1"
        override val occurredAt: TimestampMs = TimestampMs(System.currentTimeMillis())
    }

    data class BackupCompleted(val folderId: FolderId) : Event() {
        override val id: String = "$EVENT_ID_PREFIX${this.javaClass.simpleName.uppercase()}_1"
        override val occurredAt: TimestampMs = TimestampMs(System.currentTimeMillis())
    }

    @Serializable
    data class Download(val downloadId: String, val downloadedFiles: Int, val totalFiles: Int) :
        Event() {
        override val id: String =
            "$EVENT_ID_PREFIX${this.javaClass.simpleName.uppercase()}_$downloadId"
        override val occurredAt: TimestampMs = TimestampMs(System.currentTimeMillis())
    }

    object ForcedSignOut : Event() {
        override val id: String = "$EVENT_ID_PREFIX${this.javaClass.simpleName.uppercase()}_1"
        override val occurredAt: TimestampMs = TimestampMs(System.currentTimeMillis())
    }

    object NoSpaceLeftOnDevice : Event() {
        override val id: String = "$EVENT_ID_PREFIX${this.javaClass.simpleName.uppercase()}_1"
        override val occurredAt: TimestampMs = TimestampMs(System.currentTimeMillis())
    }

    companion object {
        private const val EVENT_ID_PREFIX = "NOTIFICATION_EVENT_ID_"
    }
}
