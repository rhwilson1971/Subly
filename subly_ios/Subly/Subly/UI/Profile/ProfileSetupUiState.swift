import Foundation

struct ProfileSetupUiState {
    var fullName: String = ""
    var email: String = ""
    var dateOfBirth: Date? = nil
    var phoneNumber: String = ""
    var fullNameError: String? = nil
    var showDatePicker: Bool = false
    var isSaving: Bool = false
    var error: String? = nil
    var navigateNext: Bool = false
}
