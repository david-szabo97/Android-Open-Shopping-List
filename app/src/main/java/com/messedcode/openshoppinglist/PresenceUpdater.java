package com.messedcode.openshoppinglist;

import android.os.Handler;

import com.google.firebase.database.DatabaseReference;

public class PresenceUpdater implements Runnable {

    // TODO: Logging
    // TODO: Configurable
    public static final String CHILD_KEY = "lastActive";
    public static final int DEFAULT_INTERVAL = 1000 * 5;

    private final Handler handler;
    private final DatabaseReference dbRef;
    private final DatabaseReference childRef;

    private boolean active = false;
    private int interval = DEFAULT_INTERVAL;
    private String key = null;

    public PresenceUpdater(DatabaseReference dbRef) {
        handler = new Handler();
        this.dbRef = dbRef;

        childRef = dbRef.child(CHILD_KEY);
    }

    @Override
    public void run() {
        update();

        if (active) {
            schedule();
        }
    }

    public void update() {
        saveValue(System.currentTimeMillis());
    }

    private void saveValue(long time) {
        if (key != null) {
            childRef.child(key).setValue(time);
        }
    }

    private void schedule() {
        handler.postDelayed(this, interval);
    }

    public void start() {
        active = true;
        run();
    }

    public void stop() {
        active = false;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public void setKey(String key) {
        this.key = key;
    }

}
