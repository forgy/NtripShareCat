package com.giserpeng.ntripshare.ntrip.Network;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Worker implements Runnable {
    private static final int WORK_QUEUE = 1000;
    private static final int BLOCKING_TIMEOUT_MLS = 60000;

    private static Worker instance;

    private Thread workingThread;

    private Worker() {
        workingThread = new Thread(this);
        workingThread.start();
    }

    public static Worker getInstance() {
        if (instance == null)
            instance = new Worker();

        return instance;
    }

    private ArrayBlockingQueue<IWork> queue = new ArrayBlockingQueue(WORK_QUEUE);

    private final ExecutorService executor = Executors.newCachedThreadPool();

    public void addWork(IWork work) throws InterruptedException {
        queue.add(work);
    }

    @Override
    public void run() {
        while (true) {
            try {
                IWork work = queue.poll(BLOCKING_TIMEOUT_MLS, TimeUnit.MILLISECONDS);

                if (work == null)
                    continue;

                executor.submit(work);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
