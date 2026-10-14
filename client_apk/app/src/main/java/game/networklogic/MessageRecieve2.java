package game.networklogic;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import game.model.Actor;
import game.model.Char;
import game.model.Cout;
import game.model.DynamicObj;
import game.model.EffectData;
import game.model.EffectManager;
import game.model.EffectServerManager;
import game.model.Ground;
import game.model.IAction;
import game.model.ImageIcon;
import game.model.IsAnimal;
import game.model.MainChar;
import game.model.Mineral;
import game.model.NPC;
import game.model.Position;
import game.model.SkillClan;
import game.model.TreeInfo;
import game.model.WayPoint;
import game.render.GCanvas;
import game.render.Res;
import game.render.screen.CreateCharScr;
import game.render.screen.GameData;
import game.render.screen.GameScr;
import game.render.screen.HelpScr;
import game.render.screen.MenuWindows;
import game.render.screen.MoQuaScr;
import game.render.screen.TroChoiOChu;
import game.render.screen.ViewInfoScr;
import game.render.screen.WindowInfoScr;
import java.lang.reflect.Array;
import java.util.Vector;
import network.Message;

public class MessageRecieve2 extends Cmd_message {
    public static void Message2(Message message) {
        try {
            byte b = message.command;
            int i = 0;
            if (b == -43) {
                int i2 = message.reader().readByte();
                if (i2 > 0) {
                    Bitmap[] bitmapArr = new Bitmap[i2];
                    for (int i3 = 0; i3 < i2; i3++) {
                        try {
                            int i4 = message.reader().readInt();
                            byte[] bArr = new byte[i4];
                            message.reader().read(bArr, 0, i4);
                            bitmapArr[i3] = BitmapFactory.decodeByteArray(bArr, 0, i4);
                        } catch (Exception unused) {
                        }
                    }
                    byte b2 = message.reader().readByte();
                    while (i < b2) {
                        DynamicObj dynamicObj = new DynamicObj();
                        byte b3 = message.reader().readByte();
                        short s = message.reader().readByte();
                        short s2 = message.reader().readByte();
                        dynamicObj.w = message.reader().readByte();
                        dynamicObj.h = message.reader().readByte();
                        dynamicObj.nFrame = message.reader().readByte();
                        dynamicObj.setPosTo((short) (s * 16), (short) (s2 * 16));
                        dynamicObj.img = bitmapArr[b3];
                        dynamicObj.height = (short) (dynamicObj.img.getHeight() / dynamicObj.nFrame);
                        GCanvas.gameScr.onDynamicObj(dynamicObj);
                        i++;
                    }
                    return;
                }
                return;
            }
            if (b != -42) {
                if (b != -24) {
                    if (b == 62) {
                        GCanvas.gameScr.onItemShopOnline(message);
                        return;
                    }
                    if (b == 87) {
                        GCanvas.gameScr.onLuckyDraw(message);
                        return;
                    }
                    if (b == 105) {
                        int i5 = message.reader().readByte();
                        int i6 = message.reader().readByte();
                        Res.locationMap = new short[i6][][];
                        Res.nameMap = new String[i6][][];
                        for (int i7 = 0; i7 < i6; i7++) {
                            int i8 = message.reader().readByte();
                            Res.locationMap[i7] = new short[i8][];
                            Res.nameMap[i7] = new String[i8][];
                            for (int i9 = 0; i9 < i8; i9++) {
                                int i10 = i5 * 3;
                                Res.locationMap[i7][i9] = new short[i10];
                                Res.nameMap[i7][i9] = new String[i5];
                                for (int i11 = 0; i11 < i10; i11++) {
                                    Res.locationMap[i7][i9][i11] = message.reader().readShort();
                                    if (i11 % 3 == 0) {
                                        Res.nameMap[i7][i9][i11 / 3] = message.reader().readUTF();
                                    }
                                }
                            }
                        }
                        int i12 = message.reader().readByte();
                        GameScr.mapID = new short[i12][];
                        GameScr.idArrMap = new short[i12];
                        for (int i13 = 0; i13 < i12; i13++) {
                            GameScr.mapID[i13] = new short[i5];
                            for (int i14 = 0; i14 < i5; i14++) {
                                GameScr.mapID[i13][i14] = message.reader().readShort();
                            }
                            GameScr.idArrMap[i13] = message.reader().readShort();
                        }
                        byte b4 = message.reader().readByte();
                        int i15 = i5 * 3;
                        Res.localXaphu = (short[][]) Array.newInstance((Class<?>) Short.TYPE, b4, i15);
                        Res.namMap = (String[][]) Array.newInstance((Class<?>) String.class, b4, i5);
                        for (int i16 = 0; i16 < b4; i16++) {
                            for (int i17 = 0; i17 < i15; i17++) {
                                Res.localXaphu[i16][i17] = message.reader().readShort();
                                if (i17 % 3 == 0) {
                                    Res.namMap[i16][i17 / 3] = message.reader().readUTF();
                                }
                            }
                        }
                        return;
                    }
                    if (b != 123) {
                        switch (b) {
                            case -76:
                                GCanvas.gameScr.onShopNew(message);
                                return;
                            case -75:
                                GCanvas.gameScr.onCountDown(message);
                                return;
                            case -74:
                                GCanvas.gameScr.onEffData(message);
                                return;
                            case -73:
                                GCanvas.gameScr.onPetAttack(message);
                                return;
                            default:
                                switch (b) {
                                    case -71:
                                        GCanvas.gameScr.onDynamicEffect(message);
                                        return;
                                    case -70:
                                        GCanvas.gameScr.onNewEffect(message);
                                        return;
                                    case -69:
                                        GCanvas.gameScr.onAutoImbue(message);
                                        return;
                                    case -68:
                                        GCanvas.gameScr.onTachNguyenLieu(message);
                                        return;
                                    case -67:
                                        GCanvas.gameScr.onTuBinh(message);
                                        return;
                                    case -66:
                                        GCanvas.gameScr.onFruit(message);
                                        return;
                                    default:
                                        switch (b) {
                                            case -64:
                                                GCanvas.gameScr.onQuest(message);
                                                return;
                                            case -63:
                                                int i18 = message.reader().readByte();
                                                byte[] bArr2 = new byte[i18];
                                                while (i < i18) {
                                                    bArr2[i] = message.reader().readByte();
                                                    i = (byte) (i + 1);
                                                }
                                                TroChoiOChu.gI().arrNum = bArr2;
                                                TroChoiOChu.gI().switchToMe();
                                                TroChoiOChu.gI().init();
                                                return;
                                            case -62:
                                                byte b5 = message.reader().readByte();
                                                if (b5 == 0) {
                                                    byte b6 = message.reader().readByte();
                                                    int i19 = message.reader().readByte();
                                                    String[] strArr = new String[i19];
                                                    while (i < i19) {
                                                        strArr[i] = message.reader().readUTF();
                                                        i = (byte) (i + 1);
                                                    }
                                                    MoQuaScr.gI().switchToMe();
                                                    MoQuaScr.gI().init();
                                                    MoQuaScr.gI().setInfo(b6, strArr);
                                                    return;
                                                }
                                                if (b5 != 1) {
                                                    if (b5 == 2) {
                                                        MoQuaScr.gI().finishGame();
                                                        return;
                                                    }
                                                    return;
                                                }
                                                MoQuaScr.gI().selected = message.reader().readByte();
                                                MoQuaScr.gI().Chuoi = message.reader().readUTF();
                                                int i20 = message.reader().readByte();
                                                String[] strArr2 = new String[i20];
                                                while (i < i20) {
                                                    strArr2[i] = message.reader().readUTF();
                                                    i = (byte) (i + 1);
                                                }
                                                MoQuaScr.gI().thongBao = strArr2;
                                                return;
                                            case -61:
                                                byte b7 = message.reader().readByte();
                                                byte b8 = message.reader().readByte();
                                                byte b9 = message.reader().readByte();
                                                short s3 = message.reader().readShort();
                                                byte b10 = message.reader().readByte();
                                                byte bEp = 0;
                                                try {
                                                    bEp = message.reader().readByte();
                                                } catch (Exception unused2) {
                                                }
                                                GCanvas.gameScr.onEpDothu((short) 1000, b7, b8, s3, b9, b10, bEp);
                                                return;
                                            case -60:
                                                GCanvas.gameScr.addMsgWorld(message.reader().readUTF());
                                                return;
                                            case -59:
                                                byte b11 = message.reader().readByte();
                                                for (byte b12 = 0; b12 < b11; b12 = (byte) (b12 + 1)) {
                                                    byte b13 = message.reader().readByte();
                                                    byte b14 = message.reader().readByte();
                                                    String utf = message.reader().readUTF();
                                                    GCanvas.gameScr.setMyFarm(b13, b14, message.reader().readByte() * 16, message.reader().readByte() * 16, utf, utf, message.reader().readByte(), message.reader().readInt(), message.reader().readBoolean(), message.reader().readByte(), message.reader().readByte());
                                                }
                                                GCanvas.gameScr.xBeginFrame = (short) getMinMax(GCanvas.gameScr.actors, (byte) 0, 5000);
                                                GCanvas.gameScr.yBeginFrame = (short) getMinMax(GCanvas.gameScr.actors, (byte) 1, 5000);
                                                GCanvas.gameScr.xTheendFrame = (short) getMinMax(GCanvas.gameScr.actors, (byte) 2, -1);
                                                GCanvas.gameScr.yTheendFrame = (short) getMinMax(GCanvas.gameScr.actors, (byte) 3, -1);
                                                return;
                                            default:
                                                switch (b) {
                                                    case -57:
                                                        if (message.reader().readByte() == 0) {
                                                            boolean z = message.reader().readBoolean();
                                                            short s4 = message.reader().readShort();
                                                            byte b15 = message.reader().readByte();
                                                            if (GCanvas.gameScr.mainChar.ID == s4) {
                                                                GCanvas.gameScr.mainChar.isAnimal = z;
                                                                if (z) {
                                                                    GCanvas.gameScr.mainChar.myAnimal = new IsAnimal();
                                                                    GCanvas.gameScr.mainChar.myAnimal.idMonster = b15;
                                                                    GCanvas.gameScr.mainChar.myAnimal.type = b15;
                                                                    GCanvas.gameScr.mainChar.effAnimal = new WayPoint(GCanvas.gameScr.mainChar.x, GCanvas.gameScr.mainChar.y - 5, (short) 1, (byte) 1, false);
                                                                    GCanvas.gameScr.mainChar.timeRemoEffanimal = (System.currentTimeMillis() / 1000) + 5;
                                                                } else if (GCanvas.gameScr.mainChar.myAnimal != null) {
                                                                    GCanvas.gameScr.mainChar.myAnimal = null;
                                                                }
                                                            } else {
                                                                Char r3 = (Char) GCanvas.gameScr.findCharByID(s4);
                                                                if (r3 != null) {
                                                                    r3.isAnimal = z;
                                                                    if (z) {
                                                                        r3.myAnimal = new IsAnimal();
                                                                        r3.myAnimal.idMonster = b15;
                                                                        r3.myAnimal.type = b15;
                                                                        r3.effAnimal = new WayPoint(r3.x, r3.y - 5, (short) 1, (byte) 1, false);
                                                                        r3.timeRemoEffanimal = (System.currentTimeMillis() / 1000) + 5;
                                                                    } else if (r3.myAnimal != null) {
                                                                        r3.myAnimal = null;
                                                                    }
                                                                }
                                                            }
                                                            if (IsAnimal.img[b15] == null) {
                                                                GCanvas.gameScr.gameService.sendGetImgIsanimal(b15, s4);
                                                                return;
                                                            }
                                                            return;
                                                        }
                                                        byte b16 = message.reader().readByte();
                                                        int i21 = message.reader().readShort();
                                                        byte[] bArr3 = new byte[i21];
                                                        message.reader().read(bArr3, 0, i21);
                                                        IsAnimal.hImg[b16] = message.reader().readByte();
                                                        IsAnimal.img[b16] = Res.createImgByByteArray(bArr3);
                                                        IsAnimal.wimg[b16] = IsAnimal.img[b16].getWidth();
                                                        IsAnimal.frameRun[b16] = (byte[][]) Array.newInstance((Class<?>) Byte.TYPE, 4, message.reader().readByte());
                                                        for (int i22 = 0; i22 < IsAnimal.frameRun[b16][0].length; i22++) {
                                                            IsAnimal.frameRun[b16][0][i22] = message.reader().readByte();
                                                            IsAnimal.frameRun[b16][1][i22] = message.reader().readByte();
                                                            IsAnimal.frameRun[b16][2][i22] = message.reader().readByte();
                                                            IsAnimal.frameRun[b16][3][i22] = message.reader().readByte();
                                                        }
                                                        IsAnimal.frameStand[b16] = (byte[][]) Array.newInstance((Class<?>) Byte.TYPE, 4, message.reader().readByte());
                                                        for (int i23 = 0; i23 < IsAnimal.frameStand[b16][0].length; i23++) {
                                                            IsAnimal.frameStand[b16][0][i23] = message.reader().readByte();
                                                            IsAnimal.frameStand[b16][1][i23] = message.reader().readByte();
                                                            IsAnimal.frameStand[b16][2][i23] = message.reader().readByte();
                                                            IsAnimal.frameStand[b16][3][i23] = message.reader().readByte();
                                                        }
                                                        IsAnimal.frameAt[b16] = (byte[][]) Array.newInstance((Class<?>) Byte.TYPE, 4, message.reader().readByte());
                                                        for (int i24 = 0; i24 < IsAnimal.frameAt[b16][0].length; i24++) {
                                                            IsAnimal.frameAt[b16][0][i24] = message.reader().readByte();
                                                            IsAnimal.frameAt[b16][1][i24] = message.reader().readByte();
                                                            IsAnimal.frameAt[b16][2][i24] = message.reader().readByte();
                                                            IsAnimal.frameAt[b16][3][i24] = message.reader().readByte();
                                                        }
                                                        IsAnimal.addx[b16] = message.reader().readByte();
                                                        IsAnimal.addy[b16] = message.reader().readByte();
                                                        return;
                                                    case -56:
                                                        Cout.println("SKILL_CLAN: ");
                                                        int i25 = message.reader().readByte();
                                                        SkillClan[] skillClanArr = new SkillClan[i25];
                                                        for (int i26 = 0; i26 < i25; i26++) {
                                                            SkillClan skillClan = new SkillClan();
                                                            skillClanArr[i26] = skillClan;
                                                            skillClan.time = message.reader().readShort();
                                                            skillClanArr[i26].idIcon = message.reader().readByte();
                                                            skillClanArr[i26].info = message.reader().readUTF();
                                                            skillClanArr[i26].lv = message.reader().readByte();
                                                        }
                                                        int i27 = message.reader().readByte();
                                                        SkillClan[] skillClanArr2 = new SkillClan[i27];
                                                        while (i < i27) {
                                                            SkillClan skillClan2 = new SkillClan();
                                                            skillClanArr2[i] = skillClan2;
                                                            skillClan2.time = message.reader().readShort();
                                                            skillClanArr2[i].idIcon = message.reader().readByte();
                                                            skillClanArr2[i].info = message.reader().readUTF();
                                                            skillClanArr2[i].lv = message.reader().readByte();
                                                            i++;
                                                        }
                                                        GCanvas.gameScr.onSkillClan(skillClanArr, skillClanArr2);
                                                        return;
                                                    case -55:
                                                        GCanvas.gameScr.onDropItem2Ground(message);
                                                        return;
                                                    default:
                                                        switch (b) {
                                                            case -52:
                                                                String utf2 = message.reader().readUTF();
                                                                short s5 = message.reader().readShort();
                                                                byte b17 = message.reader().readByte();
                                                                Vector vector = new Vector();
                                                                for (int i28 = 0; i28 < b17; i28++) {
                                                                    Mineral mineral = new Mineral();
                                                                    mineral.name = message.reader().readUTF();
                                                                    mineral.idTemplate = message.reader().readShort();
                                                                    mineral.number = message.reader().readByte();
                                                                    vector.addElement(mineral);
                                                                }
                                                                byte b18 = message.reader().readByte();
                                                                try {
                                                                    i = message.reader().readByte();
                                                                    break;
                                                                } catch (Exception unused3) {
                                                                }
                                                                GCanvas.gameScr.onCheDo(utf2, s5, vector, b18, i);
                                                                return;
                                                            case -51:
                                                                short s6 = message.reader().readShort();
                                                                int iAvailable = message.reader().available();
                                                                byte[] bArr4 = new byte[iAvailable];
                                                                message.reader().read(bArr4);
                                                                ImageIcon imageIcon = (ImageIcon) GameData.listImgIcon.get("" + ((int) s6));
                                                                imageIcon.isLoad = false;
                                                                imageIcon.img = BitmapFactory.decodeByteArray(bArr4, 0, iAvailable);
                                                                return;
                                                            case -50:
                                                                GCanvas.gameScr.setNPC_server(message.reader().readUTF(), message.reader().readShort(), message.reader().readShort(), message.reader().readShort(), message.reader().readShort(), message.reader().readShort(), message.reader().readShort(), message.reader().readByte(), message.reader().readByte());
                                                                return;
                                                            case -49:
                                                                byte b19 = message.reader().readByte();
                                                                short s7 = message.reader().readByte();
                                                                if (b19 == 0) {
                                                                    if (EffectServerManager.getEffect(s7) == null) {
                                                                        GameService.gI().doRequestEffectData(s7);
                                                                    }
                                                                    EffectServerManager effectServerManager = new EffectServerManager();
                                                                    effectServerManager.ID = s7;
                                                                    effectServerManager.style = message.reader().readByte();
                                                                    short s8 = message.reader().readByte();
                                                                    effectServerManager.count = s8;
                                                                    effectServerManager.loopLimit = s8;
                                                                    effectServerManager.loop = message.reader().readShort();
                                                                    effectServerManager.loopType = message.reader().readByte();
                                                                    if (effectServerManager.loopType == 1) {
                                                                        effectServerManager.radius = message.reader().readShort();
                                                                    } else if (effectServerManager.loopType == 2) {
                                                                        int i29 = message.reader().readByte();
                                                                        effectServerManager.xLoop = new short[i29];
                                                                        effectServerManager.yLoop = new short[i29];
                                                                        while (i < i29) {
                                                                            effectServerManager.xLoop[i] = message.reader().readShort();
                                                                            effectServerManager.yLoop[i] = message.reader().readShort();
                                                                            i++;
                                                                        }
                                                                    }
                                                                    if (effectServerManager.style == 0) {
                                                                        effectServerManager.idPlayer = message.reader().readShort();
                                                                    } else {
                                                                        effectServerManager.x = message.reader().readShort();
                                                                        effectServerManager.y = message.reader().readShort();
                                                                    }
                                                                    GCanvas.gameScr.onEffect(effectServerManager);
                                                                    return;
                                                                }
                                                                EffectData effectData = new EffectData();
                                                                effectData.ID = s7;
                                                                effectData.setInfo(message);
                                                                EffectServerManager.effectList.addElement(effectData);
                                                                return;
                                                            case -48:
                                                                byte b20 = message.reader().readByte();
                                                                if (b20 != -1) {
                                                                    byte[] bArr5 = new byte[message.reader().readShort()];
                                                                    message.reader().read(bArr5);
                                                                    TreeInfo tree = Res.getTree(b20);
                                                                    if (tree != null) {
                                                                        tree.onImage(bArr5, b20);
                                                                        return;
                                                                    }
                                                                    return;
                                                                }
                                                                byte b21 = message.reader().readByte();
                                                                byte b22 = message.reader().readByte();
                                                                int iAvailable2 = message.reader().available();
                                                                byte[] bArr6 = new byte[iAvailable2];
                                                                while (i < iAvailable2) {
                                                                    bArr6[i] = message.reader().readByte();
                                                                    i++;
                                                                }
                                                                GCanvas.gameScr.onLoadCloth(bArr6, b21, b22);
                                                                return;
                                                            case -47:
                                                                short s9 = message.reader().readShort();
                                                                boolean z2 = message.reader().readBoolean();
                                                                byte[] bArr7 = new byte[message.reader().readShort()];
                                                                message.reader().read(bArr7);
                                                                if (Res.monsterTemplates[s9] != null) {
                                                                    Res.monsterTemplates[s9].onImage(z2, bArr7);
                                                                    return;
                                                                }
                                                                return;
                                                            default:
                                                                switch (b) {
                                                                    case -39:
                                                                        GameScr.onWeather(message.reader().readByte(), message.reader().readShort(), message.reader().readInt());
                                                                        return;
                                                                    case -38:
                                                                        GameScr.isFight = message.reader().readBoolean();
                                                                        int i30 = message.reader().readByte();
                                                                        GameScr.timeChiemThanh = new int[i30];
                                                                        GameScr.nameChiemThanh = new String[i30];
                                                                        GameScr.curTimeCT = new int[i30];
                                                                        GameScr.idChiemThanh = new short[i30];
                                                                        GameScr.nameClan = new String[i30];
                                                                        while (i < i30) {
                                                                            GameScr.timeChiemThanh[i] = message.reader().readShort();
                                                                            short s10 = message.reader().readShort();
                                                                            GameScr.nameChiemThanh[i] = message.reader().readUTF();
                                                                            GameScr.curTimeCT[i] = (int) (System.currentTimeMillis() / 1000);
                                                                            Char r4 = (Char) GCanvas.gameScr.findCharByID(s10);
                                                                            GameScr.idChiemThanh[i] = s10;
                                                                            if (r4 != null) {
                                                                                r4.EffFace = null;
                                                                            }
                                                                            if (r4 != null && GameScr.timeChiemThanh[i] > 0) {
                                                                                r4.EffFace = new WayPoint(r4.x, r4.y - 5, (short) 1, (byte) 1, false);
                                                                            }
                                                                            GameScr.nameClan[i] = message.reader().readUTF().toLowerCase();
                                                                            i++;
                                                                        }
                                                                        return;
                                                                    case -37:
                                                                        byte b23 = message.reader().readByte();
                                                                        if (b23 != 0) {
                                                                            if (b23 == 1) {
                                                                                GCanvas.gameScr.onContentClanQuest(message.reader().readUTF());
                                                                                return;
                                                                            }
                                                                            if (b23 == 2) {
                                                                                String utf3 = message.reader().readUTF();
                                                                                Res.PORTION_ITEM_NAME_CLAN = message.reader().readUTF();
                                                                                ViewInfoScr.gI().setInfo("NHIỆM VỤ", utf3);
                                                                                ViewInfoScr.gI().switchToMe();
                                                                                GCanvas.endDlg();
                                                                                return;
                                                                            }
                                                                            return;
                                                                        }
                                                                        String str = "`" + message.reader().readUTF();
                                                                        byte b24 = message.reader().readByte();
                                                                        if (b24 == 0) {
                                                                            WindowInfoScr.idMonsterClanQuest = message.reader().readByte();
                                                                            WindowInfoScr.numberQuestAll = message.reader().readShort();
                                                                            WindowInfoScr.numberQuestGet = message.reader().readShort();
                                                                            str = str + "\nHoàn thành: " + ((int) WindowInfoScr.numberQuestGet) + "/" + ((int) WindowInfoScr.numberQuestAll) + "Con";
                                                                        } else if (b24 == 1) {
                                                                            GameScr.idItemClanquest = message.reader().readByte();
                                                                            WindowInfoScr.numberQuestAll = message.reader().readShort();
                                                                            WindowInfoScr.numberQuestGet = message.reader().readShort();
                                                                            str = str + "\nĐã lấy: " + ((int) WindowInfoScr.numberQuestGet) + "/" + ((int) WindowInfoScr.numberQuestAll);
                                                                        } else if (b24 == 2) {
                                                                            GameScr.posNpcReceiveClan = new Position();
                                                                            GameScr.posNpcReceiveClan.index = message.reader().readByte();
                                                                            int size = GCanvas.gameScr.actors.size();
                                                                            for (int i31 = 0; i31 < size; i31++) {
                                                                                Actor actor = (Actor) GCanvas.gameScr.actors.elementAt(i31);
                                                                                if ((actor instanceof NPC) && ((NPC) actor).type == GameScr.posNpcReceiveClan.index) {
                                                                                    GameScr.posNpcReceiveClan.x = actor.x;
                                                                                    GameScr.posNpcReceiveClan.y = actor.y;
                                                                                }
                                                                            }
                                                                        }
                                                                        Res.PORTION_ITEM_NAME_CLAN = message.reader().readUTF();
                                                                        WindowInfoScr.gI().questClan(str, (byte) 0);
                                                                        return;
                                                                    default:
                                                                        switch (b) {
                                                                            case -33:
                                                                                int i32 = message.reader().readByte() * 16;
                                                                                int i33 = message.reader().readByte() * 16;
                                                                                short s11 = message.reader().readShort();
                                                                                GCanvas.gameScr.posBoss = new Position(i32, i33);
                                                                                GCanvas.gameScr.posBoss.index = s11;
                                                                                if (GCanvas.gameScr.posBoss.index == GCanvas.gameScr.map) {
                                                                                    EffectManager.lowEffects.addElement(new WayPoint(i32, i33, s11, (byte) 0, true));
                                                                                    EffectManager.hiEffects.addElement(new WayPoint(i32, i33, s11, (byte) 1, true));
                                                                                    return;
                                                                                }
                                                                                return;
                                                                            case -32:
                                                                                final short s12 = message.reader().readShort();
                                                                                final byte b25 = message.reader().readByte();
                                                                                final String utf4 = message.reader().readUTF();
                                                                                GCanvas.startYesNoDlg(utf4, new IAction() {
                                                                                    @Override
                                                                                    public void perform() {
                                                                                        GCanvas.endDlg();
                                                                                        GameService.gI().doCustomPopup(s12, b25, utf4, 1);
                                                                                    }
                                                                                }, new IAction() {
                                                                                    @Override
                                                                                    public void perform() {
                                                                                        GCanvas.endDlg();
                                                                                        GameService.gI().doCustomPopup(s12, b25, utf4, 0);
                                                                                    }
                                                                                });
                                                                                return;
                                                                            case -31:
                                                                                GCanvas.gameScr.onTextBox(message.reader().readShort(), message.reader().readByte(), message.reader().readUTF(), message.reader().readByte());
                                                                                return;
                                                                            case -30:
                                                                                short s13 = message.reader().readShort();
                                                                                byte b26 = message.reader().readByte();
                                                                                int i34 = message.reader().readByte();
                                                                                String[] strArr3 = new String[i34];
                                                                                while (i < i34) {
                                                                                    strArr3[i] = message.reader().readUTF();
                                                                                    i++;
                                                                                }
                                                                                GCanvas.gameScr.onMenuOption(s13, b26, strArr3);
                                                                                return;
                                                                            default:
                                                                                switch (b) {
                                                                                    case -28:
                                                                                        byte[] bArr8 = new byte[message.reader().readShort()];
                                                                                        message.reader().read(bArr8);
                                                                                        GCanvas.gameScr.onCapCha(bArr8);
                                                                                        break;
                                                                                    case -27:
                                                                                        new MainChar();
                                                                                        byte b27 = message.reader().readByte();
                                                                                        byte b28 = message.reader().readByte();
                                                                                        if (b28 != -1) {
                                                                                            int i35 = message.reader().readShort();
                                                                                            byte[] bArr9 = new byte[i35];
                                                                                            for (int i36 = 0; i36 < i35; i36++) {
                                                                                                bArr9[i36] = message.reader().readByte();
                                                                                            }
                                                                                            int i37 = message.reader().readShort();
                                                                                            byte[] bArr10 = new byte[i37];
                                                                                            while (i < i37) {
                                                                                                bArr10[i] = message.reader().readByte();
                                                                                                i++;
                                                                                            }
                                                                                            Bitmap bitmapCreateImgByHeader = Res.createImgByHeader(bArr9, bArr10);
                                                                                            short s14 = message.reader().readByte();
                                                                                            short s15 = message.reader().readByte();
                                                                                            if (b27 == 0) {
                                                                                                CreateCharScr.gI().ch.imgWpa = bitmapCreateImgByHeader;
                                                                                                CreateCharScr.gI().ch.dxWear = s14;
                                                                                                CreateCharScr.gI().ch.dyWear = s15;
                                                                                            } else if (b27 == 1) {
                                                                                                GCanvas.gameScr.mainChar.weaponIndex = b28;
                                                                                                GCanvas.gameScr.mainChar.imgWpa = bitmapCreateImgByHeader;
                                                                                                GCanvas.gameScr.mainChar.dxWear = s14;
                                                                                                GCanvas.gameScr.mainChar.dyWear = s15;
                                                                                            } else if (b27 == 2) {
                                                                                                if (WindowInfoScr.getIndex() == 1) {
                                                                                                    WindowInfoScr.charWearing.imgWpa = bitmapCreateImgByHeader;
                                                                                                    WindowInfoScr.charWearing.dxWear = s14;
                                                                                                    WindowInfoScr.charWearing.dyWear = s15;
                                                                                                } else {
                                                                                                    WindowInfoScr.gI().imgWeaponAvatar = bitmapCreateImgByHeader;
                                                                                                }
                                                                                                if (MenuWindows.getIndex() == 1) {
                                                                                                    MenuWindows.charWearing.imgWpa = bitmapCreateImgByHeader;
                                                                                                    MenuWindows.charWearing.dxWear = s14;
                                                                                                    MenuWindows.charWearing.dyWear = s15;
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                        GCanvas.endDlg();
                                                                                        break;
                                                                                }
                                                                                return;
                                                                        }
                                                                }
                                                        }
                                                }
                                        }
                                }
                        }
                    }
                    byte b29 = message.reader().readByte();
                    if (b29 == 0) {
                        GCanvas.isShow = false;
                    }
                    if (b29 == 1) {
                        GCanvas.initTime();
                        GCanvas.isShow = true;
                        return;
                    }
                    return;
                }
                byte b30 = message.reader().readByte();
                String utf5 = message.reader().readUTF();
                if (b30 == 0) {
                    HelpScr.gI().setInfo(utf5);
                } else if (b30 == 1) {
                    return;
                }
                GCanvas.gameScr.onMSGServer(message.reader().readUTF());
                return;
            }
            short s16 = message.reader().readShort();
            int iAvailable3 = (byte) (message.reader().available() / 2);
            byte[] bArr11 = new byte[iAvailable3];
            byte[] bArr12 = new byte[iAvailable3];
            while (message.reader().available() > 0) {
                bArr11[i] = message.reader().readByte();
                bArr12[i] = message.reader().readByte();
                i++;
            }
            GCanvas.gameScr.onSetCharKham(s16, bArr11, bArr12);
        } catch (Exception unused4) {
        }
    }

    public static int getMinMax(Vector vector, byte b, int i) {
        int size = vector.size();
        for (int i2 = 0; i2 < size; i2++) {
            Actor actor = (Actor) vector.elementAt(i2);
            if (actor.catagory == 10) {
                Ground ground = (Ground) actor;
                if (b != 0) {
                    if (b != 1) {
                        if (b == 2) {
                            if (i < ground.x) {
                                i = ground.x;
                            }
                        } else if (b == 3 && i < ground.y) {
                            i = ground.y;
                        }
                    } else if (i > ground.y) {
                        i = ground.y;
                    }
                } else if (i > ground.x) {
                    i = ground.x;
                }
            }
        }
        return i;
    }
}
