import Foundation

struct User: Equatable, Codable {
    var uid: String
    var email: String?
    var displayName: String?
    var fullName: String?
    var dateOfBirth: String?       // ISO-8601 (yyyy-MM-dd)
    var phoneNumber: String?
    var profilePictureUrl: String?
    var onboardingCompleted: Bool

    init(
        uid: String,
        email: String? = nil,
        displayName: String? = nil,
        fullName: String? = nil,
        dateOfBirth: String? = nil,
        phoneNumber: String? = nil,
        profilePictureUrl: String? = nil,
        onboardingCompleted: Bool = false
    ) {
        self.uid = uid
        self.email = email
        self.displayName = displayName
        self.fullName = fullName
        self.dateOfBirth = dateOfBirth
        self.phoneNumber = phoneNumber
        self.profilePictureUrl = profilePictureUrl
        self.onboardingCompleted = onboardingCompleted
    }
}
