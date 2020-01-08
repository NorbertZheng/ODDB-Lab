import java.io.File;
import java.util.ArrayList;

public class test_writeOneTuple {

	private static String baseLocation;

	public static void main(String[] args) {
		int i, n_block;
		ArrayList<String> subBaseLocation;
		ClassStruct classStruct;
		Attribute attribute;
		ArrayList<String> tuple;

		test_writeOneTuple.baseLocation = System.getProperty("user.dir");
		subBaseLocation = fileToolset.pathParser(baseLocation);
		test_writeOneTuple.baseLocation = "";
		for (i = 0; i < subBaseLocation.size() - 5; i++) {
			test_writeOneTuple.baseLocation += subBaseLocation.get(i) + File.separator;
		}
		test_writeOneTuple.baseLocation += subBaseLocation.get(i);

		virtualDisk vdisk = new virtualDisk(test_writeOneTuple.baseLocation);
		// get free block to store data
		n_block = vdisk.getFreeBlock();
		if (!vdisk.setNextBlock(n_block, n_block)) {
			return;
		}
		System.out.printf("get free block: %d\n", n_block);

		// init classStruct
		classStruct = new ClassStruct();
		classStruct.attrList = new ArrayList<Attribute>();
		// int attr
		attribute = new Attribute();
		attribute.attrType = virtualDisk.ATTRTYPE_INTEGER;
		classStruct.attrList.add(attribute);
		// string attr
		attribute = new Attribute();
		attribute.attrType = virtualDisk.ATTRTYPE_STRING;
		classStruct.attrList.add(attribute);
		// int attr
		attribute = new Attribute();
		attribute.attrType = virtualDisk.ATTRTYPE_INTEGER;
		classStruct.attrList.add(attribute);

		// init tuple
		tuple = new ArrayList<String>();
		tuple.add(Integer.toString(0xEF36));
		tuple.add("fassial");
		tuple.add(Integer.toString(0xFFFFFFFF));
		System.out.println("origin tuple:");
		for (i = 0; i < tuple.size(); i++) {
			System.out.println(tuple.get(i));
		}
		if (!vdisk.writeOneTuple(vdisk.getLengthList(classStruct), vdisk.getTypeList(classStruct), n_block, 0, tuple)) {
			return;
		}
		tuple = vdisk.readOneTuple(vdisk.getLengthList(classStruct), vdisk.getTypeList(classStruct), n_block, 0);
		System.out.println("fetch tuple:");
		for (i = 0; i < tuple.size(); i++) {
			System.out.println(tuple.get(i));
		}

		return;
	}
}

