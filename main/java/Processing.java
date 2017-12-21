import com.vk.api.sdk.actions.Messages;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Acer on 12-Apr-17.
 */
public class Processing {
    private long ts;
    private String key;
    private String server;

    public String startLongPoll(Messages messages, UserActor actor){
        try {
            ts = messages.getLongPollServer(actor).execute().getTs();
            key = messages.getLongPollServer(actor).execute().getKey();
            server = messages.getLongPollServer(actor).execute().getServer();
            requestandjson();
        }catch(ApiException e){
            System.out.println("API Exception");
        }catch(ClientException e){
            System.out.println("Client Exception");
        }
        return null;
    }

    private void requestandjson(){
        long result = 0;
        try {
            URLConnection connection = new URL("https://"+server+"?act=a_check&key="+key+"&ts="+ts+"&wait=25&mode=2&version=1 ").openConnection();

            InputStream is = connection.getInputStream();
            InputStreamReader reader = new InputStreamReader(is);
            char[] buffer = new char[256];
            int rc;

            StringBuilder sb = new StringBuilder();

            while ((rc = reader.read(buffer)) != -1)
                sb.append(buffer, 0, rc);

            reader.close();

            try {
                result = (Long) parseJSON(sb.toString()).get("ts");
                System.out.println(sb);
                Main.response(parseJSONArraytoMsg(sb.toString()));
            }catch(Exception e){
                System.out.println("Трабл в парсинге JSON или просто произошло обновление.");
            }
        }catch(Exception e){
            System.out.println("Трабл в запросе");
        }
        ts = result;
        requestandjson();
    }

    private JSONObject parseJSON(String s) throws ParseException {
        JSONParser parser = new JSONParser();

        Object obj = parser.parse(s);
        return (JSONObject) obj;
    }

    private Object[] parseJSONArraytoMsg(String s) throws ParseException {
        Object[] objcts = new Object[3];
        JSONParser parser = new JSONParser();

        Object obj = parser.parse(s);
        JSONObject jobj = (JSONObject) obj;

        JSONArray ja = (JSONArray) jobj.get("updates");
        JSONArray jaa = ((JSONArray)ja.get(1));
        objcts[0] = jaa.get(3);
        objcts[1] = jaa.get(6);
        objcts[2] = jaa.get(1);

        return objcts;
    }
}
