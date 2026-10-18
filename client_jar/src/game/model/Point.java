package game.model;

public class Point {
    public int x;
    public int y;
    public int g;
    public int v;
    public int w;
    public int h;
    public int color = 0;
    public int limitY;
    public int vx;
    public int vy;
    public int f;
    public byte dis;
    public int frame;

    public Point() {
    }

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void update() {
        this.f++;
        this.x += this.vx;
        this.y += this.vy;
    }
}
