package form24q.util;

import java.util.Arrays;
import java.util.List;

public enum Form24QRecordTypes
{
	FILE_HEADER("FH", "File Header"),
	BATCH_HEADER("BH", "Batch Header"),
	CHALLAN_DETAIL("CD", "Challan Detail"),
	DEDUCTEE_DETAIL("DD", "Deductee Detail"),
	SALARY_DETAIL("SD", "Salary Detail"),
	SECTION_16("S16", "Section 16"),
	CHAPTER_6A("C6A", "Chapter 6A");

	String code;
	String name;

	Form24QRecordTypes(String code, String name)
	{
		this.code = code;
		this.name = name;
	}

	public static final List<Form24QRecordTypes> SALARY_DETAIL_RELATED_RECORDS = Arrays.asList(SALARY_DETAIL, SECTION_16, CHAPTER_6A);

	public static Form24QRecordTypes getForm24QRecordTypesFromCode(String currentCode) throws Exception
	{
		if (currentCode.contains(Form24QConstants.QUOTE_REGEX))
		{
			currentCode = currentCode.replaceAll(Form24QConstants.QUOTE_REGEX, "");
		}
		for (Form24QRecordTypes form24QRecordType : values())
		{
			if (currentCode.equalsIgnoreCase(form24QRecordType.code))
			{
				return form24QRecordType;
			}
		}
		throw new Exception("Invalid Form24QRecordTypes currentCode: " + currentCode);
	}

	@Override public String toString()
	{
		return super.name() + " code: " + getCode() + " name: " + getName();
	}

	public String getCode()
	{
		return this.code;
	}

	public String getName()
	{
		return this.code;
	}
}
