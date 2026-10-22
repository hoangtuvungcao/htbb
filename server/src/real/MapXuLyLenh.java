package real;


import data.Animal;
import data.CharInfo;
import data.DataGame;
import data.Database;
import data.ItemInfo;
import data.ItemLuckyDraw;
import data.Net;
import data.NewClan;
import io.Message;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;
import real.cmd.LoginHandler;
import server.TeamServer;

/**
 * Than cac method xu ly lenh va dieu huong cua Map: dinh tuyen goi tin tu
 * client, chuyen ban do, nhat vat pham, dot den, nap SMS, bang xep hang,
 * thong tin chi tiet nhan vat, chap nhan thach dau to doi, cong kinh nghiem,
 * mua trang phuc.
 *
 * Tach tu real/Map.java. Cac method VAN CON trong Map, giu NGUYEN chu ky cu,
 * chi than chuyen sang day. Tham so "Map map" thay cho "this" truoc day
 * (tru method von la static thi khong co tham so nay).
 */
class MapXuLyLenh {

    static void addXpCharEvent(Char p, long dxp, boolean isNhansam, String pos) {
        try {
            if (dxp < 0L) {
                // Exp AM lam tut cap (va exp co the thanh am -> LevelDetail nem loi ngay giua
                // khoi synchronized, de lai nhan vat lv 0). Khong nguoi goi nao co y truyen so
                // am; duong sinh ra no la phep tinh tran int (xem Map.calculatorXpParty). Chan
                // va ghi lai de con biet neu con duong khac.
                Database.instance.saveOrtherLog("", p.charname, "bo qua exp am " + dxp + " tu " + pos, "expam");
                return;
            }
            synchronized (p.lvDetail) {

                if (Map.checkRcvXP) {
                    if (p.receiveXP() == 3) {
                        return;
                    }

                    dxp /= (long) p.receiveXP();
                }

                int currentLevel = p.lvDetail.lv;
                long currentXP = p.lvDetail.getExp();
                if (dxp == 0L) {
                    dxp = 1L;
                }

                int div = 1;
                if (currentLevel >= 60 && !isNhansam && !com.htbb.pokemon.LauDai.bat()) {
                    div = 3;
                }

                if (com.htbb.pokemon.LauDai.bat()) {
                    // GD2 3.2/1.6: bo chia 3, cat theo tran cap mua, phan thua -> tinh thong.
                    currentXP = ExpLauDai.congTheoTran(p, currentXP, dxp);
                } else {
                    currentXP += dxp / (long) div + (long) (1 + Map.r.nextInt(5));
                }
                p.lvDetail.setExp(currentXP, p.oldLv, p.charname, "addxpcharevent map");
                p.expRace = p.expRace + dxp + (long) (1 + Map.r.nextInt(5));
                int newlevel = p.lvDetail.lv;
                byte[] var10000 = new byte[]{5, 5, 5, 5, 5, 5, 5};
                if (newlevel >= currentLevel + 1) {
                    // An mung len cap tu 60 tro len bang mua hoa toan server, giong tinh
                    // luyen than binh thanh cong (yeu cau nguoi dung 18/09/2026). Kich hoat
                    // MOI LAN len cap ma ket qua >=60, khong chi lan dau dat moc.
                    if (newlevel >= 60) {
                        Map.muaHoaAnMungToanServer();
                    }

                    if (Char.isDemTrungThu() && p.lvDetail.lv >= 20 && p.nBinhTra == 0) {
                        p.nBinhTra = 1;
                        p.potions[125]++;
                        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                        p.sendMessage(MessageCreator.createMsgChat(p.id, "Nhận được hộp quà trung thu"));
                        Database.instance.saveOrtherLog("", p.charname, "nhan qua trung thu khi len 20", "nhanhopquatt");
                    }

                    Database.instance
                            .saveOrtherLog(
                                    "",
                                    p.charname,
                                    currentLevel
                                    + "_"
                                    + newlevel
                                    + " len lv:  xp cu: "
                                    + currentXP
                                    + " >> xpmoi: "
                                    + currentXP
                                    + " >> expadd: "
                                    + dxp
                                    + " > vi tri set: "
                                    + pos,
                                    "lenlevel"
                            );
                    if (p.getSession().bigProvider.equals("1") && !p.getSession().username.equals("") && p.getSession().privateProvider.equals("1")) {
                        TeamServer.addQueue(p.getSession().username + "/LEVELUP/" + newlevel + "/" + p.charname);
                    }

                    Message m = new Message(33);
                    m.dos.writeShort(p.id);
                    m.dos.writeByte(newlevel);
                    m.dos.writeInt(p.maxhp);
                    m.dos.writeInt(p.maxmp);
                    p.hp = p.maxhp;
                    p.mp = p.maxmp;
                    p.sendMessage(m);
                    p.sendToNearPlayer(m);
                    m.cleanup();
                    // Pokemon (viec S5): tien hoa neu cap moi du moc. Dat TRUOC goi 3
                    // ben duoi de goi 3 kem -90 da mang loai moi.
                    TienHoaPokemon.kiemTienHoaTheoCap(p);
                    SoTayLauDai.khiLenCap(p, currentLevel, newlevel); // GD2 7.5 moc su pham (tat co: khong lam gi)
                    KhoaBanDoGame.capNhat(p); // len cap: ban do vua du cap -> bao + gui lai goi -108
                    if (newlevel > p.lastLV) {
                        // diem tiem nang / chi so / diem ky nang + tu hoc chieu (HocChieuGame: luat cu khi tat)
                        HocChieuGame.congLenCap(p, newlevel);
                        m = new Message(32);
                        m.dos.writeShort(p.id);
                        m.dos.writeByte(2);
                        m.dos.writeUTF("bp");
                        m.dos.writeInt(p.basepoint);
                        m.dos.writeUTF("sp");
                        m.dos.writeInt(p.skillpoint);
                        m.dos.writeByte(p.lvDetail.lv);
                        m.dos.writeShort(p.lvDetail.percent);
                        p.sendMessage(m);
                        m.cleanup();
                        p.calculateAttrib();
                        p.hp = p.maxhp;
                        p.mp = p.maxmp;
                        p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
                    } else {
                        m = new Message(32);
                        m.dos.writeShort(p.id);
                        m.dos.writeByte(2);
                        m.dos.writeUTF("bp");
                        m.dos.writeInt(p.basepoint);
                        m.dos.writeUTF("sp");
                        m.dos.writeInt(p.skillpoint);
                        m.dos.writeByte(p.lvDetail.lv);
                        m.dos.writeShort(p.lvDetail.percent);
                        p.sendMessage(m);
                        m.cleanup();
                        p.calculateAttrib();
                        p.hp = p.maxhp;
                        p.mp = p.maxmp;
                        p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
                    }
                }

                Message m = MessageCreator.createXPMessage(p, dxp);
                p.sendMessage(m);
                m.cleanup();
            }
        } catch (Exception var16) {
        }
    }

    static void addXPForChar(Char p, long dxp, boolean isNhansam, String pos) throws IOException {
        if (dxp < 0L) {
            // Xem chu thich o addXpCharEvent. Moi nguoi goi (ke ca LenhWeb dat cap tu web) deu
            // chi truyen so duong - LenhWeb.apDungOnline nem loi truoc khi goi neu khong phai
            // nang cap.
            Database.instance.saveOrtherLog("", p.charname, "bo qua exp am " + dxp + " tu " + pos, "expam");
            return;
        }
        synchronized (p.lvDetail) {
            boolean isAdminBuff = pos != null && pos.contains("adminpanel");
            if (!isAdminBuff && Map.checkRcvXP) {
                if (p.receiveXP() == 3) {
                    return;
                }
                dxp /= (long) p.receiveXP();
            }

            int currentLevel = p.lvDetail.lv;
            long currentXP = p.lvDetail.getExp();
            if (dxp == 0L) {
                dxp = 1L;
            }

            int div = 1;
            if (currentLevel >= 60 && !isNhansam && !isAdminBuff && !com.htbb.pokemon.LauDai.bat()) {
                div = 3;
            }

            // Nếu là admin buff thì không chia, không random
            if (isAdminBuff) {
                currentXP += dxp;
            } else if (com.htbb.pokemon.LauDai.bat()) {
                // GD2 3.2/1.6: bo chia 3, cat theo tran cap mua, phan thua -> tinh thong.
                currentXP = ExpLauDai.congTheoTran(p, currentXP, dxp);
            } else {
                currentXP += dxp / (long) div + (long) (1 + Map.r.nextInt(5));
            }
            p.lvDetail.setExp(currentXP, p.oldLv, p.charname, "addxpforchar map");
            p.expRace = p.expRace + dxp + (long) (1 + Map.r.nextInt(5));
            int newlevel = p.lvDetail.lv;
            byte[] var10000 = new byte[]{5, 5, 5, 5, 5, 5, 5};
            if (newlevel >= currentLevel + 1) {
                // An mung len cap tu 60 tro len bang mua hoa toan server, giong tinh luyen
                // than binh thanh cong (yeu cau nguoi dung 18/09/2026). Kich hoat MOI LAN
                // len cap ma ket qua >=60, khong chi lan dau dat moc. Ham nay va
                // addXpCharEvent la HAI cho DUY NHAT xu ly len cap (xem chu thich dau file
                // ve viec them tinh nang phai vao ca hai).
                if (newlevel >= 60) {
                    Map.muaHoaAnMungToanServer();
                }

                if (Char.isDemTrungThu() && p.lvDetail.lv >= 20 && p.nBinhTra == 0) {
                    p.nBinhTra = 1;
                    p.potions[125]++;
                    p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                    p.sendMessage(MessageCreator.createMsgChat(p.id, "Nhận được hộp quà trung thu"));
                    Database.instance.saveOrtherLog("", p.charname, "nhan qua trung thu khi len 20", "nhanhopquatt");
                }

                Database.instance
                        .saveOrtherLog(
                                "",
                                p.charname,
                                currentLevel + "_" + newlevel + " len lv:  xp cu: " + currentXP + " >> xpmoi: " + currentXP + " >> expadd: " + dxp + " > vi tri: " + pos,
                                "lenlevel"
                        );
                if (p.getSession().bigProvider.equals("1") && !p.getSession().username.equals("") && p.getSession().privateProvider.equals("1")) {
                    TeamServer.addQueue(p.getSession().username + "/LEVELUP/" + newlevel + "/" + p.charname);
                }

                Message m = new Message(33);
                m.dos.writeShort(p.id);
                m.dos.writeByte(newlevel);
                m.dos.writeInt(p.maxhp);
                m.dos.writeInt(p.maxmp);
                p.hp = p.maxhp;
                p.mp = p.maxmp;
                p.sendMessage(m);
                p.sendToNearPlayer(m);
                m.cleanup();
                // Pokemon (viec S5): ban sao thu hai cua khoi len cap - xem tren.
                TienHoaPokemon.kiemTienHoaTheoCap(p);
                    SoTayLauDai.khiLenCap(p, currentLevel, newlevel); // GD2 7.5 moc su pham (tat co: khong lam gi)
                    KhoaBanDoGame.capNhat(p); // len cap: ban do vua du cap -> bao + gui lai goi -108
                if (newlevel > p.lastLV) {
                    // diem tiem nang / chi so / diem ky nang + tu hoc chieu (HocChieuGame: luat cu khi tat)
                    HocChieuGame.congLenCap(p, newlevel);
                    m = new Message(32);
                    m.dos.writeShort(p.id);
                    m.dos.writeByte(2);
                    m.dos.writeUTF("bp");
                    m.dos.writeInt(p.basepoint);
                    m.dos.writeUTF("sp");
                    m.dos.writeInt(p.skillpoint);
                    m.dos.writeByte(p.lvDetail.lv);
                    m.dos.writeShort(p.lvDetail.percent);
                    p.sendMessage(m);
                    m.cleanup();
                    p.calculateAttrib();
                    p.hp = p.maxhp;
                    p.mp = p.maxmp;
                    p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
                } else {
                    m = new Message(32);
                    m.dos.writeShort(p.id);
                    m.dos.writeByte(2);
                    m.dos.writeUTF("bp");
                    m.dos.writeInt(p.basepoint);
                    m.dos.writeUTF("sp");
                    m.dos.writeInt(p.skillpoint);
                    m.dos.writeByte(p.lvDetail.lv);
                    m.dos.writeShort(p.lvDetail.percent);
                    p.sendMessage(m);
                    m.cleanup();
                    p.calculateAttrib();
                    p.hp = p.maxhp;
                    p.mp = p.maxmp;
                    p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
                }
            }

            Message m = MessageCreator.createXPMessage(p, dxp);
            p.sendMessage(m);
            m.cleanup();
        }
    }

    /**
     * Qua NPC cap 35 (mot lan moi nhan vat). Chu game duyet 19/10/2026: bo 10 trieu xu thoi thu nghiem (= ~580 gio thu
     * nhap o cap 35, ~1.300 lan so xu trung vi nguoi choi cap 35 dang giu trong tools/luu_tru_sinh_du_lieu/du_lieu/mo_phong_kinh_te.csv - ca mot
     * nguon lam phat lan mot muc tieu nuoi clone). Nay mot gia tri cho ca hai che do:
     *   - {@link #XU_CAP_35} xu = von khoi dau (Database.XU_KHOI_DAU), ~0,3 gio thu nhap o cap 35 (17.270 xu/gio);
     *   - {@link #LUONG_KHOA_CAP_35} luong khoa = giua thanh tuu cap 30 (5) va cap 40 (6) (tools/luu_tru_sinh_du_lieu/du_lieu/nhiem_vu_moi.csv A-003/A-004);
     *   - {@link #NGUYEN_LIEU_CAP_35} nguyen lieu KHOA moi loai cap 6 (giu nhu thiet ke GD2 2.4; khoa = khong ban NPC / cho).
     * Test: test/KiemThuongTanThuVaLenCap35.java.
     */
    static final long XU_CAP_35 = com.htbb.pokemon.QuaMocCap.XU_CAP_35;
    static final int LUONG_KHOA_CAP_35 = com.htbb.pokemon.QuaMocCap.LUONG_KHOA_CAP_35;
    static final int NGUYEN_LIEU_CAP_35 = com.htbb.pokemon.QuaMocCap.NGUYEN_LIEU_CAP_35;

    static void thuongLenCap35(Char p) {
        if (!p.canReceiveGif35()) {
            return;
        }
        p.thuongLv35 = 1;
        p.gif35 = 1;
        p.addXu(XU_CAP_35, "thuong len cap 35");
        p.addLuongLock(LUONG_KHOA_CAP_35);
        for (int i = 0; i < GemTemplate.ID_MATERIAL_LOW[5].length; i++) {
            p.doAddGemItem(GemTemplate.ID_MATERIAL_LOW[5][i], NGUYEN_LIEU_CAP_35, true);
            p.doAddGemItem(GemTemplate.ID_MATERIAL_HIGHT[5][i], NGUYEN_LIEU_CAP_35, true);
        }
        p.sendMessage(MessageCreator.createCharGemItem(p));
        p.sendMessage(MessageCreator.createServerAlertMessage(
                "Bạn đã nhận quà cấp 35: " + String.format("%,d", XU_CAP_35).replace(',', '.') + " xu, " + LUONG_KHOA_CAP_35
                + " lượng khoá và " + NGUYEN_LIEU_CAP_35 + " nguyên liệu khoá mỗi loại cấp 6.", ""));
        Database.instance.saveOrtherLog("", p.charname, "thuong len cap 35: " + XU_CAP_35 + " xu + " + LUONG_KHOA_CAP_35
                + " luong khoa + " + NGUYEN_LIEU_CAP_35 + " khoa moi loai nlsc/nlcc cap 6", "thuonglv35");
    }

    static void doGetItem(Map map, Char player, short id) {
        try {
            if (player == null) {
                return;
            }
    
            if (player.isFullInventory() && !player.isAutoBan && !player.isNghienAuto) {
                player.sendMessage(MessageCreator.createMsgChat(player.id, "Hành trang đã đầy"));
                return;
            }
    
            Item item = map.getItem(id, player.inCountry);
            if (item == null) {
                Message m = new Message(20);
                m.dos.writeByte(3);
                m.dos.writeShort(id);
                player.sendMessage(m);
                m.cleanup();
                return;
            }
    
            if (item.istrungPet() || (item.belongUser != player.charDBID && item.belongUser != 0)) {
                return;
            }
    
            // Chiem nguyen tu truoc khi cho vao tui: ham nay duoc goi CA tu goi 18 (luong phien)
            // CA tu vong tu dong nhat trong CharVongLap (luong ban do). Ban cu chi remove roi
            // cap luon -> hai luong cung nhat mot mon = mon do nam trong tui hai nguoi (hoac hai
            // lan trong mot nguoi) va duoc luu that xuong DB. Xem Map.chiemItem.
            // 19/10/2026: tui day ma bat tu nghien: chi nhat mon SE bi nghien ngay. Truoc day mon khong nghien duoc
            // van bi chiem roi roi xuong kiem "day tui" o cuoi ham -> mon bien mat (khong vao tui, khong con duoi dat).
            if (player.isFullInventory() && !player.isAutoBan && !(player.isNghienAuto
                    && !Map.isWearingAnimal(item.getTemplate().type) && Map.isItemChar(item.getTemplate().type))) {
                player.sendMessage(MessageCreator.createMsgChat(player.id, "Hành trang đã đầy"));
                return;
            }
            if (!map.chiemItem(item, player.inCountry)) {
                return;
            }
            item.owner = player.charDBID;
            short oldid = item.id;
            item.id = player.getIDItem();
            item.identify = player.charDBID;
            item.isItemDrop = 1;
    
            Message m = new Message(18);
            m.dos.writeShort(player.id);
            m.dos.writeByte(item.clazz);
            m.dos.writeShort(oldid);
            m.dos.writeShort(item.id);
            m.dos.writeShort(item.getTemplate().id);
            m.dos.writeByte(item.plus);
            m.dos.writeByte(item.level);
            m.dos.writeShort(item.durable);
            m.dos.writeShort(item.mDurable);
            player.sendMessage(m);
            player.sendToNearPlayer(m);
            m.cleanup();
    
            if (player.isNghienAuto && !Map.isWearingAnimal(item.getTemplate().type) && 
                item.isItemDrop != 0 && Map.isItemChar(item.getTemplate().type)) {
                
                int sl = item.colorName == Item.COLOR_BLUE ? Map.r.nextInt(2) + 2 :
                        item.colorName == Item.COLOR_RED ? Map.r.nextInt(4) + 2 :
                        item.colorName == Item.COLOR_GREEN ? Map.r.nextInt(6) + 2 : 1;
                final boolean lauDai = com.htbb.pokemon.LauDai.bat();
                if (lauDai) {
                    sl = RoiLauDai.phanRa(player, item); // GD2 2.9: 2 da + 10% gia, khong phi
                } else {
                    player.doAddGemItem(246, sl, item.lock == 1);
                }
                Database.instance.deleteItem(item.dbid);
                // Mon do bien mat o day. Ghi ro "vua nhat len" de nguoi tra cuu khong
                // hieu nham la do trong tui: nhanh nay chi cham vao vat pham vua duoc
                // nhat tu duoi dat, do da nam trong hanh trang khong di qua day.
                Database.instance.saveLogMatVatPham(player, item,
                        "nghien do tu dong (do vua nhat len) nhan " + sl + (lauDai ? " da (lau dai)" : " bot thuong"));
                if (!lauDai) {
                    player.subXu(2000L, true, "offline map 9");
                }
                player.sendMessage(MessageCreator.createCharGemItem(player));
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                player.sendMessage(MessageCreator.createMsgChat(player.id, "Đã nghiền được " + item.getName() + " nhận được " + sl + " " + "bột thường"));
                return;
            }
    
            if (player.isAutoBan) {
                int price = Map.itemTemplates.get(5).get(item.getTemplate().id).price;
                int xuban = com.htbb.pokemon.LauDai.bat() ? (int) com.htbb.pokemon.KinhTeLauDai.giaBanNpc(price) : price / 5; // GD2 2.9
                player.addXu(xuban, "map 6");
                // Cung nhu nhanh nghien: chi ban mon VUA NHAT LEN, khong dung toi tui do.
                Database.instance.saveLogMatVatPham(player, item,
                        "ban tu dong (do vua nhat len) duoc " + xuban + " xu");
                player.sendMessage(MessageCreator.createCharGemItem(player));
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                player.sendMessage(MessageCreator.createMsgChat(player.id, "Đã bán được " + item.getName() + " nhận được " + xuban + " " + "xu"));
                return;
            }
    
            if (player.isFullInventory()) {
                player.sendMessage(MessageCreator.createMsgChat(player.id, "Hành trang đã đầy"));
                return;
            }
            player.iItems.add(item);
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));

    
        } catch (Exception e) {
            System.out.println("Error in doGetItem: " + e.getMessage());
        }
    }

    static void processMessage(Map map, Char player, Message message) throws IOException {
        // NPC tinh co dich vu dat tren ban do ngoai troi (Chansey, Kecleon, Machamp, Kangaskhan... o thi tran Hoenn):
        // lenh mua / ban / ren / kho cua nguoi choi dung canh NPC do chay bang than OfflineMap nhu trong nha.
        final Map banDoDv = NpcTinhBanDo.banDoDichVu(map, player, message.command);
        if (banDoDv != null) {
            processMessage(banDoDv, player, message);
            return;
        }
        // Chong spam thao tac lap (menu NPC 53/55, rao ban 62/92, mua ban 24/28/95): moi lenh
        // nay cham CSDL / tao popup; bot bam lien tuc thi bo bot, khong cat ket noi. Xem ChongSpam.
        final com.htbb.chongspam.ChongSpam.HanhDong hdSpam = com.htbb.chongspam.ChongSpam.hanhDongCuaLenh(message.command);
        if (hdSpam != null && !MapXaHoi.kiemSpamHanhDong(player, hdSpam)) {
            return;
        }
        // Tu dong danh chinh thuc: nhip nguoi that luc dang tu dong (danh / nhat), danh nguoi = PvP -> tat. TuDongGame.
        if (!TuDongGame.kiemLenh(player, message.command)) {
            return;
        }
        // 0.9.8 ngam tay PvP: lenh ban theo huong (-113) + chan / doi danh khoa muc tieu vao nguoi choi (6). BanHuongGame.
        if (BanHuongGame.xuLy(map, player, message)) {
            return;
        }

        switch (message.command) {
            case -76:
                map.doBuyItemShopNew(player, message);
            case -75:
            case -74:
            case -73:
            case -72:
            case -64:
            case -60:
            case -58:
            case -56:
            case -55:
            case -51:
            case -50:
            case -49:
            case -48:
            case -47:
            case -44:
            case -43:
            case -42:
            case -40:
            case -39:
            case -38:
            case -29:
            case -26:
            case -4:
            case -1:
            case 0:
            case 1:
            case 2:
            case 3:
            case 5:
            case 8:
            case 10:
            case 11:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 20:
            case 23:
            case 25:
            case 26:
            case 30:
            case 32:
            case 33:
            case 35:
            case 37:
            case 38:
            case 39:
            case 40:
            case 41:
            case 42:
            case 43:
            case 44:
            case 45:
            case 46:
            case 47:
            case 54:
            case 56:
            case 57:
            case 58:
            case 59:
            case 60:
            case 63:
            case 64:
            case 65:
            case 73:
            case 80:
            case 83:
            case 84:
            case 89:
            case 90:
            case 91:
            case 93:
            case 96:
            case 97:
            case 98:
            case 99:
            case 103:
            case 105:
            default:
                break;
            case -100: // Tu dong danh chinh thuc (com.htbb.pokemon.TuDong)
                TuDongGame.xuLy(player, message);
                break;
            case -124: // Kenh chat 0.9.8: gui theo kenh / chan / ban be / tra id (real.KenhChatGame)
                KenhChatGame.xuLy(player, message);
                break;
            case -109: // Dau truong Bao Boi 3v3 (real.DauTruongGame, com.htbb.pokemon.DauTruongTran; client >= 908)
                DauTruongGame.xuLy(player, message);
                break;
            case -111: // Danh hieu: xin bang / deo / thao / da xem (real.DanhHieuGame, com.htbb.pokemon.DanhHieuLogic)
                DanhHieuGame.xuLy(player, message);
                break;
            case -126: // Loai day du: sprite theo nhu cau, bang loai, Pokedex 1.025 loai (real.LoaiMayChu)
                LoaiMayChu.xuLy(player, message);
                break;
            case -103: // Ngoai hinh trang bi: an/hien phu kien, trang phuc (com.htbb.pokemon.NgoaiHinh)
                NgoaiHinhGame.xuLy(player, message);
                break;
            case -101: // Guc / hoi sinh (com.htbb.pokemon.HoiSinh): xin / chon trong hop thoai
                HoiSinhGame.xuLy(player, message);
                break;
            case HocChieuGame.LENH: // -122 hoc chieu theo loai + Diem huan luyen (client >= 908)
                HocChieuGame.xuLy(player, message);
                break;
            case -120: // [bo-ngoc] che tao / khảm ngoc he (real.BoNgocGame, real.CheTaoGame)
                BoNgocGame.xuLy(player, message);
                break;
            case -121: // [bat-pokemon] thu phuc: trang thai / nem bong / Pokedex (real.BatPokemonGame)
                BatPokemonGame.xuLy(player, message);
                break;
            case -112: // 0.9.7 phong rieng: cham the trang thai -> "Rời" (pho ban) / "Bỏ cuộc" (tran cup)
                PhongRiengGame.xuLy(player, message);
                break;
            case MoRongTuiGame.LENH: // -107 Mo rong hanh trang: byte kieu 0 trang thai / 1 dung ve / 2 mua xu
                MoRongTuiGame.xuLy(player, message);
                break;
            case -116: // [trang-phuc] tu do trang tri: xin / mac / dong chi so / nhan / che / mua (real.TrangPhucGame)
                TrangPhucGame.xuLy(player, message);
                break;
            case -71:
                map.doSetTaxTown(player, message);
                break;
            case -70:
                MapDoi.doInfoClanQuest(map, player, message);
                break;
            case -69:
                MapEpNgoc.doAutoImbue(map, player, message);
                break;
            case -68:
                byte type = message.dis.readByte();
                if (type == 0) {
                    MapEpNgoc.doTachNguyenLieu(map, player, message.dis.readShort(), 2);
                } else if (type == 1) {
                    map.doAddNewAttribute(player, message.dis.readShort(), message.dis.readShort(), message.dis.readByte());
                } else if (type == 2) {
                    map.doLockItemAnimal(player, message.dis.readShort(), message.dis.readShort(), message.dis.readByte());
                } else if (type == 3) {
                    map.doLockItemChar(player, message.dis.readShort(), message.dis.readShort(), message.dis.readByte());
                } else if (type == 4) {
                    map.doNghienBot(player, message);
                } else if (type == 5) {
                    MapCheTao.doAddNewAttBot(map, player, message);
                }
                break;
            case -67:
                map.doTuBinh(player, message);
                break;
            case -66:
                map.doFruit(player, message);
                break;
            case -65:
                map.doGetItemQuest(player, message);
                break;
            case -63:
                map.doBuyXoso(player, message);
                break;
            case -62:
                MapQuaTang.doOpenLuckyBox(map, player, message);
                break;
            case -61:
                map.doProcessItemAnimal(player, message);
                break;
            case -59:
                map.doPlant(player, message);
                break;
            case -57:
                map.doMsgCharMonster(player, message);
                break;
            case -54:
                map.doChangTypeItem(player, message);
                break;
            case -53:
                NewClan.getTopMemberClan(player, message.dis.readShort());
                break;
            case -52:
                MapCheTao.doUserCreateItem(map, player, message);
                break;
            case -46:
                player.sendMessage(MessageCreator.createMsgSound(1));
                break;
            case -45:
                map.doCuoiThu(player, message);
                break;
            case -41:
                map.doGetGemItem(player, message);
                break;
            case -37:
                MapDoi.doQuestClan(map, player, message);
                break;
            case -36:
                MapEpNgoc.doFinishEpNgoc(map, player, message.dis.readByte(), message);
                break;
            case -35:
                MapEpNgoc.doFinishKhamItem(map, player, message.dis.readByte());
                break;
            case -34:
                MapEpNgoc.doKhamItem(map, player, message.dis.readByte());
                break;
            case -33:
                map.doMove2Warp(player, message);
                break;
            case -32:
                map.doPopup(player, message);
                break;
            case -31:
                map.doProcessInputText(player, message);
                break;
            case -30:
                map.doMenu(player, message);
                break;
            case -28:
                map.doConfirmCapcha(player, message);
                break;
            case -27:
                map.doSendWeaponeData(player, message);
                break;
            case -25:
                map.doGiveCardTown(player, message);
                break;
            case -24:
                map.doSendStringInfo(player, message);
                break;
            case -23: // vut nguyen lieu / vat pham dac biet: byte type, short id, [byte khoa neu type 0] [, short soLuong]
                HanhDongVatPham.boc(player, HanhDongVatPham.VUT, () -> MapEpNgoc.doGiveSpecialGem2Ground(map, player, message));
                break;
            case -22:
                doSendDetailChar(map, player, message);
                break;
            case -21: // vut binh: short potionType [, short soLuong] (thieu = ca chong)
                HanhDongVatPham.boc(player, HanhDongVatPham.VUT, () -> map.doGivePotion2Ground(player, message));
                break;
            case -20:
                MapDoi.doGiveMoneyClan(map, player, message);
                break;
            case -19:
                doSendTop(map, player, message);
                break;
            case -18:
                MapDoi.doChatClan(map, player, message);
                break;
            case -17:
                MapDoi.doSendMessage2Clan(map, player, message);
                break;
            case -16:
                MapDoi.doSetSloganClan(map, player, message);
                break;
            case -15:
                MapDoi.doUserOutClan(map, player);
                break;
            case -14:
                MapDoi.doDelClan(map, player);
                break;
            case -13:
                MapDoi.doKickMemberOutClan(map, player, message);
                break;
            case -12:
                MapDoi.doSendInfoClan(map, player, message);
                break;
            case -11:
                MapDoi.doInviteChar2Clan(map, player, message);
                break;
            case -10:
                MapDoi.doCreateClan(map, player, message);
                break;
            case -9:
                MapDoi.doRequestCreateClan(map, player);
                break;
            case -8:
                MapDoi.doSendImgIconClan(map, player, message);
                break;
            case -7:
                MapDoi.doSendListMemberClan(map, player, message);
                break;
            case -6:
                map.delFriend(player, message.dis.readUTF());
                break;
            case -5:
                MapXaHoi.doSendMessage(map, player, message);
                break;
            case -3:
                map.doSendWearingInfo(player, message.dis.readByte(), message.dis.readShort());
                break;
            case -2:
                map.doSendMainInfo(player, message.dis.readByte(), message.dis.readShort());
                break;
            case 4:
                map.doPlayerMove(player, message);
                break;
            case 6:
                map.doAttackPlayer(player, message);
                break;
            case 7:
                map.doRequestMonterInfo(player, message);
                break;
            case 9:
                synchronized (BanHuongGame.khoa(player)) { // 0.9.8: khong chen giua luc dan ngam tay gay sat thuong
                    map.doAttackMonster(player, message);
                }
                break;
            case 12:
                doChangeMap(map, player, message);
                break;
            case 18:
                map.doGetItem(player, message);
                System.err.println("ok");
                break;
            case 19:
                map.doGetPotion(player, message);
                break;
            case 21:
                MapCheTao.doRequestItemInfo(map, player, message);
                break;
            case 22:
                map.doUsePotion(player, message);
                break;
            case 24:
                HanhDongVatPham.boc(player, HanhDongVatPham.MUA, () -> map.doBuyItem(player, message));
                break;
            // Truoc day dong nay bi comment -> server khong xu ly chat (lenh 27) chut nao:
            // nguoi choi khong chat duoc cho nhau (client chi tu hien lai cau minh vua go),
            // va moi lenh admin go trong chat (stown, tgct, ktct...) deu vo tac dung.
            case 27:
                map.doChat(player, message);
                break;
            case 28:
                HanhDongVatPham.boc(player, HanhDongVatPham.BAN, () -> map.doSellItem(player, message));
                break;
            case 29:
                map.doUseItem(player, message);
                break;
            case 31:
                map.doComeHomeWhenDie(player);
                break;
            case 34:
                map.doAddBasePoint(player, message);
                break;
            case 36:
                map.doAddSkillPoint(player, message);
                break;
            case 48:
                player.createParty();
                break;
            case 49:
                MapXaHoi.doParty(map, player, message);
                break;
            case 50:
                map.doKickParty(player, message);
                break;
            case 51:
                map.doUserBuff(player, message);
                break;
            case 52:
                player.receiveQuest(message);
                break;
            case 53:
                player.talkWithNpc(message.dis.readByte(), message.dis.readByte(), message.dis.readShort());
                break;
            case 55:
                player.talkWithNpcResponse(message.dis.readByte(), message.dis.readByte(), message.dis.readShort());
                break;
            case 61:
                HanhDongVatPham.boc(player, HanhDongVatPham.VUT, () -> map.doGiveItemToGround(player, message));
                break;
            case 62:
                map.doRequestSellItemMarket(player, message);
                break;
            case 66:
                HanhDongVatPham.boc(player, HanhDongVatPham.GIAO_DICH, () -> MapMuaBan.doTrade(map, player, message, 0));
                break;
            case 67:
                map.doSendKiller(player);
                break;
            case 68:
                map.doPutItem2Bag(player, message.dis.readShort());
                break;
            case 69:
                map.doGetItemFromBag(player, message.dis.readShort());
                break;
            case 70:
                player.isPutItem2Bag = false;
                break;
            case 71:
                MapEpNgoc.doImbue(map, player, message.dis.readByte());
                break;
            case 72:
                map.doRepair(player, message.dis.readByte());
                break;
            case 74:
                map.doBuyGemItem(player, message);
                break;
            case 75:
                MapEpNgoc.doAddItemImbue(map, player, message.dis.readShort());
                break;
            case 76:
                MapEpNgoc.doAddGemItemImbue(map, player, message.dis.readShort(), message);
                break;
            case 77:
                MapEpNgoc.doFinishImbue(map, player, message.dis.readByte());
                break;
            case 78:
                HanhDongVatPham.boc(player, HanhDongVatPham.BAN, () -> map.doSellGemItem(player, message));
                break;
            case 79:
                map.doMove2Map(player, message.dis.readByte(), message.dis.readShort());
                break;
            case 81:
                map.doBuyTicket(player);
                break;
            case 82:
                map.doUp2Board(player);
                break;
            // case 85 (doBuySpecialItem) xoa 15/09/2026: lenh 85 nam trong
            // RealController.privateCmd nen khong bao gio toi duoc switch nay - xem
            // real/MapMuaBan.java cho chi tiet day du.
            case 86:
                MapMuaBan.doUseSpecialITem(map, player, message);
                break;
            case 87:
                map.doLuckyDraw(player, message);
                break;
            case 88:
                map.doRequestMainCharInfo(player);
                break;
            case 92:
                map.doUserSellItem(player, message);
                break;
            case 94:
                map.proccessDepositeItem(player, message);
                break;
            case 95:
                map.doBuyItemOfUser(player, message);
                break;
            case 100:
                map.doSendMonsterTemplate(player, 0);
                break;
            case 101:
                MapXaHoi.doAddFriend(map, player, message);
                break;
            case 102:
                map.doGetDetailChar(player, message);
                break;
            case 104:
                player.config(message.dis.readByte());
                break;
            case 106:
                synchronized (BanHuongGame.khoa(player)) { // 0.9.8: nhu lenh 9
                    map.doAttackMultiTarget(player, message);
                }
                break;
            case 107:
                map.doNapSMS(player, message);
                break;
            case 108:
                map.doXuongThuCuoi(player, message);
                break;
            case 109:
                map.doLearnSkil(player, message);
                break;
            case 110:
                map.doReadMessage(player, message);
        }

        try {
            message.cleanup();
        } catch (Exception var4) {
        }
    }

    static void doAcceptChallengeParty(Map map, Char player, int ok) {
        try {
            Char p = CharManager.instance.getCharByCharName(player.nameCompetitor);
            if (p != null) {
                if (ok == 0) {
                    p.nameCompetitor = "";
                    p.moneyThachdau = 0;
                    p.sendMessage(MessageCreator.createServerAlertMessage(player.charname + " không chấp nhận lời thách đấu của bạn", ""));
                    return;
                }

                for (int i = 0; i < p.party.userParty.size(); i++) {
                    ((Char) p.party.userParty.get(i))
                            .sendMessage(
                                    MessageCreator.createServerAlertMessage(
                                            player.charname
                                            + " đã chấp nhận lời thách đấu nhóm của bạn. Trận đấu của nhóm bạn và nhóm "
                                            + player.nameCompetitor
                                            + " sẽ diễn ra sau 30s nữa.",
                                            ""
                                    )
                            );
                }

                for (int i = 0; i < player.party.userParty.size(); i++) {
                    ((Char) player.party.userParty.get(i))
                            .sendMessage(
                                    MessageCreator.createServerAlertMessage(
                                            "Bạn đã chấp nhận lời thách đấu của nhóm "
                                            + p.charname
                                            + ". Trận đấu của nhóm bạn và nhóm "
                                            + p.nameCompetitor
                                            + " sẽ diễn ra sau 30s nữa.",
                                            ""
                                    )
                            );
                }

                // Kiem-va-tru NGUYEN TU ngay luc chap nhan, khong tin vao so du da kiem luc
                // moi (MapXaHoi.doSendChallenParty) - giua hai moc do co the da tieu xu (mua
                // do, giao dich...), va nhanh moi cung KHONG he kiem lai so du cua CHINH nguoi
                // moi (chi kiem doi phuong). subXu cu chi chan ve 0 ma khong bao loi, nen mot
                // hoac ca hai ben tra it hon/khong tra gi trong khi arena van tra thuong DUNG
                // gap doi so cuoc goc cho nguoi thang - sinh xu tu khong khi. Hoan lai ben da
                // tru neu ben kia thieu, huy tran dau thay vi tao arena.
                // 19/10/2026: ky quy nguyen tu qua real.ThachDauCuoc (truoc: tru hai doi truong nhung arena kieu 4 KHONG
                // tra ai - ca hai cuoc bien mat). Muc cuoc theo cap thap nhat cua CA HAI nhom, cong chong clone moi
                // thanh vien, nguoi thang nhan 2 x cuoc - phi.
                final long cuoc = (long) p.moneyThachdau;
                final java.util.List<Char> moiNguoi = new java.util.ArrayList<Char>();
                for (final Object o : p.party.userParty) {
                    moiNguoi.add((Char) o);
                }
                for (final Object o : player.party.userParty) {
                    moiNguoi.add((Char) o);
                }
                String loiTran = cuoc > 0L ? ThachDauCuoc.kiemCuoc(cuoc, moiNguoi.toArray(new Char[0])) : "Số tiền cược không hợp lệ.";
                if (loiTran == null) {
                    for (final Char o : moiNguoi) {
                        if (com.htbb.pokemon.ChongClone.chanCaHai(o, o, player, com.htbb.pokemon.ChongClone.KIEU_THACH_DAU)) {
                            loiTran = "";
                            break;
                        }
                    }
                }
                final int idTran = Map.IDARENA;
                if (loiTran == null) {
                    loiTran = ThachDauCuoc.kyQuy(p, player, cuoc, 4, idTran);
                }
                if (loiTran != null) {
                    p.nameCompetitor = "";
                    p.moneyThachdau = 0;
                    player.nameCompetitor = "";
                    player.moneyThachdau = 0;
                    if (!loiTran.isEmpty()) {
                        p.sendMessage(MessageCreator.createServerAlertMessage(loiTran, ""));
                        player.sendMessage(MessageCreator.createServerAlertMessage(loiTran, ""));
                    }
                    return;
                }
                Map.IDARENA = (idTran + 1) % 2000000000;

                TeamArena team1 = new TeamArena(TeamArena.genIDTeam());
                TeamArena team2 = new TeamArena(TeamArena.genIDTeam());
                ArenaMap mm = (ArenaMap) RealController.mapList.get(205);
                ArenaData arena = new ArenaData();
                arena.addTeam(team1);
                arena.addTeam(team2);
                arena.idArena = idTran;
                arena.typeArena = 4;
                arena.start();
                mm.doAddArena(arena);
                p.idArena = idTran;
                player.idArena = idTran;
                arena.moneyThachdau = (int) Math.min(Integer.MAX_VALUE,
                        com.htbb.pokemon.ThachDauLogic.chia(ThachDauCuoc.cauHinh(), cuoc, 0).tra0);
                team1.addMember(p);
                team2.addMember(player);
                Message m = null;
                m = new Message(65);
                m.dos.writeShort(p.id);
                p.pk = 14;
                m.dos.writeByte(1);
                m.dos.writeByte(14);
                p.timeUsePK = System.currentTimeMillis();
                p.sendMessage(m);
                p.sendToNearPlayer(m);

                for (int i = 0; i < p.party.userParty.size(); i++) {
                    Char pp = (Char) p.party.userParty.get(i);
                    if (pp.id != p.id) {
                        m = new Message(65);
                        m.dos.writeShort(pp.id);
                        pp.pk = 14;
                        m.dos.writeByte(1);
                        m.dos.writeByte(14);
                        pp.timeUsePK = System.currentTimeMillis();
                        pp.sendMessage(m);
                        pp.sendToNearPlayer(m);
                        pp.idArena = idTran;
                        team1.addMember(pp);
                    }
                }

                for (int ix = 0; ix < player.party.userParty.size(); ix++) {
                    Char pp = (Char) player.party.userParty.get(ix);
                    if (pp.id != player.id) {
                        m = new Message(65);
                        m.dos.writeShort(pp.id);
                        pp.pk = 15;
                        m.dos.writeByte(1);
                        m.dos.writeByte(15);
                        pp.timeUsePK = System.currentTimeMillis();
                        pp.sendMessage(m);
                        pp.sendToNearPlayer(m);
                        pp.idArena = idTran;
                        team2.addMember(pp);
                    }
                }

                m = new Message(65);
                m.dos.writeShort(player.id);
                player.pk = 15;
                m.dos.writeByte(1);
                m.dos.writeByte(15);
                player.timeUsePK = System.currentTimeMillis();
                player.sendMessage(m);
                player.sendToNearPlayer(m);
                arena.sendInfoMatch = true;
                arena.timeWaitFight = System.currentTimeMillis() + 30000L;
                player.nameCompetitor = "";
                p.nameCompetitor = "";
                p.moneyThachdau = 0;
                player.moneyThachdau = 0;
            } else {
                player.sendMessage(MessageCreator.createServerAlertMessage(player.nameCompetitor + " đã thoát khỏi game", ""));
            }
        } catch (Exception var11) {
            var11.printStackTrace();
        }
    }

    static void doDotDen(Map map, Char player, int type_dotden) {
        if (!Char.isFinishAllSuKienTrungThul2016()) {
            if (type_dotden <= 2) {
                if (player.potions[136] >= 1) {
                    if (type_dotden != 1 || player.getLuong() >= 4) {
                        if (type_dotden != 2 || player.getxu() >= 5000000L) {
                            addXpCharEvent(player, 100000L, false, "doDotDen");
                            int[] per = new int[]{
                                5000,
                                5000,
                                5000,
                                300,
                                100,
                                100,
                                50,
                                3000,
                                3000,
                                100,
                                100,
                                100,
                                100,
                                100,
                                100,
                                5000,
                                5000,
                                5000,
                                5000,
                                5000,
                                500,
                                500,
                                500,
                                500,
                                500,
                                200,
                                0,
                                50,
                                5000,
                                5000,
                                5000,
                                50,
                                50,
                                100,
                                100
                            };
                            int[] idgift = new int[]{
                                93,
                                95,
                                117,
                                35,
                                75,
                                84,
                                100,
                                GemTemplate.BOT_TRANG,
                                GemTemplate.BOT_XANH,
                                GemTemplate.BOT_DO,
                                -1,
                                -1,
                                -1,
                                -1,
                                -1,
                                -1,
                                -1,
                                -1,
                                -1,
                                -1,
                                -1,
                                -1,
                                -1,
                                -1,
                                -1,
                                -2,
                                -3,
                                GemTemplate.BOT_XANH_LA,
                                114,
                                112,
                                113,
                                119,
                                -4,
                                -5,
                                -6
                            };
                            int[] typegift = new int[]{
                                4, 4, 4, 4, 4, 4, 4, 6, 6, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -2, -3, 6, 4, 4, 4, 4, -4, -5, -6
                            };
                            int[] soluongmax = new int[]{
                                100,
                                100,
                                3,
                                1,
                                1,
                                1,
                                1,
                                3,
                                3,
                                3,
                                10000,
                                20000,
                                30000,
                                40000,
                                50000,
                                100,
                                200,
                                300,
                                400,
                                500,
                                50,
                                60,
                                70,
                                80,
                                90,
                                1,
                                1,
                                3,
                                1,
                                1,
                                1,
                                1,
                                1,
                                1,
                                1,
                                1,
                                1
                            };
                            int[] soluongmin = new int[]{
                                50,
                                50,
                                1,
                                1,
                                1,
                                1,
                                1,
                                1,
                                1,
                                1,
                                10000,
                                20000,
                                30000,
                                40000,
                                50000,
                                100,
                                200,
                                300,
                                400,
                                500,
                                50,
                                60,
                                70,
                                80,
                                90,
                                1,
                                1,
                                1,
                                1,
                                1,
                                1,
                                1,
                                1,
                                1,
                                1,
                                1,
                                1
                            };
                            String info = "Chúc mừng bạn nhận được ";
                            int index = Char.getIndexRandom(per);
                            if (typegift[index] == -2) {
                                if (!player.isFullInventory()) {
                                    int[] minute = new int[]{1440, 4320, 7200, 10080};
                                    Item newItem = ItemLuckyDraw.createItemCoat(player, Map.r.nextInt(2), false, minute[Map.r.nextInt(minute.length)]);
                                    player.iItems.add(newItem);
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                                    info = info + newItem.getName() + " thời hạn " + newItem.minuteExist + " phút";
                                    player.sendMessage(MessageCreator.createMsgChat(player.id, info));
                                } else {
                                    player.addXu((long) (100 + Map.r.nextInt(100)), "map 58");
                                    info = info + soluongmax[index] + " xu";
                                    player.sendMessage(MessageCreator.createMsgChat(player.id, info));
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                }
                            } else if (typegift[index] == -3) {
                                if (!player.isFullInventory()) {
                                    int[] minute = new int[]{1440, 4320, 7200, 10080};
                                    Item newItem = ItemLuckyDraw.createItemCoat(player, Map.r.nextInt(2), false, minute[Map.r.nextInt(minute.length)]);
                                    player.iItems.add(newItem);
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                                    info = info + newItem.getName() + " thời hạn " + newItem.minuteExist + " phút";
                                    player.sendMessage(MessageCreator.createMsgChat(player.id, info));
                                } else {
                                    player.addXu((long) (100 + Map.r.nextInt(100)), "map 55");
                                    info = info + soluongmax[index] + " xu";
                                    player.sendMessage(MessageCreator.createMsgChat(player.id, info));
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                }
                            } else if (typegift[index] == -5) {
                                if (!player.isFullInventory()) {
                                    int[] minute = new int[]{1440, 4320, 7200, 10080};
                                    Item newItem = ItemLuckyDraw.createtemCoat(742, player, Map.r.nextInt(2), true, minute[Map.r.nextInt(minute.length)]);
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                                    info = info + newItem.getName() + " thời hạn " + newItem.minuteExist + " phút";
                                    player.sendMessage(MessageCreator.createMsgChat(player.id, info));
                                } else {
                                    player.addXu((long) (100 + Map.r.nextInt(100)), "map 55");
                                    info = info + soluongmax[index] + " xu";
                                    player.sendMessage(MessageCreator.createMsgChat(player.id, info));
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                }
                            } else if (typegift[index] == -4) {
                                int soluong = soluongmin[index];
                                if (soluongmin[index] < soluongmax[index]) {
                                    soluong += Map.r.nextInt(soluongmax[index] - soluongmin[index] + 1);
                                }

                                boolean lock = false;
                                int idgem = Map.ID_GEM_TU_BINH[2 + Map.r.nextInt(2)][Map.r.nextInt(Map.ID_GEM_TU_BINH[0].length)];
                                player.doAddGemItem(idgem, soluong, lock);
                                info = info + soluong + " " + Map.gemTemplate[idgem].name + (lock ? " khoá" : "");
                                player.sendMessage(MessageCreator.createCharGemItem(player));
                                player.sendMessage(MessageCreator.createMsgChat(player.id, info));
                            } else if (typegift[index] == -6) {
                                int time = Map.r.nextInt(3) + 3;
                                player.createPoro((long) time, false);
                                info = info + " Wooloo";
                            } else if (typegift[index] == -1) {
                                player.addXu((long) soluongmax[index], "map 56");
                                info = info + soluongmax[index] + " xu";
                                player.sendMessage(MessageCreator.createMsgChat(player.id, info));
                                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                            } else if (typegift[index] == 4) {
                                int soluong = soluongmin[index];
                                if (soluongmin[index] < soluongmax[index]) {
                                    soluong += Map.r.nextInt(soluongmax[index] - soluongmin[index] + 1);
                                }

                                player.potions[idgift[index]] = player.potions[idgift[index]] + soluong;
                                info = info + soluong + " " + LoginHandler.PORTION_NAME[idgift[index]];
                                player.sendMessage(MessageCreator.createMsgChat(player.id, info));
                                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                            } else if (typegift[index] == 6) {
                                int soluong = soluongmin[index];
                                if (soluongmin[index] < soluongmax[index]) {
                                    soluong += Map.r.nextInt(soluongmax[index] - soluongmin[index] + 1);
                                }

                                boolean lock = Map.r.nextInt(2) == 1;
                                player.doAddGemItem(idgift[index], soluong, lock);
                                info = info + soluong + " " + Map.gemTemplate[idgift[index]].name + (lock ? " khoá" : "");
                                player.sendMessage(MessageCreator.createCharGemItem(player));
                                player.sendMessage(MessageCreator.createMsgChat(player.id, info));
                                if (idgift[index] == GemTemplate.LKD_35 || idgift[index] == GemTemplate.LKD_40) {
                                    try {
                                        Map.sendAllCharServer(
                                                -1,
                                                MessageCreator.createServerAlertAutoOffMessage(
                                                        player.charname + " đã nhận được " + Map.gemTemplate[idgift[index]].name + " đốt đèn"
                                                )
                                        );
                                    } catch (IOException var13) {
                                    }
                                }
                            }

                            player.potions[136]--;
                            if (player.potions[136] < 0) {
                                player.potions[136] = 0;
                            }

                            Database.instance
                                    .saveOrtherLog("", player.charname, info + " " + (type_dotden == 1 ? "dung luong " : (type_dotden == 2 ? "dung xu" : "")), "dotden");
                            if (type_dotden != 0) {
                                EffectBuff ef = new EffectBuff(EffectBuff.THA_DEN, System.currentTimeMillis() + 3000L);
                                ef.type = EffectBuff.BY_ACTOR;
                                ef.damFly = 0;
                                if (Char.isSuKienTrungThul2016()) {
                                    player.banphaohoa++;
                                    Database.instance.addCharEventTrungthu(player);
                                }

                                player.sendMessage(
                                        MessageCreator.createMsgAddDynamicNewEffect(
                                                ef.idEff, ef.timeExist, ef.type, player.id, player.x / 16, player.y / 16, player.cat, EffectBuff.ODER_PAINT[ef.idEff], ef.damFly
                                        )
                                );
                                player.sendToNearPlayer(
                                        MessageCreator.createMsgAddDynamicNewEffect(
                                                ef.idEff, ef.timeExist, ef.type, player.id, player.x / 16, player.y / 16, player.cat, EffectBuff.ODER_PAINT[ef.idEff], ef.damFly
                                        )
                                );
                            }

                            if (type_dotden == 1) {
                                player.subLuong(4L);
                                Database.instance.saveLogThongKeChiTieu("tha den luong", 0, 1, 4L, "luong");
                            } else if (type_dotden == 2) {
                                player.subXu(5000000L, false, "");
                                Database.instance.saveLogThongKeChiTieu("tha den xu", 0, 1, 5000000L, "xu");
                            }

                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                        }
                    }
                }
            }
        }
    }

    static void doNapSMS(Map map, Char player, int type, String cardID, String cardCode) {
        try {
            // SECURITY: disabled external card top-up that sent card ID/code + account info to third-party (teamobi).
            player.sendMessage(MessageCreator.createServerAlertMessage("Chức năng nạp thẻ hiện không khả dụng.", ""));
            if (true) {
                return;
            }
            if (!Map.onSMS) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Chức năng hiện đang tạm khoá.", ""));
                return;
            }

            if (Map.openLog && System.currentTimeMillis() - player.getSession().timeNapMoney < 30000L) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Có lỗi xảy ra. Xin thử lại sau", ""));
                return;
            }

            player.getSession().timeNapMoney = System.currentTimeMillis();
            String url = "";
            url = Net.url[4][0];
            url = url
                    + player.charname
                    + "&cardid="
                    + cardID
                    + "&cardcode="
                    + cardCode
                    + "&provider="
                    + player.getSession().clientProvider
                    + "&game=9&moneytype="
                    + (type == 0 ? "luong" : "xu")
                    + "&server="
                    + TeamServer.server
                    + "&agent="
                    + player.getSession().agentProvider
                    + "&account="
                    + player.getAccountName();
            String noidug = Net.getHttp(url);
            if (noidug.indexOf("http") > -1 || noidug.indexOf("www") > -1) {
                noidug = "Có lỗi xảy ra. Lỗi 42";
            }

            Database.instance.saveLogNapMoney(player.charname, cardID + "|" + cardCode + "_" + noidug);
            player.sendMessage(MessageCreator.createServerAlertMessage(noidug, ""));
        } catch (Exception var7) {
        }
    }

    static void doSendDetailChar(Map map, Char player, Message msg) {
        try {
            Char p = CharManager.instance.getByCharID(msg.dis.readShort());
            if (p == null) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Không tìm thấy thông tin", ""));
                return;
            }

            byte typeView = 0;

            try {
                typeView = msg.dis.readByte();
            } catch (Exception var11) {
            }

            Message m = new Message(-22);
            m.dos.writeByte(typeView);
            if (typeView == 0) {
                if (p.isCharCopy()) {
                    player.sendMessage(MessageCreator.createServerAlertMessage(p.getinfoCharCopy(), ""));
                    return;
                }

                m.dos.writeUTF(p.charname);
                m.dos.writeByte(p.headStyle);
                m.dos.writeByte(p.lvDetail.lv);
                Vector<Item> charWearing = p.changePosRing(p.getCharWearing());
                m.dos.writeByte(charWearing.size());

                for (int i = 0; i < charWearing.size(); i++) {
                    Item item = (Item) charWearing.get(i);
                    m.dos.writeByte(item.clazz);
                    m.dos.writeShort(item.getTemplate().id);
                    m.dos.writeByte(item.level);
                    m.dos.writeByte(item.plus);
                    m.dos.writeShort(item.id);
                    m.dos.writeByte(item.colorName);

                    for (int j = 0; j < 5; j++) {
                        m.dos.writeShort(item.atb[j + 28]);
                    }

                    m.dos.writeByte(item.heItem);
                    m.dos.writeByte(item.beKick);
                    m.dos.writeByte(item.hangItem);
                    m.dos.writeByte(item.magic_physic);
                    m.dos.writeByte(item.lock);
                    m.dos.writeUTF(item.charSeal);
                    m.dos.write(item.newAtb);
                    m.dos.write(item.lockAtb);
                    m.dos.write(item.addMoreLevelSkill);
                }

                m.dos.writeShort(p.idClan);
                m.dos.writeByte(p.wModel.idModel);
                short[] idModel = p.getIDModel();

                for (int i = 0; i < idModel.length; i++) {
                    m.dos.writeShort(idModel[i]);
                }

                int type = -1;
                if (p.animalRide != null) {
                    type = p.animalRide.idImg;
                }

                m.dos.writeByte(type);
                if (type != -1) {
                    String info = com.htbb.pokemon.ChuPokemon.tenThuCuoi(p.animalRide.name) + " lv " + p.animalRide.level + "\n" + p.animalRide.getInfoSendUser();
                    m.dos.writeUTF(info);
                    int idIcon = Animal.idImgIcon[p.animalRide.idImg];
                    if (p.rankGov == 0) {
                        idIcon = 13;
                    }

                    m.dos.writeByte(idIcon);
                }
            } else {
                Vector<Item> awItems = p.getAnimalWearing();
                byte len = (byte) (p.animalRide != null ? awItems.size() : -1);
                m.dos.writeByte(len);

                for (int i = 0; i < len; i++) {
                    Item item = (Item) awItems.get(i);
                    m.dos.writeByte(item.clazz);
                    m.dos.writeShort(item.id);
                    m.dos.writeShort(item.getTemplate().id);
                    m.dos.writeByte(item.plus);
                    m.dos.writeByte(item.level);
                    m.dos.writeShort(item.mDurable);
                    m.dos.writeShort(item.durable);
                    m.dos.writeByte(item.colorName);

                    for (int j = 0; j < 5; j++) {
                        m.dos.writeShort(item.atb[j + 28]);
                    }

                    m.dos.writeByte(item.heItem);
                    m.dos.writeByte(item.beKick);
                    m.dos.writeByte(item.hangItem);
                    m.dos.writeByte(item.magic_physic);
                    m.dos.writeByte(item.lock);
                    m.dos.writeUTF(item.charSeal);
                    m.dos.write(item.newAtb);
                    m.dos.write(item.lockAtb);
                    m.dos.write(item.addMoreLevelSkill);
                }

                byte[] fr = Animal.fr;
                if (p.animalRide != null) {
                    // O giua bang "Trang bi thu" ve CON THU THAT, khong phai skin.
                    //
                    // Ban skin dau tien ghi de cho nay bang dai 3 khung cua skin. Nhung o do la cho
                    // xem con linh thu CUA MINH - con dang mang chi so, dang len cap. Skin chi la mon
                    // do khoac ngoai, no da co o rieng trong bang roi (o dau tien ben phai), nen de no
                    // chiem luon cho xem con thu la mat thong tin chu khong them duoc gi.
                    // Hinh skin van hien binh thuong tren ban do, do la duong idThutool khac han.
                    if (p.rankGov == 0) {
                        m.dos.writeByte(fr[6]);
                        m.dos.write(DataGame.imgAvtAnimal[6]);
                    } else {
                        m.dos.writeByte(fr[p.animalRide.idImg]);
                        m.dos.write(DataGame.imgAvtAnimal[p.animalRide.idImg]);
                    }
                }
            }

            player.sendMessage(m);
        } catch (Exception var12) {
            var12.printStackTrace();
        }
    }

    static void doBuyModelClothe(Char player, ShopTemplate item, int nday) {
        ItemTemplates template = null;
        Item newItem = null;
        switch (item.id) {
            case 50:
                template = (ItemTemplates) ((Hashtable) Map.itemTemplates.get(0)).get(new Integer(267));
                newItem = new Item(template, false, 0, 0, 267);
                break;
            case 51:
                template = (ItemTemplates) ((Hashtable) Map.itemTemplates.get(0)).get(new Integer(265));
                newItem = new Item(template, false, 0, 0, 265);
                break;
            case 52:
                template = (ItemTemplates) ((Hashtable) Map.itemTemplates.get(0)).get(new Integer(579));
                newItem = new Item(template, false, -1, 0, 579);
                break;
            case 53:
                template = (ItemTemplates) ((Hashtable) Map.itemTemplates.get(0)).get(new Integer(508));
                newItem = new Item(template, false, -1, 0, 508);
                break;
            case 54:
                template = (ItemTemplates) ((Hashtable) Map.itemTemplates.get(0)).get(new Integer(266));
                newItem = new Item(template, false, 0, 0, 266);
                break;
            case 55:
                template = (ItemTemplates) ((Hashtable) Map.itemTemplates.get(0)).get(new Integer(264));
                newItem = new Item(template, false, -1, 0, 264);
                break;
            case 56:
                template = (ItemTemplates) ((Hashtable) Map.itemTemplates.get(0)).get(new Integer(507));
                newItem = new Item(template, false, -1, 0, 507);
                break;
            case 57:
                template = (ItemTemplates) ((Hashtable) Map.itemTemplates.get(0)).get(new Integer(580));
                newItem = new Item(template, false, -1, 0, 580);
                break;
            case 58:
                template = (ItemTemplates) ((Hashtable) Map.itemTemplates.get(0)).get(new Integer(581));
                newItem = new Item(template, false, -1, 0, 581);
                break;
            case 86:
                template = (ItemTemplates) ((Hashtable) Map.itemTemplates.get(5)).get(new Integer(573));
                newItem = new Item(template, false, -1, 0, 573);
                break;
            case 87:
                template = (ItemTemplates) ((Hashtable) Map.itemTemplates.get(5)).get(new Integer(574));
                newItem = new Item(template, false, -1, 0, 574);
                break;
            case 88:
                template = (ItemTemplates) ((Hashtable) Map.itemTemplates.get(5)).get(new Integer(575));
                newItem = new Item(template, false, -1, 0, 575);
                break;
            case 89:
                template = (ItemTemplates) ((Hashtable) Map.itemTemplates.get(5)).get(new Integer(576));
                newItem = new Item(template, false, -1, 0, 576);
                break;
            case 90:
                template = (ItemTemplates) ((Hashtable) Map.itemTemplates.get(5)).get(new Integer(577));
                newItem = new Item(template, false, -1, 0, 577);
                break;
            case 91:
                template = (ItemTemplates) ((Hashtable) Map.itemTemplates.get(5)).get(new Integer(578));
                newItem = new Item(template, false, -1, 0, 578);
                break;
            case 105:
                template = (ItemTemplates) ((Hashtable) Map.itemTemplates.get(5)).get(new Integer(582));
                newItem = new Item(template, false, -1, 0, 582);
                break;
            case 106:
                template = (ItemTemplates) ((Hashtable) Map.itemTemplates.get(5)).get(new Integer(583));
                newItem = new Item(template, false, -1, 0, 583);
                break;
            case 113:
                template = (ItemTemplates) ((Hashtable) Map.itemTemplates.get(5)).get(new Integer(609));
                newItem = new Item(template, false, -1, 0, 609);
                break;
            case 114:
                template = (ItemTemplates) ((Hashtable) Map.itemTemplates.get(5)).get(new Integer(610));
                newItem = new Item(template, false, -1, 0, 610);
                break;
            case 137:
                template = (ItemTemplates) ((Hashtable) Map.itemTemplates.get(5)).get(new Integer(620));
                newItem = new Item(template, false, -1, 0, 620);
                break;
            case 138:
                template = (ItemTemplates) ((Hashtable) Map.itemTemplates.get(5)).get(new Integer(621));
                newItem = new Item(template, false, -1, 0, 621);
                break;
            case 139:
                template = (ItemTemplates) ((Hashtable) Map.itemTemplates.get(5)).get(new Integer(622));
                newItem = new Item(template, false, -1, 0, 622);
                break;
            case 140:
                template = (ItemTemplates) ((Hashtable) Map.itemTemplates.get(5)).get(new Integer(623));
                newItem = new Item(template, false, -1, 0, 623);
                break;
            case 142:
                template = (ItemTemplates) ((Hashtable) Map.itemTemplates.get(5)).get(new Integer(671));
                newItem = new Item(template, false, -1, 0, 671);
                break;
            case 143:
                template = (ItemTemplates) ((Hashtable) Map.itemTemplates.get(5)).get(new Integer(672));
                newItem = new Item(template, false, -1, 0, 672);
                break;
            case 144:
                template = (ItemTemplates) ((Hashtable) Map.itemTemplates.get(5)).get(new Integer(673));
                newItem = new Item(template, false, -1, 0, 673);
                break;
            case 145:
                template = (ItemTemplates) ((Hashtable) Map.itemTemplates.get(5)).get(new Integer(674));
                newItem = new Item(template, false, -1, 0, 674);
            case 723:
            case 724:
        }

        if (newItem != null) {
            newItem.id = player.getIDItem();
            newItem.owner = player.charDBID;
            newItem.level = newItem.getTemplate().level;
            player.iItems.add(newItem);
            newItem.clazz = 0;
            newItem.identify = player.charDBID;
            if (nday > 0) {
                newItem.minuteExist = nday * 24 * 60;
                newItem.timeLoan = System.currentTimeMillis();
            }

            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
            Database.instance.saveLogBuyItemShop(player.charname, newItem.getTemplate().name, 0, item.getPrice(Map.isSale), player.getxu() + "_" + player.getLuong());
            Database.instance.saveItem(newItem);
        }
    }

    static void doSendTop(Map map, Char player, Message msg) {
        try {
            Message m = new Message(-19);
            byte type = msg.dis.readByte();
            byte page = msg.dis.readByte();
            m.dos.writeByte(type);
            int start = page * 10;
            int end = page * 10 + 10;
            if (start < 0) {
                start = 0;
            }
            if (end < 0) {
                end = 0;
            }
            switch (type) {
                case 3:
                    m.dos.writeUTF(com.htbb.pokemon.ChuPokemon.doi("Top đại gia"));
                    if (start >= Map.topRich.size()) {
                        start = Map.topRich.size();
                    }

                    if (end >= Map.topRich.size()) {
                        end = Map.topRich.size();
                    }

                    m.dos.writeShort(end - start);

                    for (int i = start; i < end; i++) {
                        CharInfo charinfo = (CharInfo) Map.topRich.get(i);
                        String online = " - off";
                        if (CharManager.instance.getCharByCharName(charinfo.name) != null) {
                            online = " - on";
                        }

                        m.dos.writeUTF(charinfo.name + online);
                        m.dos.writeByte(charinfo.headStyle);
                        m.dos.writeByte(charinfo.level);
                        m.dos.writeByte(charinfo.wearingItem.size());

                        for (int j = 0; j < charinfo.wearingItem.size(); j++) {
                            ItemInfo item = (ItemInfo) charinfo.wearingItem.get(j);
                            m.dos.writeByte(item.charClass);
                            m.dos.writeShort(item.idTemplate);
                            m.dos.writeByte(item.level);
                            m.dos.writeByte(item.plus);
                        }

                        m.dos.writeShort(charinfo.idClan);
                        m.dos.writeByte(charinfo.titlesClan);
                        m.dos.writeLong(SuKien.xuChoTop(charinfo.money));
                        m.dos.writeInt(SuKien.luongChoTop(charinfo.luong));
                        m.dos.writeByte(charinfo.country);
                    }
                    break;
                case 4:
                    m.dos.writeUTF(com.htbb.pokemon.ChuPokemon.doi("Top sức mạnh"));
                    if (start >= Map.topLv.size()) {
                        start = Map.topLv.size();
                    }

                    if (end >= Map.topLv.size()) {
                        end = Map.topLv.size();
                    }

                    m.dos.writeShort(end - start);

                    for (int i = start; i < end; i++) {
                        CharInfo charinfo = (CharInfo) Map.topLv.get(i);
                        String online = " - off";
                        if (CharManager.instance.getCharByCharName(charinfo.name) != null) {
                            online = " - on";
                        }

                        m.dos.writeUTF(charinfo.name + online);
                        m.dos.writeByte(charinfo.headStyle);
                        m.dos.writeByte(charinfo.level);
                        m.dos.writeByte(charinfo.wearingItem.size());

                        for (int j = 0; j < charinfo.wearingItem.size(); j++) {
                            ItemInfo item = (ItemInfo) charinfo.wearingItem.get(j);
                            m.dos.writeByte(item.charClass);
                            m.dos.writeShort(item.idTemplate);
                            m.dos.writeByte(item.level);
                            m.dos.writeByte(item.plus);
                        }

                        m.dos.writeShort(charinfo.idClan);
                        m.dos.writeByte(charinfo.titlesClan);
                        m.dos.writeLong(SuKien.xuChoTop(charinfo.money));
                        m.dos.writeInt(SuKien.luongChoTop(charinfo.luong));
                        m.dos.writeByte(charinfo.country);
                    }
                    break;
                case 5:
                    m.dos.writeUTF(com.htbb.pokemon.ChuPokemon.doi("Top đội"));
                    if (start >= Map.topClan.size()) {
                        start = Map.topClan.size();
                    }

                    if (end >= Map.topClan.size()) {
                        end = Map.topClan.size();
                    }

                    m.dos.writeShort(end - start);

                    for (int i = start; i < end; i++) {
                        NewClan clan = (NewClan) Map.topClan.get(i);
                        m.dos.writeShort(clan.id);
                        m.dos.writeUTF(clan.name);
                        m.dos.writeUTF(clan.master);
                        m.dos.writeByte(clan.level);
                        m.dos.writeShort(clan.getAllMember().size());
                        m.dos.writeLong(SuKien.xuChoTop(clan.money));
                        m.dos.writeByte(clan.country);
                    }
                case 6:
                default:
                    break;
                case 7:
                    final int mucBv = XepHangBangVang.muc(player.session == null ? -1 : player.session.banClient, player.myCountry);
                    if (mucBv >= 0) {
                        map.doSendTopNation(player, mucBv);
                        return;
                    }
                    player.sendMessage(
                            MessageCreator.createMsgMenu(
                                    new String[]{
                                        "Top sức mạnh Kanto",
                                        "Top sức mạnh Johto",
                                        "Top sức mạnh Hoenn",
                                        "Top công trạng Kanto",
                                        "Top công trạng Johto",
                                        "Top công trạng Hoenn",
                                        "Top chuỗi thắng Kanto",
                                        "Top chuỗi thắng Johto",
                                        "Top chuỗi thắng Hoenn",
                                        "Danh sách đối thủ"
                                    },
                                    -504,
                                    0
                            )
                    );
                    return;
                case 8:
                    m.dos.writeUTF(com.htbb.pokemon.ChuPokemon.doi("TOP NHÓM ĐUA"));
                    int len = Map.topRich.size();
                    if (len > 10) {
                        len = 10;
                    }

                    m.dos.writeShort(len);

                    for (int i = start; i < end; i++) {
                        CharInfo charinfo = (CharInfo) Map.topRich.get(i);
                        String online = " - off";
                        if (CharManager.instance.getCharByCharName(charinfo.name) != null) {
                            online = " - on";
                        }

                        m.dos.writeUTF(charinfo.name + online);
                        m.dos.writeByte(charinfo.headStyle);
                        m.dos.writeByte(-1);
                        m.dos.writeByte(charinfo.wearingItem.size());

                        for (int j = 0; j < charinfo.wearingItem.size(); j++) {
                            ItemInfo item = (ItemInfo) charinfo.wearingItem.get(j);
                            m.dos.writeByte(item.charClass);
                            m.dos.writeShort(item.idTemplate);
                            m.dos.writeByte(item.level);
                            m.dos.writeByte(item.plus);
                        }

                        m.dos.writeShort(charinfo.idClan);
                        m.dos.writeByte(charinfo.titlesClan);
                        m.dos.writeLong(SuKien.xuChoTop(charinfo.money));
                        m.dos.writeInt(SuKien.luongChoTop(charinfo.luong));
                        m.dos.writeByte(charinfo.country);
                    }
            }

            player.sendMessage(m);
        } catch (Exception var14) {
        }
    }

    static void doChangeMap(Map map, Char player, Message message) {
        try {
            int toMapID = message.dis.readShort();
            if (toMapID == player.mapID || player.mapID == 118 && player.monster != null && player.mapID == player.monster.map.mapId) {
                Map toMap = (Map) RealController.mapList.get(player.mapID);
                Message m = MessageCreator.createMapMessage(player);
                player.sendMessage(m);
                map.doSendDynamicObj(player);
                player.map.sendInfoNpc(player);
                player.map.sendDynamicEff(player);
                player.checkCreateThangBe(toMapID);
                if (player.isNguoiTuyet()) {
                    MessageCreator.createMsgCharMonster(player, player);
                }

                player.sendMessage(MessageCreator.createCharWearingMessage(player, player));

                try {
                    if (player.mapID == Map.mapIDFarm) {
                        map.doSendFarm(player);
                    }

                    if (toMap != null) {
                        for (int i = 0; i < Map.bossLocation.size(); i++) {
                            if (((BossLocaltion) Map.bossLocation.get(i)).mapID == toMapID) {
                                BossLocaltion b = (BossLocaltion) Map.bossLocation.get(i);
                                player.sendMessage(MessageCreator.createGate(b.type, b.x, b.y, b.mapID));
                                break;
                            }
                        }

                        if (toMap != null
                                && (toMap.mapIDLoadMap == 80
                                || toMap.mapIDLoadMap == 70
                                || toMap.mapIDLoadMap == 0
                                || toMap.mapIDLoadMap == 1
                                || toMap.mapIDLoadMap == 2
                                || toMap.mapIDLoadMap == 12
                                || toMap.mapIDLoadMap == 106)) {
                            player.sendMessage(MessageCreator.createMessageLocation(real.LangKhoiDau.oCong(player, player.inCountry)));
                        }

                        return;
                    }
                } catch (Exception var38) {
                }
            } else if (toMapID == -500) {
                player.calculateAttrib();
                player.isCrazyChangeMap(player.isCrazy);
                player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                if (player.map.isMapChienTruongMoba()) {
                    player.sendInfoChienTruong(Char.ID_ALL_KHU_CHIEN_TRUONG_MOBA, 0);
                    player.sendInfoChienTruong(Char.ID_NOI_LUC, 0);
                    player.map.getRegionMoba(player.region).sendInfoPlayer(player);
                    CharChienTruong c = MapChienTruongMoba.getCharChienTruong(player.charname);
                    if (c.getTimeBuffNoiLuc() > 1L) {
                        Message msg = MessageCreator.createMsgNewEffectSkill5Long(
                                EffectBuff.EFF_BUFF_NOI_LUC, (int) c.getTimeBuffNoiLuc() * 1000, player.id, player.cat, true, false
                        );
                        player.sendMessage(msg);
                    }
                }

                player.sendInfoNuiChauBau();
                player.isDoChangeMap = false;
                NhiemVuEngine.suKien(player, "den_ban_do", String.valueOf(player.mapID), 1); // danh hieu kham pha
                DanhHieuGame.khiVaoBanDo(player); // lan dau trong phien: nhan danh hieu, so moi, thuong mua PvP
                KenhChatGame.khiVaoBanDo(player); // lan dau trong phien: kenh chat, ban be online
                NgoaiHinhGame.guiONhanh(player);  // 0.9.8: trang chieu (o nhanh) cua nhan vat - may chu giu
                map.doJoinMapOk(player);
            } else if (player.mapID == Map.idMapChanNui) {
                map.doReturnVillage(player);
            } else if (map.isMapOffline && player.mapID_the_mua_ban > -1) {
                map.playerExit(player);
                player.region = player.region_the_mua_ban;
                map.move2Map(player, player.x_the_mua_ban, player.y_the_mua_ban, player.mapID_the_mua_ban, player.inCountry);
                player.mapID_the_mua_ban = -1;
            } else {
                boolean checklocation = true;
                player.isDoChangeMap = true;
                map.vx = 0;
                player.idItem.removeAllElements();
                player.idItemQuest.removeAllElements();
                player.idPotion.removeAllElements();
                if (Map.getTown[player.inCountry] && Map.giveCardFail(player)) {
                    map.doSend2AllChar(MessageCreator.createServerAlertAutoOffMessage(player.charname + " giao thẻ thất bại"), player.inCountry);
                    map.sendAllPlayer(MessageCreator.createMsgStartGetTown(player.inCountry), player.inCountry);
                }

                if (toMapID == 17) {
                    toMapID = player.mapID;
                }

                int toTX = message.dis.readShort();
                int toTY = message.dis.readShort();
                // lo #3-b loi.xlsx (tam): log chan doan "Canh rung dong -> nham Binh Kieu".
                // Chi log khi dang o nhom Bach Hac (18, 661-664). XOA sau khi sua xong.
                if (map.mapIDLoadMap == 18) {
                    System.out.println("[DO-CHANGE-MAP] player=" + player.charname + " tu mapId=" + map.mapId
                            + " mapIDLoadMap=" + map.mapIDLoadMap + " -> toMapID(client gui)=" + toMapID
                            + " toTX=" + toTX + " toTY=" + toTY);
                }
                if (toMapID != 9 && toMapID != 481 && toMapID != 482 && toMapID != 483 && toMapID != 484) {
                    if (toMapID == 201) {
                        if (map.mapIDLoadMap == 9 && (toTX != 39 || toTY != 90 || player.y / 16 > 10 && player.idClan > -1)) {
                            toMapID = player.mapID;
                            toTX = player.x / 16;
                            toTY = player.y / 16;
                            CharManager.instance.kickPlayer(player, "map 3");
                            Database.instance.saveOrtherLog("", player.charname, "hack chuyen map 9", "hcm");
                        }
                    } else if (toMapID == player.mapID) {
                        toMapID = player.mapID;
                        toTX = player.x / 16;
                        toTY = player.y / 16;
                        CharManager.instance.kickPlayer(player, "map 4");
                        Database.instance.saveOrtherLog("", player.charname, "hack chuyen map cung map " + player.mapID, "hcm");
                    }
                } else if (map.mapIDLoadMap == 8) {
                    if (toTX != 28 || toTY != 98) {
                        toMapID = player.mapID;
                        toTX = player.x / 16;
                        toTY = player.y / 16;
                        CharManager.instance.kickPlayer(player, "map 1");
                        Database.instance.saveOrtherLog("", player.charname, "hack chuyen map 8", "hcm");
                    }
                } else if (map.mapIDLoadMap == 201 && (toTX != 33 || toTY != 1)) {
                    toMapID = player.mapID;
                    toTX = player.x / 16;
                    toTY = player.y / 16;
                    CharManager.instance.kickPlayer(player, "map 2");
                    Database.instance.saveOrtherLog("", player.charname, "hack chuyen map 201 ", "hcm");
                }

                // Vung Hoenn: cong vao chi mo khi tran cap mua >= 91 (sv.lau_dai) va du cap - xem real.VungHoenn.
                if (VungHoenn.chanVao(player, map, toMapID) || KhoaBanDoGame.chan(player, toMapID)) {
                    toMapID = player.mapID;
                    toTX = player.x / 16;
                    toTY = player.y / 16;
                    checklocation = false;
                }

                if (toMapID == 111 && !Char.isSuKienHe2017()) {
                    toMapID = player.mapID;
                    toTX = player.x / 16;
                    toTY = player.y / 16;
                    checklocation = false;
                }

                if (player.myCountry == -1) {
                    int[] mapstart = new int[]{117, 117, 117, 117, 117};
                    toTX = 39 + Database.r.nextInt() % 3;
                    toTY = 32 + Database.r.nextInt() % 5;
                    toMapID = mapstart[Map.r.nextInt(MessageCreator.nclone)];
                    checklocation = false;
                } else if (toMapID == 206) {
                    if (player.idClan == -1) {
                        toMapID = player.mapID;
                        toTX = player.x / 16;
                        toTY = player.y / 16;
                        checklocation = false;
                    }
                } else if (player.mapID == 105) {
                    int[][] mapstart = real.LangKhoiDau.bang(player);
                    int[][] pos = real.LangKhoiDau.bangViTri(player);
                    int index = Map.r.nextInt(4);
                    toTX = pos[player.myCountry][index * 2] + Map.r.nextInt() % 3;
                    toTY = pos[player.myCountry][index * 2 + 1] + Map.r.nextInt() % 3;
                    player.inCountry = player.myCountry;
                    toMapID = mapstart[player.myCountry][Map.r.nextInt(mapstart[player.myCountry].length)];
                    checklocation = false;
                } else if ((player.myCountry != player.inCountry && Map.getTown[player.inCountry]
                        || Map.nwar[player.inCountry] && player.myCountry != player.inCountry && Map.nationBeAttack[player.inCountry] != player.myCountry
                        || player.inCountry != player.myCountry && MapTown.addDragon[player.inCountry])
                        && toMapID == Map.idMapTown) {
                    player.sendMessage(MessageCreator.createServerAlertAutoOffMessage("Không thể tham gia sự kiện tại vùng khác."));
                    int[] mapstart = new int[]{9, 481, 482, 483, 484};
                    toTX = 16 + Database.r.nextInt() % 5;
                    toTY = 84;
                    toMapID = mapstart[Map.r.nextInt(MessageCreator.nclone)];
                    checklocation = false;
                } else if (player.idClan == -1 && Map.getTown[player.myCountry] && toMapID == Map.idMapTown) {
                    player.sendMessage(MessageCreator.createServerAlertAutoOffMessage("Bạn không thể vào Gym trong thơi gian này do chưa tham gia đội."));
                    int[] mapstart = new int[]{9, 481, 482, 483, 484};
                    toTX = 16 + Database.r.nextInt() % 5;
                    toTY = 84;
                    toMapID = mapstart[Map.r.nextInt(MessageCreator.nclone)];
                    checklocation = false;
                } else if (player.lvDetail.lv < 60 && Map.getTown[player.inCountry] && toMapID == Map.idMapTown) {
                    player.sendMessage(MessageCreator.createServerAlertAutoOffMessage("Bạn phải đạt cấp độ 60 trở lên mới có thể vào thành trong thời gian này."));
                    int[] mapstart = new int[]{9, 481, 482, 483, 484};
                    toTX = 16 + Database.r.nextInt() % 5;
                    toTY = 84;
                    toMapID = mapstart[Map.r.nextInt(MessageCreator.nclone)];
                    checklocation = false;
                } else if (RealController.mapList.get(toMapID) != null) {
                    boolean onPos = false;
                    boolean contentMap = false;
                    Vector<LocationMap> a = (Vector<LocationMap>) Map.ALL_LOCALTION_MAP.get(Integer.valueOf(map.getMapLoad(map.mapId)));
                    if (a != null) {
                        for (int k = 0; k < a.size(); k++) {
                            if (((LocationMap) a.get(k)).mapout == ((Map) RealController.mapList.get(toMapID)).getMapLoad(toMapID)) {
                                contentMap = true;
                            }

                            if (((LocationMap) a.get(k))
                                    .checkCanChangeMap(player.x, player.y, toTX, toTY, ((Map) RealController.mapList.get(toMapID)).getMapLoad(toMapID))) {
                                onPos = true;
                            }
                        }

                        if (!onPos && contentMap
                                && map.dungCanhCong(player, toMapID, toTX, toTY)) {
                            onPos = true; // vung chu nhat goc lech voi ban do Pokemon: xem Map.dungCanhCong
                        }
                        if (!onPos && contentMap) {
                            try { // nhat ky: phan biet hack that voi o cong lech vung (bao cao cho nguoi van hanh)
                                com.htbb.log.Nhat.canhBao("DOI_MAP", "kick doi map khong dung o: " + player.charname + " map " + map.mapId
                                        + " o " + player.x / 16 + "," + player.y / 16 + " -> " + toMapID + " (" + toTX + "," + toTY + ")");
                            } catch (Throwable ignored) {
                            }
                            if (toMapID != player.mapID) {
                                toMapID = player.mapID;
                                toTX = player.x / 16;
                                toTY = player.y / 16;
                                CharManager.instance.kickPlayer(player, "map 6");
                                Database.instance.saveOrtherLog("", player.charname, "hack chuyen map " + map.getMapLoad(map.mapId) + " to " + toMapID, "hcm");
                                return;
                            }

                            toMapID = player.mapID;
                            toTX = player.x / 16;
                            toTY = player.y / 16;
                            CharManager.instance.kickPlayer(player, "map 5");
                            Database.instance.saveOrtherLog("", player.charname, "hack chuyen map cung map " + player.mapID, "hcm");
                            return;
                        }
                    }
                }

                Map toMap = (Map) RealController.mapList.get(toMapID);
                if (toMap == null && (player.map.mapIDLoadMap == Map.idMapTown && Map.getTown[player.inCountry] || map.isMapLienDau())) {
                    toMapID = player.mapID;
                    toTX = player.x / 16;
                    toTY = player.y / 16;
                    checklocation = false;
                    toMap = (Map) RealController.mapList.get(toMapID);
                }

                Map oldMap = player.map;
                boolean onMapMonster = false;
                if (toMap != null) {
                    boolean except = false;
                    if ((player.map.mapIDLoadMap == 1 || player.map.mapIDLoadMap == 2 || player.map.mapIDLoadMap == 106 || player.map.mapIDLoadMap == 12)
                            && (toMap.mapIDLoadMap == 0 || toMap.mapIDLoadMap == 70 || toMap.mapIDLoadMap == 80)) {
                        // Truoc day nhanh nay bo qua het kiem tra vi tri (checkChangeMap) va tin
                        // toMapID/toTX/toTY client tu gui -> tu map luyen quai co the "ve lang"
                        // toi bat ky toa do nao trong bat ky instance lang nao, khong can dung o
                        // cong lang. Sua: server tu tinh diem ve lang (giong nhanh player.mapID
                        // == 105 o tren), bo qua hoan toan toMapID/toTX/toTY client gui.
                        int[][] mapstart = real.LangKhoiDau.bang(player);
                        int[][] pos = real.LangKhoiDau.bangViTri(player);
                        int index = Map.r.nextInt(4);
                        toTX = pos[player.myCountry][index * 2] + Map.r.nextInt() % 3;
                        toTY = pos[player.myCountry][index * 2 + 1] + Map.r.nextInt() % 3;
                        toMapID = mapstart[player.myCountry][Map.r.nextInt(mapstart[player.myCountry].length)];
                        toMap = (Map) RealController.mapList.get(toMapID);
                        except = true;
                    }

                    boolean canchange = false;
                    if (!map.isMapOffline) {
                        Map tempMap = (Map) RealController.mapList.get(Integer.valueOf(map.getMapLoad(map.mapId)));
                        if (!map.checkChangeMap(player, 0, 0, toMapID, toTX, toTY) && checklocation && !except) {
                            toMapID = player.mapID;
                            toTX = player.x / 16;
                            toTY = player.y / 16;
                            toMap = (Map) RealController.mapList.get(Integer.valueOf(map.getMapLoad(toMapID)));
                        }

                        if (tempMap == null) {
                            canchange = true;
                        } else {
                            short[] tempmapid = (short[]) Map.mapChange.get(map.getMapLoad((short) map.mapId));
                            if (tempMap != null) {
                                int mapcheck = toMap.getMapLoad(toMap.mapId);

                                for (int ix = 0; ix < tempmapid.length; ix++) {
                                    if (mapcheck == tempmapid[ix]) {
                                        canchange = true;
                                        break;
                                    }
                                }
                            }
                        }
                    } else if (map.isMapOffline) {
                        Map offlineMap = (Map) RealController.mapList.get(-1);
                        if (!map.checkChangeMap(player, 0, 0, toMapID, toTX, toTY) && checklocation) {
                            System.out.print(" " + player.charname + " hack cmn offline ");
                            data.NghiVanGianLan.ghiChung(player, "hackdoimap",
                                    "doi map trai phep tu map offline " + player.mapID
                                  + " sang map " + toMapID + " (" + toTX + "," + toTY + ")"
                                  + " - server da keo ve cho cu");
                            toMapID = player.mapID;
                            toTX = player.x / 16;
                            toTY = player.y / 16;
                            toMap = (Map) RealController.mapList.get(Integer.valueOf(map.getMapLoad(toMapID)));
                        }

                        if (player.mapID < 200) {
                            if (toMap.mapIDLoadMap != 0 && toMap.mapIDLoadMap != 70 && toMap.mapIDLoadMap != 80) {
                                canchange = false;
                            } else {
                                canchange = true;
                            }
                        } else if (toMap.mapIDLoadMap == 201) {
                            canchange = true;
                        } else {
                            canchange = false;
                        }
                    }

                    // lo #3-b loi.xlsx (tam): xem chu thich o dau ham. XOA sau khi sua xong.
                    if (map.mapIDLoadMap == 18) {
                        System.out.println("[DO-CHANGE-MAP] player=" + player.charname + " KET QUA CUOI: canchange=" + canchange
                                + " toMapID(sau khi kiem tra/co the bi ghi de)=" + toMapID
                                + " toTX=" + toTX + " toTY=" + toTY
                                + " toMap=" + (toMap == null ? "null" : ("mapId=" + toMap.mapId + " mapIDLoadMap=" + toMap.mapIDLoadMap)));
                    }

                    if (!canchange) {
                        return;
                    }

                    try {
                        if (player.monster != null
                                && player.map.mapId == player.monster.map.mapId
                                && player.monster.region == player.region
                                && Map.inRangeActor(player, player.monster, 120)
                                && (toMap.mapId == toMap.mapIDLoadMap && toMap.mapId < 301 || Map.isMapLang(toMap))) {
                            onMapMonster = true;
                        }
                    } catch (Exception var39) {
                    }

                    if (player.map != null) {
                        if (!Map.isMapLang(player.map) && player.map.mapId != player.map.mapIDLoadMap) {
                            onMapMonster = false;
                        }

                        map.playerExit(player);
                    }

                    toMap.playerJoin(player);
                } else {
                    if (player.map != null) {
                        map.playerExit(player);
                    }

                    Map offlineMapx = (Map) RealController.mapList.get(-1);
                    if (toMapID < 200) {
                        if (map.mapIDLoadMap != 0 && map.mapIDLoadMap != 70 && map.mapIDLoadMap != 80) {
                            return;
                        }

                        offlineMapx.playerJoin(player);
                    } else {
                        if (map.mapIDLoadMap != 201) {
                            return;
                        }

                        offlineMapx.playerJoin(player);
                    }
                }

                player.nearChars.removeAllElements();
                player.nearMons.removeAllElements();
                player.mapID = toMapID;
                player.x = toTX * 16 + 8;
                player.y = toTY * 16 + 8;
                player.doChangeMapMonster(onMapMonster, oldMap);
                if (player.x / 16 <= 1) {
                    player.x += 32;
                }

                if (player.y / 16 <= 1) {
                    player.y += 32;
                }

                player.moved = false;
                player.doFinishAutoInbue();
                Message m = MessageCreator.createMapMessage(player);
                player.sendMessage(m);
                map.doSendDynamicObj(player);
                player.map.sendInfoNpc(player);
                player.map.sendDynamicEff(player);
                player.checkCreateThangBe(toMapID);
                if (player.isNguoiTuyet()) {
                    MessageCreator.createMsgCharMonster(player, player);
                }

                player.sendMessage(MessageCreator.createCharWearingMessage(player, player));

                try {
                    if (player.mapID == Map.mapIDFarm) {
                        map.doSendFarm(player);
                    }

                    if (toMap != null) {
                        for (int ixx = 0; ixx < Map.bossLocation.size(); ixx++) {
                            if (((BossLocaltion) Map.bossLocation.get(ixx)).mapID == toMapID) {
                                BossLocaltion b = (BossLocaltion) Map.bossLocation.get(ixx);
                                player.sendMessage(MessageCreator.createGate(b.type, b.x, b.y, b.mapID));
                                break;
                            }
                        }

                        if (toMap != null
                                && (toMap.mapIDLoadMap == 80
                                || toMap.mapIDLoadMap == 70
                                || toMap.mapIDLoadMap == 0
                                || toMap.mapIDLoadMap == 1
                                || toMap.mapIDLoadMap == 2
                                || toMap.mapIDLoadMap == 12
                                || toMap.mapIDLoadMap == 106)) {
                            player.sendMessage(MessageCreator.createMessageLocation(real.LangKhoiDau.oCong(player, player.inCountry)));
                        }
                    }
                } catch (Exception var37) {
                }
            }
        } catch (Exception var40) {
            System.out.print(" " + player.charname + " ");
        } finally {
            try {
                player.isDoChangeMap = false;
            } catch (Exception var36) {
            }
        }
    }

}
