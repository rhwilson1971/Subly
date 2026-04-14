import Foundation
import Observation

@Observable
final class AuthViewModel {
    var uiState = AuthUiState()

    private let authRepository: AuthRepository

    init(authRepository: AuthRepository) {
        self.authRepository = authRepository
    }

    func signIn(email: String, password: String) {
        Task { @MainActor in
            uiState.isLoading = true
            uiState.error = nil
            let result = await authRepository.signInWithEmail(email: email, password: password)
            switch result {
            case .success:
                uiState.isLoading = false
                uiState.isAuthenticated = true
            case .failure(let error):
                uiState.isLoading = false
                uiState.error = error.localizedDescription
            }
        }
    }

    func register(email: String, password: String) {
        Task { @MainActor in
            uiState.isLoading = true
            uiState.error = nil
            let result = await authRepository.registerWithEmail(email: email, password: password)
            switch result {
            case .success:
                uiState.isLoading = false
                uiState.isAuthenticated = true
            case .failure(let error):
                uiState.isLoading = false
                uiState.error = error.localizedDescription
            }
        }
    }

    func signInWithGoogle(idToken: String) {
        Task { @MainActor in
            uiState.isLoading = true
            uiState.error = nil
            let result = await authRepository.signInWithGoogle(idToken: idToken)
            switch result {
            case .success:
                uiState.isLoading = false
                uiState.isAuthenticated = true
            case .failure(let error):
                uiState.isLoading = false
                uiState.error = error.localizedDescription
            }
        }
    }

    func clearError() {
        uiState.error = nil
    }
}
