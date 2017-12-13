package com.readboy.wearlauncher.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.readboy.wearlauncher.LauncherApplication;
import com.readboy.wearlauncher.R;
import com.readboy.wearlauncher.utils.WatchController;

import java.util.Calendar;

/**
 * TODO: document your custom view class.
 */
public class WatchDialTypeH extends DialBaseLayout{
    LauncherApplication mApplication;
    private Context mContext;


    public WatchDialTypeH(Context context) {
        super(context);
        init(context,null, 0);
    }

    public WatchDialTypeH(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs, 0);
    }
    public WatchDialTypeH(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context,attrs, defStyle);

    }

    private void init(Context context,AttributeSet attrs, int defStyle) {
        // Load attributes
        mContext = context;
        mApplication = (LauncherApplication) context.getApplicationContext();

    }

    @Override
    public void addChangedCallback() {
        addDateChangedCallback();
        mDigitClock.setTimeRunning();
    }

    @Override
    public void setButtonEnable() {

    }

    @Override
    public void onStepChange(int step) {

    }

    @Override
    public void onCallUnreadChanged(int unreadNum) {

    }

    @Override
    public void onWeTalkUnreadChanged(int unreadNum) {

    }

    @Override
    public void onDateChange(int year, int month, int day, int week) {
        setDate();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }
    protected void onFinishInflate() {
        super.onFinishInflate();
        mDigitClock = (DigitClock) findViewById(R.id.digit_clock);
        mDigitClock.setCurTime();
        setDate();
    }

    private void setDate(){
        TextView mDateText = (TextView) findViewById(R.id.date_tvid);
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int week = (calendar.get(Calendar.DAY_OF_WEEK) - 1) % WatchController.WEEK_NAME_CN_LONG.length;
        //String dateFormat = String.format("%d %s %d",day, WatchController.MONTHS_NAME_EN_SHORT[month],year);
        String dateFormat = String.format("%d/%d  %s",month,day,WatchController.WEEK_NAME_CN_LONG[week]);
        mDateText.setText(dateFormat);
    }

}
