// import tools.fileToolset.fileToolset;
// local jar
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class test_readJsonFile {

	private static String filePath = "./test/test_readJsonFile.json";

	public static void main(String[] args) {
		JSONArray t_readJsonFile = jsonToolset.readJsonFile(test_readJsonFile.filePath);
		int size = t_readJsonFile.size();

		System.out.println("test jsonToolset.readJsonFile: ");
		System.out.println("Size: " + size);
		for(int i = 0; i < size; i++){
			JSONObject jsonObject = t_readJsonFile.getJSONObject(i);
			System.out.println("[" + i + "]name=" + jsonObject.get("name"));
			System.out.println("[" + i + "]package_name=" + jsonObject.get("package_name"));
			System.out.println("[" + i + "]check_version=" + jsonObject.get("check_version"));
		}
	}
}

