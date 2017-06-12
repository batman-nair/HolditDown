package com.test.holditdown;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.lang.*;
import java.lang.Object;
import java.util.Random;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Arjun on 22-11-2016.
 */

public class GameMenu extends SurfaceView implements
        SurfaceHolder.Callback {

    private static final String TAG = GameMenu.class.getSimpleName();
    private static final String MY_PREFS_NAME = "GAMEDATA";
    private static final String LEVEL_TEXT = "LEVELS";
    private static final String ARCADE_TEXT = "ARCADE";
    private static final String ARCADE_PREF_KEY = "ArcadeData";
    private static final String TUTORIAL_TEXT = "TUTORIAL";

    private MainThread thread;
    private int MAIN_BUTTON_WIDTH = 350;
    private int MAIN_BUTTON_HEIGHT = 120;
    private int levelButtonX;
    private int levelButtonY;
    private int arcadeButtonX;
    private int arcadeButtonY;
    private int tutorialButtonX;
    private int tutorialButtonY;
    private Paint paint;
    private Paint captionPaint;
    private int color;
    private int colorInvert;
    protected static Random random = new Random();
    private boolean NeverOpened;
    private int selectedLevel;
    private Typeface font;
    private int arcadeHighscore;


    public GameMenu(Context context) {
        super(context);
        getHolder().addCallback(this);
        Log.d(TAG, "GameMenu: ");

        color = Color.rgb(rndInt(0, 255), rndInt(0, 255), rndInt(0, 255));
        colorInvert = 0xffffffff - color + 0xff000000;
        //Making lighter color as primary color
        if (Color.red(color) + Color.blue(color) + Color.green(color) > Color.red(colorInvert) + Color.blue(colorInvert) + Color.green(colorInvert)) {
            color = color + colorInvert;
            colorInvert = color - colorInvert;
            color = color - colorInvert;
            Log.d(TAG, "GameMenu: Inverted");
        }
        paint = new Paint(color);
        font = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Light.ttf");
        paint.setColor(color);
        paint.setTypeface(font);
        paint.setTextSize(60);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        captionPaint = new Paint(paint);
        captionPaint.setTextSize(20);

        SharedPreferences prefs = getContext().getSharedPreferences(ARCADE_PREF_KEY, MODE_PRIVATE);
        arcadeHighscore = prefs.getInt("highscore", 0);

//        initData(context);
        selectedLevel = 1;
        // create the game loop thread


        setFocusable(true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged: ");
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated: ");

        levelButtonX = getWidth() / 2 - MAIN_BUTTON_WIDTH / 2;
        levelButtonY = getHeight() / 2 - MAIN_BUTTON_HEIGHT / 2 - 150;
        arcadeButtonX = getWidth() / 2 - MAIN_BUTTON_WIDTH / 2;
        arcadeButtonY = getHeight() / 2 - MAIN_BUTTON_HEIGHT / 2;
        tutorialButtonX = getWidth() /2 - MAIN_BUTTON_WIDTH / 2;
        tutorialButtonY = getHeight() / 2 - MAIN_BUTTON_HEIGHT / 2 + 150;

        thread = new MainThread(getHolder(), this);
        thread.setRunning(true);
        if (thread.getState() == Thread.State.NEW)
        {
            thread.start();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "Surface is being destroyed");
        // tell the thread to shut down and wait for it to finish
        // this is a clean shutdown
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
//        Context context = getContext();
//        ((Activity) getContext()).finish();
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

            if (event.getY() >= levelButtonY && event.getY() <= levelButtonY + MAIN_BUTTON_HEIGHT) {
                if (event.getX() >= levelButtonX && event.getX() <= levelButtonX + MAIN_BUTTON_WIDTH) {
                    Toast.makeText(getContext(), "Under Development", Toast.LENGTH_SHORT).show();
//                    Context context = getContext();
//                    Intent intent = new Intent(context, GameActivity.class);
//                    thread.setRunning(false);
//                    ((Activity) getContext()).finish();
//                    context.startActivity(intent);
                }
            }

            if (event.getY() >= arcadeButtonY && event.getY() <= arcadeButtonY + MAIN_BUTTON_HEIGHT) {
                if (event.getX() >= arcadeButtonX && event.getX() <= arcadeButtonX + MAIN_BUTTON_WIDTH) {

                    Context context = getContext();
                    Intent intent = new Intent(context, ArcadeActivity.class);
                    thread.setRunning(false);
                    ((Activity) getContext()).finish();
                    context.startActivity(intent);
                }
            }

            if (event.getY() >= tutorialButtonY && event.getY() <= tutorialButtonY + MAIN_BUTTON_HEIGHT) {
                if (event.getX() >= tutorialButtonX && event.getX() <= tutorialButtonX + MAIN_BUTTON_WIDTH) {
                    Toast.makeText(getContext(), "Press your finger over the square to get points. Score High!", Toast.LENGTH_SHORT).show();
//                    Context context = getContext();
//                    Intent intent = new Intent(context, GameActivity.class);
//                    thread.setRunning(false);
//                    ((Activity) getContext()).finish();
//                    context.startActivity(intent);
                }
            }
        }

        return true;
    }

    public void initData(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        NeverOpened = prefs.getBoolean("NeverOpened", true);
        if (NeverOpened == false) {
            editor.putBoolean("NeverOpened", false);
            editor.putInt("ClearedLevels", 0);
            editor.putInt("HighScore", 0);
            editor.apply();
        }
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

    public void update() {
    }

    public void render(Canvas canvas) {
        if (canvas != null) {
            canvas.drawColor(Color.BLACK);
            paint.setColor(color);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(levelButtonX, levelButtonY, levelButtonX + MAIN_BUTTON_WIDTH, levelButtonY + MAIN_BUTTON_HEIGHT, paint);
            canvas.drawRect(arcadeButtonX, arcadeButtonY, arcadeButtonX + MAIN_BUTTON_WIDTH, arcadeButtonY + MAIN_BUTTON_HEIGHT, paint);
            canvas.drawRect(tutorialButtonX, tutorialButtonY, tutorialButtonX + MAIN_BUTTON_WIDTH, tutorialButtonY + MAIN_BUTTON_HEIGHT, paint);

            paint.setColor(colorInvert);
            captionPaint.setColor(colorInvert);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawText(LEVEL_TEXT, levelButtonX + MAIN_BUTTON_WIDTH / 2, levelButtonY + MAIN_BUTTON_HEIGHT - 30, paint);
            canvas.drawText(ARCADE_TEXT, arcadeButtonX + MAIN_BUTTON_WIDTH / 2, arcadeButtonY + MAIN_BUTTON_HEIGHT - 40, paint);
            canvas.drawText("Highscore: " + arcadeHighscore, arcadeButtonX + MAIN_BUTTON_WIDTH / 2, arcadeButtonY + MAIN_BUTTON_HEIGHT - 10, captionPaint);
            canvas.drawText(TUTORIAL_TEXT, tutorialButtonX + MAIN_BUTTON_WIDTH / 2, tutorialButtonY + MAIN_BUTTON_HEIGHT - 30, paint);

            // display fps
            displayFps(canvas, avgFps);
        }
    }


}
