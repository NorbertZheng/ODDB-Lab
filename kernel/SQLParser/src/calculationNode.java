public class calculationNode {
	final static int PLUS = 0, MINUS = 1, MULTIPLY = 2, DIVIDE = 3, INTEGER = 4, IDENTIFIER = 5;

	public int operator;
	public int valueInt;
	public String valueString;
	public calculationNode left, right;

	public calculationNode() {
		this.operator = calculationNode.PLUS;
		this.valueInt = 0;
		this.valueString = null;
		this.left = null;
		this.right = null;
	}

	public String toString() {
		String result = "";

		if (this.operator == calculationNode.PLUS) {
			result = this.left.toString() + " + " + this.right.toString();
		} else if (this.operator == calculationNode.MINUS) {
			result = this.left.toString() + " - " + this.right.toString();
		} else if (this.operator == calculationNode.MULTIPLY) {
			if ((this.left.operator == calculationNode.PLUS) || (this.left.operator == calculationNode.MINUS)) {
				result += "(" + this.left.toString() + ")";
			} else {
				result += this.left.toString();
			}
			result += " * ";
			if ((this.right.operator == calculationNode.PLUS) || (this.right.operator == calculationNode.MINUS)) {
				result += "(" + this.right.toString() + ")";
			} else {
				result += this.right.toString();
			}
		} else if (this.operator == calculationNode.DIVIDE) {
			if ((this.left.operator == calculationNode.PLUS) || (this.left.operator == calculationNode.MINUS)) {
				result += "(" + this.left.toString() + ")";
			} else {
				result += this.left.toString();
			}
			result += " / ";
			if ((this.right.operator == calculationNode.PLUS) || (this.right.operator == calculationNode.MINUS)) {
				result += "(" + this.right.toString() + ")";
			} else {
				result += this.right.toString();
			}
		} else if (this.operator == calculationNode.INTEGER) {
			result = Integer.toString(this.valueInt);
		} else if (this.operator == calculationNode.IDENTIFIER) {
			result = this.valueString;
		} else {
			result = "";
		}

		return result;
	}
}

