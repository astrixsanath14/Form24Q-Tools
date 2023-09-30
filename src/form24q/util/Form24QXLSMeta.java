package form24q.util;

import org.apache.poi.xssf.usermodel.XSSFSheet;

public class Form24QXLSMeta
{
	private String recordType;
	private int rowNumber;
	private int recordCount;
	private XSSFSheet xssfSheet;
	int headerColumnCount;

	public Form24QXLSMeta(String recordType)
	{
		new Form24QXLSMeta(recordType, 0);
	}

	public Form24QXLSMeta(String recordType, int startingRowNumber)
	{
		this.recordType = recordType;
		this.rowNumber = startingRowNumber;
		this.recordCount = 0;
		this.xssfSheet = null;
		this.headerColumnCount = -1;
	}

	public void incrementRowNumber()
	{
		this.rowNumber++;
	}

	public void incrementRowCount()
	{
		this.recordCount++;
	}

	public void setXssfSheet(XSSFSheet xssfSheet)
	{
		this.xssfSheet = xssfSheet;
	}

	public int getRowNumber()
	{
		return this.rowNumber;
	}

	public int getRecordCount()
	{
		return this.recordCount;
	}

	public XSSFSheet getXssfSheet()
	{
		return xssfSheet;
	}

	public String getRecordType()
	{
		return recordType;
	}

	public int getHeaderColumnCount()
	{
		return headerColumnCount;
	}

	public void setHeaderColumnCount(int headerColumnCount)
	{
		this.headerColumnCount = headerColumnCount;
	}
}
