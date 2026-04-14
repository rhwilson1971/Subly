package net.cynreub.subly.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt module for Dropbox integration.
 * DropboxAuthManager and DropboxSyncProvider are @Singleton with @Inject constructors
 * and are provided automatically by Hilt.
 */
@Module
@InstallIn(SingletonComponent::class)
object DropboxModule
