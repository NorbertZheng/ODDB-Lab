import java.util.ArrayList;

public class test_pathParser {
	final static String path = "./data/data.sqlite";

	public static void main(String[] args) {
		ArrayList<String> subPath = fileToolset.pathParser(test_pathParser.path);

		System.out.println("subPath:");
		for (int i = 0; i < subPath.size(); i++) {
			System.out.println("\t" + subPath.get(i));
		}
	}
}

