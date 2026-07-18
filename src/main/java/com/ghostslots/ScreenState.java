package com.ghostslots;

public final class ScreenState {
    private static boolean screenOpen;

    private ScreenState() {
    }

    public static void markScreenOpen() {
        screenOpen = true;
    }

    public static void markScreenClosed() {
        screenOpen = false;
    }

    public static boolean isScreenOpen() {
        return screenOpen;
    }
}
