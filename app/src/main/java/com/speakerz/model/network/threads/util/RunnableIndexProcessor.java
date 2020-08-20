package com.speakerz.model.network.threads.util;

import com.ealva.ealvalog.LoggerFactory;
import com.speakerz.debug.D;

import java.util.logging.Logger;

public class RunnableIndexProcessor implements Runnable {

    private volatile boolean running = true;

    public void terminate() {
        running = false;
    }

    @Override
    public void run() {
        while (running) {
            try {
                D.log("Sleeping");
                Thread.sleep((long) 15000);

                D.log("Processing");
            } catch (InterruptedException e) {
                D.log("interrupted, no problem");
                e.printStackTrace();
                running = false;
            }
        }

    }
}