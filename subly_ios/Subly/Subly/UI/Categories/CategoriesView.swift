import SwiftUI

struct CategoriesView: View {
    @EnvironmentObject private var services: ServiceContainer
    @State private var viewModel: CategoriesViewModel?

    var body: some View {
        NavigationStack {
            Group {
                if let viewModel {
                    content(viewModel: viewModel)
                } else {
                    ProgressView()
                }
            }
            .navigationTitle("Categories")
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        viewModel?.openAddSheet()
                    } label: {
                        Image(systemName: "plus")
                    }
                }
            }
        }
        .task {
            if viewModel == nil {
                viewModel = CategoriesViewModel(categoryRepository: services.categoryRepository)
            }
        }
    }

    @ViewBuilder
    private func content(viewModel: CategoriesViewModel) -> some View {
        let state = viewModel.uiState

        List {
            ForEach(state.categories) { item in
                CategoryRow(item: item)
                    .contentShape(Rectangle())
                    .onTapGesture { viewModel.openEditSheet(category: item.category) }
                    .swipeActions(edge: .trailing, allowsFullSwipe: false) {
                        if item.subscriptionCount == 0 {
                            Button(role: .destructive) {
                                viewModel.requestDelete(item)
                            } label: {
                                Label("Delete", systemImage: "trash")
                            }
                        }
                    }
                    .swipeActions(edge: .leading) {
                        Button {
                            viewModel.openEditSheet(category: item.category)
                        } label: {
                            Label("Edit", systemImage: "pencil")
                        }
                        .tint(.blue)
                    }
            }
        }
        .listStyle(.plain)
        // Add/Edit sheet
        .sheet(isPresented: Binding(
            get: { viewModel.uiState.showAddEditSheet },
            set: { if !$0 { viewModel.closeSheet() } }
        )) {
            AddEditCategorySheet(
                editing: state.editingCategory,
                isSaving: state.isSaving,
                saveError: state.saveError,
                onSave: { name, emoji, color in
                    viewModel.saveCategory(displayName: name, emoji: emoji, colorHex: color)
                },
                onDismiss: { viewModel.closeSheet() }
            )
            .presentationDetents([.medium])
        }
        // Delete confirmation
        .confirmationDialog(
            "Delete Category",
            isPresented: Binding(
                get: { viewModel.uiState.deleteCandidate != nil },
                set: { if !$0 { viewModel.dismissDelete() } }
            ),
            titleVisibility: .visible
        ) {
            Button("Delete", role: .destructive) { viewModel.confirmDelete() }
            Button("Cancel", role: .cancel) { viewModel.dismissDelete() }
        } message: {
            if let c = state.deleteCandidate {
                
                let category = c.category.displayName.description
                
                Text("Delete \(category)? This cannot be undone.")
            }
        }
        // Delete error alert
        .alert("Error", isPresented: Binding(
            get: { state.deleteError != nil },
            set: { if !$0 { viewModel.uiState.deleteError = nil } }
        )) {
            Button("OK", role: .cancel) { viewModel.uiState.deleteError = nil }
        } message: {
            Text(state.deleteError ?? "")
        }
    }
}

// MARK: - Category Row

private struct CategoryRow: View {
    let item: CategoryWithCount

    var body: some View {
        HStack(spacing: 12) {
            Text(item.category.emoji)
                .font(.title2)
                .frame(width: 44, height: 44)
                .background(colorFromHex(item.category.colorHex).opacity(0.15))
                .clipShape(RoundedRectangle(cornerRadius: 10))

            VStack(alignment: .leading, spacing: 2) {
                Text(item.category.displayName)
                    .font(.body)
                    .fontWeight(.medium)
            }

            Spacer()

            if item.subscriptionCount > 0 {
                Text("\(item.subscriptionCount)")
                    .font(.caption)
                    .fontWeight(.semibold)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(Color.accentColor.opacity(0.1))
                    .foregroundColor(.accentColor)
                    .clipShape(Capsule())
            }
        }
        .padding(.vertical, 4)
    }

    private func colorFromHex(_ hex: String) -> Color {
        let h = hex.trimmingCharacters(in: .init(charactersIn: "#"))
        guard h.count == 6, let rgb = UInt64(h, radix: 16) else { return .accentColor }
        return Color(
            red:   Double((rgb >> 16) & 0xFF) / 255,
            green: Double((rgb >> 8)  & 0xFF) / 255,
            blue:  Double(rgb         & 0xFF) / 255
        )
    }
}

// MARK: - Add/Edit Category Sheet

private struct AddEditCategorySheet: View {
    let editing: Category?
    let isSaving: Bool
    let saveError: String?
    let onSave: (String, String, String) -> Void
    let onDismiss: () -> Void

    @State private var displayName: String
    @State private var emoji: String
    @State private var colorHex: String

    private let colorOptions: [(String, String)] = [
        ("#E91E63", "Pink"), ("#9C27B0", "Purple"), ("#2196F3", "Blue"),
        ("#00BCD4", "Cyan"), ("#4CAF50", "Green"), ("#8BC34A", "Light Green"),
        ("#FF9800", "Orange"), ("#FF5722", "Deep Orange"), ("#F44336", "Red"),
        ("#607D8B", "Blue Grey"), ("#795548", "Brown"), ("#9E9E9E", "Grey")
    ]

    init(editing: Category?, isSaving: Bool, saveError: String?,
         onSave: @escaping (String, String, String) -> Void,
         onDismiss: @escaping () -> Void) {
        self.editing = editing
        self.isSaving = isSaving
        self.saveError = saveError
        self.onSave = onSave
        self.onDismiss = onDismiss
        _displayName = State(initialValue: editing?.displayName ?? "")
        _emoji = State(initialValue: editing?.emoji ?? "📦")
        _colorHex = State(initialValue: editing?.colorHex ?? "#607D8B")
    }

    var body: some View {
        NavigationStack {
            Form {
                Section("Name") {
                    TextField("Category name", text: $displayName)
                }

                Section("Icon") {
                    HStack {
                        Text("Emoji")
                        Spacer()
                        TextField("", text: $emoji)
                            .multilineTextAlignment(.trailing)
                            .frame(width: 44)
                    }
                }

                Section("Color") {
                    LazyVGrid(columns: Array(repeating: GridItem(.flexible()), count: 6), spacing: 12) {
                        ForEach(colorOptions, id: \.0) { hex, _ in
                            Circle()
                                .fill(colorFromHex(hex))
                                .frame(width: 36, height: 36)
                                .overlay {
                                    if hex == colorHex {
                                        Image(systemName: "checkmark")
                                            .font(.caption)
                                            .fontWeight(.bold)
                                            .foregroundColor(.white)
                                    }
                                }
                                .onTapGesture { colorHex = hex }
                        }
                    }
                    .padding(.vertical, 4)
                }

                if let err = saveError {
                    Section {
                        Text(err).font(.footnote).foregroundColor(.red)
                    }
                }
            }
            .navigationTitle(editing == nil ? "New Category" : "Edit Category")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { onDismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Save") {
                        onSave(displayName, emoji, colorHex)
                    }
                    .disabled(isSaving || displayName.trimmingCharacters(in: .whitespaces).isEmpty)
                }
            }
        }
    }

    private func colorFromHex(_ hex: String) -> Color {
        let h = hex.trimmingCharacters(in: .init(charactersIn: "#"))
        guard h.count == 6, let rgb = UInt64(h, radix: 16) else { return .accentColor }
        return Color(
            red:   Double((rgb >> 16) & 0xFF) / 255,
            green: Double((rgb >> 8)  & 0xFF) / 255,
            blue:  Double(rgb         & 0xFF) / 255
        )
    }
}

#Preview {
    CategoriesView()
        .environmentObject(ServiceContainer())
}
