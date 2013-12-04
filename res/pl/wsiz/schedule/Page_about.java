package pl.wsiz.schedule;

import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

enum Direction {
	UP, RIGHT, DOWN, LEFT
}

public class Page_about extends Activity {
	Bitmap myBitmap, bitmapFlag;
	MySurfaceView mySurfaceView;
	int x1,y1,x2,y2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mySurfaceView = new MySurfaceView(this);
		setContentView(R.layout.about);
		((Button) findViewById(R.id.about_back)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		bitmapFlag = ((BitmapDrawable) getResources().getDrawable( R.drawable.photo2)).getBitmap();
		myBitmap = ((BitmapDrawable) getResources().getDrawable( R.drawable.photo)).getBitmap();
		x1 = (int) (0.54 * myBitmap.getWidth());
		y1 = (int) (0.16 * myBitmap.getHeight());
		x2 = (int) (0.67 * myBitmap.getWidth());
		y2 = (int) (0.29 * myBitmap.getHeight());
		LinearLayout ll = (LinearLayout) findViewById(R.id.about_ll);
		ll.addView(mySurfaceView);
	}
	
	 @Override
	 protected void onResume() {
	  super.onResume();
	  mySurfaceView.onResumeMySurfaceView();
	 }
	 
	 @Override
	 protected void onPause() {
	  super.onPause();
	  mySurfaceView.onPauseMySurfaceView();
	 }
	 
	class MySurfaceView extends SurfaceView implements Runnable{
	     
	    Thread thread = null;
	    SurfaceHolder surfaceHolder;
	    volatile boolean running = false;
	     
	    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	    Random random;
	     
	    volatile boolean touched = false, act = false, anim = false;
	    volatile float touched_x, touched_y;
	    float x_prev, y_prev, length=0, p=1, angle=0;
	    float anim_a, anim_b;
	    float progress;
	    
	    int touchCount=0, time=0;
	    int showPic = 1; // 1 - flag, 2 - Ganja
	    boolean c1 = false,c2 = false,c3 = false,c4 = false,cheat_wait = false;
	 
	  public MySurfaceView(Context context) {
	   super(context);
	   // TODO Auto-generated constructor stub
	   surfaceHolder = getHolder();
	   random = new Random();
	  }
	   
	  public void onResumeMySurfaceView(){
	   running = true;
	   thread = new Thread(this);
	   thread.start();
	  }
	   
	  public void onPauseMySurfaceView(){
	   boolean retry = true;
	   running = false;
	   while(retry){
	    try {
	     thread.join();
	     retry = false;
	    } catch (InterruptedException e) {
	     // TODO Auto-generated catch block
	     e.printStackTrace();
	    }
	   }
	  }
	 
	  @Override
	  public void run() {
		  boolean first = true;
	   // TODO Auto-generated method stub
	   while(running){
	    if(surfaceHolder.getSurface().isValid()){
	     if(!touched && first!=true && !anim) continue;
	     Canvas canvas = surfaceHolder.lockCanvas();
	     //... actual drawing on canvas
	     
	     paint.setStyle(Paint.Style.FILL);
	     //paint.setStrokeWidth(3);
	     paint.setColor(0x77888888);
	     
	     canvas.drawRect(0, 0, 270, 270, paint);
	     
	     /*int w = canvas.getWidth();
	     int h = canvas.getHeight();
	     int x = random.nextInt(w-1); 
	     int y = random.nextInt(h-1);
	     int r = random.nextInt(255);
	     int g = random.nextInt(255);
	     int b = random.nextInt(255);
	     paint.setColor(0xff000000 + (r << 16) + (g << 8) + b);*/
	     paint.setColor(0xfffcfcfc);
	     
	     float to_x = (float) (10+p*Math.cos(Math.toRadians(angle)));
	     float to_y = (float) (10+p*Math.sin(Math.toRadians(angle)));
	     
	     canvas.drawBitmap(myBitmap, 10, 10, paint);
	     if (!anim)
	     canvas.drawRoundRect(new RectF(to_x+x1, to_y+y1, to_x+x2, to_y+y2), 25, 25, paint);
	     if (anim)
	     {
	    	 progress+=1;
	    	 canvas.drawRoundRect(new RectF(
	    			 (float)(x1+10+p*Math.cos(Math.toRadians(angle))-EaseOutBounce(progress,0,p,100)*Math.cos(Math.toRadians(angle))), 
		    		 (float)(y1+10+p*Math.sin(Math.toRadians(angle))-EaseOutBounce(progress,0,p,100)*Math.sin(Math.toRadians(angle))), 
		    		 (float)(x2+10+p*Math.cos(Math.toRadians(angle))-EaseOutBounce(progress,0,p,100)*Math.cos(Math.toRadians(angle))), 
		    		 (float)(y2+10+p*Math.sin(Math.toRadians(angle))-EaseOutBounce(progress,0,p,100)*Math.sin(Math.toRadians(angle))) ), 25, 25, paint);
	    	 if (progress>=100) { 
	    		 anim=false;
	    		 p=1;
	    		 angle=0;
	    	 }
	     }
	     
	     if (touchCount != 0)
	     {
	    	 paint.setColor(0xff00bc00);
	    	 //
	 		//x1 = (int) (0.54 * myBitmap.getWidth());
			//y1 = (int) (0.16 * myBitmap.getHeight());
	    	 //if (touchCount_wait)
	    	 if (cheat_wait && time<=50) time++;
	    	 if (cheat_wait && time>=50) {
	    		 cheat_wait = false;
	    		 time=0;
	    		 touchCount = 0;
				 c1 = c2 = c3 = c4 = false;
	    	 }
	    	 if (!c1 && touchCount>=1 && !cheat_wait){cheat_wait = true;}
	    	 if (!c2 && touchCount>=2 && !cheat_wait){cheat_wait = true;}
	    	 if (!c3 && touchCount>=3 && !cheat_wait){cheat_wait = true;}
	    	 if (!c4 && touchCount>=4 && !cheat_wait){cheat_wait = true;}
	    	 
	    	 
	    	 if (c1 && touchCount>=1){
	    		 paint.setColor(0xff00bc00);
	    	 canvas.drawRect(new RectF(
	    			 (float) (0.05 * canvas.getWidth()), 
	    			 (float) (0.95 * canvas.getHeight()), 
	    			 (float) (0.2 * canvas.getWidth()), 
	    			 (float) (1.0 * canvas.getHeight())), paint);
	    	 } else if (!c1 && touchCount>=1){
	    		 paint.setColor(0xffbc0000);
	    	 canvas.drawRect(new RectF(
	    			 (float) (0.05 * canvas.getWidth()), 
	    			 (float) (0.95 * canvas.getHeight()), 
	    			 (float) (0.2 * canvas.getWidth()), 
	    			 (float) (1.0 * canvas.getHeight())), paint);
	    	 }
	    	 
	    	 if (c2 && touchCount>=2){
	    		 paint.setColor(0xff00bc00);
	    		 canvas.drawRect(new RectF(
		    			 (float) (0.3 * canvas.getWidth()), 
		    			 (float) (0.95 * canvas.getHeight()), 
		    			 (float) (0.45 * canvas.getWidth()), 
		    			 (float) (1.0 * canvas.getHeight())), paint);
	    	 } else if (!c2 && touchCount>=2){
	    		 paint.setColor(0xffbc0000);
	    		 canvas.drawRect(new RectF(
		    			 (float) (0.3 * canvas.getWidth()), 
		    			 (float) (0.95 * canvas.getHeight()), 
		    			 (float) (0.45 * canvas.getWidth()), 
		    			 (float) (1.0 * canvas.getHeight())), paint);
	    	 }
	    	 
	    	 if (c3 && touchCount>=3){
	    		 paint.setColor(0xff00bc00);
	    		 canvas.drawRect(new RectF(
		    			 (float) (0.55 * canvas.getWidth()), 
		    			 (float) (0.95 * canvas.getHeight()), 
		    			 (float) (0.7 * canvas.getWidth()), 
		    			 (float) (1.0 * canvas.getHeight())), paint);
	    	 } else if (!c3 && touchCount>=3){
	    		 paint.setColor(0xffbc0000);
	    		 canvas.drawRect(new RectF(
		    			 (float) (0.55 * canvas.getWidth()), 
		    			 (float) (0.95 * canvas.getHeight()), 
		    			 (float) (0.7 * canvas.getWidth()), 
		    			 (float) (1.0 * canvas.getHeight())), paint);
	    	 }
	    	 
	    	 if (c4 && touchCount>=4){
	    		 paint.setColor(0xff00bc00);
	    		 canvas.drawRect(new RectF(
		    			 (float) (0.8 * canvas.getWidth()), 
		    			 (float) (0.95 * canvas.getHeight()), 
		    			 (float) (0.95 * canvas.getWidth()), 
		    			 (float) (1.0 * canvas.getHeight())), paint);
	    	 } else if (!c4 && touchCount>=4){
	    		 paint.setColor(0xffbc0000);
	    		 canvas.drawRect(new RectF(
		    			 (float) (0.8 * canvas.getWidth()), 
		    			 (float) (0.95 * canvas.getHeight()), 
		    			 (float) (0.95 * canvas.getWidth()), 
		    			 (float) (1.0 * canvas.getHeight())), paint);
	    	 }
	    	 if (c1 && c2 && c3 && c4 && touchCount == 4) {
	    		 paint.setColor(Color.WHITE);
	    		 canvas.drawRect(new RectF(0, 0, canvas.getWidth(), canvas.getHeight()), paint);
	    		 running = false;
	    		 
	    		 if (showPic == 2) { // Draw Ganja
	    		 paint.setColor(0xFF026600);
	    		 canvas.drawRect(new RectF(0, 0, canvas.getWidth(), canvas.getHeight()/3), paint);
	    		 paint.setColor(0xFFf7521c);
	    		 canvas.drawRect(new RectF(0, canvas.getHeight()/3, canvas.getWidth(), canvas.getHeight()/3*2), paint);
	    		 paint.setColor(0xFFa20000);
	    		 canvas.drawRect(new RectF(0, canvas.getHeight()/3*2, canvas.getWidth(), canvas.getHeight()), paint);
	    		 
	    		 paint.setStrokeWidth(4);
	    	     paint.setColor(Color.GREEN);
	    	     Path path = new Path();
	    	     float drawx = myBitmap.getWidth()/2;
	    	     for (float a=0; a<2*Math.PI; a+=0.002)
	    	     {	
	    	     	float r = (float) (40 * (1+Math.sin(a)) *(1+0.9*Math.cos(8*a))*(1+0.1*Math.cos(24*a)));
	    	     	if (a==0) path.moveTo((float)(r*Math.cos(a))+drawx, (float)(-r*Math.sin(a))+drawx*1.45f+20);
	    	     	else path.lineTo((float)(r*Math.cos(a))+drawx, (float)(-r*Math.sin(a))+drawx*1.45f+20);
	    	     	canvas.drawLine((float)(r*Math.cos(a))+drawx, (float)(-r*Math.sin(a))+drawx*1.45f+20,
	    	     			(float)(r*Math.cos(a))+drawx+2, (float)(-r*Math.sin(a))+drawx*1.45f+22, paint);
	    	     }
	    	     paint.setColor(0xFF669900);
	    	     canvas.drawPath(path, paint);
	    	     paint.setColor(Color.WHITE);
	    	     paint.setTextSize(16);
	    	     canvas.drawText("handlemind@gmail.com", 1f, 0.99f*myBitmap.getWidth(), paint);
	    		 }// Draw Ganja
	    		 else {
	    			 // Draw flag
	    			 canvas.drawBitmap(bitmapFlag, 10, 10, paint);
	    		 }
	    	 }
	     }
	     
	     //canvas.drawPoint(x, y, paint);
	     first = false;
	     surfaceHolder.unlockCanvasAndPost(canvas);
	    }
	   }
	  }
	  
	  public float EaseOutBounce(float currentTime, float startValue, float changeInValue, float totalTime)
	  {
	      float magic1 = 7.5625f;
	      float magic2 = 2.75f;
	      float magic3 = 1.5f;
	      float magic4 = 2.25f;
	      float magic5 = 2.625f;
	      float magic6 = 0.75f;
	      float magic7 = 0.9375f;
	      float magic8 = 0.984375f;

	      if ((currentTime /= totalTime) < (1 / magic2)) //0.36%
	      {
	          return changeInValue * (magic1 * currentTime * currentTime) + startValue;
	      }
	      else if (currentTime < (2 / magic2)) //0.72%
	      {
	          return changeInValue * (magic1 * (currentTime -= (magic3 / magic2)) * currentTime + magic6) + startValue;
	      }
	      else if (currentTime < (2.5 / magic2)) //0.91%
	      {
	          return changeInValue * (magic1 * (currentTime -= (magic4 / magic2)) * currentTime + magic7) + startValue;
	      }
	      else
	      {
	          return changeInValue * (magic1 * (currentTime -= (magic5 / magic2)) * currentTime + magic8) + startValue;
	      }
	  }
	 
		@Override
		public boolean onTouchEvent(MotionEvent event) {
			// TODO Auto-generated method stub

			touched_x = event.getX();
			touched_y = event.getY();

			int action = event.getAction();
			switch (action) {
			case MotionEvent.ACTION_DOWN:
				touched = true;
				if (!anim && (touched_x+35) > x1 && (touched_x-35) < x2 && (touched_y+35) > y1
						&& (touched_y-35) < y2) {
					// fix X&Y
					x_prev = touched_x;
					y_prev = touched_y;
					act = true;
					anim = false;
					anim_a = 0;
					anim_b = 1;
					progress = 0;
				}
				break;
			case MotionEvent.ACTION_MOVE:
				if (act) {
					// get length
					length = (float) Math.sqrt(Math
							.pow((touched_x - x_prev), 2)
							+ Math.pow((touched_y - y_prev), 2));
					if (length <= 200)
						p = Math.abs(length / 2);
					else
						p = 100;
					if (touched_x > x_prev)
						angle = (float) Math.toDegrees(Math
								.asin((touched_y - y_prev) / length));
					else
						angle = 90 + (90 - (float) Math.toDegrees(Math
								.asin((touched_y - y_prev) / length)));
				}
				touched = true;
				break;
			case MotionEvent.ACTION_UP:
				touched = false;
				act = false;
				if (p>25 && !cheat_wait && !anim)
				{
					touchCount++;
					Direction direct;
					if (angle>(-45)&&angle<(45)) { direct = Direction.RIGHT;}//to right
					else if (angle>(45)&&angle<(135)) { direct = Direction.DOWN;}//to down
					else if (angle>(135)&&angle<(225)) { direct = Direction.LEFT;}//to left
					else { direct = Direction.UP;}//to up
					switch (touchCount) {
					case 1:
						if (direct == Direction.DOWN) c1 = true;
						break;
					case 2:
						if (direct == Direction.LEFT) {
							c2 = true;
							showPic = 1; // flag
						} else if (direct == Direction.UP) {
							c2 = true;
							showPic = 2; // ganja
						}
						break;
					case 3:
						if (direct == Direction.LEFT) c3 = true;
						break;
					case 4:
						if (direct == Direction.RIGHT) c4 = true;
						break;
					}
					if (touchCount == 5){
						touchCount = 0;
						showPic = 1;
						c1 = c2 = c3 = c4 = false;
					}
				}
				anim = true;
				break;
			case MotionEvent.ACTION_CANCEL:
				touched = false;
				act = false;
				break;
			case MotionEvent.ACTION_OUTSIDE:
				touched = false;
				act = false;
				break;
			default:
			}
			return true; // processed
		}

	}
}
