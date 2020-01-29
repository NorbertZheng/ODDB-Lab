import java.io.File;
import java.util.ArrayList;

public class test_biPointerTable {

	private static String baseLocation;

	public static void main(String[] args) {
		int i, fakeBlockNum, fakeBlockOffset, parentFakeBlockNum, parentFakeBlockOffset;
		classStruct classStruct;
		Attribute attribute;
		ArrayList<String> subBaseLocation, tuple, tupleBiPointerList;

		test_biPointerTable.baseLocation = System.getProperty("user.dir");
		subBaseLocation = fileToolset.pathParser(baseLocation);
		test_biPointerTable.baseLocation = "";
		for (i = 0; i < subBaseLocation.size() - 5; i++) {
			test_biPointerTable.baseLocation += subBaseLocation.get(i) + File.separator;
		}
		test_biPointerTable.baseLocation += subBaseLocation.get(i);

		virtualDisk vdisk = new virtualDisk(test_biPointerTable.baseLocation);

		// init classStruct
		classStruct = new classStruct();
		classStruct.name = "product";
		// add attr-0
		attribute = new Attribute();
		attribute.type = Attribute.INT;
		attribute.name = "id";
		attribute.className = "product";
		classStruct.insertAttrList(attribute);
		// add attr-1
		attribute = new Attribute();
		attribute.type = Attribute.STRING;
		attribute.name = "name";
		attribute.className = "product";
		classStruct.insertAttrList(attribute);
		// add attr-2
		attribute = new Attribute();
		attribute.type = Attribute.INT;
		attribute.name = "price";
		attribute.className = "product";
		classStruct.insertAttrList(attribute);
		// show classStruct
		System.out.println(classStruct.toString());
		// CREATE_CLASS
		if (!vdisk.createClass(classStruct.name, classStruct)) {
			System.out.println("ERROR: in (test_biPointerTable.main) createClass fail!");
			return;
		}
		// get classStruct
		classStruct = vdisk.getClassStruct(classStruct.name);
		// show classStruct
		System.out.println(classStruct.toString());

		// initial vdisk
		vdisk.initial("product");
		// init tuple-0
		tuple = new ArrayList<String>();
		tupleBiPointerList = new ArrayList<String>();
		tupleBiPointerList.add("2");
		tuple.add(virtualDisk.encode(tupleBiPointerList));
		tuple.add("1");
		tuple.add("mac");
		tuple.add("14000");
		// show tuple-0
		System.out.println("tuple-0:");
		System.out.println("\t" + test_biPointerTable.tupleToString(tuple));
		// insert tuple-0
		vdisk.insert("product", tuple);
		// get fakeBlockNum && fakeBlockOffset
		parentFakeBlockNum = vdisk.getOffset() / virtualDisk.PAGESIZE;
		parentFakeBlockOffset = vdisk.getOffset() % virtualDisk.PAGESIZE;
		System.out.printf("parentFakeBlockNum = %d, parentFakeBlockOffset = %d\n", parentFakeBlockNum, parentFakeBlockOffset);
		// initial vdisk
		vdisk.initial("product", parentFakeBlockNum, parentFakeBlockOffset);
		// get Next tuple(tuple-0)
		tuple = vdisk.Next();
		// show tuple-0
		System.out.println("tuple-0:");
		System.out.println("\t" + test_biPointerTable.tupleToString(tuple));

		// initial vdisk
		vdisk.initial("product");
		// init tuple-0
		tuple = new ArrayList<String>();
		tupleBiPointerList = new ArrayList<String>();
		tuple.add(virtualDisk.encode(tupleBiPointerList));
		tuple.add("1");
		tuple.add("mac");
		tuple.add("14000");
		// show tuple-0
		System.out.println("tuple-0:");
		System.out.println("\t" + test_biPointerTable.tupleToString(tuple));
		// insert tuple-0
		vdisk.insert("product", tuple);
		// get fakeBlockNum && fakeBlockOffset
		parentFakeBlockNum = vdisk.getOffset() / virtualDisk.PAGESIZE;
		parentFakeBlockOffset = vdisk.getOffset() % virtualDisk.PAGESIZE;
		System.out.printf("parentFakeBlockNum = %d, parentFakeBlockOffset = %d\n", parentFakeBlockNum, parentFakeBlockOffset);
		// initial vdisk
		vdisk.initial("product", parentFakeBlockNum, parentFakeBlockOffset);
		// get Next tuple(tuple-0)
		tuple = vdisk.Next();
		// show tuple-0
		System.out.println("tuple-0:");
		System.out.println("\t" + test_biPointerTable.tupleToString(tuple));

		// init classStruct
		classStruct = new classStruct();
		classStruct.name = "usproduct";
		classStruct.parent = "product";
		classStruct.condition = "price > 5000";
		// add attr-0
		attribute = new Attribute();
		attribute.type = Attribute.INT;
		attribute.name = "sales";
		attribute.className = "usproduct";
		classStruct.insertAttrList(attribute);
		// add vattr-0
		attribute = new Attribute();
		attribute.type = Attribute.INT;
		attribute.name = "name";
		attribute.expression = "name";
		attribute.className = "usproduct";
		classStruct.insertAttrList(attribute);
		// add vattr-1
		attribute = new Attribute();
		attribute.type = Attribute.INT;
		attribute.name = "usprice";
		attribute.expression = "price / 7";
		attribute.className = "usproduct";
		classStruct.insertAttrList(attribute);
		// show classStruct
		System.out.println(classStruct.toString());
		// CREATE_CLASS
		if (!vdisk.createClass(classStruct.name, classStruct)) {
			System.out.println("ERROR: in (test_biPointerTable.main) createClass fail!");
			return;
		}
		// get classStruct
		classStruct = vdisk.getClassStruct(classStruct.name);
		// show classStruct
		System.out.println(classStruct.toString());

		// get parent classStruct
		classStruct = vdisk.getClassStruct("product");
		// show classStruct
		System.out.println(classStruct.toString());

		// initial vdisk
		vdisk.initial("usproduct");
		// init tuple-0
		tuple = new ArrayList<String>();
		tupleBiPointerList = new ArrayList<String>();
		tuple.add(virtualDisk.encode(tupleBiPointerList));
		tuple.add("1");
		// show tuple-0
		System.out.println("tuple-0:");
		System.out.println("\t" + test_biPointerTable.tupleToString(tuple));
		// insert tuple-0
		vdisk.insert("usproduct", tuple);
		// get fakeBlockNum && fakeBlockOffset
		fakeBlockNum = vdisk.getOffset() / virtualDisk.PAGESIZE;
		fakeBlockOffset = vdisk.getOffset() % virtualDisk.PAGESIZE;
		System.out.printf("fakeBlockNum = %d, fakeBlockOffset = %d\n", fakeBlockNum, fakeBlockOffset);
		// initial vdisk
		vdisk.initial("usproduct", fakeBlockNum, fakeBlockOffset);
		// get Next tuple(tuple-0)
		tuple = vdisk.Next();
		// show tuple-0
		System.out.println("tuple-0:");
		System.out.println("\t" + test_biPointerTable.tupleToString(tuple));

		// initial vdisk
		vdisk.initial("product", parentFakeBlockNum, parentFakeBlockOffset);
		// init tuple-0
		tuple = new ArrayList<String>();
		tupleBiPointerList = new ArrayList<String>();
		tupleBiPointerList.add(Integer.toString((fakeBlockNum * virtualDisk.PAGESIZE) + fakeBlockOffset));
		tuple.add(virtualDisk.encode(tupleBiPointerList));
		tuple.add("1");
		tuple.add("mac");
		tuple.add("14000");
		// show tuple-0
		System.out.println("tuple-0:");
		System.out.println("\t" + test_biPointerTable.tupleToString(tuple));
		// update tuple-0
		vdisk.update(tuple);
		// get fakeBlockNum && fakeBlockOffset
		parentFakeBlockNum = vdisk.getOffset() / virtualDisk.PAGESIZE;
		parentFakeBlockOffset = vdisk.getOffset() % virtualDisk.PAGESIZE;
		System.out.printf("fakeBlockNum = %d, fakeBlockOffset = %d\n", parentFakeBlockNum, parentFakeBlockOffset);
		// initial vdisk
		vdisk.initial("product", parentFakeBlockNum, parentFakeBlockOffset);
		// get Next tuple(tuple-0)
		tuple = vdisk.Next();
		// show tuple-0
		System.out.println("tuple-0:");
		System.out.println("\t" + test_biPointerTable.tupleToString(tuple));

		// initial vdisk
		vdisk.initial("usproduct", fakeBlockNum, fakeBlockOffset);
		// delete tuple-0
		vdisk.delete();
		// get fakeBlockNum && fakeBlockOffset
		fakeBlockNum = vdisk.getOffset() / virtualDisk.PAGESIZE;
		fakeBlockOffset = vdisk.getOffset() % virtualDisk.PAGESIZE;
		System.out.printf("fakeBlockNum = %d, fakeBlockOffset = %d\n", fakeBlockNum, fakeBlockOffset);
		// initial vdisk
		vdisk.initial("usproduct", fakeBlockNum, fakeBlockOffset);
		// get Next tuple(tuple-0)
		tuple = vdisk.Next();
		// show tuple-0
		System.out.println("tuple-0:");
		System.out.println("\t" + test_biPointerTable.tupleToString(tuple));

		// drop class
		if (!vdisk.dropClass("usproduct")) {
			System.out.println("ERROR: (in test_biPointerTable) drop class fail!");
			return;
		}
		// initial vdisk
		vdisk.initial("product", parentFakeBlockNum, parentFakeBlockOffset);
		// get Next tuple(tuple-0)
		tuple = vdisk.Next();
		// show tuple-0
		System.out.println("tuple-0:");
		System.out.println("\t" + test_biPointerTable.tupleToString(tuple));
		// initial vdisk
		vdisk.initial("product", parentFakeBlockNum, parentFakeBlockOffset);
		// init tuple-0
		tuple = new ArrayList<String>();
		tupleBiPointerList = new ArrayList<String>();
		tuple.add(virtualDisk.encode(tupleBiPointerList));
		tuple.add("1");
		tuple.add("mac");
		tuple.add("14000");
		// show tuple-0
		System.out.println("tuple-0:");
		System.out.println("\t" + test_biPointerTable.tupleToString(tuple));
		// update tuple-0
		vdisk.update(tuple);
		// get fakeBlockNum && fakeBlockOffset
		parentFakeBlockNum = vdisk.getOffset() / virtualDisk.PAGESIZE;
		parentFakeBlockOffset = vdisk.getOffset() % virtualDisk.PAGESIZE;
		System.out.printf("fakeBlockNum = %d, fakeBlockOffset = %d\n", parentFakeBlockNum, parentFakeBlockOffset);
		// initial vdisk
		vdisk.initial("product", parentFakeBlockNum, parentFakeBlockOffset);
		// get Next tuple(tuple-0)
		tuple = vdisk.Next();
		// show tuple-0
		System.out.println("tuple-0:");
		System.out.println("\t" + test_biPointerTable.tupleToString(tuple));
	}

	public static String tupleToString(ArrayList<String> tuple) {
		String result = "";

		if ((tuple == null) || (tuple.size() == 0)) {
			result += "null!";
			return result;
		} else {
			result += tuple.get(0);
			for (int i = 1; i < tuple.size(); i++) {
				result += "\t" + tuple.get(i);
			}

			return result;
		}
	}

}

