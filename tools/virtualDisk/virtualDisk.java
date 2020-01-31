import java.io.File;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.ArrayList;
// local jar
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class virtualDisk {
	final static int BITS_OF_BYTE = 8, MAX_INTEGER = 0x7FFFFFFF;
	final static int OFFSET_DISKSIZE = 0, OFFSET_BLOCKSIZE = 4, OFFSET_ENTRYSIZE = 8, OFFSET_DATAOFFSET = 12;
	final static int CONFIG_BLOCK_FLAG = 0x0000, FREE_BLOCK_FLAG = 0x0001;
	final static int BYTES_OF_STRING_DATA = 20, BYTES_OF_INTEGER_DATA = 4;
	final static String CLASS_TABLE = "CLASS_TABLE", ATTRIBUTE_TABLE = "ATTRIBUTE_TABLE", DEPUTY_TABLE = "DEPUTY_TABLE", DEPUTYRULE_TABLE = "DEPUTYRULE_TABLE", OBJECT_TABLE = "OBJECT_TABLE", SWITCHING_TABLE = "SWITCHING_TABLE", BIPOINTER_TABLE = "BIPOINTER_TABLE";
	final static int ATTRTYPE_INTEGER = 0, ATTRTYPE_STRING = 1;
	final static int N_OF_CONFIG_BLOCK = 1, N_BUFFERLINE = 8;
	final static String charSet = "utf-8";

	public final static int PAGESIZE = 32, DEFAULT_BIPOINTER = -1;

	private String baseLocation;		// the location of the whole project
	private String configLocation;		// the location of config file
	private String vdiskLocation;		// the location of vdisk
	private String systemTableLocation;	// the location of system table

	private int diskSize;
	private int blockSize;
	private int entrySize;
	private int dataOffset;

	private ArrayList<classTable> systemClassTable;
	private ArrayList<attributeTable> systemAttributeTable;
	private ArrayList<deputyTable> systemDeputyTable;
	private ArrayList<deputyRuleTable> systemDeputyRuleTable;
	private ArrayList<objectTable> systemObjectTable;
	private ArrayList<switchingTable> systemSwitchingTable;
	private ArrayList<biPointerTable> systemBiPointerTable;

	private int fakeBlockNum, fakeBlockOffset;
	private String currClassName;

	private ArrayList<bufferLine> buffer;

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
		this.systemTableLocation = this.baseLocation + File.separator + configure.getJSONObject(0).get("systemTable").toString();
		System.out.println("systemTable location: " + this.systemTableLocation);
		this.diskSize = Integer.parseInt(configure.getJSONObject(0).get("diskSize").toString());
		this.blockSize = Integer.parseInt(configure.getJSONObject(0).get("blockSize").toString());
		this.entrySize = Integer.parseInt(configure.getJSONObject(0).get("entrySize").toString());
		this.dataOffset = (((1 << (this.entrySize * virtualDisk.BITS_OF_BYTE)) * this.entrySize) / this.blockSize) + virtualDisk.N_OF_CONFIG_BLOCK;

		// init buffer, must before newVdisk()
		this.buffer = new ArrayList<bufferLine>();
		for (int i = 0; i < virtualDisk.N_BUFFERLINE; i++) {
			this.buffer.add(new bufferLine(this.blockSize));
		}

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

		// init system table
		File systemTableFile = new File(this.systemTableLocation + File.separator + virtualDisk.CLASS_TABLE);
		if (!systemTableFile.exists()) {
			fileToolset.createFile(this.systemTableLocation + File.separator + virtualDisk.CLASS_TABLE);
		}
		systemTableFile = new File(this.systemTableLocation + File.separator + virtualDisk.ATTRIBUTE_TABLE);
		if (!systemTableFile.exists()) {
			fileToolset.createFile(this.systemTableLocation + File.separator + virtualDisk.ATTRIBUTE_TABLE);
		}
		systemTableFile = new File(this.systemTableLocation + File.separator + virtualDisk.DEPUTY_TABLE);
		if (!systemTableFile.exists()) {
			fileToolset.createFile(this.systemTableLocation + File.separator + virtualDisk.DEPUTY_TABLE);
		}
		systemTableFile = new File(this.systemTableLocation + File.separator + virtualDisk.DEPUTYRULE_TABLE);
		if (!systemTableFile.exists()) {
			fileToolset.createFile(this.systemTableLocation + File.separator + virtualDisk.DEPUTYRULE_TABLE);
		}
		systemTableFile = new File(this.systemTableLocation + File.separator + virtualDisk.OBJECT_TABLE);
		if (!systemTableFile.exists()) {
			fileToolset.createFile(this.systemTableLocation + File.separator + virtualDisk.OBJECT_TABLE);
		}
		systemTableFile = new File(this.systemTableLocation + File.separator + virtualDisk.SWITCHING_TABLE);
		if (!systemTableFile.exists()) {
			fileToolset.createFile(this.systemTableLocation + File.separator + virtualDisk.SWITCHING_TABLE);
		}
		systemTableFile = new File(this.systemTableLocation + File.separator + virtualDisk.BIPOINTER_TABLE);
		if (!systemTableFile.exists()) {
			fileToolset.createFile(this.systemTableLocation + File.separator + virtualDisk.BIPOINTER_TABLE);
		}

		// get system table
		getSystemTable();
	}

	@Override
	protected void finalize() throws Throwable {
		this.flushToDisk();
		super.finalize();
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
				temp = virtualDisk.short2ByteArray((short) virtualDisk.CONFIG_BLOCK_FLAG);
			} else {
				// init FAT entry as unused (0x0001)
				temp = virtualDisk.short2ByteArray(virtualDisk.int2UnsignedShort((short) virtualDisk.FREE_BLOCK_FLAG));
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

	/*
	 * get config from vdisk
	 * @Args:
	 *  None
	 * @Ret:
	 *  flag(boolean)	: whether get config successfully
	 */
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
	 * get systemTable from system table
	 * @Args:
	 *  None
	 * @Ret:
	 *  flag(boolean)	: whether get successfully
	 */
	private boolean getSystemTable() {
		if (!this.getClassTable()) {
			System.err.println("ERROR: get class table error!");
			return false;
		}
		if (!this.getAttributeTable()) {
			System.err.println("ERROR: get attribute table error!");
			return false;
		}
		if (!this.getDeputyTable()) {
			System.err.println("ERROR: get deputy table error!");
			return false;
		}
		if (!this.getDeputyRuleTable()) {
			System.err.println("ERROR: get deputyRule table error!");
			return false;
		}
		if (!this.getObjectTable()) {
			System.err.println("ERROR: get object table error!");
			return false;
		}
		if (!this.getSwitchingTable()) {
			System.err.println("ERROR: get switching table error!");
			return false;
		}
		if (!this.getBiPointerTable()) {
			System.err.println("ERROR: get biPointer table error!");
			return false;
		}
		return true;
	}

	/*
	 * flush this.systemClassTable into system table
	 * @Args:
	 *  None
	 * @Ret:
	 *  flag(boolean)	: whether flush successfully
	 */
	private boolean flushSystemTable() {
		if (!this.flushClassTable()) {
			System.err.println("ERROR: flush class table error!");
			return false;
		}
		if (!this.flushAttributeTable()) {
			System.err.println("ERROR: flush attribute table error!");
			return false;
		}
		if (!this.flushDeputyTable()) {
			System.err.println("ERROR: flush deputy table error!");
			return false;
		}
		if (!this.flushDeputyRuleTable()) {
			System.err.println("ERROR: flush deputyRule table error!");
			return false;
		}
		if (!this.flushObjectTable()) {
			System.err.println("ERROR: flush object table error!");
			return false;
		}
		if (!this.flushSwitchingTable()) {
			System.err.println("ERROR: flush switching table error!");
			return false;
		}
		if (!this.flushBiPointerTable()) {
			System.err.println("ERROR: flush biPointer table error!");
			return false;
		}
		return true;
	}

	/*
	 * get this.systemClassTable from system table
	 * @Args:
	 *  None
	 * @Ret:
	 *  flag(boolean)	: whether get successfully
	 */
	private boolean getClassTable() {
		ArrayList<String> temp;
		ArrayList<String> data = virtualDisk.decode(fileToolset.readFile(this.systemTableLocation + File.separator + virtualDisk.CLASS_TABLE));

		if ((data.size() % classTable.N_ATTR) != 0) {
			System.err.printf("ERROR: data.size()(%d) classTable.N_ATTR(%d) from getClassTable!\n", data.size(), classTable.N_ATTR);
			return false;
		} else if (data.size() == 0) {
			this.systemClassTable = new ArrayList<classTable>();
			return true;
		} else {
			this.systemClassTable = new ArrayList<classTable>();
			temp = new ArrayList<String>();
			for (int i = 0; i < data.size(); i++) {
				temp.add(data.get(i));
				if (((i + 1) % classTable.N_ATTR) == 0) {
					this.systemClassTable.add(new classTable(temp));
					// new ArrayList<String>
					temp = new ArrayList<String>();
				}
			}
			return true;
		}
	}

	/*
	 * flush this.systemClassTable into system table
	 * @Args:
	 *  None
	 * @Ret:
	 *  flag(boolean)	: whether flush successfully
	 */
	private boolean flushClassTable() {
		ArrayList<String> data, temp;

		data = new ArrayList<String>();
		for (int i = 0; i < this.systemClassTable.size(); i++) {
			temp = this.systemClassTable.get(i).class2StringList();
			for (int j = 0; j < classTable.N_ATTR; j++) {
				data.add(temp.get(j));
			}
		}

		return fileToolset.writeFile(this.systemTableLocation + File.separator + virtualDisk.CLASS_TABLE, virtualDisk.encode(data));
	}

	/*
	 * get this.systemAttributeTable from system table
	 * @Args:
	 *  None
	 * @Ret:
	 *  flag(boolean)	: whether get successfully
	 */
	private boolean getAttributeTable() {
		ArrayList<String> temp;
		ArrayList<String> data = virtualDisk.decode(fileToolset.readFile(this.systemTableLocation + File.separator + virtualDisk.ATTRIBUTE_TABLE));

		if ((data.size() % attributeTable.N_ATTR) != 0) {
			return false;
		} else if (data.size() == 0) {
			this.systemAttributeTable = new ArrayList<attributeTable>();
			return true;
		} else {
			this.systemAttributeTable = new ArrayList<attributeTable>();
			temp = new ArrayList<String>();
			for (int i = 0; i < data.size(); i++) {
				temp.add(data.get(i));
				if (((i + 1) % attributeTable.N_ATTR) == 0) {
					this.systemAttributeTable.add(new attributeTable(temp));
					// new ArrayList<String>
					temp = new ArrayList<String>();
				}
			}
			return true;
		}
	}

	/*
	 * flush this.systemAttributeTable into system table
	 * @Args:
	 *  None
	 * @Ret:
	 *  flag(boolean)	: whether flush successfully
	 */
	private boolean flushAttributeTable() {
		ArrayList<String> data, temp;

		data = new ArrayList<String>();
		for (int i = 0; i < this.systemAttributeTable.size(); i++) {
			temp = this.systemAttributeTable.get(i).class2StringList();
			for (int j = 0; j < attributeTable.N_ATTR; j++) {
				data.add(temp.get(j));
			}
		}

		return fileToolset.writeFile(this.systemTableLocation + File.separator + virtualDisk.ATTRIBUTE_TABLE, virtualDisk.encode(data));
	}

	/*
	 * get this.systemDeputyTable from system table
	 * @Args:
	 *  None
	 * @Ret:
	 *  flag(boolean)	: whether get successfully
	 */
	private boolean getDeputyTable() {
		ArrayList<String> temp;
		ArrayList<String> data = virtualDisk.decode(fileToolset.readFile(this.systemTableLocation + File.separator + virtualDisk.DEPUTY_TABLE));

		if ((data.size() % deputyTable.N_ATTR) != 0) {
			return false;
		} else if (data.size() == 0) {
			this.systemDeputyTable = new ArrayList<deputyTable>();
			return true;
		} else {
			this.systemDeputyTable = new ArrayList<deputyTable>();
			temp = new ArrayList<String>();
			for (int i = 0; i < data.size(); i++) {
				temp.add(data.get(i));
				if (((i + 1) % deputyTable.N_ATTR) == 0) {
					this.systemDeputyTable.add(new deputyTable(temp));
					// new ArrayList<String>
					temp = new ArrayList<String>();
				}
			}
			return true;
		}
	}

	/*
	 * flush this.systemDeputyTable into system table
	 * @Args:
	 *  None
	 * @Ret:
	 *  flag(boolean)	: whether flush successfully
	 */
	private boolean flushDeputyTable() {
		ArrayList<String> data, temp;

		data = new ArrayList<String>();
		for (int i = 0; i < this.systemDeputyTable.size(); i++) {
			temp = this.systemDeputyTable.get(i).class2StringList();
			for (int j = 0; j < deputyTable.N_ATTR; j++) {
				data.add(temp.get(j));
			}
		}

		return fileToolset.writeFile(this.systemTableLocation + File.separator + virtualDisk.DEPUTY_TABLE, virtualDisk.encode(data));
	}

	/*
	 * get this.systemDeputyRuleTable from system table
	 * @Args:
	 *  None
	 * @Ret:
	 *  flag(boolean)	: whether get successfully
	 */
	private boolean getDeputyRuleTable() {
		ArrayList<String> temp;
		ArrayList<String> data = virtualDisk.decode(fileToolset.readFile(this.systemTableLocation + File.separator + virtualDisk.DEPUTYRULE_TABLE));

		if ((data.size() % deputyRuleTable.N_ATTR) != 0) {
			return false;
		} else if (data.size() == 0) {
			this.systemDeputyRuleTable = new ArrayList<deputyRuleTable>();
			return true;
		} else {
			this.systemDeputyRuleTable = new ArrayList<deputyRuleTable>();
			temp = new ArrayList<String>();
			for (int i = 0; i < data.size(); i++) {
				temp.add(data.get(i));
				if (((i + 1) % deputyRuleTable.N_ATTR) == 0) {
					this.systemDeputyRuleTable.add(new deputyRuleTable(temp));
					// new ArrayList<String>
					temp = new ArrayList<String>();
				}
			}
			return true;
		}
	}

	/*
	 * flush this.systemDeputyRuleTable into system table
	 * @Args:
	 *  None
	 * @Ret:
	 *  flag(boolean)	: whether flush successfully
	 */
	private boolean flushDeputyRuleTable() {
		ArrayList<String> data, temp;

		data = new ArrayList<String>();
		for (int i = 0; i < this.systemDeputyRuleTable.size(); i++) {
			temp = this.systemDeputyRuleTable.get(i).class2StringList();
			for (int j = 0; j < deputyRuleTable.N_ATTR; j++) {
				data.add(temp.get(j));
			}
		}

		return fileToolset.writeFile(this.systemTableLocation + File.separator + virtualDisk.DEPUTYRULE_TABLE, virtualDisk.encode(data));
	}

	/*
	 * get this.systemObjectTable from system table
	 * @Args:
	 *  None
	 * @Ret:
	 *  flag(boolean)	: whether get successfully
	 */
	private boolean getObjectTable() {
		ArrayList<String> temp;
		ArrayList<String> data = virtualDisk.decode(fileToolset.readFile(this.systemTableLocation + File.separator + virtualDisk.OBJECT_TABLE));

		if ((data.size() % objectTable.N_ATTR) != 0) {
			return false;
		} else if (data.size() == 0) {
			this.systemObjectTable = new ArrayList<objectTable>();
			return true;
		} else {
			this.systemObjectTable = new ArrayList<objectTable>();
			temp = new ArrayList<String>();
			for (int i = 0; i < data.size(); i++) {
				temp.add(data.get(i));
				if (((i + 1) % objectTable.N_ATTR) == 0) {
					this.systemObjectTable.add(new objectTable(temp));
					// new ArrayList<String>
					temp = new ArrayList<String>();
				}
			}
			return true;
		}
	}

	/*
	 * flush this.systemObjectTable into system table
	 * @Args:
	 *  None
	 * @Ret:
	 *  flag(boolean)	: whether flush successfully
	 */
	private boolean flushObjectTable() {
		ArrayList<String> data, temp;

		data = new ArrayList<String>();
		for (int i = 0; i < this.systemObjectTable.size(); i++) {
			temp = this.systemObjectTable.get(i).class2StringList();
			for (int j = 0; j < objectTable.N_ATTR; j++) {
				data.add(temp.get(j));
			}
		}

		return fileToolset.writeFile(this.systemTableLocation + File.separator + virtualDisk.OBJECT_TABLE, virtualDisk.encode(data));
	}

	/*
	 * get this.systemSwitchingTable from system table
	 * @Args:
	 *  None
	 * @Ret:
	 *  flag(boolean)	: whether get successfully
	 */
	private boolean getSwitchingTable() {
		ArrayList<String> temp;
		ArrayList<String> data = virtualDisk.decode(fileToolset.readFile(this.systemTableLocation + File.separator + virtualDisk.SWITCHING_TABLE));

		if ((data.size() % switchingTable.N_ATTR) != 0) {
			return false;
		} else if (data.size() == 0) {
			this.systemSwitchingTable = new ArrayList<switchingTable>();
			return true;
		} else {
			this.systemSwitchingTable = new ArrayList<switchingTable>();
			temp = new ArrayList<String>();
			for (int i = 0; i < data.size(); i++) {
				temp.add(data.get(i));
				if (((i + 1) % switchingTable.N_ATTR) == 0) {
					this.systemSwitchingTable.add(new switchingTable(temp));
					// new ArrayList<String>
					temp = new ArrayList<String>();
				}
			}
			return true;
		}
	}

	/*
	 * flush this.systemSwitchingTable into system table
	 * @Args:
	 *  None
	 * @Ret:
	 *  flag(boolean)	: whether flush successfully
	 */
	private boolean flushSwitchingTable() {
		ArrayList<String> data, temp;

		data = new ArrayList<String>();
		for (int i = 0; i < this.systemSwitchingTable.size(); i++) {
			temp = this.systemSwitchingTable.get(i).class2StringList();
			for (int j = 0; j < switchingTable.N_ATTR; j++) {
				data.add(temp.get(j));
			}
		}

		return fileToolset.writeFile(this.systemTableLocation + File.separator + virtualDisk.SWITCHING_TABLE, virtualDisk.encode(data));
	}

	/*
	 * get this.systemBiPointerTable from system table
	 * @Args:
	 *  None
	 * @Ret:
	 *  flag(boolean)	: whether get successfully
	 */
	private boolean getBiPointerTable() {
		ArrayList<String> temp;
		ArrayList<String> data = virtualDisk.decode(fileToolset.readFile(this.systemTableLocation + File.separator + virtualDisk.BIPOINTER_TABLE));

		if ((data.size() % biPointerTable.N_ATTR) != 0) {
			return false;
		} else if (data.size() == 0) {
			this.systemBiPointerTable = new ArrayList<biPointerTable>();
			return true;
		} else {
			this.systemBiPointerTable = new ArrayList<biPointerTable>();
			temp = new ArrayList<String>();
			for (int i = 0; i < data.size(); i++) {
				temp.add(data.get(i));
				if (((i + 1) % biPointerTable.N_ATTR) == 0) {
					this.systemBiPointerTable.add(new biPointerTable(temp));
					// new ArrayList<String>
					temp = new ArrayList<String>();
				}
			}
			return true;
		}
	}

	/*
	 * flush this.systemBiPointerTable into system table
	 * @Args:
	 *  None
	 * @Ret:
	 *  flag(boolean)	: whether flush successfully
	 */
	private boolean flushBiPointerTable() {
		ArrayList<String> data, temp;

		data = new ArrayList<String>();
		for (int i = 0; i < this.systemBiPointerTable.size(); i++) {
			temp = this.systemBiPointerTable.get(i).class2StringList();
			for (int j = 0; j < biPointerTable.N_ATTR; j++) {
				data.add(temp.get(j));
			}
		}

		return fileToolset.writeFile(this.systemTableLocation + File.separator + virtualDisk.BIPOINTER_TABLE, virtualDisk.encode(data));
	}

	/*
	 * check whether n_block hit
	 * @Args:
	 *  n_block(int)		: n_block
	 * @Ret:
	 *  index(int)			: the index of bufferLine
	 */
	private int bufferHit(int n_block) {
		if (this.buffer == null) {
			System.err.printf("ERROR: (in virtualDisk.bufferHit) this.buffer does not exist!\n");
			return virtualDisk.MAX_INTEGER;
		} else if (n_block >= (this.diskSize / this.blockSize)) {
			return virtualDisk.MAX_INTEGER;
		} else {
			for (int i = 0; i < this.buffer.size(); i++) {
				if (this.buffer.get(i).n_block == n_block) {
					return i;
				}
			}
			return virtualDisk.MAX_INTEGER;
		}
	}

	/*
	 * find a free bufferLine
	 * @Args:
	 *  None
	 * @Ret:
	 *  index(int)		: the index of free bufferLine
	 */
	private int _findFreeBufferLine() {
		int i;

		if (this.buffer == null) {
			return virtualDisk.MAX_INTEGER;
		} else {
			for (i = 0; i < this.buffer.size(); i++) {
				if (this.buffer.get(i).isDirty == 0) {
					return i;
				}
			}

			return virtualDisk.MAX_INTEGER;
		}
	}

	/*
	 * write back bufferLine to vdisk
	 * @Args:
	 *  bufferLineIndex(int)		: the index of bufferLine which will be written back
	 * @Ret:
	 *  flag(boolean)				: whether write back successfully
	 */
	private boolean writeBackBufferLine(int bufferLineIndex) {
		if (bufferLineIndex >= virtualDisk.N_BUFFERLINE) {
			System.err.printf("ERROR: (in virtualDisk.writeBackBufferLine) bufferLineIndex(%d) to large!\n", bufferLineIndex);
			return false;
		} else if (this.buffer == null) {
			System.err.printf("ERROR: (in virtualDisk.writeBackBufferLine) this.buffer does not exist!\n");
			return false;
		} else {
			if (this.buffer.get(bufferLineIndex).isDirty == 0) {
				return true;
			} else {
				if (this.buffer.get(bufferLineIndex).buffer == null) {
					System.err.printf("ERROR: (in virtualDisk.writeBackBufferLine) bufferLineIndex(%d).buffer == null!\n", bufferLineIndex);
					return false;
				} else {
					this.buffer.get(bufferLineIndex).isDirty = 0;
					return this._write(this.buffer.get(bufferLineIndex).n_block, 0, this.buffer.get(bufferLineIndex).buffer, this.blockSize);
				}
			}
		}
	}

	/*
	 * read n_block to bufferLine
	 * @Args:
	 *  n_block(int)		: n_block to read
	 * @Ret:
	 *  flag(boolean)		: whether read n_block to bufferLine successfully
	 */
	private boolean readToBufferLine(int n_block) {
		int freeBufferLineIndex;

		if (this.buffer == null) {
			return false;
		} else {
			if (this.bufferHit(n_block) != virtualDisk.MAX_INTEGER) {
				return true;
			} else {
				freeBufferLineIndex = this._findFreeBufferLine();
				if (freeBufferLineIndex != virtualDisk.MAX_INTEGER) {
					this.buffer.get(freeBufferLineIndex).n_block = n_block;
					this.buffer.get(freeBufferLineIndex).isDirty = 0;
					this.buffer.get(freeBufferLineIndex).buffer = this._read(n_block, 0, this.blockSize);
					return true;
				} else {
					if (!this.writeBackBufferLine(n_block % virtualDisk.N_BUFFERLINE)) {
						return false;
					} else {
						freeBufferLineIndex = this._findFreeBufferLine();
						// no need to check again
						this.buffer.get(freeBufferLineIndex).n_block = n_block;
						this.buffer.get(freeBufferLineIndex).isDirty = 0;
						this.buffer.get(freeBufferLineIndex).buffer = this._read(n_block, 0, this.blockSize);
						return true;
					}
				}
			}
		}
	}

	/*
	 * flush bufferLine vdisk
	 * @Args:
	 *  None
	 * @Ret:
	 *  flag(boolean)		: whether flush bufferLine to vdisk successfully
	 */
	private boolean _flushToDisk() {
		if (this.buffer == null) {
			System.err.println("ERROR: (in virtualDisk._flushToDisk) this.buffer == null!");
			return true;
		} else {
			for (int i = 0; i < virtualDisk.N_BUFFERLINE; i++) {
				if (!this.writeBackBufferLine(i)) {
					System.err.printf("ERROR: (in virtualDisk._flushToDisk) bufferLine(%d) write back error!\n", i);
					return false;
				}
			}
			return true;
		}
	}

	/*
	 * write data to buffer, not vdisk
	 * @Args:
	 *  n_block(int)		: n_block to write
	 *  offset(int)			: offset to write
	 *  data(byte[])		: data to write
	 *  length(int)			: length to write
	 * @Ret:
	 *  flag(boolean)		: whether write successfully
	 */
	private boolean write(int n_block, int offset, byte[] data, int length) {
		int bufferLineIndex;
		byte[] left, right;

		bufferLineIndex = this.bufferHit(n_block);
		if (bufferLineIndex != virtualDisk.MAX_INTEGER) {
			if (offset + length <= this.blockSize) {
				if (!virtualDisk.byteArrayCopy(this.buffer.get(bufferLineIndex).buffer, data, offset)) {
					return false;
				} else {
					this.buffer.get(bufferLineIndex).isDirty = 1;
					return true;
				}
			} else {
				left = new byte[this.blockSize - offset];
				if (!virtualDisk.byteArrayIntercept(left, data, 0)) {
					return false;
				} else {
					// write n_block
					if (!virtualDisk.byteArrayCopy(this.buffer.get(bufferLineIndex).buffer, left, offset)) {
						return false;
					}
					this.buffer.get(bufferLineIndex).isDirty = 1;
					// n_block + 1
					bufferLineIndex = this.bufferHit(n_block + 1);
					if (bufferLineIndex != virtualDisk.MAX_INTEGER) {
						right = new byte[length + offset - this.blockSize];
						if (!virtualDisk.byteArrayIntercept(right, data, this.blockSize - offset)) {
							return false;
						} else {
							if (!virtualDisk.byteArrayCopy(this.buffer.get(bufferLineIndex).buffer, right, 0)) {
								return false;
							} else {
								this.buffer.get(bufferLineIndex).isDirty = 1;
								return true;
							}
						}
					} else {
						// not hit
						if (!this.readToBufferLine(n_block + 1)) {
							return false;
						} else {
							bufferLineIndex = this.bufferHit(n_block + 1);
							// no need to check again
							right = new byte[length + offset - this.blockSize];
							if (!virtualDisk.byteArrayIntercept(right, data, this.blockSize - offset)) {
								return false;
							} else {
								if (!virtualDisk.byteArrayCopy(this.buffer.get(bufferLineIndex).buffer, right, 0)) {
									return false;
								} else {
									this.buffer.get(bufferLineIndex).isDirty = 1;
									return true;
								}
							}
						}
					}
				}
			}
		} else {
			// not hit
			if (!this.readToBufferLine(n_block)) {
				return false;
			} else {
				bufferLineIndex = this.bufferHit(n_block);
				// no need to check again
				if (offset + length <= this.blockSize) {
					if (!virtualDisk.byteArrayCopy(this.buffer.get(bufferLineIndex).buffer, data, offset)) {
						return false;
					} else {
						this.buffer.get(bufferLineIndex).isDirty = 1;
						return true;
					}
				} else {
					left = new byte[this.blockSize - offset];
					if (!virtualDisk.byteArrayIntercept(left, data, 0)) {
						return false;
					} else {
						// write n_block
						if (!virtualDisk.byteArrayCopy(this.buffer.get(bufferLineIndex).buffer, left, offset)) {
							return false;
						}
						this.buffer.get(bufferLineIndex).isDirty = 1;
						// n_block + 1
						bufferLineIndex = this.bufferHit(n_block + 1);
						if (bufferLineIndex != virtualDisk.MAX_INTEGER) {
							right = new byte[length + offset - this.blockSize];
							if (!virtualDisk.byteArrayIntercept(right, data, this.blockSize - offset)) {
								return false;
							} else {
								if (!virtualDisk.byteArrayCopy(this.buffer.get(bufferLineIndex).buffer, right, 0)) {
									return false;
								} else {
									this.buffer.get(bufferLineIndex).isDirty = 1;
									return true;
								}
							}
						} else {
							// not hit
							if (!this.readToBufferLine(n_block + 1)) {
								return false;
							} else {
								bufferLineIndex = this.bufferHit(n_block + 1);
								// no need to check again
								right = new byte[length + offset - this.blockSize];
								if (!virtualDisk.byteArrayIntercept(right, data, this.blockSize - offset)) {
									return false;
								} else {
									if (!virtualDisk.byteArrayCopy(this.buffer.get(bufferLineIndex).buffer, right, 0)) {
										return false;
									} else {
										this.buffer.get(bufferLineIndex).isDirty = 1;
										return true;
									}
								}
							}
						}
					}
				}
			}
		}
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
	private boolean _write(int n_block, int offset, byte[] data, int length) {
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
	 * read data from buffer, not vdisk
	 * @Args:
	 *  n_block(int)	: the number of start block
	 *  offset(int)		: the offset in the block
	 *  length(int)		: valid data in data array
	 * @Ret:
	 *  data(byte[])	: read data
	 */
	private byte[] read(int n_block, int offset, int length) {
		int bufferLineIndex;
		byte[] data, left, right;

		bufferLineIndex = this.bufferHit(n_block);
		if (bufferLineIndex != virtualDisk.MAX_INTEGER) {
			if (offset + length <= this.blockSize) {
				data = new byte[length];
				if (!virtualDisk.byteArrayIntercept(data, this.buffer.get(bufferLineIndex).buffer, offset)) {
					return null;
				} else {
					return data;
				}
			} else {
				left = new byte[this.blockSize - offset];
				if (!virtualDisk.byteArrayIntercept(left, this.buffer.get(bufferLineIndex).buffer, offset)) {
					return null;
				} else {
					bufferLineIndex = this.bufferHit(n_block + 1);
					if (bufferLineIndex != virtualDisk.MAX_INTEGER) {
						right = new byte[length + offset - this.blockSize];
						if (!virtualDisk.byteArrayIntercept(right, this.buffer.get(bufferLineIndex).buffer, 0)) {
							return null;
						} else {
							data = new byte[length];
							if (!virtualDisk.byteArrayCopy(data, left, 0)) {
								return null;
							}
							if (!virtualDisk.byteArrayCopy(data, right, this.blockSize - offset)) {
								return null;
							}
							return data;
						}
					} else {
						// not hit
						if (!this.readToBufferLine(n_block + 1)) {
							return null;
						} else {
							bufferLineIndex = this.bufferHit(n_block + 1);
							// no need to check again
							right = new byte[length + offset - this.blockSize];
							if (!virtualDisk.byteArrayIntercept(right, this.buffer.get(bufferLineIndex).buffer, 0)) {
								return null;
							} else {
								data = new byte[length];
								if (!virtualDisk.byteArrayCopy(data, left, 0)) {
									return null;
								}
								if (!virtualDisk.byteArrayCopy(data, right, this.blockSize - offset)) {
									return null;
								}
								return data;
							}
						}
					}
				}
			}
		} else {
			// not hit
			if (!this.readToBufferLine(n_block)) {
				return null;
			} else {
				bufferLineIndex = this.bufferHit(n_block);
				// no need to check again
				if (offset + length <= this.blockSize) {
					data = new byte[length];
					if (!virtualDisk.byteArrayIntercept(data, this.buffer.get(bufferLineIndex).buffer, offset)) {
						return null;
					} else {
						return data;
					}
				} else {
					left = new byte[this.blockSize - offset];
					if (!virtualDisk.byteArrayIntercept(left, this.buffer.get(bufferLineIndex).buffer, offset)) {
						return null;
					} else {
						bufferLineIndex = this.bufferHit(n_block + 1);
						if (bufferLineIndex != virtualDisk.MAX_INTEGER) {
							right = new byte[length + offset - this.blockSize];
							if (!virtualDisk.byteArrayIntercept(right, this.buffer.get(bufferLineIndex).buffer, 0)) {
								return null;
							} else {
								data = new byte[length];
								if (!virtualDisk.byteArrayCopy(data, left, 0)) {
									return null;
								}
								if (!virtualDisk.byteArrayCopy(data, right, this.blockSize - offset)) {
									return null;
								}
								return data;
							}
						} else {
							// not hit
							if (!this.readToBufferLine(n_block + 1)) {
								return null;
							} else {
								bufferLineIndex = this.bufferHit(n_block + 1);
								// no need to check again
								right = new byte[length + offset - this.blockSize];
								if (!virtualDisk.byteArrayIntercept(right, this.buffer.get(bufferLineIndex).buffer, 0)) {
									return null;
								} else {
									data = new byte[length];
									if (!virtualDisk.byteArrayCopy(data, left, 0)) {
										return null;
									}
									if (!virtualDisk.byteArrayCopy(data, right, this.blockSize - offset)) {
										return null;
									}
									return data;
								}
							}
						}
					}
				}
			}
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
	private byte[] _read(int n_block, int offset, int length) {
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

	/*
	 * encode ArrayList<String> to String, then write system table
	 * @Args:
	 *  data(ArrayList<String>)	: source data
	 * @Ret:
	 *  result(String)			: encode string
	 */
	public static String encode(ArrayList<String> data) {
		int length, tempLength;
		String temp, result = "";

		if (data == null) {
			return "";
		} else {
			length = data.size();

			for (int i = 0; i < length; i++) {
				temp = data.get(i);

				if (temp != null) {
					tempLength = temp.length();
				} else {
					tempLength = 0;
				}
				for (int j = 0; j < tempLength; j++) {
					if (temp.charAt(j) == ';') {
						result += "!;";
					} else if (temp.charAt(j) == '!') {
						result += "!!";
					}else{
						result += temp.charAt(j);
					}
				}
				result += ";";
			}

			return result;
		}
	}

	/*
	 * decode String to ArrayList<String>, after read system table
	 * @Args:
	 *  code(String)				: source data
	 * @Ret:
	 *  result(ArrayList<String>)	: decode string list
	 */
	public static ArrayList<String> decode(String code) {
		String temp;
		ArrayList<String> result;


		try{
			result = new ArrayList<>();
            for (int i = 0; i < code.length(); i++) {
				temp = "";
				for (; code.charAt(i) != ';'; i++) {
					if (code.charAt(i) == '!') {
						i++;
					}
					temp += code.charAt(i);
				}
				result.add(temp);
			}
			return result;
		}catch (Exception ex){
			return new ArrayList<>();
		}
	}

	/*
	 * get a free block from FAT-like table
	 * @Args:
	 *  None
	 * @Ret:
	 *  n_block(int)	: n_block of free
	 */
	private int getFreeBlock() {
		int n_block, blockEntry;

		for (n_block = this.dataOffset; n_block < (this.diskSize / this.blockSize); n_block++) {
			blockEntry = virtualDisk.unsignedShort2Int(virtualDisk.byteArray2Short(this.read(this.getFATEntryBlock(n_block), this.getFATEntryOffset(n_block), this.entrySize)));
			if (blockEntry == virtualDisk.FREE_BLOCK_FLAG) {
				break;
			}
		}

		if (n_block == (this.diskSize / this.blockSize)) {
			n_block = virtualDisk.CONFIG_BLOCK_FLAG;
		}

		return n_block;
	}

	/*
	 * set the next block from FAT-like table
	 * @Args:
	 *  n_block(int)	: source block
	 *  n_nextBlock(int): next block
	 * @Ret:
	 *  flag(boolean)	: whether set successfully
	 */
	private boolean setNextBlock(int n_block, int n_nextBlock) {
		byte[] data;

		if ((n_block < this.dataOffset) || (n_nextBlock < this.dataOffset)) {
			return false;
		} else {
			if (this.getNextBlock(n_block) != n_block) {
				if (n_block != n_nextBlock) {
					return false;
				}
			}
			data = virtualDisk.short2ByteArray(virtualDisk.int2UnsignedShort(n_nextBlock));
			if (!this.write(this.getFATEntryBlock(n_block), this.getFATEntryOffset(n_block), data, this.entrySize)) {
				return false;
			}
			if (!this.write(this.getFATEntryBlock(n_nextBlock), this.getFATEntryOffset(n_nextBlock), data, this.entrySize)) {
				// should reverse here
				return false;
			}
			return true;
		}
	}

	/*
	 * get the next block from FAT-like table
	 * @Args:
	 *  n_block(int)	: source block
	 * @Ret:
	 *  n_nextBlock(int): next block
	 */
	private int getNextBlock(int n_block) {
		int n_nextBlock;

		n_nextBlock = virtualDisk.unsignedShort2Int(virtualDisk.byteArray2Short(this.read(this.getFATEntryBlock(n_block), this.getFATEntryOffset(n_block), this.entrySize)));

		return n_nextBlock;
	}

	/*
	 * delete block FAT-like entry & its children
	 * @Args:
	 *  n_block(int)	: delete block
	 * @Ret:
	 *  flag(boolean)	: whether delete successfully
	 */
	private boolean deleteBlock(int n_block) {
		int n_nextBlock;

		if (n_block < this.dataOffset) {
			return false;
		} else {
			n_nextBlock = this.getNextBlock(n_block);
			// System.out.printf("(%d, %d)\n", n_block, n_nextBlock);

			if (n_nextBlock != n_block) {
				if (!this.deleteBlock(n_nextBlock)) {
					return false;
				}
			}
			return this.deleteOneBlock(n_block);
		}
	}

	/*
	 * delete one block FAT-like entry
	 * @Args:
	 *  n_block(int)	: delete block
	 * @Ret:
	 *  flag(boolean)	: whether delete successfully
	 */
	private boolean deleteOneBlock(int n_block) {
		byte[] data;
		int block, offset;

		if (n_block < this.dataOffset) {
			return false;
		} else {
			data = virtualDisk.short2ByteArray(virtualDisk.int2UnsignedShort(virtualDisk.FREE_BLOCK_FLAG));
			block = this.getFATEntryBlock(n_block);
			offset = this.getFATEntryOffset(n_block);

			return this.write(block, offset, data, this.entrySize);
		}
	}

	/*
	 * get FAT entry offset
	 * @Args:
	 *  n_block(int)	: number of block
	 * @Ret:
	 *  offset(int)		: offset
	 */
	private int getFATEntryOffset(int n_block) {
		int offset;

		offset = (n_block * this.entrySize) % this.blockSize;

		return offset;
	}

	/*
	 * get FAT entry block
	 * @Args:
	 *  n_block(int)	: number of block
	 * @Ret:
	 *  block(int)		: block
	 */
	private int getFATEntryBlock(int n_block) {
		int block;

		block = virtualDisk.N_OF_CONFIG_BLOCK + ((n_block * this.entrySize) / this.blockSize);

		return block;
	}

	/*
	 * get attr list of class
	 * @Args:
	 *  src(classStruct)			: src class struct
	 * @Ret:
	 *  data(ArrayList<String>)		: corresponding attr list
	 */
	public ArrayList<Attribute> getRealAttributeList(classStruct src) {
		Attribute attr;
		ArrayList<Attribute> data;

		if (src == null) {
			System.out.println("ERROR: (in virtualDisk.getTypeList) src == null!");
			return null;
		} else {
			data = new ArrayList<Attribute>();
			for (int i = 0; i < src.attrList.size(); i++) {
				attr = src.attrList.get(i);

				if ((attr.expression != null) && (!attr.expression.equals(""))) {
					// virtualAttribute
					continue;
				}

				data.add(attr);
			}
			return data;
		}
	}

	/*
	 * get type list of class
	 * @Args:
	 *  src(classStruct)			: src class struct
	 * @Ret:
	 *  data(ArrayList<Integer>)	: corresponding type list
	 */
	private ArrayList<Integer> getTypeList(classStruct src) {
		Attribute attr;
		ArrayList<Integer> data;

		if (src == null) {
			System.out.println("ERROR: (in virtualDisk.getTypeList) src == null!");
			return null;
		} else {
			data = new ArrayList<Integer>();
			for (int i = 0; i < src.attrList.size(); i++) {
				attr = src.attrList.get(i);

				if ((attr.expression != null) && (!attr.expression.equals(""))) {
					// virtualAttribute
					continue;
				}

				if (attr.type == virtualDisk.ATTRTYPE_INTEGER) {
					data.add(virtualDisk.ATTRTYPE_INTEGER);
				} else if (attr.type == virtualDisk.ATTRTYPE_STRING) {
					data.add(virtualDisk.ATTRTYPE_STRING);
				} else {
					System.out.println("ERROR: (in virtualDisk.getTypeList) unknown attribute type!");
					return null;
				}
			}
			return data;
		}
	}

	/*
	 * get length list of class
	 * @Args:
	 *  src(classStruct)			: src class struct
	 * @Ret:
	 *  data(ArrayList<Integer>)	: corresponding length list
	 */
	private ArrayList<Integer> getLengthList(classStruct src) {
		Attribute attr;
		ArrayList<Integer> data;

		if (src == null) {
			System.out.println("ERROR: (in virtualDisk.getLengthList) src == null!");
			return null;
		} else {
			data = new ArrayList<Integer>();
			for (int i = 0; i < src.attrList.size(); i++) {
				attr = src.attrList.get(i);

				if ((attr.expression != null) && (!attr.expression.equals(""))) {
					// virtualAttribute
					continue;
				}

				if (attr.type == virtualDisk.ATTRTYPE_INTEGER) {
					data.add(virtualDisk.BYTES_OF_INTEGER_DATA);
				} else if (attr.type == virtualDisk.ATTRTYPE_STRING) {
					data.add(virtualDisk.BYTES_OF_STRING_DATA);
				} else {
					System.out.println("ERROR: (in virtualDisk.getLengthList) unknown attribute type!");
					return null;
				}
			}
			return data;
		}
	}

	/*
	 * write one tuple into class data, forget 1 data > 1 block
	 * @Args:
	 *  lengthList(ArrayList<Integer>)	: attr-length list
	 *  typeList(ArrayList<Integer>)	: attr-type list
	 *  n_block(int)					: n_block of vdisk
	 *  offset(int)						: offset in block
	 *  src(ArrayList<String>)			: tuple to write
	 * @Ret:
	 *  flag(boolean)					: whether write tuple successfully
	 */
	private boolean writeOneTuple(ArrayList<Integer> lengthList, ArrayList<Integer> typeList, int n_block, int offset, ArrayList<String> src) {
		int length = 0, count = 0;
		int n_nextBlock;
		byte[] temp, data, left, right;

		if ((lengthList.size() != typeList.size()) || (lengthList.size() != src.size()) || (src.size() != typeList.size())) {
			return false;
		} else if (src == null) {
			return false;
		} else {
			// get one tuple data
			for (int i = 0; i < lengthList.size(); i++) {
				length += lengthList.get(i).intValue();
			}
			data = new byte[length];
			for (int i = 0; i < lengthList.size(); i++) {
				if (typeList.get(i).intValue() == virtualDisk.ATTRTYPE_STRING) {
					temp = new byte[lengthList.get(i).intValue()];
					// clear up temp
					clearByteArray(temp);
					if (!virtualDisk.byteArrayCopy(temp, virtualDisk.string2ByteArray(src.get(i)), 0)) {
							return false;
					}
				} else if (typeList.get(i).intValue() == virtualDisk.ATTRTYPE_INTEGER) {
					if (!virtualDisk.canParseInt(src.get(i))) {
						System.err.println("ERROR: cannot parse string(" + src.get(i) + ") to int!");
						return false;
					} else {
						temp = virtualDisk.int2ByteArray(Integer.parseInt(src.get(i)));
					}
				} else {
					return false;
				}
				if (!virtualDisk.byteArrayCopy(data, temp, count)) {
					return false;
				}
				count += lengthList.get(i).intValue();
			}
			// check offset + length
			if (length + offset > this.blockSize) {
				left = new byte[this.blockSize - offset];
				right = new byte[length + offset - this.blockSize];

				if (!byteArrayIntercept(left, data, 0)) {
					return false;
				}
				if (!byteArrayIntercept(right, data, this.blockSize - offset)) {
					return false;
				}
				// need read second block
				n_nextBlock = this.getNextBlock(n_block);
				if ((n_nextBlock == n_block) || (n_nextBlock == virtualDisk.FREE_BLOCK_FLAG) || (n_nextBlock == virtualDisk.CONFIG_BLOCK_FLAG)) {
					n_nextBlock = this.getFreeBlock();
					if (n_nextBlock == virtualDisk.CONFIG_BLOCK_FLAG) {
						return false;
					}
					if (!this.setNextBlock(n_block, n_nextBlock)) {
						return false;
					}
				}
				// write to vdisk
				if (!this.write(n_block, offset, left, left.length)) {
					return false;
				}
				if (!this.write(n_nextBlock, 0, right, right.length)) {
					return false;
				}

				return true;
			} else {
				if (!this.write(n_block, offset, data, data.length)) {
					return false;
				}

				return true;
			}
		}
	}

	/*
	 * read one tuple from class data, forget 1 data > 1 block
	 * @Args:
	 *  lengthList(ArrayList<Integer>)	: attr-length list
	 *  typeList(ArrayList<Integer>)	: attr-type list
	 *  n_block(int)					: n_block of vdisk
	 *  offset(int)						: offset in block
	 * @Ret:
	 *  result(ArrayList<String>)		: tuple data
	 */
	private ArrayList<String> readOneTuple(ArrayList<Integer> lengthList, ArrayList<Integer> typeList, int n_block, int offset) {
		int length = 0, count = 0, stringLength;
		int n_nextBlock;
		byte[] left, right, data, temp, tempString;
		ArrayList<String> result;

		if (lengthList.size() != typeList.size()) {
			System.err.println("ERROR: (in virtualDisk.readOneTuple) the size of lengthList not equals the size of typeList!");
			return null;
		} else {
			result = new ArrayList<String>();
			// get one tuple data
			for (int i = 0; i < lengthList.size(); i++) {
				length += lengthList.get(i).intValue();
			}
			data = new byte[length];
			if (length + offset > this.blockSize) {
				// need read second block
				n_nextBlock = this.getNextBlock(n_block);
				if ((n_nextBlock == n_block) || (n_nextBlock == virtualDisk.FREE_BLOCK_FLAG) || (n_nextBlock == virtualDisk.CONFIG_BLOCK_FLAG)) {
					return null;
				}
				left = this.read(n_block, offset, this.blockSize - offset);
				right = this.read(n_nextBlock, 0, length + offset - this.blockSize);
				if (!virtualDisk.byteArrayCopy(data, left, 0)) {
					return null;
				}
				if (!virtualDisk.byteArrayCopy(data, right, this.blockSize - offset)) {
					return null;
				}
			} else {
				data = this.read(n_block, offset, length);
			}

			for (int i = 0; i < lengthList.size(); i++) {
				temp = new byte[lengthList.get(i).intValue()];
				if (!virtualDisk.byteArrayIntercept(temp, data, count)) {
					System.err.println("ERROR: (in virtualDisk.readOneTuple) byteArrayIntercept fail!");
					return null;
				}
				count += temp.length;
				if (typeList.get(i).intValue() == virtualDisk.ATTRTYPE_STRING) {
					for (stringLength = 0; stringLength < virtualDisk.BYTES_OF_STRING_DATA; stringLength++) {
						if (temp[stringLength] == ((byte) 0x00)) {
							break;
						}
					}
					tempString = new byte[stringLength];
					if (!virtualDisk.byteArrayIntercept(tempString, temp, 0)) {
						System.err.println("ERROR: (in virtualDisk.readOneTuple) byteArrayIntercept fail!");
						return null;
					}
					result.add(virtualDisk.byteArray2String(tempString));
				} else if (typeList.get(i).intValue() == virtualDisk.ATTRTYPE_INTEGER) {
					result.add(Integer.toString(virtualDisk.byteArray2Int(temp)));
				} else {
					System.err.println("ERROR: (in virtualDisk.readOneTuple) unknown attr type!");
					return null;
				}
			}

			return result;
		}
	}

	/*
	 * string can parse to int
	 * @Args:
	 *  src(String)		: source string
	 * @Ret:
	 *  flag(boolean)	: whether can string parse to int
	 */
	public static boolean canParseInt(String src) {
		if (src == null) {
			return false;
		} else {
			return src.matches("^(\\-|\\+)?\\d+");
		}
	}

	/*
	 * convert byte[] to hex string
	 * @Args:
	 *  src(byte[])		: source byte[]
	 * @Ret:
	 *  data(String)	: hex string
	 */
	public static String byteArray2HexString(byte[] src) {
		StringBuffer stringBuffer;
		String data;

		if (src == null) {
			return null;
		} else {
			stringBuffer = new StringBuffer(src.length);
			for (int i = 0; i < src.length; i++) {
				data = Integer.toHexString(0xff & src[i]);
				if (data.length() < 2) {
					stringBuffer.append(0);
				}
				stringBuffer.append(data.toUpperCase());
			}

			return stringBuffer.toString();
		}
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
		if ((des == null) || (src == null)) {
			System.err.println("ERROR: src or des is null!");
			return false;
		} else if (des.length + offset > src.length) {
			System.err.printf("ERROR: des.length(%d) + offset(%d) >= src.length(%d)!\n", des.length, offset, src.length);
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
		if ((des == null) || (src == null)) {
			return false;
		}else if (des.length < src.length + offset) {
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
		if (src == null) {
			return;
		} else {
			for (int i = 0; i < src.length; i++) {
				src[i] = 0;
			}
			return;
		}
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

		if (src == null) {
			return 0;
		} else {
			data = ((src[3] << 24) & 0xff000000) + ((src[2] << 16) & 0xff0000) + ((src[1] << 8) & 0xff00) + (src[0] & 0xff);

			return data;
		}
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

		if (src == null) {
			return ((short) 0x0000);
		} else {
			data = (short) (((src[1] << 8) & 0xff00) + (src[0] & 0xff));

			return data;
		}
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

	/*
	 * convert unsigned short to int
	 * @Args:
	 *  src(short)		: source short
	 * @Ret:
	 *  data(int)		: corresponding int
	 */
	public static int unsignedShort2Int(short src) {
		int data;

		data = ((int) src) & 0xffff;

		return data;
	}

	/*
	 * convert int to unsigned short
	 * @Args:
	 *  src(int)		: source int
	 * @Ret:
	 *  data(short)		: corresponding unsigned short
	 */
	public static short int2UnsignedShort(int src) {
		short data;

		data = (short) (src & 0xffff);

		return data;
	}

	/*
	 * ocupy one tuple
	 * @Args:
	 *  n_block(int)	: config block
	 *  index(int)		: tuple index
	 * @Ret:
	 *  flag(boolean)	: whether ocupy successfully
	 */
	private boolean ocupyOneTuple(int n_block, int index) {
		byte[] data = new byte[1];

		if ((this.getNextBlock(n_block) == virtualDisk.CONFIG_BLOCK_FLAG) || (this.getNextBlock(n_block) == virtualDisk.FREE_BLOCK_FLAG)) {
			return false;
		} else if (index >= (virtualDisk.BITS_OF_BYTE * this.blockSize)) {
			System.err.println("ERROR: index to large!");
			return false;
		} else {
			data = this.read(n_block, index / virtualDisk.BITS_OF_BYTE, 1);
			if ((index % virtualDisk.BITS_OF_BYTE) == 0) {
				data[0] = (byte) (data[0] | 0x01);
			} else if ((index % virtualDisk.BITS_OF_BYTE) == 1) {
				data[0] = (byte) (data[0] | 0x02);
			} else if ((index % virtualDisk.BITS_OF_BYTE) == 2) {
				data[0] = (byte) (data[0] | 0x04);
			} else if ((index % virtualDisk.BITS_OF_BYTE) == 3) {
				data[0] = (byte) (data[0] | 0x08);
			} else if ((index % virtualDisk.BITS_OF_BYTE) == 4) {
				data[0] = (byte) (data[0] | 0x10);
			} else if ((index % virtualDisk.BITS_OF_BYTE) == 5) {
				data[0] = (byte) (data[0] | 0x20);
			} else if ((index % virtualDisk.BITS_OF_BYTE) == 6) {
				data[0] = (byte) (data[0] | 0x40);
			} else if ((index % virtualDisk.BITS_OF_BYTE) == 7) {
				data[0] = (byte) (data[0] | 0x80);
			} else {
				return false;
			}
			return this.write(n_block, index / virtualDisk.BITS_OF_BYTE, data, 1);
		}
	}

	/*
	 * free one tuple
	 * @Args:
	 *  n_block(int)	: config block
	 *  index(int)		: tuple index
	 * @Ret:
	 *  flag(boolean)	: whether free successfully
	 */
	private boolean freeOneTuple(int n_block, int index) {
		byte[] data;

		if ((this.getNextBlock(n_block) == virtualDisk.CONFIG_BLOCK_FLAG) || (this.getNextBlock(n_block) == virtualDisk.FREE_BLOCK_FLAG)) {
			return false;
		} else if (index >= (virtualDisk.BITS_OF_BYTE * this.blockSize)) {
			System.err.println("ERROR: index to large!");
			return false;
		} else {
			data = this.read(n_block, index / virtualDisk.BITS_OF_BYTE, 1);
			if ((index % virtualDisk.BITS_OF_BYTE) == 0) {
				data[0] = (byte) (data[0] & 0xfe);
			} else if ((index % virtualDisk.BITS_OF_BYTE) == 1) {
				data[0] = (byte) (data[0] & 0xfd);
			} else if ((index % virtualDisk.BITS_OF_BYTE) == 2) {
				data[0] = (byte) (data[0] & 0xfb);
			} else if ((index % virtualDisk.BITS_OF_BYTE) == 3) {
				data[0] = (byte) (data[0] & 0xf7);
			} else if ((index % virtualDisk.BITS_OF_BYTE) == 4) {
				data[0] = (byte) (data[0] & 0xef);
			} else if ((index % virtualDisk.BITS_OF_BYTE) == 5) {
				data[0] = (byte) (data[0] & 0xdf);
			} else if ((index % virtualDisk.BITS_OF_BYTE) == 6) {
				data[0] = (byte) (data[0] & 0xbf);
			} else if ((index % virtualDisk.BITS_OF_BYTE) == 7) {
				data[0] = (byte) (data[0] & 0x7f);
			} else {
				return false;
			}
			return this.write(n_block, index / virtualDisk.BITS_OF_BYTE, data, 1);
		}
	}

	/*
	 * get free tuple index
	 * @Args:
	 *  n_block(int)	: config block
	 * @Ret:
	 *  index(int)		: free tuple index
	 */
	private int getFreeTuple(int n_block) {
		byte[] data;

		if ((this.getNextBlock(n_block) == virtualDisk.CONFIG_BLOCK_FLAG) || (this.getNextBlock(n_block) == virtualDisk.FREE_BLOCK_FLAG)) {
			return virtualDisk.MAX_INTEGER;
		} else {
			for (int offset = 0; offset < this.blockSize; offset++) {
				data = this.read(n_block, offset, 1);
				System.out.println("data[0]: " + data[0]);
				if (data[0] == (byte) 0xff) {
					continue;
				} else {
					if ((data[0] & 0x01) == 0x00) {
						// 0 free
						return (offset * virtualDisk.BITS_OF_BYTE);
					} else if ((data[0] & 0x02) == 0x00) {
						// 1 free
						return (offset * virtualDisk.BITS_OF_BYTE) + 1;
					} else if ((data[0] & 0x04) == 0x00) {
						// 2 free
						return (offset * virtualDisk.BITS_OF_BYTE) + 2;
					} else if ((data[0] & 0x08) == 0x00) {
						// 3 free
						return (offset * virtualDisk.BITS_OF_BYTE) + 3;
					} else if ((data[0] & 0x10) == 0x00) {
						// 4 free
						return (offset * virtualDisk.BITS_OF_BYTE) + 4;
					} else if ((data[0] & 0x20) == 0x00) {
						// 5 free
						return (offset * virtualDisk.BITS_OF_BYTE) + 5;
					} else if ((data[0] & 0x40) == 0x00) {
						// 6 free
						return (offset * virtualDisk.BITS_OF_BYTE) + 6;
					} else if ((data[0] & 0x80) == 0x00) {
						// 7 free
						return (offset * virtualDisk.BITS_OF_BYTE) + 7;
					} else {
						return virtualDisk.MAX_INTEGER;
					}
				}
			}
			return virtualDisk.MAX_INTEGER;
		}
	}

	/*
	 * get next ocupied tuple
	 * @Args:
	 *  n_block(int)	: config block
	 *  index(int)		: current index
	 * @Ret:
	 *  nextIndex(int)	: nextOcupiedTuple index
	 */
	private int getNextOcupiedTuple(int n_block, int index) {
		byte[] data;
		int nextIndex;

		if ((this.getNextBlock(n_block) == virtualDisk.CONFIG_BLOCK_FLAG) || (this.getNextBlock(n_block) == virtualDisk.FREE_BLOCK_FLAG)) {
			return virtualDisk.MAX_INTEGER;
		} else if (index >= (virtualDisk.BITS_OF_BYTE * this.blockSize)) {
			System.err.println("ERROR: index to large!");
			return virtualDisk.MAX_INTEGER;
		} else if ((index + 1) == (virtualDisk.BITS_OF_BYTE * this.blockSize)) {
			System.err.println("ERROR: this is the last one(temp not support extend slub!");
			return virtualDisk.MAX_INTEGER;
		} else {
			nextIndex = index + 1;
			data = this.read(n_block, nextIndex / virtualDisk.BITS_OF_BYTE, 1);
			for (; nextIndex < (virtualDisk.BITS_OF_BYTE * this.blockSize); nextIndex++) {			
				if ((nextIndex % virtualDisk.BITS_OF_BYTE) == 0) {
					// update data
					data = this.read(n_block, nextIndex / virtualDisk.BITS_OF_BYTE, 1);
				}
				if ((data[0] >> (nextIndex % virtualDisk.BITS_OF_BYTE) & 0x01) == 0x01) {
					// ocupied
					return nextIndex;
				}
			}
			return virtualDisk.MAX_INTEGER;
		}
	}

	/*
	 * convert fakeBlockOffset to realBlockOffset, before Next()
	 * @Args:
	 *  None
	 * @Ret:
	 *  realBlockOffset(int)	: real block offset
	 */
	private int fakeOffset2RealOffset() {
		int classId, tupleLength = 0, realBlockOffset;
		ArrayList<Integer> lengthList;
		classStruct classStruct;

		classId = this.getClassId(this.currClassName);
		classStruct = this.getClassStruct(this.currClassName);
		lengthList = this.getLengthList(classStruct);
		// get tuple length
		for (int i = 0; i < lengthList.size(); i++) {
			tupleLength += lengthList.get(i).intValue();
		}
		if (tupleLength == 0) {
			return virtualDisk.MAX_INTEGER;
		}
		// calculate realBlockOffset
		realBlockOffset = (((this.fakeBlockNum * virtualDisk.PAGESIZE) + this.fakeBlockOffset) * tupleLength) % this.blockSize;

		return realBlockOffset;
	}

	/*
	 * convert fakeBlockNum to realBlockNum, before Next()
	 * @Args:
	 *  None
	 * @Ret:
	 *  realBlockNum(int)	: real block num
	 */
	private int fakeBlock2RealBlock() {
		int n_block, n_nextBlock, classId, tupleLength = 0, realBlockNum, realBlockOffset;
		ArrayList<Integer> lengthList;
		classStruct classStruct;

		n_block = n_nextBlock = virtualDisk.MAX_INTEGER;
		classId = this.getClassId(this.currClassName);
		classStruct = this.getClassStruct(this.currClassName);
		lengthList = this.getLengthList(classStruct);
		// get tuple length
		for (int i = 0; i < lengthList.size(); i++) {
			tupleLength += lengthList.get(i).intValue();
		}
		if (tupleLength == 0) {
			return virtualDisk.MAX_INTEGER;
		}
		// calculate realBlockNum
		realBlockNum = (((this.fakeBlockNum * virtualDisk.PAGESIZE) + this.fakeBlockOffset) * tupleLength) / this.blockSize;
		// get start n_block
		for (int i = 0; i < this.systemObjectTable.size(); i++) {
			if (this.systemObjectTable.get(i).classId == classId) {
				n_block = this.systemObjectTable.get(i).blockId;
				break;
			}
		}
		if ((n_block == virtualDisk.MAX_INTEGER) || (n_block == virtualDisk.CONFIG_BLOCK_FLAG) || (n_block == virtualDisk.FREE_BLOCK_FLAG)) {
			return virtualDisk.MAX_INTEGER;
		} else {
			for (int i = 0; i <= realBlockNum; i++) {
				n_nextBlock = this.getNextBlock(n_block);
				if ((n_nextBlock == virtualDisk.MAX_INTEGER) || (n_nextBlock == virtualDisk.CONFIG_BLOCK_FLAG) || (n_nextBlock == virtualDisk.FREE_BLOCK_FLAG)) {
					return virtualDisk.MAX_INTEGER;
				}
				n_block = n_nextBlock;
			}
			return n_block;
		}
	}

	/*
	 * get free id from table
	 * @Args:
	 *  tableName(String)	: system table name
	 * @Ret:
	 *  freeId(int)			: free id
	 */
	private int getFreeId(String tableName) {
		int freeId, i;

		if (tableName.equals(virtualDisk.CLASS_TABLE)) {
			for (freeId = 0; freeId < virtualDisk.MAX_INTEGER; freeId++) {
				if (this.systemClassTable.size() == 0) {
					freeId = 0;
					break;
				} else {
					for (i = 0; i < this.systemClassTable.size(); i++) {
						if (this.systemClassTable.get(i).classId == freeId) {
							break;
						}
					}
					if (i == this.systemClassTable.size()) {
						break;
					} else {
						continue;
					}
				}
			}

			return freeId;
		} else if (tableName.equals(virtualDisk.ATTRIBUTE_TABLE)) {
			for (freeId = 0; freeId <= virtualDisk.MAX_INTEGER; freeId++) {
				if (this.systemAttributeTable.size() == 0) {
					freeId = 0;
					break;
				} else {
					for (i = 0; i < this.systemAttributeTable.size(); i++) {
						if (this.systemAttributeTable.get(i).attrId == freeId) {
							break;
						}
					}
					if (i == this.systemAttributeTable.size()) {
						break;
					} else {
						continue;
					}
				}
			}

			return freeId;
		} else if (tableName.equals(virtualDisk.DEPUTYRULE_TABLE)) {
			for (freeId = 0; freeId <= virtualDisk.MAX_INTEGER; freeId++) {
				if (this.systemDeputyRuleTable.size() == 0) {
					freeId = 0;
					break;
				} else {
					for (i = 0; i < this.systemDeputyRuleTable.size(); i++) {
						if (this.systemDeputyRuleTable.get(i).deputyRuleId == freeId) {
							break;
						}
					}
					if (i == this.systemDeputyRuleTable.size()) {
						break;
					} else {
						continue;
					}
				}
			}

			return freeId;
		} else {
			return virtualDisk.MAX_INTEGER;
		}
	}

	/*
	 * get className by classId
	 * @Args:
	 *  classId(int)		: class id
	 * @Ret:
	 *  className(String)	: corresponding class name
	 */
	private String getClassName(int classId) {
		for (int i = 0; i < this.systemClassTable.size(); i++) {
			if (this.systemClassTable.get(i).classId == classId) {
				return this.systemClassTable.get(i).className;
			}
		}
		return null;
	}

	/*
	 * get classId by className
	 * @Args:
	 *  className(String)	: class name
	 * @Ret:
	 *  classId(int)		: corresponding class id
	 */
	private int getClassId(String className) {
		if (!this.existClass(className)) {
			return virtualDisk.MAX_INTEGER;
		} else {
			for (int i = 0; i < this.systemClassTable.size(); i++) {
				if (this.systemClassTable.get(i).className.equals(className)) {
					return this.systemClassTable.get(i).classId;
				}
			}
			return virtualDisk.MAX_INTEGER;
		}
	}

	/*
	 * save class struct to system table
	 * @Args:
	 *  className(String)			: class name
	 *  classStruct(classStruct)	: class struct
	 * @Ret:
	 *  flag(boolean)				: whether save class struct successfully
	 */
	private boolean saveClassStruct(String className, classStruct classStruct) {
		byte[] data;
		int classId, selectClassId, attrId, deputyRuleId, n_block, n_nextBlock, i;
		ArrayList<String> temp;
		classTable tempClassTable;
		attributeTable tempAttributeTable;
		deputyTable tempDeputyTable;
		deputyRuleTable tempDeputyRuleTable;
		objectTable tempObjectTable;
		switchingTable tempSwitchingTable;
		biPointerTable tempBiPointerTable;
		Attribute attribute;

		// if already exist, delete class
		if (this.existClass(className)) {
			if (!this.deleteClass(className)) {
				return false;
			}
		}

		// set classTable
		temp = new ArrayList<String>();
		temp.add(classStruct.name);
		classId = this.getFreeId(virtualDisk.CLASS_TABLE);
		temp.add(Integer.toString(classId));
		temp.add(Integer.toString(classStruct.attrList.size()));
		if ((classStruct.parent == null) || (classStruct.parent.equals(""))) {
			temp.add(Integer.toString(classTable.originClass));
		} else {
			temp.add(Integer.toString(classTable.deputyClass));
		}
		// init classTable
		tempClassTable = new classTable(temp);
		this.systemClassTable.add(tempClassTable);
		// set attribute table
		temp = new ArrayList<String>();
		for (int j = 0; j < classStruct.attrList.size(); j++) {
			temp = new ArrayList<String>();
			attribute = classStruct.attrList.get(j);

			if ((attribute.expression == null) || (attribute.expression.equals(""))) {
				// real attribute

				temp.add(Integer.toString(classId));
				attrId = this.getFreeId(virtualDisk.ATTRIBUTE_TABLE);
				temp.add(Integer.toString(attrId));
				temp.add(attribute.name);
				temp.add(Integer.toString(attribute.type));
				// not virtual attr
				temp.add(Integer.toString(attributeTable.notVirtual));
				// init attributeTable
				tempAttributeTable = new attributeTable(temp);
				this.systemAttributeTable.add(tempAttributeTable);
			} else {
				// virtual attribute

				temp.add(Integer.toString(classId));
				attrId = this.getFreeId(virtualDisk.ATTRIBUTE_TABLE);
				temp.add(Integer.toString(attrId));
				temp.add(attribute.name);
				temp.add(Integer.toString(Attribute.INT));
				temp.add(Integer.toString(attributeTable.isVirtual));
				// init attributeTable
				tempAttributeTable = new attributeTable(temp);
				this.systemAttributeTable.add(tempAttributeTable);
				// set switching table
				temp = new ArrayList<String>();

				temp.add(Integer.toString(attrId));
				temp.add(attribute.expression);
				tempSwitchingTable = new switchingTable(temp);
				this.systemSwitchingTable.add(tempSwitchingTable);
			}
		}
		// set deputy table
		temp = new ArrayList<String>();
		if ((classStruct.parent == null) || (classStruct.parent.equals(""))) {
			// origin class
		} else {
			selectClassId = this.getClassId(classStruct.parent);
			if (selectClassId == virtualDisk.MAX_INTEGER) {
				return false;
			} else {
				temp.add(Integer.toString(selectClassId));
				temp.add(Integer.toString(classId));
				deputyRuleId = this.getFreeId(virtualDisk.DEPUTYRULE_TABLE);
				temp.add(Integer.toString(deputyRuleId));
				// init deputyTable
				tempDeputyTable = new deputyTable(temp);
				this.systemDeputyTable.add(tempDeputyTable);
				// set deputy rule table
				temp = new ArrayList<String>();
				temp.add(Integer.toString(deputyRuleId));
				temp.add(classStruct.condition);
				// init deputyRuleTable
				tempDeputyRuleTable = new deputyRuleTable(temp);
				this.systemDeputyRuleTable.add(tempDeputyRuleTable);
			}
		}
		// set object table
		temp = new ArrayList<String>();

		temp.add(Integer.toString(classId));
		temp.add(Integer.toString(0));
		n_block = this.getFreeBlock();
		if (n_block == virtualDisk.CONFIG_BLOCK_FLAG) {
			return false;
		} else {
			if (!this.setNextBlock(n_block, n_block)) {
				return false;
			} else {
				temp.add(Integer.toString(n_block));
				// set class config page
				data = new byte[this.blockSize];
				virtualDisk.clearByteArray(data);
				if (!this.write(n_block, 0, data, this.blockSize)) {
					System.err.println("ERROR: (in virtualDisk.saveClassStruct) write fail!");
					return false;
				}
				n_nextBlock = this.getFreeBlock();
				if (n_nextBlock == virtualDisk.CONFIG_BLOCK_FLAG) {
					return false;
				} else {
					if (!this.setNextBlock(n_block, n_nextBlock)) {
						return false;
					}
				}
			}
		}
		temp.add(Integer.toString(0));
		// init objectTable
		tempObjectTable = new objectTable(temp);
		this.systemObjectTable.add(tempObjectTable);

		// flush system table to disk
		return this.flushSystemTable();
	}

	/*
	 * delete class from system table
	 * @Args:
	 *  className(String)	: class name
	 * @Ret:
	 *  flag(boolean)		: whether delete successfully
	 */
	private boolean deleteClass(String className) {
		int classId, attrId, deputyRuleId, n_block;
		classTable tempClassTable;
		attributeTable tempAttributeTable;
		deputyTable tempDeputyTable;
		deputyRuleTable tempDeputyRuleTable;
		objectTable tempObjectTable;
		switchingTable tempSwitchingTable;
		biPointerTable tempBiPointerTable;

		if (!this.existClass(className)) {
			return false;
		} else {
			classId = this.getClassId(className);

			// init n_block
			n_block = virtualDisk.MAX_INTEGER;

			// delete object table
			for (int i = 0; i < this.systemObjectTable.size(); i++) {
				tempObjectTable = this.systemObjectTable.get(i);
				if (tempObjectTable.classId == classId) {
					this.systemObjectTable.remove(i);
					n_block = tempObjectTable.blockId;
					break;
				}
			}
			if (n_block == virtualDisk.MAX_INTEGER) {
				return false;
			}
			if (!this.deleteBlock(n_block)) {
				return false;
			}
			// delete attribute table
			for (int i = 0; i < this.systemAttributeTable.size(); i++) {
				tempAttributeTable = this.systemAttributeTable.get(i);
				if (tempAttributeTable.classId == classId) {
					if (tempAttributeTable.isDeputy == attributeTable.isVirtual) {
						attrId = tempAttributeTable.attrId;

						// delete switching table
						for (int j = 0; j < this.systemSwitchingTable.size(); j++) {
							tempSwitchingTable = this.systemSwitchingTable.get(j);

							if (tempSwitchingTable.attrId == attrId) {
								this.systemSwitchingTable.remove(j);
								break;
							}
						}
					}
					this.systemAttributeTable.remove(i);
					// no break
				}
			}
			// delete class table
			for (int i = 0; i < this.systemClassTable.size(); i++) {
				tempClassTable = this.systemClassTable.get(i);
				if (tempClassTable.classType == classTable.deputyClass) {
					for (int j = 0; j < this.systemDeputyTable.size(); j++) {
						tempDeputyTable = this.systemDeputyTable.get(j);
						if (tempDeputyTable.deputyId == classId) {
							deputyRuleId = tempDeputyTable.deputyRuleId;
							for (int k = 0; k < this.systemDeputyRuleTable.size(); k++) {
								tempDeputyRuleTable = this.systemDeputyRuleTable.get(k);
								if (tempDeputyRuleTable.deputyRuleId == deputyRuleId) {
									this.systemDeputyRuleTable.remove(k);
									break;
								}
							}
							this.systemDeputyTable.remove(j);
							break;
						}
					}
				}
				if (tempClassTable.classId == classId) {
					this.systemClassTable.remove(i);
					break;
				}
			}

			// delete biPointerTable
			for (int i = 0; i < this.systemBiPointerTable.size(); i++) {
				tempBiPointerTable = this.systemBiPointerTable.get(i);
				if ((tempBiPointerTable.classId == classId) || (tempBiPointerTable.deputyClassId == classId)) {
					// the son of classStruct will be handled by SQLExecutor, not in this function
					this.systemBiPointerTable.remove(i);
					// no break
				}
			}

			// flush system table to disk
			return this.flushSystemTable();
		}
	}

	/*
	 * whether exist class
	 * @Args:
	 *  className(String)	: class name
	 * @Ret:
	 *  flag(boolean)		: whether exist class
	 */
	public boolean existClass(String className) {
		if (this.systemClassTable == null) {
			if (!this.getClassTable()) {
				System.err.println("ERROR: get class table error!");
				return false;
			}
		}

		for (int i = 0; i < this.systemClassTable.size(); i++) {
			if (this.systemClassTable.get(i).className.equals(className)) {
				return true;
			}
		}

		return false;
	}

	/*
	 * get classStruct
	 * @Args:
	 *  className(String)			: class name
	 * @Ret:
	 *  classStruct(ClassStruct)	: corresponding class struct
	 */
	public classStruct getClassStruct(String className) {
		int classId, deputyClassId, deputyRuleId, attrId;
		classStruct classStruct;
		classTable tempClassTable;
		attributeTable tempAttributeTable;
		deputyTable tempDeputyTable;
		deputyRuleTable tempDeputyRuleTable;
		objectTable tempObjectTable;
		switchingTable tempSwitchingTable;
		biPointerTable tempBiPointerTable;
		String deputyClassName;
		Attribute attribute;

		if (!this.existClass(className)) {
			return null;
		} else {
			classStruct = new classStruct();

			// init classId
			classId = virtualDisk.MAX_INTEGER;
			deputyClassId = virtualDisk.MAX_INTEGER;

			// get from class table
			classStruct.parent = "";
			classStruct.condition = "";
			for (int i = 0; i < this.systemClassTable.size(); i++) {
				tempClassTable = this.systemClassTable.get(i);
				if (tempClassTable.className.equals(className)) {
					classStruct.name = className;
					// deputy class
					if (tempClassTable.classType == classTable.deputyClass) {
						deputyClassId = tempClassTable.classId;
						for (int j = 0; j < this.systemDeputyTable.size(); j++) {
							tempDeputyTable = this.systemDeputyTable.get(j);
							if (tempDeputyTable.deputyId == deputyClassId) {
								classId = tempDeputyTable.originId;
								deputyRuleId = tempDeputyTable.deputyRuleId;
								for (int k = 0; k < this.systemDeputyRuleTable.size(); k++) {
									tempDeputyRuleTable = this.systemDeputyRuleTable.get(k);
									if (tempDeputyRuleTable.deputyRuleId == deputyRuleId) {
										classStruct.condition = tempDeputyRuleTable.deputyRule;
										classStruct.parent = this.getClassName(classId);
										break;
									}
								}
								break;
							}
						}
						// set back
						classId = deputyClassId;
					} else {
						classId = tempClassTable.classId;
					}
					break;
				}
			}
			if (classId == virtualDisk.MAX_INTEGER) {
				return null;
			}
			// get from deputy table
			classStruct.children = new ArrayList<String>();
			for (int i = 0; i < this.systemDeputyTable.size(); i++) {
				tempDeputyTable = this.systemDeputyTable.get(i);
				if (tempDeputyTable.originId == classId) {
					deputyClassName = this.getClassName(tempDeputyTable.deputyId);
					if (deputyClassName == null) {
						continue;
					} else {
						classStruct.children.add(deputyClassName);
					}
					// no break, get all children
				}
			}
			// get from attribute table
			classStruct.attrList = new ArrayList<Attribute>();
			for (int i = 0; i < this.systemAttributeTable.size(); i++) {
				tempAttributeTable = this.systemAttributeTable.get(i);
				if (tempAttributeTable.classId == classId) {
					attrId = tempAttributeTable.attrId;
					// virtual attr
					if (tempAttributeTable.isDeputy == attributeTable.isVirtual) {
						attribute = new Attribute();
						attribute.name = tempAttributeTable.attrName;
						attribute.type = tempAttributeTable.attrType;
						for (int j = 0; j < this.systemSwitchingTable.size(); j++) {
							tempSwitchingTable = this.systemSwitchingTable.get(j);
							if (tempSwitchingTable.attrId == attrId) {
								attribute.expression = tempSwitchingTable.rule;
								break;
							}
						}
						attribute.className = classStruct.name;

						classStruct.attrList.add(attribute);
					} else if (tempAttributeTable.isDeputy == attributeTable.notVirtual) {
						attribute = new Attribute();

						attribute.name = tempAttributeTable.attrName;
						attribute.type = tempAttributeTable.attrType;
						attribute.expression = "";
						attribute.className = classStruct.name;

						classStruct.attrList.add(attribute);
					} else {
						return null;
					}
					// no break, get all attribute
				}
			}

			return classStruct;
		}
	}

	/*
	 * set classStruct
	 * @Args:
	 *  className(String)			: class name
	 *  classStruct(classStruct)	: corresponding class struct
	 * @Ret:
	 *  flag(boolean)				: whether set classStruct successfully
	 */
	public boolean setClassStruct(String className, classStruct classStruct) {
		// if already exist, just return, deleteClass will do the pointer delete
		if (this.existClass(className)) {
			System.out.println("ERROR: (in virtualDisk.setClassStruct) className already exist!");
			return false;
		} else {
			return this.saveClassStruct(className, classStruct);
		}
	}

	/*
	 * create class
	 * @Args:
	 *  className(String)			: class name
	 *  classStruct(classStruct)	: corresponding class struct
	 * @Ret:
	 *  flag(boolean)				: whether create class successfully
	 */
	public boolean createClass(String className, classStruct classStruct) {
		if (this.existClass(className)) {
			return false;
		} else {
			return this.saveClassStruct(className, classStruct);
		}
	}

	/*
	 * drop class
	 * @Args:
	 *  className(String)	: class name
	 * @Ret:
	 *  flag(boolean)		: whether drop class successfully
	 */
	public boolean dropClass(String className) {
		return this.deleteClass(className);
	}

	/*
	 * initial vdisk.currClassName
	 * @Args:
	 *  className(String)		: class name
	 * @Ret:
	 *  None
	 */
	public void initial(String className) {
		// flushToDisk();
		this.currClassName = className;
		this.fakeBlockNum = 0;
		this.fakeBlockOffset = 0;
	}

	/*
	 * initial vdisk.currClassName
	 * @Args:
	 *  className(String)		: class name
	 *  _fakeBlockNum(int)		: fake block num
	 *  _fakeBlockOffset(int)	: fake block offset
	 * @Ret:
	 *  None
	 */
	public void initial(String className, int _fakeBlockNum, int _fakeBlockOffset) {
		// flushToDisk();
		this.currClassName = className;
		this.fakeBlockNum = _fakeBlockNum;
		this.fakeBlockOffset = _fakeBlockOffset;
	}

	/*
	 * flush buffer data to disk
	 * @Args:
	 *  None
	 * @Ret:
	 *  flag(boolean)		: whether flush successfully
	 */
	public boolean flushToDisk() {
		return this._flushToDisk();
	}

	/*
	 * get tupleBiPointerList
	 * @Args:
	 *  classStruct(classStruct)				: classStruct
	 *  objectId(int)							: objectId
	 * @Ret:
	 *  tupleBiPointerList(ArrayList<String>)	: tupleBiPointerList
	 */
	private ArrayList<String> getTupleBiPointerList(classStruct classStruct, int objectId) {
		int i, j, classId, deputyClassId;
		biPointerTable tempBiPointerTable;
		ArrayList<String> tupleBiPointerList = new ArrayList<String>();

		if ((classStruct == null) || (objectId == virtualDisk.MAX_INTEGER)) {
			System.out.printf("ERROR: (in virtualDisk.getTupleBiPointerList) ((classStruct == null) || (objectId(%d) == virtualDisk.MAX_INTEGER))!\n", objectId);
			return null;
		}

		// get classId
		classId = this.getClassId(classStruct.name);
		if (classId == virtualDisk.MAX_INTEGER) {
			System.out.println("ERROR: (in virtualDisk.getTupleBiPointerList) (classId == virtualDisk.MAX_INTEGER)!");
			return null;
		}

		// remove all deleted children biPointer
		if (!this.removeDeletedChildrenBiPointerTable(classStruct)) {
			System.out.println("ERROR: (in virtualDisk.getTupleBiPointerList) remove all deleted children biPointer fail!");
			return null;
		}

		// get current children biPointer
		for (i = 0; i < classStruct.children.size(); i++) {
			deputyClassId = this.getClassId(classStruct.children.get(i));
			if (deputyClassId == virtualDisk.MAX_INTEGER) {
				System.out.println("ERROR: (in virtualDisk.getTupleBiPointerList) (deputyClassId == virtualDisk.MAX_INTEGER)!");
				return null;
			}
			// get corresponding biPointer
			for (j = 0; j < this.systemBiPointerTable.size(); j++) {
				tempBiPointerTable = this.systemBiPointerTable.get(j);
				if ((tempBiPointerTable.classId == classId) && (tempBiPointerTable.objectId == objectId) && (tempBiPointerTable.deputyClassId == deputyClassId)) {
					tupleBiPointerList.add(Integer.toString(tempBiPointerTable.deputyObjectId));
					break;
				}
			}
			if (j == this.systemBiPointerTable.size()) {
				// not find, add ""
				tupleBiPointerList.add("");
			}
		}

		return tupleBiPointerList;
	}

	/*
	 * get next tuple
	 * @Args:
	 *  None
	 * @Ret:
	 *  result(ArrayList<String>)	: next data tuple
	 */
	public ArrayList<String> Next() {
		int realBlockNum, realBlockOffset, index, lastIndex, n_block, classId;
		classStruct classStruct;
		ArrayList<Integer> lengthList, typeList;
		ArrayList<String> result, tupleBiPointerList;

		classId = this.getClassId(this.currClassName);
		classStruct = this.getClassStruct(this.currClassName);
		// get first block
		n_block = virtualDisk.MAX_INTEGER;
		for (int i = 0; i < this.systemObjectTable.size() ; i++) {
			if (this.systemObjectTable.get(i).classId == classId) {
				n_block = this.systemObjectTable.get(i).blockId;
				break;
			}
		}
		// valid check
		if ((n_block == virtualDisk.MAX_INTEGER) || (n_block == virtualDisk.CONFIG_BLOCK_FLAG) || (n_block == virtualDisk.FREE_BLOCK_FLAG)) {
			return null;
		}

		// whether current Offset valid, update fakeBlock
		if (this.fakeBlockOffset == virtualDisk.PAGESIZE) {
			this.fakeBlockOffset = 0;
			this.fakeBlockNum += 1;
		}
		lastIndex = (this.fakeBlockNum * virtualDisk.PAGESIZE) + this.fakeBlockOffset - 1;
		index = this.getNextOcupiedTuple(n_block, lastIndex);
		if (index == virtualDisk.MAX_INTEGER) {
			return null;
		} else if (index > lastIndex + 1) {
			this.fakeBlockNum = index / virtualDisk.PAGESIZE;
			this.fakeBlockOffset = index % virtualDisk.PAGESIZE;
		}	

		// calculate offset & block
		realBlockNum = this.fakeBlock2RealBlock();
		realBlockOffset = this.fakeOffset2RealOffset();
		// valid check
		if (realBlockNum == virtualDisk.MAX_INTEGER) {
			return null;
		}
		if (realBlockOffset == virtualDisk.MAX_INTEGER) {
			return null;
		}
		lengthList = this.getLengthList(classStruct);
		typeList = this.getTypeList(classStruct);
		// valid check, null pointer
		if ((typeList == null) || (lengthList == null)) {
			return null;
		}
		result = this.readOneTuple(lengthList, typeList, realBlockNum, realBlockOffset);

		// add biPointer to head
		tupleBiPointerList = this.getTupleBiPointerList(classStruct, (this.fakeBlockNum * virtualDisk.PAGESIZE) + this.fakeBlockOffset);
		if (tupleBiPointerList == null) {
			System.out.println("ERROR: (in virtualDisk.Next) tupleBiPointerList == null!");
			return null;
		}
		result.add(0, virtualDisk.encode(tupleBiPointerList));

		return result;
	}

	/*
	 * get vdisk total offset(index)
	 * @Args:
	 *  None
	 * @Ret:
	 *  offset(int)			: total offset
	 */
	public int getOffset() {
		return this.fakeBlockNum * virtualDisk.PAGESIZE + this.fakeBlockOffset;
	}

	/*
	 * remove deleted children's biPointerTable
	 * @Args:
	 *  classStruct(classStruct)		: classStruct
	 * @Ret:
	 *  flag(boolean)					: whether remove biPointerTable successfully
	 */
	private boolean removeDeletedChildrenBiPointerTable(classStruct classStruct) {
		int i, j, classId, childrenClassId;
		biPointerTable tempBiPointerTable;
		ArrayList<Integer> childrenClassIdList;

		if (classStruct == null) {
			System.out.println("ERROR: (in virtualDisk.removeDeletedChildrenBiPointerTable) classStruct == null!");
			return false;
		}
		
		// get classId
		classId = this.getClassId(classStruct.name);
		if (classId == virtualDisk.MAX_INTEGER) {
			System.out.println("ERROR: (in virtualDisk.removeDeletedChildrenBiPointerTable) classId == virtualDisk.MAX_INTEGER!");
			return false;
		}

		// get all childrenClassIdList
		childrenClassIdList = new ArrayList<Integer>();
		if (classStruct.children != null) {
			for (i = 0; i < classStruct.children.size(); i++) {
				childrenClassId = this.getClassId(classStruct.children.get(i));
				if (childrenClassId == virtualDisk.MAX_INTEGER) {
					System.out.println("ERROR: (in virtualDisk.removeDeletedChildrenBiPointerTable) childrenClassId == virtualDisk.MAX_INTEGER!");
					return false;
				} else {
					childrenClassIdList.add(childrenClassId);
				}
			}
		}
		// remove all deleted children biPointer
		for (i = 0; i < this.systemBiPointerTable.size(); i++) {
			tempBiPointerTable = this.systemBiPointerTable.get(i);
			if (tempBiPointerTable.classId == classId) {
				// check deputyClassId
				for (j = 0; j < childrenClassIdList.size(); j++) {
					if (tempBiPointerTable.deputyClassId == childrenClassIdList.get(j).intValue()) {
						break;
					}
				}
				if (j == childrenClassIdList.size()) {
					// not match, need delete
					this.systemBiPointerTable.remove(i);
				}
			}
		}

		return true;
	}

	/*
	 * update biPointerTable
	 * @Args:
	 *  classStruct(classStruct)	: classStruct
	 *  tupleBiPointer(String)		: tuple biPointer(encoded)
	 * @Ret:
	 *  flag(boolean)				: whether update biPointerTable successfully
	 */
	private boolean updateBiPointerTable(classStruct classStruct, String tupleBiPointer) {
		int i, j, parentClassId, parentObjectId, classId, childrenClassId, objectId, deputyObjectId;
		ArrayList<Integer> childrenClassIdList;
		ArrayList<String> tupleBiPointerList;
		biPointerTable tempBiPointerTable;

		if ((classStruct == null) || (tupleBiPointer == null)) {
			System.out.println("ERROR: (in virtualDisk.updateBiPointerTable) ((classStruct == null) || (tupleBiPointer == null))!");
			return false;
		}
		// get classId && decode tupleBiPointer
		classId = this.getClassId(classStruct.name);
		if (classId == virtualDisk.MAX_INTEGER) {
			System.out.println("ERROR: (in virtualDisk.updateBiPointerTable) classId == virtualDisk.MAX_INTEGER!");
			return false;
		}
		tupleBiPointerList = virtualDisk.decode(tupleBiPointer);
		if (tupleBiPointerList.size() - 1 != classStruct.children.size()) {
			System.out.println("ERROR: (in virtualDisk.updateBiPointerTable) (tupleBiPointerList.size() - 1 != classStruct.children.size())!");
			return false;
		}
		// get all childrenClassIdList
		childrenClassIdList = new ArrayList<Integer>();
		if (classStruct.children != null) {
			for (i = 0; i < classStruct.children.size(); i++) {
				childrenClassId = this.getClassId(classStruct.children.get(i));
				if (childrenClassId == virtualDisk.MAX_INTEGER) {
					System.out.println("ERROR: (in virtualDisk.updateBiPointerTable) childrenClassId == virtualDisk.MAX_INTEGER!");
					return false;
				} else {
					childrenClassIdList.add(childrenClassId);
				}
			}
		}
		// remove all deleted children biPointer
		if (!this.removeDeletedChildrenBiPointerTable(classStruct)) {
			System.out.println("ERROR: (in virtualDisk.updateBiPointerTable) remove all deleted children biPointer fail!");
			return false;
		}
		// update rest biPointer && insert new children biPointer
		objectId = (this.fakeBlockNum * virtualDisk.PAGESIZE) + this.fakeBlockOffset;
		// update biPointer point to parent
		// check whether parent exists
		if ((classStruct.parent != null) && (!classStruct.parent.equals(""))) {
			// parent exists
			parentClassId = this.getClassId(classStruct.parent);
			if (parentClassId == virtualDisk.MAX_INTEGER) {
				System.out.println("ERROR: (in virtualDisk.updateBiPointerTable) (parentClassId == virtualDisk.MAX_INTEGER)!");
				return false;
			}
			if (!virtualDisk.canParseInt(tupleBiPointerList.get(0))) {
				System.out.printf("ERROR: (in virtualDisk.updateBiPointerTable) cannot parse String(%s) to int!\n", tupleBiPointerList.get(0));
				return false;
			}
			parentObjectId = Integer.parseInt(tupleBiPointerList.get(0));
			for (j = 0; j < this.systemBiPointerTable.size(); j++) {
				tempBiPointerTable = this.systemBiPointerTable.get(j);
				if ((tempBiPointerTable.classId == parentClassId) && (tempBiPointerTable.objectId == parentObjectId) && (tempBiPointerTable.deputyClassId == classId)) {
					// only need to update deputyObjectId
					tempBiPointerTable.deputyObjectId = objectId;
					break;
				}
			}
			// no match, create a new biPointerTable
			tempBiPointerTable = new biPointerTable();
			tempBiPointerTable.classId = parentClassId;
			tempBiPointerTable.objectId = parentObjectId;
			tempBiPointerTable.deputyClassId = classId;
			tempBiPointerTable.deputyObjectId = objectId;

			this.systemBiPointerTable.add(tempBiPointerTable);
		}
		for (i = 1; i < tupleBiPointerList.size(); i++) {
			if (!virtualDisk.canParseInt(tupleBiPointerList.get(i))) {
				System.out.printf("ERROR: (in virtualDisk.updateBiPointerTable) cannot parse String(%s) to int!\n", tupleBiPointerList.get(i));
				return false;
			}
			deputyObjectId = Integer.parseInt(tupleBiPointerList.get(i));
			// get children classId
			childrenClassId = this.getClassId(classStruct.children.get(i - 1));
			for (j = 0; j < this.systemBiPointerTable.size(); j++) {
				tempBiPointerTable = this.systemBiPointerTable.get(j);
				if ((tempBiPointerTable.classId == classId) && (tempBiPointerTable.objectId == objectId) && (tempBiPointerTable.deputyClassId == childrenClassId)) {
					// only need to update deputyObjectId
					tempBiPointerTable.deputyObjectId = deputyObjectId;
					break;
				}
			}
			// no match, create a new biPointerTable
			tempBiPointerTable = new biPointerTable();
			tempBiPointerTable.classId = classId;
			tempBiPointerTable.objectId = objectId;
			tempBiPointerTable.deputyClassId = childrenClassId;
			tempBiPointerTable.deputyObjectId = deputyObjectId;

			this.systemBiPointerTable.add(tempBiPointerTable);
		}

		if (!this.flushBiPointerTable()) {
			System.out.println("ERROR: (in virtualDisk.updateBiPointerTable) flush biPointerTable fail!");
			return false;
		}

		return true;
	}
				

	/*
	 * insert tuple to class
	 * @Args:
	 *  className(String)			: class name
	 *  tuple(ArrayList<String>)	: data tuple
	 * @Ret:
	 *  flag(boolean)				: whether insert tuple successfully
	 */
	public boolean insert(String className, ArrayList<String> tuple) {
		int realBlockNum, realBlockOffset, index, n_block, classId;
		classStruct classStruct;
		String tupleBiPointer;
		ArrayList<Integer> lengthList, typeList;

		// avoid className not match this.currClassName
		this.flushToDisk();
		this.initial(className);
		classId = this.getClassId(this.currClassName);
		if (classId == virtualDisk.MAX_INTEGER) {
			System.out.println("ERROR: (in virtualDisk.insert) classId == virtualDisk.MAX_INTEGER!");
			return false;
		}
		classStruct = this.getClassStruct(this.currClassName);
		if (classStruct == null) {
			System.err.println("ERROR: (in virtualDisk.insert) classStruct == null!");
			return false;
		}
		// remove first element from tuple
		tupleBiPointer = tuple.remove(0);
		// get first block
		n_block = virtualDisk.MAX_INTEGER;
		for (int i = 0; i < this.systemObjectTable.size() ; i++) {
			if (this.systemObjectTable.get(i).classId == classId) {
				n_block = this.systemObjectTable.get(i).blockId;
				break;
			}
		}
		// valid check
		if ((n_block == virtualDisk.MAX_INTEGER) || (n_block == virtualDisk.CONFIG_BLOCK_FLAG) || (n_block == virtualDisk.FREE_BLOCK_FLAG)) {
			System.err.println("ERROR: (in virtualDisk.insert) n_block == virtualDisk.MAX_INTEGER!");
			return false;
		}

		index = this.getFreeTuple(n_block);
		if (!ocupyOneTuple(n_block, index)) {
			System.err.println("ERROR: (in virtualDisk.insert) ocupyOneTuple() fail!");
			return false;
		}
		// valid check
		if (index == virtualDisk.MAX_INTEGER) {
			System.err.println("ERROR: (in virtualDisk.insert) index == virtualDisk.MAX_INTEGER!");
			return false;
		} else {
			this.fakeBlockNum = index / virtualDisk.PAGESIZE;
			this.fakeBlockOffset = index % virtualDisk.PAGESIZE;
		}

		// calculate offset & block
		realBlockNum = this.fakeBlock2RealBlock();
		realBlockOffset = this.fakeOffset2RealOffset();
		// valid check
		if (realBlockNum == virtualDisk.MAX_INTEGER) {
			System.err.println("ERROR: (in virtualDisk.insert) realBlockNum == virtualDisk.MAX_INTEGER!");
			return false;
		}
		if (realBlockOffset == virtualDisk.MAX_INTEGER) {
			System.err.println("ERROR: (in virtualDisk.insert) realBlockOffset == virtualDisk.MAX_INTEGER!");
			return false;
		}
		lengthList = this.getLengthList(classStruct);
		typeList = this.getTypeList(classStruct);
		// valid check, null pointer
		if ((typeList == null) || (lengthList == null)) {
			System.err.println("ERROR: (in virtualDisk.insert) (typeList == null) || (lengthList == null)!");
			return false;
		}

		// write tuple to vdisk
		if (!this.writeOneTuple(lengthList, typeList, realBlockNum, realBlockOffset, tuple)) {
			System.out.println("ERROR: (in virtualDisk.insert) writeOneTuple fail!");
			return false;
		}

		// update biPointerTable
		if (!this.updateBiPointerTable(classStruct, tupleBiPointer)) {
			System.out.println("ERROR: (in virtualDisk.insert) update biPointerTable fail!");
			if (!this.freeOneTuple(n_block, index)) {
				System.out.printf("ERROR: (in virtualDisk.insert) free one tuple(%d, %d) fail!\n", n_block, index);
			}
			return false;
		}

		return true;
	}

	/*
	 * update tuple in class data
	 * @Args:
	 *  tuple(ArrayList<String>)	: update data tuple
	 * @Ret:
	 *  flag(boolean)				: whether update tuple successfully
	 */
	public boolean update(ArrayList<String> tuple) {
		int realBlockNum, realBlockOffset;
		String tupleBiPointer;
		classStruct classStruct;
		ArrayList<Integer> lengthList, typeList;
		ArrayList<String> oldTuple;

		classStruct = this.getClassStruct(this.currClassName);
		if (classStruct == null) {
			System.err.println("ERROR: (in virtualDisk.update) classStruct == null!");
			return false;
		}

		// remove first element from tuple
		tupleBiPointer = tuple.remove(0);
		
		// calculate offset & block
		realBlockNum = this.fakeBlock2RealBlock();
		realBlockOffset = this.fakeOffset2RealOffset();
		// valid check
		if (realBlockNum == virtualDisk.MAX_INTEGER) {
			System.err.println("ERROR: (in virtualDisk.update) (realBlockNum == virtualDisk.MAX_INTEGER)!");
			return false;
		}
		if (realBlockOffset == virtualDisk.MAX_INTEGER) {
			System.err.println("ERROR: (in virtualDisk.update) (realBlockOffset == virtualDisk.MAX_INTEGER)!");
			return false;
		}
		lengthList = this.getLengthList(classStruct);
		typeList = this.getTypeList(classStruct);
		// valid check, null pointer
		if ((typeList == null) || (lengthList == null)) {
			System.err.println("ERROR: (in virtualDisk.update) ((typeList == null) || (lengthList == null))!");
			return false;
		}

		// before update, save oldTuple
		oldTuple = this.readOneTuple(lengthList, typeList, realBlockNum, realBlockOffset);
		if (oldTuple == null) {
			System.err.println("ERROR: (in virtualDisk.update) (oldTuple == null)!");
			return false;
		}
		// update tuple
		if (!this.writeOneTuple(lengthList, typeList, realBlockNum, realBlockOffset, tuple)) {
			System.err.println("ERROR: (in virtualDisk.update) writeOneTuple(tuple) fail!");
			return false;
		}

		// update biPointerTable
		if (!this.updateBiPointerTable(classStruct, tupleBiPointer)) {
			System.out.println("ERROR: (in virtualDisk.update) update biPointerTable fail!");
			if (!this.writeOneTuple(lengthList, typeList, realBlockNum, realBlockOffset, oldTuple)) {
				System.err.println("ERROR: (in virtualDisk.update) writeOneTuple(oldTuple) fail!");
			}
			return false;
		}

		return true;
	}

	/*
	 * delete biPointerTable
	 * @Args:
	 *  classStruct(classStruct)	: classStruct
	 *  objectId(int)				: objectId
	 * @Ret:
	 *  flag(boolean)				: whether delete biPointerTable successfully
	 */
	private boolean deleteBiPointerTable(classStruct classStruct, int objectId) {
		int classId, childrenClassId;
		biPointerTable tempBiPointerTable;

		if ((classStruct == null) || (objectId == virtualDisk.MAX_INTEGER)) {
			System.out.printf("ERROR: (in virtualDisk.deleteBiPointerTable) ((classStruct == null) || (objectId(%d) == virtualDisk.MAX_INTEGER))!\n", objectId);
			return false;
		}

		classId = this.getClassId(classStruct.name);
		if (classId == virtualDisk.MAX_INTEGER) {
			System.out.println("ERROR: (in virtualDisk.deleteBiPointerTable) (classId == virtualDisk.MAX_INTEGER)!");
			return false;
		}

		// remove all deleted children biPointer
		if (!this.removeDeletedChildrenBiPointerTable(classStruct)) {
			System.out.println("ERROR: (in virtualDisk.deleteBiPointerTable) remove all deleted children biPointer fail!");
			return false;
		}

		// remove deleted object's biPointer
		for (int i = 0; i < this.systemBiPointerTable.size(); i++) {
			tempBiPointerTable = this.systemBiPointerTable.get(i);
			// remove biPointer point to son
			if ((tempBiPointerTable.classId == classId) && (tempBiPointerTable.objectId == objectId)) {
				this.systemBiPointerTable.remove(i);
				// no break
			}
			// remove biPointer point to parent
			if ((tempBiPointerTable.deputyClassId == classId) && (tempBiPointerTable.deputyObjectId == objectId)) {
				this.systemBiPointerTable.remove(i);
				// no break
			}
		}

		if (!this.flushBiPointerTable()) {
			System.out.println("ERROR: (in virtualDisk.deleteBiPointerTable) flush biPointerTable fail!");
			return false;
		}

		return true;
	}

	/*
	 * delete tuple in class data
	 * @Args:
	 *  None
	 * @Ret:
	 *  flag(boolean)		: whether delete successfully
	 */
	public boolean delete() {
		int index, n_block, classId;
		classStruct classStruct;

		classId = this.getClassId(this.currClassName);
		classStruct = this.getClassStruct(this.currClassName);
		if ((classStruct == null) || (classId == virtualDisk.MAX_INTEGER)) {
			System.err.printf("ERROR: (in virtualDisk.delete) ((classStruct == null) || (classId(%d) == virtualDisk.MAX_INTEGER))!\n", classId);
			return false;
		}
		// get first block
		n_block = virtualDisk.MAX_INTEGER;
		for (int i = 0; i < this.systemObjectTable.size() ; i++) {
			if (this.systemObjectTable.get(i).classId == classId) {
				n_block = this.systemObjectTable.get(i).blockId;
				break;
			}
		}
		// valid check
		if ((n_block == virtualDisk.MAX_INTEGER) || (n_block == virtualDisk.CONFIG_BLOCK_FLAG) || (n_block == virtualDisk.FREE_BLOCK_FLAG)) {
			System.out.println("ERROR: (in virtualDisk.delete) ((n_block == virtualDisk.MAX_INTEGER) || (n_block == virtualDisk.CONFIG_BLOCK_FLAG) || (n_block == virtualDisk.FREE_BLOCK_FLAG))!");
			return false;
		}

		// current - 1
		index = (this.fakeBlockNum * virtualDisk.PAGESIZE) + this.fakeBlockOffset;

		// check whether index is valid
		if (index >= (this.blockSize * virtualDisk.BITS_OF_BYTE)) {
			System.err.printf("ERROR: (in virtualDisk.delete) index(%d) to large!\n", index);
			return false;
		}

		// delete biPointerTable
		if (!this.deleteBiPointerTable(classStruct, index)) {
			System.out.println("ERROR: (in virtualDisk.delete) deleteBiPointerTable fail!");
			return false;
		}

		// free one tuple from slub-like table
		if (!this.freeOneTuple(n_block, index)) {
			System.out.println("ERROR: (in virtualDisk.delete) freeOneTuple fail!");
			return false;
		}

		return true;
	}

}

	
