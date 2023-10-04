package util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateUtil
{
	public static String getCurrFormattedTime()
	{
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM.dd.yyyy HH:mm:ss:SS");
		return dateFormat.format(cal.getTimeInMillis());
	}
}
