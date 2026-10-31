package game.render.screen;

import game.model.Cout;
import game.model.MainChar;
import game.model.QuickSlot;
import game.render.Canvas;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.microedition.rms.RecordStore;

public class RMS {
    /*
     * Ten RecordStore da nam tren may nguoi choi tu ban phat hanh dau (dat theo game goc). GIU NGUYEN chuoi:
     * doi ten = mat mat khau da nho / o nhanh / danh sach may chu / anh da tai cua moi nguoi choi cu.
     * Danh sach ngoai le: du_lieu/ten_cu_cho_phep.csv (server/test/KiemTenCu.java).
     */
    /** nho tai khoan / mat khau (LoginScr) */
    public static final String KHO_DANG_NHAP = "nqshlogin"; // ten kho RMS cu tren may nguoi choi - giu de khong mat du lieu
    /** danh sach may chu da chon (ServerListScr, TaiDanhSach) */
    public static final String KHO_DS_MAY_CHU = "ipnqsh"; // ten kho RMS cu tren may nguoi choi - giu de khong mat du lieu
    /** danh sach ip tai ve (saveRMSStringIp) */
    public static final String KHO_IP = "nqshIP"; // ten kho RMS cu tren may nguoi choi - giu de khong mat du lieu
    /** thanh o nhanh (OChieuNhanh) */
    public static final String KHO_O_NHANH = "nqshQuickSlot"; // ten kho RMS cu tren may nguoi choi - giu de khong mat du lieu
    /** anh binh thuoc / ngua da dung (GameData.loadImgPotion) */
    public static final String KHO_ANH_THUOC = "nqshImgPotionNew"; // ten kho RMS cu tren may nguoi choi - giu de khong mat du lieu
    /** kho anh thuoc doi cu hon nua: chi de XOA khi mo game (GameData.loadImgPotion) */
    public static final String KHO_ANH_THUOC_CU = "nqshImgPotion"; // ten kho RMS cu can xoa tren may nguoi choi

    public static void deleteAllRMS() {
        try {
            String[] str = RecordStore.listRecordStores();
            if (str != null) {
                for (String str2 : str) {
                    RecordStore.deleteRecordStore(str2);
                }
            }
        } catch (Exception e) {
        }
    }

    public static void DeleteRMS(String name) {
        try {
            String[] str = RecordStore.listRecordStores();
            if (str != null) {
                for (int i = 0; i < str.length; i++) {
                    if (str[i].equals(name)) {
                        RecordStore.deleteRecordStore(str[i]);
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    public static byte[] loadRMS(String filename) {
        try {
            RecordStore rec = RecordStore.openRecordStore(filename, false);
            byte[] data = rec.getRecord(1);
            rec.closeRecordStore();
            return data;
        } catch (Exception e) {
            return null;
        }
    }

    public static void saveRMSString(String filename, String s) {
        try {
            saveRMS(filename, s.getBytes("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveRMS(String filename, byte[] data) throws Exception {
        RecordStore rec = RecordStore.openRecordStore(filename, true);
        if (rec.getNumRecords() > 0) {
            rec.setRecord(1, data, 0, data.length);
        } else {
            rec.addRecord(data, 0, data.length);
        }
        rec.closeRecordStore();
    }

    public static void saveRMSStringIp(String[] ip, short[] port, String[] name) {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bo);
        try {
            dos.writeByte(ip.length);
            for (int i = 0; i < ip.length; i++) {
                dos.writeUTF(ip[i]);
                dos.writeShort(port[i]);
                dos.writeUTF(name[i]);
            }
            saveRMS(KHO_IP, bo.toByteArray());
            dos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String loadRMSString(String filename) {
        byte[] data = loadRMS(filename);
        if (data == null) {
            return null;
        }
        try {
            String s = new String(data, "UTF-8");
            return s;
        } catch (Exception e) {
            return new String(data);
        }
    }

    public static int loadRMSInt(String file) {
        byte[] b = loadRMS(file);
        if (b == null) {
            return -1;
        }
        return b[0];
    }

    public static void saveRMSInt(String file, int x) {
        try {
            saveRMS(file, new byte[]{(byte) x});
        } catch (Exception e) {
        }
    }

    /** Lưu ô nhanh vào máy + gửi máy chủ (TrangChieu, client 0.9.8). */
    public static void saveQuickSlot() {
        luuQuickSlotMay();
        game.pokemon.TrangChieu.gui();
    }

    /** Chỉ lưu vào máy (RMS) - dùng khi áp dữ liệu máy chủ. */
    public static void luuQuickSlotMay() {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bo);
        try {
            dos.writeUTF(Canvas.gameScr.mainChar.name);
            for (int i = 0; i < MainChar.quickSlot.length; i++) {
                for (int j = 0; j < MainChar.quickSlot[i].length; j++) {
                    QuickSlot qs = MainChar.quickSlot[i][j];
                    dos.writeByte(qs.quickslotType);
                    if (qs.quickslotType == 2) {
                        dos.writeByte(qs.getPotionType());
                    } else {
                        dos.writeByte(qs.getSkillType());
                    }
                    dos.writeBoolean(qs.isBuff);
                    dos.writeByte(GameScr.indexTabSlot);
                }
            }
            byte[] bData = bo.toByteArray();
            saveRMS(KHO_O_NHANH, bData);
            dos.close();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public static void loadQuickSlot() {
        byte[] bData = loadRMS(KHO_O_NHANH);
        if (bData == null) {
            return;
        }
        ByteArrayInputStream bi = new ByteArrayInputStream(bData);
        DataInputStream dis = new DataInputStream(bi);
        try {
            String name = dis.readUTF();
            if (!name.equals(Canvas.gameScr.mainChar.name)) {
                return;
            }
            for (int i = 0; i < MainChar.quickSlot.length; i++) {
                for (int j = 0; j < MainChar.quickSlot[i].length; j++) {
                    QuickSlot qs = MainChar.quickSlot[i][j];
                    qs.quickslotType = dis.readUnsignedByte();
                    byte map = dis.readByte();
                    qs.isBuff = dis.readBoolean();
                    if (qs.quickslotType == 2) {
                        if (game.pokemon.MoTaVatPham.ganONhanh(map & 255)) {
                            qs.setIsPotion(map);
                        } else {
                            qs.setIsNothing();   // 0.9.14: chỉ bình HP / PP ở ô nhanh
                        }
                    } else if (qs.quickslotType == 1) {
                        qs.setIsSkill(map, Canvas.gameScr.mainChar.clazz, qs.isBuff);
                    } else {
                        qs.setIsNothing();   // pokemon: ô trống (lưu -1 = 255) giữ trống, không thành chiêu 0
                    }
                    GameScr.indexTabSlot = Math.max(0, Math.min(game.pokemon.TrangChieu.so - 1, dis.readByte()));
                }
            }
            dis.close();
        } catch (IOException e) {
            Cout.println("ERROR LOAD QUICKSLOT");
        }
    }
}
