package real;


import data.CreateItemTemplate;
import data.Database;
import data.GemItem;
import io.Message;
import java.util.Vector;

/**
 * Ep ngoc va kham ngoc vao trang bi: ep (imbue), kham, tach nguyen lieu,
 * roi ngoc khi danh quai, kiem tra o ngoc trong.
 *
 * Tach nguyen van tu real/Map.java. Deu la method PRIVATE cua Map nen khong
 * the bi goi tu ben ngoai -> chuyen ra day khong doi API cua Map.
 * Tham so "Map map" thay cho "this" truoc day.
 */
// Thanh vien static cua Map ma nhom nay dung khong dinh danh khi con nam trong Map.
import static real.Map.doCreateBookSkillPet;
import static real.Map.gemTemplate;
import static real.Map.getPercentInSu;
import static real.Map.getTown;
import static real.Map.isItemCanImbue;
import static real.Map.isItemSelled;
import static real.Map.isItemThanThu;
import static real.Map.isSpesialLkd;
import static real.Map.isWearingAnimal;
import static real.Map.r;
import static real.Map.random;

class MapEpNgoc {

    static final int PRICE_KHAM = 20000;

    private static boolean isGemKham(GemItem gem) {
        if (gem == null || gem.idGemTemplate < 12
                || gem.idGemTemplate > GemTemplate.DUC_TUONG_KHAM_6) {
            return false;
        }
        GemTemplate template = gem.getTemplate();
        return template != null && (template.type == GemTemplate.TYPE_KHAM
                || template.type == GemTemplate.TYPE_DUC_LO);
    }

    static Vector<GemItem> dropGemMaterial(Map map) {
        int[] id = new int[]{71, 78, 85, 92, 99};
        Vector<GemItem> gem = new Vector();
        gem.add(new GemItem(id[r.nextInt(id.length)]));
        id = new int[]{63, 64};
        gem.add(new GemItem(id[r.nextInt(id.length)]));
        return gem;
    }

    static GemItem createGemItem(Map map, int idTemplate) {
        return new GemItem(idTemplate);
    }

    /**
     * Chan so luong nguyen lieu AM ngay LUC DOC GOI, truoc bat ky hieu ung nao duoc ap dung
     * (khoa do, dong dau, doi thuoc tinh, doi skill, tao do moi...).
     *
     * deleteGemItem (duoi day) da co chot tu choi so am tu 29/08/2026, nhung no la buoc
     * CUOI CUNG cua moi duong che tao - moi nguoi goi deu da SUA DOI VAT PHAM (item.lock=1,
     * item.charSeal=.., item.initAtt(...), item.changLevelSkill(), hoac tao han mot Item moi
     * roi add vao iItems) TRUOC KHI goi no, va khong ai doc gia tri tra ve. Ket qua: hieu ung
     * da cap, deleteGemItem tu choi tru, nguoi choi duoc nang cap/doi thuoc tinh/tao do MIEN
     * PHI, lap lai vo han. Phai chan o day - noi DUY NHAT con kip, ngay sau khi doc material
     * tu goi tin va truoc enoughMaterial lan hai / truoc khi ap bat ky hieu ung nao.
     */
    static boolean coSoLuongAm(int[][] material) {
        if (material == null) {
            return false;
        }
        for (int[] hang : material) {
            if (hang == null) {
                continue;
            }
            for (int v : hang) {
                if (v < 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Kiem va ghi nghi van neu co so am. Goi NGAY sau khi doc material tu goi tin, truoc moi
     * buoc khac. @return true neu PHAT HIEN so am (nguoi goi phai bao loi va return ngay).
     */
    static boolean chanSoLuongAm(Char player, int[][] material, String noiGoi) {
        if (!coSoLuongAm(material)) {
            return false;
        }
        data.NghiVanGianLan.ghiVatPham(player, "hackngl",
                "gui so luong nguyen lieu AM khi che tao (" + noiGoi + ")");
        return true;
    }

    static String deleteGemItem(Map map, Char player, byte[] startID, int[][] material, int materialUse) {
        String gemUse = "ngL " + materialUse;

        // Mang "material" den THANG tu goi tin cua client: 12 byte co dau doc bang
        // message.dis.readByte() (Map.java:6465, 6469, 6849; MapCheTao.java:197,
        // 278, 359). Ben duoi tru bang phep tru THO "listGemitem[aa] - material[..]"
        // nen mot so AM se CONG ngoc thay vi tru - nhan doi nguyen lieu vo han.
        //
        // enoughMaterial() khong chan duoc: no kiem "listGemitem[..] < material[i]",
        // ma voi material[i] am thi phep so sanh do luon sai -> luon cho qua.
        //
        // Chan o day vi day la cho DUY NHAT moi duong che tao deu di qua.
        if (material != null) {
            for (int hang = 0; hang < material.length; hang++) {
                if (material[hang] == null) {
                    continue;
                }
                for (int cot = 0; cot < material[hang].length; cot++) {
                    if (material[hang][cot] < 0) {
                        data.NghiVanGianLan.ghiVatPham(player, "hackngl",
                                "gui so luong nguyen lieu AM khi che tao: material[" + hang + "][" + cot
                              + "]=" + material[hang][cot] + ", che do nguyen lieu " + materialUse);
                        return gemUse + " | TU CHOI: so luong am";
                    }
                }
            }
        }

        try {
            if (material[0] != null) {
                for (int m = 0; m < material[0].length; m++) {
                    int aa = CreateItemTemplate.startIDMaterial[startID[0]] + m;
                    if (materialUse == 0) {
                        player.listGemitem[aa] = player.listGemitem[aa] - material[0][m];
                        player.allGemUse[aa] = player.allGemUse[aa] + material[0][m];
                    } else if (materialUse == 1) {
                        player.listGemitemLock[aa] = player.listGemitemLock[aa] - material[0][m];
                        player.allGemUseLock[aa] = player.allGemUseLock[aa] + material[0][m];
                    } else {
                        int n = material[0][m];
                        if (player.listGemitemLock[aa] > 0) {
                            if (player.listGemitemLock[aa] >= n) {
                                player.listGemitemLock[aa] = player.listGemitemLock[aa] - n;
                                player.allGemUseLock[aa] = player.allGemUseLock[aa] + n;
                            } else {
                                n -= player.listGemitemLock[aa];
                                player.allGemUseLock[aa] = player.allGemUseLock[aa] + player.listGemitemLock[aa];
                                player.listGemitemLock[aa] = 0;
                                player.listGemitem[aa] = player.listGemitem[aa] - n;
                                player.allGemUse[aa] = player.allGemUse[aa] + n;
                            }
                        } else {
                            player.listGemitem[aa] = player.listGemitem[aa] - n;
                            player.allGemUse[aa] = player.allGemUse[aa] + n;
                        }
                    }

                    gemUse = gemUse + " " + (CreateItemTemplate.startIDMaterial[startID[0]] + m) + "_" + material[0][m];

                    try {
                        if (player.listGemitem[aa] <= 0 && player.listGemitemLock[aa] <= 0) {
                            for (int i = 0; i < player.gemItem.size(); i++) {
                                GemItem g = (GemItem) player.gemItem.get(i);
                                if (g.idGemTemplate == aa) {
                                    player.removeItemOutVector(g.realId, player.gemItem);
                                    break;
                                }
                            }
                        }
                    } catch (Exception var10) {
                    }
                }
            }

            gemUse = gemUse + "|";
            if (material[1] != null) {
                for (int m = 0; m < material[1].length; m++) {
                    int aa = CreateItemTemplate.startIDMaterial[startID[startID.length < 2 ? 0 : 1]] + m;
                    if (materialUse == 0) {
                        player.listGemitem[aa] = player.listGemitem[aa] - material[1][m];
                        player.allGemUse[aa] = player.allGemUse[aa] + material[1][m];
                    } else if (materialUse == 1) {
                        player.listGemitemLock[aa] = player.listGemitemLock[aa] - material[1][m];
                        player.allGemUseLock[aa] = player.allGemUseLock[aa] + material[1][m];
                    } else {
                        int n = material[1][m];
                        if (player.listGemitemLock[aa] > 0) {
                            if (player.listGemitemLock[aa] >= n) {
                                player.listGemitemLock[aa] = player.listGemitemLock[aa] - n;
                                player.allGemUseLock[aa] = player.allGemUseLock[aa] + n;
                                int var17 = 0;
                            } else {
                                n -= player.listGemitemLock[aa];
                                player.allGemUseLock[aa] = player.allGemUseLock[aa] + player.listGemitemLock[aa];
                                player.listGemitemLock[aa] = 0;
                                player.listGemitem[aa] = player.listGemitem[aa] - n;
                                player.allGemUse[aa] = player.allGemUse[aa] + n;
                            }
                        } else {
                            player.listGemitem[aa] = player.listGemitem[aa] - n;
                            player.allGemUse[aa] = player.allGemUse[aa] + n;
                        }
                    }

                    try {
                        if (player.listGemitem[aa] <= 0 && player.listGemitemLock[aa] <= 0) {
                            for (int ix = 0; ix < player.gemItem.size(); ix++) {
                                GemItem g = (GemItem) player.gemItem.get(ix);
                                if (g.idGemTemplate == aa) {
                                    player.removeItemOutVector(g.realId, player.gemItem);
                                    break;
                                }
                            }
                        }
                    } catch (Exception var9) {
                    }

                    gemUse = gemUse + " " + (CreateItemTemplate.startIDMaterial[startID[startID.length < 2 ? 0 : 1]] + m) + "_" + material[1][m];
                }
            }
        } catch (Exception var11) {
            var11.printStackTrace();
        }

        return gemUse;
    }

    static void doTachNguyenLieu(Map map, Char player, short idItem, int typeStone) {
        if (!player.isCorrectPass && !player.passWord.equals("")) {
            player.sendMessage(MessageCreator.createMsgInputText(player.id, 4, "Nhập mật khẩu", 0));
        } else {
            try {
                Item item = player.getItemFormVector(player.iItems, idItem);
                if (item == null) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không tìm thấy vật phẩm", ""));
                } else {
                    if (item.level >= 80 || isWearingAnimal(item.getTemplate().type)) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Vật phẩm cấp độ này chưa tách được nguyên liệu.", ""));
                        return;
                    }

                    if (item.isHoanMy() && item.level >= 50) {
                        if (typeStone == 2) {
                            if (player.listGemitem[178] > 0 || player.listGemitemLock[178] > 0) {
                                player.sendMessage(MessageCreator.createMsgMenu(new String[]{"Đá thường", "Đá đặc biệt"}, -510, 0));
                                player.idItemTach = item.id;
                                return;
                            }

                            typeStone = 0;
                        }

                        int[] idDa = new int[]{175, 176, 177, 178};
                        int idg;
                        if (typeStone == 0) {
                            if (item.isWeapone()) {
                                idg = idDa[(item.level - 51) / 10];
                            } else {
                                idg = idDa[(item.level - 50) / 10];
                            }
                        } else if (typeStone == 1) {
                            idg = 178;
                        } else {
                            // Truoc day idg mac dinh = 0 (gem id 0 "Bao hiem 20%", 100 XU) khi
                            // typeStone ngoai {0,1} - client tu gui idOptionMenu bat ky (menu -510)
                            // la dung mot vien bao hiem re thay vi da tach that (175-178, 15-100
                            // luong) de tach nguyen lieu. Chan han.
                            data.NghiVanGianLan.ghiVatPham(player, "hacknl",
                                    "gui typeStone=" + typeStone + " ngoai bien {0,1} khi tach nguyen lieu");
                            player.sendMessage(MessageCreator.createServerAlertMessage("Lựa chọn không hợp lệ", ""));
                            return;
                        }

                        if (player.listGemitem[idg] <= 0 && player.listGemitemLock[idg] <= 0) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ đá tách nguyên liệu", ""));
                            return;
                        }

                        int[] idstart = item.getIDMaterialGetBack();
                        int total = item.getTotalMaterial(typeStone == 1);
                        int[] soluong = (int[]) null;
                        String info = "Nhận được ";

                        // TRA GIA TRUOC, NHAN THUONG SAU.
                        //
                        // Truoc day thu tu nguoc lai: ba vong ben duoi cong nguyen lieu
                        // vao tui (doAddGemItem) roi moi tru vien da tach va go trang bi
                        // ra khoi hanh trang. Bat ky ngoai le nao roi vao khoang giua -
                        // ma ca ham nam trong mot khoi try nuot loi (dong 177) - la nguoi
                        // choi giu duoc CA nguyen lieu LAN trang bi. Dung khuon loi da gap
                        // that o Huyet Bo De (Map.doUseItem + Pet.doChangeThongThao).
                        //
                        // Doc item.getIDMaterialGetBack()/getTotalMaterial() o tren truoc
                        // roi moi go item ra khoi Vector - go khoi Vector khong xoa doi
                        // tuong nen phan ghi nhat ky ben duoi van doc duoc ten va thuoc tinh.
                        if (player.listGemitemLock[idg] > 0) {
                            player.delGem(idg, 1, true);
                        } else {
                            player.delGem(idg, 1, false);
                        }
                        player.iItems.remove(item);
                        if (item.hangItem == 1) {
                            soluong = new int[2];
                            int[] idtem = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1};

                            for (int i = 0; i < total; i++) {
                                soluong[idtem[r.nextInt(idtem.length)]]++;
                            }

                            for (int i = 0; i < soluong.length; i++) {
                                if (soluong[i] > 0) {
                                    int idgem = idstart[r.nextInt(2)];
                                    player.doAddGemItem(idgem + 4 + i, soluong[i], true);
                                    info = info + soluong[i] + " " + gemTemplate[idgem + 4 + i].name + " ";
                                }
                            }
                        } else if (item.hangItem == 2) {
                            soluong = new int[2];
                            int[] idtem = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1};

                            for (int ix = 0; ix < total; ix++) {
                                soluong[idtem[r.nextInt(idtem.length)]]++;
                            }

                            for (int ix = 0; ix < soluong.length; ix++) {
                                if (soluong[ix] > 0) {
                                    int idgem = idstart[r.nextInt(2)];
                                    player.doAddGemItem(idgem + 3 + ix, soluong[ix], true);
                                    info = info + soluong[ix] + " " + gemTemplate[idgem + 3 + ix].name + " ";
                                }
                            }
                        } else {
                            soluong = new int[3];
                            int[] idtem = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 2, 2};

                            for (int ixx = 0; ixx < total; ixx++) {
                                soluong[idtem[r.nextInt(idtem.length)]]++;
                            }

                            for (int ixx = 0; ixx < soluong.length; ixx++) {
                                if (soluong[ixx] > 0) {
                                    int idgem = idstart[r.nextInt(2)];
                                    player.doAddGemItem(idgem + ixx, soluong[ixx], true);
                                    info = info + soluong[ixx] + " " + gemTemplate[idgem + ixx].name + " ";
                                }
                            }
                        }

                        player.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
                        player.sendMessage(MessageCreator.createCharGemItem(player));
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                        Database.instance
                                .saveOrtherLog(
                                        "",
                                        player.charname,
                                        item.getName()
                                        + "_"
                                        + item.hangItem
                                        + "_"
                                        + item.colorName
                                        + "_"
                                        + gemTemplate[idg].name
                                        + "> "
                                        + info
                                        + " | "
                                        + item.getAttribute()
                                        + "|"
                                        + item.getDBInfo(),
                                        "tach"
                                );
                    } else {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có vật phẩm hoàn mỹ cấp 50 trở lên mới có thể tách nguyên liệu", ""));
                    }
                }
            } catch (Exception var14) {
            }
        }
    }

    static void doAddItemImbue(Map map, Char player, short id) {
        if (map.checkTrade(player)) {
            return;
        }
        if (!isItemSelled(player, id, null)) {
            try {
                Item it = player.getItemFormVector(player.iItems, id);
                if (it != null && it.getTemplate().ndayLoan > 0) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Vật phẩm không thể luyện.", ""));
                    return;
                }

                if (isItemThanThu(it.getTemplate().type)) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Vật phẩm không thể luyện.", ""));
                    return;
                }

                if (it.isManhSuuTap() || it.isTBSuuTap() || it.isCanh() || it.isCaiTrangSuuTap()) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không thể cường hoá mảnh", ""));
                    return;
                }

                if (!isItemCanImbue(it.getTemplate().type)) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không thể cường hoá trang bị này", ""));
                    return;
                }

                if (player.itemImbue == 40000) {
                    player.itemImbue = id;
                } else if (player.itemImbue != id) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không thể luyện 2 vật phẩm cùng lúc", ""));
                } else if (player.itemImbue == id) {
                    player.itemImbue = 40000;
                }
            } catch (Exception var4) {
            }
        }
    }

    static void doAddGemItemImbue(Map map, Char player, short id, Message msg) {
        try {
            if (!NpcTinhBanDo.oNoiDichVu(player, NpcTinhBanDo.NPC_THO_REN)) {
                return;
            }

            if (player.gemItemImbue.size() >= 8) {
                return;
            }

            int type = 0;

            try {
                type = msg.dis.readByte();
            } catch (Exception var8) {
            }

            int idGem = player.isExistInvector(id, player.gemItem);
            if (idGem != -1) {
                if (type == 1) {
                    for (int i = 0; i < player.gemItemImbue.size(); i++) {
                        if (((GemItem) player.gemItemImbue.get(i)).realId == id) {
                            player.gemItemImbue.remove(i);
                            return;
                        }
                    }

                    return;
                }

                GemItem gem = player.getItemFormVector(id, player.gemItem);
                if (gem != null) {
                    // Goi them nguyen lieu dung chung cho cuong hoa va kham/duc lo.
                    // Ngoc kham va duc phai kiem dung tui da chon o menu -506.
                    if (isGemKham(gem)) {
                        int mode = player.typeGemKham;
                        if (mode < 0 || mode > 2) {
                            return;
                        }
                        int normal = Math.max(0, player.listGemitem[gem.idGemTemplate]
                                - player.listGemitemSell[gem.idGemTemplate]);
                        int locked = player.listGemitemLock[gem.idGemTemplate];
                        int available = mode == 0 ? normal : mode == 1 ? locked : normal + locked;
                        if (available <= map.getToTalGem(gem.idGemTemplate, player)) {
                            player.sendMessage(MessageCreator.createServerAlertMessage(
                                    "Không đủ ngọc hoặc đục lỗ trong loại nguyên liệu đã chọn", ""));
                            return;
                        }
                        player.gemItemImbue.add(gem);
                        return;
                    }
                    if (gem.isSelling && player.typeGemImbue == 0) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Không thể thêm vì bạn đang bán loại ngọc này", ""));
                        return;
                    }

                    // Che do nguyen lieu phai la 0, 1 hoac 2. Ba dieu kien duoi day khong
                    // co nhanh mac dinh, nen mot gia tri thu tu se lot qua het ma khong
                    // he kiem ton kho. Chan o day la lop thu hai; lop dau o cho gan
                    // (MapDoMenu case -505).
                    if (player.typeGemImbue < 0 || player.typeGemImbue > 2) {
                        data.NghiVanGianLan.ghiVatPham(player, "hackepngoc",
                                "che do nguyen lieu ep ngoc khong hop le luc them ngoc: " + player.typeGemImbue);
                        return;
                    }

                    int totalgem = map.getToTalGem(gem.idGemTemplate, player);
                    if (player.typeGemImbue == 0 && player.listGemitem[gem.idGemTemplate] - totalgem <= 0
                            || player.typeGemImbue == 1 && player.listGemitemLock[gem.idGemTemplate] - totalgem <= 0
                            || player.typeGemImbue == 2 && player.listGemitemLock[gem.idGemTemplate] + player.listGemitem[gem.idGemTemplate] - totalgem <= 0) {
                        return;
                    }

                    if (player.countBaoHiem() >= 1 && gem.idGemTemplate < 5) {
                        return;
                    }

                    if (player.countLKD() < 1 || gem.idGemTemplate < 8 || gem.idGemTemplate == 155 || gem.idGemTemplate == 156) {
                        player.gemItemImbue.add(gem);
                    }
                }
            } else {
                player.removeItemOutVector(id, player.gemItemImbue);
            }
        } catch (Exception var9) {
            var9.printStackTrace();
        }
    }

    static boolean haveGem(Map map, Char player) {
        for (int i = 0; i < player.gemItemImbue.size(); i++) {
            GemItem gem = (GemItem) player.gemItemImbue.elementAt(i);
            if (gem.idGemTemplate > 7 && gem.idGemTemplate < 12
                    || gem.idGemTemplate == GemTemplate.LKD_35
                    || gem.idGemTemplate == GemTemplate.LKD_40
                    || gem.idGemTemplate == 157
                    || gem.idGemTemplate == 158) {
                return true;
            }
        }

        return false;
    }

    static int getGem(Map map, Char player) {
        for (int i = 0; i < player.gemItemImbue.size(); i++) {
            GemItem gem = (GemItem) player.gemItemImbue.elementAt(i);
            if (gem.idGemTemplate > 7 && gem.idGemTemplate < 12 || gem.idGemTemplate == GemTemplate.LKD_35 || gem.idGemTemplate == GemTemplate.LKD_40) {
                return gem.idGemTemplate;
            }
        }

        return -1;
    }

    static GemItem haveGemKham(Map map, Char player) {
        for (int i = 0; i < player.gemItemImbue.size(); i++) {
            GemItem gem = (GemItem) player.gemItemImbue.elementAt(i);
            if (isGemKham(gem)) {
                return gem;
            }
        }

        return null;
    }

    static void doAutoImbue(Map map, Char player, Message msg) {
        if (!player.isCorrectPass && !player.passWord.equals("")) {
            // Mo khoa ScreenDapDo (co static isFinishImbue) truoc khi hien o nhap mat
            // khau, nhung KHONG kem hop thoai bao loi vi no se de len o nhap mat khau.
            // Chi lam khi CHUA co phien nao chay, de khong huy nham phien dang chay.
            if (!player.startAutoImbue) {
                player.sendStopAutoImbue("", false);
            }
            player.sendMessage(MessageCreator.createMsgInputText(player.id, 4, "Nhập mật khẩu", 0));
        } else if (!player.startAutoImbue) {
            try {
                if (player.lvDetail.lv < 10) {
                    player.sendStopAutoImbue("Hãy đi luyện đến cấp 10 trở lên để sử dụng chức năng này.");
                    return;
                }

                if (!com.htbb.pokemon.LauDai.bat() && player.getxu() < 5000L) {
                    player.sendStopAutoImbue("Bạn phải có tối thiểu 5000 xu mới có thể sử dụng chức năng này");
                    return;
                }

                short id = msg.dis.readShort();
                byte plus = msg.dis.readByte();
                byte size = msg.dis.readByte();
                if (size > 3) {
                    player.sendStopAutoImbue("Không thể bắt đầu luyện tự động");
                    return;
                }

                short[] idGemtemplate = new short[size];
                byte[] lock = new byte[size];

                for (int i = 0; i < size; i++) {
                    idGemtemplate[i] = msg.dis.readShort();
                    lock[i] = msg.dis.readByte();
                }

                player.doSetAutoImbue(id, idGemtemplate, plus, lock);
            } catch (Exception var9) {
                var9.printStackTrace();
                // lo #7 loi.xlsx: truoc day catch-all nay chi in log server, khong bao gi
                // cho client - nguoi choi bam Start thay "khong phan hoi" giong nhu treo,
                // du nguyen nhan that su co the la bat ky loi nao (doc goi tin sai, item
                // hong...). Bao loi chung de it nhat ho biet la co loi, khong phai lag.
                player.sendStopAutoImbue("Không thể bắt đầu luyện tự động, thử lại sau");
            }
        } else {
            // lo #7 loi.xlsx: truoc day nhanh nay im lang hoan toan khi cờ startAutoImbue
            // dang bi ket true (xem fix o CharCapNhat.doAutoImbue) - nguoi choi bam Start
            // ma khong thay gi xay ra, tuong la loi.
            player.sendMessage(MessageCreator.createServerAlertMessage("Đang trong phiên luyện tự động, hãy dừng trước khi bắt đầu lại", ""));
        }
    }

    static void doFinishImbue(Map map, Char player, int ok) {
        if (NpcTinhBanDo.oNoiDichVu(player, NpcTinhBanDo.NPC_THO_REN)) {
            int prizeImbue = 5000;
            if (ok == -1) {
                player.itemImbue = 40000;
                player.gemItemImbue.removeAllElements();
            } else if (!com.htbb.pokemon.LauDai.bat() && player.getxu() < (long) prizeImbue) {
                try {
                    player.itemImbue = 40000;
                    player.gemItemImbue.removeAllElements();
                    player.sendMessage(MessageCreator.createServerAlertMessage("Hết tiền", ""));
                } catch (Exception var13) {
                    var13.printStackTrace();
                    System.out.println("LOI ROI NE");
                }

                System.out.println("KHONG DU TIEN de imbue");
            } else {
                if (player.itemImbue != 40000) {
                    int[] percent = new int[]{100, 90, 85, 80, 70, 30, 15, 10, 7, 4, 3, 2, 1, 1, 0};
                    Item item = player.getItemFormVector(player.iItems, (short) player.itemImbue);

                    try {
                        if (isItemSelled(player, item.id, item)) {
                            return;
                        }
                    } catch (Exception var18) {
                    }

                    try {
                        if (item.isManhSuuTap() || item.isTBSuuTap() || item.isCanh() || item.isCaiTrangSuuTap()) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không thể cường hoá mảnh", ""));
                            return;
                        }
                        if (!isItemCanImbue(item.getTemplate().type)) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không thể cường hoá trang bị này", ""));
                            return;
                        }
                    } catch (Exception var17) {
                    }

                    if (item != null) {
                        if (!haveGem(map, player)) {
                            try {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Chưa có luyện kim dược", ""));
                            } catch (Exception var14) {
                            }

                            return;
                        }

                        // GD2 2.5: thiet ke dai han - gia theo bac, pity, bua bao ho. Ham chung
                        // voi CharCapNhat.doAutoImbue (NangCapLauDaiGame).
                        if (com.htbb.pokemon.LauDai.bat()) {
                            nangCapLauDai(map, player, item);
                            return;
                        }

                        // GD2 1.2: truoc day tru 5.000 xu TRUOC khi kiem tra co luyen kim duoc
                        // (va ca khi item == null) - thieu da van mat tien. Gio kiem xong moi tru,
                        // kiem-va-tru trong Char.KHOA_TIEN (truXuNeuDu).
                        if (!player.truXuNeuDu((long) prizeImbue, true, "map 72")) {
                            try {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Hết tiền", ""));
                            } catch (Exception var14b) {
                            }
                            return;
                        }

                        if (item.plus < 15) {
                            int pcInsu = random(100);
                            int bh = getPercentInSu(player);
                            int pc = random(100);
                            int up = map.calculatePercenUp(player, percent[item.plus]);
                            if (pc <= percent[item.plus] + up) {
                                item.plus++;
                                int[] plus = new int[]{4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 6, 10};

                                for (int i = 0; i < 10; i++) {
                                    item.atb[i] = (short) (item.atb[i] + (i > 0 ? 1 : plus[item.plus]));
                                }

                                Message m = new Message(77);

                                try {
                                    m.dos.writeUTF("Chúc mừng bạn, vật phẩm đã được luyện lên +" + item.plus);
                                    player.sendMessage(m);
                                    m.cleanup();
                                    String sub = item.getTemplate().name + "_" + item.plus + "|" + item.getAttribute() + "|" + item.getDBInfo();
                                    Database.instance
                                            .saveOrtherLog("tob_log_other_kham", player.charname, getGem(map, player) + " imbue" + sub + " > " + player.typeGemImbue, "ibok");
                                } catch (Exception var16) {
                                }
                            } else {
                                Message m = new Message(77);

                                try {
                                    if (pcInsu > bh) {
                                        item.plus--;
                                        if (item.plus > 0) {
                                            int[] plus = new int[]{4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 6, 10};

                                            for (int i = 0; i < 10; i++) {
                                                item.atb[i] = (short) (item.atb[i] - (i > 0 ? 1 : plus[item.plus + 1]));
                                                if (item.atb[i] < 0) {
                                                    item.atb[i] = 0;
                                                }
                                            }
                                        }
                                    }

                                    m.dos.writeUTF(pcInsu <= bh ? "Thất bại" : "Thất bại. Vật phẩm đã bị hỏng và bị giảm 1 điểm cộng.");
                                    String sub = item.getTemplate().name + "_" + item.plus + "|" + item.getAttribute() + "|" + item.getDBInfo();
                                    Database.instance
                                            .saveOrtherLog("tob_log_other_kham", player.charname, getGem(map, player) + " imbue" + sub, pcInsu <= bh ? "fib" : "fibd");
                                    player.sendMessage(m);
                                    m.cleanup();
                                } catch (Exception var19) {
                                    var19.printStackTrace();
                                }
                            }

                            boolean isLock = false;

                            for (int ix = 0; ix < player.gemItemImbue.size(); ix++) {
                                GemItem gem = (GemItem) player.gemItemImbue.elementAt(ix);
                                if (player.typeGemImbue == 0) {
                                    if (player.listGemitem[gem.idGemTemplate] > 0) {
                                        player.listGemitem[gem.idGemTemplate]--;
                                    }

                                    player.allGemUse[gem.idGemTemplate]++;
                                } else if (player.typeGemImbue == 1) {
                                    if (player.listGemitemLock[gem.idGemTemplate] > 0) {
                                        player.listGemitemLock[gem.idGemTemplate]--;
                                    }

                                    if (isSpesialLkd(gem.idGemTemplate)) {
                                        isLock = true;
                                    }

                                    player.allGemUseLock[gem.idGemTemplate]++;
                                } else {
                                    // Bao gom ca typeGemImbue == 2 VA moi gia tri la khac.
                                    // Truoc day day la "else if (== 2)" khong co nhanh mac
                                    // dinh: gia tri la thi khong tru vien nao -> ep mien phi.
                                    if (player.listGemitemLock[gem.idGemTemplate] > 0) {
                                        player.listGemitemLock[gem.idGemTemplate]--;
                                        if (isSpesialLkd(gem.idGemTemplate)) {
                                            isLock = true;
                                        }

                                        player.allGemUseLock[gem.idGemTemplate]++;
                                    } else if (player.listGemitem[gem.idGemTemplate] > 0) {
                                        player.listGemitem[gem.idGemTemplate]--;
                                        player.allGemUse[gem.idGemTemplate]++;
                                    }
                                }

                                if (player.listGemitem[gem.idGemTemplate] <= 0 && player.listGemitemLock[gem.idGemTemplate] <= 0) {
                                    player.removeItemOutVector(gem.realId, player.gemItem);
                                }
                            }

                            if (isLock) {
                                item.lock = 1;
                                Database.instance
                                        .saveOrtherLog(
                                                "", player.charname, "khoa item khi ch dung lkd tim khoa " + item.getName() + " > " + item.getInfoSave(), "khoaitemch"
                                        );
                            }

                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                            player.sendMessage(MessageCreator.createMsgItemInfo(item));
                            player.gemItemImbue.removeAllElements();
                            player.sendMessage(MessageCreator.createCharGemItem(player));
                        }

                        try {
                            if (r.nextInt(10000) < 5) {
                                doCreateBookSkillPet(player, 0);
                            }
                        } catch (Exception var15) {
                        }
                    }
                }
            }
        }
    }

    /** Nhanh dai han cua doFinishImbue (tay): mot lan thu, roi tra ngoc ve nhu ban cu. */
    static void nangCapLauDai(Map map, Char player, Item item) {
        if (!player.iItems.contains(item)) {
            return;
        }
        final int idLkd = getGem(map, player);
        boolean coBua = false;
        for (int i = 0; i < player.gemItemImbue.size(); i++) {
            final GemItem g = (GemItem) player.gemItemImbue.elementAt(i);
            if (g.idGemTemplate >= 0 && g.idGemTemplate <= 4) {
                coBua = true;
            }
        }
        final String[] ra = {""};
        final int kq = NangCapLauDaiGame.thu(player, item, idLkd, player.typeGemImbue, coBua, ra);
        try {
            if (kq == NangCapLauDaiGame.THIEU || kq == NangCapLauDaiGame.DA_TOI_DA) {
                player.sendMessage(MessageCreator.createServerAlertMessage(ra[0], ""));
            } else {
                Message m = new Message(77);
                m.dos.writeUTF(ra[0]);
                player.sendMessage(m);
                m.cleanup();
            }
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            player.sendMessage(MessageCreator.createMsgItemInfo(item));
            player.gemItemImbue.removeAllElements();
            player.sendMessage(MessageCreator.createCharGemItem(player));
        } catch (Exception e) {
        }
    }

    static void doKhamItem(Map map, Char player, int type) {
        int prizeImbue = 10000;

        try {
            if (player.lvDetail.lv < 10) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Hãy đi luyện đến cấp 10 trở lên để sử dụng chức năng này.", ""));
                return;
            }

            player.isImbue = type == 0;
            if (player.isImbue) {
                player.sendMessage(MessageCreator.createMsgMenu(new String[]{"Nguyên liệu thường", "Nguyên liệu khóa"}, -506, 0));
            }
        } catch (Exception var5) {
        }
    }

    /** Ti le ep ngoc 3/4/5 vien = 10/20/100%. {@code roll} 0..99. */
    static boolean epNgocThanhCong(int roll, int soVien) {
        final int[] percent = {10, 20, 100};
        if (soVien < 3 || soVien > 5) {
            return false;
        }
        return roll < percent[soVien - 3];
    }

    static void doFinishEpNgoc(Map map, Char player, int ok, Message message) {
        try {
            short prize = 5000;
            if (ok == 0) {
                Message m = new Message(-36);
                m.dos.writeByte(0);
                m.dos.writeShort(prize);
                player.sendMessage(m);
            } else if (ok == 1) {
                int len = message.dis.readByte();
                Vector<GemItem> gem = new Vector();
                if (player.getxu() < (long) prize) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền", ""));
                    player.sendMessage(MessageCreator.createCharGemItem(player));
                    return;
                }

                // len doc tu goi tin (readByte) va chi bi chan can DUOI. Ben duoi dung
                // "percent[gem.size() - 3]" ma percent chi co 3 phan tu, nen gui tu 6
                // vien tro len la vuot mang - ngoai le bi khoi catch cuoi ham nuot,
                // nguoi choi khong mat gi nhung cung khong nhan gi.
                if (len < 3 || len > 5) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có thể hợp thành từ 3 đến 5 nguyên liệu", ""));
                    player.sendMessage(MessageCreator.createCharGemItem(player));
                    return;
                }

                for (int i = 0; i < len; i++) {
                    GemItem g = player.getItemFormVector(message.dis.readShort(), player.gemItem);
                    if (g != null) {
                        if (player.listGemitem[g.idGemTemplate] - player.listGemitemSell[g.idGemTemplate] <= 0
                                || player.listGemitem[g.idGemTemplate] - player.listGemitemSell[g.idGemTemplate] < len) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ nguyên liệu", ""));
                            player.sendMessage(MessageCreator.createCharGemItem(player));
                            return;
                        }

                        // Dung dung truong GemTemplate.ep - truong sinh ra CHINH DE danh dau
                        // "vien nay hop thanh len tang tren duoc" (GemTemplate.java:222, 262).
                        //
                        // Truoc day o day loc bang dai so cung "id >= 60 va != 65", rong hon
                        // ep rat nhieu. Hai nhom lot qua:
                        //   - Tang DINH cua moi ho da (73, 80, 87, 94, 101, 108, 115, 122,
                        //     129, 135) co ep = 0 dung de chan. Hop thanh ra "gtemplate + 1"
                        //     tuc la tang DAY cua ho KE TIEP -> nhay ho, leo dan len.
                        //   - Toan bo id tu 136 tro len, gom ca vat pham nap luong.
                        // ep cung la thu server GUI CHO CLIENT (MessageCreator.java:842) nen
                        // dung no o day thi hai ben khop nhau.
                        if (g.idGemTemplate < 0 || g.idGemTemplate >= gemTemplate.length
                                || gemTemplate[g.idGemTemplate] == null
                                || gemTemplate[g.idGemTemplate].ep != 1) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Loại nguyên liệu này không thể hợp thành", ""));
                            player.sendMessage(MessageCreator.createCharGemItem(player));
                            return;
                        }

                        for (int j = 0; j < gem.size(); j++) {
                            if (g.idGemTemplate != ((GemItem) gem.get(j)).idGemTemplate) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Không thể dùng 2 nguyên liệu khác loại", ""));
                                player.sendMessage(MessageCreator.createCharGemItem(player));
                                return;
                            }
                        }

                        gem.add(g);
                    }
                }

                if (gem.size() < 3) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có thể ép ít nhất 3 nguyên liệu trở lên", ""));
                    player.sendMessage(MessageCreator.createCharGemItem(player));
                    return;
                }

                byte[] percent = new byte[]{10, 20, 100};
                // GD2 1.4: "<=" cho ti le that 11%/21% (thay vi 10%/20%). Xem epNgocThanhCong.
                if (epNgocThanhCong(r.nextInt(100), gem.size())) {
                    int gtemplate = ((GemItem) gem.get(0)).idGemTemplate;

                    for (int ix = 0; ix < gem.size(); ix++) {
                        GemItem g = (GemItem) gem.get(ix);
                        if (player.listGemitem[g.idGemTemplate] > 0) {
                            player.listGemitem[g.idGemTemplate]--;
                        }

                        if (player.listGemitem[g.idGemTemplate] <= 0) {
                            player.removeItemOutVector(g.realId, player.gemItem);
                        }

                        player.allGemUse[g.idGemTemplate]++;
                    }

                    if (player.listGemitem[gtemplate + 1] == 0 && player.listGemitemLock[gtemplate + 1] == 0) {
                        GemItem ge = new GemItem(gtemplate + 1);
                        ge.ownerId = player.charDBID;
                        ge.realId = player.getIDItem();
                        player.gemItem.add(ge);
                    }

                    player.listGemitem[gtemplate + 1]++;
                    player.allGemGet[gtemplate + 1]++;
                    Database.instance.saveOrtherLog("tob_log_other_kham", player.charname, gemTemplate[gtemplate + 1].name, "epG");
                    player.sendMessage(MessageCreator.createServerAlertMessage("Hợp thành thành công", ""));
                    Message m = new Message(-36);
                    m.dos.writeByte(0);
                    m.dos.writeShort(prize);
                    player.sendMessage(m);
                } else {
                    int gtemplate1 = ((GemItem) gem.get(0)).idGemTemplate;

                    for (int ix = 0; ix < gem.size(); ix++) {
                        GemItem gx = (GemItem) gem.get(ix);
                        if (player.listGemitem[gx.idGemTemplate] > 0) {
                            player.listGemitem[gx.idGemTemplate]--;
                        }

                        if (player.listGemitem[gx.idGemTemplate] <= 0) {
                            player.removeItemOutVector(gx.realId, player.gemItem);
                        }

                        player.allGemUse[gx.idGemTemplate]++;
                    }

                    Database.instance.saveOrtherLog("tob_log_other_kham", player.charname, gemTemplate[gtemplate1].name, "epGF");
                    player.sendMessage(MessageCreator.createServerAlertMessage("Hợp thành thất bại", ""));
                    Message m = new Message(-36);
                    m.dos.writeByte(0);
                    m.dos.writeShort(prize);
                    player.sendMessage(m);
                }

                player.subXu((long) prize, true, "map 76");
                player.sendMessage(MessageCreator.createCharGemItem(player));
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            }
        } catch (Exception var11) {
        }
    }

    static void doFinishKhamItem(Map map, Char player, int ok) {
        if (ok == -1) {
            player.itemImbue = 40000;
            player.gemItemImbue.removeAllElements();
            return;
        }
        if (ok != 1) {
            return;
        }

        // synchronized (player): tu day tro xuong kiem lai ton kho ngoc/xu roi tru - gop
        // thanh mot khoi nguyen tu (dung chung khoa voi Char.truGemNeuDu()) de 2 goi tin
        // "Xong" gan dong thoi cho cung nhan vat khong the cung doc thay du truoc khi
        // ben nao kip tru.
        synchronized (player) {
        Item item = player.itemImbue == 40000 ? null : player.getItemFormVector(player.iItems, (short) player.itemImbue);
        if (item == null) {
            finishKhamResult(player, "Không tìm thấy vật phẩm");
            return;
        }
        if (isItemSelled(player, item.id, item)) {
            finishKhamResult(player, "Không thể thực hiện khi vật phẩm đang được bán");
            return;
        }

        GemItem gem = haveGemKham(map, player);
        if (gem == null) {
            finishKhamResult(player, "Chưa đặt ngọc hoặc đục lỗ.");
            return;
        }
        int idGem = gem.idGemTemplate;
        int mode = player.typeGemKham;
        if (mode < 0 || mode > 2) {
            finishKhamResult(player, "Lựa chọn nguyên liệu không hợp lệ");
            return;
        }

        // Kiem lai ton kho luc bam Xong; nguyen lieu co the da dung/rao ban sau khi dat.
        int normal = Math.max(0, player.listGemitem[idGem] - player.listGemitemSell[idGem]);
        int locked = player.listGemitemLock[idGem];
        boolean useLocked = mode == 1 || mode == 2 && locked > 0;
        if (player.getItemFormVector(gem.realId, player.gemItem) != gem
                || (useLocked ? locked : normal) <= 0) {
            finishKhamResult(player, "Không đủ ngọc hoặc đục lỗ đã chọn");
            return;
        }

        boolean drilling = gem.getTemplate().type == GemTemplate.TYPE_DUC_LO;
        if (drilling) {
            if (item.nSocAdd >= 6) {
                finishKhamResult(player, "Không thể tạo thêm");
                return;
            }
            if (idGem - GemTemplate.DUC_TUONG_KHAM_1 != item.nSocAdd) {
                finishKhamResult(player, "Bạn phải dùng đục cấp " + (item.nSocAdd + 1));
                return;
            }
        } else {
            if (item.nSocAdd <= 0) {
                finishKhamResult(player, "Vật phẩm không có chỗ để khảm vào.");
                return;
            }
            if (item.nhadSock >= item.nSocAdd) {
                finishKhamResult(player, "Vật phẩm không còn chỗ để khảm vào.");
                return;
            }
            if (!gem.getTemplate().belongItem(item.getTemplate().type)) {
                finishKhamResult(player, "Không thể khảm vào vật phẩm này.");
                return;
            }
        }
        // GD2 2.13 / THIET_KE 3.3: bat co sv.lau_dai thi gia khảm = 1 gio thu nhap x 1,45^(lan-1)
        // (lan = so vien da khảm + 1, toi da 10), duc lo = 1 gio. Tat co: 20.000 xu nhu cu.
        final long giaKham = com.htbb.pokemon.LauDai.bat()
                ? com.htbb.pokemon.KinhTeLauDai.giaNgocHe(player.lvDetail.lv, drilling ? 1 : item.nhadSock + 1)
                : PRICE_KHAM;
        if (player.getxu() < giaKham) {
            finishKhamResult(player, "Hết tiền");
            return;
        }

        // Chi thu phi sau khi hop le, va chi tieu hao vien thuc su duoc ap dung.
        player.subXu(giaKham, true, "map 75");
        player.delGem(idGem, 1, useLocked);
        NhiemVuEngine.suKien(player, "kham_ngoc", "", 1); // GD2b
        if (drilling) {
            item.nSocAdd++;
        } else {
            item.nhadSock++;
            gem.getTemplate().khamItem(item);
        }
        finishKhamResult(player, drilling ? "Vật phẩm hiện có " + item.nSocAdd + " lỗ" : "Khảm thành công");
        player.sendMessage(MessageCreator.createMsgItemInfo(item));
        try {
            String sub = item.getTemplate().name + "_" + item.plus + "_" + item.nhadSock
                    + "|" + item.getAttribute() + "|" + item.getDBInfo();
            Database.instance.saveOrtherLog("tob_log_other_kham", player.charname,
                    "kham " + sub + " >> " + mode, drilling ? "sock" : "kok");
        } catch (Exception e) {
            e.printStackTrace();
        }
        }
    }

    private static void finishKhamResult(Char player, String text) {
        // Goi -35 xoa nguyen lieu da dat tren client, ke ca khi that bai.
        // Dong bo lai tui de client khong giu xu tru tam hoac nguyen lieu cu.
        player.gemItemImbue.removeAllElements();
        try {
            Message m = new Message(-35);
            m.dos.writeUTF(text);
            player.sendMessage(m);
            m.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
        player.sendMessage(MessageCreator.createCharGemItem(player));
    }

    static void doImbue(Map map, Char player, int type) {
        if (player.startAutoImbue) {
            player.sendMessage(MessageCreator.createServerAlertMessage("Không thể thực hiện khi đang trong chế độ đập cộng tự động", ""));
        } else {
            int prizeImbue = 5000;

            try {
                if (getTown[player.inCountry]) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không thể thực hiện giao dịch trong thời gian chiếm Gym", ""));
                    return;
                }

                if (player.lvDetail.lv < 10) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Hãy đi luyện đến cấp 10 trở lên để sử dụng chức năng này.", ""));
                    return;
                }

                player.isImbue = type == 0;
                if (player.isImbue) {
                    player.sendMessage(MessageCreator.createMsgMenu(new String[]{"Nguyên liệu thường", "Nguyên liệu khóa", "Nguyên liệu tổng hợp."}, -505, 0));
                }
            } catch (Exception var5) {
            }
        }
    }

    static void doGiveSpecialGem2Ground(Map map, Char player, Message message) {
        if (!player.isCorrectPass && !player.passWord.equals("")) {
            player.sendMessage(MessageCreator.createMsgInputText(player.id, 4, "Nhập mật khẩu", 0));
        } else if (!map.checkTrade(player)) { // 19/10/2026: truoc day vut nguyen lieu duoc ca khi dang trao doi
            try {
                int type = message.dis.readByte();
                int id = message.dis.readShort();
                String info = "";
                if (type == 0) {
                    byte typeMaterial = message.dis.readByte();
                    // 19/10/2026: so luong TUY CHON (thieu = ca chong, nhu cu).
                    final int soLuongXin = HanhDongVatPham.docSoLuong(message);
                    if (typeMaterial == 1) {
                        DungVatPham.loi(player, "Vật phẩm khoá không bỏ được.");
                        return;
                    }
                    if (id < 0 || id >= player.listGemitem.length || player.listGemitem[id] <= 0) {
                        DungVatPham.loi(player, "Bạn không còn nguyên liệu này.");
                        return;
                    }

                    int i = 0;
                    int n = 0;

                    while (i < player.gemItem.size()) {
                        GemItem gem = (GemItem) player.gemItem.get(i);
                        i++;
                        if (gem.idGemTemplate == id) {
                            if (player.listGemitemSell[gem.idGemTemplate] > 0 && typeMaterial == 0) {
                                DungVatPham.loi(player, "Nguyên liệu đang rao bán, không bỏ được.");
                                return;
                            }

                            int var13 = 0;
                            if (typeMaterial == 0) {
                                n = HanhDongVatPham.kepSoLuong(soLuongXin, player.listGemitem[gem.idGemTemplate]);
                                player.listGemitem[gem.idGemTemplate] -= n;
                                player.allGemUse[gem.idGemTemplate] = player.allGemUse[gem.idGemTemplate] + n;
                            } else {
                                n = player.listGemitemLock[gem.idGemTemplate];
                                player.listGemitemLock[gem.idGemTemplate] = 0;
                                player.allGemUseLock[gem.idGemTemplate] = player.allGemUseLock[gem.idGemTemplate] + n;
                            }

                            if (player.listGemitem[gem.idGemTemplate] <= 0 && player.listGemitemLock[gem.idGemTemplate] <= 0) {
                                player.listGemitem[gem.idGemTemplate] = 0;
                                player.listGemitemLock[gem.idGemTemplate] = 0;
                                player.gemItem.remove(gem);
                                player.removeIDItem(gem.realId);
                            }

                            info = n + " " + gem.getTemplate().name + "_" + typeMaterial;
                            DungVatPham.bao(player, "Đã bỏ " + gem.getTemplate().name + " ×" + n + ".");
                            break;
                        }
                    }

                    player.sendMessage(MessageCreator.createCharGemItem(player));
                } else if (type == 1) {
                    int i = 0;

                    while (i < player.specialItem.size()) {
                        ShopTemplate shop = (ShopTemplate) player.specialItem.get(i);
                        i++;
                        if (shop.id == id) {
                            i = 0;
                            player.specialItem.remove(shop);
                            player.removeIDItem(shop.realId);
                            Database.instance.deleteGemItem(shop.dbid);
                            // Neu khong dat lai bo dem nay ve 0, doBuySpecialItem se nghi la
                            // nguoi choi van con ve (listSpItem[id] > 0) sau khi da vut ve ra
                            // dat, tu choi tao ve moi nhung VAN tru tien -> mat tien oan.
                            player.listSpItem[shop.id] = 0;
                            info = i + " " + shop.name;
                            DungVatPham.bao(player, "Đã bỏ " + shop.name + ".");
                        }
                    }

                    player.sendMessage(MessageCreator.createCharSpecialItem(player));
                }

                Database.instance.saveOrtherLog("tob_log_other_gem", player.charname, "Bỏ " + info + " ra đất", "delSP");
            } catch (Exception var10) {
                var10.printStackTrace();
            }
        }
    }

}
