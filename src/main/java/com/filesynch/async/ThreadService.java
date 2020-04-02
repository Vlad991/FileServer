package com.filesynch.async;

import java.util.Stack;

public class ThreadService {
    private Stack<Thread> threadList;
    private int maxSize = 3;

    public Thread getThread(Thread thread) {
        if (threadList.size() == maxSize) {
            try {
                synchronized (threadList) {
                    threadList.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return threadList.push(thread);
    }

    public void returnThread(Thread thread) {
        threadList.remove(thread);
        threadList.notify();
    }
}
