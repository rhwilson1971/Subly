import Foundation

enum SubscriptionType: String, Codable, CaseIterable {
    case streaming  = "STREAMING"
    case magazine   = "MAGAZINE"
    case service    = "SERVICE"
    case membership = "MEMBERSHIP"
    case club       = "CLUB"
    case utility    = "UTILITY"
    case software   = "SOFTWARE"
    case other      = "OTHER"

    var displayName: String {
        switch self {
        case .streaming:  return "Streaming"
        case .magazine:   return "Magazine"
        case .service:    return "Service"
        case .membership: return "Membership"
        case .club:       return "Club"
        case .utility:    return "Utility"
        case .software:   return "Software"
        case .other:      return "Other"
        }
    }
}
