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

	public String toString() {
		String result = "";

		// name
		result += "classStruct.name:\t";
		if ((this.name == null) || (this.name.equals(""))) {
			System.out.println("ERROR: (in classStruct.toString) ((this.name == null) || (this.name.equals(\"\")))!");
			return "";
		} else {
			result += this.name + "\n";
		}
		// parent
		result += "\tclassStruct.parent:\n";
		if ((this.parent == null) || (this.parent.equals(""))) {
			result += "\t\tnull!\n";
		} else {
			result += "\t\t" + this.parent + "\n";
		}
		// children
		result += "\tclassStruct.children:\n";
		if ((this.children == null) || (this.children.size() == 0)) {
			result += "\t\tnull!\n";
		} else {
			for (int i = 0; i < this.children.size(); i++) {
				result += "\t\t" + this.children.get(i) + "\n";
			}
		}
		// attrList
		result += "\tclassStruct.attrList:\n";
		if ((this.attrList == null) || (this.attrList.size() == 0)) {
			result += "\t\tnull!\n";
		} else {
			for (int i = 0; i < this.attrList.size(); i++) {
				result += "\t\t" + this.attrList.get(i).toString() + "\n";
			}
		}
		// condition
		result += "\tclassStruct.condition:\n";
		if ((this.condition == null) || (this.condition.equals(""))) {
			result += "\t\tnull!\n";
		} else {
			result += "\t\t" + this.condition + "\n";
		}

		return result;
	}

}
	
	
