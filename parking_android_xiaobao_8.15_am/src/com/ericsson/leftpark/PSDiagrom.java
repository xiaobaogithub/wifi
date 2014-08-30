package com.ericsson.leftpark;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;

public class PSDiagrom extends View {
	
	Paint paint;
	List<Point> points = new ArrayList<Point>();
	int count;
	Rect rect;
	String[] timeString = {"8:00", "8:10", "8:20", "8:30", "8:40", "8:50", "9:00"};
	String[] weekString = {"Mon", "Tue", "Wen", "Thu", "Fri"};
	static int OFFX = 60;
	static int OFFY = 50;
	int w,h;
	int xSpace;   
	int ySpace;
	Bitmap background = null;
	private static int textSize = 0;

	public PSDiagrom(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public PSDiagrom(Context context, AttributeSet attrs) {
		super(context, attrs);
		paint = new Paint();
		paint.setColor(Color.GRAY);
		paint.setStyle(Style.FILL_AND_STROKE);
		if(textSize == 0) {
			final float scale = context.getResources().getDisplayMetrics().density;
			textSize = (int)(12*scale + 0.5f);
			OFFX = (int)(40*scale + 0.5f);
//			OFFY =(int)(20*scale + 0.5f);
		}
		paint.setTextSize(textSize);
		WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
		w = wm.getDefaultDisplay().getWidth()-80;
		h = wm.getDefaultDisplay().getHeight()/2;
		background = BitmapFactory.decodeResource(getResources(), R.drawable.background);  
		xSpace = (w-OFFX*2)/5;
		ySpace = (h-OFFY*2)/6;
		count = 6;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		 
	}
	
	
	
	@Override
	public void draw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.draw(canvas);
		
		paint.setColor(Color.GRAY);
		
		for(int i=0; i<5; i++) {
			canvas.drawText(weekString[i], OFFX + xSpace*(i+1)-10, h, paint);
		}
		
		for(int i=0; i<=count; i++) {
			canvas.drawText(timeString[i], 0, h - OFFY - ySpace*i, paint);
		}
//		paint.setStrokeWidth(1);
		paint.setColor(Color.GRAY);
		canvas.drawLine(OFFX, h-OFFY, OFFX, 0, paint);
		canvas.drawLine(OFFX, h-OFFY, w, h-OFFY, paint);
		for(int n=0; n<=count<<1; n++) {
			canvas.drawLine(OFFX, h-OFFY - ySpace*n/2, w, h-OFFY - ySpace*n/2, paint);
		}
		
		if(points.size() <= 0) {
			return;
		}
//		paint.setStrokeWidth(2);
		paint.setColor(Color.BLUE);
		for(int k=0; k<points.size()-1; k++) {
			paint.setStyle(Style.FILL);
			canvas.drawCircle(points.get(k).x, points.get(k).y, 4, paint);
			canvas.drawLine(points.get(k).x, points.get(k).y, points.get(k+1).x, points.get(k+1).y, paint);
//			System.out.println("point1 ==" + points.get(k).x + ":" + points.get(k).y + "point2==" + points.get(k+1).x + points.get(k+1).y);
		}
		canvas.drawCircle(points.get(points.size()-1).x, points.get(points.size()-1).y, 4, paint);
	}

	public void initDiagrom(List<Point> points, int count) {
		this.points = points;
		this.count = count;
		
	}
	
	public int caculatePoints(List<Integer> list) {
		int x = 0;
		for(int j=0; j<list.size(); j++) {
			if(x < list.get(j)) {
				x = list.get(j);
			}
		}
		
		count = x/10;
		
		if(count < 3 ) {
			count = 3;
		}
		else if(count < 6) {
			count ++;
		} 
		
//		System.out.println("count ==" + count);
		
		ySpace = (h-OFFY*2)/count;
		points.clear();
		for(int i=0; i<list.size(); i++) {
			Point p = new Point();
			p.x = OFFX + (i+1)*xSpace;
			p.y = h - list.get(i)*(h-OFFY*2)/(count*10) - OFFY;		
			points.add(p);		
		}				
		return count;
	}
	
	public void clear(Canvas canvas) {		
		paint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
        canvas.drawPaint(paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC));
    }

}