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
	final static String expr = "SELECT sales , name , usprice FROM usproduct;";
	// final static String expr = "SELECT id AS id, (price*15) AS jpprice FROM product WHERE price<=7000;";
	// final static String expr = "SELECT id, jpprice FROM jpproduct;";
	// final static String expr = "SELECT sales,name,usprice FROM usproduct;";
	// final static String expr = "SELECT id, jpprice FROM jpproduct;";
	// final static String expr = "SELECT sales,name,usprice FROM usproduct;";
	// final static String expr = "SELECT id, jpprice FROM jpproduct;";
	// final static String expr = "SELECT sales,name,usprice FROM usproduct;";
	// final static String expr = "SELECT id, jpprice FROM jpproduct;";
	// final static String expr = "SELECT sales,name,usprice FROM usproduct;";
	// final static String expr = "SELECT id, jpprice FROM jpproduct;";
	// CROSS_SELECT_TUPLE
	// final static String expr = "SELECT  jpproduct->product->usproduct.sales ,  jpproduct->product->usproduct.name FROM jpproduct WHERE id=2;";
	// CREATE_SELECT_DEPUTY_CLASS
	// final static String expr = "CREATE SELECTDEPUTY usproduct ( sales int ) SELECT name AS name, (price/7) AS usprice FROM product WHERE price>5000;";
	// final static String expr = "CREATE SELECTDEPUTY usproduct ( sales char ) SELECT name AS name, (price/7) AS usprice FROM product WHERE price>5000;";
	// final static String expr = "CREATE SELECTDEPUTY jpproduct SELECT id AS id, (price*15) AS jpprice FROM product WHERE price<=7000;";
	// INSERT_TUPLE
	// final static String expr = "INSERT INTO product ( id , name , price ) VALUES ( 1 , \"mac\" , 14000 );";
	// final static String expr = "INSERT INTO product ( id , name , price ) VALUES ( 2 , \"ipad\" , 7000 );";
	// final static String expr = "INSERT INTO product ( id , name , price ) VALUES ( 3 , \"iphone\" , 7000 );";
	// final static String expr = "INSERT INTO product ( id , name , price ) VALUES ( 4 , \"mi\" , 2500 );";
	// final static String expr = "INSERT INTO product ( id , name , price ) VALUES ( 5 , \"vivo\" , 3000 );";
	// final static String expr = "INSERT INTO product ( id , name , price ) VALUES ( 6 , \"huawei\" , 7700 );";
	// DELETE_TUPLE
	// final static String expr = "DELETE FROM product WHERE name=\"mi\" ;";
	// final static String expr = "DELETE FROM product WHERE name=\"ipad\";";
	// UPDATE_TUPLE
	// final static String expr = "UPDATE product SET price=4900 WHERE name=\"iphone\";";
	// final static String expr = "UPDATE usproduct SET sales=3000 WHERE name=\"ipad\";";
	// final static String expr = "UPDATE usproduct SET sales=2000 WHERE name=\"mac\";";
	// final static String expr = "UPDATE product SET price=4900 WHERE name=\"huawei\";";
	// final static String expr = "UPDATE product SET price=4500 WHERE name=\"huawei\";";

	public static void main(String[] args) {
		try {
			System.out.println(SQLParser.evaluate(test_SQLParser.expr));
		} catch (ParseException ex) {
			System.err.println(ex.getMessage());
		}
	}
}

