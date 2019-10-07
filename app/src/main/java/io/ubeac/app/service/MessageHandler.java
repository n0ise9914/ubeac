package io.ubeac.app.service;

import android.os.Handler;
import android.os.Message;
import io.ubeac.app.service.listeners.MessageListener;

public class MessageHandler extends Handler{
    private MessageListener messageListener;

    @Override
    public void handleMessage(Message msg) {
        if (messageListener != null)
            messageListener.onReceive(msg);
    }

    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }
}