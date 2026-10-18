package game.render.screen;

import game.model.Cout;
import game.model.MainChar;
import game.model.QuickSlot;
import game.render.GCanvas;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class RMS {
    public static void deleteAllRMS() {
        try {
            File[] fileArrListFiles = GCanvas.Context.getCacheDir().listFiles();
            if (fileArrListFiles != null) {
                for (File file : fileArrListFiles) {
                    file.delete();
                }
            }
        } catch (Exception unused) {
        }
    }

    public static void deleteRMS(String str) {
        try {
            GCanvas.Context.getFileStreamPath(str).delete();
        } catch (Exception unused) {
        }
    }

    public static byte[] loadRMS(String str) {
        try {
            FileInputStream fileInputStreamOpenFileInput = GCanvas.gCanvas.openFileInput(str);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] bArr = new byte[1024];
            while (true) {
                int i = fileInputStreamOpenFileInput.read(bArr);
                if (i == -1) {
                    break;
                }
                byteArrayOutputStream.write(bArr, 0, i);
            }
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            if (fileInputStreamOpenFileInput != null) {
                fileInputStreamOpenFileInput.close();
                return byteArray;
            }
        } catch (Exception unused) {
        }
        return null;
    }

    public static void saveRMSString(String str, String str2) {
        try {
            saveRMS(str, str2.getBytes("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveRMS(String str, byte[] bArr) throws Exception {
        try {
            FileOutputStream fileOutputStreamOpenFileOutput = GCanvas.gCanvas.openFileOutput(str, 0);
            fileOutputStreamOpenFileOutput.write(bArr);
            if (fileOutputStreamOpenFileOutput != null) {
                fileOutputStreamOpenFileOutput.close();
            }
        } catch (Exception e) {
            Cout.println("save rms '" + str + "' error! " + e.toString() + " " + bArr.length);
        }
    }

    public static void saveRMSStringIp(String[] strArr, short[] sArr, String[] strArr2) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        try {
            dataOutputStream.writeByte(strArr.length);
            for (int i = 0; i < strArr.length; i++) {
                dataOutputStream.writeUTF(strArr[i]);
                dataOutputStream.writeShort(sArr[i]);
                dataOutputStream.writeUTF(strArr2[i]);
            }
            saveRMS("nqshIP", byteArrayOutputStream.toByteArray());
            dataOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String loadRMSString(String str) {
        byte[] bArrLoadRMS = loadRMS(str);
        if (bArrLoadRMS == null) {
            return null;
        }
        try {
            return new String(bArrLoadRMS, "UTF-8");
        } catch (Exception unused) {
            return new String(bArrLoadRMS);
        }
    }

    public static int loadRMSInt(String str) {
        byte[] bArrLoadRMS = loadRMS(str);
        if (bArrLoadRMS == null) {
            return -1;
        }
        return bArrLoadRMS[0];
    }

    public static void saveRMSInt(String str, int i) {
        try {
            saveRMS(str, new byte[]{(byte) i});
        } catch (Exception unused) {
        }
    }

    public static void saveQuickSlot() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        try {
            dataOutputStream.writeUTF(GCanvas.gameScr.mainChar.name);
            for (int i = 0; i < MainChar.quickSlot.length; i++) {
                for (int i2 = 0; i2 < MainChar.quickSlot[i].length; i2++) {
                    QuickSlot quickSlot = MainChar.quickSlot[i][i2];
                    dataOutputStream.writeByte(quickSlot.quickslotType);
                    if (quickSlot.quickslotType == 2) {
                        dataOutputStream.writeByte(quickSlot.getPotionType());
                    } else {
                        dataOutputStream.writeByte(quickSlot.getSkillType());
                    }
                    dataOutputStream.writeBoolean(quickSlot.isBuff);
                    dataOutputStream.writeByte(GameScr.indexTabSlot);
                }
            }
            saveRMS("nqshQuickSlot", byteArrayOutputStream.toByteArray());
            dataOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadQuickSlot() {
        byte[] bArrLoadRMS = loadRMS("nqshQuickSlot");
        if (bArrLoadRMS == null) {
            return;
        }
        DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(bArrLoadRMS));
        try {
            if (dataInputStream.readUTF().equals(GCanvas.gameScr.mainChar.name)) {
                for (int i = 0; i < MainChar.quickSlot.length; i++) {
                    for (int i2 = 0; i2 < MainChar.quickSlot[i].length; i2++) {
                        QuickSlot quickSlot = MainChar.quickSlot[i][i2];
                        quickSlot.quickslotType = dataInputStream.readUnsignedByte();
                        byte b = dataInputStream.readByte();
                        quickSlot.isBuff = dataInputStream.readBoolean();
                        if (quickSlot.quickslotType == 2) {
                            quickSlot.setIsPotion(b);
                        } else {
                            quickSlot.setIsSkill(b, GCanvas.gameScr.mainChar.clazz, quickSlot.isBuff);
                        }
                        GameScr.indexTabSlot = dataInputStream.readByte();
                    }
                }
                dataInputStream.close();
            }
        } catch (IOException unused) {
            Cout.println("ERROR LOAD QUICKSLOT");
        }
    }
}
