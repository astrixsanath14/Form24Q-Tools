package form24q;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import form24q.util.Form24QConstants;
import form24q.util.Form24QRecordTypes;
import form24q.util.Form24QUtil;

public class Form24QTextFileCorrectReferenceNumber
{
	private static final String OUTPUT_FILE_PREFIX = "CorrectedReferenceNumber_";
	private static final String FORM24Q_TEXT_FILE_PATH = "FORM24Q_TEXT_FILE_PATH";

	public static void main(String[] args) throws Exception
	{
		String outputFileDirectory = null;
		outputFileDirectory = Form24QUtil.processOutputDirectory(FORM24Q_TEXT_FILE_PATH, outputFileDirectory);
		replaceFields(FORM24Q_TEXT_FILE_PATH, outputFileDirectory, Form24QRecordTypes.DEDUCTEE_DETAIL, "Employee Serial No (Employee Reference Number provided by Employer)");
	}

	private static void replaceFields(String form24QInputFilePath, String outputFileDirectory, Form24QRecordTypes form24QRecordTypeToBeReplaced, String field) throws Exception
	{
		FileReader form24QInputTextFile = new FileReader(form24QInputFilePath);
		BufferedReader form24QTextFileReader = new BufferedReader(form24QInputTextFile);
		File inputFile = new File(form24QInputFilePath);
		String inputFileName = inputFile.getName();
		String outputFileName = OUTPUT_FILE_PREFIX + inputFileName;
		String form24QOutputFilePath = outputFileDirectory + outputFileName;

		int form24QNumberOfLines = 0;
		int addedZeroCount = 0;
		int form24QNumberOfRecordTypeLines = 0;
		int replacesDone = 0;
		int form24QTypeFieldIndex = Form24QUtil.getIndexOfForm24QTypeField(form24QRecordTypeToBeReplaced, field);

		Map<String, String> originalValueVsReplacedValues = new HashMap<>();
		Map<String, String> replacedValuesVsOriginalValue = new HashMap<>();
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

				if (NumberUtils.isNumber(currentValue))
				{
					//System.out.println("No change ::: lineNumber: " + lineNumber + " currentValue: " + currentValue);
					nonReplacedValues.add(currentValue);
				}
				else
				{
					String toValue;
					if (originalValueVsReplacedValues.containsKey(currentValue))
					{
						toValue = originalValueVsReplacedValues.get(currentValue);
					}
					else
					{
						toValue = Form24QUtil.extractNumbers(currentValue);
						if (StringUtils.isEmpty(toValue))
						{
							throw new Exception("Invalid toValue ::: " + toValue + " lineNumber: " + lineNumber + " currentValue: " + currentValue);
						}
						boolean addedZero = false;
						while (replacedValuesVsOriginalValue.containsKey(toValue))
						{
							addedZero = true;
							System.out.println("Adding 0 to " + toValue);
							toValue = "0" + toValue;
							if (toValue.length() > 9)
							{
								throw new Exception("Duplicate replaced value is occurring! toValue :::" + toValue + " + lineNumber:" + lineNumber + " currentValue: " + currentValue + " existing for: " + replacedValuesVsOriginalValue.get(toValue));
							}
						}
						addedZeroCount += addedZero ? 1 : 0;
						originalValueVsReplacedValues.put(currentValue, toValue);
						replacedValuesVsOriginalValue.put(toValue, currentValue);

					}
					recordLineContents[form24QTypeFieldIndex] = toValue;
					replacedRecordLine = String.join(Form24QConstants.CAP_STR, recordLineContents);
					System.out.println("Replacing ::: lineNumber: " + lineNumber + " currentValue: " + currentValue + " toValue: " + toValue);
					replacesDone++;
				}
				form24QOutputTextFileWriter.write(replacedRecordLine);
			}
			else
			{
				//System.out.println("lineNumber writing: " + lineNumber);
				//System.out.println("recordLine: " + recordLine);
				form24QOutputTextFileWriter.write(recordLine);
			}

			form24QOutputTextFileWriter.write("\n");
			form24QOutputTextFileWriter.close();
		}

		System.out.println();
		System.out.println("Form24QTotalRowCount: " + form24QNumberOfLines);
		System.out.println("Form24QTotalRecordTypeRowCount: " + form24QNumberOfRecordTypeLines);
		System.out.println("addedZeroCount: " + addedZeroCount);
		System.out.println("replacesDone: " + replacesDone);
	}
}
