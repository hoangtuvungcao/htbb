package game.networklogic;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.teamobi.kpah.GameMidlet;
import effect.DataSkillEff;
import effect.DynamicEffect;
import effect.PartFrame;
import effect.mDataEffect;
import game.model.AnimalInfo;
import game.model.AnimalMove;
import game.model.Char;
import game.model.ChatInfo;
import game.model.ClanInfo;
import game.model.Command;
import game.model.Cout;
import game.model.GemTemp;
import game.model.IAction;
import game.model.InfoAttributeItem;
import game.model.ItemInInventory;
import game.model.ItemTemplate;
import game.model.MainChar;
import game.model.MonsterTemplate;
import game.model.NameAttributeItem;
import game.model.PotionTemplate;
import game.model.Property;
import game.model.monster.Pet;
import game.model.skill.SkillManager;
import game.render.GCanvas;
import game.render.Res;
import game.render.screen.CharSelectScr;
import game.render.screen.GameData;
import game.render.screen.GameScr;
import game.render.screen.ListScr;
import game.render.screen.MenuWindows;
import game.render.screen.MessageScr;
import game.render.screen.PauseMenu;
import game.render.screen.WindowInfoScr;
import java.lang.reflect.Array;
import java.util.Vector;
import network.Message;

public class MessageRecieve1 extends Cmd_message {
    public static void Message1(Message message) {
        int width;
        byte b;
        Bitmap bitmap;
        int height;
        int i;
        short s;
        byte b2;
        byte b3;
        short s2;
        byte b4;
        byte b5;
        short s3;
        byte b6;
        byte b7;
        int i2;
        int height2;
        byte b8;
        Bitmap bitmapCreateImgByByteArray;
        String utf;
        byte b9;
        AnimalMove animalMove;
        long j;
        int i3;
        int i4;
        int i5;
        int i6;
        int i7;
        int i8;
        try {
            new IAction() {
                @Override
                public void perform() {
                }
            };
            byte b10 = message.command;
            int i9 = 0;
            if (b10 == 58) {
                int i10 = message.reader().readByte();
                int[] iArr = new int[i10];
                while (i9 < i10) {
                    iArr[i9] = message.reader().readShort();
                    i9++;
                }
                return;
            }
            if (b10 == 59) {
                GCanvas.gameScr.onInfoGiftQuest(message.reader().readUTF());
                return;
            }
            switch (b10) {
                case -65:
                    try {
                        GCanvas.gameScr.onGetItemQuestFromGround(message.reader().readShort(), message.reader().readShort(), message.reader().readByte());
                        return;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                case -53:
                    GCanvas.gameScr.onFriendList(message, 6, message.reader().readUTF().toUpperCase());
                    return;
                case -41:
                    GCanvas.gameScr.onGetGemFromGround(message.reader().readShort(), message.reader().readShort(), message.reader().readByte());
                    return;
                case -22:
                    Char r0 = new Char();
                    if (message.reader().readByte() == 0) {
                        r0.name = message.reader().readUTF();
                        r0.currentHead = message.reader().readByte();
                        r0.dir = (short) 0;
                        r0.level = message.reader().readByte();
                        r0.weapon_frame = (short) 0;
                        byte b11 = message.reader().readByte();
                        Vector vector = new Vector();
                        for (int i11 = 0; i11 < b11; i11++) {
                            ItemInInventory itemInInventory = new ItemInInventory();
                            byte b12 = message.reader().readByte();
                            itemInInventory.charClazz = b12;
                            itemInInventory.clazz = b12;
                            r0.clazz = b12;
                            itemInInventory.idTemplate = message.reader().readShort();
                            itemInInventory.level = message.reader().readByte();
                            itemInInventory.plusTemplate = message.reader().readByte();
                            itemInInventory.itemID = message.reader().readShort();
                            itemInInventory.colorName = message.reader().readByte();
                            itemInInventory.attCreate = new short[5];
                            for (int i12 = 0; i12 < 5; i12++) {
                                itemInInventory.attCreate[i12] = message.reader().readShort();
                            }
                            itemInInventory.heItem = message.reader().readByte();
                            itemInInventory.beKicked = message.reader().readByte();
                            itemInInventory.hangItem = message.reader().readByte();
                            itemInInventory.magic_physic = message.reader().readByte();
                            itemInInventory.lock = message.reader().readByte();
                            itemInInventory.nameCharSeal = message.reader().readUTF();
                            for (int i13 = 0; i13 < itemInInventory.newAtb.length; i13++) {
                                itemInInventory.newAtb[i13] = message.reader().readByte();
                            }
                            for (int i14 = 0; i14 < itemInInventory.lockAtb.length; i14++) {
                                itemInInventory.lockAtb[i14] = message.reader().readByte();
                            }
                            for (int i15 = 0; i15 < itemInInventory.addMoreLevelSkill.length; i15++) {
                                itemInInventory.addMoreLevelSkill[i15] = message.reader().readByte();
                            }
                            vector.addElement(itemInInventory);
                            ItemTemplate item = Res.getItem(itemInInventory.charClazz, itemInInventory.idTemplate);
                            if (item.type >= 3 && item.type < 8) {
                                GameService.gI().doRequestWeapone(2, item.type, item.style, Res.getItem(r0.clazz, itemInInventory.idTemplate).colorItem);
                                GCanvas.startWaitDlg();
                            }
                        }
                        r0.idClan = message.reader().readShort();
                        r0.idFashion = message.reader().readByte();
                        while (i9 < r0.idModel.length) {
                            r0.idModel[i9] = message.reader().readShort();
                            i9++;
                        }
                        r0.idAnimal = message.reader().readByte();
                        if (r0.idAnimal != -1) {
                            r0.infoAnimal = message.reader().readUTF();
                        }
                        r0.setWearingInfo(vector);
                        if (GCanvas.gameScr.focusedActor != null) {
                            r0.ID = GCanvas.gameScr.focusedActor.ID;
                        }
                        GCanvas.gameScr.onViewCharInfo(r0);
                        return;
                    }
                    Vector vector2 = new Vector();
                    byte b13 = message.reader().readByte();
                    if (b13 <= -1) {
                        GCanvas.startOKDlg("Chưa có thông tin linh thú");
                        return;
                    }
                    for (int i16 = 0; i16 < b13; i16++) {
                        ItemInInventory itemInInventory2 = new ItemInInventory();
                        byte b14 = message.reader().readByte();
                        itemInInventory2.clazz = b14;
                        itemInInventory2.charClazz = b14;
                        itemInInventory2.itemID = message.reader().readShort();
                        itemInInventory2.idTemplate = message.reader().readShort();
                        itemInInventory2.plusTemplate = message.reader().readByte();
                        itemInInventory2.level = message.reader().readByte();
                        itemInInventory2.mDurable = message.reader().readShort();
                        itemInInventory2.durable = message.reader().readShort();
                        itemInInventory2.colorName = message.reader().readByte();
                        itemInInventory2.attCreate = new short[5];
                        for (int i17 = 0; i17 < 5; i17++) {
                            itemInInventory2.attCreate[i17] = message.reader().readShort();
                        }
                        itemInInventory2.heItem = message.reader().readByte();
                        itemInInventory2.beKicked = message.reader().readByte();
                        itemInInventory2.hangItem = message.reader().readByte();
                        itemInInventory2.magic_physic = message.reader().readByte();
                        itemInInventory2.lock = message.reader().readByte();
                        itemInInventory2.nameCharSeal = message.reader().readUTF();
                        for (int i18 = 0; i18 < itemInInventory2.newAtb.length; i18++) {
                            itemInInventory2.newAtb[i18] = message.reader().readByte();
                        }
                        for (int i19 = 0; i19 < itemInInventory2.lockAtb.length; i19++) {
                            itemInInventory2.lockAtb[i19] = message.reader().readByte();
                        }
                        for (int i20 = 0; i20 < itemInInventory2.addMoreLevelSkill.length; i20++) {
                            itemInInventory2.addMoreLevelSkill[i20] = message.reader().readByte();
                        }
                        vector2.addElement(itemInInventory2);
                    }
                    byte b15 = message.reader().readByte();
                    int iAvailable = message.reader().available();
                    byte[] bArr = new byte[iAvailable];
                    while (message.reader().available() > 0) {
                        message.reader().read(bArr, 0, iAvailable);
                    }
                    if (iAvailable > 0) {
                        Bitmap bitmapCreateImgByByteArray2 = Res.createImgByByteArray(bArr);
                        byte b16 = (byte) (b15 == 3 ? 3 : 6);
                        if (bitmapCreateImgByByteArray2 != null) {
                            b = b16;
                            width = bitmapCreateImgByByteArray2.getWidth();
                            bitmap = bitmapCreateImgByByteArray2;
                            height = bitmapCreateImgByByteArray2.getHeight() / b15;
                        } else {
                            b = b16;
                            width = 0;
                            bitmap = bitmapCreateImgByByteArray2;
                            height = 0;
                        }
                    } else {
                        width = 0;
                        b = 0;
                        bitmap = null;
                        height = 0;
                    }
                    GCanvas.gameScr.onAnimalWearingInfo(GCanvas.gameScr.saveIDViewInfoAnimal, vector2, bitmap, b15, width, height, b);
                    return;
                case -5:
                    byte b17 = message.reader().readByte();
                    for (int i21 = 0; i21 < b17; i21++) {
                        String utf2 = message.reader().readUTF();
                        MessageScr.gI().addTab(utf2 + ": " + message.reader().readUTF(), utf2, 0);
                    }
                    return;
                case 11:
                    GCanvas.gameScr.onPingBack();
                    return;
                case 30:
                    GCanvas.gameScr.onSetXP(message.reader().readShort(), message.reader().readShort(), message.reader().readInt());
                    return;
                case 54:
                    GCanvas.gameScr.onFinishQuest(message);
                    return;
                case 56:
                    GCanvas.gameScr.onInfoQuestLogin(message);
                    return;
                case 61:
                    short s4 = message.reader().readShort();
                    short s5 = message.reader().readShort();
                    ItemDropInfo itemDropInfo = new ItemDropInfo();
                    itemDropInfo.itemCatagory = message.reader().readByte();
                    itemDropInfo.itemTemplateID = message.reader().readByte();
                    itemDropInfo.itemID = message.reader().readShort();
                    itemDropInfo.x = message.reader().readShort();
                    itemDropInfo.y = message.reader().readShort();
                    GCanvas.gameScr.onGiveItemToGround(s4, s5, itemDropInfo);
                    return;
                case 64:
                    int i22 = message.reader().readByte();
                    ItemDropInfo[] itemDropInfoArr = new ItemDropInfo[i22];
                    while (i9 < i22) {
                        ItemDropInfo itemDropInfo2 = new ItemDropInfo();
                        itemDropInfoArr[i9] = itemDropInfo2;
                        itemDropInfo2.itemCatagory = message.reader().readByte();
                        itemDropInfoArr[i9].itemTemplateID = message.reader().readByte();
                        itemDropInfoArr[i9].itemID = message.reader().readShort();
                        itemDropInfoArr[i9].x = message.reader().readShort();
                        itemDropInfoArr[i9].y = message.reader().readShort();
                        i9++;
                    }
                    return;
                case 78:
                    GCanvas.gameScr.onSellGemItem(message.reader().readShort());
                    return;
                case 83:
                    GCanvas.gameScr.onBossAttackPlayer(message);
                    return;
                case 100:
                    GCanvas.gameScr.onTemplateInfo(message);
                    return;
                default:
                    switch (b10) {
                        case -20:
                            final int i23 = message.reader().readInt();
                            GCanvas.startOKDlg("Bạn nhận được " + i23 + "xu từ bang hội.", new IAction() {
                                @Override
                                public void perform() {
                                    GCanvas.gameScr.mainChar.charXu += (long) i23;
                                }
                            });
                            return;
                        case -19:
                            byte b18 = message.reader().readByte();
                            String utf3 = message.reader().readUTF();
                            Vector vector3 = new Vector();
                            if (b18 != 5) {
                                if (b18 != 7 && b18 != 8) {
                                    if (b18 == 4) {
                                        GCanvas.gameScr.onFriendList(message, b18, "TOP CAO THỦ");
                                        return;
                                    } else {
                                        GCanvas.gameScr.onFriendList(message, b18, "TOP ĐẠI GIA");
                                        return;
                                    }
                                }
                                GCanvas.gameScr.onFriendList(message, b18, utf3);
                                return;
                            }
                            short s6 = message.reader().readShort();
                            while (i9 < s6) {
                                ClanInfo clanInfo = new ClanInfo();
                                clanInfo.id = message.reader().readShort();
                                clanInfo.name = message.reader().readUTF();
                                clanInfo.master = message.reader().readUTF();
                                clanInfo.level = message.reader().readByte();
                                clanInfo.members = message.reader().readShort();
                                clanInfo.money = message.reader().readLong();
                                clanInfo.nation = message.reader().readByte();
                                vector3.addElement(clanInfo);
                                i9++;
                            }
                            ListScr.gI().setInfo(vector3, b18, "TOP BANG HỘI");
                            ListScr.gI().switchToMe();
                            GCanvas.endDlg();
                            return;
                        case -18:
                            MessageScr.gI().addTab(message.reader().readUTF(), "Bang hội", 1);
                            return;
                        case -17:
                            Vector vector4 = new Vector();
                            short s7 = message.reader().readShort();
                            while (i9 < s7) {
                                ChatInfo chatInfo = new ChatInfo();
                                chatInfo.ID = message.reader().readInt();
                                chatInfo.name = message.reader().readUTF();
                                chatInfo.content = message.reader().readUTF();
                                vector4.addElement(chatInfo);
                                i9++;
                            }
                            ListScr.gI().setInfo(vector4, 2, "THÔNG BÁO");
                            ListScr.gI().switchToMe();
                            GCanvas.endDlg();
                            return;
                        default:
                            switch (b10) {
                                case -13:
                                    GCanvas.gameScr.mainChar.idClan = (short) -1;
                                    GCanvas.startOKDlg("Bạn bị mời khỏi bang hội.");
                                    return;
                                case -12:
                                    ClanInfo clanInfo2 = new ClanInfo();
                                    clanInfo2.id = message.reader().readShort();
                                    clanInfo2.name = message.reader().readUTF();
                                    clanInfo2.master = message.reader().readUTF();
                                    clanInfo2.level = message.reader().readByte();
                                    clanInfo2.members = message.reader().readShort();
                                    clanInfo2.money = message.reader().readLong();
                                    clanInfo2.dedicationPoint = message.reader().readLong();
                                    clanInfo2.xp = message.reader().readLong();
                                    clanInfo2.date = message.reader().readUTF();
                                    clanInfo2.status = message.reader().readUTF();
                                    clanInfo2.dissolved = message.reader().readBoolean();
                                    clanInfo2.nation = message.reader().readByte();
                                    if (clanInfo2.dissolved) {
                                        clanInfo2.strDissolved = message.reader().readUTF();
                                    }
                                    GCanvas.gameScr.showClanInfo(clanInfo2);
                                    return;
                                case -11:
                                    if (message.reader().readByte() == 0) {
                                        final short s8 = message.reader().readShort();
                                        GCanvas.startYesNoDlg(message.reader().readUTF(), new IAction() {
                                            @Override
                                            public void perform() {
                                                GameService.gI().sendRequestAddClan(s8, (byte) 1, true);
                                                GCanvas.endDlg();
                                            }
                                        }, new IAction() {
                                            @Override
                                            public void perform() {
                                                GameService.gI().sendRequestAddClan(s8, (byte) 1, false);
                                                GCanvas.endDlg();
                                            }
                                        });
                                        return;
                                    }
                                    String utf4 = message.reader().readUTF();
                                    if (message.reader().readBoolean()) {
                                        short s9 = message.reader().readShort();
                                        short s10 = message.reader().readShort();
                                        if (s9 == GCanvas.gameScr.mainChar.ID) {
                                            GCanvas.gameScr.mainChar.idClan = s9;
                                            PauseMenu.gI().initName();
                                        } else {
                                            ((Char) GCanvas.gameScr.findCharByID(s9)).idClan = s10;
                                        }
                                    }
                                    GCanvas.startOKDlg(utf4);
                                    return;
                                case -10:
                                    short s11 = message.reader().readShort();
                                    String utf5 = message.reader().readUTF();
                                    GCanvas.gameScr.mainChar.idClan = s11;
                                    GCanvas.gameScr.mainChar.isMaster = (byte) 0;
                                    PauseMenu.gI().initName();
                                    MenuWindows.gI().initName();
                                    GCanvas.startOKDlg(utf5);
                                    return;
                                case -9:
                                    int i24 = message.reader().readShort();
                                    final short[] sArr = new short[i24];
                                    while (i9 < i24) {
                                        sArr[i9] = message.reader().readShort();
                                        i9++;
                                    }
                                    String utf6 = message.reader().readUTF();
                                    if (utf6.equals("")) {
                                        GCanvas.gameScr.onRegClan(sArr);
                                        return;
                                    } else {
                                        GCanvas.startYesNoDlg(utf6, new IAction() {
                                            @Override
                                            public void perform() {
                                                GCanvas.gameScr.onRegClan(sArr);
                                                GCanvas.endDlg();
                                            }
                                        });
                                        return;
                                    }
                                case -8:
                                    byte b19 = message.reader().readByte();
                                    byte b20 = message.reader().readByte();
                                    int i25 = message.reader().readShort();
                                    byte[] bArr2 = new byte[i25];
                                    message.reader().read(bArr2);
                                    if (b19 == 1) {
                                        GameData.saveImgSkill(b20, bArr2);
                                        return;
                                    }
                                    if (b19 != 2) {
                                        if (b19 == 3) {
                                            GameData.saveImgGem(b20, bArr2);
                                            return;
                                        } else {
                                            if (b19 != 4) {
                                                return;
                                            }
                                            AnimalInfo.imgAnimal = BitmapFactory.decodeByteArray(bArr2, 0, i25);
                                            return;
                                        }
                                    }
                                    message.reader().read(new byte[message.reader().readShort()]);
                                    int i26 = message.reader().readByte();
                                    byte[][] bArr3 = new byte[i26][];
                                    byte[][][] bArr4 = new byte[i26][][];
                                    for (int i27 = 0; i27 < i26; i27++) {
                                        bArr3[i27] = new byte[message.reader().readShort()];
                                        message.reader().read(bArr3[i27]);
                                        int i28 = message.reader().readByte();
                                        bArr4[i27] = new byte[i28][];
                                        for (int i29 = 0; i29 < i28; i29++) {
                                            bArr4[i27][i29] = new byte[message.reader().readShort()];
                                            message.reader().read(bArr4[i27][i29]);
                                        }
                                    }
                                    GameData.saveImgPotion(b20, bArr2, null, bArr3, bArr4);
                                    return;
                                case -7:
                                    GCanvas.gameScr.onFriendList(message, 1, message.reader().readUTF().toUpperCase());
                                    return;
                                default:
                                    switch (b10) {
                                        case 13:
                                            int i30 = message.reader().readByte();
                                            if (i30 == -1) {
                                                GCanvas.startOKDlg(message.reader().readUTF());
                                                return;
                                            }
                                            MainChar[] mainCharArr = new MainChar[i30];
                                            for (int i31 = 0; i31 < i30; i31++) {
                                                MainChar mainChar = new MainChar();
                                                mainCharArr[i31] = mainChar;
                                                mainChar.IDDB = message.reader().readInt();
                                                mainCharArr[i31].name = message.reader().readUTF();
                                                mainCharArr[i31].currentHead = message.reader().readByte();
                                                byte b21 = message.reader().readByte();
                                                for (int i32 = 0; i32 < b21; i32++) {
                                                    byte b22 = message.reader().readByte();
                                                    byte b23 = message.reader().readByte();
                                                    if (b22 == 0) {
                                                        mainCharArr[i31].bodyStyle = b23;
                                                    } else if (b22 == 1) {
                                                        mainCharArr[i31].legStyle = b23;
                                                    } else if (b22 == 2) {
                                                        mainCharArr[i31].hatStyle = b23;
                                                    } else if (b22 == 3 || b22 == 4 || b22 == 5 || b22 == 6 || b22 == 7) {
                                                        mainCharArr[i31].weaponType = b22;
                                                        mainCharArr[i31].weaponStyle = b23;
                                                    } else if (b22 == 19) {
                                                        mainCharArr[i31].coatStyle = b23;
                                                    }
                                                }
                                                mainCharArr[i31].lastLv = message.reader().readShort();
                                                mainCharArr[i31].using = message.reader().readByte();
                                                mainCharArr[i31].nationID = message.reader().readByte();
                                                mainCharArr[i31].day = message.reader().readShort();
                                                if (message.reader().readByte() != -1) {
                                                    int i33 = message.reader().readShort();
                                                    byte[] bArr5 = new byte[i33];
                                                    for (int i34 = 0; i34 < i33; i34++) {
                                                        bArr5[i34] = message.reader().readByte();
                                                    }
                                                    int i35 = message.reader().readShort();
                                                    byte[] bArr6 = new byte[i35];
                                                    for (int i36 = 0; i36 < i35; i36++) {
                                                        bArr6[i36] = message.reader().readByte();
                                                    }
                                                    mainCharArr[i31].imgWpa = Res.createImgByHeader(bArr5, bArr6);
                                                    mainCharArr[i31].dxWear = message.reader().readByte();
                                                    mainCharArr[i31].dyWear = message.reader().readByte();
                                                }
                                            }
                                            while (i9 < i30) {
                                                try {
                                                    i = message.reader().readInt();
                                                } catch (Exception unused) {
                                                    i = -1;
                                                }
                                                if (i != -1 && mainCharArr[i9].IDDB == i) {
                                                    try {
                                                        s = message.reader().readShort();
                                                        b2 = message.reader().readByte();
                                                        b3 = message.reader().readByte();
                                                        s2 = message.reader().readShort();
                                                        b4 = message.reader().readByte();
                                                        b5 = message.reader().readByte();
                                                        s3 = message.reader().readShort();
                                                        b6 = message.reader().readByte();
                                                        b7 = message.reader().readByte();
                                                    } catch (Exception unused2) {
                                                        s = -1;
                                                        b2 = -1;
                                                        b3 = -1;
                                                        s2 = -1;
                                                        b4 = -1;
                                                        b5 = -1;
                                                        s3 = -1;
                                                        b6 = -1;
                                                        b7 = -1;
                                                    }
                                                    mainCharArr[i9].idImgAVT = s;
                                                    mainCharArr[i9].indexAVT = b2;
                                                    mainCharArr[i9].nFrameAVT = b3;
                                                    mainCharArr[i9].idImgPP = s2;
                                                    mainCharArr[i9].indexPP = b4;
                                                    mainCharArr[i9].nFramePP = b5;
                                                    mainCharArr[i9].idImageWeapon = s3;
                                                    mainCharArr[i9].typeWeapon = b6;
                                                    mainCharArr[i9].nFrameWP = b7;
                                                    break;
                                                }
                                                i9++;
                                                break;
                                            }
                                            CharSelectScr.gI().setCharList(mainCharArr);
                                            CharSelectScr.gI().switchToMe();
                                            return;
                                        case 14:
                                            GCanvas.startOKDlg("Xin chọn tên nhân vật khác");
                                            return;
                                        case 15:
                                            if (message.reader().readByte() != 0) {
                                                Vector vector5 = new Vector();
                                                short s12 = message.reader().readShort();
                                                byte b24 = message.reader().readByte();
                                                if (b24 > -1) {
                                                    for (int i37 = 0; i37 < b24; i37++) {
                                                        ItemInInventory itemInInventory3 = new ItemInInventory();
                                                        byte b25 = message.reader().readByte();
                                                        itemInInventory3.clazz = b25;
                                                        itemInInventory3.charClazz = b25;
                                                        itemInInventory3.itemID = message.reader().readShort();
                                                        itemInInventory3.idTemplate = message.reader().readShort();
                                                        itemInInventory3.plusTemplate = message.reader().readByte();
                                                        itemInInventory3.level = message.reader().readByte();
                                                        itemInInventory3.mDurable = message.reader().readShort();
                                                        itemInInventory3.durable = message.reader().readShort();
                                                        itemInInventory3.colorName = message.reader().readByte();
                                                        itemInInventory3.viTriVe = message.reader().readByte();
                                                        itemInInventory3.heItem = message.reader().readByte();
                                                        itemInInventory3.beKicked = message.reader().readByte();
                                                        itemInInventory3.hangItem = message.reader().readByte();
                                                        itemInInventory3.magic_physic = message.reader().readByte();
                                                        itemInInventory3.lock = message.reader().readByte();
                                                        itemInInventory3.nameCharSeal = message.reader().readUTF();
                                                        itemInInventory3.allAttribute.removeAllElements();
                                                        byte b26 = message.reader().readByte();
                                                        for (byte b27 = 0; b27 < b26; b27 = (byte) (b27 + 1)) {
                                                            itemInInventory3.allAttribute.addElement(new InfoAttributeItem((short) message.reader().readUnsignedByte(), message.reader().readShort()));
                                                        }
                                                        itemInInventory3.isEnoughData = true;
                                                        vector5.addElement(itemInInventory3);
                                                    }
                                                    byte b28 = message.reader().readByte();
                                                    int iAvailable2 = message.reader().available();
                                                    byte[] bArr7 = new byte[iAvailable2];
                                                    while (message.reader().available() > 0) {
                                                        message.reader().read(bArr7, 0, iAvailable2);
                                                    }
                                                    if (iAvailable2 > 0) {
                                                        bitmapCreateImgByByteArray = Res.createImgByByteArray(bArr7);
                                                        byte b29 = (byte) (b28 == 3 ? 3 : 6);
                                                        if (bitmapCreateImgByByteArray != null) {
                                                            int width2 = bitmapCreateImgByByteArray.getWidth();
                                                            height2 = bitmapCreateImgByByteArray.getHeight() / b28;
                                                            i2 = width2;
                                                        } else {
                                                            i2 = 0;
                                                            height2 = 0;
                                                        }
                                                        b8 = b29;
                                                    } else {
                                                        i2 = 0;
                                                        height2 = 0;
                                                        b8 = 1;
                                                        bitmapCreateImgByByteArray = null;
                                                    }
                                                    GCanvas.gameScr.onAnimalWearingInfo(s12, vector5, bitmapCreateImgByByteArray, b28, i2, height2, b8);
                                                    return;
                                                }
                                                return;
                                            }
                                            Vector vector6 = new Vector();
                                            short s13 = message.reader().readShort();
                                            byte b30 = message.reader().readByte();
                                            for (int i38 = 0; i38 < b30; i38++) {
                                                ItemInInventory itemInInventory4 = new ItemInInventory();
                                                byte b31 = message.reader().readByte();
                                                itemInInventory4.clazz = b31;
                                                itemInInventory4.charClazz = b31;
                                                itemInInventory4.itemID = message.reader().readShort();
                                                itemInInventory4.idTemplate = message.reader().readShort();
                                                itemInInventory4.plusTemplate = message.reader().readByte();
                                                itemInInventory4.level = message.reader().readByte();
                                                itemInInventory4.mDurable = message.reader().readShort();
                                                itemInInventory4.durable = message.reader().readShort();
                                                itemInInventory4.colorName = message.reader().readByte();
                                                itemInInventory4.viTriVe = message.reader().readByte();
                                                itemInInventory4.heItem = message.reader().readByte();
                                                itemInInventory4.beKicked = message.reader().readByte();
                                                itemInInventory4.hangItem = message.reader().readByte();
                                                itemInInventory4.magic_physic = message.reader().readByte();
                                                itemInInventory4.lock = message.reader().readByte();
                                                itemInInventory4.nameCharSeal = message.reader().readUTF();
                                                itemInInventory4.allAttribute.removeAllElements();
                                                itemInInventory4.lastTime = System.currentTimeMillis();
                                                itemInInventory4.dayUse = message.reader().readUnsignedShort();
                                                byte b32 = message.reader().readByte();
                                                for (byte b33 = 0; b33 < b32; b33 = (byte) (b33 + 1)) {
                                                    itemInInventory4.allAttribute.addElement(new InfoAttributeItem((short) message.reader().readUnsignedByte(), message.reader().readShort()));
                                                }
                                                itemInInventory4.isEnoughData = true;
                                                vector6.addElement(itemInInventory4);
                                            }
                                            byte b34 = message.reader().readByte();
                                            short[] sArr2 = new short[5];
                                            sArr2[0] = -1;
                                            sArr2[1] = -1;
                                            sArr2[2] = -1;
                                            sArr2[3] = -1;
                                            sArr2[4] = -1;
                                            for (int i39 = 0; i39 < 5; i39++) {
                                                sArr2[i39] = message.reader().readShort();
                                            }
                                            byte b35 = message.reader().readByte();
                                            if (b35 != -1) {
                                                utf = message.reader().readUTF();
                                                b9 = message.reader().readByte();
                                            } else {
                                                utf = "";
                                                b9 = 0;
                                            }
                                            if (message.reader().readByte() != -1) {
                                                AnimalMove animalMove2 = new AnimalMove();
                                                animalMove2.type = message.reader().readByte();
                                                animalMove2.imgID = message.reader().readShort();
                                                animalMove2.sumFrame = message.reader().readByte();
                                                animalMove2.infoPet = message.reader().readUTF();
                                                animalMove2.totalTime = message.reader().readInt();
                                                animalMove2.timeStart = System.currentTimeMillis();
                                                animalMove2.idFrame = (short) 0;
                                                animalMove2.count = (short) 0;
                                                animalMove2.catagory = (byte) 12;
                                                animalMove = animalMove2;
                                            } else {
                                                animalMove = null;
                                            }
                                            GCanvas.gameScr.onCharWearingInfo(s13, vector6, b34, b35, utf, animalMove, sArr2, b9);
                                            return;
                                        case 16:
                                            byte b36 = message.reader().readByte();
                                            int[] iArr2 = new int[Char.CHAR_POTION];
                                            if (b36 == 0) {
                                                try {
                                                    j = message.reader().readLong();
                                                    iArr2[0] = 0;
                                                    int unsignedByte = message.reader().readUnsignedByte();
                                                    for (int i40 = 0; i40 < unsignedByte; i40++) {
                                                        int unsignedByte2 = message.reader().readUnsignedByte();
                                                        int i41 = message.reader().readInt();
                                                        MainChar.listPotion[unsignedByte2].number = i41;
                                                        iArr2[unsignedByte2] = i41;
                                                    }
                                                } catch (Exception e2) {
                                                    e2.printStackTrace();
                                                    System.out.println("LOI CHARINVENTORY " + ((int) b36));
                                                    return;
                                                }
                                            } else {
                                                j = 0;
                                            }
                                            long j2 = j;
                                            Vector vector7 = new Vector();
                                            if (b36 == 1) {
                                                short s14 = message.reader().readShort();
                                                for (int i42 = 0; i42 < s14; i42++) {
                                                    ItemInInventory itemInInventory5 = new ItemInInventory();
                                                    itemInInventory5.charClazz = message.reader().readByte();
                                                    itemInInventory5.itemID = message.reader().readShort();
                                                    itemInInventory5.idTemplate = message.reader().readShort();
                                                    itemInInventory5.plusTemplate = message.reader().readByte();
                                                    itemInInventory5.level = message.reader().readByte();
                                                    itemInInventory5.mDurable = message.reader().readShort();
                                                    itemInInventory5.durable = message.reader().readShort();
                                                    itemInInventory5.clazz = message.reader().readByte();
                                                    itemInInventory5.numKham = message.reader().readByte();
                                                    itemInInventory5.totalKham = message.reader().readByte();
                                                    itemInInventory5.colorName = message.reader().readByte();
                                                    itemInInventory5.heItem = message.reader().readByte();
                                                    itemInInventory5.beKicked = message.reader().readByte();
                                                    itemInInventory5.hangItem = message.reader().readByte();
                                                    itemInInventory5.magic_physic = message.reader().readByte();
                                                    itemInInventory5.lock = message.reader().readByte();
                                                    itemInInventory5.nameCharSeal = message.reader().readUTF();
                                                    itemInInventory5.allAttribute.removeAllElements();
                                                    itemInInventory5.lastTime = System.currentTimeMillis();
                                                    itemInInventory5.dayUse = message.reader().readUnsignedShort();
                                                    byte b37 = message.reader().readByte();
                                                    for (byte b38 = 0; b38 < b37; b38 = (byte) (b38 + 1)) {
                                                        itemInInventory5.allAttribute.addElement(new InfoAttributeItem((short) message.reader().readUnsignedByte(), message.reader().readShort()));
                                                    }
                                                    itemInInventory5.isItemDrop = message.reader().readByte();
                                                    itemInInventory5.isEnoughData = true;
                                                    vector7.addElement(itemInInventory5);
                                                }
                                            }
                                            GCanvas.gameScr.mainChar.luong = message.reader().readInt();
                                            Vector vector8 = new Vector();
                                            if (b36 == 2) {
                                                byte b39 = message.reader().readByte();
                                                for (int i43 = 0; i43 < b39; i43++) {
                                                    AnimalInfo animalInfo = new AnimalInfo();
                                                    animalInfo.id = message.reader().readShort();
                                                    animalInfo.idImg = message.reader().readByte();
                                                    animalInfo.idPaint = message.reader().readByte();
                                                    animalInfo.level = message.reader().readByte();
                                                    animalInfo.name = message.reader().readUTF();
                                                    animalInfo.info = message.reader().readUTF();
                                                    animalInfo.typeAnimal = message.reader().readByte();
                                                    vector8.addElement(animalInfo);
                                                }
                                                byte b40 = message.reader().readByte();
                                                for (int i44 = 0; i44 < b40; i44++) {
                                                    AnimalInfo animalInfo2 = new AnimalInfo();
                                                    animalInfo2.id = message.reader().readShort();
                                                    animalInfo2.idImg = message.reader().readByte();
                                                    animalInfo2.idPaint = message.reader().readByte();
                                                    animalInfo2.name = message.reader().readUTF();
                                                    animalInfo2.typePet = message.reader().readByte();
                                                    animalInfo2.info = message.reader().readUTF();
                                                    animalInfo2.totalTimeCon = message.reader().readInt();
                                                    animalInfo2.timeStart = System.currentTimeMillis();
                                                    animalInfo2.typeAnimal = message.reader().readByte();
                                                    vector8.addElement(animalInfo2);
                                                }
                                            }
                                            if (b36 == 3) {
                                                MainChar.itemQuest = new short[message.reader().readByte()];
                                                while (i9 < MainChar.itemQuest.length) {
                                                    MainChar.itemQuest[i9] = message.reader().readShort();
                                                    i9++;
                                                }
                                            }
                                            System.out.println("DONG XONG DATA ");
                                            GCanvas.gameScr.mainChar.luongKhoa = message.reader().readInt();
                                            GCanvas.gameScr.onCharInventory(j2, iArr2, vector7, vector8, b36);
                                            return;
                                        case 17:
                                            try {
                                                MonsterDieInfo monsterDieInfo = new MonsterDieInfo();
                                                monsterDieInfo.attacker = message.reader().readShort();
                                                monsterDieInfo.attacked = message.reader().readShort();
                                                monsterDieInfo.skillID = message.reader().readByte();
                                                monsterDieInfo.hpLost = message.reader().readInt();
                                                monsterDieInfo.effect = message.reader().readByte();
                                                monsterDieInfo.itemDrop = new ItemDropInfo[message.reader().readByte()];
                                                Cout.println("SO LUONG ITEMDROP " + monsterDieInfo.itemDrop.length);
                                                while (i9 < monsterDieInfo.itemDrop.length) {
                                                    monsterDieInfo.itemDrop[i9] = new ItemDropInfo();
                                                    monsterDieInfo.itemDrop[i9].itemCatagory = message.reader().readByte();
                                                    String str = ((int) monsterDieInfo.itemDrop[i9].itemCatagory) + "_";
                                                    monsterDieInfo.itemDrop[i9].itemTemplateID = message.reader().readShort();
                                                    String str2 = str + ((int) monsterDieInfo.itemDrop[i9].itemTemplateID) + "_";
                                                    monsterDieInfo.itemDrop[i9].itemID = message.reader().readShort();
                                                    String str3 = str2 + ((int) monsterDieInfo.itemDrop[i9].itemID) + "_";
                                                    monsterDieInfo.itemDrop[i9].x = message.reader().readShort();
                                                    String str4 = str3 + ((int) monsterDieInfo.itemDrop[i9].x) + "_";
                                                    monsterDieInfo.itemDrop[i9].y = message.reader().readShort();
                                                    Cout.println(str4 + ((int) monsterDieInfo.itemDrop[i9].y) + "_");
                                                    i9++;
                                                }
                                                Cout.println("DOC XONG DATA ITEMDROP");
                                                if (message.reader().available() > 0) {
                                                    monsterDieInfo.x2 = message.reader().readByte();
                                                }
                                                if (message.reader().available() > 0) {
                                                    monsterDieInfo.buffAttack = message.reader().readByte();
                                                }
                                                if (message.reader().available() > 0) {
                                                    monsterDieInfo.level = message.reader().readByte();
                                                }
                                                GCanvas.gameScr.onMonsterDie(monsterDieInfo);
                                                return;
                                            } catch (Exception e3) {
                                                Cout.println("LOI TRONG HAM NHAN DATA ");
                                                e3.printStackTrace();
                                                return;
                                            }
                                        case 18:
                                            short s15 = message.reader().readShort();
                                            ItemInInventory itemInInventory6 = new ItemInInventory();
                                            byte b41 = message.reader().readByte();
                                            itemInInventory6.charClazz = b41;
                                            itemInInventory6.clazz = b41;
                                            itemInInventory6.itemOldID = message.reader().readShort();
                                            itemInInventory6.itemID = message.reader().readShort();
                                            itemInInventory6.idTemplate = message.reader().readShort();
                                            itemInInventory6.plusTemplate = message.reader().readByte();
                                            itemInInventory6.level = message.reader().readByte();
                                            itemInInventory6.durable = message.reader().readShort();
                                            itemInInventory6.mDurable = message.reader().readShort();
                                            GCanvas.gameScr.onGetItemFromGround(s15, itemInInventory6);
                                            return;
                                        case 19:
                                            GCanvas.gameScr.onGetPotionFromGround(message.reader().readShort(), message.reader().readShort(), (short) message.reader().readUnsignedByte(), message.reader().readShort());
                                            return;
                                        case 20:
                                            GCanvas.gameScr.onRemoveActor(message.reader().readShort(), message.reader().readByte());
                                            return;
                                        case 21:
                                            ItemInInventory itemInInventory7 = new ItemInInventory();
                                            itemInInventory7.itemID = message.reader().readShort();
                                            itemInInventory7.durable = message.reader().readShort();
                                            itemInInventory7.clazz = message.reader().readByte();
                                            itemInInventory7.dayUse = message.reader().readUnsignedShort();
                                            itemInInventory7.allAttribute.removeAllElements();
                                            byte b42 = message.reader().readByte();
                                            while (i9 < b42) {
                                                itemInInventory7.allAttribute.addElement(new InfoAttributeItem((short) message.reader().readUnsignedByte(), message.reader().readShort()));
                                                i9 = (byte) (i9 + 1);
                                            }
                                            itemInInventory7.plusTemplate = message.reader().readByte();
                                            try {
                                                itemInInventory7.lock = message.reader().readByte();
                                                break;
                                            } catch (Exception unused3) {
                                            }
                                            GCanvas.gameScr.onItemInfo(itemInInventory7);
                                            return;
                                        case 22:
                                            short s16 = message.reader().readShort();
                                            byte b43 = message.reader().readByte();
                                            short s17 = message.reader().readShort();
                                            int i45 = message.reader().readInt();
                                            try {
                                                i9 = message.reader().readByte();
                                                break;
                                            } catch (Exception unused4) {
                                            }
                                            GCanvas.gameScr.onUsePotion(s16, b43, s17, i45, i9);
                                            return;
                                        case 23:
                                            byte b44 = message.reader().readByte();
                                            if (b44 == 0) {
                                                int i46 = message.reader().readByte();
                                                byte[] bArr8 = new byte[i46];
                                                for (int i47 = 0; i47 < i46; i47++) {
                                                    bArr8[i47] = message.reader().readByte();
                                                }
                                                GCanvas.gameScr.onNPCSellPotion(bArr8);
                                            }
                                            if (b44 == 1) {
                                                byte b45 = message.reader().readByte();
                                                WindowInfoScr.gI().idClass = b45;
                                                Vector vector9 = new Vector();
                                                short s18 = message.reader().readShort();
                                                for (int i48 = 0; i48 < s18; i48++) {
                                                    ItemInInventory itemInInventory8 = new ItemInInventory();
                                                    itemInInventory8.isEnoughData = true;
                                                    itemInInventory8.isShopItem = true;
                                                    short s19 = message.reader().readShort();
                                                    if (b45 == -1) {
                                                        itemInInventory8.charClazz = (byte) getIndexClassFromItemID(s19);
                                                    } else {
                                                        itemInInventory8.charClazz = b45;
                                                    }
                                                    itemInInventory8.idTemplate = s19;
                                                    itemInInventory8.clazz = (byte) (b45 == -1 ? getIndexClassFromItemID(s19) : b45);
                                                    ItemTemplate item2 = Res.getItem(itemInInventory8.charClazz, s19);
                                                    itemInInventory8.level = item2.level;
                                                    itemInInventory8.durable = item2.durable;
                                                    vector9.addElement(itemInInventory8);
                                                }
                                                try {
                                                    ItemInInventory.typeItemBuy = (byte) 2;
                                                    ItemInInventory.typeItemBuy = message.reader().readByte();
                                                    break;
                                                } catch (Exception unused5) {
                                                }
                                                GCanvas.gameScr.onNPCSellItem(vector9);
                                            }
                                            if (b44 == 2) {
                                                try {
                                                    Vector vector10 = new Vector();
                                                    byte b46 = message.reader().readByte();
                                                    for (int i49 = 0; i49 < b46; i49++) {
                                                        ItemInInventory itemInInventory9 = new ItemInInventory();
                                                        byte b47 = message.reader().readByte();
                                                        itemInInventory9.itemID = message.reader().readShort();
                                                        short s20 = message.reader().readShort();
                                                        if (s20 < 0) {
                                                            s20 = (short) (s20 + 256);
                                                        }
                                                        itemInInventory9.charClazz = b47;
                                                        itemInInventory9.idTemplate = s20;
                                                        itemInInventory9.plusTemplate = message.reader().readByte();
                                                        itemInInventory9.level = message.reader().readByte();
                                                        itemInInventory9.mDurable = message.reader().readShort();
                                                        itemInInventory9.durable = message.reader().readShort();
                                                        itemInInventory9.clazz = b47;
                                                        vector10.addElement(itemInInventory9);
                                                    }
                                                    GCanvas.gameScr.onNPCKeepItem(vector10);
                                                } catch (Exception e4) {
                                                    e4.printStackTrace();
                                                    System.err.println("LOI NHAN ITEM TRONG KHO DO");
                                                }
                                                break;
                                            }
                                            if (b44 == 3) {
                                                GCanvas.gameScr.onNPCSellGemItem(null);
                                            }
                                            if (b44 == 4) {
                                                byte b48 = message.reader().readByte();
                                                byte[] bArr9 = new byte[b48 + 2];
                                                while (i9 < b48) {
                                                    bArr9[i9] = message.reader().readByte();
                                                    i9++;
                                                }
                                                bArr9[b48] = message.reader().readByte();
                                                bArr9[b48 + 1] = message.reader().readByte();
                                                GCanvas.gameScr.onListShopOfNPC(bArr9);
                                                return;
                                            }
                                            return;
                                        case 24:
                                            GCanvas.endDlg();
                                            return;
                                        case 25:
                                            int unsignedByte3 = message.reader().readUnsignedByte();
                                            ItemTemplate.ALL_NAME_ATTRIBUTE_ITEM.clear();
                                            for (int i50 = 0; i50 < unsignedByte3; i50++) {
                                                ItemTemplate.ALL_NAME_ATTRIBUTE_ITEM.put(i50 + "", new NameAttributeItem((short) message.reader().readUnsignedByte(), message.reader().readUTF(), message.reader().readByte(), message.reader().readByte()));
                                            }
                                            byte b49 = message.reader().readByte();
                                            for (int i51 = 0; i51 < b49; i51++) {
                                                Res.potionTemplates[i51] = new PotionTemplate(message.reader().readShort(), message.reader().readShort());
                                            }
                                            for (int i52 = 0; i52 < 5; i52++) {
                                                Res.percent[i52] = message.reader().readByte();
                                            }
                                            short s21 = message.reader().readShort();
                                            ItemTemplate[] itemTemplateArr = new ItemTemplate[s21 + 1];
                                            for (int i53 = 0; i53 < s21; i53++) {
                                                short s22 = message.reader().readShort();
                                                try {
                                                    ItemTemplate itemTemplate = new ItemTemplate();
                                                    itemTemplateArr[s22] = itemTemplate;
                                                    itemTemplate.id = s22;
                                                    itemTemplateArr[s22].name = message.reader().readUTF();
                                                    itemTemplateArr[s22].type = message.reader().readByte();
                                                    itemTemplateArr[s22].style = message.reader().readByte();
                                                    itemTemplateArr[s22].he = message.reader().readByte();
                                                    itemTemplateArr[s22].gender = message.reader().readByte();
                                                    itemTemplateArr[s22].level = message.reader().readByte();
                                                    itemTemplateArr[s22].durable = message.reader().readShort();
                                                    for (int i54 = 0; i54 < 10; i54++) {
                                                        itemTemplateArr[s22].attb[i54] = message.reader().readShort();
                                                    }
                                                    itemTemplateArr[s22].price = message.reader().readInt();
                                                    itemTemplateArr[s22].clazz = message.reader().readByte();
                                                    itemTemplateArr[s22].colorItem = message.reader().readByte();
                                                    itemTemplateArr[s22].idIcon = message.reader().readShort();
                                                    itemTemplateArr[s22].ndayLoan = message.reader().readShort();
                                                } catch (Exception e5) {
                                                    e5.printStackTrace();
                                                }
                                            }
                                            Res.itemTemplates.addElement(itemTemplateArr);
                                            short s23 = message.reader().readShort();
                                            Res.gemTemplate.removeAllElements();
                                            for (int i55 = 0; i55 < s23; i55++) {
                                                GemTemp gemTemp = new GemTemp();
                                                gemTemp.id = message.reader().readShort();
                                                gemTemp.idImage = message.reader().readByte();
                                                gemTemp.price = message.reader().readInt();
                                                gemTemp.name = message.reader().readUTF();
                                                String strSubstring = gemTemp.name.substring(gemTemp.name.length() - 1);
                                                gemTemp.decript = message.reader().readUTF();
                                                gemTemp.type = message.reader().readByte();
                                                try {
                                                    int i56 = Integer.parseInt(strSubstring);
                                                    if (i56 >= 4 && gemTemp.type != 4) {
                                                        gemTemp.isEff = (byte) (i56 - 4);
                                                    }
                                                } catch (Exception unused6) {
                                                }
                                                gemTemp.isSell = message.reader().readBoolean();
                                                gemTemp.typeEp = message.reader().readByte();
                                                gemTemp.typeMoney = message.reader().readByte();
                                                Res.gemTemplate.addElement(gemTemp);
                                            }
                                            int unsignedByte4 = message.reader().readUnsignedByte();
                                            Res.shopTemplate.removeAllElements();
                                            while (i9 < unsignedByte4) {
                                                GemTemp gemTemp2 = new GemTemp();
                                                gemTemp2.id = (short) message.reader().readUnsignedByte();
                                                gemTemp2.idImage = (short) message.reader().readUnsignedByte();
                                                gemTemp2.price = message.reader().readInt();
                                                gemTemp2.name = message.reader().readUTF();
                                                gemTemp2.decript = message.reader().readUTF();
                                                gemTemp2.typeMoney = message.reader().readByte();
                                                gemTemp2.shopType = message.reader().readByte();
                                                gemTemp2.isSell = message.reader().readBoolean();
                                                gemTemp2.value = message.reader().readShort();
                                                Res.shopTemplate.addElement(gemTemp2);
                                                i9++;
                                            }
                                            return;
                                        case 26:
                                            Res.monsterTemplates = new MonsterTemplate[message.reader().readUnsignedByte()];
                                            byte[] bArr10 = new byte[message.reader().readShort()];
                                            message.reader().read(bArr10);
                                            Res.setEffSh(bArr10);
                                            try {
                                                int unsignedByte5 = (short) message.reader().readUnsignedByte();
                                                byte[][] bArr11 = new byte[unsignedByte5][];
                                                for (int i57 = 0; i57 < unsignedByte5; i57++) {
                                                    bArr11[i57] = new byte[message.reader().readShort()];
                                                    message.reader().read(bArr11[i57]);
                                                }
                                                DynamicEffect.setDataEffect(bArr11);
                                            } catch (Exception e6) {
                                                e6.printStackTrace();
                                            }
                                            try {
                                                short unsignedByte6 = (short) message.reader().readUnsignedByte();
                                                for (int i58 = 0; i58 < unsignedByte6; i58++) {
                                                    short s24 = message.reader().readShort();
                                                    byte[] bArr12 = new byte[message.reader().readShort()];
                                                    message.reader().read(bArr12);
                                                    Pet.ALL_PET_DATA.put("" + ((int) s24), new mDataEffect(bArr12, s24, true));
                                                }
                                            } catch (Exception unused7) {
                                            }
                                            try {
                                                short s25 = message.reader().readShort();
                                                for (int i59 = 0; i59 < s25; i59++) {
                                                    short s26 = message.reader().readShort();
                                                    byte[] bArr13 = new byte[message.reader().readShort()];
                                                    message.reader().read(bArr13);
                                                    DataSkillEff.ALL_DATA_EFF.put(((int) s26) + "", new PartFrame(s26, bArr13));
                                                }
                                            } catch (Exception unused8) {
                                            }
                                            try {
                                                int i60 = message.reader().readByte();
                                                Char.dx_horse = new byte[i60][];
                                                Char.dy_horse = new byte[i60][];
                                                for (int i61 = 0; i61 < i60; i61++) {
                                                    Char.dx_horse[i61] = new byte[4];
                                                    Char.dy_horse[i61] = new byte[4];
                                                    for (int i62 = 0; i62 < 4; i62++) {
                                                        Char.dx_horse[i61][i62] = message.reader().readByte();
                                                    }
                                                    for (int i63 = 0; i63 < 4; i63++) {
                                                        Char.dy_horse[i61][i63] = message.reader().readByte();
                                                    }
                                                }
                                            } catch (Exception unused9) {
                                                Char.dx_horse = new byte[][]{new byte[]{0, 0, 7, -7}};
                                                Char.dy_horse = new byte[][]{new byte[]{-20, -15, -15, -15}};
                                            }
                                            try {
                                                for (int i64 = 0; i64 < 2; i64++) {
                                                    int i65 = message.reader().readByte();
                                                    Char.Dx_Dy_WP[i64] = new byte[i65];
                                                    for (int i66 = 0; i66 < i65; i66++) {
                                                        Char.Dx_Dy_WP[i64][i66] = message.reader().readByte();
                                                    }
                                                }
                                            } catch (Exception unused_Dx_Dy_WP) {
                                                Char.Dx_Dy_WP = new byte[][]{new byte[]{0}, new byte[]{0}};
                                            }
                                            try {
                                                for (int i64 = 0; i64 < 2; i64++) {
                                                    int i65 = message.reader().readByte();
                                                    Char.Dx_Dy_PP[i64] = new byte[i65];
                                                    for (int i66 = 0; i66 < i65; i66++) {
                                                        Char.Dx_Dy_PP[i64][i66] = message.reader().readByte();
                                                    }
                                                }
                                            } catch (Exception unused_Dx_Dy_PP) {
                                                Char.Dx_Dy_PP = new byte[][]{new byte[]{0}, new byte[]{0}};
                                            }
                                            try {
                                                for (int i64 = 0; i64 < 2; i64++) {
                                                    int i65 = message.reader().readByte();
                                                    Char.Dx_Dy_AVT[i64] = new byte[i65];
                                                    for (int i66 = 0; i66 < i65; i66++) {
                                                        Char.Dx_Dy_AVT[i64][i66] = message.reader().readByte();
                                                    }
                                                }
                                            } catch (Exception unused_Dx_Dy_AVT) {
                                                Char.Dx_Dy_AVT = new byte[][]{new byte[]{0}, new byte[]{0}};
                                            }
                                            return;
                                        case 27:
                                            GCanvas.gameScr.onChat(message.reader().readShort(), message.reader().readUTF());
                                            return;
                                        case 28:
                                            GCanvas.gameScr.onSellItem(message.reader().readShort());
                                            return;
                                        default:
                                            switch (b10) {
                                                case 32:
                                                    short s27 = message.reader().readShort();
                                                    int i67 = message.reader().readByte();
                                                    Property[] propertyArr = new Property[i67];
                                                    while (i9 < i67) {
                                                        Property property = new Property();
                                                        propertyArr[i9] = property;
                                                        property.key = message.reader().readUTF();
                                                        propertyArr[i9].value = message.reader().readInt();
                                                        i9++;
                                                    }
                                                    if (s27 == GCanvas.gameScr.mainChar.ID) {
                                                        GCanvas.gameScr.mainChar.level = message.reader().readByte();
                                                        GCanvas.gameScr.mainChar.lvpercent = message.reader().readShort();
                                                    }
                                                    GCanvas.gameScr.onCharProperties(s27, propertyArr);
                                                    break;
                                                case 33:
                                                    GCanvas.gameScr.onLevelUp(message.reader().readShort(), message.reader().readByte(), message.reader().readInt(), message.reader().readInt());
                                                    break;
                                                case 34:
                                                    GCanvas.endDlg();
                                                    break;
                                                case 35:
                                                    SkillManager.SKILL_DAM_PERCENT = (short[][][]) Array.newInstance((Class<?>) Short.TYPE, 1, 15, 11);
                                                    for (int i68 = 0; i68 < 15; i68++) {
                                                        for (int i69 = 0; i69 < 11; i69++) {
                                                            SkillManager.SKILL_DAM_PERCENT[0][i68][i69] = message.reader().readShort();
                                                        }
                                                    }
                                                    SkillManager.SKILL_COOLDOWN = (int[][][]) Array.newInstance((Class<?>) Integer.TYPE, 1, 15, 11);
                                                    for (int i70 = 0; i70 < 15; i70++) {
                                                        for (int i71 = 0; i71 < 11; i71++) {
                                                            SkillManager.SKILL_COOLDOWN[0][i70][i71] = message.reader().readShort() * 100;
                                                        }
                                                    }
                                                    SkillManager.SKILL_RANGE = (short[][]) Array.newInstance((Class<?>) Short.TYPE, 1, 15);
                                                    for (int i72 = 0; i72 < 15; i72++) {
                                                        SkillManager.SKILL_RANGE[0][i72] = message.reader().readShort();
                                                    }
                                                    SkillManager.SKILL_MP = (short[][][]) Array.newInstance((Class<?>) Short.TYPE, 1, 15, 11);
                                                    for (int i73 = 0; i73 < 15; i73++) {
                                                        for (int i74 = 0; i74 < 11; i74++) {
                                                            SkillManager.SKILL_MP[0][i73][i74] = (short) message.reader().readUnsignedByte();
                                                        }
                                                    }
                                                    SkillManager.TIME_LIFE_BUFF_SKILL = (short[][]) Array.newInstance((Class<?>) Short.TYPE, 15, 11);
                                                    for (int i75 = 0; i75 < 15; i75++) {
                                                        for (int i76 = 0; i76 < 11; i76++) {
                                                            SkillManager.TIME_LIFE_BUFF_SKILL[i75][i76] = message.reader().readShort();
                                                        }
                                                    }
                                                    SkillManager.LEVEL_ADD_SKILL = (byte[][]) Array.newInstance((Class<?>) Byte.TYPE, 15, 11);
                                                    for (int i77 = 0; i77 < 15; i77++) {
                                                        for (int i78 = 0; i78 < 11; i78++) {
                                                            SkillManager.LEVEL_ADD_SKILL[i77][i78] = message.reader().readByte();
                                                        }
                                                    }
                                                    SkillManager.SKILL_AEO = new byte[5][];
                                                    for (int i79 = 0; i79 < 5; i79++) {
                                                        SkillManager.SKILL_AEO[i79] = new byte[message.reader().readByte()];
                                                        for (int i80 = 0; i80 < SkillManager.SKILL_AEO[i79].length; i80++) {
                                                            SkillManager.SKILL_AEO[i79][i80] = message.reader().readByte();
                                                        }
                                                    }
                                                    break;
                                                case 36:
                                                    GCanvas.endDlg();
                                                    break;
                                                case 37:
                                                    String utf7 = message.reader().readUTF();
                                                    final String utf8 = message.reader().readUTF();
                                                    if (utf8.equals("")) {
                                                        GCanvas.startOKDlg(utf7);
                                                    } else {
                                                        GCanvas.gameScr.isNewVersionAvailable = true;
                                                        IAction iAction = new IAction() {
                                                            @Override
                                                            public void perform() {
                                                                try {
                                                                    GameMidlet.instance.platformRequest(utf8);
                                                                    Thread.sleep(500L);
                                                                } catch (Exception unused13) {
                                                                }
                                                                GameMidlet.instance.notifyDestroyed();
                                                            }
                                                        };
                                                        GCanvas.msgdlg.isIcon = false;
                                                        GCanvas.msgdlg.setInfo(utf7, new Command("OK", iAction), new Command("", iAction), new Command("Đóng", new IAction() {
                                                            @Override
                                                            public void perform() {
                                                                GCanvas.endDlg();
                                                            }
                                                        }));
                                                        GCanvas.currentDialog = GCanvas.msgdlg;
                                                    }
                                                    break;
                                                case 38:
                                                    GameScr.serverInfo.addElement(message.reader().readUTF());
                                                    break;
                                                case 39:
                                                    final String utf9 = message.reader().readUTF();
                                                    boolean z = message.reader().readBoolean();
                                                    final String utf10 = message.reader().readUTF();
                                                    final String utf11 = message.reader().readUTF();
                                                    if (z) {
                                                        GCanvas.startYesNoDlg("Bạn có muốn gửi tin nhắn không?", new IAction() {
                                                            @Override
                                                            public void perform() {
                                                                GCanvas.startWaitDlg();
                                                                GameMidlet.sendSMS(utf10 + utf9, "sms://" + utf11, new IAction() {
                                                                    @Override
                                                                    public void perform() {
                                                                        GCanvas.startOKDlg("Tài khoản " + utf9 + " với M.Khẩu: " + GCanvas.loginScr.tfPass.getText() + " đã được đăng ký.Vui lòng thoát game và chờ trong ít phút.");
                                                                    }
                                                                }, new IAction() {
                                                                    @Override
                                                                    public void perform() {
                                                                        GCanvas.startOKDlg("Có lỗi khi gửi tin nhắn đăng ký. Xin hãy thử lại");
                                                                    }
                                                                });
                                                            }
                                                        }, new IAction() {
                                                            @Override
                                                            public void perform() {
                                                                GCanvas.endDlg();
                                                            }
                                                        });
                                                    } else {
                                                        GCanvas.startOKDlg("Xin chọn nick khác");
                                                    }
                                                    break;
                                                default:
                                                    switch (b10) {
                                                        case 48:
                                                            GCanvas.gameScr.onCreadtePTOK(message.reader().readShort());
                                                            break;
                                                        case 49:
                                                            GCanvas.gameScr.onAdd2Party(message);
                                                            break;
                                                        case 50:
                                                            GCanvas.gameScr.onKickOutParty(message);
                                                            break;
                                                        case 51:
                                                            GCanvas.gameScr.onCharUseBuff(message);
                                                            break;
                                                        case 52:
                                                            GCanvas.gameScr.receiveQuest(message);
                                                            break;
                                                    }
                                                    break;
                                            }
                                            return;
                                    }
                            }
                    }
            }
        } catch (Exception unused13) {
        }
    }

    public static int getIndexClassFromItemID(int i) {
        if (i >= 79 && i <= 113) {
            return (i - 79) / 7;
        }
        if (i >= 174 && i <= 213) {
            return (i - 174) / 8;
        }
        if (i < 214 || i > 263) {
            return 0;
        }
        return (i - 214) / 10;
    }
}
