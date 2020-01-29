import java.util.ArrayList;

public class SQLExecutor {
	final static String EXECUTE_FAIL = "fail", EXECUTE_SUCCESS = "success";

	private SQLNode SQLInstruction;
	private dataStorer vdisk;

	public SQLExecutor(String baseLocation) {
		this.SQLInstruction = null;
		this.vdisk = new dataStorer(baseLocation);
	}

	@Override
	protected void finalize() throws Throwable {
		if (this.vdisk != null) {
			this.vdisk.flushToDisk();
		}
		super.finalize();
	}

	public ArrayList<String> evaluate(SQLNode SQLInstruction) {
		ArrayList<String> result;

		// set SQLNode
		this.SQLInstruction = SQLInstruction;
		// switch SQLNode.type
		switch (this.SQLInstruction.type) {
			case SQLNode.CREATE_CLASS: {
				return this.createClass();
			}
			case SQLNode.CREATE_SELECT_DEPUTY_CLASS: {
				return this.createSelectDeputyClass();
			}
			case SQLNode.DROP_CLASS: {
				return this.dropClass();
			}
			case SQLNode.INSERT_TUPLE: {
				return this.insertTuple();
			}
			case SQLNode.DELETE_TUPLE: {
				return this.deleteTuple();
			}
			case SQLNode.UPDATE_TUPLE: {
				return this.updateTuple();
			}
			case SQLNode.SELECT_TUPLE: {
				return this.selectTuple();
			}
			case SQLNode.CROSS_SELECT_TUPLE: {
				return this.crossSelectTuple();
			}
			default: {
				System.out.println("ERROR: (in SQLExecutor.evaluate) unknown SQLInstruction.type!");
				result = new ArrayList<String>();
				result.add(SQLExecutor.EXECUTE_FAIL);
				result.add("ERROR: (in SQLExecutor.evaluate) unknown SQLInstruction.type!");
				return result;
			}
		}
	}

	private ArrayList<String> createClass() {
		String className;
		classStruct classStruct;
		Attribute attribute;
		ArrayList<String> result = new ArrayList<String>();

		// check SQLNode
		// check SQLNode.classNameList
		if ((this.SQLInstruction.classNameList == null) || (this.SQLInstruction.classNameList.size() != 1)) {
			System.out.println("ERROR: (in SQLExecutor.createClass) ((this.SQLInstruction.classNameList == null) || (this.SQLInstruction.classNameList.size() != 1))!");
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.createClass) SQLNode's classNameList can only hold one className!");
			return result;
		}
		// check SQLNode.attrList not null
		if ((this.SQLInstruction.attrList == null) || (this.SQLInstruction.attrList.size() == 0)) {
			System.out.println("ERROR: (in SQLExecutor.createClass) ((this.SQLInstruction.attrList == null) || (this.SQLInstruction.attrList.size() == 0))!");
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.createClass) SQLNode's attrList cannot be null!");
			return result;
		}
		// check SQLNode.attrList's attribute valid
		for (int i = 0; i < this.SQLInstruction.attrList.size(); i++) {
			attribute = this.SQLInstruction.attrList.get(i);
			if ((attribute.name == null) || attribute.name.equals("")) {
				System.out.printf("ERROR: (in SQLExecutor.createClass) attribute(NO.%d) ((attribute.name == null) || attribute.name.equals(\"\"))!\n", i);
				result.add(SQLExecutor.EXECUTE_FAIL);
				result.add("ERROR: (in SQLExecutor.createClass) attribute(NO." + Integer.toString(i) + ")'s name cannot be null!");
				return result;
			}
			if ((attribute.expression != null) && (!attribute.expression.equals(""))) {
				System.out.printf("ERROR: (in SQLExecutor.createClass) attribute(NO.%d) ((attribute.expression != null) && (!attribute.expression.equals(\"\"))!\n", i);
				result.add(SQLExecutor.EXECUTE_FAIL);
				result.add("ERROR: (in SQLExecutor.createClass) attribute(NO." + Integer.toString(i) + ")'s expression must be null!");
				return result;
			}
			if ((attribute.className == null) || attribute.className.equals("")) {
				System.out.printf("ERROR: (in SQLExecutor.createClass) attribute(NO.%d) ((attribute.className == null) || attribute.className.equals(\"\"))!\n", i);
				result.add(SQLExecutor.EXECUTE_FAIL);
				result.add("ERROR: (in SQLExecutor.createClass) attribute(NO." + Integer.toString(i) + ")'s className cannot be null!");
				return result;
			}
		}
		// check attrValueList
		if ((this.SQLInstruction.attrValueList != null) && (this.SQLInstruction.attrValueList.size() != 0)) {
			System.out.println("ERROR: (in SQLExecutor.createClass) ((this.SQLInstruction.attrValueList != null) && (this.SQLInstruction.attrValueList.size() != 0))!");
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.createClass) SQLNode's attrValueList must be null!");
			return result;
		}
		// check where
		if (this.SQLInstruction.where != null) {
			System.out.println("ERROR: (in SQLExecutor.createClass) (this.SQLInstruction.where != null)!");
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.createClass) SQLNode's where must be null!");
			return result;
		}

		// check whether className already exists
		className = this.SQLInstruction.classNameList.get(0);
		if (this.vdisk.existClass(className)) {
			System.out.printf("INFO: (in SQLExecutor.createClass) className(%s) already exists!\n", className);
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("INFO: className(" + className + ") already exists!");
			return result;
		}

		// execute createClass
		classStruct = new classStruct();
		classStruct.name = className;
		classStruct.parent = null;
		classStruct.children = null;
		classStruct.attrList = this.SQLInstruction.attrList;
		classStruct.condition = null;
		if (!this.vdisk.createClass(classStruct.name, classStruct)) {
			System.out.println("ERROR: (in SQLExecutor.createClass) this.vdisk createClass fail!");
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.createClass) this.vdisk createClass fail!");
		} else {
			result.add(SQLExecutor.EXECUTE_SUCCESS);
			result.add("INFO: create class(" + classStruct.name + ") successfully!");
		}

		return result;			
	}

	private ArrayList<String> createSelectDeputyClass() {
		return null;
	}

	private ArrayList<String> dropClass() {
		String className;
		ArrayList<String> result = new ArrayList<String>();

		// check SQLNode
		// check SQLNode.classNameList
		if ((this.SQLInstruction.classNameList == null) || (this.SQLInstruction.classNameList.size() != 1)) {
			System.out.println("ERROR: (in SQLExecutor.dropClass) ((this.SQLInstruction.classNameList == null) || (this.SQLInstruction.classNameList.size() != 1))!");
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.dropClass) SQLNode's classNameList can only hold one className!");
			return result;
		}
		// check SQLNode.attrList is null
		if ((this.SQLInstruction.attrList != null) && (this.SQLInstruction.attrList.size() != 0)) {
			System.out.println("ERROR: (in SQLExecutor.dropClass) ((this.SQLInstruction.attrList != null) && (this.SQLInstruction.attrList.size() != 0))!");
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.dropClass) SQLNode's attrList must be null!");
			return result;
		}
		// check attrValueList
		if ((this.SQLInstruction.attrValueList != null) && (this.SQLInstruction.attrValueList.size() != 0)) {
			System.out.println("ERROR: (in SQLExecutor.dropClass) ((this.SQLInstruction.attrValueList != null) && (this.SQLInstruction.attrValueList.size() != 0))!");
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.dropClass) SQLNode's attrValueList must be null!");
			return result;
		}
		// check where
		if (this.SQLInstruction.where != null) {
			System.out.println("ERROR: (in SQLExecutor.dropClass) (this.SQLInstruction.where != null)!");
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.dropClass) SQLNode's where must be null!");
			return result;
		}

		// check whether className exists
		className = this.SQLInstruction.classNameList.get(0);
		if (!this.vdisk.existClass(className)) {
			System.out.printf("ERROR: (in SQLExecutor.dropClass) className(%s) do not exists!\n", className);
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.dropClass) className(" + className + ") do not exists!");
			return result;
		}

		// execute dropClass
		if (!this.vdisk.dropClass(className)) {
			System.out.println("ERROR: (in SQLExecutor.dropClass) this.vdisk dropClass fail!");
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.dropClass) this.vdisk dropClass fail!");
		} else {
			result.add(SQLExecutor.EXECUTE_SUCCESS);
			result.add("INFO: drop class(" + className + ") successfully!");
		}

		return result;			
	}

	private ArrayList<String> insertTuple() {
		int i, j;
		String className;
		classStruct classStruct;
		Attribute attribute;
		ArrayList<Attribute> realAttributeList;
		ArrayList<String> tuple = new ArrayList<String>();
		ArrayList<String> result = new ArrayList<String>();

		// check SQLNode
		// check SQLNode.classNameList
		if ((this.SQLInstruction.classNameList == null) || (this.SQLInstruction.classNameList.size() != 1)) {
			System.out.println("ERROR: (in SQLExecutor.insertTuple) ((this.SQLInstruction.classNameList == null) || (this.SQLInstruction.classNameList.size() != 1))!");
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.insertTuple) SQLNode's classNameList can only hold one className!");
			return result;
		}
		// check whether className exists
		className = this.SQLInstruction.classNameList.get(0);
		if (!this.vdisk.existClass(className)) {
			System.out.printf("ERROR: (in SQLExecutor.insertTuple) className(%s) do not exists!\n", className);
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.insertTuple) className(" + className + ") do not exists!");
			return result;
		}
		// get classStruct
		classStruct = this.vdisk.getClassStruct(className);
		// check SQLNode.attrList not null
		if ((this.SQLInstruction.attrList == null) || (this.SQLInstruction.attrList.size() == 0)) {
			System.out.println("ERROR: (in SQLExecutor.insertTuple) ((this.SQLInstruction.attrList == null) || (this.SQLInstruction.attrList.size() == 0))!");
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.insertTuple) SQLNode's attrList cannot be null!");
			return result;
		}
		// check SQLNode.attrList.size() == realAttribute.size()
		if ((classStruct.attrList == null) || (classStruct.attrList.size() == 0)) {
			System.out.println("ERROR: (in SQLExecutor.insertTuple) ((classStruct.attrList == null) || (classStruct.attrList.size() == 0))!");
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.insertTuple) ((classStruct.attrList == null) || (classStruct.attrList.size() == 0))!");
			return result;
		}
		realAttributeList = this.vdisk.getRealAttributeList(classStruct);
		if (realAttributeList.size() != this.SQLInstruction.attrList.size()) {
			System.out.printf("ERROR: (in SQLExecutor.insertTuple) (realAttributeList.size()(%d) != this.SQLInstruction.attrList.size()(%d))!\n", realAttributeList.size(), this.SQLInstruction.attrList.size());
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.insertTuple) (realAttributeList.size()(" + Integer.toString(realAttributeList.size()) + ") != this.SQLInstruction.attrList.size()(" + Integer.toString(this.SQLInstruction.attrList.size()) + "))!");
			return result;
		}
		// check SQLNode.attrList's attribute valid
		for (i = 0; i < this.SQLInstruction.attrList.size(); i++) {
			attribute = this.SQLInstruction.attrList.get(i);
			if ((attribute.name == null) || attribute.name.equals("")) {
				System.out.printf("ERROR: (in SQLExecutor.insertTuple) attribute(NO.%d) ((attribute.name == null) || attribute.name.equals(\"\"))!\n", i);
				result.add(SQLExecutor.EXECUTE_FAIL);
				result.add("ERROR: (in SQLExecutor.insertTuple) attribute(NO." + Integer.toString(i) + ")'s name cannot be null!");
				return result;
			}
			// check is in realAttributeList
			for (j = 0; j < realAttributeList.size(); j++) {
				if (attribute.name.equals(realAttributeList.get(j).name)) {
					break;
				}
			}
			if (j == realAttributeList.size()) {
				System.out.printf("ERROR: (in SQLExecutor.insertTuple) attrName(%s) not in realAttributeList!\n", attribute.name);
				result.add(SQLExecutor.EXECUTE_FAIL);
				result.add("ERROR: (in SQLExecutor.insertTuple) attrName(" + attribute.name + ") not in realAttributeList!");
				return result;
			}
			// check attrList is a set
			for (j = i + 1; j < this.SQLInstruction.attrList.size(); j++) {
				if (attribute.name.equals(this.SQLInstruction.attrList.get(j).name)) {
					System.out.printf("ERROR: (in SQLExecutor.insertTuple) attribute(NO.%d)'name is not unique!\n", i);
					result.add(SQLExecutor.EXECUTE_FAIL);
					result.add("ERROR: (in SQLExecutor.insertTuple) attribute(NO." + Integer.toString(i) + ")'name is not unique!");
					return result;
				}
			}
			// check attribute.className not null
			if ((attribute.className == null) || attribute.className.equals("")) {
				System.out.printf("ERROR: (in SQLExecutor.insertTuple) attribute(NO.%d) ((attribute.className == null) || attribute.className.equals(\"\"))!\n", i);
				result.add(SQLExecutor.EXECUTE_FAIL);
				result.add("ERROR: (in SQLExecutor.insertTuple) attribute(NO." + Integer.toString(i) + ")'s className cannot be null!");
				return result;
			}
		}
		// check attrValueList not null
		if ((this.SQLInstruction.attrValueList == null) || (this.SQLInstruction.attrValueList.size() == 0)) {
			System.out.println("ERROR: (in SQLExecutor.insertTuple) ((this.SQLInstruction.attrValueList == null) || (this.SQLInstruction.attrValueList.size() == 0))!");
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.insertTuple) SQLNode's attrValueList cannot be null!");
			return result;
		}
		// check attrValueList.size() == attrList.size()
		if (this.SQLInstruction.attrValueList.size() != this.SQLInstruction.attrList.size()) {
			System.out.println("ERROR: (in SQLExecutor.insertTuple) (this.SQLInstruction.attrValueList.size() != this.SQLInstruction.attrList.size())!");
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.insertTuple) SQLNode's attrValueList.size() != attrList.size()!");
			return result;
		}
		// check attrValueList's element type && set tuple
		for (i = 0; i < realAttributeList.size(); i++) {
			attribute = realAttributeList.get(i);
			for (j = 0; j < this.SQLInstruction.attrList.size(); j++) {
				if (attribute.name.equals(this.SQLInstruction.attrList.get(j).name)) {
					// check type
					if (attribute.type == Attribute.INT) {
						if (!dataStorer.canParseInt(this.SQLInstruction.attrValueList.get(j))) {
							System.out.printf("ERROR: (in SQLExecutor.insertTuple) attrValue(%s) cannot be parsed into INT!\n", this.SQLInstruction.attrValueList.get(j));
							result.add(SQLExecutor.EXECUTE_FAIL);
							result.add("ERROR: (in SQLExecutor.insertTuple) attrValue(" + this.SQLInstruction.attrValueList.get(j) + ") cannot be parsed into INT!");
							return result;
						}
						tuple.add(this.SQLInstruction.attrValueList.get(j));
					} else if (attribute.type == Attribute.STRING) {
						tuple.add(this.SQLInstruction.attrValueList.get(j));
					} else {
						System.out.println("ERROR: (in SQLExecutor.insertTuple) unknown type!");
						result.add(SQLExecutor.EXECUTE_FAIL);
						result.add("ERROR: (in SQLExecutor.insertTuple) unknown type!");
						return result;
					}
					break;
				}
			}
		}

		// temp add null pointer
		tuple.add(0, dataStorer.encode(new ArrayList<String>()));

		// execute insertTuple
		if (!this.vdisk.insert(className, tuple)) {
			System.out.println("ERROR: (in SQLExecutor.insertTuple) this.vdisk insert fail!");
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.insertTuple) this.vdisk insert fail!");
		} else {
			result.add(SQLExecutor.EXECUTE_SUCCESS);
			result.add("INFO: insert tuple(" + SQLExecutor.tuple2String(tuple) + ") successfully!");
		}

		return result;
	}

	private ArrayList<String> deleteTuple() {
		return null;
	}

	private ArrayList<String> updateTuple() {
		return null;
	}

	private ArrayList<String> selectTuple() {
		return null;
	}

	private ArrayList<String> crossSelectTuple() {
		return null;
	}

	public static String tuple2String(ArrayList<String> tuple) {
		String result = "";

		if ((tuple == null) || (tuple.size() == 0)) {
			// do nothing
		} else {
			result += tuple.get(0);
			for (int i = 1; i < tuple.size(); i++) {
				result += ", " + tuple.get(i);
			}
		}

		return result;
	}

}

		
