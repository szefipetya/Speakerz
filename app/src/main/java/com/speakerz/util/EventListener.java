package com.speakerz.util;

public interface EventListener<E extends EventArgs> {
    void action(E args);
}
