import SwiftUI

struct SettingsView: View {
    @EnvironmentObject private var services: ServiceContainer
    @State private var viewModel: SettingsViewModel?
    @State private var appVersion: String = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "1.0"

    var body: some View {
        NavigationStack {
            Group {
                if let viewModel {
                    settingsForm(viewModel: viewModel)
                } else {
                    ProgressView()
                }
            }
            .navigationTitle("Settings")
        }
        .task {
            if viewModel == nil {
                viewModel = SettingsViewModel(authRepository: services.authRepository)
            }
        }
    }

    @ViewBuilder
    private func settingsForm(viewModel: SettingsViewModel) -> some View {
        let state = viewModel.uiState

        Form {
            // MARK: - Appearance
            Section("Appearance") {
                Picker("Theme", selection: Binding(
                    get: { state.selectedTheme },
                    set: { viewModel.onThemeChange($0) }
                )) {
                    ForEach(ThemePreference.allCases, id: \.self) { theme in
                        Text(theme.displayName).tag(theme)
                    }
                }
                .pickerStyle(.segmented)
                .listRowInsets(EdgeInsets(top: 8, leading: 16, bottom: 8, trailing: 16))
            }

            // MARK: - Notifications
            Section("Notifications") {
                Toggle("Enable Notifications", isOn: Binding(
                    get: { state.notificationsEnabled },
                    set: { viewModel.onNotificationsEnabledChange($0) }
                ))

                if state.notificationsEnabled {
                    // Morning reminder
                    HStack {
                        Toggle("Morning Reminder", isOn: Binding(
                            get: { state.morningReminderEnabled },
                            set: { viewModel.onMorningReminderEnabledChange($0) }
                        ))
                    }

                    if state.morningReminderEnabled {
                        Button {
                            viewModel.uiState.showMorningTimePicker = true
                        } label: {
                            HStack {
                                Text("Morning Time")
                                    .foregroundColor(.primary)
                                Spacer()
                                Text(state.morningReminderTime)
                                    .foregroundColor(.secondary)
                                Image(systemName: "chevron.right")
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                            }
                        }
                        .buttonStyle(.plain)
                    }

                    // Evening reminder
                    Toggle("Evening Reminder", isOn: Binding(
                        get: { state.eveningReminderEnabled },
                        set: { viewModel.onEveningReminderEnabledChange($0) }
                    ))

                    if state.eveningReminderEnabled {
                        Button {
                            viewModel.uiState.showEveningTimePicker = true
                        } label: {
                            HStack {
                                Text("Evening Time")
                                    .foregroundColor(.primary)
                                Spacer()
                                Text(state.eveningReminderTime)
                                    .foregroundColor(.secondary)
                                Image(systemName: "chevron.right")
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                            }
                        }
                        .buttonStyle(.plain)
                    }

                    // Reminder days slider
                    VStack(alignment: .leading, spacing: 4) {
                        HStack {
                            Text("Remind me")
                            Spacer()
                            Text("\(state.reminderDaysBefore) day\(state.reminderDaysBefore == 1 ? "" : "s") before")
                                .foregroundColor(.secondary)
                                .font(.subheadline)
                        }
                        Slider(
                            value: Binding(
                                get: { Double(state.reminderDaysBefore) },
                                set: { viewModel.onReminderDaysChange(Int($0)) }
                            ),
                            in: 1...30,
                            step: 1
                        )
                    }
                    .padding(.vertical, 4)
                }
            }

            // MARK: - Storage & Sync
            Section("Storage & Sync") {
                ForEach(StorageProviderPreference.allCases, id: \.self) { provider in
                    Button {
                        viewModel.onStorageProviderChange(provider)
                    } label: {
                        HStack(spacing: 12) {
                            Image(systemName: provider.sfSymbol)
                                .foregroundColor(.accentColor)
                                .frame(width: 24)
                            Text(provider.displayName)
                                .foregroundColor(.primary)
                            Spacer()
                            if state.selectedStorageProvider == provider {
                                Image(systemName: "checkmark")
                                    .foregroundColor(.accentColor)
                            }
                        }
                    }
                }
            }

            // MARK: - Account
            Section("Account") {
                Button(role: .destructive) {
                    viewModel.uiState.showSignOutConfirmation = true
                } label: {
                    Label("Sign Out", systemImage: "rectangle.portrait.and.arrow.right")
                }
            }

            // MARK: - About
            Section("About") {
                HStack {
                    Text("Version")
                    Spacer()
                    Text(appVersion)
                        .foregroundColor(.secondary)
                }
            }
        }
        // Morning time picker sheet
        .sheet(isPresented: Binding(
            get: { viewModel.uiState.showMorningTimePicker },
            set: { viewModel.uiState.showMorningTimePicker = $0 }
        )) {
            TimePickerSheet(
                title: "Morning Reminder",
                current: state.morningReminderTime
            ) { time in
                viewModel.onMorningTimeChange(time)
            }
            .presentationDetents([.medium])
        }
        // Evening time picker sheet
        .sheet(isPresented: Binding(
            get: { viewModel.uiState.showEveningTimePicker },
            set: { viewModel.uiState.showEveningTimePicker = $0 }
        )) {
            TimePickerSheet(
                title: "Evening Reminder",
                current: state.eveningReminderTime
            ) { time in
                viewModel.onEveningTimeChange(time)
            }
            .presentationDetents([.medium])
        }
        // Sign out confirmation
        .confirmationDialog(
            "Sign Out",
            isPresented: Binding(
                get: { viewModel.uiState.showSignOutConfirmation },
                set: { viewModel.uiState.showSignOutConfirmation = $0 }
            ),
            titleVisibility: .visible
        ) {
            Button("Sign Out", role: .destructive) {
                Task { await viewModel.signOut() }
            }
            Button("Cancel", role: .cancel) {}
        } message: {
            Text("You will be returned to the login screen.")
        }
        // Error alert
        .alert("Error", isPresented: Binding(
            get: { state.error != nil },
            set: { if !$0 { viewModel.uiState.error = nil } }
        )) {
            Button("OK", role: .cancel) { viewModel.uiState.error = nil }
        } message: {
            Text(state.error ?? "")
        }
    }
}

// MARK: - Time Picker Sheet

private struct TimePickerSheet: View {
    let title: String
    let current: String
    let onSelect: (String) -> Void

    @State private var date: Date
    @Environment(\.dismiss) private var dismiss

    init(title: String, current: String, onSelect: @escaping (String) -> Void) {
        self.title = title
        self.current = current
        self.onSelect = onSelect

        // Parse "HH:mm" → Date
        let formatter = DateFormatter()
        formatter.dateFormat = "HH:mm"
        _date = State(initialValue: formatter.date(from: current) ?? Date())
    }

    var body: some View {
        NavigationStack {
            DatePicker("", selection: $date, displayedComponents: .hourAndMinute)
                .datePickerStyle(.wheel)
                .labelsHidden()
                .padding()
                .navigationTitle(title)
                .navigationBarTitleDisplayMode(.inline)
                .toolbar {
                    ToolbarItem(placement: .cancellationAction) {
                        Button("Cancel") { dismiss() }
                    }
                    ToolbarItem(placement: .confirmationAction) {
                        Button("Done") {
                            let formatter = DateFormatter()
                            formatter.dateFormat = "HH:mm"
                            onSelect(formatter.string(from: date))
                            dismiss()
                        }
                    }
                }
        }
    }
}

#Preview {
    SettingsView()
        .environmentObject(ServiceContainer())
}
