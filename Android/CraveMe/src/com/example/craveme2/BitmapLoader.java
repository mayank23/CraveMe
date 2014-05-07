package com.example.craveme2;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;
 

public class BitmapLoader extends AsyncTaskLoader<Bitmap>{
	private String url;
	private String path;
	private byte[] bytes;
	private int reqWidth;
	private int reqHeight;
	private Bitmap mData;
	
	public BitmapLoader(Context context, String url, int reqWidth, int reqHeight) {
		super(context);
		this.url = url;
		this.reqHeight = reqHeight;
		this.reqWidth = reqWidth;
	}
	
	public BitmapLoader(Context context, byte[] bytes, int reqWidth, int reqHeight) {
		super(context);
		this.bytes = bytes;
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
	public Bitmap decodeSampledBitmap(int reqWidth, int reqHeight) {

	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    if(bytes != null) {
	    	BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
	    }
	    else if(path != null) {
	    	BitmapFactory.decodeFile(path, options);
	    }
	    else {
	    	return null;
	    }

	    Log.i("options", options.outWidth + " " + options.outHeight);
	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    options.inPreferredConfig = Bitmap.Config.RGB_565;
	    if(bytes != null) {
	    	return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
	    }
	    else if(path != null) {
	    	return BitmapFactory.decodeFile(path, options);
	    }
	    else {
	    	return null;
	    }
	}

	private static final String HOSTNAME = "data.cs.purdue.edu";
    private static final int PORT = 9012;
    private static final int NET_CONNECT_TIMEOUT_MILLIS = 15000;  // 15 seconds
    private static final int NET_READ_TIMEOUT_MILLIS = 10000;  // 10 seconds
    
	@Override
	public Bitmap loadInBackground() {
		try {
			if(url != null) {
				File outputDir = getContext().getCacheDir();
				int fileExtStart = url.lastIndexOf('.') + 1;
				int fileExtEnd = fileExtStart + 3;
				File outputFile = File.createTempFile("prefix", url.substring(fileExtStart, fileExtEnd), outputDir);
				InputStream in;
				OutputStream out;
				if(url.subSequence(0,  6).equals("photos")) {
					JSONObject obj = new JSONObject();
					obj.put("option", "get_photo");
					obj.put("server_file_path", url.replace("\\", ""));
					Socket s = new Socket();
			    	SocketAddress remoteAddr = new InetSocketAddress(HOSTNAME, PORT);
			    	s.setSoTimeout(NET_READ_TIMEOUT_MILLIS);
			    	s.connect(remoteAddr, NET_CONNECT_TIMEOUT_MILLIS);
			    	OutputStreamWriter wr = new OutputStreamWriter(s.getOutputStream());
			    	wr.write(obj.toString());
			    	wr.append('\n');
			    	wr.flush();
			    	in = s.getInputStream();
				}
				else {
					URLConnection conn = new URL(url).openConnection();
					in = conn.getInputStream();
				}
				out = new FileOutputStream(outputFile);
				byte[] buf = new byte[4096];
				int read;
				while((read = in.read(buf)) != -1) {
					out.write(buf, 0, read);
				}
				in.close();
				out.close();
				path = outputFile.getAbsolutePath();
			}
			Bitmap bmp = decodeSampledBitmap(reqWidth, reqHeight);
			Log.i("decode", "" + bmp);
			mData = bmp;
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
