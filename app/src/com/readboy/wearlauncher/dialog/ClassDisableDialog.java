package com.readboy.wearlauncher.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.readboy.wearlauncher.R;

/**
 * Created by Administrator on 2017/7/11.
 */

public class ClassDisableDialog {
    private static Dialog dialog = null;
    public static void showClassDisableDialog(Context context){
        if(dialog == null){
            dialog = new Dialog(context,R.style.dialog_fs);//(new AlertDialog.Builder(context)).create();
        }
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        //dialog.getWindow().getAttributes().systemUiVisibility |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        //View view = View.inflate(context, R.layout.dialog_class_disable, null);
        dialog.getWindow().setContentView(R.layout.dialog_class_disable);
        dialog.getWindow().getDecorView().findViewById(R.id.imageButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissClassDisableDialog();
            }
        });
        dialog.show();
        mHandler.removeMessages(0x10);
        mHandler.sendEmptyMessageDelayed(0x10,1000*2);
//        DisplayMetrics dm = new DisplayMetrics();
//        dm = context.getApplicationContext().getResources().getDisplayMetrics();
//        int screenWidth = dm.widthPixels;
//        int screenHeight = dm.heightPixels;
//        android.view.WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
//        params.width = screenWidth;
//        params.height = screenHeight;
//        dialog.getWindow().setAttributes(params);
    }

    public static void dismissClassDisableDialog(){
        if(dialog != null && dialog.isShowing()) {
            mHandler.removeMessages(0x10);
            dialog.dismiss();
            dialog = null;
        }
    }

    static Handler mHandler = new Handler(){
        @Override
        public void dispatchMessage(Message msg) {
            dismissClassDisableDialog();
            super.dispatchMessage(msg);
        }
    };
}
