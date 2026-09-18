package real;

// Mot dong niem yet ngoc trong gian hang, mang theo SO LUONG (thay vi 1 dong = 1 vien
// nhu truoc, xem CharSell/ShopSellItem). 19/09/2026, ban theo yeu cau "chuyen nguyen
// lieu so luong lon trong 1 lan nhap" tai NPC Nhat Nguu/Tam Giap.
// Ke thua Actor (lop cu the, khong bat buoc override gi) chi de tuong thich voi cac cho
// dang dung kieu "Actor item" dung chung cho ca type==0 (Item) lan type==1 (truoc day GemItem).
public class GemSellEntry extends Actor {
    public short idChar;
    public short realId;
    public short idGemTemplate;
    public int soLuong;
    /** Gia MOI VIEN. Tong tien phai tra = prizeSell * soLuong (tinh o noi dung). */
    public int prizeSell;
    public String charCanBuy = "";

    @Override
    public String getCharcanBuy() {
        return this.charCanBuy;
    }

    @Override
    public void setCharcanBuy(String charname) {
        this.charCanBuy = charname;
    }
}
