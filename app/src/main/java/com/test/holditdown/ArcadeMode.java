package com.test.holditdown;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.text.DecimalFormat;
import java.util.Random;

/**
 * Created by Arjun on 22-11-2016.
 */

public class ArcadeMode extends SurfaceView implements
        SurfaceHolder.Callback {

    private static final String TAG = ArcadeMode.class.getSimpleName();
    private static final int MESSAGE_SHOW_FRAMES = 90;
    private static final int STATE_LOST = -1;
    private static final int STATE_WON = 1;
    private static final String LEVEL_1_TITLE = "ARCADE MODE";
    private static final String LEVEL_1_CAPTION = "Score as much as you can";
    private static final String LOOSE_MESSAGE = "GAME OVER";
    private static final String EXIT_MESSAGE = "EXIT";
    private static final String ARCADE_PREF_KEY = "ArcadeData";
    private int timeLimit;
    private ArcadeModeThread thread;
    private Object object;
    private int currentLevel;
    private int highscore;

    private int gameState;
    private Paint paint;
    private Paint messagePaint;
    private Paint gameMessagePaint;
    private int gameMessageColor;
    private int color, messageColor, messageAlpha;
    private Typeface plain;
    private DecimalFormat df = new DecimalFormat("0.##");


    private long StartTime;

    protected static Random random = new Random();
    private int MessageFrameCount;


    public ArcadeMode(Context context) {
        super(context);
        getHolder().addCallback(this);

        SharedPreferences prefs = getContext().getSharedPreferences(ARCADE_PREF_KEY, Context.MODE_PRIVATE);
        highscore = prefs.getInt("highscore", 0); //0 is the default value

        plain = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Light.ttf");

        color = Color.argb(255, rndInt(0, 255), rndInt(0, 255), rndInt(0, 255));
        messageAlpha = 10;
        messageColor = Color.argb(messageAlpha, 255, 255, 255);
        gameMessageColor = Color.argb(255, 255, 255, 255);

        paint = new Paint(color);
        messagePaint = new Paint(messageColor);
        messagePaint.setColor(messageColor);
        messagePaint.setStyle(Paint.Style.FILL);
        messagePaint.setTextSize(50);
        messagePaint.setTextAlign(Paint.Align.CENTER);
        messagePaint.setTypeface(plain);
        messagePaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        gameMessagePaint = new Paint(gameMessageColor);
        gameMessagePaint.setColor(gameMessageColor);
        gameMessagePaint.setTextSize(20);
        gameMessagePaint.setStyle(Paint.Style.FILL);
        gameMessagePaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        gameMessagePaint.setTypeface(plain);

        StartTime = System.currentTimeMillis();
        timeLimit = 20 * 1000;      //time limit for lvl1
        currentLevel = 1;
        gameState = 0;
        object = new Object(getWidth() / 2, getHeight() / 2, currentLevel);
        MessageFrameCount = 0;

        setFocusable(true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged: ");
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated: ");
        SharedPreferences prefs = getContext().getSharedPreferences(ARCADE_PREF_KEY, Context.MODE_PRIVATE);
        long pauseStartTime = prefs.getLong("pauseStartTime", 0);
        long pauseTime = System.currentTimeMillis() - pauseStartTime;
        StartTime += pauseTime;
        // create the game loop thread
        thread = new ArcadeModeThread(getHolder(), this);
        thread.setRunning(true);
        if (thread.getState() == Thread.State.NEW) {
            thread.start();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "Surface is being destroyed");
        // tell the thread to shut down and wait for it to finish
        // this is a clean shutdown
        SharedPreferences.Editor editor = getContext().getSharedPreferences(ARCADE_PREF_KEY, Context.MODE_PRIVATE).edit();
        editor.putLong("pauseStartTime", System.currentTimeMillis());
        editor.apply();

        thread.setRunning(false);
        boolean retry = true;
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
                // try again shutting down the thread
            }
        }
        Log.d(TAG, "Thread was shut down cleanly");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {

            // check if in the lower part of the screen we exit
            if (event.getY() > getHeight() - 50) {
                thread.setRunning(false);
                ((Activity) getContext()).finish();
            } else {
                Log.d(TAG, "Coords: x=" + event.getX() + ",y=" + event.getY());
            }
            object.handleActionDown(event.getX(), event.getY());
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            object.handleActionUp();
        }
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            object.handleActionDown(event.getX(), event.getY());
        }

        return true;
    }

    public void showLevelMessage(Canvas canvas, String message) {
//        Log.d(TAG, "showLevelMessage: ");

        canvas.drawColor(Color.BLACK);


        if (Color.alpha(messageColor) < 255) {      //Fade in animation
//            Log.d(TAG, "showLevelMessage: " + String.valueOf(messageAlpha));
            messageAlpha += 5;
            messageColor = Color.argb(messageAlpha, 255, 255, 255);
        }
        messagePaint.setColor(messageColor);
        messagePaint.setTextSize(50);

        canvas.drawText(message, getWidth() / 2, getHeight() / 2, messagePaint);

        StartTime = System.currentTimeMillis();
    }

    public void showLevelMessage(Canvas canvas, String message, String caption) {

        canvas.drawColor(Color.BLACK);

        if (Color.alpha(messageColor) < 255) {
//            Log.d(TAG, "showLevelMessage: " + String.valueOf(messageAlpha));
            messageAlpha += 5;
            messageColor = Color.argb(messageAlpha, 255, 255, 255);
        }
        messagePaint.setColor(messageColor);
        messagePaint.setTextSize(50);
        canvas.drawText(message, getWidth() / 2, getHeight() / 2, messagePaint);

        messagePaint.setTextSize(20);
        int y = getHeight() / 2 + 40;
        //for displaying multi - line captions
        for (String line : caption.split("\n")) {
            canvas.drawText(line, getWidth() / 2, y, messagePaint);
            y += messagePaint.descent() - messagePaint.ascent();
        }
        //Start time set after message ends
        StartTime = System.currentTimeMillis();
    }

    public static int rndInt(int min, int max) {
        return random.nextInt(max - min) + min;
    }

    private String avgFps;

    public void setAvgFps(String avgFps) {
        this.avgFps = avgFps;
    }

    private void displayFps(Canvas canvas, String fps) {
        if (canvas != null && fps != null) {
            Paint paint = new Paint();
            paint.setARGB(255, 255, 255, 255);
            canvas.drawText(fps, this.getWidth() - 50, 20, paint);
        }
    }

    public void currentScore(Canvas canvas) {
        if (canvas != null) {
            gameMessagePaint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText("Score: " + df.format(object.totalScore()), getWidth() - 10, 20, gameMessagePaint);
        }
    }

    public void timeLeft(Canvas canvas) {
        if (canvas != null) {
            gameMessagePaint.setTextAlign(Paint.Align.LEFT);
            long timeLeft = (StartTime + timeLimit) - System.currentTimeMillis() - 19800 * 1000;    //current time calc from 1/1/1970 5:30:00
            canvas.drawText("Time Left: " + DateFormat.format("mm:ss", timeLeft).toString(), 10, 20, gameMessagePaint);
        }
    }

    public void update() {

        if (System.currentTimeMillis() >= StartTime + timeLimit) {
            Log.d(TAG, "update: lost");
            if (object.totalScore() > highscore) {
                highscore = (int) object.totalScore();
                SharedPreferences prefs = getContext().getSharedPreferences(ARCADE_PREF_KEY, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("highscore", highscore);
                editor.commit();
            }
            gameState = STATE_LOST;
            messageAlpha = 10;
            MessageFrameCount = 0;  //To show lost message before exit
        } else {
            if (object.GameWon()) {
                nextLevel();
            }

            object.update();
        }
    }

    public void nextLevel() {
        int x = (int) object.getX();
        int y = (int) object.getY();
        currentLevel++;
        object = new Object(x, y, currentLevel);
        double Sn = 0;
        for (int i = 1; i <= currentLevel; i++) {  //Harmonic Series
            Sn += 1 / (double)i;
        }
        timeLimit += 10 * Sn * 1000;      //seconds added
    }

    public void render(Canvas canvas) {
        if (canvas != null) {
            //Showing only game message for the first (MESSAGE_SHOW_FRAMES) frames
            if (MessageFrameCount < MESSAGE_SHOW_FRAMES) {
                if (gameState == STATE_LOST) {
                    messageAlpha = 10;
                    showLevelMessage(canvas, LOOSE_MESSAGE, "Highscore : " + String.valueOf(highscore));
                } else {
                    showLevelMessage(canvas, LEVEL_1_TITLE, LEVEL_1_CAPTION + "\n Highscore : " + String.valueOf(highscore));    //Only called at the start
                }
                MessageFrameCount++;
            } else {
                if (gameState == STATE_LOST) {   //Message shown now quit level
                    Context context = getContext();
                    Intent intent = new Intent(context, MainActivity.class);
                    thread.setRunning(false);
                    ((Activity) getContext()).finish();
                    context.startActivity(intent);
                }
                if (object.isTouched()) {
                    canvas.drawColor(0xFFFFFFFF - color + 0xFF000000);  //Inverted color
                } else {
                    canvas.drawColor(Color.BLACK);
                }
                paint.setColor(this.color);
                canvas.drawRect(0, getHeight() - 50, getWidth(), getHeight(), paint);
                messagePaint.setTextSize(20);
                canvas.drawText(EXIT_MESSAGE, getWidth() / 2, getHeight() - 20, messagePaint);
                object.draw(canvas);

                timeLeft(canvas);
                currentScore(canvas);
                // display fps
//                displayFps(canvas, avgFps);
            }
        }
    }

}
