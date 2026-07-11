import SwiftUI

struct AddEditSubscriptionView: View {
    @EnvironmentObject private var services: ServiceContainer
    @Environment(\.dismiss) private var dismiss

    let subscriptionId: UUID?

    @State private var viewModel: AddEditSubscriptionViewModel?

    private var vm: AddEditSubscriptionViewModel {
        viewModel! // only accessed after .task
    }

    var body: some View {
        NavigationStack {
            Group {
                if let viewModel {
                    AddEditSubscriptionFormView(viewModel: viewModel)
                } else {
                    ProgressView()
                }
            }
            .navigationTitle(subscriptionId == nil ? "Add Subscription" : "Edit Subscription")
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
                viewModel = AddEditSubscriptionViewModel(
                    subscriptionRepository: services.subscriptionRepository,
                    paymentMethodRepository: services.paymentMethodRepository,
                    categoryRepository: services.categoryRepository,
                    subscriptionId: subscriptionId
                )
            }
        }
    }
}

// MARK: - Form Content

/// Split out from `AddEditSubscriptionView` so each section/sheet is its own
/// small, independently type-checked expression. The previous single
/// `Form { ... }` with six sections and five chained `.sheet` modifiers was
/// too large for the Swift type-checker to diagnose reliably.
private struct AddEditSubscriptionFormView: View {
    let viewModel: AddEditSubscriptionViewModel

    private var state: AddEditSubscriptionUiState { viewModel.uiState }

    var body: some View {
        formView
            .sheet(isPresented: categoryPickerPresented) { categoryPickerContent }
            .sheet(isPresented: currencyPickerPresented) { currencyPickerContent }
            .sheet(isPresented: frequencyPickerPresented) { frequencyPickerContent }
            .sheet(isPresented: paymentMethodPickerPresented) { paymentMethodPickerContent }
            .sheet(isPresented: datePickerPresented) { datePickerContent }
    }

    private var formView: some View {
        Form {
            basicInfoSection
            billingSection
            paymentSection
            settingsSection
            notesSection
            errorSection
        }
    }

    // MARK: Sheet content

    @ViewBuilder
    private var categoryPickerContent: some View {
        PickerSheet(
            title: "Category",
            items: state.availableCategories,
            selectedId: state.selectedCategoryId,
            label: { "\($0.emoji) \($0.displayName)" },
            onSelect: { viewModel.onCategorySelected($0) }
        )
        .presentationDetents([.medium])
    }

    @ViewBuilder
    private var currencyPickerContent: some View {
        CurrencyPickerSheet(selected: state.currency) { currency in
            viewModel.onCurrencySelected(currency)
        }
        .presentationDetents([.medium])
    }

    @ViewBuilder
    private var frequencyPickerContent: some View {
        PickerSheet(
            title: "Frequency",
            items: BillingFrequency.allCases,
            selectedId: state.selectedFrequency.id,
            label: { $0.displayName },
            onSelect: { viewModel.onFrequencySelected($0) }
        )
        .presentationDetents([.medium])
    }

    @ViewBuilder
    private var paymentMethodPickerContent: some View {
        PaymentMethodPickerSheet(
            methods: state.availablePaymentMethods,
            selectedId: state.selectedPaymentMethodId
        ) { method in
            viewModel.onPaymentMethodSelected(method)
        }
        .presentationDetents([.medium])
    }

    @ViewBuilder
    private var datePickerContent: some View {
        DatePickerSheet(selected: state.startDate) { date in
            viewModel.onStartDateSelected(date)
        }
        .presentationDetents([.medium])
    }

    // MARK: Sections

    @ViewBuilder
    private var basicInfoSection: some View {
        Section("Basic Info") {
            VStack(alignment: .leading, spacing: 4) {
                TextField("Name", text: Binding(
                    get: { state.name },
                    set: { viewModel.onNameChange($0) }
                ))
                if let error = state.nameError {
                    Text(error)
                        .font(.caption)
                        .foregroundColor(.red)
                }
            }

            // Category picker row
            Button {
                viewModel.uiState.showCategoryPicker = true
            } label: {
                HStack {
                    Text("Category")
                        .foregroundColor(.primary)
                    Spacer()
                    if let cat = state.selectedCategory {
                        Text("\(cat.emoji) \(cat.displayName)")
                            .foregroundColor(.secondary)
                    }
                    Image(systemName: "chevron.right")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
            }
            .buttonStyle(.plain)
        }
    }

    @ViewBuilder
    private var billingSection: some View {
        Section("Billing") {
            VStack(alignment: .leading, spacing: 4) {
                HStack {
                    TextField("Amount", text: Binding(
                        get: { state.amount },
                        set: { viewModel.onAmountChange($0) }
                    ))
                    .keyboardType(.decimalPad)

                    Button {
                        viewModel.uiState.showCurrencyPicker = true
                    } label: {
                        Text(state.currency)
                            .foregroundColor(.accentColor)
                            .fontWeight(.medium)
                    }
                    .buttonStyle(.plain)
                }
                if let error = state.amountError {
                    Text(error)
                        .font(.caption)
                        .foregroundColor(.red)
                }
            }

            Button {
                viewModel.uiState.showFrequencyPicker = true
            } label: {
                HStack {
                    Text("Frequency")
                        .foregroundColor(.primary)
                    Spacer()
                    Text(state.selectedFrequency.displayName)
                        .foregroundColor(.secondary)
                    Image(systemName: "chevron.right")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
            }
            .buttonStyle(.plain)

            Button {
                viewModel.uiState.showDatePicker = true
            } label: {
                HStack {
                    Text("Start Date")
                        .foregroundColor(.primary)
                    Spacer()
                    Text(state.startDate, style: .date)
                        .foregroundColor(.secondary)
                    Image(systemName: "chevron.right")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
            }
            .buttonStyle(.plain)
        }
    }

    @ViewBuilder
    private var paymentSection: some View {
        Section("Payment") {
            Button {
                viewModel.uiState.showPaymentMethodPicker = true
            } label: {
                HStack {
                    Text("Payment Method")
                        .foregroundColor(.primary)
                    Spacer()
                    Text(state.selectedPaymentMethod?.nickname ?? "None")
                        .foregroundColor(.secondary)
                    Image(systemName: "chevron.right")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
            }
            .buttonStyle(.plain)
        }
    }

    @ViewBuilder
    private var settingsSection: some View {
        Section("Settings") {
            Toggle("Active", isOn: Binding(
                get: { state.isActive },
                set: { viewModel.onIsActiveChange($0) }
            ))

            HStack {
                Text("Reminder")
                Spacer()
                Stepper(
                    "\(state.reminderDaysBefore) day\(state.reminderDaysBefore == 1 ? "" : "s") before",
                    value: Binding(
                        get: { state.reminderDaysBefore },
                        set: { viewModel.onReminderDaysChange($0) }
                    ),
                    in: 0...30
                )
            }
        }
    }

    @ViewBuilder
    private var notesSection: some View {
        Section("Notes") {
            TextField("Optional notes…", text: Binding(
                get: { state.notes },
                set: { viewModel.onNotesChange($0) }
            ), axis: .vertical)
            .lineLimit(3...6)
        }
    }

    @ViewBuilder
    private var errorSection: some View {
        if let error = state.error {
            Section {
                Text(error)
                    .foregroundColor(.red)
                    .font(.footnote)
            }
        }
    }

    // MARK: Sheet presentation bindings

    private var categoryPickerPresented: Binding<Bool> {
        Binding(
            get: { viewModel.uiState.showCategoryPicker },
            set: { viewModel.uiState.showCategoryPicker = $0 }
        )
    }

    private var currencyPickerPresented: Binding<Bool> {
        Binding(
            get: { viewModel.uiState.showCurrencyPicker },
            set: { viewModel.uiState.showCurrencyPicker = $0 }
        )
    }

    private var frequencyPickerPresented: Binding<Bool> {
        Binding(
            get: { viewModel.uiState.showFrequencyPicker },
            set: { viewModel.uiState.showFrequencyPicker = $0 }
        )
    }

    private var paymentMethodPickerPresented: Binding<Bool> {
        Binding(
            get: { viewModel.uiState.showPaymentMethodPicker },
            set: { viewModel.uiState.showPaymentMethodPicker = $0 }
        )
    }

    private var datePickerPresented: Binding<Bool> {
        Binding(
            get: { viewModel.uiState.showDatePicker },
            set: { viewModel.uiState.showDatePicker = $0 }
        )
    }
}

// MARK: - Picker Sheet

private struct PickerSheet<T: Identifiable & Equatable>: View {
    let title: String
    let items: [T]
    let selectedId: T.ID?
    let label: (T) -> String
    let onSelect: (T) -> Void

    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            List(items) { item in
                Button {
                    onSelect(item)
                    dismiss()
                } label: {
                    HStack {
                        Text(label(item))
                            .foregroundColor(.primary)
                        Spacer()
                        if item.id == selectedId {
                            Image(systemName: "checkmark")
                                .foregroundColor(.accentColor)
                        }
                    }
                }
            }
            .navigationTitle(title)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
            }
        }
    }
}

extension BillingFrequency: Identifiable {
    public var id: String { rawValue }
}

// MARK: - Currency Picker Sheet

private struct CurrencyPickerSheet: View {
    let selected: String
    let onSelect: (String) -> Void
    @Environment(\.dismiss) private var dismiss

    private let currencies = ["USD", "EUR", "GBP", "CAD", "AUD", "JPY", "CHF", "CNY", "INR", "MXN", "BRL", "SGD", "HKD", "NOK", "SEK", "DKK", "NZD", "ZAR", "KRW"]

    var body: some View {
        NavigationStack {
            List(currencies, id: \.self) { currency in
                Button {
                    onSelect(currency)
                    dismiss()
                } label: {
                    HStack {
                        Text(currency)
                            .foregroundColor(.primary)
                        Spacer()
                        if currency == selected {
                            Image(systemName: "checkmark")
                                .foregroundColor(.accentColor)
                        }
                    }
                }
            }
            .navigationTitle("Currency")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
            }
        }
    }
}

// MARK: - Payment Method Picker Sheet

private struct PaymentMethodPickerSheet: View {
    let methods: [PaymentMethod]
    let selectedId: UUID?
    let onSelect: (PaymentMethod?) -> Void
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            List {
                Button {
                    onSelect(nil)
                    dismiss()
                } label: {
                    HStack {
                        Text("None")
                            .foregroundColor(.primary)
                        Spacer()
                        if selectedId == nil {
                            Image(systemName: "checkmark")
                                .foregroundColor(.accentColor)
                        }
                    }
                }

                ForEach(methods) { method in
                    Button {
                        onSelect(method)
                        dismiss()
                    } label: {
                        HStack {
                            Text(method.nickname)
                                .foregroundColor(.primary)
                            Spacer()
                            if method.id == selectedId {
                                Image(systemName: "checkmark")
                                    .foregroundColor(.accentColor)
                            }
                        }
                    }
                }
            }
            .navigationTitle("Payment Method")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
            }
        }
    }
}

// MARK: - Date Picker Sheet

private struct DatePickerSheet: View {
    @State private var date: Date
    let onSelect: (Date) -> Void
    @Environment(\.dismiss) private var dismiss

    init(selected: Date, onSelect: @escaping (Date) -> Void) {
        _date = State(initialValue: selected)
        self.onSelect = onSelect
    }

    var body: some View {
        NavigationStack {
            DatePicker("Start Date", selection: $date, displayedComponents: .date)
                .datePickerStyle(.graphical)
                .padding()
                .navigationTitle("Start Date")
                .navigationBarTitleDisplayMode(.inline)
                .toolbar {
                    ToolbarItem(placement: .cancellationAction) {
                        Button("Cancel") { dismiss() }
                    }
                    ToolbarItem(placement: .confirmationAction) {
                        Button("Done") {
                            onSelect(date)
                            dismiss()
                        }
                    }
                }
        }
    }
}

#Preview {
    AddEditSubscriptionView(subscriptionId: nil)
        .environmentObject(ServiceContainer())
}
