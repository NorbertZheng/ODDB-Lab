import java.util.ArrayList;

public class biPointerTable {
	public final static int N_ATTR = 4;

	public int classId;
	public int objectId;
	public int deputyClassId;
	public int deputyObjectId;

	public biPointerTable() {
		this.classId = 0;
		this.objectId = 0;
		this.deputyClassId = 0;
		this.deputyObjectId = 0;
	}

	public biPointerTable(ArrayList<String> src) {
		this.classId = Integer.parseInt(src.get(0));
		this.objectId = Integer.parseInt(src.get(1));
		this.deputyClassId = Integer.parseInt(src.get(2));
		this.deputyObjectId = Integer.parseInt(src.get(3));
	}

	public ArrayList<String> class2StringList() {
		ArrayList<String> data = new ArrayList<String>();

		data.add(Integer.toString(this.classId));
		data.add(Integer.toString(this.objectId));
		data.add(Integer.toString(this.deputyClassId));
		data.add(Integer.toString(this.deputyObjectId));

		return data;
	}
}

