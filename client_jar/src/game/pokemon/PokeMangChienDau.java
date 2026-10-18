package game.pokemon;

import game.model.Actor;
import game.model.LiveActor;
import game.model.Monster;
import game.render.Canvas;
import game.render.screen.GameScr;
import java.io.DataInputStream;

/**
 * Goi chien dau Pokemon -95..-99 (HTBB docs/pokemon/THIET_KE_LAU_DAI.md muc 4.6, giai doan 2 cua
 * server). Moi goi la goi KEM (Message.kemTheo) di sau goi cu: server chua gui thi client van chay
 * nhu cu (hieu ung tu doan), server gui thi hieu ung theo dung so lieu server.
 * Goi tu PokeMang.xuLy (dau MessageHandler.onMessage). -95..-99 da grep: chua lenh nao dung.
 */
public final class PokeMangChienDau {
    public static final int POKE_DON = -95;
    public static final int POKE_CANH_BAO = -96;
    public static final int POKE_TRANG_THAI = -97;
    public static final int POKE_BOSS = -98;
    public static final int POKE_DONG_GOP = -99;

    private PokeMangChienDau() {
    }

    static Actor tim(short id, int loai) {
        GameScr gs = Canvas.gameScr;
        if (gs == null) {
            return null;
        }
        if (loai == 0 && gs.mainChar != null && gs.mainChar.ID == id) {
            return gs.mainChar;
        }
        return gs.findActorByID_Cat(id, (byte) (loai == 0 ? 0 : 1));
    }

    /** true = la lenh -95..-99 (da xu ly, ke ca goi hong). */
    public static boolean xuLy(int lenh, DataInputStream r) {
        if (lenh > POKE_DON || lenh < POKE_DONG_GOP) {
            return false;
        }
        try {
            switch (lenh) {
                case POKE_DON: {
                    r.readShort();                       // nguon
                    r.readUnsignedByte();                // loaiNguon
                    short dich = r.readShort();
                    int loaiDich = r.readUnsignedByte();
                    int dam = r.readInt();
                    int co = r.readUnsignedByte();
                    int he = r.readUnsignedByte();
                    int tt = r.readUnsignedByte();
                    Actor a = tim(dich, loaiDich);
                    if (a == null) {
                        break;
                    }
                    SoBay.donMayChu(a.x, a.y - 30, co, dam);
                    if (a instanceof LiveActor && (co & 2) == 0) {
                        LiveActor la = (LiveActor) a;
                        int manh = (co & 5) != 0 ? 1 : 0;
                        if (HieuUngDanh.tickTu(la.pokeTrungLuc) > 2) {
                            HieuUngDanh.trung(la, he, manh);
                        } else if (manh > 0) {
                            HieuUngDanh.noHe(he, la.x, la.y - 16, 1);
                            HieuUngDanh.rung(5, 2);
                        }
                        if ((co & 64) != 0 && tt > 0 && tt <= 5 && (la.pokeTT == 0 || System.currentTimeMillis() > la.pokeTTHet)) {
                            la.pokeTT = tt;
                            la.pokeTTHet = System.currentTimeMillis() + 3000L;
                            la.pokeTTChong = 1;
                        }
                    }
                    break;
                }
                case POKE_CANH_BAO: {
                    r.readShort();                       // boss
                    r.readUnsignedShort();               // idChieu
                    int he = r.readUnsignedByte();
                    int hinh = r.readUnsignedByte();
                    int x = r.readShort();
                    int y = r.readShort();
                    int rr = r.readShort();
                    int rong = r.readShort();
                    int huong = r.readShort();
                    int ms = r.readUnsignedShort();
                    HieuUngDanh.vungCanhBao(x, y, hinh, rr, rong, huong, he, ms);
                    break;
                }
                case POKE_TRANG_THAI: {
                    short id = r.readShort();
                    int loai = r.readUnsignedByte();
                    int tt = r.readUnsignedByte();
                    int chong = r.readUnsignedByte();
                    int ms = r.readUnsignedShort();
                    Actor a = tim(id, loai);
                    if (a instanceof LiveActor) {
                        LiveActor la = (LiveActor) a;
                        if (ms == 0 || tt == 0 || tt > 5) {
                            la.pokeTT = 0;
                        } else {
                            la.pokeTT = tt;
                            la.pokeTTChong = chong;
                            la.pokeTTHet = System.currentTimeMillis() + ms;
                        }
                    }
                    break;
                }
                case POKE_BOSS: {
                    short id = r.readShort();
                    int pha = r.readUnsignedByte();
                    int suKien = r.readUnsignedByte();
                    int hp = r.readInt();
                    int giay = r.readUnsignedShort();
                    Actor a = tim(id, 1);
                    Monster m = a instanceof Monster ? (Monster) a : null;
                    if (m != null && hp >= 0) {
                        m.setRealHP(hp);
                    }
                    HieuUngDanh.bossPha(m, id, pha, suKien, giay);
                    break;
                }
                case POKE_DONG_GOP: {
                    int n = r.readUnsignedByte();
                    short[] id = new short[n];
                    int[] phan = new int[n];
                    for (int i = 0; i < n; i++) {
                        id[i] = r.readShort();
                        phan[i] = r.readUnsignedShort();
                    }
                    int cuaToi = r.readUnsignedShort();
                    HieuUngDanh.dongGop(id, phan, n, cuaToi);
                    break;
                }
                default:
                    return false;
            }
        } catch (Throwable e) {
            // goi hong / thieu truong: bo qua
        }
        return true;
    }
}
