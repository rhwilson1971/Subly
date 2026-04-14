# Subly — Firestore Schema

Shared data contract used by both the Android and iOS clients.
Both platforms connect directly to the same Firebase project using their respective SDKs.

---

## Collections

### `users/{userId}`

| Field | Type | Notes |
|---|---|---|
| `displayName` | String | Set during profile setup |
| `email` | String | Firebase Auth email |
| `createdAt` | Timestamp | Account creation time |
| `onboardingCompleted` | Boolean | Whether onboarding flow has been completed |

---

### `users/{userId}/subscriptions/{subscriptionId}`

| Field | Type | Notes |
|---|---|---|
| `id` | String | UUID |
| `name` | String | e.g. "Netflix" |
| `type` | String | Enum: `STREAMING`, `MAGAZINE`, `SERVICE`, `MEMBERSHIP`, `CLUB`, `UTILITY`, `SOFTWARE`, `OTHER` |
| `amount` | Number | Decimal |
| `currency` | String | ISO 4217 code, e.g. `USD` |
| `frequency` | String | Enum: `WEEKLY`, `MONTHLY`, `QUARTERLY`, `SEMI_ANNUAL`, `ANNUAL`, `CUSTOM` |
| `startDate` | String | ISO 8601 date `YYYY-MM-DD` |
| `nextBillingDate` | String | ISO 8601 date `YYYY-MM-DD` |
| `paymentMethodId` | String | Reference to payment method UUID |
| `notes` | String? | Optional |
| `isActive` | Boolean | |
| `reminderDaysBefore` | Number | Days before billing to notify (1–30) |
| `categoryId` | String? | Optional reference to category UUID |

---

### `users/{userId}/paymentMethods/{paymentMethodId}`

| Field | Type | Notes |
|---|---|---|
| `id` | String | UUID |
| `nickname` | String | e.g. "US Bank Rewards" |
| `type` | String | Enum: `CREDIT_CARD`, `DEBIT_CARD`, `PAYPAL`, `BANK_TRANSFER`, `CASH`, `OTHER` |
| `lastFourDigits` | String? | Optional, display only — no sensitive data stored |

---

### `users/{userId}/categories/{categoryId}`

| Field | Type | Notes |
|---|---|---|
| `id` | String | UUID |
| `name` | String | Display name |
| `colorHex` | String | e.g. `#FF5733` |

---

## Notes

- No full card numbers or CVV are ever stored.
- All monetary amounts are stored as plain decimals; currency is always stored alongside.
- Dates are stored as ISO 8601 strings (`YYYY-MM-DD`) for cross-platform compatibility.
