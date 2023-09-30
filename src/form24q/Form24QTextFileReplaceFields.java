package form24q;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import form24q.util.Form24QConstants;
import form24q.util.Form24QRecordTypes;
import form24q.util.Form24QUtil;

public class Form24QTextFileReplaceFields
{
	private static final String OUTPUT_FILE_PREFIX = "ReplacedFields_";
	private static final String FORM24Q_TEXT_FILE_PATH = "FORM24Q_TEXT_FILE_PATH";
	private static final String REPLACE_FROM_TO_FIELD_VALUES_FILE = "REPLACE_FROM_TO_FIELD_VALUES_FILE" + ".xlsx";

	public static void main(String[] args) throws Exception
	{
		String outputFileDirectory = null;
		outputFileDirectory = Form24QUtil.processOutputDirectory(FORM24Q_TEXT_FILE_PATH, outputFileDirectory);
		replaceFields(FORM24Q_TEXT_FILE_PATH, REPLACE_FROM_TO_FIELD_VALUES_FILE, outputFileDirectory, Form24QRecordTypes.DEDUCTEE_DETAIL, "Employee Serial No (Employee Reference Number provided by Employer)");
	}

	private static void replaceFields(String form24QInputFilePath, String replaceFromToFieldValuesFile, String outputFileDirectory, Form24QRecordTypes form24QRecordTypeToBeReplaced, String field) throws Exception
	{
		FileReader form24QInputTextFile = new FileReader(form24QInputFilePath);
		BufferedReader form24QTextFileReader = new BufferedReader(form24QInputTextFile);
		File inputFile = new File(form24QInputFilePath);
		String inputFileName = inputFile.getName();
		String outputFileName = OUTPUT_FILE_PREFIX + inputFileName;
		String form24QOutputFilePath = outputFileDirectory + outputFileName;

		int form24QNumberOfLines = 0;
		int form24QNumberOfRecordTypeLines = 0;
		int replacesDone = 0;
		int form24QTypeFieldIndex = Form24QUtil.getIndexOfForm24QTypeField(form24QRecordTypeToBeReplaced, field);
		Map<String, String> replaceFieldValuesMap = getFieldValuesToBeReplaced(replaceFromToFieldValuesFile);
		System.out.println(replaceFieldValuesMap);
		Map<String, Boolean> replaceFieldValuesReplacedAtLeastOnceMap = new HashMap<>();

		Iterator<String> replaceValuesItr = replaceFieldValuesMap.keySet().iterator();
		while (replaceValuesItr.hasNext())
		{
			String fromVal = replaceValuesItr.next();
			replaceFieldValuesReplacedAtLeastOnceMap.put(fromVal, false);
		}

		Set<String> replacedValues = new HashSet<>();
		Set<String> nonReplacedValues = new HashSet<>();
		FileWriter form24QOutputTextFileWriterTemp = new FileWriter(form24QOutputFilePath);
		//form24QOutputTextFileWriterTemp.write("");
		form24QOutputTextFileWriterTemp.close();
		while (form24QTextFileReader.ready())
		{
			FileWriter form24QOutputTextFileWriter = new FileWriter(form24QOutputFilePath, true);
			String recordLine = form24QTextFileReader.readLine();
			form24QNumberOfLines++;
			String[] recordLineContents = Form24QUtil.getRecordContents(recordLine);
			String recordType = recordLineContents[Form24QConstants.Form24QIndexConstants.RECORD_TYPE];
			String lineNumber = recordLineContents[Form24QConstants.Form24QIndexConstants.LINE_NUMBER];
			Form24QRecordTypes form24QRecordType = Form24QRecordTypes.getForm24QRecordTypesFromCode(recordType);

			if (form24QRecordType == form24QRecordTypeToBeReplaced)
			{
				form24QNumberOfRecordTypeLines++;

				String currentValue = recordLineContents[form24QTypeFieldIndex];
				String replacedRecordLine = recordLine;
				if (!replaceFieldValuesMap.containsKey(currentValue))
				{
					nonReplacedValues.add(currentValue);
					System.out.println("replaceFieldValuesMap does not contain currentValue: " + currentValue);
				}
				else
				{
					String toValue = replaceFieldValuesMap.get(currentValue);
					if (currentValue.equals(toValue))
					{
						System.out.println("No change ::: lineNumber: " + lineNumber + " currentValue: " + currentValue + " toValue: " + toValue);
					}
					else
					{
						replacedValues.add(currentValue);
						recordLineContents[form24QTypeFieldIndex] = toValue;
						replacedRecordLine = String.join(Form24QConstants.CAP_STR, recordLineContents);
						System.out.println("Replacing ::: lineNumber: " + lineNumber + " currentValue: " + currentValue + " toValue: " + toValue);
						replacesDone++;
					}
				}
				System.out.println("lineNumber: " + lineNumber);
				form24QOutputTextFileWriter.write(replacedRecordLine);
			}
			else
			{
				System.out.println("lineNumber writing: " + lineNumber);
				System.out.println("recordLine: " + recordLine);
				form24QOutputTextFileWriter.write(recordLine);
			}

			form24QOutputTextFileWriter.write("\r\n");
			form24QOutputTextFileWriter.close();
		}

		System.out.println();
		System.out.println("Form24QTotalRowCount: " + form24QNumberOfLines);
		System.out.println("Form24QTotalRecordTypeRowCount: " + form24QNumberOfRecordTypeLines);
		System.out.println("replacesDone: " + replacesDone);
	}

	private static Map<String, String> getFieldValuesToBeReplaced(String replaceFromToFieldValuesFile) throws Exception
	{
		Map<String, String> replaceFieldValuesMap = new HashMap<>();
		XSSFWorkbook workbook = new XSSFWorkbook(replaceFromToFieldValuesFile);
		XSSFSheet xssfSheet = workbook.getSheetAt(0);
		Iterator<Row> rowsItr = xssfSheet.rowIterator();
		Row headerRow = rowsItr.next();
		Iterator<Cell> headerRowCellsItr = headerRow.cellIterator();
		List<String> headerRowColumnHeadersList = new ArrayList<>();
		while (headerRowCellsItr.hasNext())
		{
			Cell headerCell = headerRowCellsItr.next();
			headerRowColumnHeadersList.add(headerCell.getStringCellValue());
		}
		int fromColIndex = headerRowColumnHeadersList.indexOf(Form24QConstants.FROM_HEADER_COL_NAME);
		int toColIndex = headerRowColumnHeadersList.indexOf(Form24QConstants.TO_HEADER_COL_NAME);

		while (rowsItr.hasNext())
		{
			Row row = rowsItr.next();
			Iterator<Cell> cellsItr = row.cellIterator();
			List<String> columnHeadersList = new ArrayList<>();
			while (cellsItr.hasNext())
			{
				Cell cell = cellsItr.next();
				String val;
				CellType cellType = cell.getCellType();
				if (cellType == CellType.NUMERIC)
				{
					val = String.valueOf(Math.round(cell.getNumericCellValue()));
				}
				else if (cellType == CellType.STRING)
				{
					val = cell.getStringCellValue();
				}
				else
				{
					throw new Exception("Unsupported CellType found: " + cellType);
				}
				columnHeadersList.add(val);
			}
			String fromValue = columnHeadersList.get(fromColIndex);
			String toValue = columnHeadersList.get(toColIndex);
			if (replaceFieldValuesMap.containsKey(fromValue))
			{
				String existingToValue = replaceFieldValuesMap.get(fromValue);
				if (existingToValue.equals(toValue))
				{
					System.out.println("WARNING ::: fromValue: " + fromValue + " repeated");
					continue;
				}
				throw new Exception("fromValue: " + fromValue + " repeated. toValue: " + toValue + " existingToValue: " + existingToValue);
			}
			replaceFieldValuesMap.put(fromValue, toValue);

		}
		return replaceFieldValuesMap;
	}

}
