//
//  SublyApp.swift
//  Subly
//
//  Created by Dr Reuben Wilson on 4/13/26.
//

import SwiftUI
import SwiftData
import FirebaseCore

@main
struct SublyApp: App {
    @StateObject private var services: ServiceContainer
    @StateObject private var prefs = PreferencesManager.shared

    init() {
        FirebaseApp.configure()
        _services = StateObject(wrappedValue: ServiceContainer())
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(services)
                .environmentObject(prefs)
                .preferredColorScheme(prefs.theme.colorScheme)
                .modelContainer(services.modelContainer)
        }
    }
}
