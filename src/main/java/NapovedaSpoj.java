public class NapovedaSpoj {
    int indexX;
    int indexY;
    boolean vertical;

    long time;
    public NapovedaSpoj(int x, int y, boolean v){
        this.indexX = x;
        this.indexY = y;
        this.vertical = v;

        time = System.currentTimeMillis();

    }
    public boolean shouldRemove(){
        return System.currentTimeMillis() - time > 1500;
    }

    public int getIndexX() {
        return indexX;
    }

    public int getIndexY() {
        return indexY;
    }

    public boolean isVertical() {
        return vertical;
    }
    public boolean isThisIndex(int x, int y){
        return x == this.indexX && y == this.indexY;
    }
}