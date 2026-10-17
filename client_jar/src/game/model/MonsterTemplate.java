package game.model;

import effect.DataEffect;
import game.networklogic.GameService;
import game.render.Res;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Vector;
import game.pokemon.PokeBody;
import game.pokemon.VatThePokemon;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class MonsterTemplate {
    public static Hashtable ALL_DATE_NEW_MONSTER = new Hashtable();
    public int type;
    public Image image;
    byte moveType;
    byte speed;
    byte height;
    public byte w;
    public byte h;
    public byte xCenter;
    public byte yCenter;
    public byte xadd;
    public byte yadd;
    public byte isNewMonster;
    public String name;
    public byte he;
    public byte palate;
    public byte spalate;
    public int maxhp;
    public boolean isLoad;
    public ImageObj imageObj;
    public Vector dataMonsterNew;
    public long timePaint;
    byte[] arrayDataMonsterNew;

    /**
     * pokemon: Pokemon cua tung template quai, index = idTemplate (lenh 26 dem bang 1 byte).
     * Nhan qua lenh -91 POKE_QUAI ngay sau lenh 26. pokeDex = 0: ve kieu cu. pokeTiLe: % (100 = 1x).
     */
    public static short[] pokeDex = new short[256];
    public static boolean[] pokeShiny = new boolean[256];
    public static short[] pokeTiLe = new short[256];

    public static void datPoke(int idTemplate, int dex, boolean shiny, int tiLe) {
        if (idTemplate < 0 || idTemplate >= pokeDex.length) {
            return;
        }
        pokeDex[idTemplate] = (short) dex;
        pokeShiny[idTemplate] = shiny;
        pokeTiLe[idTemplate] = (short) (tiLe <= 0 ? 100 : tiLe);
    }

    public static void xoaPoke() {
        for (int i = 0; i < pokeDex.length; i++) {
            pokeDex[i] = 0;
            pokeShiny[i] = false;
            pokeTiLe[i] = 100;
        }
    }

    public static int layPokeDex(int idTemplate) {
        if (idTemplate < 0 || idTemplate >= pokeDex.length) {
            return 0;
        }
        return pokeDex[idTemplate];
    }

    /** Template nay ve bang PokeBody va co du sprite trong jar. */
    public static boolean laPokemon(int idTemplate) {
        int dex = layPokeDex(idTemplate);
        return dex > 0 && PokeBody.coSprite(dex, pokeShiny[idTemplate]);
    }

    public MonsterTemplate() {
        this.type = -1;
        this.speed = (byte) 1;
        this.isNewMonster = (byte) 0;
        this.isLoad = false;
        this.dataMonsterNew = new Vector();
        this.arrayDataMonsterNew = null;
    }

    public MonsterTemplate(int type, String name, String image, byte moveType, byte speed, byte height, byte w, byte h, byte xCenter, byte yCenter, byte xadd, byte yadd) {
        this.type = -1;
        this.speed = (byte) 1;
        this.isNewMonster = (byte) 0;
        this.isLoad = false;
        this.dataMonsterNew = new Vector();
        this.arrayDataMonsterNew = null;
        this.type = type;
        this.name = name;
        this.moveType = moveType;
        this.speed = speed;
        this.height = height;
        this.w = w;
        this.h = h;
        this.xCenter = xCenter;
        this.yCenter = yCenter;
        this.xadd = xadd;
        this.yadd = yadd;
    }

    public void setInfo(int type, String name, String image, byte moveType, byte speed, byte height, byte w, byte h, byte xCenter, byte yCenter, byte xadd, byte yadd) {
        this.type = type;
        this.name = name;
        this.moveType = moveType;
        this.speed = speed;
        this.height = height;
        this.w = w;
        this.h = h;
        this.xCenter = xCenter;
        this.yCenter = yCenter;
        this.xadd = xadd;
        this.yadd = yadd;
    }

    public void paint(Graphics g, int x, int y, int flip, int anchor, int f) {
        if (this.image != null) {
            try {
                g.drawRegion(this.image, 0, f * this.h, this.w, this.h, flip, x - this.xCenter, y - this.yCenter, anchor);
            } catch (Exception e) {
            }
        }
        this.timePaint = System.currentTimeMillis();
    }

    public void paint(Graphics g) {
        if (this.image != null) {
            g.drawImage(this.image, 10, 10, 0);
        }
    }

    public void resetImage() {
        if (System.currentTimeMillis() - this.timePaint > 60000) {
            this.image = null;
            this.imageObj = null;
            this.dataMonsterNew.removeAllElements();
            ALL_DATE_NEW_MONSTER.remove(new StringBuffer().append((int) this.spalate).toString());
        }
    }

    public Vector getDataMonster() {
        DataInputStream dis;
        try {
            if (this.type > -1) {
                Vector temp = (Vector) ALL_DATE_NEW_MONSTER.get(new StringBuffer().append((int) this.spalate).toString());
                if (temp != null) {
                    this.dataMonsterNew = temp;
                } else {
                    if (this.arrayDataMonsterNew != null) {
                        dis = new DataInputStream(new ByteArrayInputStream(this.arrayDataMonsterNew));
                    } else {
                        InputStream is = "".getClass().getResourceAsStream(new StringBuffer("/boss/").append((int) this.spalate).toString());
                        dis = new DataInputStream(is);
                    }
                    if (dis != null) {
                        byte len = dis.readByte();
                        for (int i = 0; i < len; i++) {
                            byte[] data = new byte[dis.readShort()];
                            dis.read(data, 0, data.length);
                            this.dataMonsterNew.addElement(new DataEffect(data));
                        }
                        ALL_DATE_NEW_MONSTER.put(new StringBuffer().append((int) this.spalate).toString(), this.dataMonsterNew);
                        dis.close();
                    }
                }
            }
            this.timePaint = System.currentTimeMillis();
        } catch (Exception e) {
        }
        return this.dataMonsterNew;
    }

    public void loadImage() {
        // pokemon: template da la Pokemon (-91) hoac do vat PMD thi khong xin anh quai cu (-47)
        if (laPokemon(this.type) || VatThePokemon.laVatThe(this.type)) {
            return;
        }
        if (!this.isLoad && this.type > -1) {
            GameService.gI().doRequestImageMonster(this.type, this.palate, this.spalate);
            this.isLoad = true;
        }
    }

    public void onImage(boolean isSave, byte[] arr) {
        try {
            if ((this.palate >= 70 && this.palate <= 88) || this.palate == 90 || this.palate == 91 || this.palate == 92 || this.isNewMonster == 1) {
                this.imageObj = new ImageObj(arr);
            } else {
                FilePack.initByArray(arr);
                if (this.palate == 39 || this.palate == 40) {
                    this.imageObj = new ImageObj(this.palate, this.spalate);
                } else {
                    byte[] header = FilePack.instance.loadFile(new StringBuffer(String.valueOf((int) this.spalate)).append("_h").toString());
                    byte[] data = FilePack.instance.loadFile("data");
                    this.image = Res.createImgByHeader(header, data);
                }
                FilePack.reset();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.isLoad = false;
    }

    public Image getImage(int index) {
        if (this.imageObj == null) {
            return null;
        }
        return this.imageObj.getImage(index);
    }

    public void doSetDataMonsterNew(byte[] array) {
        try {
            this.arrayDataMonsterNew = array;
            Vector temp = (Vector) ALL_DATE_NEW_MONSTER.get(new StringBuffer().append((int) this.spalate).toString());
            if (temp != null) {
                this.dataMonsterNew = temp;
                return;
            }
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(array));
            byte len = dis.readByte();
            for (int i = 0; i < len; i++) {
                byte[] data = new byte[dis.readShort()];
                dis.read(data, 0, data.length);
                this.dataMonsterNew.addElement(new DataEffect(data));
            }
            ALL_DATE_NEW_MONSTER.put(new StringBuffer().append((int) this.spalate).toString(), this.dataMonsterNew);
            dis.close();
        } catch (Exception e) {
        }
    }
}
