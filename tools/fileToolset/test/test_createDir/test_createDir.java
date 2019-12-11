// import tools.fileToolset.fileToolset;

public class test_createDir {

	private static String filePath = "./test/test";

	public static void main(String[] args) {
		int t_createDir = fileToolset.createDir(test_createDir.filePath);

		System.out.println("test fileToolset.createDir, flag: " + t_createDir);
	}
}

