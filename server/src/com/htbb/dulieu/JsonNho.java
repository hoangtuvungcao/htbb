package com.htbb.dulieu;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Bo doc JSON toi gian (server khong co thu vien JSON luc chay). Chi dung cho o JSON nho do tool sinh ra
 * (vd {@code cau_hinh_danh_muc.gioi_han}). Tra: Map (object), List (mang), String, Long / Double (so),
 * Boolean, null. JSON sai cu phap -> {@link IllegalArgumentException}.
 */
public final class JsonNho {

    private final String s;
    private int i;

    private JsonNho(final String s) {
        this.s = s;
    }

    public static Object doc(final String s) {
        final JsonNho j = new JsonNho(s == null ? "" : s);
        j.trang();
        if (j.i >= j.s.length()) {
            return null;
        }
        final Object v = j.giaTri();
        j.trang();
        if (j.i != j.s.length()) {
            throw j.loi("thua ky tu");
        }
        return v;
    }

    /** Doc object JSON (chuoi rong / "{}" -> map rong). */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> docObject(final String s) {
        final Object v = doc(s);
        if (v == null) {
            return new LinkedHashMap<String, Object>();
        }
        if (!(v instanceof Map)) {
            throw new IllegalArgumentException("JSON khong phai object: " + s);
        }
        return (Map<String, Object>) v;
    }

    private IllegalArgumentException loi(final String m) {
        return new IllegalArgumentException("JSON loi o vi tri " + i + ": " + m + " (" + s + ")");
    }

    private void trang() {
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
            i++;
        }
    }

    private Object giaTri() {
        trang();
        if (i >= s.length()) {
            throw loi("het chuoi");
        }
        final char c = s.charAt(i);
        if (c == '{') {
            return object();
        }
        if (c == '[') {
            return mang();
        }
        if (c == '"') {
            return chuoi();
        }
        if (s.startsWith("true", i)) {
            i += 4;
            return Boolean.TRUE;
        }
        if (s.startsWith("false", i)) {
            i += 5;
            return Boolean.FALSE;
        }
        if (s.startsWith("null", i)) {
            i += 4;
            return null;
        }
        return so();
    }

    private Map<String, Object> object() {
        final Map<String, Object> m = new LinkedHashMap<String, Object>();
        i++;
        trang();
        if (i < s.length() && s.charAt(i) == '}') {
            i++;
            return m;
        }
        while (true) {
            trang();
            if (i >= s.length() || s.charAt(i) != '"') {
                throw loi("can khoa");
            }
            final String k = chuoi();
            trang();
            if (i >= s.length() || s.charAt(i) != ':') {
                throw loi("can ':'");
            }
            i++;
            m.put(k, giaTri());
            trang();
            if (i < s.length() && s.charAt(i) == ',') {
                i++;
                continue;
            }
            if (i < s.length() && s.charAt(i) == '}') {
                i++;
                return m;
            }
            throw loi("can ',' hoac '}'");
        }
    }

    private List<Object> mang() {
        final List<Object> a = new ArrayList<Object>();
        i++;
        trang();
        if (i < s.length() && s.charAt(i) == ']') {
            i++;
            return a;
        }
        while (true) {
            a.add(giaTri());
            trang();
            if (i < s.length() && s.charAt(i) == ',') {
                i++;
                continue;
            }
            if (i < s.length() && s.charAt(i) == ']') {
                i++;
                return a;
            }
            throw loi("can ',' hoac ']'");
        }
    }

    private String chuoi() {
        final StringBuilder b = new StringBuilder();
        i++;
        while (i < s.length()) {
            final char c = s.charAt(i++);
            if (c == '"') {
                return b.toString();
            }
            if (c != '\\') {
                b.append(c);
                continue;
            }
            if (i >= s.length()) {
                break;
            }
            final char d = s.charAt(i++);
            switch (d) {
                case 'n':
                    b.append('\n');
                    break;
                case 't':
                    b.append('\t');
                    break;
                case 'r':
                    b.append('\r');
                    break;
                case 'b':
                    b.append('\b');
                    break;
                case 'f':
                    b.append('\f');
                    break;
                case 'u':
                    if (i + 4 > s.length()) {
                        throw loi("\\u thieu");
                    }
                    b.append((char) Integer.parseInt(s.substring(i, i + 4), 16));
                    i += 4;
                    break;
                default:
                    b.append(d);
            }
        }
        throw loi("chuoi khong dong");
    }

    private Object so() {
        final int dau = i;
        while (i < s.length() && "+-0123456789.eE".indexOf(s.charAt(i)) >= 0) {
            i++;
        }
        final String t = s.substring(dau, i);
        if (t.isEmpty()) {
            throw loi("gia tri la");
        }
        try {
            if (t.indexOf('.') < 0 && t.indexOf('e') < 0 && t.indexOf('E') < 0) {
                return Long.valueOf(t);
            }
            return Double.valueOf(t);
        } catch (final NumberFormatException e) {
            throw loi("so sai '" + t + "'");
        }
    }
}
