import java.util.Map;
import java.util.HashMap;

public class calculationExecutor {

	/*
	 * calculate the expression(int)
	 * @Args:
	 *  expression(String)	: the expression we are going to calculate
	 *  map(Map)			: the identifier-value map<String, Integer>
	 * @Rets:
	 *  result(int)			: the result of the expression
	 */
	public static int calculate(String expression, Map<String, Integer> map) {
		int result;
		calculationNode root;

		try {
			root = calculationParser.evaluate(expression);
		} catch (ParseException ex) {
			System.err.println(ex.getMessage());
			return 0;
		}
		result = calculationExecutor.calculateHelper(root, map);

		return result;
	}

	/*
	 * calculate the expression(int)
	 * @Args:
	 *  node(calculationNode)	: the root of the expression tree
	 *  map(Map)				: the identifier-value map<String, Integer>
	 * @Rets:
	 *  result(int)				: the result of the expression tree
	 */
	public static int calculateHelper(calculationNode node, Map<String, Integer> map) {
		if (node.operator == calculationNode.IDENTIFIER) {
			return map.get(node.valueString);
		} else if (node.operator == calculationNode.INTEGER) {
			return node.valueInt;
		} else if (node.operator == calculationNode.MINUS) {
			if (node.right == null) {
				return -calculationExecutor.calculateHelper(node.left, map);
			} else {
				return calculationExecutor.calculateHelper(node.left, map) - calculationExecutor.calculateHelper(node.right, map);
			}
		} else if (node.operator == calculationNode.PLUS) {
			return calculationExecutor.calculateHelper(node.left, map) + calculationExecutor.calculateHelper(node.right, map);
		} else if (node.operator == calculationNode.MULTIPLY) {
			return calculationExecutor.calculateHelper(node.left, map) * calculationExecutor.calculateHelper(node.right, map);
		} else if (node.operator == calculationNode.DIVIDE) {
			return calculationExecutor.calculateHelper(node.left, map) / calculationExecutor.calculateHelper(node.right, map);
		} else {
			return 0;
		}
	}
}

