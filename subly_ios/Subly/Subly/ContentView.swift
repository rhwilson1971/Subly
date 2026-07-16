//
//  ContentView.swift
//  Subly
//
//  Created by Dr Reuben Wilson on 4/13/26.
//

import SwiftUI

struct ContentView: View {
    @EnvironmentObject private var services: ServiceContainer
    @State private var hasCompletedOnboarding: Bool
    @State private var showSignUp = false
    @State private var showLogin = false

    init() {
        _hasCompletedOnboarding = State(initialValue: PreferencesManager.shared.hasCompletedOnboarding)
    }

    var body: some View {
        Group {
            if hasCompletedOnboarding {
                MainTabView(
                    onRequestSignUp: { showSignUp = true },
                    onRequestLogin: { showLogin = true }
                )
            } else {
                OnboardingView(
                    onRequestSignUp: { showSignUp = true },
                    onDone: { hasCompletedOnboarding = true }
                )
            }
        }
        .fullScreenCover(isPresented: $showSignUp) {
            ProfileSetupView(
                authRepository: services.authRepository,
                userProfileRepository: services.userProfileRepository,
                onNavigateToLogin: {
                    showSignUp = false
                    showLogin = true
                },
                onSignUpSuccess: {
                    hasCompletedOnboarding = true
                    showSignUp = false
                },
                onCancel: { showSignUp = false }
            )
        }
        .fullScreenCover(isPresented: $showLogin) {
            LoginView(
                authRepository: services.authRepository,
                onNavigateToRegister: {
                    showLogin = false
                    showSignUp = true
                },
                onAuthSuccess: {
                    PreferencesManager.shared.hasCompletedOnboarding = true
                    hasCompletedOnboarding = true
                    showLogin = false
                },
                onCancel: { showSignUp = false; showLogin = false }
            )
        }
    }
}

#Preview {
    ContentView()
        .environmentObject(ServiceContainer())
}
