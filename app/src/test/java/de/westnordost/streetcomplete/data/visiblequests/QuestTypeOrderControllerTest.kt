package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.quest.*
import de.westnordost.streetcomplete.testutils.any
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.on
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.verify

class QuestTypeOrderControllerTest {
    private lateinit var questTypeOrderDao: QuestTypeOrderDao
    private lateinit var questProfilesSource: QuestProfilesSource
    private lateinit var ctrl: QuestTypeOrderController
    private lateinit var listener: QuestTypeOrderSource.Listener

    private lateinit var questProfilesListener: QuestProfilesSource.Listener

    private val questA = TestQuestTypeA()
    private val questB = TestQuestTypeB()
    private val questC = TestQuestTypeC()
    private val questD = TestQuestTypeD()

    @Before fun setUp() {
        questTypeOrderDao = mock()
        questProfilesSource = mock()

        on(questProfilesSource.addListener(any())).then { invocation ->
            questProfilesListener = (invocation.arguments[0] as QuestProfilesSource.Listener)
            Unit
        }

        on(questProfilesSource.selectedQuestProfileId).thenReturn(0)

        ctrl = QuestTypeOrderController(questTypeOrderDao, questProfilesSource)

        listener = mock()
        ctrl.addListener(listener)
    }

    @Test fun `notifies listener when changing quest profile`() {
        questProfilesListener.onSelectedQuestProfileChanged()
        verify(listener).onQuestTypeOrdersChanged()
    }

    @Test fun sort() {
        val list = mutableListOf<QuestType<*>>(questA, questB, questC, questD)
        on(questTypeOrderDao.getAll(0)).thenReturn(listOf(
            // A,B,C,D -> A,D,B,C
            questD.name to questA.name,
            // A,D,B,C -> A,D,C,B
            questC.name to questD.name,
            // A,D,C,B -> D,C,B,A
            questA.name to questB.name
        ))

        ctrl.sort(list)
        assertEquals(
            listOf(questD, questC, questB, questA),
            list
        )
    }

    @Test fun `adding order item`() {
        ctrl.addOrderItem(questA, questB)
        verify(questTypeOrderDao).put(0, questA.name to questB.name)
        verify(listener).onQuestTypeOrderAdded(questA, questB)
    }

    @Test fun `clear orders`() {
        ctrl.clear()
        verify(questTypeOrderDao).clear(0)
        verify(listener).onQuestTypeOrdersChanged()
    }
}

private val QuestType<*>.name get() = this::class.simpleName!!
