/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.readboy.wearlauncher.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.readboy.wearlauncher.R;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;


/**
 * This widget display an analogic clock with two hands for hours and
 * minutes.
 */
public class AnalogClock extends View {
    private boolean mAttached;
    private Drawable mHourHand;
    private Drawable mMinuteHand;
    private Drawable mSecondHand;
    private Drawable mDial;

    private final int mDialWidth;
    private final int mDialHeight;

    private float mSeconds;
    private float mMinutes;
    private float mHour;
    private boolean mChanged;
    private final Context mContext;
    private boolean mNoSeconds = false;
    private boolean mNoHour = false;
    private boolean mNominute = false;

    private final float mDotRadius;
    private final float mDotOffset;
    private Paint mDotPaint;

    public AnalogClock(Context context) {
        this(context, null);
    }

    public AnalogClock(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnalogClock(Context context, AttributeSet attrs,
                       int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        Resources r = mContext.getResources();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AnalogClock);
        mDotRadius = a.getDimension(R.styleable.AnalogClock_jewelRadius, 0);
        mDotOffset = a.getDimension(R.styleable.AnalogClock_jewelOffset, 0);
        
        mDial = a.getDrawable(R.styleable.AnalogClock_analogDial);
        mHourHand = a.getDrawable(R.styleable.AnalogClock_analogHour);
        mMinuteHand = a.getDrawable(R.styleable.AnalogClock_analogMinute);
        mSecondHand = a.getDrawable(R.styleable.AnalogClock_analogSecond);
        mNoSeconds = a.getBoolean(R.styleable.AnalogClock_noSecond, false);
        mNoHour = a.getBoolean(R.styleable.AnalogClock_noHour, false);
        mNominute = a.getBoolean(R.styleable.AnalogClock_noMinute, false);

        if (mDial == null){
        	mDial = r.getDrawable(R.drawable.clock_analog_dial);
        }
        if (mHourHand == null){
        	mHourHand = r.getDrawable(R.drawable.clock_analog_hour);
        }
        if (mMinuteHand == null){
        	mMinuteHand = r.getDrawable(R.drawable.clock_analog_minute);
        }
        if (mSecondHand == null){
        	mSecondHand = r.getDrawable(R.drawable.clock_analog_second);
        }
        
        final int dotColor = a.getColor(R.styleable.AnalogClock_jewelColor, Color.TRANSPARENT);
        if (dotColor != 0) {
            mDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mDotPaint.setColor(dotColor);
        }

        mDialWidth = mDial.getIntrinsicWidth();
        mDialHeight = mDial.getIntrinsicHeight();
        
        a.recycle();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mChanged = true;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!mAttached) {
            mAttached = true;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAttached) {
            removeCallbacks(mClockTick);
            mAttached = false;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        boolean changed = mChanged;
        if (changed) {
            mChanged = false;
        }

        int availableWidth = getWidth();
        int availableHeight = getHeight();

        int x = availableWidth / 2;
        int y = availableHeight / 2;

        if(!mNoHour){
            drawHand(canvas, mHourHand, x, y, mHour / 12.0f * 360.0f, changed);
        }
        if(!mNominute){
            drawHand(canvas, mMinuteHand, x, y, mMinutes / 60.0f * 360.0f, changed);
        }
        if (!mNoSeconds) {
            drawHand(canvas, mSecondHand, x, y, mSeconds / 60.0f * 360.0f, changed);
        }
    }

    private void drawHand(Canvas canvas, Drawable hand, int x, int y, float angle,
          boolean changed) {
        canvas.save();
        canvas.rotate(angle, x, y);
        if (changed) {
          final int w = hand.getIntrinsicWidth();
          final int h = hand.getIntrinsicHeight();
          hand.setBounds(0, 0,  getWidth(), getHeight());
        }
        hand.draw(canvas);
        canvas.restore();
    }

    private void onTimeChanged() {
        GregorianCalendar time = new GregorianCalendar();
        time.setTime(new Date());
        int hour = time.get(Calendar.HOUR);
        int minute = time.get(Calendar.MINUTE);
        int second = time.get(Calendar.SECOND);
        int am_pm = time.get(Calendar.AM_PM);

        mSeconds = second;//(float) ((second * 1000 + millis) / 166.666);
        mMinutes = minute + second / 60.0f;
        mHour = hour + mMinutes / 60.0f;
        mChanged = true;
        invalidate();
    }

    private final Runnable mClockTick = new Runnable () {

        @Override
        public void run() {
            onTimeChanged();

            Calendar calendar = Calendar.getInstance();
            int currentMillisecond = calendar.get(Calendar.MILLISECOND);
            AnalogClock.this.postDelayed(mClockTick, 1000 - currentMillisecond);
        }
    };

    public void setTimeRunning(){
        post(mClockTick);
    }

    public void setCurTime(){
        GregorianCalendar time = new GregorianCalendar();
        time.setTime(new Date());
        int hour = time.get(Calendar.HOUR);
        int minute = time.get(Calendar.MINUTE);
        int second = time.get(Calendar.SECOND);
        int am_pm = time.get(Calendar.AM_PM);

        mSeconds = second;//(float) ((second * 1000 + millis) / 166.666);
        mMinutes = minute + second / 60.0f;
        mHour = hour + mMinutes / 60.0f;
        mChanged = true;

        postInvalidate();
    }


    public void setTime(float hour, float minute, float second){
        mHour = hour;
        mMinutes = minute;
        mSeconds = second;
        postInvalidate();
    }

    public void setTime(float hour, float minute, float second, boolean hourHandDisplay, boolean minuteHandDisplay, boolean secondHandDisplay){
        mHour = hour;
        mMinutes = minute;
        mSeconds = second;
        mNoHour = !hourHandDisplay;
        mNominute = !minuteHandDisplay;
        mNoSeconds = !secondHandDisplay;
        postInvalidate();
    }
}

