public class test_SQLParser {
	// final static String expr = "CREATE CLASS product ( id int, name char , price int ); ";
	final static String expr = "DROP CLASS usproduct;";

	public static void main(String[] args) {
		try {
			System.out.println(SQLParser.evaluate(test_SQLParser.expr));
		} catch (ParseException ex) {
			System.err.println(ex.getMessage());
		}
	}
}

