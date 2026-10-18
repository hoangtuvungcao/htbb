package javax.microedition.rms;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;


/**
 * RecordStore lưu trong <thư mục dữ liệu người dùng>/rms/<tên>.rs (htbb.pc.CaiDat.thuMucDuLieu) (ghi cả tệp mỗi lần đổi, qua tệp tạm + đổi tên).
 * Định dạng: int idKeTiep, int soBanGhi, rồi từng (int id, int dai, byte[dai]); dai = -1 là null.
 */
public class RecordStore {
    public static final int AUTHMODE_PRIVATE = 0;
    public static final int AUTHMODE_ANY = 1;

    private static final Hashtable MO = new Hashtable();
    private final String ten;
    private final File tep;
    private final Vector ids = new Vector();
    private final Vector duLieu = new Vector();
    private int idKeTiep = 1;
    private int soMo;
    private long phienBan;
    private long lanDoi = System.currentTimeMillis();

    private RecordStore(String ten, File tep) {
        this.ten = ten;
        this.tep = tep;
    }

    private static File thuMuc() {
        File d = new File(htbb.pc.CaiDat.thuMucDuLieu(), "rms");
        if (!d.isDirectory()) d.mkdirs();
        return d;
    }

    private static String maHoa(String s) {
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_' || c == '-') b.append(c);
            else b.append('%').append(Integer.toHexString(c)).append(';');
        }
        return b.toString();
    }

    private static String giaiMa(String s) {
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '%') {
                int j = s.indexOf(';', i);
                b.append((char) Integer.parseInt(s.substring(i + 1, j), 16));
                i = j;
            } else {
                b.append(c);
            }
        }
        return b.toString();
    }

    public static synchronized RecordStore openRecordStore(String name, boolean create) throws RecordStoreException {
        if (name == null || name.length() == 0 || name.length() > 32) throw new IllegalArgumentException();
        RecordStore r = (RecordStore) MO.get(name);
        if (r == null) {
            File f = new File(thuMuc(), maHoa(name) + ".rs");
            if (!f.exists() && !create) throw new RecordStoreNotFoundException(name);
            r = new RecordStore(name, f);
            if (f.exists()) r.doc();
            else r.ghi();
            MO.put(name, r);
        }
        r.soMo++;
        return r;
    }

    public static RecordStore openRecordStore(String name, boolean create, int auth, boolean writable) throws RecordStoreException {
        return openRecordStore(name, create);
    }

    public static RecordStore openRecordStore(String name, String vendor, String suite) throws RecordStoreException {
        return openRecordStore(name, false);
    }

    public static synchronized String[] listRecordStores() {
        File[] fs = thuMuc().listFiles();
        if (fs == null) return null;
        Vector v = new Vector();
        for (int i = 0; i < fs.length; i++) {
            String n = fs[i].getName();
            if (n.endsWith(".rs")) v.addElement(giaiMa(n.substring(0, n.length() - 3)));
        }
        if (v.isEmpty()) return null;
        String[] a = new String[v.size()];
        v.copyInto(a);
        return a;
    }

    public static synchronized void deleteRecordStore(String name) throws RecordStoreException {
        RecordStore r = (RecordStore) MO.get(name);
        if (r != null && r.soMo > 0) throw new RecordStoreException("dang mo: " + name);
        MO.remove(name);
        File f = new File(thuMuc(), maHoa(name) + ".rs");
        if (!f.exists()) throw new RecordStoreNotFoundException(name);
        f.delete();
    }

    private void doc() throws RecordStoreException {
        DataInputStream in = null;
        try {
            in = new DataInputStream(new FileInputStream(tep));
            idKeTiep = in.readInt();
            int n = in.readInt();
            for (int i = 0; i < n; i++) {
                int id = in.readInt();
                int dai = in.readInt();
                byte[] d = null;
                if (dai >= 0) {
                    d = new byte[dai];
                    in.readFully(d);
                }
                ids.addElement(new Integer(id));
                duLieu.addElement(d);
            }
        } catch (IOException e) {
            ids.removeAllElements();
            duLieu.removeAllElements();
            idKeTiep = 1;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // bỏ qua
                }
            }
        }
    }

    private void ghi() throws RecordStoreException {
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            DataOutputStream o = new DataOutputStream(bo);
            o.writeInt(idKeTiep);
            o.writeInt(ids.size());
            for (int i = 0; i < ids.size(); i++) {
                o.writeInt(((Integer) ids.elementAt(i)).intValue());
                byte[] d = (byte[]) duLieu.elementAt(i);
                if (d == null) {
                    o.writeInt(-1);
                } else {
                    o.writeInt(d.length);
                    o.write(d);
                }
            }
            o.flush();
            File tam = new File(tep.getPath() + ".tam");
            FileOutputStream fo = new FileOutputStream(tam);
            try {
                fo.write(bo.toByteArray());
                fo.getFD().sync();
            } finally {
                fo.close();
            }
            if (!tam.renameTo(tep)) {
                tep.delete();
                tam.renameTo(tep);
            }
            phienBan++;
            lanDoi = System.currentTimeMillis();
        } catch (IOException e) {
            throw new RecordStoreFullException(e.toString());
        }
    }

    private void kiemMo() throws RecordStoreNotOpenException {
        if (soMo <= 0) throw new RecordStoreNotOpenException(ten);
    }

    private int viTri(int id) throws InvalidRecordIDException {
        for (int i = 0; i < ids.size(); i++) {
            if (((Integer) ids.elementAt(i)).intValue() == id) return i;
        }
        throw new InvalidRecordIDException("id " + id);
    }

    public void closeRecordStore() throws RecordStoreNotOpenException, RecordStoreException {
        synchronized (RecordStore.class) {
            kiemMo();
            soMo--;
            if (soMo == 0) MO.remove(ten);
        }
    }

    public synchronized String getName() throws RecordStoreNotOpenException {
        kiemMo();
        return ten;
    }

    public synchronized int getVersion() throws RecordStoreNotOpenException {
        kiemMo();
        return (int) phienBan;
    }

    public synchronized long getLastModified() throws RecordStoreNotOpenException {
        kiemMo();
        return lanDoi;
    }

    public synchronized int getNumRecords() throws RecordStoreNotOpenException {
        kiemMo();
        return ids.size();
    }

    public synchronized int getSize() throws RecordStoreNotOpenException {
        kiemMo();
        int n = 0;
        for (int i = 0; i < duLieu.size(); i++) {
            byte[] d = (byte[]) duLieu.elementAt(i);
            if (d != null) n += d.length;
        }
        return n;
    }

    public synchronized int getSizeAvailable() throws RecordStoreNotOpenException {
        kiemMo();
        return 4 * 1024 * 1024;
    }

    public synchronized int getNextRecordID() throws RecordStoreNotOpenException {
        kiemMo();
        return idKeTiep;
    }

    public synchronized int addRecord(byte[] data, int off, int len) throws RecordStoreException {
        kiemMo();
        byte[] d = null;
        if (data != null) {
            d = new byte[len];
            System.arraycopy(data, off, d, 0, len);
        }
        int id = idKeTiep++;
        ids.addElement(new Integer(id));
        duLieu.addElement(d);
        ghi();
        return id;
    }

    public synchronized void deleteRecord(int id) throws RecordStoreException {
        kiemMo();
        int i = viTri(id);
        ids.removeElementAt(i);
        duLieu.removeElementAt(i);
        ghi();
    }

    public synchronized byte[] getRecord(int id) throws RecordStoreException {
        kiemMo();
        byte[] d = (byte[]) duLieu.elementAt(viTri(id));
        if (d == null) return null;
        byte[] c = new byte[d.length];
        System.arraycopy(d, 0, c, 0, d.length);
        return c;
    }

    public synchronized int getRecord(int id, byte[] buf, int off) throws RecordStoreException {
        kiemMo();
        byte[] d = (byte[]) duLieu.elementAt(viTri(id));
        if (d == null) return 0;
        System.arraycopy(d, 0, buf, off, d.length);
        return d.length;
    }

    public synchronized int getRecordSize(int id) throws RecordStoreException {
        kiemMo();
        byte[] d = (byte[]) duLieu.elementAt(viTri(id));
        return d == null ? 0 : d.length;
    }

    public synchronized void setRecord(int id, byte[] data, int off, int len) throws RecordStoreException {
        kiemMo();
        int i = viTri(id);
        byte[] d = null;
        if (data != null) {
            d = new byte[len];
            System.arraycopy(data, off, d, 0, len);
        }
        duLieu.setElementAt(d, i);
        ghi();
    }

    public synchronized RecordEnumeration enumerateRecords(RecordFilter filter, RecordComparator comp, boolean keepUpdated) throws RecordStoreNotOpenException {
        kiemMo();
        final Vector v = new Vector();
        for (int i = 0; i < ids.size(); i++) {
            byte[] d = (byte[]) duLieu.elementAt(i);
            if (filter == null || filter.matches(d)) v.addElement(ids.elementAt(i));
        }
        final RecordStore rs = this;
        return new RecordEnumeration() {
            int i = 0;

            public int numRecords() {
                return v.size();
            }

            public byte[] nextRecord() throws RecordStoreException {
                return rs.getRecord(nextRecordId());
            }

            public int nextRecordId() throws InvalidRecordIDException {
                if (i >= v.size()) throw new InvalidRecordIDException();
                return ((Integer) v.elementAt(i++)).intValue();
            }

            public byte[] previousRecord() throws RecordStoreException {
                return rs.getRecord(previousRecordId());
            }

            public int previousRecordId() throws InvalidRecordIDException {
                if (i <= 0) throw new InvalidRecordIDException();
                return ((Integer) v.elementAt(--i)).intValue();
            }

            public boolean hasNextElement() {
                return i < v.size();
            }

            public boolean hasPreviousElement() {
                return i > 0;
            }

            public void reset() {
                i = 0;
            }

            public void rebuild() {
            }

            public void keepUpdated(boolean k) {
            }

            public boolean isKeptUpdated() {
                return false;
            }

            public void destroy() {
            }
        };
    }

    public void addRecordListener(RecordListener l) {
    }

    public void removeRecordListener(RecordListener l) {
    }

    public void setMode(int auth, boolean writable) {
    }
}
