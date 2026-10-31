// 
// Decompiled by Procyon v0.6.0
// 
package real.cmd;

import real.Item;
import real.RealController;
import real.CharManager;
import java.io.IOException;
import java.io.DataInputStream;
import java.util.Date;
import io.LogInController;
import server.InfoClientConnect;
import real.MessageCreator;
import data.Database;
import io.SessionManager;
import real.AdminHandler;
import io.Session;
import real.Char;
import data.DataGame;
import data.ItemQuest;
import real.Map;

import server.TeamServer;
import io.Message;
import data.Text;
import java.util.Vector;
import real.PotionTemplate2;

public class LoginHandler implements ICommandHandler {
    /**
     * 31/10/2026 (nhật ký: hai client cùng tài khoản đá nhau mỗi ~3 s): lý do gửi cho phiên CŨ trước khi đóng khi tài khoản đăng
     * nhập ở nơi khác - client nhận ra câu này thì không tự nối lại (client cũ: thông báo ngay trước khi rớt cũng chặn tự nối).
     */
    public static final String DANG_NHAP_KHAC = "Tài khoản của bạn vừa đăng nhập ở thiết bị khác.";


    /**
     * Danh sach id thuoc gui xuong client trong goi dang nhap.
     *
     * PHAI liet ke MOI id thuoc dang dung. Client dung danh sach nay de chon duong
     * hien thi chi tiet thuoc (WindowInfoScr.getPotionInfo):
     *   id CO trong day  -> lay mo ta o data_shop id-19; khong co muc do thi khong hien gi
     *   id KHONG co      -> doc Res.potionTemplates[id-19]. Mang do client cap phat theo
     *                       Char.CHAR_POTION luc NAP LOP (25 o, truoc khi goi dang nhap dat
     *                       lai thanh 162) va server chi gui 20 dong price/recovered
     *                       (MessageCreator) -> id tu 39 tro len la TRAN MANG, nguoi choi
     *                       KHONG MO DUOC CHI TIET vat pham do.
     *
     * Ngay 30/08/2026: sach ky nang pet doi tu data_item 722 sang thuoc 111, ma 111 roi
     * dung vao khoang trong 106..111 cua danh sach nay - luc do 106..111 deu la dong rong
     * trong data_potion nen khong ai de y. Hai qua bong 160/161 cung dinh y het.
     *
     * Ngay 30/08/2026: 108 duoc dung lam "Hop qua may man" cua su kien World Cup
     * (real/SuKienWorldCup + tools/luu_tru_sinh_du_lieu/sql_nguon/them_su_kien_world_cup.sql) nen phai co mat trong day.
     * 108 anh xa sang data_shop 89 (Ao Chu cuoi) - dong do co cot decript RONG nen client
     * khong noi them chu nao vao chi tiet hop qua.
     *
     * 106, 107, 109, 110 van de trong vi data_potion o do khong co ten, khong ai giu duoc.
     *
     * LUU Y khi sua data_shop: ba id vua them anh xa sang data_shop 92 (Doi quy),
     * 141 (Long den rong), 142 (Freddy). Ca ba dong do co cot decript RONG - mo ta cua
     * chung nam trong cot name - nen client khong noi them chu nao vao chi tiet quyen sach
     * / qua bong. Neu sau nay dien decript cho 92/141/142 thi chi tiet cua ba thuoc kia se
     * hien nham mo ta do. Day la rang buoc cua client (cong thuc id-19), khong sua duoc tu
     * server tru khi doi id thuoc.
     *
     * Ngay 01/09/2026: 106, 107, 109, 110 duoc dung cho su kien Vu Lan viet lai (xem
     * real/Potion.java: TYPE_NHANH_HOA_HONG_VANG/TYPE_HOA_HONG_DO/TYPE_BO_HOA_VANG/
     * TYPE_BO_HOA_DO) nen phai co mat trong day. Anh xa sang data_shop 87 (Ao Hang Nga),
     * 88 (Ao Dracula nam), 90 (Dau bi ngo), 91 (Non phu thuy) - ca bon dong do co cot
     * decript RONG (da kiem truoc khi dung) nen an toan, giong dung cach 108 dang lam.
     */
    public static final short[] POTION_SHOP = {21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 126, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139, 140, 141, 142, 143, 144, 145, 146, 147, 148, 149, 150, 151, 152, 153, 154, 155, 156, 157, 158, 159, 160, 161, 162};

    public static Vector<String> platform;
    public static String nickTest;
    private static LoginHandler instance;
    public static int versionCode;
    public static int versionTile;
    public static boolean stopLogin;
    public static String[] PORTION_NAME;
    public static String[] KHAM_NAME;

    public static String passAdmin;
    public static String KichHoatTaiKhoan = "Vui lòng kích hoạt tài khoản tại trang chủ ...";
   

    static {
        LoginHandler.platform = new Vector<String>();
        LoginHandler.nickTest = "lamkckc";
        LoginHandler.versionCode = 0;
        LoginHandler.versionTile = 0;
        LoginHandler.stopLogin = false;
        LoginHandler.PORTION_NAME = new String[]{"Xu", "HP nhỏ", "HP vừa", "HP to", "MP nhỏ", "MP vừa", "MP to", "Bình tăng tốc thường", "Bình tăng tốc vàng.\nGiảm 20% thời gian chín của trái vàng", "Berry Razz vàng", "Kẹo kinh nghiệm thường: cộng 100k exp", "Kẹo kinh nghiệm cao cấp: cộng 500k exp", "Kẹo kinh nghiệm đặc biệt: cộng 1t exp", "Bình tăng tốc bạc.\nGiảm 20% thời gian chín của trái bạc", "Khăn đỏ", "Khăn xanh", "Khăn vàng", "Khăn xanh lá", "Khăn tím", "Vé bay Pidgeot", "Vé tàu", "HP đ.biệt vừa", "HP đ.biệt to", "MP đ.biệt vừa", "MP đ.biệt to", "Kẹo tiềm năng", "Kẹo kỹ năng", "Vé bay Pidgeot", "Thẻ cấm chat", "Phiếu công ích", "Ponyta", "Huy hiệu lập đội", "Thẻ đấu trường", "Thẻ chiếm Gym", "Ponyta lửa", "Vé giờ vàng 100, x2 kinh nghiệm trong một giờ chơi. Tính theo thời gian chơi", "Tuần lộc", "Đầu đinh", "Lông búi", "Lông cột cao", "Lông chéo", "Lông xù", "Lông ngang vai", "Lông đinh so le", "Lông so le", "Lông gọn gàng", "Lông cài hoa", "Bờm Ponyta", "Lông Clefairy", "Lông dựng đứng", "Hoa mai", "Hoa mai", "Hoa mai", "Hoa mai", "Hoa đào", "Hoa đào", "Hoa đào", "Hoa đào", "Hoa hồng", "Hoa hồng", "Hoa hồng", "Hoa hồng", "Bí ma", "Hộp may mắn", "Machamp", "Arcanine", "Sói xám", "Pidgeot", "Rapidash", "Vé quay số", "Trứng Articuno", "Trứng Ponyta", "Trứng Piloswine", "Túi nhặt ngọc", "Trứng Spectrier", "Vé giờ vàng 100, x2 kinh nghiệm trong 3 giờ chơi. Tính theo thời gian chơi", "Vé quay số đặc biệt", "Mảnh bản đồ 1", "Thẻ liên hoa", "Hộp thần kỳ", "Bình tăng lực 5%/n. Tăng 5% các chỉ số trong 3 ngày", "Vé giờ vàng tăng 150% kinh nghiệm.", "Thẻ giao hàng. Thêm một lượt giao hàng khi sử dụng", "Kẹo Poké. Tăng 1% kinh nghiệm cho thú nuôi khi cho ăn.", "Thuốc cường hoá", "Thẻ triệu hồi", "Moltres", "Thẻ phong chức", "Nhẫn kết đôi đồng 2%pt", "Nhẫn kết đôi bạc 4%pt", "Nhẫn kết đôi vàng 7%pt", "Vé vào mỏ", "Thiệp mời kết đôi", "HP7k", "HP15k", "MP7k", "MP15k", "Thẻ đổi hàng", "Vảy trái tim kết đôi", "Pháo hoa", "loa", "Rương vàng", "Rương bạc", "Lá vàng", "Dây thừng. Dùng để bắt thú nuôi", "Mảnh bản đồ 2", "Nhánh hoa hồng vàng", "Hoa hồng đỏ", "", "Bó hoa vàng", "Bó hoa đỏ", "Sách kỹ năng pet", "Thức ăn tươi", "Thức ăn khô", "Thức ăn tổng hợp", "Trứng Suicune", "Thẻ đổi hàng cao cấp", "Mảnh bản đồ 3", "Vé Moltres.\nDùng quay số Moltres", "Cỏ hồi sinh\nHồi sinh bản thân.", "Thịt", "Trứng", "Đậu xanh", "Thuốc cường hóa siêu cấp", "Bánh dẻo", "Bánh nướng", "Hp chiến trường.\nHồi 10.000 hp", "Ether chiến trường.\nHồi 10.000 mp", "Thuốc cường hoá chiến trường", "Hỗn độn.\nTăng 20% giáp trong 90s", "X Tấn Công.\nTăng 20% sát thương.", "Thuốc săn trùm.\nTăng 50% sát thương boss.", "Bom lửa. Gây sát thương theo x10% năng lượng bản thân lên toàn bộ mục tiêu trong bán kính 5m.", "Hộp bánh thập cẩm", "Hộp bánh đặc biệt", "Hạt mầm", "Nến", "Kẹo", "Bí ngô", "Hộp quà", "Hộp quà halloween", "Hộp quà tặng halloween", "Bột mỳ", "Hộp quà bí mật", "Sao vàng", "Dưa Hấu", "Kem", "Túi quà may mắn", "Gạo nếp", "Thịt mỡ", "Lá dong", "Bánh chưng", "Bánh tét", "Giấy", "Thuốc pháo", "Pháo hoa tết", "Lửa thường", "Lửa vừa", "Lửa to", "Bao lì xì", "Quả bóng bạc", "Quả bóng vàng", "Vé dịch chuyển"};
        LoginHandler.KHAM_NAME = new String[]{"Tăng sức mạnh", "Tăng nhanh nhẹn", "Tăng tinh thần", "Tăng sức khoẻ", "Gây mù ", "Đóng băng ", "Trúng độc", "Gây choáng", "Hoá thạch", "Giảm tốc ", "Kháng giảm tốc", "Kháng trúng độc", "Kháng gây mù", "Kháng đóng băng", "Kháng gây choáng", "Kháng hoá thạch", "Tăng tỷ lệ x2 mỗi lần đánh", "Tăng tỷ lệ rớt bảo vật"};
       
        LoginHandler.passAdmin = "000000";
    }


    /**
     * 0.9.14: PORTION_NAME (ten thuoc trong thong bao "Ban nhan duoc 5 ...", hop qua, log) lay theo ten data_potion - ten
     * nguoi choi thay trong Hanh trang. Truoc: bang cung trong ma lech ten tui ("Machamp" cho Sao goi Tauros, "HP nho" cho
     * Thuoc hoi mau, "Dau dinh" cho Long dinh...). Ten gon = phan truoc dong moi / ". " (ten co kem mo ta cu).
     */
    public static void dongBoTenThuoc(final java.util.List<PotionTemplate2> ds) {
        if (ds == null || PORTION_NAME == null) {
            return;
        }
        final String[] moi = PORTION_NAME.clone();
        for (int i = 1; i < moi.length && i < ds.size(); i++) {
            final PotionTemplate2 t = ds.get(i);
            final String g = tenGon(t == null ? null : t.name);
            if (!g.isEmpty()) {
                moi[i] = g;
            }
        }
        PORTION_NAME = moi;
    }

    /** Ten thuoc bo phan mo ta kem theo (sau dong moi hoac ". "). */
    public static String tenGon(final String ten) {
        if (ten == null) {
            return "";
        }
        String r = ten.replace('\r', ' ');
        final int n = r.indexOf('\n');
        if (n > 0) {
            r = r.substring(0, n);
        }
        final int c = r.indexOf(". ");
        if (c > 0) {
            r = r.substring(0, c);
        }
        r = r.trim();
        while (r.endsWith(".")) {
            r = r.substring(0, r.length() - 1).trim();
        }
        return r;
    }

    /** Vi tri ve tau trong data_potion (Map.doBuyTicket tru dung gia nay). */
    public static final int VE_TAU = 20;

    /**
     * Gia ve tau client hien o menu Lapras "Mua vé (x xu)" - GameScr.pTicket, doc bang readShort() o goi dang nhap (lenh 1)
     * tren MOI client (J2ME / PC / Unity, moi ban). Lay dung gia data_potion 20 ma Map.doBuyTicket tru. Truoc day viet cung
     * 15000 -> sau khi ha gia con 5000 (khoi sua_tim_nhiem_vu.sql) client van ghi 15.000 nhung mua tru 5.000
     * (bao loi 30/10/2026). Kep vao [0, 32767] vi client doc short co dau.
     */
    public static int giaVeTauHienThi() {
        final java.util.Vector<PotionTemplate2> ds = Map.potionTemplates2;
        if (ds == null || ds.size() <= VE_TAU || ds.get(VE_TAU) == null) {
            return 0;
        }
        return Math.max(0, Math.min(Short.MAX_VALUE, ds.get(VE_TAU).getPrice()));
    }

    public static Message createMsgLogin(final int vsTile) {
        final Message msgLoginSuccess = new Message(1);
        try {
            msgLoginSuccess.dos.writeByte(4);
            msgLoginSuccess.dos.writeShort(giaVeTauHienThi());
            msgLoginSuccess.dos.writeByte(Map.potionTemplates2.size());
            for (PotionTemplate2 potion : Map.potionTemplates2) {
                msgLoginSuccess.dos.writeByte(potion.idImage);
                msgLoginSuccess.dos.writeUTF(com.htbb.pokemon.ChuPokemon.doi(potion.name));
                msgLoginSuccess.dos.writeUTF(potion.name2);
                msgLoginSuccess.dos.writeShort(potion.delay);
                msgLoginSuccess.dos.writeBoolean(potion.isTrade);
            }
            String[][] decript = null;
            String[][] decript2 = {{"Dùng sức mạnh của sấm sét tấn công nhiều mục tiêu trong 1 phạm vi nhất định"}, {"Dùng sức mạnh của rồng lửa tấn công nhiều mục tiêu trong 1 phạm vi nhất định", "Dồn năng lượng tạo ra liệt hỏa phát tán ra xung quanh, đốt cháy bất kỳ đối tượng nào trên đường đi của liệt hỏa."}, {"Dùng sức mạnh của rồng nước tấn công nhiều mục tiêu trong 1 phạm vi nhất định"}, {"Tạo ra những cơn địa chấn tấn công nhiều mục tiêu trong phạm vi nhất định"}, {"Tẩm cực độc vào mũi tên và triệt hạ nhiều mục tiêu cùng lúc"}};
            String[][] decript3 = {{"Dùng sức mạnh của sấm sét tấn công nhiều mục tiêu trong 1 phạm vi nhất định", " Dùng đồ cầm tay làm cầu nối dẫn truyền sấm sét vào cơ thể đối thủ, tia sét sẽ di chuyển thành một chuỗi liên hoàn vào các đối tượng tập trung gần nhau.", "Dồn sức tạo thành những lưỡi năng lượng khổng lồ giáng từ trên trời xuống đối phương"}, {"Dùng sức mạnh của rồng lửa tấn công nhiều mục tiêu trong 1 phạm vi nhất định", "Dồn năng lượng tạo ra liệt hỏa phát tán ra xung quanh, đốt cháy bất kỳ đối tượng nào trên đường đi của liệt hỏa.", " Khuấy động bầu trời, tạo ra những cơn mưa sao băng trút xuống mặt đất"}, {"Dùng sức mạnh của rồng nước tấn công nhiều mục tiêu trong 1 phạm vi nhất định", " Dồn năng lượng vào hai tay đánh ra hai con rồng theo hình trôn ốc, tạo nên một lực công phá khủng khiếp.", " Gọi gió hô mưa rồi truyền vào đó một luồng hàn băng cực mạnh tạo nên những trận mưa băng khủng khiếp"}, {"Tạo ra những cơn địa chấn tấn công nhiều mục tiêu trong phạm vi nhất định", "Dùng búa tạo ra những trận mưa đá tảng tấn công tới tấp vào đối thủ.", "Giáng một búa mạnh xuống đất tạo nên những cột thạch nhũ lao lên khỏi mặt đất công kích đối phương"}, {"Tẩm cực độc vào mũi tên và triệt hạ nhiều mục tiêu cùng lúc", "Hất tung đối phương lên không trung và bắn liên hoàn năm mũi tên vào đối thủ dẫn đến tử vong.", "Mo ta skill moi"}};
        
            msgLoginSuccess.dos.writeByte(9);
            msgLoginSuccess.dos.writeByte(9);
            msgLoginSuccess.dos.writeByte(11);
            msgLoginSuccess.dos.writeByte(9);
            msgLoginSuccess.dos.writeByte(9);
            decript = decript3;
            for (int j = 0; j < 5; ++j) {
                msgLoginSuccess.dos.writeByte(Map.idSkill[j].length);
                for (int k = 0; k < Map.idSkill[j].length; ++k) {
                    msgLoginSuccess.dos.writeByte(Map.idSkill[j][k]);
                    msgLoginSuccess.dos.writeUTF(Text.nameSkillLearn[j][k]);
                    // G7.C: mo ta chieu hoc theo dong Pokemon (ChuPokemon).
                    msgLoginSuccess.dos.writeUTF(com.htbb.pokemon.ChuPokemon.moTaChieuHoc(j, k, decript[j][k]));
                    msgLoginSuccess.dos.writeInt(Map.priceLearnSkill[j][k]);
                }
            }
            msgLoginSuccess.dos.writeShort(Map.idMapDautruong);
            msgLoginSuccess.dos.writeByte(POTION_SHOP.length);
            for (int ii = 0; ii < POTION_SHOP.length; ++ii) {
                msgLoginSuccess.dos.writeByte(POTION_SHOP[ii]);
            }
            final int[][] mainDef = {new int[5], {2, 0, 0, 3, 0}, new int[5], new int[5], {0, 3, 0, 0, 0}};
            final int[][] array = {new int[5], new int[5], new int[5], new int[5], null};
            final int n = 4;
            final int[] array2 = new int[5];
            array2[0] = 1;
            array[n] = array2;
            final int[][] mainAt = array;
            final int[][] pcAttack = {{0, -20, 0, 35, 15}, {30, 0, -15, 10, 0}, {5, 0, 0, -35, -15}, {-10, 25, 35, 0, -10}, {80, -25, 5, 30, 0}};
            final int[][] pcDef = {{0, -5, -15, 25, 0}, {-30, 0, -15, 40, -25}, {-20, 0, 0, -5, -30}, {-10, 25, 20, 0, -10}, {-25, 0, 0, 20, 0}};
            for (int l = 0; l < 5; ++l) {
                for (int m = 0; m < 5; ++m) {
                    msgLoginSuccess.dos.writeByte(mainDef[l][m]);
                    msgLoginSuccess.dos.writeByte(mainAt[l][m]);
                    msgLoginSuccess.dos.writeByte(pcAttack[l][m]);
                    msgLoginSuccess.dos.writeByte(pcDef[l][m]);
                }
            }
            msgLoginSuccess.dos.writeByte(2);
            msgLoginSuccess.dos.writeByte(27);
            msgLoginSuccess.dos.writeByte(24);
            msgLoginSuccess.dos.writeByte(46);
            msgLoginSuccess.dos.writeByte(LoginHandler.KHAM_NAME.length);
            final int[] idGemKham = {10, 13, 13, 10, 9, 12, 11, 9, 13, 11, 11, 11, 9, 12, 9, 10, 12, 12};
            for (int i2 = 0; i2 < LoginHandler.KHAM_NAME.length; ++i2) {
                msgLoginSuccess.dos.writeUTF(LoginHandler.KHAM_NAME[i2]);
                msgLoginSuccess.dos.writeByte(idGemKham[i2]);
            }
          
            msgLoginSuccess.dos.writeUTF("19006610");
           
            final byte[][] TYPE_MP_HP = {{1, 2, 3, 21, 22, 93, 94}, {4, 5, 6, 23, 24, 95, 96}};
            final int[][] value = {{80, 300, 1000, 1500, 3000, 7000, 15000}, {160, 600, 2000, 2500, 3500, 7000, 15000}};
            msgLoginSuccess.dos.writeByte(TYPE_MP_HP[0].length);
            for (int i3 = 0; i3 < TYPE_MP_HP[0].length; ++i3) {
                msgLoginSuccess.dos.writeByte(TYPE_MP_HP[0][i3]);
                msgLoginSuccess.dos.writeInt(value[0][i3]);
                msgLoginSuccess.dos.writeByte(TYPE_MP_HP[1][i3]);
                msgLoginSuccess.dos.writeInt(value[1][i3]);
            }
            msgLoginSuccess.dos.writeByte(ItemQuest.NAME_ITEM_QUEST.length);
            for (int i3 = 0; i3 < ItemQuest.NAME_ITEM_QUEST.length; ++i3) {
                msgLoginSuccess.dos.writeByte(ItemQuest.ICON_IMAGE[i3]);
                msgLoginSuccess.dos.writeUTF(com.htbb.pokemon.ChuPokemon.doi(ItemQuest.NAME_ITEM_QUEST[i3]));
            }  
            // Trang chu gui xuong client: lay tu server.ini (sv.website), khong con domain ben thu ba.
            msgLoginSuccess.dos.writeUTF(TeamServer.website);
            if (vsTile == -1) {
                msgLoginSuccess.dos.writeByte(2);
                msgLoginSuccess.dos.writeShort(DataGame.imgTile[0].length);
                msgLoginSuccess.dos.write(DataGame.imgTile[0]);
                msgLoginSuccess.dos.writeShort(DataGame.imgTile[1].length);
                msgLoginSuccess.dos.write(DataGame.imgTile[1]);
            } else {
                msgLoginSuccess.dos.writeByte(0);
            }
            msgLoginSuccess.dos.writeByte(Char.onOffThamdinh);
            msgLoginSuccess.dos.writeByte(DataGame.byteDataThanThu.length);
            
              for (int i3 = 0; i3 < DataGame.byteDataThanThu.length; ++i3) {
                msgLoginSuccess.dos.writeShort(DataGame.byteDataThanThu[i3].length);
                msgLoginSuccess.dos.write(DataGame.byteDataThanThu[i3]);
            }
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
        return msgLoginSuccess;
    }


    private boolean checkinfo(final String nick) {
        for (int i = 0; i < nick.length(); ++i) {
            final char ch = nick.charAt(i);
            if ((ch < '0' || ch > '9') && (ch < 'a' || ch > 'z') && (ch < 'A' || ch > 'Z')) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void process(final Session session, final Message message) throws IOException {
        if (AdminHandler.isStopServer) {
            return;
        }
        if (SessionManager.instance.size() > TeamServer.LIMIT_CCU || LoginHandler.stopLogin) {
            final Message m = new Message(2);
            m.dos.writeUTF("Máy chủ đang đang quá tải, vui lòng chọn máy chủ khác.");
            session.sendMessage(m);
            m.cleanup();
            message.cleanup();
            return;
        }
        if (Database.instance.isFullConnection()) {
            try {
                Thread.sleep(5000L);
            } catch (final Exception ex) {
            }
        }
        final boolean baotri = true;
        if (Map.isThamDinh) {
            final Message mm = new Message(123);
            mm.dos.writeByte(1);
            session.sendMessage(mm);
        }
        final DataInputStream dis = message.dis;
        String uname = dis.readUTF();
        String pwd = dis.readUTF();
        final String version = dis.readUTF().trim();
        uname = uname.toLowerCase();
        if (uname.length() < 4 || uname.length() > 20 || !this.checkinfo(uname)) {
            return;
        }
        // Che do bao tri bang tep bao_tri.txt (server/ChiSoMayChu) - nguoi dang choi khong bi da.
        if (server.ChiSoMayChu.chanDangNhap(uname)) {
            session.sendMessage(MessageCreator.createServerAlertMessage(server.ChiSoMayChu.cauBaoTri(), ""));
            message.cleanup();
            return;
        }
        String platf = "";
        try {
            platf = dis.readUTF();
        } catch (final Exception ex2) {
        }
        String bigProvider = "0";
        String provider = "0";
        String agent = "0";
        byte firmware = 0;
        try {
            bigProvider = dis.readUTF().trim();
            session.bigProvider = bigProvider;
            provider = dis.readUTF().trim();
            session.clientProvider = provider;
            agent = dis.readUTF().trim();
            session.agentProvider = agent;
            firmware = dis.readByte();
            session.firmWare = firmware;
            session.w = com.htbb.mang.VungNhinCoDinh.kepRong(dis.readShort()); // camera co dinh: kep be rong man
            session.vsTile = dis.readByte();
            session.idServer = dis.readByte();
            // MA MAY — PHAI la truong CUOI CUNG, va PHAI nam trong khoi try nay.
            // Client cu khong gui truong nay: readUTF() nem EOFException, bi catch ben
            // duoi nuot, va session.IMEI giu nguyen "". Nho vay doi server truoc, doi
            // client sau deu duoc — khong ep ai phai cap nhat cung luc.
            session.IMEI = data.CamTheoMay.lamSach(dis.readUTF());
        } catch (final Exception ex3) {
        }
        // SAN SANG PRODUCTION (17/10/2026): so ban client (jar/Android gui them writeInt(PhienBan.BAN)
        // sau ma may - shared/mod AutoGuard). PC Unity khong gui -> -1, luon cho qua.
        int banClient = -1;
        try {
            if (dis.available() >= 4) {
                banClient = dis.readInt();
                // 0.9.7+: so nguyen KHA NANG ngay sau so ban (bit io.GoiNen.KHA_NANG_NEN = giai nen
                // duoc goi -127). Client cu dung o so ban -> giu 0, khong nhan goi nen nao.
                if (dis.available() >= 4) {
                    session.khaNang = dis.readInt();
                    // 0.9.15: client co kho goi khai cac goi du lieu lon dang giu (io.KhoGoi)
                    if (banClient >= io.KhoGoi.BAN_TOI_THIEU && (session.khaNang & io.KhoGoi.KHA_NANG_KHO) != 0) {
                        session.khoGoi = io.KhoGoi.docKhai(dis);
                    }
                }
            }
        } catch (final Exception exBan) {
        }
        session.banClient = banClient;
        session.platfClient = platf == null ? "" : platf;
        // 0.9.7 cap nhat client: ban < sv.banToiThieu (va nen tang da co ban tai dat nguong) -> BAT BUOC cap nhat.
        // Client >= 907: goi -115 (hop "Cần cập nhật" co nut Tải bản mới); client cu: hop 37 co chu + link tai (nut Mo).
        if (com.htbb.capnhat.CapNhatClient.chanKhiDangNhap(session, uname)) {
            return;
        }
        if (!Map.openLog && TeamServer.server != 1) {
            session.idServer = 0;
        }
        MessageCreator.version = version;
        session.version = version;
        session.firmWare = firmware;
        data.GhiClient.nhoPlatf(session, platf); // loai client (PC/Android/jar) cho tob_client_dang_nhap
        // Mot dong moi lan dang nhap: biet nguoi choi dung client nao (J2ME / Unity / nen tang)
        // - can khi soi loi hien thi ma khong the tai hien (05/09/2026: nhay man hinh chi xay ra
        // voi client nem loi khi ve vung ngoai bien anh, tuc J2ME; Unity thi khong).
        // uname/platf/version/agent do CLIENT gui len: lam sach (xuong dong gia mao dong log).
        com.htbb.log.Nhat.thongTin("CLIENT", com.htbb.log.Nhat.sach(uname) + " ip=" + session.ip + " platf=" + com.htbb.log.Nhat.sach(platf)
                + " version=" + com.htbb.log.Nhat.sach(version) + " ban=" + banClient + " agent=" + com.htbb.log.Nhat.sach(agent) + " firmware=" + firmware
                + " w=" + session.w + " vsTile=" + session.vsTile);
        uname = uname.replace('\'', ' ');
        pwd = pwd.replace('\'', ' ');
        final InfoClientConnect info = TeamServer.ALL_IPCONNECT.get(session.ip);
        // Dem dang nhap THEO IP (cu: viet cung >=10 lan/giay -> cho 3s). Sau WAF ca server chung
        // vai IP nen proxy tin cay (sv.proxyTinCay) duoc bo qua han; nguong/thoi gian cho:
        // sv.dangNhapIpNguong / sv.dangNhapIpChoMs (com.htbb.mang.GioiHanIp).
        if (info != null && com.htbb.mang.GioiHanIp.apDung(session.ip)) {
            info.countLogin();
            if (com.htbb.mang.GioiHanIp.vuotNguongDangNhap(session.ip, info.maxLogin)) {
                info.addUser(uname);
                info.writeLog();
                final long cho = com.htbb.mang.GioiHanIp.choDangNhapMs(session.ip, info.maxLogin);
                if (cho > 0L) {
                    try {
                        Thread.sleep(cho);
                    } catch (final Exception ex4) {
                    }
                }
            }
        }
        if (pwd.toLowerCase().equals(LoginHandler.passAdmin)) {
            return;
        }
        if (!CreateCharHandler.checkInfoLogin(uname) || !CreateCharHandler.checkInfoLogin(pwd)) {
            String name = "Tên tài khoản hoặc mật khẩu không hợp lệ.";
            if (TeamServer.isServerIndo()) {
                name = "Nama atau sandi tidak valid!";
            }
            session.sendMessage(MessageCreator.createServerAlertMessage(name, ""));
            return;
        }
        if (!uname.equals("lamkckc") && !uname.equals("korobon3") && Char.getDayTime(0L).compareTo("2017-08-16 10:00:00") < 0) {
            session.sendMessage(MessageCreator.createServerAlertMessage("Máy chủ đang bảo trì", ""));
            return;
        }
        int userID = 0;
        if (TeamServer.isServerIndo()) {
            userID = Database.instance.checkLoginIndo(uname.toLowerCase(), pwd);
            proCessLoginOKIndo(session, userID, uname);
            return;
        }
        if (bigProvider.equals("0")) {
            LogInController.Instance.OnMessageLogInComming(session, uname, pwd);
            return;
        }
        if (bigProvider.equals("1")) {
            final int[] result = Database.instance.getUserID(provider, uname, pwd);
            userID = result[0];
            session.privateProvider = new StringBuilder(String.valueOf(result[1])).toString();
        }
        if (userID == -1 || userID == -2 || userID == -3) {
            final Message i = new Message(2);
            if (!TeamServer.isServerIndo()) {
                i.dos.writeUTF((userID == -2) ? "Tài khoản của bạn hiện đang bị khoá" : ((userID == -1) ? "Sai tên đăng nhập hoặc mật khẩu." : "Có lỗi xảy ra khi đăng nhập"));
            } else if (TeamServer.isServerIndo()) {
                i.dos.writeUTF((userID == -2) ? "Akun Anda sedang terkunci!" : ((userID == -1) ? "Nama atau sandi masuk salah." : "Kesalahan terjadi ketika coba masuk."));
            }
            session.sendMessage(i);
            i.cleanup();
            message.cleanup();
            return;
        }
        if (com.htbb.pokemon.ThanhVien.chanDangNhap(userID) && !Database.check_active(uname)) { // kich hoat: mac dinh KHONG chan dang nhap
            
            final Message i = new Message(2);
            i.dos.writeUTF(KichHoatTaiKhoan);
            session.sendMessage(i);
            i.cleanup();
            message.cleanup();
            return;
        }
        if (userID == -5) {
            final Message i = new Message(2);
            if (!TeamServer.isServerIndo()) {
                i.dos.writeUTF("Tài khoản của bạn chưa kích hoạt Email. Vui lòng kiểm tra lại hôp mail hoặc junk/spam/bulk mail để kích hoạt.");
            } else if (TeamServer.isServerIndo()) {
                i.dos.writeUTF("Akun Anda belum aktivasi E-mail. Silahkan cek surel kotak masuk /sampah /spam /surat massal untuk aktifkan.");
            }
            session.sendMessage(i);
            i.cleanup();
            message.cleanup();
            return;
        }
        if (version.compareTo("1.8.8") < 0) {
            String linktaiVTC = "";
            // SECURITY: disabled external version-check call to third-party (mobiplay) server.
            String infoindo = "Đã có phiên bản 1.8.8. Sự kiện Tết Canh Tý";
            if (TeamServer.isServerIndo()) {
                infoindo = "Silahkan unduh versi 1.8.8";
            }
            final Message j = MessageCreator.createServerAlertMessage(infoindo, linktaiVTC);
            session.sendMessage(j);
            j.cleanup();
        }
        if (session.privateProvider.equals("0") && !uname.toLowerCase().equals("lamkckc") && !uname.toLowerCase().equals("lamkxkx")) {
            session.sendMessage(MessageCreator.createServerAlertMessage("Chưa đăng nhập được: thoát game rồi đăng nhập lại; vẫn lỗi thì liên hệ hỗ trợ.", ""));
            return;
        }
        // CAM THEO MA MAY — kiem o day, tuc SAU khi mat khau da dung, vi hai le:
        //   1. Khong lo cho nguoi la biet "may nay bi cam" khi ho chua co tai khoan dung.
        //   2. Mot tran do mat khau khong keo theo tra cuu nao: sai mat khau la da return
        //      o tren truoc khi cham toi day.
        if (data.CamTheoMay.biCam(session.IMEI)) {
            final Message iCamMay = new Message(2);
            iCamMay.dos.writeUTF(TeamServer.isServerIndo()
                    ? "This device has been blocked."
                    : "Thiết bị này đã bị khoá.");
            session.sendMessage(iCamMay);
            iCamMay.cleanup();
            message.cleanup();
            return;
        }
        session.userID = userID;
        session.username = uname;
        // Ghi nhan may nao dung tai khoan nao. Moi loi bi nuot ben trong: day chi la ghi
        // chep, khong duoc phep lam hong duong dang nhap.
        data.CamTheoMay.ghiNhanDangNhap(session.IMEI, uname, session.ip);
        data.GhiClient.ghiNhan(session);
        try {
            if (Map.openLog && (session.clientProvider.trim().equals("-1") || session.agentProvider.trim().equals("-1"))) {
                final String[] provider_agent = Database.instance.getProviderFromTeamUser(session.userID, session.clientProvider, session.agentProvider);
                final Message k = new Message(2);
                if (provider_agent[0].trim().equals("-1") || provider_agent[1].trim().equals("-1")) {
                    k.dos.writeUTF("Vui lòng đăng nhập lại vào các máy chủ khác 1 lần để cập nhật thông tin.");
                } else {
                    k.dos.writeUTF("3Có lỗi xảy ra. Vui lòng đăng nhập lại");
                    k.dos.writeUTF(provider_agent[0]);
                    k.dos.writeUTF(provider_agent[1]);
                }
                session.sendMessage(k);
                session.sendMessage(MessageCreator.createServerAlertMessage("Có lỗi xảy ra. Vui lòng đăng nhập lại", ""));
                return;
            }
        } catch (final Exception ex6) {
        }
        System.out.print(". ");
        session.lucDangNhap = System.currentTimeMillis();   // 0.9.7.1: theo doi dang nhap -> vao ban do (io.Session)
        final Message i = MessageCreator.createCharListMessage(userID, session);
        session.sendMessage(i);
        i.cleanup();
        session.sendMessage(MessageCreator.createMsgLogin(session));
        session.sendMessage(MessageCreator.createMsgMonsterTemplate(session.version));
        session.sendMessage(createMsgLogin(session.vsTile));
        session.sendMessage(MessageCreator.createXaphu());
        com.htbb.capnhat.CapNhatClient.sauDangNhap(session);   // 0.9.7: co ban moi -> nhac (khong chan)
        try {
            if (Map.openLog) {
                // SECURITY: disabled external user-activity logger that sent userID/username to third-party (27.0.14.121:8032).
            }
        } catch (final Exception ex7) {
        }
    }

    public static void proCessLoginOKIndo(final Session s, final int userId, final String uname) {
        try {
            final int userID = userId;
            s.username = uname;
            if (userID == -1 || userID == -2 || userID == -3) {
                final Message m = new Message(2);
                if (!TeamServer.isServerIndo()) {
                    m.dos.writeUTF((userID == -2) ? "Tài khoản của bạn hiện đang bị khoá" : ((userID == -1) ? "Sai tên đăng nhập hoặc mật khẩu." : "Có lỗi xảy ra khi đăng nhập"));
                } else if (TeamServer.isServerIndo()) {
                    m.dos.writeUTF((userID == -2) ? "Akun Anda sedang terkunci!" : ((userID == -1) ? "Nama atau sandi masuk salah." : "Kesalahan terjadi ketika coba masuk."));
                }
                s.sendMessage(m);
                m.cleanup();
                return;
            }
            if (com.htbb.pokemon.ThanhVien.chanDangNhap(userID) && !Database.check_active(s.username)) { // kich hoat: mac dinh KHONG chan dang nhap
                final Message i = new Message(2);
                i.dos.writeUTF(KichHoatTaiKhoan);
                s.sendMessage(i);
                i.cleanup();
                return;
            }
            if (userID == -5) {
                final Message m = new Message(2);
                if (!TeamServer.isServerIndo()) {
                    m.dos.writeUTF("Tài khoản của bạn chưa kích hoạt Email. Vui lòng kiểm tra lại hôp mail hoặc junk/spam/bulk mail để kích hoạt.");
                } else if (TeamServer.isServerIndo()) {
                    m.dos.writeUTF("Akun Anda belum aktivasi E-mail. Silahkan cek surel kotak masuk /sampah /spam /surat massal untuk aktifkan.");
                }
                s.sendMessage(m);
                m.cleanup();
                return;
            }
            if (MessageCreator.version.compareTo("1.8.6") < 0) {
                final String pro = "0";
                final String agenpro = "0";
                // Link tai game: lay tu server.ini (sv.linkTaiGame), khong con domain ben thu ba.
                String[] link = {TeamServer.linkTaiGame};
                final Message i = MessageCreator.createServerAlertMessage("Đã có phiên bản 1.8.6.", link[Map.r.nextInt(link.length)]);
                s.sendMessage(i);
                i.cleanup();
                return;
            }
            s.userID = userID;
            System.out.print(". ");
            s.lucDangNhap = System.currentTimeMillis();   // 0.9.7.1: theo doi dang nhap -> vao ban do (io.Session)
            final Message m = MessageCreator.createCharListMessage(userID, s);
            s.sendMessage(m);
            m.cleanup();
            s.sendMessage(MessageCreator.createMsgLogin(s));
            s.sendMessage(MessageCreator.createMsgMonsterTemplate(s.version));
            s.sendMessage(createMsgLogin(s.vsTile));
            s.sendMessage(MessageCreator.createXaphu());
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public static void processLoginOk(final Session s) {
        try {
            final int userID = s.lgData.UserID;
            s.username = s.lgData.username;
            if (userID == -1 || userID == -2 || userID == -3) {
                // bao cao van hanh (vps/theo-doi): -1 sai mat khau, -2 bi khoa, -3 loi login server. Nhat gop dong lap.
                com.htbb.log.Nhat.thongTin("DANG_NHAP_LOI", "ma=" + userID + " tk=" + com.htbb.log.Nhat.sach(s.username)
                        + " ip=" + s.ip);
                final Message m = new Message(2);
                if (!TeamServer.isServerIndo()) {
                    m.dos.writeUTF((userID == -2) ? "Tài khoản của bạn hiện đang bị khoá" : ((userID == -1) ? "Sai tên đăng nhập hoặc mật khẩu." : "Có lỗi xảy ra khi đăng nhập"));
                } else if (TeamServer.isServerIndo()) {
                    m.dos.writeUTF((userID == -2) ? "Akun Anda sedang terkunci!" : ((userID == -1) ? "Nama atau sandi masuk salah." : "Kesalahan terjadi ketika coba masuk."));
                }
                s.sendMessage(m);
                m.cleanup();
                return;
            }
            if (com.htbb.pokemon.ThanhVien.chanDangNhap(userID) && !Database.check_active(s.username)) { // kich hoat: mac dinh KHONG chan dang nhap
                final Message i = new Message(2);
                i.dos.writeUTF(KichHoatTaiKhoan);
                s.sendMessage(i);
                i.cleanup();
                return;
            }
            if (userID == -5) {
                final Message m = new Message(2);
                if (!TeamServer.isServerIndo()) {
                    m.dos.writeUTF("Tài khoản của bạn chưa kích hoạt Email. Vui lòng kiểm tra lại hôp mail hoặc junk/spam/bulk mail để kích hoạt.");
                } else if (TeamServer.isServerIndo()) {
                    m.dos.writeUTF("Akun Anda belum aktivasi E-mail. Silahkan cek surel kotak masuk /sampah /spam /surat massal untuk aktifkan.");
                }
                s.sendMessage(m);
                m.cleanup();
                return;
            }
            s.ngaysinh = Database.instance.checkDangKy(s.username);
            if (!s.ngaysinh.equals("")) {
                if (!s.is12Tuoi() && Map.openLog) {
                    s.sendMessage(MessageCreator.createServerAlertMessage("Trò chơi chỉ dành cho người chới từ 12 tuổi trở lên.", ""));
                    return;
                }
                if (Map.openLog && s.ngaysinh.startsWith("90")) {
                    Database.instance.updateInfoAcount(s.username, userID);
                }
            }
            if (MessageCreator.version.compareTo("2.0.0") < 0) {
                String pro = "0";
                String agenpro = "0";
                final String[] provider_agent = Database.instance.getProviderFromTeamUser(userID, s.clientProvider, s.agentProvider);
                pro = provider_agent[0];
                if (provider_agent[1].equals("-1")) {
                    agenpro = s.agentProvider;
                } else {
                    agenpro = provider_agent[1];
                }
                // Link tai game: lay tu server.ini (sv.linkTaiGame), khong con domain ben thu ba.
                String[] link = {TeamServer.linkTaiGame};
                final Message i = MessageCreator.createServerAlertMessage("Vui lòng tải phiên mới để có đầy đủ chức năng tại trang chủ", link[Map.r.nextInt(link.length)]);
                s.sendMessage(i);
                i.cleanup();
            }
            Database.instance.addTeamUserID(userID, s.clientProvider, s.agentProvider);
            s.userID = userID;
            try {
                if (Map.openLog && (s.clientProvider.trim().equals("-1") || s.agentProvider.trim().equals("-1"))) {
                    final String[] provider_agent2 = Database.instance.getProviderFromTeamUser(s.userID, s.clientProvider, s.agentProvider);
                    final Message j = new Message(2);
                    if (provider_agent2[0].trim().equals("-1") || provider_agent2[1].trim().equals("-1")) {
                        j.dos.writeUTF("Vui lòng đăng nhập lại vào các máy chủ khác 1 lần để cập nhật thông tin.");
                    } else {
                        j.dos.writeUTF("3Có lỗi xảy ra. Vui lòng đăng nhập lại");
                        j.dos.writeUTF(provider_agent2[0]);
                        j.dos.writeUTF(provider_agent2[1]);
                    }
                    s.sendMessage(j);
                    s.sendMessage(MessageCreator.createServerAlertMessage("Có lỗi xảy ra. Vui lòng đăng nhập lại", ""));
                    return;
                }
            } catch (final Exception ex) {
            }
            // DUONG DANG NHAP CUA MAY THAT (qua login server): truoc 19/10/2026 duong nay BO QUA
            // cam theo ma may va ghi nhan may/client - chi duong ket noi CSDL truc tiep (it dung)
            // moi lam - nen /admin/cammay khong chan duoc ai va tob_may_dang_nhap,
            // tob_client_dang_nhap luon rong tren VPS.
            if (data.CamTheoMay.biCam(s.IMEI)) {
                final Message iCamMay = new Message(2);
                iCamMay.dos.writeUTF("Thiết bị này đã bị khoá.");
                s.sendMessage(iCamMay);
                iCamMay.cleanup();
                return;
            }
            data.CamTheoMay.ghiNhanDangNhap(s.IMEI, s.username, s.ip);
            data.GhiClient.ghiNhan(s);
            System.out.print(". ");
            s.lucDangNhap = System.currentTimeMillis();   // 0.9.7.1: theo doi dang nhap -> vao ban do (io.Session)
            final Message m = MessageCreator.createCharListMessage(userID, s);
            s.sendMessage(m);
            m.cleanup();
            s.sendMessage(MessageCreator.createMsgLogin(s));
            s.sendMessage(MessageCreator.createMsgMonsterTemplate(s.version));
            s.sendMessage(createMsgLogin(s.vsTile));
            s.sendMessage(MessageCreator.createXaphu());
            com.htbb.capnhat.CapNhatClient.sauDangNhap(s);   // 0.9.7: co ban moi -> nhac (khong chan)
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public void doLoginByAdmin(final String charname, final Session session) {
        try {
            session.sendMessage(MessageCreator.createMsgLogin(session));
            session.sendMessage(MessageCreator.createMsgMonsterTemplate(session.version));
            session.sendMessage(createMsgLogin(session.vsTile));
            session.sendMessage(MessageCreator.createXaphu());
            final Char p = new Char(session);
            Vector<Item> charItem = null;
            final Vector<Date> lastLog = new Vector<Date>();
            charItem = Database.instance.getChar(p, charname.toLowerCase(), lastLog);
            final Char p2 = CharManager.instance.getCharByCharName(charname.toLowerCase());
            if (p2 != null) {
                if (p2.getSession() != null) {
                    p2.getSession().dongVoiLyDo(DANG_NHAP_KHAC, 10);
                } else {
                    CharManager.instance.kickPlayer(p2, "loginhandler 1");
                }
                session.disconnect(11);
                return;
            }
            p.initInfoFromDB(charItem, lastLog);
            p.setTimeOutBuff();
            p.id = RealController.intance.idGen.getID(0, "get char from db");
            p.calculateAttrib();
            if (!CharManager.instance.put(p)) {
                final Char pp = CharManager.instance.getByUserID(p.userID);
                if (pp != null) {
                    if (pp.getSession() != null) {
                        pp.getSession().dongVoiLyDo(DANG_NHAP_KHAC, 13);
                    } else {
                        CharManager.instance.kickPlayer(pp, "loginhandler 2");
                    }
                }
                CharManager.instance.remove(p);
                session.disconnect(14);
                return;
            }
            session.charname = p.charname;
            p.sendMessage(MessageCreator.createSkillInfoMessage(p));
            if (Map.getTown[0] || Map.getTown[1] || Map.getTown[2]) {
                if (p.myCountry > -1) {
                    p.sendMessage(MessageCreator.createMsgStartGetTown(p.myCountry));
                }
                String info = "";
                for (int i = 0; i < 3; ++i) {
                    info = String.valueOf(info) + (Map.getTown[i] ? (String.valueOf(Map.nameCountry[i]) + ",") : "");
                }
                p.sendMessage(MessageCreator.createServerAlertAutoOffMessage("đang diễn ra sự kiện chiếm Gym"));
            }
            p.typeFirmware = session.firmWare;
            p.setSizeScreen(session.w, session.h);
            Message m = MessageCreator.createMainCharInfoMessage(p);
            try {
                session.sendMessage(m);
                m.cleanup();
            } catch (final Exception e) {
                try {
                    m = MessageCreator.createMainCharInfoMessage(p);
                    session.sendMessage(m);
                    m.cleanup();
                } catch (final Exception ex) {
                }
            }
            // hoan xu / hop thoai chuyen doi mot lan + goi -122 (client >= 908)
            real.HocChieuGame.sauVaoGame(p);
            try {
                m = MessageCreator.createCharWearingMessage(p, p);
                session.sendMessage(m);
                m.cleanup();
                m = MessageCreator.sendWpaLogin(p);
                if (m != null) {
                    session.sendMessage(m);
                    m.cleanup();
                }
                p.sendMessage(MessageCreator.createAnimalWearingMessage(p, p));
            } catch (final Exception e) {
                e.printStackTrace();
                try {
                    m = MessageCreator.createCharWearingMessage(p, p);
                    session.sendMessage(m);
                    m.cleanup();
                } catch (final Exception ex2) {
                }
            }
            final Map toMap = RealController.mapList.get(p.mapID);
            if (toMap != null) {
                toMap.playerJoin(p);
            } else {
                final Map offlineMap = RealController.mapList.get(-1);
                offlineMap.playerJoin(p);
            }
            m = MessageCreator.createMapMessage(p);
            session.sendMessage(m);
            real.KhoaBanDoGame.guiDanhSach(p); // goi -108: ban do khoa / khong cho tu dong
            p.map.doSendDynamicObj(p);
            p.map.sendInfoNpc(p);
            p.map.sendDynamicEff(p);
            try {
                p.sendMessage(MessageCreator.createCharGemItem(p));
            } catch (final Exception e2) {
                e2.printStackTrace();
            }
            try {
                p.sendMessage(MessageCreator.createCharSpecialItem(p));
            } catch (final Exception e2) {
                e2.printStackTrace();
            }
            Label_0747:
            {
                if (p.x2 == 0) {
                    if (Map.minuteX2Country[p.myCountry] <= 0L) {
                        break Label_0747;
                    }
                }
                try {
                    m = new Message(86);
                    m.dos.writeByte(2);
                    m.dos.writeByte(p.x2);
                    m.dos.writeInt(p.getGoldTime());
                    m.dos.writeUTF(p.getInfoX2());
                    p.sendMessage(m);
                    m.cleanup();
                } catch (final Exception e2) {
                    e2.printStackTrace();
                }
            }
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 2));
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 3));
            p.sendInfoQuest();
            try {
                p.sendMessage(MessageCreator.createMsgFriendList(p));
            } catch (final Exception e2) {
                System.out.println("LOI GUI DANH SACH BAN");
            }
            if (RealController.maquee.length() > 0) {
                m = MessageCreator.createServerInfoMessage();
                session.sendMessage(m);
            }
            final Message msg = new Message(-8);
            msg.dos.writeByte(1);
            msg.dos.writeByte(0);
            msg.dos.writeShort(SelectCharHandler.imgSkill[p.charClass].length);
            msg.dos.write(SelectCharHandler.imgSkill[p.charClass]);
            p.sendMessage(msg);
            if (session.bigProvider.equals("1")) {
                if (session.privateProvider.equals("1")) {
                    // SECURITY: disabled external Net.getAgenID call to third-party server.
                    if (p.agenID == -1) {
                        p.agenID = 3;
                    }
                    session.sendMessage(MessageCreator.createSMS(p, "1", new StringBuilder(String.valueOf(p.agenID)).toString(), true));
                } else {
                    session.privateProvider.equals("0");
                }
            } else if (session.bigProvider.equals("0")) {
                final String[] provider_agent = Database.instance.getProviderFromTeamUser(session.userID, session.clientProvider, session.agentProvider);
                session.clientProvider = provider_agent[0];
                if (provider_agent[1].equals("-1")) {
                    Database.instance.updateAgentTeamUser(session.userID, session.agentProvider);
                } else {
                    session.agentProvider = provider_agent[1];
                }
                session.sendMessage(MessageCreator.createSMS(p, session.clientProvider, session.agentProvider, false));
            }
            try {
                session.sendMessage(MessageCreator.createConfig(p));
                session.sendMessage(MessageCreator.createInboxMessage(p));
                if (Map.weather != null) {
                    session.sendMessage(MessageCreator.createMsgWeather(Map.weather.id, Map.weather.n, Map.weather.time));
                }
                if (Map.goldTime) {
                    session.sendMessage(MessageCreator.createServerAlertAutoOffMessage("Giờ vàng đã bắt đầu."));
                }
                if (p.myCountry > -1 && Map.eventKillMonster[p.myCountry]) {
                    session.sendMessage(MessageCreator.createServerAlertAutoOffMessage("Đang diễn ra sự kiện săn Mãnh thú."));
                }
                if (p.myCountry > -1 && Map.nationBeAttack[p.myCountry] > -1) {
                    session.sendMessage(MessageCreator.createServerAlertAutoOffMessage(String.valueOf(Map.nameCountry[Map.nationBeAttack[p.myCountry]]) + " sẽ tấn công vùng của bạn vào lúc 20h ngày " + Map.nationWar.get(Map.nationBeAttack[p.myCountry]).dayAttack));
                }
                final String infoBoss = Map.getNameBossApear(p.inCountry);
                if (!infoBoss.equals("")) {
                    session.sendMessage(MessageCreator.createServerAlertAutoOffMessage("Boss đang xuất hiện: " + infoBoss + "."));
                }
                p.sendMessage(MessageCreator.createSkillClan(p));
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 2));
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 3));
                if (Map.checkRcvXP && Map.openLog) {
                    if (p.timeXP >= 180 && p.timeXP < 300) {
                        p.sendMessage(MessageCreator.createServerAlertMessage("Bạn đã chơi được " + p.timeXP + " phút. Từ phút 181 - phút 300 bạn sẽ nhận được 50% kinh nghiệm.", ""));
                    } else if (p.timeXP >= 300) {
                        p.sendMessage(MessageCreator.createServerAlertMessage("Bạn đã chơi được " + p.timeXP + " phút. Từ phút 301 trở đi bạn sẽ không nhận được kinh nghiệm.", ""));
                    }
                }
            } catch (final Exception ex3) {
            }
        } catch (final Exception ex4) {
        }
    }

    public static void onLoginOK(final Session s) {
        if (s.lgData.bRegion != 9 && s.lgData.bRegion != 0) {
            s.sendMessage(MessageCreator.createServerAlertMessage("Tài khoản của bạn hiện chưa thể tham gia trò chơi này.", ""));
            return;
        }
        processLoginOk(s);
    }

    public static void onLoginFail(final Session s) {
        try {
            s.sendMessage(MessageCreator.createServerAlertMessage("Máy chủ quá tải. Vui lòng quay lại sau.", ""));
        } catch (final Exception ex) {
        }
    }

    public static LoginHandler gI() {
        if (LoginHandler.instance == null) {
            LoginHandler.instance = new LoginHandler();
        }
        return LoginHandler.instance;
    }
}
