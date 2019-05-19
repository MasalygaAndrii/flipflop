package com.android.flipflop;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "service_flipflop";
    public final static String BROADCAST_ACTION = "com.homework.android.FLIPFLOP";
    public final static String SERVICE_CODE = "code";
    public final static String FF_CLOSED_STATE = "closed";
    public final static String FF_OPENED_STATE = "opened";
    public final static String FF_STATE = "state";

    TextView stateTextView;
    Button ffOpenedBtn, ffClosedBtn;

    BroadcastReceiver receiver;
    IntentFilter filter;

    private boolean ffState;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        setListeners();
        initBroadcast();

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, filter);
    }

    private void initBroadcast() {
        filter = new IntentFilter(BROADCAST_ACTION);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "onReceive: ");
                if (intent != null && intent.hasExtra(FF_STATE)) {

                    ffState = intent.getExtras().getBoolean(FF_STATE);

                    stateTextView.setText(ffState ? FF_OPENED_STATE : FF_CLOSED_STATE);

                    if (!ffState) {
                        ffClosedBtn.setClickable(false);
                        ffOpenedBtn.setClickable(true);

                    } else {
                        ffClosedBtn.setClickable(true);
                        ffOpenedBtn.setClickable(false);
                    }
                }
            }
        };
    }

    private void setListeners() {

        ffOpenedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FlipFlopService.class);
                intent.putExtra(SERVICE_CODE, FF_OPENED_STATE);

                startService(intent);
            }
        });

        ffClosedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FlipFlopService.class);
                intent.putExtra(SERVICE_CODE, FF_CLOSED_STATE);

                startService(intent);
            }
        });
    }

    private void init() {
        Log.d(TAG, "init: ");
        stateTextView = findViewById(R.id.ffStateTextView);
        ffOpenedBtn = findViewById(R.id.ffOpenedBtn);
        ffClosedBtn = findViewById(R.id.ffClosedBtn);

    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(receiver);

    }

}
