import SwiftUI
import GoogleSignIn

struct RegisterView: View {
    let onNavigateToLogin: () -> Void
    let onAuthSuccess: () -> Void

    @State private var viewModel: AuthViewModel
    @State private var email = ""
    @State private var password = ""
    @State private var confirmPassword = ""
    @State private var passwordMismatch = false

    init(
        authRepository: AuthRepository,
        onNavigateToLogin: @escaping () -> Void,
        onAuthSuccess: @escaping () -> Void
    ) {
        self.onNavigateToLogin = onNavigateToLogin
        self.onAuthSuccess = onAuthSuccess
        _viewModel = State(initialValue: AuthViewModel(authRepository: authRepository))
    }

    var body: some View {
        VStack(spacing: 0) {
            Spacer()

            VStack(spacing: 24) {
                Text("Create account")
                    .font(.largeTitle.bold())

                VStack(spacing: 12) {
                    TextField("Email", text: $email)
                        .textFieldStyle(.roundedBorder)
                        .textContentType(.emailAddress)
                        .keyboardType(.emailAddress)
                        .autocorrectionDisabled()
                        .textInputAutocapitalization(.never)

                    SecureField("Password", text: $password)
                        .textFieldStyle(.roundedBorder)
                        .textContentType(.newPassword)
                        .onChange(of: password) { _, _ in passwordMismatch = false }

                    VStack(alignment: .leading, spacing: 4) {
                        SecureField("Confirm Password", text: $confirmPassword)
                            .textFieldStyle(.roundedBorder)
                            .textContentType(.newPassword)
                            .onChange(of: confirmPassword) { _, _ in passwordMismatch = false }
                        if passwordMismatch {
                            Text("Passwords do not match")
                                .font(.caption)
                                .foregroundStyle(.red)
                        }
                    }
                }

                if let error = viewModel.uiState.error {
                    Text(error)
                        .font(.caption)
                        .foregroundStyle(.red)
                        .frame(maxWidth: .infinity, alignment: .leading)
                }

                VStack(spacing: 12) {
                    Button {
                        guard password == confirmPassword else {
                            passwordMismatch = true
                            return
                        }
                        viewModel.register(email: email, password: password)
                    } label: {
                        Group {
                            if viewModel.uiState.isLoading {
                                ProgressView().tint(.white)
                            } else {
                                Text("Create Account").fontWeight(.semibold)
                            }
                        }
                        .frame(maxWidth: .infinity)
                    }
                    .buttonStyle(.borderedProminent)
                    .controlSize(.large)
                    .disabled(viewModel.uiState.isLoading || email.isEmpty || password.isEmpty || confirmPassword.isEmpty)

                    Button {
                        signUpWithGoogle()
                    } label: {
                        Label("Sign up with Google", systemImage: "globe")
                            .frame(maxWidth: .infinity)
                    }
                    .buttonStyle(.bordered)
                    .controlSize(.large)
                    .disabled(viewModel.uiState.isLoading)
                }
            }
            .padding(.horizontal, 24)

            Spacer()

            Button(action: onNavigateToLogin) {
                Text("Already have an account? ")
                    .foregroundStyle(.secondary)
                + Text("Sign In").bold()
            }
            .padding(.bottom, 32)
        }
        .onChange(of: viewModel.uiState.isAuthenticated) { _, isAuthenticated in
            if isAuthenticated { onAuthSuccess() }
        }
    }

    private func signUpWithGoogle() {
        guard let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
              let rootVC = windowScene.windows.first?.rootViewController else { return }
        GIDSignIn.sharedInstance.signIn(withPresenting: rootVC) { result, error in
            guard let idToken = result?.user.idToken?.tokenString else { return }
            viewModel.signInWithGoogle(idToken: idToken)
        }
    }
}
