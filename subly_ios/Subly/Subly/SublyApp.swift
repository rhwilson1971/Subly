//
//  SublyApp.swift
//  Subly
//
//  Created by Dr Reuben Wilson on 4/13/26.
//

import SwiftUI

@main
struct SublyApp: App {
    @StateObject private var services = ServiceContainer()
    @StateObject private var prefs = PreferencesManager.shared

    init() {
        // Register BGAppRefreshTask identifier before the first runloop pass
        BackgroundTaskManager.register()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(services)
                .environmentObject(prefs)
                .preferredColorScheme(prefs.theme.colorScheme)
                .task {
                    await onLaunch()
                }
        }
        .modelContainer(services.modelContainer)
    }

    @MainActor
    private func onLaunch() async {
        // Wire background task manager with the subscription repository
        BackgroundTaskManager.shared.configure(subscriptionRepository: services.subscriptionRepository)

        // Request notification permission on first launch (no-op if already decided)
        let granted = await NotificationScheduler.shared.requestAuthorization()

        // Schedule daily reminders if permission is granted and notifications are enabled
        if granted && prefs.notificationsEnabled {
            NotificationScheduler.shared.scheduleDailyReminders(
                morningTime: prefs.morningReminderTime,
                eveningTime: prefs.eveningReminderTime,
                morningEnabled: prefs.morningReminderEnabled,
                eveningEnabled: prefs.eveningReminderEnabled
            )
        }

        // Queue first background refresh
        BackgroundTaskManager.shared.scheduleAppRefresh()
    }
}
