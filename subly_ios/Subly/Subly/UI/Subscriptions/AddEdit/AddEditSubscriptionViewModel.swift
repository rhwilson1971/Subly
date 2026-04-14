import Foundation
import Combine
import Observation

@Observable
final class AddEditSubscriptionViewModel {
    var uiState = AddEditSubscriptionUiState()

    private let subscriptionRepository: SubscriptionRepository
    private let paymentMethodRepository: PaymentMethodRepository
    private let categoryRepository: CategoryRepository
    private var cancellables = Set<AnyCancellable>()

    init(
        subscriptionRepository: SubscriptionRepository,
        paymentMethodRepository: PaymentMethodRepository,
        categoryRepository: CategoryRepository,
        subscriptionId: UUID? = nil
    ) {
        self.subscriptionRepository = subscriptionRepository
        self.paymentMethodRepository = paymentMethodRepository
        self.categoryRepository = categoryRepository
        uiState.subscriptionId = subscriptionId
        uiState.isEditMode = subscriptionId != nil
        loadInitialData()
    }

    private func loadInitialData() {
        uiState.isLoading = true

        Publishers.CombineLatest(
            paymentMethodRepository.getAllPaymentMethods(),
            categoryRepository.getAllCategories()
        )
        .receive(on: DispatchQueue.main)
        .first()
        .sink { [weak self] paymentMethods, categories in
            guard let self else { return }
            uiState.availablePaymentMethods = paymentMethods
            uiState.availableCategories = categories

            if uiState.selectedCategoryId == nil {
                uiState.selectedCategoryId = categories.first?.id ?? Category.defaultId
            }

            if let id = uiState.subscriptionId {
                loadSubscription(id: id)
            } else {
                uiState.isLoading = false
            }
        }
        .store(in: &cancellables)
    }

    private func loadSubscription(id: UUID) {
        subscriptionRepository.getSubscriptionById(id)
            .receive(on: DispatchQueue.main)
            .first()
            .sink { [weak self] subscription in
                guard let self, let subscription else {
                    self?.uiState.isLoading = false
                    self?.uiState.error = "Subscription not found"
                    return
                }
                populateForm(from: subscription)
            }
            .store(in: &cancellables)
    }

    private func populateForm(from subscription: Subscription) {
        uiState.name = subscription.name
        uiState.selectedCategoryId = subscription.categoryId
        uiState.amount = String(format: "%g", subscription.amount)
        uiState.currency = subscription.currency
        uiState.selectedFrequency = subscription.frequency
        uiState.startDate = subscription.startDate
        uiState.selectedPaymentMethodId = subscription.paymentMethodId
        uiState.notes = subscription.notes ?? ""
        uiState.reminderDaysBefore = subscription.reminderDaysBefore
        uiState.isActive = subscription.isActive
        uiState.isLoading = false
    }

    // MARK: - Field Updates

    func onNameChange(_ name: String) {
        uiState.name = name
        uiState.nameError = nil
    }

    func onAmountChange(_ amount: String) {
        // Allow only valid decimal input
        let filtered = amount.filter { $0.isNumber || $0 == "." }
        uiState.amount = filtered
        uiState.amountError = nil
    }

    func onNotesChange(_ notes: String) {
        uiState.notes = notes
    }

    func onReminderDaysChange(_ days: Int) {
        uiState.reminderDaysBefore = max(0, min(30, days))
    }

    func onIsActiveChange(_ isActive: Bool) {
        uiState.isActive = isActive
    }

    func onCurrencySelected(_ currency: String) {
        uiState.currency = currency
        uiState.showCurrencyPicker = false
    }

    func onCategorySelected(_ category: Category) {
        uiState.selectedCategoryId = category.id
        uiState.showCategoryPicker = false
    }

    func onFrequencySelected(_ frequency: BillingFrequency) {
        uiState.selectedFrequency = frequency
        uiState.showFrequencyPicker = false
    }

    func onPaymentMethodSelected(_ paymentMethod: PaymentMethod?) {
        uiState.selectedPaymentMethodId = paymentMethod?.id
        uiState.showPaymentMethodPicker = false
    }

    func onStartDateSelected(_ date: Date) {
        uiState.startDate = date
        uiState.showDatePicker = false
    }

    // MARK: - Save

    func onSaveClick(onSuccess: @escaping () -> Void) {
        uiState.nameError = nil
        uiState.amountError = nil
        uiState.error = nil

        var hasErrors = false

        if uiState.name.trimmingCharacters(in: .whitespaces).isEmpty {
            uiState.nameError = "Name is required"
            hasErrors = true
        }

        let amountValue = Double(uiState.amount)
        if uiState.amount.isEmpty {
            uiState.amountError = "Amount is required"
            hasErrors = true
        } else if amountValue == nil {
            uiState.amountError = "Invalid amount"
            hasErrors = true
        } else if let v = amountValue, v <= 0 {
            uiState.amountError = "Amount must be greater than 0"
            hasErrors = true
        }

        if hasErrors { return }

        let subscription = buildSubscription()
        uiState.isSaving = true

        Task { @MainActor in
            do {
                if uiState.isEditMode {
                    try await subscriptionRepository.updateSubscription(subscription)
                } else {
                    try await subscriptionRepository.insertSubscription(subscription)
                }
                uiState.isSaving = false
                onSuccess()
            } catch {
                uiState.isSaving = false
                uiState.error = "Failed to save: \(error.localizedDescription)"
            }
        }
    }

    private func buildSubscription() -> Subscription {
        let categoryId = uiState.selectedCategoryId ?? Category.defaultId
        let startDate = uiState.startDate
        let frequency = uiState.selectedFrequency
        let nextBillingDate = calculateNextBillingDate(from: startDate, frequency: frequency)

        return Subscription(
            id: uiState.subscriptionId ?? UUID(),
            name: uiState.name.trimmingCharacters(in: .whitespaces),
            categoryId: categoryId,
            amount: Double(uiState.amount) ?? 0,
            currency: uiState.currency,
            frequency: frequency,
            startDate: startDate,
            nextBillingDate: nextBillingDate,
            paymentMethodId: uiState.selectedPaymentMethodId,
            notes: uiState.notes.trimmingCharacters(in: .whitespaces).isEmpty ? nil : uiState.notes.trimmingCharacters(in: .whitespaces),
            isActive: uiState.isActive,
            reminderDaysBefore: uiState.reminderDaysBefore
        )
    }

    private func calculateNextBillingDate(from startDate: Date, frequency: BillingFrequency) -> Date {
        let cal = Calendar.current
        switch frequency {
        case .weekly:     return cal.date(byAdding: .weekOfYear, value: 1, to: startDate) ?? startDate
        case .monthly:    return cal.date(byAdding: .month, value: 1, to: startDate) ?? startDate
        case .quarterly:  return cal.date(byAdding: .month, value: 3, to: startDate) ?? startDate
        case .semiAnnual: return cal.date(byAdding: .month, value: 6, to: startDate) ?? startDate
        case .annual:     return cal.date(byAdding: .year, value: 1, to: startDate) ?? startDate
        case .custom:     return cal.date(byAdding: .month, value: 1, to: startDate) ?? startDate
        }
    }
}
