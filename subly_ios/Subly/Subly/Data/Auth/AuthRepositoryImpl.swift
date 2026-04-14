import Foundation
import FirebaseAuth
import GoogleSignIn

/// Wraps Firebase Auth — mirrors Android `AuthRepositoryImpl`.
final class AuthRepositoryImpl: AuthRepository {

    private let auth = Auth.auth()

    var currentUser: User? {
        guard let firebaseUser = auth.currentUser else { return nil }
        return User(
            uid: firebaseUser.uid,
            email: firebaseUser.email,
            displayName: firebaseUser.displayName,
            profilePictureUrl: firebaseUser.photoURL?.absoluteString
        )
    }

    var isLoggedIn: Bool { auth.currentUser != nil }

    func signInWithEmail(email: String, password: String) async -> Result<User, Error> {
        do {
            let result = try await auth.signIn(withEmail: email, password: password)
            let user = map(result.user)
            await syncOnLogin(uid: user.uid)
            return .success(user)
        } catch {
            return .failure(error)
        }
    }

    func registerWithEmail(email: String, password: String) async -> Result<User, Error> {
        do {
            let result = try await auth.createUser(withEmail: email, password: password)
            let user = map(result.user)
            await syncOnLogin(uid: user.uid)
            return .success(user)
        } catch {
            return .failure(error)
        }
    }

    func signInWithGoogle(idToken: String) async -> Result<User, Error> {
        do {
            let credential = GoogleAuthProvider.credential(withIDToken: idToken, accessToken: "")
            let result = try await auth.signIn(with: credential)
            let user = map(result.user)
            await syncOnLogin(uid: user.uid)
            return .success(user)
        } catch {
            return .failure(error)
        }
    }

    func signOut() async {
        try? auth.signOut()
    }

    func syncOnLogin(uid: String) async {
        // Full Firestore pull implemented in HIB-192 (FirestoreSyncOrchestrator)
    }

    // MARK: - Private

    private func map(_ firebaseUser: FirebaseAuth.User) -> User {
        User(
            uid: firebaseUser.uid,
            email: firebaseUser.email,
            displayName: firebaseUser.displayName,
            profilePictureUrl: firebaseUser.photoURL?.absoluteString
        )
    }
}
