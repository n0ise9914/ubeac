package io.ubeac.app.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.*;
import android.support.annotation.Nullable;
import io.ubeac.app.services.listeners.ConnectionListener;
import io.ubeac.app.services.listeners.MessageListener;
import io.ubeac.app.services.listeners.SignalListener;
import io.ubeac.app.services.models.Destination;
import io.ubeac.app.services.models.Signal;
import io.ubeac.app.services.models.SignalType;
import io.ubeac.app.utils.Constants;

public abstract class BaseService extends Service implements IBaseService {
    private SignalListener signalListener;
    private Messenger messenger;
    private BaseConnection connection;
    protected Messenger subscriber;

    public BaseService() {
        MessageHandler messageHandler = new MessageHandler();
        messageHandler.setMessageListener(new MessageListener() {
            @Override
            public void onReceive(Message message) {
                try {
                    Signal signal = (Signal) message.getData().getSerializable(Constants.SIGNAL_KEY);
                    if (signal == null)
                        return;
                    if (signal.getType() == SignalType.Register) {
                        subscriber = message.replyTo;
                        sendSignal(Destination.Activity, new Signal(SignalType.Registered, null));
                    }
                    onSignal(signal);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        messenger = new Messenger(messageHandler);
    }

    public void bind(Context context, Class<?> aClass) {
        Intent intent = new Intent(context, aClass);
        connection = new BaseConnection();
        connection.setMessageListener(new MessageListener() {
            @Override
            public void onReceive(Message message) {
                if (signalListener != null && connection.isBound()) {
                    try {
                        signalListener.onSignal((Signal) message.getData().getSerializable(Constants.SIGNAL_KEY));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                }
            }
        });
        connection.setOnConnected(new ConnectionListener() {
            @Override
            public void onRaise() {
                sendSignal(Destination.Service, new Signal(SignalType.Register, null), connection.getIncomingMessenger());
            }
        });
        context.startService(intent);
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    public void setSignalListener(SignalListener signalListener) {
        this.signalListener = signalListener;
    }

    public void sendSignal(Destination destination, Signal signal) {
        sendSignal(destination, signal, null);
    }

    public void sendSignal(Destination destination, Signal signal, Messenger replyTo) {
        try {
            Message msg = Message.obtain();
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.SIGNAL_KEY, signal);
            msg.setData(bundle);
            if (replyTo != null)
                msg.replyTo = replyTo;
            try {
                if (destination == Destination.Service && connection.isBound()) {
                    connection.getOutgoingMessenger().send(msg);
                } else if (destination == Destination.Activity && subscriber != null) {
                    subscriber.send(msg);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        subscriber = null;
        return true;
    }

    public void unbind(Context context) {
        context.unbindService(connection);
    }
}
