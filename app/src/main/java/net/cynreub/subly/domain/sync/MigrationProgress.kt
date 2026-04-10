package net.cynreub.subly.domain.sync

sealed class MigrationProgress {
    data object Idle : MigrationProgress()
    data class Running(val step: Int, val total: Int) : MigrationProgress()
    data object Success : MigrationProgress()
    data class Failure(val cause: Throwable) : MigrationProgress()
}
