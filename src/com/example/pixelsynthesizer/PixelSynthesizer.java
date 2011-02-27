package com.example.pixelsynthesizer;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.content.ContentResolver;
import android.media.AudioTrack;
import android.media.AudioManager;
import android.media.AudioFormat;
import android.net.Uri;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Display;
import android.widget.LinearLayout;
import android.widget.ImageView;

public class PixelSynthesizer extends Activity
{
	static int REQUEST_PICK_CONTACT = 0;
	ImageView mImageView;
	LinearLayout mLinearLayout;
	Bitmap mBitmap;
	Canvas mCanvas;
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType("image/*");
		startActivityForResult(intent, REQUEST_PICK_CONTACT);

		mImageView = new ImageView(this) {
			public boolean onTouchEvent(MotionEvent e) {
				if(e.getAction() != MotionEvent.ACTION_DOWN) return true;
				int x = (int)e.getX();
				int y = (int)e.getY();
				int r = 45;
				int sx = x - r > 0 ? x - r: 0;
				int sy = y - r > 0 ? y - r: 0;
				sx = sx + r*2 < mBitmap.getWidth() ? sx : mBitmap.getWidth() - r*2;
				sy = sy + r*2 < mBitmap.getHeight() ? sy : mBitmap.getHeight() - r*2;

				//Canvas canvas = new Canvas(mBitmap);
				//Paint paint = new Paint();
				//paint.setColor(0xFFFFFFFF);
				//paint.setStyle(Paint.Style.FILL);
				//canvas.drawRect(sx, sy, ex, ey, paint);
				//BitmapDrawable bitmapDrawable = new BitmapDrawable(mBitmap);
				//bitmapDrawable.setBounds(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
				//bitmapDrawable.draw(canvas);
				//mBitmap = bitmapDrawable.getBitmap();
				//mImageView.setImageBitmap(mBitmap);
				int[] pixels = new int[r*2*r*2];
				mBitmap.getPixels(pixels, 0, r*2, sx, sy, r*2, r*2);
				short[] audioData = synthesizePixels(pixels);
				play(audioData);

				return true;
			}
		};
		setContentView(mImageView);
	}
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    	try {
			ContentResolver contentResolver = getContentResolver();
			Bitmap bitmap = MediaStore.Images.Media.getBitmap(contentResolver, data.getData());
			Display display = getWindowManager().getDefaultDisplay();
			Log.v("hoge", "disp:"+display.getWidth()+":"+display.getHeight());
			mBitmap = Bitmap.createScaledBitmap(bitmap, display.getWidth(), display.getHeight(), true);
			Log.v("hoge", "bmp:"+mBitmap.getWidth()+":"+mBitmap.getHeight());
			mImageView.setImageBitmap(mBitmap);
			mImageView.setScaleType(ImageView.ScaleType.CENTER);
			Log.v("hoge", "iv:"+mImageView.getWidth()+":"+mImageView.getHeight());
		} catch(FileNotFoundException e) {
		} catch(IOException e) {
		}
	}
	public short[] synthesizePixels(int[] pixels) {
	    	short[] audioData = new short[pixels.length];
		for(int i = 0; i < pixels.length; i++) {
			int color = pixels[i];
			float[] hsv = new float[3];
			Color.colorToHSV(color, hsv);
			float s = hsv[2];
			audioData[i] = (short)(s*Short.MAX_VALUE*2 - Short.MAX_VALUE);
		}
		return audioData;
	}
	public void play(short[] audioData) {
		AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, 8000, AudioFormat.CHANNEL_CONFIGURATION_DEFAULT, AudioFormat.ENCODING_PCM_16BIT, 8000, AudioTrack.MODE_STATIC);
		track.write(audioData, 0, 2000);
		track.play();
		//track.stop();
		//track.release();
	}
}
