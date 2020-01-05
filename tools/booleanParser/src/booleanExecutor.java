import java.util.Map;
import java.util.HashMap;

public class booleanExecutor {

	/*
	 * calculate the expression(int)
	 * @Args:
	 *  expression(String)	: the expression we are going to calculate
	 *  intMap(Map)			: the identifier-value map<String, Integer>
	 *  stringMap(Map)		: the identifier-value map<String, String>
	 * @Rets:
	 *  result(boolean)		: the result of the expression
	 */
	public static boolean calculate(String expression, Map<String, Integer> intMap, Map<String, String> stringMap) {
		boolean result;
		whereNode root;

		try {
			root = booleanParser.evaluate(expression);
		} catch (ParseException ex) {
			System.err.println(ex.getMessage());
			return false;
		}
		result = booleanExecutor.calculateHelper(root, intMap, stringMap);

		return result;
	}

	/*
	 * calculate the expression(int)
	 * @Args:
	 *  node(whereNode)			: the root of the expression tree
	 *  intMap(Map)				: the identifier-value map<String, Integer>
	 *  stringMap(Map)			: the identifier-value map<String, String>
	 * @Rets:
	 *  result(boolean)			: the result of the expression tree
	 */
	public static boolean calculateHelper(whereNode node, Map<String, Integer> intMap, Map<String, String> stringMap) {
		Integer leftInt, rightInt;
		String leftString, rightString;

		if (node.type == whereNode.OR) {
			return booleanExecutor.calculateHelper(node.left, intMap, stringMap) || booleanExecutor.calculateHelper(node.right, intMap, stringMap);
		} else if (node.type == whereNode.AND) {
			return booleanExecutor.calculateHelper(node.left, intMap, stringMap) && booleanExecutor.calculateHelper(node.right, intMap, stringMap);
		} else if (node.type == whereNode.NOT) {
			return !booleanExecutor.calculateHelper(node.left, intMap, stringMap);
		} else if (node.type == whereNode.MORE) {
			if ((node.left == null) || (node.right == null)) {
				return false;
			} else {
				if ((node.left.type == whereNode.INTEGER) && (node.right.type == whereNode.INTEGER)) {
					return (node.left.valueInt > node.right.valueInt);
				} else if ((node.left.type == whereNode.INTEGER) && (node.right.type == whereNode.IDENTIFIER)) {
					rightInt = intMap.get(node.right.valueString);

					if (rightInt == null) {
						return false;
					} else {
						return (node.left.valueInt > rightInt);
					}
				} else if ((node.left.type == whereNode.IDENTIFIER) && (node.right.type == whereNode.INTEGER)) {
					leftInt = intMap.get(node.left.valueString);

					if (leftInt == null) {
						return false;
					} else {
						return (leftInt > node.right.valueInt);
					}
				} else if ((node.left.type == whereNode.IDENTIFIER) && (node.right.type == whereNode.IDENTIFIER)) {
					leftInt = intMap.get(node.left.valueString);
					rightInt = intMap.get(node.right.valueString);

					if ((leftInt == null) || (rightInt == null)) {
						return false;
					} else {
						return (leftInt > rightInt);
					}
				} else {
					return false;
				}
			}
		} else if (node.type == whereNode.LESS) {
			if ((node.left == null) || (node.right == null)) {
				return false;
			} else {
				if ((node.left.type == whereNode.INTEGER) && (node.right.type == whereNode.INTEGER)) {
					return (node.left.valueInt < node.right.valueInt);
				} else if ((node.left.type == whereNode.INTEGER) && (node.right.type == whereNode.IDENTIFIER)) {
					rightInt = intMap.get(node.right.valueString);

					if (rightInt == null) {
						return false;
					} else {
						return (node.left.valueInt < rightInt);
					}
				} else if ((node.left.type == whereNode.IDENTIFIER) && (node.right.type == whereNode.INTEGER)) {
					leftInt = intMap.get(node.left.valueString);

					if (leftInt == null) {
						return false;
					} else {
						return (leftInt < node.right.valueInt);
					}
				} else if ((node.left.type == whereNode.IDENTIFIER) && (node.right.type == whereNode.IDENTIFIER)) {
					leftInt = intMap.get(node.left.valueString);
					rightInt = intMap.get(node.right.valueString);

					if ((leftInt == null) || (rightInt == null)) {
						return false;
					} else {
						return (leftInt < rightInt);
					}
				} else {
					return false;
				}
			}
		} else if (node.type == whereNode.NOTEQUAL) {
			if ((node.left == null) || (node.right == null)) {
				return false;
			} else {
				if ((node.left.type == whereNode.INTEGER) && (node.right.type == whereNode.INTEGER)) {
					return (node.left.valueInt != node.right.valueInt);
				} else if ((node.left.type == whereNode.INTEGER) && (node.right.type == whereNode.IDENTIFIER)) {
					rightInt = intMap.get(node.right.valueString);

					if (rightInt == null) {
						return false;
					} else {
						return (node.left.valueInt != rightInt);
					}
				} else if ((node.left.type == whereNode.IDENTIFIER) && (node.right.type == whereNode.INTEGER)) {
					leftInt = intMap.get(node.left.valueString);

					if (leftInt == null) {
						return false;
					} else {
						return (leftInt != node.right.valueInt);
					}
				} else if ((node.left.type == whereNode.IDENTIFIER) && (node.right.type == whereNode.IDENTIFIER)) {
					leftInt = intMap.get(node.left.valueString);
					rightInt = intMap.get(node.right.valueString);

					if ((leftInt == null) || (rightInt == null)) {
						return false;
					} else {
						return (leftInt != rightInt);
					}
				} else {
					return false;
				}
			}
		} else if (node.type == whereNode.MOREEQ) {
			if ((node.left == null) || (node.right == null)) {
				return false;
			} else {
				if ((node.left.type == whereNode.INTEGER) && (node.right.type == whereNode.INTEGER)) {
					return (node.left.valueInt >= node.right.valueInt);
				} else if ((node.left.type == whereNode.INTEGER) && (node.right.type == whereNode.IDENTIFIER)) {
					rightInt = intMap.get(node.right.valueString);

					if (rightInt == null) {
						return false;
					} else {
						return (node.left.valueInt >= rightInt);
					}
				} else if ((node.left.type == whereNode.IDENTIFIER) && (node.right.type == whereNode.INTEGER)) {
					leftInt = intMap.get(node.left.valueString);

					if (leftInt == null) {
						return false;
					} else {
						return (leftInt >= node.right.valueInt);
					}
				} else if ((node.left.type == whereNode.IDENTIFIER) && (node.right.type == whereNode.IDENTIFIER)) {
					leftInt = intMap.get(node.left.valueString);
					rightInt = intMap.get(node.right.valueString);

					if ((leftInt == null) || (rightInt == null)) {
						return false;
					} else {
						return (leftInt >= rightInt);
					}
				} else {
					return false;
				}
			}
		} else if (node.type == whereNode.LESSEQ) {
			if ((node.left == null) || (node.right == null)) {
				return false;
			} else {
				if ((node.left.type == whereNode.INTEGER) && (node.right.type == whereNode.INTEGER)) {
					return (node.left.valueInt <= node.right.valueInt);
				} else if ((node.left.type == whereNode.INTEGER) && (node.right.type == whereNode.IDENTIFIER)) {
					rightInt = intMap.get(node.right.valueString);

					if (rightInt == null) {
						return false;
					} else {
						return (node.left.valueInt <= rightInt);
					}
				} else if ((node.left.type == whereNode.IDENTIFIER) && (node.right.type == whereNode.INTEGER)) {
					leftInt = intMap.get(node.left.valueString);

					if (leftInt == null) {
						return false;
					} else {
						return (leftInt <= node.right.valueInt);
					}
				} else if ((node.left.type == whereNode.IDENTIFIER) && (node.right.type == whereNode.IDENTIFIER)) {
					leftInt = intMap.get(node.left.valueString);
					rightInt = intMap.get(node.right.valueString);

					if ((leftInt == null) || (rightInt == null)) {
						return false;
					} else {
						return (leftInt <= rightInt);
					}
				} else {
					return false;
				}
			}
		}  else if (node.type == whereNode.EQUAL) {
			if ((node.left == null) || (node.right == null)) {
				return false;
			} else {
				if ((node.left.type == whereNode.INTEGER) && (node.right.type == whereNode.INTEGER)) {
					return (node.left.valueInt == node.right.valueInt);
				} else if ((node.left.type == whereNode.INTEGER) && (node.right.type == whereNode.IDENTIFIER)) {
					rightInt = intMap.get(node.right.valueString);

					if (rightInt == null) {
						return false;
					} else {
						return (node.left.valueInt == rightInt);
					}
				} else if ((node.left.type == whereNode.IDENTIFIER) && (node.right.type == whereNode.INTEGER)) {
					leftInt = intMap.get(node.left.valueString);

					if (leftInt == null) {
						return false;
					} else {
						return (leftInt == node.right.valueInt);
					}
				} else if ((node.left.type == whereNode.IDENTIFIER) && (node.right.type == whereNode.IDENTIFIER)) {
					leftInt = intMap.get(node.left.valueString);
					rightInt = intMap.get(node.right.valueString);

					if ((leftInt == null) || (rightInt == null)) {
						return false;
					} else {
						return (leftInt == rightInt);
					}
				} else if ((node.left.type == whereNode.STRING) && (node.right.type == whereNode.STRING)) {
					return (node.left.valueString == node.right.valueString);
				} else if ((node.left.type == whereNode.STRING) && (node.right.type == whereNode.IDENTIFIER)) {
					rightString = stringMap.get(node.right.valueString);

					if (rightString == null) {
						return false;
					} else {
						return (node.left.valueString.equals(rightString));
					}
				} else if ((node.left.type == whereNode.IDENTIFIER) && (node.right.type == whereNode.STRING)) {
					leftString = stringMap.get(node.left.valueString);

					if (leftString == null) {
						return false;
					} else {
						return (leftString.equals(node.right.valueString));
					}
				} else if ((node.left.type == whereNode.IDENTIFIER) && (node.right.type == whereNode.IDENTIFIER)) {
					leftString = stringMap.get(node.left.valueString);
					rightString = stringMap.get(node.right.valueString);

					if ((leftString == null) || (rightString == null)) {
						return false;
					} else {
						return (leftString.equals(rightString));
					}
				} else {
					return false;
				}
			}
		} else {
			return false;
		}
	}
}

