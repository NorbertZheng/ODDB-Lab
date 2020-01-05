public class Attribute {
	final static int INT = 0, STRING = 1;

	public int type;
	public String name;
	public String expression;	// for virtualAttr
	public String className;

	public Attribute() {
		this.type = Attribute.INT;
		this.name = null;
		this.expression = null;
		this.className = null;
	}
}

