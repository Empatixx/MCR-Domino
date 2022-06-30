import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Panel extends JPanel implements Runnable{
    public final static int WIDTH = 482;
    public final static int HEIGHT = 423+100;

    private Thread thread;

    public final static int offsetImageX = 2;
    public final static int offsetImageY = 3;

    public Domino[] dominos;
    public BufferedImage pozadi;

    public int[][] map;
    public boolean[][] rotated;
    public boolean[][] used;

    private boolean saved = false;
    private String loadedMap = "priklad01.bmp";

    private final ArrayList<Spoj> spoje = new ArrayList<>();
    private final ArrayList<DuplicateSpoj> dupSpoje = new ArrayList<>();
    private final ArrayList<NapovedaSpoj> napovedaSpoje = new ArrayList<>();

    private int renderTextType = -1;
    private static final int LICHY_ERROR = 0;
    private static final int DOKONCENI = 1;
    private static final int DUPLICATE = 2;
    private long lastTimeChangedTextType;

    private final Lock lock = new ReentrantLock();

    private static class Cell {
        public int x,y;
        public Cell(int x, int y){
            this.x = x;
            this.y = y;
        }
    }

    public Panel(){
        this.setPreferredSize(new Dimension(WIDTH,HEIGHT));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
    }

    /**
     * initial function
     */
    public void start(){
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
        String soubor = "map\\"+loadedMap;
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
                Thread.sleep(10000/60);
            } catch (Exception e){

            }


        }
    }
    public void update() {
        String newMap = NapovedaMenu.getMapName();
        if(newMap != null){
            if(!NapovedaMenu.getMapName().equals(loadedMap)){
                loadedMap = newMap;
                String soubor = "map\\"+loadedMap;
                try {
                    pozadi = ImageIO.read(new File(soubor));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                loadMap(soubor);

                spoje.clear();
                dupSpoje.clear();
                napovedaSpoje.clear();
                saved = false;

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
            }
        }
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
                if (!used[y][x]) {
                    canSave = false;
                    break;
                }
            }
        }
        if(canSave && !saved){
            renderTextType = DOKONCENI;
            lastTimeChangedTextType = System.currentTimeMillis();
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
        if(System.currentTimeMillis() - lastTimeChangedTextType < 2000){
            if(renderTextType != -1){
                g.setFont(new Font("TimesRoman", Font.PLAIN, 16));
                g2.setColor(Color.white);
                if(renderTextType == LICHY_ERROR){
                    g2.drawString("Vznikl lichy pocet bunek, proto nelze dokoncit tah.",423/2-140,482);
                } else if (renderTextType == DOKONCENI){
                    g2.drawString("Vyhral jsi tuhle hru!",423/2-20,482);
                } else if (renderTextType == DUPLICATE){
                    g2.drawString("Domino, ktere se snazis oznacit, je jiz nekde pouzite.",423/2-140,482);
                }
            }
        }

        g2.dispose();
    }

    /**
     * Loads our map
     * @param soubor filepath
     */
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

    /**
     * mouse handler, checks if we clicked some dominos
     * @param e
     */
    public void mouseClick(MouseEvent e){
        Point mouse = MouseInfo.getPointerInfo().getLocation();
        int mouseX = (int) mouse.getX() - this.getLocationOnScreen().x;
        int mouseY = (int) mouse.getY() - this.getLocationOnScreen().y;

        Rectangle mouseR = new Rectangle(mouseX, mouseY,10,10);

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
                            if(!VznikneLichy(x,y,false) && (getOrientation(x,y) & 1) == 1 && (getOrientation(x+1,y) & 1) == 1){
                                createDuplicateSpoj(x,y,false);
                            }
                        }
                        return;
                    }
                    if(VznikneLichy(x,y,false)){ // zamezuje vytvoreni vytvoreni oblasti s lichym poctem
                        return;
                    }
                    if(!used[y][x+1] && !used[y][x]
                            && ((getOrientation(x,y) & 1) == 1 && (getOrientation(x+1,y) & 1) == 1)){
                        used[y][x+1] = true;
                        used[y][x] = true;
                        useDomino(value1,value2,x,y,false);
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
                            if(!VznikneLichy(x,y,true) && (getOrientation(x,y) & 2) == 2 && (getOrientation(x,y+1) & 2) == 2){
                                createDuplicateSpoj(x,y,true);
                            }
                        }
                        return;
                    }
                    if(VznikneLichy(x,y,true)){ // zamezuje vytvoreni vytvoreni oblasti s lichym poctem
                        return;
                    }
                    if(!used[y+1][x] && !used[y][x]
                            && ((getOrientation(x,y) & 2) == 2 && (getOrientation(x,y+1) & 2) == 2)){
                        used[y+1][x] = true;
                        used[y][x] = true;
                        useDomino(value1,value2,x,y,true);
                        Spoj spoj = new Spoj(x,y, true);
                        spoje.add(spoj);
                    }
                }
            }
        }

    }

    /**
     * checks if domino was already used
     * @param a - value1 of domino
     * @param b - value2 of domino
     * @return if domino was used
     */
    public boolean isUsed(int a,int b){
        for(Domino d : dominos){
            if((d.getValue1() == a && d.getValue2() == b) || (d.getValue2() == a && d.getValue1() == b)){
                return d.isUsed();
            }
        }
        return false;
    }

    /**
     * use domino, so we can't use it again
     * @param a - value1 of domino
     * @param b - value2 of domino
     * @param x - col of map
     * @param y - row of map
     * @param v - if move was vertical
     */
    public void useDomino(int a, int b, int x, int y, boolean v){
        for(Domino d : dominos){
            if((d.getValue1() == a && d.getValue2() == b) || (d.getValue2() == a && d.getValue1() == b)){
                d.setUsed(true);
                d.setLoc(x,y);
            }
        }
    }

    /**
     * release domino, so we can use it again
     * @param a - value1 of domino
     * @param b - value2 of domino
     */
    public void freeDomino(int a, int b){
        for(Domino d : dominos){
            if((d.getValue1() == a && d.getValue2() == b) || (d.getValue2() == a && d.getValue1() == b)){
                d.setUsed(false);
                d.setLoc(-1,-1);
            }
        }
    }

    /**
     * if re-clicking already marked domino can be demarked
     * @param a - value1 of domino
     * @param b - value2 of domino
     * @param x - col of map
     * @param y - row of map
     * @return true if can
     */
    public boolean canRemove(int a,int b, int x, int y){
        for(Domino d : dominos){
            if(((d.getValue1() == a && d.getValue2() == b) || (d.getValue2() == a && d.getValue1() == b))
            && d.getX() == x && d.getY() == y){
                return true;
            }
        }
        return false;
    }

    /**
     * creates visual that this domino was already used
     * @param x - col of map
     * @param y - row of map
     * @param vertical - if move was vertical
     */
    public void createDuplicateSpoj(int x,int y, boolean vertical){
        for(DuplicateSpoj spoj : dupSpoje){
            if(spoj.isAlready(x,y)){
                return;
            }
        }
        DuplicateSpoj spoj = new DuplicateSpoj(x, y, vertical);
        dupSpoje.add(spoj);
        renderTextType = DUPLICATE;
        lastTimeChangedTextType = System.currentTimeMillis();
    }

    /**
     * checks if previous move didnt make any odd regions (1,3,5 cells together etc.)
     * @param x - col of map
     * @param y - row of map
     * @param vertical - if previous move was vertical
     * @return true if there is any region with odd cells
     */
    public boolean VznikneLichy(int x, int y, boolean vertical){
        Queue<Cell> emptyCells = new LinkedList<>();
        boolean[][] cloneUsed = new boolean[7][8];
        for(int i = 0;i<7;i++){
            System.arraycopy(used[i], 0, cloneUsed[i], 0, 8);
        }
        if(vertical){
            cloneUsed[y][x] = true;
            cloneUsed[y+1][x] = true;
        } else {
            cloneUsed[y][x] = true;
            cloneUsed[y][x+1] = true;
        }
        for(int i = 0;i<7;i++){
            for(int j = 0;j<8;j++){
                if(cloneUsed[i][j]) continue;

                int xCell = j;
                int yCell = i;
                int count = 1;
                do{
                    if(!emptyCells.isEmpty()){
                        Cell cell = emptyCells.remove();
                        xCell = cell.x;
                        yCell = cell.y;
                    }
                    boolean up = true;
                    boolean left = true;
                    boolean down = true;
                    boolean right = true;

                    if(yCell > 0) up = cloneUsed[yCell-1][xCell];
                    if(yCell < 6) down = cloneUsed[yCell+1][xCell];
                    if(xCell < 7) right = cloneUsed[yCell][xCell+1];
                    if(xCell > 0) left = cloneUsed[yCell][xCell-1];

                    if(!up){
                        count++;
                        emptyCells.add(new Cell(xCell,yCell-1));
                        cloneUsed[yCell][xCell] = true;
                        cloneUsed[yCell-1][xCell] = true;
                    }
                    if(!down){
                        count++;
                        emptyCells.add(new Cell(xCell,yCell+1));
                        cloneUsed[yCell][xCell] = true;
                        cloneUsed[yCell+1][xCell] = true;
                    }
                    if(!left){
                        count++;
                        emptyCells.add(new Cell(xCell-1,yCell));
                        cloneUsed[yCell][xCell] = true;
                        cloneUsed[yCell][xCell-1] = true;
                    }
                    if(!right){
                        count++;
                        emptyCells.add(new Cell(xCell+1,yCell));
                        cloneUsed[yCell][xCell] = true;
                        cloneUsed[yCell][xCell+1] = true;
                    }
                } while (!emptyCells.isEmpty());

                if(count % 2 == 1){
                    renderTextType = LICHY_ERROR;
                    lastTimeChangedTextType = System.currentTimeMillis();
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * saves solution to bmp file
     */
    public void ulozitReseni() {
        BufferedImage image = new BufferedImage(getWidth(), getHeight()-100, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = image.createGraphics();
        this.paint(graphics2D);
        try {
            ImageIO.write(image, "bmp", new File("RESENI.bmp"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * returns if current image is rotated vertically or horizontally
     * @param cislo - number in image
     * @param bi - raw buffer of image
     * @return true if image is rotated vertically
     */
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

    /**
     * function for hint, suggestion for next move
     */
    public void napoveda(){
        Queue<Cell> cells = new LinkedList<>();
        // these have no errors
        cells.add(new Cell(0,0));
        cells.add(new Cell(7,0));
        cells.add(new Cell(0,6));
        cells.add(new Cell(7,6));

        // these can have little errors
        for(int i = 0;i<7;i++){
            cells.add(new Cell(i,0));
            cells.add(new Cell(i,6));
        }
        for(int i = 0;i<6;i++){
            cells.add(new Cell(0,i));
            cells.add(new Cell(7,i));
        }
        // corners
        do{
            Cell cell = cells.remove();
            int x = cell.x;
            int y = cell.y;

            int value1;
            int value2;
            boolean right, left, up, down;
            if (x + 1 < 8) {
                value1 = map[y][x];
                value2 = map[y][x + 1];
                right = used[y][x] || used[y][x + 1] || isUsed(value1, value2) || VznikneLichy(x, y, false)
                        || !((getOrientation(x, y) & 1) == 1)
                        || (getOrientation(x, y) & 3) == 3;

                if (!right) {
                    napovedaSpoje.add(new NapovedaSpoj(x,y,false));
                    return;
                }
            }
            if (x - 1 >= 0) {
                value1 = map[y][x];
                value2 = map[y][x - 1];
                left = used[y][x] || used[y][x - 1] || isUsed(value1, value2) || VznikneLichy(x-1, y, false)
                        || !((getOrientation(x, y) & 1) == 1)
                        || (getOrientation(x, y) & 3) == 3;
                if (!left) {
                    napovedaSpoje.add(new NapovedaSpoj(x-1,y,false));
                    return;
                }
            }
            if (y + 1 < 7) {
                value1 = map[y + 1][x];
                value2 = map[y][x];
                down = used[y][x] || used[y + 1][x] || isUsed(value1, value2) || VznikneLichy(x, y, true)
                        || !((getOrientation(x, y) & 2) == 2)
                        || (getOrientation(x, y) & 3) == 3;
                if (!down) {
                    napovedaSpoje.add(new NapovedaSpoj(x,y,true));
                    return;
                }
            }
            if (y - 1 >= 0) {
                value1 = map[y - 1][x];
                value2 = map[y][x];
                up = used[y][x] || used[y - 1][x] || isUsed(value1, value2) || VznikneLichy(x, y-1, true)
                        || !((getOrientation(x, y) & 2) == 2)
                        || (getOrientation(x, y) & 3) == 3;
                if (!up) {
                    napovedaSpoje.add(new NapovedaSpoj(x,y-1,true));
                    return;
                }
            }
        } while(!cells.isEmpty());
        // with two rotations
        for(int y = 0;y<7;y++){
            for(int x = 0;x<8;x++){
                if(used[y][x]) continue;
                boolean down;
                boolean right;
                int value1;
                int value2;
                if(x < 7){
                    value1 = map[y][x];
                    value2 = map[y][x+1];
                    right = used[y][x] || used[y][x+1] || isUsed(value1,value2)  || VznikneLichy(x,y,false)
                            || !((getOrientation(x,y) & 1) == 1 && (getOrientation(x+1,y) & 1) == 1)
                            || !((getOrientation(x,y) & 3) != 3 && (getOrientation(x+1,y) & 3) != 3);
                    if(!right){
                        napovedaSpoje.add(new NapovedaSpoj(x,y,false));
                        return;
                    }
                }
                if(y < 6){
                    value1 = map[y+1][x];
                    value2 = map[y][x];
                    down = used[y][x] || used[y+1][x] || isUsed(value1,value2) || VznikneLichy(x,y,true)
                            || !((getOrientation(x,y) & 2) == 2 && (getOrientation(x,y+1) & 2) == 2)
                            || !((getOrientation(x,y) & 3) != 3 && (getOrientation(x,y+1) & 3) != 3);
                    if(!down){
                        napovedaSpoje.add(new NapovedaSpoj(x,y,true));
                        return;
                    }
                }
            }
        }
        // with one rotation
        for(int y = 0;y<7;y++){
            for(int x = 0;x<8;x++){
                if(used[y][x]) continue;
                boolean down = true;
                boolean right = true;
                int value1;
                int value2;
                if(x < 7){
                    value1 = map[y][x];
                    value2 = map[y][x+1];
                    right = used[y][x] || used[y][x+1] || isUsed(value1,value2)  || VznikneLichy(x,y,false)
                            || !((getOrientation(x,y) & 1) == 1 && (getOrientation(x+1,y) & 1) == 1)
                            || !((getOrientation(x,y) & 3) != 3 || (getOrientation(x+1,y) & 3) != 3);
                    if(!right){
                        napovedaSpoje.add(new NapovedaSpoj(x,y,false));
                        return;
                    }
                }
                if(y < 6){
                    value1 = map[y+1][x];
                    value2 = map[y][x];
                    down = used[y][x] || used[y+1][x] || isUsed(value1,value2) || VznikneLichy(x,y,true)
                            || !((getOrientation(x,y) & 2) == 2 && (getOrientation(x,y+1) & 2) == 2)
                            || !((getOrientation(x,y) & 3) != 3 || (getOrientation(x,y+1) & 3) != 3);
                    if(!down){
                        napovedaSpoje.add(new NapovedaSpoj(x,y,true));
                        return;
                    }
                }
            }
        }
        // without any rotations
        for(int y = 0;y<7;y++){
            for(int x = 0;x<8;x++){
                if(used[y][x]) continue;
                boolean down = true;
                boolean right = true;
                int value1;
                int value2;
                if(x < 7){
                    value1 = map[y][x];
                    value2 = map[y][x+1];
                    right = used[y][x] || used[y][x+1] || isUsed(value1,value2)  || VznikneLichy(x,y,false)
                            || !((getOrientation(x,y) & 1) == 1 && (getOrientation(x+1,y) & 1) == 1);
                    if(!right){
                        napovedaSpoje.add(new NapovedaSpoj(x,y,false));
                        return;
                    }
                }
                if(y < 6){
                    value1 = map[y+1][x];
                    value2 = map[y][x];
                    down = used[y][x] || used[y+1][x] || isUsed(value1,value2) || VznikneLichy(x,y,true)
                            || !((getOrientation(x,y) & 2) == 2 && (getOrientation(x,y+1) & 2) == 2);
                    if(!down){
                        napovedaSpoje.add(new NapovedaSpoj(x,y,true));
                        return;

                    }
                }
            }
        }
    }
    // returns if cell can be vertical/horizontal or universal
    // 1 - horizontal
    // 2 - vertical
    // 3 - universal
    private int getOrientation(int x, int y){
        if(map[y][x] == 1 || map[y][x] == 4 || map[y][x] == 5 || map[y][x] == 0) return 3;
        else if(rotated[y][x]) return 2;
        else return 1;
    }

    /**
     * function for solving game
     */
    public void vyresit(){
        spoje.clear();
        dupSpoje.clear();
        napovedaSpoje.clear();
        saved = false;
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
        for(int i = 0;i<7;i++){
            Arrays.fill(used[i],false);

        }
        vyresitBunku(0,0);
    }

    /**
     * Tries to solve game, finding correct dominos
     * @param x - col of map
     * @param y - row of map
     * @return false if any domino cant be created on empty cell -> backtracing and redoing previous dominos, returns true if everything is fine
     */
    private boolean vyresitBunku(int x, int y){
        boolean down;
        boolean right;
        int value1;
        int value2;
        if(y == 6 && x >= 8) return true; // last empty cell
        if(x >= 8){
            y++;
            x = 0;
        }
        if(used[y][x]){
            if(vyresitBunku(x+1,y)) return true;
        }
        if(x < 7){
            value1 = map[y][x];
            value2 = map[y][x+1];
            right = used[y][x] || used[y][x+1] || isUsed(value1,value2)  || VznikneLichy(x,y,false)
                    || !((getOrientation(x,y) & 1) == 1 && (getOrientation(x+1,y) & 1) == 1);
            if(!right){
                useDomino(value1,value2,x,y, false);
                if(!allDominosAreUsable()){
                    freeDomino(value1,value2);
                } else {
                    used[y][x+1] = true;
                    used[y][x] = true;
                    Spoj spoj = new Spoj(x,y, false);
                    spoje.add(spoj);

                    if(vyresitBunku(x+2,y)){
                        return true;
                    }
                    used[y][x+1] = false;
                    used[y][x] = false;
                    spoje.remove(spoj);
                    freeDomino(value1,value2);
                }

            }
        }
        if(y < 6){
            value1 = map[y+1][x];
            value2 = map[y][x];
            down = used[y][x] || used[y+1][x] || isUsed(value1,value2) || VznikneLichy(x,y,true)
                    || !((getOrientation(x,y) & 2) == 2 && (getOrientation(x,y+1) & 2) == 2);

            if(!down){
                useDomino(value1,value2,x,y,true);
                if(!allDominosAreUsable()){
                    freeDomino(value1,value2);
                } else {
                    used[y+1][x] = true;
                    used[y][x] = true;
                    Spoj spoj = new Spoj(x,y, true);
                    spoje.add(spoj);

                    if(vyresitBunku(x+1,y)){
                        return true;
                    }
                    used[y+1][x] = false;
                    used[y][x] = false;
                    spoje.remove(spoj);
                    freeDomino(value1,value2);
                }

            }
        }
        return false;
    }

    /**
     * Checks if all not used dominos can be used in game
     * @return true if they can, false if they cant
     */
    private boolean allDominosAreUsable() {
        for(Domino d : dominos) {
            if(!d.isUsed()) {
                boolean canBeCreated = false;
                int value1 = d.getValue1();
                int value2 = d.getValue2();
                for(int y = 0;y<7;y++) {
                    for (int x = 0; x < 8; x++) {
                        if(!used[y][x]) {
                            if(x < 7){
                                boolean right = used[y][x] || used[y][x+1] || isUsed(value1,value2)  || VznikneLichy(x,y,false)
                                        || !((getOrientation(x,y) & 1) == 1 && (getOrientation(x+1,y) & 1) == 1)
                                        || !((map[y][x] == value1 && map[y][x+1] == value2) || (map[y][x] == value2 && map[y][x+1] == value1));
                                if(!right) {
                                    canBeCreated = true;
                                    break;
                                }
                            }
                            if(y < 6){
                                boolean down = used[y][x] || used[y+1][x] || isUsed(value1,value2) || VznikneLichy(x,y,true)
                                        || !((getOrientation(x,y) & 2) == 2 && (getOrientation(x,y+1) & 2) == 2)
                                        || !((map[y][x] == value1 && map[y+1][x] == value2) || (map[y][x] == value2 && map[y+1][x] == value1));
                                if(!down){
                                    canBeCreated = true;
                                    break;
                                }
                            }
                        }
                    }
                }
                if(!canBeCreated) return false;
            }
        }
        return true;
    }
}
