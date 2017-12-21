import com.vk.api.sdk.client.TransportClient;
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
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.util.List;
import java.util.Random;

/**
 * Created by Acer on 12-Apr-17.
 */
public class Main {
    static final Integer APP_ID = 5888890;
    static final String TOKEN = "0211b040bd9a37c7bb0e09cf158c807c21a4bbd77adff19093dce8242559e7e3eba8b473d513cf9737b08";
    static VkApiClient vk;
    static UserActor actor;
    private static int friend_id;
    static SeaBattleGame sea_battle;
    static boolean is_first;
    static String idPeople;

    public static void main(String[] args) throws IOException {
        sea_battle = null;
        TransportClient transport_client = HttpTransportClient.getInstance();
        vk = new VkApiClient(transport_client);
        actor = new UserActor(APP_ID, TOKEN);
        Processing p = new Processing();
        p.startLongPoll(vk.messages(), actor);
    }

    public static void response(Object[] obj){
        try {
            if(friend_id != Integer.parseInt(obj[2].toString())) {
                String inComingMsg = (String) obj[1];
                inComingMsg = new String(inComingMsg.getBytes(), "UTF-8");
                String idP = obj[0].toString();

                if (inComingMsg.equals("Начать морской бой!") && sea_battle == null) {
                    sea_battle = new SeaBattleGame();
                    idPeople = idP;
                    is_first = Math.random() > 0.5;
                    if(is_first) {
                        friend_id = vk.messages().send(actor).message("Я стреляю первым").userId(Integer.parseInt(idPeople)).randomId(new Random().nextInt()).execute();
                        playSeaBattle(inComingMsg, idPeople);
                    }else{
                        friend_id = vk.messages().send(actor).message("Ты стреляешь первым").userId(Integer.parseInt(idPeople)).randomId(new Random().nextInt()).execute();
                    }
                }else if((sea_battle != null) && (idP.equals(idPeople))){
                    playSeaBattle(inComingMsg, idPeople);
                }
            }
        }catch(Exception e){
            System.out.println("Error");
            System.out.println(e.getMessage());
            System.out.println(e.getCause());
            System.out.println(e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    public static void playSeaBattle(String income_msg, String idPeople) throws Exception{
            Thread.sleep(500);
            try {
                String ans = sea_battle.checkAttack(income_msg);
                if (ans.equals("Все, ты победил(")) {
                    vk.messages().send(actor).message(ans).userId(Integer.parseInt(idPeople)).randomId(new Random().nextInt()).execute();
                    uploadPhoto();
                    System.exit(0);
                }
                if (ans.equals("Ранил") || ans.equals("Убил")) {
                    friend_id = vk.messages().send(actor).message(ans).userId(Integer.parseInt(idPeople)).randomId(new Random().nextInt()).execute();
                    return;
                }
                friend_id = vk.messages().send(actor).message(ans).userId(Integer.parseInt(idPeople)).randomId(new Random().nextInt()).execute();
                if (ans.equals("Ты уже стрелял сюда, лошара")) return;
            } catch (Exception e) {
                if (income_msg.contains("Стреляю")) {
                    friend_id = vk.messages().send(actor).message("Куда ты стреляешь?").userId(Integer.parseInt(idPeople)).randomId(new Random().nextInt()).execute();
                    return;
                } else {
                    int tmp = sea_battle.mark(income_msg);
                    if ((tmp == 1 || tmp == 2) && (!income_msg.equals("Начать морской бой!"))) return;
                }
            }
            Thread.sleep(2000);
            friend_id = vk.messages().send(actor).message(sea_battle.Attack()).userId(Integer.parseInt(idPeople)).randomId(new Random().nextInt()).execute();
    }

    private static void uploadPhoto(){
        try {
            sendMultipartData(vk.photos().getMessagesUploadServer(actor).execute().getUploadUrl());
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    private static void sendMultipartData(String url){
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost uploadFile = new HttpPost(url);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addTextBody("field1", "yes", ContentType.MULTIPART_FORM_DATA);
            // This attaches the file to the POST:
            File f = new File("C:\\SeaBattle\\filled_field.jpg");
            builder.addBinaryBody(
                    "file",
                    new FileInputStream(f),
                    ContentType.APPLICATION_OCTET_STREAM,
                    f.getName()
            );
            HttpEntity multipart = builder.build();
            uploadFile.setEntity(multipart);
            CloseableHttpResponse response = httpClient.execute(uploadFile);
            HttpEntity responseEntity = response.getEntity();

            InputStream is = responseEntity.getContent();
            InputStreamReader reader = new InputStreamReader(is);
            char[] buffer = new char[256];
            int rc;
            StringBuilder sb = new StringBuilder();
            while ((rc = reader.read(buffer)) != -1)
                sb.append(buffer, 0, rc);
            reader.close();
            System.out.println(sb);

            JSONParser parser = new JSONParser();
            Object obj = parser.parse(sb.toString());
            JSONObject jobj = (JSONObject) obj;
            long server = (long) jobj.get("server");
            String photo = (String) jobj.get("photo");
            String hash = (String) jobj.get("hash");
            List<Photo> ph = vk.photos().saveMessagesPhoto(actor, photo).server((int)server).hash(hash).execute();
            vk.messages().send(actor).attachment("photo"+ph.get(0).getOwnerId()+"_"+ph.get(0).getId()).userId(Integer.parseInt(idPeople)).randomId(new Random().nextInt()).execute();
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
}
