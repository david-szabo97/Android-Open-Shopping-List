package com.messedcode.openshoppinglist.firebase;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FirebaseDatabaseConnectionWatcher implements ValueEventListener {

    private static final String TAG = "FBDBConnectionWatcher";

    private boolean connected = false;
    private boolean isInitialConnection = true;
    private ArrayList<OnConnectionChangeListener> listeners = new ArrayList<>();

    public FirebaseDatabaseConnectionWatcher() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(".info/connected");
        ref.addValueEventListener(this);
    }

    public void addListener(OnConnectionChangeListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        connected = dataSnapshot.getValue(Boolean.class);
        Log.v(TAG, "Connection changed: " + connected);

        if (connected) {
            if (isInitialConnection) {
                Log.v(TAG, "Connected for the first time: " + connected);
            } else {
                Log.v(TAG, "Connected: " + connected);
            }

            isInitialConnection = false;
            for (OnConnectionChangeListener listener : listeners) {
                listener.onConnected();
            }
        } else if (!isInitialConnection) {
            Log.v(TAG, "Disconnected: " + connected);

            for (OnConnectionChangeListener listener : listeners) {
                listener.onDisconnected();
            }
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        Log.e(TAG, "Cancelled " + databaseError.toString());
    }

    public boolean isConnected() {
        return connected;
    }

    public interface OnConnectionChangeListener {

        void onConnected();

        void onDisconnected();

    }

}
