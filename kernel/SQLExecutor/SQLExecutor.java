import java.util.Map;
import java.util.HashMap;
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
		int i, j, k, parentFakeOffset, fakeOffset;
		String className, parentClassName;
		classStruct classStruct, parentClassStruct;
		Attribute attribute, parentAttribute;
		calculationNode calculationExpression;
		ArrayList<String> expressionIdentifier, tuple, parentTuple, tupleBiPointer, parentTupleBiPointer, result = new ArrayList<String>();

		// check SQLNode
		// check SQLNode.classNameList
		if ((this.SQLInstruction.classNameList == null) || (this.SQLInstruction.classNameList.size() != 2)) {
			System.out.println("ERROR: (in SQLExecutor.createSelectDeputyClass) ((this.SQLInstruction.classNameList == null) || (this.SQLInstruction.classNameList.size() != 2))!");
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.createSelectDeputyClass) SQLNode's classNameList must hold two classNames!");
			return result;
		}
		// check SQLNode.attrList not null
		if ((this.SQLInstruction.attrList == null) || (this.SQLInstruction.attrList.size() == 0)) {
			System.out.println("ERROR: (in SQLExecutor.createSelectDeputyClass) ((this.SQLInstruction.attrList == null) || (this.SQLInstruction.attrList.size() == 0))!");
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.createSelectDeputyClass) SQLNode's attrList cannot be null!");
			return result;
		}
		// check SQLNode.attrList's attribute valid
		for (i = 0; i < this.SQLInstruction.attrList.size(); i++) {
			attribute = this.SQLInstruction.attrList.get(i);
			if ((attribute.name == null) || attribute.name.equals("")) {
				System.out.printf("ERROR: (in SQLExecutor.createSelectDeputyClass) attribute(NO.%d) ((attribute.name == null) || attribute.name.equals(\"\"))!\n", i);
				result.add(SQLExecutor.EXECUTE_FAIL);
				result.add("ERROR: (in SQLExecutor.createSelectDeputyClass) attribute(NO." + Integer.toString(i) + ")'s name cannot be null!");
				return result;
			}
			if ((attribute.className == null) || attribute.className.equals("")) {
				System.out.printf("ERROR: (in SQLExecutor.createSelectDeputyClass) attribute(NO.%d) ((attribute.className == null) || attribute.className.equals(\"\"))!\n", i);
				result.add(SQLExecutor.EXECUTE_FAIL);
				result.add("ERROR: (in SQLExecutor.createSelectDeputyClass) attribute(NO." + Integer.toString(i) + ")'s className cannot be null!");
				return result;
			}
		}
		// check attrValueList
		if ((this.SQLInstruction.attrValueList != null) && (this.SQLInstruction.attrValueList.size() != 0)) {
			System.out.println("ERROR: (in SQLExecutor.createSelectDeputyClass) ((this.SQLInstruction.attrValueList != null) && (this.SQLInstruction.attrValueList.size() != 0))!");
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.createSelectDeputyClass) SQLNode's attrValueList must be null!");
			return result;
		}
		// check where
		if (this.SQLInstruction.where == null) {
			System.out.println("ERROR: (in SQLExecutor.createSelectDeputyClass) (this.SQLInstruction.where == null)!");
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.createSelectDeputyClass) SQLNode's where cannot be null!");
			return result;
		}

		// check whether className already exists
		className = this.SQLInstruction.classNameList.get(0);
		if (this.vdisk.existClass(className)) {
			System.out.printf("INFO: (in SQLExecutor.createSelectDeputyClass) className(%s) already exists!\n", className);
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("INFO: className(" + className + ") already exists!");
			return result;
		}
		// check whether parentClassName exists
		parentClassName = this.SQLInstruction.classNameList.get(1);
		if (!this.vdisk.existClass(parentClassName)) {
			System.out.printf("INFO: (in SQLExecutor.createSelectDeputyClass) className(%s) do not exists!\n", parentClassName);
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("INFO: className(" + parentClassName + ") do not exist!");
			return result;
		}
		// get parentClassStruct
		parentClassStruct = this.vdisk.getClassStruct(parentClassName);
		if (parentClassStruct == null) {
			System.out.printf("INFO: (in SQLExecutor.createSelectDeputyClass) get parentClassStruct(%s) fail!\n", parentClassName);
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("INFO: get parentClassStruct(" + parentClassName + ") fail!");
			return result;
		}
		// check virtualAttribute all in parent's attr
		for (i = 0; i < this.SQLInstruction.attrList.size(); i++) {
			attribute = this.SQLInstruction.attrList.get(i);
			// only have to check virtualAttribute
			if ((attribute.expression != null) && (!attribute.expression.equals(""))) {
				try {
					calculationExpression = calculationParser.evaluate(attribute.expression);
				} catch (ParseException ex) {
					System.err.println(ex.getMessage());
					System.out.printf("ERROR: (in SQLExecutor.createSelectDeputyClass) attribute.expression(%s) cannot be parsered into calculationNode!\n", attribute.expression);
					result.add(SQLExecutor.EXECUTE_FAIL);
					result.add("ERROR: (in SQLExecutor.createSelectDeputyClass) attribute.expression(" + attribute.expression + ") cannot be parsered into calculationNode!");
					return result;
				}
				// get all identifier
				expressionIdentifier = calculationExpression.getAllIdentifier();
				if (expressionIdentifier == null) {
					System.out.printf("ERROR: (in SQLExecutor.createSelectDeputyClass) attribute.expression(%s) getAllIdentifier fail!\n", attribute.expression);
					result.add(SQLExecutor.EXECUTE_FAIL);
					result.add("ERROR: (in SQLExecutor.createSelectDeputyClass) attribute.expression(" + attribute.expression + ") getAllIdentifier fail!");
				}
				// check all identifier in parent's attrList
				for (j = 0; j < expressionIdentifier.size(); j++) {
					for (k = 0; k < parentClassStruct.attrList.size(); k++) {
						parentAttribute = parentClassStruct.attrList.get(k);
						if (parentAttribute.name.equals(expressionIdentifier.get(j))) {
							break;
						}
					}
					if (k == parentClassStruct.attrList.size()) {
						// do not match
						System.out.printf("ERROR: (in SQLExecutor.createSelectDeputyClass) parent(%s) do not have attr(%s)!", parentClassStruct.name, expressionIdentifier.get(j));
						result.add(SQLExecutor.EXECUTE_FAIL);
						result.add("ERROR: (in SQLExecutor.createSelectDeputyClass) parent(" + parentClassStruct.name + ") do not have attr(" + expressionIdentifier.get(j) + ")!");
						return result;
					}
				}
			}
		}
		// check where all in parent's attr
		expressionIdentifier = this.SQLInstruction.where.getAllIdentifier();
		// check all identifier in parent's attrList
		for (j = 0; j < expressionIdentifier.size(); j++) {
			for (k = 0; k < parentClassStruct.attrList.size(); k++) {
				parentAttribute = parentClassStruct.attrList.get(k);
				if (parentAttribute.name.equals(expressionIdentifier.get(j))) {
					break;
				}
			}
			if (k == parentClassStruct.attrList.size()) {
				// do not match
				System.out.printf("ERROR: (in SQLExecutor.createSelectDeputyClass) parent(%s) do not have attr(%s)!", parentClassStruct.name, expressionIdentifier.get(j));
				result.add(SQLExecutor.EXECUTE_FAIL);
				result.add("ERROR: (in SQLExecutor.createSelectDeputyClass) parent(" + parentClassStruct.name + ") do not have attr(" + expressionIdentifier.get(j) + ")!");
				return result;
			}
		}

		// execute createSelectDeputyClass
		classStruct = new classStruct();
		classStruct.name = className;
		classStruct.parent = parentClassName;
		classStruct.children = null;
		classStruct.attrList = this.SQLInstruction.attrList;
		classStruct.condition = this.SQLInstruction.where.toString();
		if (!this.vdisk.createClass(classStruct.name, classStruct)) {
			System.out.println("ERROR: (in SQLExecutor.createSelectDeputyClass) this.vdisk createClass fail!");
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.createSelectDeputyClass) this.vdisk createClass fail!");
			return result;
		}

		// update parentClassStruct
		parentClassStruct = this.vdisk.getClassStruct(parentClassName);
		if (parentClassStruct == null) {
			System.out.printf("INFO: (in SQLExecutor.createSelectDeputyClass) get parentClassStruct(%s) fail!\n", parentClassName);
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("INFO: get parentClassStruct(" + parentClassName + ") fail!");
			return result;
		}

		// insert satisfied tuple
		parentFakeOffset = 0;
		fakeOffset = 0;
		this.vdisk.initial(parentClassName);
		while ((parentTuple = this.vdisk.Next()) != null) {
			if (this.tupleSatisfied(this.SQLInstruction.where, parentClassStruct, parentTuple)) {
				parentFakeOffset = this.vdisk.getOffset();
				tuple = this.initTuple(classStruct);
				if (tuple == null) {
					System.out.println("ERROR: (in SQLExecutor.createSelectDeputyClass) (tuple == null)!");
					result.add(SQLExecutor.EXECUTE_FAIL);
					result.add("ERROR: (in SQLExecutor.createSelectDeputyClass) (tuple == null)!");
					return result;
				}
				tupleBiPointer = new ArrayList<String>();
				tupleBiPointer.add(Integer.toString(parentFakeOffset));
				// set biPointer point to parent
				tuple.set(0, dataStorer.encode(tupleBiPointer));
				// already record parentFakeOffset, initial className
				this.vdisk.initial(className, fakeOffset / dataStorer.PAGESIZE, fakeOffset % dataStorer.PAGESIZE);
				if (!this.vdisk.insert(className, tuple)) {
					System.out.println("ERROR: (in SQLExecutor.createSelectDeputyClass) this.vdisk insert(className, tuple) fail!");
					result.add(SQLExecutor.EXECUTE_FAIL);
					result.add("ERROR: (in SQLExecutor.createSelectDeputyClass) this.vdisk insert(className, tuple) fail!");
					return result;
				}
				fakeOffset = this.vdisk.getOffset();
				// get parentTupleBiPointer
				parentTupleBiPointer = dataStorer.decode(parentTuple.get(0));
				// should be ok, match children's order
				parentTupleBiPointer.add(Integer.toString(fakeOffset));
				parentTuple.set(0, dataStorer.encode(parentTupleBiPointer));
				// fakeOffset already recorded, initial parentClassName
				this.vdisk.initial(parentClassName, parentFakeOffset / dataStorer.PAGESIZE, parentFakeOffset % dataStorer.PAGESIZE);
				if (!this.vdisk.update(parentTuple)) {
					System.out.println("ERROR: (in SQLExecutor.createSelectDeputyClass) this.vdisk update(parentTuple) fail!");
					result.add(SQLExecutor.EXECUTE_FAIL);
					result.add("ERROR: (in SQLExecutor.createSelectDeputyClass) this.vdisk update(parentTuple) fail!");
					return result;
				}

				// update fakeOffset & parentFakeOffset
				parentFakeOffset += 1;
				fakeOffset += 1;
			} else {
				// get parentTupleBiPointer
				parentTupleBiPointer = dataStorer.decode(parentTuple.get(0));
				// should be ok, match children's order
				parentTupleBiPointer.add(Integer.toString(-1));
				parentTuple.set(0, dataStorer.encode(parentTupleBiPointer));
				// fakeOffset already recorded, initial parentClassName
				this.vdisk.initial(parentClassName, parentFakeOffset / dataStorer.PAGESIZE, parentFakeOffset % dataStorer.PAGESIZE);
				if (!this.vdisk.update(parentTuple)) {
					System.out.println("ERROR: (in SQLExecutor.createSelectDeputyClass) this.vdisk update(parentTuple) fail!");
					result.add(SQLExecutor.EXECUTE_FAIL);
					result.add("ERROR: (in SQLExecutor.createSelectDeputyClass) this.vdisk update(parentTuple) fail!");
					return result;
				}

				// update fakeOffset & parentFakeOffset
				parentFakeOffset += 1;
				// not update fakeOffset
			}
		}

		if (!this.vdisk.flushToDisk()) {
			System.out.println("ERROR: (in SQLExecutor.createSelectDeputyClass) this.vdisk flushToDisk fail!");
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.createSelectDeputyClass) this.vdisk flushToDisk fail!");
		} else {
			result.add(SQLExecutor.EXECUTE_SUCCESS);
			result.add("INFO: create select deputy class(" + classStruct.name + ") successfully!");
		}

		return result;
	}

	private ArrayList<String> initTuple(classStruct classStruct) {
		Attribute attribute;
		ArrayList<Attribute> realAttributeList;
		ArrayList<String> tupleBiPointer, tuple = new ArrayList<String>();

		// check params
		if (classStruct == null) {
			System.out.println("ERROR: (in SQLExecutor.initTuple) (classStruct == null)!");
			return null;
		}
		// get realAttribute
		realAttributeList = this.vdisk.getRealAttributeList(classStruct);
		if (realAttributeList == null) {
			System.out.println("ERROR: (in SQLExecutor.initTuple) (realAttributeList == null)!");
			return null;
		}
		// init tuple
		// add biPointer
		tupleBiPointer = new ArrayList<String>();
		tupleBiPointer.add("-1");
		tuple.add(dataStorer.encode(tupleBiPointer));
		for (int i = 0; i < realAttributeList.size(); i++) {
			attribute = realAttributeList.get(i);
			if (attribute.type == Attribute.INT) {
				tuple.add(Integer.toString(0));
			} else if (attribute.type == Attribute.STRING) {
				tuple.add("");
			} else {
				System.out.println("ERROR: (in SQLExecutor.initTuple) unknown attr type!");
				return null;
			}
		}

		return tuple;
	}

	private boolean tupleSatisfied(whereNode condition, classStruct classStruct, ArrayList<String> tuple) {
		boolean result;
		int i, j;
		String identifier, tupleElement;
		Attribute attribute;
		ArrayList<Attribute> realAttributeList;
		ArrayList<String> conditionIdentifier;
		Map<String, Integer> intMap = new HashMap<>();
		Map<String, String> stringMap = new HashMap<>();

		// check params
		if ((condition == null) || (classStruct == null) || (classStruct.attrList == null) || (tuple == null)) {
			System.out.println("ERROR: (in SQLExecutor.tupleSatisfied) ERROR: (in SQLExecutor.tupleSatisfied)!");
			return false;
		}

		conditionIdentifier = condition.getAllIdentifier();
		// check condition identifier
		if (conditionIdentifier == null) {
			System.out.println("ERROR: (in SQLExecutor.tupleSatisfied) (conditionIdentifier == null)!");
			return false;
		}
		// only support real-attr for now
		realAttributeList = this.vdisk.getRealAttributeList(classStruct);
		if (realAttributeList == null) {
			System.out.println("ERROR: (in SQLExecutor.tupleSatisfied) (realAttributeList == null)!");
			return false;
		}
		// careful! tuple include biPointer at index-0
		if (realAttributeList.size() != tuple.size() - 1) {
			System.out.println("ERROR: (in SQLExecutor.tupleSatisfied) (realAttributeList.size() != tuple.size() - 1)!");
			return false;
		}
		for (i = 0; i < conditionIdentifier.size(); i++) {
			identifier = conditionIdentifier.get(i);
			// find attribute in classStruct
			for (j = 0; j < realAttributeList.size(); j++) {
				attribute = realAttributeList.get(j);
				if (attribute.name.equals(identifier)) {
					tupleElement = tuple.get(j + 1);
					if (attribute.type == Attribute.INT) {
						if (!dataStorer.canParseInt(tupleElement)) {
							System.out.printf("ERROR: (in SQLExecutor.tupleSatisfied) tupleElement(%s) cannot be parsered into INT!\n", tupleElement);
							return false;
						}
						intMap.put(attribute.name, Integer.parseInt(tupleElement));
					} else if (attribute.type == Attribute.STRING) {
						stringMap.put(attribute.name, tupleElement);
					}
					break;
				}
			}
			if (j == realAttributeList.size()) {
				System.out.printf("ERROR: (in SQLExecutor.tupleSatisfied) class(%s) do not have attr(%s)!\n", classStruct.name, identifier);
				return false;
			}
		}
		// all map ready, call booleanExecutor
		result = booleanExecutor.calculate(condition.toString(), intMap, stringMap);

		return result;
	}

	private ArrayList<String> dropClass() {
		String className;
		classStruct classStruct;
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
		// get classStruct
		classStruct = this.vdisk.getClassStruct(className);
		if (classStruct == null) {
			System.out.printf("ERROR: (in SQLExecutor.dropClass) classStruct(%s) is null!\n", className);
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.dropClass) classStruct(" + className + ") is null!");
			return result;
		}

		// execute dropClass
		if (!this.vdisk.dropClass(className)) {
			System.out.println("ERROR: (in SQLExecutor.dropClass) this.vdisk dropClass fail!");
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.dropClass) this.vdisk dropClass fail!");
			return result;
		} else {
			if (classStruct.children != null) {
				for (int i = 0; i < classStruct.children.size(); i++) {
					if (!this.vdisk.dropClass(classStruct.children.get(i))) {
						System.out.printf("ERROR: (in SQLExecutor.dropClass) this.vdisk drop children class(%s) fail!\n", classStruct.children.get(i));
						result.add(SQLExecutor.EXECUTE_FAIL);
						result.add("ERROR: (in SQLExecutor.dropClass) this.vdisk drop children class(" + classStruct.children.get(i) + ") fail!");
						return result;
					}
				}
			}
		}

		result.add(SQLExecutor.EXECUTE_SUCCESS);
		result.add("INFO: drop class(" + className + ") successfully!");

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

		
