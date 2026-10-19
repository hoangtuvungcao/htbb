/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real;

import data.Database;
import util.Logger;

public class NpcReceiveCardLienDau extends NpcReceiveCard {
    public int idClan = -1;
    public byte posNpc = 0;
    public Char charGive = null;
    public String nameClan = "";
    public short time = 0;
    public static byte[] x = new byte[]{40, 18, 61};
    public static byte[] y = new byte[]{15, 45, 45};
    public static String[] npc = new String[]{"Trần thống lĩnh", "Tả thống lĩnh", "Hữu thống lĩnh"};
    public byte inCountry = 0;
    static short timeGiveCard = 60;
    public static boolean[] isFinish = new boolean[3];
    public static String[] nameCountry = new String[]{"Thanh long", "Hắc hổ", "Huyền vũ"};

    public NpcReceiveCardLienDau(int pos, int country) {
        super(pos, country);
        this.posNpc = (byte)pos;
        this.inCountry = (byte)country;
    }

    @Override
    public boolean giveCard(Char p, boolean isClanTown) {
        int posNpc = this.checkInRange(p.x / 16, p.y / 16);
        if (posNpc == -1) {
            return false;
        } else if (this.charGive != null) {
            return false;
        } else if (this.idClan == p.myCountry) {
            return false;
        } else {
            this.time = timeGiveCard;
            this.charGive = p;
            p.timeGiveCardTown = System.currentTimeMillis();
            Database.instance
                .saveOrtherLog(
                    "",
                    p.charname,
                    p.myCountry + " x= " + p.x / 16 + " y= " + p.y / 16 + " mid= " + p.mapID + " posnpc= " + this.posNpc + " " + Char.getDayTime(0L),
                    "stGive"
                );
            return true;
        }
    }

    private int checkInRange(int xChar, int yChar) {
        return xChar >= x[this.posNpc] - 3 && xChar <= x[this.posNpc] + 3 && yChar >= y[this.posNpc] - 3 && yChar <= y[this.posNpc] + 3 ? 1 : -1;
    }

    @Override
    public void update() {
        try {
            if (this.charGive != null && this.charGive.hp > 0 && this.charGive.timeGiveCardTown > 0L) {
                if (System.currentTimeMillis() - this.charGive.timeGiveCardTown > NpcReceiveCard.timeGiveCard * 1000) {
                    this.idClan = this.charGive.myCountry;
                    Map mapok = this.charGive.map;
                    this.nameClan = nameCountry[this.idClan];
                    this.charGive
                        .map
                        .sendAllPlayer(
                            MessageCreator.createServerAlertAutoOffMessage("Lãnh thổ " + nameCountry[this.idClan] + " đã giao thẻ thành công tại " + npc[this.posNpc]),
                            this.inCountry
                        );
                    String charname = this.charGive.charname;
                    this.charGive.timeGiveCardTown = 0L;

                    try {
                        Database.instance
                            .saveLogClan(charname + "_" + nameCountry[this.idClan], "gcok", "Giao the ok tai " + npc[this.posNpc] + " " + Char.getDayTime(0L));
                        this.charGive.map.sendAllPlayer(((MapLienDau)this.charGive.map).createMsgStartGetTown(this.inCountry), this.inCountry);
                    } catch (Exception var8) {
                    }

                    NpcReceiveCardLienDau npc1 = ((MapLienDau)this.charGive.map).npcReceiveCard.get(0);
                    NpcReceiveCardLienDau npc2 = ((MapLienDau)this.charGive.map).npcReceiveCard.get(1);
                    NpcReceiveCardLienDau npc3 = ((MapLienDau)this.charGive.map).npcReceiveCard.get(2);
                    if (npc1.idClan == npc2.idClan && npc1.idClan == npc3.idClan && npc1.idClan != -1) {
                        this.charGive.map.curday[this.inCountry] = "";
                        this.charGive.timeGiveCardTown = 0L;
                        Map.sendAllCharServer(
                            -1, MessageCreator.createServerAlertAutoOffMessage("Chúc mừng lãnh thổ " + nameCountry[this.charGive.myCountry] + " đã chiếm được thành.")
                        );

                        try {
                            Logger.logStringWithDate(Map.getNameMap(this.charGive.map.mapId) + " " + nameCountry[this.charGive.myCountry], "ketqualiendau.txt");
                        } catch (Exception var7) {
                        }

                        MapLienDau.NAME_WIN[this.charGive.map.mapId - 30] = nameCountry[this.charGive.myCountry];
                        if (npc1.charGive != null) {
                            npc1.charGive.timeGiveCardTown = 0L;
                        }

                        if (npc2.charGive != null) {
                            npc2.charGive.timeGiveCardTown = 0L;
                        }

                        if (npc3.charGive != null) {
                            npc3.charGive.timeGiveCardTown = 0L;
                        }

                        ((MapLienDau)this.charGive.map).isStart = false;
                        npc1.charGive = null;
                        npc2.charGive = null;
                        npc3.charGive = null;
                        Database.instance.saveLogClan(this.nameClan, "cgt", "đã chiếm thành " + Char.getDayTime(0L));
                    }

                    mapok.sendAllPlayer(((MapLienDau)mapok).createMsgStartGetTown(this.inCountry), this.inCountry);
                    this.time = 0;
                    this.charGive = null;
                } else {
                    this.time = (short)(timeGiveCard - (System.currentTimeMillis() - this.charGive.timeGiveCardTown) / 1000L);
                    if (this.checkInRange(this.charGive.x / 16, this.charGive.y / 16) == -1 || !this.charGive.map.isMapLienDau()) {
                        Database.instance
                            .saveOrtherLog(
                                "",
                                this.charGive.charname,
                                this.charGive.idClan
                                    + " x= "
                                    + this.charGive.x / 16
                                    + " y= "
                                    + this.charGive.y / 16
                                    + " mid= "
                                    + this.charGive.mapID
                                    + " posnpc= "
                                    + this.posNpc
                                    + " > "
                                    + this.charGive.inCountry,
                                "ngoairange"
                            );
                        this.charGive.timeGiveCardTown = 0L;
                        this.charGive
                            .map
                            .sendAllPlayer(
                                MessageCreator.createServerAlertAutoOffMessage("Lãnh thổ " + nameCountry[this.charGive.myCountry] + " giao thẻ thất bại"),
                                this.charGive.myCountry
                            );
                        this.time = 0;
                        this.charGive.map.sendAllPlayer(((MapLienDau)this.charGive.map).createMsgStartGetTown(this.inCountry), this.inCountry);
                        this.charGive = null;
                    }
                }
            } else if (this.charGive != null && (this.charGive.hp <= 0 || !this.charGive.map.isMapLienDau())) {
                this.charGive.timeGiveCardTown = 0L;
                Database.instance
                    .saveOrtherLog(
                        "",
                        this.charGive.charname,
                        this.charGive.idClan
                            + " x= "
                            + this.charGive.x / 16
                            + " y= "
                            + this.charGive.y / 16
                            + " mid= "
                            + this.charGive.mapID
                            + " posnpc= "
                            + this.posNpc
                            + " > "
                            + this.charGive.inCountry,
                        "ngoairange1"
                    );
                this.charGive
                    .map
                    .sendAllPlayer(
                        MessageCreator.createServerAlertAutoOffMessage("Lãnh thổ " + nameCountry[this.charGive.myCountry] + " giao thẻ thất bại"),
                        this.charGive.myCountry
                    );
                this.time = 0;
                this.charGive.map.sendAllPlayer(MessageCreator.createMsgStartGetTown(this.inCountry), this.inCountry);
                this.charGive = null;
            }
        } catch (Exception var9) {
            var9.printStackTrace();
        }
    }

    @Override
    public String getNameCharGive() {
        return this.charGive == null ? "" : this.charGive.charname;
    }

    @Override
    public short getIDCharGive() {
        return this.charGive == null ? 32000 : this.charGive.id;
    }

    @Override
    public String getNameNpc() {
        return npc[this.posNpc];
    }
}
