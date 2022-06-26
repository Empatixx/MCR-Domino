import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Panel extends JPanel implements Runnable{
    public final static int WIDTH = 482;
    public final static int HEIGHT = 423;

    private Thread thread;
    private int mouseX;
    private int mouseY;

    private int tileSize = 60;
    public final static int offsetImageX = 2;
    public final static int offsetImageY = 3;

    public Domino[] dominos;
    public BufferedImage pozadi;

    public int[][] map;
    public boolean[][] rotated;
    public boolean[][] used;

    private boolean saved = false;

    private ArrayList<Spoj> spoje = new ArrayList<>();
    private ArrayList<DuplicateSpoj> dupSpoje = new ArrayList<>();
    private final ArrayList<NapovedaSpoj> napovedaSpoje = new ArrayList<>();

    private Lock lock = new ReentrantLock();

    public Panel(){
        this.setPreferredSize(new Dimension(WIDTH,HEIGHT));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
    }
    public void start(){
        // nastaveni sady
        dominos = new Domino[7*4];
        int value1 = 0;
        int value2 = 0;
        int shift = 0;
        for(int i = 0;i<7*4;i++){
            dominos[i] = new Domino(value1,value2);
            value1++;
            if(value1 > 6){
                shift++;
                value2++;
                value1 = shift;
            }
        }
        // zde zmenime jaky soubor chceme pouzit
        String soubor = "map\\priklad05.bmp";
        try {
            pozadi = ImageIO.read(new File(soubor));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        loadMap(soubor);

        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        while(thread != null){
            // logics
            update();
            // draw
            repaint();
            try{
                Thread.sleep(1000/60);
            } catch (Exception e){

            }


        }
    }
    public void update() {
        for(int i = 0;i<dupSpoje.size();i++){
            DuplicateSpoj spoj = dupSpoje.get(i);
            if(spoj.shouldRemove()){
                dupSpoje.remove(i);
                i--;
            }
            if(spoj.vertical){
                if(used[spoj.indexY][spoj.indexX] || used[spoj.indexY+1][spoj.indexX]){
                    dupSpoje.remove(i);
                    i--;
                }
            } else {
                if(used[spoj.indexY][spoj.indexX] || used[spoj.indexY][spoj.indexX+1]){
                    dupSpoje.remove(i);
                    i--;
                }
            }
        }
        for(int i = 0;i<napovedaSpoje.size();i++){
            if(napovedaSpoje.get(i).shouldRemove()){
                napovedaSpoje.remove(i);
                i--;
            }
        }

        // po dokonceni vsech domin -> ulozeni vysledku
        boolean canSave = true;
        for(int y = 0;y<7;y++){
            for(int x = 0;x<8;x++){
                if(!used[y][x]) canSave = false;
            }
        }
        if(canSave && !saved){
           ulozitReseni();
           saved = true;
        }
    }
    public void paintComponent(Graphics g){
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        //g2.setColor(Color.red);
        //g2.fillRect(mouseX,mouseY,20,20);
        g2.drawImage(pozadi,0,0,null);

        for(Spoj spoj : spoje){
            int x = spoj.getIndexX();
            int y = spoj.getIndexY();
            if(spoj.isVertical()){
                g2.setColor(Color.blue);
                g2.setStroke(new BasicStroke(3));
                g2.drawRect(4+x*60,5+y*60,54,114);
            } else {
                g2.setColor(Color.red);
                g2.setStroke(new BasicStroke(3));
                g2.drawRect(4+x*60,5+y*60,114,54);
            }
        }
        for(DuplicateSpoj spoj : dupSpoje){
            int x = spoj.indexX;
            int y = spoj.indexY;
            if(spoj.isVertical()){
                g2.setColor(Color.CYAN);
                g2.setStroke(new BasicStroke(3));
                g2.drawRect(4+x*60,5+y*60,54,114);
            } else {
                g2.setColor(Color.CYAN);
                g2.setStroke(new BasicStroke(3));
                g2.drawRect(4+x*60,5+y*60,114,54);
            }
        }
        if(System.currentTimeMillis() / 150 % 2 == 0) {
            lock.lock();
            try{
                for(NapovedaSpoj spoj : napovedaSpoje){
                    int x = spoj.indexX;
                    int y = spoj.indexY;
                    if(spoj.isVertical()){
                        g2.setColor(Color.GREEN);
                        g2.setStroke(new BasicStroke(3));
                        g2.drawRect(4+x*60,5+y*60,54,114);
                    } else {
                        g2.setColor(Color.GREEN);
                        g2.setStroke(new BasicStroke(3));
                        g2.drawRect(4+x*60,5+y*60,114,54);
                    }
                }
            } finally {
                lock.unlock();
            }
        }
        g2.dispose();
    }
    public void loadMap(String soubor){
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File(soubor));
        } catch (IOException e) {

        }
        this.map = new int[7][8];
        this.used = new boolean[7][8];
        this.rotated = new boolean[7][8];

        for(int i = 0; i<7 ;i++){
            for(int j = 0;j<8;j++){
                BufferedImage sub = img.getSubimage(offsetImageX+j*60,offsetImageY+i*60,58,58);
                int height = sub.getHeight(), width = sub.getWidth();
                int blackPixels = 0;
                for (int y = 0; y < height; y++) { // hledani cerne
                    for (int x = 0; x < width; x++) {
                        int RGBA = sub.getRGB(x, y);
                        int alpha = (RGBA >> 24) & 255;
                        int red = (RGBA >> 16) & 255;
                        int green = (RGBA >> 8) & 255;
                        int blue = RGBA & 255;
                        if(red < 40 && green < 40 && blue < 40){
                            blackPixels++;
                        }
                    }
                    int type = Math.round(blackPixels / 128f); // round kvuli sumu
                    map[i][j] = type;
                    rotated[i][j] = isRotated(type,sub);
                }
            }
        }
    }
    public void mouseClick(MouseEvent e){
        Point mouse = MouseInfo.getPointerInfo().getLocation();
        mouseX = (int) mouse.getX() - this.getLocationOnScreen().x;
        mouseY = (int) mouse.getY() - this.getLocationOnScreen().y;

        Rectangle mouseR = new Rectangle(mouseX,mouseY,10,10);

        for(int y = 0;y<7;y++){
            for(int x = 0;x<8;x++){
                Rectangle hRect = new Rectangle(60 + x*60,30 + y*60,15,15);
                if(mouseR.intersects(hRect)){ // horizontal click
                    int value1 = map[y][x+1];
                    int value2 = map[y][x];
                    if(isUsed(value1,value2)){
                        if(canRemove(value1,value2,x,y)){
                            for(Spoj spoj : spoje){
                                if(spoj.isThisIndex(x,y)){
                                    used[y][x+1] = false;
                                    used[y][x] = false;
                                    spoje.remove(spoj);
                                    freeDomino(value1,value2);
                                    break;
                                }
                            }
                        } else {
                            createDuplicateSpoj(x,y,false);
                        }
                        return;
                    }
                    if(VznikneLichy(x,y,false)){ // zamezuje vytvoreni vytvoreni oblasti s lichym poctem
                        return;
                    }
                    if(!used[y][x+1] && !used[y][x]  && checkRotations(x,y,x+1,y)){
                        used[y][x+1] = true;
                        used[y][x] = true;
                        useDomino(value1,value2,x,y);
                        Spoj spoj = new Spoj(x,y, false);
                        spoje.add(spoj);
                    }

                }
                Rectangle vRect = new Rectangle(30 + x*60,60 + y*60,15,15);

                if(mouseR.intersects(vRect)){ // vertical click
                    int value1 = map[y+1][x];
                    int value2 = map[y][x];
                    if(isUsed(value1,value2)){
                        if(canRemove(value1,value2,x,y)){
                            for(Spoj spoj : spoje){
                                if(spoj.isThisIndex(x,y)){
                                    used[y+1][x] = false;
                                    used[y][x] = false;
                                    spoje.remove(spoj);
                                    freeDomino(value1,value2);
                                    break;
                                }
                            }
                        } else {
                            createDuplicateSpoj(x,y,true);
                        }
                        return;
                    }
                    if(VznikneLichy(x,y,true)){ // zamezuje vytvoreni vytvoreni oblasti s lichym poctem
                        return;
                    }
                    if(!used[y+1][x] && !used[y][x] && checkRotations(x,y,x,y+1)){
                        used[y+1][x] = true;
                        used[y][x] = true;
                        useDomino(value1,value2,x,y);
                        Spoj spoj = new Spoj(x,y, true);
                        spoje.add(spoj);
                    }
                }
            }
        }

    }
    public boolean isUsed(int a,int b){
        for(Domino d : dominos){
            if((d.getValue1() == a && d.getValue2() == b) || (d.getValue2() == a && d.getValue1() == b)){
                return d.isUsed();
            }
        }
        return false;
    }
    public void useDomino(int a, int b, int x, int y){
        for(Domino d : dominos){
            if((d.getValue1() == a && d.getValue2() == b) || (d.getValue2() == a && d.getValue1() == b)){
                d.setUsed(true);
                d.setLoc(x,y);
            }
        }
    }
    public void freeDomino(int a, int b){
        for(Domino d : dominos){
            if((d.getValue1() == a && d.getValue2() == b) || (d.getValue2() == a && d.getValue1() == b)){
                d.setUsed(false);
                d.setLoc(-1,-1);
            }
        }
    }
    public boolean canRemove(int a,int b, int x, int y){
        for(Domino d : dominos){
            if(((d.getValue1() == a && d.getValue2() == b) || (d.getValue2() == a && d.getValue1() == b))
            && d.getX() == x && d.getY() == y){
                return true;
            }
        }
        return false;
    }
    public void createDuplicateSpoj(int x,int y, boolean vertical){
        for(DuplicateSpoj spoj : dupSpoje){
            if(spoj.isAlready(x,y)){
                return;
            }
        }
        DuplicateSpoj spoj = new DuplicateSpoj(x, y, vertical);
        dupSpoje.add(spoj);
    }
    public boolean VznikneLichy(int x, int y, boolean vertical){
        if(vertical){
            boolean[][] cloneUsed = new boolean[7][8];
            for(int i = 0;i<7;i++){
                System.arraycopy(used[i], 0, cloneUsed[i], 0, 8);
            }
            cloneUsed[y][x] = true;
            cloneUsed[y+1][x] = true;
            for(int i = 0;i<7;i++){
                for(int j = 0;j<8;j++){
                    if(cloneUsed[i][j]) continue;
                    boolean up = true;
                    boolean left = true;
                    boolean down = true;
                    boolean right = true;

                    if(i < 6) up = cloneUsed[i+1][j];
                    if(i > 0) down = cloneUsed[i-1][j];
                    if(j < 7) right = cloneUsed[i][j+1];
                    if(j > 0) left = cloneUsed[i][j-1];

                    int count = 0;
                    if(!up) count++;
                    if(!down) count++;
                    if(!left) count++;
                    if(!right) count++;

                    if(count < 1) return true;
                }
            }
        } else {
            boolean[][] cloneUsed = new boolean[7][8];
            for(int i = 0;i<7;i++){
                System.arraycopy(used[i], 0, cloneUsed[i], 0, 8);
            }
            cloneUsed[y][x] = true;
            cloneUsed[y][x+1] = true;
            for(int i = 0;i<7;i++){
                for(int j = 0;j<8;j++){
                    if(cloneUsed[i][j]) continue;
                    boolean up = true;
                    boolean left = true;
                    boolean down = true;
                    boolean right = true;

                    if(i < 6) up = cloneUsed[i+1][j];
                    if(i > 0) down = cloneUsed[i-1][j];
                    if(j < 7) right = cloneUsed[i][j+1];
                    if(j > 0) left = cloneUsed[i][j-1];

                    int count = 0;
                    if(!up) count++;
                    if(!down) count++;
                    if(!left) count++;
                    if(!right) count++;

                    if(count < 1){
                        System.out.println(count);
                        System.out.println("Y: "+i+" X:"+ j);
                        return true;
                    }
                }
            }

        }
        return false;
    }

    public void ulozitReseni() {
        Rectangle screenRect = new Rectangle(Main.window.getBounds());
        BufferedImage capture = null;
        try {
            capture = new Robot().createScreenCapture(screenRect);
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
        try {
            ImageIO.write(capture, "bmp", new File("RESENI.bmp"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public boolean isRotated(int cislo, BufferedImage bi){
        if(cislo == 0 || cislo == 1 || cislo == 5 || cislo == 4) return false;
        else {
            if(cislo == 2 || cislo == 3) {
                int RGBA = bi.getRGB(17, 40);
                int alpha = (RGBA >> 24) & 255;
                int red = (RGBA >> 16) & 255;
                int green = (RGBA >> 8) & 255;
                int blue = RGBA & 255;
                return red < 40 && green < 40 && blue < 40;
            } else { // pokud je sestka
                int RGBA = bi.getRGB(17, 30);
                int alpha = (RGBA >> 24) & 255;
                int red = (RGBA >> 16) & 255;
                int green = (RGBA >> 8) & 255;
                int blue = RGBA & 255;
                return red < 40 && green < 40 && blue < 40;
            }
        }
    }
    public boolean checkRotations(int x1, int y1, int x2, int y2){ // returns true if rotations are correct
        // 1,4,5,0
        if((map[y1][x1] == 1 || map[y1][x1] == 4 || map[y1][x1] == 5 || map[y1][x1] == 0) ||
                (map[y2][x2] == 1 || map[y2][x2] == 4 || map[y2][x2] == 5 || map[y2][x2] == 0)){
            return true;
        } else return rotated[y1][x1] == rotated[y2][x2];
    }

    public void napoveda(){
        for(int y = 0;y<7;y++){
            for(int x = 0;x<8;x++){
                if(used[y][x]) continue;
                boolean down = true;
                boolean right = true;

                if(y < 6){
                    int value1 = map[y+1][x];
                    int value2 = map[y][x];
                    down = used[y+1][x] || isUsed(value1,value2) || VznikneLichy(x,y,true);
                    if(!down){
                        down = !checkRotations(x,y,x,y+1);
                    }
                }
                if(x < 7){
                    int value1 = map[y][x];
                    int value2 = map[y][x+1];
                    right = used[y][x+1] || isUsed(value1,value2)  || VznikneLichy(x,y,false);
                    if(!right){
                        right = !checkRotations(x+1,y,x,y);
                    }
                }
                if (!right){
                    NapovedaSpoj spoj = new NapovedaSpoj(x,y,false);
                    lock.lock();
                    try{
                        napovedaSpoje.add(spoj);
                    } finally {
                        lock.unlock();
                    }
                    return;
                }
                else if(!down){
                    NapovedaSpoj spoj = new NapovedaSpoj(x,y,true);
                    lock.lock();
                    try{
                        napovedaSpoje.add(spoj);
                    } finally {
                        lock.unlock();
                    }
                    return;
                }
            }
        }
    }
    public void vyresit(){
        boolean zmena;
        do{
            zmena = false;
            for(int y = 0;y<7;y++){
                for(int x = 0;x<8;x++){
                    if(used[y][x]) continue;
                    boolean down = true;
                    boolean right = true;
                    int value1;
                    int value2;
                    if(x < 7 && !zmena){
                        value1 = map[y][x];
                        value2 = map[y][x+1];
                        right = used[y][x+1] || isUsed(value1,value2)  || VznikneLichy(x,y,false);
                        if(!right){
                            right = !checkRotations(x+1,y,x,y);
                        }
                        if(!right){
                            used[y][x+1] = true;
                            used[y][x] = true;
                            useDomino(value1,value2,x,y);
                            Spoj spoj = new Spoj(x,y, false);
                            spoje.add(spoj);
                        }
                    }
                    /*if(y < 6){
                        value1 = map[y+1][x];
                        value2 = map[y][x];
                        down = used[y+1][x] || isUsed(value1,value2) || VznikneLichy(x,y,true);
                        if(!down){
                            down = !checkRotations(x,y,x,y+1);
                        }
                        if(!down){
                            used[y+1][x] = true;
                            used[y][x] = true;
                            useDomino(value1,value2,x,y);
                            Spoj spoj = new Spoj(x,y, true);
                            spoje.add(spoj);
                        }
                    }*/
                }
            }
        } while (zmena);
    }
}
