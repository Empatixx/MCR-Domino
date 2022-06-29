public class LastMove {
    private Domino domino;
    private int x, y;
    private boolean vertical;
    public LastMove(int x, int y, Domino d, boolean vertical){
        this.x = x;
        this.y = y;
        domino = d;
        this.vertical = vertical;
    }

    public boolean isDomino(Domino d) {
        return domino.equals(d);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
    public void free(){
        domino.setUsed(false);
        domino.setLoc(-1,-1);
    }

    public boolean isVertical() {
        return vertical;
    }
}
