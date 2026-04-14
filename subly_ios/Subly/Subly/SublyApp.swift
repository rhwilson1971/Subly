//
//  SublyApp.swift
//  Subly
//
//  Created by Dr Reuben Wilson on 4/13/26.
//

import SwiftUI

@main
struct SublyApp: App {
    @StateObject private var services = ServiceContainer()

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(services)
        }
        .modelContainer(services.modelContainer)
    }
}
