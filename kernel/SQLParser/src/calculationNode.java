import java.util.ArrayList;

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

	public ArrayList<String> getAllIdentifier() {
		ArrayList<String> left, right, result;

		// init result
		result = new ArrayList<String>();
		// get identifier
		if (this.operator == calculationNode.IDENTIFIER) {
			if ((this.valueString == null) || (this.valueString.equals(""))) {
				System.out.println("ERROR: (in calculationNode.getAllIdentifier) ((this.valueString == null) || (this.valueString.equals(\"\")))!");
				return null;
			} else {
				result.add(this.valueString);
			}
		} else if (this.operator == calculationNode.INTEGER) {
			// do nothing
		} else {
			if (this.left == null) {
				System.out.println("ERROR: (in calculationNode.getAllIdentifier) (this.left == null)!");
				return null;
			} else {
				left = this.left.getAllIdentifier();
			}
			if (this.right == null) {
				System.out.println("ERROR: (in calculationNode.getAllIdentifier) (this.right == null)!");
				return null;
			} else {
				right = this.right.getAllIdentifier();
			}
			// check left & right
			if ((left == null) || (right == null)) {
				System.out.println("ERROR: (in calculationNode.getAllIdentifier) ((left == null) || (right == null))!");
				return null;
			}
			// get result
			for (int i = 0; i < left.size(); i++) {
				result.add(left.get(i));
			}
			for (int i = 0; i < right.size(); i++) {
				result.add(right.get(i));
			}
		}

		return result;
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

