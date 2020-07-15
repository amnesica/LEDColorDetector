package com.amnesica.ledcolordetector;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

/**
 * Class which draws a circle with a specific position and a specific radius
 */
@SuppressLint("ViewConstructor")
class DrawCircle extends View {
    private int screenCenterX;
    private int screenCenterY;
    private final int radius = 100;
    private Paint p;

    public DrawCircle(Context context, int screenCenterX, int screenCenterY) {
        super(context);
        //center circle
        this.screenCenterX = screenCenterX;
        this.screenCenterY = screenCenterY;
        p = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //draw circle
        p.setColor(Color.BLUE);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(20f);

        canvas.drawCircle(screenCenterX, screenCenterY, radius, p);
        invalidate();
        super.onDraw(canvas);
    }

    public int getScreenCenterX() {
        return screenCenterX;
    }

    public void setScreenCenterX(int screenCenterX) {
        this.screenCenterX = screenCenterX;
    }

    public int getScreenCenterY() {
        return screenCenterY;
    }

    public void setScreenCenterY(int screenCenterY) {
        this.screenCenterY = screenCenterY;
    }

    public int getRadius() {
        return radius;
    }

    public Paint getP() {
        return p;
    }

    public void setP(Paint p) {
        this.p = p;
    }
}