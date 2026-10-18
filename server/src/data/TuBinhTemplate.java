/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package data;

import java.io.IOException;
import java.util.Vector;
import real.Char;
import real.MessageCreator;

public class TuBinhTemplate {
    public byte id;
    public byte nmanh = 5;
    public byte[] manh = new byte[4];
    public String name;
    public short attack;
    public short def;
    public short crit;
    public short dog;
    public short hp;
    public short mp;
    public short level;
    public static Vector<TuBinhTemplate> ALL_TU_BINH = new Vector<>();
    public static byte MAX_CO_VAT = 4;

    public TuBinhTemplate getTemplate() {
        return ALL_TU_BINH.get(this.id);
    }

    public String getInfoSave() {
        return this.manh[0] + "," + this.manh[1] + "," + this.manh[2] + "," + this.manh[3];
    }

    public boolean isFullManh() {
        return this.manh[0] == this.getTemplate().nmanh
            && this.manh[1] == this.getTemplate().nmanh
            && this.manh[2] == this.getTemplate().nmanh
            && this.manh[3] == this.getTemplate().nmanh;
    }

    public String[] getInfo() {
        String[] info = new String[]{"", ""};
        info[0] = "Tấn công: +" + this.getTemplate().attack;
        info[0] = info[0] + "|Phòng thủ: +" + this.getTemplate().def;
        info[0] = info[0] + "|Chí mạng: +" + this.getTemplate().crit;
        info[0] = info[0] + "|Né tránh: +" + this.getTemplate().dog;
        info[0] = info[0] + "|HP: +" + this.getTemplate().hp;
        info[0] = info[0] + "|MP: +" + this.getTemplate().mp;
        info[1] = "Mảnh 1: " + this.manh[0] + "/" + this.getTemplate().nmanh;
        info[1] = info[1] + "|Mảnh 2: " + this.manh[1] + "/" + this.getTemplate().nmanh;
        info[1] = info[1] + "|Mảnh 3: " + this.manh[2] + "/" + this.getTemplate().nmanh;
        info[1] = info[1] + "|Mảnh 4: " + this.manh[3] + "/" + this.getTemplate().nmanh;
        return info;
    }

    public int getIdImage() {
        return this.id;
    }

    public boolean addManh(int idManh, Char p, int nManh) {
        if (this.manh[idManh] < this.getTemplate().nmanh) {
            this.manh[idManh] = (byte)(this.manh[idManh] + nManh);
            if (this.manh[idManh] > this.getTemplate().nmanh) {
                this.manh[idManh] = this.getTemplate().nmanh;
            }

            return true;
        } else {
            return false;
        }
    }

    public boolean addManh(int idManh, Char p) {
        if (this.manh[idManh] < this.getTemplate().nmanh) {
            this.manh[idManh]++;
            p.sendMessage(
                MessageCreator.createServerAlertMessage(
                    "Chúc mừng bạn đã ghép được " + this.manh[idManh] + "/" + this.getTemplate().nmanh + " mảnh " + this.getTemplate().name + " " + (idManh + 1), ""
                )
            );
            if (this.isFullManh()) {
                p.calculateAttrib();

                try {
                    p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
                } catch (IOException var4) {
                }
            }

            return true;
        } else {
            return false;
        }
    }

    public int getAttack() {
        return this.isFullManh() ? this.getTemplate().attack : 0;
    }

    public int getDef() {
        return this.isFullManh() ? this.getTemplate().def : 0;
    }

    public int getCrit() {
        return this.isFullManh() ? this.getTemplate().crit : 0;
    }

    public int getDog() {
        return this.isFullManh() ? this.getTemplate().dog : 0;
    }

    public int getHP() {
        return this.isFullManh() ? this.getTemplate().hp : 0;
    }

    public int getMP() {
        return this.isFullManh() ? this.getTemplate().mp : 0;
    }
}
