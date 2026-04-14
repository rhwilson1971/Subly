import Foundation
import Observation
import FirebaseAuth

@Observable
final class ProfileSetupViewModel {
    var uiState = ProfileSetupUiState()

    private let userProfileRepository: UserProfileRepository

    init(userProfileRepository: UserProfileRepository) {
        self.userProfileRepository = userProfileRepository
        if let firebaseUser = Auth.auth().currentUser {
            uiState.email = firebaseUser.email ?? ""
            uiState.fullName = firebaseUser.displayName ?? ""
        }
    }

    func onFullNameChange(_ value: String) {
        uiState.fullName = value
        uiState.fullNameError = nil
    }

    func onPhoneNumberChange(_ value: String) {
        uiState.phoneNumber = value
    }

    func showDatePicker() { uiState.showDatePicker = true }
    func dismissDatePicker() { uiState.showDatePicker = false }

    func onDateOfBirthSelected(_ date: Date) {
        uiState.dateOfBirth = date
        uiState.showDatePicker = false
    }

    func clearDateOfBirth() { uiState.dateOfBirth = nil }

    func saveProfile() {
        guard !uiState.fullName.trimmingCharacters(in: .whitespaces).isEmpty else {
            uiState.fullNameError = "Full name is required"
            return
        }
        guard let uid = Auth.auth().currentUser?.uid else {
            uiState.error = "Not signed in. Please log in again."
            return
        }

        let dobString: String? = uiState.dateOfBirth.map { date in
            let formatter = ISO8601DateFormatter()
            formatter.formatOptions = [.withFullDate]
            return formatter.string(from: date)
        }

        let user = User(
            uid: uid,
            email: uiState.email.isEmpty ? nil : uiState.email,
            displayName: uiState.fullName,
            fullName: uiState.fullName,
            dateOfBirth: dobString,
            phoneNumber: uiState.phoneNumber.isEmpty ? nil : uiState.phoneNumber
        )

        Task { @MainActor in
            uiState.isSaving = true
            uiState.error = nil
            let result = await userProfileRepository.saveProfile(user)
            switch result {
            case .success:
                uiState.isSaving = false
                uiState.navigateNext = true
            case .failure(let error):
                uiState.isSaving = false
                uiState.error = error.localizedDescription
            }
        }
    }

    func onNavigateNextHandled() { uiState.navigateNext = false }
}
