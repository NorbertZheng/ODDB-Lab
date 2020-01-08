import java.util.ArrayList;

public class WhereClause{

	int operationType;
	// >: 0, <:1, =: 2, <>: 3, >=: 4, <=:5, NOT:6, AND:7, OR:8, int: 9, String: 10
	int valueInt;
	String valueString;
	WhereClause left;
	WhereClause right;

	ArrayList<String> tuple;
	ArrayList<Attribute> attrs;
	int is_int;

    private String getValue(String name){
		int len = tuple.size();

		for(int i=0;i<len;i++){
			if(name.equals(attrs.get(i).attrName)){
				is_int = (attrs.get(i).attrType == 0) ? 1 : 0;
				return tuple.get(i);
			}
		}
		return "";
	}

	private boolean node(WhereClause cond){
		WhereClause _left = cond.left, _right=cond.right;
		int l;
		String sl;
		switch (cond.operationType) {
 			case 0:
				sl = getValue(_left.valueString);
				if (_right.operationType == 9) {
					return Integer.parseInt(sl) > _right.valueInt;
				} else {
					return (sl.compareTo(_right.valueString) > 0) ? true : false;
				}
			case 1:
				sl = getValue(_left.valueString);
				if (_right.operationType == 9) {
					return Integer.parseInt(sl) < _right.valueInt;
				} else {
					return (sl.compareTo(_right.valueString) < 0) ? true : false;
				}
 			case 2:
				sl = getValue(_left.valueString);
				if (is_int == 1) {
					return Integer.parseInt(sl) == _right.valueInt;
				} else {
					return sl.equals(_right.valueString);
				}
			case 3:
				sl = getValue(_left.valueString);
				if (is_int == 1) {
					return Integer.parseInt(sl) != _right.valueInt;
				} else {
					return !sl.equals(_right.valueString);
				}
			case 4:
				sl = getValue(_left.valueString);
				if (_right.operationType==9) {
					return Integer.parseInt(sl) >= _right.valueInt;
				} else {
					return (sl.compareTo(_right.valueString) >= 0) ? true : false;
				}
			case 5:
				sl = getValue(_left.valueString);
				if (_right.operationType == 9) {
					return Integer.parseInt(sl) <= _right.valueInt;
				} else {
					return (sl.compareTo(_right.valueString) <= 0) ? true : false;
				}
			case 6:
				return !node(_left);
			case 7:
				return node(_left)&&node(_right);
			case 8:
				return node(_left)||node(_right);
			case 9:
				if (cond.valueInt == 0) {
					return false;
				} else {
					return true;
				}
			case 10:
				if (cond.valueString.equals("")) {
					return false;
				} else {
					return true;
				}
			default:
				return true;
		}
	}

	public boolean judge(ArrayList<String> _tuple, ArrayList<Attribute> _attrs) {
		tuple = _tuple;
		attrs = _attrs;
		return node(this);
	}
}

