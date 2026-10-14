package game.model;

public class Point {
    public int color = 0;
    public byte dis;
    public int g;
    public int h;
    public int limitY;
    public int v;
    public int w;
    public int x;
    public int y;

    public Point() {
    }

    public Point(int i, int i2) {
        this.x = i;
        this.y = i2;
    }
}
