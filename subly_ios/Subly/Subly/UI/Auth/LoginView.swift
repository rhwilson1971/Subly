import SwiftUI
import GoogleSignIn
import FirebaseAuth

struct LoginView: View {
    let onNavigateToRegister: () -> Void
    let onAuthSuccess: () -> Void

    @State private var viewModel: AuthViewModel
    @State private var email = ""
    @State private var password = ""

    init(
        authRepository: AuthRepository,
        onNavigateToRegister: @escaping () -> Void,
        onAuthSuccess: @escaping () -> Void
    ) {
        self.onNavigateToRegister = onNavigateToRegister
        self.onAuthSuccess = onAuthSuccess
        _viewModel = State(initialValue: AuthViewModel(authRepository: authRepository))
    }

    var body: some View {
        VStack(spacing: 0) {
            Spacer()

            VStack(spacing: 24) {
                Text("Welcome back")
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
                        .textContentType(.password)
                }

                if let error = viewModel.uiState.error {
                    Text(error)
                        .font(.caption)
                        .foregroundStyle(.red)
                        .frame(maxWidth: .infinity, alignment: .leading)
                }

                VStack(spacing: 12) {
                    Button {
                        viewModel.signIn(email: email, password: password)
                    } label: {
                        Group {
                            if viewModel.uiState.isLoading {
                                ProgressView()
                                    .tint(.white)
                            } else {
                                Text("Sign In")
                                    .fontWeight(.semibold)
                            }
                        }
                        .frame(maxWidth: .infinity)
                    }
                    .buttonStyle(.borderedProminent)
                    .controlSize(.large)
                    .disabled(viewModel.uiState.isLoading || email.isEmpty || password.isEmpty)

                    Button {
                        signInWithGoogle()
                    } label: {
                        Label("Sign in with Google", systemImage: "globe")
                            .frame(maxWidth: .infinity)
                    }
                    .buttonStyle(.bordered)
                    .controlSize(.large)
                    .disabled(viewModel.uiState.isLoading)
                }
            }
            .padding(.horizontal, 24)

            Spacer()

            Button(action: onNavigateToRegister) {
                Text("Don't have an account? ")
                    .foregroundStyle(.secondary)
                + Text("Register").bold()
            }
            .padding(.bottom, 32)
        }
        .onChange(of: viewModel.uiState.isAuthenticated) { _, isAuthenticated in
            if isAuthenticated { onAuthSuccess() }
        }
    }

    private func signInWithGoogle() {
        guard let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
              let rootVC = windowScene.windows.first?.rootViewController else { return }
        GIDSignIn.sharedInstance.signIn(withPresenting: rootVC) { result, error in
            guard let idToken = result?.user.idToken?.tokenString else { return }
            viewModel.signInWithGoogle(idToken: idToken)
        }
    }
}
