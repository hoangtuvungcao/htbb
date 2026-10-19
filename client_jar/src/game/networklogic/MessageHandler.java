package game.networklogic;

import effect.DataSkillEff;
import effect.DynamicEffect;
import effect.PartFrame;
import effect.mDataEffect;
import game.GameMidlet;
import game.model.Actor;
import game.model.AnimalInfo;
import game.model.AnimalMove;
import game.model.Char;
import game.model.ChatInfo;
import game.model.ClanInfo;
import game.model.Command;
import game.model.Cout;
import game.model.DynamicObj;
import game.model.EffectData;
import game.model.EffectManager;
import game.model.EffectServerManager;
import game.model.GemTemp;
import game.model.Ground;
import game.model.IAction;
import game.model.ImageIcon;
import game.model.InfoAttributeItem;
import game.model.IsAnimal;
import game.model.ItemInInventory;
import game.model.ItemQuest;
import game.model.ItemTemplate;
import game.model.MainChar;
import game.model.Mineral;
import game.model.MonsterInfo;
import game.model.MonsterTemplate;
import game.model.NPC;
import game.model.NameAttributeItem;
import game.model.Position;
import game.model.Potion;
import game.model.PotionTemplate;
import game.model.Property;
import game.model.SkillClan;
import game.model.Sound;
import game.model.PokemonHuyenThoai;
import game.model.Tilemap;
import game.model.TreeInfo;
import game.model.WayPoint;
import game.model.PidgeotBayTemplate;
import game.model.monster.Pet;
import game.model.skill.InfoSkillLearn;
import game.model.skill.SkillManager;
import game.render.Canvas;
import game.render.Res;
import game.render.Util;
import game.render.screen.AutoGuard;
import game.render.screen.CharSelectScr;
import game.render.screen.CreateCharScr;
import game.render.screen.Font;
import game.render.screen.GameData;
import game.render.screen.GameScr;
import game.render.screen.HelpScr;
import game.render.screen.ListScr;
import game.render.screen.LoginScr;
import game.render.screen.MTickets;
import game.render.screen.MessageScr;
import game.render.screen.MiniGame;
import game.render.screen.PauseMenu;
import game.render.screen.RMS;
import game.render.screen.ViewInfoScr;
import game.render.screen.WindowInfoScr;
import java.util.Vector;
import javax.microedition.lcdui.Image;
import network.IMessageHandler;
import network.Message;

public class MessageHandler extends Cmd_message implements IMessageHandler {
    protected static MessageHandler instance;

    public static MessageHandler gI() {
        if (instance == null) {
            instance = new MessageHandler();
        }
        return instance;
    }

    @Override
    public void onConnectOK() {
        Canvas.gameScr.onConnectOK();
    }

    @Override
    public void onConnectionFail() {
        Canvas.gameScr.onConnectFail();
    }

    @Override
    public void onDisconnected() {
        Canvas.gameScr.onDisconnect();
    }

    @Override
    public void onMessage(Message msg) {
        // mod: gói -100 Tự động đánh (TuDong)
        if (AutoGuard.goiGuard(msg)) {
            return;
        }
        // pokemon: lenh -90..-94 (than, quai, tien hoa, thu cuoi, pet)
        if (game.pokemon.PokeMang.xuLy(msg)) {
            return;
        }
        // pokemon: lenh -101 hop thoai guc / hoi sinh (HopHoiSinh)
        if (game.pokemon.HopHoiSinh.nhan(msg)) {
            return;
        }
        // pokemon: lenh -104 ket qua dung vat pham (DungDo, thong bao nhanh)
        if (game.pokemon.DungDo.nhan(msg)) {
            return;
        }
        // pokemon: lenh -110 danh hieu (bang, nhan tren dau, so moi) - DanhHieuKho
        if (game.pokemon.DanhHieuKho.nhan(msg)) {
            return;
        }
        // pokemon: lenh -118 thoi tiet / phu mau mua / ngay cong dong (ThoiTiet, 0.9.7)
        if (msg.command == game.pokemon.ThoiTiet.LENH && game.pokemon.ThoiTiet.nhan(msg)) {
            return;
        }
        // pokemon: lenh -105 trang thai server sau moi thao tac vat pham (kho trang thai client)
        if (game.pokemon.TrangThaiNguoiChoi.nhan(msg)) {
            return;
        }
        int i;
        int i2;
        int i3;
        int j;
        int i4;
        int j2;
        int cid;
        short idBigAvtAo;
        byte indexXyAo;
        byte nFrameAo;
        short idBigAvtPP;
        byte indexXyPP;
        byte nFramePP;
        short idBigAvtVK;
        byte indexXyVK;
        byte nFrameVK;
        short idhorse2;
        short typehorse2;
        short idwp2;
        short typewp2;
        short idimgwp2;
        short idHair;
        short idBody;
        short idLeg;
        short idPP1;
        byte indexpp2;
        short idImgpp2;
        byte nframewp2;
        byte nframepp2;
        byte indexAVT2;
        short idBigavt2;
        byte nframeAVT2;
        byte indexpp;
        short idImgpp;
        byte nframewp;
        byte nframepp;
        byte indexAVT;
        short idBigavt;
        byte nframeAVT;
        short idhorse;
        short typehorse;
        short idwp;
        short typewp;
        short idimgwp;
        short idHair2;
        short idBody2;
        short idLeg2;
        short idPP;
        try {
            switch (msg.command) {
                case -76:
                    Canvas.gameScr.onShopNew(msg);
                    break;
                case -75:
                    Canvas.gameScr.onCountDown(msg);
                    break;
                case -74:
                    Canvas.gameScr.onEffData(msg);
                    break;
                case -73:
                    Canvas.gameScr.onPetAttack(msg);
                    break;
                case -72:
                case -58:
                case -54:
                case -45:
                case -44:
                case -40:
                case -25:
                case -23:
                case -21:
                case -16:
                case -15:
                case -14:
                case -6:
                case -4:
                case -3:
                case -2:
                case -1:
                case 0:
                case 29:
                case 31:
                case 40:
                case 41:
                case 42:
                case 43:
                case 44:
                case 45:
                case 46:
                case 47:
                case 53:
                case 55:
                case 63:
                case 70:
                case 74:
                case 75:
                case 79:
                case 84:
                case 88:
                case 93:
                case 96:
                case 97:
                case 98:
                case 99:
                default:
                    break;
                case -71:
                    Canvas.gameScr.onDynamicEffect(msg);
                    break;
                case -70:
                    Canvas.gameScr.onNewEffect(msg);
                    break;
                case -69:
                    Canvas.gameScr.onAutoImbue(msg);
                    break;
                case -68:
                    Canvas.gameScr.onTachNguyenLieu(msg);
                    break;
                case -67:
                    Canvas.gameScr.onTuBinh(msg);
                    break;
                case -66:
                    Canvas.gameScr.onFruit(msg);
                    break;
                case -65:
                    try {
                        Cout.println("NHAT ITEM NHIEM VU CHO NAY MESSAGEHANDLER");
                        short whoGet4 = msg.reader().readShort();
                        short itemQuestID = msg.reader().readShort();
                        byte itemQuestType = msg.reader().readByte();
                        Canvas.gameScr.onGetItemQuestFromGround(whoGet4, itemQuestID, itemQuestType);
                    } catch (Exception e) {
                        Cout.println("LOI __NHAT ITEM NHIEM VU CHO NAY MESSAGEHANDLER");
                        return;
                    }
                    break;
                case -64:
                    Canvas.gameScr.onQuest(msg);
                    break;
                case -63:
                    MTickets.gi().onHappy(msg);
                    MTickets.gi().switchToMe();
                    break;
                case -62:
                    byte typeMinigame = msg.reader().readByte();
                    if (typeMinigame == 0) {
                        MiniGame.gI().switchToMe();
                        MiniGame.gI().onMiniGame(msg);
                    } else if (typeMinigame == 1) {
                        byte idBox = msg.reader().readByte();
                        String infoBox = msg.reader().readUTF();
                        int i5 = msg.reader().readByte();
                        String[] updateInfo = new String[i5];
                        for (byte ii = 0; ii < i5; ii = (byte) (ii + 1)) {
                            updateInfo[ii] = msg.reader().readUTF();
                        }
                        MiniGame.gI().getInfoBox(idBox, infoBox, updateInfo);
                    } else if (typeMinigame == 2) {
                        Canvas.startOKDlg("Bạn đã hết lượt chơi");
                        MiniGame.gI().reSetGame();
                        Canvas.gameScr.switchToMe();
                    }
                    break;
                case -61:
                    byte idColor = msg.reader().readByte();
                    byte lv = msg.reader().readByte();
                    byte slNguyenLieu = msg.reader().readByte();
                    short startIDNL = msg.reader().readShort();
                    byte magic = msg.reader().readByte();
                    byte khoa1 = 0;
                    try {
                        khoa1 = msg.reader().readByte();   // pokemon: bản dịch ngược có "break" ở đây -> có byte tuỳ chọn thì bỏ qua hàm xử lý (Unity không break)
                    } catch (Exception e2) {
                    }
                    Canvas.gameScr.onEpDothu((short) 1000, idColor, lv, startIDNL, slNguyenLieu, magic, khoa1);
                    break;
                case -60:
                    Canvas.gameScr.addMsgWorld(msg.reader().readUTF());
                    break;
                case -59:
                    byte nfarm = msg.reader().readByte();
                    for (byte k = 0; k < nfarm; k = (byte) (k + 1)) {
                        byte idFarm = msg.reader().readByte();
                        byte idTree = msg.reader().readByte();
                        String name = msg.reader().readUTF();
                        byte idImgTree = msg.reader().readByte();
                        int timelive = msg.reader().readInt();
                        byte x = msg.reader().readByte();
                        byte y = msg.reader().readByte();
                        boolean isBuyOk = msg.reader().readBoolean();
                        byte addy = msg.reader().readByte();
                        byte colorString = msg.reader().readByte();
                        Canvas.gameScr.setMyFarm(idFarm, idTree, x * 16, y * 16, name, name, idImgTree, timelive, isBuyOk, addy, colorString);
                    }
                    Canvas.gameScr.xBeginFrame = (short) getMinMax(Canvas.gameScr.actors, (byte) 0, 5000);
                    Canvas.gameScr.yBeginFrame = (short) getMinMax(Canvas.gameScr.actors, (byte) 1, 5000);
                    Canvas.gameScr.xTheendFrame = (short) getMinMax(Canvas.gameScr.actors, (byte) 2, -1);
                    Canvas.gameScr.yTheendFrame = (short) getMinMax(Canvas.gameScr.actors, (byte) 3, -1);
                    break;
                case -57:
                    byte yesnoMonter = msg.reader().readByte();
                    if (yesnoMonter == 0) {
                        boolean isMonter = msg.reader().readBoolean();
                        short idCharTrans = msg.reader().readShort();
                        byte idMonter = msg.reader().readByte();
                        if (Canvas.gameScr.mainChar.ID == idCharTrans) {
                            Canvas.gameScr.mainChar.isAnimal = isMonter;
                            if (isMonter) {
                                Canvas.gameScr.mainChar.myAnimal = new IsAnimal();
                                Canvas.gameScr.mainChar.myAnimal.idMonster = idMonter;
                                Canvas.gameScr.mainChar.myAnimal.type = idMonter;
                                Canvas.gameScr.mainChar.effAnimal = new WayPoint(Canvas.gameScr.mainChar.x, Canvas.gameScr.mainChar.y - 5, (short) 1, (byte) 1, false);
                                Canvas.gameScr.mainChar.timeRemoEffanimal = (System.currentTimeMillis() / 1000) + 5;
                            } else if (Canvas.gameScr.mainChar.myAnimal != null) {
                                Canvas.gameScr.mainChar.myAnimal = null;
                            }
                        } else {
                            Char isAnimal = (Char) Canvas.gameScr.findCharByID(idCharTrans);
                            if (isAnimal != null) {
                                isAnimal.isAnimal = isMonter;
                                if (isMonter) {
                                    isAnimal.myAnimal = new IsAnimal();
                                    isAnimal.myAnimal.idMonster = idMonter;
                                    isAnimal.myAnimal.type = idMonter;
                                    isAnimal.effAnimal = new WayPoint(isAnimal.x, isAnimal.y - 5, (short) 1, (byte) 1, false);
                                    isAnimal.timeRemoEffanimal = (System.currentTimeMillis() / 1000) + 5;
                                } else if (isAnimal.myAnimal != null) {
                                    isAnimal.myAnimal = null;
                                }
                            }
                        }
                        if (IsAnimal.img[idMonter] == null) {
                            Canvas.gameScr.gameService.sendGetImgIsanimal(idMonter, idCharTrans);
                        }
                    } else {
                        byte idmonster = msg.reader().readByte();
                        int lenArrImg = msg.reader().readShort();
                        byte[] byteImage = new byte[lenArrImg];
                        msg.reader().read(byteImage, 0, lenArrImg);
                        IsAnimal.hImg[idmonster] = msg.reader().readByte();
                        IsAnimal.img[idmonster] = Res.createImgByByteArray(byteImage);
                        IsAnimal.wimg[idmonster] = IsAnimal.img[idmonster].getWidth();
                        IsAnimal.frameRun[idmonster] = new byte[4][msg.reader().readByte()];
                        for (int j3 = 0; j3 < IsAnimal.frameRun[idmonster][0].length; j3++) {
                            IsAnimal.frameRun[idmonster][0][j3] = msg.reader().readByte();
                            IsAnimal.frameRun[idmonster][1][j3] = msg.reader().readByte();
                            IsAnimal.frameRun[idmonster][2][j3] = msg.reader().readByte();
                            IsAnimal.frameRun[idmonster][3][j3] = msg.reader().readByte();
                        }
                        IsAnimal.frameStand[idmonster] = new byte[4][msg.reader().readByte()];
                        for (int j4 = 0; j4 < IsAnimal.frameStand[idmonster][0].length; j4++) {
                            IsAnimal.frameStand[idmonster][0][j4] = msg.reader().readByte();
                            IsAnimal.frameStand[idmonster][1][j4] = msg.reader().readByte();
                            IsAnimal.frameStand[idmonster][2][j4] = msg.reader().readByte();
                            IsAnimal.frameStand[idmonster][3][j4] = msg.reader().readByte();
                        }
                        IsAnimal.frameAt[idmonster] = new byte[4][msg.reader().readByte()];
                        for (int j5 = 0; j5 < IsAnimal.frameAt[idmonster][0].length; j5++) {
                            IsAnimal.frameAt[idmonster][0][j5] = msg.reader().readByte();
                            IsAnimal.frameAt[idmonster][1][j5] = msg.reader().readByte();
                            IsAnimal.frameAt[idmonster][2][j5] = msg.reader().readByte();
                            IsAnimal.frameAt[idmonster][3][j5] = msg.reader().readByte();
                        }
                        IsAnimal.addx[idmonster] = msg.reader().readByte();
                        IsAnimal.addy[idmonster] = msg.reader().readByte();
                    }
                    break;
                case -56:
                    int i6 = msg.reader().readByte();
                    SkillClan[] skillClan = new SkillClan[i6];
                    for (int a = 0; a < i6; a++) {
                        skillClan[a] = new SkillClan();
                        skillClan[a].time = msg.reader().readShort();
                        skillClan[a].idIcon = msg.reader().readByte();
                        skillClan[a].info = msg.reader().readUTF();
                        skillClan[a].lv = msg.reader().readByte();
                    }
                    int i7 = msg.reader().readByte();
                    SkillClan[] skillClanPrivate = new SkillClan[i7];
                    for (int a2 = 0; a2 < i7; a2++) {
                        skillClanPrivate[a2] = new SkillClan();
                        skillClanPrivate[a2].time = msg.reader().readShort();
                        skillClanPrivate[a2].idIcon = msg.reader().readByte();
                        skillClanPrivate[a2].info = msg.reader().readUTF();
                        skillClanPrivate[a2].lv = msg.reader().readByte();
                    }
                    Canvas.gameScr.onSkillClan(skillClan, skillClanPrivate);
                    break;
                case -55:
                    Canvas.gameScr.onDropItem2Ground(msg);
                    break;
                case -53:
                    String nameClan = msg.reader().readUTF();
                    Canvas.gameScr.onFriendList(msg, 6, nameClan.toUpperCase());
                    break;
                case -52:
                    String nameItem = msg.reader().readUTF();
                    short idItem = msg.reader().readShort();
                    byte numKhoan = msg.reader().readByte();
                    Vector listMineral = new Vector();
                    for (int a3 = 0; a3 < numKhoan; a3++) {
                        Mineral mi = new Mineral();
                        mi.name = msg.reader().readUTF();
                        mi.idTemplate = msg.reader().readShort();
                        mi.number = msg.reader().readByte();
                        listMineral.addElement(mi);
                    }
                    byte typeCheDo = msg.reader().readByte();
                    byte khoa = 0;
                    try {
                        khoa = msg.reader().readByte();   // pokemon: bản dịch ngược có "break" ở đây -> có byte tuỳ chọn thì bỏ qua hàm xử lý (Unity không break)
                    } catch (Exception e3) {
                    }
                    Canvas.gameScr.onCheDo(nameItem, idItem, listMineral, typeCheDo, khoa);
                    break;
                case -51:
                    short idImg3 = msg.reader().readShort();
                    byte[] arrayImg1 = new byte[msg.reader().available()];
                    msg.reader().read(arrayImg1);
                    // 0.9.5: giải mã trên luồng mạng (không phải luồng vẽ) + ghi kho đĩa cho lần mở game sau
                    ImageIcon img = GameData.coIcon(idImg3);
                    if (img == null) {
                        img = GameData.getImgIcon(idImg3);
                    }
                    if (arrayImg1.length > 0) {
                        img.img = Image.createImage(arrayImg1, 0, arrayImg1.length);
                        img.isLoad = false;
                        game.pokemon.IconVatPham.luu(idImg3, arrayImg1);
                    }
                    break;
                case -50:
                    Canvas.gameScr.setNPC_server(msg.reader().readUTF(), msg.reader().readShort(), msg.reader().readShort(), msg.reader().readShort(), msg.reader().readShort(), msg.reader().readShort(), msg.reader().readShort(), msg.reader().readByte(), msg.reader().readByte());
                    break;
                case -49:
                    byte type3 = msg.reader().readByte();
                    short id3 = msg.reader().readByte();
                    if (type3 == 0) {
                        EffectData eff = EffectServerManager.getEffect(id3);
                        if (eff == null) {
                            GameService.gI().doRequestEffectData(id3);
                        }
                        EffectServerManager effObj = new EffectServerManager();
                        effObj.ID = id3;
                        effObj.style = msg.reader().readByte();
                        byte b = msg.reader().readByte();
                        effObj.count = b;
                        effObj.loopLimit = b;
                        effObj.loop = msg.reader().readShort();
                        effObj.loopType = msg.reader().readByte();
                        if (effObj.loopType == 1) {
                            effObj.radius = msg.reader().readShort();
                        } else if (effObj.loopType == 2) {
                            int i8 = msg.reader().readByte();
                            effObj.xLoop = new short[i8];
                            effObj.yLoop = new short[i8];
                            for (int a4 = 0; a4 < i8; a4++) {
                                effObj.xLoop[a4] = msg.reader().readShort();
                                effObj.yLoop[a4] = msg.reader().readShort();
                            }
                        }
                        if (effObj.style == 0) {
                            effObj.idPlayer = msg.reader().readShort();
                        } else {
                            effObj.x = msg.reader().readShort();
                            effObj.y = msg.reader().readShort();
                        }
                        Canvas.gameScr.onEffect(effObj);
                    } else {
                        EffectData ed = new EffectData();
                        ed.ID = id3;
                        ed.setInfo(msg);
                        EffectServerManager.effectList.addElement(ed);
                    }
                    break;
                case -48:
                    byte index1 = msg.reader().readByte();
                    if (index1 != -1) {
                        byte[] arrTree = new byte[msg.reader().readShort()];
                        msg.reader().read(arrTree);
                        TreeInfo tree = Res.getTree(index1);
                        if (tree != null) {
                            tree.onImage(arrTree, index1);
                        }
                    } else {
                        byte typeClothes = msg.reader().readByte();
                        byte Id = msg.reader().readByte();
                        byte[] arrayClothes = new byte[msg.reader().available()];
                        for (int j6 = 0; j6 < arrayClothes.length; j6++) {
                            arrayClothes[j6] = msg.reader().readByte();
                        }
                        Canvas.gameScr.onLoadCloth(arrayClothes, typeClothes, Id);
                    }
                    break;
                case -47:
                    short idMonsterTemplate = msg.reader().readShort();
                    boolean isSave = msg.reader().readBoolean();
                    byte[] arrMon = new byte[msg.reader().readShort()];
                    msg.reader().read(arrMon);
                    if (Res.monsterTemplates[idMonsterTemplate] != null) {
                        Res.monsterTemplates[idMonsterTemplate].onImage(isSave, arrMon);
                    }
                    break;
                case -46:
                    byte typeSound = msg.reader().readByte();
                    if (typeSound == 0) {
                        String des = msg.reader().readUTF();
                        byte idSound1 = msg.reader().readByte();
                        Sound.me.onRequestOpenSound(des, idSound1);
                    } else {
                        byte idSound = msg.reader().readByte();
                        byte loopSound = msg.reader().readByte();
                        byte[] dataS = new byte[msg.reader().available()];
                        msg.reader().read(dataS);
                        Sound.me.onSoundData(dataS, idSound, loopSound);
                    }
                    break;
                case -43:
                    int nImg = msg.reader().readByte();
                    if (nImg > 0) {
                        Image[] img2 = new Image[nImg];
                        for (int ii2 = 0; ii2 < nImg; ii2++) {
                            try {
                                byte[] arrImg = new byte[msg.reader().readInt()];
                                msg.reader().read(arrImg, 0, arrImg.length);
                                img2[ii2] = Image.createImage(arrImg, 0, arrImg.length);
                            } catch (Exception e4) {
                            }
                        }
                        byte nobj = msg.reader().readByte();
                        for (int ii3 = 0; ii3 < nobj; ii3++) {
                            DynamicObj obj = new DynamicObj();
                            int idimg = msg.reader().readByte();
                            short x2 = msg.reader().readByte();
                            short y2 = msg.reader().readByte();
                            obj.w = msg.reader().readByte();
                            obj.h = msg.reader().readByte();
                            obj.nFrame = msg.reader().readByte();
                            obj.setPosTo((short) (x2 * 16), (short) (y2 * 16));
                            obj.img = img2[idimg];
                            obj.height = (short) (obj.img.getHeight() / obj.nFrame);
                            Canvas.gameScr.onDynamicObj(obj);
                        }
                    }
                    break;
                case -42:
                    short idChar = msg.reader().readShort();
                    int iAvailable = (byte) (msg.reader().available() / 2);
                    byte[] idSkill = new byte[iAvailable];
                    byte[] time = new byte[iAvailable];
                    int i9 = 0;
                    while (msg.reader().available() > 0) {
                        idSkill[i9] = msg.reader().readByte();
                        time[i9] = msg.reader().readByte();
                        i9++;
                    }
                    Canvas.gameScr.onSetCharKham(idChar, idSkill, time);
                    break;
                case -41:
                    short whoGet3 = msg.reader().readShort();
                    byte catagory = msg.reader().readByte();
                    short gemID = msg.reader().readShort();
                    Canvas.gameScr.onGetGemFromGround(whoGet3, gemID, catagory);
                    break;
                case -39:
                    byte weather = msg.reader().readByte();
                    int number = msg.reader().readShort();
                    int timeLimit = msg.reader().readInt();
                    GameScr.onWeather(weather, number, timeLimit);
                    break;
                case -38:
                    GameScr.isFight = msg.reader().readBoolean();
                    int len = msg.reader().readByte();
                    GameScr.timeChiemThanh = new int[len];
                    GameScr.nameChiemThanh = new String[len];
                    GameScr.curTimeCT = new int[len];
                    GameScr.idChiemThanh = new short[len];
                    GameScr.nameClan = new String[len];
                    for (int i10 = 0; i10 < len; i10++) {
                        GameScr.timeChiemThanh[i10] = msg.reader().readShort();
                        short idChiem = msg.reader().readShort();
                        GameScr.nameChiemThanh[i10] = msg.reader().readUTF();
                        GameScr.curTimeCT[i10] = (int) (System.currentTimeMillis() / 1000);
                        Char cha = (Char) Canvas.gameScr.findCharByID(idChiem);
                        GameScr.idChiemThanh[i10] = idChiem;
                        if (cha != null) {
                            cha.EffFace = null;
                        }
                        if (cha != null && GameScr.timeChiemThanh[i10] > 0) {
                            cha.EffFace = new WayPoint(cha.x, cha.y - 5, (short) 1, (byte) 1, false);
                        }
                        GameScr.nameClan[i10] = msg.reader().readUTF().toLowerCase();
                    }
                    break;
                case -37:
                    byte type5 = msg.reader().readByte();
                    if (type5 == 0) {
                        String text1 = new StringBuffer("`").append(msg.reader().readUTF()).toString();
                        byte type4 = msg.reader().readByte();
                        if (type4 == 0) {
                            WindowInfoScr.idMonsterClanQuest = msg.reader().readByte();
                            WindowInfoScr.numberQuestAll = msg.reader().readShort();
                            WindowInfoScr.numberQuestGet = msg.reader().readShort();
                            text1 = new StringBuffer(String.valueOf(text1)).append("\nHoàn thành: ").append((int) WindowInfoScr.numberQuestGet).append("/").append((int) WindowInfoScr.numberQuestAll).append("Con").toString();
                        } else if (type4 == 1) {
                            GameScr.idItemClanquest = msg.reader().readByte();
                            WindowInfoScr.numberQuestAll = msg.reader().readShort();
                            WindowInfoScr.numberQuestGet = msg.reader().readShort();
                            text1 = new StringBuffer(String.valueOf(text1)).append("\nĐã lấy: ").append((int) WindowInfoScr.numberQuestGet).append("/").append((int) WindowInfoScr.numberQuestAll).toString();
                        } else if (type4 == 2) {
                            GameScr.posNpcReceiveClan = new Position();
                            GameScr.posNpcReceiveClan.index = msg.reader().readByte();
                            for (int i11 = 0; i11 < Canvas.gameScr.actors.size(); i11++) {
                                Actor ac = (Actor) Canvas.gameScr.actors.elementAt(i11);
                                if ((ac instanceof NPC) && ((NPC) ac).type == GameScr.posNpcReceiveClan.index) {
                                    GameScr.posNpcReceiveClan.x = ac.x;
                                    GameScr.posNpcReceiveClan.y = ac.y;
                                }
                            }
                        }
                        Res.PORTION_ITEM_NAME_CLAN = msg.reader().readUTF();
                        WindowInfoScr.gI().questClan(text1, (byte) 0);
                    } else if (type5 == 1) {
                        String content = msg.reader().readUTF();
                        Canvas.gameScr.onContentClanQuest(content);
                    } else if (type5 == 2) {
                        String content2 = msg.reader().readUTF();
                        Res.PORTION_ITEM_NAME_CLAN = msg.reader().readUTF();
                        ViewInfoScr.gI().setInfo("NHIỆM VỤ", content2);
                        ViewInfoScr.gI().switchToMe();
                        Canvas.endDlg();
                    }
                    break;
                case -36:
                    int typeE = msg.reader().readByte();
                    if (typeE == 0) {
                        Canvas.gameScr.onEpNgoc(msg.reader().readShort());
                    } else {
                        Canvas.gameScr.onImbueOK(msg.reader().readUTF());
                    }
                    break;
                case -35:
                    Canvas.gameScr.onImbueOK(msg.reader().readUTF());
                    break;
                case -34:
                    short price1 = msg.reader().readShort();
                    byte khoa3 = 0;
                    try {
                        khoa3 = msg.reader().readByte();   // pokemon: bản dịch ngược có "break" ở đây -> có byte tuỳ chọn thì bỏ qua hàm xử lý (Unity không break)
                    } catch (Exception e5) {
                    }
                    Canvas.gameScr.onKham(price1, khoa3);
                    break;
                case -33:
                    int xBoss = msg.reader().readByte() * 16;
                    int yBoss = msg.reader().readByte() * 16;
                    short idMap = msg.reader().readShort();
                    Canvas.gameScr.posBoss = new Position(xBoss, yBoss);
                    Canvas.gameScr.posBoss.index = idMap;
                    if (Canvas.gameScr.posBoss.index == Canvas.gameScr.map) {
                        EffectManager.lowEffects.addElement(new WayPoint(xBoss, yBoss, idMap, (byte) 0, true));
                        EffectManager.hiEffects.addElement(new WayPoint(xBoss, yBoss, idMap, (byte) 1, true));
                    }
                    break;
                case -32:
                    final int idB = msg.reader().readShort();
                    final byte idPopup = msg.reader().readByte();
                    final String text5 = msg.reader().readUTF();
                    Canvas.startYesNoDlg(text5, new IAction() {
                        @Override
                        public void perform() {
                            Canvas.endDlg();
                            GameService.gI().doCustomPopup(idB, idPopup, text5, 1);
                        }
                    }, new IAction() {
                        @Override
                        public void perform() {
                            Canvas.endDlg();
                            GameService.gI().doCustomPopup(idB, idPopup, text5, 0);
                        }
                    });
                    break;
                case -31:
                    int userID = msg.reader().readShort();
                    byte ID1 = msg.reader().readByte();
                    String nameText = msg.reader().readUTF();
                    int typeInput = msg.reader().readByte();
                    Canvas.gameScr.onTextBox(userID, ID1, nameText, typeInput);
                    break;
                case -30:
                    int idBoss = msg.reader().readShort();
                    byte idMenu = msg.reader().readByte();
                    int sizeMenu = msg.reader().readByte();
                    String[] listStr = new String[sizeMenu];
                    for (int i12 = 0; i12 < sizeMenu; i12++) {
                        listStr[i12] = msg.reader().readUTF();
                    }
                    Canvas.gameScr.onMenuOption(idBoss, idMenu, listStr);
                    break;
                case -29:
                    break;
                case -28:
                    int sizeCapcha = msg.reader().readShort();
                    byte[] arrayImg = new byte[sizeCapcha];
                    msg.reader().read(arrayImg);
                    Canvas.gameScr.onCapCha(arrayImg);
                    break;
                case -27:
                    int type9 = msg.reader().readByte();
                    byte weaponIndex = msg.reader().readByte();
                    if (weaponIndex != -1) {
                        int size7 = msg.reader().readShort();
                        byte[] header = new byte[size7];
                        for (int a5 = 0; a5 < size7; a5++) {
                            header[a5] = msg.reader().readByte();
                        }
                        int size8 = msg.reader().readShort();
                        byte[] data = new byte[size8];
                        for (int a6 = 0; a6 < size8; a6++) {
                            data[a6] = msg.reader().readByte();
                        }
                        Image imgWpa = Res.createImgByHeader(header, data);
                        short dxWear = msg.reader().readByte();
                        short dyWear = msg.reader().readByte();
                        if (type9 == 0) {
                            CreateCharScr.gI().ch.imgWpa = imgWpa;
                            CreateCharScr.gI().ch.dxWear = dxWear;
                            CreateCharScr.gI().ch.dyWear = dyWear;
                        } else if (type9 == 1) {
                            Canvas.gameScr.mainChar.weaponIndex = weaponIndex;
                            Canvas.gameScr.mainChar.imgWpa = imgWpa;
                            Canvas.gameScr.mainChar.dxWear = dxWear;
                            Canvas.gameScr.mainChar.dyWear = dyWear;
                        } else if (type9 == 2) {
                            if (WindowInfoScr.getIndex() == 1) {
                                WindowInfoScr.charWearing.imgWpa = imgWpa;
                                WindowInfoScr.charWearing.dxWear = dxWear;
                                WindowInfoScr.charWearing.dyWear = dyWear;
                            } else {
                                WindowInfoScr.gI().imgWeaponAvatar = imgWpa;
                            }
                        }
                    }
                    Canvas.endDlg();
                    break;
                case -26: {
                    String chay = msg.reader().readUTF();
                    game.pokemon.SinhThai.docThongBaoChay(chay);
                    Canvas.gameScr.onMSGServer(chay);
                    break;
                }
                case -24:
                    int typeStr = msg.reader().readByte();
                    String strt = msg.reader().readUTF();
                    if (typeStr == 0) {
                        HelpScr.gI().setInfo(strt);
                    } else if (typeStr == 1) {
                    }
                    // gói hướng dẫn (typeStr 0) chỉ có một chuỗi: đọc thêm khi còn dữ liệu (trước đây EOFException)
                    if (msg.reader().available() > 0) {
                        Canvas.gameScr.onMSGServer(msg.reader().readUTF());
                    }
                    break;
                case -22:
                    Char ch = new Char();
                    byte typeView = msg.reader().readByte();
                    if (typeView == 0) {
                        ch.name = msg.reader().readUTF();
                        ch.currentHead = msg.reader().readByte();
                        ch.dir = (short) 0;
                        ch.level = msg.reader().readByte();
                        ch.weapon_frame = (short) 0;
                        byte numItem = msg.reader().readByte();
                        Vector we = new Vector();
                        for (int a7 = 0; a7 < numItem; a7++) {
                            ItemInInventory item = new ItemInInventory();
                            byte b2 = msg.reader().readByte();
                            item.charClazz = b2;
                            item.clazz = b2;
                            ch.clazz = b2;
                            item.idTemplate = msg.reader().readShort();
                            item.level = msg.reader().readByte();
                            item.plusTemplate = msg.reader().readByte();
                            item.itemID = msg.reader().readShort();
                            item.colorName = msg.reader().readByte();
                            item.attCreate = new short[5];
                            for (int j7 = 0; j7 < 5; j7++) {
                                item.attCreate[j7] = msg.reader().readShort();
                            }
                            item.heItem = msg.reader().readByte();
                            item.beKicked = msg.reader().readByte();
                            item.hangItem = msg.reader().readByte();
                            item.magic_physic = msg.reader().readByte();
                            item.lock = msg.reader().readByte();
                            item.nameCharSeal = msg.reader().readUTF();
                            for (int k2 = 0; k2 < item.newAtb.length; k2++) {
                                item.newAtb[k2] = msg.reader().readByte();
                            }
                            for (int k3 = 0; k3 < item.lockAtb.length; k3++) {
                                item.lockAtb[k3] = msg.reader().readByte();
                            }
                            for (int k4 = 0; k4 < item.addMoreLevelSkill.length; k4++) {
                                item.addMoreLevelSkill[k4] = msg.reader().readByte();
                            }
                            we.addElement(item);
                            ItemTemplate it = Res.getItem(item.charClazz, item.idTemplate);
                            if (it.type >= 3 && it.type < 8) {
                                ItemTemplate itwp = Res.getItem(ch.clazz, item.idTemplate);
                                GameService.gI().doRequestWeapone(2, it.type, it.style, itwp.colorItem);
                                Canvas.startWaitDlg();
                            }
                        }
                        ch.idClan = msg.reader().readShort();
                        ch.idFashion = msg.reader().readByte();
                        for (int i13 = 0; i13 < ch.idModel.length; i13++) {
                            ch.idModel[i13] = msg.reader().readShort();
                        }
                        ch.idAnimal = msg.reader().readByte();
                        if (ch.idAnimal != -1) {
                            ch.infoAnimal = msg.reader().readUTF();
                        }
                        ch.setWearingInfo(we);
                        if (Canvas.gameScr.focusedActor != null) {
                            ch.ID = Canvas.gameScr.focusedActor.ID;
                        }
                        Canvas.gameScr.onViewCharInfo(ch);
                    } else {
                        Vector infoAnimal = new Vector();
                        int nItemAnimal = msg.reader().readByte();
                        if (nItemAnimal > -1) {
                            for (int i14 = 0; i14 < nItemAnimal; i14++) {
                                ItemInInventory cwi = new ItemInInventory();
                                byte b3 = msg.reader().readByte();
                                cwi.clazz = b3;
                                cwi.charClazz = b3;
                                cwi.itemID = msg.reader().readShort();
                                cwi.idTemplate = msg.reader().readShort();
                                cwi.plusTemplate = msg.reader().readByte();
                                cwi.level = msg.reader().readByte();
                                cwi.mDurable = msg.reader().readShort();
                                cwi.durable = msg.reader().readShort();
                                cwi.colorName = msg.reader().readByte();
                                cwi.attCreate = new short[5];
                                for (int j8 = 0; j8 < 5; j8++) {
                                    cwi.attCreate[j8] = msg.reader().readShort();
                                }
                                cwi.heItem = msg.reader().readByte();
                                cwi.beKicked = msg.reader().readByte();
                                cwi.hangItem = msg.reader().readByte();
                                cwi.magic_physic = msg.reader().readByte();
                                cwi.lock = msg.reader().readByte();
                                cwi.nameCharSeal = msg.reader().readUTF();
                                for (int k5 = 0; k5 < cwi.newAtb.length; k5++) {
                                    cwi.newAtb[k5] = msg.reader().readByte();
                                }
                                for (int k6 = 0; k6 < cwi.lockAtb.length; k6++) {
                                    cwi.lockAtb[k6] = msg.reader().readByte();
                                }
                                for (int k7 = 0; k7 < cwi.addMoreLevelSkill.length; k7++) {
                                    cwi.addMoreLevelSkill[k7] = msg.reader().readByte();
                                }
                                infoAnimal.addElement(cwi);
                            }
                            Image imgAnimal = null;
                            byte numFrame = msg.reader().readByte();
                            int wimg = 0;
                            int himg = 0;
                            byte timeChangeFrame = 0;
                            byte[] arr = new byte[msg.reader().available()];
                            while (msg.reader().available() > 0) {
                                msg.reader().read(arr, 0, arr.length);
                            }
                            if (arr.length > 0) {
                                imgAnimal = Res.createImgByByteArray(arr);
                                timeChangeFrame = (byte) (numFrame == 3 ? 3 : 6);
                                if (imgAnimal != null) {
                                    wimg = imgAnimal.getWidth();
                                    himg = imgAnimal.getHeight() / numFrame;
                                }
                            }
                            Canvas.gameScr.onAnimalWearingInfo(Canvas.gameScr.saveIDViewInfoAnimal, infoAnimal, imgAnimal, numFrame, wimg, himg, timeChangeFrame);
                        } else {
                            Canvas.startOKDlg("Chưa có thông tin thú cưỡi");
                        }
                    }
                    break;
                case -20:
                    final int moneyClan1 = msg.reader().readInt();
                    Canvas.startOKDlg(new StringBuffer("Bạn nhận được ").append(moneyClan1).append("xu từ đội.").toString(), new IAction() {
                        @Override
                        public void perform() {
                            Canvas.gameScr.mainChar.charXu += (long) moneyClan1;
                        }
                    });
                    break;
                case -19:
                    byte type6 = msg.reader().readByte();
                    String nameTile = msg.reader().readUTF();
                    Vector list6 = new Vector();
                    if (type6 == 5) {
                        short sizeclan = msg.reader().readShort();
                        for (int i15 = 0; i15 < sizeclan; i15++) {
                            ClanInfo clan2 = new ClanInfo();
                            clan2.id = msg.reader().readShort();
                            clan2.name = msg.reader().readUTF();
                            clan2.master = msg.reader().readUTF();
                            clan2.level = msg.reader().readByte();
                            clan2.members = msg.reader().readShort();
                            clan2.money = msg.reader().readLong();
                            clan2.nationID = msg.reader().readByte();
                            list6.addElement(clan2);
                        }
                        ListScr.gI().setInfo(list6, type6, "TOP ĐỘI");
                        ListScr.gI().switchToMe();
                        Canvas.endDlg();
                    } else if (type6 == 7 || type6 == 8) {
                        Canvas.gameScr.onFriendList(msg, type6, nameTile);
                    } else if (type6 == 4) {
                        Canvas.gameScr.onFriendList(msg, type6, "TOP SỨC MẠNH");
                    } else {
                        Canvas.gameScr.onFriendList(msg, type6, "TOP ĐẠI GIA");
                    }
                    break;
                case -18:
                    String info2 = msg.reader().readUTF();
                    MessageScr.gI().addTab(info2, "Đội", 1);
                    break;
                case -17:
                    Vector list = new Vector();
                    short size5 = msg.reader().readShort();
                    for (int i16 = 0; i16 < size5; i16++) {
                        ChatInfo c = new ChatInfo();
                        c.ID = msg.reader().readInt();
                        c.name = msg.reader().readUTF();
                        c.content = msg.reader().readUTF();
                        list.addElement(c);
                    }
                    ListScr.gI().setInfo(list, 2, "THÔNG BÁO");
                    ListScr.gI().switchToMe();
                    Canvas.endDlg();
                    break;
                case -13:
                    Canvas.gameScr.mainChar.idClan = (short) -1;
                    Canvas.startOKDlg("Bạn bị mời khỏi đội.");
                    break;
                case -12:
                    ClanInfo clan = new ClanInfo();
                    clan.id = msg.reader().readShort();
                    clan.name = msg.reader().readUTF();
                    clan.master = msg.reader().readUTF();
                    clan.level = msg.reader().readByte();
                    clan.members = msg.reader().readShort();
                    clan.money = msg.reader().readLong();
                    clan.dedicationPoint = msg.reader().readLong();
                    clan.xp = msg.reader().readLong();
                    clan.date = msg.reader().readUTF();
                    clan.status = msg.reader().readUTF();
                    clan.dissolved = msg.reader().readBoolean();
                    clan.nationID = msg.reader().readByte();
                    if (clan.dissolved) {
                        clan.strDissolved = msg.reader().readUTF();
                    }
                    Canvas.gameScr.showClanInfo(clan);
                    break;
                case -11:
                    byte type1 = msg.reader().readByte();
                    if (type1 == 0) {
                        final short idUser1 = msg.reader().readShort();
                        String info3 = msg.reader().readUTF();
                        Canvas.startYesNoDlg(info3, new IAction() {
                            @Override
                            public void perform() {
                                GameService.gI().sendRequestAddClan(idUser1, (byte) 1, true);
                                Canvas.endDlg();
                            }
                        }, new IAction() {
                            @Override
                            public void perform() {
                                GameService.gI().sendRequestAddClan(idUser1, (byte) 1, false);
                                Canvas.endDlg();
                            }
                        });
                    } else {
                        String info4 = msg.reader().readUTF();
                        boolean is = msg.reader().readBoolean();
                        if (is) {
                            short idUser = msg.reader().readShort();
                            short idClan4 = msg.reader().readShort();
                            if (idUser == Canvas.gameScr.mainChar.ID) {
                                Canvas.gameScr.mainChar.idClan = idUser;
                                PauseMenu.gI().initName();
                            } else {
                                ((Char) Canvas.gameScr.findCharByID(idUser)).idClan = idClan4;
                            }
                        }
                        Canvas.startOKDlg(info4);
                    }
                    break;
                case -10:
                    short idClan = msg.reader().readShort();
                    String info1 = msg.reader().readUTF();
                    Canvas.gameScr.mainChar.idClan = idClan;
                    Canvas.gameScr.mainChar.isMaster = (byte) 0;
                    PauseMenu.gI().initName();
                    Canvas.startOKDlg(info1);
                    break;
                case -9:
                    int i17 = msg.reader().readShort();
                    final short[] arrayIndex = new short[i17];
                    for (int i18 = 0; i18 < i17; i18++) {
                        arrayIndex[i18] = msg.reader().readShort();
                    }
                    String info = msg.reader().readUTF();
                    if (info.equals("")) {
                        Canvas.gameScr.onRegClan(arrayIndex);
                    } else {
                        Canvas.startYesNoDlg(info, new IAction() {
                            @Override
                            public void perform() {
                                Canvas.gameScr.onRegClan(arrayIndex);
                                Canvas.endDlg();
                            }
                        });
                    }
                    break;
                case -8:
                    byte type = msg.reader().readByte();
                    byte ver = msg.reader().readByte();
                    byte[] array = new byte[msg.reader().readShort()];
                    msg.reader().read(array);
                    switch (type) {
                        case 1:
                            GameData.saveImgSkill(ver, array);
                            break;
                        case 2:
                            byte[] arrayItem = new byte[msg.reader().readShort()];
                            msg.reader().read(arrayItem);
                            int i19 = msg.reader().readByte();
                            byte[][] dataH = new byte[i19][];
                            byte[][][] headH = new byte[i19][][];
                            for (int a8 = 0; a8 < i19; a8++) {
                                dataH[a8] = new byte[msg.reader().readShort()];
                                msg.reader().read(dataH[a8]);
                                int i20 = msg.reader().readByte();
                                headH[a8] = new byte[i20][];
                                for (int i21 = 0; i21 < i20; i21++) {
                                    headH[a8][i21] = new byte[msg.reader().readShort()];
                                    msg.reader().read(headH[a8][i21]);
                                }
                            }
                            GameData.saveImgPotion(ver, array, null, dataH, headH);
                            break;
                        case 3:
                            GameData.saveImgGem(ver, array);
                            break;
                        case 4:
                            AnimalInfo.imgAnimal = Image.createImage(array, 0, array.length);
                            break;
                        default:
                            break;
                    }
                    break;
                case -7:
                    String nameClan1 = msg.reader().readUTF();
                    Canvas.gameScr.onFriendList(msg, 1, nameClan1.toUpperCase());
                    break;
                case -5:
                    byte numMsg = msg.reader().readByte();
                    for (int i22 = 0; i22 < numMsg; i22++) {
                        String name2 = msg.reader().readUTF();
                        String info5 = msg.reader().readUTF();
                        MessageScr.gI().addTab(new StringBuffer(String.valueOf(name2)).append(": ").append(info5).toString(), name2, 0);
                    }
                    break;
                case 1:
                    Char.char_speed = msg.reader().readByte();
                    if (Char.char_speed < 3) {
                        Char.char_speed = (byte) 3;
                    }
                    GameScr.pTicket = msg.reader().readShort();
                    Char.CHAR_POTION = (short) msg.reader().readUnsignedByte();
                    MainChar.listPotion = new Potion[Char.CHAR_POTION];
                    Canvas.gameScr.mainChar.potionLastTimeUse = new long[Char.CHAR_POTION];
                    for (int i23 = 0; i23 < Char.CHAR_POTION; i23++) {
                        MainChar.listPotion[i23] = new Potion();
                        MainChar.listPotion[i23].index = (short) msg.reader().readUnsignedByte();
                        MainChar.listPotion[i23].name = msg.reader().readUTF();
                        MainChar.listPotion[i23].name2 = msg.reader().readUTF();
                        MainChar.listPotion[i23].delay = msg.reader().readShort();
                        MainChar.listPotion[i23].id = (short) i23;
                        MainChar.listPotion[i23].isTrade = msg.reader().readBoolean();
                    }
                    for (int i24 = 0; i24 < Char.nSkill.length; i24++) {
                        Char.nSkill[i24] = msg.reader().readByte();
                    }
                    GameScr.infoNewSkill.removeAllElements();
                    for (int i25 = 0; i25 < 5; i25++) {
                        int nskill = msg.reader().readByte();
                        Vector infonewskill = new Vector();
                        for (int j9 = 0; j9 < nskill; j9++) {
                            InfoSkillLearn skill = new InfoSkillLearn();
                            skill.idSkill = msg.reader().readByte();
                            skill.name = msg.reader().readUTF();
                            skill.decript = msg.reader().readUTF();
                            skill.price = msg.reader().readInt();
                            skill.charClass = (byte) i25;
                            infonewskill.addElement(skill);
                        }
                        GameScr.infoNewSkill.addElement(infonewskill);
                    }
                    if (msg.reader().available() > 0) {
                        GameScr.idMapPK = msg.reader().readShort();
                        int npo = msg.reader().readUnsignedByte();
                        for (int i26 = 0; i26 < npo; i26++) {
                            String str = new StringBuffer(String.valueOf(msg.reader().readUnsignedByte())).toString();
                            WindowInfoScr.gI().potionShop.addElement(str);
                        }
                        for (int i27 = 0; i27 < 5; i27++) {
                            for (int j10 = 0; j10 < 5; j10++) {
                                Char.mainDef[i27][j10] = msg.reader().readByte();
                                Char.mainAt[i27][j10] = msg.reader().readByte();
                                Char.pcAttack[i27][j10] = msg.reader().readByte();
                                Char.pcDef[i27][j10] = msg.reader().readByte();
                            }
                        }
                    }
                    GameScr.xArena = msg.reader().readByte();
                    GameScr.yArena = msg.reader().readByte();
                    GameScr.wArena = msg.reader().readByte();
                    GameScr.hArena = msg.reader().readByte();
                    int i28 = msg.reader().readByte();
                    Res.khamProperty = new String[i28];
                    Res.idNgocKham = new byte[i28];
                    for (int i29 = 0; i29 < i28; i29++) {
                        Res.khamProperty[i29] = msg.reader().readUTF();
                        Res.idNgocKham[i29] = msg.reader().readByte();
                    }
                    String numberSupport = msg.reader().readUTF();
                    if (!LoginScr.numberSupport.equals(numberSupport)) {
                        LoginScr.numberSupport = numberSupport;
                        RMS.saveRMSString("numbersupport", numberSupport);
                    }
                    int i30 = msg.reader().readByte();
                    GameScr.TYPE_MP_HP[0] = new byte[i30];
                    GameScr.TYPE_MP_HP[1] = new byte[i30];
                    GameScr.VALUE_MP_HP[1] = new int[i30];
                    GameScr.VALUE_MP_HP[0] = new int[i30];
                    for (int i31 = 0; i31 < i30; i31++) {
                        GameScr.TYPE_MP_HP[0][i31] = msg.reader().readByte();
                        GameScr.VALUE_MP_HP[0][i31] = msg.reader().readInt();
                        GameScr.TYPE_MP_HP[1][i31] = msg.reader().readByte();
                        GameScr.VALUE_MP_HP[1][i31] = msg.reader().readInt();
                    }
                    int allItemquest = msg.reader().readByte();
                    ItemQuest.NAME_ITEM = new String[allItemquest];
                    ItemQuest.ICON_IMG = new byte[allItemquest];
                    for (int i32 = 0; i32 < allItemquest; i32++) {
                        ItemQuest.ICON_IMG[i32] = msg.reader().readByte();
                        ItemQuest.NAME_ITEM[i32] = msg.reader().readUTF();
                    }
                    GameMidlet.url = msg.reader().readUTF();
                    byte totalTile = msg.reader().readByte();
                    Cout.println(new StringBuffer(String.valueOf((int) totalTile)).append("tong so tile").toString());
                    for (int i33 = 0; i33 < totalTile; i33++) {
                        int i34 = msg.reader().readShort();
                        byte[] data2 = new byte[i34];
                        for (int j11 = 0; j11 < i34; j11++) {
                            data2[j11] = msg.reader().readByte();
                        }
                        RMS.saveRMS(Canvas.nameTile[i33], data2);
                    }
                    msg.reader().readByte();
                    try {
                        PokemonHuyenThoai.ALL_DATA_THAN_THU.clear();
                        byte nthanthu = msg.reader().readByte();
                        for (int i35 = 0; i35 < nthanthu; i35++) {
                            byte[] data3 = new byte[msg.reader().readShort()];
                            msg.reader().read(data3);
                            PokemonHuyenThoai.setDataThanThu(i35, data3);
                        }
                    } catch (Exception e6) {
                    }
                    Canvas.gameScr.onLoginSuccess();
                    break;
                case 2:
                    String alert = msg.reader().readUTF();
                    if (alert.startsWith("2")) {
                        String[] ip = new String[msg.reader().readByte()];
                        short[] port = new short[ip.length];
                        String[] name3 = new String[ip.length];
                        for (int i36 = 0; i36 < ip.length; i36++) {
                            ip[i36] = msg.reader().readUTF();
                            port[i36] = msg.reader().readShort();
                            name3[i36] = msg.reader().readUTF();
                        }
                        Canvas.gameScr.listServer(alert.substring(1), ip, port, name3);
                    } else if (alert.startsWith("3")) {
                        String pro = msg.reader().readUTF();
                        String agent = msg.reader().readUTF();
                        GameMidlet.provider = pro;
                        GameMidlet.agent = agent;
                        RMS.saveRMSString("provider", pro);
                        RMS.saveRMSString("agent", agent);
                    } else {
                        Canvas.gameScr.onLoginFail(alert);
                    }
                    break;
                case 3:
                    MainChar m = Canvas.gameScr.mainChar;
                    try {
                        m.ID = msg.reader().readShort();
                        m.name = msg.reader().readUTF();
                        m.wName = (short) Font.smallFont.getWidth(m.name);
                        int i37 = msg.reader().readInt();
                        game.pokemon.MauPP.datHp(m, i37);   // pokemon: một nguồn HP (hủy đồng bộ trễ cũ)
                        m.maxhp = msg.reader().readInt();
                        m.mp = msg.reader().readInt();
                        m.maxmp = msg.reader().readInt();
                        m.currentHead = msg.reader().readByte();
                        m.clazz = msg.reader().readByte();
                        m.attack = msg.reader().readInt();
                        m.defend = msg.reader().readInt();
                        m.defend_magic = msg.reader().readInt();
                        m.accurate = msg.reader().readShort();
                        m.dodge = msg.reader().readShort();
                        m.critical = msg.reader().readShort();
                        m.he = msg.reader().readByte();
                        m.level = msg.reader().readByte();
                        m.lvpercent = msg.reader().readShort();
                        m.strength = msg.reader().readShort();
                        m.agility = msg.reader().readShort();
                        m.spirit = msg.reader().readShort();
                        m.health = msg.reader().readShort();
                        m.luck = msg.reader().readShort();
                        m.basePointLeft = msg.reader().readShort();
                        m.skillPointLeft = msg.reader().readShort();
                        m.dedicationPoint = msg.reader().readInt();
                        m.baokich = msg.reader().readShort();
                        Char.skillLevelLearnt = new byte[msg.reader().readByte()];
                        for (int i38 = 0; i38 < Char.skillLevelLearnt.length; i38++) {
                            Char.skillLevelLearnt[i38] = msg.reader().readByte();
                        }
                        m.killer = msg.reader().readShort();
                        if (m.killer > 0) {
                            m.iskiller = true;
                        }
                        m.gender = msg.reader().readByte();
                        byte hor = msg.reader().readByte();
                        if (hor >= 0) {
                            m.useHorse = (byte) (hor - 1);
                        }
                        m.anmFly = msg.reader().readByte();
                        m.idImgHorse = msg.reader().readByte();
                        m.speed = msg.reader().readByte();
                        m.idClan = msg.reader().readShort();
                        if (m.idClan != -1) {
                            Canvas.gameScr.mainChar.isMaster = msg.reader().readByte();
                        }
                        if (Canvas.gameScr.mainChar.isMaster == 0) {
                            Char.dissolved = msg.reader().readBoolean();
                        }
                        m.isNo = msg.reader().readBoolean();
                        m.limItemBag = msg.reader().readByte();
                        m.nationID = msg.reader().readByte();
                        m.inCountry = msg.reader().readByte();
                        m.lienTram = msg.reader().readShort();
                        m.congTrang = msg.reader().readInt();
                        m.hoatdong = msg.reader().readShort();
                        m.pArena = msg.reader().readInt();
                        m.nameHusband_wife = msg.reader().readUTF();
                        m.paintHat = msg.reader().readBoolean();
                        try {
                            idhorse = msg.reader().readShort();
                            typehorse = msg.reader().readByte();
                        } catch (Exception e7) {
                            idhorse = -1;
                            typehorse = -1;
                        }
                        Canvas.gameScr.mainChar.setHorse(idhorse, typehorse);
                        try {
                            idwp = msg.reader().readShort();
                            typewp = msg.reader().readByte();
                            idimgwp = msg.reader().readShort();
                        } catch (Exception e8) {
                            idwp = -1;
                            typewp = -1;
                            idimgwp = -1;
                        }
                        Canvas.gameScr.mainChar.setWeapon(idwp, typewp, idimgwp);
                        try {
                            idHair2 = msg.reader().readShort();
                            idBody2 = msg.reader().readShort();
                            idLeg2 = msg.reader().readShort();
                        } catch (Exception e9) {
                            idHair2 = -1;
                            idBody2 = -1;
                            idLeg2 = -1;
                        }
                        Canvas.gameScr.mainChar.setPartWearing(idHair2, idBody2, idLeg2);
                        try {
                            int i39 = msg.reader().readByte();
                            short[] arrEff = new short[i39];
                            byte[] arrTick = new byte[i39];
                            for (int i40 = 0; i40 < i39; i40++) {
                                arrEff[i40] = msg.reader().readShort();
                                arrTick[i40] = msg.reader().readByte();
                            }
                            Canvas.gameScr.mainChar.setEff(arrEff, arrTick);
                            String a9 = msg.reader().readUTF();
                            if (!a9.equals("")) {
                                WindowInfoScr.infoMainCharServer = Util.split(a9, "@");
                            } else {
                                WindowInfoScr.infoMainCharServer = null;
                            }
                        } catch (Exception e10) {
                        }
                        try {
                            idPP = msg.reader().readShort();
                        } catch (Exception e11) {
                            idPP = -1;
                        }
                        Canvas.gameScr.mainChar.idPhiPhong = idPP;
                        break;
                    } catch (Exception e12) {
                    }
                    try {
                        indexpp = msg.reader().readByte();
                        idImgpp = msg.reader().readShort();
                        nframewp = msg.reader().readByte();
                        nframepp = msg.reader().readByte();
                    } catch (Exception e13) {
                        indexpp = -1;
                        idImgpp = -1;
                        nframewp = 0;
                        nframepp = 0;
                    }
                    Canvas.gameScr.mainChar.indexPP = indexpp;
                    Canvas.gameScr.mainChar.idImgPP = idImgpp;
                    Canvas.gameScr.mainChar.nFrameWP = nframewp;
                    Canvas.gameScr.mainChar.nFramePP = nframepp;
                    try {
                        indexAVT = msg.reader().readByte();
                        idBigavt = msg.reader().readShort();
                        nframeAVT = msg.reader().readByte();
                    } catch (Exception e14) {
                        indexAVT = -1;
                        idBigavt = -1;
                        nframeAVT = 0;
                    }
                    Canvas.gameScr.mainChar.indexAVT = indexAVT;
                    Canvas.gameScr.mainChar.idImgAVT = idBigavt;
                    Canvas.gameScr.mainChar.nFrameAVT = nframeAVT;
                    Canvas.gameScr.onMainCharInfo();
                    game.pokemon.TrangThaiNguoiChoi.doi(game.pokemon.TrangThaiNguoiChoi.TAT_CA);
                    break;
                case 4:
                    // Sửa lỗi dịch ngược: các "break;" sau từng trường tuỳ chọn (idchar / isnewmonster / isThanthu)
                    // thoát cả vòng trước khi gọi onActorMove -> Pokémon hoang dã không bao giờ hiện (như PC).
                    while (msg.reader().available() > 0) {
                        byte actorCatagory = msg.reader().readByte();
                        short type2 = (short) msg.reader().readUnsignedByte();
                        short actorID = msg.reader().readShort();
                        short x3 = msg.reader().readShort();
                        short y3 = msg.reader().readShort();
                        byte pk = msg.reader().readByte();
                        byte isnewmonster = 0;
                        int idchar = -1;
                        try {
                            idchar = msg.reader().readInt();
                        } catch (Exception e15) {
                        }
                        if (actorCatagory == 1) {
                            try {
                                isnewmonster = msg.reader().readByte();
                            } catch (Exception e16) {
                            }
                        }
                        byte isThanthu = -1;
                        try {
                            isThanthu = msg.reader().readByte();
                        } catch (Exception e17) {
                        }
                        boolean canfocus = true;
                        try {
                            canfocus = msg.reader().readBoolean();
                        } catch (Exception e18) {
                        }
                        Canvas.gameScr.onActorMove(actorCatagory, type2, actorID, x3, y3, pk, idchar, isnewmonster, isThanthu, canfocus);
                    }
                    break;
                case 5:
                    if (Canvas.gameScr.readyGetGameLogic) {
                        short id2 = msg.reader().readShort();
                        Char charInfo = (Char) Canvas.gameScr.findCharByID(id2);
                        if (charInfo != null) {
                            charInfo.ID = id2;
                            if (charInfo.idBoss != -1) {
                                charInfo.dir = (short) 0;
                            }
                            charInfo.name = msg.reader().readUTF();
                            charInfo.x = msg.reader().readShort();
                            charInfo.y = msg.reader().readShort();
                            charInfo.hp = msg.reader().readInt();
                            charInfo.maxhp = msg.reader().readInt();
                            charInfo.mp = msg.reader().readInt();
                            charInfo.maxmp = msg.reader().readInt();
                            charInfo.currentHead = msg.reader().readByte();
                            charInfo.clazz = msg.reader().readByte();
                            msg.reader().read(charInfo.buffType, 0, charInfo.buffType.length);
                            for (int i41 = 0; i41 < charInfo.buffType.length; i41++) {
                                charInfo.countDown[i41] = msg.reader().readShort();
                            }
                            charInfo.killer = msg.reader().readShort();
                            charInfo.pk = msg.reader().readByte();
                            charInfo.defend = msg.reader().readShort();
                            charInfo.defend_magic = msg.reader().readShort();
                            charInfo.level = msg.reader().readByte();
                            charInfo.he = msg.reader().readByte();
                            byte hors = msg.reader().readByte();
                            if (hors >= 0) {
                                charInfo.useHorse = (byte) (hors - 1);
                            }
                            charInfo.anmFly = msg.reader().readByte();
                            charInfo.idImgHorse = msg.reader().readByte();
                            charInfo.speed = msg.reader().readByte();
                            charInfo.idClan = msg.reader().readShort();
                            charInfo.idBoss = msg.reader().readByte();
                            if (charInfo.idClan != -1) {
                                charInfo.isMaster = msg.reader().readByte();
                            }
                            charInfo.idFashion = msg.reader().readByte();
                            for (int i42 = 0; i42 < charInfo.idModel.length; i42++) {
                                charInfo.idModel[i42] = msg.reader().readShort();
                            }
                            charInfo.isNo = msg.reader().readBoolean();
                            charInfo.setCharInfo();
                            Canvas.gameScr.onCharInfo(charInfo);
                            if (msg.reader().readBoolean()) {
                                Canvas.gameScr.gameService.requestCharMonter(charInfo.ID);
                            }
                            charInfo.nationID = msg.reader().readByte();
                            charInfo.inCountry = msg.reader().readByte();
                            charInfo.paintHat = msg.reader().readBoolean();
                            try {
                                idhorse2 = msg.reader().readShort();
                                typehorse2 = msg.reader().readByte();
                            } catch (Exception e19) {
                                idhorse2 = -1;
                                typehorse2 = -1;
                            }
                            charInfo.setHorse(idhorse2, typehorse2);
                            try {
                                idwp2 = msg.reader().readShort();
                                typewp2 = msg.reader().readByte();
                                idimgwp2 = msg.reader().readShort();
                            } catch (Exception e20) {
                                idwp2 = -1;
                                typewp2 = -1;
                                idimgwp2 = -1;
                            }
                            charInfo.setWeapon(idwp2, typewp2, idimgwp2);
                            try {
                                idHair = msg.reader().readShort();
                                idBody = msg.reader().readShort();
                                idLeg = msg.reader().readShort();
                            } catch (Exception e21) {
                                idHair = -1;
                                idBody = -1;
                                idLeg = -1;
                            }
                            charInfo.setPartWearing(idHair, idBody, idLeg);
                            try {
                                int i43 = msg.reader().readByte();
                                short[] arrEff2 = new short[i43];
                                byte[] arrTick2 = new byte[i43];
                                for (int i44 = 0; i44 < i43; i44++) {
                                    arrEff2[i44] = msg.reader().readShort();
                                    arrTick2[i44] = msg.reader().readByte();
                                }
                                charInfo.setEff(arrEff2, arrTick2);
                                break;
                            } catch (Exception e22) {
                            }
                            try {
                                idPP1 = msg.reader().readShort();
                            } catch (Exception e23) {
                                idPP1 = -1;
                            }
                            charInfo.idPhiPhong = idPP1;
                            try {
                                indexpp2 = msg.reader().readByte();
                                idImgpp2 = msg.reader().readShort();
                                nframewp2 = msg.reader().readByte();
                                nframepp2 = msg.reader().readByte();
                            } catch (Exception e24) {
                                indexpp2 = -1;
                                idImgpp2 = -1;
                                nframewp2 = 0;
                                nframepp2 = 0;
                            }
                            charInfo.indexPP = indexpp2;
                            charInfo.idImgPP = idImgpp2;
                            charInfo.nFrameWP = nframewp2;
                            charInfo.nFramePP = nframepp2;
                            try {
                                indexAVT2 = msg.reader().readByte();
                                idBigavt2 = msg.reader().readShort();
                                nframeAVT2 = msg.reader().readByte();
                            } catch (Exception e25) {
                                indexAVT2 = -1;
                                idBigavt2 = -1;
                                nframeAVT2 = 0;
                            }
                            charInfo.indexAVT = indexAVT2;
                            charInfo.idImgAVT = idBigavt2;
                            charInfo.nFrameAVT = nframeAVT2;
                        }
                        break;
                    }
                    break;
                case 6:
                    short attacker2 = msg.reader().readShort();
                    short attacked2 = msg.reader().readShort();
                    byte skillID2 = msg.reader().readByte();
                    int hpLost2 = msg.reader().readInt();
                    int hpLeft2 = msg.reader().readInt();
                    byte effect2 = msg.reader().readByte();
                    byte x22 = msg.reader().readByte();
                    byte buffAttack2 = msg.reader().readByte();
                    byte level = msg.reader().readByte();
                    Canvas.gameScr.onPlayerAttackPlayer(attacker2, attacked2, skillID2, hpLost2, hpLeft2, effect2, x22, buffAttack2, level);
                    break;
                case 7:
                    MonsterInfo monsterInfo = new MonsterInfo();
                    monsterInfo.id = msg.reader().readShort();
                    monsterInfo.monster_type = (short) msg.reader().readUnsignedByte();
                    monsterInfo.x = msg.reader().readShort();
                    monsterInfo.y = msg.reader().readShort();
                    monsterInfo.hp = msg.reader().readInt();
                    monsterInfo.lv = msg.reader().readByte();
                    monsterInfo.he = msg.reader().readByte();
                    monsterInfo.maxhp = msg.reader().readInt();
                    monsterInfo.timeLive = msg.reader().readInt();
                    Canvas.gameScr.onMonsterInfo(monsterInfo);
                    break;
                case 8:
                    while (msg.reader().available() > 0) {
                        short charID = msg.reader().readShort();
                        Canvas.gameScr.charOutGame(charID);
                    }
                    break;
                case 9:
                    short attacker1 = msg.reader().readShort();
                    short attacked1 = msg.reader().readShort();
                    byte skillID1 = msg.reader().readByte();
                    int hpLost1 = msg.reader().readInt();
                    int hpLeft1 = msg.reader().readInt();
                    byte effect1 = msg.reader().readByte();
                    byte x21 = msg.reader().readByte();
                    byte buffAttack1 = msg.reader().readByte();
                    byte _level = msg.reader().readByte();
                    Canvas.gameScr.onPlayerAttackMonster(attacker1, attacked1, skillID1, hpLost1, hpLeft1, effect1, x21, buffAttack1, _level);
                    break;
                case 10:
                    short attacker = msg.reader().readShort();
                    short attacked = msg.reader().readShort();
                    int hpLost = msg.reader().readInt();
                    int hpLeft = msg.reader().readInt();
                    Canvas.gameScr.onMonsterAttackPlayer(attacker, attacked, hpLost, hpLeft);
                    break;
                case 11:
                    Canvas.gameScr.onPingBack();
                    break;
                case 12:
                    short map = msg.reader().readShort();
                    short toX = msg.reader().readShort();
                    short toY = msg.reader().readShort();
                    byte idxp = -1;
                    try {
                        idxp = msg.reader().readByte();
                    } catch (Exception e26) {
                        e26.printStackTrace();
                    }
                    short mapLoad = msg.reader().readShort();
                    String mapName = msg.reader().readUTF();
                    byte[] byteMap = (byte[]) null;
                    try {
                        boolean haveMap = msg.reader().readBoolean();
                        if (haveMap) {
                            int len2 = 0;
                            int byteRead = 0;
                            byteMap = new byte[msg.reader().available()];
                            while (len2 != -1 && byteRead < byteMap.length) {
                                len2 = msg.reader().read(byteMap, 0, byteMap.length - byteRead);
                                byteRead += len2;
                            }
                        }
                    } catch (Exception e27) {
                    }
                    Canvas.gameScr.onMap(map, toX, toY, mapLoad, mapName, byteMap);
                    Tilemap.idXaphu = idxp;
                    break;
                case 13:
                    int i45 = msg.reader().readByte();
                    if (i45 == -1) {
                        Canvas.startOKDlg(msg.reader().readUTF());
                    } else {
                        MainChar[] charInfos = new MainChar[i45];
                        for (int i46 = 0; i46 < i45; i46++) {
                            charInfos[i46] = new MainChar();
                            charInfos[i46].IDDB = msg.reader().readInt();
                            charInfos[i46].name = msg.reader().readUTF();
                            charInfos[i46].currentHead = msg.reader().readByte();
                            byte nItem = msg.reader().readByte();
                            for (int it2 = 0; it2 < nItem; it2++) {
                                byte itemType = msg.reader().readByte();
                                byte itemStype = msg.reader().readByte();
                                if (itemType == 0) {
                                    charInfos[i46].bodyStyle = itemStype;
                                } else if (itemType == 1) {
                                    charInfos[i46].legStyle = itemStype;
                                } else if (itemType == 2) {
                                    charInfos[i46].hatStyle = itemStype;
                                } else if (itemType == 3 || itemType == 4 || itemType == 5 || itemType == 6 || itemType == 7) {
                                    charInfos[i46].weaponType = itemType;
                                    charInfos[i46].weaponStyle = itemStype;
                                } else if (itemType == 19) {
                                    charInfos[i46].coatStyle = itemStype;
                                }
                            }
                            charInfos[i46].lastLv = msg.reader().readShort();
                            charInfos[i46].using = msg.reader().readByte();
                            charInfos[i46].nationID = msg.reader().readByte();
                            charInfos[i46].day = msg.reader().readShort();
                            byte indexColor = msg.reader().readByte();
                            if (indexColor != -1) {
                                int size6 = msg.reader().readShort();
                                byte[] header2 = new byte[size6];
                                for (int a10 = 0; a10 < size6; a10++) {
                                    header2[a10] = msg.reader().readByte();
                                }
                                int size9 = msg.reader().readShort();
                                byte[] data4 = new byte[size9];
                                for (int a11 = 0; a11 < size9; a11++) {
                                    data4[a11] = msg.reader().readByte();
                                }
                                charInfos[i46].imgWpa = Res.createImgByHeader(header2, data4);
                                charInfos[i46].dxWear = msg.reader().readByte();
                                charInfos[i46].dyWear = msg.reader().readByte();
                            }
                        }
                        if (charInfos != null) {
                            for (int i47 = 0; i47 < i45; i47++) {
                                try {
                                    cid = msg.reader().readInt();
                                } catch (Exception e28) {
                                    cid = -1;
                                }
                                if (cid != -1 && charInfos[i47].IDDB == cid) {
                                    try {
                                        idBigAvtAo = msg.reader().readShort();
                                        indexXyAo = msg.reader().readByte();
                                        nFrameAo = msg.reader().readByte();
                                        idBigAvtPP = msg.reader().readShort();
                                        indexXyPP = msg.reader().readByte();
                                        nFramePP = msg.reader().readByte();
                                        idBigAvtVK = msg.reader().readShort();
                                        indexXyVK = msg.reader().readByte();
                                        nFrameVK = msg.reader().readByte();
                                    } catch (Exception e29) {
                                        idBigAvtAo = -1;
                                        indexXyAo = -1;
                                        nFrameAo = -1;
                                        idBigAvtPP = -1;
                                        indexXyPP = -1;
                                        nFramePP = -1;
                                        idBigAvtVK = -1;
                                        indexXyVK = -1;
                                        nFrameVK = -1;
                                    }
                                    charInfos[i47].idImgAVT = idBigAvtAo;
                                    charInfos[i47].indexAVT = indexXyAo;
                                    charInfos[i47].nFrameAVT = nFrameAo;
                                    charInfos[i47].idImgPP = idBigAvtPP;
                                    charInfos[i47].indexPP = indexXyPP;
                                    charInfos[i47].nFramePP = nFramePP;
                                    charInfos[i47].idImageWeapon = idBigAvtVK;
                                    charInfos[i47].typeWeapon = indexXyVK;
                                    charInfos[i47].nFrameWP = nFrameVK;
                                    break;
                                }
                            }
                        }
                        CharSelectScr.gI().setCharList(charInfos);
                        CharSelectScr.gI().switchToMe();
                    }
                    break;
                case 14:
                    Canvas.startOKDlg("Xin chọn tên Pokémon khác");
                    break;
                case 15:
                    byte typeWearing = msg.reader().readByte();
                    if (typeWearing == 0) {
                        Vector v = new Vector();
                        short id = msg.reader().readShort();
                        int nItem2 = msg.reader().readByte();
                        for (int i48 = 0; i48 < nItem2; i48++) {
                            ItemInInventory cwi2 = new ItemInInventory();
                            byte b4 = msg.reader().readByte();
                            cwi2.clazz = b4;
                            cwi2.charClazz = b4;
                            cwi2.itemID = msg.reader().readShort();
                            cwi2.idTemplate = msg.reader().readShort();
                            cwi2.plusTemplate = msg.reader().readByte();
                            cwi2.level = msg.reader().readByte();
                            cwi2.mDurable = msg.reader().readShort();
                            cwi2.durable = msg.reader().readShort();
                            cwi2.colorName = msg.reader().readByte();
                            cwi2.viTriVe = msg.reader().readByte();
                            cwi2.heItem = msg.reader().readByte();
                            cwi2.beKicked = msg.reader().readByte();
                            cwi2.hangItem = msg.reader().readByte();
                            cwi2.magic_physic = msg.reader().readByte();
                            cwi2.lock = msg.reader().readByte();
                            cwi2.nameCharSeal = msg.reader().readUTF();
                            cwi2.allAttribute.removeAllElements();
                            cwi2.lastTime = System.currentTimeMillis();
                            cwi2.dayUse = msg.reader().readUnsignedShort();
                            byte len4 = msg.reader().readByte();
                            for (byte ii4 = 0; ii4 < len4; ii4 = (byte) (ii4 + 1)) {
                                InfoAttributeItem info6 = new InfoAttributeItem((short) msg.reader().readUnsignedByte(), msg.reader().readShort());
                                cwi2.allAttribute.addElement(info6);
                            }
                            cwi2.isEnoughData = true;
                            v.addElement(cwi2);
                        }
                        byte idFashion = msg.reader().readByte();
                        short[] idModel = new short[5];
                        idModel[0] = -1;
                        idModel[1] = -1;
                        idModel[2] = -1;
                        idModel[3] = -1;
                        idModel[4] = -1;
                        for (int i49 = 0; i49 < idModel.length; i49++) {
                            idModel[i49] = msg.reader().readShort();
                        }
                        byte typeAnimal = msg.reader().readByte();
                        byte idIconAnimal = 0;
                        String infoAnimal2 = "";
                        if (typeAnimal != -1) {
                            infoAnimal2 = msg.reader().readUTF();
                            idIconAnimal = msg.reader().readByte();
                        }
                        byte typePet = msg.reader().readByte();
                        AnimalMove pet = null;
                        if (typePet != -1) {
                            pet = new AnimalMove();
                            pet.type = msg.reader().readByte();
                            pet.imgID = msg.reader().readShort();
                            pet.sumFrame = msg.reader().readByte();
                            pet.infoPet = msg.reader().readUTF();
                            pet.totalTime = msg.reader().readInt();
                            pet.timeStart = System.currentTimeMillis();
                            pet.idFrame = (short) 0;
                            pet.count = (short) 0;
                            pet.catagory = (byte) 12;
                        }
                        Canvas.gameScr.onCharWearingInfo(id, v, idFashion, typeAnimal, infoAnimal2, pet, idModel, idIconAnimal);
                    } else {
                        Vector infoAnimal3 = new Vector();
                        short idChar2 = msg.reader().readShort();
                        int nItemAnimal2 = msg.reader().readByte();
                        if (nItemAnimal2 > -1) {
                            for (int i50 = 0; i50 < nItemAnimal2; i50++) {
                                ItemInInventory cwi3 = new ItemInInventory();
                                byte b5 = msg.reader().readByte();
                                cwi3.clazz = b5;
                                cwi3.charClazz = b5;
                                cwi3.itemID = msg.reader().readShort();
                                cwi3.idTemplate = msg.reader().readShort();
                                cwi3.plusTemplate = msg.reader().readByte();
                                cwi3.level = msg.reader().readByte();
                                cwi3.mDurable = msg.reader().readShort();
                                cwi3.durable = msg.reader().readShort();
                                cwi3.colorName = msg.reader().readByte();
                                cwi3.viTriVe = msg.reader().readByte();
                                cwi3.heItem = msg.reader().readByte();
                                cwi3.beKicked = msg.reader().readByte();
                                cwi3.hangItem = msg.reader().readByte();
                                cwi3.magic_physic = msg.reader().readByte();
                                cwi3.lock = msg.reader().readByte();
                                cwi3.nameCharSeal = msg.reader().readUTF();
                                cwi3.allAttribute.removeAllElements();
                                byte len5 = msg.reader().readByte();
                                for (byte ii5 = 0; ii5 < len5; ii5 = (byte) (ii5 + 1)) {
                                    InfoAttributeItem info7 = new InfoAttributeItem((short) msg.reader().readUnsignedByte(), msg.reader().readShort());
                                    cwi3.allAttribute.addElement(info7);
                                }
                                cwi3.isEnoughData = true;
                                infoAnimal3.addElement(cwi3);
                            }
                            byte numFrame2 = msg.reader().readByte();
                            Image imgAnimal2 = null;
                            int wimg2 = 0;
                            int himg2 = 0;
                            byte timeChangeFrame2 = 1;
                            byte[] arr2 = new byte[msg.reader().available()];
                            while (msg.reader().available() > 0) {
                                msg.reader().read(arr2, 0, arr2.length);
                            }
                            if (arr2.length > 0) {
                                imgAnimal2 = Res.createImgByByteArray(arr2);
                                timeChangeFrame2 = (byte) (numFrame2 == 3 ? 3 : 6);
                                if (imgAnimal2 != null) {
                                    wimg2 = imgAnimal2.getWidth();
                                    himg2 = imgAnimal2.getHeight() / numFrame2;
                                }
                            }
                            WindowInfoScr.gI().countFrame = 0;
                            Canvas.gameScr.onAnimalWearingInfo(idChar2, infoAnimal3, imgAnimal2, numFrame2, wimg2, himg2, timeChangeFrame2);
                        }
                    }
                    break;
                case 16:
                    int type_inventory = msg.reader().readByte();
                    int[] potion = new int[Char.CHAR_POTION];
                    long charxu = 0;
                    if (type_inventory == 0) {
                        try {
                            charxu = msg.reader().readLong();
                            potion[0] = 0;
                            int spotion = msg.reader().readUnsignedByte();
                            for (int i51 = 0; i51 < spotion; i51++) {
                                int id4 = msg.reader().readUnsignedByte();
                                int value = msg.reader().readInt();
                                MainChar.listPotion[id4].number = value;
                                potion[id4] = value;
                            }
                        } catch (Exception e30) {
                            Cout.println(new StringBuffer("LOI CHARINVENTORY ").append(type_inventory).append(e30.toString()).toString());
                            return;
                        }
                    }
                    Vector items = new Vector();
                    if (type_inventory == 1) {
                        int nItem1 = msg.reader().readShort();
                        for (int i52 = 0; i52 < nItem1; i52++) {
                            ItemInInventory cii = new ItemInInventory();
                            cii.charClazz = msg.reader().readByte();
                            cii.itemID = msg.reader().readShort();
                            cii.idTemplate = msg.reader().readShort();
                            cii.plusTemplate = msg.reader().readByte();
                            cii.level = msg.reader().readByte();
                            cii.mDurable = msg.reader().readShort();
                            cii.durable = msg.reader().readShort();
                            cii.clazz = msg.reader().readByte();
                            cii.numKham = msg.reader().readByte();
                            cii.totalKham = msg.reader().readByte();
                            cii.colorName = msg.reader().readByte();
                            cii.heItem = msg.reader().readByte();
                            cii.beKicked = msg.reader().readByte();
                            cii.hangItem = msg.reader().readByte();
                            cii.magic_physic = msg.reader().readByte();
                            cii.lock = msg.reader().readByte();
                            cii.nameCharSeal = msg.reader().readUTF();
                            cii.allAttribute.removeAllElements();
                            cii.lastTime = System.currentTimeMillis();
                            cii.dayUse = msg.reader().readUnsignedShort();
                            byte len6 = msg.reader().readByte();
                            for (byte ii6 = 0; ii6 < len6; ii6 = (byte) (ii6 + 1)) {
                                InfoAttributeItem info8 = new InfoAttributeItem((short) msg.reader().readUnsignedByte(), msg.reader().readShort());
                                cii.allAttribute.addElement(info8);
                            }
                            cii.isItemDrop = msg.reader().readByte();
                            cii.isEnoughData = true;
                            items.addElement(cii);
                        }
                    }
                    Canvas.gameScr.mainChar.luong = msg.reader().readInt();
                    Vector animal = new Vector();
                    if (type_inventory == 2) {
                        int nanimal = msg.reader().readByte();
                        for (int i53 = 0; i53 < nanimal; i53++) {
                            AnimalInfo anm = new AnimalInfo();
                            anm.id = msg.reader().readShort();
                            anm.idImg = msg.reader().readByte();
                            anm.idPaint = msg.reader().readByte();
                            anm.level = msg.reader().readByte();
                            anm.name = msg.reader().readUTF();
                            anm.info = msg.reader().readUTF();
                            anm.typeAnimal = msg.reader().readByte();
                            animal.addElement(anm);
                        }
                        int nanimal2 = msg.reader().readByte();
                        for (int i54 = 0; i54 < nanimal2; i54++) {
                            AnimalInfo anm2 = new AnimalInfo();
                            anm2.id = msg.reader().readShort();
                            anm2.idImg = msg.reader().readByte();
                            anm2.idPaint = msg.reader().readByte();
                            anm2.name = msg.reader().readUTF();
                            anm2.typePet = msg.reader().readByte();
                            anm2.info = msg.reader().readUTF();
                            anm2.totalTimeCon = msg.reader().readInt();
                            anm2.timeStart = System.currentTimeMillis();
                            anm2.typeAnimal = msg.reader().readByte();
                            animal.addElement(anm2);
                        }
                    }
                    if (type_inventory == 3) {
                        MainChar.itemQuest = new short[msg.reader().readByte()];
                        for (int i55 = 0; i55 < MainChar.itemQuest.length; i55++) {
                            MainChar.itemQuest[i55] = msg.reader().readShort();
                        }
                    }
                    Canvas.gameScr.mainChar.luongKhoa = msg.reader().readInt();
                    Canvas.gameScr.onCharInventory(charxu, potion, items, animal, type_inventory);
                    game.pokemon.TrangThaiNguoiChoi.doi(type_inventory == 0 ? game.pokemon.TrangThaiNguoiChoi.TIEN | game.pokemon.TrangThaiNguoiChoi.TUI : game.pokemon.TrangThaiNguoiChoi.TUI);
                    break;
                case 17:
                    try {
                        MonsterDieInfo mdi = new MonsterDieInfo();
                        mdi.attacker = msg.reader().readShort();
                        mdi.attacked = msg.reader().readShort();
                        mdi.skillID = msg.reader().readByte();
                        mdi.hpLost = msg.reader().readInt();
                        mdi.effect = msg.reader().readByte();
                        mdi.itemDrop = new ItemDropInfo[msg.reader().readByte()];
                        for (int i56 = 0; i56 < mdi.itemDrop.length; i56++) {
                            mdi.itemDrop[i56] = new ItemDropInfo();
                            mdi.itemDrop[i56].itemCatagory = msg.reader().readByte();
                            String info9 = new StringBuffer(String.valueOf((int) mdi.itemDrop[i56].itemCatagory)).append("_").toString();
                            mdi.itemDrop[i56].itemTemplateID = msg.reader().readShort();
                            String info10 = new StringBuffer(String.valueOf(info9)).append((int) mdi.itemDrop[i56].itemTemplateID).append("_").toString();
                            mdi.itemDrop[i56].itemID = msg.reader().readShort();
                            String info11 = new StringBuffer(String.valueOf(info10)).append((int) mdi.itemDrop[i56].itemID).append("_").toString();
                            mdi.itemDrop[i56].x = msg.reader().readShort();
                            String info12 = new StringBuffer(String.valueOf(info11)).append((int) mdi.itemDrop[i56].x).append("_").toString();
                            mdi.itemDrop[i56].y = msg.reader().readShort();
                            new StringBuffer(String.valueOf(info12)).append((int) mdi.itemDrop[i56].y).append("_").toString();
                        }
                        if (msg.reader().available() > 0) {
                            mdi.x2 = msg.reader().readByte();
                        }
                        if (msg.reader().available() > 0) {
                            mdi.buffAttack = msg.reader().readByte();
                        }
                        if (msg.reader().available() > 0) {
                            mdi.level = msg.reader().readByte();
                        }
                        Canvas.gameScr.onMonsterDie(mdi);
                    } catch (Exception e31) {
                        Cout.println(new StringBuffer("LOI TRONG HAM NHAN DATA ").append(e31.toString()).toString());
                        return;
                    }
                    break;
                case 18:
                    short whoGet = msg.reader().readShort();
                    ItemInInventory cii2 = new ItemInInventory();
                    byte b6 = msg.reader().readByte();
                    cii2.charClazz = b6;
                    cii2.clazz = b6;
                    cii2.itemOldID = msg.reader().readShort();
                    cii2.itemID = msg.reader().readShort();
                    cii2.idTemplate = msg.reader().readShort();
                    cii2.plusTemplate = msg.reader().readByte();
                    cii2.level = msg.reader().readByte();
                    cii2.durable = msg.reader().readShort();
                    cii2.mDurable = msg.reader().readShort();
                    Canvas.gameScr.onGetItemFromGround(whoGet, cii2);
                    break;
                case 19:
                    short whoGet2 = msg.reader().readShort();
                    short potionID = msg.reader().readShort();
                    short potionType1 = (short) msg.reader().readUnsignedByte();
                    short potionQuantity = msg.reader().readShort();
                    Canvas.gameScr.onGetPotionFromGround(whoGet2, potionID, potionType1, potionQuantity);
                    break;
                case 20:
                    byte actorCat = msg.reader().readByte();
                    short actorID2 = msg.reader().readShort();
                    Canvas.gameScr.onRemoveActor(actorID2, actorCat);
                    break;
                case 21:
                    ItemInInventory item6 = new ItemInInventory();
                    item6.itemID = msg.reader().readShort();
                    item6.durable = msg.reader().readShort();
                    item6.clazz = msg.reader().readByte();
                    item6.dayUse = msg.reader().readUnsignedShort();
                    item6.allAttribute.removeAllElements();
                    byte len7 = msg.reader().readByte();
                    for (byte i57 = 0; i57 < len7; i57 = (byte) (i57 + 1)) {
                        InfoAttributeItem info13 = new InfoAttributeItem((short) msg.reader().readUnsignedByte(), msg.reader().readShort());
                        item6.allAttribute.addElement(info13);
                    }
                    item6.plusTemplate = msg.reader().readByte();
                    try {
                        item6.lock = msg.reader().readByte();   // pokemon: bản dịch ngược có "break" ở đây -> có byte tuỳ chọn thì bỏ qua hàm xử lý (Unity không break)
                    } catch (Exception e32) {
                    }
                    Canvas.gameScr.onItemInfo(item6);
                    break;
                case 22: {
                    // pokemon: bản dịch ngược có "break" trong try đọc cờ isHP -> server gửi cờ (bình HP) thì
                    // onUsePotion KHÔNG chạy, HP trên HUD cũ tới gói định kỳ sau (game.pokemon.MauPP)
                    int[] v22 = game.pokemon.MauPP.doc22(msg.reader());
                    Canvas.gameScr.onUsePotion((short) v22[0], (byte) v22[1], (short) v22[2], v22[3], v22[4]);
                    break;
                }
                case 23:
                    byte npcActionType = msg.reader().readByte();
                    if (npcActionType == 0) {
                        int i58 = msg.reader().readByte();
                        byte[] potionsSell = new byte[i58];
                        for (int i59 = 0; i59 < i58; i59++) {
                            potionsSell[i59] = msg.reader().readByte();
                        }
                        Canvas.gameScr.onNPCSellPotion(potionsSell);
                    }
                    if (npcActionType == 1) {
                        int cl = msg.reader().readByte();
                        WindowInfoScr.gI().idClass = cl;
                        Vector itemsSell = new Vector();
                        int nItemSell = msg.reader().readShort();
                        for (int i60 = 0; i60 < nItemSell; i60++) {
                            ItemInInventory sellItem = new ItemInInventory();
                            sellItem.isEnoughData = true;
                            sellItem.isShopItem = true;
                            int idClazz = msg.reader().readShort();
                            if (cl == -1) {
                                sellItem.charClazz = (byte) getIndexClassFromItemID(idClazz);
                            } else {
                                sellItem.charClazz = (byte) cl;
                            }
                            sellItem.idTemplate = (short) idClazz;
                            sellItem.clazz = (byte) (cl == -1 ? getIndexClassFromItemID(idClazz) : cl);
                            ItemTemplate item2 = Res.getItem(sellItem.charClazz, idClazz);
                            sellItem.level = item2.level;
                            sellItem.durable = item2.durable;
                            itemsSell.addElement(sellItem);
                        }
                        try {
                            ItemInInventory.typeItemBuy = (byte) 2;
                            ItemInInventory.typeItemBuy = msg.reader().readByte();   // pokemon: bản dịch ngược có "break" ở đây -> có byte tuỳ chọn thì bỏ qua hàm xử lý (Unity không break)
                        } catch (Exception e34) {
                        }
                        Canvas.gameScr.onNPCSellItem(itemsSell);
                    }
                    if (npcActionType == 2) {
                        try {
                            Vector itemBag = new Vector();
                            byte nbag = msg.reader().readByte();
                            for (int i61 = 0; i61 < nbag; i61++) {
                                ItemInInventory cb = new ItemInInventory();
                                byte cl2 = msg.reader().readByte();
                                cb.itemID = msg.reader().readShort();
                                short idTemplate = msg.reader().readShort();
                                if (idTemplate < 0) {
                                    idTemplate = (short) (idTemplate + 256);
                                }
                                cb.charClazz = cl2;
                                cb.idTemplate = idTemplate;
                                cb.plusTemplate = msg.reader().readByte();
                                cb.level = msg.reader().readByte();
                                cb.mDurable = msg.reader().readShort();
                                cb.durable = msg.reader().readShort();
                                cb.clazz = cl2;
                                itemBag.addElement(cb);
                            }
                            Canvas.gameScr.onNPCKeepItem(itemBag);
                        } catch (Exception e35) {
                            e35.printStackTrace();
                            Cout.println("LOI NHAN ITEM TRONG KHO DO");
                        }
                    }
                    if (npcActionType == 3) {
                        Canvas.gameScr.onNPCSellGemItem(null);
                    }
                    if (npcActionType == 4) {
                        byte nshop = msg.reader().readByte();
                        byte[] list2 = new byte[nshop + 2];
                        for (int i62 = 0; i62 < nshop; i62++) {
                            list2[i62] = msg.reader().readByte();
                        }
                        list2[nshop] = msg.reader().readByte();
                        list2[nshop + 1] = msg.reader().readByte();
                        Canvas.gameScr.onListShopOfNPC(list2);
                    }
                    break;
                case 24:
                    Canvas.endDlg();
                    break;
                case 25:
                    int nNameAttribute = msg.reader().readUnsignedByte();
                    ItemTemplate.ALL_NAME_ATTRIBUTE_ITEM.clear();
                    for (int i63 = 0; i63 < nNameAttribute; i63++) {
                        ItemTemplate.ALL_NAME_ATTRIBUTE_ITEM.put(new StringBuffer(String.valueOf(i63)).toString(), new NameAttributeItem((short) msg.reader().readUnsignedByte(), msg.reader().readUTF(), msg.reader().readByte(), msg.reader().readByte()));
                    }
                    int npotion = msg.reader().readByte();
                    for (int i64 = 0; i64 < npotion; i64++) {
                        Res.potionTemplates[i64] = new PotionTemplate(msg.reader().readShort(), msg.reader().readShort());
                    }
                    for (int i65 = 0; i65 < 5; i65++) {
                        Res.percent[i65] = msg.reader().readByte();
                    }
                    int n = msg.reader().readShort();
                    ItemTemplate[] itemBasic = new ItemTemplate[n + 1];
                    for (int i66 = 0; i66 < n; i66++) {
                        int idi = msg.reader().readShort();
                        try {
                            itemBasic[idi] = new ItemTemplate();
                            itemBasic[idi].id = (short) idi;
                            itemBasic[idi].name = msg.reader().readUTF();
                            itemBasic[idi].type = msg.reader().readByte();
                            itemBasic[idi].style = msg.reader().readByte();
                            itemBasic[idi].he = msg.reader().readByte();
                            itemBasic[idi].gender = msg.reader().readByte();
                            itemBasic[idi].level = msg.reader().readByte();
                            itemBasic[idi].durable = msg.reader().readShort();
                            for (int k8 = 0; k8 < 10; k8++) {
                                itemBasic[idi].attb[k8] = msg.reader().readShort();
                            }
                            itemBasic[idi].price = msg.reader().readInt();
                            itemBasic[idi].clazz = msg.reader().readByte();
                            itemBasic[idi].colorItem = msg.reader().readByte();
                            itemBasic[idi].idIcon = msg.reader().readShort();
                            itemBasic[idi].ndayLoan = msg.reader().readShort();
                        } catch (Exception e36) {
                            e36.printStackTrace();
                        }
                    }
                    Res.itemTemplates.addElement(itemBasic);
                    short nGem = msg.reader().readShort();
                    Res.gemTemplate.removeAllElements();
                    for (int i67 = 0; i67 < nGem; i67++) {
                        GemTemp gemItem = new GemTemp();
                        gemItem.id = msg.reader().readShort();
                        gemItem.idImage = msg.reader().readByte();
                        gemItem.price = msg.reader().readInt();
                        gemItem.name = msg.reader().readUTF();
                        String str2 = gemItem.name.substring(gemItem.name.length() - 1);
                        gemItem.decript = msg.reader().readUTF();
                        gemItem.type = msg.reader().readByte();
                        try {
                            int aa = Integer.parseInt(str2);
                            if (aa >= 4 && gemItem.type != 4) {
                                gemItem.isEff = (byte) (aa - 4);
                            }
                        } catch (Exception e37) {
                        }
                        gemItem.isSell = msg.reader().readBoolean();
                        gemItem.typeEp = msg.reader().readByte();
                        gemItem.typeMoney = msg.reader().readByte();
                        Res.gemTemplate.addElement(gemItem);
                    }
                    int nshop2 = msg.reader().readUnsignedByte();
                    Res.shopTemplate.removeAllElements();
                    for (int i68 = 0; i68 < nshop2; i68++) {
                        GemTemp shop = new GemTemp();
                        shop.id = (short) msg.reader().readUnsignedByte();
                        shop.idImage = (short) msg.reader().readUnsignedByte();
                        shop.price = msg.reader().readInt();
                        shop.name = msg.reader().readUTF();
                        shop.decript = msg.reader().readUTF();
                        shop.typeMoney = msg.reader().readByte();
                        shop.shopType = msg.reader().readByte();
                        shop.isSell = msg.reader().readBoolean();
                        shop.value = msg.reader().readShort();
                        Res.shopTemplate.addElement(shop);
                    }
                    break;
                case 26:
                    int nMonster = msg.reader().readUnsignedByte();
                    Res.monsterTemplates = new MonsterTemplate[nMonster];
                    // pokemon: bang Pokemon cua template gui lai bang -91 ngay sau lenh nay
                    MonsterTemplate.xoaPoke();
                    byte[] effSh = new byte[msg.reader().readShort()];
                    msg.reader().read(effSh);
                    Res.setEffSh(effSh);
                    try {
                        int unsignedByte = (short) msg.reader().readUnsignedByte();
                        byte[][] deff = new byte[unsignedByte][];
                        for (int i69 = 0; i69 < unsignedByte; i69++) {
                            deff[i69] = new byte[msg.reader().readShort()];
                            msg.reader().read(deff[i69]);
                        }
                        DynamicEffect.setDataEffect(deff);
                    } catch (Exception e38) {
                        e38.printStackTrace();
                    }
                    try {
                        short leneffsh = (short) msg.reader().readUnsignedByte();
                        for (int i70 = 0; i70 < leneffsh; i70++) {
                            int id5 = msg.reader().readShort();
                            byte[] deff2 = new byte[msg.reader().readShort()];
                            msg.reader().read(deff2);
                            mDataEffect meff = new mDataEffect(deff2, id5, true);
                            Pet.ALL_PET_DATA.put(new StringBuffer().append(id5).toString(), meff);
                        }
                    } catch (Exception e39) {
                    }
                    try {
                        int leneff = msg.reader().readShort();
                        for (int i71 = 0; i71 < leneff; i71++) {
                            int id6 = msg.reader().readShort();
                            byte[] effdata = new byte[msg.reader().readShort()];
                            msg.reader().read(effdata);
                            PartFrame part = new PartFrame(id6, effdata);
                            DataSkillEff.ALL_DATA_EFF.put(new StringBuffer(String.valueOf(id6)).toString(), part);
                        }
                    } catch (Exception e40) {
                    }
                    try {
                        int i72 = msg.reader().readByte();
                        Char.dx_horse = new byte[i72][];
                        Char.dy_horse = new byte[i72][];
                        for (int i73 = 0; i73 < i72; i73++) {
                            Char.dx_horse[i73] = new byte[4];
                            Char.dy_horse[i73] = new byte[4];
                            for (int j12 = 0; j12 < 4; j12++) {
                                Char.dx_horse[i73][j12] = msg.reader().readByte();
                            }
                            for (int j13 = 0; j13 < 4; j13++) {
                                Char.dy_horse[i73][j13] = msg.reader().readByte();
                            }
                        }
                    } catch (Exception e41) {
                        Char.dx_horse = new byte[][]{new byte[]{0, 0, 7, -7}};
                        Char.dy_horse = new byte[][]{new byte[]{-20, -15, -15, -15}};
                    }
                    try {
                        for (int k = 0; k < 2; k++) {
                            int dai = msg.reader().readByte();
                            Char.Dx_Dy_WP[k] = new byte[dai];
                            for (int l = 0; l < dai; l++) {
                                Char.Dx_Dy_WP[k][l] = msg.reader().readByte();
                            }
                        }
                    } catch (Exception e) {
                        Char.Dx_Dy_WP = new byte[][]{new byte[1], new byte[1]};
                    }
                    try {
                        for (int k = 0; k < 2; k++) {
                            int dai = msg.reader().readByte();
                            Char.Dx_Dy_PP[k] = new byte[dai];
                            for (int l = 0; l < dai; l++) {
                                Char.Dx_Dy_PP[k][l] = msg.reader().readByte();
                            }
                        }
                    } catch (Exception e) {
                        Char.Dx_Dy_PP = new byte[][]{new byte[1], new byte[1]};
                    }
                    try {
                        for (int k = 0; k < 2; k++) {
                            int dai = msg.reader().readByte();
                            Char.Dx_Dy_AVT[k] = new byte[dai];
                            for (int l = 0; l < dai; l++) {
                                Char.Dx_Dy_AVT[k][l] = msg.reader().readByte();
                            }
                        }
                    } catch (Exception e) {
                        Char.Dx_Dy_AVT = new byte[][]{new byte[1], new byte[1]};
                    }
                    break;
                case 27:
                    short chatFromID = msg.reader().readShort();
                    String text = msg.reader().readUTF();
                    Canvas.gameScr.onChat(chatFromID, text);
                    break;
                case 28:
                    short itemID = msg.reader().readShort();
                    Canvas.gameScr.onSellItem(itemID);
                    break;
                case 30:
                    short whoSetXP = msg.reader().readShort();
                    short newpercent = msg.reader().readShort();
                    int dxp = msg.reader().readInt();
                    Canvas.gameScr.onSetXP(whoSetXP, newpercent, dxp);
                    break;
                case 32:
                    short idC = msg.reader().readShort();
                    int i76 = msg.reader().readByte();
                    Property[] ps = new Property[i76];
                    for (int i77 = 0; i77 < i76; i77++) {
                        ps[i77] = new Property();
                        ps[i77].key = msg.reader().readUTF();
                        ps[i77].value = msg.reader().readInt();
                    }
                    if (idC == Canvas.gameScr.mainChar.ID) {
                        Canvas.gameScr.mainChar.level = msg.reader().readByte();
                        Canvas.gameScr.mainChar.lvpercent = msg.reader().readShort();
                    }
                    Canvas.gameScr.onCharProperties(idC, ps);
                    break;
                case 33:
                    short idc = msg.reader().readShort();
                    byte newlevel = msg.reader().readByte();
                    Canvas.gameScr.onLevelUp(idc, newlevel, msg.reader().readInt(), msg.reader().readInt());
                    break;
                case 34:
                    Canvas.endDlg();
                    break;
                case 35:
                    SkillManager.SKILL_DAM_PERCENT = new short[1][15][11];
                    for (int j15 = 0; j15 < 15; j15++) {
                        for (int k9 = 0; k9 < 11; k9++) {
                            SkillManager.SKILL_DAM_PERCENT[0][j15][k9] = msg.reader().readShort();
                        }
                    }
                    SkillManager.SKILL_COOLDOWN = new int[1][15][11];
                    for (int j16 = 0; j16 < 15; j16++) {
                        for (int k10 = 0; k10 < 11; k10++) {
                            SkillManager.SKILL_COOLDOWN[0][j16][k10] = msg.reader().readShort() * 100;
                        }
                    }
                    SkillManager.SKILL_RANGE = new short[1][15];
                    for (int j17 = 0; j17 < 15; j17++) {
                        SkillManager.SKILL_RANGE[0][j17] = msg.reader().readShort();
                    }
                    SkillManager.SKILL_MP = new short[1][15][11];
                    for (int j18 = 0; j18 < 15; j18++) {
                        for (int k11 = 0; k11 < 11; k11++) {
                            SkillManager.SKILL_MP[0][j18][k11] = (short) msg.reader().readUnsignedByte();
                        }
                    }
                    SkillManager.TIME_LIFE_BUFF_SKILL = new short[15][11];
                    for (int i78 = 0; i78 < 15; i78++) {
                        for (int k12 = 0; k12 < 11; k12++) {
                            SkillManager.TIME_LIFE_BUFF_SKILL[i78][k12] = msg.reader().readShort();
                        }
                    }
                    SkillManager.LEVEL_ADD_SKILL = new byte[15][11];
                    for (int j19 = 0; j19 < 15; j19++) {
                        for (int k13 = 0; k13 < 11; k13++) {
                            SkillManager.LEVEL_ADD_SKILL[j19][k13] = msg.reader().readByte();
                        }
                    }
                    SkillManager.SKILL_AEO = new byte[5][];
                    for (int i79 = 0; i79 < 5; i79++) {
                        SkillManager.SKILL_AEO[i79] = new byte[msg.reader().readByte()];
                        for (int j20 = 0; j20 < SkillManager.SKILL_AEO[i79].length; j20++) {
                            SkillManager.SKILL_AEO[i79][j20] = msg.reader().readByte();
                        }
                    }
                    break;
                case 36:
                    Canvas.endDlg();
                    break;
                case 37:
                    game.pokemon.TuNoiLai.coThongBao();   // 0.9.4: server vừa báo lý do -> rớt ngay sau không tự nối lại
                    String message = msg.reader().readUTF();
                    final String url = msg.reader().readUTF();
                    // pokemon (hệ sinh thái): kết quả câu cá hiện sau hoạt cảnh cá cắn; đọc mốc đua / điểm đào
                    if (url.equals("") && game.pokemon.SinhThai.chanThongBao(message)) {
                        break;
                    }
                    if (url.equals("")) {
                        if (game.pokemon.ThongBao.trongGame()) {
                            // 0.9.6.1: trong game, phân loại theo nội dung (game.pokemon.ThongBao): kết quả / lỗi / hiếm
                            // = thẻ nhanh không chặn, boss = biểu ngữ trên; chỉ việc nghiêm trọng / tin dài mới là hộp
                            game.pokemon.ThongBao.tuMayChu(message);
                        } else {
                            Canvas.startOKDlg(message);
                        }
                    } else {
                        Canvas.gameScr.isNewVersionAvailable = true;
                        IAction actionOpenURL = new IAction() {
                            @Override
                            public void perform() {
                                try {
                                    GameMidlet.instance.platformRequest(url);
                                    Thread.sleep(500L);
                                } catch (Exception e45) {
                                }
                                GameMidlet.instance.notifyDestroyed();
                            }
                        };
                        if (game.pokemon.MenuNguCanh.bat()) {
                            game.pokemon.HopXacNhan.hoi("Xác nhận", message, "Mở", actionOpenURL, null);
                            break;
                        }
                        Canvas.msgdlg.isIcon = false;
                        Canvas.msgdlg.setInfo(message, new Command("OK", actionOpenURL), new Command("", actionOpenURL), new Command("Đóng", new IAction() {
                            @Override
                            public void perform() {
                                Canvas.endDlg();
                            }
                        }));
                        Canvas.currentDialog = Canvas.msgdlg;
                    }
                    break;
                case 38:
                    String str3 = msg.reader().readUTF();
                    GameScr.serverInfo.addElement(str3);
                    break;
                case 39:
                    // Đăng ký trong game (giao thức mới, sau chuỗi "#dk2") - xem DangKyScr
                    if (game.render.screen.DangKyScr.DAU.equals(msg.reader().readUTF())) {
                        game.render.screen.DangKyScr.nhan(msg.reader());
                    }
                    break;
                case 48:
                    Canvas.gameScr.onCreadtePTOK(msg.reader().readShort());
                    break;
                case 49:
                    Canvas.gameScr.onAdd2Party(msg);
                    break;
                case 50:
                    Canvas.gameScr.onKickOutParty(msg);
                    break;
                case 51:
                    Canvas.gameScr.onCharUseBuff(msg);
                    break;
                case 52:
                    Canvas.gameScr.receiveQuest(msg);
                    break;
                case 54:
                    Canvas.gameScr.onFinishQuest(msg);
                    break;
                case 56:
                    Canvas.gameScr.onInfoQuestLogin(msg);
                    break;
                case 57:
                    break;
                case 58:
                    int nMst = msg.reader().readByte();
                    int[] idMonster = new int[nMst];
                    for (int i80 = 0; i80 < nMst; i80++) {
                        idMonster[i80] = msg.reader().readShort();
                    }
                    break;
                case 59:
                    Canvas.gameScr.onInfoGiftQuest(msg.reader().readUTF());
                    break;
                case 60:
                    Canvas.gameScr.onNewHPMP(msg);
                    break;
                case 61:
                    short cid2 = msg.reader().readShort();
                    short iditem = msg.reader().readShort();
                    ItemDropInfo iteminfo = new ItemDropInfo();
                    iteminfo.itemCatagory = msg.reader().readByte();
                    iteminfo.itemTemplateID = msg.reader().readByte();
                    iteminfo.itemID = msg.reader().readShort();
                    iteminfo.x = msg.reader().readShort();
                    iteminfo.y = msg.reader().readShort();
                    Canvas.gameScr.onGiveItemToGround(cid2, iditem, iteminfo);
                    break;
                case 62:
                    Canvas.gameScr.onItemShopOnline(msg);
                    break;
                case 64:
                    ItemDropInfo[] drop_list = new ItemDropInfo[msg.reader().readByte()];
                    for (int i81 = 0; i81 < drop_list.length; i81++) {
                        drop_list[i81] = new ItemDropInfo();
                        drop_list[i81].itemCatagory = msg.reader().readByte();
                        drop_list[i81].itemTemplateID = msg.reader().readByte();
                        drop_list[i81].itemID = msg.reader().readShort();
                        drop_list[i81].x = msg.reader().readShort();
                        drop_list[i81].y = msg.reader().readShort();
                    }
                    break;
                case 65:
                    Canvas.gameScr.onUseItemPK(msg.reader().readShort(), msg.reader().readByte(), msg.reader().readByte());
                    break;
                case 66:
                    byte cmd = msg.reader().readByte();
                    if (cmd == 0) {
                        Canvas.gameScr.onRequestTrade(msg.reader().readShort());
                    } else if (cmd == -2) {
                        Canvas.startOKDlg("Người bạn mời đang trao đổi với người khác");
                    } else if (cmd == 1) {
                        Canvas.gameScr.onOkTrade(msg.reader().readShort());
                    } else if (cmd == -1) {
                        Canvas.gameScr.onDeniedTrade(msg.reader().readShort());
                    } else if (cmd == 2) {
                        byte typeT = msg.reader().readByte();
                        Canvas.gameScr.onItemTrade(msg.reader().readShort(), msg, typeT);
                    } else if (cmd == 3) {
                        Canvas.gameScr.oncancelTrade();
                    } else if (cmd == 4) {
                        Canvas.gameScr.onFinishTrade();
                    } else if (cmd == 5) {
                        Canvas.gameScr.onConfirmTrade();
                    }
                    break;
                case 67:
                    Canvas.gameScr.onKiller(msg.reader().readShort(), msg.reader().readByte(), msg.reader().readShort());
                    break;
                case 68:
                    Canvas.gameScr.onPutItem2Bag(msg.reader().readShort());
                    Canvas.endDlg();
                    break;
                case 69:
                    Canvas.gameScr.onGetItemFromBag(msg.reader().readShort());
                    Canvas.endDlg();
                    break;
                case 71:
                    msg.reader().readByte();
                    short price = msg.reader().readShort();
                    byte khoa2 = 0;
                    try {
                        khoa2 = msg.reader().readByte();   // pokemon: bản dịch ngược có "break" ở đây -> có byte tuỳ chọn thì bỏ qua hàm xử lý (Unity không break)
                    } catch (Exception e45) {
                    }
                    Canvas.gameScr.onImbue(price, khoa2);
                    break;
                case 72:
                    Canvas.startOKDlg("Đã sửa xong.");
                    break;
                case 73:
                    Canvas.gameScr.onGemItem(msg);
                    break;
                case 76:
                    Canvas.gameScr.onSpecialItem(msg);
                    break;
                case 77:
                    Canvas.gameScr.onImbueOK(msg.reader().readUTF());
                    break;
                case 78:
                    short idGem = msg.reader().readShort();
                    Canvas.gameScr.onSellGemItem(idGem);
                    break;
                case 80:
                    Res.XAPHU = new PidgeotBayTemplate[msg.reader().readByte()];
                    for (int i82 = 0; i82 < Res.XAPHU.length; i82++) {
                        Res.XAPHU[i82] = new PidgeotBayTemplate();
                        int kk = msg.reader().readByte();
                        Res.XAPHU[i82].mapID = new int[kk];
                        for (int j21 = 0; j21 < Res.XAPHU[i82].mapID.length; j21++) {
                            Res.XAPHU[i82].mapID[j21] = msg.reader().readByte();
                        }
                        Res.XAPHU[i82].x = new int[kk];
                        for (int j22 = 0; j22 < Res.XAPHU[i82].x.length; j22++) {
                            Res.XAPHU[i82].x[j22] = msg.reader().readShort();
                        }
                        Res.XAPHU[i82].y = new int[kk];
                        for (int j23 = 0; j23 < Res.XAPHU[i82].y.length; j23++) {
                            Res.XAPHU[i82].y[j23] = msg.reader().readShort();
                        }
                        Res.XAPHU[i82].price = new int[kk];
                        for (int j24 = 0; j24 < Res.XAPHU[i82].price.length; j24++) {
                            Res.XAPHU[i82].price[j24] = msg.reader().readShort();
                        }
                    }
                    break;
                case 81:
                    Canvas.startOKDlg("Đã mua được vé");
                    break;
                case 82:
                    Canvas.currentDialog = null;
                    if (Canvas.gameScr.mainChar.delay <= 0) {
                        Canvas.gameScr.mainChar.delay = msg.reader().readByte();
                        Canvas.gameScr.mainChar.realTime = Canvas.gameScr.mainChar.delay;
                        Canvas.gameScr.mainChar.timeWait2Board = System.currentTimeMillis();
                    }
                    break;
                case 83:
                    Canvas.gameScr.onBossAttackPlayer(msg);
                    break;
                case 85:
                    Canvas.startOKDlg("Đã mua, món đồ đang ở trong hành trang.");
                    break;
                case 86:
                    int check = msg.reader().readByte();
                    if (check == 1) {
                        Canvas.gameScr.mainChar.timeUseGolgTicket = System.currentTimeMillis();
                        Char.idGoldTicket = msg.reader().readByte() - 1;
                        Char.goldTimeMinute = 1440;
                        Char.goldTime = msg.reader().readUTF();
                    } else if (check == 0) {
                        Canvas.gameScr.mainChar.timeUseGolgTicket = 0L;
                        Char.goldTimeMinute = 0;
                        Char.goldTime = "";
                    } else if (check == 2) {
                        Char.idGoldTicket = msg.reader().readByte() - 1;
                        Canvas.gameScr.mainChar.timeUseGolgTicket = System.currentTimeMillis();
                        Char.goldTimeMinute = msg.reader().readInt();
                        Char.goldTime = msg.reader().readUTF();
                    }
                    break;
                case 87:
                    Canvas.gameScr.onLuckyDraw(msg);
                    break;
                case 89:
                    Canvas.gameScr.onBuffAttack(msg);
                    break;
                case 90:
                    Canvas.gameScr.onActorDie(msg);
                    break;
                case 91:
                    break;
                case 92:
                    Canvas.gameScr.onUserSellItem(msg);
                    break;
                case 94:
                    Canvas.gameScr.onListInfoDepositeItem(msg);
                    break;
                case 95:
                    short itemID4 = msg.reader().readShort();
                    short charID4 = msg.reader().readShort();
                    byte typeB = msg.reader().readByte();
                    Canvas.gameScr.onBuyItemOfUser(itemID4, charID4, typeB);
                    break;
                case 100:
                    Canvas.gameScr.onTemplateInfo(msg);
                    break;
                case 101:
                    Canvas.gameScr.onInfoFriend(msg);
                    break;
                case 102:
                    Canvas.gameScr.onFriendList(msg, 0, "BẠN BÈ");
                    break;
                case 103:
                    Canvas.gameScr.onSMSStruct(msg);
                    break;
                case 104:
                    try {
                        GameScr.loadConfig(msg.reader().readByte());
                        msg.reader().readByte();
                    } catch (Exception e46) {
                        Cout.println(new StringBuffer("LOI CHO NAY NE ").append(e46.toString()).toString());
                        return;
                    }
                    break;
                case 105:
                    int nClone = msg.reader().readByte();
                    int nMap = msg.reader().readByte();
                    Res.locationMap = new short[nMap][][];
                    Res.nameMap = new String[nMap][][];
                    for (int i83 = 0; i83 < nMap; i83++) {
                        int nm = msg.reader().readByte();
                        Res.locationMap[i83] = new short[nm][];
                        Res.nameMap[i83] = new String[nm][];
                        for (int j25 = 0; j25 < nm; j25++) {
                            Res.locationMap[i83][j25] = new short[nClone * 3];
                            Res.nameMap[i83][j25] = new String[nClone];
                            for (int k14 = 0; k14 < nClone * 3; k14++) {
                                Res.locationMap[i83][j25][k14] = msg.reader().readShort();
                                if (k14 % 3 == 0) {
                                    Res.nameMap[i83][j25][k14 / 3] = msg.reader().readUTF();
                                }
                            }
                        }
                    }
                    int nMap2 = msg.reader().readByte();
                    GameScr.mapID = new short[nMap2][];
                    GameScr.idArrMap = new short[nMap2];
                    for (int i84 = 0; i84 < nMap2; i84++) {
                        GameScr.mapID[i84] = new short[nClone];
                        for (int j26 = 0; j26 < nClone; j26++) {
                            GameScr.mapID[i84][j26] = msg.reader().readShort();
                        }
                        GameScr.idArrMap[i84] = msg.reader().readShort();
                    }
                    int nlocalXaphu = msg.reader().readByte();
                    Res.localXaphu = new short[nlocalXaphu][nClone * 3];
                    Res.namMap = new String[nlocalXaphu][nClone];
                    for (int i85 = 0; i85 < nlocalXaphu; i85++) {
                        for (int j27 = 0; j27 < nClone * 3; j27++) {
                            Res.localXaphu[i85][j27] = msg.reader().readShort();
                            if (j27 % 3 == 0) {
                                Res.namMap[i85][j27 / 3] = msg.reader().readUTF();
                            }
                        }
                    }
                    break;
                case 106:
                    Canvas.gameScr.onAttackMultiTarget(msg);
                    break;
            }
        } catch (Exception e47) {
            e47.printStackTrace();
            Cout.println(new StringBuffer(String.valueOf((int) msg.command)).append(" tai cmd nay").toString());
            Cout.println(new StringBuffer(String.valueOf((int) msg.command)).append(" tai cmd nay").toString());
        }
    }

    public int getMinMax(Vector data, byte typeXY, int aa) {
        for (int i = 0; i < data.size(); i++) {
            Actor ac = (Actor) data.elementAt(i);
            if (ac.catagory == 10) {
                Ground gr = (Ground) ac;
                switch (typeXY) {
                    case 0:
                        if (aa > gr.x) {
                            aa = gr.x;
                        }
                        break;
                    case 1:
                        if (aa > gr.y) {
                            aa = gr.y;
                        }
                        break;
                    case 2:
                        if (aa < gr.x) {
                            aa = gr.x;
                        }
                        break;
                    case 3:
                        if (aa < gr.y) {
                            aa = gr.y;
                        }
                        break;
                }
            }
        }
        return aa;
    }

    public int getIndexClassFromItemID(int itemID) {
        if (itemID >= 79 && itemID <= 113) {
            return (itemID - 79) / 7;
        }
        if (itemID >= 174 && itemID <= 213) {
            return (itemID - 174) / 8;
        }
        if (itemID >= 214 && itemID <= 263) {
            return (itemID - 214) / 10;
        }
        return 0;
    }
}
