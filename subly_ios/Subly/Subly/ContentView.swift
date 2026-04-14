//
//  ContentView.swift
//  Subly
//
//  Created by Dr Reuben Wilson on 4/13/26.
//

import SwiftUI

struct ContentView: View {
    var body: some View {
        SubscriptionsView()
    }
}

#Preview {
    ContentView()
        .environmentObject(ServiceContainer())
}
