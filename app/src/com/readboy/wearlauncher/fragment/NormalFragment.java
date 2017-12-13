package com.readboy.wearlauncher.fragment;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.readboy.wearlauncher.Launcher;
import com.readboy.wearlauncher.LauncherApplication;
import com.readboy.wearlauncher.LauncherSharedPrefs;
import com.readboy.wearlauncher.notification.NotificationActivity;
import com.readboy.wearlauncher.R;
import com.readboy.wearlauncher.application.AppInfo;
import com.readboy.wearlauncher.application.AppsLoader;
import com.readboy.wearlauncher.battery.BatteryController;
import com.readboy.wearlauncher.dialog.ClassDisableDialog;
import com.readboy.wearlauncher.utils.Utils;
import com.readboy.wearlauncher.view.DaialParentLayout;
import com.readboy.wearlauncher.view.DialBaseLayout;
import com.readboy.wearlauncher.view.GestureView;
import com.readboy.wearlauncher.view.WatchAppGridView;
import com.readboy.wearlauncher.view.WatchDials;

import java.util.ArrayList;
import java.util.List;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

/**
 * Created by Administrator on 2017/7/7.
 */

public class NormalFragment extends Fragment implements LoaderManager.LoaderCallbacks<ArrayList<AppInfo>>,WatchAppGridView.OnClickItemListener,
        GestureView.MyGestureListener, BatteryController.BatteryStateChangeCallback {
    private static final String TAG = "NormalFragment";
    private static final int LOADER_ID = 0x10;

    private View mRootView;
    private GestureView mGestureView;
    DialBaseLayout mLowDialBaseLayout;
    private Context mContext;
    private LauncherSharedPrefs mSharedPrefs;
    private LayoutInflater mInflater;
    private ViewPager mViewpager;
    private ViewPagerAdpater mViewPagerAdpater;
    private List<View> mViewList = new ArrayList<View>();
    private View mNegativeView;
    private DaialParentLayout mDaialView;
    private WatchAppGridView mAppView;
    private WatchDials mWatchDials;
    int mTouchSlopSquare;
    int mViewPagerScrollState = ViewPager.SCROLL_STATE_IDLE;
    private int mWatchType;

    BatteryController mBatteryController;
    int mBatteryLevel = -1;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mBatteryController = new BatteryController(getContext());
        mBatteryController.addStateChangedCallback(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mBatteryController.unregisterReceiver();
        mBatteryController.removeStateChangedCallback(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
        mSharedPrefs = ((LauncherApplication)mContext.getApplicationContext()).getSharedPrefs();
        mWatchType = mSharedPrefs.getWatchType();
        /*Bundle bundle = getArguments();
        if (bundle != null) {
            mWatchType = bundle.getInt("watch_type");
            bundle.clear();
        }*/
        ViewConfiguration configuration = ViewConfiguration.get(mContext);
        int touchSlop = configuration.getScaledTouchSlop();
        mTouchSlopSquare = touchSlop * 20;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mInflater = inflater;
        mRootView = inflater.inflate(R.layout.fragment_normal, container, false);
        mGestureView = (GestureView) mRootView.findViewById(R.id.content_container);
        mGestureView.setGestureListener(this);
        mLowDialBaseLayout = (DialBaseLayout) mRootView.findViewById(R.id.low);

        mNegativeView = inflater.inflate(R.layout.negative_screen,null);

        mDaialView = (DaialParentLayout) inflater.inflate(R.layout.watch_dial_layout, null);
        mDaialView.removeAllViews();
        DialBaseLayout childDaialView = (DialBaseLayout) inflater.inflate(WatchDials.mDialList.get(mWatchType%WatchDials.mDialList.size()), mDaialView, false);
        childDaialView.addChangedCallback();
        childDaialView.setButtonEnable();
        mDaialView.addView(childDaialView);
        mAppView = (WatchAppGridView) inflater.inflate(R.layout.watch_app_gridview,null);
        mAppView.setOnClickItemListener(this);
        mViewList.clear();
        mViewList.add(mNegativeView);
        mViewList.add(mDaialView);
        mViewList.add(mAppView);
        loadApps(false);
        mViewpager = (ViewPager) mRootView.findViewById(R.id.viewpager);
        mViewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                mViewPagerScrollState = state;
            }
        });
        mViewPagerAdpater = new ViewPagerAdpater(mViewList);
        mViewpager.setAdapter(mViewPagerAdpater);
        mViewpager.setOffscreenPageLimit(3);
        mViewpager.setCurrentItem(1);
        OverScrollDecoratorHelper.setUpOverScroll(mViewpager);

        return mRootView;
    }

    @Override
    public void onClick(int position) {
        AppInfo info = mAppView.getAppInfo(position);
        Utils.startActivity(mContext,info.mPackageName, info.mClassName);
    }

    @Override
    public Loader<ArrayList<AppInfo>> onCreateLoader(int i, Bundle bundle) {
        if (i != LOADER_ID) {
            return null;
        }
        //return new AppsLoader(mContext);
        return null;
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<AppInfo>> loader, ArrayList<AppInfo> appInfos) {
        if (loader.getId() == LOADER_ID) {
            mAppView.refreshData(appInfos);
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<AppInfo>> loader) {
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if(WatchDials.getWatchDialsStatus() == WatchDials.ANIMATE_STATE_OPENING ||
                WatchDials.getWatchDialsStatus() == WatchDials.ANIMATE_STATE_OPENED){
            return false;
        }
        int cur = mViewpager.getCurrentItem();
        if(e1 == null || e2 == null || cur != 1){
            return false;
        }
        float vDistance = e1.getY() - e2.getY();
        boolean bVerticalMove = Math.abs(velocityX) - Math.abs(velocityY) < 0;
        if(vDistance > mTouchSlopSquare && bVerticalMove && mViewPagerScrollState == ViewPager.SCROLL_STATE_IDLE){
            Utils.startActivity(mContext,"com.readboy.watch.speech","com.readboy.watch.speech.Main2Activity");
            return true;
        }else if(vDistance < -mTouchSlopSquare && bVerticalMove && mViewPagerScrollState == ViewPager.SCROLL_STATE_IDLE){
            boolean isEnable = ((LauncherApplication)LauncherApplication.getApplication()).getWatchController().isNowEnable();
            if(isEnable){
                ClassDisableDialog.showClassDisableDialog(mContext);
                return true;
            }
            if(!isNotificationEnabled()){
                startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
            }else {
                getActivity().startActivity(new Intent(getContext(),NotificationActivity.class));
                Log.d(TAG,"lzx switchToFragment");
                //Utils.startActivity(mContext, "com.readboy.wearlauncher",NotificationActivity.class.getName());
                //((Launcher)getActivity()).switchToFragment(NotificationFragment.class.getName(),null,true,true);
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if(WatchDials.getWatchDialsStatus() == WatchDials.ANIMATE_STATE_OPENING ||
                WatchDials.getWatchDialsStatus() == WatchDials.ANIMATE_STATE_OPENED){
            mGestureView.setIsGestureDrag(true);
            closeDials();
            mWatchType = mSharedPrefs.getWatchType();
            setDialFromType(mWatchType);
        }
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        if(WatchDials.getWatchDialsStatus() == WatchDials.ANIMATE_STATE_OPENING ||
                WatchDials.getWatchDialsStatus() == WatchDials.ANIMATE_STATE_OPENED){
            return;
        }
        int cur = mViewpager.getCurrentItem();
        if(cur == 1){
            mGestureView.setIsGestureDrag(true);
            openDials();
        }
    }

    @Override
    public void onBatteryLevelChanged(int level, boolean pluggedIn, boolean charging) {
        if(mGestureView == null || mLowDialBaseLayout == null) return;
        if(mBatteryLevel == -1 || Math.abs(mBatteryLevel - level) > 5){
            mBatteryLevel = level;
            if(mBatteryLevel < 15){//low powe
                mGestureView.setVisibility(View.INVISIBLE);
                mLowDialBaseLayout.setVisibility(View.VISIBLE);
                mLowDialBaseLayout.addChangedCallback();
                mLowDialBaseLayout.setButtonEnable();
            } else{
                mGestureView.setVisibility(View.VISIBLE);
                mLowDialBaseLayout.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void onPowerSaveChanged() {

    }

    class ViewPagerAdpater extends PagerAdapter {
        public List<View> mViewList;

        public ViewPagerAdpater(List<View> viewList){
            this.mViewList = viewList;
        }

        public void setData(List<View> viewList){
            mViewList = viewList;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mViewList !=null ? mViewList.size() : 0;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            if (position >= 0 && position < mViewList.size()){
                container.removeView(mViewList.get(position));
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            // TODO Auto-generated method stub
            container.addView(mViewList.get(position));
            return mViewList.get(position);
        }
    }

    private void loadApps(boolean reLoad){
        if (reLoad) {
            getLoaderManager().restartLoader(LOADER_ID, null, this);
        } else {
            getLoaderManager().initLoader(LOADER_ID, null, this);
        }
    }

    private void openDials(){
        mWatchDials = WatchDials.fromXml(mContext);
        mGestureView.cancelLongPress();
        mGestureView.addView(mWatchDials);
        mWatchDials.animateOpen();
    }

    private void closeDials(){
        if(mWatchDials != null && mWatchDials.isShown()){
            mWatchDials.animateClose(true);
        }
    }

    private boolean isNotificationEnabled() {
        String pkgName = this.getActivity().getPackageName();
        final String flat = Settings.Secure.getString(mContext.getContentResolver(),
                "enabled_notification_listeners");
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void setDialFromType(int type){
        mDaialView.removeAllViews();
        DialBaseLayout childDaialView = (DialBaseLayout) mInflater.inflate(WatchDials.mDialList.get(type%WatchDials.mDialList.size()), mDaialView, false);
        childDaialView.addChangedCallback();
        childDaialView.setButtonEnable();
        mDaialView.addView(childDaialView);
        mViewPagerAdpater.setData(mViewList);
    }

    private void createNotification(){
        NotificationManager manager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder ncBuilder = new NotificationCompat.Builder(getContext());
        ncBuilder.setContentTitle("Notification Test");
        ncBuilder.setContentText("Notification Listener Service");
        ncBuilder.setSmallIcon(R.drawable.negative_icon_alarm);
        Bundle extras = new Bundle();
        extras.putString("extra_type","readboy");
        ncBuilder.setExtras(extras);
        ncBuilder.setAutoCancel(true);
        manager.notify((int)System.currentTimeMillis(),ncBuilder.build());
    }

    private void switchToFragment(Fragment fragment,boolean addToBackStack, boolean withTransition) {
        FragmentManager mFragmentManager = getChildFragmentManager();
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
//		if (withTransition) {
//			transaction.setCustomAnimations(R.anim.push_in_right,
//					R.anim.push_out_right, R.anim.back_in_left,
//					R.anim.back_out_left);
//		}
		if (addToBackStack) {
			transaction.addToBackStack(fragment.getClass().getSimpleName());
		}
        transaction.replace(R.id.content_container, fragment, fragment.getClass().getSimpleName());
        transaction.commit();
        mFragmentManager.executePendingTransactions();
    }
}
