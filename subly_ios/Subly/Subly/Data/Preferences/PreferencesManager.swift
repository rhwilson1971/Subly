import Foundation
import Combine
import SwiftUI

enum ThemePreference: String, CaseIterable {
    case system = "SYSTEM"
    case light  = "LIGHT"
    case dark   = "DARK"

    var displayName: String {
        switch self {
        case .system: return "System"
        case .light:  return "Light"
        case .dark:   return "Dark"
        }
    }

    var colorScheme: ColorScheme? {
        switch self {
        case .system: return nil
        case .light:  return .light
        case .dark:   return .dark
        }
    }
}

enum StorageProviderPreference: String, CaseIterable {
    case local       = "LOCAL"
    case firebase    = "FIREBASE"
    case googleDrive = "GOOGLE_DRIVE"
    case dropbox     = "DROPBOX"
    case oneDrive    = "ONE_DRIVE"

    var displayName: String {
        switch self {
        case .local:       return "Local Only"
        case .firebase:    return "Firebase"
        case .googleDrive: return "Google Drive"
        case .dropbox:     return "Dropbox"
        case .oneDrive:    return "OneDrive"
        }
    }

    var sfSymbol: String {
        switch self {
        case .local:       return "internaldrive"
        case .firebase:    return "flame"
        case .googleDrive: return "externaldrive.badge.icloud"
        case .dropbox:     return "archivebox"
        case .oneDrive:    return "icloud"
        }
    }
}

/// Centralised preferences store backed by UserDefaults via @AppStorage.
/// Mirrors Android's PreferencesManager / DataStore.
@MainActor
final class PreferencesManager: ObservableObject {
    static let shared = PreferencesManager()

    @AppStorage("notificationsEnabled")    var notificationsEnabled: Bool = true
    @AppStorage("morningReminderEnabled")  var morningReminderEnabled: Bool = true
    @AppStorage("eveningReminderEnabled")  var eveningReminderEnabled: Bool = false
    @AppStorage("morningReminderTime")     var morningReminderTime: String = "09:00"
    @AppStorage("eveningReminderTime")     var eveningReminderTime: String = "18:00"
    @AppStorage("reminderDaysBefore")      var reminderDaysBefore: Int = 2
    @AppStorage("theme")                   private var _theme: String = ThemePreference.system.rawValue
    @AppStorage("storageProvider")         private var _storageProvider: String = StorageProviderPreference.firebase.rawValue

    var theme: ThemePreference {
        get { ThemePreference(rawValue: _theme) ?? .system }
        set { _theme = newValue.rawValue }
    }

    var storageProvider: StorageProviderPreference {
        get { StorageProviderPreference(rawValue: _storageProvider) ?? .firebase }
        set { _storageProvider = newValue.rawValue }
    }

    private init() {}
}
