package game.model;

import effect.DataEffectPokemonHuyenThoai;
import game.render.Res;
import game.render.screen.GameData;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.Hashtable;
import java.util.Vector;
import game.pokemon.PokeBody;
import game.pokemon.PokeDuLieu;
import javax.microedition.lcdui.Graphics;

public class PokemonHuyenThoai extends Char {
    public static Hashtable ALL_DATA_THAN_THU = new Hashtable();
    short p1;
    short p2;
    short p3;
    short vx;
    short vy;
    public static final byte STAND = 0;
    public static final byte DEAD = 1;
    public static final byte WALK = 2;
    public static final byte ATTACK = 3;
    public static final byte INJURE = 4;
    public static final byte DEADFLY = 5;
    public byte idThanThu = 0;
    public Vector data = new Vector();
    short lastDir = -100;
    byte huongY = 0;
    byte flip = 0;
    short xTo = 0;
    short yTo = 0;

    public PokemonHuyenThoai() {
        this.catagory = (byte) 0;
        this.speed = (short) 3;
        this.centerX = (short) 12;
        this.centerY = (short) 12;
        this.height = (short) 55;
        this.width = (short) 40;
        this.hp = 1;
        this.dyThanthu = (byte) 15;
        this.realHP = 1;
        this.dir = (short) LiveActor.r.nextInt(4);
        this.isDie = false;
    }

    @Override
    public int getIdThanThu() {
        return this.idThanThu;
    }

    public static void setDataThanThu(int id, byte[] array) {
        DataInputStream dis;
        try {
            try {
                Vector dataMonsterNew = new Vector();
                Vector temp = (Vector) ALL_DATA_THAN_THU.get(new StringBuffer().append(id).toString());
                if (temp == null && (dis = new DataInputStream(new ByteArrayInputStream(array))) != null) {
                    for (int i = 0; i < 1; i++) {
                        byte[] data = new byte[(short) dis.available()];
                        dis.read(data, 0, data.length);
                        dataMonsterNew.addElement(new DataEffectPokemonHuyenThoai(data));
                    }
                    ALL_DATA_THAN_THU.put(new StringBuffer().append(id).toString(), dataMonsterNew);
                    dis.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e2) {
        }
    }

    @Override
    public void setIdThanThu(int id) {
        this.idThanThu = (byte) id;
        try {
            this.data = (Vector) ALL_DATA_THAN_THU.get(new StringBuffer().append(id).toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setPosTo(short x, short y) {
        moveTo(x, y);
    }

    @Override
    public void moveTo(short xTo, short yTo) {
        this.xTo = xTo;
        this.yTo = yTo;
        if (this.x == xTo && this.y == yTo) {
            if (this.state != 3 && this.state != 1) {
                this.state = (byte) 0;
                return;
            }
            return;
        }
        this.state = (byte) 2;
    }

    public void setMove(int i, int j) {
        this.speed = (short) 4;
        boolean isMatchX = false;
        boolean isMatchY = false;
        int dx1 = Math.abs(this.x - i);
        int dy1 = Math.abs(this.y - j);
        if (dx1 <= this.speed) {
            this.x = (short) i;
            isMatchX = true;
        }
        if (dy1 < this.speed) {
            this.y = (short) j;
            isMatchY = true;
        }
        if (isMatchX && isMatchY) {
            this.p3 = (short) 0;
            this.p2 = (short) 0;
            this.p1 = (short) 0;
            this.state = (byte) 0;
            this.vy = (short) 0;
            this.vx = (short) 0;
            this.lastDir = this.dir;
        } else if (this.x < i) {
            this.x = (short) (this.x + this.speed);
            this.dir = (short) 3;
        } else if (this.x > i) {
            this.x = (short) (this.x - this.speed);
            this.dir = (short) 2;
        } else if (this.y > j) {
            this.y = (short) (this.y - this.speed);
            this.dir = (short) 1;
        } else if (this.y < j) {
            this.dir = (short) 0;
            this.y = (short) (this.y + this.speed);
        }
        setHuong(i, j);
    }

    public void setHuong(int xto, int yto) {
        this.huongY = (byte) (this.y > yto ? 1 : 0);
        this.flip = (byte) (this.x - xto > 0 ? 0 : 2);
        if (this.data.size() > 0) {
            this.flip = (byte) 0;
            if (this.dir == 3) {
                this.flip = (byte) 2;
                return;
            }
            if (this.dir != 2) {
                if (this.dir == 0) {
                    this.huongY = (byte) 0;
                } else if (this.dir == 1) {
                    this.huongY = (byte) 1;
                }
            }
        }
    }

    /** pokemon: than thu (Hoa ky lan cap 1..3 = idThanThu 0..2) -> Ponyta, Rapidash, Rapidash shiny. */
    public static final short[] POKE_DEX_THAN_THU = {77, 78, 78};
    public static final boolean[] POKE_SHINY_THAN_THU = {false, false, true};

    private boolean paintPokemonThanThu(Graphics g) {
        if (this.idThanThu < 0 || this.idThanThu >= POKE_DEX_THAN_THU.length) {
            return false;
        }
        int dex = POKE_DEX_THAN_THU[this.idThanThu];
        boolean shiny = POKE_SHINY_THAN_THU[this.idThanThu];
        if (!PokeBody.coSprite(dex, shiny)) {
            return false;
        }
        int dongTac;
        switch (this.state) {
            case DEAD:
            case DEADFLY:
                dongTac = PokeDuLieu.GUC_NGA;
                break;
            case WALK:
                dongTac = PokeDuLieu.CHAY;
                break;
            case ATTACK:
                dongTac = PokeDuLieu.DANH_GAN;
                break;
            case INJURE:
                dongTac = PokeDuLieu.TRUNG_DON;
                break;
            default:
                dongTac = PokeDuLieu.DUNG;
                break;
        }
        g.drawImage(Res.imgShadow, this.x, this.y - 2, 3);
        PokeBody.paint(g, dex, shiny, this.x + this.vangx, this.y + this.vangy, this.dir, dongTac, pokeTickTrongDongTac(dongTac), 100);
        return true;
    }

    @Override
    public void paint(Graphics g) {
        if (this.isDie) {
            return;
        }
        if (paintPokemonThanThu(g)) {
            return;
        }
        if (this.data.size() == 0) {
            return;
        }
        try {
            DataEffectPokemonHuyenThoai deff = (DataEffectPokemonHuyenThoai) this.data.elementAt(0);
            if (deff != null) {
                paintTopDataEff(g);
                int status = this.state;
                ImageIcon img = GameData.getImgIcon((short) (this.idThanThu + 12000));
                g.drawImage(Res.imgShadow, this.x + (this.flip == 2 ? (short) -10 : (short) 10), this.y - 10, 3);
                if (img != null && img.img != null) {
                    deff.paint(g, deff.getFrame(this.frame, status, this.huongY), this.x + this.vangx, this.y + this.vangy, this.flip, img.img);
                }
            }
            paintBottomDataEff(g);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void paintCorner(Graphics g, int corner, int corner2) {
    }

    @Override
    public void startAttack(LiveActor target, AttackResult atr, byte skillID, byte level) {
        this.state = (byte) 3;
        this.frame = (byte) 0;
    }

    @Override
    public void update() {
        if (this.data.size() > 0) {
            DataEffectPokemonHuyenThoai deff = (DataEffectPokemonHuyenThoai) this.data.elementAt(0);
            int status = this.state;
            if (this.state == 3 && this.frame >= deff.getAnim(status, this.huongY).frame.length - 1) {
                status = 0;
                this.state = (byte) 0;
            }
            this.frame = (byte) ((this.frame + 1) % deff.getAnim(status, this.huongY).frame.length);
        }
        switch (this.state) {
            case 1:
                if (this.hp > 0 && this.realHP > 0) {
                    this.state = (byte) 0;
                    break;
                }
                break;
            case 2:
                setMove(this.xTo, this.yTo);
                break;
        }
    }
}
