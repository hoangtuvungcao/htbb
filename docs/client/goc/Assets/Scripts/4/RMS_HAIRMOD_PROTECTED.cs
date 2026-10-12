// DP, Version=1.0.0.0, Culture=neutral, PublicKeyToken=null
// RMS_HAIRMOD_PROTECTED
using System;
using System.IO;
using System.Threading;
using UnityEngine;

public class RMS_HAIRMOD_PROTECTED
{
    private const int INTERVAL = 5;

    private const int MAXTIME = 500;

    public static int status;

    public static sbyte[] data;

    public static string filename;

    public static void saveRMS(string filename, sbyte[] data)
    {
        if (Thread.CurrentThread.Name == Main_HAIRMOD_PROTECTED.mainThreadName)
        {
            __saveRMS(filename, data);
        }
        else
        {
            _saveRMS(filename, data);
        }
    }

    public static sbyte[] loadRMS(string filename)
    {
        if (Thread.CurrentThread.Name == Main_HAIRMOD_PROTECTED.mainThreadName)
        {
            return __loadRMS(filename);
        }
        return _loadRMS(filename);
    }

    private static void _saveRMS(string filename, sbyte[] data)
    {
        if (status != 0)
        {
            Debug.LogError("Cannot save RMS " + filename + " because current is saving " + RMS_HAIRMOD_PROTECTED.filename);
            return;
        }
        RMS_HAIRMOD_PROTECTED.filename = filename;
        RMS_HAIRMOD_PROTECTED.data = data;
        status = 2;
        int i;
        for (i = 0; i < 500; i++)
        {
            Thread.Sleep(5);
            if (status == 0)
            {
                break;
            }
        }
        if (i == 500)
        {
            Debug.LogError("TOO LONG TO SAVE RMS " + filename);
        }
    }

    public static void saveRMSInt(string file, int x)
    {
        try
        {
            saveRMS(file, new sbyte[1] { (sbyte)x });
        }
        catch (Exception)
        {
        }
    }

    public static string loadRMSString(string filename)
    {
        sbyte[] array = loadRMS(filename);
        if (array == null)
        {
            return null;
        }
        DataInputStream_HAIRMOD_PROTECTED dataInputStream_HAIRMOD_PROTECTED = new DataInputStream_HAIRMOD_PROTECTED(array);
        try
        {
            string result = dataInputStream_HAIRMOD_PROTECTED.readUTF();
            dataInputStream_HAIRMOD_PROTECTED.close();
            return result;
        }
        catch (Exception)
        {
        }
        return null;
    }

    public static byte[] convertSbyteToByte(sbyte[] var)
    {
        byte[] array = new byte[var.Length];
        for (int i = 0; i < var.Length; i++)
        {
            if (var[i] > 0)
            {
                array[i] = (byte)var[i];
            }
            else
            {
                array[i] = (byte)(var[i] + 256);
            }
        }
        return array;
    }

    private static sbyte[] _loadRMS(string filename)
    {
        if (status != 0)
        {
            Debug.LogError("Cannot load RMS " + filename + " because current is loading " + RMS_HAIRMOD_PROTECTED.filename);
            return null;
        }
        RMS_HAIRMOD_PROTECTED.filename = filename;
        data = null;
        status = 3;
        int i;
        for (i = 0; i < 500; i++)
        {
            Thread.Sleep(5);
            if (status == 0)
            {
                break;
            }
        }
        if (i == 500)
        {
            Debug.LogError("TOO LONG TO LOAD RMS " + filename);
        }
        return data;
    }

    public static void update()
    {
        if (status == 2)
        {
            status = 1;
            __saveRMS(filename, data);
            status = 0;
        }
        else if (status == 3)
        {
            status = 1;
            data = __loadRMS(filename);
            status = 0;
        }
    }

    public static void saveRMSStringIp(string[] ip, short[] port, string[] name)
    {
        DataOutputStream_HAIRMOD_PROTECTED dataOutputStream_HAIRMOD_PROTECTED = new DataOutputStream_HAIRMOD_PROTECTED();
        try
        {
            dataOutputStream_HAIRMOD_PROTECTED.writeByte((sbyte)ip.Length);
            for (int i = 0; i < ip.Length; i++)
            {
                dataOutputStream_HAIRMOD_PROTECTED.writeUTF(ip[i]);
                dataOutputStream_HAIRMOD_PROTECTED.writeShort(port[i]);
                dataOutputStream_HAIRMOD_PROTECTED.writeUTF(name[i]);
            }
            saveRMS("nqshIP", dataOutputStream_HAIRMOD_PROTECTED.toByteArray());
            dataOutputStream_HAIRMOD_PROTECTED.close();
        }
        catch (Exception)
        {
        }
    }

    public static void saveNumberSuport(string number)
    {
        DataOutputStream_HAIRMOD_PROTECTED dataOutputStream_HAIRMOD_PROTECTED = new DataOutputStream_HAIRMOD_PROTECTED();
        try
        {
            dataOutputStream_HAIRMOD_PROTECTED.writeUTF(number);
            sbyte[] array = dataOutputStream_HAIRMOD_PROTECTED.toByteArray();
            saveRMS("numbersupport", array);
            dataOutputStream_HAIRMOD_PROTECTED.close();
        }
        catch (Exception)
        {
        }
    }

    public static void saveQuickSlot()
    {
        DataOutputStream_HAIRMOD_PROTECTED dataOutputStream_HAIRMOD_PROTECTED = new DataOutputStream_HAIRMOD_PROTECTED();
        try
        {
            dataOutputStream_HAIRMOD_PROTECTED.writeUTF(Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.name);
            for (int i = 0; i < MainChar_HAIRMOD_PROTECTED.quickSlot.Length; i++)
            {
                for (int j = 0; j < MainChar_HAIRMOD_PROTECTED.quickSlot[i].Length; j++)
                {
                    QuickSlot_HAIRMOD_PROTECTED quickSlot_HAIRMOD_PROTECTED = MainChar_HAIRMOD_PROTECTED.quickSlot[i][j];
                    dataOutputStream_HAIRMOD_PROTECTED.writeByte((sbyte)quickSlot_HAIRMOD_PROTECTED.quickslotType);
                    if (quickSlot_HAIRMOD_PROTECTED.quickslotType == QuickSlot_HAIRMOD_PROTECTED.TYPE_POTION)
                    {
                        dataOutputStream_HAIRMOD_PROTECTED.writeByte((sbyte)quickSlot_HAIRMOD_PROTECTED.getPotionType());
                    }
                    else
                    {
                        dataOutputStream_HAIRMOD_PROTECTED.writeByte(quickSlot_HAIRMOD_PROTECTED.getSkillType());
                    }
                    dataOutputStream_HAIRMOD_PROTECTED.writeBoolean(quickSlot_HAIRMOD_PROTECTED.isBuff);
                    dataOutputStream_HAIRMOD_PROTECTED.writeByte((sbyte)GameScr_HAIRMOD_PROTECTED.indexTabSlot);
                }
            }
            sbyte[] array = dataOutputStream_HAIRMOD_PROTECTED.toByteArray();
            saveRMS("nqshQuickSlot", array);
            dataOutputStream_HAIRMOD_PROTECTED.close();
        }
        catch (Exception)
        {
        }
    }

    public static void loadQuickSlot()
    {
        sbyte[] array = loadRMS("nqshQuickSlot");
        if (array == null)
        {
            return;
        }
        DataInputStream_HAIRMOD_PROTECTED dataInputStream_HAIRMOD_PROTECTED = new DataInputStream_HAIRMOD_PROTECTED(array);
        try
        {
            string text = dataInputStream_HAIRMOD_PROTECTED.readUTF();
            if (!text.Equals(Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.name))
            {
                return;
            }
            for (int i = 0; i < MainChar_HAIRMOD_PROTECTED.quickSlot.Length; i++)
            {
                for (int j = 0; j < MainChar_HAIRMOD_PROTECTED.quickSlot[i].Length; j++)
                {
                    QuickSlot_HAIRMOD_PROTECTED quickSlot_HAIRMOD_PROTECTED = MainChar_HAIRMOD_PROTECTED.quickSlot[i][j];
                    quickSlot_HAIRMOD_PROTECTED.quickslotType = dataInputStream_HAIRMOD_PROTECTED.readUnsignedByte();
                    sbyte b = dataInputStream_HAIRMOD_PROTECTED.readByte();
                    quickSlot_HAIRMOD_PROTECTED.isBuff = dataInputStream_HAIRMOD_PROTECTED.readBoolean();
                    if (quickSlot_HAIRMOD_PROTECTED.quickslotType == QuickSlot_HAIRMOD_PROTECTED.TYPE_POTION)
                    {
                        quickSlot_HAIRMOD_PROTECTED.setIsPotion(b);
                    }
                    else
                    {
                        quickSlot_HAIRMOD_PROTECTED.setIsSkill(b, Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.clazz, quickSlot_HAIRMOD_PROTECTED.isBuff);
                    }
                    GameScr_HAIRMOD_PROTECTED.indexTabSlot = dataInputStream_HAIRMOD_PROTECTED.readByte();
                }
            }
            dataInputStream_HAIRMOD_PROTECTED.close();
        }
        catch (IOException)
        {
            Out_HAIRMOD_PROTECTED.println("ERROR LOAD QUICKSLOT");
        }
    }

    private static void __saveRMS(string filename, sbyte[] data)
    {
        string value = ByteArrayToString(ArrayCast_HAIRMOD_PROTECTED.cast(data));
        PlayerPrefs.SetString(filename, value);
    }

    private static sbyte[] __loadRMS(string filename)
    {
        string @string = PlayerPrefs.GetString(filename);
        byte[] array;
        try
        {
            array = StringToByteArray(@string);
        }
        catch (Exception ex)
        {
            Debug.LogError("PARSE RMS STRING FAIL " + ex.StackTrace);
            return null;
        }
        if (array.Length == 0)
        {
            return null;
        }
        return ArrayCast_HAIRMOD_PROTECTED.cast(array);
    }

    public static void clearRecord(string name)
    {
        PlayerPrefs.DeleteKey(name);
    }

    public static void deleteAll()
    {
        Debug.LogWarning("ALL RMS CLEAR");
        PlayerPrefs.DeleteAll();
    }

    public static string ByteArrayToString(byte[] ba)
    {
        string text = BitConverter.ToString(ba);
        return text.Replace("-", string.Empty);
    }

    public static byte[] StringToByteArray(string hex)
    {
        int length = hex.Length;
        byte[] array = new byte[length / 2];
        for (int i = 0; i < length; i += 2)
        {
            array[i / 2] = Convert.ToByte(hex.Substring(i, 2), 16);
        }
        return array;
    }

    public static void saveRMSString(string filename, string data)
    {
        DataOutputStream_HAIRMOD_PROTECTED dataOutputStream_HAIRMOD_PROTECTED = new DataOutputStream_HAIRMOD_PROTECTED();
        try
        {
            dataOutputStream_HAIRMOD_PROTECTED.writeUTF(data);
            saveRMS(filename, dataOutputStream_HAIRMOD_PROTECTED.toByteArray());
            dataOutputStream_HAIRMOD_PROTECTED.close();
        }
        catch (Exception)
        {
        }
    }
}
