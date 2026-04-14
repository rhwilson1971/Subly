import Foundation

struct AddEditPaymentMethodUiState {
    var nickname: String = ""
    var selectedType: PaymentType = .visa
    var lastFourDigits: String = ""

    var isLoading: Bool = false
    var isSaving: Bool = false
    var error: String? = nil

    var nicknameError: String? = nil
    var lastFourDigitsError: String? = nil

    var showTypePicker: Bool = false

    var isEditMode: Bool = false
    var paymentMethodId: UUID? = nil
}
