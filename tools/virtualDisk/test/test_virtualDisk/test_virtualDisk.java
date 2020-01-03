import java.io.File;
import java.util.ArrayList;

public class test_virtualDisk {

	private static String baseLocation;

	public static void main(String[] args) {
		int i;
		ArrayList<String> subBaseLocation;

		test_virtualDisk.baseLocation = System.getProperty("user.dir");
		subBaseLocation = fileToolset.pathParser(baseLocation);
		test_virtualDisk.baseLocation = "";
		for (i = 0; i < subBaseLocation.size() - 5; i++) {
			test_virtualDisk.baseLocation += subBaseLocation.get(i) + File.separator;
		}
		test_virtualDisk.baseLocation += subBaseLocation.get(i);

		virtualDisk vdisk = new virtualDisk(test_virtualDisk.baseLocation);
	}
}

