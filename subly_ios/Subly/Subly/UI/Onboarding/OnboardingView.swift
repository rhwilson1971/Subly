import SwiftUI

struct OnboardingView: View {
    let onRequestSignUp: () -> Void
    let onDone: () -> Void

    @State private var viewModel = OnboardingViewModel()
    @State private var currentPage = 0

    var body: some View {
        VStack(spacing: 0) {
            HStack {
                Spacer()
                Button("Skip") {
                    viewModel.completeOnboarding()
                    onDone()
                }
                .padding()
            }

            TabView(selection: $currentPage) {
                ForEach(Array(viewModel.pages.enumerated()), id: \.offset) { index, page in
                    pageContent(page)
                        .tag(index)
                }
            }
            .tabViewStyle(.page(indexDisplayMode: .always))
            .onChange(of: currentPage) { _, newValue in
                viewModel.onPageChanged(newValue)
            }

            VStack(spacing: 8) {
                Button {
                    if currentPage == viewModel.pages.count - 1 {
                        viewModel.completeOnboarding()
                        onDone()
                    } else {
                        withAnimation { currentPage += 1 }
                    }
                } label: {
                    Text(viewModel.pages[currentPage].ctaLabel)
                        .fontWeight(.semibold)
                        .frame(maxWidth: .infinity)
                }
                .buttonStyle(.borderedProminent)
                .controlSize(.large)

                if currentPage == 1 {
                    Button("Set Up Sync Now", action: onRequestSignUp)
                        .buttonStyle(.bordered)
                        .controlSize(.large)
                        .frame(maxWidth: .infinity)
                }
            }
            .padding(.horizontal, 24)
            .padding(.bottom, 32)
        }
    }

    private func pageContent(_ page: OnboardingPage) -> some View {
        VStack(spacing: 24) {
            Spacer()
            Image(systemName: page.systemImage)
                .font(.system(size: 56))
                .frame(width: 120, height: 120)
                .background(Circle().fill(Color.accentColor.opacity(0.15)))
                .foregroundStyle(Color.accentColor)

            Text(page.title)
                .font(.title2.bold())
                .multilineTextAlignment(.center)

            Text(page.description)
                .font(.body)
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)

            Spacer()
        }
        .padding(.horizontal, 32)
    }
}
