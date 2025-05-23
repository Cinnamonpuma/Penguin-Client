package cinnamon.penguinclient.event

/**
 * Base sealed class for all events.
 * Allows for type-safe event handling.
 */
sealed class Event {
    /**
     * Represents an event that can be cancelled.
     */
    open class Cancellable : Event() {
        var isCancelled: Boolean = false
            private set

        fun cancel() {
            isCancelled = true
        }
    }

    /**
     * Example Event: Fired when the client ticks.
     * Could be posted from a client tick mixin.
     */
    object ClientTickEvent : Event()

    /**
     * Example Cancellable Event: Fired before a chat message is sent.
     * Allows modification or cancellation of the message.
     * @property message The chat message.
     */
    data class ChatMessageSendEvent(var message: String) : Cancellable()
    
    /**
     * Generic data event that can be used for various purposes.
     * @param T The type of data this event carries.
     * @property data The data payload of the event.
     */
    data class GenericDataEvent<T>(val data: T) : Event()
}
