package form24q;

import java.io.File;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Timer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import form24q.util.Form24QConstants;
import form24q.util.Form24QUtil;
import thread.tools.ThreadInterrupter;
import thread.tools.TimeOutTask;

/**
 * In order to update FVU version, change the dependency in Form24Q Tools.iml file and remake the project.
 */
import com.tin.FVU.FVU;

public class FVUGenerator
{
	private static final String DIR = "DIR";
	private static final String FORM24Q_FILE_NAME = "FORM24Q_FILE_NAME" + ".txt";
	private static final String CSI_FILE_NAME = "CSI_FILE_NAME" + ".csi";
	private static final String OUTPUT_DIRECTORY_NAME = "AutoOutput/";

	/**
	 * In order to update FVU version, change the dependency in Form24Q Tools.iml file and remake the project.
	 */
	public static void main(String[] args) throws Exception
	{
		String baseDir = DIR;
		if (!baseDir.endsWith(Form24QConstants.FILE_SEPARATOR))
		{
			baseDir += Form24QConstants.FILE_SEPARATOR;
		}

		String form24QTextFilePath = baseDir + FORM24Q_FILE_NAME;
		String challanFilePath = baseDir + CSI_FILE_NAME;
		String outputPath = baseDir + OUTPUT_DIRECTORY_NAME;

		String form24QTextFileName = Form24QUtil.getTextFileName(form24QTextFilePath);
		File outputDir = new File(outputPath);
		if (!outputDir.exists())
		{
			outputDir.mkdir();
		}

		Form24QUtil.checkIfFileOrFolderExists(baseDir);
		Form24QUtil.checkIfFileOrFolderExists(form24QTextFilePath);
		Form24QUtil.checkIfFileOrFolderExists(challanFilePath);
		Form24QUtil.checkIfFileOrFolderExists(outputPath);

		String[] params = new String[7];
		params[0] = form24QTextFilePath;
		params[1] = outputPath; //errorFileNameSAM
		params[2] = outputPath + form24QTextFileName + Form24QConstants.FVU_EXTENSION; //outFileNameSAM, errFilepath
		//params[2] = outputPath + "FvuFile" + FVU_EXTENSION; //outFileNameSAM, errFilepath
		params[3] = "0";
		params[4] = Form24QConstants.FVU_VERSION;
		params[5] = "1";
		params[6] = challanFilePath;
		System.out.println("params: " + Arrays.asList(params));
		System.out.println("paramsSize: " + Arrays.asList(params).size());

		//testFVUGenWithGUI(params);
		//testFVUGenWithFuture(params);
		//testFVUGenWithTimerTask(params);

		//testFVUGenWithFVUInterruptTask(params);

		//FVU fvu = new FVU();
		//fvu.i(params[0], params[1], params[2], params[4], 1, params[6]);
		//fvu.j(null, null, null, null, 1, null);

		/**
		 * In order to update FVU version, change the dependency in Form24Q Tools.iml file and remake the project.
		 */
		FVU.main(params);

		System.out.println("Process Completed!");
	}

	private static void testFVUGenWithFVUInterruptTask(String[] params) throws ExecutionException, InterruptedException
	{
		System.out.println("testFVUGenWithFVUInterruptTask:::START::: " + form24q.FVUGenerator.getCurrFormattedTime());
		PrintStream stdout = System.out;
		PrintStream stderr = System.err;
		Thread thread = new Thread(() -> {
			try
			{
				FVU.main(params);
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		});
		thread.start();
		Thread.sleep(1000);
		ThreadInterrupter threadInterrupter = new ThreadInterrupter(thread);
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		executorService.awaitTermination(5, TimeUnit.SECONDS);
		Future future = executorService.submit(() -> threadInterrupter.initiateInterruption());
		boolean processCompleted = (boolean) future.get();
		System.setOut(stdout);
		System.setErr(stderr);
		System.out.println("processCompleted = " + processCompleted);
		System.out.println("Completed Interruption..");
		executorService.shutdown();
		System.out.println("testFVUGenWithFVUInterruptTask:::END::: " + form24q.FVUGenerator.getCurrFormattedTime());
	}

	private static void testFVUGenWithTimerTask(String[] params) throws InterruptedException, TimeoutException, ExecutionException
	{
		Thread thread = new Thread(() -> {
			try
			{
				FVU.main(params);
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		});
		thread.start();

		Timer timer = new Timer();
		TimeOutTask timeOutTask = new TimeOutTask(thread, timer);
		timer.schedule(timeOutTask, 10000);
	}

	private static void testFVUGenWithFuture(String[] params) throws InterruptedException, ExecutionException, TimeoutException
	{
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.awaitTermination(5, TimeUnit.SECONDS);
		Future<String> future = executor.submit(new FVUGeneratorTask(params));
		PrintStream stdout = System.out;
		PrintStream stderr = System.err;
		try
		{
			System.out.println("Started..");
			Object res = future.get(1, TimeUnit.SECONDS);
			System.setOut(stdout);
			System.setErr(stderr);
			System.out.println("res = " + res);
			System.out.println("Finished!");
		}
		catch (TimeoutException e)
		{
			System.setOut(stdout);
			System.setErr(stderr);
			System.out.println("TimeoutException " + future.isCancelled());
			System.out.println("Cancelling FutureTask");
			future.cancel(true);
		}
		catch (Exception e)
		{
			System.setOut(stdout);
			System.setErr(stderr);
			System.out.println("Terminated!");
		}
		finally
		{
			System.out.println("Shutting Down Executor");
			executor.shutdownNow();
		}
	}

	private static void testFVUGenWithGUI(String[] params)
	{
		FVU.main(params);
	}

	public static String getCurrFormattedTime()
	{
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM.dd.yyyy HH:mm:ss:SS");
		return dateFormat.format(cal.getTimeInMillis());
	}

	private static class FVUGeneratorTask implements Callable
	{
		private String[] fvuParams;

		public FVUGeneratorTask(String[] params)
		{
			this.fvuParams = params;
		}

		@Override public Object call() throws Exception
		{
			try
			{
				FVU.main(fvuParams);
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
			return null;
		}
	}
}