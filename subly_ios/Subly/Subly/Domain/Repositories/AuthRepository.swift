import Foundation

protocol AuthRepository {
    var currentUser: User? { get }
    var isLoggedIn: Bool { get }
    func signInWithEmail(email: String, password: String) async -> Result<User, Error>
    func registerWithEmail(email: String, password: String) async -> Result<User, Error>
    func signInWithGoogle(idToken: String) async -> Result<User, Error>
    func signOut() async
    func syncOnLogin(uid: String) async
}
