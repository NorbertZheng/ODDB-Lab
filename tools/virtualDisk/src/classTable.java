import java.util.ArrayList;

public class classTable {
	public final static int N_ATTR = 4;
	public final static int originClass = 0, deputyClass = 1;

	public String className;
	public int classId;
	public int attrNum;
	public int classType;

	public classTable() {
		this.className = null;
		this.classId = 0;
		this.attrNum = 0;
		this.classType = classTable.originClass;
	}

	public classTable(ArrayList<String> src) {
		this.className = src.get(0);
		this.classId = Integer.parseInt(src.get(1));
		this.attrNum = Integer.parseInt(src.get(2));
		this.classType = Integer.parseInt(src.get(3));
	}

	public ArrayList<String> class2StringList() {
		ArrayList<String> data = new ArrayList<String>();

		if (this.className == null) {
			data.add("");
		} else {
			data.add(this.className);
		}
		data.add(Integer.toString(this.classId));
		data.add(Integer.toString(this.attrNum));
		data.add(Integer.toString(this.classType));

		return data;
	}
}

