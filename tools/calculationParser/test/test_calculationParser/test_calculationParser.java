public class test_calculationParser {
	final static String expr = "1 + 3";

	public static void main(String[] args) {
		try {
			System.out.println(calculationParser.evaluate(test_calculationParser.expr));
		} catch (ParseException ex) {
			System.err.println(ex.getMessage());
		}
	}
}

