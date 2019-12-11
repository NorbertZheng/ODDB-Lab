// import tools.fileToolset.fileToolset;

public class test_writeFile {

	private static String filePath = "./test/test_writeFile.txt";
	private static String fileContent = "Hello World!";

	public static void main(String[] args) {
		boolean t_writeFile = fileToolset.writeFile(test_writeFile.filePath, test_writeFile.fileContent);

		System.out.println("test fileToolset.writeFile, flag: " + t_writeFile);
	}
}

