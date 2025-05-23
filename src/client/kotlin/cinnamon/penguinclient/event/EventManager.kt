package cinnamon.penguinclient.event

import kotlin.reflect.KClass

/**
 * A simple event dispatcher.
 * Allows subscribing to specific event types and posting events.
 */
object EventManager {
    // Stores listeners mapped by event class.
    // The value is a list of functions (listeners) that take an Event subtype and return Unit.
    private val listeners = mutableMapOf<KClass<out Event>, MutableList<(Event) -> Unit>>()

    /**
     * Subscribes a listener to a specific event type.
     *
     * @param T The type of event to subscribe to.
     * @param listener The function to be called when the event is posted.
     */
    inline fun <reified T : Event> subscribe(noinline listener: (T) -> Unit) {
        val eventClass = T::class
        // Get the list of listeners for this event type, or create it if it doesn't exist.
        // The listener is cast to (Event) -> Unit for storage, but will be invoked with type safety.
        listeners.computeIfAbsent(eventClass) { mutableListOf() }.add(listener as (Event) -> Unit)
    }

    /**
     * Unsubscribes a listener from a specific event type.
     * Note: This requires the exact listener reference to be passed.
     *
     * @param T The type of event to unsubscribe from.
     * @param listener The listener function to remove.
     */
    inline fun <reified T : Event> unsubscribe(noinline listener: (T) -> Unit) {
        val eventClass = T::class
        listeners[eventClass]?.remove(listener as (Event) -> Unit)
    }

    /**
     * Posts an event to all subscribed listeners.
     * If the event is an instance of Event.Cancellable, processing stops if it's cancelled.
     *
     * @param event The event to post.
     */
    fun post(event: Event) {
        // Get listeners for the exact event class
        listeners[event::class]?.forEach { listener ->
            try {
                listener(event)
                if (event is Event.Cancellable && event.isCancelled) {
                    return // Stop processing if a cancellable event is cancelled
                }
            } catch (e: Exception) {
                // Log error using a proper logger once available
                println("Error in event listener for ${event::class.simpleName}: ${e.message}")
                e.printStackTrace()
            }
        }

        // Optional: If you want superclass listeners to also receive events (e.g. subscribe to Event for all events)
        // This makes the system more complex as you need to decide on event propagation rules.
        // For now, only exact type matching is implemented.
    }
}
