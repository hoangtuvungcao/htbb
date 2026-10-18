package real;

// Tach tu lib/htbb-core.jar (19/09/2026) de them buoc "nhap so luong" cho ngoc (type==1)
// giua buoc nhap ten nguoi mua va buoc niem yet that su. charBuy da co san tu truoc
// (khong dung toi) nay dung de mang ten nguoi mua qua them 1 buoc nua.
public class ItemWaitSell {
    public int type;
    public int seller;
    public int shopId;
    public int itemID;
    public int prize_sell;
    boolean sell;
    public String charBuy = "";
}
