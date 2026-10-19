package htbb.pc;

import java.awt.event.InputMethodEvent;
import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;

/** Nhập chữ bản PC: tách chữ đã chốt của bộ gõ, lọc chữ dán (Ctrl+V) - hàm thuần, kiểm được không cần màn hình. */
public final class NhapChu {
    private NhapChu() {
    }

    /** Phần chữ ĐÃ CHỐT (committed) của sự kiện bộ gõ; phần đang soạn (pre-edit) bỏ qua. */
    public static String chuDaChot(InputMethodEvent e) {
        AttributedCharacterIterator it = e.getText();
        int n = e.getCommittedCharacterCount();
        if (it == null || n <= 0) return "";
        StringBuilder b = new StringBuilder(n);
        char c = it.first();
        for (int i = 0; i < n && c != CharacterIterator.DONE; i++) {
            b.append(c);
            c = it.next();
        }
        return b.toString();
    }

    /** KEY_TYPED là chữ thật (không phải Ctrl / Alt / Meta + phím = phím tắt; AltGr vẫn là chữ). */
    public static boolean laChuGo(java.awt.event.KeyEvent e) {
        char c = e.getKeyChar();
        if (c == java.awt.event.KeyEvent.CHAR_UNDEFINED) return false;
        int m = e.getModifiersEx();
        if ((m & java.awt.event.KeyEvent.ALT_GRAPH_DOWN_MASK) != 0) return true;
        return (m & (java.awt.event.KeyEvent.CTRL_DOWN_MASK | java.awt.event.KeyEvent.META_DOWN_MASK
                | java.awt.event.KeyEvent.ALT_DOWN_MASK)) == 0;
    }

    /** Chữ dán: xuống dòng / tab thành dấu cách, bỏ ký tự điều khiển, cắt tối đa toiDa ký tự. */
    public static String locDan(String s, int toiDa) {
        if (s == null) return "";
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length() && b.length() < toiDa; i++) {
            char c = s.charAt(i);
            if (c == '\n' || c == '\r' || c == '\t') c = ' ';
            if (c < 32 || c == 127) continue;
            b.append(c);
        }
        return b.toString();
    }
}
