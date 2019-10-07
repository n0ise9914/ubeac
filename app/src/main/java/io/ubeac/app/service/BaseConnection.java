package io.ubeac.app.service;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import io.ubeac.app.service.listeners.ConnectionListener;
import io.ubeac.app.service.listeners.MessageListener;

public class BaseConnection implements ServiceConnection {
    private Messenger outgoingMessenger;
    private Messenger incomingMessenger;
    private MessageListener messageListener;
    private ConnectionListener onConnected;

    private boolean bound = false;

    public BaseConnection() {
        MessageHandler handler = new MessageHandler();
        handler.setMessageListener(new MessageListener() {
            @Override
            public void onReceive(Message message) {
                messageListener.onReceive(message);
            }
        });
        incomingMessenger = new Messenger(handler);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        outgoingMessenger = new Messenger(service);
        this.bound = true;
        try {
            onConnected.onRaise();
        } catch (Exception ignored) {
        }
    }

    public Messenger getIncomingMessenger() {
        return incomingMessenger;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        outgoingMessenger = null;
        this.bound = false;
    }

    public void setMessageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
    }

    public boolean isBound() {
        return bound;
    }

    public void setBound(boolean bound) {
        this.bound = bound;
    }

    public Messenger getOutgoingMessenger() {
        return outgoingMessenger;
    }

    public void setOnConnected(ConnectionListener onConnected) {
        this.onConnected = onConnected;
    }
}
