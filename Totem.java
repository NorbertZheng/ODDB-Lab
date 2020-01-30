import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;

public class Totem {
	final static String INFO = "Totem>", EXIT = "exit", EXIT_INFO = "INFO: Bye Bye~";
	final static BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

	public static void displayInfo() {
		System.out.printf(Totem.INFO);
	}

	public static void displayExecuteResult(ArrayList<String> executeResult) {
		if ((executeResult == null) || (executeResult.size() == 0)) {
			System.out.println("ERROR: (in Totem.displayExecuteResult) executeResult is null!");
		} else {
			for (int i = 0; i < executeResult.size(); i++) {
				System.out.println(executeResult.get(i));
			}
		}
	}

	public static void main(String[] args) {
		String SQLInstruction, baseLocation;
		SQLNode SQLParseredInstruction;
		SQLExecutor sqlExecutor;
		ArrayList<String> executeResult;

		// get base location
		baseLocation = System.getProperty("user.dir");
		// System.out.println(baseLocation);
		// init sqlExecutor
		sqlExecutor = new SQLExecutor(baseLocation);

		while (true) {
			// display info
			Totem.displayInfo();
			// get SQL instruction
			try {
				SQLInstruction = Totem.bufferedReader.readLine();
			} catch(IOException ex) {   
				ex.printStackTrace();
				return;
			}
			// check exit
			if (SQLInstruction.equals(Totem.EXIT)) {
				System.out.println(Totem.EXIT_INFO);
				return;
			}
			// parse SQLInstruction && execute SQLInstruction
			try {
				SQLParseredInstruction = SQLParser.evaluate(SQLInstruction);
			} catch (ParseException ex) {
				System.err.println(ex.getMessage());
				return;
			}
			executeResult = sqlExecutor.evaluate(SQLParseredInstruction);
			// display execute result
			Totem.displayExecuteResult(executeResult);
		}
	}

}

