package io.github.evercraftmc.core.api.events;

public abstract class ECCancelableEvent extends ECEvent {
    protected boolean cancelled = false;

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}