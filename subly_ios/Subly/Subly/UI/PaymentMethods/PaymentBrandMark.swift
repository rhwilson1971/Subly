import SwiftUI

/// Chip-style brand mark for a `PaymentType`: a colored/text mark for
/// recognizable card networks and payment services, or a tinted system
/// icon for the rest.
struct PaymentBrandMark: View {
    let type: PaymentType
    var size: CGFloat = 44

    private var cornerRadius: CGFloat { size * 0.22 }

    var body: some View {
        ZStack {
            RoundedRectangle(cornerRadius: cornerRadius)
                .fill(backgroundColor)
                .overlay(
                    RoundedRectangle(cornerRadius: cornerRadius)
                        .stroke(borderColor, lineWidth: 1)
                )
            content
        }
        .frame(width: size, height: size)
    }

    @ViewBuilder
    private var content: some View {
        switch type {
        case .visa:
            brandText("VISA", color: Color(red: 0.102, green: 0.122, blue: 0.443), sizeRatio: 0.24, italic: true)

        case .mastercard:
            ZStack {
                Circle()
                    .fill(Color(red: 0.922, green: 0.0, blue: 0.106))
                    .frame(width: size * 0.5, height: size * 0.5)
                    .offset(x: -size * 0.125)
                Circle()
                    .fill(Color(red: 0.969, green: 0.620, blue: 0.106))
                    .frame(width: size * 0.5, height: size * 0.5)
                    .offset(x: size * 0.125)
            }

        case .amex:
            brandText("AMEX", color: .white, sizeRatio: 0.24)

        case .discover:
            ZStack {
                brandText("Discover", color: .primary, sizeRatio: 0.16)
                Circle()
                    .fill(Color(red: 1.0, green: 0.4, blue: 0.0))
                    .frame(width: size * 0.14, height: size * 0.14)
                    .offset(x: size * 0.22, y: size * 0.14)
            }

        case .paypal:
            HStack(spacing: 0) {
                brandText("Pay", color: Color(red: 0.0, green: 0.188, blue: 0.529), sizeRatio: 0.17)
                brandText("Pal", color: Color(red: 0.0, green: 0.612, blue: 0.871), sizeRatio: 0.17)
            }

        case .venmo:
            brandText("V", color: .white, sizeRatio: 0.42)

        case .cashApp:
            brandText("$", color: .white, sizeRatio: 0.42)

        case .affirm:
            ZStack {
                brandText("affirm", color: .white, sizeRatio: 0.16)
                Circle()
                    .fill(Color(red: 0.290, green: 0.290, blue: 0.957))
                    .frame(width: size * 0.09, height: size * 0.09)
                    .offset(x: size * 0.27, y: -size * 0.12)
            }

        case .klarna:
            brandText("Klarna", color: .black, sizeRatio: 0.17)

        case .debitCard, .bankTransfer, .cash, .other:
            Image(systemName: type.sfSymbol)
                .font(.system(size: size * 0.42))
                .foregroundColor(.secondary)
        }
    }

    private var backgroundColor: Color {
        switch type {
        case .amex: return Color(red: 0.0, green: 0.435, blue: 0.812)
        case .venmo: return Color(red: 0.0, green: 0.549, blue: 1.0)
        case .cashApp, .affirm: return .black
        case .klarna: return Color(red: 1.0, green: 0.702, blue: 0.780)
        default: return Color(.secondarySystemBackground)
        }
    }

    private var borderColor: Color {
        switch type {
        case .visa, .mastercard, .discover, .paypal,
             .debitCard, .bankTransfer, .cash, .other:
            return Color(.separator)
        default:
            return .clear
        }
    }

    private func brandText(_ text: String, color: Color, sizeRatio: CGFloat, italic: Bool = false) -> some View {
        Group {
            if italic {
                Text(text).font(.custom("Georgia-BoldItalic", size: size * sizeRatio))
            } else {
                Text(text).font(.custom("Georgia-Bold", size: size * sizeRatio))
            }
        }
        .foregroundColor(color)
        .lineLimit(1)
        .minimumScaleFactor(0.5)
    }
}

#Preview {
    VStack(alignment: .leading, spacing: 12) {
        ForEach(PaymentType.allCases, id: \.self) { type in
            HStack {
                PaymentBrandMark(type: type)
                Text(type.displayName)
            }
        }
    }
    .padding()
}
