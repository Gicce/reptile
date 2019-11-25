package com.jandar.file.reptile.pkulaw;

import java.util.Queue;

public class PageThread implements Runnable {

    private int startPage;
    private int endPage;
    private Queue<String> smallUrlQueue;
    private PageCallback pageCallback;

    public PageThread(int startPage, int endPage, Queue<String> smallUrlQueue, PageCallback pageCallback) {
        this.startPage = startPage;
        this.endPage = endPage;
        this.smallUrlQueue = smallUrlQueue;
        this.pageCallback = pageCallback;
    }

    @Override
    public void run() {
        pageCallback.callback(startPage, endPage, smallUrlQueue);
    }


    public interface PageCallback {
        void callback(int startPage, int endPage, Queue<String> smallUrlQueue);
    }
}
