package com.test.holditdown;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

import java.text.DecimalFormat;
import java.util.Random;

import static android.content.ContentValues.TAG;

/**
 * Created by Arjun on 23-11-2016.
 */

public class Object {
    private static final int STATE_ALIVE = 1;
    private static final int STATE_DEAD = 0;
    private static final double SPEED_INC = 0.4;
    private static final double SPEED_DEC = 0.7;
    public static int SIZE = 40;

    private int state;
    private float x, y;
    private int width, height;
    private double vx, vy;
    private int maxSpeed;
    private int color;
    private int age;
    private boolean isTouched;
    private Paint paint;
    private DecimalFormat df = new DecimalFormat("0.##");
    private double goal;
    private double score;
    private int currentLevel;

    protected static Random random = new Random();

    public static double rndDbl(double min, double max) {
        double range = max - min;
        double scaled = random.nextDouble() * range;
        double shifted = scaled + min;
        return shifted; // == (rand.nextDouble() * (max-min)) + min;
    }

    public Object(int x, int y, int level) {
        Log.d(TAG, "Object: Created");
        this.x = x;
        this.y = y;
        width = Resources.getSystem().getDisplayMetrics().widthPixels;
        height = Resources.getSystem().getDisplayMetrics().heightPixels;
        SIZE = width * 50 / 720;

        currentLevel = level;
        vx = vy = 0;
        maxSpeed = level*10;
        resetSpeed();

        isTouched = false;
        age = 0;
        goal = 50 * currentLevel;
        score = 0;

        color = Color.argb(255, random.nextInt(155)+100, random.nextInt(155)+100, random.nextInt(155)+100);
        paint = new Paint(color);
    }

    private void resetSpeed() {     //Changes speed direction
        double speedLimit = maxSpeed;
        if(vx != 0 || vy != 0) {
            speedLimit *= (Math.pow(vx,2)+Math.pow(vy,2))/Math.pow(maxSpeed,2);
        }
        this.vx = (rndDbl(0, speedLimit * 2) - speedLimit);
        this.vy = (rndDbl(0, speedLimit * 2) - speedLimit);
        if(Math.pow(vx,2) + Math.pow(vy,2) > Math.pow(speedLimit,2)) {
            vx *= 0.7;
            vy *= 0.7;
        }
    }
    public void update() {

        age++;
        if(age%20 == 0) {
            resetSpeed();
        }
        if(isTouched) {         //Speed Decreases slightly on touch
//            vx *= SPEED_DEC;
//            vy *= SPEED_DEC;
            score += 10 * currentLevel;
        }
        else {
            if (Math.pow(vx, 2) + Math.pow(vy, 2) < Math.pow(maxSpeed, 2)) {    //Speed goes back up when not touched
                double increase;
                increase = Math.sqrt(Math.pow(maxSpeed, 2) / (Math.pow(vx, 2) + Math.pow(vy, 2)));
                vx *= increase;
                vy *= increase;
            }
        }
        if (this.getX() <= 0) {
            if (!this.isMovingRight()) {
                this.invertSpeedX();
            }
        } else if (this.getX() >= width - SIZE) {
            if (this.isMovingRight()) {
                this.invertSpeedX();
            }
        }
        if (this.getY() <= 50) {
            if (!this.isMovingDown()) {
                this.invertSpeedY();
            }
        } else if (this.getY() >= height - SIZE - 50) {
            if (this.isMovingDown()) {
                this.invertSpeedY();
            }
        }
        x += vx;
        y += vy;
//        Log.d(TAG, "update: Current Speeds:  " + String.valueOf(vx) + ", " + String.valueOf(vy));
    }

    public void draw(Canvas canvas) {
//        Log.d(TAG, "draw: ");
        paint.setColor(color);
        canvas.drawRect(x, y, x+SIZE, y+SIZE, paint);

        Paint paintScore = new Paint();
        paintScore.setARGB(255, 255, 255, 255);
//        canvas.drawText(("To Win: " + df.format(goal - score)), 10, 10, paintScore);
    }

    public void handleActionDown(float eventX, float eventY) {
        if(eventX >= x && eventX <= x+SIZE) {
            if (eventY >= y && eventY <= y+SIZE) {
                isTouched = true;
            }
            else {
                isTouched = false;
            }
        }
        else {
            isTouched = false;
        }
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public boolean isMovingRight() { return vx >= 0;}
    public boolean isMovingDown() { return vy >= 0;}
    public void invertSpeedX() { this.vx *= -1; }
    public void invertSpeedY() { this.vy *= -1; }
    public boolean GameWon() { return this.score >= goal; }
    public double scoreRequired() { return goal - this.score; }
    public double totalScore() { return this.score + 50 * (currentLevel - 1) * currentLevel / 2; }
    public boolean isTouched() { return isTouched; }

    public void handleActionUp() {
        isTouched = false;
    }
}
