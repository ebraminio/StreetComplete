package de.westnordost.streetcomplete.data.osmnotes

import de.westnordost.streetcomplete.data.NotesApi
import de.westnordost.osmapi.common.Handler
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.notes.Note
import de.westnordost.streetcomplete.*
import de.westnordost.streetcomplete.data.user.UserStore
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.verify
import java.util.concurrent.atomic.AtomicBoolean

class NotesDownloaderTest {
    private lateinit var notesApi: NotesApi
    private lateinit var noteController: NoteController
    private lateinit var userStore: UserStore

    @Before fun setUp() {
        notesApi = mock()
        noteController = mock()

        userStore = mock()
        on(userStore.userId).thenReturn(1L)
    }

    @Test fun `calls controller with all notes coming from the notes api`() {
        val note1 = note()
        val noteApi = TestListBasedNotesApi(listOf(note1))
        val dl = NotesDownloader(noteApi, userStore, noteController)
        val bbox = bbox()
        dl.download(bbox, AtomicBoolean(false))

        verify(noteController).putAllForBBox(eq(bbox), eq(listOf(note1)))
    }
}

private class TestListBasedNotesApi(val notes: List<Note>) :  NotesApi(null) {
    override fun getAll(bounds: BoundingBox, handler: Handler<Note>, limit: Int, hideClosedNoteAfter: Int) {
        for (note in notes) {
            handler.handle(note)
        }
    }
}
