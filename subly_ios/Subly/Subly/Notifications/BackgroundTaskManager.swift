import Foundation
import BackgroundTasks

/// Registers and handles BGAppRefreshTask for daily subscription reminder checks.
/// Mirrors Android's WorkManager periodic work registration in Application.onCreate().
///
/// Usage: call `BackgroundTaskManager.register()` once at app startup (in SublyApp.init),
/// then call `scheduleAppRefresh()` after each task fires and on first launch.
@MainActor
final class BackgroundTaskManager {
    static let shared = BackgroundTaskManager()

    private static let taskIdentifier = "net.cynreub.subly.reminder-check"

    private var reminderChecker: ReminderChecker?

    private init() {}

    func configure(subscriptionRepository: SubscriptionRepository) {
        reminderChecker = ReminderChecker(subscriptionRepository: subscriptionRepository)
    }

    /// Call once during app init (before the first runloop pass).
    nonisolated static func register() {
        BGTaskScheduler.shared.register(
            forTaskWithIdentifier: taskIdentifier,
            using: nil
        ) { task in
            guard let refreshTask = task as? BGAppRefreshTask else { return }
            Task { @MainActor in
                await BackgroundTaskManager.shared.handleAppRefresh(task: refreshTask)
            }
        }
    }

    /// Schedule the next background refresh. Call after handling a task and at app launch.
    func scheduleAppRefresh() {
        let request = BGAppRefreshTaskRequest(identifier: Self.taskIdentifier)
        // Earliest next fire: 20 hours from now (system may delay further)
        request.earliestBeginDate = Date(timeIntervalSinceNow: 20 * 3600)
        try? BGTaskScheduler.shared.submit(request)
    }

    // MARK: - Handler

    private func handleAppRefresh(task: BGAppRefreshTask) async {
        // Schedule the next refresh immediately so the chain continues
        scheduleAppRefresh()

        let prefs = PreferencesManager.shared
        guard prefs.notificationsEnabled else {
            task.setTaskCompleted(success: true)
            return
        }

        task.expirationHandler = { task.setTaskCompleted(success: false) }

        await reminderChecker?.checkAndNotify(reminderDaysBefore: prefs.reminderDaysBefore)
        task.setTaskCompleted(success: true)
    }
}
