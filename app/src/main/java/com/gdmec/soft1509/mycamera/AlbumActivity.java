package com.gdmec.soft1509.mycamera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.io.File;
import java.util.Vector;

public class AlbumActivity extends AppCompatActivity {

    private ViewFlipper flipper;
    private Bitmap[] mBgList;
    private long startTime = 0;
    private SensorManager sm;
    private SensorEventListener sel;

    public String[] loadAlbum() {
        String pathName = android.os.Environment.getExternalStorageDirectory().getPath() +
                "/mycamera";
        File file = new File(pathName);
        Vector<Bitmap> fileName = new Vector<>();
        if (file.exists() && file.isDirectory()) {
            String[] strs = file.list();
            for (String s : strs) {
                if (new File(pathName + "/" + s).isFile()) {
                    fileName.addElement(loadImage(pathName + "/" + s));
                }
            }
            mBgList = fileName.toArray(new Bitmap[]{});
            Log.d("AlbumActivity", "mBgList.length:" + mBgList.length);
        }
        return null;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        flipper = (ViewFlipper) findViewById(R.id.ViewFlipper01);
        loadAlbum();
        if (mBgList == null) {
            Toast.makeText(this, "相册无图片", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            for (int i = 0; i < mBgList.length; i++) {
                Log.d("AlbumActivity", "i:" + i);
                flipper.addView(
                        addImage(mBgList[i]), i,
                        new ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT)
                );
            }
        }
        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor sensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sel = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                float x = sensorEvent.values[SensorManager.DATA_X];
                if (x > 10 && System.currentTimeMillis() > startTime + 1000) {
                    startTime = System.currentTimeMillis();
                    flipper.setInAnimation(AnimationUtils.loadAnimation(AlbumActivity.this, R.anim.push_right_in));
                    flipper.setInAnimation(AnimationUtils.loadAnimation(AlbumActivity.this, R.anim.push_right_out));
                    flipper.showPrevious();
                } else if (x < -10 && System.currentTimeMillis() > startTime + 1000) {
                    startTime = System.currentTimeMillis();
                    flipper.setInAnimation(AnimationUtils.loadAnimation(AlbumActivity.this, R.anim.push_right_in));
                    flipper.setInAnimation(AnimationUtils.loadAnimation(AlbumActivity.this, R.anim.push_right_out));
                    flipper.showNext();
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
        sm.registerListener(sel, sensor, SensorManager.SENSOR_DELAY_GAME);
    }

    private Bitmap loadImage(String s) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(s, options);

        WindowManager manager = getWindowManager();
        Display display = manager.getDefaultDisplay();

        int screenWidth = display.getWidth();
        options.inSampleSize = options.outWidth / screenWidth;
        options.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile(s, options);
        return bitmap;
    }

    private View addImage(Bitmap bitmap) {
        ImageView imageView = new ImageView(this);
        imageView.setImageBitmap(bitmap);
        return imageView;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sm.unregisterListener(sel);
    }
}
