import java.util.ArrayList;

public class classStruct {

	public String name;
	public String parent;
	public ArrayList<String> children;
	public ArrayList<Attribute> attrList;
	public String condition;

	public classStruct() {
		this.name = null;
		this.parent = null;
		this.children = null;
		this.attrList = null;
		this.condition = null;
	}

	public void insertChildren(String child) {
		if (this.children == null) {
			this.children = new ArrayList<String>();
		}
		this.children.add(child);
	}

	public void insertAttrList(Attribute attribute) {
		if (this.attrList == null) {
			this.attrList = new ArrayList<Attribute>();
		}
		this.attrList.add(attribute);
	}
}
	
	
