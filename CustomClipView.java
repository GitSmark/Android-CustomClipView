package hxy.ttt.com.customclipview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by huangxy on 2016/4/1.
 * Origin by https://github.com/GitSmark/Android-CustomClipView
 */

public class CustomClipView extends View {

	//clip picture of type
	public final static int CLIPTYPE1 = 0; //正方形
	public final static int CLIPTYPE2 = 1; //固定宽高矩形
	public final static int CLIPTYPE3 = 2; //等比例矩形

	//These matrices will be used to move and zoom image
	private Matrix savedMatrix = new Matrix();
	private Matrix matrix = new Matrix();

	//Remember some things for zooming
	private PointF start = new PointF();
	private PointF mid = new PointF();
	private float oldDist = 1f;

	//We can be in one of these 3 states
	private final int NONE = 0;
	private final int DRAG = 1;
	private final int ZOOM = 2;
	private int mode = NONE;

	//default Clip width, height, type
	private boolean hadDraw = false;
	private int clipwidth = 1200;
	private int clipheight= 1200;
	private int cliptype  = 0;

	private int width;
	private int height;

	public CustomClipView(Context context) {
		super(context);
	}

	public CustomClipView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CustomClipView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		hadDraw = true;
		setView();

//		Paint paint = new Paint();
//		paint.setColor(0xaa000000);
//		canvas.drawRect(0, 0, width, height / 4, paint);
//		canvas.drawRect(0, height / 4, (width - height / 2) / 2, height * 3 / 4, paint);
//		canvas.drawRect((width + height / 2) / 2, height / 4, width, height * 3 / 4, paint);
//		canvas.drawRect(0, height * 3 / 4, width, height, paint);
//		paint.setColor(getResources().getColor(R.color.white));
//		canvas.drawRect((width - height / 2) / 2 - 1, height / 4 - 1, (width + height / 2) / 2 + 1, (height / 4), paint);
//		canvas.drawRect((width - height / 2) / 2 - 1, height / 4, (width - height / 2) / 2, height * 3 / 4, paint);
//		canvas.drawRect((width + height / 2) / 2, height / 4, (width + height / 2) / 2 + 1, height * 3 / 4, paint);
//		canvas.drawRect((width - height / 2) / 2 - 1, height * 3 / 4, (width + height / 2) / 2 + 1, height * 3 / 4 + 1, paint);

		//drawRect of clip width, height
		Paint paint = new Paint();
		paint.setColor(0xaa000000);
		canvas.drawRect(0, 0, width, (height - clipheight)/2, paint);
		canvas.drawRect(0, (height - clipheight)/2, (width - clipwidth)/2, (height + clipheight)/2, paint);
		canvas.drawRect((width + clipwidth)/2, (height - clipheight)/2, width, (height + clipheight)/2, paint);
		canvas.drawRect(0, (height + clipheight)/2, width, height, paint);

		paint.setColor(0xffffff);
		canvas.drawRect((width - clipwidth)/2, (height - clipheight)/2 - 1, (width + clipwidth)/2, (height - clipheight)/2 + 1, paint);
		canvas.drawRect((width - clipwidth)/2 - 1, (height - clipheight)/2, (width - clipwidth)/2 + 1, (height + clipheight)/2, paint);
		canvas.drawRect((width + clipwidth)/2 - 1, (height - clipheight)/2, (width + clipwidth)/2 + 1, (height + clipheight)/2, paint);
		canvas.drawRect((width - clipwidth)/2, (height + clipheight)/2 - 1, (width + clipwidth)/2, (height + clipheight)/2 + 1, paint);

	}

	public boolean onTouch(View v, MotionEvent event) {
		// Handle touch events here...
		ImageView view = (ImageView) v;
		switch (event.getAction() & MotionEvent.ACTION_MASK)
		{
			case MotionEvent.ACTION_DOWN:
				savedMatrix.set(matrix);
				start.set(event.getX(), event.getY());
				mode = DRAG;
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				oldDist = spacing(event);
				if (oldDist > 10f)
				{
					savedMatrix.set(matrix);
					midPoint(mid, event);
					mode = ZOOM;
				}
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
				mode = NONE;
				break;
			case MotionEvent.ACTION_MOVE:
				if (mode == DRAG)
				{
					// ...
					matrix.set(savedMatrix);
					matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
				} else if (mode == ZOOM)
				{
					float newDist = spacing(event);
					if (newDist > 10f)
					{
						matrix.set(savedMatrix);
						float scale = newDist / oldDist;
						matrix.postScale(scale, scale, mid.x, mid.y);
					}
				}
				break;
		}

		view.setImageMatrix(matrix);
		return true;
	}

	/** Determine the space between the first two fingers */
	private float spacing(MotionEvent event)
	{
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	/** Calculate the mid point of the first two fingers */
	private void midPoint(PointF point, MotionEvent event)
	{
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}

	public String ActionClip(Window window, String filedir){

		Bitmap fianBitmap = getBitmap(window);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		fianBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		byte[] bitmapByte = baos.toByteArray();
		try {
			File file = new File(filedir);
			if(!file.exists()){
				file.mkdirs();
			}

			String imageFileName = filedir + "PIC_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".jpg";
			FileOutputStream fos = new FileOutputStream(imageFileName);
			fos.write(baos.toByteArray());
			fos.flush();
			fos.close();
			return imageFileName;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public Bitmap getBitmap(Window window){

		int[] location = new int[2];
		this.getLocationOnScreen(location);

		setView();
		//getBarHeight(window);
		Bitmap screenShoot = takeScreenShot(window);
		Bitmap finalBitmap = Bitmap.createBitmap(screenShoot, (width - clipwidth)/2+1 + location[0], (height - clipheight)/2+1 + location[1], clipwidth-2, clipheight-2);
//		Bitmap finalBitmap = Bitmap.createBitmap(screenShoot, (width - height / 2) / 2, height / 4 + titleBarHeight + statusBarHeight + head, height / 2, height / 2);
		return finalBitmap;

	}

//	int titleBarHeight = 0;
//	int statusBarHeight = 0;
//	private void getBarHeight(Window window)
//	{
//		Rect frame = new Rect();
//		window.getDecorView().getWindowVisibleDisplayFrame(frame);
//		statusBarHeight = frame.top;
//		int contenttop = window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
//		titleBarHeight = contenttop - statusBarHeight;
//	}

	private Bitmap takeScreenShot(Window window)
	{
		View view = window.getDecorView();
		view.setDrawingCacheEnabled(false);
		view.destroyDrawingCache();
		view.buildDrawingCache();
		return view.getDrawingCache();
	}

	private void setView() {

		width = this.getWidth();
		height = this.getHeight();
		clipwidth = (clipwidth > width) ? width : clipwidth;
		switch (cliptype) {
			case CLIPTYPE1:
				clipheight = clipwidth;
				break;
			case CLIPTYPE2:
				clipheight = (clipheight > height) ? height : clipheight;
				break;
			case CLIPTYPE3:
				clipheight = (clipwidth > width) ? (int) Math.ceil(clipheight/(clipwidth*1.0/clipwidth)) : clipheight;
				if(clipheight > height){
					clipheight = height;
					clipwidth = (int) Math.ceil(clipwidth/(clipheight*1.0/clipheight));
				}
				break;
			default:
				break;
		}
	}

	public void setClipParamsPx(int width, int height, int type){
		clipwidth = width;
		clipheight = height;
		cliptype = type;
		if (hadDraw) {
			setView();
			invalidate();
		}
	}

	public void setClipParamsDp(int width, int height, int type){
		clipwidth = dip2px(width);
		clipheight = dip2px(height);
		cliptype = type;
		if (hadDraw) {
			setView();
			invalidate();
		}
	}

	/**
	 * 获取设备屏幕宽度 px
	 */
	private int getDisplayWidthPx() {
		return getContext().getResources().getDisplayMetrics().widthPixels;
	}

	/**
	 * 获取设备屏幕高度 px
	 */
	private int getDisplayHeightPx() {
		return getContext().getResources().getDisplayMetrics().heightPixels;
	}

	/**
	 * 根据手机的分辨率从 dp的单位 转成为 px(像素)
	 */
	private int dip2px(float dpValue) {
		final float scale = getContext().getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	/**
	 * 根据手机的分辨率从 px(像素)的单位 转成为 dp
	 */
	private int px2dip( float pxValue) {
		final float scale = getContext().getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}
}
