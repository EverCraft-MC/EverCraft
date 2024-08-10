package io.github.evercraftmc.core.api.events;

public enum ECHandlerOrder {
    FIRST(0),
    BEFORE(25),
    DONT_CARE(50),
    AFTER(75),
    LAST(100);

    private final int value;

    private ECHandlerOrder(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}