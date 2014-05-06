package edu.purdue.craveme;


import java.io.FileDescriptor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

public class BitmapLoader extends AsyncTaskLoader<Bitmap> {

	private Uri uri;

	private int reqWidth;
	private int reqHeight;
	private Bitmap mData;
	
	public BitmapLoader(Context context, Uri uri, int reqWidth, int reqHeight) {
		super(context);
		this.uri = uri;
		this.reqHeight = reqHeight;
		this.reqWidth = reqWidth;
	}
	
	//http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
	public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
	    // Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;
	
	    if (height > reqHeight || width > reqWidth) {
	
	        final int halfHeight = height / 2;
	        final int halfWidth = width / 2;
	
	        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
	        // height and width larger than the requested height and width.
	        while ((halfHeight / inSampleSize) > reqHeight
	                && (halfWidth / inSampleSize) > reqWidth) {
	            inSampleSize *= 2;
	        }
	    }
	
	    return inSampleSize;
	}
	
	//http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
	public static Bitmap decodeSampledBitmapFromFileDesc(FileDescriptor fd,
	        int reqWidth, int reqHeight) {

	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeFileDescriptor(fd, null, options);

	    Log.i("options", options.outWidth + " " + options.outHeight);
	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    options.inPreferredConfig = Bitmap.Config.RGB_565;
	    return BitmapFactory.decodeFileDescriptor(fd, null, options);
	}

	@Override
	public Bitmap loadInBackground() {
		try {
			Log.i("uri", uri.toString());
			ParcelFileDescriptor fd = getContext().getContentResolver().openFileDescriptor(uri, "r");
			Log.i("fd", fd.toString());
			Bitmap bmp = decodeSampledBitmapFromFileDesc(fd.getFileDescriptor(), reqWidth, reqHeight);
			Log.i("decode", "" + bmp);
			return bmp;
		}
		catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void onStartLoading() {
		Log.i("CALLED", "start");
	  if (mData != null) {
	    deliverResult(mData);
	  }
	  else{
	    forceLoad();
	  }
	}
}
