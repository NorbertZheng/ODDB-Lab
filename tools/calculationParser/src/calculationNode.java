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
}

