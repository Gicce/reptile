package com.jandar.file.reptile.motto;

import java.util.Queue;

public class MottoThread implements Runnable {

    private String html;
    private Queue<String> smallUrlQueue;
    private MottoCallback mottoCallback;

    public MottoThread(String html, Queue<String> smallUrlQueue, MottoCallback mottoCallback) {
        this.html = html;
        this.smallUrlQueue = smallUrlQueue;
        this.mottoCallback = mottoCallback;
    }

    @Override
    public void run() {
        mottoCallback.callback(html, smallUrlQueue);
    }

    public interface MottoCallback {
        void callback(String html, Queue<String> smallUrlQueue);
    }
}
