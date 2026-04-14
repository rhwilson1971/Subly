import SwiftUI

struct AddEditPaymentMethodView: View {
    @EnvironmentObject private var services: ServiceContainer
    @Environment(\.dismiss) private var dismiss

    let paymentMethodId: UUID?

    @State private var viewModel: AddEditPaymentMethodViewModel?

    var body: some View {
        NavigationStack {
            Group {
                if let viewModel {
                    formContent(viewModel: viewModel)
                } else {
                    ProgressView()
                }
            }
            .navigationTitle(paymentMethodId == nil ? "Add Payment Method" : "Edit Payment Method")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    if let viewModel {
                        Button("Save") {
                            viewModel.onSaveClick { dismiss() }
                        }
                        .disabled(viewModel.uiState.isSaving)
                    }
                }
            }
        }
        .task {
            if viewModel == nil {
                viewModel = AddEditPaymentMethodViewModel(
                    paymentMethodRepository: services.paymentMethodRepository,
                    paymentMethodId: paymentMethodId
                )
            }
        }
    }

    @ViewBuilder
    private func formContent(viewModel: AddEditPaymentMethodViewModel) -> some View {
        let state = viewModel.uiState

        Form {
            Section("Details") {
                VStack(alignment: .leading, spacing: 4) {
                    TextField("Nickname (e.g. US Bank Rewards)", text: Binding(
                        get: { state.nickname },
                        set: { viewModel.onNicknameChange($0) }
                    ))
                    if let err = state.nicknameError {
                        Text(err).font(.caption).foregroundColor(.red)
                    }
                }

                Button {
                    viewModel.uiState.showTypePicker = true
                } label: {
                    HStack {
                        Label(state.selectedType.displayName, systemImage: state.selectedType.sfSymbol)
                            .foregroundColor(.primary)
                        Spacer()
                        Image(systemName: "chevron.right")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }
                .buttonStyle(.plain)
            }

            Section("Optional") {
                VStack(alignment: .leading, spacing: 4) {
                    TextField("Last 4 digits", text: Binding(
                        get: { state.lastFourDigits },
                        set: { viewModel.onLastFourChange($0) }
                    ))
                    .keyboardType(.numberPad)
                    if let err = state.lastFourDigitsError {
                        Text(err).font(.caption).foregroundColor(.red)
                    }
                }
            }

            if let error = state.error {
                Section {
                    Text(error).font(.footnote).foregroundColor(.red)
                }
            }
        }
        .sheet(isPresented: Binding(
            get: { viewModel.uiState.showTypePicker },
            set: { viewModel.uiState.showTypePicker = $0 }
        )) {
            PaymentTypePickerSheet(
                selected: state.selectedType,
                onSelect: { viewModel.onTypeSelected($0) }
            )
            .presentationDetents([.medium, .large])
        }
    }
}

// MARK: - Type Picker Sheet

private struct PaymentTypePickerSheet: View {
    let selected: PaymentType
    let onSelect: (PaymentType) -> Void
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            List(PaymentType.allCases, id: \.self) { type in
                Button {
                    onSelect(type)
                    dismiss()
                } label: {
                    HStack(spacing: 12) {
                        Image(systemName: type.sfSymbol)
                            .foregroundColor(.accentColor)
                            .frame(width: 24)
                        Text(type.displayName)
                            .foregroundColor(.primary)
                        Spacer()
                        if type == selected {
                            Image(systemName: "checkmark")
                                .foregroundColor(.accentColor)
                        }
                    }
                }
            }
            .navigationTitle("Payment Type")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
            }
        }
    }
}

#Preview {
    AddEditPaymentMethodView(paymentMethodId: nil)
        .environmentObject(ServiceContainer())
}
