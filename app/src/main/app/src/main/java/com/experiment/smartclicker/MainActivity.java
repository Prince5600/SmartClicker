package com.experiment.smartclicker;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import rikka.shizuku.Shizuku;

public class MainActivity extends Activity {

    private static final int REQ_CODE = 100;
    private TextView log;

    private final Shizuku.OnRequestPermissionResultListener permListener =
            new Shizuku.OnRequestPermissionResultListener() {
                @Override
                public void onRequestPermissionResult(int requestCode, int grantResult) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        print("Permission GRANTED");
                    } else {
                        print("Permission DENIED");
                    }
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);

        Button btnPing = new Button(this);
        btnPing.setText("1. Check Shizuku");
        btnPing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkShizuku();
            }
        });

        Button btnPerm = new Button(this);
        btnPerm.setText("2. Request Permission");
        btnPerm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPerm();
            }
        });

        Button btnCmd = new Button(this);
        btnCmd.setText("3. Run test command (id)");
        btnCmd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runTest();
            }
        });

        log = new TextView(this);
        log.setText("Log:\n");
        ScrollView sv = new ScrollView(this);
        sv.addView(log);

        layout.addView(btnPing);
        layout.addView(btnPerm);
        layout.addView(btnCmd);
        layout.addView(sv);
        setContentView(layout);

        try {
            Shizuku.addRequestPermissionResultListener(permListener);
        } catch (Throwable t) {
            print("Listener error: " + t);
        }
    }

    private void print(final String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                log.append(s + "\n");
            }
        });
    }

    private void checkShizuku() {
        try {
            if (Shizuku.pingBinder()) {
                print("Shizuku running. Version: " + Shizuku.getVersion()
                        + ", uid: " + Shizuku.getUid());
            } else {
                print("Shizuku NOT running. Shizuku app kholo aur Start karo.");
            }
        } catch (Throwable t) {
            print("Check error: " + t);
        }
    }

    private void requestPerm() {
        try {
            if (!Shizuku.pingBinder()) {
                print("Pehle Shizuku start karo.");
                return;
            }
            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                print("Permission already GRANTED");
            } else {
                Shizuku.requestPermission(REQ_CODE);
            }
        } catch (Throwable t) {
            print("Permission error: " + t);
        }
    }

    private void runTest() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    java.lang.reflect.Method m = Shizuku.class.getDeclaredMethod(
                            "newProcess", String[].class, String[].class, String.class);
                    m.setAccessible(true);
                    Object proc = m.invoke(null,
                            new String[]{"sh", "-c", "id"}, null, null);
                    java.lang.Process p = (java.lang.Process) proc;
                    java.io.BufferedReader r = new java.io.BufferedReader(
                            new java.io.InputStreamReader(p.getInputStream()));
                    String line;
                    while ((line = r.readLine()) != null) {
                        print("OUT: " + line);
                    }
                    p.waitFor();
                    print("Command done");
                } catch (Throwable t) {
                    print("Command error: " + t);
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            Shizuku.removeRequestPermissionResultListener(permListener);
        } catch (Throwable t) {
            // ignore
        }
    }
                        }
