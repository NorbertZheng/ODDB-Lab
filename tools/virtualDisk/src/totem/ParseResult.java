import java.util.ArrayList;

public class ParseResult {

	public int Type;// 0:create class; 1:create select deputy class
	//2:Drop;     3:Insert;  4:Delete
	//5:select;   6: cross select
	public String className;
	public String selectClassName;
	public ArrayList<Attribute> attrList;
	public ArrayList<AttrNameTuple> attrNameList;
	public ArrayList<String> valueList;
	public WhereClause where;

}

