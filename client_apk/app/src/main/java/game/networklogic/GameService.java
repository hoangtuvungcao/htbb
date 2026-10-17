package game.networklogic;

import com.htbb.client.GameMidlet;
import game.model.Actor;
import game.model.GemTemp;
import game.model.ItemInInventory;
import game.model.ItemTemplate;
import game.model.RealID;
import game.model.Tilemap;
import game.render.GCanvas;
import game.render.Res;
import game.render.screen.ChangScr;
import game.render.screen.NqshMod;
import game.render.screen.ServerListScr;
import game.render.screen.WindowInfoScr;
import java.io.IOException;
import java.util.Vector;
import network.ISession;
import network.Message;

public class GameService extends Cmd_message {
    protected static GameService instance;
    public ISession session;

    public void outParty(int i, int i2) {
    }

    public static GameService gI() {
        if (instance == null) {
            instance = new GameService();
        }
        return instance;
    }

    private Message init(byte b) {
        return new Message(b);
    }

    public void dosendKhacMenu() {
        Message messageInit = init((byte) -76);
        try {
            messageInit.writer().writeShort(-10000);
            this.session.sendMessage(messageInit);
            messageInit.cleanup();
        } catch (Exception unused) {
        }
    }

    public void giveCard2NPC(int i, int i2) {
        Message messageInit = init((byte) -25);
        try {
            messageInit.writer().writeByte(i);
            messageInit.writer().writeByte(i2);
            this.session.sendMessage(messageInit);
        } catch (Exception unused) {
        }
    }

    public void learnSkill(int i, int i2) {
        Message messageInit = init((byte) 109);
        try {
            messageInit.writer().writeByte(i);
            messageInit.writer().writeByte(i2);
            this.session.sendMessage(messageInit);
        } catch (Exception unused) {
        }
    }

    public void buyItemShopnew(byte b, short s) {
        Message messageInit = init((byte) -76);
        try {
            messageInit.writer().writeShort(s);
            messageInit.writer().writeByte(b);
            this.session.sendMessage(messageInit);
            messageInit.cleanup();
        } catch (Exception unused) {
        }
    }

    public void unRideHorse(byte b) {
        Message messageInit = init((byte) 108);
        try {
            messageInit.writer().writeByte(b);
            this.session.sendMessage(messageInit);
        } catch (Exception unused) {
        }
    }

    public void setConfig(int i) {
        Message messageInit = init((byte) 104);
        try {
            messageInit.writer().writeByte(i);
            this.session.sendMessage(messageInit);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onSendNapSMS(int i, String str, String str2, String str3) {
        Message messageInit = init((byte) 107);
        try {
            messageInit.writer().writeByte(i);
            messageInit.writer().writeUTF(str);
            messageInit.writer().writeUTF(str2);
            messageInit.writer().writeUTF(str3);
            this.session.sendMessage(messageInit);
        } catch (Exception unused) {
        }
    }

    public void requestGetListUser(int i, int i2) {
        Message messageInit = init((byte) 94);
        try {
            messageInit.writer().writeByte(0);
            messageInit.writer().writeByte(i);
            messageInit.writer().writeByte(i2);
            this.session.sendMessage(messageInit);
            messageInit.cleanup();
        } catch (Exception unused) {
        }
    }

    public void requestGetListItem(int i, int i2, int i3) {
        Message messageInit = init((byte) 94);
        try {
            messageInit.writer().writeByte(1);
            messageInit.writer().writeByte(i);
            messageInit.writer().writeByte(i2);
            messageInit.writer().writeShort(i3);
            this.session.sendMessage(messageInit);
            messageInit.cleanup();
        } catch (Exception unused) {
        }
    }

    public void requestGetListItemMain(int i, int i2, int i3) {
        Message messageInit = init((byte) 94);
        try {
            messageInit.writer().writeByte(2);
            messageInit.writer().writeByte(i);
            messageInit.writer().writeByte(i2);
            messageInit.writer().writeShort(i3);
            this.session.sendMessage(messageInit);
            messageInit.cleanup();
        } catch (Exception unused) {
        }
    }

    public void requestSellItem(int i, int i2, ItemInInventory itemInInventory, short s, int i3, boolean z) {
        Message messageInit = init((byte) 92);
        try {
            messageInit.writer().writeBoolean(z);
            messageInit.writer().writeByte(i);
            messageInit.writer().writeByte(i2);
            if (itemInInventory != null) {
                messageInit.writer().writeShort(itemInInventory.itemID);
            } else {
                messageInit.writer().writeShort(s);
            }
            messageInit.writer().writeInt(i3);
            if (itemInInventory != null) {
                messageInit.writer().writeByte(0);
            } else {
                messageInit.writer().writeByte(1);
            }
            this.session.sendMessage(messageInit);
            messageInit.cleanup();
        } catch (Exception unused) {
        }
    }

    public void requestBuyItemUser(int i, int i2, int i3, int i4, int i5) {
        Message messageInit = init((byte) 95);
        try {
            messageInit.writer().writeByte(i2);
            messageInit.writer().writeByte(i3);
            messageInit.writer().writeShort(i);
            messageInit.writer().writeShort(i4);
            messageInit.writer().writeByte(i5);
            this.session.sendMessage(messageInit);
            messageInit.cleanup();
        } catch (Exception unused) {
        }
    }

    public void sendGetMonsterTEmplate(int i, int i2) {
        Message messageInit = init((byte) 100);
        try {
            messageInit.writer().writeByte(i);
            messageInit.writer().writeShort(i2);
            this.session.sendMessage(messageInit);
            messageInit.cleanup();
        } catch (Exception unused) {
        }
    }

    public void doUseSpecialItem(int i) {
        Message messageInit = init((byte) 86);
        try {
            messageInit.writer().writeShort(i);
            this.session.sendMessage(messageInit);
            messageInit.cleanup();
        } catch (Exception unused) {
        }
    }

    public void buyItemShopSpecial(int i) {
        Message messageInit = init((byte) 85);
        try {
            messageInit.writer().writeByte(i);
            this.session.sendMessage(messageInit);
            messageInit.cleanup();
        } catch (Exception unused) {
        }
    }

    public void moveUpBoard() {
        try {
            this.session.sendMessage(init((byte) 82));
        } catch (Exception unused) {
        }
    }

    public void doMove2Map(byte b, int i) {
        Message messageInit = init((byte) 79);
        try {
            messageInit.writer().writeByte(b);
            messageInit.writer().writeShort(i);
            this.session.sendMessage(messageInit);
        } catch (Exception unused) {
        }
    }

    public void addItemImbue(short s) {
        Message messageInit = init((byte) 75);
        try {
            messageInit.writer().writeShort(s);
            this.session.sendMessage(messageInit);
        } catch (Exception unused) {
        }
    }

    public void addGemItemImbue(short s, byte b) {
        Message messageInit = init((byte) 76);
        try {
            messageInit.writer().writeShort(s);
            messageInit.writer().writeByte(b);
            this.session.sendMessage(messageInit);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void repairItem(int i) {
        Message messageInit = init((byte) 72);
        try {
            messageInit.writer().writeByte(i);
            this.session.sendMessage(messageInit);
        } catch (Exception unused) {
        }
    }

    public void requestImbue(int i) {
        Message messageInit = init((byte) 71);
        try {
            messageInit.writer().writeByte(i);
            this.session.sendMessage(messageInit);
            messageInit.cleanup();
        } catch (IOException unused) {
        }
    }

    public void doImbueItem(int i) {
        Message messageInit = init((byte) 77);
        try {
            messageInit.writer().writeByte(i);
            this.session.sendMessage(messageInit);
            messageInit.cleanup();
        } catch (Exception unused) {
        }
    }

    public void login(String str, String str2, String str3) {
        // mod: ghi nhớ tài khoản (tự đăng nhập lại) + gửi mã máy, số bản
        if (NqshMod.loginGuard(str, str2, str3)) {
            return;
        }
        Message messageInit = init((byte) 1);
        try {
            messageInit.writer().writeUTF(str);
            messageInit.writer().writeUTF(str2);
            messageInit.writer().writeUTF(str3);
            messageInit.writer().writeUTF("nokia/1/1");
            GameMidlet.client_Provider.trim();
            messageInit.writer().writeUTF(GameMidlet.client_Provider);
            messageInit.writer().writeUTF(GameMidlet.provider);
            messageInit.writer().writeUTF(GameMidlet.agent);
            messageInit.writer().writeByte(GameMidlet.VERSION);
            messageInit.writer().writeShort(GCanvas.w);
            messageInit.writer().writeByte(GCanvas.isLoadTileServer ? -1 : 0);
            messageInit.writer().writeByte(getInDexServer(ServerListScr.index));
            this.session.sendMessage(messageInit);
            messageInit.cleanup();
        } catch (IOException unused) {
        }
    }

    public byte getInDexServer(int i) {
        if (i > ServerListScr.index_server.length - 1) {
            return (byte) 0;
        }
        return ServerListScr.index_server[i];
    }

    public void requestKiller() {
        Message messageInit = init((byte) 67);
        try {
            messageInit.writer().writeByte(1);
            this.session.sendMessage(messageInit);
            messageInit.cleanup();
        } catch (Exception unused) {
        }
    }

    public void requestCreateParty(int i) {
        Message messageInit = init((byte) 48);
        try {
            messageInit.writer().writeInt(i);
            this.session.sendMessage(messageInit);
            messageInit.cleanup();
        } catch (Exception unused) {
        }
    }

    public void invite2Party(int i, int i2) {
        Message messageInit = init((byte) 49);
        try {
            messageInit.writer().writeByte(i2);
            messageInit.writer().writeShort(i);
            this.session.sendMessage(messageInit);
            messageInit.cleanup();
        } catch (Exception unused) {
        }
    }

    public void trade(int i) {
        Message messageInit = init((byte) 66);
        try {
            messageInit.writer().writeByte(0);
            messageInit.writer().writeShort(i);
            this.session.sendMessage(messageInit);
            messageInit.cleanup();
        } catch (Exception unused) {
        }
    }

    public void sendInfoAddFriend(int i, int i2) {
        Message messageInit = init((byte) 101);
        try {
            messageInit.writer().writeShort(i);
            messageInit.writer().writeByte(i2);
            this.session.sendMessage(messageInit);
        } catch (Exception unused) {
        }
    }

    public void getFriendList() {
        Message messageInit = init((byte) 102);
        try {
            messageInit.writer().writeByte(0);
            this.session.sendMessage(messageInit);
        } catch (Exception unused) {
        }
    }

    public void getDetailFriend(int i) {
        Message messageInit = init((byte) 102);
        try {
            messageInit.writer().writeByte(1);
            messageInit.writer().writeInt(i);
            this.session.sendMessage(messageInit);
        } catch (Exception unused) {
        }
    }

    public void sendOk_notOkInviteTrafe(int i, int i2) {
        Message messageInit = init((byte) 66);
        try {
            messageInit.writer().writeByte(i2);
            messageInit.writer().writeShort(i);
            this.session.sendMessage(messageInit);
            messageInit.cleanup();
        } catch (Exception unused) {
        }
    }

    public void sendOkTrade(int i) {
        Message messageInit = init((byte) 66);
        try {
            messageInit.writer().writeByte(4);
            messageInit.writer().writeShort(i);
            this.session.sendMessage(messageInit);
            messageInit.cleanup();
        } catch (Exception unused) {
        }
    }

    public void sendCancelTrade(int i) {
        Message messageInit = init((byte) 66);
        try {
            messageInit.writer().writeByte(3);
            messageInit.writer().writeShort(i);
            this.session.sendMessage(messageInit);
            messageInit.cleanup();
        } catch (Exception unused) {
        }
    }

    public void sendAddItemTrade(short s, int i, int i2) {
        Message messageInit = init((byte) 66);
        try {
            messageInit.writer().writeByte(2);
            messageInit.writer().writeByte(i);
            if (i == 0) {
                messageInit.writer().writeShort(s);
            } else if (i == 1) {
                messageInit.writer().writeByte(s);
                messageInit.writer().writeShort(i2);
            } else if (i == 2) {
                messageInit.writer().writeByte(s);
            }
            this.session.sendMessage(messageInit);
            messageInit.cleanup();
        } catch (Exception unused) {
        }
    }

    public void kickOutParty(int i, byte b) {
        Message messageInit = init((byte) 50);
        try {
            messageInit.writer().writeByte(b);
            messageInit.writer().writeShort(i);
            this.session.sendMessage(messageInit);
            messageInit.cleanup();
        } catch (Exception unused) {
        }
    }

    public void joinParty(short s) {
        Message messageInit = init((byte) 49);
        try {
            messageInit.writer().writeShort(s);
            this.session.sendMessage(messageInit);
            messageInit.cleanup();
        } catch (Exception unused) {
        }
    }

    public void moveChar(int i, int i2) {
        if (Tilemap.isOfflineMap || Tilemap.tileTypeAtPixel(i, i2, 2) || ((i2 / 16) * Tilemap.w) + (i / 16) >= Tilemap.type.length) {
            return;
        }
        Message messageInit = init((byte) 4);
        try {
            messageInit.writer().writeShort(i);
            messageInit.writer().writeShort(i2);
        } catch (Exception unused) {
        }
        this.session.sendMessage(messageInit);
        messageInit.cleanup();
    }

    public void requestCharInfo(short s) {
        Message messageInit = init((byte) 5);
        try {
            messageInit.writer().writeShort(s);
        } catch (Exception unused) {
        }
        this.session.sendMessage(messageInit);
        messageInit.cleanup();
    }

    public void attackMonster(short s, byte b, byte b2, int i, int i2, int i3) {
        Message messageInit = init((byte) 9);
        try {
            messageInit.writer().writeShort(s);
            messageInit.writer().writeByte(b);
        } catch (Exception unused) {
        }
        this.session.sendMessage(messageInit);
        messageInit.cleanup();
    }

    public void attackMultiMonster(short s, byte b, byte b2, int i, int i2, int i3, Vector vector) {
        Message messageInit = init((byte) 106);
        try {
            messageInit.writer().writeByte(b);
            messageInit.writer().writeByte(vector.size());
            for (int i4 = 0; i4 < vector.size(); i4++) {
                messageInit.writer().writeShort(((Actor) vector.elementAt(i4)).ID);
            }
        } catch (Exception unused) {
        }
        this.session.sendMessage(messageInit);
        messageInit.cleanup();
    }

    public void attackOtherPlayer(short s, byte b, byte b2, int i, int i2, int i3) {
        Message messageInit = init((byte) 6);
        try {
            messageInit.writer().writeShort(s);
            messageInit.writer().writeByte(b);
        } catch (Exception unused) {
        }
        this.session.sendMessage(messageInit);
        messageInit.cleanup();
    }

    public void useBuff(short s, byte b, byte b2, short s2) {
        Message messageInit = init((byte) 51);
        try {
            messageInit.writer().writeByte(1);
            messageInit.writer().writeShort(s);
            messageInit.writer().writeByte(b);
            messageInit.writer().writeByte(b2);
            messageInit.writer().writeShort(s2);
        } catch (Exception unused) {
        }
        this.session.sendMessage(messageInit);
        messageInit.cleanup();
    }

    public void endBuff(short s, byte b) {
        Message messageInit = init((byte) 51);
        try {
            messageInit.writer().writeByte(0);
            messageInit.writer().writeShort(s);
            messageInit.writer().writeByte(b);
        } catch (Exception unused) {
        }
        this.session.sendMessage(messageInit);
        messageInit.cleanup();
    }

    public void requestMonsterInfo(short s) {
        // mod: auto vận tiêu nháy map - không xin thông tin quái thường (tránh chống flood của server)
        if (game.render.screen.AutoGuard.chanXinQuai(s)) {
            return;
        }
        Message messageInit = init((byte) 7);
        try {
            messageInit.writer().writeShort(s);
        } catch (Exception unused) {
        }
        this.session.sendMessage(messageInit);
        messageInit.cleanup();
    }

    public void ping() {
        init((byte) 11).cleanup();
    }

    public void setSession(ISession iSession) {
        this.session = iSession;
    }

    public void selectChar(int i, int i2) {
        try {
            Message messageInit = init((byte) 13);
            messageInit.writer().writeByte(i2);
            messageInit.writer().writeInt(i);
            this.session.sendMessage(messageInit);
            messageInit.cleanup();
        } catch (IOException unused) {
        }
    }

    public void createChar(String str, int i, int i2, int i3, int i4) {
        Message messageInit = init((byte) 14);
        try {
            messageInit.writer().writeUTF(str);
            messageInit.writer().writeByte(i);
            messageInit.writer().writeByte(i2);
            messageInit.writer().writeByte(i3);
            messageInit.writer().writeByte(i4);
            messageInit.writer().writeByte(ServerListScr.index_server[ServerListScr.index]);
            this.session.sendMessage(messageInit);
            messageInit.cleanup();
        } catch (IOException unused) {
        }
    }

    public void setClientType() {
        Message messageInit = init((byte) -1);
        try {
            messageInit.writer().writeByte(2);
        } catch (Exception unused) {
        }
        this.session.sendMessage(messageInit);
        messageInit.cleanup();
    }

    public void getDropableFromGround(byte b, short s) {
        try {
            if (b == 3) {
                Message messageInit = init((byte) 18);
                messageInit.writer().writeShort(s);
                this.session.sendMessage(messageInit);
                messageInit.cleanup();
            } else if (b == 4) {
                Message messageInit2 = init((byte) 19);
                messageInit2.writer().writeShort(s);
                this.session.sendMessage(messageInit2);
                messageInit2.cleanup();
            } else if (b == 7 || b == 6) {
                Message messageInit3 = init((byte) -41);
                messageInit3.writer().writeByte(b);
                messageInit3.writer().writeShort(s);
                this.session.sendMessage(messageInit3);
                messageInit3.cleanup();
            } else {
                if (b != 14) {
                    return;
                }
                Message messageInit4 = init((byte) -65);
                messageInit4.writer().writeShort(s);
                this.session.sendMessage(messageInit4);
            }
        } catch (IOException unused) {
        }
    }

    public void requestMainCharInfo() {
        Message messageInit = init((byte) 88);
        try {
            this.session.sendMessage(messageInit);
        } catch (Exception unused) {
        }
        messageInit.cleanup();
    }

    public void sendRequestMainInfo(int i, int i2) {
        Message messageInit = init((byte) -2);
        try {
            messageInit.writer().writeByte(i);
            messageInit.writer().writeShort(i2);
            this.session.sendMessage(messageInit);
        } catch (Exception unused) {
        }
        messageInit.cleanup();
    }

    public void requestWearingInfo(int i, int i2) {
        Message messageInit = init((byte) -3);
        try {
            messageInit.writer().writeByte(i);
            messageInit.writer().writeShort(i2);
            this.session.sendMessage(messageInit);
        } catch (Exception unused) {
        }
        messageInit.cleanup();
    }

    public void buyTicket() {
        Message messageInit = init((byte) 81);
        try {
            this.session.sendMessage(messageInit);
            messageInit.cleanup();
        } catch (Exception unused) {
        }
    }

    public void requestItemInfo(short s, short s2) {
        Message messageInit = init((byte) 21);
        try {
            messageInit.writer().writeShort(s);
            messageInit.writer().writeShort(s2);
            this.session.sendMessage(messageInit);
            messageInit.cleanup();
        } catch (IOException unused) {
        }
    }

    public void usePotion(int i) {
        Message messageInit = init((byte) 22);
        try {
            messageInit.writer().writeByte(i);
            this.session.sendMessage(messageInit);
            messageInit.cleanup();
        } catch (IOException unused) {
        }
    }

    public void changeMap(int i, int i2, int i3) {
        ChangScr.gI().setMap(i, i2, i3);
        GCanvas.gameScr.posBoss = null;
        Message messageInit = init((byte) 12);
        try {
            messageInit.writer().writeShort(i);
            messageInit.writer().writeShort(i2);
            messageInit.writer().writeShort(i3);
            this.session.sendMessage(messageInit);
            messageInit.cleanup();
        } catch (IOException unused) {
        }
    }

    public void changeMapOK(int i, int i2, int i3) {
        Message messageInit = init((byte) 12);
        try {
            messageInit.writer().writeShort(i);
            messageInit.writer().writeShort(i2);
            messageInit.writer().writeShort(i3);
            this.session.sendMessage(messageInit);
            messageInit.cleanup();
        } catch (IOException unused) {
        }
    }

    public void requestNPCInfo(int i) {
        Message messageInit = init((byte) 23);
        try {
            messageInit.writer().writeByte(i);
            this.session.sendMessage(messageInit);
            messageInit.cleanup();
        } catch (IOException unused) {
        }
    }

    public void requestNPCInfo(int i, int i2) {
        Message messageInit = init((byte) 23);
        try {
            messageInit.writer().writeByte(i);
            messageInit.writer().writeByte(i2);
            this.session.sendMessage(messageInit);
            messageInit.cleanup();
        } catch (IOException unused) {
        }
    }

    public void buyGemItem(Vector vector) {
        Message messageInit = init((byte) 74);
        try {
            int size = vector.size();
            int i = 0;
            for (int i2 = 0; i2 < size; i2++) {
                i += ((GemTemp) vector.elementAt(i2)).number;
            }
            messageInit.writer().writeShort(i);
            for (int i3 = 0; i3 < size; i3++) {
                GemTemp gemTemp = (GemTemp) vector.elementAt(i3);
                for (int i4 = 0; i4 < gemTemp.number; i4++) {
                    messageInit.writer().writeShort(gemTemp.id);
                }
            }
            this.session.sendMessage(messageInit);
            messageInit.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void buyItem(Vector vector) {
        Message messageInit = init((byte) 24);
        try {
            int size = vector.size();
            messageInit.writer().writeByte(size);
            for (int i = 0; i < size; i++) {
                ItemInInventory itemInInventory = (ItemInInventory) vector.elementAt(i);
                ItemTemplate item = Res.getItem(itemInInventory.charClazz, itemInInventory.idTemplate);
                messageInit.writer().writeByte(itemInInventory.catagory);
                messageInit.writer().writeShort(item.id);
                messageInit.writer().writeShort(1);
                if (itemInInventory.catagory == 3) {
                    if (WindowInfoScr.gI().idClass >= 0) {
                        messageInit.writer().writeByte(WindowInfoScr.gI().idClass);
                    } else {
                        messageInit.writer().writeByte(item.clazz);
                    }
                }
            }
            this.session.sendMessage(messageInit);
            messageInit.cleanup();
        } catch (IOException unused) {
        }
    }

    public void buyPotion(Vector vector) {
        Message messageInit = init((byte) 24);
        try {
            int size = vector.size();
            messageInit.writer().writeByte(size);
            for (int i = 0; i < size; i++) {
                ItemInInventory itemInInventory = (ItemInInventory) vector.elementAt(i);
                messageInit.writer().writeByte(4);
                messageInit.writer().writeShort(itemInInventory.potionType);
                messageInit.writer().writeShort(itemInInventory.number);
            }
            this.session.sendMessage(messageInit);
            messageInit.cleanup();
        } catch (IOException unused) {
        }
    }

    public void chat(String str) {
        Message messageInit = init((byte) 27);
        try {
            messageInit.writer().writeUTF(str);
            this.session.sendMessage(messageInit);
            messageInit.cleanup();
        } catch (IOException unused) {
        }
    }

    public void finishPutItem2Bag() {
        Message messageInit = init((byte) 70);
        try {
            this.session.sendMessage(messageInit);
            messageInit.cleanup();
        } catch (Exception unused) {
        }
    }

    public void putItem2Bag(short s) {
        Message messageInit = init((byte) 68);
        try {
            messageInit.writer().writeShort(s);
            this.session.sendMessage(messageInit);
            messageInit.cleanup();
        } catch (IOException unused) {
        }
    }

    public void getItemFromBag(short s) {
        Message messageInit = init((byte) 69);
        try {
            messageInit.writer().writeShort(s);
            this.session.sendMessage(messageInit);
            messageInit.cleanup();
        } catch (IOException unused) {
        }
    }

    public void sellItem(short s) {
        Message messageInit = init((byte) 28);
        try {
            messageInit.writer().writeShort(s);
            this.session.sendMessage(messageInit);
            messageInit.cleanup();
        } catch (IOException unused) {
        }
    }

    public void sellGemItem(short s, byte b) {
        Message messageInit = init((byte) 78);
        try {
            messageInit.writer().writeShort(s);
            messageInit.writer().writeByte(b);
            this.session.sendMessage(messageInit);
            messageInit.cleanup();
        } catch (IOException unused) {
        }
    }

    public void useItem(short s) {
        Message messageInit = init((byte) 29);
        try {
            messageInit.writer().writeShort(s);
            this.session.sendMessage(messageInit);
            messageInit.cleanup();
        } catch (IOException unused) {
        }
    }

    public void giveItemToGround(short s) {
        Message messageInit = init((byte) 61);
        try {
            messageInit.writer().writeShort(s);
            this.session.sendMessage(messageInit);
            messageInit.cleanup();
        } catch (IOException unused) {
        }
    }

    public void comeHomeWhenDie() {
        Message messageInit = init((byte) 31);
        this.session.sendMessage(messageInit);
        messageInit.cleanup();
    }

    public void addBasePoint(int i, int i2) {
        Message messageInit = init((byte) 34);
        try {
            messageInit.writer().writeByte(i);
            messageInit.writer().writeShort(i2);
            this.session.sendMessage(messageInit);
            messageInit.cleanup();
        } catch (IOException unused) {
        }
    }

    public void addSkillPoint(int i) {
        Message messageInit = init((byte) 36);
        try {
            messageInit.writer().writeByte(i);
            this.session.sendMessage(messageInit);
            messageInit.cleanup();
        } catch (IOException unused) {
        }
    }

    public void requestRegister(String str, String str2) {
        Message messageInit = init((byte) 39);
        try {
            messageInit.writer().writeUTF(str);
            messageInit.writer().writeUTF(str2);
            GameMidlet.client_Provider.trim();
            messageInit.writer().writeUTF(GameMidlet.client_Provider);
            messageInit.writer().writeUTF(GameMidlet.provider);
            messageInit.writer().writeUTF(GameMidlet.agent);
            messageInit.writer().writeUTF("");
            messageInit.writer().writeInt(0);
            messageInit.writer().writeByte(1);
        } catch (IOException unused) {
        }
        this.session.sendMessage(messageInit);
        messageInit.cleanup();
    }

    public void receiveQuest(int i, int i2, int i3) {
        Message messageInit = init((byte) 52);
        try {
            messageInit.writer().writeByte(i3);
            messageInit.writer().writeByte(i);
            messageInit.writer().writeShort(i2);
        } catch (Exception unused) {
        }
        this.session.sendMessage(messageInit);
        messageInit.cleanup();
    }

    public void talk_npc_response_quest(int i, int i2, int i3) {
        Message messageInit = init((byte) 55);
        try {
            messageInit.writer().writeByte(i);
            messageInit.writer().writeByte(i2);
            messageInit.writer().writeShort(i3);
            this.session.sendMessage(messageInit);
        } catch (Exception unused) {
        } catch (Throwable th) {
            try {
                messageInit.cleanup();
            } catch (Exception unused2) {
            }
            throw th;
        }
        try {
            messageInit.cleanup();
        } catch (Exception unused3) {
        }
    }

    public void talk_npc(int i, int i2, int i3) {
        Message messageInit = init((byte) 53);
        try {
            messageInit.writer().writeByte(i);
            messageInit.writer().writeByte(i2);
            messageInit.writer().writeShort(i3);
            this.session.sendMessage(messageInit);
        } catch (Exception unused) {
        } catch (Throwable th) {
            try {
                messageInit.cleanup();
            } catch (Exception unused2) {
            }
            throw th;
        }
        try {
            messageInit.cleanup();
        } catch (Exception unused3) {
        }
    }

    public void sendRequestAddClan(short s, byte b, boolean z) {
        Message messageInit = init((byte) -11);
        try {
            messageInit.writer().writeByte(b);
            messageInit.writer().writeShort(s);
            if (b == 1) {
                messageInit.writer().writeBoolean(z);
            }
        } catch (IOException unused) {
        }
        this.session.sendMessage(messageInit);
    }

    public void sendMsgPrivate(String str, String str2) {
        Message messageInit = init((byte) -5);
        try {
            messageInit.writer().writeUTF(str);
            messageInit.writer().writeUTF(str2);
        } catch (IOException unused) {
        }
        this.session.sendMessage(messageInit);
    }

    public void removeFriend(String str) {
        Message messageInit = init((byte) -6);
        try {
            messageInit.writer().writeUTF(str);
        } catch (IOException unused) {
        }
        this.session.sendMessage(messageInit);
    }

    public void requestClanList(int i, byte b) {
        Message messageInit = init((byte) -7);
        try {
            messageInit.writer().writeByte(b);
            messageInit.writer().writeShort(i);
        } catch (IOException unused) {
        }
        this.session.sendMessage(messageInit);
    }

    public void doGetImage(byte b, int i) {
        Message messageInit = init((byte) -8);
        try {
            messageInit.writer().writeByte(b);
            messageInit.writer().writeByte(i);
        } catch (IOException unused) {
        }
        this.session.sendMessage(messageInit);
    }

    public void registerClan() {
        this.session.sendMessage(init((byte) -9));
    }

    public void chooseIconClan(short s, String str) {
        Message messageInit = init((byte) -10);
        try {
            messageInit.writer().writeShort(s);
            messageInit.writer().writeUTF(str);
        } catch (IOException unused) {
        }
        this.session.sendMessage(messageInit);
    }

    public void requestClanInfo(int i) {
        GCanvas.startWaitDlg();
        Message messageInit = init((byte) -12);
        try {
            messageInit.writer().writeShort(i);
        } catch (IOException unused) {
        }
        this.session.sendMessage(messageInit);
    }

    public void requestEvictionClan(String str) {
        Message messageInit = init((byte) -13);
        try {
            messageInit.writer().writeUTF(str);
        } catch (IOException unused) {
        }
        this.session.sendMessage(messageInit);
    }

    public void dissolveCLan() {
        this.session.sendMessage(init((byte) -14));
    }

    public void outClan() {
        this.session.sendMessage(init((byte) -15));
    }

    public void setSologan(String str) {
        Message messageInit = init((byte) -16);
        try {
            messageInit.writer().writeUTF(str);
        } catch (IOException unused) {
        }
        this.session.sendMessage(messageInit);
    }

    public void msgClanAll(String str, byte b, int i) {
        Message messageInit = init((byte) -17);
        try {
            messageInit.writer().writeByte(b);
            if (b == 0) {
                messageInit.writer().writeUTF(str);
            } else if (b == 2) {
                messageInit.writer().writeInt(i);
            }
        } catch (IOException unused) {
        }
        this.session.sendMessage(messageInit);
    }

    public void chatToClan(String str) {
        Message messageInit = init((byte) -18);
        try {
            messageInit.writer().writeUTF(str);
        } catch (IOException unused) {
        }
        this.session.sendMessage(messageInit);
    }

    public void requestTopStronger_Righer(int i, int i2) {
        GCanvas.startWaitDlg();
        Message messageInit = init((byte) -19);
        try {
            messageInit.writer().writeByte(i);
            messageInit.writer().writeByte(i2);
        } catch (IOException unused) {
        }
        this.session.sendMessage(messageInit);
    }

    public void transMoney(int i) {
        Message messageInit = init((byte) -20);
        try {
            messageInit.writer().writeInt(i);
        } catch (IOException unused) {
        }
        this.session.sendMessage(messageInit);
    }

    public void dellPotion(int i) {
        Message messageInit = init((byte) -21);
        try {
            messageInit.writer().writeShort(i);
        } catch (IOException unused) {
        }
        this.session.sendMessage(messageInit);
    }

    public void requestSomeOneInfo(short s, byte b) {
        Message messageInit = init((byte) -22);
        try {
            messageInit.writer().writeShort(s);
            messageInit.writer().writeByte(b);
        } catch (IOException unused) {
        }
        this.session.sendMessage(messageInit);
    }

    public void dellGemItem(short s, int i, byte b) {
        Message messageInit = init((byte) -23);
        try {
            messageInit.writer().writeByte(i);
            messageInit.writer().writeShort(s);
            if (i == 0) {
                messageInit.writer().writeByte(b);
            }
        } catch (IOException unused) {
        }
        this.session.sendMessage(messageInit);
    }

    public void requestString(int i) {
        Message messageInit = init((byte) -24);
        try {
            messageInit.writer().writeByte(i);
        } catch (Exception unused) {
        }
        this.session.sendMessage(messageInit);
    }

    public void doRequestWeapone(int i, int i2, int i3, byte b) {
        Message messageInit = init((byte) -27);
        try {
            messageInit.writer().writeByte(i);
            messageInit.writer().writeByte(i2);
            messageInit.writer().writeByte(i3);
            messageInit.writer().writeByte(b);
        } catch (Exception unused) {
        }
        this.session.sendMessage(messageInit);
    }

    public void doCapCha(String str) {
        Message messageInit = init((byte) -28);
        try {
            messageInit.writer().writeUTF(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.session.sendMessage(messageInit);
    }

    public void doMenuOption(int i, byte b, int i2) {
        Message messageInit = init((byte) -30);
        try {
            messageInit.writer().writeShort(i);
            messageInit.writer().writeByte(b);
            messageInit.writer().writeByte(i2);
        } catch (IOException unused) {
        }
        this.session.sendMessage(messageInit);
    }

    public void doTextBox(int i, byte b, String str) {
        Message messageInit = init((byte) -31);
        try {
            messageInit.writer().writeShort(i);
            messageInit.writer().writeByte(b);
            messageInit.writer().writeUTF(str);
        } catch (IOException unused) {
        }
        this.session.sendMessage(messageInit);
    }

    public void doCustomPopup(int i, byte b, String str, int i2) {
        Message messageInit = init((byte) -32);
        try {
            messageInit.writer().writeShort(i);
            messageInit.writer().writeByte(b);
            messageInit.writer().writeUTF(str);
            messageInit.writer().writeByte(i2);
        } catch (Exception unused) {
        }
        this.session.sendMessage(messageInit);
    }

    public void doWayPoint(int i, int i2, short s) {
        Message messageInit = init((byte) -33);
        try {
            messageInit.writer().writeByte(i / 16);
            messageInit.writer().writeByte(i2 / 16);
            messageInit.writer().writeShort(s);
        } catch (Exception unused) {
        }
        this.session.sendMessage(messageInit);
    }

    public void requestKham(int i) {
        Message messageInit = init((byte) -34);
        try {
            messageInit.writer().writeByte(i);
        } catch (Exception unused) {
        }
        this.session.sendMessage(messageInit);
    }

    public void doKhamItem(int i) {
        Message messageInit = init((byte) -35);
        try {
            messageInit.writer().writeByte(i);
        } catch (Exception unused) {
        }
        this.session.sendMessage(messageInit);
    }

    public void epNgoc(int i, Vector vector) {
        Message messageInit = init((byte) -36);
        try {
            messageInit.writer().writeByte(i);
            if (i == 1) {
                int size = vector.size();
                messageInit.writer().writeByte(size);
                for (int i2 = 0; i2 < size; i2++) {
                    messageInit.writer().writeShort(((RealID) vector.elementAt(i2)).realID);
                }
            }
        } catch (IOException unused) {
        }
        this.session.sendMessage(messageInit);
    }

    public void epDothu(Vector vector, byte[] bArr) {
        Message messageInit = init((byte) -61);
        try {
            messageInit.writer().writeByte(vector.size());
            for (int i = 0; i < vector.size(); i++) {
                messageInit.writer().writeShort(((ItemInInventory) vector.elementAt(i)).itemID);
            }
            for (byte b : bArr) {
                messageInit.writer().writeByte(b);
            }
        } catch (IOException unused) {
        }
        this.session.sendMessage(messageInit);
    }

    public void questClan(int i) {
        Message messageInit = init((byte) -37);
        try {
            messageInit.writer().writeByte(i);
        } catch (Exception unused) {
        }
        this.session.sendMessage(messageInit);
    }

    public void doRideAnimal(short s, byte b) {
        Message messageInit = init((byte) -45);
        try {
            messageInit.writer().writeByte(b);
            messageInit.writer().writeShort(s);
        } catch (Exception unused) {
        }
        this.session.sendMessage(messageInit);
    }

    public void doRequestSoundData(byte b) {
        Message message = new Message((byte) -46);
        try {
            message.writer().writeByte(b);
        } catch (Exception unused) {
        }
        this.session.sendMessage(message);
    }

    public void doRequestImageMonster(int i, byte b, byte b2) {
        Message message = new Message((byte) -47);
        try {
            message.writer().writeByte(b);
            message.writer().writeByte(b2);
            message.writer().writeShort(i);
        } catch (Exception unused) {
        }
        this.session.sendMessage(message);
    }

    public void getTreeImage(int i, int i2, int i3) {
        Message message = new Message((byte) -48);
        try {
            if (i == -1) {
                message.writer().writeByte(i);
                message.writer().writeByte(i2);
                message.writer().writeByte(i3);
            } else {
                message.writer().writeByte(i);
            }
        } catch (Exception unused) {
        }
        this.session.sendMessage(message);
    }

    public void doRequestEffectData(short s) {
        Message message = new Message((byte) -49);
        try {
            message.writer().writeByte(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.session.sendMessage(message);
    }

    public void doCheDo(short s, short s2, byte[][] bArr) {
        Message message = new Message((byte) -52);
        try {
            message.writer().writeByte(WindowInfoScr.optionCreateItem);
            message.writer().writeShort(s2);
            for (int i = 0; i < s; i++) {
                for (int i2 = 0; i2 < bArr[i].length; i2++) {
                    message.writer().writeByte(bArr[i][i2]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.session.sendMessage(message);
    }

    public void doRequestTachNguyenLIeu(short s, int i, int i2, int i3) {
        Message message = new Message((byte) -68);
        try {
            message.writer().writeByte(i);
            message.writer().writeShort(s);
            message.writer().writeShort(i2);
            message.writer().writeByte(i3);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.session.sendMessage(message);
    }

    public void doRequestThemThuocTinh(short s, int i, int i2, int i3, int i4, int i5) {
        Message message = new Message((byte) -68);
        try {
            message.writer().writeByte(i);
            message.writer().writeShort(s);
            message.writer().writeShort(i2);
            message.writer().writeByte(i3);
            message.writer().writeShort(i4);
            message.writer().writeByte(i5);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.session.sendMessage(message);
    }

    public void doRequestActionCheDo(int i, short s) {
        Message message = new Message((byte) -52);
        try {
            message.writer().writeByte(i);
            message.writer().writeShort(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.session.sendMessage(message);
    }

    public void requestQuanLyBang(short s) {
        Message message = new Message((byte) -53);
        try {
            message.writer().writeShort(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.session.sendMessage(message);
    }

    public void doGetImgIcon(short s) {
        Message message = new Message((byte) -51);
        try {
            message.writer().writeShort(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.session.sendMessage(message);
    }

    public void doRequestChangTypeItem(short s, int i) {
        Message message = new Message((byte) -54);
        try {
            message.writer().writeByte(i);
            message.writer().writeShort(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.session.sendMessage(message);
    }

    public void sendGetImgIsanimal(int i, int i2) {
        Message message = new Message((byte) -57);
        try {
            message.writer().writeByte(i);
            message.writer().writeShort(i2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.session.sendMessage(message);
    }

    public void requestCharMonter(int i) {
        Message message = new Message((byte) -57);
        try {
            message.writer().writeByte(-1);
            message.writer().writeShort(i);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.session.sendMessage(message);
    }

    public void doPlant(byte b, byte b2) {
        Message message = new Message((byte) -59);
        try {
            message.writer().writeByte(b);
            message.writer().writeByte(b2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.session.sendMessage(message);
    }

    public void openLuckyBox(byte b, byte b2) {
        Message message = new Message((byte) -62);
        try {
            if (b2 == -100) {
                message.writer().writeByte(b);
            } else {
                message.writer().writeByte(b);
                message.writer().writeByte(b2);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.session.sendMessage(message);
    }

    public void lottery(byte b) {
        Message message = new Message((byte) -63);
        try {
            message.writer().writeByte(b);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.session.sendMessage(message);
    }

    public void onStartLuckyDraw(int i) {
        Message message = new Message((byte) 87);
        try {
            message.writer().writeByte(i);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.session.sendMessage(message);
    }

    public void onSendInfoQuest(int i, int i2, int i3) {
        Message message = new Message((byte) -64);
        try {
            message.writer().writeByte(i);
            message.writer().writeShort(i2);
            message.writer().writeByte(i3);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.session.sendMessage(message);
    }

    public void sendConfirmTrade() {
        Message messageInit = init((byte) 66);
        try {
            messageInit.writer().writeByte(5);
            this.session.sendMessage(messageInit);
            messageInit.cleanup();
        } catch (Exception unused) {
        }
    }

    public void sendInfoFruit(int i, int i2) {
        Message messageInit = init((byte) -66);
        try {
            messageInit.writer().writeByte(i2);
            messageInit.writer().writeByte(i);
            this.session.sendMessage(messageInit);
        } catch (Exception unused) {
        }
    }

    public void douseGemItem(int i, byte b) {
        Message messageInit = init((byte) -67);
        try {
            messageInit.writer().writeShort(i);
            messageInit.writer().writeByte(b);
            this.session.sendMessage(messageInit);
        } catch (Exception unused) {
        }
    }

    public void doAutoImbue(int i, Vector vector, int i2, Vector vector2, int i3) {
        Message messageInit = init((byte) -69);
        try {
            messageInit.writer().writeShort(i);
            messageInit.writer().writeByte(i2);
            messageInit.writer().writeByte(vector.size());
            for (int i4 = 0; i4 < vector.size(); i4++) {
                messageInit.writer().writeShort(Short.parseShort((String) vector.elementAt(i4)));
                messageInit.writer().writeByte(Byte.parseByte((String) vector2.elementAt(i4)));
            }
            messageInit.writer().writeByte(i3);
            this.session.sendMessage(messageInit);
        } catch (Exception unused) {
        }
    }

    public void doRequestSelItem(int i, int i2, int i3, int i4) {
        Message messageInit = init((byte) 62);
        try {
            messageInit.writer().writeByte(i);
            messageInit.writer().writeShort(i2);
            messageInit.writer().writeInt(i3);
            messageInit.writer().writeInt(i4);
            this.session.sendMessage(messageInit);
        } catch (Exception unused) {
        }
    }

    public void doBuyItemShopOnline(int i, int i2) {
        Message messageInit = init((byte) 62);
        try {
            messageInit.writer().writeByte(3);
            messageInit.writer().writeByte(i2);
            messageInit.writer().writeShort(i);
            this.session.sendMessage(messageInit);
        } catch (Exception unused) {
        }
    }

    public void doBid(int i, int i2, int i3) {
        Message messageInit = init((byte) 62);
        try {
            messageInit.writer().writeByte(6);
            messageInit.writer().writeByte(i2);
            messageInit.writer().writeShort(i);
            messageInit.writer().writeInt(i3);
            this.session.sendMessage(messageInit);
        } catch (Exception unused) {
        }
    }

    public void doGetItemBackInventory(int i) {
        Message messageInit = init((byte) 62);
        try {
            messageInit.writer().writeByte(4);
            messageInit.writer().writeByte(i);
            this.session.sendMessage(messageInit);
        } catch (Exception unused) {
        }
    }

    public void doGetMoneySellInventory() {
        Message messageInit = init((byte) 62);
        try {
            messageInit.writer().writeByte(5);
            this.session.sendMessage(messageInit);
        } catch (Exception unused) {
        }
    }

    public void doSearchItemSell(int i, int i2, int i3, int i4, int i5) {
        Message messageInit = init((byte) 62);
        try {
            messageInit.writer().writeByte(2);
            messageInit.writer().writeByte(i);
            messageInit.writer().writeByte(i2);
            messageInit.writer().writeByte(i3);
            messageInit.writer().writeByte(i4);
            messageInit.writer().writeByte(i5);
            this.session.sendMessage(messageInit);
        } catch (Exception unused) {
        }
    }

    public void doSearchByName(String str) {
        Message messageInit = init((byte) 62);
        try {
            messageInit.writer().writeByte(7);
            messageInit.writer().writeUTF(str);
            this.session.sendMessage(messageInit);
        } catch (Exception unused) {
        }
    }
}
