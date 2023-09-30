package form24q.util;

public class Form24QFieldInfo
{
	public final Form24QRecordTypes form24QRecordType;
	public final String fieldName;
	public final int fieldIndex;

	public Form24QFieldInfo(Form24QRecordTypes form24QRecordType, String fieldName, int fieldIndex)
	{
		this.form24QRecordType = form24QRecordType;
		this.fieldName = fieldName;
		this.fieldIndex = fieldIndex;
	}
}
