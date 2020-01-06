import java.io.File;
import java.util.ArrayList;

public class test_FAT {
	final static int n_block = 257;

	private static String baseLocation;

	public static void main(String[] args) {
		int i;
		ArrayList<String> subBaseLocation;

		test_FAT.baseLocation = System.getProperty("user.dir");
		subBaseLocation = fileToolset.pathParser(baseLocation);
		test_FAT.baseLocation = "";
		for (i = 0; i < subBaseLocation.size() - 5; i++) {
			test_FAT.baseLocation += subBaseLocation.get(i) + File.separator;
		}
		test_FAT.baseLocation += subBaseLocation.get(i);

		virtualDisk vdisk = new virtualDisk(test_FAT.baseLocation);
		System.out.printf("%d\n", vdisk.getNextBlock(test_FAT.n_block));
	}
}

