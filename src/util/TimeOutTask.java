package util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class TimeOutTask extends TimerTask
{
	private Thread thread;
	private Timer timer;
	private static final int MAX_LIMIT_THREAD_INTERRUPT = 100;
	private static final int INTERRUPT_PERIOD = 1000;

	public TimeOutTask(Thread thread, Timer timer)
	{
		this.thread = thread;
		this.timer = timer;
	}

	public TimeOutTask(Thread thread)
	{
		this.thread = thread;
	}

	@Override
	public void run()
	{
		int threadInterruptAttempt = 0;
		while (isThreadStillActive() && threadInterruptAttempt++ < MAX_LIMIT_THREAD_INTERRUPT)
		{
			System.out.println("Interrupting thread: " + thread.getId() + " threadInterruptAttempt: " + threadInterruptAttempt + " at " + getCurrFormattedTime());
			thread.interrupt();
			try
			{
				thread.sleep(INTERRUPT_PERIOD);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		if (isThreadStillActive())
		{
			System.out.println("WARNING!!! Going to stop thread: " + thread.getId() + " at " + getCurrFormattedTime());
			try
			{
				thread.stop();
			}
			catch (Exception exception)
			{
				exception.printStackTrace();
			}
		}
		if (timer != null)
		{
			timer.cancel();
		}
	}

	private String getCurrFormattedTime()
	{
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM.dd.yyyy HH:mm:ss");
		return dateFormat.format(cal.getTimeInMillis());
	}

	private boolean isThreadStillActive()
	{
		return thread != null && thread.isAlive();
	}
}