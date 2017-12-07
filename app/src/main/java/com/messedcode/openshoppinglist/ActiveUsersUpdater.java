package com.messedcode.openshoppinglist;

import android.os.Handler;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ActiveUsersUpdater implements Runnable {

    // TODO: Configurable
    public static final String CHILD_KEY = "lastActive";
    public static final int DEFAULT_INTERVAL = 1000 * 5;
    public static final int DEFAULT_ACTIVE_TIME = 1000 * 30;
    private static final String TAG = "ActiveUsersUpdated";
    private final Handler handler;
    private final DatabaseReference dbRef;
    private final DatabaseReference childRef;

    private boolean active = false;
    private int interval = DEFAULT_INTERVAL;
    private int activeTime = DEFAULT_ACTIVE_TIME;
    private ArrayList<String> lastData = new ArrayList<>();
    private ArrayList<OnUserConnectionChanged> listeners = new ArrayList<>();

    public ActiveUsersUpdater(DatabaseReference dbRef) {
        handler = new Handler();
        this.dbRef = dbRef;

        childRef = dbRef.child(CHILD_KEY);
    }

    public void addListener(OnUserConnectionChanged listener) {
        listeners.add(listener);
    }

    public void removeListener(OnUserConnectionChanged listener) {
        listeners.remove(listener);
    }

    @Override
    public void run() {
        update();

        if (active) {
            schedule();
        }
    }

    public void update() {
        childRef.orderByValue().startAt(System.currentTimeMillis() - activeTime).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.v(TAG, "Data received");
                ArrayList<String> currentData = new ArrayList<>((int) dataSnapshot.getChildrenCount());
                for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                    currentData.add(dsp.getKey());
                }

                ArrayList<String> lastDataTemp = lastData;
                lastData = currentData;

                int connected = 0;
                int disconnected = 0;
                for (String current : currentData) {
                    if (!lastDataTemp.contains(current)) {
                        Log.v(TAG, "Connected: " + current);
                        connected++;
                        for (OnUserConnectionChanged listener : listeners) {
                            listener.onConnected(current);
                        }
                    }
                }

                for (String last : lastDataTemp) {
                    if (!currentData.contains(last)) {
                        Log.v(TAG, "Disconnected: " + last);
                        disconnected++;
                        for (OnUserConnectionChanged listener : listeners) {
                            listener.onDisconnected(last);
                        }
                    }
                }

                if (connected > 0 || disconnected > 0) {
                    Log.v(TAG, "Active users changed: " + connected + " new connections, " + disconnected + " disconnections");
                    for (OnUserConnectionChanged listener : listeners) {
                        listener.onActivesChanged(lastData);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Cancelled " + databaseError.toString());
            }
        });
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
        handler.removeCallbacksAndMessages(null);
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public void setActiveTime(int activeTime) {
        this.activeTime = activeTime;
    }

    public ArrayList<String> getActives() {
        return lastData;
    }

    public interface OnUserConnectionChanged {

        void onConnected(String connectedName);

        void onDisconnected(String disconnectedName);

        void onActivesChanged(ArrayList<String> actives);

    }

}
