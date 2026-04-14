import Foundation

struct AddEditSubscriptionUiState {
    // Form Fields
    var name: String = ""
    var selectedCategoryId: UUID? = nil
    var amount: String = ""
    var currency: String = "USD"
    var selectedFrequency: BillingFrequency = .monthly
    var startDate: Date = Date()
    var selectedPaymentMethodId: UUID? = nil
    var notes: String = ""
    var reminderDaysBefore: Int = 2
    var isActive: Bool = true

    // Available Options (loaded from repositories)
    var availablePaymentMethods: [PaymentMethod] = []
    var availableCategories: [Category] = []

    // UI State
    var isLoading: Bool = false
    var isSaving: Bool = false
    var error: String? = nil

    // Validation Errors (shown on submit only)
    var nameError: String? = nil
    var amountError: String? = nil

    // Sheet / Picker State
    var showCategoryPicker: Bool = false
    var showCurrencyPicker: Bool = false
    var showFrequencyPicker: Bool = false
    var showPaymentMethodPicker: Bool = false
    var showDatePicker: Bool = false

    // Edit Mode
    var isEditMode: Bool = false
    var subscriptionId: UUID? = nil

    // Derived
    var selectedCategory: Category? {
        availableCategories.first { $0.id == selectedCategoryId }
    }

    var selectedPaymentMethod: PaymentMethod? {
        availablePaymentMethods.first { $0.id == selectedPaymentMethodId }
    }
}
