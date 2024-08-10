package io.github.evercraftmc.core.api.events;

import org.jetbrains.annotations.NotNull;

public abstract class ECCancelableReasonEvent extends ECCancelableEvent {
    protected @NotNull String cancelReason = "";

    public @NotNull String getCancelReason() {
        return this.cancelReason;
    }

    public void setCancelReason(@NotNull String cancelReason) {
        this.cancelReason = cancelReason;
    }
}