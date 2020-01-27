import java.util.ArrayList;

public class SQLNode {
	final static int CREATE_CLASS = 0, CREATE_SELECT_DEPUTY_CLASS = 1, DROP_CLASS = 2, INSERT_TUPLE = 3, DELETE_TUPLE = 4, UPDATE_TUPLE = 5, SELECT_TUPLE = 6, CROSS_SELECT_TUPLE = 7;

	public int type;
	public ArrayList<String> classNameList;
	public ArrayList<Attribute> attrList;
	public ArrayList<String> attrValueList;
	public whereNode where;

	public SQLNode() {
		this.type = SQLNode.CREATE_CLASS;
		this.classNameList = null;
		this.attrList = null;
		this.attrValueList = null;
		this.where = null;
	}

	public void insertClassNameList(String className) {
		if (this.classNameList == null) {
			this.classNameList = new ArrayList<String>();
		}
		this.classNameList.add(className);
	}

	public void insertAttrList(Attribute attribute) {
		if (this.attrList == null) {
			this.attrList = new ArrayList<Attribute>();
		}
		this.attrList.add(attribute);
	}

	public void insertAttrList(Attribute attribute, String className) {
		if (this.attrList == null) {
			this.attrList = new ArrayList<Attribute>();
		}
		attribute.className = className;
		this.attrList.add(attribute);
	}

	public void insertAttrList(ArrayList<Attribute> attributeList) {
		if (this.attrList == null) {
			this.attrList = new ArrayList<Attribute>();
		}
		if (attributeList == null) {
			return;
		}
		for (int i = 0; i < attributeList.size(); i++) {
			this.attrList.add(attributeList.get(i));
		}
	}

	public void insertAttrList(ArrayList<Attribute> attributeList, String className) {
		if (this.attrList == null) {
			this.attrList = new ArrayList<Attribute>();
		}
		if (attributeList == null) {
			return;
		}
		for (int i = 0; i < attributeList.size(); i++) {
			attributeList.get(i).className = className;
			this.attrList.add(attributeList.get(i));
		}
	}

	public void insertAttrValueList(String attrValue) {
		if (this.attrValueList == null) {
			this.attrValueList = new ArrayList<String>();
		}
		this.attrValueList.add(attrValue);
	}

	public void insertAttrValueList(ArrayList<String> attrValueList) {
		if (this.attrValueList == null) {
			this.attrValueList = new ArrayList<String>();
		}
		if (attrValueList == null) {
			return;
		}
		for (int i = 0; i < attrValueList.size(); i++) {
			this.attrValueList.add(attrValueList.get(i));
		}
	}

	public String toString() {
		String result = "";
		Attribute attribute;

		// type
		if (this.type == SQLNode.CREATE_CLASS) {
			result += "CREATE_CLASS: \n";
		} else if (this.type == SQLNode.CREATE_SELECT_DEPUTY_CLASS) {
			result += "CREATE_SELECT_DEPUTY_CLASS: \n";
		} else if (this.type == SQLNode.DROP_CLASS) {
			result += "DROP_CLASS: \n";
		} else if (this.type == SQLNode.INSERT_TUPLE) {
			result += "INSERT_TUPLE: \n";
		} else if (this.type == SQLNode.DELETE_TUPLE) {
			result += "DELETE_TUPLE: \n";
		} else if (this.type == SQLNode.UPDATE_TUPLE) {
			result += "UPDATE_TUPLE: \n";
		} else if (this.type == SQLNode.SELECT_TUPLE) {
			result += "SELECT_TUPLE: \n";
		} else if (this.type == SQLNode.CROSS_SELECT_TUPLE) {
			result += "CROSS_SELECT_TUPLE: \n";
		} else {
			System.out.println("ERROR: (SQLNode.toString()) unknown type!");
			return null;
		}
		// classNameList
		result += "\tclassNameList:\n";
		if (this.classNameList == null) {
			result += "\t\tnull!\n";
		} else {
			for (int i = 0; i < this.classNameList.size(); i++) {
				result += "\t\t" + this.classNameList.get(i) + "\n";
			}
		}
		// attrList
		result += "\tattrList:\n";
		if (this.attrList == null) {
			result += "\t\tnull!\n";
		} else {
			for (int i = 0; i < this.attrList.size(); i++) {
				attribute = this.attrList.get(i);
				result += "\t\t";
				// type
				if (attribute.type == Attribute.INT) {
					result += "INT";
				} else if (attribute.type == Attribute.STRING) {
					result += "STRING";
				} else {
					System.out.println("ERROR: (SQLNode.toString()) unknown Attribute.type!");
					return null;
				}
				// name
				if (attribute.name == null) {
					result += "\tnull";
				} else {
					result += "\t" + attribute.name;
				}
				// expression
				if (attribute.expression == null) {
					result += "\tnull";
				} else {
					result += "\t" + attribute.expression;
				}
				// className
				if (attribute.className == null) {
					result += "\tnull";
				} else {
					result += "\t" + attribute.className;
				}
				result += "\n";
			}
		}
		// attrList
		result += "\tattrValueList:\n";
		if (this.attrValueList == null) {
			result += "\t\tnull!\n";
		} else {
			for (int i = 0; i < this.attrValueList.size(); i++) {
				result += "\t\t" + this.attrValueList.get(i) + "\n";
			}
		}
		// where
		result += "\twhere:\n";
		if (this.where == null) {
			result += "\t\tnull!\n";
		} else {
			result += "\t\t" + this.where.toString() + "\n";
		}

		return result;
	}
}

