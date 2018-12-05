package com.zsrun.floatingpbview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.zsrun.floatingpbview.view.FloatingPbView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private FloatingPbView floatingPbView;


    private TextView timeTv;
    private Button button;
    private Timer timer;
    private TimerTask task;
    private int currentTime = 0;
    private boolean flag = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        timeTv = (TextView) findViewById(R.id.time_tv);
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "点击了按钮", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void initView() {
        floatingPbView = findViewById(R.id.floating_pb_view);
        floatingPbView.setTotalTime(10000);
        floatingPbView.setImageBefore(R.drawable.fb_red_package);
        floatingPbView.setAwardTextSize(18);
        floatingPbView.setAwardText("30");

        floatingPbView.setOnProgressListener(new FloatingPbView.OnProgressListener() {
            @Override
            public void onProgress(int progress) {
                Log.i("???", "onProgress: " + Thread.currentThread().getName() + "----" + progress);
            }
        });

        floatingPbView.start();

        button();
    }

    private void button() {
        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floatingPbView.start();
            }
        });

        findViewById(R.id.stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floatingPbView.stop();
            }
        });

        findViewById(R.id.restart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floatingPbView.reStart();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        startTimer();
    }

    private void initTimer() {
        // 初始化计时器
        task = new MyTask();
        timer = new Timer();
    }

    class MyTask extends TimerTask {
        @Override
        public void run() {
            // 初始化计时器
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    currentTime++;
                    timeTv.setText(String.valueOf(currentTime));
                    if (currentTime == 10) {
                        //在这里弹窗然后停止计时
                        Toast.makeText(MainActivity.this, "你去哪里了?", Toast.LENGTH_SHORT).show();
                        floatingPbView.stop();
                        flag = true;
                        stopTimer();
                    }
                }
            });
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //有按下动作时取消定时
                stopTimer();
                break;
            case MotionEvent.ACTION_UP:
                //抬起时启动定时
                startTimer();
                break;
        }
        return super.dispatchTouchEvent(ev);

    }

    private void startTimer() {
        //启动计时器
        if (flag) {
            floatingPbView.start();
            flag = false;
        }
        /**
         * java.util.Timer.schedule(TimerTask task, long delay, long period)：
         * 这个方法是说，delay/1000秒后执行task,然后进过period/1000秒再次执行task，
         * 这个用于循环任务，执行无数次，当然，你可以用timer.cancel();取消计时器的执行。
         */
        initTimer();
        try {
            timer.schedule(task, 1000, 1000);
        } catch (IllegalStateException e) {
            e.printStackTrace();
            initTimer();
            timer.schedule(task, 1000, 1000);
        }
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
        }
        currentTime = 0;
        if (timeTv != null) {
            timeTv.setText(String.valueOf(currentTime));
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        //当activity不在前台是停止定时
        stopTimer();
    }

    @Override
    protected void onDestroy() {
        //销毁时停止定时
        stopTimer();
        super.onDestroy();
    }

}
