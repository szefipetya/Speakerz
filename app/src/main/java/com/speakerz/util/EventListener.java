package com.speakerz.util;

public interface EventListener<E extends EventArgs> {
    public void action(E args);
}
