package com.htbb.capnhat;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Bo doc JSON NHO (khong thu vien ngoai - server chi co jxl / mysql / htbb-core): doi tuong -> LinkedHashMap,
 * mang -> ArrayList, chuoi -> String, so -> Double (nguyen thi Long), true/false -> Boolean, null -> null.
 * Dung cho web_cai_dat 'taiGame' (com.htbb.capnhat.CapNhatClient). JSON hong -> IllegalArgumentException.
 */
public final class JsonNho {

    private final String s;
    private int i;
    private int sau;

    private JsonNho(final String s) {
        this.s = s;
    }

    /** Doc mot gia tri JSON (chuoi rong / null -> null). */
    public static Object doc(final String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        final JsonNho j = new JsonNho(json);
        final Object v = j.giaTri();
        j.boTrang();
        if (j.i != j.s.length()) {
            throw new IllegalArgumentException("JSON thua ky tu o vi tri " + j.i);
        }
        return v;
    }

    private void boTrang() {
        while (i < s.length()) {
            final char c = s.charAt(i);
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                i++;
            } else {
                break;
            }
        }
    }

    private IllegalArgumentException loi(final String chu) {
        return new IllegalArgumentException("JSON hong (" + chu + ") o vi tri " + i);
    }

    private Object giaTri() {
        if (++sau > 64) {
            throw loi("long qua sau");
        }
        try {
            boTrang();
            if (i >= s.length()) {
                throw loi("het du lieu");
            }
            final char c = s.charAt(i);
            switch (c) {
                case '{':
                    return doiTuong();
                case '[':
                    return mang();
                case '"':
                    return chuoi();
                case 't':
                    return tu("true", Boolean.TRUE);
                case 'f':
                    return tu("false", Boolean.FALSE);
                case 'n':
                    return tu("null", null);
                default:
                    if (c == '-' || (c >= '0' && c <= '9')) {
                        return so();
                    }
                    throw loi("ky tu '" + c + "'");
            }
        } finally {
            sau--;
        }
    }

    private Object tu(final String t, final Object v) {
        if (!s.startsWith(t, i)) {
            throw loi("mong doi " + t);
        }
        i += t.length();
        return v;
    }

    private Map<String, Object> doiTuong() {
        final Map<String, Object> m = new LinkedHashMap<String, Object>();
        i++;
        boTrang();
        if (i < s.length() && s.charAt(i) == '}') {
            i++;
            return m;
        }
        while (true) {
            boTrang();
            if (i >= s.length() || s.charAt(i) != '"') {
                throw loi("mong doi khoa");
            }
            final String k = chuoi();
            boTrang();
            if (i >= s.length() || s.charAt(i) != ':') {
                throw loi("mong doi ':'");
            }
            i++;
            m.put(k, giaTri());
            boTrang();
            if (i >= s.length()) {
                throw loi("het du lieu trong doi tuong");
            }
            final char c = s.charAt(i++);
            if (c == '}') {
                return m;
            }
            if (c != ',') {
                throw loi("mong doi ',' hoac '}'");
            }
        }
    }

    private List<Object> mang() {
        final List<Object> a = new ArrayList<Object>();
        i++;
        boTrang();
        if (i < s.length() && s.charAt(i) == ']') {
            i++;
            return a;
        }
        while (true) {
            a.add(giaTri());
            boTrang();
            if (i >= s.length()) {
                throw loi("het du lieu trong mang");
            }
            final char c = s.charAt(i++);
            if (c == ']') {
                return a;
            }
            if (c != ',') {
                throw loi("mong doi ',' hoac ']'");
            }
        }
    }

    private String chuoi() {
        i++;
        final StringBuilder b = new StringBuilder();
        while (true) {
            if (i >= s.length()) {
                throw loi("chuoi chua dong");
            }
            final char c = s.charAt(i++);
            if (c == '"') {
                return b.toString();
            }
            if (c != '\\') {
                b.append(c);
                continue;
            }
            if (i >= s.length()) {
                throw loi("thoat chuoi do dang");
            }
            final char e = s.charAt(i++);
            switch (e) {
                case '"':
                case '\\':
                case '/':
                    b.append(e);
                    break;
                case 'b':
                    b.append('\b');
                    break;
                case 'f':
                    b.append('\f');
                    break;
                case 'n':
                    b.append('\n');
                    break;
                case 'r':
                    b.append('\r');
                    break;
                case 't':
                    b.append('\t');
                    break;
                case 'u':
                    if (i + 4 > s.length()) {
                        throw loi("\\u do dang");
                    }
                    try {
                        b.append((char) Integer.parseInt(s.substring(i, i + 4), 16));
                    } catch (final NumberFormatException ex) {
                        throw loi("\\u sai");
                    }
                    i += 4;
                    break;
                default:
                    throw loi("thoat chuoi la \\" + e);
            }
        }
    }

    private Object so() {
        final int dau = i;
        if (s.charAt(i) == '-') {
            i++;
        }
        boolean thuc = false;
        while (i < s.length()) {
            final char c = s.charAt(i);
            if (c >= '0' && c <= '9') {
                i++;
            } else if (c == '.' || c == 'e' || c == 'E' || c == '+' || c == '-') {
                thuc = true;
                i++;
            } else {
                break;
            }
        }
        final String t = s.substring(dau, i);
        try {
            if (!thuc) {
                return Long.valueOf(Long.parseLong(t));
            }
            return Double.valueOf(Double.parseDouble(t));
        } catch (final NumberFormatException ex) {
            throw loi("so sai '" + t + "'");
        }
    }
}
