package form24q;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import form24q.util.Form24QConstants;
import form24q.util.Form24QRecordTypes;
import form24q.util.Form24QUtil;

public class Form24QCorrectLineNumber
{
	private static final String OUTPUT_FILE_PREFIX = "CorrectedLineNumber_";
	private static final String FORM24Q_TEXT_FILE_PATH = "FORM24Q_TEXT_FILE_PATH";

	public static void main(String[] args) throws Exception
	{
		String outputFileDirectory = null;
		outputFileDirectory = Form24QUtil.processOutputDirectory(FORM24Q_TEXT_FILE_PATH, outputFileDirectory);
		new Form24QCorrectLineNumberCorrecter().correctLineNumbers(FORM24Q_TEXT_FILE_PATH, outputFileDirectory);
	}

	static class Form24QCorrectLineNumberCorrecter
	{
		int form24QNumberOfLines = 0;
		int form24QNumberOfRecordTypeLines = 0;
		int lineNumberReplacesDone = 0;
		int sdRecNoReplacesDone = 0;
		int sdRelatedRecNoReplacesDone = 0;

		int expectedLineNumber = 1;
		int expectedSalaryDetailRecordNumber = 1;

		void correctLineNumbers(String form24QInputFilePath, String outputFileDirectory) throws Exception
		{
			FileReader form24QInputTextFile = new FileReader(form24QInputFilePath);
			BufferedReader form24QTextFileReader = new BufferedReader(form24QInputTextFile);
			File inputFile = new File(form24QInputFilePath);
			String inputFileName = inputFile.getName();
			String outputFileName = OUTPUT_FILE_PREFIX + inputFileName;
			String form24QOutputFilePath = outputFileDirectory + outputFileName;

			FileWriter form24QOutputTextFileWriterTemp = new FileWriter(form24QOutputFilePath);
			//form24QOutputTextFileWriterTemp.write("");
			form24QOutputTextFileWriterTemp.close();

			while (form24QTextFileReader.ready())
			{
				FileWriter form24QOutputTextFileWriter = new FileWriter(form24QOutputFilePath, true);
				String recordLine = form24QTextFileReader.readLine();
				form24QNumberOfLines++;
				String[] recordLineContents = Form24QUtil.getRecordContents(recordLine);

				int lineNumber = Integer.valueOf(recordLineContents[Form24QConstants.Form24QIndexConstants.LINE_NUMBER]);

				boolean isChanged = Boolean.FALSE;

				if (expectedLineNumber != lineNumber)
				{
					System.out.println("Line Number mismatch found for lineNumber: " + lineNumber + " expectedLineNumber: " + expectedLineNumber);
					recordLineContents[Form24QConstants.Form24QIndexConstants.LINE_NUMBER] = String.valueOf(expectedLineNumber);
					System.out.println("Replacing ::: lineNumber: " + lineNumber);
					lineNumberReplacesDone++;
					isChanged = Boolean.TRUE;
				}
				handleSalaryDetailRecordNumberChange(recordLineContents);

				if (isChanged)
				{
					String replacedRecordLine = String.join(Form24QConstants.CAP_STR, recordLineContents);
					form24QOutputTextFileWriter.write(replacedRecordLine);
				}
				else
				{
					form24QOutputTextFileWriter.write(recordLine);
				}

				form24QOutputTextFileWriter.write("\r\n");
				form24QOutputTextFileWriter.close();
				expectedLineNumber++;
			}

			System.out.println();
			System.out.println("Form24TotalRowCount: " + form24QNumberOfLines);
			System.out.println("Form24TotalRecordTypeRowCount: " + form24QNumberOfRecordTypeLines);
			System.out.println("lineNumberReplacesDone: " + lineNumberReplacesDone);
			System.out.println("sdRecNoReplacesDone: " + sdRecNoReplacesDone);
			System.out.println("sdRelatedRecNoReplacesDone: " + sdRelatedRecNoReplacesDone);

			System.out.println();
			System.out.println("Successfully converted Form24Q Text File into XLS.");

			System.out.println("InputFilePath: " + form24QInputFilePath);
			System.out.println("OutputFilePath: " + form24QOutputFilePath);
		}

		private boolean handleSalaryDetailRecordNumberChange(String[] recordLineContents) throws Exception
		{
			String recordType = recordLineContents[Form24QConstants.Form24QIndexConstants.RECORD_TYPE];
			int lineNumber = Integer.valueOf(recordLineContents[Form24QConstants.Form24QIndexConstants.LINE_NUMBER]);

			boolean isChanged = Boolean.FALSE;
			if (isRecordRelatedToSalaryDetail(recordType))
			{
				Integer salaryDetailRecordNumberIndex = getSalaryDetailRecordNumberColIndex(recordType);
				if (salaryDetailRecordNumberIndex == null)
				{
					throw new Exception("salaryDetailRecordNumberIndex is null");
				}
				int salaryDetailRecordNumber = Integer.valueOf(recordLineContents[salaryDetailRecordNumberIndex]);
				if (recordType.equals(Form24QRecordTypes.SALARY_DETAIL.getCode()))
				{
					if (expectedSalaryDetailRecordNumber != salaryDetailRecordNumber)
					{
						System.out.println("Salary Detail Record Number mismatch found for lineNumber: " + lineNumber + " salaryDetailRecordNumber: " + salaryDetailRecordNumber + " expectedSalaryDetailRecordNumber: " + expectedSalaryDetailRecordNumber);
						recordLineContents[salaryDetailRecordNumberIndex] = String.valueOf(expectedSalaryDetailRecordNumber);
						sdRecNoReplacesDone++;
						isChanged = Boolean.TRUE;
					}
				}
				else
				{
					int expectedSalaryDetailRelatedRecordNumber = expectedSalaryDetailRecordNumber - 1;
					if (expectedSalaryDetailRelatedRecordNumber != salaryDetailRecordNumber)
					{
						System.out.println("Salary Detail Related Record Number mismatch found for lineNumber: " + lineNumber + " salaryDetailRecordNumber: " + salaryDetailRecordNumber + " expectedSalaryDetailRelatedRecordNumber: " + expectedSalaryDetailRelatedRecordNumber);
						recordLineContents[salaryDetailRecordNumberIndex] = String.valueOf(expectedSalaryDetailRelatedRecordNumber);
						sdRelatedRecNoReplacesDone++;
						isChanged = Boolean.TRUE;
					}
				}

				if (recordType.equals(Form24QRecordTypes.SALARY_DETAIL.getCode()))
				{
					expectedSalaryDetailRecordNumber++;
				}
			}
			return isChanged;
		}
	}

	private static Integer getSalaryDetailRecordNumberColIndex(String recordType) throws Exception
	{
		Integer index = null;
		if (isRecordRelatedToSalaryDetail(recordType))
		{
			if (Form24QRecordTypes.SALARY_DETAIL.getCode().equals(recordType))
			{
				String salaryDetailRecordNumberColName = "Salary Details  Record No (Serial Number of Employee)";
				index = Form24QUtil.getIndexOfForm24QTypeField(Form24QRecordTypes.SALARY_DETAIL, salaryDetailRecordNumberColName);
			}
			else
			{
				String salaryDetailRecordNumberForRelatedRecordColName = "Salary Detail Record No";
				index = Form24QUtil.getIndexOfForm24QTypeField(Form24QRecordTypes.getForm24QRecordTypesFromCode(recordType), salaryDetailRecordNumberForRelatedRecordColName);
			}
		}

		return index;
	}

	private static boolean isRecordRelatedToSalaryDetail(String recordType) throws Exception
	{
		Form24QRecordTypes form24QRecordTypes = Form24QRecordTypes.getForm24QRecordTypesFromCode(recordType);

		return Form24QRecordTypes.SALARY_DETAIL_RELATED_RECORDS.contains(form24QRecordTypes);
	}
}
