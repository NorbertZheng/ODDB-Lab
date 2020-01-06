import java.io.File;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.FileNotFoundException;
// local jar
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class virtualDisk {
	final static int BITS_OF_BYTE = 8, N_OF_CONFIG_BLOCK = 1;
	final static int OFFSET_DISKSIZE = 0, OFFSET_BLOCKSIZE = 4, OFFSET_ENTRYSIZE = 8, OFFSET_DATAOFFSET = 12;
	final static String charSet = "utf-8";

	private String baseLocation;	// the location of the whole project
	private String configLocation;	// the location of config file
	private String vdiskLocation;		// the location of vdisk

	private int diskSize;
	private int blockSize;
	private int entrySize;
	private int dataOffset;

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
		this.diskSize = Integer.parseInt(configure.getJSONObject(0).get("diskSize").toString());
		this.blockSize = Integer.parseInt(configure.getJSONObject(0).get("blockSize").toString());
		this.entrySize = Integer.parseInt(configure.getJSONObject(0).get("entrySize").toString());
		this.dataOffset = (((1 << (this.entrySize * virtualDisk.BITS_OF_BYTE)) * this.entrySize) / this.blockSize) + virtualDisk.N_OF_CONFIG_BLOCK;

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
		File vdiskFile;
		byte[] temp;
		byte[] data = new byte[this.blockSize];

		if (fileToolset.createFile(this.vdiskLocation) != 0) {
			return false;
		}

		// write configSector
		virtualDisk.clearByteArray(data);
		if (!virtualDisk.byteArrayCopy(data, virtualDisk.int2ByteArray(this.diskSize), virtualDisk.OFFSET_DISKSIZE)) {
			vdiskFile = new File(this.vdiskLocation);
			vdiskFile.delete();
			return false;
		}
		if (!virtualDisk.byteArrayCopy(data, virtualDisk.int2ByteArray(this.blockSize), virtualDisk.OFFSET_BLOCKSIZE)) {
			vdiskFile = new File(this.vdiskLocation);
			vdiskFile.delete();
			return false;
		}
		if (!virtualDisk.byteArrayCopy(data, virtualDisk.int2ByteArray(this.entrySize), virtualDisk.OFFSET_ENTRYSIZE)) {
			vdiskFile = new File(this.vdiskLocation);
			vdiskFile.delete();
			return false;
		}
		if (!virtualDisk.byteArrayCopy(data, virtualDisk.int2ByteArray(this.dataOffset), virtualDisk.OFFSET_DATAOFFSET)) {
			vdiskFile = new File(this.vdiskLocation);
			vdiskFile.delete();
			return false;
		}
		if (!this.write(0, 0, data, data.length)) {
			vdiskFile = new File(this.vdiskLocation);
			vdiskFile.delete();
			return false;
		}

		// write FAT-like entry
		virtualDisk.clearByteArray(data);
		for (int n_block = 0; n_block < this.diskSize / this.blockSize; n_block++) {
			// get the FAT-like entry
			if (n_block < this.dataOffset) {
				temp = virtualDisk.short2ByteArray((short) 0x0000);
			} else {
				temp = virtualDisk.short2ByteArray(virtualDisk.int2UnsignedShort(n_block));
			}
			// byte array copy
			if (!virtualDisk.byteArrayCopy(data, temp, (n_block * this.entrySize) % this.blockSize)) {
				vdiskFile = new File(this.vdiskLocation);
				vdiskFile.delete();
				return false;
			}
			// whether write vdisk
			if ((((n_block + 1) * this.entrySize) % this.blockSize) == 0) {
				// System.out.println(virtualDisk.byteArray2HexString(data));
				if (!this.write(((n_block * this.entrySize) / this.blockSize) + virtualDisk.N_OF_CONFIG_BLOCK, 0, data, data.length)) {
					vdiskFile = new File(this.vdiskLocation);
					vdiskFile.delete();
					return false;
				}
				// clear up data
				virtualDisk.clearByteArray(data);
			}
		}

		// write data sector
		virtualDisk.clearByteArray(data);
		for (int n_block = this.dataOffset; n_block < this.diskSize / this.blockSize; n_block++) {
			if (!this.write(n_block, 0, data, data.length)) {
				vdiskFile = new File(this.vdiskLocation);
				vdiskFile.delete();
				return false;
			}
		}

		System.out.println("newVdisk: build successfully!");
		return true;
	}

	private boolean getVdiskConfig() {
		byte[] data;
		byte[] temp = new byte[4];

		data = this.read(0, 0, this.blockSize);
		// get diskSize
		if (!virtualDisk.byteArrayIntercept(temp, data, virtualDisk.OFFSET_DISKSIZE)) {
			return false;
		} else {
			this.diskSize = virtualDisk.byteArray2Int(temp);
			System.out.println(virtualDisk.byteArray2Int(temp));
		}
		// get blockSize
		if (!virtualDisk.byteArrayIntercept(temp, data, virtualDisk.OFFSET_BLOCKSIZE)) {
			return false;
		} else {
			this.blockSize = virtualDisk.byteArray2Int(temp);
			System.out.println(virtualDisk.byteArray2Int(temp));
		}
		// get entrySize
		if (!virtualDisk.byteArrayIntercept(temp, data, virtualDisk.OFFSET_ENTRYSIZE)) {
			return false;
		} else {
			this.entrySize = virtualDisk.byteArray2Int(temp);
			System.out.println(virtualDisk.byteArray2Int(temp));
		}
		// get dataOffset
		if (!virtualDisk.byteArrayIntercept(temp, data, virtualDisk.OFFSET_DATAOFFSET)) {
			return false;
		} else {
			this.dataOffset = virtualDisk.byteArray2Int(temp);
			System.out.println(virtualDisk.byteArray2Int(temp));
		}

		return true;
	}

	/*
	 * convert byte[] to hex string
	 * @Args:
	 *  src(byte[])		: source byte[]
	 * @Ret:
	 *  data(String)	: hex string
	 */
	public static String byteArray2HexString(byte[] src) {
		StringBuffer stringBuffer = new StringBuffer(src.length);
		String data;

		for (int i = 0; i < src.length; i++) {
			data = Integer.toHexString(0xff & src[i]);
			if (data.length() < 2) {
				stringBuffer.append(0);
			}
			stringBuffer.append(data.toUpperCase());
		}

		return stringBuffer.toString();
	}

	/*
	 * intercept byte[] to byte[]
	 * @Args:
	 *  des(byte[])		: target byte[]
	 *  src(byte[])		: source byte[]
	 *  offset(int)		: the start source of target byte[]
	 * @Ret:
	 *  flag(boolean)	: whether copy successfully
	 */
	public static boolean byteArrayIntercept(byte[] des, byte[] src, int offset) {
		if (des.length + offset >= src.length) {
			return false;
		} else {
			for (int i = 0; i < des.length; i++) {
				des[i] = src[offset + i];
			}
			return true;
		}
	}

	/*
	 * copy byte[] into byte[]
	 * @Args:
	 *  des(byte[])		: target byte[]
	 *  src(byte[])		: source byte[]
	 *  offset(int)		: the start offset of source byte[]
	 * @Ret:
	 *  flag(boolean)	: whether copy successfully
	 */
	public static boolean byteArrayCopy(byte[] des, byte[] src, int offset) {
		if (des.length < src.length + offset) {
			return false;
		} else {
			for (int i = 0; i < src.length; i++) {
				des[offset + i] = src[i];
			}
			return true;
		}
	}

	/*
	 * clear up byte[]
	 * @Args:
	 *  src(byte[])		: source byte[]
	 */
	public static void clearByteArray(byte[] src) {
		int length = src.length;

		for (int i = 0; i < length; i++) {
			src[i] = 0;
		}

		return;
	}

	/*
	 * convert int to byte[]
	 * @Args:
	 *  src(int)		: source int
	 * @Ret:
	 *  data(byte[])	: corresponding byte[]
	 */
	public static byte[] int2ByteArray(int src) {
		byte[] data = new byte[4];

		data[0] = (byte) (src & 0xff);
		data[1] = (byte) ((src >> 8) & 0xff);
		data[2] = (byte) ((src >> 16) & 0xff);
		data[3] = (byte) ((src >> 24) & 0xff);

		return data;
	}

	/*
	 * convert byte[] to int
	 * @Args:
	 *  src(byte[])		: source byte[]
	 * @Ret:
	 *  data(int)		: corresponding int
	 */
	public static int byteArray2Int(byte[] src) {
		int data = 0;

		data = ((src[3] << 24) & 0xff000000) + ((src[2] << 16) & 0xff0000) + ((src[1] << 8) & 0xff00) + (src[0] & 0xff);

		return data;
	}

	/*
	 * convert short to byte[]
	 * @Args:
	 *  src(short)		: source short
	 * @Ret:
	 *  data(byte[])	: corresponding byte[]
	 */
	public static byte[] short2ByteArray(short src) {
		byte[] data = new byte[2];

		data[0] = (byte) (src & 0xff);
		data[1] = (byte) ((src >> 8) & 0xff);

		return data;
	}

	/*
	 * convert byte[] to short
	 * @Args:
	 *  src(byte[])		: source byte[]
	 * @Ret:
	 *  data(short)		: corresponding short
	 */
	public static short byteArray2Short(byte[] src) {
		short data = 0;

		data = (short) (((src[1] << 8) & 0xff00) + (src[0] & 0xff));

		return data;
	}

	/*
	 * convert string to byte[]
	 * @Args:
	 *  src(String)		: source string
	 * @Ret:
	 *  data(byte[])	: corresponding byte[]
	 */
	public static byte[] string2ByteArray(String src) {
		byte[] data;

		try {
			data = src.getBytes(virtualDisk.charSet);
		} catch (Exception ex) {
			System.err.println(ex.getMessage());
			return null;
		}

		return data;
	}

	/*
	 * convert byte[] to string
	 * @Args:
	 *  src(byte[])		: source byte[]
	 * @Ret:
	 *  data(String)	: corresponding string
	 */
	public static String byteArray2String(byte[] src) {
		String data;

		try {
			data = new String(src, virtualDisk.charSet);
		} catch (Exception ex) {
			System.err.println(ex.getMessage());
			return null;
		}

		return data;
	}

	public static int unsignedShort2Int(short src) {
		int data;

		data = ((int) src) & 0xffff;

		return data;
	}

	public static short int2UnsignedShort(int src) {
		short data;

		data = (short) (src & 0xffff);

		return data;
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

	
