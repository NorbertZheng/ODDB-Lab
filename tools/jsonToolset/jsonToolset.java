import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
// local jar
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class jsonToolset {

	/*
	 * json to jsonarray
	 * @Args:
	 *  path		: String
	 * @Ret:
	 *  jsonArray	: JSONArray
	 */
	public static JSONArray readJsonFile(String path) {
		String fileContent = fileToolset.readFile(path);

		if (fileContent == null) {
			return null;
		} else {
			return JSONArray.fromObject(fileContent);
		}
	}

}


