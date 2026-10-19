# ProGuard cho bản PC phát hành (0.9.5): đổi tên class / hàm / biến của game + lớp nền PC thành a, b, c..., bỏ thông tin
# gỡ lỗi - như bản jar J2ME (client_jar/release.pro). KHÔNG tối ưu (giữ nguyên logic đã chạy ổn).
# -injars / -outjars / -printmapping / -libraryjars (jmod của JDK đang chạy) do dong_goi_pc.sh truyền vào.
-dontwarn **
-ignorewarnings
-dontoptimize
-dontnote **

# điểm vào
-keep public class htbb.pc.Chay {
    public static void main(java.lang.String[]);
}
# MIDlet tạo bằng Class.forName("game.GameMidlet")
-keep public class game.GameMidlet {
    public <init>();
}
# đọc bằng phản chiếu (ThanhLenh: kích thước hộp MsgDlg; TrangThai F9: trường menu)
-keepclassmembers class game.render.screen.MsgDlg {
    int wHop;
    int hHop;
    int y;
}
-keepclassmembers class game.render.screen.Menu {
    int menuX;
    int menuY;
    int menuW;
    int menuH;
    int menuTemY;
    int cyBag;
    int ctoyBag;
    int selected;
}
# lớp nền PC (cửa sổ, MIDP trên Java2D) giữ tên: ít bí mật, và kiểm thử KiemPc chạy được trên bản đã làm rối.
# Mã GAME (game.**, network.**, effect.**) bị đổi tên hết trừ 3 hàm KiemPc gọi.
-keep class htbb.pc.** { *; }
-keep class javax.microedition.** { *; }
-keep class javax.wireless.** { *; }
-keep class game.model.Char {
    public static int tinhDyTen(...);
}
-keep class game.pokemon.HopThoaiDuoi {
    public static long thoiGianDoc(...);
}
-keep class game.pokemon.VungMap {
    public static java.lang.String ten(...);
}
# luồng / sự kiện AWT gọi lại theo tên của thư viện: ProGuard tự giữ hàm ghi đè lớp thư viện.

-repackageclasses ''
-allowaccessmodification
-dontusemixedcaseclassnames
-keepattributes Exceptions,Signature,InnerClasses,EnclosingMethod
