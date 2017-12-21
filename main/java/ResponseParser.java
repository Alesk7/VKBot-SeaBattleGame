import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.HashMap;

/**
 * Created by Acer on 21-Dec-17.
 */
public class ResponseParser {
    public JSONObject parseJsonToJSONObject(String s) throws ParseException {
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(s);
        return (JSONObject) obj;
    }

    public HashMap<String, String> getResponseMap(String s) throws ParseException {
        HashMap<String, String> map = new HashMap<>();
        JSONParser parser = new JSONParser();
        JSONObject jobj = (JSONObject) parser.parse(s);

        JSONArray ja = (JSONArray) jobj.get("updates");
        JSONArray jaa = ((JSONArray)ja.get(1));

        map.put(VKAPI.VKUSERID, jaa.get(3).toString());
        map.put(VKAPI.VKMESSAGE, jaa.get(6).toString());
        map.put(VKAPI.VKFRIENDID, jaa.get(1).toString());

        return map;
    }
}
