package form24q;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
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

public class AddSection16PT
{
	private static final String OUTPUT_FILE_PREFIX = "Section16PTAdded_";
	public static final String LINE_NUMBER_COL_NAME = "Line Number";
	public static final String AMOUNT_COL_NAME = "Diff";
	private static final String FORM24Q_TEXT_FILE_PATH = "FORM24Q_TEXT_FILE_PATH";
	public static final String SECTION16_ADD_VALUES_FILE_PATH = "SECTION16_ADD_VALUES_FILE_PATH" + ".xlsx";

	public static void main(String[] args) throws Exception
	{
		String outputFileDirectory = null;
		outputFileDirectory = Form24QUtil.processOutputDirectory(FORM24Q_TEXT_FILE_PATH, outputFileDirectory);
		replaceFields(FORM24Q_TEXT_FILE_PATH, SECTION16_ADD_VALUES_FILE_PATH, outputFileDirectory);
	}

	private static void replaceFields(String form24QInputFilePath, String section16AddValuesFile, String outputFileDirectory) throws Exception
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
		Map<String, String> lineNumberVsAmountToBeAdded = getLineNumberVsAmountToBeAdded(section16AddValuesFile);
		System.out.println(lineNumberVsAmountToBeAdded);
		Map<String, Boolean> addedAtLeastOnceMap = new HashMap<>();

		Iterator<String> LineNumberVsAmountToBeAddedItr = lineNumberVsAmountToBeAdded.keySet().iterator();
		while (LineNumberVsAmountToBeAddedItr.hasNext())
		{
			String lineNumber = LineNumberVsAmountToBeAddedItr.next();
			addedAtLeastOnceMap.put(lineNumber, false);
		}

		Set<String> replacedValues = new HashSet<>();
		Set<String> nonReplacedValues = new HashSet<>();
		FileWriter form24QOutputTextFileWriterTemp = new FileWriter(form24QOutputFilePath);
		//form24QOutputTextFileWriterTemp.write("");
		form24QOutputTextFileWriterTemp.close();

		int countSec16DeductionColIndex = Form24QUtil.getIndexOfForm24QTypeField(Form24QRecordTypes.SALARY_DETAIL, "Count of ' Salary Details  - Section 16 Detail ' Records  associated with this Deductee");
		int totalSec16DeductionColIndex = Form24QUtil.getIndexOfForm24QTypeField(Form24QRecordTypes.SALARY_DETAIL, "Gross Total of 'Total Deduction under section 16' under associated 'Salary Details  - Section 16 Detail'");
		int salaryDetailRecordNumberColIndex = Form24QUtil.getIndexOfForm24QTypeField(Form24QRecordTypes.SALARY_DETAIL, "Salary Details  Record No (Serial Number of Employee)");

		while (form24QTextFileReader.ready())
		{
			FileWriter form24QOutputTextFileWriter = new FileWriter(form24QOutputFilePath, true);
			String recordLine = form24QTextFileReader.readLine();
			form24QNumberOfLines++;
			String[] recordLineContents = Form24QUtil.getRecordContents(recordLine);
			String recordType = recordLineContents[Form24QConstants.Form24QIndexConstants.RECORD_TYPE];
			String lineNumber = recordLineContents[Form24QConstants.Form24QIndexConstants.LINE_NUMBER];
			Form24QRecordTypes form24QRecordType = Form24QRecordTypes.getForm24QRecordTypesFromCode(recordType);

			if (form24QRecordType == Form24QRecordTypes.SALARY_DETAIL)
			{
				form24QNumberOfRecordTypeLines++;
				if (!lineNumberVsAmountToBeAdded.containsKey(lineNumber))
				{
					nonReplacedValues.add(lineNumber);
					//System.out.println("lineNumberVsAmountToBeAdded does not contain lineNumber: " + lineNumber);
					writeAndClose(form24QOutputTextFileWriter, recordLine);
				}
				else
				{
					int existingCountSec16Deduction = Integer.valueOf(recordLineContents[countSec16DeductionColIndex]);
					if (existingCountSec16Deduction != 1)
					{
						throw new Exception("Unexpected existingCountSec16Deduction: " + existingCountSec16Deduction + "for lineNumber: " + lineNumber);
					}
					int salaryDetailRecordNumber = Integer.valueOf(recordLineContents[salaryDetailRecordNumberColIndex]);
					String addValue = lineNumberVsAmountToBeAdded.get(lineNumber);
					BigDecimal amount = new BigDecimal(addValue);
					BigDecimal existingTotalSec16Deduction = new BigDecimal(recordLineContents[totalSec16DeductionColIndex]);
					BigDecimal newTotalSec16Deduction = existingTotalSec16Deduction.add(amount);

					recordLineContents[countSec16DeductionColIndex] = String.valueOf(2);
					recordLineContents[totalSec16DeductionColIndex] = newTotalSec16Deduction.setScale(Form24QConstants.INTEGER_TWO).toString();
					replacedValues.add(lineNumber);
					boolean isAddDone = true;
					String replacedRecordLine = String.join(Form24QConstants.CAP_STR, recordLineContents);
					System.out.println("Replacing ::: lineNumber: " + lineNumber + " existingTotalSec16Deduction: " + existingTotalSec16Deduction + " newTotalSec16Deduction: " + newTotalSec16Deduction);
					replacesDone++;
					writeAndClose(form24QOutputTextFileWriter, replacedRecordLine);

					if (isAddDone)
					{
						form24QOutputTextFileWriter = new FileWriter(form24QOutputFilePath, true);
						//writing sec16 standard deduction
						writeAndClose(form24QOutputTextFileWriter, form24QTextFileReader.readLine());

						//Example: 361^S16^1^2^2^16(iii)^1325.00^
						String ptLine = "-1^S16^1^" + salaryDetailRecordNumber + "^2^16(iii)^" + amount.setScale(Form24QConstants.INTEGER_TWO) + "^";
						System.out.println("Going to add ptLine: " + ptLine);
						form24QOutputTextFileWriter = new FileWriter(form24QOutputFilePath, true);
						//writing sec16 pt deduction
						writeAndClose(form24QOutputTextFileWriter, ptLine);
					}
				}
			}
			else
			{
				writeAndClose(form24QOutputTextFileWriter, recordLine);
			}
		}

		System.out.println();
		System.out.println("Form24QTotalRowCount: " + form24QNumberOfLines);
		System.out.println("Form24QTotalRecordTypeRowCount: " + form24QNumberOfRecordTypeLines);
		System.out.println("replacesDone: " + replacesDone);

		new Form24QCorrectLineNumber.Form24QCorrectLineNumberCorrecter().correctLineNumbers(form24QOutputFilePath, outputFileDirectory);
	}

	private static void writeAndClose(FileWriter form24QOutputTextFileWriter, String recordLine) throws IOException
	{
		form24QOutputTextFileWriter.write(recordLine);
		form24QOutputTextFileWriter.write("\r\n");
		form24QOutputTextFileWriter.close();
	}

	private static Map<String, String> getLineNumberVsAmountToBeAdded(String replaceFromToFieldValuesFile) throws Exception
	{
		Map<String, String> lineNumberVsAmountToBeAdded = new HashMap<>();
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
		int lineNumberIndex = headerRowColumnHeadersList.indexOf(LINE_NUMBER_COL_NAME);
		int amountColIndex = headerRowColumnHeadersList.indexOf(AMOUNT_COL_NAME);

		while (rowsItr.hasNext())
		{
			Row row = rowsItr.next();
			Iterator<Cell> cellsItr = row.cellIterator();
			List<String> columnHeadersList = new ArrayList<>();
			while (cellsItr.hasNext())
			{
				Cell cell = cellsItr.next();
				String val = null;
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
					//val = (new BigDecimal("1837")).toString();
					throw new Exception("Unsupported CellType found: " + cellType);
				}
				columnHeadersList.add(val);
			}
			String lineNumber = columnHeadersList.get(lineNumberIndex);
			String amount = columnHeadersList.get(amountColIndex);
			if (lineNumberVsAmountToBeAdded.containsKey(lineNumber))
			{
				String existingAmount = lineNumberVsAmountToBeAdded.get(lineNumber);
				if (existingAmount.equals(amount))
				{
					System.out.println("WARNING ::: lineNumber: " + lineNumber + " repeated");
					continue;
				}
				throw new Exception("lineNumber: " + lineNumber + " repeated. amount: " + amount + " existingAmount: " + existingAmount);
			}
			lineNumberVsAmountToBeAdded.put(lineNumber, amount);

		}
		return lineNumberVsAmountToBeAdded;
	}

}
