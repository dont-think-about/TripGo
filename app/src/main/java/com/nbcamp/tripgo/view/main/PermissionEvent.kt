package com.nbcamp.tripgo.view.main

sealed interface PermissionEvent {
    data class GetGalleryPermission(
        val permission: String
    ) : PermissionEvent

    data class GetLocationPermission(
        val permission: String
    ) : PermissionEvent
}
