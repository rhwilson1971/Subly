import Foundation

struct ProfileSetupUiState {
    var email: String = ""
    var password: String = ""
    var emailError: String? = nil
    var passwordErrors: [String] = []
    var fullName: String = ""
    var dateOfBirth: Date? = nil
    var phoneNumber: String = ""
    var showDatePicker: Bool = false
    var isSaving: Bool = false
    var error: String? = nil
    var navigateNext: Bool = false
}
