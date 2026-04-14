import SwiftUI

struct ProfileSetupView: View {
    let onNavigateNext: () -> Void

    @State private var viewModel: ProfileSetupViewModel

    init(userProfileRepository: UserProfileRepository, onNavigateNext: @escaping () -> Void) {
        self.onNavigateNext = onNavigateNext
        _viewModel = State(initialValue: ProfileSetupViewModel(userProfileRepository: userProfileRepository))
    }

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 20) {
                    // Avatar placeholder
                    VStack(spacing: 8) {
                        Image(systemName: "person.circle.fill")
                            .font(.system(size: 80))
                            .foregroundStyle(.secondary)
                        Text("Profile photo coming soon")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                    .padding(.top, 8)

                    // Full Name
                    VStack(alignment: .leading, spacing: 4) {
                        TextField("Full Name", text: Binding(
                            get: { viewModel.uiState.fullName },
                            set: { viewModel.onFullNameChange($0) }
                        ))
                        .textFieldStyle(.roundedBorder)
                        .textContentType(.name)
                        if let error = viewModel.uiState.fullNameError {
                            Text(error).font(.caption).foregroundStyle(.red)
                        }
                    }

                    // Email (read-only)
                    TextField("Email", text: .constant(viewModel.uiState.email))
                        .textFieldStyle(.roundedBorder)
                        .disabled(true)
                        .foregroundStyle(.secondary)

                    // Date of Birth
                    dateOfBirthRow

                    // Phone Number
                    TextField("Phone Number (Optional)", text: Binding(
                        get: { viewModel.uiState.phoneNumber },
                        set: { viewModel.onPhoneNumberChange($0) }
                    ))
                    .textFieldStyle(.roundedBorder)
                    .textContentType(.telephoneNumber)
                    .keyboardType(.phonePad)

                    // Error banner
                    if let error = viewModel.uiState.error {
                        Text(error)
                            .font(.callout)
                            .padding()
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .background(.red.opacity(0.1))
                            .foregroundStyle(.red)
                            .clipShape(RoundedRectangle(cornerRadius: 8))
                    }

                    // Save button
                    Button {
                        viewModel.saveProfile()
                    } label: {
                        Group {
                            if viewModel.uiState.isSaving {
                                HStack(spacing: 8) {
                                    ProgressView().tint(.white)
                                    Text("Saving…")
                                }
                            } else {
                                Text("Continue").fontWeight(.semibold)
                            }
                        }
                        .frame(maxWidth: .infinity)
                    }
                    .buttonStyle(.borderedProminent)
                    .controlSize(.large)
                    .disabled(viewModel.uiState.isSaving)
                }
                .padding(16)
            }
            .navigationTitle("Set Up Your Profile")
            .navigationBarTitleDisplayMode(.inline)
        }
        .onChange(of: viewModel.uiState.navigateNext) { _, navigateNext in
            if navigateNext {
                viewModel.onNavigateNextHandled()
                onNavigateNext()
            }
        }
        .sheet(isPresented: Binding(
            get: { viewModel.uiState.showDatePicker },
            set: { if !$0 { viewModel.dismissDatePicker() } }
        )) {
            DateOfBirthPickerSheet(
                selection: Binding(
                    get: { viewModel.uiState.dateOfBirth ?? Calendar.current.date(byAdding: .year, value: -25, to: Date())! },
                    set: { viewModel.onDateOfBirthSelected($0) }
                ),
                onDismiss: viewModel.dismissDatePicker
            )
        }
    }

    private var dateOfBirthRow: some View {
        Button {
            viewModel.showDatePicker()
        } label: {
            HStack {
                VStack(alignment: .leading, spacing: 2) {
                    Text("Date of Birth (Optional)")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                    Text(viewModel.uiState.dateOfBirth.map { $0.formatted(date: .abbreviated, time: .omitted) } ?? "Not set")
                        .foregroundStyle(viewModel.uiState.dateOfBirth == nil ? .secondary : .primary)
                }
                Spacer()
                if viewModel.uiState.dateOfBirth != nil {
                    Button("Clear") { viewModel.clearDateOfBirth() }
                        .font(.callout)
                }
                Image(systemName: "calendar")
                    .foregroundStyle(.secondary)
            }
            .padding()
            .background(.regularMaterial)
            .clipShape(RoundedRectangle(cornerRadius: 8))
        }
        .buttonStyle(.plain)
    }
}

private struct DateOfBirthPickerSheet: View {
    @Binding var selection: Date
    let onDismiss: () -> Void

    var body: some View {
        NavigationStack {
            DatePicker(
                "Date of Birth",
                selection: $selection,
                in: ...Date(),
                displayedComponents: .date
            )
            .datePickerStyle(.graphical)
            .padding()
            .navigationTitle("Date of Birth")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button("Done", action: onDismiss)
                }
            }
        }
        .presentationDetents([.medium])
    }
}
