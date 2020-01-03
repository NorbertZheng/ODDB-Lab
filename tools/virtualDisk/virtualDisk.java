import java.io.File;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.FileNotFoundException;
// local jar
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class virtualDisk {
	private String baseLocation;	// the location of the whole project
	private String configLocation;	// the location of config file
	private String vdiskLocation;		// the location of vdisk

	private int diskSize;
	private int blockSize;

	public virtualDisk(String baseLocation) {
		JSONArray configure;

		// init location
		this.baseLocation = baseLocation;
		this.configLocation = this.baseLocation + File.separator + "configure";
		System.out.println("configure location: " + this.configLocation);

		// get configure & vdiskLocation
		configure = jsonToolset.readJsonFile(this.configLocation);
		this.vdiskLocation = this.baseLocation + File.separator + configure.getJSONObject(0).get("location").toString();
		System.out.println("vdisk location: " + this.vdiskLocation);

		// check whether vdisk exists
		File vdiskFile = new File(this.vdiskLocation);
		if (vdiskFile.exists()) {
			if (vdiskFile.length() < 512) {
				System.out.println("vdiskFile size: " + Long.toString(vdiskFile.length()) + ". Prepare to rebuild vdisk!");
				vdiskFile.delete();
				this.newVdisk();
			}
			getVdiskConfig();
		} else {
			this.newVdisk();
		}
	}

	/*
	 * create a new vdisk
	 * @Ret:
	 *  flag(boolean)	: whether create vdisk successfully
	 */
	private boolean newVdisk() {
		boolean flag;

		flag = (fileToolset.createFile(this.vdiskLocation) == 0);
		return flag;
	}

	private static boolean getVdiskConfig() {
		return false;
	}

	/*
	 * virtualDisk write
	 * @Args:
	 *  n_block(int)	: the number of start block
	 *  offset(int)		: the offset in the block
	 *  data(byte[])	: data array
	 *  length(int)		: valid data in data array
	 * @Ret:
	 *  flag(boolean)	: whether write successfully
	 */
	public boolean write(int n_block, int offset, byte[] data, int length) {
		int filePointer = (n_block * this.blockSize) + offset;

		try {
			RandomAccessFile raf = new RandomAccessFile(new File(this.vdiskLocation), "rw");

			if ((filePointer + length) > this.diskSize) {
				raf.close();
				return false;
			} else {
				raf.seek(filePointer);
				raf.write(data, 0, length);
				raf.close();
				return true;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/*
	 * virtualDisk write
	 * @Args:
	 *  n_block(int)	: the number of start block
	 *  offset(int)		: the offset in the block
	 *  length(int)		: valid data in data array
	 * @Ret:
	 *  data(byte[])	: read data
	 */
	public byte[] read(int n_block, int offset, int length) {
		int filePointer = (n_block * this.blockSize) + offset;
		byte[] data = new byte[length];

		try {
			RandomAccessFile raf = new RandomAccessFile(new File(this.vdiskLocation), "rw");

			if ((filePointer + length) > this.diskSize) {
				raf.close();
				return null;
			} else {
				raf.seek(filePointer);
				raf.read(data);
				raf.close();
				return data;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

}

	
