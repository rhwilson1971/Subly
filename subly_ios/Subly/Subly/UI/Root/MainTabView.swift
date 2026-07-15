import SwiftUI

struct MainTabView: View {
    let onRequestSignUp: () -> Void
    let onRequestLogin: () -> Void

    var body: some View {
        TabView {
            HomeView()
                .tabItem { Label("Home", systemImage: "house.fill") }

            SubscriptionsView()
                .tabItem { Label("Subscriptions", systemImage: "list.bullet") }

            PaymentMethodsView()
                .tabItem { Label("Payments", systemImage: "creditcard.fill") }

            SettingsView(
                onNavigateToSignUp: onRequestSignUp,
                onNavigateToLogin: onRequestLogin
            )
            .tabItem { Label("Settings", systemImage: "gearshape.fill") }
        }
    }
}
