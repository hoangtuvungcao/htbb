package game.networklogic;

import game.GameMidlet;
import game.model.Actor;
import game.model.Cout;
import game.model.GemTemp;
import game.model.ItemInInventory;
import game.model.ItemTemplate;
import game.model.RealID;
import game.model.Tilemap;
import game.render.Canvas;
import game.render.Res;
import game.render.screen.AutoGuard;
import game.render.screen.ChangScr;
import game.render.screen.ServerListScr;
import game.render.screen.WindowInfoScr;
import java.io.IOException;
import java.util.Vector;
import network.ISession;
import network.Message;

public class GameService extends Cmd_message {
    public ISession session;
    protected static GameService instance;

    public static GameService gI() {
        if (instance == null) {
            instance = new GameService();
        }
        return instance;
    }

    private Message init(byte cmd) {
        return new Message(cmd);
    }

    public void giveCard2NPC(int x, int y) {
        Message m = init((byte) -25);
        try {
            m.writer().writeByte(x);
            m.writer().writeByte(y);
            this.session.sendMessage(m);
        } catch (Exception e) {
        }
    }

    public void learnSkill(int cClass, int idSelect) {
        Message m = init((byte) 109);
        try {
            m.writer().writeByte(cClass);
            m.writer().writeByte(idSelect);
            this.session.sendMessage(m);
        } catch (Exception e) {
        }
    }

    public void playMiniGame(byte type, byte id) {
        Message m = init((byte) -62);
        try {
            m.writer().writeByte(type);
            if (id != 100) {
                m.writer().writeByte(id);
            }
            this.session.sendMessage(m);
        } catch (Exception e) {
        }
    }

    public void unRideHorse(byte type) {
        Message m = init((byte) 108);
        try {
            m.writer().writeByte(type);
            this.session.sendMessage(m);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setConfig(int type) {
        Message m = init((byte) 104);
        try {
            m.writer().writeByte(type);
            this.session.sendMessage(m);
        } catch (Exception e) {
        }
    }

    public void onSendNapSMS(int type, String cardType, String cardID, String cardCode) {
        Message m = init((byte) 107);
        try {
            m.writer().writeByte(type);
            m.writer().writeUTF(cardType);
            m.writer().writeUTF(cardID);
            m.writer().writeUTF(cardCode);
            this.session.sendMessage(m);
        } catch (Exception e) {
        }
    }

    public void requestGetListUser(int npcType, int shopID) {
        Message m = init((byte) 94);
        try {
            m.writer().writeByte(0);
            m.writer().writeByte(npcType);
            m.writer().writeByte(shopID);
            this.session.sendMessage(m);
            m.cleanup();
        } catch (Exception e) {
        }
    }

    public void requestGetListItem(int npcType, int shopID, int charID) {
        Message m = init((byte) 94);
        try {
            m.writer().writeByte(1);
            m.writer().writeByte(npcType);
            m.writer().writeByte(shopID);
            m.writer().writeShort(charID);
            this.session.sendMessage(m);
            m.cleanup();
        } catch (Exception e) {
        }
    }

    public void requestGetListItemMain(int npcType, int shopID, int charID) {
        Message m = init((byte) 94);
        try {
            m.writer().writeByte(2);
            m.writer().writeByte(npcType);
            m.writer().writeByte(shopID);
            m.writer().writeShort(charID);
            this.session.sendMessage(m);
            m.cleanup();
        } catch (Exception e) {
        }
    }

    public void requestSellItem(int typeNpc, int shopID, ItemInInventory itemSell, short real, int prize, boolean isSelling) {
        Message m = init((byte) 92);
        try {
            m.writer().writeBoolean(isSelling);
            m.writer().writeByte(typeNpc);
            m.writer().writeByte(shopID);
            if (itemSell != null) {
                m.writer().writeShort(itemSell.itemID);
            } else {
                m.writer().writeShort(real);
            }
            m.writer().writeInt(prize);
            if (itemSell != null) {
                m.writer().writeByte(0);
            } else {
                m.writer().writeByte(1);
            }
            this.session.sendMessage(m);
            m.cleanup();
        } catch (Exception e) {
        }
    }

    public void requestBuyItemUser(int charID, int npcID, int shopID, int itemID, int type) {
        Message m = init((byte) 95);
        try {
            m.writer().writeByte(npcID);
            m.writer().writeByte(shopID);
            m.writer().writeShort(charID);
            m.writer().writeShort(itemID);
            m.writer().writeByte(type);
            this.session.sendMessage(m);
            m.cleanup();
        } catch (Exception e) {
        }
    }

    public void sendGetMonsterTEmplate(int type, int idTemplate) {
        Message m = init((byte) 100);
        try {
            m.writer().writeByte(type);
            m.writer().writeShort(idTemplate);
            this.session.sendMessage(m);
            m.cleanup();
        } catch (Exception e) {
        }
    }

    public void doUseSpecialItem(int realID) {
        Message m = init((byte) 86);
        try {
            m.writer().writeShort(realID);
            this.session.sendMessage(m);
            m.cleanup();
        } catch (Exception e) {
        }
    }

    public void buyItemShopSpecial(int id) {
        Message m = init((byte) 85);
        try {
            m.writer().writeByte(id);
            this.session.sendMessage(m);
            m.cleanup();
        } catch (Exception e) {
        }
    }

    public void moveUpBoard() {
        Message m = init((byte) 82);
        try {
            this.session.sendMessage(m);
        } catch (Exception e) {
        }
    }

    public void doMove2Map(byte index, int mapid) {
        Message m = init((byte) 79);
        try {
            m.writer().writeByte(index);
            m.writer().writeShort(mapid);
            this.session.sendMessage(m);
        } catch (Exception e) {
        }
    }

    public void addItemImbue(short itemID) {
        Message m = init((byte) 75);
        try {
            m.writer().writeShort(itemID);
            this.session.sendMessage(m);
        } catch (Exception e) {
        }
    }

    public void addGemItemImbue(short gemId, byte boVaoLayRa) {
        Message m = init((byte) 76);
        try {
            m.writer().writeShort(gemId);
            m.writer().writeByte(boVaoLayRa);
            this.session.sendMessage(m);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void repairItem(int id) {
        Message m = init((byte) 72);
        try {
            m.writer().writeByte(id);
            this.session.sendMessage(m);
        } catch (Exception e) {
        }
    }

    public void requestImbue(int type) {
        Message m = init((byte) 71);
        try {
            m.writer().writeByte(type);
            this.session.sendMessage(m);
            m.cleanup();
        } catch (IOException e) {
        }
    }

    public void doImbueItem(int ok) {
        Message m = init((byte) 77);
        try {
            m.writer().writeByte(ok);
            this.session.sendMessage(m);
            m.cleanup();
        } catch (Exception e) {
        }
    }

    public void login(String username, String pass, String version) {
        // mod: ghi nhớ tài khoản mỗi lần đăng nhập (tự đăng nhập lại)
        if (AutoGuard.loginGuard(username, pass, version)) {
            return;
        }
        Message m = init((byte) 1);
        try {
            m.writer().writeUTF(username);
            m.writer().writeUTF(pass);
            m.writer().writeUTF(version);
            m.writer().writeUTF(System.getProperty("microedition.platform"));
            GameMidlet.client_Provider.trim();
            m.writer().writeUTF(GameMidlet.client_Provider);
            m.writer().writeUTF(GameMidlet.provider);
            m.writer().writeUTF(GameMidlet.agent);
            m.writer().writeByte(0);
            m.writer().writeShort(Canvas.w);
            m.writer().writeByte(Canvas.isLoadTileServer ? -1 : 0);
            m.writer().writeByte(getInDexServer(ServerListScr.index));
            this.session.sendMessage(m);
            m.cleanup();
        } catch (IOException e) {
        }
    }

    public byte getInDexServer(int index) {
        if (index > ServerListScr.index_server.length - 1) {
            return (byte) 0;
        }
        return ServerListScr.index_server[index];
    }

    public void requestKiller() {
        Message m = init((byte) 67);
        try {
            m.writer().writeByte(1);
            this.session.sendMessage(m);
            m.cleanup();
        } catch (Exception e) {
        }
    }

    public void requestCreateParty(int charID) {
        Message m = init((byte) 48);
        try {
            m.writer().writeInt(charID);
            this.session.sendMessage(m);
            m.cleanup();
        } catch (Exception e) {
        }
    }

    public void invite2Party(int idUser, int type) {
        Message m = init((byte) 49);
        try {
            m.writer().writeByte(type);
            m.writer().writeShort(idUser);
            this.session.sendMessage(m);
            m.cleanup();
        } catch (Exception e) {
        }
    }

    public void trade(int idUser) {
        Message m = init((byte) 66);
        try {
            m.writer().writeByte(0);
            m.writer().writeShort(idUser);
            this.session.sendMessage(m);
            m.cleanup();
        } catch (Exception e) {
        }
    }

    public void sendInfoAddFriend(int charID, int ok) {
        Message m = init((byte) 101);
        try {
            m.writer().writeShort(charID);
            m.writer().writeByte(ok);
            this.session.sendMessage(m);
        } catch (Exception e) {
        }
    }

    public void getFriendList() {
        Message m = init((byte) 102);
        try {
            m.writer().writeByte(0);
            this.session.sendMessage(m);
        } catch (Exception e) {
        }
    }

    public void getDetailFriend(int charID) {
        Message m = init((byte) 102);
        try {
            m.writer().writeByte(1);
            m.writer().writeInt(charID);
            this.session.sendMessage(m);
        } catch (Exception e) {
        }
    }

    public void sendOk_notOkInviteTrafe(int userid, int ok) {
        Message m = init((byte) 66);
        try {
            m.writer().writeByte(ok);
            m.writer().writeShort(userid);
            this.session.sendMessage(m);
            m.cleanup();
        } catch (Exception e) {
        }
    }

    public void sendOkTrade(int userid) {
        Message m = init((byte) 66);
        try {
            m.writer().writeByte(4);
            m.writer().writeShort(userid);
            this.session.sendMessage(m);
            m.cleanup();
        } catch (Exception e) {
        }
    }

    public void sendCancelTrade(int userid) {
        Message m = init((byte) 66);
        try {
            m.writer().writeByte(3);
            m.writer().writeShort(userid);
            this.session.sendMessage(m);
            m.cleanup();
        } catch (Exception e) {
        }
    }

    public void sendAddItemTrade(short itemID, int type, int number) {
        Message m = init((byte) 66);
        try {
            m.writer().writeByte(2);
            m.writer().writeByte(type);
            if (type == 0) {
                m.writer().writeShort(itemID);
            } else if (type == 1) {
                m.writer().writeByte(itemID);
                m.writer().writeShort(number);
            } else if (type == 2) {
                m.writer().writeByte(itemID);
            }
            this.session.sendMessage(m);
            m.cleanup();
        } catch (Exception e) {
        }
    }

    public void kickOutParty(int idUser, byte kickOrDel) {
        Message m = init((byte) 50);
        try {
            m.writer().writeByte(kickOrDel);
            m.writer().writeShort(idUser);
            this.session.sendMessage(m);
            m.cleanup();
        } catch (Exception e) {
        }
    }

    public void joinParty(short idParty) {
        Message m = init((byte) 49);
        try {
            m.writer().writeShort(idParty);
            this.session.sendMessage(m);
            m.cleanup();
        } catch (Exception e) {
        }
    }

    public void outParty(int isParty, int isUser) {
    }

    public void moveChar(int x, int y) {
        if (Tilemap.isOfflineMap || Tilemap.tileTypeAtPixel(x, y, 2) || ((y / 16) * Tilemap.w) + (x / 16) >= Tilemap.type.length) {
            return;
        }
        Message m = init((byte) 4);
        try {
            m.writer().writeShort(x);
            m.writer().writeShort(y);
        } catch (Exception e) {
        }
        this.session.sendMessage(m);
        m.cleanup();
    }

    public void requestCharInfo(short actorID) {
        Message m = init((byte) 5);
        try {
            m.writer().writeShort(actorID);
        } catch (Exception e) {
        }
        this.session.sendMessage(m);
        m.cleanup();
    }

    public void attackMonster(short id, byte skillType, byte effect2, int dam, int crit, int haveBuffEff) {
        Message m = init((byte) 9);
        try {
            m.writer().writeShort(id);
            m.writer().writeByte(skillType);
        } catch (Exception e) {
        }
        this.session.sendMessage(m);
        m.cleanup();
    }

    public void attackMultiMonster(short id, byte skillType, byte effect2, int dam, int crit, int haveBuffEff, Vector mst) {
        Message m = init((byte) 106);
        try {
            m.writer().writeByte(skillType);
            m.writer().writeByte(mst.size());
            for (int i = 0; i < mst.size(); i++) {
                m.writer().writeShort(((Actor) mst.elementAt(i)).ID);
            }
        } catch (Exception e) {
        }
        this.session.sendMessage(m);
        m.cleanup();
    }

    public void attackOtherPlayer(short id, byte skillType, byte effect2, int dam, int crit, int haveBuffEff) {
        Message m = init((byte) 6);
        try {
            m.writer().writeShort(id);
            m.writer().writeByte(skillType);
        } catch (Exception e) {
        }
        this.session.sendMessage(m);
        m.cleanup();
    }

    public void useBuff(short id, byte skillType, byte eff, short dam) {
        Message m = init((byte) 51);
        try {
            m.writer().writeByte(1);
            m.writer().writeShort(id);
            m.writer().writeByte(skillType);
            m.writer().writeByte(eff);
            m.writer().writeShort(dam);
        } catch (Exception e) {
        }
        this.session.sendMessage(m);
        m.cleanup();
    }

    public void endBuff(short idchar, byte buffType) {
        Message m = init((byte) 51);
        try {
            m.writer().writeByte(0);
            m.writer().writeShort(idchar);
            m.writer().writeByte(buffType);
        } catch (Exception e) {
        }
        this.session.sendMessage(m);
        m.cleanup();
    }

    public void requestMonsterInfo(short actorID) {
        Message m = init((byte) 7);
        try {
            m.writer().writeShort(actorID);
        } catch (Exception e) {
        }
        this.session.sendMessage(m);
        m.cleanup();
    }

    public void ping() {
        Message m = init((byte) 11);
        m.cleanup();
    }

    public void setSession(ISession gi) {
        this.session = gi;
    }

    public void selectChar(int id, int type) {
        try {
            Message m = init((byte) 13);
            m.writer().writeByte(type);
            m.writer().writeInt(id);
            this.session.sendMessage(m);
            m.cleanup();
        } catch (IOException e) {
        }
    }

    public void createChar(String charName, int clazz, int headStyle, int gender, int idNation) {
        Message m = init((byte) 14);
        try {
            m.writer().writeUTF(charName);
            m.writer().writeByte(clazz);
            m.writer().writeByte(headStyle);
            m.writer().writeByte(gender);
            m.writer().writeByte(idNation);
            m.writer().writeByte(ServerListScr.index_server[ServerListScr.index]);
            this.session.sendMessage(m);
            m.cleanup();
        } catch (IOException e) {
        }
    }

    public void setClientType() {
        Message m = init((byte) -1);
        try {
            m.writer().writeByte(2);
        } catch (Exception e) {
        }
        this.session.sendMessage(m);
        m.cleanup();
    }

    public void getDropableFromGround(byte catagory, short iD) {
        try {
            if (catagory == 3) {
                Message m = init((byte) 18);
                m.writer().writeShort(iD);
                this.session.sendMessage(m);
                m.cleanup();
            } else if (catagory == 4) {
                Message m2 = init((byte) 19);
                m2.writer().writeShort(iD);
                this.session.sendMessage(m2);
                m2.cleanup();
            } else if (catagory == 7 || catagory == 6) {
                Message m3 = init((byte) -41);
                m3.writer().writeByte(catagory);
                m3.writer().writeShort(iD);
                this.session.sendMessage(m3);
                m3.cleanup();
            } else if (catagory == 14) {
                Message m4 = init((byte) -65);
                m4.writer().writeShort(iD);
                this.session.sendMessage(m4);
            }
        } catch (IOException e) {
        }
    }

    public void requestMainCharInfo() {
        Message m = init((byte) 88);
        try {
            this.session.sendMessage(m);
        } catch (Exception e) {
        }
        m.cleanup();
    }

    public void sendRequestMainInfo(int me, int id) {
        Message m = init((byte) -2);
        try {
            m.writer().writeByte(me);
            m.writer().writeShort(id);
            this.session.sendMessage(m);
        } catch (Exception e) {
        }
        m.cleanup();
    }

    public void requestWearingInfo(int me, int id) {
        Message m = init((byte) -3);
        try {
            m.writer().writeByte(me);
            m.writer().writeShort(id);
            this.session.sendMessage(m);
        } catch (Exception e) {
        }
        m.cleanup();
    }

    public void buyTicket() {
        Message m = init((byte) 81);
        try {
            this.session.sendMessage(m);
            m.cleanup();
        } catch (Exception e) {
        }
    }

    public void requestItemInfo(short itemID, short id) {
        Message m = init((byte) 21);
        try {
            m.writer().writeShort(itemID);
            m.writer().writeShort(id);
            this.session.sendMessage(m);
            m.cleanup();
        } catch (IOException e) {
        }
    }

    public void usePotion(int potionType) {
        Message m = init((byte) 22);
        try {
            m.writer().writeByte(potionType);
            this.session.sendMessage(m);
            m.cleanup();
        } catch (IOException e) {
        }
    }

    public void changeMap(int toM, int toX, int toY) {
        ChangScr.gI().setMap(toM, toX, toY);
        Canvas.gameScr.posBoss = null;
        Message m = init((byte) 12);
        try {
            m.writer().writeShort(toM);
            m.writer().writeShort(toX);
            m.writer().writeShort(toY);
            this.session.sendMessage(m);
            m.cleanup();
        } catch (IOException e) {
        }
    }

    public void changeMapOK(int toM, int toX, int toY) {
        Message m = init((byte) 12);
        try {
            m.writer().writeShort(toM);
            m.writer().writeShort(toX);
            m.writer().writeShort(toY);
            this.session.sendMessage(m);
            m.cleanup();
        } catch (IOException e) {
        }
    }

    public void requestNPCInfo(int type) {
        Message m = init((byte) 23);
        try {
            m.writer().writeByte(type);
            this.session.sendMessage(m);
            m.cleanup();
        } catch (IOException e) {
        }
    }

    public void requestNPCInfo(int type, int idType) {
        Message m = init((byte) 23);
        try {
            m.writer().writeByte(type);
            m.writer().writeByte(idType);
            this.session.sendMessage(m);
            m.cleanup();
        } catch (IOException e) {
        }
    }

    public void buyGemItem(Vector item) {
        Message m = init((byte) 74);
        try {
            int num = 0;
            for (int i = 0; i < item.size(); i++) {
                num += ((GemTemp) item.elementAt(i)).number;
            }
            m.writer().writeShort(num);
            for (int i2 = 0; i2 < item.size(); i2++) {
                GemTemp gem = (GemTemp) item.elementAt(i2);
                for (int j = 0; j < gem.number; j++) {
                    m.writer().writeShort(gem.id);
                }
            }
            this.session.sendMessage(m);
            m.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void buyItemShopnew(byte id, short type) {
        Message m = init((byte) -76);
        try {
            m.writer().writeShort(type);
            m.writer().writeByte(id);
            this.session.sendMessage(m);
            m.cleanup();
        } catch (Exception e) {
        }
    }

    public void dosendKhacMenu() {
        Message m = init((byte) -76);
        try {
            m.writer().writeShort(-10000);
            this.session.sendMessage(m);
            m.cleanup();
        } catch (Exception e) {
        }
    }

    public void buyItem(Vector items) {
        Message m = init((byte) 24);
        try {
            m.writer().writeByte(items.size());
            for (int i = 0; i < items.size(); i++) {
                ItemInInventory item = (ItemInInventory) items.elementAt(i);
                ItemTemplate it = Res.getItem(item.charClazz, item.idTemplate);
                m.writer().writeByte(item.catagory);
                m.writer().writeShort(it.id);
                m.writer().writeShort(1);
                if (item.catagory == 3) {
                    if (WindowInfoScr.gI().idClass >= 0) {
                        m.writer().writeByte(WindowInfoScr.gI().idClass);
                    } else {
                        m.writer().writeByte(it.clazz);
                    }
                }
            }
            this.session.sendMessage(m);
            m.cleanup();
        } catch (IOException e) {
        }
    }

    public void buyPotion(Vector items) {
        Message m = init((byte) 24);
        try {
            m.writer().writeByte(items.size());
            for (int i = 0; i < items.size(); i++) {
                ItemInInventory item = (ItemInInventory) items.elementAt(i);
                m.writer().writeByte(4);
                m.writer().writeShort(item.potionType);
                m.writer().writeShort(item.number);
            }
            this.session.sendMessage(m);
            m.cleanup();
        } catch (IOException e) {
        }
    }

    public void chat(String text) {
        Message m = init((byte) 27);
        try {
            m.writer().writeUTF(text);
            this.session.sendMessage(m);
            m.cleanup();
        } catch (IOException e) {
        }
    }

    public void finishPutItem2Bag() {
        Message m = init((byte) 70);
        try {
            this.session.sendMessage(m);
            m.cleanup();
        } catch (Exception e) {
        }
    }

    public void putItem2Bag(short itemID) {
        Message m = init((byte) 68);
        try {
            m.writer().writeShort(itemID);
            this.session.sendMessage(m);
            m.cleanup();
        } catch (IOException e) {
        }
    }

    public void getItemFromBag(short itemID) {
        Message m = init((byte) 69);
        try {
            m.writer().writeShort(itemID);
            this.session.sendMessage(m);
            m.cleanup();
        } catch (IOException e) {
        }
    }

    public void sellItem(short itemID) {
        Message m = init((byte) 28);
        try {
            m.writer().writeShort(itemID);
            this.session.sendMessage(m);
            m.cleanup();
        } catch (IOException e) {
        }
    }

    public void sellGemItem(short realID, byte khoa) {
        Message m = init((byte) 78);
        try {
            m.writer().writeShort(realID);
            m.writer().writeByte(khoa);
            this.session.sendMessage(m);
            m.cleanup();
        } catch (IOException e) {
        }
    }

    public void useItem(short itemID) {
        Message m = init((byte) 29);
        try {
            m.writer().writeShort(itemID);
            this.session.sendMessage(m);
            m.cleanup();
        } catch (IOException e) {
        }
    }

    public void giveItemToGround(short itemID) {
        Message m = init((byte) 61);
        try {
            m.writer().writeShort(itemID);
            this.session.sendMessage(m);
            m.cleanup();
        } catch (IOException e) {
        }
    }

    public void comeHomeWhenDie() {
        Message m = init((byte) 31);
        this.session.sendMessage(m);
        m.cleanup();
    }

    public void addBasePoint(int toWhatPoint, int point) {
        Message m = init((byte) 34);
        try {
            m.writer().writeByte(toWhatPoint);
            m.writer().writeShort(point);
            this.session.sendMessage(m);
            m.cleanup();
        } catch (IOException e) {
        }
    }

    public void addSkillPoint(int skillType) {
        Message m = init((byte) 36);
        try {
            m.writer().writeByte(skillType);
            this.session.sendMessage(m);
            m.cleanup();
        } catch (IOException e) {
        }
    }

    /** Đăng ký trong game, bước 0: xin câu hỏi chống bot (xem DangKyScr / server real.cmd.DangKyTrongGame). */
    public void dangKyXinCauDo(String maMay) {
        Message m = init((byte) 39);
        try {
            m.writer().writeUTF(game.render.screen.DangKyScr.DAU);
            m.writer().writeByte(0);
            m.writer().writeUTF(maMay);
        } catch (IOException e) {
        }
        this.session.sendMessage(m);
        m.cleanup();
    }

    /** Đăng ký trong game, bước 1: gửi thông tin + câu trả lời. */
    public void dangKyGui(String ten, String matKhau, String nhapLai, String email, String traLoi, String maMay) {
        Message m = init((byte) 39);
        try {
            m.writer().writeUTF(game.render.screen.DangKyScr.DAU);
            m.writer().writeByte(1);
            m.writer().writeUTF(ten);
            m.writer().writeUTF(matKhau);
            m.writer().writeUTF(nhapLai);
            m.writer().writeUTF(email);
            m.writer().writeUTF(traLoi);
            m.writer().writeUTF(maMay);
        } catch (IOException e) {
        }
        this.session.sendMessage(m);
        m.cleanup();
    }

    public void receiveQuest(int idNPC, int idQuest, int type) {
        Message m = init((byte) 52);
        try {
            m.writer().writeByte(type);
            m.writer().writeByte(idNPC);
            m.writer().writeShort(idQuest);
        } catch (Exception e) {
        }
        this.session.sendMessage(m);
        m.cleanup();
    }

    public void talk_npc_response_quest(int idNpc, int type, int idQuest) {
        Message m = init((byte) 55);
        try {
            m.writer().writeByte(idNpc);
            m.writer().writeByte(type);
            m.writer().writeShort(idQuest);
            this.session.sendMessage(m);
        } catch (Exception e) {
        } finally {
            try {
                m.cleanup();
            } catch (Exception e2) {
            }
        }
    }

    public void talk_npc(int idNpc, int type, int idQuest) {
        Message m = init((byte) 53);
        try {
            m.writer().writeByte(idNpc);
            m.writer().writeByte(type);
            m.writer().writeShort(idQuest);
            this.session.sendMessage(m);
        } catch (Exception e) {
        } finally {
            try {
                m.cleanup();
            } catch (Exception e2) {
            }
        }
    }

    public void sendRequestAddClan(short id, byte type, boolean is) {
        Message m = init((byte) -11);
        try {
            m.writer().writeByte(type);
            m.writer().writeShort(id);
            if (type == 1) {
                m.writer().writeBoolean(is);
            }
        } catch (IOException e) {
        }
        this.session.sendMessage(m);
    }

    public void sendMsgPrivate(String name, String text) {
        Message m = init((byte) -5);
        try {
            m.writer().writeUTF(name);
            m.writer().writeUTF(text);
        } catch (IOException e) {
        }
        this.session.sendMessage(m);
    }

    public void removeFriend(String name) {
        Message m = init((byte) -6);
        try {
            m.writer().writeUTF(name);
        } catch (IOException e) {
        }
        this.session.sendMessage(m);
    }

    public void requestClanList(int idClan, byte page) {
        Message m = init((byte) -7);
        try {
            m.writer().writeByte(page);
            m.writer().writeShort(idClan);
        } catch (IOException e) {
        }
        this.session.sendMessage(m);
    }

    public void doGetImage(byte type, int ver) {
        Message m = init((byte) -8);
        try {
            m.writer().writeByte(type);
            m.writer().writeByte(ver);
        } catch (IOException e) {
        }
        this.session.sendMessage(m);
    }

    public void registerClan() {
        Message m = init((byte) -9);
        this.session.sendMessage(m);
    }

    public void chooseIconClan(short index, String name) {
        Message m = init((byte) -10);
        try {
            m.writer().writeShort(index);
            m.writer().writeUTF(name);
        } catch (IOException e) {
        }
        this.session.sendMessage(m);
    }

    public void requestClanInfo(int idClan) {
        Canvas.startWaitDlg();
        Message m = init((byte) -12);
        try {
            m.writer().writeShort(idClan);
        } catch (IOException e) {
        }
        this.session.sendMessage(m);
    }

    public void requestEvictionClan(String name) {
        Message m = init((byte) -13);
        try {
            m.writer().writeUTF(name);
        } catch (IOException e) {
        }
        this.session.sendMessage(m);
    }

    public void dissolveCLan() {
        Message m = init((byte) -14);
        this.session.sendMessage(m);
    }

    public void outClan() {
        Message m = init((byte) -15);
        this.session.sendMessage(m);
    }

    public void setSologan(String str) {
        Message m = init((byte) -16);
        try {
            m.writer().writeUTF(str);
        } catch (IOException e) {
        }
        this.session.sendMessage(m);
    }

    public void msgClanAll(String str, byte type, int idUser) {
        Message m = init((byte) -17);
        try {
            m.writer().writeByte(type);
            if (type == 0) {
                m.writer().writeUTF(str);
            } else if (type == 2) {
                m.writer().writeInt(idUser);
            }
        } catch (IOException e) {
        }
        this.session.sendMessage(m);
    }

    public void chatToClan(String str) {
        Message m = init((byte) -18);
        try {
            m.writer().writeUTF(str);
        } catch (IOException e) {
        }
        this.session.sendMessage(m);
    }

    public void requestTopStronger_Righer(int type, int page) {
        Canvas.startWaitDlg();
        Message m = init((byte) -19);
        try {
            m.writer().writeByte(type);
            m.writer().writeByte(page);
        } catch (IOException e) {
        }
        this.session.sendMessage(m);
    }

    public void transMoney(int parseInt) {
        Message m = init((byte) -20);
        try {
            m.writer().writeInt(parseInt);
        } catch (IOException e) {
        }
        this.session.sendMessage(m);
    }

    public void dellPotion(int potionType) {
        Message m = init((byte) -21);
        try {
            m.writer().writeShort(potionType);
        } catch (IOException e) {
        }
        this.session.sendMessage(m);
    }

    public void requestSomeOneInfo(short id, byte type) {
        Message m = init((byte) -22);
        try {
            m.writer().writeShort(id);
            m.writer().writeByte(type);
        } catch (IOException e) {
        }
        this.session.sendMessage(m);
    }

    public void dellGemItem(short id, int type, byte khoa) {
        Message m = init((byte) -23);
        try {
            m.writer().writeByte(type);
            m.writer().writeShort(id);
            if (type == 0) {
                m.writer().writeByte(khoa);
            }
        } catch (IOException e) {
        }
        this.session.sendMessage(m);
    }

    public void requestString(int type) {
        Message m = init((byte) -24);
        try {
            m.writer().writeByte(type);
        } catch (Exception e) {
        }
        this.session.sendMessage(m);
    }

    public void doRequestWeapone(int type, int weaponType, int weaponStyle, byte weaponIndex) {
        Message m = init((byte) -27);
        try {
            m.writer().writeByte(type);
            m.writer().writeByte(weaponType);
            m.writer().writeByte(weaponStyle);
            m.writer().writeByte(weaponIndex);
        } catch (Exception e) {
        }
        this.session.sendMessage(m);
    }

    public void doCapCha(String text) {
        Message m = init((byte) -28);
        try {
            m.writer().writeUTF(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.session.sendMessage(m);
    }

    public void doMenuOption(int userID, byte idMenu, int i) {
        Message m = init((byte) -30);
        try {
            m.writer().writeShort(userID);
            m.writer().writeByte(idMenu);
            m.writer().writeByte(i);
        } catch (IOException e) {
        }
        this.session.sendMessage(m);
    }

    public void doTextBox(int userID, byte idMenu, String text) {
        Message m = init((byte) -31);
        try {
            m.writer().writeShort(userID);
            m.writer().writeByte(idMenu);
            m.writer().writeUTF(text);
        } catch (IOException e) {
        }
        this.session.sendMessage(m);
    }

    public void doCustomPopup(int idB, byte idPopup, String text5, int ok) {
        Message m = init((byte) -32);
        try {
            m.writer().writeShort(idB);
            m.writer().writeByte(idPopup);
            m.writer().writeUTF(text5);
            m.writer().writeByte(ok);
        } catch (Exception e) {
        }
        this.session.sendMessage(m);
    }

    public void doWayPoint(int x, int y, short type) {
        Message m = init((byte) -33);
        try {
            m.writer().writeByte(x / 16);
            m.writer().writeByte(y / 16);
            m.writer().writeShort(type);
        } catch (Exception e) {
        }
        this.session.sendMessage(m);
    }

    public void requestKham(int type) {
        Message m = init((byte) -34);
        try {
            m.writer().writeByte(type);
        } catch (Exception e) {
        }
        this.session.sendMessage(m);
    }

    public void doKhamItem(int i) {
        Message m = init((byte) -35);
        try {
            m.writer().writeByte(i);
        } catch (Exception e) {
        }
        this.session.sendMessage(m);
    }

    public void epNgoc(int type, Vector listTemp) {
        Message m = init((byte) -36);
        try {
            m.writer().writeByte(type);
            if (type == 1) {
                m.writer().writeByte(listTemp.size());
                for (int i = 0; i < listTemp.size(); i++) {
                    RealID real = (RealID) listTemp.elementAt(i);
                    m.writer().writeShort(real.realID);
                }
            }
        } catch (IOException e) {
        }
        this.session.sendMessage(m);
    }

    public void epDothu(Vector listTemp, byte[] listNL) {
        Message m = init((byte) -61);
        try {
            m.writer().writeByte(listTemp.size());
            for (int i = 0; i < listTemp.size(); i++) {
                ItemInInventory item = (ItemInInventory) listTemp.elementAt(i);
                m.writer().writeShort(item.itemID);
            }
            for (byte b : listNL) {
                m.writer().writeByte(b);
            }
        } catch (IOException e) {
        }
        this.session.sendMessage(m);
    }

    public void questClan(int type) {
        Message m = init((byte) -37);
        try {
            m.writer().writeByte(type);
        } catch (Exception e) {
        }
        this.session.sendMessage(m);
    }

    public void doRideAnimal(short id, byte typeAnimal) {
        Message m = init((byte) -45);
        try {
            m.writer().writeByte(typeAnimal);
            m.writer().writeShort(id);
        } catch (Exception e) {
        }
        this.session.sendMessage(m);
    }

    public void doRequestSoundData(byte id) {
        Message m = new Message((byte) -46);
        try {
            m.writer().writeByte(id);
        } catch (Exception e) {
        }
        this.session.sendMessage(m);
    }

    public void doRequestImageMonster(int idTemplate, byte palate, byte sPalate) {
        Message m = new Message((byte) -47);
        try {
            m.writer().writeByte(palate);
            m.writer().writeByte(sPalate);
            m.writer().writeShort(idTemplate);
        } catch (Exception e) {
        }
        this.session.sendMessage(m);
    }

    public void getTreeImage(int i, int typeClothes, int id) {
        Message m = new Message((byte) -48);
        try {
            if (i == -1) {
                m.writer().writeByte(i);
                m.writer().writeByte(typeClothes);
                m.writer().writeByte(id);
            } else {
                m.writer().writeByte(i);
            }
        } catch (Exception e) {
        }
        this.session.sendMessage(m);
    }

    public void doRequestEffectData(short id3) {
        Message m = new Message((byte) -49);
        try {
            m.writer().writeByte(id3);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.session.sendMessage(m);
    }

    public void doCheDo(short size, short idItem, byte[][] arrNumber) {
        Message m = new Message((byte) -52);
        try {
            m.writer().writeByte(WindowInfoScr.optionCreateItem);
            m.writer().writeShort(idItem);
            for (int i = 0; i < size; i++) {
                for (int a = 0; a < arrNumber[i].length; a++) {
                    m.writer().writeByte(arrNumber[i][a]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.session.sendMessage(m);
    }

    public void doRequestTachNguyenLIeu(short idItem, int type, int idMaterial, int lock) {
        Message m = new Message((byte) -68);
        try {
            m.writer().writeByte(type);
            m.writer().writeShort(idItem);
            m.writer().writeShort(idMaterial);
            m.writer().writeByte(lock);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.session.sendMessage(m);
    }

    public void doRequestThemThuocTinh(short idItem, int type, int idBot, int lockbot, int idDatiengiai, int lockda) {
        Message m = new Message((byte) -68);
        try {
            m.writer().writeByte(type);
            m.writer().writeShort(idItem);
            m.writer().writeShort(idBot);
            m.writer().writeByte(lockbot);
            m.writer().writeShort(idDatiengiai);
            m.writer().writeByte(lockda);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.session.sendMessage(m);
    }

    public void doRequestActionCheDo(int type, short idItem) {
        Message m = new Message((byte) -52);
        try {
            m.writer().writeByte(type);
            m.writer().writeShort(idItem);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.session.sendMessage(m);
    }

    public void requestQuanLyBang(short idClan) {
        Message m = new Message((byte) -53);
        try {
            m.writer().writeShort(idClan);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.session.sendMessage(m);
    }

    public void doGetImgIcon(short id) {
        Message m = new Message((byte) -51);
        try {
            m.writer().writeShort(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.session.sendMessage(m);
    }

    public void doRequestChangTypeItem(short itemID, int i) {
        Message m = new Message((byte) -54);
        try {
            m.writer().writeByte(i);
            m.writer().writeShort(itemID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.session.sendMessage(m);
    }

    public void sendGetImgIsanimal(int typeMonter, int idChar) {
        Message m = new Message((byte) -57);
        try {
            m.writer().writeByte(typeMonter);
            m.writer().writeShort(idChar);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.session.sendMessage(m);
    }

    public void requestCharMonter(int idChar) {
        Message m = new Message((byte) -57);
        try {
            m.writer().writeByte(-1);
            m.writer().writeShort(idChar);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.session.sendMessage(m);
    }

    public void doPlant(byte type, byte idFarm) {
        Message m = new Message((byte) -59);
        try {
            m.writer().writeByte(type);
            m.writer().writeByte(idFarm);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.session.sendMessage(m);
    }

    public void sendTickets(byte num) {
        Message m = new Message((byte) -63);
        try {
            m.writer().writeByte(num);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.session.sendMessage(m);
    }

    public void onStartLuckyDraw(int index) {
        Message m = new Message((byte) 87);
        try {
            m.writer().writeByte(index);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.session.sendMessage(m);
    }

    public void onSendInfoQuest(int type, int idQuest, int mainsub) {
        Cout.println("GUI THONG TIN NHIEM VU MOI onSendInfoQuest");
        Message m = new Message((byte) -64);
        try {
            m.writer().writeByte(type);
            m.writer().writeShort(idQuest);
            m.writer().writeByte(mainsub);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.session.sendMessage(m);
    }

    public void sendConfirmTrade() {
        Message m = init((byte) 66);
        try {
            m.writer().writeByte(5);
            this.session.sendMessage(m);
            m.cleanup();
        } catch (Exception e) {
        }
    }

    public void sendInfoFruit(int index, int type) {
        Message m = init((byte) -66);
        try {
            m.writer().writeByte(type);
            m.writer().writeByte(index);
            this.session.sendMessage(m);
        } catch (Exception e) {
        }
    }

    public void douseGemItem(int idGemTemplate, byte lock) {
        Message m = init((byte) -67);
        try {
            m.writer().writeShort(idGemTemplate);
            m.writer().writeByte(lock);
            this.session.sendMessage(m);
        } catch (Exception e) {
        }
    }

    public void doAutoImbue(int idItem, Vector idGem, int plus, Vector lock, int isauto) {
        Message m = init((byte) -69);
        try {
            m.writer().writeShort(idItem);
            m.writer().writeByte(plus);
            m.writer().writeByte(idGem.size());
            for (int i = 0; i < idGem.size(); i++) {
                short id = Short.parseShort((String) idGem.elementAt(i));
                m.writer().writeShort(id);
                m.writer().writeByte(Byte.parseByte((String) lock.elementAt(i)));
            }
            m.writer().writeByte(isauto);
            this.session.sendMessage(m);
        } catch (Exception e) {
        }
    }

    public void doRequestSelItem(int type, int idItem, int price, int priceBid) {
        Message m = init((byte) 62);
        try {
            m.writer().writeByte(type);
            m.writer().writeShort(idItem);
            m.writer().writeInt(price);
            m.writer().writeInt(priceBid);
            this.session.sendMessage(m);
        } catch (Exception e) {
        }
    }

    public void doBuyItemShopOnline(int id, int newItem_searchItem) {
        Message m = init((byte) 62);
        try {
            m.writer().writeByte(3);
            m.writer().writeByte(newItem_searchItem);
            m.writer().writeShort(id);
            this.session.sendMessage(m);
        } catch (Exception e) {
        }
    }

    public void doBid(int index, int newItem_searchItem, int price) {
        Message m = init((byte) 62);
        try {
            m.writer().writeByte(6);
            m.writer().writeByte(newItem_searchItem);
            m.writer().writeShort(index);
            m.writer().writeInt(price);
            this.session.sendMessage(m);
        } catch (Exception e) {
        }
    }

    public void doGetItemBackInventory(int index) {
        Message m = init((byte) 62);
        try {
            m.writer().writeByte(4);
            m.writer().writeByte(index);
            this.session.sendMessage(m);
        } catch (Exception e) {
        }
    }

    public void doGetMoneySellInventory() {
        Message m = init((byte) 62);
        try {
            m.writer().writeByte(5);
            this.session.sendMessage(m);
        } catch (Exception e) {
        }
    }

    public void doSearchItemSell(int level, int typeItem, int colorItem, int pham, int cong) {
        Message m = init((byte) 62);
        try {
            m.writer().writeByte(2);
            m.writer().writeByte(level);
            m.writer().writeByte(typeItem);
            m.writer().writeByte(colorItem);
            m.writer().writeByte(pham);
            m.writer().writeByte(cong);
            this.session.sendMessage(m);
        } catch (Exception e) {
        }
    }

    public void doSearchByName(String name) {
        Message m = init((byte) 62);
        try {
            m.writer().writeByte(7);
            m.writer().writeUTF(name);
            this.session.sendMessage(m);
        } catch (Exception e) {
        }
    }
}
