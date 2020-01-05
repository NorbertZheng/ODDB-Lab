import java.util.Map;
import java.util.HashMap;

public class test_booleanExecutor {
	final static String expression = "price >= 1 AND name = \"fassial\"";

	public static void main(String[] args) {
		boolean result;
		Map<String, Integer> intMap = new HashMap<>();
		Map<String, String> stringMap = new HashMap<>();
		

		intMap.put("price", 1);
		stringMap.put("name", "fassial");

		result = booleanExecutor.calculate(expression, intMap, stringMap);
		System.out.println("Result: " + result);
	}
}

