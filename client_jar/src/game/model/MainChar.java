package game.model;

import game.model.skill.SkillManager;
import game.networklogic.GameService;
import game.render.Canvas;
import game.render.Util;
import game.render.screen.GameScr;
import game.render.screen.RMS;
import game.render.screen.WindowInfoScr;
import java.util.Vector;

public class MainChar extends Char {
    public static QuickSlot[][] quickSlot;
    public boolean isFirstTimeGetItem;
    public boolean isFirstTimeGetMoney;
    public boolean isFirstTimeGetPotion;
    public boolean check;
    public boolean canMove;
    public byte limItemBag;
    public static Vector gemitemImbue = new Vector();
    public static Vector gemItem = new Vector();
    public static Vector gemItemKhoa = new Vector();
    public static Vector shopItem = new Vector();
    public static Potion[] listPotion;
    public static short[] itemQuest;
    public short lastLv;
    public short xBegin;
    public short yBegin;
    public short[] posTransRoad;
    public short day;
    public int dedicationPoint;
    public Quest mainQuest;
    public int pArena;
    public byte idSkill = 0;
    public int idMonster = 0;
    public Vector tItems = new Vector();
    public Vector rItems = new Vector();
    public boolean first = false;
    public int countRoad = 0;
    public String nameHusband_wife = "";
    public boolean stop = false;

    public static void remove() {
        gemitemImbue.removeAllElements();
        gemItem.removeAllElements();
        gemItemKhoa.removeAllElements();
        shopItem.removeAllElements();
        quickSlot = null;
        listPotion = null;
    }

    @Override
    public boolean isMainchar() {
        return true;
    }

    public void loadQuickSlot() {
        if (quickSlot != null) {
            return;
        }
        quickSlot = new QuickSlot[game.pokemon.TrangChieu.TOI_DA][5];   // 0.9.8: tối đa 3 trang (mở trang 3 bằng thẻ / xu)
        for (int t = 0; t < quickSlot.length; t++) {
            for (int i = 0; i < 5; i++) {
                quickSlot[t][i] = new QuickSlot();
            }
        }
        // 0.9.5: bộ 1 = chiêu cơ bản + chiêu đã học (ThanhNhanh tự điền), thuốc có nút HP / PP riêng trên HUD
        quickSlot[0][2].setIsSkill(0, this.clazz, false);
        RMS.loadQuickSlot();
        game.pokemon.ThanhNhanh.dien(quickSlot[0]);
        game.pokemon.TrangChieu.apDung();   // trang chiêu máy chủ giữ (gói -103 có thể tới trước bộ ô)
    }

    public String getPercent() {
        return new StringBuffer(String.valueOf(this.lvpercent / 10)).append(".").append(this.lvpercent % 10).append("%").toString();
    }

    public void add2Party(PartyInfo info) {
        for (int i = 0; i < Char.party.size(); i++) {
            if (((PartyInfo) Char.party.elementAt(i)).name.equals(info.name)) {
                return;
            }
        }
        Char.party.addElement(info);
    }

    public void add2Party(Char info) {
        if (Char.party.contains(info)) {
            return;
        }
        Char.party.addElement(info);
    }

    public String removeUserParty(short iduser) {
        for (int i = 0; i < Char.party.size(); i++) {
            if (((PartyInfo) Char.party.elementAt(i)).id == iduser) {
                String name = ((PartyInfo) Char.party.elementAt(i)).name;
                Char.party.removeElementAt(i);
                return name;
            }
        }
        return "";
    }

    public String removeCharParty(short iduser) {
        for (int i = 0; i < Char.party.size(); i++) {
            if (((Char) Char.party.elementAt(i)).ID == iduser) {
                String name = ((Char) Char.party.elementAt(i)).name;
                Char.party.removeElementAt(i);
                return name;
            }
        }
        return "";
    }

    // ---------------------------------------------------------------- đo / sửa vị trí (bao 0.9.2 đi giật)
    /** Số lệnh 4 máy chủ gửi cho chính mình (= máy chủ sửa vị trí) / số lần lệch lớn phải đặt lại. */
    public static int soSuaViTri, soNhayViTri;
    /** Số khung cập nhật chậm (> 80 ms giữa hai khung) và khung chậm nhất (ms). */
    public static int soKhungCham, khungChamNhat;
    private long lucKhungTruoc;
    /** Đang lướt tới vị trí máy chủ đặt (không gửi vị trí giữa đường - kẻo kéo máy chủ lùi lại). */
    private boolean theoMayChu;
    /** Lệch quá ngưỡng này (px) khi đang đi thì đặt lại ngay; nhỏ hơn thì giữ vị trí client (gói sau tự khớp). */
    static final int LECH_LON = 48;

    /**
     * Lệnh 4 cho chính mình: máy chủ không nhận bước đi (tường, quá nhanh, choáng...) hoặc dời mình (dịch chuyển).
     * Cũ (Char.setPosTo -> moveTo): luôn ĐI NGƯỢC về điểm cũ với tốc độ tăng dần, trong lúc đó lại gửi các vị trí
     * giữa đường -> kéo lùi rồi giật tới (rubber-band). Mới: đang đi mà lệch <= 48 px thì giữ nguyên (máy chủ khớp
     * ở gói sau); lệch lớn thì đặt lại ngay (không đi lùi); đứng yên thì lướt tới điểm máy chủ, không gửi gì.
     */
    @Override
    public void setPosTo(short x, short y) {
        soSuaViTri++;
        int lech = Math.max(Math.abs(x - this.x), Math.abs(y - this.y));
        if (lech == 0) {
            return;
        }
        if (this.state == 1 && lech <= LECH_LON) {
            return;
        }
        if (lech > LECH_LON || this.state == 1) {
            soNhayViTri++;
            this.x = x;
            this.y = y;
            this.xTo = x;
            this.yTo = y;
            this.posTransRoad = null;
            this.countRoad = 0;
            if (this.state == 1) {
                this.state = (byte) 0;
            }
            this.theoMayChu = false;
            return;
        }
        this.theoMayChu = true;
        moveTo(x, y);
    }

    @Override
    public void update() {
        long bay = System.currentTimeMillis();
        if (this.lucKhungTruoc != 0L) {
            int kc = (int) (bay - this.lucKhungTruoc);
            if (kc > 80) {
                soKhungCham++;
                if (kc > khungChamNhat) {
                    khungChamNhat = kc;
                }
            }
        }
        this.lucKhungTruoc = bay;
        super.update();
        if (!this.isFreeze && this.posTransRoad != null) {
            updateWayPointer();
            this.check = false;
        }
        try {
            Canvas.gameScr.capNhatDuoi();
        } catch (Exception e) {
        }
        if (this.state != 1) {
            this.theoMayChu = false;
        } else if (!this.theoMayChu) {
            // đang đi (phím, chạm đất, đuổi quái): gửi VỊ TRÍ THẬT mỗi GameScr.dtmove, không gửi điểm đích
            this.stop = false;
        }
        if (this.theoMayChu) {
            this.stop = true;
        }
        if (!this.stop && System.currentTimeMillis() - GameScr.timeMove >= 0) {
            GameScr.timeMove = System.currentTimeMillis() + ((long) GameScr.dtmove);
            this.stop = true;
            GameService.gI().moveChar(this.x, this.y);
        }
        checkChangemapTouch();
    }

    private void checkChangemapTouch() {
        if (Canvas.instance.hasPointerEvents() && GameScr.iTaskAuto != 2) {
            if (this.canMove && !this.check && this.state == 1) {
                int xx = 0;
                int yy = 0;
                if (this.dir == 2) {
                    xx = -32;
                } else if (this.dir == 3) {
                    xx = 32;
                }
                if (this.dir == 0) {
                    yy = 32;
                } else if (this.dir == 1) {
                    yy = -32;
                }
                if ((Math.abs(this.x - this.xBegin) >= 16 || Math.abs(this.y - this.yBegin) >= 16) && Canvas.gameScr.checkCanChangeMap(this.x + xx, this.y + yy, this.dir)) {
                    this.posTransRoad = null;
                    this.check = true;
                    return;
                }
            }
            if (this.state == 1) {
                this.canMove = true;
            } else {
                this.canMove = false;
            }
        }
    }

    /**
     * Đi theo đường (DuongDi.tim): tới gần điểm hiện tại (<= tốc độ) thì nhận ô kế tiếp. Điểm đường mã hoá
     * DuongDi.ma (ô tuyệt đối, đúng cả map > 127 ô). Không gửi điểm đích nữa: update() gửi vị trí thật khi đang đi.
     */
    private void updateWayPointer() {
        int gan = Math.max(3, this.speed + this.upSpeed);
        if (Util.abs(this.x - this.xTo) <= gan && Util.abs(this.y - this.yTo) <= gan) {
            for (int i = (this.posTransRoad.length - 1) - this.countRoad; i >= 0; i--) {
                if (DuongDi.co(this.posTransRoad[i])) {
                    int xx = DuongDi.maX(this.posTransRoad[i]);
                    int yy = DuongDi.maY(this.posTransRoad[i]);
                    int px = (xx * 16) + 8;
                    int py = (yy * 16) + 8;
                    if (GameScr.iTaskAuto != 2 && ((this.dir == 1 || Tilemap.isOfflineMap) && Canvas.gameScr.checkCanChangeMap(px, py, this.dir))) {
                        this.posTransRoad = null;
                        this.countRoad = 0;
                        return;
                    } else {
                        if (Canvas.gameScr.checkMoveLimit(px, py)) {
                            this.posTransRoad = null;
                            return;
                        }
                        moveTo((short) px, (short) py);
                        this.stop = false;
                        this.posTransRoad[i] = 0;
                        this.countRoad++;
                        return;
                    }
                }
                if (i == 0) {
                    this.posTransRoad = null;
                    this.countRoad = 0;
                    return;
                }
            }
        }
    }

    public String[] getInfoSkill(int idSkill) {
        String string;
        String string2;
        String string3;
        String string4;
        String string5;
        String string6;
        String string7;
        String string8;
        String string9;
        String[] info = (String[]) null;
        if (Char.skillLevelLearnt[idSkill] == -1) {
            info = new String[]{"Chưa học chiêu này", new StringBuffer("Cấp yêu cầu: ").append((int) SkillManager.LEVEL_ADD_SKILL[idSkill][1]).toString()};
        } else if (Char.skillLevelLearnt[idSkill] == 0) {
            info = new String[]{"Chưa học chiêu này", getLvRequest(idSkill)};
        } else if (!WindowInfoScr.gI().isBuffSkill()) {
            info = new String[5];
            info[0] = new StringBuffer("Thời gian đánh: ").append(SkillManager.getSkillCooldown((byte) idSkill, this.clazz, Char.skillLevelLearnt[idSkill])).append(" ms").toString();
            info[1] = new StringBuffer(String.valueOf(SkillManager.infoString[this.clazz][idSkill])).append(SkillManager.getSkillDamPercent(idSkill, Char.skillLevelLearnt[idSkill], this.clazz)).append("%").toString();
            info[2] = new StringBuffer("Phạm vi: ").append(SkillManager.getSkillRange((byte) idSkill, this.clazz)).toString();
            info[3] = new StringBuffer("Tốn PP: ").append(SkillManager.getSkillMP(idSkill, Char.skillLevelLearnt[idSkill], this.clazz)).toString();
            if (Char.skillLevelLearnt[idSkill] < 9) {
                string9 = new StringBuffer("Cấp yêu cầu: ").append((int) SkillManager.LEVEL_ADD_SKILL[idSkill][Char.skillLevelLearnt[idSkill]]).toString();
            } else {
                string9 = "";
            }
            info[4] = string9;
        } else {
            byte[][] temp = {new byte[]{1, 5}, new byte[]{1, 2}, new byte[]{3, 4, 5, 6}, new byte[]{0, 1}, new byte[]{0, 3}};
            String mpMat = new StringBuffer("Tốn PP: ").append(SkillManager.getSkillMP(idSkill, Char.skillLevelLearnt[idSkill], this.clazz)).toString();
            switch (this.clazz) {
                case 0:
                    if (idSkill == 4) {
                        info = new String[4];
                        info[0] = "Đánh xuyên giáp";
                        info[1] = new StringBuffer("Tỷ lệ thành công: ").append((int) SkillManager.SKILL_DAM_PERCENT[this.clazz][idSkill][Char.skillLevelLearnt[idSkill]]).append("%").toString();
                        info[2] = mpMat;
                        if (Char.skillLevelLearnt[idSkill] < 9) {
                            string8 = new StringBuffer("Cấp yêu cầu: ").append((int) SkillManager.LEVEL_ADD_SKILL[idSkill][Char.skillLevelLearnt[idSkill]]).toString();
                        } else {
                            string8 = "";
                        }
                        info[3] = string8;
                    } else if (idSkill == 5) {
                        info = new String[6];
                        info[0] = "Gây tác hại lại 1 phần";
                        info[1] = "sức công cho đối thủ";
                        info[2] = new StringBuffer("Thời gian: ").append((int) SkillManager.TIME_LIFE_BUFF_SKILL[temp[this.clazz][idSkill - 4]][Char.skillLevelLearnt[idSkill]]).append("s").toString();
                        info[3] = new StringBuffer("Tỷ lệ: ").append((int) SkillManager.SKILL_DAM_PERCENT[this.clazz][idSkill][Char.skillLevelLearnt[idSkill]]).append("%").toString();
                        info[4] = mpMat;
                        if (Char.skillLevelLearnt[idSkill] < 9) {
                            string7 = new StringBuffer("Cấp yêu cầu: ").append((int) SkillManager.LEVEL_ADD_SKILL[idSkill][Char.skillLevelLearnt[idSkill]]).toString();
                        } else {
                            string7 = "";
                        }
                        info[5] = string7;
                    }
                    break;
                case 1:
                    if (idSkill == 5) {
                        info = new String[4];
                        info[0] = "Tăng sức tấn công";
                        info[1] = "của bản thân";
                        info[2] = new StringBuffer("Tỷ lệ tăng: ").append(SkillManager.getSkillDamPercent(idSkill, Char.skillLevelLearnt[idSkill], this.clazz)).append("%").toString();
                        if (Char.skillLevelLearnt[idSkill] < 9) {
                            string6 = new StringBuffer("Cấp yêu cầu: ").append((int) SkillManager.LEVEL_ADD_SKILL[idSkill][Char.skillLevelLearnt[idSkill]]).toString();
                        } else {
                            string6 = "";
                        }
                        info[3] = string6;
                    } else if (idSkill == 4) {
                        info = new String[4];
                        info[0] = new StringBuffer("Phòng thủ tăng: ").append(SkillManager.getSkillDamPercent(idSkill, Char.skillLevelLearnt[idSkill], this.clazz)).append("%").toString();
                        info[1] = new StringBuffer("Thời gian: ").append((int) SkillManager.TIME_LIFE_BUFF_SKILL[temp[this.clazz][idSkill - 4]][Char.skillLevelLearnt[idSkill]]).append("s").toString();
                        info[2] = mpMat;
                        if (Char.skillLevelLearnt[idSkill] < 9) {
                            string5 = new StringBuffer("Cấp yêu cầu: ").append((int) SkillManager.LEVEL_ADD_SKILL[idSkill][Char.skillLevelLearnt[idSkill]]).toString();
                        } else {
                            string5 = "";
                        }
                        info[3] = string5;
                    }
                    break;
                case 2:
                    if (idSkill == 5) {
                        info = new String[4];
                        info[0] = "Tăng tinh thần";
                        info[1] = "của bản thân";
                        info[2] = new StringBuffer("Tỷ lệ tăng: ").append(SkillManager.getSkillDamPercent(idSkill, Char.skillLevelLearnt[idSkill], this.clazz)).append("%").toString();
                        if (Char.skillLevelLearnt[idSkill] < 9) {
                            string4 = new StringBuffer("Cấp yêu cầu: ").append((int) SkillManager.LEVEL_ADD_SKILL[idSkill][Char.skillLevelLearnt[idSkill]]).toString();
                        } else {
                            string4 = "";
                        }
                        info[3] = string4;
                    } else if (idSkill == 4) {
                        info = new String[4];
                        info[0] = new StringBuffer("HP + PP tăng: ").append(SkillManager.getSkillDamPercent(idSkill, Char.skillLevelLearnt[idSkill], this.clazz)).append(" %").toString();
                        info[1] = new StringBuffer("Thời gian: ").append((int) SkillManager.TIME_LIFE_BUFF_SKILL[temp[this.clazz][idSkill - 4]][Char.skillLevelLearnt[idSkill]]).append("s").toString();
                        info[2] = mpMat;
                        if (Char.skillLevelLearnt[idSkill] < 9) {
                            string3 = new StringBuffer("Cấp yêu cầu: ").append((int) SkillManager.LEVEL_ADD_SKILL[idSkill][Char.skillLevelLearnt[idSkill]]).toString();
                        } else {
                            string3 = "";
                        }
                        info[3] = string3;
                    } else if (idSkill == 6) {
                        info = new String[4];
                        info[0] = "Hồi sinh";
                        info[1] = new StringBuffer("HP hồi phục: ").append(SkillManager.getSkillDamPercent(idSkill, Char.skillLevelLearnt[idSkill], this.clazz)).append("%").toString();
                        info[2] = mpMat;
                        if (Char.skillLevelLearnt[idSkill] < 9) {
                            string2 = new StringBuffer("Cấp yêu cầu: ").append((int) SkillManager.LEVEL_ADD_SKILL[idSkill][Char.skillLevelLearnt[idSkill]]).toString();
                        } else {
                            string2 = "";
                        }
                        info[3] = string2;
                    } else if (idSkill == 7) {
                        info = new String[7];
                        info[0] = "Khiên PP";
                        info[1] = "Chuyển 1 lượng hao tổn";
                        info[2] = "HP sang PP";
                        info[3] = new StringBuffer("Tỷ lệ chuyển: ").append(SkillManager.getSkillDamPercent(idSkill, Char.skillLevelLearnt[idSkill], this.clazz)).append("%").toString();
                        info[4] = new StringBuffer("Thời gian: ").append((int) SkillManager.TIME_LIFE_BUFF_SKILL[temp[this.clazz][idSkill - 4]][Char.skillLevelLearnt[idSkill]]).append("s").toString();
                        info[5] = mpMat;
                        if (Char.skillLevelLearnt[idSkill] < 9) {
                            string = new StringBuffer("Cấp yêu cầu: ").append((int) SkillManager.LEVEL_ADD_SKILL[idSkill][Char.skillLevelLearnt[idSkill]]).toString();
                        } else {
                            string = "";
                        }
                        info[6] = string;
                    }
                    break;
                case 3:
                    if (idSkill == 5) {
                        info = new String[]{"Tăng sức phòng thủ", "của bản thân", new StringBuffer("Tỷ lệ tăng: ").append(SkillManager.getSkillDamPercent(idSkill, Char.skillLevelLearnt[idSkill], this.clazz)).append("%").toString(), getLvRequest(idSkill)};
                    } else if (idSkill == 4) {
                        info = new String[]{"Gây choáng cho", "đối phương", "Thời gian: 5s", new StringBuffer("Tỷ lệ gây choáng: ").append(SkillManager.getSkillDamPercent(idSkill, Char.skillLevelLearnt[idSkill], this.clazz)).append("%").toString(), mpMat, getLvRequest(idSkill)};
                    }
                    break;
                case 4:
                    if (idSkill == 5) {
                        info = new String[]{"Tăng độc tính sử dụng", new StringBuffer("Tỷ lệ tăng: ").append(SkillManager.getSkillDamPercent(idSkill, Char.skillLevelLearnt[idSkill], this.clazz)).append("%").toString(), getLvRequest(idSkill)};
                    } else if (idSkill == 4) {
                        info = new String[]{"Tẩm độc vào tên: ", new StringBuffer("Độc tính: ").append(SkillManager.getSkillDamPercent(idSkill, Char.skillLevelLearnt[idSkill], this.clazz)).toString(), new StringBuffer("Thời gian: ").append(SkillManager.getSkillDamPercent(idSkill, Char.skillLevelLearnt[idSkill], this.clazz)).append("s").toString(), mpMat, getLvRequest(idSkill)};
                    }
                    break;
            }
        }
        return info;
    }

    public String getLvRequest(int idSkill) {
        return new StringBuffer("Cấp yêu cầu: ").append((int) SkillManager.LEVEL_ADD_SKILL[idSkill][Char.skillLevelLearnt[idSkill]]).toString();
    }

    public boolean setWay(int vX, int vY) {
        short xx = this.x;
        if (vX != 0) {
            if (!Tilemap.tileTypeAtPixel(this.x + vX, this.y - 16, 2) && !Tilemap.tileTypeAtPixel(this.x, this.y - 16, 2)) {
                moveTo(xx, (short) (this.y - 16));
                return true;
            }
            if (!Tilemap.tileTypeAtPixel(this.x + vX, this.y + 16, 2) && !Tilemap.tileTypeAtPixel(this.x, this.y + 16, 2)) {
                moveTo(xx, (short) (this.y + 16));
                return true;
            }
            return false;
        }
        if (vY != 0) {
            if (!Tilemap.tileTypeAtPixel(this.x - 16, this.y + vY, 2) && !Tilemap.tileTypeAtPixel(this.x - 16, this.y, 2)) {
                moveTo((short) (this.x - 16), this.y);
                return true;
            }
            if (!Tilemap.tileTypeAtPixel(this.x + 16, this.y + vY, 2) && !Tilemap.tileTypeAtPixel(this.x + 16, this.y, 2)) {
                moveTo((short) (this.x + 16), this.y);
                return true;
            }
            return false;
        }
        return false;
    }
}
