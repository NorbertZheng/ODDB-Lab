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

	public String toString() {
		String result = "";

		// name
		if ((this.name == null) || (this.name.equals(""))) {
			result += "null";
		} else {
			result += this.name;
		}
		// type
		if (this.type == Attribute.INT) {
			result += "\tINT";
		} else if (this.type == Attribute.STRING) {
			result += "\tSTRING";
		} else {
			System.out.println("ERROR: (in Attribute.toString) unknown type!");
			result += "\tUNKNOWN";
		}
		// expression
		if ((this.expression == null) || (this.expression.equals(""))) {
			result += "\tnull";
		} else {
			result += "\t" + this.expression;
		}
		// className
		if ((this.className == null) || (this.className.equals(""))) {
			result += "\tnull";
		} else {
			result += "\t" + this.className;
		}

		return result;
	}

}

