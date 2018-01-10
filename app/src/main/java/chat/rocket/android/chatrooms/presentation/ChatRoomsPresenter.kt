package chat.rocket.android.chatrooms.presentation

import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.util.launchUI
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.model.Subscription
import chat.rocket.core.internal.realtime.*
import chat.rocket.core.internal.rest.chatRooms
import chat.rocket.core.model.ChatRoom
import chat.rocket.core.model.Room
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import timber.log.Timber
import javax.inject.Inject

class ChatRoomsPresenter @Inject constructor(private val view: ChatRoomsView,
                                             private val strategy: CancelStrategy,
                                             private val serverInteractor: GetCurrentServerInteractor,
                                             private val factory: RocketChatClientFactory) {
    private val client: RocketChatClient = factory.create(serverInteractor.get()!!)

    // FIXME - cache rooms on memory while we don't have a Database...
    private var chatRooms = ArrayList<ChatRoom>()
    private var reloadJob: Job? = null

    fun loadChatRooms() {
        launchUI(strategy) {
            view.showLoading()
            loadRooms()
            updateRooms()
            view.hideLoading()
            subscribeRoomUpdates()
        }
    }

    private suspend fun loadRooms() {
        val rooms = client.chatRooms().update
        chatRooms.clear()
        chatRooms.addAll(rooms)
    }

    private fun sortRooms(): List<ChatRoom> {
        val openChatRooms = getOpenChatRooms(chatRooms)
        return sortChatRooms(openChatRooms)
    }

    private fun updateRooms() {
        launch {
            view.updateChatRooms(sortRooms().toMutableList())
        }
    }

    private fun getOpenChatRooms(chatRooms: List<ChatRoom>): List<ChatRoom> {
        return chatRooms.filter(ChatRoom::open)
    }

    private fun sortChatRooms(chatRooms: List<ChatRoom>): List<ChatRoom> {
        return chatRooms.sortedByDescending {
            chatRoom -> chatRoom.lastMessage?.timestamp
        }
    }

    // TODO - Temporary stuff, remove when adding DB support
    private suspend fun subscribeRoomUpdates() {
        launch(CommonPool + strategy.jobs) {
            for (status in client.statusChannel) {
                Timber.d("Changing status to: $status")
                when (status) {
                    State.Authenticating -> Timber.d("Authenticating")
                    State.Connected -> {
                        Timber.d("Connected")
                        client.subscribeSubscriptions()
                        client.subscribeRooms()
                    }
                }
            }
            Timber.d("Done on statusChannel")
        }

        client.connect()

        launch(CommonPool + strategy.jobs) {
            for (message in client.roomsChannel) {
                Timber.d("Got message: $message")
                updateRoom(message)
            }
        }

        launch(CommonPool + strategy.jobs) {
            for (message in client.subscriptionsChannel) {
                Timber.d("Got message: $message")
                updateSubscription(message)
            }
        }
    }

    private fun updateRoom(message: StreamMessage<Room>) {
        launchUI(strategy) {
            when (message.type) {
                Type.Removed -> {
                    removeRoom(message.data.id)
                }
                Type.Updated -> {
                    updateRoom(message.data)
                }
                Type.Inserted -> {
                    // On insertion, just get all chatrooms again, since we can't create one just
                    // from a Room
                    reloadRooms()
                }
            }

            updateRooms()
        }
    }

    private fun updateSubscription(message: StreamMessage<Subscription>) {
        launchUI(strategy) {
            when (message.type) {
                Type.Removed -> {
                    removeRoom(message.data.roomId)
                }
                Type.Updated -> {
                    updateSubscription(message.data)
                }
                Type.Inserted -> {
                    // On insertion, just get all chatrooms again, since we can't create one just
                    // from a Subscription
                    reloadRooms()
                }
            }

            updateRooms()
        }
    }

    private suspend fun reloadRooms() {
        reloadJob?.cancel()

        reloadJob = launch(CommonPool + strategy.jobs) {
            delay(1000)
            Timber.d("reloading rooms after wait")
            loadRooms()
        }
        reloadJob?.join()
    }

    // Update a ChatRoom with a Room information
    private fun updateRoom(room: Room) {
        val chatRoom = chatRooms.find { chatRoom -> chatRoom.id == room.id }
        chatRoom?.apply {
            val newRoom = ChatRoom(room.id,
                    room.type,
                    room.user ?: user,
                    room.name ?: name,
                    room.fullName ?: fullName,
                    room.readonly,
                    room.updatedAt ?: updatedAt,
                    timestamp,
                    lastModified,
                    room.topic,
                    room.announcement,
                    default,
                    open,
                    alert,
                    unread,
                    userMenstions,
                    groupMentions,
                    room.lastMessage,
                    client)
            removeRoom(room.id)
            chatRooms.add(newRoom)
        }
    }

    // Update a ChatRoom with a Subscription information
    private fun updateSubscription(subscription: Subscription) {
        val chatRoom = chatRooms.find { chatRoom -> chatRoom.id == subscription.roomId }
        chatRoom?.apply {
            val newRoom = ChatRoom(subscription.roomId,
                    subscription.type,
                    subscription.user ?: user,
                    subscription.name,
                    subscription.fullName ?: fullName,
                    subscription.readonly ?: readonly,
                    subscription.updatedAt ?: updatedAt,
                    subscription.timestamp ?: timestamp,
                    subscription.lastModified ?: lastModified,
                    topic,
                    announcement,
                    subscription.isDefault,
                    subscription.open,
                    subscription.alert,
                    subscription.unread,
                    subscription.userMentions,
                    subscription.groupMentions,
                    lastMessage,
                    client)
            removeRoom(subscription.roomId)
            chatRooms.add(newRoom)
        }
    }

    private fun removeRoom(id: String) {
        synchronized(chatRooms) {
            chatRooms.removeAll { chatRoom -> chatRoom.id == id }
        }
    }
}