public class Spoj {
    int indexX;
    int indexY;
    boolean vertical;
    public Spoj(int x, int y, boolean v){
        this.indexX = x;
        this.indexY = y;
        this.vertical = v;
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
