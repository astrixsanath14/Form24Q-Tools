package form24q;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Map;

import form24q.util.Form24QConstants;
import form24q.util.Form24QRecordTypes;
import form24q.util.Form24QUtil;

public class Form24QCorrectColumnCount
{
	private static final String OUTPUT_FILE_PREFIX = "CorrectedColumnCount_";
	private static final String FORM24Q_TEXT_FILE_PATH = "FORM24Q_TEXT_FILE_PATH";

	public static void main(String[] args) throws Exception
	{
		String outputFileDirectory = null;
		outputFileDirectory = Form24QUtil.processOutputDirectory(FORM24Q_TEXT_FILE_PATH, outputFileDirectory);
		correctColumnCount(FORM24Q_TEXT_FILE_PATH, outputFileDirectory);
	}

	private static void correctColumnCount(String form24QInputFilePath, String outputFileDirectory) throws Exception
	{
		Map<Form24QRecordTypes, Integer> form24QRecordTypeVsHeaderCount = Form24QUtil.getForm24QRecordTypeVsHeaderCount();
		FileReader form24QInputTextFile = new FileReader(form24QInputFilePath);
		BufferedReader form24QTextFileReader = new BufferedReader(form24QInputTextFile);
		File inputFile = new File(form24QInputFilePath);
		String inputFileName = inputFile.getName();
		String outputFileName = OUTPUT_FILE_PREFIX + inputFileName;
		String form24QOutputFilePath = outputFileDirectory + outputFileName;

		int form24QNumberOfLines = 0;
		int form24QNumberOfRecordTypeLines = 0;
		int replacesDone = 0;

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
			int expectedColumnCount = form24QRecordTypeVsHeaderCount.get(form24QRecordType);
			String replacedRecordLine = recordLine;
			if (recordLineContents.length != expectedColumnCount)
			{
				System.out.println("Column count mismatch found for lineNumber: " + lineNumber + " recordLineContents.length: " + recordLineContents.length + " expectedColumnCount: " + expectedColumnCount);
				String[] actualRecordLineContents = new String[expectedColumnCount];
				for (int i = 0; i < expectedColumnCount; i++)
				{
					if (i < recordLineContents.length)
					{
						actualRecordLineContents[i] = recordLineContents[i];
					}
					else
					{
						actualRecordLineContents[i] = "";
					}
					replacedRecordLine = String.join(Form24QConstants.CAP_STR, actualRecordLineContents);
				}
				System.out.println("Replacing ::: lineNumber: " + lineNumber);
				replacesDone++;
			}

			form24QOutputTextFileWriter.write(replacedRecordLine);
			form24QOutputTextFileWriter.write("\r\n");
			form24QOutputTextFileWriter.close();
		}

		System.out.println();
		System.out.println("Form24TotalRowCount: " + form24QNumberOfLines);
		System.out.println("Form24TotalRecordTypeRowCount: " + form24QNumberOfRecordTypeLines);
		System.out.println("replacesDone: " + replacesDone);

		System.out.println();
		System.out.println("Successfully converted Form24Q Text File into XLS.");

		System.out.println("InputFilePath: " + form24QInputFilePath);
		System.out.println("OutputFilePath: " + form24QOutputFilePath);
	}
}
