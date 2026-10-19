package com.htbb.pokemon.hd;

/**
 * Toán thuần (không Android) của HdPhong - phóng cửa sổ jar thành thẻ lớn giữa khung 640x360 - tách ra để kiểm thử
 * không cần máy (client_apk/test/KiemHdPhong.java).
 *
 * Mỗi LỚP (0 = màn, 1 = hộp thoại trên màn) giữ trạng thái riêng: màn đang phóng, lúc mở (hoạt hình trượt vào),
 * khung thẻ đo được, phép phóng (cho đổi chạm). Bản cũ dùng MỘT trạng thái chung: có hộp thoại / bảng nhập số trên
 * cửa sổ đang phóng thì hai lớp giành nhau "màn trước" -> hoạt hình mở chạy lại MỖI khung (thẻ kẹt ở 92% cỡ, lệch
 * xuống 14 px) và khung đo bị bỏ -> popup xác nhận mua / bán / nhập số lệch; menu (Canvas.menu, vẽ không phóng) còn
 * bị đổi chạm theo phép phóng của cửa sổ bên dưới.
 */
public final class HdPhongToan {
    /** sau khoảng này không vẽ lớp đó = coi như đóng; vẽ lại = mở mới (có hoạt hình) */
    public static final long HET_MS = 300;
    /** thời gian hoạt hình mở */
    public static final long MO_MS = 180;

    private HdPhongToan() {
    }

    /** Trạng thái một lớp. */
    public static final class Lop {
        public Object man;
        public long lucMo, lanCuoi = Long.MIN_VALUE / 2;
        public float tdX, tdY, tdW, tdH;
        /** phép phóng khung trước (đổi chạm): man' = z * man + (dx, dy) */
        public float z = 1f, dx, dy;

        /** Bắt đầu vẽ lớp này với màn 'man' lúc 'now': true = mở mới (đặt lại hoạt hình + bỏ khung đo cũ). */
        public boolean vao(Object m, long now) {
            boolean moi = m != man || now - lanCuoi > HET_MS;
            if (moi) {
                man = m;
                lucMo = now;
                tdW = 0;
                tdH = 0;
            }
            lanCuoi = now;
            return moi;
        }

        /** Độ mở hoạt hình (0..1, ease-out bậc 3). */
        public float doMo(long now) {
            float t = Math.min(1f, Math.max(0f, (now - lucMo) / (float) MO_MS));
            return 1 - (1 - t) * (1 - t) * (1 - t);
        }

        public boolean dangDong(long now) {
            return now - lanCuoi < 100 && now - lucMo < MO_MS + 40;
        }

        public void boPhong() {
            z = 1f;
            dx = 0;
            dy = 0;
        }
    }

    /** Hệ số phóng đích (đã làm tròn theo 1/k để điểm ảnh thật nguyên khi k nguyên); <= 1.01 = không phóng. */
    public static float heSo(int W, int H, int band, float y, float w, float h, float zMax, int k, boolean neoTren) {
        float zz = neoTren ? Math.min((W - 24f) / w, (H - band - 6f - y) / h)
                : Math.min((W - 24f) / w, (H - band - 14f) / h);
        zz = Math.min(zz, zMax);
        k = Math.max(1, k);
        return (float) Math.floor(zz * k) / k;
    }

    /**
     * Phép phóng {z, dx, dy} cho thẻ (x, y, w, h) với độ mở e (1 = mở xong): thẻ canh giữa vùng trên thanh phím mềm
     * (cao band), neoTren = giữ mép trên.
     */
    public static float[] bienDoi(int W, int H, int band, float x, float y, float w, float h, float zz, int k, float e, boolean neoTren) {
        k = Math.max(1, k);
        float zHien = zz * (0.92f + 0.08f * e);
        float cx = x + w / 2f, cy = y + h / 2f;
        float tx = W / 2f, ty = (H - band) / 2f + 2 + (1 - e) * 14;
        float dx = Math.round((tx - zHien * cx) * k) / (float) k;
        float dy = neoTren ? Math.round((y - zHien * y) * k) / (float) k : Math.round((ty - zHien * cy) * k) / (float) k;
        return new float[]{zHien, dx, dy};
    }

    /** Điểm màn -> toạ độ jar theo phép phóng của lớp (thanh phím mềm đáy giữ nguyên). */
    public static int doi(float v, float z, float d, int y, int H, int band) {
        if (z == 1f && d == 0f || y >= H - band) return (int) v;
        return (int) Math.floor((v - d) / z);
    }
}
