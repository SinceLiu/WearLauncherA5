package com.readboy.wearlauncher.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.antonyt.infiniteviewpager.GalleryTransformer;
import com.antonyt.infiniteviewpager.InfinitePagerAdapter;
import com.readboy.wearlauncher.DialPane.DialCircleIndicator;
import com.readboy.wearlauncher.DialPane.DialPagerAdapter;
import com.readboy.wearlauncher.LauncherApplication;
import com.readboy.wearlauncher.LauncherSharedPrefs;
import com.readboy.wearlauncher.R;
import com.readboy.wearlauncher.utils.Utils;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/6/13.
 */

public class WatchDials extends FrameLayout {

    public static final ArrayList<Integer> mDialList = new ArrayList<Integer>(){{
        add(R.layout.dialtype_g_layout_cell);
        add(R.layout.dialtype_b_layout_cell);
        add(R.layout.dialtype_k_layout_cell);
        add(R.layout.dialtype_n_layout_cell);
        add(R.layout.dialtype_j_layout_cell);
        add(R.layout.dialtype_c_layout_cell);
        add(R.layout.dialtype_e_layout_cell);
        add(R.layout.dialtype_m_layout_cell);
        //add(R.layout.dialtype_o_layout_cell);//低电模式
        //add(R.layout.dialtype_h_layout_cell);
        //add(R.layout.dialtype_i_layout_cell);
        //add(R.layout.dialtype_a_layout_cell);
        //add(R.layout.dialtype_d_layout_cell);//低电模式
        //add(R.layout.dialtype_f_layout_cell);
        //add(R.layout.dialtype_l_layout_cell);

    }};
    public static final int ANIMATE_STATE_IDLE = 0;
    public static final int ANIMATE_STATE_OPENING = 1;
    public static final int ANIMATE_STATE_OPENED = 2;
    public static final int ANIMATE_STATE_CLOSING = 3;
    public static final int ANIMATE_STATE_CLOSED = 4;
    public static int mWatchDialsStatus = ANIMATE_STATE_IDLE;
    private Context mContext;
    private LauncherSharedPrefs mSharedPrefs;

    private ObjectAnimator mOpenCloseAnimator;

    private DialCircleIndicator mDialCircleIndicator;
    private ViewPager mViewPager;

    private int mExpandDuration = 300;
    private int mLastDialIndex;

    public WatchDials(Context context) {
        this(context,null);
    }

    public WatchDials(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public WatchDials(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        mSharedPrefs = ((LauncherApplication)context.getApplicationContext()).getSharedPrefs();

        mLastDialIndex = mSharedPrefs.getWatchType();
    }

    public static WatchDials fromXml(Context context) {
        return (WatchDials) LayoutInflater.from(context).inflate(R.layout.watch_dials, null);
    }

    public static int getWatchDialsStatus(){
        return mWatchDialsStatus;
    }

    public void animateOpen(){
        mWatchDialsStatus = ANIMATE_STATE_OPENING;
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 1.46f,1.0f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY",1.46f,1.0f);
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 0.25f,1.0f);
        final ObjectAnimator oa = mOpenCloseAnimator =
                LauncherAnimUtils.ofPropertyValuesHolder(this,  scaleX, scaleY, alpha);

        oa.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {

            }
            @Override
            public void onAnimationEnd(Animator animation) {
                setLayerType(LAYER_TYPE_NONE, null);

                setScaleX(1);
                setScaleY(1);
                setAlpha(1);
                mWatchDialsStatus = ANIMATE_STATE_OPENED;
            }
        });
        oa.setDuration(mExpandDuration);
        setLayerType(LAYER_TYPE_HARDWARE, null);
        oa.start();
    }

    public void animateClose(boolean saveMode){
        if(saveMode){
            setChooseModeType();
        }
        mWatchDialsStatus = ANIMATE_STATE_CLOSING;
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 1.0f, 1.46f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 1.0f, 1.46f);
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 1.0f,0.25f);
        final ObjectAnimator oa = mOpenCloseAnimator =
                LauncherAnimUtils.ofPropertyValuesHolder(this,  scaleX, scaleY, alpha);

        oa.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                ViewGroup parent = (ViewGroup) WatchDials.this.getParent();
                if (parent != null) {
                    parent.removeView(WatchDials.this);
                }
                setLayerType(LAYER_TYPE_NONE, null);
                mWatchDialsStatus = ANIMATE_STATE_CLOSED;
            }
            @Override
            public void onAnimationStart(Animator animation) {

            }
        });
        oa.setDuration(mExpandDuration);
        setLayerType(LAYER_TYPE_HARDWARE, null);
        oa.start();
    }

    public void setChooseModeType(){
        int type = mViewPager.getCurrentItem();
        mSharedPrefs.setWatchtype(type);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mViewPager = (ViewPager) findViewById(R.id.watch_type_vpid);
        DialPagerAdapter dialPagerAdapter = new DialPagerAdapter(mContext,mDialList);
        PagerAdapter wrappedAdapter = new InfinitePagerAdapter(dialPagerAdapter);
        mViewPager.setAdapter(wrappedAdapter);
        mViewPager.setCurrentItem(mLastDialIndex);
        mViewPager.setPageTransformer(true, new GalleryTransformer());
        mViewPager.setPageMargin(-Utils.px2dip(mContext,50));
        mViewPager.setOffscreenPageLimit(3);
    }
}
