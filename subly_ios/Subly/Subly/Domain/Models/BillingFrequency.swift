import Foundation

enum BillingFrequency: String, Codable, CaseIterable {
    case weekly     = "WEEKLY"
    case monthly    = "MONTHLY"
    case quarterly  = "QUARTERLY"
    case semiAnnual = "SEMI_ANNUAL"
    case annual     = "ANNUAL"
    case custom     = "CUSTOM"

    var displayName: String {
        switch self {
        case .weekly:     return "Weekly"
        case .monthly:    return "Monthly"
        case .quarterly:  return "Quarterly"
        case .semiAnnual: return "Semi-Annual"
        case .annual:     return "Annual"
        case .custom:     return "Custom"
        }
    }

    /// Multiplier to convert this frequency's amount to a monthly equivalent
    var monthlyMultiplier: Double {
        switch self {
        case .weekly:     return 4.0
        case .monthly:    return 1.0
        case .quarterly:  return 1.0 / 3.0
        case .semiAnnual: return 1.0 / 6.0
        case .annual:     return 1.0 / 12.0
        case .custom:     return 1.0
        }
    }
}
