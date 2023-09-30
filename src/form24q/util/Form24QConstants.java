package form24q.util;

import java.io.IOException;
import java.util.Map;

public class Form24QConstants
{
	public static final String CAP_REGEX = "\\^";
	public static final String QUOTE_REGEX = "\"";
	public static final String CAP_STR = "^";
	public static final int STARTING_ROW_NUMBER = 2;
	public static final String DUMMY_VAL_STRING = "DUMMY_VAL";
	public static final String TXT_EXTENSION = ".txt";
	public static final String CSV_EXTENSION = ".csv";
	public static final String XLS_EXTENSION = ".xls";
	public static final int INTEGER_TWO = 2;
	public static String FVU_EXTENSION = ".fvu";
	public static final String FROM_HEADER_COL_NAME = "Employee Number";
	public static final String TO_HEADER_COL_NAME = "Unique Reference Number";

	public static String FILE_SEPARATOR = "/";

	public static class Form24QIndexConstants
	{

		public static int LINE_NUMBER = 0;
		public static int RECORD_TYPE = 1;
	}

	public static final String FVU_VERSION = "8.2";

	public static String FORM24Q_COLUMN_HEADER_MAPPING_INPUT_PATH = "./data/formats/" + FVU_VERSION + "/Form24Q_Regular_Column_Header_Mapping_Input.xls";

	public static final Map<Form24QRecordTypes, String[]> FOR24Q_INPUT_MAPPING;

	static
	{
		try
		{
			FOR24Q_INPUT_MAPPING = Form24QUtil.loadInputMapping();
		}
		catch (IOException e)
		{
			System.out.println("Exception occurred while FOR24Q_INPUT_MAPPING loading!");
			throw new RuntimeException(e);
		}
	}
}
