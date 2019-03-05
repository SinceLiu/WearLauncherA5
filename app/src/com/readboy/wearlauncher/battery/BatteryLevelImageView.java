package com.readboy.wearlauncher.battery;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.readboy.wearlauncher.R;

public class BatteryLevelImageView extends ImageView implements BatteryController.BatteryStateChangeCallback{

    private BatteryController mBatteryController;
    private Context mContext;
    private int mLevel = -1;
    private int mAnimOffset;
    private boolean mCharging;
    private boolean mPluggedIn;
    private static final int ADD_LEVEL = 10;
    private static final int ANIM_DURATION = 500;
    private static final int FULL = 96;
    private Handler mHandler = new Handler();
    private Bitmap chargingBitmap;
    private Bitmap bitmap;
    private Bitmap emptyBitmap;
    private Bitmap chargingFullBitmap;
    private Bitmap fullBitmap;
    private Bitmap lowBitmap;
    private int emptyBitmapWidth;
    private int emptyBitmapHeight;
    private Canvas chargingCanvas;
    private Canvas canvas;
    private Rect rect;
    private Paint paint;

	public BatteryLevelImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mContext = context;
	}

    private final Runnable mInvalidate = new Runnable() {
        @Override
        public void run() {
            final int level = updateChargingAnimLevel();
            setBackgroundResource(0);
            if (mPluggedIn && mCharging) {
                setImageBitmap(createBatteryChargingImage(level));
            } else if (level > 0) {
                setImageBitmap(createBatteryImage(level));
            }
        }
    };

	@Override
	public void onBatteryLevelChanged(int level, boolean pluggedIn,
			boolean charging) {
		// TODO Auto-generated method stub
		mLevel = level;
		mPluggedIn = pluggedIn;
		mCharging =charging;
		mHandler.post(mInvalidate);
//		if(pluggedIn){
//			//anim
//			setBackgroundResource(R.anim.battery_plugged_anim);
//			AnimationDrawable anim = (AnimationDrawable) getBackground();
//			anim.start();
//			setImageResource(R.drawable.battery_plugged_1);
//		}else{
//			setBackgroundResource(0);
//			setImageBitmap(createBatteryImage(level));
//		}
	}

	private int updateChargingAnimLevel() {
		int curLevel = mLevel;
		if (!mCharging) {
			mAnimOffset = 0;
			mHandler.removeCallbacks(mInvalidate);
		} else {
			curLevel += mAnimOffset;
			if (curLevel >= FULL) {
				curLevel = 100;
				mAnimOffset = 0;
			} else {
				mAnimOffset += ADD_LEVEL;
			}

			mHandler.removeCallbacks(mInvalidate);
			mHandler.postDelayed(mInvalidate, ANIM_DURATION);
		}
		return curLevel;
	}

    public void init() {
        emptyBitmap = BitmapFactory.decodeResource(this.getContext().getResources(), R.drawable.battery_nor_empty);
        fullBitmap = BitmapFactory.decodeResource(this.getContext().getResources(), R.drawable.battery_nor_full);
        chargingFullBitmap = BitmapFactory.decodeResource(this.getContext().getResources(), R.drawable.battery_charging_full);
        lowBitmap = BitmapFactory.decodeResource(this.getContext().getResources(), R.drawable.battery_nor_low);
        emptyBitmapWidth = emptyBitmap.getWidth();
        emptyBitmapHeight = emptyBitmap.getHeight();
        chargingBitmap = Bitmap.createBitmap(emptyBitmapWidth, emptyBitmapHeight, emptyBitmap.getConfig());
        chargingCanvas = new Canvas(chargingBitmap);
        rect = new Rect(0, 0, 0, emptyBitmapHeight);
        bitmap = Bitmap.createBitmap(emptyBitmapWidth, emptyBitmapHeight, emptyBitmap.getConfig());
        canvas = new Canvas(bitmap);
        paint = new Paint();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    private Bitmap createBatteryChargingImage(int level) {
        chargingCanvas.drawPaint(paint);  //清空画布
        chargingCanvas.drawBitmap(emptyBitmap, new Matrix(), null);
        rect.right = (emptyBitmapWidth * 22 / 30) * level / 100 + emptyBitmapWidth * 3 / 30;
        chargingCanvas.drawBitmap(chargingFullBitmap, rect, rect, null);
		return chargingBitmap;
    }

    private Bitmap createBatteryImage(int level) {
        canvas.drawPaint(paint);
        canvas.drawBitmap(emptyBitmap, new Matrix(), null);
        rect.right = (emptyBitmapWidth * 22 / 30) * level / 100 + emptyBitmapWidth * 3 / 30;
        if (level < 20) {
            canvas.drawBitmap(lowBitmap, rect, rect, null);
        } else {
            canvas.drawBitmap(fullBitmap, rect, rect, null);
        }
        return bitmap;
    }

	@Override
	public void onPowerSaveChanged() {
		// TODO Auto-generated method stub
		
	}

    public void setBatteryController(int level) {
        mLevel = level;
        if (mBatteryController == null) {
            mBatteryController = new BatteryController(mContext);
        }
        mBatteryController.addStateChangedCallback(this);
    }

    public void cancelBatteryController() {
        if (mBatteryController != null) {
            mBatteryController.removeStateChangedCallback(this);
            mBatteryController.unregisterReceiver();
            mBatteryController = null;
        }
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        init();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        canvas = null;
        chargingCanvas = null;
        paint = null;
        bitmap = null;
        emptyBitmap = null;
        fullBitmap = null;
        lowBitmap = null;
        chargingBitmap = null;
        chargingFullBitmap = null;
    }


}
