import SwiftUI

struct SubscriptionDetailView: View {
    @EnvironmentObject private var services: ServiceContainer
    @Environment(\.dismiss) private var dismiss

    let subscriptionId: UUID

    @State private var viewModel: SubscriptionDetailViewModel?
    @State private var showEditSheet = false

    var body: some View {
        Group {
            if let viewModel {
                detailContent(viewModel: viewModel)
            } else {
                ProgressView()
            }
        }
        .task {
            if viewModel == nil {
                viewModel = SubscriptionDetailViewModel(
                    subscriptionId: subscriptionId,
                    subscriptionRepository: services.subscriptionRepository,
                    categoryRepository: services.categoryRepository,
                    paymentMethodRepository: services.paymentMethodRepository
                )
            }
        }
    }

    @ViewBuilder
    private func detailContent(viewModel: SubscriptionDetailViewModel) -> some View {
        let state = viewModel.uiState

        Group {
            if state.isLoading {
                ProgressView()
            } else if let subscription = state.subscription {
                ScrollView {
                    VStack(spacing: 20) {
                        // Hero card
                        heroCard(subscription: subscription, category: state.category)

                        // Details
                        detailsCard(subscription: subscription, paymentMethod: state.paymentMethod)

                        // Actions
                        actionsCard(viewModel: viewModel, subscription: subscription)
                    }
                    .padding()
                }
            } else {
                ContentUnavailableView("Not Found", systemImage: "creditcard.slash", description: Text("This subscription could not be loaded."))
            }
        }
        .navigationTitle(state.subscription?.name ?? "Details")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button("Edit") { showEditSheet = true }
                    .disabled(state.subscription == nil)
            }
        }
        .sheet(isPresented: $showEditSheet) {
            AddEditSubscriptionView(subscriptionId: subscriptionId)
        }
        .confirmationDialog(
            "Delete Subscription",
            isPresented: Binding(
                get: { viewModel.uiState.showDeleteConfirmation },
                set: { viewModel.uiState.showDeleteConfirmation = $0 }
            ),
            titleVisibility: .visible
        ) {
            Button("Delete", role: .destructive) {
                viewModel.deleteSubscription { dismiss() }
            }
            Button("Cancel", role: .cancel) {}
        } message: {
            Text("This action cannot be undone.")
        }
        .alert("Error", isPresented: Binding(
            get: { state.error != nil },
            set: { if !$0 { viewModel.uiState.error = nil } }
        )) {
            Button("OK", role: .cancel) { viewModel.uiState.error = nil }
        } message: {
            Text(state.error ?? "")
        }
    }

    // MARK: - Hero Card

    @ViewBuilder
    private func heroCard(subscription: Subscription, category: Category?) -> some View {
        VStack(spacing: 8) {
            Text(category?.emoji ?? "📦")
                .font(.system(size: 56))
                .padding(.top, 8)

            Text(subscription.name)
                .font(.title2)
                .fontWeight(.bold)
                .multilineTextAlignment(.center)

            Text(category?.displayName ?? "Other")
                .font(.subheadline)
                .foregroundColor(.secondary)

            HStack(alignment: .firstTextBaseline, spacing: 2) {
                Text(subscription.currency)
                    .font(.headline)
                    .foregroundColor(.secondary)
                Text(String(format: "%.2f", subscription.amount))
                    .font(.system(size: 40, weight: .bold, design: .rounded))
                Text("/ \(subscription.frequency.displayName.lowercased())")
                    .font(.subheadline)
                    .foregroundColor(.secondary)
                    .padding(.bottom, 4)
            }
            .padding(.top, 4)

            if !subscription.isActive {
                Label("Inactive", systemImage: "pause.circle.fill")
                    .font(.caption)
                    .foregroundColor(.orange)
                    .padding(.horizontal, 12)
                    .padding(.vertical, 4)
                    .background(Color.orange.opacity(0.1))
                    .clipShape(Capsule())
            }
        }
        .frame(maxWidth: .infinity)
        .padding()
        .background(Color(.secondarySystemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }

    // MARK: - Details Card

    @ViewBuilder
    private func detailsCard(subscription: Subscription, paymentMethod: PaymentMethod?) -> some View {
        VStack(alignment: .leading, spacing: 0) {
            Text("Details")
                .font(.headline)
                .padding(.horizontal)
                .padding(.top, 16)
                .padding(.bottom, 8)

            Divider()

            detailRow(label: "Next Billing", value: subscription.nextBillingDate.formatted(date: .medium, time: .omitted))
            Divider().padding(.leading)

            detailRow(label: "Start Date", value: subscription.startDate.formatted(date: .medium, time: .omitted))
            Divider().padding(.leading)

            detailRow(label: "Frequency", value: subscription.frequency.displayName)
            Divider().padding(.leading)

            detailRow(label: "Monthly Cost", value: String(format: "%@ %.2f", subscription.currency, subscription.monthlyAmount))
            Divider().padding(.leading)

            detailRow(label: "Payment Method", value: paymentMethod?.nickname ?? "Not set")
            Divider().padding(.leading)

            detailRow(label: "Reminder", value: "\(subscription.reminderDaysBefore) day\(subscription.reminderDaysBefore == 1 ? "" : "s") before")

            if let notes = subscription.notes, !notes.isEmpty {
                Divider().padding(.leading)
                VStack(alignment: .leading, spacing: 4) {
                    Text("Notes")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                    Text(notes)
                        .font(.body)
                }
                .padding(.horizontal)
                .padding(.vertical, 12)
            }
        }
        .background(Color(.secondarySystemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }

    private func detailRow(label: String, value: String) -> some View {
        HStack {
            Text(label)
                .foregroundColor(.secondary)
            Spacer()
            Text(value)
                .fontWeight(.medium)
        }
        .padding(.horizontal)
        .padding(.vertical, 12)
    }

    // MARK: - Actions Card

    @ViewBuilder
    private func actionsCard(viewModel: SubscriptionDetailViewModel, subscription: Subscription) -> some View {
        VStack(spacing: 0) {
            Button {
                viewModel.toggleActive()
            } label: {
                HStack {
                    Label(
                        subscription.isActive ? "Deactivate Subscription" : "Activate Subscription",
                        systemImage: subscription.isActive ? "pause.circle" : "play.circle"
                    )
                    .foregroundColor(subscription.isActive ? .orange : .green)
                    Spacer()
                    if viewModel.uiState.isTogglingActive {
                        ProgressView()
                    }
                }
                .padding()
            }

            Divider().padding(.leading)

            Button(role: .destructive) {
                viewModel.uiState.showDeleteConfirmation = true
            } label: {
                HStack {
                    Label("Delete Subscription", systemImage: "trash")
                    Spacer()
                    if viewModel.uiState.isDeleting {
                        ProgressView()
                    }
                }
                .padding()
            }
        }
        .background(Color(.secondarySystemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }
}

#Preview {
    NavigationStack {
        SubscriptionDetailView(subscriptionId: UUID())
            .environmentObject(ServiceContainer())
    }
}
