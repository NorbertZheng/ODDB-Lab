import java.io.File;
import java.util.ArrayList;

public class test_insert {

	private static String baseLocation;

	public static void main(String[] args) {
		int i, n_block;
		ArrayList<String> subBaseLocation;
		ClassStruct classStruct;
		Attribute attribute;
		AttrNameTuple attrNameTuple;
		String child;
		ArrayList<String> tuple;

		test_insert.baseLocation = System.getProperty("user.dir");
		subBaseLocation = fileToolset.pathParser(baseLocation);
		test_insert.baseLocation = "";
		for (i = 0; i < subBaseLocation.size() - 5; i++) {
			test_insert.baseLocation += subBaseLocation.get(i) + File.separator;
		}
		test_insert.baseLocation += subBaseLocation.get(i);

		virtualDisk vdisk = new virtualDisk(test_insert.baseLocation);
		System.out.println("vdisk init complete!");

		// init classStruct
		classStruct = new ClassStruct();
		classStruct.attrList = new ArrayList<Attribute>();
		classStruct.virtualAttr = new ArrayList<AttrNameTuple>();
		classStruct.children = new ArrayList<String>();
		// className
		classStruct.className = "fassial";
		// selectClassName
		classStruct.selectClassName = "";
		// condition
		classStruct.condition = "";
		// fake-tuplenum
		classStruct.tupleNum = 6;
		// attrList
		// int attr
		attribute = new Attribute();
		attribute.attrName = "attr1";
		attribute.attrType = virtualDisk.ATTRTYPE_INTEGER;
		attribute.attrSize = virtualDisk.BYTES_OF_INTEGER_DATA;
		attribute.defaultVal = "";
		classStruct.attrList.add(attribute);
		// string attr
		attribute = new Attribute();
		attribute.attrName = "attr2";
		attribute.attrType = virtualDisk.ATTRTYPE_STRING;
		attribute.attrSize = virtualDisk.BYTES_OF_STRING_DATA;
		attribute.defaultVal = "";
		classStruct.attrList.add(attribute);
		// int attr
		attribute = new Attribute();
		attribute.attrName = "attr3";
		attribute.attrType = virtualDisk.ATTRTYPE_INTEGER;
		attribute.attrSize = virtualDisk.BYTES_OF_INTEGER_DATA;
		attribute.defaultVal = "";
		classStruct.attrList.add(attribute);
		// virtualAttr
		// v-attr 1
		attrNameTuple = new AttrNameTuple();
		attrNameTuple.attrName = "v-attr1";
		attrNameTuple.attrRename = "attr1 / 2";
		classStruct.virtualAttr.add(attrNameTuple);
		// v-attr 2
		attrNameTuple = new AttrNameTuple();
		attrNameTuple.attrName = "v-attr2";
		attrNameTuple.attrRename = "attr3 / 3";
		classStruct.virtualAttr.add(attrNameTuple);
		// children
		// child fake 1
		child = "fake-child1";
		classStruct.children.add(child);
		// child fake 2
		child = "fake-child2";
		classStruct.children.add(child);

		// origin class struct
		System.out.println("origin class struct: ");
		System.out.println("className: " + classStruct.className);
		System.out.println("selectClassName: " + classStruct.selectClassName);
		System.out.println("tupleNum: " + classStruct.tupleNum);
		System.out.println("condition: " + classStruct.condition);
		System.out.println("children: ");
		for (i = 0; i < classStruct.children.size(); i++) {
			System.out.println("\t" + classStruct.children.get(i));
		}
		System.out.println("attrList: ");
		for (i = 0; i < classStruct.attrList.size(); i++) {
			System.out.println("\tattrList(" + i + ")");
			System.out.println("\tattrName: " + classStruct.attrList.get(i).attrName);
			System.out.println("\tattrType: " + classStruct.attrList.get(i).attrType);
			System.out.println("\tattrSize: " + classStruct.attrList.get(i).attrSize);
			System.out.println("\tdefaultVal: " + classStruct.attrList.get(i).defaultVal);
		}
		System.out.println("virtualAttr: ");
		for (i = 0; i < classStruct.virtualAttr.size(); i++) {
			System.out.println("\tvirtualAttr(" + i + ")");
			System.out.println("\tattrName: " + classStruct.virtualAttr.get(i).attrName);
			System.out.println("\tattrRename: " + classStruct.virtualAttr.get(i).attrRename);
		}

		// save & restore
		vdisk.createClass(classStruct.className, classStruct);
		classStruct = vdisk.getClassStruct(classStruct.className);

		// convert class struct
		System.out.println("convert class struct: ");
		System.out.println("className: " + classStruct.className);
		System.out.println("selectClassName: " + classStruct.selectClassName);
		System.out.println("tupleNum: " + classStruct.tupleNum);
		System.out.println("condition: " + classStruct.condition);
		System.out.println("children: ");
		for (i = 0; i < classStruct.children.size(); i++) {
			System.out.println("\t" + classStruct.children.get(i));
		}
		System.out.println("attrList: ");
		for (i = 0; i < classStruct.attrList.size(); i++) {
			System.out.println("\tattrList(" + i + ")");
			System.out.println("\tattrName: " + classStruct.attrList.get(i).attrName);
			System.out.println("\tattrType: " + classStruct.attrList.get(i).attrType);
			System.out.println("\tattrSize: " + classStruct.attrList.get(i).attrSize);
			System.out.println("\tdefaultVal: " + classStruct.attrList.get(i).defaultVal);
		}
		System.out.println("virtualAttr: ");
		for (i = 0; i < classStruct.virtualAttr.size(); i++) {
			System.out.println("\tvirtualAttr(" + i + ")");
			System.out.println("\tattrName: " + classStruct.virtualAttr.get(i).attrName);
			System.out.println("\tattrRename: " + classStruct.virtualAttr.get(i).attrRename);
		}

		// initial vdisk
		System.out.println("initial vdisk!");
		vdisk.initial(classStruct.className);
		// insert tuple 1
		System.out.println("insert tuple 1:");
		System.out.printf("before insert, offset: %d\n", vdisk.getOffset());
		tuple = new ArrayList<String>();
		tuple.add(Integer.toString(1));
		tuple.add("Hello World!");
		tuple.add(Integer.toString(-1));
		vdisk.insert(classStruct.className, tuple);
		System.out.printf("after insert, offset: %d\n", vdisk.getOffset());
		// insert tuple 2
		System.out.println("insert tuple 2:");
		System.out.printf("before insert, offset: %d\n", vdisk.getOffset());
		tuple = new ArrayList<String>();
		tuple.add(Integer.toString(2));
		tuple.add("Hello fassial!");
		tuple.add(Integer.toString(0xffff));
		vdisk.insert(classStruct.className, tuple);
		System.out.printf("after insert, offset: %d\n", vdisk.getOffset());
		// reinitial
		System.out.println("reinitial vdisk!");
		vdisk.initial(classStruct.className);
		// Next
		System.out.println("get 1st tuple: ");
		tuple = vdisk.Next();
		for (int index = 0; index < tuple.size(); index++) {
			System.out.printf("\tattr-%d: %s\n", index, tuple.get(index));
		}
		System.out.println("get 2nd tuple: ");
		tuple = vdisk.Next();
		for (int index = 0; index < tuple.size(); index++) {
			System.out.printf("\tattr-%d: %s\n", index, tuple.get(index));
		}
		// reinitial
		System.out.println("reinitial vdisk!");
		vdisk.initial(classStruct.className);
		// update tuple 1
		System.out.println("Next()");
		tuple = vdisk.Next();
		System.out.println("update tuple 1:");
		System.out.printf("before update, offset: %d\n", vdisk.getOffset());
		tuple = new ArrayList<String>();
		tuple.add(Integer.toString(3));
		tuple.add("Hello aosa!");
		tuple.add(Integer.toString(-3));
		vdisk.update(tuple);
		System.out.printf("after insert, offset: %d\n", vdisk.getOffset());
		// reinitial
		System.out.println("reinitial vdisk!");
		vdisk.initial(classStruct.className);
		// Next
		System.out.println("get 1st tuple: ");
		tuple = vdisk.Next();
		for (int index = 0; index < tuple.size(); index++) {
			System.out.printf("\tattr-%d: %s\n", index, tuple.get(index));
		}
		System.out.println("get 2nd tuple: ");
		tuple = vdisk.Next();
		for (int index = 0; index < tuple.size(); index++) {
			System.out.printf("\tattr-%d: %s\n", index, tuple.get(index));
		}
		// reinitial
		System.out.println("reinitial vdisk!");
		vdisk.initial(classStruct.className);
		// delete tuple 1
		System.out.println("Next()");
		tuple = vdisk.Next();
		System.out.println("delete tuple 1:");
		System.out.printf("before delete, offset: %d\n", vdisk.getOffset());
		vdisk.delete();
		System.out.printf("after delete, offset: %d\n", vdisk.getOffset());
		// reinitial
		System.out.println("reinitial vdisk!");
		vdisk.initial(classStruct.className);
		// Next
		System.out.println("get 1st tuple: ");
		tuple = vdisk.Next();
		for (int index = 0; index < tuple.size(); index++) {
			System.out.printf("\tattr-%d: %s\n", index, tuple.get(index));
		}

		return;
	}
}

