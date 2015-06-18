
public class varDetails 
{
	private String type, kind;
	private int index;
	
	public varDetails(String vKind, String vType, int vIndex)
	{
		kind = vKind;
		type = vType;
		index = vIndex;
	}
	
	public int getIndex() {
		return index;
	}
	
	public String getType() {
		return type;
	}
	
	public String getKind() {
		return kind;
	}
}
