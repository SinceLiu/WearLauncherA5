package com.readboy.wearlauncher.utils;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.telecom.TelecomManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author oubin
 * @date 2019/1/23
 */
public class ClassForbidUtils {
    private static final String TAG = "ClassForbidUtils";
    private static final int RINGER_MODE_IDLE = -1;
    private static int mLastRingerMode = RINGER_MODE_IDLE;

    private static String CATEGORY_CLASS_FORBID = "readboy.intent.category.CLASS_FORBID";

    private ClassForbidUtils() {
    }

    private static boolean isInCall(String pkgName, Context context) {
        if (!pkgName.equals("com.android.dialer")) {
            return false;
        }
        TelecomManager tm = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
        return tm.isInCall();
    }

    public static void killRecentTask(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RecentTaskInfo> appRecentList = am.getRecentTasksForUser(100,
                ActivityManager.RECENT_WITH_EXCLUDED, UserHandle.CURRENT.getIdentifier());
        List<Integer> idList = new ArrayList<Integer>();
        List<String> packageNameList = new ArrayList<String>();
        int size = appRecentList.size();
        for (int i = 0; i < size; i++) {
            ActivityManager.RecentTaskInfo info = appRecentList.get(i);
            if (info.baseIntent != null) {
                Set<String> categories = info.baseIntent.getCategories();
                if (categories != null
                        && (categories.contains(Intent.CATEGORY_HOME)
                        || categories.contains(CATEGORY_CLASS_FORBID))) {
                    continue;
                }

                ComponentName component = info.baseIntent.getComponent();
                if (component != null && component.getPackageName().equals(context.getPackageName())) {
                    continue;
                }

                if (component != null && isInCall(component.getPackageName(), context)) {
                    TelecomManager tm = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
                    if (tm != null) {
                        tm.endCall();
                    }
                    continue;
                }


                if (component != null && component.getPackageName().equals("com.readboy.wearlauncher")) {
                    continue;
                }

                if (component != null && component.getPackageName().equals("com.readboy.wear.rbsos")) {
                    continue;
                }

                if (component != null && component.getPackageName().equals("com.readboy.rbfota")) {
                    continue;
                }
            }

            idList.add(info.id);
            idList.add(info.persistentId);
            packageNameList.add(info.baseIntent.getComponent().getPackageName());
        }

        for (int id : idList) {
            am.removeTask(id);
        }
        Log.e(TAG, "上课禁用或低电杀进程：" + packageNameList);
        for (String pkgName : packageNameList) {
            am.forceStopPackage(pkgName);
        }
    }

    private static void setClassDisableAudioMode(boolean enable, Context context) {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int mode = am.getRingerMode();
        if (enable) {
            //am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            if (mode == AudioManager.RINGER_MODE_SILENT || mode == AudioManager.RINGER_MODE_VIBRATE) {
                return;
            }
            if (mLastRingerMode == RINGER_MODE_IDLE) {
                mLastRingerMode = mode;
            }
            am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
        } else {
            if (mLastRingerMode != RINGER_MODE_IDLE && mode == AudioManager.RINGER_MODE_VIBRATE) {
                am.setRingerMode(mLastRingerMode);
            }
            mLastRingerMode = RINGER_MODE_IDLE;
        }
    }

    public static void handleClassForbid(boolean enable, Context context) {
        if (enable) {
            killRecentTask(context);
        }
        setClassDisableAudioMode(enable, context);
    }

}
