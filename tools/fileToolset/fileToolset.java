import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class fileToolset {
	private static String encoding = "UTF-8";
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

	/*
	 * Read entire file content
	 * @Args:
	 *  path		: String
	 * @Ret:
	 *  fileContent	: String
	 */
	public static String readFile(String path) {
		File file = new File(path);

		if (file.exists()) {
			if (file.isDirectory()) {
				return null;
			} else {
				Long fileLength = file.length();
				byte[] fileContent = new byte[fileLength.intValue()];

				try (FileInputStream in = new FileInputStream(file)) {
					in.read(fileContent);
					in.close();
					return new String(fileContent, fileToolset.encoding);
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}
		} else {
			return null;
		}
	}

	/*
	 * Write entire file content(override)
	 * @Args:
	 *  path		: String
	 *  fileContent	: String
	 * @Ret:
	 *  flag		: boolean
	 */
	public static boolean writeFile(String path, String fileContent) {
		File file = new File(path);

		if (file.exists()) {
			if (file.isDirectory()) {
				return false;
			} else {
				try (FileOutputStream out = new FileOutputStream(file)) {
					byte[] fileContentBytes = fileContent.getBytes();

					out.write(fileContentBytes);
					out.flush();
					out.close();
					return true;
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			}
		} else {
			if (fileToolset.createFile(path) == 0) {
				try (FileOutputStream out = new FileOutputStream(file)) {
					byte[] fileContentBytes = fileContent.getBytes();

					out.write(fileContentBytes);
					out.flush();
					out.close();
					return true;
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			} else {
				return false;
			}
		}
	}

}

