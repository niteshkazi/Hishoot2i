package org.illegaller.ratabb.hishoot2i.util;

import java.io.IOException;
import java.io.InputStream;

import org.illegaller.ratabb.hishoot2i.R;
import org.illegaller.ratabb.hishoot2i.skin.GetResources;
import org.illegaller.ratabb.hishoot2i.skin.SkinDescription;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.AsyncTask;
import android.util.Log;

public class ImageTask extends AsyncTask<Void, Void, Bitmap> {
	private static final String TAG = "Hishoot:ImageTask";
	private OnImageTaskListener mListener;
	private SkinDescription skinDescription;
	private Point wh;
	private Context mContext;
	boolean mThrow = false, single = false, blur = false,
			hideWattermark = false;

	public interface OnImageTaskListener {
		void onPostResult(Bitmap result);

		void onPre();

		void onThrow(boolean flag);

		String packageTemplate();

		String[] bitmapAll();

		Point WH();

		Bitmap[] wat();

		Boolean oneSS();

		Boolean onBlur();

		Boolean onHideWm();

	}

	public ImageTask(Context context, OnImageTaskListener listener)
			throws Throwable {
		mListener = listener;
		mContext = context;
	}

	@Override
	protected void onPreExecute() {
		mListener.onPre();
		mThrow = false;
		wh = mListener.WH();
		single = mListener.oneSS();
		blur = mListener.onBlur();
		hideWattermark = mListener.onHideWm();
	}

	@Override
	protected void onPostExecute(Bitmap result) {
		mListener.onPostResult(result);
		mListener.onThrow(mThrow);
	}

	@Override
	protected Bitmap doInBackground(Void... params) {
		//
		System.gc();
		long startMs = System.currentTimeMillis();
		int lebar = wh.x, tinggi = wh.y;
		String[] allBitmap = mListener.bitmapAll();

		Bitmap ss1 = BitmapUtil
				.loadImage(mContext, allBitmap[0], lebar, tinggi);
		Bitmap ss2 = BitmapUtil
				.loadImage(mContext, allBitmap[1], lebar, tinggi);
		Bitmap wall = BitmapUtil.loadImage(mContext, allBitmap[2]);

		int TL = getDimensionPixelSize(R.dimen.def_tl);
		int TT = getDimensionPixelSize(R.dimen.def_tt);
		int BL = getDimensionPixelSize(R.dimen.def_bl);
		int BT = getDimensionPixelSize(R.dimen.def_bt);

		int topx = (int) (TL);
		int topy = (int) (TT);
		int totx = (int) ((TL + BL));
		int toty = (int) ((TT + BT));

		Bitmap frame = BitmapUtil.getNine(R.drawable.frame1, lebar + totx,
				tinggi + toty, mContext);

		String skinPkg = mListener.packageTemplate();
		if (skinPkg != null) {
			GetResources getResources = new GetResources(mContext);
			Bitmap framefrom = getResources.getImage(skinPkg, "skin");

			Context c;
			InputStream is = null;
			try {
				c = mContext.createPackageContext(skinPkg, 0);
				AssetManager am = c.getAssets();
				is = am.open("keterangan.xml");
				if (is != null) {
					skinDescription = new SkinDescription(is);
					is.close();
				}
			} catch (PackageManager.NameNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			TL = notNol(skinDescription.getTx());
			TT = notNol(skinDescription.getTy());
			BL = notNol(skinDescription.getBx());
			BT = notNol(skinDescription.getBy());

			topx = (int) (TL);
			topy = (int) (TT);
			totx = (int) ((TL + BL));
			toty = (int) ((TT + BT));

			BitmapUtil.recycleBitmap(frame);
			frame = Bitmap.createScaledBitmap(framefrom, lebar + totx, tinggi
					+ toty, true);
			BitmapUtil.recycleBitmap(framefrom);

		}
		Bitmap xwall = null, mixthem = null, mix1 = null, mix2 = null;
		try {
			mix1 = BitmapUtil.DrawMe(frame, 0, 0, ss1, topx, topy, mContext,
					lebar + totx, tinggi + toty);
			BitmapUtil.recycleBitmap(ss1);
			mix2 = (single) ? null : BitmapUtil.DrawMe(frame, 0, 0, ss2, topx,
					topy, mContext, lebar + totx, tinggi + toty);

			BitmapUtil.recycleBitmap(frame);
			BitmapUtil.recycleBitmap(ss2);

			mixthem = Bitmap.createBitmap(
					(single) ? mix1.getWidth() : mix1.getWidth() * 2,
					mix1.getHeight(), Bitmap.Config.ARGB_8888);

			Point point = getPointMax(single,
					new Point(wall.getWidth(), wall.getHeight()), new Point(
							mixthem.getWidth(), mixthem.getHeight()));

			xwall = BitmapUtil.resizeImage(wall, point.x, point.y);
		} catch (Throwable e) {
			// OOM ?
			Log.e(TAG, "OOM");
			mThrow = true;
			xwall = wall;
			mixthem = BitmapUtil.copyFrom(xwall);
		}

		Bitmap wm = mListener.wat()[0];
		@SuppressWarnings("unused")
		Bitmap wmi = mListener.wat()[1];
		Canvas canvas = new Canvas(mixthem);

		if (blur) {
			BitmapUtil.drawItBlur(canvas, xwall, mContext);
		} else {
			BitmapUtil.drawIt(canvas, xwall, 0, 0);
		}
		BitmapUtil.recycleBitmap(wall);
		BitmapUtil.recycleBitmap(xwall);

		BitmapUtil.drawIt(canvas, mix1, 0, 0);
		BitmapUtil.recycleBitmap(mix1);
		if (!single) {
			BitmapUtil.drawIt(canvas, mix2, mix1.getWidth(), 0);
			BitmapUtil.recycleBitmap(mix2);
		}

		if (!hideWattermark) {
			BitmapUtil.drawIt(canvas, wm,
					(mixthem.getWidth() / 2) - (wm.getWidth() / 2),
					(mixthem.getHeight() - wm.getHeight()));
			// XXX
			// BitmapUtil.drawIt(canvas, wmi, mixthem.getWidth() -
			// wmi.getWidth(),
			// 0);
		}
		Log.d(TAG, "doInBackground: " + (System.currentTimeMillis() - startMs)
				+ "ms");
		return mixthem;
	}

	private int notNol(int i) {
		return (i > 0) ? i : 1;
	}

	private int getDimensionPixelSize(int dimenId) {
		return mContext.getResources().getDimensionPixelSize(dimenId);
	}

	private Point getPointMax(boolean flag, Point wall, Point mix) {
		int w = mix.x, h = mix.y;
		boolean wmin = wall.x < mix.x, hmin = wall.y < mix.y;

		if (flag) {
			if (wmin) {
				h = mix.y * mix.y;
				w = mix.x;
			}
			if (hmin) {
				h = mix.y;
				w = mix.x * mix.x;
			}
		} else {
			if (hmin) {
				h = mix.y;
				w = mix.x * mix.x;
			}
			if (wmin) {
				h = mix.y * mix.y;
				w = mix.x;
			}
		}

		return new Point(w, h);
	}

}
