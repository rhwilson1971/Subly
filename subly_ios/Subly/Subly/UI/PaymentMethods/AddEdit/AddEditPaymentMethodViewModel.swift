import Foundation
import Combine
import Observation

@Observable
final class AddEditPaymentMethodViewModel {
    var uiState = AddEditPaymentMethodUiState()

    private let paymentMethodRepository: PaymentMethodRepository
    private var cancellables = Set<AnyCancellable>()

    init(paymentMethodRepository: PaymentMethodRepository, paymentMethodId: UUID? = nil) {
        self.paymentMethodRepository = paymentMethodRepository
        uiState.paymentMethodId = paymentMethodId
        uiState.isEditMode = paymentMethodId != nil
        if let id = paymentMethodId { loadPaymentMethod(id: id) }
    }

    private func loadPaymentMethod(id: UUID) {
        uiState.isLoading = true
        paymentMethodRepository.getPaymentMethodById(id)
            .receive(on: DispatchQueue.main)
            .first()
            .sink { [weak self] method in
                guard let self, let method else {
                    self?.uiState.isLoading = false
                    self?.uiState.error = "Payment method not found"
                    return
                }
                uiState.nickname = method.nickname
                uiState.selectedType = method.type
                uiState.lastFourDigits = method.lastFourDigits ?? ""
                uiState.isLoading = false
            }
            .store(in: &cancellables)
    }

    func onNicknameChange(_ value: String) {
        uiState.nickname = value
        uiState.nicknameError = nil
    }

    func onTypeSelected(_ type: PaymentType) {
        uiState.selectedType = type
        uiState.showTypePicker = false
    }

    func onLastFourChange(_ value: String) {
        let filtered = value.filter { $0.isNumber }
        uiState.lastFourDigits = String(filtered.prefix(4))
        uiState.lastFourDigitsError = nil
    }

    func onSaveClick(onSuccess: @escaping () -> Void) {
        uiState.nicknameError = nil
        uiState.lastFourDigitsError = nil
        uiState.error = nil

        var hasErrors = false
        if uiState.nickname.trimmingCharacters(in: .whitespaces).isEmpty {
            uiState.nicknameError = "Nickname is required"
            hasErrors = true
        }
        let digits = uiState.lastFourDigits
        if !digits.isEmpty && digits.count != 4 {
            uiState.lastFourDigitsError = "Must be exactly 4 digits"
            hasErrors = true
        }
        if hasErrors { return }

        let method = PaymentMethod(
            id: uiState.paymentMethodId ?? UUID(),
            nickname: uiState.nickname.trimmingCharacters(in: .whitespaces),
            type: uiState.selectedType,
            lastFourDigits: uiState.lastFourDigits.isEmpty ? nil : uiState.lastFourDigits
        )

        uiState.isSaving = true
        Task { @MainActor in
            do {
                if uiState.isEditMode {
                    try await paymentMethodRepository.updatePaymentMethod(method)
                } else {
                    try await paymentMethodRepository.insertPaymentMethod(method)
                }
                uiState.isSaving = false
                onSuccess()
            } catch {
                uiState.isSaving = false
                uiState.error = "Failed to save: \(error.localizedDescription)"
            }
        }
    }
}
