package com.android.flipflop;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.android.flipflop.MainActivity.BROADCAST_ACTION;
import static com.android.flipflop.MainActivity.FF_CLOSED_STATE;
import static com.android.flipflop.MainActivity.FF_OPENED_STATE;
import static com.android.flipflop.MainActivity.FF_STATE;
import static com.android.flipflop.MainActivity.SERVICE_CODE;
import static com.android.flipflop.MainActivity.TAG;


public class FlipFlopService extends Service {

    private static final int NOTIFF_ID = 314159264;
    private static final String CHANNEL_ID = "flipflop";

    ExecutorService executorService;

    private HandlerInRunnable hir;
    private Handler serviceHandler;
    private NotificationCompat.Builder builder;
    private Notification foregroudNotif;
    private NotificationManager manager;

    private boolean fFlopState;

    public FlipFlopService() {
        super();
        Log.d(TAG, "FlipFlopService: constructor");
        executorService = Executors.newFixedThreadPool(1);

    }

    @Override
    public void onCreate() {

        super.onCreate();
        init();
        startLooper();
        createNotiffChannel();

    }

    private void startLooper() {
        hir = new HandlerInRunnable(serviceHandler) {

            @Override
            public void run() {
                Looper.prepare();
                Log.d(TAG, "run: HandlerInRunnable");
                serviceHandler = new Handler();
                Looper.loop();
            }

        };
        executorService.submit(hir);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Log.d(TAG, "onBind: ");
        return new Binder();
    }

    private void init() {
        Log.d(TAG, "init: ");
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        if (intent.getExtras() != null)
            if (intent.getExtras().containsKey(SERVICE_CODE)) {
                String command = intent.getExtras().getString(SERVICE_CODE);
                Log.d(TAG, "onStartCommand: command = " + command);
                switch (command) {
                    case FF_OPENED_STATE: {
                        Log.d(TAG, "onHandleIntent: open");

                        cancelTask();

                        break;
                    }
                    case FF_CLOSED_STATE: {
                        Log.d(TAG, "onHandleIntent: close");

                        prepareNotification();

                        Log.d(TAG, "onHandleIntent: start task");

                        changeFFState(false);

                        someTask();

                        startForeground(NOTIFF_ID, foregroudNotif);

                        break;
                    }
                }
            }


        return super.onStartCommand(intent, flags, startId);
    }

    private void prepareNotification() {
        Log.d(TAG, "prepareNotification: for foreground");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                    .setContentText("Notification text")
                    .setContentTitle("alert")
                    .setSmallIcon(R.drawable.icon);
        } else {
            builder = new NotificationCompat.Builder(getApplicationContext())
                    .setContentText("Notification text")
                    .setContentTitle("alert")
                    .setSmallIcon(R.drawable.icon)
                    .setAutoCancel(false)
            ;
        }
        foregroudNotif = builder.build();
    }

    private void cancelTask() {
        Log.d(TAG, "cancelTask: ");
        changeFFState(true);

        Intent intent = new Intent(BROADCAST_ACTION);
        intent.putExtra(FF_STATE, fFlopState);
        sendBroadcast(intent);

        stopForeground(true);

        serviceHandler = null;
        hir = null;
        executorService.shutdown();

        stopSelf();
    }


    void someTask() {

        Log.d(TAG, "someTask: ");
        serviceHandler.post(new Runnable() {
            @Override
            public void run() {

                Log.d(TAG, "ff state before task = " + (getFFState() ? FF_OPENED_STATE : FF_CLOSED_STATE));
                while (!getFFState()) {
                    Log.d(TAG, "run: sending broadcast");
                    Intent intent = new Intent(BROADCAST_ACTION);
                    intent.putExtra(FF_STATE, fFlopState);
                    sendBroadcast(intent);

                    try {
                        TimeUnit.MILLISECONDS.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }

                }
                Log.d(TAG, "ff state after task = " + (fFlopState ? FF_OPENED_STATE : FF_CLOSED_STATE));

            }
        });


    }

    private synchronized boolean getFFState() {
        return fFlopState;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy: Service");

    }

    private void createNotiffChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = "Android_FF_channel";
            String description = "FFwork task";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, importance);
            channel.setDescription(description);
            manager.createNotificationChannel(channel);
        }
    }

    abstract class HandlerInRunnable implements Runnable {
        Handler handler;

        HandlerInRunnable(Handler handler) {
            this.handler = handler;
        }

    }

    synchronized void changeFFState(boolean state) {
        fFlopState = state;
        Log.d(TAG, "changeFFState: " + fFlopState);

    }

}
