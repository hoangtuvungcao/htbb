import java.util.Random;

/**
 * Ten nhan vat kieu game thu Viet cho Huan luyen vien AI: 4..10 ky tu [0-9a-zA-Z] (luat CreateCharHandler.loiTen /
 * checkInfo). May chu van kiem tu cam (bang bad_word) va trung ten - bi tu choi thi dich vu thu ten khac. Client hien
 * "bot_ai: <ten>" (may chu them tien to), nen ten khong can danh dau la bot.
 */
final class HlvAiTen {

    private HlvAiTen() {
    }

    static final String[] TEN = {"An", "Anh", "Bao", "Binh", "Chau", "Chi", "Cuong", "Dat", "Duc", "Dung", "Duy", "Giang", "Ha",
        "Hai", "Han", "Hao", "Hieu", "Hoa", "Hoang", "Hung", "Huy", "Khang", "Khanh", "Khoa", "Kien", "Lam", "Lan", "Linh", "Loc",
        "Long", "Luan", "Mai", "Minh", "My", "Nam", "Nga", "Ngoc", "Nhan", "Nhi", "Phat", "Phong", "Phuc", "Phuong", "Quan", "Quang",
        "Quynh", "Son", "Tai", "Tam", "Tan", "Thang", "Thanh", "Thao", "Thinh", "Thu", "Tien", "Toan", "Trang", "Tri", "Trung",
        "Tu", "Tuan", "Tung", "Uyen", "Viet", "Vinh", "Vy", "Xuan", "Yen"};
    static final String[] BIET_DANH = {"Bin", "Bon", "Bi", "Bo", "Cun", "Ti", "Teo", "Ben", "Na", "Su", "Mon", "Bap", "Tun", "Xu",
        "Gau", "Meo", "Soi", "Tho", "Rong", "Cop", "Ga", "Vit", "Heo", "Sam", "Bong", "Kem", "Mit", "Dau", "Nho", "Cam"};
    static final String[] DUOI = {"Pro", "Vip", "Kute", "Xinh", "Dz", "Vn", "PK", "Sky", "Boy", "Girl", "Kun", "Chan", "Top",
        "Hd", "Ok", "Zz", "Real", "Max", "Neo", "One"};
    static final String[] POKE = {"Pika", "Eevee", "Mew", "Charm", "Bulba", "Squirt", "Snor", "Lucar", "Gengar", "Riolu", "Togep",
        "Psy", "Jiggly", "Meowth", "Vulpix", "Growl", "Dratini", "Abra", "Zubat", "Pichu"};

    /** Mot ten ngau nhien 4..10 ky tu chu so. */
    static String moi(Random r) {
        for (int lan = 0; lan < 50; lan++) {
            String t;
            switch (r.nextInt(7)) {
                case 0: t = TEN[r.nextInt(TEN.length)] + TEN[r.nextInt(TEN.length)]; break;
                case 1: t = TEN[r.nextInt(TEN.length)] + DUOI[r.nextInt(DUOI.length)]; break;
                case 2: t = BIET_DANH[r.nextInt(BIET_DANH.length)] + BIET_DANH[r.nextInt(BIET_DANH.length)] + (r.nextInt(3) == 0 ? "" : String.valueOf(r.nextInt(99) + 1)); break;
                case 3: t = TEN[r.nextInt(TEN.length)] + (r.nextBoolean() ? 2000 + r.nextInt(12) : r.nextInt(100)); break;
                case 4: t = POKE[r.nextInt(POKE.length)] + TEN[r.nextInt(TEN.length)]; break;
                case 5: t = BIET_DANH[r.nextInt(BIET_DANH.length)] + TEN[r.nextInt(TEN.length)]; break;
                default: t = POKE[r.nextInt(POKE.length)] + (r.nextBoolean() ? DUOI[r.nextInt(DUOI.length)] : String.valueOf(r.nextInt(999)));
            }
            if (r.nextInt(4) == 0) t = t.toLowerCase();
            if (hopLe(t)) return t;
        }
        return "Hlv" + (1000 + r.nextInt(9000));
    }

    static boolean hopLe(String t) {
        return t != null && t.length() >= 4 && t.length() <= 10 && t.matches("[0-9a-zA-Z]+");
    }
}
