package game.model;

import game.networklogic.GameService;
import game.render.Canvas;
import game.render.Res;
import game.render.screen.GameScr;
import java.util.Vector;

public class Quest {
    public static final byte TYPE_KILL_MONS = 0;
    public static final byte TYPE_TRANSFORM = 1;
    public static final byte TYPE_GET_ITEM = 2;
    public static final byte TYPE_TALK = 3;
    public static final byte TYPE_KILL_MONSTER_LEVEL = 4;
    public static final byte RECEIVE = 0;
    public static final byte DEL = 2;
    public static final byte RESPONSE = 1;
    public short idQuest;
    public short[] idMonsKill;
    public short[] allMonsKilled;
    public short[] totalMonskill;
    public byte stateQuest;
    public short[] idItemGet;
    public short[] allItemGet;
    public short[] totalItemGet;
    byte index;
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
    /**
     * 0.9.15 (máy chủ gửi đuôi gói -64 cho client >= 915): câu mục tiêu ("Hạ 20 Weedle ở Lavender"), nơi trả
     * ("Machamp (Pewter)", "" = không NPC / Sổ tay), gợi ý khoá ("" = không), cờ (bit0 cửa ải ở ô đội, bit1 bản đồ
     * mục tiêu đang khoá). Rỗng = máy chủ cũ: sổ nhiệm vụ vẽ như trước.
     */
    public String muc = "";
    public String tra = "";
    public String khoa = "";
    public int co = 0;
    public static final int CO_CUA_AI = 1, CO_KHOA = 2;

    /** Có dữ liệu mục tiêu từ máy chủ (đuôi gói -64). */
    public boolean coMuc() {
        return this.muc != null && this.muc.length() > 0;
    }

    public boolean laCuaAi() {
        return (this.co & CO_CUA_AI) != 0;
    }

    public Quest(int id) {
        this.stateQuest = (byte) 0;
        this.index = (byte) 0;
        this.index = (byte) 0;
        this.stateQuest = (byte) -1;
        this.idQuest = (short) id;
    }

    public void update(Actor mychar, Actor npc) {
        try {
            capNhat(mychar, npc);
        } catch (RuntimeException e) {
            // dữ liệu nhiệm vụ lệch (chỉ số dòng thoại): đóng hội thoại thay vì ném lỗi mỗi khung hình
            this.isShow = false;
            this.index = (byte) 0;
        }
    }

    /** Lời thoại bỏ ký tự người nói đầu dòng ("1" NPC / "0" người chơi). */
    static String loi(Object o) {
        String s = o == null ? "" : (String) o;
        return s.length() > 0 && (s.charAt(0) == '0' || s.charAt(0) == '1') ? s.substring(1) : s;
    }

    /** 0.9.4: hỏi nhận nhiệm vụ (lời cuối làm câu hỏi). Có / Không đều đóng hội thoại. */
    void hoiNhan() {
        String hoi = this.content.size() > 0 ? loi(this.content.elementAt(this.content.size() - 1)) : "";
        if (hoi.length() == 0) {
            hoi = "Nhận nhiệm vụ " + this.name + "?";
        }
        Canvas.startYesNoDlg(hoi, new IAction() {
            @Override
            public void perform() {
                GameService.gI().onSendInfoQuest(0, Quest.this.idQuest, Quest.this.main_sub);
                Quest.this.isShow = false;
                Quest.this.index = (byte) 0;
                Canvas.endDlg();
            }
        }, new IAction() {
            @Override
            public void perform() {
                Quest.this.isShow = false;
                Quest.this.index = (byte) 0;
                Canvas.endDlg();
            }
        });
    }

    /** 0.9.4: đóng hội thoại nhiệm vụ (Back / Esc / phím mềm) - người chơi luôn thoát được. */
    public void dong() {
        this.isShow = false;
        this.index = (byte) 0;
        game.pokemon.HopThoaiDuoi.giu(false);
        game.pokemon.HopThoaiDuoi.tat();
    }

    private void capNhat(Actor mychar, Actor npc) {
        if (Canvas.keyPressed[12] || Canvas.keyPressed[13]) {
            Canvas.keyPressed[12] = false;
            Canvas.keyPressed[13] = false;
            dong();
            return;
        }
        if (Canvas.keyPressed[5]) {
            Canvas.keyPressed[5] = false;
            if (npc != null && ((npc.getTypeNpc() != this.idNpcReceive && this.stateQuest == 0) || (npc.getTypeNpc() != this.idNpcResponse && this.stateQuest == 1))) {
                this.isShow = false;
                this.index = (byte) 0;
                return;
            }
            this.index = (byte) (this.index + 1);
            if (this.stateQuest == 0) {
                if (this.index >= this.content.size() - 1) {
                    // hết lời thoại. Trước đây elementAt(index) với index = size (nội dung 1 dòng) ném
                    // ArrayIndexOutOfBounds MỖI lần bấm phím (isShow vẫn bật nên GameScr gọi lại mãi)
                    if (this.type == 3) {
                        if (this.content.size() > 0) {
                            GameScr.addChat(npc, (String) this.content.elementAt(this.content.size() - 1), 100);
                        }
                        this.stateQuest = (byte) 3;
                        GameService.gI().onSendInfoQuest(1, this.idQuest, this.main_sub);
                        this.isShow = false;
                        this.index = (byte) 0;
                        return;
                    }
                    // không phải loại 3, hết lời mà chưa hỏi (nhiệm vụ 1 dòng): hỏi nhận - trước 0.9.4 đóng luôn,
                    // nhiệm vụ đánh quái / nhặt đồ 1 dòng không bao giờ nhận được
                    hoiNhan();
                    return;
                }
                String info = (String) this.content.elementAt(this.index);
                if (info.startsWith("1")) {
                    GameScr.addChat(npc, info.substring(1), 100);
                } else {
                    GameScr.addChat(mychar, info.substring(1), 100);
                }
                if (this.type != 3 && this.index >= this.content.size() - 2 && this.index + 1 < this.content.size()) {
                    this.index = (byte) (this.index + 1);
                    hoiNhan();
                    return;
                }
                return;
            }
            if (this.stateQuest == 1) {
                if (this.index >= this.rescontent.size() - 1) {
                    GameService.gI().onSendInfoQuest(1, this.idQuest, this.main_sub);
                    this.isShow = false;
                    this.stateQuest = (byte) 3;
                    Canvas.endDlg();
                    return;
                }
                String info2 = (String) this.rescontent.elementAt(this.index);
                if (info2.startsWith("1")) {
                    GameScr.addChat(npc, info2.substring(1), 100);
                } else {
                    GameScr.addChat(mychar, info2.substring(1), 100);
                }
                if (this.index >= this.rescontent.size() - 2) {
                    this.index = (byte) (this.index + 1);
                    GameService.gI().onSendInfoQuest(1, this.idQuest, this.main_sub);
                    this.isShow = false;
                    this.stateQuest = (byte) 3;
                    Canvas.endDlg();
                    return;
                }
                return;
            }
            if (this.stateQuest == 2) {
                GameScr.addChat(npc, this.decript, 100);
                this.isShow = false;
                this.index = (byte) 0;
            }
        }
    }

    public void startTalk(Actor mychar, Actor npc, int state) {
        this.index = (byte) 0;
        if (state == 0 && this.content.size() == 0 || state == 1 && this.rescontent.size() == 0) {
            this.isShow = false;       // server gửi nhiệm vụ không có lời thoại: không mở hội thoại rỗng
            return;
        }
        String info = "";
        if (state == 0) {
            info = (String) this.content.elementAt(this.index);
        } else if (state == 2) {
            String info2 = this.decript;
            GameScr.addChat(npc, info2, 100);
            return;
        } else if (state == 1) {
            info = (String) this.rescontent.elementAt(this.index);
        }
        this.isShow = true;   // 0.9.4: bật trước addChat - lời của người chơi cũng vào hộp đáy (GameScr.dangTroChuyen)
        if (info.startsWith("1")) {
            GameScr.addChat(npc, info.substring(1), 100);
        } else {
            GameScr.addChat(mychar, info.substring(1), 100);
        }
        if (this.stateQuest == 0 && this.type != 3 && this.content.size() <= 2) {
            this.index = (byte) (this.content.size() - 1);
            hoiNhan();
        } else if (this.stateQuest == 1 && this.type != 3 && this.rescontent.size() <= 2) {
            this.index = (byte) (this.index + 1);
            GameService.gI().onSendInfoQuest(1, this.idQuest, this.main_sub);
            this.isShow = false;
            this.stateQuest = (byte) 3;
            Canvas.endDlg();
        }
    }

    public String getItemQuest(int type) {
        if (this.type == 2) {
            for (int i = 0; i < this.idItemGet.length; i++) {
                if (this.idItemGet[i] == type && this.allItemGet[i] < this.totalItemGet[i]) {
                    short[] sArr = this.allItemGet;
                    int i2 = i;
                    sArr[i2] = (short) (sArr[i2] + 1);
                    return this.allItemGet[i] < this.totalItemGet[i] ? new StringBuffer("Nhặt được ").append((int) this.allItemGet[i]).append("/").append((int) this.totalItemGet[i]).append(" ").append(ItemQuest.NAME_ITEM[type]).toString() : new StringBuffer("Nhiệm vụ ").append(this.name).append(" đã hoàn thành").toString();
                }
            }
            return "";
        }
        return "";
    }

    public String killMonster(int idMonstemplate, String nameMons) {
        if (this.type == 0) {
            for (int i = 0; i < this.idMonsKill.length; i++) {
                if (this.idMonsKill[i] == idMonstemplate && this.allMonsKilled[i] < this.totalMonskill[i]) {
                    short[] sArr = this.allMonsKilled;
                    int i2 = i;
                    sArr[i2] = (short) (sArr[i2] + 1);
                    return this.allMonsKilled[i] < this.totalMonskill[i] ? new StringBuffer("Hạ được ").append((int) this.allMonsKilled[i]).append("/").append((int) this.totalMonskill[i]).append(" ").append(nameMons).toString() : new StringBuffer("Nhiệm vụ ").append(this.name).append(" đã hoàn thành").toString();
                }
            }
            return "";
        }
        return "";
    }

    public String killMonsterByLv(int lv) {
        if (this.type == 4 && lv <= this.deltaLv) {
            for (int i = 0; i < this.allMonsKilled.length; i++) {
                if (this.allMonsKilled[i] < this.totalMonskill[i]) {
                    short[] sArr = this.allMonsKilled;
                    int i2 = i;
                    sArr[i2] = (short) (sArr[i2] + 1);
                    return this.allMonsKilled[i] < this.totalMonskill[i] ? new StringBuffer("Hạ được ").append((int) this.allMonsKilled[i]).append("/").append((int) this.totalMonskill[i]).append(" ").toString() : new StringBuffer("Nhiệm vụ ").append(this.name).append(" đã hoàn thành").toString();
                }
            }
            return "";
        }
        return "";
    }

    public String getInfoPaintScr() {
        String info = "";
        if (this.stateQuest == 1) {
            info = new StringBuffer(String.valueOf(this.decript)).append("|").append(this.infoGift).toString();
        } else if (this.stateQuest == 2) {
            switch (this.type) {
                case 0:
                    String info2 = new StringBuffer(String.valueOf(Res.monsterTemplates[this.idMonsKill[0]].name)).append(": ").append((int) this.allMonsKilled[0]).append("/").append((int) this.totalMonskill[0]).toString();
                    for (int i = 1; i < this.idMonsKill.length; i++) {
                        info2 = new StringBuffer(String.valueOf(info2)).append("|").append(Res.monsterTemplates[this.idMonsKill[i]]).append(": ").append((int) this.allMonsKilled[i]).append("/").append((int) this.totalMonskill[i]).toString();
                    }
                    info = new StringBuffer(String.valueOf(info2)).append("|").append(this.infoGift).toString();
                    break;
                case 2:
                    String info3 = new StringBuffer(String.valueOf(ItemQuest.NAME_ITEM[this.idItemGet[0]])).append(": ").append((int) this.allItemGet[0]).append("/").append((int) this.totalItemGet[0]).toString();
                    for (int i2 = 1; i2 < this.idItemGet.length; i2++) {
                        info3 = new StringBuffer(String.valueOf(info3)).append("|").append(ItemQuest.NAME_ITEM[this.idItemGet[i2]]).append(": ").append((int) this.allItemGet[i2]).append("/").append((int) this.totalItemGet[i2]).toString();
                    }
                    info = new StringBuffer(String.valueOf(info3)).append("|").append(this.infoGift).toString();
                    break;
                case 4:
                    info = new StringBuffer(String.valueOf(new StringBuffer("Hạ: ").append((int) this.allMonsKilled[0]).append("/").append((int) this.totalMonskill[0]).toString())).append("|").append(this.infoGift).toString();
                    break;
            }
        }
        return info;
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
