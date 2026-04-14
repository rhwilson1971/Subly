import Foundation

protocol UserProfileRepository {
    func saveProfile(_ user: User) async -> Result<Void, Error>
    func getProfile(uid: String) async -> Result<User?, Error>
    func markOnboardingCompleted(uid: String) async -> Result<Void, Error>
}
