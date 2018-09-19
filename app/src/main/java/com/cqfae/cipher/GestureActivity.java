package com.cqfae.cipher;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;


public class GestureActivity extends AppCompatActivity {
    GestureCipherView gestureCipherView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gesture);
        gestureCipherView = findViewById(R.id.view_gesture);
        gestureCipherView.setTraceListener(new TraceListener() {
            @Override
            public void onTraceFinished(String traceCode) {
                Toast.makeText(GestureActivity.this, traceCode, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTraceUnFinish() {
                Toast.makeText(GestureActivity.this, "请至少连接4个点", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
