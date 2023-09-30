package form24q;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import form24q.util.Form24QConstants;
import form24q.util.Form24QRecordTypesXLSMeta;
import form24q.util.Form24QUtil;

public class Form24QTextFileConverter
{
	private static final String OUTPUT_FILE_PREFIX = "";
	private static final String FORM24Q_TEXT_FILE_PATH = "FORM24Q_TEXT_FILE_PATH";

	public static void main(String[] args) throws Exception
	{
		String outputFileDirectory = null;
		outputFileDirectory = Form24QUtil.processOutputDirectory(FORM24Q_TEXT_FILE_PATH, outputFileDirectory);

		convertForm24QTextFileIntoXLS(FORM24Q_TEXT_FILE_PATH, outputFileDirectory);
	}

	private static void convertForm24QTextFileIntoXLS(String form24QInputFilePath, String outputFileDirectory) throws Exception
	{
		//Create blank workbook
		XSSFWorkbook workbook = new XSSFWorkbook();

		Form24QRecordTypesXLSMeta FORM24Q_XLS_OBJ = new Form24QRecordTypesXLSMeta(Form24QConstants.STARTING_ROW_NUMBER);
		FORM24Q_XLS_OBJ.initializeForm24QSheets(workbook);
		FORM24Q_XLS_OBJ.populateForm24QColumnHeaders();

		FileReader form24QTextFile = new FileReader(form24QInputFilePath);
		BufferedReader form24QTextFileReader = new BufferedReader(form24QTextFile);

		int form24QNumberOfLines = 0;
		while (form24QTextFileReader.ready())
		{
			String recordLine = form24QTextFileReader.readLine();
			form24QNumberOfLines++;
			System.out.println("recordLine: " + recordLine);
			String[] recordLineContents = Form24QUtil.getRecordContents(recordLine);
			String recordType = recordLineContents[Form24QConstants.Form24QIndexConstants.RECORD_TYPE];
			Form24QUtil.writeIntoXLSSheetForForm24QRecordType(FORM24Q_XLS_OBJ, false, recordType, recordLineContents);
		}

		autoSizeAllColumnsInAllSheets(workbook);

		File inputFile = new File(form24QInputFilePath);
		String inputFileName = inputFile.getName();
		String outputFileName = OUTPUT_FILE_PREFIX + inputFileName;
		outputFileName = outputFileName.replace(Form24QConstants.TXT_EXTENSION, Form24QConstants.XLS_EXTENSION);
		String form24QOutputFilePath = outputFileDirectory + outputFileName;

		System.out.println();
		System.out.println("Form24TotalRowCount: " + form24QNumberOfLines);
		System.out.println("FileHeaderRowCount: " + FORM24Q_XLS_OBJ.FILE_HEADER.getRecordCount());
		System.out.println("BatchHeaderRowCount: " + FORM24Q_XLS_OBJ.BATCH_HEADER.getRecordCount());
		System.out.println("ChallanDetailRowCount: " + FORM24Q_XLS_OBJ.CHALLAN_DETAIL.getRecordCount());
		System.out.println("DeducteeDetailRowCount: " + FORM24Q_XLS_OBJ.DEDUCTEE_DETAIL.getRecordCount());
		System.out.println("SalaryDetailRowCount: " + FORM24Q_XLS_OBJ.SALARY_DETAIL.getRecordCount());
		System.out.println("Section16RowCount: " + FORM24Q_XLS_OBJ.SECTION_16.getRecordCount());
		System.out.println("Chapter6ARowCount: " + FORM24Q_XLS_OBJ.CHAPTER_6A.getRecordCount());
		assert form24QNumberOfLines == (FORM24Q_XLS_OBJ.FILE_HEADER.getRecordCount() + FORM24Q_XLS_OBJ.BATCH_HEADER.getRecordCount() + FORM24Q_XLS_OBJ.CHALLAN_DETAIL.getRecordCount() + FORM24Q_XLS_OBJ.DEDUCTEE_DETAIL.getRecordCount() + FORM24Q_XLS_OBJ.SALARY_DETAIL.getRecordCount() + FORM24Q_XLS_OBJ.SECTION_16.getRecordCount() + FORM24Q_XLS_OBJ.CHAPTER_6A.getRecordCount()) : "Row Count Mismatch!!";

		//Write the workbook in file system
		FileOutputStream out = new FileOutputStream(form24QOutputFilePath);
		workbook.write(out);
		out.close();
		System.out.println();
		System.out.println("Successfully converted Form24Q Text File into XLS.");
		System.out.println("InputFilePath: " + form24QInputFilePath);
		System.out.println("OutputFilePath: " + form24QOutputFilePath);
	}

	private static void autoSizeAllColumnsInAllSheets(XSSFWorkbook workbook)
	{
		System.out.println();
		System.out.println("Going to Autosize...");
		Iterator<Sheet> sheetItr = workbook.sheetIterator();
		if (sheetItr.hasNext())
		{
			while (sheetItr.hasNext())
			{
				Sheet sheet = sheetItr.next();
				Iterator rowIterator = sheet.rowIterator();
				if (rowIterator.hasNext())
				{
					Row headerRow = (Row) rowIterator.next();
					int numberOfColumns = headerRow.getPhysicalNumberOfCells();
					Iterator<Cell> cellIterator = headerRow.cellIterator();
					int cellNumber = 0;
					if (cellIterator.hasNext())
					{
						while (cellIterator.hasNext())
						{
							sheet.autoSizeColumn(cellNumber++);
							cellIterator.next();
						}
						System.out.println("Sheet: " + sheet.getSheetName() + ", ColumnCountFromMethod: " + numberOfColumns + ", ColumnCountFromIteration: " + cellNumber);
					}
					else
					{
						System.out.println("No sheets cells for autosize for Sheet: " + sheet.getSheetName() + " !!!");
					}
					//for (int i = 0; i < numberOfColumns; i++)
					//{
					//	sheet.autoSizeColumn(i);
					//}
				}
				else
				{
					System.out.println("No rows found for autosize for Sheet: " + sheet.getSheetName() + " !!!");
				}
			}
		}
		else
		{
			System.out.println("No sheets found for autosize!!!");
		}
		System.out.println("Completed Autosize!!!");
		System.out.println();
	}

	private static void convertForm24QTextFileIntoCSV(String form24QInputFilePath, String outputFileDirectory) throws IOException
	{
		FileReader form24QTextFile = new FileReader(form24QInputFilePath);
		BufferedReader form24QTextFileReader = new BufferedReader(form24QTextFile);
		File inputFile = new File(form24QInputFilePath);
		String inputFileName = inputFile.getName();
		String outputFileName = inputFileName;
		outputFileName = outputFileName.replace(Form24QConstants.TXT_EXTENSION, Form24QConstants.CSV_EXTENSION);
		String form24QOutputFilePath = outputFileDirectory + outputFileName;

		FileWriter csvWriter = new FileWriter(form24QOutputFilePath);
		while (form24QTextFileReader.ready())
		{
			String recordLine = form24QTextFileReader.readLine();
			String parsedLine = getParsedLine(recordLine);
			csvWriter.append(parsedLine);
			csvWriter.append("\n");
		}
		csvWriter.flush();
		csvWriter.close();
		System.out.println("Successfully converted Form24Q Text File into CSV.");
		System.out.println("InputFilePath: " + form24QInputFilePath);
		System.out.println("OutputFilePath: " + form24QOutputFilePath);
	}

	private static String getParsedLine(String recordLine)
	{
		String recordLineClone = recordLine;
		recordLineClone = recordLineClone.replaceAll(Form24QConstants.CAP_REGEX, Form24QConstants.QUOTE_REGEX + Form24QConstants.CAP_REGEX + Form24QConstants.QUOTE_REGEX);
		recordLineClone = recordLineClone.replaceAll(Form24QConstants.CAP_REGEX, ",");
		System.out.println("recordLine: " + recordLine);
		recordLineClone = Form24QConstants.QUOTE_REGEX + recordLineClone + Form24QConstants.QUOTE_REGEX;
		System.out.println("recordLineClone: " + recordLineClone);
		return recordLineClone;
	}

}
