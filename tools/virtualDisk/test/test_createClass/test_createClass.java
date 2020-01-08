import java.io.File;
import java.util.ArrayList;

public class test_createClass {

	private static String baseLocation;

	public static void main(String[] args) {
		int i, n_block;
		ArrayList<String> subBaseLocation;
		ClassStruct classStruct;
		Attribute attribute;
		AttrNameTuple attrNameTuple;
		String child;

		test_createClass.baseLocation = System.getProperty("user.dir");
		subBaseLocation = fileToolset.pathParser(baseLocation);
		test_createClass.baseLocation = "";
		for (i = 0; i < subBaseLocation.size() - 5; i++) {
			test_createClass.baseLocation += subBaseLocation.get(i) + File.separator;
		}
		test_createClass.baseLocation += subBaseLocation.get(i);

		virtualDisk vdisk = new virtualDisk(test_createClass.baseLocation);

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

		return;
	}
}

