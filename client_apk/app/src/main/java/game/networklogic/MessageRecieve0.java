package game.networklogic;

import com.teamobi.kpah.GameMidlet;
import game.model.Char;
import game.model.IAction;
import game.model.ItemQuest;
import game.model.MainChar;
import game.model.MonsterInfo;
import game.model.Potion;
import game.model.ThanThu;
import game.model.Tilemap;
import game.model.XaphuTemplate;
import game.model.skill.InfoSkillLearn;
import game.render.GCanvas;
import game.render.Res;
import game.render.Util;
import game.render.screen.Font;
import game.render.screen.GameScr;
import game.render.screen.NqshMod;
import game.render.screen.RMS;
import game.render.screen.WindowInfoScr;
import java.io.IOException;
import java.util.Vector;
import network.Message;

public class MessageRecieve0 extends Cmd_message {
    public static void Message0(Message message) {
        short s;
        byte b;
        byte b2;
        byte b3;
        short s2;
        short s3;
        short s4;
        short s5;
        short s6;
        short s7;
        short s8;
        short s9;
        short s10;
        short s11;
        int i;
        byte b4;
        byte b5;
        byte b6;
        boolean z;
        short s12;
        byte b7;
        byte b8;
        byte b9;
        short s13;
        short s14;
        short s15;
        short s16;
        short s17;
        short s18;
        short s19;
        short s20;
        short s21;
        short s22;
        byte b10;
        try {
            new IAction() {
                @Override
                public void perform() {
                }
            };
            byte b11 = message.command;
            byte b12 = 0;
            byte b13 = 0;
            byte b14 = 0;
            byte b15 = 0;
            if (b11 == 1) {
                Char.char_speed = message.reader().readByte();
                if (Char.char_speed < 3) {
                    Char.char_speed = (byte) 3;
                }
                GameScr.pTicket = message.reader().readShort();
                Char.CHAR_POTION = (short) message.reader().readUnsignedByte();
                MainChar.listPotion = new Potion[Char.CHAR_POTION];
                GCanvas.gameScr.mainChar.potionLastTimeUse = new long[Char.CHAR_POTION];
                for (int i2 = 0; i2 < Char.CHAR_POTION; i2++) {
                    MainChar.listPotion[i2] = new Potion();
                    MainChar.listPotion[i2].index = (short) message.reader().readUnsignedByte();
                    MainChar.listPotion[i2].name = message.reader().readUTF();
                    MainChar.listPotion[i2].name2 = message.reader().readUTF();
                    MainChar.listPotion[i2].delay = message.reader().readShort();
                    MainChar.listPotion[i2].id = (short) i2;
                    MainChar.listPotion[i2].isTrade = message.reader().readBoolean();
                }
                for (int i3 = 0; i3 < Char.nSkill.length; i3++) {
                    Char.nSkill[i3] = message.reader().readByte();
                }
                GameScr.infoNewSkill.removeAllElements();
                for (int i4 = 0; i4 < 5; i4++) {
                    byte b16 = message.reader().readByte();
                    Vector vector = new Vector();
                    for (int i5 = 0; i5 < b16; i5++) {
                        InfoSkillLearn infoSkillLearn = new InfoSkillLearn();
                        infoSkillLearn.idSkill = message.reader().readByte();
                        infoSkillLearn.name = message.reader().readUTF();
                        infoSkillLearn.decript = message.reader().readUTF();
                        infoSkillLearn.price = message.reader().readInt();
                        infoSkillLearn.charClass = (byte) i4;
                        vector.addElement(infoSkillLearn);
                    }
                    GameScr.infoNewSkill.addElement(vector);
                }
                if (message.reader().available() > 0) {
                    GameScr.idMapPK = message.reader().readShort();
                    int unsignedByte = message.reader().readUnsignedByte();
                    for (int i6 = 0; i6 < unsignedByte; i6++) {
                        WindowInfoScr.gI().potionShop.addElement(message.reader().readUnsignedByte() + "");
                    }
                    for (int i7 = 0; i7 < 5; i7++) {
                        for (int i8 = 0; i8 < 5; i8++) {
                            Char.mainDef[i7][i8] = message.reader().readByte();
                            Char.mainAt[i7][i8] = message.reader().readByte();
                            Char.pcAttack[i7][i8] = message.reader().readByte();
                            Char.pcDef[i7][i8] = message.reader().readByte();
                        }
                    }
                }
                GameScr.xArena = message.reader().readByte();
                GameScr.yArena = message.reader().readByte();
                GameScr.wArena = message.reader().readByte();
                GameScr.hArena = message.reader().readByte();
                int i9 = message.reader().readByte();
                Res.khamProperty = new String[i9];
                Res.idNgocKham = new byte[i9];
                for (int i10 = 0; i10 < i9; i10++) {
                    Res.khamProperty[i10] = message.reader().readUTF();
                    Res.idNgocKham[i10] = message.reader().readByte();
                }
                String utf = message.reader().readUTF();
                if (!GameMidlet.numberSupport.equals(utf)) {
                    GameMidlet.numberSupport = utf;
                    RMS.saveRMSString("numbersupport", utf);
                }
                int i11 = message.reader().readByte();
                GameScr.TYPE_MP_HP[0] = new byte[i11];
                GameScr.TYPE_MP_HP[1] = new byte[i11];
                GameScr.VALUE_MP_HP[1] = new int[i11];
                GameScr.VALUE_MP_HP[0] = new int[i11];
                for (int i12 = 0; i12 < i11; i12++) {
                    GameScr.TYPE_MP_HP[0][i12] = message.reader().readByte();
                    GameScr.VALUE_MP_HP[0][i12] = message.reader().readInt();
                    GameScr.TYPE_MP_HP[1][i12] = message.reader().readByte();
                    GameScr.VALUE_MP_HP[1][i12] = message.reader().readInt();
                }
                int i13 = message.reader().readByte();
                ItemQuest.NAME_ITEM = new String[i13];
                ItemQuest.ICON_IMG = new byte[i13];
                for (int i14 = 0; i14 < i13; i14++) {
                    ItemQuest.ICON_IMG[i14] = message.reader().readByte();
                    ItemQuest.NAME_ITEM[i14] = message.reader().readUTF();
                }
                GameMidlet.url = NqshMod.suaLink(message.reader().readUTF()); // mod: link trỏ thẳng IP -> tên miền
                byte b17 = message.reader().readByte();
                for (int i15 = 0; i15 < b17; i15++) {
                    int i16 = message.reader().readShort();
                    byte[] bArr = new byte[i16];
                    for (int i17 = 0; i17 < i16; i17++) {
                        bArr[i17] = message.reader().readByte();
                    }
                    RMS.saveRMS(GCanvas.nameTile[i15], bArr);
                }
                try {
                    GameScr.paint18plush = message.reader().readByte();
                } catch (Exception unused) {
                }
                try {
                    ThanThu.ALL_DATA_THAN_THU.clear();
                    byte b18 = message.reader().readByte();
                    for (int i18 = 0; i18 < b18; i18++) {
                        byte[] bArr2 = new byte[message.reader().readShort()];
                        message.reader().read(bArr2);
                        ThanThu.setDataThanThu(i18, bArr2);
                    }
                } catch (Exception unused2) {
                }
                GCanvas.gameScr.onLoginSuccess();
                return;
            }
            if (b11 == 2) {
                String utf2 = message.reader().readUTF();
                if (utf2.startsWith("2")) {
                    int i19 = message.reader().readByte();
                    String[] strArr = new String[i19];
                    short[] sArr = new short[i19];
                    String[] strArr2 = new String[i19];
                    for (int i20 = 0; i20 < i19; i20++) {
                        strArr[i20] = message.reader().readUTF();
                        sArr[i20] = message.reader().readShort();
                        strArr2[i20] = message.reader().readUTF();
                    }
                    GCanvas.gameScr.listServer(utf2.substring(1), strArr, sArr, strArr2);
                    return;
                }
                if (utf2.startsWith("3")) {
                    String utf3 = message.reader().readUTF();
                    String utf4 = message.reader().readUTF();
                    GameMidlet.provider = utf3;
                    GameMidlet.agent = utf4;
                    RMS.saveRMSString("provider", utf3);
                    RMS.saveRMSString("agent", utf4);
                    return;
                }
                GCanvas.gameScr.onLoginFail(utf2);
                return;
            }
            byte[] bArr3 = null;
            byte b19 = -1;
            if (b11 == 3) {
                MainChar mainChar = GCanvas.gameScr.mainChar;
                try {
                    mainChar.ID = message.reader().readShort();
                    mainChar.name = message.reader().readUTF();
                    mainChar.wName = (short) Font.verySmallFont.getWidth(mainChar.name);
                    int i21 = message.reader().readInt();
                    mainChar.realHP = i21;
                    mainChar.hp = i21;
                    mainChar.maxhp = message.reader().readInt();
                    mainChar.mp = message.reader().readInt();
                    mainChar.maxmp = message.reader().readInt();
                    mainChar.currentHead = message.reader().readByte();
                    mainChar.clazz = message.reader().readByte();
                    mainChar.attack = message.reader().readInt();
                    mainChar.defend = message.reader().readInt();
                    mainChar.defend_magic = message.reader().readInt();
                    mainChar.accurate = message.reader().readShort();
                    mainChar.dodge = message.reader().readShort();
                    mainChar.critical = message.reader().readShort();
                    mainChar.he = message.reader().readByte();
                    mainChar.level = message.reader().readByte();
                    mainChar.lvpercent = message.reader().readShort();
                    mainChar.strength = message.reader().readShort();
                    mainChar.agility = message.reader().readShort();
                    mainChar.spirit = message.reader().readShort();
                    mainChar.health = message.reader().readShort();
                    mainChar.luck = message.reader().readShort();
                    mainChar.basePointLeft = message.reader().readShort();
                    mainChar.skillPointLeft = message.reader().readShort();
                    mainChar.dedicationPoint = message.reader().readInt();
                    mainChar.baokich = message.reader().readShort();
                    MainChar.skillLevelLearnt = new byte[message.reader().readByte()];
                    for (int i22 = 0; i22 < MainChar.skillLevelLearnt.length; i22++) {
                        MainChar.skillLevelLearnt[i22] = message.reader().readByte();
                    }
                    mainChar.killer = message.reader().readShort();
                    if (mainChar.killer > 0) {
                        mainChar.iskiller = true;
                    }
                    mainChar.gender = message.reader().readByte();
                    byte b20 = message.reader().readByte();
                    if (b20 >= 0) {
                        mainChar.useHorse = (byte) (b20 - 1);
                    }
                    mainChar.anmFly = message.reader().readByte();
                    mainChar.idImgHorse = message.reader().readByte();
                    mainChar.speed = message.reader().readByte();
                    mainChar.idClan = message.reader().readShort();
                    if (mainChar.idClan != -1) {
                        GCanvas.gameScr.mainChar.isMaster = message.reader().readByte();
                    }
                    if (GCanvas.gameScr.mainChar.isMaster == 0) {
                        MainChar.dissolved = message.reader().readBoolean();
                    }
                    mainChar.isNo = message.reader().readBoolean();
                    mainChar.limItemBag = message.reader().readByte();
                    mainChar.nationID = message.reader().readByte();
                    mainChar.inCountry = message.reader().readByte();
                    mainChar.lienTram = message.reader().readShort();
                    mainChar.congTrang = message.reader().readInt();
                    mainChar.hoatdong = message.reader().readShort();
                    mainChar.pArena = message.reader().readInt();
                    mainChar.nameHusband_wife = message.reader().readUTF();
                    mainChar.paintHat = message.reader().readBoolean();
                    try {
                        s4 = message.reader().readShort();
                        s5 = message.reader().readByte();
                    } catch (Exception unused3) {
                        s4 = -1;
                        s5 = -1;
                    }
                    GCanvas.gameScr.mainChar.setHorse(s4, s5);
                    try {
                        s6 = message.reader().readShort();
                        s7 = message.reader().readByte();
                        s8 = message.reader().readShort();
                    } catch (Exception unused4) {
                        s6 = -1;
                        s7 = -1;
                        s8 = -1;
                    }
                    GCanvas.gameScr.mainChar.setWeapon(s6, s7, s8);
                    try {
                        s9 = message.reader().readShort();
                        s10 = message.reader().readShort();
                        s11 = message.reader().readShort();
                    } catch (Exception unused5) {
                        s9 = -1;
                        s10 = -1;
                        s11 = -1;
                    }
                    GCanvas.gameScr.mainChar.setPartWearing(s9, s10, s11);
                    int i23 = message.reader().readByte();
                    short[] sArr2 = new short[i23];
                    byte[] bArr4 = new byte[i23];
                    for (int i24 = 0; i24 < i23; i24++) {
                        sArr2[i24] = message.reader().readShort();
                        bArr4[i24] = message.reader().readByte();
                    }
                    GCanvas.gameScr.mainChar.setEff(sArr2, bArr4);
                    String utf5 = message.reader().readUTF();
                    if (!utf5.equals("")) {
                        WindowInfoScr.infoMainCharServer = Util.split(utf5, "@");
                    } else {
                        WindowInfoScr.infoMainCharServer = null;
                    }
                } catch (Exception unused6) {
                }
                try {
                    s = message.reader().readShort();
                } catch (Exception unused7) {
                    s = -1;
                }
                GCanvas.gameScr.mainChar.idPhiPhong = s;
                try {
                    b3 = message.reader().readByte();
                    s2 = message.reader().readShort();
                    b = message.reader().readByte();
                    b2 = message.reader().readByte();
                } catch (Exception unused8) {
                    b = 0;
                    b2 = 0;
                    b3 = -1;
                    s2 = -1;
                }
                GCanvas.gameScr.mainChar.indexPP = b3;
                GCanvas.gameScr.mainChar.idImgPP = s2;
                GCanvas.gameScr.mainChar.nFrameWP = b;
                GCanvas.gameScr.mainChar.nFramePP = b2;
                try {
                    byte b21 = message.reader().readByte();
                    s3 = message.reader().readShort();
                    b12 = message.reader().readByte();
                    b19 = b21;
                } catch (Exception unused9) {
                    s3 = -1;
                }
                GCanvas.gameScr.mainChar.indexAVT = b19;
                GCanvas.gameScr.mainChar.idImgAVT = s3;
                GCanvas.gameScr.mainChar.nFrameAVT = b12;
                GCanvas.gameScr.onMainCharInfo();
                return;
            }
            if (b11 == 4) {
                while (message.reader().available() > 0) {
                    byte b22 = message.reader().readByte();
                    short unsignedByte2 = (short) message.reader().readUnsignedByte();
                    short s23 = message.reader().readShort();
                    short s24 = message.reader().readShort();
                    short s25 = message.reader().readShort();
                    byte b23 = message.reader().readByte();
                    try {
                        i = message.reader().readInt();
                    } catch (Exception unused10) {
                        i = -1;
                    }
                    if (b22 == 1) {
                        try {
                            b5 = message.reader().readByte();
                        } catch (Exception unused11) {
                            b5 = 0;
                        }
                    } else {
                        b5 = 0;
                    }
                    b4 = b5;
                    try {
                        b6 = message.reader().readByte();
                    } catch (Exception unused12) {
                        b6 = -1;
                    }
                    try {
                        z = message.reader().readBoolean();
                    } catch (Exception unused13) {
                        z = true;
                    }
                    GCanvas.gameScr.onActorMove(b22, unsignedByte2, s23, s24, s25, b23, i, b4, b6, z);
                }
                return;
            }
            if (b11 == 5) {
                if (GCanvas.gameScr.readyGetGameLogic) {
                    short s26 = message.reader().readShort();
                    Char r2 = (Char) GCanvas.gameScr.findCharByID(s26);
                    if (r2 != null) {
                        r2.ID = s26;
                        if (r2.idBoss != -1) {
                            r2.dir = (short) 0;
                        }
                        r2.name = message.reader().readUTF();
                        r2.x = message.reader().readShort();
                        r2.y = message.reader().readShort();
                        r2.hp = message.reader().readInt();
                        r2.maxhp = message.reader().readInt();
                        r2.mp = message.reader().readInt();
                        r2.maxmp = message.reader().readInt();
                        r2.currentHead = message.reader().readByte();
                        r2.clazz = message.reader().readByte();
                        message.reader().read(r2.buffType, 0, r2.buffType.length);
                        for (int i25 = 0; i25 < r2.buffType.length; i25++) {
                            r2.countDown[i25] = message.reader().readShort();
                        }
                        r2.killer = message.reader().readShort();
                        r2.pk = message.reader().readByte();
                        r2.defend = message.reader().readShort();
                        r2.defend_magic = message.reader().readShort();
                        r2.level = message.reader().readByte();
                        r2.he = message.reader().readByte();
                        byte b24 = message.reader().readByte();
                        if (b24 >= 0) {
                            r2.useHorse = (byte) (b24 - 1);
                        }
                        r2.anmFly = message.reader().readByte();
                        r2.idImgHorse = message.reader().readByte();
                        r2.speed = message.reader().readByte();
                        r2.idClan = message.reader().readShort();
                        r2.idBoss = message.reader().readByte();
                        if (r2.idClan != -1) {
                            r2.isMaster = message.reader().readByte();
                        }
                        r2.idFashion = message.reader().readByte();
                        for (int i26 = 0; i26 < r2.idModel.length; i26++) {
                            r2.idModel[i26] = message.reader().readShort();
                        }
                        r2.isNo = message.reader().readBoolean();
                        r2.setCharInfo();
                        GCanvas.gameScr.onCharInfo(r2);
                        if (message.reader().readBoolean()) {
                            GCanvas.gameScr.gameService.requestCharMonter(r2.ID);
                        }
                        r2.nationID = message.reader().readByte();
                        r2.inCountry = message.reader().readByte();
                        r2.paintHat = message.reader().readBoolean();
                        try {
                            s15 = message.reader().readShort();
                            s16 = message.reader().readByte();
                        } catch (Exception unused14) {
                            s15 = -1;
                            s16 = -1;
                        }
                        r2.setHorse(s15, s16);
                        try {
                            s17 = message.reader().readShort();
                            s18 = message.reader().readByte();
                            s19 = message.reader().readShort();
                        } catch (Exception unused15) {
                            s17 = -1;
                            s18 = -1;
                            s19 = -1;
                        }
                        r2.setWeapon(s17, s18, s19);
                        try {
                            s20 = message.reader().readShort();
                            s21 = message.reader().readShort();
                            s22 = message.reader().readShort();
                        } catch (Exception unused16) {
                            s20 = -1;
                            s21 = -1;
                            s22 = -1;
                        }
                        r2.setPartWearing(s20, s21, s22);
                        try {
                            int i27 = message.reader().readByte();
                            short[] sArr3 = new short[i27];
                            byte[] bArr5 = new byte[i27];
                            for (int i28 = 0; i28 < i27; i28++) {
                                sArr3[i28] = message.reader().readShort();
                                bArr5[i28] = message.reader().readByte();
                            }
                            r2.setEff(sArr3, bArr5);
                        } catch (Exception unused17) {
                        }
                    }
                    try {
                        s12 = message.reader().readShort();
                    } catch (Exception unused18) {
                        s12 = -1;
                    }
                    r2.idPhiPhong = s12;
                    try {
                        b9 = message.reader().readByte();
                        s13 = message.reader().readShort();
                        b7 = message.reader().readByte();
                        b8 = message.reader().readByte();
                    } catch (Exception unused19) {
                        b7 = 0;
                        b8 = 0;
                        b9 = -1;
                        s13 = -1;
                    }
                    r2.indexPP = b9;
                    r2.idImgPP = s13;
                    r2.nFrameWP = b7;
                    r2.nFramePP = b8;
                    try {
                        byte b25 = message.reader().readByte();
                        s14 = message.reader().readShort();
                        b13 = message.reader().readByte();
                        b19 = b25;
                    } catch (Exception unused20) {
                        s14 = -1;
                    }
                    r2.indexAVT = b19;
                    r2.idImgAVT = s14;
                    r2.nFrameAVT = b13;
                    return;
                }
                return;
            }
            if (b11 != 6) {
                if (b11 != 7) {
                    if (b11 == 76) {
                        GCanvas.gameScr.onSpecialItem(message);
                        return;
                    }
                    if (b11 == 77) {
                        GCanvas.gameScr.onImbueOK(message.reader().readUTF());
                        return;
                    }
                    if (b11 == 85) {
                        GCanvas.startOKDlg("Đã mua, món đồ đang ở trong hành trang.");
                        return;
                    }
                    if (b11 == 86) {
                        byte b26 = message.reader().readByte();
                        if (b26 == 1) {
                            GCanvas.gameScr.mainChar.timeUseGolgTicket = System.currentTimeMillis();
                            Char.idGoldTicket = message.reader().readByte() - 1;
                            Char.goldTimeMinute = 1440;
                            Char.goldTime = message.reader().readUTF();
                            return;
                        }
                        if (b26 == 0) {
                            GCanvas.gameScr.mainChar.timeUseGolgTicket = 0L;
                            Char.goldTimeMinute = 0;
                            Char.goldTime = "";
                            return;
                        } else {
                            if (b26 == 2) {
                                Char.idGoldTicket = message.reader().readByte() - 1;
                                GCanvas.gameScr.mainChar.timeUseGolgTicket = System.currentTimeMillis();
                                MainChar.goldTimeMinute = message.reader().readInt();
                                Char.goldTime = message.reader().readUTF();
                                return;
                            }
                            return;
                        }
                    }
                    if (b11 == 89) {
                        GCanvas.gameScr.onBuffAttack(message);
                        return;
                    }
                    if (b11 == 90) {
                        GCanvas.gameScr.onActorDie(message);
                        return;
                    }
                    if (b11 == 94) {
                        GCanvas.gameScr.onListInfoDepositeItem(message);
                        return;
                    }
                    if (b11 == 95) {
                        GCanvas.gameScr.onBuyItemOfUser(message.reader().readShort(), message.reader().readShort(), message.reader().readByte());
                        return;
                    }
                    switch (b11) {
                        case -36:
                            if (message.reader().readByte() == 0) {
                                GCanvas.gameScr.onEpNgoc(message.reader().readShort());
                            } else {
                                GCanvas.gameScr.onImbueOK(message.reader().readUTF());
                            }
                            break;
                        case -35:
                            GCanvas.gameScr.onImbueOK(message.reader().readUTF());
                            break;
                        case -34:
                            short s27 = message.reader().readShort();
                            try {
                                b14 = message.reader().readByte();
                                break;
                            } catch (Exception unused21) {
                            }
                            GCanvas.gameScr.onKham(s27, b14);
                            break;
                        default:
                            switch (b11) {
                                case 7:
                                    break;
                                case 8:
                                    while (message.reader().available() > 0) {
                                        GCanvas.gameScr.charOutGame(message.reader().readShort());
                                    }
                                    break;
                                case 9:
                                    GCanvas.gameScr.onPlayerAttackMonster(message.reader().readShort(), message.reader().readShort(), message.reader().readByte(), message.reader().readInt(), message.reader().readInt(), message.reader().readByte(), message.reader().readByte(), message.reader().readByte(), message.reader().readByte());
                                    break;
                                case 10:
                                    GCanvas.gameScr.onMonsterAttackPlayer(message.reader().readShort(), message.reader().readShort(), message.reader().readInt(), message.reader().readInt());
                                    break;
                                case 12:
                                    short s28 = message.reader().readShort();
                                    short s29 = message.reader().readShort();
                                    short s30 = message.reader().readShort();
                                    try {
                                        b10 = message.reader().readByte();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        b10 = -1;
                                    }
                                    short s31 = message.reader().readShort();
                                    String utf6 = message.reader().readUTF();
                                    try {
                                        if (message.reader().readBoolean()) {
                                            int iAvailable = message.reader().available();
                                            bArr3 = new byte[iAvailable];
                                            int i29 = 0;
                                            int i30 = 0;
                                            while (i29 != -1 && i30 < iAvailable) {
                                                i29 = message.reader().read(bArr3, 0, iAvailable - i30);
                                                i30 += i29;
                                            }
                                        }
                                    } catch (Exception unused22) {
                                    }
                                    GCanvas.gameScr.onMap(s28, s29, s30, s31, utf6, bArr3);
                                    Tilemap.idXaphu = b10;
                                    break;
                                case 60:
                                    GCanvas.gameScr.onNewHPMP(message);
                                    break;
                                case 92:
                                    GCanvas.gameScr.onUserSellItem(message);
                                    break;
                                case 106:
                                    GCanvas.gameScr.onAttackMultiTarget(message);
                                    break;
                                default:
                                    switch (b11) {
                                        case 65:
                                            GCanvas.gameScr.onUseItemPK(message.reader().readShort(), message.reader().readByte(), message.reader().readByte());
                                            break;
                                        case 66:
                                            byte b27 = message.reader().readByte();
                                            if (b27 == 0) {
                                                GCanvas.gameScr.onRequestTrade(message.reader().readShort());
                                            } else if (b27 == -2) {
                                                GCanvas.startOKDlg("Người bạn mời đang trao đổi với người khác");
                                            } else if (b27 == 1) {
                                                GCanvas.gameScr.onOkTrade(message.reader().readShort());
                                            } else if (b27 == -1) {
                                                GCanvas.gameScr.onDeniedTrade(message.reader().readShort());
                                            } else if (b27 == 2) {
                                                GCanvas.gameScr.onItemTrade(message.reader().readShort(), message, message.reader().readByte());
                                            } else if (b27 == 3) {
                                                GCanvas.gameScr.oncancelTrade();
                                            } else if (b27 == 4) {
                                                GCanvas.gameScr.onFinishTrade();
                                            } else if (b27 == 5) {
                                                GCanvas.gameScr.onConfirmTrade();
                                            }
                                            break;
                                        case 67:
                                            GCanvas.gameScr.onKiller(message.reader().readShort(), message.reader().readByte(), message.reader().readShort());
                                            break;
                                        case 68:
                                            GCanvas.gameScr.onPutItem2Bag(message.reader().readShort());
                                            GCanvas.endDlg();
                                            break;
                                        case 69:
                                            GCanvas.gameScr.onGetItemFromBag(message.reader().readShort());
                                            GCanvas.endDlg();
                                            break;
                                        default:
                                            switch (b11) {
                                                case 71:
                                                    message.reader().readByte();
                                                    short s32 = message.reader().readShort();
                                                    try {
                                                        b15 = message.reader().readByte();
                                                        break;
                                                    } catch (Exception unused23) {
                                                    }
                                                    GCanvas.gameScr.onImbue(s32, b15);
                                                    break;
                                                case 72:
                                                    GCanvas.startOKDlg("Đã sửa xong.");
                                                    break;
                                                case 73:
                                                    GCanvas.gameScr.onGemItem(message);
                                                    break;
                                                default:
                                                    switch (b11) {
                                                        case 80:
                                                            Res.XAPHU = new XaphuTemplate[message.reader().readByte()];
                                                            for (int i31 = 0; i31 < Res.XAPHU.length; i31++) {
                                                                Res.XAPHU[i31] = new XaphuTemplate();
                                                                int i32 = message.reader().readByte();
                                                                Res.XAPHU[i31].mapID = new int[i32];
                                                                for (int i33 = 0; i33 < Res.XAPHU[i31].mapID.length; i33++) {
                                                                    Res.XAPHU[i31].mapID[i33] = message.reader().readByte();
                                                                }
                                                                Res.XAPHU[i31].x = new int[i32];
                                                                for (int i34 = 0; i34 < Res.XAPHU[i31].x.length; i34++) {
                                                                    Res.XAPHU[i31].x[i34] = message.reader().readShort();
                                                                }
                                                                Res.XAPHU[i31].y = new int[i32];
                                                                for (int i35 = 0; i35 < Res.XAPHU[i31].y.length; i35++) {
                                                                    Res.XAPHU[i31].y[i35] = message.reader().readShort();
                                                                }
                                                                Res.XAPHU[i31].price = new int[i32];
                                                                for (int i36 = 0; i36 < Res.XAPHU[i31].price.length; i36++) {
                                                                    Res.XAPHU[i31].price[i36] = message.reader().readShort();
                                                                }
                                                            }
                                                            break;
                                                        case 81:
                                                            GCanvas.startOKDlg("Đã mua được vé");
                                                            break;
                                                        case 82:
                                                            GCanvas.currentDialog = null;
                                                            if (GCanvas.gameScr.mainChar.delay <= 0) {
                                                                GCanvas.gameScr.mainChar.delay = message.reader().readByte();
                                                                GCanvas.gameScr.mainChar.realTime = GCanvas.gameScr.mainChar.delay;
                                                                GCanvas.gameScr.mainChar.timeWait2Board = System.currentTimeMillis();
                                                            }
                                                            break;
                                                        default:
                                                            switch (b11) {
                                                                case 101:
                                                                    GCanvas.gameScr.onInfoFriend(message);
                                                                    break;
                                                                case 102:
                                                                    GCanvas.gameScr.onFriendList(message, 0, "BẠN BÈ");
                                                                    break;
                                                                case 103:
                                                                    GCanvas.gameScr.onSMSStruct(message);
                                                                    break;
                                                                case 104:
                                                                    GameScr.loadConfig(message.reader().readByte());
                                                                    message.reader().readByte();
                                                                    break;
                                                            }
                                                            break;
                                                    }
                                                    break;
                                            }
                                            break;
                                    }
                                    break;
                            }
                            break;
                    }
                    return;
                }
                MonsterInfo monsterInfo = new MonsterInfo();
                monsterInfo.id = message.reader().readShort();
                monsterInfo.monster_type = (short) message.reader().readUnsignedByte();
                monsterInfo.x = message.reader().readShort();
                monsterInfo.y = message.reader().readShort();
                monsterInfo.hp = message.reader().readInt();
                monsterInfo.lv = message.reader().readByte();
                monsterInfo.he = message.reader().readByte();
                monsterInfo.maxhp = message.reader().readInt();
                monsterInfo.timeLive = message.reader().readInt();
                GCanvas.gameScr.onMonsterInfo(monsterInfo);
                return;
            }
            GCanvas.gameScr.onPlayerAttackPlayer(message.reader().readShort(), message.reader().readShort(), message.reader().readByte(), message.reader().readInt(), message.reader().readInt(), message.reader().readByte(), message.reader().readByte(), message.reader().readByte(), message.reader().readByte());
        } catch (Exception unused24) {
        }
    }
}
