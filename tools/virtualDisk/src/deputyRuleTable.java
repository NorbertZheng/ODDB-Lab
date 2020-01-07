import java.util.ArrayList;

public class deputyRuleTable {
	public final static int N_ATTR = 2;

	public int deputyRuleId;
	public String deputyRule;

	public deputyRuleTable() {
		this.deputyRuleId = 0;
		this.deputyRule = null;
	}

	public deputyRuleTable(ArrayList<String> src) {
		this.deputyRuleId = Integer.parseInt(src.get(0));
		this.deputyRule = src.get(1);
	}

	public ArrayList<String> class2StringList() {
		ArrayList<String> data = new ArrayList<String>();

		data.add(Integer.toString(this.deputyRuleId));
		if (this.deputyRule == null) {
			data.add("");
		} else {
			data.add(this.deputyRule);
		}

		return data;
	}
}

