// import tools.fileToolset.fileToolset;

public class test_readFile {

	private static String filePath = "./test/test_readFile.txt";

	public static void main(String[] args) {
		String t_readFile = fileToolset.readFile(test_readFile.filePath);

		System.out.println("test fileToolset.readFile, content: " + t_readFile);
	}
}

