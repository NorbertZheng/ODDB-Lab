public class Attribute {
	public String attrName;
	public int attrType;
	public int attrSize;
	public String defaultVal;

	public Attribute(String attrName, int attrType, int attrSize, String defaultVal) {
		this.attrName = attrName;
		this.attrType = attrType;
		this.attrSize = attrSize;
		this.defaultVal = (defaultVal == null) ? "" : defaultVal;
	}

	public Attribute() {

	}
}

