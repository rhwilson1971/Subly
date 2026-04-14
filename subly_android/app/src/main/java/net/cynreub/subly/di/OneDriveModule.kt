package net.cynreub.subly.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Feature-boundary Hilt module for OneDrive / MSAL. All bindings use @Inject constructors. */
@Module
@InstallIn(SingletonComponent::class)
object OneDriveModule
