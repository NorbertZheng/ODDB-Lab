import java.util.ArrayList;

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

	public ArrayList<String> getAllIdentifier() {
		ArrayList<String> left, right, result;

		// init result
		result = new ArrayList<String>();
		// get identifier
		if (this.type == whereNode.IDENTIFIER) {
			if ((this.valueString == null) || (this.valueString.equals(""))) {
				System.out.println("ERROR: (in whereNode.getAllIdentifier) ((this.valueString == null) || (this.valueString.equals(\"\")))!");
				return null;
			} else {
				result.add(this.valueString);
			}
		} else if ((this.type == whereNode.INTEGER) || (this.type == whereNode.STRING)) {
			// do nothing
		} else {
			if (this.left == null) {
				System.out.println("ERROR: (in whereNode.getAllIdentifier) (this.left == null)!");
				return null;
			} else {
				left = this.left.getAllIdentifier();
			}
			if (this.right == null) {
				System.out.println("ERROR: (in whereNode.getAllIdentifier) (this.right == null)!");
				return null;
			} else {
				right = this.right.getAllIdentifier();
			}
			// check left & right
			if ((left == null) || (right == null)) {
				System.out.println("ERROR: (in whereNode.getAllIdentifier) ((left == null) || (right == null))!");
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

		if (this.type == whereNode.NOT) {
			result = "NOT ";
			if ((this.left.type == whereNode.OR) || (this.left.type == whereNode.AND)) {
				result += "(" + this.left.toString() + ")";
			} else {
				result += this.left.toString();
			}
		} else if (this.type == whereNode.AND) {
			if (this.left.type == whereNode.OR) {
				result += "(" + this.left.toString() + ")";
			} else {
				result += this.left.toString();
			}
			result += " AND ";
			if (this.right.type == whereNode.OR) {
				result += "(" + this.right.toString() + ")";
			} else {
				result += this.right.toString();
			}
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

