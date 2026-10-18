package real;


import data.CharInfo;
import data.Cooking;
import data.Database;
import data.GameCanvas;
import data.ItemLuckyDraw;
import data.NewClan;
import data.Weather;
import io.Message;
import io.SessionManager;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;
import real.cmd.LoginHandler;
import server.TeamServer;
import util.Logger;

/**
 * Than cua Map.doChat - xu ly tin nhan chat cua nguoi choi, gom ca cac lenh
 * chat dac biet cua admin/GM.
 *
 * 1.497 dong nay truoc nam trong real/Map.java. Map.doChat VAN CON, giu
 * NGUYEN chu ky public cu, chi uy nhiem sang day. real/MapTown.java van
 * override doChat nhu cu, khong bi anh huong.
 */
// Thanh vien static cua Map ma than method nay dung khong dinh danh.
import static real.Map.ALL_CHAR_BUY_RUONG;
import static real.Map.ALL_CHAR_VIP;
import static real.Map.COUNT_DOWN;
import static real.Map.MAX_LI_XI;
import static real.Map.MAX_QUAY;
import static real.Map.adminTest;
import static real.Map.all_ngoc_rong_hac_ho;
import static real.Map.all_ngoc_rong_thanh_long;
import static real.Map.boss;
import static real.Map.checkRcvXP;
import static real.Map.countQuaySo;
import static real.Map.doBuyModelClothe;
import static real.Map.doBuyRuongMayMan;
import static real.Map.dropHoaHong;
import static real.Map.event;
import static real.Map.getLogVantieu;
import static real.Map.giveSpecialItem;
import static real.Map.hourGiveLixi;
import static real.Map.indexPet;
import static real.Map.isItemThanThu;
import static real.Map.isThamDinh;
import static real.Map.istestliendau;
import static real.Map.itemTemplates;
import static real.Map.loadGhost;
import static real.Map.max_phuong_hoang;
import static real.Map.minuteEventMons;
import static real.Map.minuteGiveLixi;
import static real.Map.monsterTemplates;
import static real.Map.nPhuongHoang;
import static real.Map.okChedo;
import static real.Map.onOffSound;
import static real.Map.openvantieu;
import static real.Map.resetShield;
import static real.Map.testMove;
import static real.Map.timeEventMonster;
import static real.Map.timeWait;
import static real.Map.timeWaitSellRuongMM;
import static real.Map.totaltrungget;
import static real.Map.totalxuathienvv;
import static real.Map.weather;

class MapChat {

    static void doChat(Map map, Char player, Message message) throws IOException {
        if (player.hp > 0 || player.isAdmin) {
            if (player.forbitChat > 0 && !player.isAdmin) {
                player.sendMessage(MessageCreator.createMsgChat(player.id, "Bạn đang bị cấm chat. Thời gian cấm chat còn lại là " + player.forbitChat));
            } else {
                Message m = new Message(27);
                m.dos.writeShort(player.id);
                String msgChat = message.dis.readUTF().trim();

                // "boskin": thao skin linh thu ra, tra vat pham ve hanh trang.
                //
                // Phai lam bang lenh chat vi bang "Trang bi thu" ben client KHONG co nut
                // thao do: doUseItem chi tim vat pham trong hanh trang (player.iItems)
                // nen bam vao mon dang mac khong goi duoc gi, cach doi trang bi thu duy
                // nhat cua game la mac de len mon khac. Khong co lenh nay thi nguoi choi
                // lo mac skin vao la khong bao gio tro lai hinh thu that duoc.
                //
                // De TRUOC cho chan lenh admin vi day la lenh danh cho moi nguoi choi.
                if (msgChat.equalsIgnoreCase("boskin")) {
                    SkinThuCuoi.thaoSkin(player);
                    return;
                }
                // he sinh thai: tra loi "Ai la Pokemon?", lenh tat "hst"/"cauca"/"dao"/"dua". Tat co: false.
                if (com.htbb.pokemon.HeSinhThaiGame.khiChat(player, msgChat)) {
                    return;
                }
                // GD2b: dap an cau do nhiem vu - dung thi hoan thanh va KHONG phat len kenh.
                if (NhiemVuEngine.khiChat(player, msgChat)) {
                    return;
                }

                // Nguoi choi thuong chi chat, khong di qua chuoi lenh ben duoi.
                // Ly do: rat nhieu lenh cu trong chuoi nay khong kiem tra quyen admin
                // (dvip, ngoc, ruong, quayso, die, class, openboss...) nen neu de nguoi
                // choi thuong cung chay qua thi ho co the tu cap do / trieu boss.
                if (!player.isAdmin) {
                    MapXaHoi.doChatBinhThuong(map, player, m, msgChat);
                    return;
                }

                // Bang setting cho admin (popup nhieu cap): chiem thanh, ti le drop.
                if (msgChat.startsWith("setting")) {
                    // "setting menu" = menu danh sach (chon bang chuot).
                    // "setting"      = o nhap lenh, dieu khien 100% bang ban phim.
                    if (msgChat.contains("menu")) {
                        AdminMenu.moMenu(player);
                    } else {
                        AdminMenu.moONhapLenh(player);
                    }
                    return;
                }

                if (msgChat.startsWith("chiemthanh")) {
                    // Chi admin moi duoc ep gio chiem thanh (truoc day nguoi choi thuong
                    // chat "chiemthanh <gio> <phut>" cung set duoc).
                    // chiemthanh <gio> <phut> [yyyy-MM-dd|Mon..Sun]
                    if (player.isAdmin) {
                        try {
                            String[] data = Char.split(msgChat, " ");
                            MapTown.TimeAdminCheat = Integer.parseInt(data[1]);
                            MapTown.minuteAdminCheat = Integer.parseInt(data[2]);
                            MapTown.dayAdminCheat = data.length > 3 ? data[3].trim() : "";
                            server.CauHinh.luuGioChiemThanh();
                            player.sendMessage(MessageCreator.createServerAlertMessage(
                                    "Đã set chiếm Gym " + MapTown.TimeAdminCheat + "h" + MapTown.minuteAdminCheat
                                            + (MapTown.dayAdminCheat.isEmpty() ? " mỗi ngày." : " ngày " + MapTown.dayAdminCheat + ".")
                                            + "\n(đã lưu vào server.ini)", ""));
                        } catch (Exception exCT) {
                            player.sendMessage(MessageCreator.createServerAlertMessage(
                                    "Cú pháp: chiemthanh <giờ> <phút> [yyyy-MM-dd|Mon..Sun]", ""));
                        }
                    }
                    return;
                }
                if (msgChat.startsWith("adm")) {
                    if (player.isAdmin) {
                        adminTest = !adminTest;
                        System.out.println("msg chat: " + msgChat + " >> " + player.isAdmin + " " + adminTest);
                    }
                } else {
                    if (player.isAdmin) 
                    {
                        // Lam sach: tin chat co the chua xuong dong -> gia mao dong kiem toan.
                        Logger.logStringWithDate(player.charname + ": " + com.htbb.log.Nhat.sach(msgChat), "AdminChat.txt");
                        com.htbb.log.Nhat.kiemToan("ADMIN", "chat lenh " + player.charname + ": " + msgChat);
                        
                        if (msgChat.startsWith("tlixi")) {
                            String[] data = Char.split(msgChat, " ");
                            MAX_LI_XI = Integer.parseInt(data[1]);
                            hourGiveLixi = Integer.parseInt(data[2]);
                            minuteGiveLixi = Integer.parseInt(data[3]);
                            return;
                        }

                        if (msgChat.startsWith("reloadvip")) {
                            String[] data = Char.split(msgChat, " ");
                            Database.instance.doGetAllCharVip(Integer.parseInt(data[1]));
                            return;
                        }

                        if (msgChat.startsWith("dendvip")) {
                            String[] data = Char.split(msgChat, " ");
                            Char.dayEndVip = Integer.parseInt(data[1]);
                            Char.timeEndVip = Integer.parseInt(data[2]);
                            Char.minuteEnd = Integer.parseInt(data[3]);
                        }

                        if (msgChat.startsWith("dvip")) {
                            String[] data = Char.split(msgChat, " ");
                            player.diemNapVip = Integer.parseInt(data[2]);
                            player.diemxaiVip = Integer.parseInt(data[3]);
                        }

                        if (msgChat.startsWith("rsvip")) {
                            String[] data = Char.split(msgChat, " ");
                            player.vip = Byte.parseByte(data[2]);
                            CharInfo cinfo = (CharInfo) ALL_CHAR_BUY_RUONG.get(player.charname);
                            if (cinfo != null) {
                                cinfo.luong = 0;
                                Database.instance.doUpdateCharBuyRuong(cinfo);
                            }

                            if (player.vip == 0) {
                                Database.instance.doresetCharVip(player.charname);
                                ALL_CHAR_VIP.remove(player.charname);
                            } else if (ALL_CHAR_VIP.get(player.charname) == null) {
                                Database.instance.doAddCharVip(player.charname, player.vip);
                                ALL_CHAR_VIP.put(player.charname, Integer.valueOf(player.vip));
                            } else {
                                ALL_CHAR_VIP.put(player.charname, Integer.valueOf(player.vip));
                                Database.instance.doUpdateCharVip(player.charname, player.vip);
                            }

                            player.calculateAttrib();
                            player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                            player.sendToNearPlayer(MessageCreator.createCharInfo(player));
                            return;
                        }

                        if (msgChat.startsWith("rslixi")) {
                            Char.resetLixi();
                            return;
                        }

                        if (msgChat.startsWith("shopnew")) {
                            String[] datax = Char.split(msgChat, " ");
                            player.sendMessage(MessageCreator.createMessageShopNew(player, player.iItems, datax[1], datax[2], 0));
                            return;
                        }

                        if (msgChat.startsWith("quacb")) {
                            String[] datax = Char.split(msgChat, " ");
                            player.doAddGifThungGoNuiChauBau(Integer.parseInt(datax[1]));
                            return;
                        }

                        if (msgChat.startsWith("loidai")) {
                            MapLoiDai.isStartLoiDai = true;
                            return;
                        }

                        if (msgChat.startsWith("tloidai")) {
                            String[] datax = Char.split(msgChat, " ");
                            MapLoiDai.HOUR_LOI_DAI = Integer.parseInt(datax[1]);
                            MapLoiDai.start_minute = Integer.parseInt(datax[2]);
                            return;
                        }

                        if (msgChat.startsWith("capcha")) {
                            player.stCapcha = MessageCreator.createMsgCapcha(player);
                        }

                        if (msgChat.startsWith("thead")) {
                            String[] datax = Char.split(msgChat, " ");
                            player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                            return;
                        }

                        if (msgChat.startsWith("ppkt")) {
                            try {
                                System.out.println("vao day ne");
                                ItemLuckyDraw.createtemCoat(742, player, Map.r.nextInt(2), true, 0);
                            } catch (Exception var13) {
                                var13.printStackTrace();
                            }

                            return;
                        }

                        if (msgChat.startsWith("pphv")) {
                            try {
                                ItemLuckyDraw.createtemCoat(750, player, Map.r.nextInt(2), true, 0);
                            } catch (Exception var14) {
                                var14.printStackTrace();
                            }

                            return;
                        }

                        if (msgChat.startsWith("chaubau")) {
                            try {
                                MapChauBau mapChauBau = (MapChauBau) RealController.mapList.get(42);
                                RegionNuiChauBau rg = mapChauBau.createNewRegion();
                                player.idRegionNuiChauBau = rg.idRegion;
                                mapChauBau.loadTru(rg, true, player.lvDetail.lv);
                                player.map.move2Map(player, 10, 10, 42, 0);
                            } catch (Exception var19) {
                                var19.printStackTrace();
                            }
                        }

                        if (msgChat.startsWith("bom")) {
                            player.doUseHoaDuoc();
                        }

                        if (msgChat.startsWith("chucnu")) {
                            player.doInitCharCopy();
                        }

                        

                        if (msgChat.startsWith("dkcc")) {
                            player.sendMessage(
                                    MessageCreator.createServerAlertMessage(
                                            "tong nguoi chien truong "
                                            + MapChienTruongMoba.v_all_char_chien_truong.size()
                                            + " > "
                                            + MapChienTruongMoba.all_char_chien_truong.size(),
                                            ""
                                    )
                            );
                            return;
                        }

                        if (msgChat.startsWith("bmb")) {
                            if (map.isMapChienTruongMoba()) {
                                String[] datax = Char.split(msgChat, " ");
                                map.doCreateBossCharCopy(player.region, Integer.parseInt(datax[1]));
                            }

                            return;
                        }

                        if (msgChat.startsWith("hoisinh")) {
                            player.sendMessage(MessageCreator.createMsgChat(player.id, "Hồi sinh sau 5s"));
                            return;
                        }

                        if (msgChat.startsWith("time")) {
                            player.sendMessage(MessageCreator.createMsgTimeCountdown("60", 10, -1, 0, COUNT_DOWN, 0));
                        }

                        if (msgChat.startsWith("hmoba")) {
                            String[] datax = Char.split(msgChat, " ");
                            MapChienTruongMoba.HOUR_ADMIN_CHEAT_START = Integer.parseInt(datax[1]);
                            return;
                        }

                        if (msgChat.startsWith("moba")) {
                            MapChienTruongMoba mapMoba = (MapChienTruongMoba) RealController.mapList.get(40);
                            mapMoba.doCheckCreateMatch(player);
                            return;
                        }

                        if (msgChat.startsWith("khu") && map.nRegion > 0) {
                            String[] menu = new String[player.map.nRegion];

                            for (int i = 0; i < player.map.nRegion; i++) {
                                menu[i] = "Khu " + (i + 1);
                            }

                            player.sendMessage(MessageCreator.createMsgMenu(menu, -34, 0));
                        }

                        if (msgChat.startsWith("mualai")) {
                            map.sendListItemSell2Shop(player);
                            return;
                        }

                        if (msgChat.startsWith("pget")) {
                            String[] datax = Char.split(msgChat, " ");
                            int sl = Integer.parseInt(datax[1]);
                            totaltrungget = sl;
                            Database.instance.saveEvent(event.getInfo());
                            return;
                        }

                        if (msgChat.startsWith("mpget")) {
                            String[] datax = Char.split(msgChat, " ");
                            int sl = Integer.parseInt(datax[1]);
                            totalxuathienvv = sl;
                            Database.instance.saveEvent(event.getInfo());
                            return;
                        }

                        if (msgChat.startsWith("thunggo")) {
                            player.doAddGiftThungGo();
                            return;
                        }

                        if (msgChat.startsWith("nguoituyet")) {
                            BossNguoiTuyet.doAddGift(player);
                            return;
                        }

                        if (msgChat.startsWith("orbb")) {
                            String[] datax = Char.split(msgChat, " ");
                            int sl = Integer.parseInt(datax[1]);
                            Char.total_open = sl;
                            return;
                        }

                        if (msgChat.startsWith("diemluong")) {
                            String[] datax = Char.split(msgChat, " ");
                            player.luongxainhanRuong = player.luongxainhanRuong + (long) Integer.parseInt(datax[1]);
                            return;
                        }

                        if (msgChat.startsWith("quadacbiet")) {
                            player.doDoiQuaDacBietNoel2020();
                        }

                        if (msgChat.startsWith("ruongmm")) {
                            doBuyRuongMayMan(player, 3);
                            return;
                        }

                        if (msgChat.startsWith("truongmm")) {
                            timeWaitSellRuongMM = 1L;
                            return;
                        }

                        if (msgChat.startsWith("att")) {
                            String[] datax = Char.split(msgChat, " ");

                            try {
                                Char p = CharManager.instance.getCharByCharName(datax[1]);
                                Item it = doBuyModelClothe(p, Integer.parseInt(datax[2]), 10, 1);
                                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
                                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 2));
                            } catch (Exception var15) {
                                var15.printStackTrace();
                            }

                            return;
                        }

                        if (msgChat.startsWith("sdh")) {
                            String[] datax = Char.split(msgChat, " ");
                            Char p = CharManager.instance.getCharByCharName(datax[1]);
                            p.addDanhHieu(Short.parseShort(datax[2]) + 8700, Long.parseLong(datax[3]));
                            p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
                            p.sendToNearPlayer(MessageCreator.createCharInfo(p));
                            return;
                        }

                        if (msgChat.startsWith("skp") && player.petUsing != null) {
                            String[] datax = Char.split(msgChat, " ");
                            player.petUsing.doLearSkillTest(Integer.parseInt(datax[1]));
                            player.calculateAttrib();
                            player.sendMessage(MessageCreator.createCharWearingMessage(player, player));
                            player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                            player.sendToNearPlayer(MessageCreator.createCharInfo(player));
                        }

                        if (msgChat.startsWith("ngocruong")) {
                            String[] datax = Char.split(msgChat, " ");
                            String info = "vi tri ngoc: ";
                            Vector<Monster> allMonsterNgoc = all_ngoc_rong_hac_ho;
                            if (Integer.parseInt(datax[1]) == 0) {
                                allMonsterNgoc = all_ngoc_rong_thanh_long;
                            }

                            System.out.println(msgChat + " ngoc ruong ne: " + allMonsterNgoc.size());

                            for (int i = 0; i < allMonsterNgoc.size(); i++) {
                                if (!((Monster) allMonsterNgoc.get(i)).isDead) {
                                    info = info
                                            + "M: "
                                            + ((Monster) allMonsterNgoc.get(i)).map.mapId
                                            + " vt: "
                                            + ((Monster) allMonsterNgoc.get(i)).x / 16
                                            + " , "
                                            + ((Monster) allMonsterNgoc.get(i)).y / 16
                                            + " > ";
                                }

                                System.out
                                        .println(
                                                ((Monster) allMonsterNgoc.get(i)).getName()
                                                + " M: "
                                                + ((Monster) allMonsterNgoc.get(i)).map.mapId
                                                + " vt: "
                                                + ((Monster) allMonsterNgoc.get(i)).x / 16
                                                + " , "
                                                + ((Monster) allMonsterNgoc.get(i)).y / 16
                                        );
                            }

                            player.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
                            return;
                        }

                        if (msgChat.startsWith("poro")) {
                            String[] datax = Char.split(msgChat, " ");
                            player.createPoro((long) Integer.parseInt(datax[1]), true);
                            return;
                        }

                        if (msgChat.startsWith("lan")) {
                            String[] datax = Char.split(msgChat, " ");
                            player.createLan((long) Integer.parseInt(datax[1]), true, 0);
                            return;
                        }

                        if (msgChat.startsWith("heo")) {
                            String[] datax = Char.split(msgChat, " ");
                            player.createHeo((long) Integer.parseInt(datax[1]), true);
                            return;
                        }

                        if (msgChat.startsWith("ngua")) {
                            String[] datax = Char.split(msgChat, " ");
                            player.createNguaXuong((long) Integer.parseInt(datax[1]), true);
                            return;
                        }

                        if (msgChat.startsWith("bachthu")) {
                            String[] datax = Char.split(msgChat, " ");
                            player.doCreatePetBachThu(Integer.parseInt(datax[1]), true);
                            return;
                        }

                        if (msgChat.startsWith("ngoc")) {
                            String[] datax = Char.split(msgChat, " ");
                            map.doAddNgocRongTest(player, Integer.parseInt(datax[1]));
                            return;
                        }

                        if (msgChat.startsWith("btest")) {
                            String[] datax = Char.split(msgChat, " ");
                            map.doAddBossTest(player, Integer.parseInt(datax[1]));
                            return;
                        }

                        if (msgChat.startsWith("ruong")) {
                            map.doAddRuongMaQuai(player);
                            return;
                        }

                        if (msgChat.startsWith("thunggo")) {
                            map.doAddThungGo(player);
                            return;
                        }

                        if (msgChat.startsWith("bid")) {
                            String[] info = Char.split(msgChat, " ");
                            int money = Integer.parseInt(info[1]);
                            Database.instance.doAddCharDauGia(player, money);
                            player.sendMessage(MessageCreator.createCharWearingMessage(player, player));
                            player.sendMessage(
                                    MessageCreator.createMsgPopUp(
                                            0,
                                            0,
                                            "Bạn có muốn đấu giá mua chổi với giá "
                                            + money
                                            + " lượng không?. Bạn sẽ phải đặt cọc với số lượng là "
                                            + money / 2
                                            + " lượng. Nếu bạn chiến thắng mà không mua CHổi, bạn sẽ mất tiền cọc là "
                                            + money / 2
                                            + " lượng."
                                    )
                            );
                            return;
                        }

                        if (msgChat.startsWith("head")) {
                            String[] info = Char.split(msgChat, " ");
                            player.headModel = Byte.parseByte(info[1]);
                            System.out.println("doi sang toc: " + player.headModel);
                            player.sendMessage(MessageCreator.createCharWearingMessage(player, player));
                            return;
                        }

                        if (msgChat.startsWith("setmu")) {
                            player.khamEff[0] = System.currentTimeMillis() / 1000L;
                            player.sendMessage(player.sendEffKham(player));
                        }

                        if (msgChat.startsWith("trurong")) {
                            for (int i = 0; i < MapTown.allBossTruRong.size(); i++) {
                                try {
                                    ((BossTruRong) MapTown.allBossTruRong.get(i)).dosendMoveAll(true);
                                } catch (Exception var18) {
                                }
                            }
                        }

                   

                        if (msgChat.startsWith("test gamecanvas")) {
                            GameCanvas.test = "Bắt đầu test game canvas nào " + GameCanvas.count++;
                            TeamServer.gameCanvas.repaint();
                        }

                        if (msgChat.startsWith("class")) {
                            player.charClass = (byte) ((player.charClass + 1) % 5);
                            player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                        }

                        if (msgChat.startsWith("lulan")) {
                            player.isLuLan = !player.isLuLan;
                        }

                        if (msgChat.startsWith("gaumeo")) {
                            player.setNguoiTuyet(6);
                        }

                        if (msgChat.startsWith("liendau")) {
                            istestliendau = !istestliendau;
                        }

                        if (msgChat.startsWith("esk")) {
                            String[] info = Char.split(msgChat.substring(4), " ");
                            Char p = CharManager.instance.getCharByCharName(info[0]);
                            int idEff = Integer.parseInt(info[1]);
                            int timeExist = Integer.parseInt(info[2]);
                            System.out.println("tao eff: " + idEff + " >> " + timeExist);
                            p.sendMessage(MessageCreator.createMsgNewEffectSkill5Long(idEff, timeExist, p.id, p.cat, false, false));
                            return;
                        }

                        if (msgChat.startsWith("spe")) {
                            String[] info = Char.split(msgChat.substring(4), " ");
                            Char p = CharManager.instance.getCharByCharName(info[0]);
                            long exp = LevelPet.getPCExp(Integer.parseInt(info[1]), p.petUsing.lvDetail.lv);
                            if (p.petUsing.doEat(new long[]{exp, 1L}, p)) {
                                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                player.itemPetEat.removeAllElements();
                                Database.instance.savePet(player.petUsing);
                            }

                            return;
                        }

                        if (msgChat.startsWith("apet")) {
                            int[] idpet = new int[]{693, 698, 708, 703, 713};
                            int[] idtrungpet = new int[]{684, 685, 687, 686, 688};
                            player.doCreatePetMax(idpet[indexPet], 5, idtrungpet[indexPet], 1008000000);
                            indexPet = (indexPet + 1) % idpet.length;
                            return;
                        }

                        if (msgChat.startsWith("hkl")) {
                            String[] info = Char.split(msgChat.substring(4), " ");
                            Char p = CharManager.instance.getCharByCharName(info[0]);
                            map.doCreateHoaKyLan(p, 683, 43200L);
                            return;
                        }

                        if (msgChat.startsWith("qua")) {
                            String[] info = Char.split(msgChat.substring(4), " ");
                            Char p = CharManager.instance.getCharByCharName(info[0]);
                            p.canReceiveGiftOffline = Byte.parseByte(info[1]);
                            return;
                        }
                    }

                    if (player.isAdmin && msgChat.startsWith("ef5")) {
                        String[] a = Char.split(msgChat, " ");
                        int idEff = Integer.parseInt(a[1]) + 8700;
                        int time = Integer.parseInt(a[2]);
                        if (player.addEffBuff(idEff, System.currentTimeMillis() + (long) time, EffectBuff.BY_ACTOR, 0) != null) {
                            try {
                                Message mef = MessageCreator.createMsgNewEffectSkill5Long(idEff, time, player.id, player.cat, true, false);
                                player.sendMessage(mef);
                                player.sendToNearPlayer(mef);
                            } catch (Exception var17) {
                                var17.printStackTrace();
                            }
                        }
                    }

                    if (player.isAdmin && msgChat.startsWith("pet")) {
                        int[] idpet = new int[]{EffectBuff.RU_NGU, EffectBuff.BONG, EffectBuff.CHOANG, EffectBuff.TRUNG_DOC, EffectBuff.SUY_YEU};
                        player.huyEff(player, idpet[indexPet]);
                        indexPet = (indexPet + 1) % idpet.length;
                        System.out.println("EFF: " + idpet[indexPet]);
                        if (player.addEffBuff(idpet[indexPet], System.currentTimeMillis() + 30000L, EffectBuff.BY_ACTOR, 0) != null) {
                            player.sendEffToChar(player);
                        }
                    }

             

                    if (player.isAdmin && msgChat.startsWith("eff")) {
                        int idEff = Integer.parseInt(msgChat.substring(4));
                        if (player.addEffBuff(idEff, System.currentTimeMillis() + 30000L, EffectBuff.BY_ACTOR, 0) != null) {
                            player.sendEffToChar(player);
                            System.out.println("add vao char ne");
                        } else {
                            System.out.println("null roi");
                        }
                    }
                    

                    if ((player.isAdmin || TeamServer.isServerLocal() || TeamServer.PORT == 19150) && msgChat.startsWith("die")) {
                        player.hp = 0;
                        player.actorDie();
                        player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                    }

                    if (msgChat.startsWith("tnb") && player.isAdmin) {
                        Cooking.timeNauBanh = Byte.parseByte(msgChat.substring(4));
                    } else if (msgChat.startsWith("stnb") && player.isAdmin) {
                        Char.nauBanhChung.timeCook = Byte.parseByte(msgChat.substring(5));
                        Char.nauBanhTet.timeCook = Byte.parseByte(msgChat.substring(5));
                    } else if (msgChat.startsWith("ecook") && player.isAdmin) {
                        Char.nauBanhChung.isAdminEnd = true;
                        Char.nauBanhTet.isAdminEnd = true;
                    } else if (msgChat.startsWith("scook") && player.isAdmin) {
                        Char.nauBanhChung.isAdminStart = true;
                        Char.nauBanhTet.isAdminStart = true;
                    } else if (!msgChat.startsWith("camchat")
                            || (!player.getSession().username.toLowerCase().equals("kongpeo")
                            && !player.getSession().username.toLowerCase().equals("phieubatgiangho6789")
                            && !player.getSession().username.toLowerCase().equals("staynight")
                            || !Map.openLog)
                            && !player.isAdmin) {
                        if (player.isAdmin || Char.getDayTime(0L).compareTo(player.dayCamChat) >= 0) {
                            if (player.isAdmin) {
                                if (msgChat.startsWith("tdon")) {
                                    isThamDinh = true;
                                    Message mm = new Message(123);
                                    mm.dos.writeByte(1);
                                    Map.sendAllCharServer(-1, mm);
                                    return;
                                }

                                if (msgChat.startsWith("tdoff")) {
                                    isThamDinh = false;
                                    Message mm = new Message(123);
                                    mm.dos.writeByte(0);
                                    Map.sendAllCharServer(-1, mm);
                                    return;
                                }
                            }

                            if (msgChat.startsWith("addboss")) {
                                try {
                                    if (player.isAdmin) {
                                        int id = Integer.parseInt(msgChat.substring(8, msgChat.length()));
                                        System.out.println("THEM BOSS " + id);
                                        Monster msg = new BossTruRong(map, (MonsterTemplate) monsterTemplates.get(id), player.x, player.y, player.inCountry);
                                        msg.level = msg.getMonsterTemplate().level;
                                        msg.id = RealController.intance.idGen.getID(1, "new monster");
                                        byte[] he = new byte[]{0, 1, 2, 3, 4};
                                        msg.he = msg.heKhiSinh(he[Map.r.nextInt(5)]);
                                        byte[] t = new byte[]{0, 1, 0, 1, 0, 1, 0, 0, 0, 1, 0};
                                        msg.typeAttack = t[Map.r.nextInt(10)];
                                        msg.bornTime = 120000L;
                                        map.addMonster(msg, player.inCountry, player.region);
                                    }
                                } catch (Exception var16) {
                                    var16.printStackTrace();
                                }
                            } else if (msgChat.startsWith("market")) {
                                if (player.isAdmin) {
                                    player.sendMessage(
                                            MessageCreator.createServerAlertMessage(
                                                    "tong luot ban: " + MapTown.count_all_item_sell_per_day + " , tong item ban: " + ItemSell.ID_ITEM_SELL_AUTO_INCREMENT, ""
                                            )
                                    );
                                }
                            } else if (msgChat.startsWith("dieall")) {
                                if (player.isAdmin) {
                                    map.setDieAll(player);
                                }
                            } else if (msgChat.startsWith("tmove")) {
                                if (player.isAdmin) {
                                    testMove = true;
                                }
                            } else if (msgChat.startsWith("fmove")) {
                                if (player.isAdmin) {
                                    testMove = false;
                                }
                            } else if (msgChat.startsWith("resetblk")) {
                                if (player.isAdmin) {
                                    TeamServer.ALL_IPCONNECT_BLOCK.clear();
                                }
                            } else if (msgChat.endsWith("vtt") && player.isAdmin) {
                                map.playerExit(player);
                                player.region = Integer.parseInt(msgChat.substring(0, msgChat.indexOf("vtt")));
                                map.move2Map(player, player.x / 16, player.y / 16, player.mapID, player.inCountry);
                            } else if (msgChat.startsWith("tyt") && player.isAdmin) {
                                MapVanTienTran.startByAdmin = !MapVanTienTran.startByAdmin;
                            } else if (msgChat.startsWith("wph") && (player.isAdmin || TeamServer.isServerLocal())) {
                                String[] info = Char.split(msgChat.substring(4), " ");
                                Char p = CharManager.instance.getCharByCharName(info[0]);
                                p.winPH = 0;
                            } else if (msgChat.startsWith("sto") && player.isAdmin) {
                                String[] info = Char.split(msgChat.substring(4), " ");
                                Char p = CharManager.instance.getCharByCharName(info[0]);
                                p.timeOnlineSukien = System.currentTimeMillis() - 3540000L - 57000L;
                            } else if (msgChat.startsWith("sph") && player.isAdmin) {
                                String[] info = Char.split(msgChat.substring(4), " ");
                                nPhuongHoang = Byte.parseByte(info[0]);
                                max_phuong_hoang = Byte.parseByte(info[1]);
                                MAX_QUAY = Integer.parseInt(info[2]);
                                countQuaySo = Integer.parseInt(info[3]);
                                System.out.println("");
                                System.out
                                        .println(
                                                "thong tin qua ph: nph: " + nPhuongHoang + " : maxph: " + max_phuong_hoang + " : maxquay: " + MAX_QUAY + " >> " + countQuaySo
                                        );
                            } else if (msgChat.startsWith("tph") && player.isAdmin) {
                                String[] info = Char.split(msgChat.substring(4), " ");
                                int minute = Integer.parseInt(info[0]);
                                Char.timeGetPhuongHoang = System.currentTimeMillis() + (long) (minute * 60000);
                            } else if (msgChat.startsWith("fna") && player.isAdmin) {
                                String[] info = Char.split(msgChat.substring(4), " ");
                                Char p = CharManager.instance.getCharByCharName(info[0]);
                                p.firstNapMoney = 0;
                            } else if (msgChat.startsWith("loadx2") && player.isAdmin) {
                                Database.instance.loadTimeX2Server();
                            } else {
                                if (msgChat.startsWith("die")) {
                                    String[] info = Char.split(msgChat.substring(4), " ");
                                    Char p = CharManager.instance.getCharByCharName(info[0]);
                                    if (p != null) {
                                        p.hp = 0;
                                        p.actorDie();
                                    }
                                }

                                if (msgChat.startsWith("sct")) {
                                    if (player.isAdmin) {
                                        String[] info = Char.split(msgChat.substring(4), " ");
                                        Char p = CharManager.instance.getCharByCharName(info[0]);
                                        if (p != null) {
                                            p.myCountry = Byte.parseByte(info[1]);
                                            p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
                                            p.sendToNearPlayer(MessageCreator.createCharInfo(p));
                                        }
                                    }
                                } else if (msgChat.startsWith("scl")) {
                                    if (player.isAdmin) {
                                        String[] info = Char.split(msgChat.substring(4), " ");
                                        Char p = CharManager.instance.getCharByCharName(info[0]);
                                        if (p != null) {
                                            p.idClan = Short.parseShort(info[1]);
                                            p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
                                            p.sendToNearPlayer(MessageCreator.createCharInfo(p));
                                        }
                                    }
                                } else if (msgChat.startsWith("pet")) {
                                    if (player.isAdmin) {
                                        String[] info = Char.split(msgChat.substring(4), " ");
                                        Char p = CharManager.instance.getCharByCharName(info[0]);
                                        if (p != null) {
                                            int idpet = Integer.parseInt(info[1]);
                                            int minute = Integer.parseInt(info[2]);
                                            p.createPet(idpet, minute);
                                        }
                                    }
                                } else if (msgChat.startsWith("clear")) {
                                    if (player.isAdmin || TeamServer.isServerLocal()) {
                                        String[] info = Char.split(msgChat.substring(6), " ");
                                        Char p = CharManager.instance.getCharByCharName(info[0]);
                                        if (p != null) {
                                            int type = Integer.parseInt(info[1]);
                                            if (type == 0) {
                                                for (int i = 0; i < p.potions.length; i++) {
                                                    p.potions[i] = 0;
                                                }

                                                p.calculateAttrib();
                                                p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
                                                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                                            } else if (type == 1) {
                                                p.iItems.removeAllElements();
                                                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
                                            } else if (type == 2) {
                                                for (int i = 0; i < p.listGemitem.length; i++) {
                                                    if (p.listGemitem[i] > 0) {
                                                        p.delGem(i, p.listGemitem[i], false);
                                                    }
                                                }

                                                for (int ix = 0; ix < p.listGemitemLock.length; ix++) {
                                                    if (p.listGemitemLock[ix] > 0) {
                                                        p.delGem(ix, p.listGemitemLock[ix], true);
                                                    }
                                                }

                                                p.sendMessage(MessageCreator.createCharGemItem(p));
                                            }
                                        }
                                    }
                                } else if (msgChat.startsWith("slv")) {
                                    if (player.isAdmin) {
                                        String[] info = Char.split(msgChat.substring(4), " ");
                                        Char p = CharManager.instance.getCharByCharName(info[0]);
                                        if (p != null) {
                                            long exp = LevelDetail.getXpFromLevel(Integer.parseInt(info[1]));
                                            long xeppc = LevelDetail.getPCExp(Integer.parseInt(info[2]), Integer.parseInt(info[1]));
                                            p.lvDetail.setExpNew(exp + xeppc);
                                            p.lastLV = (short) p.lvDetail.lv;
                                            // Che do Pokemon: GM dat cap thi tien hoa theo cap moi luon.
                                            TienHoaPokemon.kiemTienHoaTheoCap(p);
                                            p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
                                            p.sendToNearPlayer(MessageCreator.createCharInfo(p));
                                            Database.instance
                                                    .saveOrtherLog(
                                                            "", p.charname, player.charname + " set xp cho " + p.charname + "len level " + info[1] + " pt " + info[2], "adminset"
                                                    );
                                        }
                                    }
                                } else if (msgChat.startsWith("quayso")) {
                                    if (player.isAdmin && Database.all_char_top.size() == 0) {
                                        Database.readText("top.txt");
                                        player.sendMessage(MessageCreator.createMsgChat(player.id, "Tong so char tham gia quay la " + Database.all_char_top.size()));
                                    }
                                } else {
                                    if (msgChat.startsWith("stk")) {
                                        String[] info = Char.split(msgChat.substring(4), " ");
                                        MapTongKim.hourStart = Byte.parseByte(info[0]);
                                        MapTongKim.minuteStart = Byte.parseByte(info[1]);
                                    }

                                    if (msgChat.startsWith("smn")) {
                                        if (player.isAdmin) {
                                            String[] info = Char.split(msgChat.substring(4), " ");
                                            Char p = CharManager.instance.getCharByCharName(info[0]);
                                            if (p != null) {
                                                long xu = Long.parseLong(info[1]);
                                                long luong = Long.parseLong(info[2]);
                                                long luongl = Long.parseLong(info[3]);
                                                p.addXu(xu, "map 83");
                                                p.addLuong((int) luong);
                                                p.addLuongLock((int) luongl);
                                                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                                            }
                                        }
                                    } else if (msgChat.startsWith("spt")) {
                                        if (player.isAdmin || TeamServer.isServerLocal()) {
                                            String[] info = Char.split(msgChat.substring(4), " ");
                                            Char p = CharManager.instance.getCharByCharName(info[0]);
                                            if (p != null) {
                                                int idGem = Integer.parseInt(info[1]);
                                                int soluong = Integer.parseInt(info[2]);
                                                p.potions[idGem] = p.potions[idGem] + soluong;
                                                p.calculateAttrib();
                                                p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
                                                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                                            }
                                        }
                                    } else if (msgChat.startsWith("anla")) {
                                        if (player.isAdmin) {
                                            String[] info = Char.split(msgChat.substring(5), " ");
                                            Char p = CharManager.instance.getCharByCharName(info[0]);
                                            int soluong = Integer.parseInt(info[1]);
                                            if (p != null) {
                                                for (int ixx = 0; ixx < GemTemplate.ID_MATERIAL_HIGHT[5].length; ixx++) {
                                                    p.doAddGemItem(GemTemplate.ID_MATERIAL_HIGHT[5][ixx], soluong, false);
                                                    p.doAddGemItem(GemTemplate.ID_MATERIAL_LOW[5][ixx], soluong, false);
                                                }

                                                p.sendMessage(MessageCreator.createCharGemItem(p));
                                            }
                                        }
                                    } else if (msgChat.startsWith("anl")) {
                                        if (player.isAdmin || TeamServer.isServerLocal()) {
                                            String[] info = Char.split(msgChat.substring(4), " ");
                                            Char p = CharManager.instance.getCharByCharName(info[0]);
                                            if (p != null) {
                                                int idGem = Integer.parseInt(info[1]);
                                                int soluong = Integer.parseInt(info[2]);
                                                boolean lock = Integer.parseInt(info[3]) == 1;
                                                p.doAddGemItem(idGem, soluong, lock);
                                                p.sendMessage(MessageCreator.createCharGemItem(p));
                                            }
                                        }
                                    } else {
                                        if (msgChat.startsWith("gem") && (player.isAdmin || TeamServer.isServerLocal())) {
                                            String[] info = Char.split(msgChat.substring(4), " ");
                                            Char p = CharManager.instance.getCharByCharName(info[0]);
                                            if (p != null) {
                                                int idGem = Integer.parseInt(info[1]);
                                                int sl = Integer.parseInt(info[2]);
                                                boolean lock = Integer.parseInt(info[3]) == 1;
                                                p.doAddGemItem(idGem, sl, lock);
                                                p.sendMessage(MessageCreator.createCharGemItem(p));
                                            }
                                        }

                                        if (msgChat.startsWith("ait")) {
                                            if (player.isAdmin || TeamServer.isServerLocal()) {
                                                String[] info = Char.split(msgChat.substring(4), " ");
                                                Char p = CharManager.instance.getCharByCharName(info[0]);
                                                if (p != null) {
                                                    int iditem = Integer.parseInt(info[1]);
                                                    int clazz = Integer.parseInt(info[2]);
                                                    int magic_vl = Integer.parseInt(info[3]);
                                                    int minute = Integer.parseInt(info[4]);
                                                    ItemTemplates template = (ItemTemplates) ((Hashtable) itemTemplates.get(clazz)).get(new Integer(iditem));
                                                    Item newItem = null;
                                                    newItem = new Item(template, false, clazz, clazz, template.id);
                                                    newItem.id = p.getIDItem();
                                                    newItem.owner = p.charDBID;
                                                    newItem.level = newItem.getTemplate().level;
                                                    p.iItems.add(newItem);
                                                    newItem.identify = p.charDBID;
                                                    newItem.clazz = (byte) clazz;
                                                    if (template.type < 3 || template.type == 10 || template.type == 11) {
                                                        newItem.magic_physic = p.typeItemBuy;
                                                        if (magic_vl == 0) {
                                                            newItem.atb[6] = newItem.atb[1];
                                                            newItem.atb[1] = (short) (newItem.atb[1] / 10);
                                                        } else if (magic_vl == 1) {
                                                            newItem.atb[6] = (short) (newItem.atb[1] / 10);
                                                        }
                                                    }

                                                    if (OfflineMap.isAnimalItem(template.type)) {
                                                        newItem.magic_physic = (byte) magic_vl;
                                                        if (newItem.magic_physic < 0) {
                                                            newItem.magic_physic = 2;
                                                        }

                                                        if (magic_vl == 0) {
                                                            newItem.atb[6] = newItem.atb[1];
                                                            newItem.atb[1] = (short) (newItem.atb[1] / 10);
                                                        } else if (magic_vl == 1) {
                                                            newItem.atb[6] = (short) (newItem.atb[1] / 10);
                                                        }
                                                    }

                                                    if (minute > 0) {
                                                        newItem.timeLoan = System.currentTimeMillis();
                                                        newItem.minuteExist = minute;
                                                    }

                                                    if (isItemThanThu(template.type)) {
                                                        newItem.doAddOptionThanThu(template.atb[0]);
                                                    }

                                                    if (newItem.isVukhiThoiTrang()) {
                                                        newItem.createAttributeVuKhiThoiTrang();
                                                    }

                                                    newItem.createAttChoi();
                                                    newItem.dateCreate = Char.getDayTime(0L);
                                                    p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
                                                }
                                            }
                                        } else if (msgChat.startsWith("sqg")) {
                                            if (player.isAdmin) {
                                                String[] info = Char.split(msgChat.substring(4), " ");
                                                Char p = CharManager.instance.getCharByCharName(info[0]);
                                                if (p != null) {
                                                    p.myCountry = Byte.parseByte(info[1]);
                                                    p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
                                                    p.sendToNearPlayer(MessageCreator.createCharInfo(p));
                                                }
                                            }
                                        } else if (msgChat.startsWith("oske") && player.isAdmin) {
                                            map.onOffSkelonton(player.region, true);
                                            map.reSetAllMonster(player.inCountry, player.region);
                                            map.killAllMonster(map.monsters, player.inCountry, player.region);
                                        } else if (msgChat.startsWith("ofske") && player.isAdmin) {
                                            map.onOffSkelonton(player.region, false);
                                            map.killAllMonster(map.temp_monsters, player.inCountry, player.region);
                                            ((Hashtable) map.temp_monsters.get(player.inCountry)).clear();
                                        } else if (msgChat.startsWith("war") && player.isAdmin) {
                                            String[] info = Char.split(msgChat, " ");
                                            MapTown.timeStart = Integer.parseInt(info[1]);
                                            MapTown.minuteStart = Integer.parseInt(info[2]);
                                            map.startWar();
                                        } else if (msgChat.startsWith("war2") && player.isAdmin) {
                                            MapTown.isSkipDayCheck = !MapTown.isSkipDayCheck;
                                            String status = MapTown.isSkipDayCheck ? "Bật" : "Tắt";
                                            player.sendMessage(MessageCreator.createServerAlertMessage(
                                                "Đã " + status + " chế độ bỏ qua kiểm tra ngày đăng ký tấn công", 
                                                ""
                                            ));
                                        } else if (msgChat.startsWith("gvantieu") && player.isAdmin) {
                                            
                                        } else if (msgChat.startsWith("gvantieu") && player.isAdmin) {
                                            String info = "";

                                            for (int ixx = 0; ixx < 5; ixx++) {
                                                info = info + getLogVantieu(ixx) + "_";
                                            }

                                            player.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
                                        } else if (msgChat.startsWith("ovantieu") && player.isAdmin) {
                                            openvantieu = true;
                                        } else if (msgChat.startsWith("offvantieu") && player.isAdmin) {
                                            openvantieu = false;
                                        } else {
                                            if (msgChat.startsWith("ondh") && player.isAdmin) {
                                                dropHoaHong = true;
                                            }

                                            if (msgChat.startsWith("offdh") && player.isAdmin) {
                                                dropHoaHong = false;
                                            }

                                            if (msgChat.startsWith("mons") && player.isAdmin) {
                                                String[] info = Char.split(msgChat, " ");
                                                timeEventMonster = (byte) Integer.parseInt(info[1]);
                                                minuteEventMons = (byte) Integer.parseInt(info[2]);
                                                
                                            } else if (msgChat.startsWith("wedding") && player.isAdmin) {
                                                map.startWedding();
                                            } else if (msgChat.startsWith("onchedo") && player.isAdmin) {
                                                okChedo = true;
                                            } else if (msgChat.startsWith("offchedo") && player.isAdmin) {
                                                okChedo = false;
                                            } else if (msgChat.startsWith("onsound")) {
                                                if (player.isAdmin) {
                                                    onOffSound = true;
                                                    Map.sendAllCharServer(-1, MessageCreator.createMsgSound(1));
                                                }
                                            } else if (msgChat.startsWith("settown")) {
                                                if (player.isAdmin) {
                                                    short idClan = Short.parseShort(msgChat.substring(8).trim());
                                                    NewClan clan = NewClan.getClan(idClan);
                                                    if (clan != null) {
                                                        Map.idClanTown[clan.country] = idClan;
                                                        NewClan.updateTown(idClan, 1, clan.country);
                                                        resetShield(clan.country);
                                                    }
                                                }
                                            } else if (msgChat.startsWith("offtown")) {
                                                if (player.isAdmin) {
                                                    short idClan = Short.parseShort(msgChat.substring(8).trim());
                                                    NewClan clan = NewClan.getClan(idClan);
                                                    if (clan != null) {
                                                        Map.idClanTown[clan.country] = -1;
                                                        NewClan.updateTown(idClan, 0, clan.country);
                                                        resetShield(clan.country);
                                                    }
                                                }
                                             } else if (msgChat.startsWith("setcuoptieu") && player.isAdmin) {
                                                String[] info = Char.split(msgChat, " ");
                                                player.cuop_tieu = Byte.parseByte(info[1]);
                                                player.sendMessage(MessageCreator.createServerAlertMessage("Đã set cuộc tấn công cho " + player.cuop_tieu, ""));
                                            } else if (msgChat.startsWith("stown") && player.isAdmin) {
                                                    // stown 0                      -> tra ve lich chuan (thu 3 + thu 7, 21h)
                                                    // stown 1 <gio> <phut>          -> chiem thanh moi ngay vao gio nay
                                                    // stown 1 <gio> <phut> <ngay>   -> ngay = yyyy-MM-dd hoac Mon..Sun
                                                    try {
                                                        String[] info = Char.split(msgChat, " ");
                                                        int set = Integer.parseInt(info[1]);
                                                        if (set == 0) {
                                                            MapTown.TimeAdminCheat = -1;
                                                            MapTown.minuteAdminCheat = -1;
                                                            MapTown.dayAdminCheat = "";
                                                            server.CauHinh.luuGioChiemThanh();
                                                            player.sendMessage(MessageCreator.createServerAlertMessage("Đã trả chiếm Gym về lịch chuẩn: 21h thứ 3 và thứ 7.\n(đã lưu vào server.ini)", ""));
                                                        } else {
                                                            MapTown.TimeAdminCheat = Integer.parseInt(info[2]);
                                                            MapTown.minuteAdminCheat = Integer.parseInt(info[3]);
                                                            MapTown.dayAdminCheat = info.length > 4 ? info[4].trim() : "";
                                                            server.CauHinh.luuGioChiemThanh();
                                                            player.sendMessage(MessageCreator.createServerAlertMessage(
                                                                    "Đã set chiếm Gym " + MapTown.TimeAdminCheat + "h" + MapTown.minuteAdminCheat
                                                                            + (MapTown.dayAdminCheat.isEmpty() ? " mỗi ngày." : " ngày " + MapTown.dayAdminCheat + ".")
                                                                            + "\n(đã lưu vào server.ini)", ""));
                                                        }
                                                    } catch (Exception exStown) {
                                                        player.sendMessage(MessageCreator.createServerAlertMessage(
                                                                "Cú pháp: stown 1 <giờ> <phút> [yyyy-MM-dd|Mon..Sun] hoặc stown 0 để tắt.", ""));
                                                    }
                                                    return;
                                            } else if (msgChat.startsWith("ktct") && player.isAdmin) {
                                                    if (player.isAdmin) {
                                                        MapTown.TimeAdminCheat = -1;
                                                        MapTown.minuteAdminCheat = -1;
                                                        MapTown.dayAdminCheat = "";
                                                        MapTown.gameOverByAdmin = -1;
                                                        server.CauHinh.luuGioChiemThanh();
                                                    }
                                            } else if (msgChat.startsWith("tgct") && player.isAdmin) {
                                                    if (player.isAdmin) {
                                                        String thongtin = MapTown.TimeAdminCheat < 0
                                                                ? "Chiếm Gym đang theo lịch chuẩn: 21h thứ 3 và thứ 7."
                                                                : "Thời gian chiếm Gym " + MapTown.TimeAdminCheat + " giờ " + MapTown.minuteAdminCheat + " phút"
                                                                        + (MapTown.dayAdminCheat.isEmpty() ? ", áp dụng mỗi ngày." : ", ngày " + MapTown.dayAdminCheat + ".");
                                                        player.sendMessage(MessageCreator.createServerAlertMessage(thongtin, ""));
                                                    }

                                            } else if (msgChat.startsWith("over")) {
                                                if (player.isAdmin) {
                                                    try {
                                                        // Lấy country từ lệnh chat (over0, over1, over2)
                                                        int country = Integer.parseInt(msgChat.substring(4));
                                                        if (country >= 0 && country <= 2) { // Kiểm tra country hợp lệ (0-2)
                                                            MapTown.gameOverByAdmin = country;
                                                            player.sendMessage(MessageCreator.createServerAlertMessage("Đã kết thúc chiến Gym cho vùng " + country, ""));
                                                        }
                                                    } catch (Exception e) {
                                                        player.sendMessage(MessageCreator.createServerAlertMessage("Sai cú pháp. Sử dụng: over[0-2]", ""));
                                                    }
                                                }
                                            
                                            } else if (msgChat.startsWith("over0")) {
                                                if (player.isAdmin) {
                                                    MapTown.gameOverByAdmin = 0;
                                                }
                                          
                                            } else if (msgChat.startsWith("over1")) {
                                                if (player.isAdmin) {
                                                    MapTown.gameOverByAdmin = 1;
                                                }
                                            } else if (msgChat.startsWith("bd0")) {
                                                if (player.isAdmin) {
                                                    map.startFight(0);
                                                }
                                            } else if (msgChat.startsWith("bd1")) {
                                                if (player.isAdmin) {
                                                    map.startFight(1);
                                                }
                                            } else if (msgChat.startsWith("bd")) {
                                                if (player.isAdmin) {
                                                    
                                                    map.startFight();
                                                }
                                         
                                            } else if (msgChat.startsWith("badw")) {
                                                if (player.isAdmin) {
                                                    Database.instance.loadBadWords();
                                                }
                                            } else if (msgChat.startsWith("txp")) {
                                                if (player.isAdmin) {
                                                    checkRcvXP = !checkRcvXP;
                                                    if (checkRcvXP) {
                                                        player.sendMessage(MessageCreator.createServerAlertMessage("đã bật chế độ tính thời gian chơi", ""));
                                                    } else {
                                                        player.sendMessage(MessageCreator.createServerAlertMessage("đã tắt chế độ tính thời gian chơi", ""));
                                                    }
                                                }
                                            } else if (msgChat.startsWith("offsound")) {
                                                if (player.isAdmin) {
                                                    onOffSound = false;
                                                    Map.sendAllCharServer(-1, MessageCreator.createMsgSound(0));
                                                }
                                            } else if (msgChat.startsWith("openlog")) {
                                                if (player.isAdmin) {
                                                    Map.openLog = true;
                                                }
                                            } else if (msgChat.startsWith("offw")) {
                                                if (player.isAdmin) {
                                                    timeWait = System.currentTimeMillis() + 900000L;
                                                    weather = null;
                                                    Map.sendAllCharServer(-1, MessageCreator.createMsgWeather(-1, 0, 0));
                                                }
                                            } else if (msgChat.startsWith("setw")) {
                                                if (player.isAdmin) {
                                                    timeWait = System.currentTimeMillis() + 600000L;
                                                    int id = Integer.parseInt(msgChat.substring(4));
                                                    weather = new Weather();
                                                    weather.id = (byte) id;
                                                    weather.n = 50;
                                                    weather.time = (short) (id == 2 ? 50 : 5);
                                                    Map.sendAllCharServer(-1, MessageCreator.createMsgWeather(id, 50, id == 2 ? 3000000 : 300000));
                                                }
                                            } else if (msgChat.startsWith("onghost") && player.isAdmin) {
                                                loadGhost = true;
                                            } else if (msgChat.startsWith("offghost") && player.isAdmin) {
                                                loadGhost = false;
                                            } else if (msgChat.startsWith("openboss")) {
                                                if (player.isAdmin) {
                                                    if (msgChat.startsWith("openboss1")) {
                                                        ((Boss) boss.get(0)).isOpen = true;
                                                        ((Boss) boss.get(0)).bornTime = System.currentTimeMillis() + 30000L;
                                                    } else if (msgChat.startsWith("openboss2")) {
                                                        ((Boss) boss.get(1)).isOpen = true;
                                                        ((Boss) boss.get(1)).bornTime = System.currentTimeMillis() + 30000L;
                                                    } else if (msgChat.startsWith("openboss3")) {
                                                        ((Boss) boss.get(2)).isOpen = true;
                                                        ((Boss) boss.get(2)).bornTime = System.currentTimeMillis() + 30000L;
                                                    } else if (msgChat.startsWith("openboss4")) {
                                                        ((Boss) boss.get(3)).isOpen = true;
                                                        ((Boss) boss.get(3)).bornTime = System.currentTimeMillis() + 30000L;
                                                    }
                                                }
                                            } else if (msgChat.startsWith("offboss")) {
                                                if (player.isAdmin) {
                                                    if (msgChat.startsWith("offboss1")) {
                                                        ((Boss) boss.get(0)).isOpen = false;
                                                    } else if (msgChat.startsWith("offboss2")) {
                                                        ((Boss) boss.get(1)).isOpen = false;
                                                    } else if (msgChat.startsWith("offboss3")) {
                                                        ((Boss) boss.get(2)).isOpen = false;
                                                    } else if (msgChat.startsWith("offboss4")) {
                                                        ((Boss) boss.get(3)).isOpen = false;
                                                    }
                                                }
                                            } else if (msgChat.startsWith("mc") && player.isAdmin) {
                                                String charname = msgChat.substring(3);
                                                Char p = CharManager.instance.getCharByCharName(charname);
                                                if (p != null) {
                                                    map.playerExit(player);
                                                    player.region = p.region;
                                                    map.move2Map(player, p.x / 16, p.y / 16, p.mapID, p.inCountry);
                                                }
                                            } else if ((msgChat.equals("tua") || msgChat.startsWith("tua ")) && player.isAdmin) {
                                                // Dong ho ao (chi may thu, sv.cho_phep_thu_nghiem=1) - xem com.htbb.pokemon.DongHo
                                                player.sendMessage(MessageCreator.createServerAlertMessage(
                                                        com.htbb.pokemon.DongHo.tua(msgChat.length() > 3 ? msgChat.substring(4) : "", player.charname), ""));
                                            } else if (msgChat.startsWith("mto")) {
                                                if (player.isAdmin || TeamServer.isServerLocal()) {
                                                    String[] info = Char.split(msgChat.substring(4), " ");
                                                    map.move2Map(player, Integer.parseInt(info[1]), Integer.parseInt(info[2]), Integer.parseInt(info[0]), player.inCountry);
                                                }
                                            } else if (msgChat.startsWith("loadshop")) {
                                                if (player.isAdmin || TeamServer.isServerLocal()) {
                                                    Map.reloadShop();
                                                }
                                            } else if (msgChat.startsWith("loadpotion")) {
                                                if (player.isAdmin || TeamServer.isServerLocal()) {
                                                    Map.potionTemplates2 = Database.instance.loadPotionTemplate();
                                                    player.sendMessage(MessageCreator.createServerAlertMessage("Đã load potion", ""));
                                                }
                                            } else if (msgChat.startsWith("getchar")) {
                                                if (player.isAdmin) {
                                                    String charname = msgChat.substring(8);
                                                    Char p = CharManager.instance.getCharByCharName(charname);
                                                    if (p != null) {
                                                        String info = "at: "
                                                                + p.attack
                                                                + "|def: "
                                                                + p.defend_physic
                                                                + "|str: "
                                                                + p.strength
                                                                + "|dex: "
                                                                + p.agitity
                                                                + " "
                                                                + p.mapID
                                                                + " x: "
                                                                + p.x / 16
                                                                + " y: "
                                                                + p.y / 16
                                                                + " "
                                                                + p.inCountry
                                                                + " "
                                                                + p.region;
                                                        m.dos.writeUTF(info);
                                                        player.sendMessage(m);
                                                    }
                                                }
                                            } else if (msgChat.startsWith("oad")) {
                                                if (player.isAdmin) {
                                                    String charname = msgChat.substring(4);
                                                    Char p = CharManager.instance.getCharByCharName(charname);
                                                    if (p != null) {
                                                        p.isAdmin = false;
                                                    }
                                                }
                                            } else if (msgChat.startsWith("sad")) {
                                                if (player.isAdmin) {
                                                    String charname = msgChat.substring(4);
                                                    Char p = CharManager.instance.getCharByCharName(charname);
                                                    if (p != null) {
                                                        p.isAdmin = true;
                                                    }
                                                }
                                            } else if (msgChat.startsWith("ongive")) {
                                                if (player.isAdmin) {
                                                    giveSpecialItem = true;
                                                }
                                            } else if (msgChat.startsWith("offgive")) {
                                                if (player.isAdmin) {
                                                    giveSpecialItem = false;
                                                }
                                            } else if (msgChat.startsWith("reb")) {
                                                if (player.isAdmin) {
                                                    Database.instance.banChar(msgChat.substring(4), 0);
                                                }
                                            } else if (msgChat.startsWith("ban")) {
                                                if (player.isAdmin) {
                                                    Database.instance.banChar(msgChat.substring(4), 1);
                                                }
                                        
                                            } else if (msgChat.startsWith("stime")) {
                                                if (player.isAdmin) {
                                                    Map.timeDelay = Short.parseShort(msgChat.substring(6));
                                                }
                                            } else if (msgChat.startsWith("sethit")) {
                                                if (player.isAdmin) {
                                                    Char.maxHit = Short.parseShort(msgChat.substring(7));
                                                }
                                            } else if (msgChat.startsWith("resetclan")) {
                                                if (player.isAdmin) {
                                                    Database.instance.reUpdateClan();
                                                }
                                            } else if (msgChat.startsWith("ongive")) {
                                                if (player.isAdmin) {
                                                    Map.stopGiveMoney = true;
                                                }
                                            } else if (msgChat.startsWith("offgive")) {
                                                if (player.isAdmin) {
                                                    Map.stopGiveMoney = false;
                                                }
                                            } else if (msgChat.startsWith("onsms")) {
                                                if (player.isAdmin) {
                                                    Map.onSMS = true;
                                                }
                                            } else if (msgChat.startsWith("offsms")) {
                                                if (player.isAdmin) {
                                                    Map.onSMS = false;
                                                }
                                            } else if (msgChat.startsWith("kick")) {
                                                if (player.isAdmin) {
                                                    String charName = msgChat.substring(5);
                                                    System.out.println("charName= " + charName);
                                                    Char p = CharManager.instance.getCharByCharName(charName);
                                                    if (p != null) {
                                                        CharManager.instance.kickPlayer(p, "map 7");
                                                    }
                                                }
                                            } else if (msgChat.startsWith("offauto")) {
                                                if (player.isAdmin) {
                                                    Char.setTimeAuto(0);
                                                }
                                            } else if (msgChat.startsWith("set")) {
                                                if (player.isAdmin) {
                                                    LoginHandler.nickTest = msgChat.substring(4);
                                                }
                                            } else if (msgChat.startsWith("onauto")) {
                                                if (player.isAdmin) {
                                                    Char.setTimeAuto(Integer.parseInt(msgChat.substring(6)));
                                                }
                                            } else if (msgChat.startsWith("offcho")) {
                                                if (player.isAdmin) {
                                                    Map.openMarket = false;
                                                }
                                            } else if (msgChat.startsWith("oncho")) {
                                                if (player.isAdmin) {
                                                    Map.openMarket = true;
                                                }
                                            } else if (msgChat.startsWith("offpause")) {
                                                if (player.isAdmin) {
                                                    Map.pause = false;
                                                }
                                            } else if (msgChat.startsWith("onpause")) {
                                                if (player.isAdmin) {
                                                    Map.pause = true;
                                                }
                                            } else if (msgChat.startsWith("offx2")) {
                                                if (player.isAdmin) {
                                                    TeamServer.isDouble = false;
                                                    Map.doubleALL = 1;
                                                    TeamServer.timeDouble = System.currentTimeMillis();
                                                }
                                            } else if (msgChat.startsWith("onx2")) {
                                                if (player.isAdmin) {
                                                    TeamServer.isDouble = true;
                                                    TeamServer.timeDouble = System.currentTimeMillis();
                                                    Map.doubleALL = 2;
                                                }
                                            } else if (msgChat.startsWith("normaluser")) {
                                                if (player.isAdmin) {
                                                    Map.config = 0;
                                                }
                                            } else if (msgChat.startsWith("maxuser")) {
                                                if (player.isAdmin) {
                                                    Map.config = 1;
                                                }
                                            } else if (msgChat.startsWith("normal")) {
                                                if (player.isAdmin) {
                                                    Map.config = 0;
                                                }
                                            } else if (msgChat.startsWith("getinfo")) {
                                                if (msgChat.indexOf("1") != -1) {
                                                    m.dos
                                                            .writeUTF(
                                                                    "MXU "
                                                                    + TeamServer.maxUser
                                                                    + " MXS "
                                                                    + TeamServer.maxCCU
                                                                    + " CRU "
                                                                    + CharManager.instance.totalChar()
                                                                    + " CSS "
                                                                    + SessionManager.instance.size()
                                                            );
                                                    player.sendMessage(m);
                                                }
                                            } else if (BadWord.checkBadWord(msgChat)) {
                                                player.sendMessage(
                                                        MessageCreator.createMsgChat(
                                                                player.id, "Nội dung bạn trò chuyện không hợp lệ. Người chơi khác sẽ không thấy câu nói của bạn"
                                                        )
                                                );
                                            } else {
                                                msgChat = Map.checkBadWord(msgChat);
                                                msgChat = msgChat.replaceAll("\\s+", " ").trim();
                                                msgChat = Map.replaceStringWithSpace(msgChat);
                                                msgChat = Map.replaceExtendDomain(msgChat);
                                                if (System.currentTimeMillis() - player.timecamchat < 0L) {
                                                    player.sendMessage(m);
                                                } else {
                                                    player.timecamchat = 0L;
                                                    long time = System.currentTimeMillis() - player.timeChat;
                                                    if (time > 30000L || player.nMsgChat < 5 || player.isAdmin) {
                                                        if (time > 30000L && !player.isAdmin) {
                                                            player.timeChat = System.currentTimeMillis();
                                                            player.nMsgChat = 0;
                                                        }

                                                        if (!player.isAdmin) {
                                                            player.nMsgChat++;
                                                            if (player.map.mapIDLoadMap == Map.idMapTown && Map.getTown[player.inCountry] || map.mapIDLoadMap == 17) {
                                                                player.nMsgChat = 0;
                                                                player.sendMessage(m);
                                                                return;
                                                            }

                                                            if (player.timeChat > 0L && System.currentTimeMillis() - player.timeChat <= 5000L) {
                                                                if (player.nMsgChat >= 5) {
                                                                    player.timecamchat = System.currentTimeMillis() + 1800000L;
                                                                    player.nMsgChat = 0;
                                                                }
                                                            } else if (System.currentTimeMillis() - player.timeChat > 5000L) {
                                                                player.timeChat = System.currentTimeMillis();
                                                                player.nMsgChat = 0;
                                                            }
                                                        }

                                                        msgChat = BadWord.checkBadWord2(msgChat);
                                                        if (player.lvDetail.lv >= 20) {
                                                            if (msgChat.length() >= 2) {
                                                                byte maxChar = 50;
                                                                if (msgChat.length() > maxChar) {
                                                                    msgChat = msgChat.substring(0, maxChar);
                                                                }

                                                                m.dos.writeUTF(msgChat);
                                                                player.sendToNearPlayer(m);
                                                                player.sendMessage(m);
                                                                m.cleanup();
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        String[] info = Char.split(msgChat.substring(8), " ");
                        Char p = CharManager.instance.getCharByCharName(info[0]);
                        if (p != null && !p.isAdmin) {
                            long time = (long) Integer.parseInt(info[1].trim());
                            if (time == -1L) {
                                time = 365L;
                            } else if (time == -2L) {
                                time = 0L;
                            }

                            p.dayCamChat = Char.getDayTime(time * 24L * 60L * 60L * 1000L);
                            Database.instance.saveOrtherLog("", p.charname, "bi " + player.getSession().username + " cấm chat " + time + " ngày", "scamchat");
                            player.sendMessage(MessageCreator.createServerAlertMessage(p.charname + " đã bị cấm chat " + time + " ngày", ""));
                        }
                    }
                }
            }
        }
    }

}
