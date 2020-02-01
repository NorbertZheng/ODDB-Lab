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
		ArrayList<Attribute> realAttributeList;
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
							// set attribute.type
							attribute.type = parentAttribute.type;
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
			// get parentFakeOffset
			parentFakeOffset = this.vdisk.getOffset();
			if (this.crossTupleSatisfied(this.SQLInstruction.where, parentClassStruct, parentFakeOffset)) {
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
				for (i = 0; i < parentClassStruct.children.size(); i++) {
					if (parentClassStruct.children.get(i).equals(classStruct.name)) {
						parentTupleBiPointer.set(i + 1, Integer.toString(fakeOffset));
						break;
					}
				}
				if (i == parentClassStruct.children.size()) {
					System.out.printf("ERROR: (in SQLExecutor.createSelectDeputyClass) cannot find classStruct(%s) from parentClassStruct(%s)'s children!\n", classStruct.name, parentClassStruct.name);
					result.add(SQLExecutor.EXECUTE_FAIL);
					result.add("ERROR: (in SQLExecutor.createSelectDeputyClass) cannot find classStruct(" + classStruct.name + ") from parentClassStruct(" + parentClassStruct.name + ")'s children!");
					return result;
				}

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
				// re-initial parentClassName
				this.vdisk.initial(parentClassName, parentFakeOffset / dataStorer.PAGESIZE, parentFakeOffset % dataStorer.PAGESIZE);
			} else {
				// get parentTupleBiPointer
				parentTupleBiPointer = dataStorer.decode(parentTuple.get(0));
				// should be ok, match children's order
				for (i = 0; i < parentClassStruct.children.size(); i++) {
					if (parentClassStruct.children.get(i).equals(classStruct.name)) {
						parentTupleBiPointer.set(i, Integer.toString(dataStorer.DEFAULT_BIPOINTER));
						break;
					}
				}
				if (i == parentClassStruct.children.size()) {
					System.out.printf("ERROR: (in SQLExecutor.createSelectDeputyClass) cannot find classStruct(%s) from parentClassStruct(%s)'s children!\n", classStruct.name, parentClassStruct.name);
					result.add(SQLExecutor.EXECUTE_FAIL);
					result.add("ERROR: (in SQLExecutor.createSelectDeputyClass) cannot find classStruct(" + classStruct.name + ") from parentClassStruct(" + parentClassStruct.name + ")'s children!");
					return result;
				}
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
				// re-initial parentClassName
				this.vdisk.initial(parentClassName, parentFakeOffset / dataStorer.PAGESIZE, parentFakeOffset % dataStorer.PAGESIZE);
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
		tupleBiPointer.add(Integer.toString(dataStorer.DEFAULT_BIPOINTER));
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
		int i, j, fakeOffset, childrenFakeOffset;
		String className;
		whereNode conditionExpression;
		classStruct classStruct, childrenClassStruct;
		Attribute attribute;
		ArrayList<Attribute> realAttributeList;
		ArrayList<String> tuple = new ArrayList<String>(), tupleBiPointer, childrenTuple, childrenTupleBiPointer;
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

		// add biPointer point to parent(which is -1)
		tupleBiPointer = new ArrayList<String>();
		tupleBiPointer.add(Integer.toString(dataStorer.DEFAULT_BIPOINTER));
		for (i = 0; i < classStruct.children.size(); i++) {
			tupleBiPointer.add(Integer.toString(dataStorer.DEFAULT_BIPOINTER));
		}
		tuple.add(0, dataStorer.encode(tupleBiPointer));

		// execute insertTuple
		if (!this.vdisk.insert(className, tuple)) {
			System.out.println("ERROR: (in SQLExecutor.insertTuple) this.vdisk insert fail!");
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.insertTuple) this.vdisk insert fail!");
			return result;
		}

		// get fakeOffset
		fakeOffset = this.vdisk.getOffset();

		// insertTupleHelper
		if (!this.insertTupleHelper(classStruct, tuple, tupleBiPointer, fakeOffset)) {
			System.out.printf("ERROR: (in SQLExecutor.insertTuple) insertTupleHelper classStruct(%s) fail!\n", classStruct.name);
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.insertTuple) insertTupleHelper classStruct(" + classStruct.name + ") fail!");
			return result;
		}

		// flush to vdisk
		if (!this.vdisk.flushToDisk()) {
			System.out.println("ERROR: (in SQLExecutor.insertTuple) this.vdisk flushToDisk fail!");
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.insertTuple) this.vdisk flushToDisk fail!");
			return result;
		}

		result.add(SQLExecutor.EXECUTE_SUCCESS);
		result.add("INFO: insert tuple(" + SQLExecutor.tuple2String(tuple) + ") successfully!");

		return result;
	}

	private boolean insertTupleHelper(classStruct classStruct, ArrayList<String> tuple, ArrayList<String> tupleBiPointer, int fakeOffset) {
		int i, childrenFakeOffset;
		classStruct childrenClassStruct;
		whereNode conditionExpression;
		ArrayList<String> childrenTuple, childrenTupleCopy, childrenTupleBiPointer;

		// check whether have children
		if ((classStruct.children != null) && (classStruct.children.size() != 0)) {
			for (i = 0; i < classStruct.children.size(); i++) {
				// get children classStruct
				childrenClassStruct = this.vdisk.getClassStruct(classStruct.children.get(i));
				if (childrenClassStruct == null) {
					System.out.printf("ERROR: (in SQLExecutor.insertTupleHelper) this.vdisk get classStruct(%s) fail!\n", classStruct.children.get(i));
					return false;
				}
				// get whereNode
				try {
					conditionExpression = booleanParser.evaluate(childrenClassStruct.condition);
				} catch (ParseException ex) {
					System.err.println(ex.getMessage());
					System.out.printf("ERROR: (in SQLExecutor.insertTupleHelper) condition(%s) cannot be parserd!\n", childrenClassStruct.condition);
					return false;
				}
				if (this.crossTupleSatisfied(conditionExpression, classStruct, fakeOffset)) {
					childrenTuple = this.initTuple(childrenClassStruct);
					childrenTupleBiPointer = new ArrayList<String>();
					childrenTupleBiPointer.add(Integer.toString(fakeOffset));
					// set biPointer point to parent
					childrenTuple.set(0, dataStorer.encode(childrenTupleBiPointer));
					// init childrenTupleCopy
					childrenTupleCopy = new ArrayList<String>();
					if ((childrenTupleCopy = SQLExecutor.tupleCopy(childrenTuple)) == null) {
						System.out.println("ERROR: (in SQLExecutor.insertTupleHelper) SQLExecutor.tupleCopy fail!");
						return false;
					}
					// fakeOffset already record, initial childrenClassName
					this.vdisk.initial(childrenClassStruct.name);
					if (!this.vdisk.insert(childrenClassStruct.name, childrenTupleCopy)) {
						System.out.printf("ERROR: (in SQLExecutor.insertTupleHelper) insert tuple to childrenClassStruct(%s) fail!\n", childrenClassStruct.name);
						return false;
					}
					childrenFakeOffset = this.vdisk.getOffset();
					// should match, suppose in order
					tupleBiPointer.set(i + 1, Integer.toString(childrenFakeOffset));
					if ((childrenTupleCopy = SQLExecutor.tupleCopy(childrenTuple)) == null) {
						System.out.println("ERROR: (in SQLExecutor.insertTupleHelper) SQLExecutor.tupleCopy fail!");
						return false;
					}
					// handle children's children
					if (!this.insertTupleHelper(childrenClassStruct, childrenTupleCopy, childrenTupleBiPointer, childrenFakeOffset)) {
						System.out.printf("ERROR: (in SQLExecutor.insertTupleHelper) insertTupleHelper childrenClassStruct(%s) fail!\n", childrenClassStruct.name);
						return false;
					}
				} else {
					tupleBiPointer.set(i + 1, Integer.toString(dataStorer.DEFAULT_BIPOINTER));
				}
			}
			tuple.set(0, dataStorer.encode(tupleBiPointer));
			System.out.println(SQLExecutor.tuple2String(tuple));
			// initial className
			this.vdisk.initial(classStruct.name, fakeOffset / dataStorer.PAGESIZE, fakeOffset % dataStorer.PAGESIZE);
			if (!this.vdisk.update(tuple)) {
				System.out.println("ERROR: (in SQLExecutor.insertTupleHelper) this.vdisk update tuple fail!");
				return false;
			} else {
				// do nothing
			}
		}

		return true;
	}

	private static ArrayList<String> tupleCopy(ArrayList<String> src) {
		ArrayList<String> target = new ArrayList<String>();

		if (src == null) {
			return null;
		}

		for (int i = 0; i < src.size(); i++) {
			target.add(new String(src.get(i)));
		}

		return target;
	}

	private ArrayList<String> deleteTuple() {
		int i, j, fakeOffset;
		String className;
		classStruct classStruct;
		ArrayList<String> result = new ArrayList<String>(), whereIdentifier, tuple;

		// check SQLNode
		// check SQLNode.classNameList
		if ((this.SQLInstruction.classNameList == null) || (this.SQLInstruction.classNameList.size() != 1)) {
			System.out.println("ERROR: (in SQLExecutor.deleteTuple) ((this.SQLInstruction.classNameList == null) || (this.SQLInstruction.classNameList.size() != 1))!");
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.deleteTuple) SQLNode's classNameList can only hold one className!");
			return result;
		}
		// check whether className exists
		className = this.SQLInstruction.classNameList.get(0);
		if (!this.vdisk.existClass(className)) {
			System.out.printf("ERROR: (in SQLExecutor.deleteTuple) className(%s) do not exists!\n", className);
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.deleteTuple) className(" + className + ") do not exists!");
			return result;
		}
		// get classStruct
		classStruct = this.vdisk.getClassStruct(className);
		// check SQLNode.attrList is null
		if ((this.SQLInstruction.attrList != null) && (this.SQLInstruction.attrList.size() != 0)) {
			System.out.println("ERROR: (in SQLExecutor.deleteTuple) ((this.SQLInstruction.attrList != null) && (this.SQLInstruction.attrList.size() != 0))!");
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.deleteTuple) SQLNode's attrList must be null!");
			return result;
		}
		// check attrValueList
		if ((this.SQLInstruction.attrValueList != null) && (this.SQLInstruction.attrValueList.size() != 0)) {
			System.out.println("ERROR: (in SQLExecutor.deleteTuple) ((this.SQLInstruction.attrValueList != null) && (this.SQLInstruction.attrValueList.size() != 0))!");
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.deleteTuple) SQLNode's attrValueList must be null!");
			return result;
		}
		// check SQLNode's where is not null
		if (this.SQLInstruction.where == null) {
			System.out.println("ERROR: (in SQLExecutor.deleteTuple) (this.SQLInstruction.where == null)!");
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.deleteTuple) SQLNode's where cannot be null!");
			return result;
		}

		// check where'identifier is in classStruct's attrList
		whereIdentifier = this.SQLInstruction.where.getAllIdentifier();
		if (whereIdentifier == null) {
			System.out.println("ERROR: (in SQLExecutor.deleteTuple) (whereIdentifier == null)!");
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.deleteTuple) (whereIdentifier == null)!");
			return result;
		}
		for (i = 0; i < whereIdentifier.size(); i++) {
			for (j = 0; j < classStruct.attrList.size(); j++) {
				if (whereIdentifier.get(i).equals(classStruct.attrList.get(j).name)) {
					break;
				}
			}
			if (j == classStruct.attrList.size()) {
				System.out.printf("ERROR: (in SQLExecutor.deleteTuple) identifier(%s) not in classStruct(%s)'s attrList!\n", whereIdentifier.get(i), classStruct.name);
				result.add(SQLExecutor.EXECUTE_FAIL);
				result.add("ERROR: (in SQLExecutor.deleteTuple) identifier(" + whereIdentifier.get(i) + ") not in classStruct(" + classStruct.name + ")'s attrList!");
				return result;
			}
		}

		// initial className
		this.vdisk.initial(className);
		while ((tuple = this.vdisk.Next()) != null) {
			// get offset
			fakeOffset = this.vdisk.getOffset();
			if (this.crossTupleSatisfied(this.SQLInstruction.where, classStruct, fakeOffset)) {
				if (!this.deleteTupleHelper(classStruct, fakeOffset, dataStorer.decode(tuple.get(0)))) {
					System.out.println("ERROR: (in SQLExecutor.deleteTuple) deleteTupleHelper(classStruct, fakeOffset) fail!");
					result.add(SQLExecutor.EXECUTE_FAIL);
					result.add("ERROR: (in SQLExecutor.deleteTuple) deleteTupleHelper(classStruct, fakeOffset) fail!");
					return result;
				}
			}
			// re-initial className with (fakeOffset + 1)
			this.vdisk.initial(className, (fakeOffset + 1) / dataStorer.PAGESIZE, (fakeOffset + 1) % dataStorer.PAGESIZE);
		}

		// flush to vdisk
		if (!this.vdisk.flushToDisk()) {
			System.out.println("ERROR: (in SQLExecutor.deleteTuple) this.vdisk flushToDisk fail!");
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.deleteTuple) this.vdisk flushToDisk fail!");
			return result;
		}

		result.add(SQLExecutor.EXECUTE_SUCCESS);
		result.add("INFO: delete tuple(" + this.SQLInstruction.where.toString() + ") successfully!");

		return result;
	}

	private boolean deleteTupleHelper(classStruct classStruct, int fakeOffset, ArrayList<String> tupleBiPointer) {
		int i, childrenFakeOffset;
		classStruct childrenClassStruct;
		ArrayList<String> childrenTuple;

		// check params
		if ((classStruct == null) || (tupleBiPointer == null)) {
			System.out.println("ERROR: (in SQLExecutor.deleteTupleHelper) ((classStruct == null) || (childrenTupleBiPointer == null))!");
			return false;
		}

		if (classStruct.children.size() + 1 != tupleBiPointer.size()) {
			System.out.printf("ERROR: (in SQLExecutor.deleteTupleHelper) (classStruct.children.size()(%d) + 1 != tupleBiPointer.size()(%d))!\n",  classStruct.children.size(), tupleBiPointer.size());
			return false;
		}

		// initial classStruct
		this.vdisk.initial(classStruct.name, fakeOffset / dataStorer.PAGESIZE, fakeOffset % dataStorer.PAGESIZE);
		// delete One Tuple
		if (!this.vdisk.delete()) {
			System.out.println("ERROR: (in SQLExecutor.deleteTupleHelper) this.vdisk delete fail!");
			return false;
		}
		// delete children tuple
		for (i = 0; i < classStruct.children.size(); i++) {
			childrenClassStruct = this.vdisk.getClassStruct(classStruct.children.get(i));
			if (childrenClassStruct == null) {
				System.out.printf("ERROR: (in SQLExecutor.deleteTupleHelper) this.vdisk getClassStruct(%s) fail!\n", classStruct.children.get(i));
				return false;
			}
			if (!dataStorer.canParseInt(tupleBiPointer.get(i + 1))) {
				System.out.printf("ERROR: (in SQLExecutor.deleteTupleHelper) biPointer(%s) cannot be parsed to INT!\n", tupleBiPointer.get(i + 1));
				return false;
			}
			childrenFakeOffset = Integer.parseInt(tupleBiPointer.get(i + 1));
			if (childrenFakeOffset != dataStorer.DEFAULT_BIPOINTER) {
				// valid biPointer
				// initial childrenClassStruct
				this.vdisk.initial(childrenClassStruct.name, childrenFakeOffset / dataStorer.PAGESIZE, childrenFakeOffset % dataStorer.PAGESIZE);
				if ((childrenTuple = this.vdisk.Next()) == null) {
					System.out.printf("ERROR: (in SQLExecutor.deleteTupleHelper) this.vdisk Next(%s) fail!\n", childrenClassStruct.name);
					return false;
				}
				if (!this.deleteTupleHelper(childrenClassStruct, childrenFakeOffset, dataStorer.decode(childrenTuple.get(0)))) {
					System.out.printf("ERROR: (in SQLExecutor.deleteTupleHelper) deleteTupleHelper(%s) fail!\n", childrenClassStruct.name);
					return false;
				}
			}
		}

		// re-initial classStruct
		this.vdisk.initial(classStruct.name, fakeOffset / dataStorer.PAGESIZE, fakeOffset % dataStorer.PAGESIZE);

		return true;
	}
			

	private ArrayList<String> updateTuple() {
		int i, j, fakeOffset, childrenFakeOffset;
		String className;
		Attribute attribute;
		classStruct classStruct, childrenClassStruct;
		whereNode conditionExpression;
		ArrayList<Attribute> realAttributeList;
		ArrayList<String> whereIdentifier, tuple, tupleBiPointer, childrenTuple, childrenTupleBiPointer;
		ArrayList<String> result = new ArrayList<String>();

		// check SQLNode
		// check SQLNode.classNameList
		if ((this.SQLInstruction.classNameList == null) || (this.SQLInstruction.classNameList.size() != 1)) {
			System.out.println("ERROR: (in SQLExecutor.updateTuple) ((this.SQLInstruction.classNameList == null) || (this.SQLInstruction.classNameList.size() != 1))!");
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.updateTuple) SQLNode's classNameList can only hold one className!");
			return result;
		}
		// check whether className exists
		className = this.SQLInstruction.classNameList.get(0);
		if (!this.vdisk.existClass(className)) {
			System.out.printf("ERROR: (in SQLExecutor.updateTuple) className(%s) do not exists!\n", className);
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.updateTuple) className(" + className + ") do not exists!");
			return result;
		}
		// get classStruct
		classStruct = this.vdisk.getClassStruct(className);
		// check SQLNode.attrList not null
		if ((this.SQLInstruction.attrList == null) || (this.SQLInstruction.attrList.size() == 0)) {
			System.out.println("ERROR: (in SQLExecutor.updateTuple) ((this.SQLInstruction.attrList == null) || (this.SQLInstruction.attrList.size() == 0))!");
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.updateTuple) SQLNode's attrList cannot be null!");
			return result;
		}
		// check SQLNode.attrList is not null
		if ((classStruct.attrList == null) || (classStruct.attrList.size() == 0)) {
			System.out.println("ERROR: (in SQLExecutor.updateTuple) ((classStruct.attrList == null) || (classStruct.attrList.size() == 0))!");
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.updateTuple) ((classStruct.attrList == null) || (classStruct.attrList.size() == 0))!");
			return result;
		}
		realAttributeList = this.vdisk.getRealAttributeList(classStruct);
		// check SQLNode.attrList's attribute valid
		for (i = 0; i < this.SQLInstruction.attrList.size(); i++) {
			attribute = this.SQLInstruction.attrList.get(i);
			if ((attribute.name == null) || attribute.name.equals("")) {
				System.out.printf("ERROR: (in SQLExecutor.updateTuple) attribute(NO.%d) ((attribute.name == null) || attribute.name.equals(\"\"))!\n", i);
				result.add(SQLExecutor.EXECUTE_FAIL);
				result.add("ERROR: (in SQLExecutor.updateTuple) attribute(NO." + Integer.toString(i) + ")'s name cannot be null!");
				return result;
			}
			// check attribute.expression is null
			if ((attribute.expression != null) && (!attribute.expression.equals(""))) {
				System.out.printf("ERROR: (in SQLExecutor.updateTuple) attribute(NO.%d) ((attribute.expression != null) && (!attribute.expression.equals(\"\")))!\n", i);
				result.add(SQLExecutor.EXECUTE_FAIL);
				result.add("ERROR: (in SQLExecutor.updateTuple) attribute(NO." + Integer.toString(i) + ")'s expression must be null!");
				return result;
			}
			// check is in realAttributeList
			for (j = 0; j < realAttributeList.size(); j++) {
				if (attribute.name.equals(realAttributeList.get(j).name)) {
					break;
				}
			}
			if (j == realAttributeList.size()) {
				System.out.printf("ERROR: (in SQLExecutor.updateTuple) attrName(%s) not in realAttributeList!\n", attribute.name);
				result.add(SQLExecutor.EXECUTE_FAIL);
				result.add("ERROR: (in SQLExecutor.updateTuple) attrName(" + attribute.name + ") not in realAttributeList!");
				return result;
			}
			// check attrList is a set
			for (j = i + 1; j < this.SQLInstruction.attrList.size(); j++) {
				if (attribute.name.equals(this.SQLInstruction.attrList.get(j).name)) {
					System.out.printf("ERROR: (in SQLExecutor.updateTuple) attribute(NO.%d)'name is not unique!\n", i);
					result.add(SQLExecutor.EXECUTE_FAIL);
					result.add("ERROR: (in SQLExecutor.updateTuple) attribute(NO." + Integer.toString(i) + ")'name is not unique!");
					return result;
				}
			}
			// check attribute.className not null
			if ((attribute.className == null) || attribute.className.equals("")) {
				System.out.printf("ERROR: (in SQLExecutor.updateTuple) attribute(NO.%d) ((attribute.className == null) || attribute.className.equals(\"\"))!\n", i);
				result.add(SQLExecutor.EXECUTE_FAIL);
				result.add("ERROR: (in SQLExecutor.updateTuple) attribute(NO." + Integer.toString(i) + ")'s className cannot be null!");
				return result;
			}
		}
		// check attrValueList not null
		if ((this.SQLInstruction.attrValueList == null) || (this.SQLInstruction.attrValueList.size() == 0)) {
			System.out.println("ERROR: (in SQLExecutor.updateTuple) ((this.SQLInstruction.attrValueList == null) || (this.SQLInstruction.attrValueList.size() == 0))!");
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.updateTuple) SQLNode's attrValueList cannot be null!");
			return result;
		}
		// check attrValueList.size() == attrList.size()
		if (this.SQLInstruction.attrValueList.size() != this.SQLInstruction.attrList.size()) {
			System.out.println("ERROR: (in SQLExecutor.updateTuple) (this.SQLInstruction.attrValueList.size() != this.SQLInstruction.attrList.size())!");
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.updateTuple) SQLNode's attrValueList.size() != attrList.size()!");
			return result;
		}
		// check attrValueList's element type
		for (i = 0; i < realAttributeList.size(); i++) {
			attribute = realAttributeList.get(i);
			for (j = 0; j < this.SQLInstruction.attrList.size(); j++) {
				if (attribute.name.equals(this.SQLInstruction.attrList.get(j).name)) {
					// check type
					if (attribute.type == Attribute.INT) {
						if (!dataStorer.canParseInt(this.SQLInstruction.attrValueList.get(j))) {
							System.out.printf("ERROR: (in SQLExecutor.updateTuple) attrValue(%s) cannot be parsed into INT!\n", this.SQLInstruction.attrValueList.get(j));
							result.add(SQLExecutor.EXECUTE_FAIL);
							result.add("ERROR: (in SQLExecutor.updateTuple) attrValue(" + this.SQLInstruction.attrValueList.get(j) + ") cannot be parsed into INT!");
							return result;
						}
					} else if (attribute.type == Attribute.STRING) {
						// do nothing
					} else {
						System.out.println("ERROR: (in SQLExecutor.updateTuple) unknown type!");
						result.add(SQLExecutor.EXECUTE_FAIL);
						result.add("ERROR: (in SQLExecutor.updateTuple) unknown type!");
						return result;
					}
					break;
				}
			}
		}
		// check SQLNode's where is not null
		if (this.SQLInstruction.where == null) {
			System.out.println("ERROR: (in SQLExecutor.updateTuple) (this.SQLInstruction.where == null)!");
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.updateTuple) SQLNode's where cannot be null!");
			return result;
		}

		// check where'identifier is in classStruct's attrList
		whereIdentifier = this.SQLInstruction.where.getAllIdentifier();
		if (whereIdentifier == null) {
			System.out.println("ERROR: (in SQLExecutor.updateTuple) (whereIdentifier == null)!");
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.updateTuple) (whereIdentifier == null)!");
			return result;
		}
		for (i = 0; i < whereIdentifier.size(); i++) {
			for (j = 0; j < classStruct.attrList.size(); j++) {
				if (whereIdentifier.get(i).equals(classStruct.attrList.get(j).name)) {
					break;
				}
			}
			if (j == classStruct.attrList.size()) {
				System.out.printf("ERROR: (in SQLExecutor.updateTuple) identifier(%s) not in classStruct(%s)'s attrList!\n", whereIdentifier.get(i), classStruct.name);
				result.add(SQLExecutor.EXECUTE_FAIL);
				result.add("ERROR: (in SQLExecutor.updateTuple) identifier(" + whereIdentifier.get(i) + ") not in classStruct(" + classStruct.name + ")'s attrList!");
				return result;
			}
		}

		// execute update
		// initial classStruct
		this.vdisk.initial(classStruct.name);
		while ((tuple = this.vdisk.Next()) != null) {
			fakeOffset = this.vdisk.getOffset();
			if (this.crossTupleSatisfied(this.SQLInstruction.where, classStruct, fakeOffset)) {
				// set tuple
				for (i = 0; i < realAttributeList.size(); i++) {
					attribute = realAttributeList.get(i);
					for (j = 0; j < this.SQLInstruction.attrList.size(); j++) {
						if (attribute.name.equals(this.SQLInstruction.attrList.get(j).name)) {
							// set tuple
							if (attribute.type == Attribute.INT) {
								tuple.set(i + 1, this.SQLInstruction.attrValueList.get(j));
							} else if (attribute.type == Attribute.STRING) {
								tuple.set(i + 1, this.SQLInstruction.attrValueList.get(j));
							}
							break;
						}
					}
				}
				// re-initial classStruct with fakeOffset
				this.vdisk.initial(classStruct.name, fakeOffset / dataStorer.PAGESIZE, fakeOffset % dataStorer.PAGESIZE);
				// update tuple
				if (!this.vdisk.update(tuple)) {
					System.out.printf("ERROR: (in SQLExecutor.updateTuple) this.vdisk update(%s) fail!\n", SQLExecutor.tuple2String(tuple));
					result.add(SQLExecutor.EXECUTE_FAIL);
					result.add("ERROR: (in SQLExecutor.updateTuple) this.vdisk update(" + SQLExecutor.tuple2String(tuple) + ") fail!");
					return result;
				}
				// check children
				tupleBiPointer = dataStorer.decode(tuple.get(0));
				for (i = 0; i < classStruct.children.size(); i++) {
					childrenClassStruct = this.vdisk.getClassStruct(classStruct.children.get(i));
					if (childrenClassStruct == null) {
						System.out.printf("ERROR: (in SQLExecutor.updateTuple) this.vdisk get childrenClassStruct(%s) fail!\n", classStruct.children.get(i));
						result.add(SQLExecutor.EXECUTE_FAIL);
						result.add("ERROR: (in SQLExecutor.updateTuple) this.vdisk get childrenClassStruct(" + classStruct.children.get(i) + ") fail!");
						return result;
					}
					try {
						conditionExpression = booleanParser.evaluate(childrenClassStruct.condition);
					} catch (ParseException ex) {
						System.err.println(ex.getMessage());
						result.add(SQLExecutor.EXECUTE_FAIL);
						result.add("ERROR: (in SQLExecutor.updateTuple) childrenClassStruct(" + childrenClassStruct.name + ")'s condition(" + childrenClassStruct.condition + ") cannot be parsed!");
						return result;
					}
					if (this.crossTupleSatisfied(conditionExpression, classStruct, fakeOffset)) {
						// must exist childrenTuple & biPointer
						if (tupleBiPointer.get(i + 1).equals(Integer.toString(dataStorer.DEFAULT_BIPOINTER))) {
							// do not exist, need insert
							childrenTuple = this.initTuple(childrenClassStruct);
							childrenTupleBiPointer = new ArrayList<String>();
							childrenTupleBiPointer.add(Integer.toString(fakeOffset));
							childrenTuple.set(0, dataStorer.encode(childrenTupleBiPointer));
							// insert childrenTuple
							// initial childrenClassStruct
							this.vdisk.initial(childrenClassStruct.name);
							if (!this.vdisk.insert(childrenClassStruct.name, childrenTuple)) {
								System.out.printf("ERROR: (in SQLExecutor.updateTuple) this.vdisk insert tuple into childrenClassStruct(%s) fail!\n", childrenClassStruct.name);
								result.add(SQLExecutor.EXECUTE_FAIL);
								result.add("ERROR: (in SQLExecutor.updateTuple) this.vdisk insert tuple into childrenClassStruct(" + childrenClassStruct.name + ") fail!");
								return result;
							}
							childrenFakeOffset = this.vdisk.getOffset();
							// call insertTupleHelper
							if (!this.insertTupleHelper(childrenClassStruct, childrenTuple, childrenTupleBiPointer, childrenFakeOffset)) {
								System.out.printf("ERROR: (in SQLExecutor.updateTuple) insertTupleHelper childrenClassStruct(%s) fail!\n", childrenClassStruct.name);
								result.add(SQLExecutor.EXECUTE_FAIL);
								result.add("ERROR: (in SQLExecutor.updateTuple) insertTupleHelper childrenClassStruct(" + childrenClassStruct.name + ") fail!");
								return result;
							}
							// set childrenFakeOffset into tupleBiPointer
							tupleBiPointer.set(i + 1, Integer.toString(childrenFakeOffset));
						}
					} else {
						// must deleted
						System.out.println("DELETE!!!!!!!!!!!");
						System.out.println(SQLExecutor.tuple2String(tupleBiPointer));
						System.out.println(tupleBiPointer.get(i + 1) + ", " + Integer.toString(i + 1));
						if (!tupleBiPointer.get(i + 1).equals(Integer.toString(dataStorer.DEFAULT_BIPOINTER))) {
							System.out.println("DELETE!!!!!!!!!!!");
							if (!dataStorer.canParseInt(tupleBiPointer.get(i + 1))) {
								System.out.printf("ERROR: (in SQLExecutor.updateTuple) biPointer(%s) cannot parse into INT!\n", tupleBiPointer.get(i + 1));
								result.add(SQLExecutor.EXECUTE_FAIL);
								result.add("ERROR: (in SQLExecutor.updateTuple) biPointer(" + tupleBiPointer.get(i + 1) + ") cannot parse into INT!");
								return result;
							}
							childrenFakeOffset = Integer.parseInt(tupleBiPointer.get(i + 1));
							// initial childrenClassStruct
							this.vdisk.initial(childrenClassStruct.name, childrenFakeOffset / dataStorer.PAGESIZE, childrenFakeOffset % dataStorer.PAGESIZE);
							// get childrenTuple
							if ((childrenTuple = this.vdisk.Next()) == null) {
								System.out.printf("ERROR: (in SQLExecutor.updateTuple) this.vdisk get childrenClassStruct(%s) next is null!\n", childrenClassStruct.name);
								result.add(SQLExecutor.EXECUTE_FAIL);
								result.add("ERROR: (in SQLExecutor.updateTuple) this.vdisk get childrenClassStruct(" + childrenClassStruct.name + ") next is null!");
								return result;
							}
							if (!this.deleteTupleHelper(childrenClassStruct, childrenFakeOffset, dataStorer.decode(childrenTuple.get(0)))) {
								System.out.printf("ERROR: (in SQLExecutor.updateTuple) deleteTupleHelper childrenClassStruct(%s) fail!\n", childrenClassStruct.name);
								result.add(SQLExecutor.EXECUTE_FAIL);
								result.add("ERROR: (in SQLExecutor.updateTuple) deleteTupleHelper childrenClassStruct(" + childrenClassStruct.name + ") fail!");
								return result;
							}
							// set DEFAULT_BIPOINTER into tupleBiPointer
							tupleBiPointer.set(i + 1, Integer.toString(dataStorer.DEFAULT_BIPOINTER));
							System.out.println(SQLExecutor.tuple2String(tupleBiPointer));
						}
					}
				}
				// set tupleBiPointer
				tuple.set(0, dataStorer.encode(tupleBiPointer));
				// re-initial classStruct with fakeOffset
				this.vdisk.initial(classStruct.name, fakeOffset / dataStorer.PAGESIZE, fakeOffset % dataStorer.PAGESIZE);
				// update tuple
				if (!this.vdisk.update(tuple)) {
					System.out.printf("ERROR: (in SQLExecutor.updateTuple) this.vdisk update(%s) fail!\n", SQLExecutor.tuple2String(tuple));
					result.add(SQLExecutor.EXECUTE_FAIL);
					result.add("ERROR: (in SQLExecutor.updateTuple) this.vdisk update(" + SQLExecutor.tuple2String(tuple) + ") fail!");
					return result;
				}
			}
			// re-initial classStruct with (fakeOffset + 1)
			this.vdisk.initial(classStruct.name, (fakeOffset + 1) / dataStorer.PAGESIZE, (fakeOffset + 1) % dataStorer.PAGESIZE);
		}

		// flush to vdisk
		if (!this.vdisk.flushToDisk()) {
			System.out.println("ERROR: (in SQLExecutor.updateTuple) this.vdisk flushToDisk fail!");
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.updateTuple) this.vdisk flushToDisk fail!");
			return result;
		}

		result.add(SQLExecutor.EXECUTE_SUCCESS);
		result.add("INFO: update tuple(" + this.SQLInstruction.where.toString() + ") successfully!");

		return result;
	}

	private ArrayList<String> selectTuple() {
		int i, j, fakeOffset, childrenFakeOffset;
		String className;
		Attribute attribute;
		classStruct classStruct, childrenClassStruct;
		whereNode conditionExpression;
		ArrayList<String> whereIdentifier, tuple, tupleBiPointer, childrenTuple, childrenTupleBiPointer;
		ArrayList<String> result = new ArrayList<String>(), resultElement, attrValue;

		// check SQLNode
		// check SQLNode.classNameList
		if ((this.SQLInstruction.classNameList == null) || (this.SQLInstruction.classNameList.size() != 1)) {
			System.out.println("ERROR: (in SQLExecutor.selectTuple) ((this.SQLInstruction.classNameList == null) || (this.SQLInstruction.classNameList.size() != 1))!");
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.selectTuple) SQLNode's classNameList can only hold one className!");
			return result;
		}
		// check whether className exists
		className = this.SQLInstruction.classNameList.get(0);
		if (!this.vdisk.existClass(className)) {
			System.out.printf("ERROR: (in SQLExecutor.selectTuple) className(%s) do not exists!\n", className);
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.selectTuple) className(" + className + ") do not exists!");
			return result;
		}
		// get classStruct
		classStruct = this.vdisk.getClassStruct(className);
		// check SQLNode.attrList not null
		if ((this.SQLInstruction.attrList == null) || (this.SQLInstruction.attrList.size() == 0)) {
			System.out.println("ERROR: (in SQLExecutor.selectTuple) ((this.SQLInstruction.attrList == null) || (this.SQLInstruction.attrList.size() == 0))!");
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.selectTuple) SQLNode's attrList cannot be null!");
			return result;
		}
		// check SQLNode.attrList is not null
		if ((classStruct.attrList == null) || (classStruct.attrList.size() == 0)) {
			System.out.println("ERROR: (in SQLExecutor.selectTuple) ((classStruct.attrList == null) || (classStruct.attrList.size() == 0))!");
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.selectTuple) ((classStruct.attrList == null) || (classStruct.attrList.size() == 0))!");
			return result;
		}
		// check SQLNode.attrList's attribute valid
		for (i = 0; i < this.SQLInstruction.attrList.size(); i++) {
			attribute = this.SQLInstruction.attrList.get(i);
			if ((attribute.expression == null) || attribute.expression.equals("")) {
				System.out.printf("ERROR: (in SQLExecutor.selectTuple) attribute(NO.%d) ((attribute.expression == null) || attribute.expression.equals(\"\"))!\n", i);
				result.add(SQLExecutor.EXECUTE_FAIL);
				result.add("ERROR: (in SQLExecutor.selectTuple) attribute(NO." + Integer.toString(i) + ")'s expression cannot be null!");
				return result;
			}
			// check attribute.name is null
			if ((attribute.name != null) && (!attribute.name.equals(""))) {
				System.out.printf("ERROR: (in SQLExecutor.selectTuple) attribute(NO.%d) ((attribute.name != null) && (!attribute.name.equals(\"\")))!\n", i);
				result.add(SQLExecutor.EXECUTE_FAIL);
				result.add("ERROR: (in SQLExecutor.selectTuple) attribute(NO." + Integer.toString(i) + ")'s name must be null!");
				return result;
			}
			// check expression is in classStruct's attrList
			for (j = 0; j < classStruct.attrList.size(); j++) {
				if (attribute.expression.equals(classStruct.attrList.get(j).name)) {
					break;
				}
			}
			if (j == classStruct.attrList.size()) {
				System.out.printf("ERROR: (in SQLExecutor.selectTuple) attrName(%s) not in classStruct.attrList!\n", attribute.expression);
				result.add(SQLExecutor.EXECUTE_FAIL);
				result.add("ERROR: (in SQLExecutor.selectTuple) attrName(" + attribute.expression + ") not in classStruct.attrList!");
				return result;
			}
			// check attrList is a set
			for (j = i + 1; j < this.SQLInstruction.attrList.size(); j++) {
				if (attribute.expression.equals(this.SQLInstruction.attrList.get(j).expression)) {
					System.out.printf("ERROR: (in SQLExecutor.selectTuple) attribute(NO.%d)'expression is not unique!\n", i);
					result.add(SQLExecutor.EXECUTE_FAIL);
					result.add("ERROR: (in SQLExecutor.selectTuple) attribute(NO." + Integer.toString(i) + ")'expression is not unique!");
					return result;
				}
			}
			// check attribute.className not null
			if ((attribute.className == null) || attribute.className.equals("")) {
				System.out.printf("ERROR: (in SQLExecutor.selectTuple) attribute(NO.%d) ((attribute.className == null) || attribute.className.equals(\"\"))!\n", i);
				result.add(SQLExecutor.EXECUTE_FAIL);
				result.add("ERROR: (in SQLExecutor.selectTuple) attribute(NO." + Integer.toString(i) + ")'s className cannot be null!");
				return result;
			}
		}
		// check SQLNode'attrValueList is null
		if ((this.SQLInstruction.attrValueList != null) && (!this.SQLInstruction.attrValueList.equals(""))) {
			System.out.println("ERROR: (in SQLExecutor.selectTuple) ((this.SQLInstruction.attrValueList != null) && (!this.SQLInstruction.attrValueList.equals(\"\")))");
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.selectTuple) SQLNode'attrValueList must be null!");
			return result;
		}
		// check where (can be either null or not null)
		if (this.SQLInstruction.where != null) {
			// check where'identifier is in classStruct's attrList
			whereIdentifier = this.SQLInstruction.where.getAllIdentifier();
			if (whereIdentifier == null) {
				System.out.println("ERROR: (in SQLExecutor.selectTuple) (whereIdentifier == null)!");
				result.add(SQLExecutor.EXECUTE_FAIL);
				result.add("ERROR: (in SQLExecutor.selectTuple) (whereIdentifier == null)!");
				return result;
			}
			for (i = 0; i < whereIdentifier.size(); i++) {
				for (j = 0; j < classStruct.attrList.size(); j++) {
					if (whereIdentifier.get(i).equals(classStruct.attrList.get(j).name)) {
						break;
					}
				}
				if (j == classStruct.attrList.size()) {
					System.out.printf("ERROR: (in SQLExecutor.selectTuple) identifier(%s) not in classStruct(%s)'s attrList!\n", whereIdentifier.get(i), classStruct.name);
					result.add(SQLExecutor.EXECUTE_FAIL);
					result.add("ERROR: (in SQLExecutor.selectTuple) identifier(" + whereIdentifier.get(i) + ") not in classStruct(" + classStruct.name + ")'s attrList!");
					return result;
				}
			}
		}

		// execute select tuple
		// initial classStruct
		this.vdisk.initial(classStruct.name);
		while ((tuple = this.vdisk.Next()) != null) {
			fakeOffset = this.vdisk.getOffset();
			if (this.crossTupleSatisfied(this.SQLInstruction.where, classStruct, fakeOffset)) {
				resultElement = new ArrayList<String>();
				for (i = 0; i < this.SQLInstruction.attrList.size(); i++) {
					attrValue = attributeGetSource(classStruct, this.SQLInstruction.attrList.get(i).expression, fakeOffset);
					if (attrValue == null) {
						System.out.println("ERROR: (in SQLExecutor.selectTuple) (attrValue == null)!");
						result = new ArrayList<String>();
						result.add(SQLExecutor.EXECUTE_FAIL);
						result.add("ERROR: (in SQLExecutor.selectTuple) (attrValue == null)!");
						return result;
					}
					resultElement.add(attrValue.get(4));
				}
				result.add(dataStorer.encode(resultElement));
			}
			// update fakeOffset, re-initial classStruct
			this.vdisk.initial(classStruct.name, (fakeOffset + 1) / dataStorer.PAGESIZE, (fakeOffset + 1) % dataStorer.PAGESIZE);
		}

		// after select tuple
		resultElement = new ArrayList<String>();
		for (i = 0; i < this.SQLInstruction.attrList.size(); i++) {
			resultElement.add(this.SQLInstruction.attrList.get(i).expression);
		}
		result.add(0, dataStorer.encode(resultElement));
		if (this.SQLInstruction.where != null) {
			result.add(0, "INFO: select tuple(" + this.SQLInstruction.where.toString() + ") from class(" + classStruct.name + ") successfully!");
		} else {
			result.add(0, "INFO: select tuple(" + ") from class(" + classStruct.name + ") successfully!");
		}
		result.add(0, SQLExecutor.EXECUTE_SUCCESS);

		return result;
	}

	private boolean crossTupleSatisfied(whereNode condition, classStruct classStruct, int fakeOffset) {
		boolean result;
		int i, j, k, l;
		String identifier, tupleElement;
		Attribute attribute;
		ArrayList<Attribute> realAttributeList;
		ArrayList<String> conditionIdentifier, parentTuple;
		Map<String, Integer> intMap = new HashMap<>();
		Map<String, String> stringMap = new HashMap<>();

		// check params
		if ((classStruct == null) || (classStruct.attrList == null)) {
			System.out.println("ERROR: (in SQLExecutor.crossTupleSatisfied) ((classStruct == null) || (classStruct.attrList == null))!");
			return false;
		}

		if (condition == null) {
			return true;
		}

		conditionIdentifier = condition.getAllIdentifier();
		// check condition identifier
		if (conditionIdentifier == null) {
			System.out.println("ERROR: (in SQLExecutor.crossTupleSatisfied) (conditionIdentifier == null)!");
			return false;
		}
		// only support real-attr for now
		realAttributeList = this.vdisk.getRealAttributeList(classStruct);
		if (realAttributeList == null) {
			System.out.println("ERROR: (in SQLExecutor.crossTupleSatisfied) (realAttributeList == null)!");
			return false;
		}
		for (i = 0; i < conditionIdentifier.size(); i++) {
			identifier = conditionIdentifier.get(i);
			if (!this.crossGetValue(classStruct, identifier, fakeOffset, intMap, stringMap)) {
				System.out.printf("ERROR: (in SQLExecutor.crossTupleSatisfied) class(%s) do not have attr(%s)!\n", classStruct.name, identifier);
				return false;
			}
		}
		// all map ready, call booleanExecutor
		result = booleanExecutor.calculate(condition.toString(), intMap, stringMap);

		return result;
	}

	private ArrayList<String> virtualAttributeGetSource(classStruct classStruct, String attributeName) {
		// TODO
		return null;
	}

	private ArrayList<String> attributeGetSource(classStruct classStruct, String attributeName, int fakeOffset) {
		int i, j;
		classStruct parentClassStruct;
		calculationNode calculationExpression;
		ArrayList<Attribute> realAttributeList;
		ArrayList<String> tuple, expressionIdentifier;
		ArrayList<String> result = new ArrayList<String>(), subResult;
		Map<String, Integer> intMap = new HashMap<>();

		// check params
		if ((classStruct == null) || (attributeName == null) || (fakeOffset == dataStorer.DEFAULT_BIPOINTER)) {
			System.out.printf("ERROR: (in SQLExecutor.attributeGetSource) ((classStruct == null) || (attributeName == null) || (fakeOffset(%d) == dataStorer.DEFAULT_BIPOINTER))!\n", fakeOffset);
			return null;
		}

		// check whether in classStruct's realAttributeList
		realAttributeList = this.vdisk.getRealAttributeList(classStruct);
		if (realAttributeList == null) {
			System.out.println("ERROR: (in SQLExecutor.attributeGetSource) (realAttributeList == null)!");
			return null;
		}
		for (i = 0 ; i < realAttributeList.size(); i++) {
			if (realAttributeList.get(i).name.equals(attributeName)) {
				// find real-attr
				// initial classStruct
				this.vdisk.initial(classStruct.name, fakeOffset / dataStorer.PAGESIZE, fakeOffset % dataStorer.PAGESIZE);
				// get tuple
				if ((tuple = this.vdisk.Next()) == null) {
					System.out.println("ERROR: (in SQLExecutor.attributeGetSource) ((tuple = this.vdisk.Next()) == null)!");
					return null;
				}
				result.add(classStruct.name);
				result.add(Integer.toString(realAttributeList.get(i).type));
				result.add(attributeName);
				result.add(realAttributeList.get(i).expression);
				result.add(tuple.get(i + 1));

				return result;
			}
		}
		// not in real-attr, check v-attr
		for (i = 0; i < classStruct.attrList.size(); i++) {
			if (classStruct.attrList.get(i).name.equals(attributeName)) {
				if ((classStruct.attrList.get(i).expression != null) && (!classStruct.attrList.get(i).expression.equals(""))) {
					// v-attr
					// get parentClassStruct
					// whether classStruct has parent
					if ((classStruct.parent == null) || (classStruct.parent.equals(""))) {
						System.out.println("ERROR: (in SQLExecutor.attributeGetSource) ((classStruct.parent == null) || (classStruct.parent.equals(\"\")))!");
						return null;
					}

					// get parentClassStruct
					parentClassStruct = this.vdisk.getClassStruct(classStruct.parent);
					if (parentClassStruct == null) {
						System.out.println("ERROR: (in SQLExecutor.attributeGetSource) (parentClassStruct == null)!");
						return null;
					}
					try {
						calculationExpression = calculationParser.evaluate(classStruct.attrList.get(i).expression);
					} catch (ParseException ex) {
						System.err.println(ex.getMessage());
						System.out.println("ERROR: (in SQLExecutor.attributeGetSource) classStruct.attrList.get(i).expression cannot be parsed!");
						return null;
					}
					expressionIdentifier = calculationExpression.getAllIdentifier();
					if ((expressionIdentifier == null) || (expressionIdentifier.size() == 0)) {
						System.out.println("ERROR: (in SQLExecutor.attributeGetSource) (expressionIdentifier == null)!");
						return null;
					}
					// initial classStruct
					this.vdisk.initial(classStruct.name, fakeOffset / dataStorer.PAGESIZE, fakeOffset % dataStorer.PAGESIZE);
					// get tuple
					if (((tuple = this.vdisk.Next()) == null) || (tuple.size() == 0)) {
						System.out.println("ERROR: (in SQLExecutor.attributeGetSource) ((tuple = this.vdisk.Next()) == null)!");
						return null;
					}
					if (!dataStorer.canParseInt(dataStorer.decode(tuple.get(0)).get(0))) {
						System.out.println("ERROR: (in SQLExecutor.attributeGetSource) !dataStorer.canParseInt(dataStorer.decode(tuple.get(0)).get(0))!");
						return null;
					}
					System.out.println(SQLExecutor.tuple2String(tuple));
					System.out.println(classStruct.name + ", " + attributeName + ", " + Integer.toString(fakeOffset));
					// suppose attributeType
					if (classStruct.attrList.get(i).type == Attribute.STRING) {
						// is a string
						subResult = this.attributeGetSource(parentClassStruct, expressionIdentifier.get(0), Integer.parseInt(dataStorer.decode(tuple.get(0)).get(0)));
						if (subResult == null) {
							System.out.println("ERROR: (in SQLExecutor.attributeGetSource) (subResult == null)!");
							return null;
						}
						if (!dataStorer.canParseInt(subResult.get(1))) {
							System.out.println("ERROR: (in SQLExecutor.attributeGetSource) !dataStorer.canParseInt(subResult.get(1))!");
							return null;
						}
						if (Integer.parseInt(subResult.get(1)) != Attribute.STRING) {
							System.out.println("ERROR: (in SQLExecutor.attributeGetSource) (Integer.parseInt(subResult.get(1)) != Attribute.STRING)!");
							return null;
						}
						result.add(classStruct.name);
						result.add(Integer.toString(Attribute.STRING));
						result.add(attributeName);
						result.add(classStruct.attrList.get(i).expression);
						result.add(subResult.get(4));
					} else {
						// all INT
						for (j = 0; j < expressionIdentifier.size(); j++) {
							subResult = this.attributeGetSource(parentClassStruct, expressionIdentifier.get(j), Integer.parseInt(dataStorer.decode(tuple.get(0)).get(0)));
							if (subResult == null) {
								System.out.println("ERROR: (in SQLExecutor.attributeGetSource) (subResult == null)!");
								return null;
							}
							if (!dataStorer.canParseInt(subResult.get(1))) {
								System.out.println("ERROR: (in SQLExecutor.attributeGetSource) !dataStorer.canParseInt(subResult.get(1))!");
								return null;
							}
							if (Integer.parseInt(subResult.get(1)) != Attribute.INT) {
								System.out.println("ERROR: (in SQLExecutor.attributeGetSource) (Integer.parseInt(subResult.get(1)) != Attribute.INT)!");
								return null;
							}
							intMap.put(subResult.get(2), Integer.parseInt(subResult.get(4)));
						}
						// all intMap get, let's calculate
						result.add(classStruct.name);
						result.add(Integer.toString(Attribute.INT));
						result.add(attributeName);
						result.add(classStruct.attrList.get(i).expression);
						result.add(Integer.toString(calculationExecutor.calculate(classStruct.attrList.get(i).expression, intMap)));;
					}

					return result;
				} else {
					// initial classStruct
					this.vdisk.initial(classStruct.name, fakeOffset / dataStorer.PAGESIZE, fakeOffset % dataStorer.PAGESIZE);
					// get tuple
					if ((tuple = this.vdisk.Next()) == null) {
						System.out.println("ERROR: (in SQLExecutor.attributeGetSource) ((tuple = this.vdisk.Next()) == null)!");
						return null;
					}
					result.add(classStruct.name);
					result.add(Integer.toString(classStruct.attrList.get(i).type));
					result.add(attributeName);
					result.add(classStruct.attrList.get(i).expression);
					result.add(tuple.get(i + 1));

					return result;
				}
			}
		}
		// not find
		System.out.println("ERROR: (in SQLExecutor.attributeGetSource) not find!");
		return null;
	}

	private boolean crossGetValue(classStruct classStruct, String identifier, int fakeOffset,  Map<String, Integer> intMap, Map<String, String> stringMap) {
		ArrayList<String> value;

		// check params
		if ((classStruct == null) || (identifier == null) || (identifier.equals("")) || (intMap == null) || (stringMap == null)) {
			System.out.println("ERROR: (in SQLExecutor.crossGetValue) ((classStruct == null) || (identifier == null) || (identifier.equals(\"\")) || (intMap == null) || (stringMap == null))!");
			return false;
		}

		value = this.attributeGetSource(classStruct, identifier, fakeOffset);
		if ((value == null) || (value.size() != 5)) {
			System.out.println("ERROR: (in SQLExecutor.crossGetValue) ((value == null) || (value.size() != 5))!");
			return false;
		}
		if (!dataStorer.canParseInt(value.get(1))) {
			System.out.println("ERROR: (in SQLExecutor.crossGetValue) (!dataStorer.canParseInt(value.get(1)))!");
			return false;
		}
		if (Integer.parseInt(value.get(1)) == Attribute.INT) {
			if (!dataStorer.canParseInt(value.get(4))) {
				System.out.println("ERROR: (in SQLExecutor.crossGetValue) (!dataStorer.canParseInt(value.get(4)))!");
				return false;
			}
			intMap.put(value.get(2), Integer.parseInt(value.get(4)));
		} else if (Integer.parseInt(value.get(1)) == Attribute.STRING) {
			stringMap.put(value.get(2), value.get(4));
		} else {
			System.out.println("ERROR: (in SQLExecutor.crossGetValue) unknown Attribute type!");
			return false;
		}

		return true;
	}	

	private ArrayList<String> crossSelectTuple() {
		int i, j, k, fakeOffset, parentFakeOffset, targetFakeOffset;
		String className, targetClassName, preTargetClassName;
		Attribute attribute, targetAttribute;
		classStruct classStruct, parentClassStruct, targetClassStruct, preTargetClassStruct;
		whereNode conditionExpression;
		ArrayList<String> whereIdentifier, tuple, parentTuple, targetTuple;
		ArrayList<String> result = new ArrayList<String>(), resultElement, attrValue, targetClassNameList, targetClassNameListElement;

		// check SQLNode
		// check SQLNode.classNameList
		if ((this.SQLInstruction.classNameList == null) || (this.SQLInstruction.classNameList.size() <= 1)) {
			System.out.println("ERROR: (in SQLExecutor.crossSelectTuple) ((this.SQLInstruction.classNameList == null) || (this.SQLInstruction.classNameList.size() <= 1))!");
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.crossSelectTuple) SQLNode's classNameList can only hold one className!");
			return result;
		}
		// check whether className exists
		className = this.SQLInstruction.classNameList.get(this.SQLInstruction.classNameList.size() - 1);
		if (!this.vdisk.existClass(className)) {
			System.out.printf("ERROR: (in SQLExecutor.crossSelectTuple) className(%s) do not exists!\n", className);
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.crossSelectTuple) className(" + className + ") do not exists!");
			return result;
		}
		// get classStruct
		classStruct = this.vdisk.getClassStruct(className);
		// check SQLNode.attrList not null
		if ((this.SQLInstruction.attrList == null) || (this.SQLInstruction.attrList.size() == 0)) {
			System.out.println("ERROR: (in SQLExecutor.crossSelectTuple) ((this.SQLInstruction.attrList == null) || (this.SQLInstruction.attrList.size() == 0))!");
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.crossSelectTuple) SQLNode's attrList cannot be null!");
			return result;
		}
		// check SQLNode.attrList is not null
		if ((classStruct.attrList == null) || (classStruct.attrList.size() == 0)) {
			System.out.println("ERROR: (in SQLExecutor.crossSelectTuple) ((classStruct.attrList == null) || (classStruct.attrList.size() == 0))!");
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.crossSelectTuple) ((classStruct.attrList == null) || (classStruct.attrList.size() == 0))!");
			return result;
		}
		// get targetClassNameList
		targetClassNameList = new ArrayList<String>();
		targetClassNameListElement = new ArrayList<String>();
		for (i = 0; i < this.SQLInstruction.classNameList.size() - 1; i++) {
			targetClassNameListElement.add(this.SQLInstruction.classNameList.get(i));
			if (this.SQLInstruction.classNameList.get(i + 1).equals(className)) {
				targetClassNameList.add(dataStorer.encode(targetClassNameListElement));
				targetClassNameListElement = new ArrayList<String>();
				// no break
			}
		}
		// check attrList's attr has class
		for (i = 0; i < this.SQLInstruction.attrList.size(); i++) {
			attribute = this.SQLInstruction.attrList.get(i);
			targetClassName = attribute.className;
			// check targetClass exists
			if (!this.vdisk.existClass(targetClassName)) {
				System.out.printf("ERROR: (in SQLExecutor.crossSelectTuple) targetClassName(%s) do not exists!\n", targetClassName);
				result.add(SQLExecutor.EXECUTE_FAIL);
				result.add("ERROR: (in SQLExecutor.crossSelectTuple) targetClassName(" + targetClassName + ") do not exists!");
				return result;
			}
			// get targetClassStruct
			targetClassStruct = this.vdisk.getClassStruct(targetClassName);
			// check SQLNode.attrList not null
			for (j = 0; j < targetClassStruct.attrList.size(); j++) {
				targetAttribute = targetClassStruct.attrList.get(j);
				if (targetAttribute.name.equals(attribute.name)) {
					break;
				}
			}
			if (j == targetClassStruct.attrList.size()) {
				System.out.printf("ERROR: (in SQLExecutor.crossSelectTuple) targetClassName(%s) do not have attr(%s)!\n", targetClassName, attribute.name);
				result.add(SQLExecutor.EXECUTE_FAIL);
				result.add("ERROR: (in SQLExecutor.crossSelectTuple) targetClassName(" + targetClassName + ") do not have attr(" + attribute.name + ")!");
				return result;
			}
		}
		// check SQLNode'attrValueList is null
		if ((this.SQLInstruction.attrValueList != null) && (!this.SQLInstruction.attrValueList.equals(""))) {
			System.out.println("ERROR: (in SQLExecutor.crossSelectTuple) ((this.SQLInstruction.attrValueList != null) && (!this.SQLInstruction.attrValueList.equals(\"\")))");
			result.add(SQLExecutor.EXECUTE_FAIL);
			result.add("ERROR: (in SQLExecutor.crossSelectTuple) SQLNode'attrValueList must be null!");
			return result;
		}
		// check where (can be either null or not null)
		if (this.SQLInstruction.where != null) {
			// check where'identifier is in classStruct's attrList
			whereIdentifier = this.SQLInstruction.where.getAllIdentifier();
			if (whereIdentifier == null) {
				System.out.println("ERROR: (in SQLExecutor.crossSelectTuple) (whereIdentifier == null)!");
				result.add(SQLExecutor.EXECUTE_FAIL);
				result.add("ERROR: (in SQLExecutor.crossSelectTuple) (whereIdentifier == null)!");
				return result;
			}
			for (i = 0; i < whereIdentifier.size(); i++) {
				for (j = 0; j < classStruct.attrList.size(); j++) {
					if (whereIdentifier.get(i).equals(classStruct.attrList.get(j).name)) {
						break;
					}
				}
				if (j == classStruct.attrList.size()) {
					System.out.printf("ERROR: (in SQLExecutor.crossSelectTuple) identifier(%s) not in classStruct(%s)'s attrList!\n", whereIdentifier.get(i), classStruct.name);
					result.add(SQLExecutor.EXECUTE_FAIL);
					result.add("ERROR: (in SQLExecutor.crossSelectTuple) identifier(" + whereIdentifier.get(i) + ") not in classStruct(" + classStruct.name + ")'s attrList!");
					return result;
				}
			}
		}

		// execute cross select tuple
		// initial classStruct
		this.vdisk.initial(classStruct.name);
		while ((tuple = this.vdisk.Next()) != null) {
			fakeOffset = this.vdisk.getOffset();
			if (this.crossTupleSatisfied(this.SQLInstruction.where, classStruct, fakeOffset)) {
				resultElement = new ArrayList<String>();
				for (i = 0; i < this.SQLInstruction.attrList.size(); i++) {
					targetClassStruct = this.vdisk.getClassStruct(this.SQLInstruction.attrList.get(i).className);
					targetClassNameListElement = dataStorer.decode(targetClassNameList.get(i));
					if (targetClassNameListElement == null) {
						System.out.println("ERROR: (in SQLExecutor.selectTuple) (targetClassNameListElement == null)!");
						result = new ArrayList<String>();
						result.add(SQLExecutor.EXECUTE_FAIL);
						result.add("ERROR: (in SQLExecutor.selectTuple) (targetClassNameListElement == null)!");
						return result;
					}
					// get targetFakeOffset
					targetFakeOffset = fakeOffset;
					targetClassName = className;
					for (j = 1; j < targetClassNameListElement.size(); j++) {
						preTargetClassName = targetClassName;
						// get preTargetClassStruct
						preTargetClassStruct = this.vdisk.getClassStruct(preTargetClassName);
						if (preTargetClassStruct == null) {
							System.out.println("ERROR: (in SQLExecutor.selectTuple) (preTargetClassStruct == null)!");
							result = new ArrayList<String>();
							result.add(SQLExecutor.EXECUTE_FAIL);
							result.add("ERROR: (in SQLExecutor.selectTuple) (preTargetClassStruct == null)!");
							return result;
						}
						targetClassName = targetClassNameListElement.get(j);
						// initial preTargetClassName
						this.vdisk.initial(preTargetClassName, targetFakeOffset / dataStorer.PAGESIZE, targetFakeOffset % dataStorer.PAGESIZE);
						// get Next
						if ((targetTuple = this.vdisk.Next()) == null) {
							System.out.println("ERROR: (in SQLExecutor.selectTuple) ((targetTuple = this.vdisk.Next()) == null)!");
							result = new ArrayList<String>();
							result.add(SQLExecutor.EXECUTE_FAIL);
							result.add("ERROR: (in SQLExecutor.selectTuple) ((targetTuple = this.vdisk.Next()) == null)!");
							return result;
						}
						// update targetOffset
						if ((preTargetClassStruct.parent != null) && (targetClassName.equals(preTargetClassStruct.parent))) {
							// targetClass is preTargetClass'parent
							targetFakeOffset = Integer.parseInt(dataStorer.decode(targetTuple.get(0)).get(0));
						} else {
							if (preTargetClassStruct.children == null) {
								System.out.println("ERROR: (in SQLExecutor.selectTuple) (preTargetClassStruct.children == null)!");
								result = new ArrayList<String>();
								result.add(SQLExecutor.EXECUTE_FAIL);
								result.add("ERROR: (in SQLExecutor.selectTuple) (preTargetClassStruct.children == null)!");
								return result;
							}
							for (k = 0; k < preTargetClassStruct.children.size(); k++) {
								if (preTargetClassStruct.children.get(k).equals(targetClassName)) {
									targetFakeOffset = Integer.parseInt(dataStorer.decode(targetTuple.get(0)).get(k + 1));
									break;
								}
							}
							if (k == preTargetClassStruct.children.size()) {
								System.out.println("ERROR: (in SQLExecutor.selectTuple) do not find targetClass in preTargetClass's children!");
								result = new ArrayList<String>();
								result.add(SQLExecutor.EXECUTE_FAIL);
								result.add("ERROR: (in SQLExecutor.selectTuple) do not find targetClass in preTargetClass's children!");
								return result;
							}
						}
					}
					attrValue = attributeGetSource(targetClassStruct, this.SQLInstruction.attrList.get(i).name, targetFakeOffset);
					if (attrValue == null) {
						System.out.println("ERROR: (in SQLExecutor.selectTuple) (attrValue == null)!");
						result = new ArrayList<String>();
						result.add(SQLExecutor.EXECUTE_FAIL);
						result.add("ERROR: (in SQLExecutor.selectTuple) (attrValue == null)!");
						return result;
					}
					resultElement.add(attrValue.get(4));
				}
				result.add(dataStorer.encode(resultElement));
			}
			// update fakeOffset, re-initial classStruct
			this.vdisk.initial(classStruct.name, (fakeOffset + 1) / dataStorer.PAGESIZE, (fakeOffset + 1) % dataStorer.PAGESIZE);
		}

		// after select tuple
		resultElement = new ArrayList<String>();
		for (i = 0; i < this.SQLInstruction.attrList.size(); i++) {
			resultElement.add(this.SQLInstruction.attrList.get(i).name);
		}
		result.add(0, dataStorer.encode(resultElement));
		if (this.SQLInstruction.where != null) {
			result.add(0, "INFO: cross select tuple(" + this.SQLInstruction.where.toString() + ") from class(" + classStruct.name + ") successfully!");
		} else {
			result.add(0, "INFO: cross select tuple(" + ") from class(" + classStruct.name + ") successfully!");
		}
		result.add(0, SQLExecutor.EXECUTE_SUCCESS);

		return result;
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

		
