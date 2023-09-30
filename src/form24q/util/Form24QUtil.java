package form24q.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.common.base.CharMatcher;

public class Form24QUtil
{
	public static Map<Form24QRecordTypes, String[]> loadInputMapping() throws IOException
	{
		Map<Form24QRecordTypes, String[]> mapping = new HashMap();

		XSSFWorkbook workbook = new XSSFWorkbook(Form24QConstants.FORM24Q_COLUMN_HEADER_MAPPING_INPUT_PATH);
		System.out.println("Going to populate Form 24Q Column Headers...");
		for (Form24QRecordTypes form24QRecordType : Form24QRecordTypes.values())
		{
			XSSFSheet xssfSheet = workbook.getSheet(form24QRecordType.code);
			Iterator<Row> rowsItr = xssfSheet.rowIterator();
			List<String> columnHeadersList = new ArrayList<>();
			while (rowsItr.hasNext())
			{
				Row row = rowsItr.next();
				if (row.getCell(1).getStringCellValue().isEmpty())
				{
					break;
				}
				columnHeadersList.add(row.getCell(1).getStringCellValue());
			}
			String[] columnHeaders = columnHeadersList.toArray(new String[0]);
			mapping.put(form24QRecordType, columnHeaders);
		}
		return mapping;
	}

	static
	{
		System.out.println("Initialising Form24QRecordTypes...");
		for (Form24QRecordTypes form24QRecordType : Form24QRecordTypes.values())
		{
			System.out.println(form24QRecordType.toString());
		}
	}

	public static String processOutputDirectory(String form24QInputFilePath, String outputFileDirectory)
	{
		if (outputFileDirectory == null)
		{
			File inputFile = new File(form24QInputFilePath);
			outputFileDirectory = inputFile.getParent();
		}
		if (outputFileDirectory.charAt(outputFileDirectory.length() - 1) != '/')
		{
			outputFileDirectory += "/";
		}
		return outputFileDirectory;
	}

	public static String extractNumbers(String value)
	{
		//String numberOnly = value.replaceAll("[^0-9]", "");
		String numberOnly = CharMatcher.inRange('0', '9').retainFrom(value);
		return numberOnly;
	}

	public static String[] getRecordContents(String form24QRecord)
	{
		form24QRecord += Form24QConstants.CAP_STR + Form24QConstants.DUMMY_VAL_STRING;
		String[] recordLineContents = form24QRecord.split(Form24QConstants.CAP_REGEX);
		List<String> recordLineContentsList = new ArrayList<>(Arrays.asList(recordLineContents));
		recordLineContentsList.remove(recordLineContentsList.size() - 1);
		recordLineContents = recordLineContentsList.toArray(new String[0]);
		return recordLineContents;
	}

	public static void writeIntoXLSSheetForForm24QRecordType(Form24QRecordTypesXLSMeta form24QXlsObj, boolean isColumnHeader, String recordType, String[] contents) throws Exception
	{
		Form24QRecordTypes form24QRecordType = Form24QRecordTypes.getForm24QRecordTypesFromCode(recordType);
		System.out.println("form24QRecordType: " + form24QRecordType);
		Form24QXLSMeta form24QXLSMeta;
		switch (form24QRecordType)
		{
			case FILE_HEADER:
				form24QXLSMeta = form24QXlsObj.FILE_HEADER;
				break;
			case BATCH_HEADER:
				form24QXLSMeta = form24QXlsObj.BATCH_HEADER;
				break;
			case CHALLAN_DETAIL:
				form24QXLSMeta = form24QXlsObj.CHALLAN_DETAIL;
				break;
			case DEDUCTEE_DETAIL:
				form24QXLSMeta = form24QXlsObj.DEDUCTEE_DETAIL;
				break;
			case SALARY_DETAIL:
				form24QXLSMeta = form24QXlsObj.SALARY_DETAIL;
				break;
			case SECTION_16:
				form24QXLSMeta = form24QXlsObj.SECTION_16;
				break;
			case CHAPTER_6A:
				form24QXLSMeta = form24QXlsObj.CHAPTER_6A;
				break;
			default:
				throw new Exception("Undefined Record Type!");
		}
		int rowNumber = form24QXLSMeta.getRowNumber();
		if (isColumnHeader)
		{
			rowNumber = 0;
			form24QXLSMeta.setHeaderColumnCount(contents.length);
		}
		else
		{
			if (contents.length != form24QXLSMeta.getHeaderColumnCount())
			{
				System.out.println("contents: " + Arrays.asList(contents));
				System.out.println("Mismatch in column count for current row: " + contents.length + " and header row: " + form24QXLSMeta.getHeaderColumnCount());
				throw new Exception("Form24QRecordType: " + form24QRecordType + " Mismatch in column count for current row and header row.");
			}
		}
		writeIntoXLSSheet(contents, form24QXLSMeta.getXssfSheet(), rowNumber);
		if (!isColumnHeader)
		{
			form24QXLSMeta.incrementRowCount();
			form24QXLSMeta.incrementRowNumber();
		}
	}

	static void writeIntoXLSSheet(String[] rowContents, XSSFSheet spreadsheet, int rowCount)
	{
		int cellID = 0;
		//Create row object
		XSSFRow row = spreadsheet.createRow(rowCount);

		List<String> recordLineContentsList = Arrays.asList(rowContents);
		for (String recordLineContent : recordLineContentsList)
		{
			//Create cell object
			Cell cell = row.createCell(cellID++);
			cell.setCellValue(recordLineContent);
		}
	}

	public static Map<Form24QRecordTypes, Integer> getForm24QRecordTypeVsHeaderCount()
	{
		Map<Form24QRecordTypes, Integer> form24QRecordTypeVsHeaderCount = new HashMap();
		for (Form24QRecordTypes form24QRecordType : Form24QRecordTypes.values())
		{
			form24QRecordTypeVsHeaderCount.put(form24QRecordType, Form24QConstants.FOR24Q_INPUT_MAPPING.get(form24QRecordType).length - 1);
		}
		return form24QRecordTypeVsHeaderCount;
	}

	public static int getIndexOfForm24QTypeField(Form24QRecordTypes form24QRecordTypeToBeReplaced, String field) throws Exception
	{
		field = field.trim();
		int fieldIndex = -1;
		System.out.println("Going to find fieldIndex in FOR24Q_INPUT_MAPPING for field: " + field);
		for (Form24QRecordTypes form24QRecordType : Form24QRecordTypes.values())
		{
			if (form24QRecordType == form24QRecordTypeToBeReplaced)
			{
				String[] columnHeaders = Form24QConstants.FOR24Q_INPUT_MAPPING.get(form24QRecordType);
				System.out.println("Found form24QRecordTypeToBeReplaced!");
				for (int ind = 1; ind < columnHeaders.length; ind++)
				{
					if (columnHeaders[ind].trim().equals(field))
					{
						fieldIndex = ind - 1;
						System.out.println("Found fieldIndex: " + fieldIndex + " in FOR24Q_INPUT_MAPPING!");
						break;
					}
				}
				if (fieldIndex != -1)
				{
					break;
				}
			}
		}
		if (fieldIndex == -1)
		{
			throw new Exception("getIndexOfForm24QTypeField failed for form24QRecordTypeToBeReplaced: " + form24QRecordTypeToBeReplaced + " field: " + field + " !!!!");
		}
		return fieldIndex;
	}

	public static String getTextFileName(String textFilePath) throws Exception
	{
		File inputFile = new File(textFilePath);
		String inputFileNameWithExtension = inputFile.getName();
		if (!inputFileNameWithExtension.contains(Form24QConstants.TXT_EXTENSION))
		{
			throw new Exception("Invalid Text File!");
		}
		String inputFileName = inputFileNameWithExtension.replace(".txt", "");
		System.out.println("textFilePath: " + textFilePath);
		System.out.println("inputFileName: " + inputFileName);
		return inputFileName;
	}

	public static void checkIfFileOrFolderExists(String path) throws Exception
	{
		File currDir = new File(path);
		if (!currDir.exists())
		{
			throw new Exception(path + " does not exist!");
		}
	}
}
