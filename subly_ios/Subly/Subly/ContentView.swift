//
//  ContentView.swift
//  Subly
//
//  Created by Dr Reuben Wilson on 4/13/26.
//

import SwiftUI

struct ContentView: View {
    var body: some View {
        HomeView()
    }
}

#Preview {
    ContentView()
        .environmentObject(ServiceContainer())
}
