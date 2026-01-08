Project Overview: Subly
Core Architecture
Tech Stack Recommendation:

Language: Kotlin
Architecture: MVVM with Clean Architecture principles
UI: Jetpack Compose (modern, declarative UI)
Database: Room (local SQLite with offline-first approach)
Dependency Injection: Hilt
Notifications: WorkManager for scheduled reminders
State Management: StateFlow/ViewModel

Phase 1: Data Model & Core Structure
Entities

// Subscription Entity
data class Subscription(
    val id: UUID,
    val name: String,
    val type: SubscriptionType,
    val amount: Double,
    val currency: String,
    val frequency: BillingFrequency,
    val startDate: LocalDate,
    val nextBillingDate: LocalDate,
    val paymentMethodId: UUID,
    val notes: String?,
    val isActive: Boolean,
    val reminderDaysBefore: Int = 2
)

// Payment Method Entity (non-sensitive)
data class PaymentMethod(
    val id: UUID,
    val nickname: String, // e.g., "US Bank Rewards", "Amex Platinum"
    val type: PaymentType, // Credit, Debit, PayPal, etc.
    val lastFourDigits: String?, // Optional, for reference only
    val icon: Int // Drawable resource for visual identification
)

// Enums
enum class SubscriptionType {
    STREAMING, MAGAZINE, SERVICE, MEMBERSHIP, CLUB, UTILITY, SOFTWARE, OTHER
}

enum class BillingFrequency {
    WEEKLY, MONTHLY, QUARTERLY, SEMI_ANNUAL, ANNUAL, CUSTOM
}

enum class PaymentType {
    CREDIT_CARD, DEBIT_CARD, PAYPAL, BANK_TRANSFER, CASH, OTHER
}
```

---

## **Phase 2: Feature Breakdown**

### **1. Subscription Management**
- **Add/Edit Subscription**
  - Material 3 bottom sheet or full-screen form
  - Date pickers for start date
  - Dropdown for type and frequency
  - Amount input with currency selector
  - Payment method selector
  - Custom reminder days setting
  
- **List View**
  - Categorized by type or sort by next billing date
  - Card-based design with key info at a glance
  - Swipe actions (edit, delete, mark as paid)
  - Search and filter functionality

- **Detail View**
  - Full subscription information
  - Payment history (future enhancement)
  - Quick actions (pay now, skip, cancel)

### **2. Dashboard/Home Screen**
- **Upcoming Bills Widget**
  - Next 7-30 days overview
  - Total upcoming costs
  - Overdue subscriptions highlighted
  
- **Analytics Cards**
  - Monthly/yearly total spend
  - Spending by category (pie chart)
  - Most expensive subscriptions
  - Cost trends over time

### **3. Payment Method Management**
- **Add/Edit Payment Methods**
  - Simple form with nickname and type
  - Optional last 4 digits for reference
  - Icon/color picker for visual identification
  
- **No Sensitive Data**
  - Clear disclaimer: "We don't store full card numbers or CVV"
  - Focus on tracking which method was used, not the details

### **4. Notification System**
- **Reminder Notifications**
  - WorkManager periodic job checking upcoming bills
  - Customizable reminder timing per subscription
  - Rich notifications with quick actions
  - Group notifications for multiple subscriptions
  
- **Settings**
  - Toggle notifications on/off globally
  - Custom reminder times
  - Notification sound/vibration preferences

---

## **Phase 3: UI/UX Design**

### **Design Principles**
- **Material 3 Design** (Material You)
  - Dynamic color theming
  - Consistent spacing and typography
  - Smooth animations and transitions
  
- **Clean & Modern**
  - Ample white space
  - Card-based layouts
  - Bottom navigation for main sections
  - FAB for quick add action
  
- **Color Coding**
  - Subscription types with distinct colors
  - Status indicators (active, due soon, overdue)
  - Payment method icons with brand colors

### **Key Screens**

1. **Home/Dashboard**
   - Summary cards at top
   - Upcoming bills list
   - Quick stats

2. **Subscriptions List**
   - Filterable/sortable list
   - Category tabs or chips
   - Search bar

3. **Add/Edit Subscription**
   - Step-by-step form or single scrollable form
   - Clear validation feedback
   - Save draft capability

4. **Settings**
   - Notification preferences
   - Currency settings
   - Data backup/export
   - About/Help

---

## **Phase 4: Technical Implementation Plan**

### **Project Structure**
```
app/
├── data/
│   ├── local/
│   │   ├── dao/
│   │   ├── entities/
│   │   └── database/
│   ├── repository/
│   └── model/
├── domain/
│   ├── usecase/
│   └── model/
├── ui/
│   ├── home/
│   ├── subscriptions/
│   ├── detail/
│   ├── payment/
│   └── settings/
├── utils/
└── notifications/

Key Components

Database Setup (Room)

SubscriptionDao
PaymentMethodDao
Migration strategy
Backup/restore functionality


Repository Layer

SubscriptionRepository
PaymentMethodRepository
Handle CRUD operations
Expose Flow for reactive updates


WorkManager Setup

DailyReminderWorker
Check subscriptions due in next 1-2 days
Schedule notifications
Handle rescheduling


ViewModels

HomeViewModel (dashboard data)
SubscriptionListViewModel
SubscriptionDetailViewModel
AddEditSubscriptionViewModel




Phase 5: Development Milestones
Milestone 1: Core Setup (Week 1-2)

Project structure and dependencies
Database schema and Room setup
Basic navigation structure
Main screen skeleton

Milestone 2: Subscription CRUD (Week 3-4)

Add subscription form
List view with basic cards
Edit and delete functionality
Payment method management

Milestone 3: UI Polish (Week 5)

Material 3 theming
Animations and transitions
Icons and visual design
Responsive layouts

Milestone 4: Reminders (Week 6)

WorkManager implementation
Notification creation
Permission handling (Android 13+)
Testing reminder logic

Milestone 5: Dashboard & Analytics (Week 7)

Home screen with stats
Chart implementation
Filtering and sorting
Search functionality

Milestone 6: Testing & Refinement (Week 8)

Unit tests for ViewModels
Repository tests
UI tests with Compose
Bug fixes and optimization


Additional Considerations
Security & Privacy

No sensitive payment data stored
Local-only storage (no cloud sync initially)
App lock with biometric authentication (optional feature)
Clear privacy policy

Future Enhancements

Export data (CSV/PDF)
Backup to Google Drive
Bill splitting for shared subscriptions
Receipt photo attachments
Free trial tracking
Subscription price change alerts
Multi-currency support

Monetization (Optional)

Free with basic features
Premium for analytics, unlimited subscriptions, themes
One-time purchase model


Next Steps

Validate Requirements - Any features to add/remove?
Finalize App Name - SubTrack, BillWatch, SubscriptMe?
UI Mockups - Sketch out key screens
Start with MVP - Focus on core CRUD and reminders first
Iterate - Add polish and advanced features
