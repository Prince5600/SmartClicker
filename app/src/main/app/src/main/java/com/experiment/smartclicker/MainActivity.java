package com.experiment.smartclicker;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {

    private static final String SHIZUKU = "rikka.shizuku.Shizuku";
    private TextView log;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final SharedPreferences sp = getSharedPreferences("crash", MODE_PRIVATE);
        final Thread.UncaughtExceptionHandler old =
                Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                sp.edit().putString("last", Log.getStackTraceString(e)).commit();
                if (old != null) {
                    old.uncaughtException(t, e);
                }
            }
        });

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);

        Button b1 = new Button(this);
        b1.setText("1. Check Shizuku");
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkShizuku();
            }
        });

        Button b2 = new Button(this);
        b2.setText("2. Request Permission");
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPerm();
            }
        });

        Button b3 = new Button(this);
        b3.setText("3. Run test command (id)");
        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runTest();
            }
        });

        log = new TextView(this);
        log.setText("Log:\n");
        ScrollView sv = new ScrollView(this);
        sv.addView(log);

        layout.addView(b1);
        layout.addView(b2);
        layout.addView(b3);
        layout.addView(sv);
        setContentView(layout);

        String last = sp.getString("last", null);
        if (last != null) {
            print("PICHLA CRASH:\n" + last);
            sp.edit().remove("last").commit();
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

    private Object call(String name, Class<?>[] types, Object[] args) throws Exception {
        Class<?> c = Class.forName(SHIZUKU);
        java.lang.reflect.Method m = c.getDeclaredMethod(name, types);
        m.setAccessible(true);
        return m.invoke(null, args);
    }

    private void checkShizuku() {
        try {
            Object ping = call("pingBinder", new Class<?>[0], new Object[0]);
            if (Boolean.TRUE.equals(ping)) {
                Object ver = call("getVersion", new Class<?>[0], new Object[0]);
                Object uid = call("getUid", new Class<?>[0], new Object[0]);
                print("Shizuku running. Version: " + ver + ", uid: " + uid);
            } else {
                print("Shizuku NOT running.");
            }
        } catch (Throwable t) {
            print("Check error: " + Log.getStackTraceString(t));
        }
    }

    private void requestPerm() {
        try {
            Object g = call("checkSelfPermission", new Class<?>[0], new Object[0]);
            if (((Integer) g).intValue() == 0) {
                print("Permission already GRANTED");
            } else {
                call("requestPermission", new Class<?>[]{int.class}, new Object[]{100});
                print("Popup bheja. Allow karke button 2 dobara dabao.");
            }
        } catch (Throwable t) {
            print("Permission error: " + Log.getStackTraceString(t));
        }
    }

    private void runTest() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Object proc = call("newProcess",
                            new Class<?>[]{String[].class, String[].class, String.class},
                            new Object[]{new String[]{"sh", "-c", "id"}, null, null});
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
                    print("Command error: " + Log.getStackTraceString(t));
                }
            }
        }).start();
    }
            }
