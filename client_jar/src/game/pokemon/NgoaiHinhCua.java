package game.pokemon;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Ngoai hinh trang bi cua MOT nhan vat (lenh -102 POKE_NGOAI_HINH, server com.htbb.pokemon.NgoaiHinh):
 * phu kien tung o (kieu, do hiem, bac, +N) + bac tot nhat, +N cao nhat, so mon cung bac (bo), trang phuc, mat
 * na an. Ve bang NgoaiHinhVe. HTBB docs/thiet-ke-game.md.
 */
public final class NgoaiHinhCua {
    public static final int MU = 0, KHAN = 1, VONG = 2, TAY = 3, LUNG = 4, DA = 5, KINH = 6, SO_O = 7;
    public static final int AN_TRANG_PHUC = 0x80;

    public final int[] kieu = new int[SO_O];
    public final int[] hiem = new int[SO_O];
    public final int[] bac = new int[SO_O];
    public final int[] cong = new int[SO_O];
    /** -1 = khong mac gi. */
    public int bacTot = -1;
    public int congTot;
    public int bo;
    public int trangPhuc;
    public int an;
    /** 0.9.8 trang tri (duoi goi -102, TrangPhucVe): so trang phuc / hao quang / skin thu cuoi / the ban dong hanh (0 = khong). */
    public int tpSo, haoQuang, skinThu, theBan;

    /** Doc phan sau charId cua goi -102 (phien ban 1; phien ban moi hon chi them truong o cuoi). */
    public static NgoaiHinhCua doc(DataInputStream r) throws IOException {
        NgoaiHinhCua n = new NgoaiHinhCua();
        r.readUnsignedByte();   // phien ban
        int co = r.readUnsignedByte();
        for (int o = 0; o < SO_O; o++) {
            if ((co & (1 << o)) == 0) {
                continue;
            }
            n.kieu[o] = r.readUnsignedByte();
            int hb = r.readUnsignedByte();
            n.hiem[o] = (hb >> 4) & 0x0F;
            n.bac[o] = hb & 0x0F;
            n.cong[o] = r.readUnsignedByte();
        }
        int b = r.readUnsignedByte();
        n.bacTot = b == 255 ? -1 : b;
        n.congTot = r.readUnsignedByte();
        n.bo = r.readUnsignedByte();
        n.trangPhuc = r.readUnsignedByte();
        n.an = r.readUnsignedByte();
        return n;
    }

    /** Duoi goi -102 (0.9.8): u8 'T', u8 phien ban, u8 trang phuc, u8 hao quang, u8 skin thu, u8 the. Khong co: bo qua. */
    public void docDuoi(DataInputStream r) {
        try {
            if (r.available() < 6 || r.readUnsignedByte() != 0x54) {
                return;
            }
            r.readUnsignedByte();
            tpSo = r.readUnsignedByte();
            haoQuang = r.readUnsignedByte();
            skinThu = r.readUnsignedByte();
            theBan = r.readUnsignedByte();
        } catch (Throwable e) {
            // server cu: khong co duoi
        }
    }

    /** Ban sao (thu trang phuc o tu do: doi tren ban sao, khong dung ban that). */
    public NgoaiHinhCua sao() {
        NgoaiHinhCua n = new NgoaiHinhCua();
        System.arraycopy(kieu, 0, n.kieu, 0, SO_O);
        System.arraycopy(hiem, 0, n.hiem, 0, SO_O);
        System.arraycopy(bac, 0, n.bac, 0, SO_O);
        System.arraycopy(cong, 0, n.cong, 0, SO_O);
        n.bacTot = bacTot;
        n.congTot = congTot;
        n.bo = bo;
        n.trangPhuc = trangPhuc;
        n.an = an;
        n.tpSo = tpSo;
        n.haoQuang = haoQuang;
        n.skinThu = skinThu;
        n.theBan = theBan;
        return n;
    }

    public boolean coGi() {
        if (bacTot >= 1 || congTot >= 7 || trangPhuc > 0 || tpSo > 0 || haoQuang > 0) {
            return true;
        }
        for (int o = 0; o < SO_O; o++) {
            if (kieu[o] > 0) {
                return true;
            }
        }
        return false;
    }
}
