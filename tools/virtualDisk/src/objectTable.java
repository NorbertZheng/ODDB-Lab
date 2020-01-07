import java.util.ArrayList;

public class objectTable {
	public final static int N_ATTR = 4;

	public int classId;
	public int tupleId;
	public int blockId;
	public int offset;

	public objectTable() {
		this.classId = 0;
		this.tupleId = 0;
		this.blockId = 0;
		this.offset = 0;
	}

	public objectTable(ArrayList<String> src) {
		this.classId = Integer.parseInt(src.get(0));
		this.tupleId = Integer.parseInt(src.get(1));
		this.blockId = Integer.parseInt(src.get(2));
		this.offset = Integer.parseInt(src.get(3));
	}

	public ArrayList<String> class2StringList() {
		ArrayList<String> data = new ArrayList<String>();

		data.add(Integer.toString(this.classId));
		data.add(Integer.toString(this.tupleId));
		data.add(Integer.toString(this.blockId));
		data.add(Integer.toString(this.offset));

		return data;
	}
}

