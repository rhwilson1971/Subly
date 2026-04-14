import Foundation
import UserNotifications
import Combine

/// Fetches upcoming subscriptions and fires per-subscription UNNotifications.
/// Called by BGAppRefreshTask and on-demand from the scheduler.
/// Mirrors Android's ReminderWorker.doWork().
final class ReminderChecker {
    private let subscriptionRepository: SubscriptionRepository
    private let center = UNUserNotificationCenter.current()
    private var cancellables = Set<AnyCancellable>()

    init(subscriptionRepository: SubscriptionRepository) {
        self.subscriptionRepository = subscriptionRepository
    }

    /// Check upcoming subscriptions and send notifications for those within their reminder window.
    func checkAndNotify(reminderDaysBefore: Int) async {
        let settings = await center.notificationSettings()
        guard settings.authorizationStatus == .authorized else { return }

        let targetDate = Calendar.current.date(byAdding: .day, value: 7, to: Date()) ?? Date()

        return await withCheckedContinuation { continuation in
            subscriptionRepository.getUpcomingSubscriptions(before: targetDate)
                .first()
                .sink { [weak self] subscriptions in
                    guard let self else { continuation.resume(); return }
                    let toNotify = subscriptions.filter { sub in
                        guard sub.isActive else { return false }
                        let days = Calendar.current.dateComponents([.day], from: Date(), to: sub.nextBillingDate).day ?? Int.max
                        return days >= 0 && days <= sub.reminderDaysBefore
                    }
                    Task {
                        await self.sendNotifications(for: toNotify)
                        continuation.resume()
                    }
                }
                .store(in: &cancellables)
        }
    }

    // MARK: - Notifications

    private func sendNotifications(for subscriptions: [Subscription]) async {
        // Remove previous reminder notifications before re-adding
        let existingIds = subscriptions.map { notificationId(for: $0) }
        center.removePendingNotificationRequests(withIdentifiers: existingIds)

        for subscription in subscriptions {
            await sendNotification(for: subscription)
        }

        if subscriptions.count > 1 {
            await sendGroupSummary(subscriptions: subscriptions)
        }
    }

    private func sendNotification(for subscription: Subscription) async {
        let days = Calendar.current.dateComponents([.day], from: Date(), to: subscription.nextBillingDate).day ?? 0

        let content = UNMutableNotificationContent()
        content.title = subscription.name
        content.body = dueBody(amount: subscription.amount, currency: subscription.currency, daysUntil: days)
        content.sound = .default
        content.threadIdentifier = "subly.reminders"

        // Fire immediately (the daily trigger already fired at the right time)
        let request = UNNotificationRequest(
            identifier: notificationId(for: subscription),
            content: content,
            trigger: nil
        )
        try? await center.add(request)
    }

    private func sendGroupSummary(subscriptions: [Subscription]) async {
        let total = subscriptions.reduce(0.0) { $0 + $1.monthlyAmount }
        let content = UNMutableNotificationContent()
        content.title = "Upcoming Subscriptions"
        content.body = "\(subscriptions.count) subscriptions due soon — \(formatCurrency(total))/mo total"
        content.sound = nil
        content.threadIdentifier = "subly.reminders"

        let request = UNNotificationRequest(
            identifier: "subly.reminder.group",
            content: content,
            trigger: nil
        )
        try? await center.add(request)
    }

    // MARK: - Helpers

    private func notificationId(for subscription: Subscription) -> String {
        "subly.reminder.\(subscription.id.uuidString)"
    }

    private func dueBody(amount: Double, currency: String, daysUntil: Int) -> String {
        let amountStr = formatCurrency(amount, currency: currency)
        switch daysUntil {
        case 0:  return "\(amountStr) due today"
        case 1:  return "\(amountStr) due tomorrow"
        default: return "\(amountStr) due in \(daysUntil) days"
        }
    }

    private func formatCurrency(_ amount: Double, currency: String = "USD") -> String {
        let formatter = NumberFormatter()
        formatter.numberStyle = .currency
        formatter.currencyCode = currency
        formatter.maximumFractionDigits = 2
        return formatter.string(from: NSNumber(value: amount)) ?? "\(currency) \(amount)"
    }
}
