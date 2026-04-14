import Foundation
import UserNotifications

/// Schedules and cancels daily morning/evening UNCalendarNotificationTriggers.
/// Mirrors Android's NotificationScheduler + WorkManager setup.
@MainActor
final class NotificationScheduler {
    static let shared = NotificationScheduler()

    private let center = UNUserNotificationCenter.current()

    private enum ID {
        static let morning = "subly.reminder.morning"
        static let evening = "subly.reminder.evening"
    }

    private init() {}

    // MARK: - Permission

    func requestAuthorization() async -> Bool {
        do {
            return try await center.requestAuthorization(options: [.alert, .sound, .badge])
        } catch {
            return false
        }
    }

    func isAuthorized() async -> Bool {
        let settings = await center.notificationSettings()
        return settings.authorizationStatus == .authorized
    }

    // MARK: - Schedule

    /// Schedule morning and/or evening daily reminders.
    /// - Parameters:
    ///   - morningTime: "HH:mm" string, e.g. "09:00"
    ///   - eveningTime: "HH:mm" string, e.g. "18:00"
    ///   - morningEnabled: whether to schedule the morning reminder
    ///   - eveningEnabled: whether to schedule the evening reminder
    func scheduleDailyReminders(
        morningTime: String,
        eveningTime: String,
        morningEnabled: Bool = true,
        eveningEnabled: Bool = true
    ) {
        if morningEnabled {
            scheduleReminder(id: ID.morning, timeString: morningTime, label: "Morning")
        } else {
            center.removePendingNotificationRequests(withIdentifiers: [ID.morning])
        }

        if eveningEnabled {
            scheduleReminder(id: ID.evening, timeString: eveningTime, label: "Evening")
        } else {
            center.removePendingNotificationRequests(withIdentifiers: [ID.evening])
        }
    }

    func cancelReminders() {
        center.removePendingNotificationRequests(withIdentifiers: [ID.morning, ID.evening])
    }

    // MARK: - Private

    private func scheduleReminder(id: String, timeString: String, label: String) {
        let parts = timeString.split(separator: ":").compactMap { Int($0) }
        guard parts.count == 2 else { return }
        let hour = parts[0], minute = parts[1]

        // Remove existing before re-adding (update scenario)
        center.removePendingNotificationRequests(withIdentifiers: [id])

        let content = UNMutableNotificationContent()
        content.title = "Subly Reminder"
        content.body = "Check your upcoming subscriptions."
        content.sound = .default

        var dateComponents = DateComponents()
        dateComponents.hour = hour
        dateComponents.minute = minute

        let trigger = UNCalendarNotificationTrigger(dateMatching: dateComponents, repeats: true)
        let request = UNNotificationRequest(identifier: id, content: content, trigger: trigger)

        center.add(request) { error in
            if let error { print("NotificationScheduler: failed to schedule \(label) — \(error)") }
        }
    }
}
