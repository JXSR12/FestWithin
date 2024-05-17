package com.jxsr.festwithin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SplashscreenActivity extends AppCompatActivity {
    ProgressBar pbSplash;
    TextView tvSplash;
    int progress;
    DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        tvSplash = findViewById(R.id.splashLoadingText);
        tvSplash.setText(R.string.loading_first);

        pbSplash = findViewById(R.id.splashProgressBar);
        pbSplash.setIndeterminate(false);
        pbSplash.setMax(100);
        pbSplash.setProgress(0);

        Handler handler = new Handler();
        Handler handler2 = new Handler();

        db = new DBHelper(getApplicationContext());
        db.resetAvailableCurrencies();
        db.seedExampleUser();
        db.seedFWUserAndWallet();
        db.seedExampleEvents();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(pbSplash.getProgress() < 5){
                    progress++;
                    android.os.SystemClock.sleep(200);
                    handler2.post(new Runnable() {
                        @Override
                        public void run() {
                            pbSplash.setProgress(progress);
                        }
                    });
                }
                handler2.post(new Runnable() {
                    @Override
                    public void run() {
                        tvSplash.setText("Preparing Resources");
                    }
                });
                while(pbSplash.getProgress() < 20){
                    progress++;
                    android.os.SystemClock.sleep(60);
                    handler2.post(new Runnable() {
                        @Override
                        public void run() {
                            pbSplash.setProgress(progress);
                        }
                    });
                }
                handler2.post(new Runnable() {
                    @Override
                    public void run() {
                        tvSplash.setText("Initializing UI");
                    }
                });
                while(pbSplash.getProgress() < 40){
                    progress++;
                    android.os.SystemClock.sleep(40);
                    handler2.post(new Runnable() {
                        @Override
                        public void run() {
                            pbSplash.setProgress(progress);
                        }
                    });
                }
                handler2.post(new Runnable() {
                    @Override
                    public void run() {
                        tvSplash.setText("Retrieving Maps Data");
                    }
                });
                while(pbSplash.getProgress() < 60){
                    progress++;
                    android.os.SystemClock.sleep(90);
                    handler2.post(new Runnable() {
                        @Override
                        public void run() {
                            pbSplash.setProgress(progress);
                        }
                    });
                }
                handler2.post(new Runnable() {
                    @Override
                    public void run() {
                        tvSplash.setText("Retrieving Database");
                    }
                });
                while(pbSplash.getProgress() < 90){
                    progress++;
                    android.os.SystemClock.sleep(60);
                    handler2.post(new Runnable() {
                        @Override
                        public void run() {
                            pbSplash.setProgress(progress);
                        }
                    });
                }
                handler2.post(new Runnable() {
                    @Override
                    public void run() {
                        tvSplash.setText("Finalizing");
                    }
                });
                while(pbSplash.getProgress() < 100){
                    progress++;
                    android.os.SystemClock.sleep(40);
                    handler2.post(new Runnable() {
                        @Override
                        public void run() {
                            pbSplash.setProgress(progress);
                        }
                    });
                }
                handler2.post(new Runnable() {
                    @Override
                    public void run() {
                        tvSplash.setText("Ready");
                    }
                });
            }
        }).start();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashscreenActivity.this, MainActivity.class));
                finish();
            }
        }, 12000);
    }
}