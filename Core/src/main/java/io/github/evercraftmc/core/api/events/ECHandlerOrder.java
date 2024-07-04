package io.github.evercraftmc.core.api.events;

public enum ECHandlerOrder {
    FIRST(1),
    BEFORE(2),
    DONT_CARE(3),
    AFTER(4),
    LAST(5);

    final int value;

    ECHandlerOrder(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}