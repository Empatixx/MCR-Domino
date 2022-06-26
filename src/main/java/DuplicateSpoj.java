public class DuplicateSpoj {
    public int indexX;
    public int indexY;
    boolean vertical;
    long time;
    public DuplicateSpoj(int x,int y, boolean v){
        this.indexX = x;
        this.indexY = y;
        this.vertical= v;

        time = System.currentTimeMillis();

    }
    public boolean isAlready(int x,int y){
        return indexX == x && indexY == y;
    }

    public boolean isVertical() {
        return vertical;
    }

    public boolean shouldRemove(){
        return System.currentTimeMillis() - time > 1000;
    }
}
