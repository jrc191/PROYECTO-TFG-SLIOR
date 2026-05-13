package com.slior.di

import com.slior.util.NotificationHelper
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface NotificationEntryPoint {
    fun notificationHelper(): NotificationHelper
}
