import java.util.ArrayList;

public class deputyTable {
	public final static int N_ATTR = 3;

	public int originId;
	public int deputyId;
	public int deputyRuleId;

	public deputyTable() {
		this.originId = 0;
		this.deputyId = 0;
		this.deputyRuleId = 0;
	}

	public deputyTable(ArrayList<String> src) {
		this.originId = Integer.parseInt(src.get(0));
		this.deputyId = Integer.parseInt(src.get(1));
		this.deputyRuleId = Integer.parseInt(src.get(2));
	}

	public ArrayList<String> class2StringList() {
		ArrayList<String> data = new ArrayList<String>();

		data.add(Integer.toString(this.originId));
		data.add(Integer.toString(this.deputyId));
		data.add(Integer.toString(this.deputyRuleId));

		return data;
	}
}

