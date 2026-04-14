import SwiftUI

struct SubscriptionsView: View {
    @EnvironmentObject private var services: ServiceContainer
    @State private var viewModel: SubscriptionsViewModel?
    @State private var showAddSheet = false
    @State private var showSortSheet = false
    @State private var selectedSubscription: Subscription? = nil
    @State private var subscriptionToEdit: Subscription? = nil

    private var vm: SubscriptionsViewModel {
        if let viewModel { return viewModel }
        fatalError("ViewModel not initialized")
    }

    var body: some View {
        NavigationStack {
            Group {
                if let viewModel {
                    subscriptionList(viewModel: viewModel)
                } else {
                    ProgressView()
                }
            }
            .navigationTitle("Subscriptions")
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        showSortSheet = true
                    } label: {
                        Image(systemName: "arrow.up.arrow.down")
                    }
                }
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        showAddSheet = true
                    } label: {
                        Image(systemName: "plus")
                    }
                }
            }
            .sheet(isPresented: $showAddSheet) {
                AddEditSubscriptionView(subscriptionId: nil)
            }
            .sheet(item: $subscriptionToEdit) { sub in
                AddEditSubscriptionView(subscriptionId: sub.id)
            }
            .sheet(isPresented: $showSortSheet) {
                if let viewModel {
                    SortOrderSheet(current: viewModel.uiState.sortOrder) { order in
                        viewModel.onSortOrderChange(order)
                        showSortSheet = false
                    }
                    .presentationDetents([.medium])
                }
            }
            .navigationDestination(item: $selectedSubscription) { sub in
                SubscriptionDetailView(subscriptionId: sub.id)
            }
        }
        .task {
            if viewModel == nil {
                viewModel = SubscriptionsViewModel(
                    subscriptionRepository: services.subscriptionRepository,
                    categoryRepository: services.categoryRepository
                )
            }
        }
    }

    @ViewBuilder
    private func subscriptionList(viewModel: SubscriptionsViewModel) -> some View {
        let state = viewModel.uiState

        VStack(spacing: 0) {
            // Search bar
            HStack {
                Image(systemName: "magnifyingglass")
                    .foregroundColor(.secondary)
                TextField("Search subscriptions…", text: Binding(
                    get: { state.searchQuery },
                    set: { viewModel.onSearchQueryChange($0) }
                ))
                .autocorrectionDisabled()
                if !state.searchQuery.isEmpty {
                    Button {
                        viewModel.onSearchQueryChange("")
                    } label: {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundColor(.secondary)
                    }
                }
            }
            .padding(10)
            .background(Color(.secondarySystemBackground))
            .clipShape(RoundedRectangle(cornerRadius: 10))
            .padding(.horizontal)
            .padding(.top, 8)

            // Filter chips
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    // Status chips
                    ForEach(SubscriptionFilter.allCases, id: \.self) { filter in
                        FilterChip(
                            label: filter.rawValue,
                            isSelected: state.selectedFilter == filter
                        ) {
                            viewModel.onFilterChange(filter)
                        }
                    }

                    Divider()
                        .frame(height: 24)
                        .padding(.horizontal, 4)

                    // Category chips
                    FilterChip(
                        label: "All Categories",
                        isSelected: state.selectedCategoryId == nil
                    ) {
                        viewModel.onCategoryFilterChange(nil)
                    }

                    ForEach(state.availableCategories) { category in
                        FilterChip(
                            label: "\(category.emoji) \(category.displayName)",
                            isSelected: state.selectedCategoryId == category.id
                        ) {
                            viewModel.onCategoryFilterChange(category.id)
                        }
                    }
                }
                .padding(.horizontal)
                .padding(.vertical, 8)
            }

            Divider()

            if state.isLoading {
                Spacer()
                ProgressView()
                Spacer()
            } else if state.filteredSubscriptions.isEmpty {
                emptyState(hasQuery: !state.searchQuery.isEmpty || state.selectedFilter != .all || state.selectedCategoryId != nil)
            } else {
                List {
                    ForEach(state.filteredSubscriptions) { subscription in
                        SubscriptionRowView(
                            subscription: subscription,
                            category: state.availableCategories.first { $0.id == subscription.categoryId }
                        )
                        .contentShape(Rectangle())
                        .onTapGesture {
                            selectedSubscription = subscription
                        }
                        .swipeActions(edge: .trailing, allowsFullSwipe: true) {
                            Button(role: .destructive) {
                                viewModel.deleteSubscription(subscription)
                            } label: {
                                Label("Delete", systemImage: "trash")
                            }
                        }
                        .swipeActions(edge: .leading) {
                            Button {
                                subscriptionToEdit = subscription
                            } label: {
                                Label("Edit", systemImage: "pencil")
                            }
                            .tint(.blue)
                        }
                    }
                }
                .listStyle(.plain)
            }
        }
    }

    @ViewBuilder
    private func emptyState(hasQuery: Bool) -> some View {
        Spacer()
        VStack(spacing: 12) {
            Image(systemName: hasQuery ? "magnifyingglass" : "creditcard.and.123")
                .font(.system(size: 48))
                .foregroundColor(.secondary)
            Text(hasQuery ? "No matching subscriptions" : "No subscriptions yet")
                .font(.headline)
            Text(hasQuery ? "Try adjusting your filters." : "Tap + to add your first subscription.")
                .font(.subheadline)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
        }
        .padding()
        Spacer()
    }
}

// MARK: - Filter Chip

private struct FilterChip: View {
    let label: String
    let isSelected: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(label)
                .font(.subheadline)
                .fontWeight(isSelected ? .semibold : .regular)
                .padding(.horizontal, 12)
                .padding(.vertical, 6)
                .background(isSelected ? Color.accentColor : Color(.secondarySystemBackground))
                .foregroundColor(isSelected ? .white : .primary)
                .clipShape(Capsule())
        }
        .buttonStyle(.plain)
    }
}

// MARK: - Subscription Row

struct SubscriptionRowView: View {
    let subscription: Subscription
    let category: Category?

    private static let currencyFormatter: NumberFormatter = {
        let f = NumberFormatter()
        f.numberStyle = .currency
        return f
    }()

    private static let dateFormatter: DateFormatter = {
        let f = DateFormatter()
        f.dateStyle = .medium
        f.timeStyle = .none
        return f
    }()

    var body: some View {
        HStack(spacing: 12) {
            // Category emoji badge
            Text(category?.emoji ?? "📦")
                .font(.title2)
                .frame(width: 44, height: 44)
                .background(Color(.secondarySystemBackground))
                .clipShape(RoundedRectangle(cornerRadius: 10))

            VStack(alignment: .leading, spacing: 2) {
                Text(subscription.name)
                    .font(.body)
                    .fontWeight(.medium)
                    .lineLimit(1)

                Text(category?.displayName ?? "Other")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }

            Spacer()

            VStack(alignment: .trailing, spacing: 2) {
                Text(formatAmount(subscription.amount, currency: subscription.currency))
                    .font(.body)
                    .fontWeight(.semibold)

                Text(subscription.frequency.displayName)
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
        }
        .padding(.vertical, 4)
        .opacity(subscription.isActive ? 1.0 : 0.5)
    }

    private func formatAmount(_ amount: Double, currency: String) -> String {
        let formatter = Self.currencyFormatter
        formatter.currencyCode = currency
        return formatter.string(from: NSNumber(value: amount)) ?? "\(currency) \(amount)"
    }
}

// MARK: - Sort Order Sheet

private struct SortOrderSheet: View {
    let current: SortOrder
    let onSelect: (SortOrder) -> Void

    var body: some View {
        NavigationStack {
            List(SortOrder.allCases, id: \.self) { order in
                Button {
                    onSelect(order)
                } label: {
                    HStack {
                        Text(order.rawValue)
                            .foregroundColor(.primary)
                        Spacer()
                        if order == current {
                            Image(systemName: "checkmark")
                                .foregroundColor(.accentColor)
                        }
                    }
                }
            }
            .navigationTitle("Sort By")
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}

#Preview {
    SubscriptionsView()
        .environmentObject(ServiceContainer())
}
