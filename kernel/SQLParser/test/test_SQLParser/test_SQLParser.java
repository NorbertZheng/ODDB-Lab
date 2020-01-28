public class test_SQLParser {
	// CREATE_CLASS
	// final static String expr = "CREATE CLASS product ( id int, name char , price int ); ";
	// DROP_CLASS
	// final static String expr = "DROP CLASS usproduct;";
	// final static String expr = "DROP CLASS product;";
	// SELECT_TUPLE
	// final static String expr = "SELECT id , name , price FROM product;";
	// final static String expr = "SELECT id , name , price FROM product;";
	// final static String expr = "SELECT name AS name, (price/7) AS usprice FROM product WHERE price>5000;";
	// final static String expr = "SELECT sales , name , usprice FROM usproduct;";
	// final static String expr = "SELECT sales , name , usprice FROM usproduct;";
	// final static String expr = "SELECT id AS id, (price*15) AS jpprice FROM product WHERE price<=7000;";
	// final static String expr = "SELECT id, jpprice FROM jpproduct;";
	// final static String expr = "SELECT sales,name,usprice FROM usproduct;";
	// final static String expr = "SELECT id, jpprice FROM jpproduct;";
	// final static String expr = "SELECT sales,name,usprice FROM usproduct;";
	// final static String expr = "SELECT id, jpprice FROM jpproduct;";
	// final static String expr = "SELECT sales,name,usprice FROM usproduct;";
	// final static String expr = "SELECT id, jpprice FROM jpproduct;";
	// final static String expr = "SELECT sales,name,usprice FROM usproduct;";
	final static String expr = "SELECT id, jpprice FROM jpproduct;";

	public static void main(String[] args) {
		try {
			System.out.println(SQLParser.evaluate(test_SQLParser.expr));
		} catch (ParseException ex) {
			System.err.println(ex.getMessage());
		}
	}
}

