package com.natureisamazing.api;

/** Extra synchronized sleep state attached to the vanilla Wolf class by a mixin. */
public interface WolfSleepState {
    boolean natureIsAmazing$isSleeping();

    void natureIsAmazing$setSleeping(boolean sleeping);
}
