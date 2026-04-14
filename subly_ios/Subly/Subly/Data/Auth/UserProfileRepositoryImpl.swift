import Foundation

/// Stub — full Firestore-backed implementation delivered in HIB-192.
final class UserProfileRepositoryImpl: UserProfileRepository {
    func saveProfile(_ user: User) async -> Result<Void, Error> {
        // TODO HIB-192: persist to Firestore users/{uid}
        return .success(())
    }

    func getProfile(uid: String) async -> Result<User?, Error> {
        // TODO HIB-192: fetch from Firestore users/{uid}
        return .success(nil)
    }

    func markOnboardingCompleted(uid: String) async -> Result<Void, Error> {
        // TODO HIB-192: update onboardingCompleted flag in Firestore
        return .success(())
    }
}
