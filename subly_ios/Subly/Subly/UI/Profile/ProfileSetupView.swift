import SwiftUI

struct ProfileSetupView: View {
    let onNavigateToLogin: () -> Void
    let onSignUpSuccess: () -> Void

    @State private var viewModel: ProfileSetupViewModel

    init(
        authRepository: AuthRepository,
        userProfileRepository: UserProfileRepository,
        onNavigateToLogin: @escaping () -> Void,
        onSignUpSuccess: @escaping () -> Void
    ) {
        self.onNavigateToLogin = onNavigateToLogin
        self.onSignUpSuccess = onSignUpSuccess
        _viewModel = State(initialValue: ProfileSetupViewModel(
            authRepository: authRepository,
            userProfileRepository: userProfileRepository
        ))
    }

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 20) {
                    VStack(spacing: 8) {
                        Image(systemName: "person.circle.fill")
                            .font(.system(size: 80))
                            .foregroundStyle(.secondary)
                        Text("Create an account to enable Cloud Sync")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                    .padding(.top, 8)

                    // Email — required
                    VStack(alignment: .leading, spacing: 4) {
                        TextField("Email", text: Binding(
                            get: { viewModel.uiState.email },
                            set: { viewModel.onEmailChange($0) }
                        ))
                        .textFieldStyle(.roundedBorder)
                        .textContentType(.emailAddress)
                        .keyboardType(.emailAddress)
                        .autocorrectionDisabled()
                        .textInputAutocapitalization(.never)
                        if let error = viewModel.uiState.emailError {
                            Text(error).font(.caption).foregroundStyle(.red)
                        }
                    }

                    // Password — required, strong
                    VStack(alignment: .leading, spacing: 4) {
                        SecureField("Password", text: Binding(
                            get: { viewModel.uiState.password },
                            set: { viewModel.onPasswordChange($0) }
                        ))
                        .textFieldStyle(.roundedBorder)
                        .textContentType(.newPassword)

                        if !viewModel.uiState.password.isEmpty {
                            passwordRequirement("At least 8 characters", met: viewModel.uiState.password.count >= 8)
                            passwordRequirement("At least one uppercase letter", met: viewModel.uiState.password.contains(where: { $0.isUppercase }))
                            passwordRequirement("At least one lowercase letter", met: viewModel.uiState.password.contains(where: { $0.isLowercase }))
                            passwordRequirement("At least one number", met: viewModel.uiState.password.contains(where: { $0.isNumber }))
                        }
                    }

                    // Full Name — optional
                    TextField("Full Name (Optional)", text: Binding(
                        get: { viewModel.uiState.fullName },
                        set: { viewModel.onFullNameChange($0) }
                    ))
                    .textFieldStyle(.roundedBorder)
                    .textContentType(.name)

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

                    if let error = viewModel.uiState.error {
                        Text(error)
                            .font(.callout)
                            .padding()
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .background(.red.opacity(0.1))
                            .foregroundStyle(.red)
                            .clipShape(RoundedRectangle(cornerRadius: 8))
                    }

                    Button {
                        viewModel.signUp()
                    } label: {
                        Group {
                            if viewModel.uiState.isSaving {
                                HStack(spacing: 8) {
                                    ProgressView().tint(.white)
                                    Text("Creating account…")
                                }
                            } else {
                                Text("Create Account").fontWeight(.semibold)
                            }
                        }
                        .frame(maxWidth: .infinity)
                    }
                    .buttonStyle(.borderedProminent)
                    .controlSize(.large)
                    .disabled(viewModel.uiState.isSaving)

                    Button(action: onNavigateToLogin) {
                        Text("Already have an account? Sign In")
                    }
                }
                .padding(16)
            }
            .navigationTitle("Create Your Profile")
            .navigationBarTitleDisplayMode(.inline)
        }
        .onChange(of: viewModel.uiState.navigateNext) { _, navigateNext in
            if navigateNext {
                viewModel.onNavigateNextHandled()
                onSignUpSuccess()
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

    @ViewBuilder
    private func passwordRequirement(_ label: String, met: Bool) -> some View {
        HStack(spacing: 6) {
            Image(systemName: met ? "checkmark.circle.fill" : "xmark.circle")
                .foregroundStyle(met ? .green : .red)
                .font(.caption)
            Text(label)
                .font(.caption)
                .foregroundStyle(met ? Color.secondary : Color.red)
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
