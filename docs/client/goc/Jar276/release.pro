# ProGuard cho ban phat hanh: doi ten toan bo class/ham/bien thanh a, b, c...
# va xoa thong tin debug. KHONG toi uu (giu nguyen logic da chay on), KHONG preverify
# (giong ban thuong). -injars/-outjars/-printmapping do build.sh truyen vao.
-libraryjars tools/midp.jar
# rt.jar cua JRE 8 (java/**): build.sh ghi vao out/rt.pro theo bien RT_JAR
-dontwarn **
-ignorewarnings
-dontoptimize
-dontpreverify
-dontnote **

# Lop MIDlet ten nam trong MANIFEST (MIDlet-1: ...,game.GameMidlet)
-keep public class game.GameMidlet {
    public <init>();
}

-repackageclasses ''
-allowaccessmodification
-dontusemixedcaseclassnames
-keepattributes Exceptions
