package real;


import data.Database;
import io.Message;
import io.ThreadPool;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;
import server.TeamServer;
import util.Logger;

/**
 * Than cac method vong lap cap nhat cua Map: cap nhat dinh ky, gio, cat,
 * gui thong tin quang cao, lang cat.
 *
 * Tach tu real/Map.java. Cac method VAN CON trong Map, giu NGUYEN chu ky cu,
 * chi than chuyen sang day (OfflineMap override update - khong bi anh huong).
 * Tham so "Map map" thay cho "this" truoc day.
 */
class MapVongLap {

    // Vong lap "RUN MAP" (updateSand) chay MAI cho MOI map. Neu co loi, no
    // lap lai moi Map.DELAY_UPDATE_MAP (300ms) - in day du stack trace moi
    // lan se ngap log ngay lap tuc. Chi bao 1 lan cho ca server, giong
    // GhiSoOnline.baoLoiMotLan (va real.Map.daBaoLoiRunMapWind/Update).
    private static boolean daBaoLoiRunMapSand = false;

    static void startSendInfoAdv() {
        Map.loadRuongMaQuai();
        Map.loadThungGo();
        new Thread(() -> {
            boolean isResetChienTruongMoba = false;

            while (!AdminHandler.isStopServer) {
                if (!TeamServer.isServerIndo() && Map.openLog) {
                    if (Char.infoAdv.size() > 0) {
                        data.AdvInfo av = Char.infoAdv.get(data.Logdata.idAdv);
                        Message m = MessageCreator.createMsgChat(((Char) Map.npcVilage.get(0)).id, (String) av.info.get(Map.indexInfoAdv));
                        Map.indexInfoAdv = (byte) ((Map.indexInfoAdv + 1) % av.info.size());
                        Map mapGoc = (Map) RealController.mapList.get(0);
                        mapGoc.sendAllCharMap(m, 1, 0);
                        mapGoc = (Map) RealController.mapList.get(301);
                        mapGoc.sendAllCharMap(m, 1, 0);
                        mapGoc = (Map) RealController.mapList.get(70);
                        mapGoc.sendAllCharMap(m, 0, 0);
                        mapGoc = (Map) RealController.mapList.get(1701);
                        mapGoc.sendAllCharMap(m, 0, 0);
                        mapGoc = (Map) RealController.mapList.get(80);
                        mapGoc.sendAllCharMap(m, 2, 0);
                        mapGoc = (Map) RealController.mapList.get(1901);
                        mapGoc.sendAllCharMap(m, 2, 0);
                    }

                    if (Char.infoAdv2.size() > 0) {
                        data.AdvInfo av = Char.infoAdv2.get(data.Logdata.idAdv2);
                        Message m = MessageCreator.createMsgChat(((Char) Map.npcVilage.get(1)).id, (String) av.info.get(Map.indexInfoAdv2));
                        Map.indexInfoAdv2 = (byte) ((Map.indexInfoAdv2 + 1) % av.info.size());
                        Map map = (Map) RealController.mapList.get(0);
                        map.sendAllCharMap(m, 1, 0);
                        map = (Map) RealController.mapList.get(301);
                        map.sendAllCharMap(m, 1, 0);
                        map = (Map) RealController.mapList.get(70);
                        map.sendAllCharMap(m, 0, 0);
                        map = (Map) RealController.mapList.get(1701);
                        map.sendAllCharMap(m, 0, 0);
                        map = (Map) RealController.mapList.get(80);
                        map.sendAllCharMap(m, 2, 0);
                        map = (Map) RealController.mapList.get(1901);
                        map.sendAllCharMap(m, 2, 0);
                    }
                }

                if (Char.isSuKienTet2017()) {
                    if ((UtilHTBB.getHour() == Map.hourGiveLixi || UtilHTBB.getHour() == Map.hourGiveLixi - 1 || TeamServer.isServerLocal()) && Map.MAX_LI_XI > 0) {
                        Message m = MessageCreator.createMsgChat(
                                ((Char) Map.npcVilage.get(3)).id,
                                "Máy chủ sẽ phát bao lì xì tại cây lì xì bắt đầu từ " + Map.hourGiveLixi + "h " + Map.minuteGiveLixi + " phút"
                        );
                        Map map = (Map) RealController.mapList.get(0);
                        map.sendAllCharMap(m, 1, 0);
                        map = (Map) RealController.mapList.get(301);
                        map.sendAllCharMap(m, 1, 0);
                        map = (Map) RealController.mapList.get(70);
                        map.sendAllCharMap(m, 0, 0);
                        map = (Map) RealController.mapList.get(1701);
                        map.sendAllCharMap(m, 0, 0);
                    }

                    if (UtilHTBB.getHour() > 0 && UtilHTBB.getHour() < 6 && (Map.MAX_LI_XI < 100 || Map.hourGiveLixi != 18)) {
                        Map.MAX_LI_XI = 100;
                        Map.hourGiveLixi = 18;
                        Map.minuteGiveLixi = 30;
                        Database.instance.updateInfoLixiEvent();
                    }
                }

                if (UtilHTBB.getDayOfMonth() == Char.dayEndVip && UtilHTBB.getHour() == Char.timeEndVip && UtilHTBB.getMinute() >= Char.minuteEnd) {
                    Database.instance.doresetCharVip();
                    Map.ALL_CHAR_VIP.clear();
                    Map.ALL_CHAR_BUY_RUONG.clear();
                    Database.instance.check();
                }

                if (MapChienTruongMoba.isDayChienTruong()) {
                    String info = "Chiến trường liên vùng sẽ mở vào 21h dành cho Pokémon có cấp độ dưới 70. Nhanh chân đến NPC Đấu Trường đăng ký trước 21h";
                    if (UtilHTBB.isSunday()) {
                        info = "Chiến trường liên vùng sẽ mở vào 21h dành cho Pokémon có cấp độ từ 70 trở lên. Nhanh chân đến NPC Đấu Trường đăng ký trước 21h";
                    }

                    Message m = MessageCreator.createMsgChat(((Char) Map.npcVilage.get(3)).id, info);
                    Map map = (Map) RealController.mapList.get(0);
                    map.sendAllCharMap(m, 1, 0);
                    map = (Map) RealController.mapList.get(301);
                    map.sendAllCharMap(m, 1, 0);
                    map = (Map) RealController.mapList.get(70);
                    map.sendAllCharMap(m, 0, 0);
                    map = (Map) RealController.mapList.get(1701);
                    map.sendAllCharMap(m, 0, 0);
                } else if (!isResetChienTruongMoba && (UtilHTBB.isTuesday() || UtilHTBB.isSaturday() || UtilHTBB.isFriday())) {
                    MapChienTruongMoba.all_char_chien_truong.clear();
                    MapChienTruongMoba.v_all_char_chien_truong.removeAllElements();
                    Database.instance.resetCharRegChienTruongMoba("");
                    Database.instance.resetCharQuaKhuCT("");
                    Database.instance.resetCharQuaTopCT("");
                    System.err.println("XOA CHAR DANG KY CHIEN TRUONG");
                    isResetChienTruongMoba = true;
                }

                if (Char.isSuKienNoel() || Char.isSuKienMini()) {
                    if (Map.ruong_ma_quai_hac_ho.isDead) {
                        long time = Map.ruong_ma_quai_hac_ho.getTimeLifeRuong();
                        if (time > 0L) {
                            Message m = MessageCreator.createMsgChat(((Char) Map.npcVilage.get(3)).id, "Rương ma quái sẽ xuất hiện sau " + time + "s");
                            Map map = (Map) RealController.mapList.get(0);
                            map.sendAllCharMap(m, 1, 0);
                            map = (Map) RealController.mapList.get(301);
                            map.sendAllCharMap(m, 1, 0);
                        }
                    }

                    if (Map.ruong_ma_quai_thanh_long.isDead) {
                        long time = Map.ruong_ma_quai_thanh_long.getTimeLifeRuong();
                        if (time > 0L) {
                            Message m = MessageCreator.createMsgChat(((Char) Map.npcVilage.get(3)).id, "Rương ma quái sẽ xuất hiện sau " + time + "s");
                            Map map = (Map) RealController.mapList.get(70);
                            map.sendAllCharMap(m, 0, 0);
                            map = (Map) RealController.mapList.get(1701);
                            map.sendAllCharMap(m, 0, 0);
                        }
                    }

                    if (Map.all_ngoc_rong_hac_ho.size() > 0) {
                        Message m = MessageCreator.createMsgChat(((Char) Map.npcVilage.get(3)).id, "Còn " + Map.all_ngoc_rong_hac_ho.size() + " ngọc chưa nhặt");
                        Map.sendAllCharServer(m, 1);
                    }

                    if (Map.all_ngoc_rong_thanh_long.size() > 0) {
                        Message m = MessageCreator.createMsgChat(((Char) Map.npcVilage.get(3)).id, "Còn " + Map.all_ngoc_rong_thanh_long.size() + " ngọc chưa nhặt");
                        Map.sendAllCharServer(m, 0);
                    }
                }

                if (Char.isSuKien2011l2016() && Map.totalChoiBan > 0) {
                    Map.initTimeSellChoi();
                    Message m = MessageCreator.createMsgChat(((Char) Map.npcVilage.get(9)).id, "Thời gian bán chổi sẽ bắt đầu từ 21h xx phút.");
                    Map map = (Map) RealController.mapList.get(70);
                    map.sendAllCharMap(m, 0, 0);
                    map = (Map) RealController.mapList.get(1701);
                    map.sendAllCharMap(m, 0, 0);
                    map = (Map) RealController.mapList.get(80);
                    map.sendAllCharMap(m, 2, 0);
                    map = (Map) RealController.mapList.get(1901);
                    map.sendAllCharMap(m, 2, 0);
                    map = (Map) RealController.mapList.get(0);
                    map.sendAllCharMap(m, 1, 0);
                    map = (Map) RealController.mapList.get(301);
                    map.sendAllCharMap(m, 1, 0);
                }

                if (MapLoiDai.isRunLoiDai()) {
                    Message m = MessageCreator.createMsgChat(
                            ((Char) Map.npcVilage.get(8)).id, "Sàn đấu sẽ bắt đầu vào " + MapLoiDai.HOUR_LOI_DAI + "h " + MapLoiDai.start_minute + "p"
                    );
                    Map map = (Map) RealController.mapList.get(70);
                    map.sendAllCharMap(m, 0, 0);
                    map = (Map) RealController.mapList.get(1701);
                    map.sendAllCharMap(m, 0, 0);
                    map = (Map) RealController.mapList.get(80);
                    map.sendAllCharMap(m, 2, 0);
                    map = (Map) RealController.mapList.get(1901);
                    map.sendAllCharMap(m, 2, 0);
                    map = (Map) RealController.mapList.get(0);
                    map.sendAllCharMap(m, 1, 0);
                    map = (Map) RealController.mapList.get(301);
                    map.sendAllCharMap(m, 1, 0);
                }

                if (Char.isOpenDauGiaChoiBlackFriday()) {
                    Message m = MessageCreator.createMsgChat(((Char) Map.npcVilage.get(6)).id, "Thời gian đấu giá sẽ bắt đầu từ 10h ngày 27/11 đến hết ngày 27/11 .");
                    Map map = (Map) RealController.mapList.get(70);
                    map.sendAllCharMap(m, 0, 0);
                    map = (Map) RealController.mapList.get(1701);
                    map.sendAllCharMap(m, 0, 0);
                    map = (Map) RealController.mapList.get(80);
                    map.sendAllCharMap(m, 2, 0);
                    map = (Map) RealController.mapList.get(1901);
                    map.sendAllCharMap(m, 2, 0);
                    map = (Map) RealController.mapList.get(0);
                    map.sendAllCharMap(m, 1, 0);
                    map = (Map) RealController.mapList.get(301);
                    map.sendAllCharMap(m, 1, 0);
                } else if (Char.isSuKienBlackFriday()) {
                    Message m = MessageCreator.createMsgChat(
                            ((Char) Map.npcVilage.get(6)).id, "Đồ đấu giá và nhận lại tiền cọc sẽ được mở từ 10h ngày 30/11 đến hết ngày 02/12/"
                    );
                    Map map = (Map) RealController.mapList.get(70);
                    map.sendAllCharMap(m, 0, 0);
                    map = (Map) RealController.mapList.get(1701);
                    map.sendAllCharMap(m, 0, 0);
                    map = (Map) RealController.mapList.get(80);
                    map.sendAllCharMap(m, 2, 0);
                    map = (Map) RealController.mapList.get(1901);
                    map.sendAllCharMap(m, 2, 0);
                    map = (Map) RealController.mapList.get(0);
                    map.sendAllCharMap(m, 1, 0);
                    map = (Map) RealController.mapList.get(301);
                    map.sendAllCharMap(m, 1, 0);
                }

                try {
                    Thread.sleep(30000L);
                } catch (Exception var6) {
                }
            }
        }).start();
    }

    static void update(Map map) {
        if (map.nRegion <= 0) {
            if (map.mapId == 201) {
                Map.event.checkNewDay();
                map.openX2Country();
                Map.timeCanPK();
                if (!Map.newDay.equals(Char.getDayOpen(0L))) {
                    Map.newDay = Char.getDayOpen(0L);
                    Map.totalDotPhao = Map.totalDotPhao - Map.totalDotPhao / 4;
                    Map.x3ExpDotPhao = 0;
                    Map.resetAllShield();
                    Database.instance.saveEvent(Map.event.getInfo());
                    String nt = new Date(System.currentTimeMillis()).toString();
                    Map.sendAllCharServer(-1, MessageCreator.createMsgLogin(null));
                    String[] name = new String[]{"nhantieu.txt", "doitieu.txt", "thetieu.txt", "tratieu.txt", "cuoptieu.txt"};

                    for (int i = 0; i < name.length; i++) {
                        Logger.logStringWithDate(Map.getLogVantieu(i), name[i]);
                    }

                    Logger.logStringWithDate(MapTown.count_all_item_sell_per_day + "_" + ItemSell.ID_ITEM_SELL_AUTO_INCREMENT, "matketinfo.txt");
                }

                boolean endGoldTime = false;
                String info = "";

                for (int i = 0; i < 3; i++) {
                    if (Map.minuteX2Country[i] > 0L && System.currentTimeMillis() - Map.minuteX2Country[i] > 0L) {
                        Map.minuteX2Country[i] = 0L;
                        Map.x2Country[i] = 0;
                        endGoldTime = true;
                        info = info + Map.nameCountry[i] + ",";

                        for (int j = 0; j < CharManager.instance.vChars.size(); j++) {
                            Char p = (Char) CharManager.instance.vChars.get(j);
                            if (p != null && p.myCountry == i) {
                                p.resetGoldTimeCountry();
                            } else if (p == null) {
                                CharManager.instance.vChars.remove(j);
                            }
                        }
                    }
                }

                if (endGoldTime) {
                    Database.instance.saveEvent(Map.event.getInfo());
                    Database.instance.saveOrtherLog("", "adminserver", info, "timeoutN");
                }

                if (Map.weather != null && System.currentTimeMillis() > Map.timeWait) {
                    Map.sendAllCharServer(-1, MessageCreator.createMsgWeather(-1, 0, 0));
                    Map.weather = null;
                    Map.timeWait = System.currentTimeMillis() + 1800000L;
                }

                if (!Map.getTown[1]) {
                    map.checkTimeGetTown(1);
                } else if (Map.getTown[1] && map.gameOver(1)) {
                    Map.getTown[1] = false;

                    for (int ix = 0; ix < CharManager.instance.vChars.size(); ix++) {
                        try {
                            ((Char) CharManager.instance.vChars.elementAt(ix))
                                    .sendMessage(MessageCreator.createMsgStartGetTown(((Char) CharManager.instance.vChars.elementAt(ix)).inCountry));
                            ((Char) CharManager.instance.vChars.elementAt(ix))
                                    .sendMessage(MessageCreator.createServerAlertAutoOffMessage("Hết thời gian chiếm Gym. Chưa có đội nào chiếm được Gym"));
                        } catch (Exception var10) {
                        }
                    }
                }

                if (!map.curday[1].equals("")) {
                    map.gameOver(1);
                }

                if (!Map.nwar[1]) {
                    map.checkTimeStartWar(((RegisterAttack) Map.nationWar.get(1)).dayAttack);
                    ((RegisterAttack) Map.nationWar.get(1)).update(1);
                } else {
                    boolean over = map.timeOutNationWar(1);
                    if (over && Map.nationBeAttack[1] > -1) {
                        ((RegisterAttack) Map.nationWar.get(1)).reset();
                        Map.nwar[1] = false;
                        Map.nationBeAttack[1] = -1;
                    }
                }

                map.doCheckCreateMatch();
            }

            if (((Vector) Map.idMapMONSTER.get(1)).contains(map.mapId)) {
                if (!map.haveCharMonter[1] && System.currentTimeMillis() - map.timeMonster[1] > 30000L && map.killMonster[1]) {
                    Vector<Char> players = (Vector<Char>) map.allPlayers.get(1);
                    if (players.size() > 0) {
                        map.timeLiveMonster[1] = System.currentTimeMillis();
                        Char c = (Char) players.get(Map.r.nextInt(players.size()));
                        c.isMonster = true;
                        c.sendMessage(MessageCreator.createMsgInfoChar2Monster(c, true, 0, "update map 1"));
                        c.sendToNearPlayer(MessageCreator.createMsgInfoChar2Monster(c, true, 0, "update map 2"));
                        map.haveCharMonter[1] = true;
                        map.nCharBeKilled[1] = 0;
                    }
                }

                if (map.haveCharMonter[1] && System.currentTimeMillis() - map.timeStartGame[1] > 600000L) {
                    map.timeLiveMonster[1] = 0L;
                    map.kickPlayerOutEventMonster(1);
                    map.haveCharMonter[1] = false;
                    map.addChar2MapMonster(1, 1);
                }

                if (map.killMonster[1]) {
                    if (((Vector) map.allPlayers.get(1)).size() == 0) {
                        map.addChar2MapMonster(2, 1);
                    }

                    map.endGameMonster(1);
                }

                if (!map.killMonster[1]) {
                    map.checkTimeEventMonster(1);
                }
            }

            Vector<Char> players = (Vector<Char>) map.allPlayers.get(1);
            if (players.size() > 0 || map.mapId == 25) {
                if (Map.bossCopy.size() > 0) {
                    for (byte ix = 0; ix < Map.bossCopy.size(); ix++) {
                        try {
                            Boss bo = (Boss) Map.bossCopy.get(ix);
                            if (bo.hp <= 0 || bo.isDead) {
                                bo.map.monsters.remove(bo.id);
                                Map.bossCopy.remove(ix);

                                try {
                                    Message m = new Message(90);
                                    m.dos.writeShort(bo.id);
                                    m.dos.writeByte(bo.cat);
                                    bo.map.sendAllPlayer(m, bo.inCountry);
                                } catch (Exception var9) {
                                    var9.printStackTrace();
                                }
                            }
                        } catch (Exception var14) {
                        }
                    }
                }

                while (((Vector) map.tempMonster.get(1)).size() > 0) {
                    Monster mt = (Monster) ((Vector) map.tempMonster.get(1)).remove(0);
                    if (mt.map != null && mt.map.mapId == map.mapId && mt.hp > 0) {
                        ((Hashtable) mt.map.monsters.get(1)).put(mt.id, mt);
                    }
                }

                while (((Vector) map.tempRemoveMonster.get(1)).size() > 0) {
                    Monster mt = (Monster) ((Vector) map.tempRemoveMonster.get(1)).remove(0);
                    if (BossLauDai.goKhoiBang(map, (Hashtable) map.monsters.get(1), mt)) {
                        continue; // boss Gym / the gioi: go ca khi da guc
                    }
                    if (mt.map != null && mt.map.mapId == map.mapId && mt.hp > 0) {
                        ((Hashtable) mt.map.monsters.get(1)).remove(mt.id);
                    }
                }

                Collection<Monster> listmonster = ((Hashtable) map.monsters.get(1)).values();
                if (map.isSkelonton) {
                    listmonster = ((Hashtable) map.temp_monsters.get(1)).values();
                }

                for (Monster mt : new java.util.ArrayList<Monster>(listmonster)) {   // ban sao: tranh ConcurrentModificationException (tai 315 bot cb2 31/10)
                    try {
                        mt.update();
                        if (mt.target != null && mt.target.exit) {
                            mt.target = null;
                        }

                        if (players.size() > 0 && (Map.isNewVersion ? !mt.isDead : !mt.isDead && (mt.moved || mt.isMonsterVantieu()))) {
                            Char fp = null;
                            int luonThem = -1; // xem Region: tinh mot lan moi quai

                            for (int ix = 0; ix < players.size(); ix++) {
                                try {
                                    Char p = (Char) players.get(ix);
                                    if (p.isBot == -1) {
                                        if (p.near(mt, p.rangeAddMonster[0])
                                                || (luonThem >= 0 ? luonThem == 1 : (luonThem = (mt.allWayAdd() || mt.isBoss || mt.idTemplate == 84 || mt.idTemplate == 90
                                                        || mt.idTemplate == 36 || mt.idTemplate == 37 || map.mapIDLoadMap == 201) ? 1 : 0) == 1)) {
                                            if (Map.isNewVersion) {
                                                if (!UtilHTBB.coId(p.nearMons, mt.id)) {
                                                    p.nearMons.add(mt.id);
                                                    p.sendMessage(p.writeActorPos(new Message(4), mt));
                                                    LoaiQuaiGame.sauThemQuai(p, mt); // 0.9.8: -125 loai rieng / dau hoang da (ban do khong chia khu)
                                                }
                                            } else {
                                                p.nearMons.add(mt.id);
                                            }
                                        }

                                        if (p.near(mt, 120)) {
                                            if (p.hp > 0
                                                    && !p.beAttack
                                                    && mt.target == null
                                                    && mt.isActive()
                                                    && !p.isAdmin
                                                    && p.isBot == -1
                                                    && p.region == mt.region
                                                    && mt.isEnemy(p)) {
                                                mt.target = p;
                                                p.beAttack = true;
                                            } else if (p.beAttack && fp == null) {
                                                fp = p;
                                            }
                                        }
                                    }
                                } catch (Exception var12) {
                                }
                            }

                            if (fp != null && mt.target == null) {
                                mt.target = fp;
                            }
                        }
                    } catch (Exception var13) {
                        Logger.logError(var13, "updateleaf.txt");
                        System.out.print("UPDATE MAP MT 1");
                    }
                }
            }

            boolean exitBoard = false;
            int size = players.size();
            int k = 0;

            while (k < players.size()) {
                try {
                    Char p = (Char) players.get(k);
                    if ((p.map == null || p.getSession() == null || p.mapID == -1) && p.isBot == -1) {
                        // Duong nay go nhan vat khoi SO DANG KY, khong chi khoi ban do.
                        // Truoc day no khong luu mot lan nao: moi thay doi ke tu lan luu
                        // truoc (tu dong 5-11 phut mot lan) bien mat khong dau vet, va lan
                        // vao lai doc dung ban ghi cu do. Phai luu truoc khi go.
                        //
                        // Chi luu khi doi tuong nay DUNG la nhan vat dang dang ky cho tai
                        // khoan do. Neu no la ban cu con sot lai trong vector nguoi choi
                        // thi ghi xuong se de len ban moi hon - dung cai loi dang di sua.
                        //
                        // PHAI QUA ThreadPool, KHONG duoc goi Database.instance.saveChar
                        // TRUC TIEP o day: ham nay chay ngay trong vong lap cap nhat cua
                        // CHINH LUONG "MAP <id>" (Map implements Runnable, moi map mot
                        // luong rieng) - dung khuon mau CharVongLap dang dung cho autosave
                        // dinh ky. saveChar la nhieu cau lenh SQL noi tiep tren bang
                        // tob_char (MyISAM, khoa CA BANG khi ghi); goi dong bo o day tung
                        // bien mat ~03/09/2026: sau mot lan khoi dong lai lam nhieu nguoi
                        // reconnect gan nhu cung luc, luong nay bi CHAN CHO khoa bang, va
                        // vi no la LUONG DUY NHAT cap nhat quai cho ca ban do nen toan bo
                        // quai tren MOI ban do ngung hien (moi luong "MAP <id>" deu co the
                        // dinh cung kieu). Day qua hang doi thi luong map duoc giai phong
                        // ngay, viec luu van xay ra, chi cham hon vai chuc ms.
                        //
                        // Phep kiem "dung la ban dang dang ky" phai lam NGAY BAY GIO,
                        // TRUOC khi remove(p) o duoi - trong bo nho, khong dung I/O nen
                        // khong chan luong. Lam phep kiem nay BEN TRONG Runnable la sai:
                        // luc do CharManager da go p roi (dong remove(p) chay truoc khi
                        // ThreadPool kip thuc thi), phep kiem se luon that bai va KHONG
                        // BAO GIO luu duoc gi ca.
                        if (CharManager.instance.getByUserID(p.userID) == p) {
                            final Char pCanLuu = p;
                            ThreadPool.instant.workList.add(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Database.instance.saveChar(pCanLuu, false);
                                    } catch (Exception exLuu) {
                                        exLuu.printStackTrace();
                                    }
                                }
                            });
                        }
                        players.remove(k);
                        CharManager.instance.remove(p);
                    } else if (p.exit) {
                        players.remove(k);
                    } else if ((!p.map.equals(map) || p.mapID != map.mapId) && p.isBot == -1) {
                        players.remove(k);
                    } else if (Map.getTown[1] && p.myCountry != p.inCountry && map.mapId == Map.idMapTown) {
                        map.kickCharToVilage(p);
                    } else {
                        p.update();
                        if ((map.mapId == 107 || map.mapId == 2441 || map.mapId == 2442 || map.mapId == 2443 || map.mapId == 2444)
                                && System.currentTimeMillis() - TeamServer.timeOutBoard >= TeamServer.timeDownBoard) {
                            exitBoard = true;
                        }

                        k++;
                    }
                } catch (Exception var11) {
                    break;
                }
            }

            if (exitBoard) {
                size = players.size();

                while (size > 0) {
                    try {
                        Char p = (Char) players.remove(0);
                        size--;
                        if (p != null) {
                            int id = map.mapId % 2440;
                            if (map.mapId == 107) {
                                id = 10;
                            } else {
                                id += 500;
                            }

                            if (p.monster != null && p.map.equals(p.monster.map)) {
                                Map.inRangeActor(p, p.monster, 120);
                            }

                            // ben kia khoa -> ve ben xuat phat mot lan (truoc: nap lai thuyen moi nhip, ket) - sua-ket-tau
                            KhoaBanDoGame.xuongThuyen(map, p, id);
                        }
                    } catch (Exception var8) {
                    }
                }

                TeamServer.timeOutBoard = 0L;
            }

            if (!map.isSkelonton) {
                if (Map.openSkelonton()) {
                    map.isSkelonton = true;
                    map.reSetAllMonster(1, 0);
                    map.killAllMonster(map.monsters, 1, 0);
                }
            } else if (!Map.openSkelonton()) {
                map.isSkelonton = false;
                map.killAllMonster(map.monsters, 1, 0);
                ((Hashtable) map.temp_monsters.get(1)).clear();
            }

            map.deletePotionAndItemOnGround(1);
        }
    }

    static void updateWind(Map map) {
        if (System.currentTimeMillis() - Map.timeCheckCallBoss > 0L) {
            Boss bo = new BossDracula(null, (MonsterTemplate) Map.monsterTemplates.get(117), 432, 182, 0);
            bo.isDead = true;
            byte[] he = new byte[]{0, 1, 2, 3, 4};
            bo.id = RealController.intance.idGen.getID(1, "new monster");
            bo.he = bo.heKhiSinh(he[Map.r.nextInt(5)]);
            bo.level = bo.getMonsterTemplate().level;
            Map.bossTime.add(bo);
            Map.timeCheckCallBoss = System.currentTimeMillis() + 14400000L;
        }

        if (map.nRegion <= 0) {
            if (map.mapId == 201) {
                if (Map.weather != null && System.currentTimeMillis() > Map.timeWait) {
                    Map.sendAllCharServer(-1, MessageCreator.createMsgWeather(-1, 0, 0));
                    Map.weather = null;
                    Map.timeWait = System.currentTimeMillis() + 1800000L;
                }

                if (!Map.getTown[2]) {
                    map.checkTimeGetTown(2);
                } else if (map.gameOver(2) && Map.getTown[2]) {
                    Map.getTown[2] = false;

                    for (int i = 0; i < CharManager.instance.vChars.size(); i++) {
                        try {
                            ((Char) CharManager.instance.vChars.elementAt(i))
                                    .sendMessage(MessageCreator.createMsgStartGetTown(((Char) CharManager.instance.vChars.elementAt(i)).inCountry));
                            ((Char) CharManager.instance.vChars.elementAt(i))
                                    .sendMessage(MessageCreator.createServerAlertAutoOffMessage("Hết thời gian chiếm Gym. Chưa có đội nào chiếm được Gym"));
                        } catch (Exception var9) {
                        }
                    }
                }

                if (!map.curday[2].equals("")) {
                    map.gameOver(2);
                }

                if (!Map.nwar[2]) {
                    map.checkTimeStartWar(((RegisterAttack) Map.nationWar.get(2)).dayAttack);
                    ((RegisterAttack) Map.nationWar.get(2)).update(2);
                } else {
                    boolean over = map.timeOutNationWar(2);
                    if (over && Map.nationBeAttack[2] > -1) {
                        ((RegisterAttack) Map.nationWar.get(2)).reset();
                        Map.nwar[2] = false;
                        Map.nationBeAttack[2] = -1;
                    }
                }
            }

            if (((Vector) Map.idMapMONSTER.get(2)).contains(map.mapId)) {
                Vector<Char> players = (Vector<Char>) map.allPlayers.get(2);
                if (!map.haveCharMonter[2] && System.currentTimeMillis() - map.timeMonster[2] > 30000L && map.killMonster[2] && players.size() > 0) {
                    map.timeLiveMonster[2] = System.currentTimeMillis();
                    Char c = (Char) players.get(Map.r.nextInt(players.size()));
                    c.isMonster = true;
                    c.sendMessage(MessageCreator.createMsgInfoChar2Monster(c, true, 0, "updateWind map 1"));
                    c.sendToNearPlayer(MessageCreator.createMsgInfoChar2Monster(c, true, 0, "updateWind map 2"));
                    map.haveCharMonter[2] = true;
                    map.nCharBeKilled[2] = 0;
                }

                if (map.haveCharMonter[2] && System.currentTimeMillis() - map.timeStartGame[2] > 600000L) {
                    map.timeLiveMonster[2] = 0L;
                    map.kickPlayerOutEventMonster(2);
                    map.haveCharMonter[2] = false;
                    map.addChar2MapMonster(1, 2);
                }

                if (map.killMonster[2]) {
                    if (players.size() == 0) {
                        map.addChar2MapMonster(2, 2);
                    }

                    map.endGameMonster(2);
                }

                if (!map.killMonster[2]) {
                    map.checkTimeEventMonster(2);
                }
            }

            Vector<Char> playersx = (Vector<Char>) map.allPlayers.get(2);
            if (playersx.size() > 0 || map.mapId == 25) {
                while (((Vector) map.tempMonster.get(2)).size() > 0) {
                    Monster mt = (Monster) ((Vector) map.tempMonster.get(2)).remove(0);
                    if (mt.map != null && mt.map.mapId == map.mapId && mt.hp > 0) {
                        ((Hashtable) mt.map.monsters.get(2)).put(mt.id, mt);
                    }
                }

                while (((Vector) map.tempRemoveMonster.get(2)).size() > 0) {
                    Monster mt = (Monster) ((Vector) map.tempRemoveMonster.get(2)).remove(0);
                    if (BossLauDai.goKhoiBang(map, (Hashtable) map.monsters.get(2), mt)) {
                        continue; // boss Gym / the gioi: go ca khi da guc
                    }
                    if (mt.map != null && mt.map.mapId == map.mapId && mt.hp > 0) {
                        ((Hashtable) mt.map.monsters.get(2)).remove(mt.id);
                    }
                }

                Collection<Monster> listmonster = ((Hashtable) map.monsters.get(2)).values();
                if (map.isSkelonton) {
                    listmonster = ((Hashtable) map.temp_monsters.get(2)).values();
                }

                for (Monster mt : new java.util.ArrayList<Monster>(listmonster)) {   // ban sao: tranh ConcurrentModificationException (tai 315 bot cb2 31/10)
                    try {
                        mt.update();
                        if (mt.target != null && mt.target.exit) {
                            mt.target = null;
                        }

                        if (playersx.size() > 0 && (Map.isNewVersion ? !mt.isDead : !mt.isDead && mt.moved)) {
                            Char fp = null;
                            int luonThem = -1; // xem Region: tinh mot lan moi quai

                            for (int i = 0; i < playersx.size(); i++) {
                                try {
                                    Char p = (Char) playersx.get(i);
                                    if (p.isBot == -1) {
                                        if (p.near(mt, p.rangeAddMonster[0])
                                                || (luonThem >= 0 ? luonThem == 1 : (luonThem = (mt.allWayAdd() || mt.isBoss || mt.idTemplate == 84 || mt.idTemplate == 90
                                                        || mt.idTemplate == 36 || mt.idTemplate == 37 || map.mapIDLoadMap == 201) ? 1 : 0) == 1)) {
                                            if (Map.isNewVersion) {
                                                if (!UtilHTBB.coId(p.nearMons, mt.id)) {
                                                    p.nearMons.add(mt.id);
                                                    p.sendMessage(p.writeActorPos(new Message(4), mt));
                                                    LoaiQuaiGame.sauThemQuai(p, mt); // 0.9.8: -125 loai rieng / dau hoang da (ban do khong chia khu)
                                                }
                                            } else {
                                                p.nearMons.add(mt.id);
                                            }
                                        }

                                        if (p.near(mt, 110) && mt.typeAttack == 1) {
                                            if (p.hp > 0
                                                    && mt.isEnemy(p)
                                                    && !p.beAttack
                                                    && mt.target == null
                                                    && mt.getMonsterTemplate().active
                                                    && !p.isAdmin
                                                    && p.isBot == -1
                                                    && p.region == mt.region) {
                                                mt.target = p;
                                                p.beAttack = true;
                                            } else if (p.beAttack && fp == null) {
                                                fp = p;
                                            }
                                        }
                                    }
                                } catch (Exception var11) {
                                }
                            }

                            if (fp != null && mt.target == null) {
                                mt.target = fp;
                            }
                        }
                    } catch (Exception var12) {
                        System.out.print("UPDATE MAP MT 2");
                    }
                }
            }

            boolean exitBoard = false;
            int size = playersx.size();
            int k = 0;

            while (k < playersx.size()) {
                try {
                    Char p = (Char) playersx.get(k);
                    if ((p.map == null || p.getSession() == null || p.mapID == -1) && p.isBot == -1) {
                        playersx.remove(k);
                        CharManager.instance.remove(p);
                    } else if ((!p.map.equals(map) || p.mapID != map.mapId) && p.isBot == -1) {
                        playersx.remove(k);
                    } else if (p.exit) {
                        playersx.remove(k);
                    } else if (Map.getTown[2] && p.myCountry != p.inCountry && map.mapId == Map.idMapTown) {
                        map.kickCharToVilage(p);
                    } else {
                        p.update();
                        if ((map.mapId == 107 || map.mapId == 2441 || map.mapId == 2442 || map.mapId == 2443 || map.mapId == 2444)
                                && System.currentTimeMillis() - TeamServer.timeOutBoard >= TeamServer.timeDownBoard) {
                            exitBoard = true;
                        }

                        k++;
                    }
                } catch (Exception var10) {
                    break;
                }
            }

            if (exitBoard) {
                size = playersx.size();

                while (size > 0) {
                    try {
                        Char p = (Char) playersx.remove(0);
                        size--;
                        if (p != null) {
                            int id = map.mapId % 2440;
                            if (map.mapId == 107) {
                                id = 10;
                            } else {
                                id += 500;
                            }

                            // ben kia khoa -> ve ben xuat phat mot lan (truoc: nap lai thuyen moi nhip, ket) - sua-ket-tau
                            KhoaBanDoGame.xuongThuyen(map, p, id);
                        }
                    } catch (Exception var8) {
                    }
                }

                TeamServer.timeOutBoard = 0L;
            }

            if (!map.isSkelonton) {
                if (Map.openSkelonton()) {
                    map.isSkelonton = true;
                    map.reSetAllMonster(2, 0);
                    map.killAllMonster(map.monsters, 2, 0);
                }
            } else if (!Map.openSkelonton()) {
                map.isSkelonton = false;
                map.killAllMonster(map.monsters, 2, 0);
                ((Hashtable) map.temp_monsters.get(2)).clear();
            }

            map.deletePotionAndItemOnGround(2);
        }
    }

    static void updateSand(Map map) {
        if (map.nRegion <= 0) {
            if (map.mapId == 201) {
                if (Map.weather != null && System.currentTimeMillis() > Map.timeWait) {
                    Map.sendAllCharServer(-1, MessageCreator.createMsgWeather(-1, 0, 0));
                    Map.weather = null;
                    Map.timeWait = System.currentTimeMillis() + 1800000L;
                }

                if (!Map.getTown[0]) {
                    map.checkTimeGetTown(0);
                } else if (map.gameOver(0) && Map.getTown[0]) {
                    Map.getTown[0] = false;

                    for (int i = 0; i < CharManager.instance.vChars.size(); i++) {
                        try {
                            ((Char) CharManager.instance.vChars.elementAt(i))
                                    .sendMessage(MessageCreator.createMsgStartGetTown(((Char) CharManager.instance.vChars.elementAt(i)).inCountry));
                            ((Char) CharManager.instance.vChars.elementAt(i))
                                    .sendMessage(MessageCreator.createServerAlertAutoOffMessage("Hết thời gian chiếm Gym. Chưa có đội nào chiếm được Gym"));
                        } catch (Exception var9) {
                        }
                    }
                }

                if (!map.curday[0].equals("")) {
                    map.gameOver(0);
                }

                if (!Map.nwar[0]) {
                    map.checkTimeStartWar(((RegisterAttack) Map.nationWar.get(0)).dayAttack);
                    ((RegisterAttack) Map.nationWar.get(0)).update(0);
                } else {
                    boolean over = map.timeOutNationWar(0);
                    if (over && Map.nationBeAttack[0] > -1) {
                        ((RegisterAttack) Map.nationWar.get(0)).reset();
                        Map.nwar[0] = false;
                        Map.nationBeAttack[0] = -1;
                    }
                }
            }

            if (((Vector) Map.idMapMONSTER.get(0)).contains(map.mapId)) {
                Vector<Char> players = (Vector<Char>) map.allPlayers.get(0);
                if (!map.haveCharMonter[0] && System.currentTimeMillis() - map.timeMonster[0] > 30000L && map.killMonster[0] && players.size() > 0) {
                    map.timeLiveMonster[0] = System.currentTimeMillis();
                    Char c = (Char) players.get(Map.r.nextInt(players.size()));
                    c.isMonster = true;
                    c.sendMessage(MessageCreator.createMsgInfoChar2Monster(c, true, 0, "updateSand map 1"));
                    c.sendToNearPlayer(MessageCreator.createMsgInfoChar2Monster(c, true, 0, "updateSand map 2"));
                    map.haveCharMonter[0] = true;
                    map.nCharBeKilled[0] = 0;
                }

                if (map.haveCharMonter[0] && System.currentTimeMillis() - map.timeStartGame[0] > 600000L) {
                    map.timeLiveMonster[0] = 0L;
                    map.kickPlayerOutEventMonster(0);
                    map.haveCharMonter[0] = false;
                    map.addChar2MapMonster(1, 0);
                }

                if (map.killMonster[0]) {
                    if (players.size() == 0) {
                        map.addChar2MapMonster(2, 0);
                    }

                    map.endGameMonster(0);
                }

                if (!map.killMonster[0]) {
                    map.checkTimeEventMonster(0);
                }
            }

            Vector<Char> playersx = (Vector<Char>) map.allPlayers.get(0);
            if (playersx.size() > 0 || map.mapId == 25) {
                while (((Vector) map.tempMonster.get(0)).size() > 0) {
                    Monster mt = (Monster) ((Vector) map.tempMonster.get(0)).remove(0);
                    if (mt.map != null && mt.map.mapId == map.mapId && mt.hp > 0) {
                        ((Hashtable) mt.map.monsters.get(0)).put(mt.id, mt);
                    }
                }

                while (((Vector) map.tempRemoveMonster.get(0)).size() > 0) {
                    Monster mt = (Monster) ((Vector) map.tempRemoveMonster.get(0)).remove(0);
                    if (BossLauDai.goKhoiBang(map, (Hashtable) map.monsters.get(0), mt)) {
                        continue; // boss Gym / the gioi: go ca khi da guc
                    }
                    if (mt.map != null && mt.map.mapId == map.mapId && mt.hp > 0) {
                        ((Hashtable) mt.map.monsters.get(0)).remove(mt.id);
                    }
                }

                Collection<Monster> listmonster = ((Hashtable) map.monsters.get(0)).values();
                if (map.isSkelonton) {
                    listmonster = ((Hashtable) map.temp_monsters.get(0)).values();
                }

                for (Monster mt : new java.util.ArrayList<Monster>(listmonster)) {   // ban sao: tranh ConcurrentModificationException (tai 315 bot cb2 31/10)
                    try {
                        mt.update();
                        if (mt.target != null && mt.target.exit) {
                            mt.target = null;
                        }

                        if (playersx.size() > 0 && (Map.isNewVersion ? !mt.isDead : !mt.isDead && mt.moved)) {
                            Char fp = null;
                            int luonThem = -1; // xem Region: tinh mot lan moi quai

                            for (int i = 0; i < playersx.size(); i++) {
                                try {
                                    Char p = (Char) playersx.get(i);
                                    if (p.isBot == -1) {
                                        if (p.near(mt, p.rangeAddMonster[0])
                                                || (luonThem >= 0 ? luonThem == 1 : (luonThem = (mt.allWayAdd() || mt.isBoss || mt.idTemplate == 84 || mt.idTemplate == 90
                                                        || mt.idTemplate == 36 || mt.idTemplate == 37 || map.mapIDLoadMap == 201) ? 1 : 0) == 1)) {
                                            if (Map.isNewVersion) {
                                                if (!UtilHTBB.coId(p.nearMons, mt.id)) {
                                                    p.nearMons.add(mt.id);
                                                    p.sendMessage(p.writeActorPos(new Message(4), mt));
                                                    LoaiQuaiGame.sauThemQuai(p, mt); // 0.9.8: -125 loai rieng / dau hoang da (ban do khong chia khu)
                                                }
                                            } else {
                                                p.nearMons.add(mt.id);
                                            }
                                        }

                                        if (p.near(mt, 110) && mt.typeAttack == 1) {
                                            if (p.hp > 0
                                                    && mt.isEnemy(p)
                                                    && !p.beAttack
                                                    && mt.target == null
                                                    && mt.getMonsterTemplate().active
                                                    && !p.isAdmin
                                                    && p.isBot == -1
                                                    && p.region == mt.region) {
                                                mt.target = p;
                                                p.beAttack = true;
                                            } else if (p.beAttack && fp == null) {
                                                fp = p;
                                            }
                                        }
                                    }
                                } catch (Exception var11) {
                                }
                            }

                            if (fp != null && mt.target == null) {
                                mt.target = fp;
                            }
                        }
                    } catch (Exception var12) {
                        System.out.print("UPDATE MAP MT 0");
                    }
                }
            }

            boolean exitBoard = false;
            int size = playersx.size();
            int k = 0;

            while (k < playersx.size()) {
                try {
                    Char p = (Char) playersx.get(k);
                    if ((p.map == null || p.getSession() == null || p.mapID == -1) && p.isBot == -1) {
                        playersx.remove(k);
                        CharManager.instance.remove(p);
                    } else if ((!p.map.equals(map) || p.mapID != map.mapId) && p.isBot == -1) {
                        playersx.remove(k);
                    } else if (p.exit) {
                        playersx.remove(k);
                    } else if (Map.getTown[0] && p.myCountry != p.inCountry && map.mapId == Map.idMapTown) {
                        map.kickCharToVilage(p);
                    } else {
                        p.update();
                        if ((map.mapId == 107 || map.mapId == 2441 || map.mapId == 2442 || map.mapId == 2443 || map.mapId == 2444)
                                && System.currentTimeMillis() - TeamServer.timeOutBoard >= TeamServer.timeDownBoard) {
                            exitBoard = true;
                        }

                        k++;
                    }
                } catch (Exception var10) {
                    break;
                }
            }

            if (exitBoard) {
                size = playersx.size();

                while (size > 0) {
                    try {
                        Char p = (Char) playersx.remove(0);
                        size--;
                        if (p != null) {
                            int id = map.mapId % 2440;
                            if (map.mapId == 107) {
                                id = 10;
                            } else {
                                id += 500;
                            }

                            // ben kia khoa -> ve ben xuat phat mot lan (truoc: nap lai thuyen moi nhip, ket) - sua-ket-tau
                            KhoaBanDoGame.xuongThuyen(map, p, id);
                        }
                    } catch (Exception var8) {
                    }
                }

                TeamServer.timeOutBoard = 0L;
            }

            if (!map.isSkelonton) {
                if (Map.openSkelonton()) {
                    map.isSkelonton = true;
                    map.reSetAllMonster(0, 0);
                    map.killAllMonster(map.monsters, 0, 0);
                }
            } else if (!Map.openSkelonton()) {
                map.isSkelonton = false;
                map.killAllMonster(map.monsters, 0, 0);
                ((Hashtable) map.temp_monsters.get(0)).clear();
            }

            map.deletePotionAndItemOnGround(0);
        }
    }

    static void startSandVilage(final Map map) {
        if (map.nRegion <= 0) {
            // Truoc: thread "THREAD SAND <id>" (updateSand moi DELAY_UPDATE_MAP) + thread goi tin
            // phe 0. Nay: Nhip + Lan tren pool chung (com.htbb.runtime.VongLapChung), than giu nguyen.
            map.nhipSand = new com.htbb.runtime.VongLapChung.Nhip("map " + map.mapId + " sand", Map.DELAY_UPDATE_MAP, new Runnable() {
                @Override
                public void run() {
                    try {
                        map.map_run_state = 0;
                        // Phai goi qua map.updateSand() (Map.updateSand() lai goi
                        // MapVongLap.updateSand(this)) chu KHONG goi thang ham tinh
                        // updateSand(map): OfflineMap ghi de updateSand() va ban ghi
                        // de do moi la ban chay auto-imbue cho nguoi choi trong map
                        // offline. Goi thang ham tinh lam mat tinh da hinh -> ban ghi
                        // de tro thanh ma chet.
                        map.updateSand();
                        map.lastTimeUpdateMap = System.currentTimeMillis();
                        map.map_run_state = 1;
                    } catch (Exception var5) {
                        if (!daBaoLoiRunMapSand) {
                            daBaoLoiRunMapSand = true;
                            System.out.println("LOI TRONG HAM RUN MAP (updateSand, chi bao 1 lan - lap lai se ngap log):");
                            var5.printStackTrace();
                        }
                    }
                }
            }, null).batDau(Map.DELAY_UPDATE_MAP);
            map.taoLanGoiTin(0);
        }
    }

    /**
     * Than vong goi tin cua map khong chia khu (giong het ba vong cu phe 0/1/2): don hang doi
     * (toi da 500 goi moi luot - vong cu ngu 5 ms moi 500 goi), roi map 201 cap nhat NPC nhan the.
     */
    static void donGoiTin(Map map, int phe, com.htbb.runtime.VongLapChung.Lan lan) {
        Vector<PlayerMessage> playerMessages = (Vector) map.allPlayerMessages.get(phe);
        try {
            int count = 0;
            while (playerMessages.size() > 0) {
                PlayerMessage pm = (PlayerMessage) playerMessages.remove(0);
                if (!pm.player.exit) {
                    map.processMessage(pm.player, pm.message);
                }
                if (++count == 500) {
                    break;
                }
            }

            if (Map.getTown[phe] && map.mapId == 201) {
                Vector<NpcReceiveCard> npcReceiveCard = (Vector<NpcReceiveCard>) Map.npcReceiveCard.get(phe);

                for (int i = 0; i < npcReceiveCard.size(); i++) {
                    ((NpcReceiveCard) npcReceiveCard.get(i)).update();
                    if (NpcReceiveCard.isFinish[phe]) {
                        break;
                    }
                }

                if (NpcReceiveCard.isFinish[phe]) {
                    Map.getTown[phe] = false;
                    npcReceiveCard.removeAll(npcReceiveCard);
                }
            }
        } catch (Exception var6) {
            var6.printStackTrace();
        }
        if (playerMessages.size() > 0 && lan != null) {
            lan.kich();
        }
    }

}
