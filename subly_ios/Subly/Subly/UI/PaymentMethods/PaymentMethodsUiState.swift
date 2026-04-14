import Foundation

struct PaymentMethodWithUsage: Identifiable {
    let paymentMethod: PaymentMethod
    let subscriptionCount: Int
    var id: UUID { paymentMethod.id }
}

struct PaymentMethodsUiState {
    var paymentMethods: [PaymentMethodWithUsage] = []
    var isLoading: Bool = true
    var error: String? = nil
    var showDeleteConfirmation: Bool = false
    var deletingMethod: PaymentMethodWithUsage? = nil
    var isDeleting: Bool = false
}
