package com.xstd.qm.fakecover;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.view.*;
import com.xstd.quick.R;

/**
 * Created by michael on 14-3-2.
 */
public class FakeScreenError {

    private Context context;
    private View coverView;
    private WindowManager wm;

    public FakeScreenError(Context context) {
        this.context = context;
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        coverView = layoutInflater.inflate(R.layout.fake_screen_error, null);
        wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    public void dismiss() {
        if (coverView != null) {
            wm.removeView(coverView);
        }

        coverView = null;
    }

    public void show() {
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
        float density = dm.density;

        //cover
        WindowManager.LayoutParams wMParams = new WindowManager.LayoutParams();
        wMParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        wMParams.format = PixelFormat.RGBA_8888;
        wMParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        wMParams.width = WindowManager.LayoutParams.FILL_PARENT;
        wMParams.height = screenHeight - (int) ((48 + 25) * density);
        wMParams.gravity = Gravity.LEFT | Gravity.TOP;
        coverView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                return true;
            }
        });
        wm.addView(coverView, wMParams);
    }

}
