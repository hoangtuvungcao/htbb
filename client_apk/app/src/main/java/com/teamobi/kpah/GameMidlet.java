package com.teamobi.kpah;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import game.core.j2me.Image;
import game.model.IAction;
import game.model.Tilemap;
import game.networklogic.GameService;
import game.networklogic.MessageHandler;
import game.render.GCanvas;
import game.render.Res;
import game.render.screen.NqshMod;
import game.render.screen.RMS;
import game.render.screen.ServerListScr;
import game.render.screen.TField;
import game.render.screen.WindowInfoScr;
import game.render.screen.myEditText;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Vector;
import network.Session_ME;

public class GameMidlet extends Activity implements Runnable {
    public static String IP = "210.245.115.227";
    public static int PORT = 19129;
    public static byte VERSION = 1;
    public static String agent = "-1";
    public static String client_Provider = "0";
    public static myEditText currentEditText = null;
    public static GCanvas gamecanvas = null;
    static HttpURLConnection httpConn = null;
    public static GameMidlet instance = null;
    public static boolean isChaCha = false;
    public static boolean isKPAH = true;
    public static boolean isLoadAgentPro = false;
    public static RelativeLayout mainView = null;
    public static String numberSupport = "19001530";
    public static String provider = "-1";
    public static Rect safeRec = null;
    public static ScrollView scrollView = null;
    public static String str = null;
    public static RelativeLayout subview = null;
    public static String url = "";
    public static String version = "2.7.7";
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            GameMidlet.this.updateGUIState();
        }
    };
    public static Vector<TField> allField = new Vector<>();
    public static boolean issend = true;
    public static int nextID = 0;
    public static String Model = Build.MODEL;
    public static AssetManager asset = null;
    public static int coutVerSion = 74;
    public static String versionName = "1.8.7";
    public static boolean IS_ANDROID_9 = false;
    static Boolean isSignalEnable = true;
    public static boolean isaction = true;
    public static int action = 0;

    @Override
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        return true;
    }

    @Override
    public boolean onKeyUp(int i, KeyEvent keyEvent) {
        return true;
    }

    @Override
    public void run() {
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        requestWindowFeature(1);
        getWindow().setFlags(1024, 1024);
        instance = this;
        asset = getAssets();
        gamecanvas = new GCanvas(this);
        Res.load.isInitGame = true;
        RelativeLayout relativeLayout = new RelativeLayout(this);
        mainView = relativeLayout;
        relativeLayout.addView(gamecanvas);
        scrollView = new ScrollView(this);
        RelativeLayout relativeLayout2 = new RelativeLayout(this);
        subview = relativeLayout2;
        scrollView.addView(relativeLayout2);
        mainView.addView(scrollView);
        setContentView(mainView);
        mainView.setFocusable(true);
        mainView.setFocusableInTouchMode(true);
        scrollView.setFocusable(true);
        scrollView.setFocusableInTouchMode(true);
        subview.setFocusable(true);
        subview.setFocusableInTouchMode(true);
        ((KeyguardManager) getSystemService("keyguard")).newKeyguardLock("keyguard").disableKeyguard();
        getDeviceSignal();
        ServerListScr.loadSv(isKPAH);
        IS_ANDROID_9 = true;
        hideSystemUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onWindowFocusChanged(boolean z) {
        super.onWindowFocusChanged(z);
        if (z) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(5894);
    }

    public void load() {
        gamecanvas.init();
        gamecanvas.getScreenSize();
        Res.loadTreeImage();
        Res.loadMonster();
        Res.loadPart2();
        Session_ME.gI().setHandler(MessageHandler.gI());
        GameService.gI().setSession(Session_ME.gI());
        Res.resetImgMonsTemp();
        Tilemap.loadMap(0, null);
        ServerListScr.gI().switchToMe();
        boolean z = isKPAH;
        if (z) {
            client_Provider = "0";
            provider = "-1";
            agent = "-1";
        } else {
            client_Provider = "1";
            provider = "1";
            agent = "3";
            isLoadAgentPro = false;
        }
        if (isLoadAgentPro && z) {
            String strLoadRMSString = RMS.loadRMSString("agent");
            agent = strLoadRMSString;
            if (strLoadRMSString == null) {
                agent = "-1";
            }
            String strLoadRMSString2 = RMS.loadRMSString("provider");
            provider = strLoadRMSString2;
            if (strLoadRMSString2 == null) {
                provider = "-1";
            }
        } else {
            agent = "0";
            provider = "0";
        }
        try {
            Thread.sleep(600L);
        } catch (Exception unused) {
        }
        GCanvas.isLoad = false;
        if (!isKPAH) {
            url = "http://ngude.go.vn/";
        }
        System.out.println(version + " thong so: " + (isKPAH ? "KPAH " : "NGU DE ") + (isLoadAgentPro ? "Ban trang " : "Ban thuong: ") + "client_Provider= " + client_Provider + " agent =: " + agent + " pro =:" + provider + (VERSION == 1 ? " BAN THUONG" : " GOOGLE"));
    }

    public void getDeviceSignal() {
        int simState = ((TelephonyManager) getSystemService("phone")).getSimState();
        if (simState == 0 || simState == 1 || simState == 2 || simState == 3 || simState == 4) {
            isSignalEnable = false;
        } else {
            if (simState != 5) {
                return;
            }
            isSignalEnable = true;
        }
    }

    private String loadDataInFile(String str2) {
        InputStream inputStreamOpenRawResource = GCanvas.res.openRawResource(Image.getDrawableID(str2));
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            for (int i = inputStreamOpenRawResource.read(); i != -1; i = inputStreamOpenRawResource.read()) {
                byteArrayOutputStream.write(i);
            }
            inputStreamOpenRawResource.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String string = byteArrayOutputStream.toString();
        str = string;
        if (string != null) {
            return string;
        }
        str = "0";
        return "0";
    }

    @Override
    protected void onStart() {
        GCanvas.endDlg();
        super.onStart();
    }

    @Override
    protected void onResume() {
        GCanvas.endDlg();
        super.onResume();
    }

    @Override
    protected void onRestart() {
        GCanvas.endDlg();
        super.onRestart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        GCanvas.gCanvas.finish();
        Session_ME.instance.close();
        WindowInfoScr.isWindowInfo = false;
        super.onDestroy();
    }

    public static void destroy() {
        GCanvas.gCanvas.finish();
        Session_ME.instance.close();
        WindowInfoScr.isWindowInfo = false;
    }

    public void notifyDestroyed() {
        GCanvas.gCanvas.finish();
        WindowInfoScr.isWindowInfo = false;
        Session_ME.instance.close();
        Process.killProcess(Process.myPid());
    }

    public static String connectHTTP(String str2) {
        try {
            InputStream inputStreamOpenHttpConnection = openHttpConnection(str2);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] bArr = new byte[4096];
            while (true) {
                int i = inputStreamOpenHttpConnection.read(bArr);
                if (i == -1) {
                    break;
                }
                byteArrayOutputStream.write(bArr, 0, i);
            }
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            if (inputStreamOpenHttpConnection != null) {
                inputStreamOpenHttpConnection.close();
                httpConn.disconnect();
            }
            return new String(byteArray);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void sendSMS(String str2, String str3, IAction iAction, IAction iAction2) {
        GCanvas.endDlg();
    }

    public void platformRequest(String str2) {
        // mod: link trỏ thẳng IP -> tên miền
        str2 = NqshMod.suaLink(str2);
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setData(Uri.parse(str2));
        GCanvas.gCanvas.startActivity(intent);
    }

    public static void requestLink(String str2) {
        try {
            instance.platformRequest(str2);
            destroy();
        } catch (Exception unused) {
        }
    }

    private static InputStream openHttpConnection(String str2) {
        try {
            URLConnection uRLConnectionOpenConnection = new URL(str2).openConnection();
            if (!(uRLConnectionOpenConnection instanceof HttpURLConnection)) {
                throw new IOException("URL is not an Http URL");
            }
            HttpURLConnection httpURLConnection = (HttpURLConnection) uRLConnectionOpenConnection;
            httpConn = httpURLConnection;
            httpURLConnection.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("GET");
            httpConn.connect();
            if (httpConn.getResponseCode() == 200) {
                return httpConn.getInputStream();
            }
            return null;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e2) {
            e2.printStackTrace();
            return null;
        }
    }

    public void update(int i, myEditText myedittext) {
        action = i;
        currentEditText = myedittext;
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                GameMidlet.this.handler.sendEmptyMessage(0);
            }
        }.start();
    }

    public void removeEditText() {
        if (currentEditText != null) {
            action = 2;
            new Thread() {
                @Override
                public void run() {
                    Looper.prepare();
                    GameMidlet.this.handler.sendEmptyMessage(0);
                }
            }.start();
        }
    }

    public static int getID() {
        int i = nextID + 1;
        nextID = i;
        return i;
    }

    public void updateGUIState() {
        int i = action;
        if (i != 0) {
            if (i != 1) {
                if (i != 2) {
                    return;
                }
                subview.removeView(currentEditText);
                action = 0;
                currentEditText = null;
                return;
            }
            subview.addView(currentEditText);
            action = 0;
            currentEditText.requestFocus();
            currentEditText.setFocusable(true);
            currentEditText.setFocusableInTouchMode(true);
            ((InputMethodManager) instance.getSystemService("input_method")).showSoftInput(currentEditText, 0);
        }
    }
}
