import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

/**
 * Created by Acer on 12-Apr-17.
 */

public class SeaBattleGame {
    private static int[][] field = new int[10][10];
    private int[][] opponent_field = new int[10][10];
    private ArrayList<Ship> ships = new ArrayList<>();
    int[] previous_attack = new int[2];
    String opponent_answer; boolean is_killing = false; int[] what_was_increased = new int[4]; int last_increase, how_increase;
    boolean success_killing = false;

    public SeaBattleGame(){
        for(int i = 0; i < 10; i++)
            for(int j = 0; j < 10; j++)
                opponent_field[i][j] = -5;

        boolean res;
        do {
            res = initialize();
        }while(!res);

        for(Ship s : ships){
            for(int i = 0; i < s.coordinats.length; i++){
                field[s.coordinats[i][1]][s.coordinats[i][0]] = 1;
            }
        }

        for(int i = 0; i < 10; i++){
            for(int j = 0; j < 10; j++){
                System.out.print(field[i][j]+" ");
            }
            System.out.println();
        }

        System.out.println("\nShips:");
        for(Ship s : ships){
            for(int i = 0; i < s.coordinats.length; i++){
                System.out.println(s.coordinats[i][0]+ " "+s.coordinats[i][1]);;
            }
        }
    }

    //полу-тупая
    public String Attack(){
        StringBuilder result = new StringBuilder();

        do{
            if (!is_killing) {
                previous_attack[0] = (int) (Math.random() * 10);
                previous_attack[1] = (int) (Math.random() * 10);
            } else {
                if(!success_killing) {
                    how_increase = (int) (Math.random() * 2);
                    last_increase = (int) (Math.random() * 2);
                }
                try {
                    if (how_increase == 0) {
                        previous_attack[last_increase] += 1;
                    } else {
                        previous_attack[last_increase] -= 1;
                    }
                }catch(Exception e){
                    System.out.println("Not increasing");
                }
            }
        }while(opponent_field[previous_attack[0]][previous_attack[1]] != -5);

        char x = (char) (1040 + previous_attack[0]);
        if((int)x < 1040){
            x = (char) 1040;
        }
        if ((int) x >= 1049) {
            x = (char) 1050;
        }
        if(previous_attack[1] > 9) previous_attack[1] = 8;
        else if(previous_attack[1] < 0) previous_attack[1] = 1;
        result.append("Стреляю " + x + (previous_attack[1]+1));

        return result.toString();
    }

    public int mark(String answer){
        this.opponent_answer = answer;
        int success = 2;

        if(answer.equals("Мимо")){
            if(is_killing){
                if(how_increase == 0) previous_attack[last_increase] -= 1;
                else previous_attack[last_increase] += 1;
            }
            opponent_field[previous_attack[0]][previous_attack[1]] = 0;
            success = 1;
        }else if(answer.equals("Ранил")){
            opponent_field[previous_attack[0]][previous_attack[1]] = 1;
            if(is_killing) success_killing = true;
            is_killing = true;
            success = 0;
        }else if(answer.equals("Убил")){
            opponent_field[previous_attack[0]][previous_attack[1]] = 1;
            is_killing = false;
            success_killing = false;
            success = 0;
        }

        return success;
    }

    private void drawResults(){
        try {
            BufferedImage img = ImageIO.read(new File("C:\\SeaBattle\\field.jpg"));
            Graphics2D gr = img.createGraphics();
            gr.setFont(new Font("TimesRoman", Font.BOLD, 82));
            for(int i = 0; i < 10; i++){
                for(int j = 0; j < 10; j++){
                    if(field[i][j] == 1){
                        gr.setColor(Color.BLACK);
                        gr.drawString("X",xcell(j),ycell(i));
                    }else if(field[i][j] == -1){
                        gr.setColor(Color.RED);
                        gr.drawString("X",xcell(j),ycell(i));
                    }else if(field[i][j] == -2){
                        gr.setColor(Color.BLUE);
                        gr.drawString(".",((j*100)+50)+(110-8),((i*100)+50)+(115+2));
                    }
                }
            }
            ImageIO.write(img, "jpg", new File("C:\\SeaBattle\\filled_field.jpg"));
            gr.dispose();
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    private int xcell(int c){
        return ((c*100)+50)+(110-26);
    }

    private int ycell(int c){
        return ((c*100)+50)+(115+27);
    }









    public String checkAttack(String msg){
        StringBuilder s = new StringBuilder(msg);
        s.delete(0,8);
        String result = "";
        int y = ((int) s.charAt(0)) - 1040;
        if(y == 10) y = 9;
        s.delete(0,1);
        int x = Integer.parseInt(s.toString())-1;
        System.out.println(x+" "+y);

        if(field[x][y] == 1){
            result = "Ранил";
            field[x][y] = -1;
        }else if(field[x][y] == 0){
            result = "Мимо";
            field[x][y] = -2;
        }else if((field[x][y] == -1) || (field[x][y] == -2)){
            result = "Ты уже стрелял сюда, лошара";
        }

        int index = -1;
        for(int j = 0; j < ships.size(); j++){
            for(int i = 0; i < ships.get(j).coordinats.length; i++){
                if(ships.get(j).coordinats[i][0] == y && ships.get(j).coordinats[i][1] == x){
                    index = j;
                    ships.get(j).coordinats[i][0] = -1;
                    ships.get(j).coordinats[i][1] = -1;
                }
            }

            if(index != -1) {
                int counter = 0;
                for (int i = 0; i < ships.get(index).coordinats.length; i++) {
                    if (ships.get(index).coordinats[i][0] == -1 && ships.get(index).coordinats[i][1] == -1) {
                        counter++;
                    }
                }

                if (counter == ships.get(index).coordinats.length)
                    result = "Убил";
            }
        }

        boolean is_end = true;
        for(int i = 0; i < 10; i++){
            for(int j = 0; j < 10; j++){
                if(field[i][j] == 1){
                    is_end = false;
                }
            }
        }

        if(is_end == true) {
            drawResults();
            result = "Все, ты победил(";
        }
        return result;
    }













    private boolean initialize(){
        ArrayList<int[]> engaged = new ArrayList<>();
        boolean res = true;
                ships.clear();
                for (int i = 1; i <= 4; i++) {
                    for (int j = 0; j < 4 - (i - 1); j++) {
                        Ship s = new Ship(i);
                        int x, y;
                        boolean isHor;
                        int counter = 0;
                        do {
                            isHor = Math.random() > 0.5;
                            x = isHor ? (int) (Math.random() * (10 - i)) : (int) (Math.random() * 9);
                            y = isHor ? (int) (Math.random() * 9) : (int) (Math.random() * (10 - i));
                            counter++;
                            if (counter > 30000) {
                                res = false;
                                break;
                            }
                        } while (!isAvailable(x, y, engaged, isHor, i));

                        s.setX(0, x);
                        s.setY(0, y);
                        int[] arr = {x, y};
                        engaged.add(arr.clone());
                        for (int q = -1; q <= 1; q++) {
                            arr[0] = x + q;
                            for (int qq = -1; qq <= 1; qq++) {
                                arr[1] = y + qq;
                                engaged.add(arr.clone());
                            }
                        }

                        for (int k = 1; k < i; k++) {
                            if (isHor) {
                                s.setX(k, x + k);
                                s.setY(k, y);
                                arr[0] = s.getX(k);
                                arr[1] = s.getY(k);
                                engaged.add(arr.clone());
                                for (int q = -1; q <= 1; q++) {
                                    arr[0] = s.getX(k) + q;
                                    for (int qq = -1; qq <= 1; qq++) {
                                        arr[1] = s.getY(k) + qq;
                                        engaged.add(arr.clone());
                                    }
                                }
                            } else {
                                s.setX(k, x);
                                s.setY(k, y + k);
                                arr[0] = s.getX(k);
                                arr[1] = s.getY(k);
                                engaged.add(arr.clone());
                                for (int q = -1; q <= 1; q++) {
                                    arr[0] = s.getX(k) + q;
                                    for (int qq = -1; qq <= 1; qq++) {
                                        arr[1] = s.getY(k) + qq;
                                        engaged.add(arr.clone());
                                    }
                                }
                            }
                        }
                        ships.add(s);
                    }
                }
        return res;
    }

    private boolean isAvailable(int x, int y, ArrayList<int[]> engaged, boolean isHor, int length){
        boolean result = true;
        for(int j = 0; j < engaged.size(); j++){
            if((engaged.get(j)[0] == x) && (engaged.get(j)[1] == y)) result = false;
            //System.out.println("x "+x+" y "+y+" engaged("+j+")[0]: "+engaged.get(j)[0]+" engaged("+j+")[1]: "+engaged.get(j)[1]);
            for(int i = 1; i <= length; i++){
                if(isHor) {
                    if ((engaged.get(j)[0] == x + i) & (engaged.get(j)[1] == y)) result = false;
                    //else
                    //System.out.println("x "+(x+i)+" y "+y+" engaged("+j+")[0]: "+engaged.get(j)[0]+" engaged("+j+")[1]: "+engaged.get(j)[1]);
                }else {
                    if ((engaged.get(j)[0] == x) & (engaged.get(j)[1] == y + i)) result = false;
                    //else
                    //System.out.println("x "+x+" y "+(y+i)+" engaged("+j+")[0]: "+engaged.get(j)[0]+" engaged("+j+")[1]: "+engaged.get(j)[1]);
                }
            }
        }
        return result;
    }







    private static class Ship{
        private int[][] coordinats;

        Ship(int cells){
            coordinats = new int[cells][2];
        }

        int getX(int cell){
            return coordinats[cell][0];
        }

        int getY(int cell){
            return coordinats[cell][1];
        }

        void setX(int cell, int value){
            coordinats[cell][0] = value;
        }

        void setY(int cell, int value){
            coordinats[cell][1] = value;
        }
    }
}
