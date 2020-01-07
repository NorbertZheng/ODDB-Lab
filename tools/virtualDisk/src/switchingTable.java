import java.util.ArrayList;

public class switchingTable {
	public final static int N_ATTR = 2;

	public int attrId;
	public String rule;

	public switchingTable() {
		this.attrId = 0;
		this.rule = null;
	}

	public switchingTable(ArrayList<String> src) {
		this.attrId = Integer.parseInt(src.get(0));
		this.rule = src.get(1);
	}

	public ArrayList<String> class2StringList() {
		ArrayList<String> data = new ArrayList<String>();

		data.add(Integer.toString(this.attrId));
		if (this.rule == null) {
			data.add("");
		} else {
			data.add(this.rule);
		}

		return data;
	}
}

