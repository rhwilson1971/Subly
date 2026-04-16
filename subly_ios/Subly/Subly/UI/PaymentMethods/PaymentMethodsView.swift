import SwiftUI

struct PaymentMethodsView: View {
    @EnvironmentObject private var services: ServiceContainer
    @State private var viewModel: PaymentMethodsViewModel?
    @State private var showAddSheet = false
    @State private var methodToEdit: PaymentMethod? = nil

    var body: some View {
        NavigationStack {
            Group {
                if let viewModel {
                    content(viewModel: viewModel)
                } else {
                    ProgressView()
                }
            }
            .navigationTitle("Payment Methods")
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button { showAddSheet = true } label: {
                        Image(systemName: "plus")
                    }
                }
            }
            .sheet(isPresented: $showAddSheet) {
                AddEditPaymentMethodView(paymentMethodId: nil)
            }
            .sheet(item: $methodToEdit) { method in
                AddEditPaymentMethodView(paymentMethodId: method.id)
            }
        }
        .task {
            if viewModel == nil {
                viewModel = PaymentMethodsViewModel(paymentMethodRepository: services.paymentMethodRepository)
            }
        }
    }

    @ViewBuilder
    private func content(viewModel: PaymentMethodsViewModel) -> some View {
        let state = viewModel.uiState

        Group {
            if state.isLoading {
                ProgressView()
            } else if state.paymentMethods.isEmpty {
                emptyState
            } else {
                List {
                    ForEach(state.paymentMethods) { item in
                        PaymentMethodRow(item: item)
                            .contentShape(Rectangle())
                            .onTapGesture { methodToEdit = item.paymentMethod }
                            .swipeActions(edge: .trailing, allowsFullSwipe: false) {
                                Button(role: .destructive) {
                                    viewModel.requestDelete(item)
                                } label: {
                                    Label("Delete", systemImage: "trash")
                                }
                            }
                            .swipeActions(edge: .leading) {
                                Button {
                                    methodToEdit = item.paymentMethod
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
        .confirmationDialog(
            "Delete Payment Method",
            isPresented: Binding(
                get: { viewModel.uiState.showDeleteConfirmation },
                set: { if !$0 { viewModel.dismissDelete() } }
            ),
            titleVisibility: .visible
        ) {
            Button("Delete", role: .destructive) { viewModel.confirmDelete() }
            Button("Cancel", role: .cancel) { viewModel.dismissDelete() }
        } message: {
            if let m = state.deletingMethod {
                                
                let nickname = m.paymentMethod.nickname.description
                
                Text("Delete \(nickname)? This cannot be undone.")
            }
        }
        .alert("Error", isPresented: Binding(
            get: { state.error != nil },
            set: { if !$0 { viewModel.clearError() } }
        )) {
            Button("OK", role: .cancel) { viewModel.clearError() }
        } message: {
            Text(state.error ?? "")
        }
    }

    private var emptyState: some View {
        VStack(spacing: 12) {
            Image(systemName: "creditcard")
                .font(.system(size: 48))
                .foregroundColor(.secondary)
            Text("No Payment Methods")
                .font(.headline)
            Text("Tap + to add your first payment method.")
                .font(.subheadline)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
        }
        .padding()
    }
}

// MARK: - Row

private struct PaymentMethodRow: View {
    let item: PaymentMethodWithUsage

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: item.paymentMethod.type.sfSymbol)
                .font(.title2)
                .foregroundColor(.accentColor)
                .frame(width: 44, height: 44)
                .background(Color(.secondarySystemBackground))
                .clipShape(RoundedRectangle(cornerRadius: 10))

            VStack(alignment: .leading, spacing: 2) {
                Text(item.paymentMethod.nickname)
                    .font(.body)
                    .fontWeight(.medium)

                HStack(spacing: 4) {
                    Text(item.paymentMethod.type.displayName)
                        .font(.caption)
                        .foregroundColor(.secondary)
                    if let last4 = item.paymentMethod.lastFourDigits {
                        Text("••••\(last4)")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }
            }

            Spacer()

            if item.subscriptionCount > 0 {
                Text("\(item.subscriptionCount)")
                    .font(.caption)
                    .fontWeight(.semibold)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(Color.accentColor.opacity(0.1))
                    .foregroundColor(.accentColor)
                    .clipShape(Capsule())
            }
        }
        .padding(.vertical, 4)
    }
}

#Preview {
    PaymentMethodsView()
        .environmentObject(ServiceContainer())
}
