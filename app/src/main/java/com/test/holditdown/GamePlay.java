package com.test.holditdown;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
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

public class GamePlay extends SurfaceView implements
        SurfaceHolder.Callback {

    private static final String TAG = GamePlay.class.getSimpleName();
    private static final int MESSAGE_SHOW_FRAMES = 90;
    private static final int STATE_LOST = -1;
    private static final int STATE_WON = 1;
    private static final String LEVEL_1_MESSAGE = "HOLD IT DOWN";
    private static final String WIN_MESSAGE = "YOU WON";
    private static final String LOOSE_MESSAGE = "YOU LOST";
    private int WIN_LEVEL = 5;
    private int timeLimit;
    private GameThread thread;
    private Object object;
    private int currentLevel;

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


    public GamePlay(Context context) {
        super(context);
        getHolder().addCallback(this);

        plain = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Light.ttf");

        color = Color.argb(255, rndInt(0, 255), rndInt(0, 255), rndInt(0, 255));
        messageAlpha = 10;
        messageColor = Color.argb(messageAlpha, 255, 255, 255);
        gameMessageColor = Color.argb(255, 255, 255, 255);
        paint = new Paint(color);
        messagePaint = new Paint(messageColor);
        gameMessagePaint = new Paint(gameMessageColor);
        gameMessagePaint.setColor(gameMessageColor);
        gameMessagePaint.setTypeface(plain);
        gameMessagePaint.setTextSize(20);
        gameMessagePaint.setStyle(Paint.Style.FILL);
        gameMessagePaint.setFlags(Paint.ANTI_ALIAS_FLAG);

        StartTime = System.currentTimeMillis();
        timeLimit = 20 * 1000;      //time limit for lvl1
        currentLevel = 1;
        gameState = 0;
        object = new Object(getWidth() / 2, getHeight() / 2, currentLevel);
        MessageFrameCount = 0;
        // create the game loop thread
        thread = new GameThread(getHolder(), this);

        setFocusable(true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged: ");
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated: ");
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "Surface is being destroyed");
        // tell the thread to shut down and wait for it to finish
        // this is a clean shutdown
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
        messagePaint.setStyle(Paint.Style.FILL);

        messagePaint.setColor(messageColor);
        if (Color.alpha(messageColor) < 255) {
            Log.d(TAG, "showLevelMessage: " + String.valueOf(messageAlpha));
            messageAlpha += 5;
            messageColor = Color.argb(messageAlpha, 255, 255, 255);
        }
        messagePaint.setTextSize(50);
        messagePaint.setTextAlign(Paint.Align.CENTER);
//        paint.setTypeface(Typeface.create("Arial",Typeface.ITALIC));
        messagePaint.setTypeface(plain);
        messagePaint.setFlags(Paint.ANTI_ALIAS_FLAG);

        canvas.drawText(message, getWidth() / 2, getHeight() / 2, messagePaint);
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

    public void displayScoreNeeded(Canvas canvas) {
        if (canvas != null) {
            gameMessagePaint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText("To Win: " + df.format(object.scoreRequired()), getWidth() - 10, 20, gameMessagePaint);
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
            gameState = STATE_LOST;
            MessageFrameCount = 0;  //To show lost message before exit
        } else {
            if (object.GameWon()) {
                nextLevel();
            }
            if (object.getX() <= 0) {
                if (!object.isMovingRight()) {
                    object.invertSpeedX();
                }
            } else if (object.getX() >= getWidth() - Object.SIZE) {
                if (object.isMovingRight()) {
                    object.invertSpeedX();
                }
            }
            if (object.getY() <= 0) {
                if (!object.isMovingDown()) {
                    object.invertSpeedY();
                }
            } else if (object.getY() >= getHeight() - Object.SIZE - 50) {
                if (object.isMovingDown()) {
                    object.invertSpeedY();
                }
            }
            object.update();
        }
    }

    public void nextLevel() {
        int x = (int) object.getX();
        int y = (int) object.getY();
        currentLevel++;
        if (currentLevel > WIN_LEVEL) {
            Log.d(TAG, "nextLevel: won");
            gameState = STATE_WON;
            MessageFrameCount = 0;
        }
        object = new Object(x, y, currentLevel);
        timeLimit += 8 * currentLevel * 1000;      //seconds added
    }

    public void render(Canvas canvas) {
        if (canvas != null) {
            //Showing only game message for the first second(MESSAGE_SHOW_FRAMES frames)
            if (MessageFrameCount < MESSAGE_SHOW_FRAMES) {
                if (gameState == STATE_LOST) {
                    showLevelMessage(canvas, LOOSE_MESSAGE);
                } else if (gameState == STATE_WON) {
                    showLevelMessage(canvas, WIN_MESSAGE);
                } else {
                    showLevelMessage(canvas, LEVEL_1_MESSAGE);
                }
                MessageFrameCount++;
            } else {
                if (gameState == STATE_LOST) {   //Message shown now quit level
                    Context context = getContext();
                    Intent intent = new Intent(context, MainActivity.class);
                    thread.setRunning(false);
                    ((Activity) getContext()).finish();
                    context.startActivity(intent);
                } else if (gameState == STATE_WON) {
                    Context context = getContext();
                    Intent intent = new Intent(context, MainActivity.class);
                    thread.setRunning(false);
                    ((Activity) getContext()).finish();
                    context.startActivity(intent);
                }

                canvas.drawColor(Color.BLACK);
                paint.setColor(this.color);
                canvas.drawRect(0, getHeight() - 50, getWidth(), getHeight(), paint);
                object.draw(canvas);

                timeLeft(canvas);
                displayScoreNeeded(canvas);
                // display fps
//                displayFps(canvas, avgFps);
            }
        }
    }

}
