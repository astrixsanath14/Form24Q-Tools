package form24q;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import form24q.util.Form24QConstants;
import form24q.util.Form24QFieldInfo;
import form24q.util.Form24QRecordTypes;
import form24q.util.Form24QUtil;

public class Form24QRefineValues
{
	private static final String OUTPUT_FILE_PREFIX = "RefinedValues_";
	private static final String FORM24Q_TEXT_FILE_PATH = "FORM24Q_TEXT_FILE_PATH";
	private static final Map<Form24QRecordTypes, List<Form24QFieldInfo>> RECORD_TYPE_VS_FIELDS_INFO_MAP;

	static
	{
		try
		{
			RECORD_TYPE_VS_FIELDS_INFO_MAP = new HashMap()
			{{
				putAll(getRecordTypeVsFieldsIndexMap(Form24QRecordTypes.SALARY_DETAIL, Arrays.asList("Name of Employee", "Name of landlord 1", "Name of landlord 2")));
				putAll(getRecordTypeVsFieldsIndexMap(Form24QRecordTypes.DEDUCTEE_DETAIL, Arrays.asList("Name of Employee / Party")));
			}};
			System.out.println("RECORD_TYPE_VS_FIELDS_MAP:: " + RECORD_TYPE_VS_FIELDS_INFO_MAP);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) throws Exception
	{
		String outputFileDirectory = null;
		outputFileDirectory = Form24QUtil.processOutputDirectory(FORM24Q_TEXT_FILE_PATH, outputFileDirectory);
		refineFields(FORM24Q_TEXT_FILE_PATH, outputFileDirectory);
	}

	private static void refineFields(String form24QInputFilePath, String outputFileDirectory) throws Exception
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

			if (RECORD_TYPE_VS_FIELDS_INFO_MAP.containsKey(form24QRecordType))
			{
				form24QNumberOfRecordTypeLines++;
				String replacedRecordLine = recordLine;
				for (Form24QFieldInfo fieldInfo : RECORD_TYPE_VS_FIELDS_INFO_MAP.get(form24QRecordType))
				{
					int form24QTypeFieldIndex = fieldInfo.fieldIndex;
					String currentValue = recordLineContents[form24QTypeFieldIndex];

					String refinedValue = getRefinedValue(currentValue);
					if (refinedValue.equals(currentValue))
					{
						//System.out.println("No change ::: lineNumber: " + lineNumber + " currentValue: " + currentValue);
						nonReplacedValues.add(currentValue);
					}
					else
					{
						recordLineContents[form24QTypeFieldIndex] = refinedValue;
						replacedRecordLine = String.join(Form24QConstants.CAP_STR, recordLineContents);
						System.out.println("Refining value ::: lineNumber: " + lineNumber + " currentValue: " + currentValue + " refinedValue: " + refinedValue + " form24QRecordType: " + fieldInfo.form24QRecordType + " fieldName: " + fieldInfo.fieldName);
						replacesDone++;
					}
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
		System.out.println("replacesDone: " + replacesDone);
	}

	private static String getRefinedValue(String currentValue)
	{
		String refinedValue = currentValue;
		List<Character> replaceWithSpaceCharacters = Arrays.asList('’', (char) 160);
		for (Character character : replaceWithSpaceCharacters)
		{
			refinedValue = StringUtils.replaceChars(refinedValue, character, ' ');
		}
		List<Character> replaceWithEmptyCharacters = Arrays.asList('’');
		for (Character character : replaceWithEmptyCharacters)
		{
			refinedValue = StringUtils.replaceChars(refinedValue, character, Character.MIN_VALUE);
		}
		refinedValue = StringUtils.normalizeSpace(refinedValue);
		return refinedValue;
	}

	private static Map<Form24QRecordTypes, List<Form24QFieldInfo>> getRecordTypeVsFieldsIndexMap(Form24QRecordTypes form24QRecordType, List<String> fieldNames) throws Exception
	{
		Map<Form24QRecordTypes, List<Form24QFieldInfo>> recordTypeVsFieldsIndexMap = new HashMap<>();
		List<Form24QFieldInfo> fieldInfos = new ArrayList<>();
		for (String fieldName : fieldNames)
		{
			int form24QTypeFieldIndex = Form24QUtil.getIndexOfForm24QTypeField(form24QRecordType, fieldName);
			fieldInfos.add(new Form24QFieldInfo(form24QRecordType, fieldName, form24QTypeFieldIndex));
		}
		recordTypeVsFieldsIndexMap.put(form24QRecordType, fieldInfos);

		return recordTypeVsFieldsIndexMap;
	}
}
