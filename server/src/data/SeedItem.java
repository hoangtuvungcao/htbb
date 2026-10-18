/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package data;

import real.Char;
import real.Map;
import real.MessageCreator;

public class SeedItem {
    public int idDB = 0;
    public int ownerID;
    public byte idFarm;
    public byte lvTree;
    public byte idTemplate = -1;
    public byte buy = 0;
    public byte lvPlot = 1;
    public byte rankTree = 0;
    public long timeLive;
    static byte seconds = 60;

    public SeedItem(int idFrame, int idSeedTemplate, byte buy) {
        this.idFarm = (byte)idFrame;
        this.idTemplate = (byte)idSeedTemplate;
        this.buy = buy;
    }

    public byte getIdImgLevel() {
        return this.idTemplate > -1 ? Map.seedsTemplate[this.idTemplate].idImageTree[this.lvTree] : 0;
    }

    public byte getDelYImgLv() {
        return this.idTemplate > -1 ? Map.seedsTemplate[this.idTemplate].deltaY[this.lvTree] : 0;
    }

    public String getNameTree() {
        return "ô đất "
            + (this.idFarm + 1)
            + " cấp độ "
            + this.lvPlot
            + " "
            + (this.idTemplate > -1 ? "đang trồng cây " + Map.seedsTemplate[this.idTemplate].name : "");
    }

    public SeedTemplate getTemplate() {
        return Map.seedsTemplate[this.idFarm];
    }

    public SeedTemplate getTemplateTree() {
        return Map.seedsTemplate[this.idTemplate];
    }

    public int getTotalTimeLive() {
        if (this.idTemplate > -1) {
            int total = Map.seedsTemplate[this.idTemplate].timeLive[Map.seedsTemplate[this.idTemplate].timeLive.length - 1];
            int second = total * seconds - this.getTimeLive();
            return second > 0 ? second : 0;
        } else {
            return 0;
        }
    }

    public int getTimeLive() {
        return this.idTemplate > -1 ? (int)((System.currentTimeMillis() - this.timeLive) / 1000L) : 0;
    }

    public void setLevelByTime() {
        if (this.idTemplate > -1) {
            int time = this.getTimeLive();
            short[] timeLive = Map.seedsTemplate[this.idTemplate].timeLive;

            for (byte i = (byte)(timeLive.length - 1); i >= 0; i--) {
                if (time > timeLive[i] * seconds) {
                    this.lvTree = (byte)(i + 1);
                    break;
                }
            }
        }
    }

    public String upLevelPlot(Char p) {
        if (this.buy == 0) {
            return "Không thể nâng cấp";
        } else if (this.idTemplate > -1 && !this.isDie()) {
            return "Không thể nâng cấp khi đang trồng cây";
        } else if (p.lvHouse >= this.lvPlot + 1 && p.lvStore >= this.lvPlot + 1) {
            short[] leaf = SeedTemplate.LEAF_UP_LEVEL_PLOT[this.lvPlot - 1];
            if (p.potions[104] < leaf[0] || p.potions[103] < leaf[1] || p.potions[105] < leaf[2]) {
                return "Bạn phải cần " + leaf[0] + " lá vàng, " + leaf[1] + " lá xanh và " + leaf[2] + " lá đỏ";
            } else if (this.lvPlot < 6) {
                this.lvPlot++;
                p.potions[104] = p.potions[104] - leaf[0];
                p.potions[103] = p.potions[103] - leaf[1];
                p.potions[105] = p.potions[105] - leaf[2];
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                p.map.doSendFarm(p);
                Database.instance.saveOrtherLog("tob_log_farm", p.getName(), this.idFarm + 1 + " lvPlot= " + this.lvPlot, "ulvp");
                return "Nâng cấp thành công. Cấp độ hiện tại của ô đất là " + this.lvPlot;
            } else {
                return "Cấp độ cao nhất của ô đất là 6.";
            }
        } else {
            return "Nhà chính và kho phải đạt cấp độ " + (this.lvPlot + 1) + " trở lên";
        }
    }

    public boolean isDie() {
        return this.idTemplate > -1 && this.lvTree > Map.seedsTemplate[this.idTemplate].maxLv;
    }

    public boolean checkAvaliable() {
        return this.idTemplate == -1
            ? true
            : this.getTimeLive() >= Map.seedsTemplate[this.idTemplate].timeLive[Map.seedsTemplate[this.idTemplate].timeLive.length - 2] * seconds;
    }

    public String doHavest(Char p) {
        if (this.buy == 0) {
            return "Bạn chưa mua ô đất này";
        } else if (this.isDie() || this.idTemplate == -1) {
            return "Cây đã chết hoặc ô đất chưa có trồng cây";
        } else if (this.lvTree < Map.seedsTemplate[this.idTemplate].maxLv - 1) {
            return "Cây chưa thể thu hoạch";
        } else if (this.lvTree >= Map.seedsTemplate[this.idTemplate].maxLv) {
            return "Không thể thu hoạch do cây đã chết";
        } else {
            byte[] xValue = new byte[]{1, 4, 3, 2, 5};
            String[] mau = new String[]{"trang", "Xanh la", "do", "xanh duong", "tim"};
            String infoReturn = "Đã thu hoạch "
                + this.getTemplateTree().name
                + ". Sản lượng thu được là "
                + this.getTemplateTree().value * xValue[this.rankTree]
                + " sản phẩm";
            switch (this.idTemplate) {
                case 0:
                case 3:
                case 6:
                    p.potions[103] = p.potions[103] + Map.seedsTemplate[this.idTemplate].value * xValue[this.rankTree];
                    p.sendMessage(
                        MessageCreator.createServerAlertMessage("Bạn thu được " + Map.seedsTemplate[this.idTemplate].value * xValue[this.rankTree] + " lá xanh", "")
                    );
                    Database.instance
                        .saveOrtherLog(
                            "tob_log_farm",
                            p.getName(),
                            this.idFarm
                                + 1
                                + " thu hoach "
                                + this.getTemplateTree().name
                                + "_"
                                + mau[this.rankTree]
                                + " sản lượng "
                                + this.getTemplateTree().value * xValue[this.rankTree]
                                + " lá xanh",
                            "havest"
                        );
                    p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                    infoReturn = "Bạn thu được " + this.getTemplateTree().value * xValue[this.rankTree] + " lá xanh";
                    break;
                case 1:
                case 4:
                case 7:
                    p.potions[105] = p.potions[105] + this.getTemplateTree().value * xValue[this.rankTree];
                    p.sendMessage(MessageCreator.createServerAlertMessage("Bạn thu được " + this.getTemplateTree().value * xValue[this.rankTree] + " lá đỏ", ""));
                    Database.instance
                        .saveOrtherLog(
                            "tob_log_farm",
                            p.getName(),
                            this.idFarm
                                + 1
                                + " thu hoach "
                                + this.getTemplateTree().name
                                + "_"
                                + mau[this.rankTree]
                                + " sản lượng "
                                + this.getTemplateTree().value * xValue[this.rankTree]
                                + " lá đỏ",
                            "havest"
                        );
                    p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                    infoReturn = "Bạn thu được " + this.getTemplateTree().value * xValue[this.rankTree] + " lá đỏ";
                    break;
                case 2:
                case 5:
                case 8:
                    p.potions[104] = p.potions[104] + this.getTemplateTree().value * xValue[this.rankTree];
                    p.sendMessage(MessageCreator.createServerAlertMessage("Bạn thu được " + this.getTemplateTree().value * xValue[this.rankTree] + " lá vàng", ""));
                    Database.instance
                        .saveOrtherLog(
                            "tob_log_farm",
                            p.getName(),
                            this.idFarm
                                + 1
                                + " thu hoach "
                                + this.getTemplateTree().name
                                + "_"
                                + mau[this.rankTree]
                                + " sản lượng "
                                + this.getTemplateTree().value * xValue[this.rankTree]
                                + " lá vàng",
                            "havest"
                        );
                    p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                    infoReturn = "Bạn thu được " + this.getTemplateTree().value * xValue[this.rankTree] + " lá vàng";
                    break;
                case 9:
                case 10:
                case 11:
                case 12:
                case 13:
                case 14:
                    try {
                        Map.addXPForChar(p, this.getTemplateTree().value * xValue[this.rankTree], false, "seeditem");
                        Database.instance
                            .saveOrtherLog(
                                "tob_log_farm",
                                p.getName(),
                                this.idFarm
                                    + 1
                                    + " thu hoach "
                                    + this.getTemplateTree().name
                                    + "_"
                                    + mau[this.rankTree]
                                    + " sản lượng "
                                    + this.getTemplateTree().value * xValue[this.rankTree]
                                    + " XP",
                                "hxp"
                            );
                        infoReturn = "Bạn nhận được " + this.getTemplateTree().value * xValue[this.rankTree] + " exp";
                    } catch (Exception var6) {
                    }
                case 15:
                case 16:
                case 17:
                case 18:
            }

            this.idTemplate = -1;
            this.lvTree = 0;
            this.timeLive = 0L;
            p.map.doSendFarm(p);
            return infoReturn;
        }
    }

    public void delTree() {
        this.reset();
    }

    public void reset() {
        this.idTemplate = -1;
        this.lvTree = 0;
        this.timeLive = 0L;
    }
}
