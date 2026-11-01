// Gia lap phan Unity ma P2PPeerova.cs can (UnityEngine.Debug, ThuongHieu) va lam dung viec Session_ME.doConnect:
// lay cong 127.0.0.1 qua P2P, mo TcpClient, gui goi -40 (xin khoa) nhu client that, doc tra loi may chu game.
namespace UnityEngine
{
    public static class Debug
    {
        public static void Log(object o) { System.Console.WriteLine(o); }
        public static void LogError(object o) { System.Console.WriteLine("ERR " + o); }
    }
}

public static class ThuongHieu
{
    public static string giaTriPhatHanh(string k) { return ""; }
}

public static class Program
{
    public static int Main(string[] a)
    {
        string ticket = a[0].StartsWith("@") ? System.IO.File.ReadAllText(a[0].Substring(1)).Trim() : a[0];
        int cong = int.Parse(a[1]);
        System.Environment.SetEnvironmentVariable("HTBB_P2P_TICKET", ticket);
        int loi = 0;
        System.Console.WriteLine("bat P2P: " + P2PPeerova.bat());
        for (int lan = 1; lan <= 2; lan++)
        {
            try
            {
                int p = P2PPeerova.congCucBo(cong);
                using (var c = new System.Net.Sockets.TcpClient("127.0.0.1", p))
                {
                    var s = c.GetStream();
                    s.Write(new byte[] { 0xD8, 0, 0 }, 0, 3); // lenh -40, do dai 0
                    var b = new byte[3];
                    int n = 0;
                    while (n < 3) { int r = s.Read(b, n, 3 - n); if (r <= 0) break; n += r; }
                    bool ok = n == 3 && (sbyte)b[0] == -40;
                    System.Console.WriteLine((ok ? "  OK   " : "  HONG ") + "lan " + lan + ": Unity P2PPeerova -> 127.0.0.1:" + p + ", may chu tra goi " + (sbyte)b[0]);
                    if (!ok) loi++;
                }
            }
            catch (System.Exception e)
            {
                System.Console.WriteLine("  HONG lan " + lan + ": " + e.Message);
                loi++;
            }
        }
        System.Console.WriteLine(loi == 0 ? "DAT KiemUnityP2P" : "HONG KiemUnityP2P");
        return loi == 0 ? 0 : 1;
    }
}
