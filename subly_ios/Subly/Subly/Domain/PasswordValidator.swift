import Foundation

/// Minimum bar for a "strong" password: 8+ characters, at least one
/// uppercase letter, one lowercase letter, and one digit. Mirrors Android's
/// `PasswordValidator` — keep the two in sync if the rule changes.
enum PasswordValidator {
    static let minLength = 8

    static func errors(_ password: String) -> [String] {
        var errors: [String] = []
        if password.count < minLength { errors.append("At least \(minLength) characters") }
        if !password.contains(where: { $0.isUppercase }) { errors.append("At least one uppercase letter") }
        if !password.contains(where: { $0.isLowercase }) { errors.append("At least one lowercase letter") }
        if !password.contains(where: { $0.isNumber }) { errors.append("At least one number") }
        return errors
    }

    static func isStrong(_ password: String) -> Bool { errors(password).isEmpty }
}
