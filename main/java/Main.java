import com.vk.api.sdk.objects.photos.Photo;

import java.io.*;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Acer on 12-Apr-17.
 */

public class Main implements OnMessagesUpdateListener{
    private int friendId;
    private SeaBattleGame seaBattle;
    private boolean isFirst;
    private String idPeople;
    private VKAPI vk;

    public Main(){
        vk = new VKAPI();
    }

    public static void main(String[] args) throws IOException {
        Main bot = new Main();
        bot.vk.init(bot);
    }

    @Override
    public void onUpdate(HashMap<String, String> response) {
        try{
            if(friendId != Integer.parseInt(response.get(VKAPI.VKFRIENDID))) {
                String incomeMessage = new String(response.get(VKAPI.VKMESSAGE).getBytes(), "UTF-8");
                String idP = response.get(VKAPI.VKUSERID);

                if (incomeMessage.equals("Начать морской бой!") && seaBattle == null) {
                    seaBattle = new SeaBattleGame();
                    idPeople = idP;
                    isFirst = Math.random() > 0.5;
                    if(isFirst) {
                        friendId = vk.sendMessage("Я стреляю первым", Integer.parseInt(idPeople));
                        playSeaBattle(incomeMessage, idPeople);
                    }else{
                        friendId = vk.sendMessage("Ты стреляешь первым", Integer.parseInt(idPeople));
                    }
                }else if((seaBattle != null) && (idP.equals(idPeople))){
                    playSeaBattle(incomeMessage, idPeople);
                }
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public void playSeaBattle(String income_msg, String idPeople) throws Exception{
            Thread.sleep(500);
            try {
                String ans = seaBattle.checkAttack(income_msg);
                if (ans.equals("Все, ты победил(")) {
                    vk.sendMessage(ans, Integer.parseInt(idPeople));
                    List<Photo> ph = vk.uploadPhoto(new File(SeaBattleGame.IMG_RESULTS_PATH));
                    vk.sendAttachment("photo"+ph.get(0).getOwnerId()+"_"+ph.get(0).getId(), Integer.parseInt(idPeople));
                    System.exit(0);
                }
                if (ans.equals("Ранил") || ans.equals("Убил")) {
                    friendId = vk.sendMessage(ans, Integer.parseInt(idPeople));
                    return;
                }
                friendId = vk.sendMessage(ans, Integer.parseInt(idPeople));
                if (ans.equals("Ты уже стрелял сюда, лошара")) return;
            } catch (Exception e) {
                if (income_msg.contains("Стреляю")) {
                    friendId = vk.sendMessage("Куда ты стреляешь?", Integer.parseInt(idPeople));
                    return;
                } else {
                    int tmp = seaBattle.mark(income_msg);
                    if ((tmp == 1 || tmp == 2) && (!income_msg.equals("Начать морской бой!"))) return;
                }
            }
            Thread.sleep(2000);
            friendId = vk.sendMessage(seaBattle.Attack(), Integer.parseInt(idPeople));
    }
}
