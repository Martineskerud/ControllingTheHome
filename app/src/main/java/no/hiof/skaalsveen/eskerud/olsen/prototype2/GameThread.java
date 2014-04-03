package no.hiof.skaalsveen.eskerud.olsen.prototype2;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

/**
 * Class inspired by http://www.mathcs.org/java/android/game_surfaceview.html
 * 
 * */
public class GameThread extends Thread
{
	private final static int SLEEP_TIME = 10;

	private boolean running = false;
	private CustomDrawableView canvas = null;
	private SurfaceHolder surfaceHolder = null;

	public GameThread(CustomDrawableView canvas)
	{
		super();
		this.canvas = canvas;
		this.surfaceHolder = canvas.getHolder();
	}

	public void startThread()
	{
		running = true;
		super.start();
	}

	public void stopThread()
	{
		running = false;
	}

	public void run()
	{
		long tpf = 0, lastTs = System.currentTimeMillis();
		
		Canvas c = null;
		while (running)
		{
			tpf = System.currentTimeMillis() - lastTs;
			lastTs = System.currentTimeMillis();
			canvas.update(tpf);
			c = null;
			try
			{
				c = surfaceHolder.lockCanvas();
				synchronized (surfaceHolder)
				{
					if (c != null)
					{
						canvas.draw(c);
					}
				}
				sleep(SLEEP_TIME);
			}
			catch(InterruptedException ie)
			{ 
			}
			finally 
			{
				// do this in a finally so that if an exception is thrown
				// we don't leave the Surface in an inconsistent state
				if (c != null) 
				{
					surfaceHolder.unlockCanvasAndPost(c);
				}
			}
		}
	}
}
