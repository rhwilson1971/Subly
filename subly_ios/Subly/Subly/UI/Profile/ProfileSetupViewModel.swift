import Foundation
import Observation

@Observable
final class ProfileSetupViewModel {
    var uiState = ProfileSetupUiState()

    private let authRepository: AuthRepository
    private let userProfileRepository: UserProfileRepository
    private let prefs: PreferencesManager

    init(
        authRepository: AuthRepository,
        userProfileRepository: UserProfileRepository,
        prefs: PreferencesManager = .shared
    ) {
        self.authRepository = authRepository
        self.userProfileRepository = userProfileRepository
        self.prefs = prefs
    }

    func onEmailChange(_ value: String) {
        uiState.email = value
        uiState.emailError = nil
    }

    func onPasswordChange(_ value: String) {
        uiState.password = value
        uiState.passwordErrors = PasswordValidator.errors(value)
    }

    func onFullNameChange(_ value: String) {
        uiState.fullName = value
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

    func signUp() {
        let isValidEmail = uiState.email.contains("@") && uiState.email.contains(".")
        let passwordErrors = PasswordValidator.errors(uiState.password)

        guard isValidEmail else {
            uiState.emailError = "Enter a valid email address"
            return
        }
        guard passwordErrors.isEmpty else {
            uiState.passwordErrors = passwordErrors
            return
        }

        Task { @MainActor in
            uiState.isSaving = true
            uiState.error = nil

            let result = await authRepository.registerWithEmail(email: uiState.email, password: uiState.password)
            switch result {
            case .success(let authUser):
                await saveProfileAndFinish(uid: authUser.uid)
            case .failure(let error):
                uiState.isSaving = false
                uiState.error = error.localizedDescription
            }
        }
    }

    @MainActor
    private func saveProfileAndFinish(uid: String) async {
        let dobString: String? = uiState.dateOfBirth.map { date in
            let formatter = ISO8601DateFormatter()
            formatter.formatOptions = [.withFullDate]
            return formatter.string(from: date)
        }

        let user = User(
            uid: uid,
            email: uiState.email,
            displayName: uiState.fullName.isEmpty ? nil : uiState.fullName,
            fullName: uiState.fullName.isEmpty ? nil : uiState.fullName,
            dateOfBirth: dobString,
            phoneNumber: uiState.phoneNumber.isEmpty ? nil : uiState.phoneNumber
        )

        // Best-effort — an account was already created; don't block the user on this write.
        let saveResult = await userProfileRepository.saveProfile(user)
        if case .failure(let error) = saveResult {
            print("ProfileSetupViewModel: failed to save profile — \(error.localizedDescription)")
        }

        prefs.storageProvider = .firebase
        prefs.hasCompletedOnboarding = true

        uiState.isSaving = false
        uiState.navigateNext = true
    }

    func onNavigateNextHandled() { uiState.navigateNext = false }
}
