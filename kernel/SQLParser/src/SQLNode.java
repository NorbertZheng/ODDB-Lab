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

	public void insertAttrValueList(String attrValue) {
		if (this.attrValueList == null) {
			this.attrValueList = new ArrayList<String>();
		}
		this.attrValueList.add(attrValue);
	}
}

