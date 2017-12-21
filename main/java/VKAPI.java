import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.photos.Photo;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Random;

/**
 * Created by Acer on 21-Dec-17.
 */

public class VKAPI {
    public static String VKMESSAGE = "message";
    public static String VKUSERID = "user_id";
    public static String VKFRIENDID = "friend_id";

    private final Integer APP_ID = 5888890;
    private final String TOKEN = "0211b040bd9a37c7bb0e09cf158c807c21a4bbd77adff19093dce8242559e7e3eba8b473d513cf9737b08";
    private VkApiClient vk;
    private UserActor actor;
    private long ts;
    private String key;
    private String server;

    private ResponseParser parser;

    public VKAPI(){
        vk = new VkApiClient(HttpTransportClient.getInstance());
        actor = new UserActor(APP_ID, TOKEN);
        parser = new ResponseParser();
    }

    public void init(OnMessagesUpdateListener listener){
        getKeys();
        startLongPoll(listener);
    }

    private void getKeys(){
        try {
            ts = vk.messages().getLongPollServer(actor).execute().getTs();
            key = vk.messages().getLongPollServer(actor).execute().getKey();
            server = vk.messages().getLongPollServer(actor).execute().getServer();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    private void startLongPoll(OnMessagesUpdateListener listener){
        long result = 0;

        try {
            URLConnection connection = new URL("https://"+server+"?act=a_check&key="+key+"&ts="+ts+"&wait=25&mode=2&version=1 ")
                    .openConnection();

            InputStreamReader reader = new InputStreamReader(connection.getInputStream());
            char[] buffer = new char[256];
            int rc;
            StringBuilder response = new StringBuilder();

            while ((rc = reader.read(buffer)) != -1)
                response.append(buffer, 0, rc);

            reader.close();

            try {
                result = (Long) parser.parseJsonToJSONObject(response.toString()).get("ts");
                System.out.println(response);
                listener.onUpdate(parser.getResponseMap(response.toString()));
            }catch(Exception e){
                System.out.println(e.getMessage());
            }
        }catch(Exception e){
            //System.out.println(e.getMessage());
        }

        ts = result;
        startLongPoll(listener);
    }

    public int sendMessage(String message, int userId){
        try {
            return vk.messages().send(actor)
                    .message(message)
                    .userId(userId)
                    .randomId(new Random().nextInt())
                    .execute();
        }catch (Exception e){
            System.out.println(e.getMessage());
            return 1;
        }
    }

    public void sendAttachment(String url, int userId){
        try {
            vk.messages().send(actor)
                    .attachment(url)
                    .userId(userId)
                    .randomId(new Random().nextInt())
                    .execute();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public List<Photo> uploadPhoto(File file){
        try {
            InputStream is = sendMultipartData(vk.photos().getMessagesUploadServer(actor).execute().getUploadUrl(), file);

            InputStreamReader reader = new InputStreamReader(is);
            char[] buffer = new char[256];
            int rc;
            StringBuilder response = new StringBuilder();

            while ((rc = reader.read(buffer)) != -1)
                response.append(buffer, 0, rc);

            reader.close();
            System.out.println(response);

            JSONObject jobj = parser.parseJsonToJSONObject(response.toString());
            long server = (long) jobj.get("server");
            String photo = (String) jobj.get("photo");
            String hash = (String) jobj.get("hash");

            return vk.photos().saveMessagesPhoto(actor, photo).server((int)server).hash(hash).execute();

        }catch(Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    private InputStream sendMultipartData(String url, File photo){
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost uploadFile = new HttpPost(url);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addTextBody("field1", "yes", ContentType.MULTIPART_FORM_DATA);

            // This attaches the file to the POST:
            builder.addBinaryBody(
                    "file",
                    new FileInputStream(photo),
                    ContentType.APPLICATION_OCTET_STREAM,
                    photo.getName()
            );

            HttpEntity multipart = builder.build();
            uploadFile.setEntity(multipart);
            CloseableHttpResponse response = httpClient.execute(uploadFile);
            HttpEntity responseEntity = response.getEntity();

            return responseEntity.getContent();
        }catch(Exception e){
            System.out.println(e.getMessage());
        }

        return null;
    }
}
