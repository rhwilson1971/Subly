import SwiftUI
import Charts

struct HomeView: View {
    @EnvironmentObject private var services: ServiceContainer
    @State private var viewModel: HomeViewModel?

    var body: some View {
        NavigationStack {
            Group {
                if let viewModel {
                    dashboardContent(viewModel: viewModel)
                } else {
                    ProgressView()
                }
            }
            .navigationTitle("Dashboard")
        }
        .task {
            if viewModel == nil {
                viewModel = HomeViewModel(
                    getUpcomingSubscriptions: services.getUpcomingSubscriptions,
                    getSubscriptionStats: services.getSubscriptionStats,
                    getCategorySpend: services.getCategorySpend
                )
            }
        }
    }

    @ViewBuilder
    private func dashboardContent(viewModel: HomeViewModel) -> some View {
        let state = viewModel.uiState

        ScrollView {
            LazyVStack(spacing: 20) {
                if state.isLoading {
                    ProgressView()
                        .padding(.top, 60)
                } else if state.stats.activeCount == 0 {
                    emptyState
                } else {
                    // Summary cards
                    summarySection(stats: state.stats)

                    // Upcoming bills
                    if !state.upcomingSubscriptions.isEmpty {
                        upcomingSection(subscriptions: state.upcomingSubscriptions)
                    }

                    // Category breakdown chart
                    if !state.categorySpend.isEmpty {
                        categoryChartSection(categorySpend: state.categorySpend)
                    }
                }
            }
            .padding(.horizontal)
            .padding(.top, 8)
            .padding(.bottom, 24)
        }
        .refreshable {
            viewModel.refresh()
        }
    }

    // MARK: - Empty State

    private var emptyState: some View {
        VStack(spacing: 16) {
            Image(systemName: "creditcard.and.123")
                .font(.system(size: 56))
                .foregroundColor(.secondary)
            Text("No subscriptions yet")
                .font(.title3)
                .fontWeight(.semibold)
            Text("Add your first subscription to see your dashboard.")
                .font(.subheadline)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
        }
        .padding(.top, 60)
        .padding(.horizontal, 32)
    }

    // MARK: - Summary Cards

    @ViewBuilder
    private func summarySection(stats: SubscriptionStats) -> some View {
        VStack(alignment: .leading, spacing: 10) {
            Text("Overview")
                .font(.headline)

            HStack(spacing: 12) {
                SummaryCard(
                    title: "Monthly",
                    value: formatCurrency(stats.totalMonthly),
                    icon: "calendar",
                    color: .blue
                )
                SummaryCard(
                    title: "Yearly",
                    value: formatCurrency(stats.totalYearly),
                    icon: "calendar.badge.clock",
                    color: .purple
                )
                SummaryCard(
                    title: "Active",
                    value: "\(stats.activeCount)",
                    icon: "checkmark.circle",
                    color: .green
                )
            }
        }
    }

    // MARK: - Upcoming Bills

    @ViewBuilder
    private func upcomingSection(subscriptions: [Subscription]) -> some View {
        VStack(alignment: .leading, spacing: 10) {
            Text("Upcoming Bills")
                .font(.headline)

            VStack(spacing: 0) {
                ForEach(Array(subscriptions.enumerated()), id: \.element.id) { index, sub in
                    UpcomingBillRow(subscription: sub)
                    if index < subscriptions.count - 1 {
                        Divider().padding(.leading, 16)
                    }
                }
            }
            .background(Color(.secondarySystemBackground))
            .clipShape(RoundedRectangle(cornerRadius: 12))
        }
    }

    // MARK: - Category Chart

    @ViewBuilder
    private func categoryChartSection(categorySpend: [CategorySpend]) -> some View {
        VStack(alignment: .leading, spacing: 10) {
            Text("Spending by Category")
                .font(.headline)

            VStack(spacing: 16) {
                // Donut chart
                Chart(categorySpend) { item in
                    SectorMark(
                        angle: .value("Amount", item.monthlyAmount),
                        innerRadius: .ratio(0.6),
                        angularInset: 1.5
                    )
                    .foregroundStyle(by: .value("Category", item.category.displayName))
                    .cornerRadius(4)
                }
                .chartLegend(.hidden)
                .frame(height: 200)

                // Legend list
                VStack(spacing: 0) {
                    ForEach(Array(categorySpend.enumerated()), id: \.element.id) { index, spend in
                        CategorySpendRow(spend: spend)
                        if index < categorySpend.count - 1 {
                            Divider().padding(.leading, 16)
                        }
                    }
                }
            }
            .padding()
            .background(Color(.secondarySystemBackground))
            .clipShape(RoundedRectangle(cornerRadius: 12))
        }
    }

    // MARK: - Helpers

    private func formatCurrency(_ amount: Double) -> String {
        let formatter = NumberFormatter()
        formatter.numberStyle = .currency
        formatter.maximumFractionDigits = 0
        return formatter.string(from: NSNumber(value: amount)) ?? "$\(Int(amount))"
    }
}

// MARK: - Summary Card

private struct SummaryCard: View {
    let title: String
    let value: String
    let icon: String
    let color: Color

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack {
                Image(systemName: icon)
                    .font(.caption)
                    .foregroundColor(color)
                Spacer()
            }
            Text(value)
                .font(.title3)
                .fontWeight(.bold)
                .minimumScaleFactor(0.7)
                .lineLimit(1)
            Text(title)
                .font(.caption)
                .foregroundColor(.secondary)
        }
        .padding(12)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(.secondarySystemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}

// MARK: - Upcoming Bill Row

private struct UpcomingBillRow: View {
    let subscription: Subscription

    private static let dateFormatter: DateFormatter = {
        let f = DateFormatter()
        f.dateStyle = .medium
        f.timeStyle = .none
        return f
    }()

    private var daysUntil: Int {
        Calendar.current.dateComponents([.day], from: Date(), to: subscription.nextBillingDate).day ?? 0
    }

    private var dueLabel: String {
        switch daysUntil {
        case 0:       return "Today"
        case 1:       return "Tomorrow"
        case let d:   return "In \(d) days"
        }
    }

    private var dueColor: Color {
        switch daysUntil {
        case 0...2:  return .red
        case 3...7:  return .orange
        default:     return .secondary
        }
    }

    var body: some View {
        HStack(spacing: 12) {
            VStack(alignment: .leading, spacing: 2) {
                Text(subscription.name)
                    .font(.subheadline)
                    .fontWeight(.medium)
                Text(Self.dateFormatter.string(from: subscription.nextBillingDate))
                    .font(.caption)
                    .foregroundColor(.secondary)
            }

            Spacer()

            VStack(alignment: .trailing, spacing: 2) {
                Text(String(format: "%@ %.2f", subscription.currency, subscription.amount))
                    .font(.subheadline)
                    .fontWeight(.semibold)
                Text(dueLabel)
                    .font(.caption)
                    .foregroundColor(dueColor)
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
    }
}

// MARK: - Category Spend Row

private struct CategorySpendRow: View {
    let spend: CategorySpend

    var body: some View {
        HStack(spacing: 12) {
            Text(spend.category.emoji)
                .font(.title3)

            Text(spend.category.displayName)
                .font(.subheadline)

            Spacer()

            VStack(alignment: .trailing, spacing: 2) {
                Text(String(format: "$%.2f", spend.monthlyAmount))
                    .font(.subheadline)
                    .fontWeight(.medium)
                Text(String(format: "%.0f%%", spend.percentage * 100))
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
        }
        .padding(.horizontal, 4)
        .padding(.vertical, 8)
    }
}

#Preview {
    HomeView()
        .environmentObject(ServiceContainer())
}
