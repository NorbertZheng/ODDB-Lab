public class whereNode {
	final static int NOT = 0, AND = 1, OR = 2, MORE = 3, LESS = 4, EQUAL = 5, NOTEQUAL = 6, MOREEQ = 7, LESSEQ = 8, INTEGER = 9, STRING = 10, IDENTIFIER = 11;

	public int type;
	public whereNode left, right;
	public int valueInt;
	public String valueString;

	public whereNode() {
		this.type = whereNode.NOT;
		this.left = null;
		this.right = null;
		this.valueInt = 0;
		this.valueString = null;
	}

	public String toString() {
		String result = "";

		if (this.type == whereNode.NOT) {
			result = "NOT " + this.left.toString();
		} else if (this.type == whereNode.AND) {
			result = this.left.toString() + " AND " + this.right.toString();
		} else if (this.type == whereNode.OR) {
			result = this.left.toString() + " OR " + this.right.toString();
		} else if (this.type == whereNode.MORE) {
			result = this.left.toString() + " > " + this.right.toString();
		} else if (this.type == whereNode.LESS) {
			result = this.left.toString() + " < " + this.right.toString();
		} else if (this.type == whereNode.EQUAL) {
			result = this.left.toString() + " = " + this.right.toString();
		} else if (this.type == whereNode.NOTEQUAL) {
			result = this.left.toString() + " <> " + this.right.toString();
		} else if (this.type == whereNode.MOREEQ) {
			result = this.left.toString() + " >= " + this.right.toString();
		} else if (this.type == whereNode.LESSEQ) {
			result = this.left.toString() + " <= " + this.right.toString();
		} else if (this.type == whereNode.INTEGER) {
			result = Integer.toString(this.valueInt);
		} else if (this.type == whereNode.STRING) {
			result = "\"" + this.valueString + "\"";
		} else if (this.type == whereNode.IDENTIFIER) {
			result = this.valueString;
		} else {
			result = "";
		}

		return result;
	}
}

