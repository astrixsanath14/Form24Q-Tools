package util;

public class ThreadInterrupter
{
	private Thread thread;
	public static final int INTERRUPT_PERIOD_MILLIS = 100;
	public static final int MAX_LIMIT_COUNT_FOR_THREAD_INTERRUPT = 100;

	public ThreadInterrupter(Thread thread)
	{
		this.thread = thread;
	}

	private boolean isThreadStillActive()
	{
		return thread != null && thread.isAlive();
	}

	public boolean initiateInterruption()
	{
		int threadInterruptAttempt = 0;
		while (isThreadStillActive() && threadInterruptAttempt++ < MAX_LIMIT_COUNT_FOR_THREAD_INTERRUPT)
		{
			System.out.println("Interrupting thread: " + thread.getId() + " threadInterruptAttempt: " + threadInterruptAttempt + " at " + DateUtil.getCurrFormattedTime());
			thread.interrupt();
			try
			{
				thread.sleep(INTERRUPT_PERIOD_MILLIS);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		boolean processCompleted = !isThreadStillActive();
		if (!processCompleted)
		{
			System.out.println("WARNING!!! Going to stop thread: " + thread.getId() + " at " + DateUtil.getCurrFormattedTime());
			try
			{
				thread.stop();
			}
			catch (Exception exception)
			{
				exception.printStackTrace();
			}
		}
		return processCompleted;
	}
}