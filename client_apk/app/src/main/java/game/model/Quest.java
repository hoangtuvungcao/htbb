package game.model;

import game.networklogic.GameService;
import game.render.GCanvas;
import game.render.Res;
import game.render.screen.GameScr;
import java.util.Vector;

public class Quest {
    public static final byte DEL = 2;
    public static final byte RECEIVE = 0;
    public static final byte RESPONSE = 1;
    public static final byte TYPE_GET_ITEM = 2;
    public static final byte TYPE_KILL_MONS = 0;
    public static final byte TYPE_KILL_MONSTER_LEVEL = 4;
    public static final byte TYPE_TALK = 3;
    public static final byte TYPE_TRANSFORM = 1;
    public short[] allItemGet;
    public short[] allMonsKilled;
    public short[] idItemGet;
    public short[] idMonsKill;
    public short idQuest;
    public short[] totalItemGet;
    public short[] totalMonskill;
    public byte type = 0;
    public short idNpcReceive = 32000;
    public short idNpcResponse = 32000;
    public byte main_sub = 0;
    public Vector content = new Vector();
    public Vector rescontent = new Vector();
    public String scontent = "";
    public String decript = "";
    public String name = "";
    public String infoGift = "";
    public boolean isShow = false;
    public short deltaLv = 0;
    byte index = 0;
    public byte stateQuest = -1;

    public Quest(int i) {
        this.idQuest = (short) i;
    }

    public void update(Actor actor, Actor actor2) {
        if (GCanvas.keyPressed[5]) {
            GCanvas.keyPressed[5] = false;
            if (actor2 != null && ((actor2.getTypeNpc() != this.idNpcReceive && this.stateQuest == 0) || (actor2.getTypeNpc() != this.idNpcResponse && this.stateQuest == 1))) {
                this.isShow = false;
                this.index = (byte) 0;
                return;
            }
            byte b = (byte) (this.index + 1);
            this.index = b;
            byte b2 = this.stateQuest;
            if (b2 == 0) {
                if (b >= this.content.size() - 1) {
                    if (this.type == 3) {
                        GameScr.addChat(actor2, (String) this.content.elementAt(this.index), 100);
                        this.stateQuest = (byte) 3;
                        GameService.gI().onSendInfoQuest(1, this.idQuest, this.main_sub);
                        this.isShow = false;
                        this.index = (byte) 0;
                        return;
                    }
                    return;
                }
                String str = (String) this.content.elementAt(this.index);
                if (str.startsWith("1")) {
                    GameScr.addChat(actor2, str.substring(1), 100);
                } else {
                    GameScr.addChat(actor, str.substring(1), 100);
                }
                if (this.type == 3 || this.index < this.content.size() - 2) {
                    return;
                }
                byte b3 = (byte) (this.index + 1);
                this.index = b3;
                GCanvas.startYesNoDlg((String) this.content.elementAt(b3), new IAction() {
                    @Override
                    public void perform() {
                        GameService.gI().onSendInfoQuest(0, Quest.this.idQuest, Quest.this.main_sub);
                        Quest.this.isShow = false;
                        GCanvas.endDlg();
                    }
                }, new IAction() {
                    @Override
                    public void perform() {
                        Quest.this.isShow = false;
                        Quest.this.index = (byte) 0;
                        GCanvas.endDlg();
                    }
                });
                return;
            }
            if (b2 != 1) {
                if (b2 == 2) {
                    GameScr.addChat(actor2, this.decript, 100);
                    this.isShow = false;
                    this.index = (byte) 0;
                    return;
                }
                return;
            }
            if (b >= this.rescontent.size() - 1) {
                GameService.gI().onSendInfoQuest(1, this.idQuest, this.main_sub);
                this.isShow = false;
                this.stateQuest = (byte) 3;
                GCanvas.endDlg();
                return;
            }
            String str2 = (String) this.rescontent.elementAt(this.index);
            if (str2.startsWith("1")) {
                GameScr.addChat(actor2, str2.substring(1), 100);
            } else {
                GameScr.addChat(actor, str2.substring(1), 100);
            }
            if (this.index >= this.rescontent.size() - 2) {
                this.index = (byte) (this.index + 1);
                GameService.gI().onSendInfoQuest(1, this.idQuest, this.main_sub);
                this.isShow = false;
                this.stateQuest = (byte) 3;
                GCanvas.endDlg();
            }
        }
    }

    public void startTalk(Actor actor, Actor actor2, int i) {
        String str;
        this.index = (byte) 0;
        if (i == 0) {
            str = (String) this.content.elementAt(0);
        } else if (i == 2) {
            GameScr.addChat(actor2, this.decript, 100);
            return;
        } else if (i != 1) {
            str = "";
        } else {
            str = (String) this.rescontent.elementAt(0);
        }
        if (str.startsWith("1")) {
            GameScr.addChat(actor2, str.substring(1), 100);
        } else {
            GameScr.addChat(actor, str.substring(1), 100);
        }
        this.isShow = true;
        if (this.stateQuest == 0 && this.type != 3 && this.content.size() <= 2) {
            byte b = (byte) (this.index + 1);
            this.index = b;
            GCanvas.startYesNoDlg((String) this.content.elementAt(b), new IAction() {
                @Override
                public void perform() {
                    GameService.gI().onSendInfoQuest(0, Quest.this.idQuest, Quest.this.main_sub);
                    Quest.this.isShow = false;
                    GCanvas.endDlg();
                }
            }, new IAction() {
                @Override
                public void perform() {
                    Quest.this.isShow = false;
                    Quest.this.index = (byte) 0;
                    GCanvas.endDlg();
                }
            });
        } else {
            if (this.stateQuest != 1 || this.type == 3 || this.rescontent.size() > 2) {
                return;
            }
            this.index = (byte) (this.index + 1);
            GameService.gI().onSendInfoQuest(1, this.idQuest, this.main_sub);
            this.isShow = false;
            this.stateQuest = (byte) 3;
            GCanvas.endDlg();
        }
    }

    public String getItemQuest(int i) {
        if (this.type != 2) {
            return "";
        }
        int i2 = 0;
        while (true) {
            short[] sArr = this.idItemGet;
            if (i2 < sArr.length) {
                if (sArr[i2] == i) {
                    short[] sArr2 = this.allItemGet;
                    short s = sArr2[i2];
                    short[] sArr3 = this.totalItemGet;
                    if (s < sArr3[i2]) {
                        short s2 = (short) (s + 1);
                        sArr2[i2] = s2;
                        return (s2 < sArr3[i2] ? new StringBuilder("Nhặt được ").append((int) this.allItemGet[i2]).append("/").append((int) this.totalItemGet[i2]).append(" ").append(ItemQuest.NAME_ITEM[i]) : new StringBuilder("Nhiệm vụ ").append(this.name).append(" đã hoàn thành")).toString();
                    }
                }
                i2++;
            } else {
                return "";
            }
        }
    }

    public String killMonster(int i, String str) {
        StringBuilder sbAppend;
        if (this.type != 0) {
            return "";
        }
        int i2 = 0;
        while (true) {
            short[] sArr = this.idMonsKill;
            if (i2 < sArr.length) {
                if (sArr[i2] == i) {
                    short[] sArr2 = this.allMonsKilled;
                    short s = sArr2[i2];
                    short[] sArr3 = this.totalMonskill;
                    if (s < sArr3[i2]) {
                        short s2 = (short) (s + 1);
                        sArr2[i2] = s2;
                        if (s2 < sArr3[i2]) {
                            sbAppend = new StringBuilder("Hạ được ").append((int) this.allMonsKilled[i2]).append("/").append((int) this.totalMonskill[i2]).append(" ");
                        } else {
                            sbAppend = new StringBuilder("Nhiệm vụ ").append(this.name);
                            str = " đã hoàn thành";
                        }
                        return sbAppend.append(str).toString();
                    }
                }
                i2++;
            } else {
                return "";
            }
        }
    }

    public String killMonsterByLv(int i) {
        StringBuilder sbAppend;
        String str;
        if (this.type != 4 || i > this.deltaLv) {
            return "";
        }
        int i2 = 0;
        while (true) {
            short[] sArr = this.allMonsKilled;
            if (i2 < sArr.length) {
                short s = sArr[i2];
                short[] sArr2 = this.totalMonskill;
                if (s < sArr2[i2]) {
                    short s2 = (short) (s + 1);
                    sArr[i2] = s2;
                    if (s2 < sArr2[i2]) {
                        sbAppend = new StringBuilder("Hạ được ").append((int) this.allMonsKilled[i2]).append("/").append((int) this.totalMonskill[i2]);
                        str = " ";
                    } else {
                        sbAppend = new StringBuilder("Nhiệm vụ ").append(this.name);
                        str = " đã hoàn thành";
                    }
                    return sbAppend.append(str).toString();
                }
                i2++;
            } else {
                return "";
            }
        }
    }

    public String getInfoPaintScr() {
        byte b = this.stateQuest;
        int i = 1;
        if (b == 1) {
            return this.decript + "|" + this.infoGift;
        }
        if (b == 2) {
            byte b2 = this.type;
            if (b2 == 0) {
                String str = Res.monsterTemplates[this.idMonsKill[0]].name + ": " + ((int) this.allMonsKilled[0]) + "/" + ((int) this.totalMonskill[0]);
                while (i < this.idMonsKill.length) {
                    str = str + "|" + Res.monsterTemplates[this.idMonsKill[i]] + ": " + ((int) this.allMonsKilled[i]) + "/" + ((int) this.totalMonskill[i]);
                    i++;
                }
                return str + "|" + this.infoGift;
            }
            if (b2 == 2) {
                String str2 = ItemQuest.NAME_ITEM[this.idItemGet[0]] + ": " + ((int) this.allItemGet[0]) + "/" + ((int) this.totalItemGet[0]);
                while (i < this.idItemGet.length) {
                    str2 = str2 + "|" + ItemQuest.NAME_ITEM[this.idItemGet[i]] + ": " + ((int) this.allItemGet[i]) + "/" + ((int) this.totalItemGet[i]);
                    i++;
                }
                return str2 + "|" + this.infoGift;
            }
            if (b2 == 4) {
                return ("Hạ: " + ((int) this.allMonsKilled[0]) + "/" + ((int) this.totalMonskill[0])) + "|" + this.infoGift;
            }
        }
        return "";
    }

    public boolean isTalk() {
        return this.type == 3;
    }

    public boolean isGetItem() {
        return this.type == 2;
    }

    public boolean isKillMons() {
        return this.type == 0;
    }

    public boolean isKillMonsByLv() {
        return this.type == 4;
    }

    public boolean isTransform() {
        return this.type == 1;
    }

    public boolean isFinish() {
        return this.stateQuest == 1;
    }

    public boolean isReceive() {
        return this.stateQuest == 0;
    }

    public boolean isWorking() {
        return this.stateQuest == 2;
    }
}
