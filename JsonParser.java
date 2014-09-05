import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JsonParser {

	String SearchResult;
	public JsonParser(String searchResult){
		this.SearchResult = searchResult;
	}
	public JSONObject parserBegin() {

		JSONParser parser = new JSONParser();
		JSONObject response = null;
		try {
			response = (JSONObject) parser.parse(SearchResult);
		} catch (ParseException pe) {
			System.out.println("Error: could not parse JSON response:");
			System.out.println(SearchResult);
			System.exit(1);
		}
		return response;
	}
}
