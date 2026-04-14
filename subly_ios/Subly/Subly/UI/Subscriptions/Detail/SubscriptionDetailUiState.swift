import Foundation

struct SubscriptionDetailUiState {
    var subscription: Subscription? = nil
    var category: Category? = nil
    var paymentMethod: PaymentMethod? = nil

    var isLoading: Bool = true
    var error: String? = nil

    var isDeleting: Bool = false
    var isTogglingActive: Bool = false
    var showDeleteConfirmation: Bool = false

    var deleteSuccess: Bool = false
}
