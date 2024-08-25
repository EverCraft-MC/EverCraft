package io.github.evercraftmc.core.api.events;

public enum ECHandlerOrder {
    FIRST(-50),
    BEFORE(-25),
    DONT_CARE(0),
    AFTER(25),
    LAST(50);

    private final int value;

    private ECHandlerOrder(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}