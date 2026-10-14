package game.model;

import game.render.Res;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class CharPartInfo {
    public int type;
    public int id;
    public int[][] x;
    public int[][] y;
    public int[][] w;
    public int[][] h;
    public Image image;
    int avxf;
    int avyf;
    int avx0;
    int avy0;
    int avw0;
    int avh0;
    long timePaint;
    public int time;
    public static final byte[][] PART_OF_FRAME = {new byte[]{0, 0, 1, 2, 3, 4}, new byte[]{0, 0, 1, 2, 3, 4}, new byte[6], new byte[6], new byte[]{0, 1, 0, 1, 0, 1}};
    static int[] indexHeadFrame = {0, 2, 1};
    public int timeRemove = 0;
    int[][] xhd = {new int[]{1, 1, 0}, new int[]{1, 1, 0}};
    int[][] yhd = {new int[]{3, 2, 2}, new int[]{3, 2, 2}};
    int zoom = 1;
    int wFrame = 17;
    public int[][] dx = new int[4][6];
    public int[][] dy = new int[4][6];

    public void load(byte[] arr, int type, int Id) {
        ByteArrayInputStream by = null;
        DataInputStream data = null;
        ByteArrayInputStream bt = null;
        DataInputStream is = null;
        try {
            ByteArrayInputStream by2 = new ByteArrayInputStream(arr);
            DataInputStream data2 = new DataInputStream(by2);
            byte[] img = new byte[data2.readShort()];
            data2.read(img, 0, img.length);
            this.image = Image.createImage(img, 0, img.length);
            byte[] dtt = new byte[data2.readShort()];
            data2.read(dtt, 0, dtt.length);
            ByteArrayInputStream bt2 = new ByteArrayInputStream(dtt);
            DataInputStream is2 = new DataInputStream(bt2);
            this.avx0 = Res.readSignByte(is2);
            this.avy0 = Res.readSignByte(is2);
            this.avw0 = Res.readSignByte(is2);
            this.avh0 = Res.readSignByte(is2);
            this.avxf = Res.readSignByte(is2);
            this.avyf = Res.readSignByte(is2);
            try {
                if (type < 4) {
                    for (int dir = 0; dir < 4; dir++) {
                        for (int i = 0; i < this.x[dir].length; i++) {
                            this.x[dir][i] = is2.read();
                            this.y[dir][i] = is2.read();
                            this.w[dir][i] = is2.read();
                            this.h[dir][i] = is2.read();
                        }
                        for (int i2 = 0; i2 < 6; i2++) {
                            this.dx[dir][i2] = Res.readSignByte(is2);
                            this.dy[dir][i2] = Res.readSignByte(is2);
                        }
                    }
                } else {
                    for (int dir2 = 0; dir2 < 3; dir2++) {
                        for (int i3 = 0; i3 < this.x[dir2].length; i3++) {
                            this.x[dir2][i3] = is2.read();
                            this.y[dir2][i3] = is2.read();
                            this.w[dir2][i3] = is2.read();
                            this.h[dir2][i3] = is2.read();
                        }
                    }
                    for (int dir3 = 0; dir3 < 4; dir3++) {
                        for (int i4 = 0; i4 < 6; i4++) {
                            this.dx[dir3][i4] = Res.readSignByte(is2);
                            this.dy[dir3][i4] = Res.readSignByte(is2);
                        }
                    }
                }
            } catch (Exception e) {
            }
            try {
                by2.close();
            } catch (Exception e2) {
            }
            try {
                bt2.close();
            } catch (Exception e3) {
            }
            try {
                is2.close();
            } catch (Exception e4) {
            }
            try {
                data2.close();
            } catch (Exception e5) {
            }
        } catch (Exception e6) {
            try {
                by.close();
            } catch (Exception e7) {
            }
            try {
                bt.close();
            } catch (Exception e8) {
            }
            try {
                is.close();
            } catch (Exception e9) {
            }
            try {
                data.close();
            } catch (Exception e10) {
            }
        } catch (Throwable th) {
            try {
                by.close();
            } catch (Exception e11) {
            }
            try {
                bt.close();
            } catch (Exception e12) {
            }
            try {
                is.close();
            } catch (Exception e13) {
            }
            try {
                data.close();
            } catch (Exception e14) {
            }
            throw th;
        }
    }

    public void loadCoat(int type, int id) {
        ByteArrayInputStream by = null;
        DataInputStream data = null;
        ByteArrayInputStream bt = null;
        DataInputStream is = null;
        try {
            data = new DataInputStream("".getClass().getResourceAsStream(new StringBuffer(String.valueOf(FilePack.charAvatar[type])).append(id).toString()));
            byte[] img = new byte[data.readShort()];
            data.read(img, 0, img.length);
            this.image = Image.createImage(img, 0, img.length);
            byte[] dtt = new byte[data.readShort()];
            data.read(dtt, 0, dtt.length);
            bt = new ByteArrayInputStream(dtt);
            is = new DataInputStream(bt);
            this.avx0 = Res.readSignByte(is);
            this.avy0 = Res.readSignByte(is);
            this.avw0 = Res.readSignByte(is);
            this.avh0 = Res.readSignByte(is);
            this.avxf = Res.readSignByte(is);
            this.avyf = Res.readSignByte(is);
            for (int dir = 0; dir < 3; dir++) {
                for (int i = 0; i < this.x[dir].length; i++) {
                    this.x[dir][i] = is.read();
                    this.y[dir][i] = is.read();
                    this.w[dir][i] = is.read();
                    this.h[dir][i] = is.read();
                }
            }
            for (int dir2 = 0; dir2 < 4; dir2++) {
                for (int i2 = 0; i2 < 6; i2++) {
                    this.dx[dir2][i2] = Res.readSignByte(is);
                    this.dy[dir2][i2] = Res.readSignByte(is);
                }
            }
            is.close();
            try {
                by.close();
            } catch (Exception e) {
            }
            try {
                bt.close();
            } catch (Exception e2) {
            }
            try {
                is.close();
            } catch (Exception e3) {
            }
            try {
                data.close();
            } catch (Exception e4) {
            }
        } catch (Exception e5) {
            try {
                by.close();
            } catch (Exception e6) {
            }
            try {
                bt.close();
            } catch (Exception e7) {
            }
            try {
                is.close();
            } catch (Exception e8) {
            }
            try {
                data.close();
            } catch (Exception e9) {
            }
        } catch (Throwable th) {
            try {
                by.close();
            } catch (Exception e10) {
            }
            try {
                bt.close();
            } catch (Exception e11) {
            }
            try {
                is.close();
            } catch (Exception e12) {
            }
            try {
                data.close();
            } catch (Exception e13) {
            }
            throw th;
        }
    }

    public void loadtest(int type, int id) {
        try {
            int x2 = this.zoom;
            String url = "/c/1_body_data";
            String urlimg = "/c/1_body.png";
            if (type == 0) {
                url = "/c/1_leg_data";
                urlimg = "/c/1_leg.png";
            } else if (type == 2) {
                url = "/c/1_head_data";
                urlimg = "/c/1_head.png";
            }
            DataInputStream is = new DataInputStream("".getClass().getResourceAsStream(url));
            this.image = Image.createImage(urlimg);
            this.avx0 = Res.readSignByte(is);
            this.avy0 = Res.readSignByte(is);
            this.avw0 = Res.readSignByte(is);
            this.avh0 = Res.readSignByte(is);
            this.avxf = Res.readSignByte(is);
            this.avyf = Res.readSignByte(is);
            for (int dir = 0; dir < 4; dir++) {
                for (int i = 0; i < this.x[dir].length; i++) {
                    try {
                        this.x[dir][i] = is.read() * x2;
                        this.y[dir][i] = is.read() * x2;
                        this.w[dir][i] = is.read() * x2;
                        this.h[dir][i] = is.read() * x2;
                    } catch (Exception e) {
                        e.printStackTrace();
                        is.close();
                    }
                }
                for (int i2 = 0; i2 < 6; i2++) {
                    this.dx[dir][i2] = Res.readSignByte(is) * x2;
                    this.dy[dir][i2] = Res.readSignByte(is) * x2;
                }
            }
            is.close();
        } catch (Exception e2) {
        }
    }

    public void load(int type, int id) {
        ByteArrayInputStream by = null;
        DataInputStream data = null;
        ByteArrayInputStream bt = null;
        DataInputStream is = null;
        try {
            data = new DataInputStream("".getClass().getResourceAsStream(new StringBuffer(String.valueOf(FilePack.charAvatar[type])).append(id).toString()));
            byte[] img = new byte[data.readShort()];
            data.read(img, 0, img.length);
            this.image = Image.createImage(img, 0, img.length);
            byte[] dtt = new byte[data.readShort()];
            data.read(dtt, 0, dtt.length);
            bt = new ByteArrayInputStream(dtt);
            is = new DataInputStream(bt);
            this.avx0 = Res.readSignByte(is);
            this.avy0 = Res.readSignByte(is);
            this.avw0 = Res.readSignByte(is);
            this.avh0 = Res.readSignByte(is);
            this.avxf = Res.readSignByte(is);
            this.avyf = Res.readSignByte(is);
            for (int dir = 0; dir < 4; dir++) {
                for (int i = 0; i < this.x[dir].length; i++) {
                    try {
                        this.x[dir][i] = is.read();
                        this.y[dir][i] = is.read();
                        this.w[dir][i] = is.read();
                        this.h[dir][i] = is.read();
                    } catch (Exception e) {
                    }
                }
                for (int i2 = 0; i2 < 6; i2++) {
                    this.dx[dir][i2] = Res.readSignByte(is);
                    this.dy[dir][i2] = Res.readSignByte(is);
                }
            }
            try {
                by.close();
            } catch (Exception e2) {
            }
            try {
                bt.close();
            } catch (Exception e3) {
            }
            try {
                is.close();
            } catch (Exception e4) {
            }
            try {
                data.close();
            } catch (Exception e5) {
            }
        } catch (Exception e6) {
            try {
                by.close();
            } catch (Exception e7) {
            }
            try {
                bt.close();
            } catch (Exception e8) {
            }
            try {
                is.close();
            } catch (Exception e9) {
            }
            try {
                data.close();
            } catch (Exception e10) {
            }
        } catch (Throwable th) {
            try {
                by.close();
            } catch (Exception e11) {
            }
            try {
                bt.close();
            } catch (Exception e12) {
            }
            try {
                is.close();
            } catch (Exception e13) {
            }
            try {
                data.close();
            } catch (Exception e14) {
            }
            throw th;
        }
    }

    public CharPartInfo(int type, int id) {
        int a;
        this.type = type;
        this.id = id;
        switch (type) {
            case 0:
            case 1:
                a = 5;
                break;
            case 2:
            case 3:
                a = 1;
                break;
            default:
                a = 2;
                break;
        }
        this.x = new int[4][a];
        this.y = new int[4][a];
        this.w = new int[4][a];
        this.h = new int[4][a];
        if (type < 4) {
            load(type, id);
        } else {
            loadCoat(type, id);
        }
    }

    public void paint(Graphics g, int xp, int yp, int dir, int frame) {
        if (this.type < 0) {
            return;
        }
        byte rota = 0;
        int DIR = dir;
        if (dir > 2) {
            rota = 2;
            DIR = 2;
        }
        if (this.image != null) {
            int xx = this.dx[DIR][frame];
            int yy = this.dy[DIR][frame];
            int ww = this.w[DIR][PART_OF_FRAME[this.type][frame]];
            int hh = this.h[DIR][PART_OF_FRAME[this.type][frame]];
            int xxx = this.x[DIR][PART_OF_FRAME[this.type][frame]];
            int yyy = this.y[DIR][PART_OF_FRAME[this.type][frame]];
            if (xxx > this.image.getWidth()) {
                xxx = 0;
            }
            if (yyy > this.image.getHeight()) {
                yyy = 0;
            }
            if (xxx + ww > this.image.getWidth()) {
                ww = this.image.getWidth() - xxx;
            }
            if (yyy + hh > this.image.getHeight()) {
                hh = this.image.getHeight() - yyy;
            }
            if (dir > 2) {
                xx = (-this.dx[DIR][frame]) - ww;
                yy = this.dy[DIR][frame];
            }
            g.drawRegion(this.image, xxx, yyy, ww, hh, rota, xp + xx, yp + yy, 0);
        } else {
            Res.paintDefault(g, xp, yp, dir, frame, this.type);
        }
        this.timePaint = System.currentTimeMillis();
    }

    public void paintStatic(Graphics g, short xp, short yp, int dir, int frame) {
        try {
            if (this.image != null) {
                int ww = this.w[dir][PART_OF_FRAME[this.type][frame]];
                int hh = this.h[dir][PART_OF_FRAME[this.type][frame]];
                int xxx = this.x[dir][PART_OF_FRAME[this.type][frame]];
                int yyy = this.y[dir][PART_OF_FRAME[this.type][frame]];
                if (xxx > this.image.getWidth()) {
                    xxx = 0;
                }
                if (yyy > this.image.getHeight()) {
                    yyy = 0;
                }
                if (xxx + ww > this.image.getWidth()) {
                    ww = this.image.getWidth() - xxx;
                }
                if (yyy + hh > this.image.getHeight()) {
                    hh = this.image.getHeight() - yyy;
                }
                g.drawRegion(this.image, xxx, yyy, ww, hh, 0, xp, yp, 3);
            }
        } catch (Exception e) {
            System.out.println(new StringBuffer("LOI PART ").append(this.id).toString());
        }
    }

    public void paintAvatar(Graphics g, short xp, short yp, int frame) {
        try {
            if (this.image != null) {
                g.drawRegion(this.image, this.avx0, this.avy0, this.avw0, this.avh0, 0, xp + this.avxf, yp + this.avyf, 0);
            }
        } catch (Exception e) {
        }
    }

    public void paintImage(Graphics g, int x, int y) {
        g.drawImage(this.image, x, y, 0);
    }
}
