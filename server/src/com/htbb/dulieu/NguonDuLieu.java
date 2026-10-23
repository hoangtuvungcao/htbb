package com.htbb.dulieu;

/**
 * Noi doc bang du lieu: CSDL that ({@code data.DatabaseDuLieuSql}, luc chay server) hoac tep dump .sql
 * trong repo ({@link DocDumpSql}, cho test / cong cu / mo phong khong co CSDL).
 */
public interface NguonDuLieu {

    /**
     * Doc cac cot {@code cot} cua bang {@code bang}. Ten bang / cot la HANG trong ma (danh sach trang
     * {@link DuLieuSql}), khong bao gio lay tu nguoi dung.
     *
     * @throws Exception bang / cot khong co, hoac loi doc (noi goi in log va quyet dinh dung server)
     */
    BangSql doc(String bang, String[] cot) throws Exception;

    /** Mo ta ngan cho log ("CSDL htbb_db", "tep ../sql/htbb.sql"). */
    String moTa();
}
