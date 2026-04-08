package net.cynreub.subly.ui.categories

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import net.cynreub.subly.domain.model.Category
import net.cynreub.subly.domain.repository.CategoryRepository
import net.cynreub.subly.util.MainCoroutineRule
import net.cynreub.subly.util.testCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.UUID

class CategoriesViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    private val categoryRepository: CategoryRepository = mockk()

    private val streaming = testCategory(
        id = UUID.randomUUID(),
        name = "STREAMING",
        displayName = "Streaming"
    )
    private val music = testCategory(
        id = UUID.randomUUID(),
        name = "MUSIC",
        displayName = "Music"
    )
    private val streamingWithCount = CategoryWithCount(streaming, subscriptionCount = 2)
    private val musicWithCount = CategoryWithCount(music, subscriptionCount = 0)

    @Before
    fun setUp() {
        every { categoryRepository.getAllCategoriesWithCount() } returns flowOf(
            listOf(streamingWithCount, musicWithCount)
        )
    }

    private fun createViewModel() = CategoriesViewModel(categoryRepository)

    // ── Load categories ────────────────────────────────────────────────────────

    @Test
    fun `loads categories on init`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(2, state.categories.size)
            assertFalse(state.isLoading)
            assertNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `error state set when repository throws`() = runTest {
        every { categoryRepository.getAllCategoriesWithCount() } returns flow {
            throw RuntimeException("Load failed")
        }

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertNotNull(state.error)
            assertTrue(state.error!!.contains("Load failed"))
            assertFalse(state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Sheet management ───────────────────────────────────────────────────────

    @Test
    fun `openAddSheet sets showAddEditSheet true with null editingCategory`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // loaded

            viewModel.openAddSheet()
            val state = awaitItem()
            assertTrue(state.showAddEditSheet)
            assertNull(state.editingCategory)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `openEditSheet sets showAddEditSheet true with correct category`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.openEditSheet(streaming)
            val state = awaitItem()
            assertTrue(state.showAddEditSheet)
            assertEquals(streaming, state.editingCategory)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `closeSheet clears sheet state`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.openAddSheet()
            awaitItem()

            viewModel.closeSheet()
            val state = awaitItem()
            assertFalse(state.showAddEditSheet)
            assertNull(state.editingCategory)
            assertNull(state.saveError)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Save category – validation ─────────────────────────────────────────────

    @Test
    fun `saveCategory with blank name sets saveError`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.saveCategory("   ", "📺", "#E91E63")
            val state = awaitItem()
            assertEquals("Name cannot be empty", state.saveError)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saveCategory with duplicate name sets saveError`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.saveCategory("Streaming", "📺", "#E91E63")
            val state = awaitItem()
            assertNotNull(state.saveError)
            assertTrue(state.saveError!!.contains("already exists"))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saveCategory duplicate check is case-insensitive`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.saveCategory("STREAMING", "📺", "#E91E63")
            val state = awaitItem()
            assertNotNull(state.saveError)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saveCategory allows duplicate name when editing same category`() = runTest {
        coEvery { categoryRepository.updateCategory(any()) } returns Unit
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.openEditSheet(streaming)
            awaitItem()

            // Same name as the category being edited should be allowed.
            // With UnconfinedTestDispatcher the coroutine runs eagerly; the single emitted
            // state has isSaving = false and the sheet closed.
            viewModel.saveCategory("Streaming", "📺", "#E91E63")

            val done = awaitItem()
            assertFalse(done.isSaving)
            assertFalse(done.showAddEditSheet)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Save category – insert ─────────────────────────────────────────────────

    @Test
    fun `saveCategory in add mode calls insertCategory`() = runTest {
        coEvery { categoryRepository.insertCategory(any()) } returns Unit
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            // Open the sheet so the state changes visibly when the sheet is dismissed on save.
            viewModel.openAddSheet()
            awaitItem() // showAddEditSheet = true

            viewModel.saveCategory("Gaming", "🎮", "#9C27B0")

            // Final state: sheet dismissed, not saving.
            val done = awaitItem()
            assertFalse(done.isSaving)
            assertFalse(done.showAddEditSheet)

            coVerify { categoryRepository.insertCategory(any()) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saveCategory sets saveError when insertCategory throws`() = runTest {
        coEvery { categoryRepository.insertCategory(any()) } throws RuntimeException("Insert failed")
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.saveCategory("Gaming", "🎮", "#9C27B0")

            val errorState = awaitItem()
            assertNotNull(errorState.saveError)
            assertTrue(errorState.saveError!!.contains("Insert failed"))
            assertFalse(errorState.isSaving)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Save category – update ─────────────────────────────────────────────────

    @Test
    fun `saveCategory in edit mode calls updateCategory with correct fields`() = runTest {
        coEvery { categoryRepository.updateCategory(any()) } returns Unit
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.openEditSheet(streaming)
            awaitItem()

            viewModel.saveCategory("Streaming+", "📺", "#FF0000")

            val done = awaitItem()
            assertFalse(done.isSaving)

            coVerify {
                categoryRepository.updateCategory(
                    streaming.copy(
                        displayName = "Streaming+",
                        name = "STREAMING+",
                        emoji = "📺",
                        colorHex = "#FF0000"
                    )
                )
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Delete management ──────────────────────────────────────────────────────

    @Test
    fun `requestDelete sets deleteCandidate`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.requestDelete(streamingWithCount)
            val state = awaitItem()
            assertEquals(streamingWithCount, state.deleteCandidate)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `dismissDeleteDialog clears deleteCandidate`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.requestDelete(streamingWithCount)
            awaitItem()

            viewModel.dismissDeleteDialog()
            val state = awaitItem()
            assertNull(state.deleteCandidate)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `confirmDelete calls deleteCategory and clears candidate`() = runTest {
        coEvery { categoryRepository.deleteCategory(any()) } returns Unit
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.requestDelete(musicWithCount)
            awaitItem()

            viewModel.confirmDelete()

            // With UnconfinedTestDispatcher the coroutine completes eagerly — final state only.
            val done = awaitItem()
            assertFalse(done.isDeleting)
            assertNull(done.deleteCandidate)
            assertNull(done.deleteError)

            coVerify { categoryRepository.deleteCategory(music) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `confirmDelete sets deleteError on IllegalStateException`() = runTest {
        coEvery { categoryRepository.deleteCategory(any()) } throws IllegalStateException("In use")
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.requestDelete(streamingWithCount)
            awaitItem()

            viewModel.confirmDelete()

            val errorState = awaitItem()
            assertEquals("In use", errorState.deleteError)
            assertFalse(errorState.isDeleting)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `confirmDelete sets deleteError on generic exception`() = runTest {
        coEvery { categoryRepository.deleteCategory(any()) } throws RuntimeException("DB error")
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.requestDelete(musicWithCount)
            awaitItem()

            viewModel.confirmDelete()

            val errorState = awaitItem()
            assertNotNull(errorState.deleteError)
            assertTrue(errorState.deleteError!!.contains("DB error"))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `confirmDelete does nothing when no candidate`() = runTest {
        val viewModel = createViewModel()

        viewModel.confirmDelete() // no-op, no candidate set

        coVerify(exactly = 0) { categoryRepository.deleteCategory(any()) }
    }
}
