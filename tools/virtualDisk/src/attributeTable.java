import java.util.ArrayList;

public class attributeTable {
	public final static int N_ATTR = 5;
	public final static int notVirtual = 0, isVirtual = 1;

	public int classId;
	public int attrId;
	public String attrName;
	public int attrType;
	public int isDeputy;

	public attributeTable() {
		this.classId = 0;
		this.attrId = 0;
		this.attrName = null;
		this.attrType = attributeTable.notVirtual;
		this.isDeputy = 0;
	}

	public attributeTable(ArrayList<String> src) {
		this.classId = Integer.parseInt(src.get(0));
		this.attrId = Integer.parseInt(src.get(1));
		this.attrName = src.get(2);
		this.attrType = Integer.parseInt(src.get(3));
		this.isDeputy = Integer.parseInt(src.get(4));
	}

	public ArrayList<String> class2StringList() {
		ArrayList<String> data = new ArrayList<String>();

		data.add(Integer.toString(this.classId));
		data.add(Integer.toString(this.attrId));
		if (this.attrName == null) {
			data.add("");
		} else {
			data.add(this.attrName);
		}
		data.add(Integer.toString(this.attrType));
		data.add(Integer.toString(this.isDeputy));

		return data;
	}
}

