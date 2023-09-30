package form24q.util;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Form24QRecordTypesXLSMeta
{
	public final Form24QXLSMeta FILE_HEADER;
	public final Form24QXLSMeta BATCH_HEADER;
	public final Form24QXLSMeta CHALLAN_DETAIL;
	public final Form24QXLSMeta DEDUCTEE_DETAIL;
	public final Form24QXLSMeta SALARY_DETAIL;
	public final Form24QXLSMeta SECTION_16;
	public final Form24QXLSMeta CHAPTER_6A;

	public Form24QRecordTypesXLSMeta(int startingRowNumber)
	{
		FILE_HEADER = new Form24QXLSMeta(Form24QRecordTypes.FILE_HEADER.code, startingRowNumber);
		BATCH_HEADER = new Form24QXLSMeta(Form24QRecordTypes.BATCH_HEADER.code, startingRowNumber);
		CHALLAN_DETAIL = new Form24QXLSMeta(Form24QRecordTypes.CHALLAN_DETAIL.code, startingRowNumber);
		DEDUCTEE_DETAIL = new Form24QXLSMeta(Form24QRecordTypes.DEDUCTEE_DETAIL.code, startingRowNumber);
		SALARY_DETAIL = new Form24QXLSMeta(Form24QRecordTypes.SALARY_DETAIL.code, startingRowNumber);
		SECTION_16 = new Form24QXLSMeta(Form24QRecordTypes.SECTION_16.code, startingRowNumber);
		CHAPTER_6A = new Form24QXLSMeta(Form24QRecordTypes.CHAPTER_6A.code, startingRowNumber);
	}

	public void initializeForm24QSheets(XSSFWorkbook workbook)
	{
		FILE_HEADER.setXssfSheet(workbook.createSheet(Form24QRecordTypes.FILE_HEADER.name));
		BATCH_HEADER.setXssfSheet(workbook.createSheet(Form24QRecordTypes.BATCH_HEADER.name));
		CHALLAN_DETAIL.setXssfSheet(workbook.createSheet(Form24QRecordTypes.CHALLAN_DETAIL.name));
		DEDUCTEE_DETAIL.setXssfSheet(workbook.createSheet(Form24QRecordTypes.DEDUCTEE_DETAIL.name));
		SALARY_DETAIL.setXssfSheet(workbook.createSheet(Form24QRecordTypes.SALARY_DETAIL.name));
		SECTION_16.setXssfSheet(workbook.createSheet(Form24QRecordTypes.SECTION_16.name));
		CHAPTER_6A.setXssfSheet(workbook.createSheet(Form24QRecordTypes.CHAPTER_6A.name));
	}

	public void populateForm24QColumnHeaders() throws Exception
	{
		System.out.println("Going to populate Form 24Q Column Headers...");
		for (Form24QRecordTypes form24QRecordType : Form24QRecordTypes.values())
		{
			Form24QUtil.writeIntoXLSSheetForForm24QRecordType(this, true, form24QRecordType.code, Form24QConstants.FOR24Q_INPUT_MAPPING.get(form24QRecordType));
		}
		System.out.println("Completed populating Form 24Q Column Headers!!!");
	}
}
