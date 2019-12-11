import java.io.File;
import java.io.IOException;

public class fileToolset {
	private static int SUCCESS = 0, ALEXIST = 1, DIFFTYPE = 2, IOFAIL = 3;

	/*
	 * Create new file
	 * @Args:
	 *  path: String
	 * @Ret:
	 *  flag: int(0 -> success, 1 -> already exist, 2 -> same name dir, 3 -> try fail)
	 */
	public static int createFile(String path) {
		try {
			File file = new File(path);
			File parentFile = file.getParentFile();

			if (parentFile.exists()) {
				if (parentFile.isDirectory()) {
					if (file.exists()) {
						if (file.isDirectory()) {
							return fileToolset.DIFFTYPE;
						} else {
							return fileToolset.ALEXIST;
						}
					} else {
						file.createNewFile();
						return fileToolset.SUCCESS;
					}
				} else {
					return fileToolset.DIFFTYPE;
				}
			} else {
				parentFile.mkdirs();
				file.createNewFile();
				return fileToolset.SUCCESS;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return fileToolset.IOFAIL;
		}
	}

	/*
	 * Create new directory
	 * @Args:
	 *  path: String
	 * @Ret:
	 *  flag: int(0 -> success, 1 -> already exist, 2 -> same name file, 3 -> try fail)
	 */
	public static int createDir(String path) {
		File file = new File(path);

		if (file.exists()) {
			if (file.isDirectory()) {
				return fileToolset.ALEXIST;
			} else {
				return fileToolset.DIFFTYPE;
			}
		} else {
			file.mkdirs();
			return fileToolset.SUCCESS;
		}
	}

}

