public class Domino {
    private int value1;
    private int value2;

    private boolean used;

    private int x,y;
    public Domino(int value1,int value2){
        this.value1 = value1;
        this.value2 = value2;
        used = false;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public int getValue1() {
        return value1;
    }

    public int getValue2() {
        return value2;
    }

    public boolean isUsed() {
        return used;
    }
    public void setLoc(int x, int y){
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
