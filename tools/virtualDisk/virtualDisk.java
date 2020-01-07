import java.io.File;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.ArrayList;
// local jar
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class virtualDisk {
	final static int BITS_OF_BYTE = 8, N_OF_CONFIG_BLOCK = 1;
	final static int OFFSET_DISKSIZE = 0, OFFSET_BLOCKSIZE = 4, OFFSET_ENTRYSIZE = 8, OFFSET_DATAOFFSET = 12;
	final static int CONFIG_BLOCK_FLAG = 0x0000, FREE_BLOCK_FLAG = 0x0001;
	final static int BYTES_OF_STRING_DATA = 20, BYTES_OF_INTEGER_DATA = 4;
	final static String CLASS_TABLE = "CLASS_TABLE", ATTRIBUTE_TABLE = "ATTRIBUTE_TABLE", DEPUTY_TABLE = "DEPUTY_TABLE", DEPUTYRULE_TABLE = "DEPUTYRULE_TABLE", OBJECT_TABLE = "OBJECT_TABLE", SWITCHING_TABLE = "SWITCHING_TABLE", BIPOINTER_TABLE = "BIPOINTER_TABLE";
	final static String charSet = "utf-8";

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
		ArrayList<String> data = this.decode(fileToolset.readFile(this.systemTableLocation + File.separator + virtualDisk.CLASS_TABLE));

		if ((data.size() / classTable.N_ATTR) != 0) {
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

		return fileToolset.writeFile(this.systemTableLocation + File.separator + virtualDisk.CLASS_TABLE, this.encode(data));
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
		ArrayList<String> data = this.decode(fileToolset.readFile(this.systemTableLocation + File.separator + virtualDisk.ATTRIBUTE_TABLE));

		if ((data.size() / attributeTable.N_ATTR) != 0) {
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

		return fileToolset.writeFile(this.systemTableLocation + File.separator + virtualDisk.ATTRIBUTE_TABLE, this.encode(data));
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
		ArrayList<String> data = this.decode(fileToolset.readFile(this.systemTableLocation + File.separator + virtualDisk.DEPUTY_TABLE));

		if ((data.size() / deputyTable.N_ATTR) != 0) {
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

		return fileToolset.writeFile(this.systemTableLocation + File.separator + virtualDisk.DEPUTY_TABLE, this.encode(data));
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
		ArrayList<String> data = this.decode(fileToolset.readFile(this.systemTableLocation + File.separator + virtualDisk.DEPUTYRULE_TABLE));

		if ((data.size() / deputyRuleTable.N_ATTR) != 0) {
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

		return fileToolset.writeFile(this.systemTableLocation + File.separator + virtualDisk.DEPUTYRULE_TABLE, this.encode(data));
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
		ArrayList<String> data = this.decode(fileToolset.readFile(this.systemTableLocation + File.separator + virtualDisk.OBJECT_TABLE));

		if ((data.size() / objectTable.N_ATTR) != 0) {
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

		return fileToolset.writeFile(this.systemTableLocation + File.separator + virtualDisk.OBJECT_TABLE, this.encode(data));
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
		ArrayList<String> data = this.decode(fileToolset.readFile(this.systemTableLocation + File.separator + virtualDisk.SWITCHING_TABLE));

		if ((data.size() / switchingTable.N_ATTR) != 0) {
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

		return fileToolset.writeFile(this.systemTableLocation + File.separator + virtualDisk.SWITCHING_TABLE, this.encode(data));
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
		ArrayList<String> data = this.decode(fileToolset.readFile(this.systemTableLocation + File.separator + virtualDisk.BIPOINTER_TABLE));

		if ((data.size() / biPointerTable.N_ATTR) != 0) {
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

		return fileToolset.writeFile(this.systemTableLocation + File.separator + virtualDisk.BIPOINTER_TABLE, this.encode(data));
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
				return false;
			} else {
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

			if (!this.deleteBlock(n_nextBlock)) {
				return false;
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
	 * encode ArrayList<String> to String, then write system table
	 * @Args:
	 *  data(ArrayList<String>)	: source data
	 * @Ret:
	 *  result(String)			: encode string
	 */
	public String encode(ArrayList<String> data) {
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
	public ArrayList<String> decode(String code) {
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
			return false;
		} else if (des.length + offset >= src.length) {
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

	
