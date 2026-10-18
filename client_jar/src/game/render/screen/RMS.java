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
            saveRMS("nqshIP", bo.toByteArray());
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

    public static void saveQuickSlot() {
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
            saveRMS("nqshQuickSlot", bData);
            dos.close();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public static void loadQuickSlot() {
        byte[] bData = loadRMS("nqshQuickSlot");
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
                        qs.setIsPotion(map);
                    } else {
                        qs.setIsSkill(map, Canvas.gameScr.mainChar.clazz, qs.isBuff);
                    }
                    GameScr.indexTabSlot = dis.readByte();
                }
            }
            dis.close();
        } catch (IOException e) {
            Cout.println("ERROR LOAD QUICKSLOT");
        }
    }
}
