public class test_booleanParser {
	final static String expr = "NOT price >= 7 AND age > 18";

	public static void main(String[] args) {
		try {
			System.out.println(booleanParser.evaluate(test_booleanParser.expr));
		} catch (ParseException ex) {
			System.err.println(ex.getMessage());
		}
	}
}

