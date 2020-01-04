import java.util.Map;
import java.util.HashMap;

public class test_calculationExecutor {
	final static String expression = "(b / a) * (c + d)";

	public static void main(String[] args) {
		int result;
		Map<String, Integer> map = new HashMap<>();

		map.put("a", 1);
		map.put("b", 2);
		map.put("c", 3);
		map.put("d", 4);

		result = calculationExecutor.calculate(expression, map);
		System.out.println("Result: " + result);
	}
}

