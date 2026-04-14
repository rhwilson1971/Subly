package net.cynreub.subly.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt module for Google Drive integration.
 * GoogleDriveAuthManager, GoogleDriveClient, and GoogleDriveSyncProvider are all
 * @Singleton with @Inject constructors and are provided automatically by Hilt.
 * This module exists as a feature-boundary marker and for future Drive-specific bindings.
 */
@Module
@InstallIn(SingletonComponent::class)
object GoogleDriveModule
