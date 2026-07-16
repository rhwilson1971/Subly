import Foundation
import Observation

struct OnboardingPage {
    let systemImage: String
    let title: String
    let description: String
    let ctaLabel: String
}

@Observable
final class OnboardingViewModel {
    var uiState = OnboardingUiState()

    let pages: [OnboardingPage] = [
        OnboardingPage(
            systemImage: "star.fill",
            title: "Welcome to Subly",
            description: "Keep all your subscriptions in one place. Never miss a payment, and always know what you're spending.",
            ctaLabel: "Next"
        ),
        OnboardingPage(
            systemImage: "icloud.fill",
            title: "Sync Across Devices",
            description: "Back up your subscriptions to the cloud so they're always safe and available. You can set this up now or any time from Settings.",
            ctaLabel: "Next"
        ),
        OnboardingPage(
            systemImage: "plus.circle.fill",
            title: "Add Your Subscriptions",
            description: "Tap the + button on the Subscriptions tab to add your first subscription. Set the amount, frequency, and a reminder so you're never caught off guard.",
            ctaLabel: "Next"
        ),
        OnboardingPage(
            systemImage: "creditcard.fill",
            title: "Track Payment Methods",
            description: "Link payment methods to your subscriptions so you always know which card or account is being charged.",
            ctaLabel: "Get Started"
        )
    ]

    private let prefs: PreferencesManager

    init(prefs: PreferencesManager = .shared) {
        self.prefs = prefs
    }

    func onPageChanged(_ page: Int) {
        uiState.currentPage = page
    }

    /// Works whether or not the user has an account — onboarding is not auth-gated.
    func completeOnboarding() {
        prefs.hasCompletedOnboarding = true
    }
}
