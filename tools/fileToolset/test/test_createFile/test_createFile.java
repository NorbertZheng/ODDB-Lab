// import tools.fileToolset.fileToolset;

public class test_createFile {

	private static String filePath = "./test/test_createFile.txt";

	public static void main(String[] args) {
		int t_createFile = fileToolset.createFile(test_createFile.filePath);

		System.out.println("test fileToolset.createFile, flag: " + t_createFile);
	}
}

