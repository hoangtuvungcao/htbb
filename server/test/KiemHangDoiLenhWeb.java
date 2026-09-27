package data;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/** JDBC gia lap: khong ket noi hay thay doi CSDL game. */
public final class KiemHangDoiLenhWeb {
    private static int total = 125;
    private static boolean fail;

    private static Object proxy(Class<?> type, java.lang.reflect.InvocationHandler handler) {
        return Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
    }

    private static Connection connection() {
        return (Connection) proxy(Connection.class, (p, m, a) -> {
            if (!m.getName().equals("prepareStatement")) return null;
            String sql = (String) a[0];
            if (!sql.contains("AND id > ? ORDER BY id LIMIT 50"))
                throw new AssertionError("Query must page by ID and limit each batch");
            long[] after = {0};
            return proxy(PreparedStatement.class, (q, method, args) -> {
                if (method.getName().equals("setLong")) after[0] = (Long) args[1];
                if (!method.getName().equals("executeQuery")) return null;
                if (fail) throw new SQLException("simulated failure");
                long[] id = {after[0]};
                long end = Math.min(total, after[0] + 50);
                return proxy(ResultSet.class, (r, getter, values) -> {
                    switch (getter.getName()) {
                        case "next": return ++id[0] <= end;
                        case "getLong": return id[0];
                        case "getInt": return "trang_thai".equals(values[0])
                                ? (id[0] <= 50 ? LenhWeb.CHO_DANG_NHAP : LenhWeb.CHO) : 1;
                        case "getString": return "test";
                        default: return null;
                    }
                });
            });
        });
    }

    private static void batch(Connection c, int size, long first) throws Exception {
        List<Object[]> rows = LenhWeb.docLenhCho(c);
        if (rows.size() != size || (size > 0 && (Long) rows.get(0)[0] != first))
            throw new AssertionError("Unexpected batch: size=" + rows.size() + ", expected first=" + first);
    }

    public static void main(String[] args) throws Exception {
        Connection c = connection();
        batch(c, 50, 1); // 50 nhan vat offline van nam nguyen trong hang doi
        fail = true;
        try {
            LenhWeb.docLenhCho(c);
            throw new AssertionError("Expected SQLException");
        } catch (SQLException expected) { }
        fail = false;
        batch(c, 50, 51); // loi doc khong lam bo qua trang
        batch(c, 25, 101);
        batch(c, 50, 1); // quay lai nhan vat offline de kiem tra dang nhap
        total = 100;
        batch(c, 50, 51);
        batch(c, 0, 0); // so lenh chia het cho 50
        batch(c, 50, 1);
        total = 0;
        batch(c, 0, 0);
        total = 1;
        batch(c, 1, 1); // lenh moi sau khi hang doi rong
        System.out.println("PASS: queue pagination, offline retry, query failure, empty queue");
    }
}
