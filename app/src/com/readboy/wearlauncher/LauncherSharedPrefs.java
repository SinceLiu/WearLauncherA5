package com.readboy.wearlauncher;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by GuanJiaYin on 16/11/21.
 */

public class LauncherSharedPrefs implements SharedPreferences.OnSharedPreferenceChangeListener{
    private static final String TAG = "Launcher.LauncherSharedPrefs";
    private static final boolean LOGD = true;
    private String shared_name = "settings";
    private Context mContext;

    private SharedPreferences preferences;

    private SharedPreferences.Editor editor;
    /**
     * 学习模式
     */
    public static String ModeLearnKey = "ModeLearnKey";
    /**
     * 运动模式
     */
    public static String ModeSportKey = "ModeSportKey";
    /**
     * 个性模式
     */
    public static String ModePersonalKey = "ModePersonalKey";
    /**
     * 当前模式（学习模式、运动模式、个性模式）
     */
    public static String CurrentModeKey = "CurrentModeKey";

    public static final String WATCHTYPE = "watchtype";

    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    public LauncherSharedPrefs(Context context){
        mContext = context;
        preferences = mContext.getSharedPreferences(shared_name,Context.MODE_PRIVATE);

        editor = preferences.edit();

    }

    /**
     * 获取当前模式
     * @return
     */
    public String getCurrentMode(){
        return preferences.getString(CurrentModeKey,ModeLearnKey);
    }

    /**
     * 设置当前模式
     * @param mode
     */
    public void setCurrentMode(String mode){
        editor.putString(CurrentModeKey,mode);
        editor.commit();
    }

    public void registerChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener){
        preferences.registerOnSharedPreferenceChangeListener(this);
        this.listener = listener;
    }
    public void unregisterChangeListener(){
        preferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * 获取表盘类型
     * @param mode
     * @return
     */
    public int getDialIndex(String mode){
        int dialIndex = preferences.getInt(mode,-1);
        if (dialIndex == -1){//未设置，默认各个模式的初始表盘
            if(mode.equals(ModeLearnKey)){
                return 0;
            }
            else if(mode.equals(ModeSportKey)){
                return 2;
            }
            else if(mode.equals(ModePersonalKey)){
                return 4;
            }
        }
        return dialIndex;//返回设置后的表盘
    }

    /**
     * 设置表盘类型
     * @param mode
     * @param index
     */
    public void  setDialModeType(String mode,int index){
        editor.putInt(mode,index);
        editor.commit();
    }

    public int getWatchType(){
        return preferences.getInt(WATCHTYPE,0);
    }

    public void setWatchtype(int watchtype){
        editor.putInt(WATCHTYPE,watchtype);
        editor.commit();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        this.listener.onSharedPreferenceChanged(sharedPreferences,s);
    }


}
