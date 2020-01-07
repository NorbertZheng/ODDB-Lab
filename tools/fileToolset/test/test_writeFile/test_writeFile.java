// import tools.fileToolset.fileToolset;

public class test_writeFile {

	private static String filePath = "./test/test_writeFile.txt";
	private static String fileContent1 = "Hello World!", fileContent2 = "123";

	public static void main(String[] args) {
		boolean t_writeFile = fileToolset.writeFile(test_writeFile.filePath, test_writeFile.fileContent1);

		System.out.println("test fileToolset.writeFile, flag: " + t_writeFile);

		t_writeFile = fileToolset.writeFile(test_writeFile.filePath, test_writeFile.fileContent2);

		System.out.println("test fileToolset.writeFile, flag: " + t_writeFile);
	}
}

