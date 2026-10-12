using System;
using System.Collections;
using System.Threading;
using UnityEngine;

public class Main_HAIRMOD_PROTECTED : MonoBehaviour
{
	public static Main_HAIRMOD_PROTECTED main;

	public static TabType_HAIRMOD_PROTECTED tab = TabType_HAIRMOD_PROTECTED.tab2;

	public static Canvas_HAIRMOD_PROTECTED canvas;

	public static MyGraphics_HAIRMOD_PROTECTED g;

	public static GameMidlet_HAIRMOD_PROTECTED midlet;

	public static string res = "res";

	public static string mainThreadName;

	public static bool started;

	public static bool isDisConnect;

	public static bool isStartSaveIp;

	public static bool isPC;

	public static int hdtype;

	public int countUpdate;

	public int countPaint;

	private bool isStart;

	private Image_HAIRMOD_PROTECTED ii;

	private bool isBeginTouchDow0;

	private bool isBeginTouchDow1;

	private bool isBeginTouchRelease0;

	private bool isBeginTouchRelease1;

	private Vector2 lastMousePos = default(Vector2);

	private Vector2 lastMousePos1 = default(Vector2);

	public string s;

	private int yy;

	public static bool isCompactDevice = true;

	private void Awake()
	{
		if (main != null)
		{
            UnityEngine.Object.Destroy(base.gameObject);
		}
		else
		{
			UnityEngine.Object.DontDestroyOnLoad(base.gameObject);
		}
	}

	private IEnumerator DoStart()
	{
		GUI.depth = 2;
		while (true)
		{
			if (Time.timeScale == 1f)
			{
				yield return new WaitForSeconds(0.1f);
				Session_ME_HAIRMOD_PROTECTED.update();
			}
			yield return new WaitForSeconds(0.5f);
		}
	}

	private void Start()
	{
		Main.tab = TabType.tab2;
        if (Application.platform is RuntimePlatform.IPhonePlayer || Application.platform is RuntimePlatform.Android)
        {
            isPC = false;
        }
        else
        {
            isPC = true;
        }
        if (!isPC)
            Screen.SetResolution(800, 400, true);
        else
            Screen.SetResolution(1068, 600, false);
    }

	public void platformRequest(string url)
	{
		Out_HAIRMOD_PROTECTED.LogWarning("PLATFORM REQUEST: " + url);
		Application.OpenURL(url);
	}

	public void loadDataGame()
	{
		Res_HAIRMOD_PROTECTED.loadRes();
		FilePack_HAIRMOD_PROTECTED.init(FilePack_HAIRMOD_PROTECTED.arrow);
		for (int i = 0; i < 7; i++)
		{
			Res_HAIRMOD_PROTECTED.imgArrow[i] = FilePack_HAIRMOD_PROTECTED.getImg("arrow" + i);
		}
		FilePack_HAIRMOD_PROTECTED.reset();
		FilePack_HAIRMOD_PROTECTED.init(FilePack_HAIRMOD_PROTECTED.eff);
		for (int j = 0; j < Res_HAIRMOD_PROTECTED.imgEffect.Length; j++)
		{
			if (j < 25 || j > 33)
			{
				Res_HAIRMOD_PROTECTED.imgEffect[j] = FilePack_HAIRMOD_PROTECTED.getImg("g" + j);
			}
			if (Res_HAIRMOD_PROTECTED.imgEffect[26] == null)
			{
				Res_HAIRMOD_PROTECTED.imgEffect[25] = FilePack_HAIRMOD_PROTECTED.getImg("g25");
				Res_HAIRMOD_PROTECTED.imgEffect[26] = FilePack_HAIRMOD_PROTECTED.getImg("g26");
				Res_HAIRMOD_PROTECTED.imgEffect[27] = Image_HAIRMOD_PROTECTED.creatImageByImage(Res_HAIRMOD_PROTECTED.imgEffect[25]);
				Res_HAIRMOD_PROTECTED.imgEffect[28] = FilePack_HAIRMOD_PROTECTED.getImg("g27");
				Res_HAIRMOD_PROTECTED.imgEffect[29] = FilePack_HAIRMOD_PROTECTED.getImg("g28");
				Res_HAIRMOD_PROTECTED.imgEffect[30] = FilePack_HAIRMOD_PROTECTED.getImg("g29");
				Res_HAIRMOD_PROTECTED.imgEffect[31] = FilePack_HAIRMOD_PROTECTED.getImg("g30");
				Res_HAIRMOD_PROTECTED.imgEffect[32] = FilePack_HAIRMOD_PROTECTED.getImg("g31");
				Res_HAIRMOD_PROTECTED.imgEffect[33] = FilePack_HAIRMOD_PROTECTED.getImg("g32");
				Res_HAIRMOD_PROTECTED.backSmoke = new FrameImage_HAIRMOD_PROTECTED(Res_HAIRMOD_PROTECTED.imgEffect[32], 12, 13);
			}
		}
		FilePack_HAIRMOD_PROTECTED.reset();
		for (int k = 0; k < 5; k++)
		{
			for (int l = 0; l < 7; l++)
			{
				for (int m = 0; m < 7; m++)
				{
					Res_HAIRMOD_PROTECTED.getImgWeaPone(k, l, m);
				}
			}
		}
		for (int n = 0; n < 5; n++)
		{
			for (int num = 0; num < 3; num++)
			{
				Res_HAIRMOD_PROTECTED.GetWPSplashInfo(n, num);
			}
		}
	}

	private void OnGUI()
	{
		if (yy >= 10 && Main.tab == TabType.tab2)
		{
			checkInput();
			Session_ME_HAIRMOD_PROTECTED.update();
			if (Event.current.type.Equals(EventType.Repaint))
			{
				canvas.paint(g);
				countPaint++;
				g.reset();
			}
		}
	}

	private void checkInput()
	{
		if (Input.GetMouseButtonDown(0))
		{
			Canvas_HAIRMOD_PROTECTED.isPointerDrange = false;
			Vector3 mousePosition = Input.mousePosition;
			canvas.pointerPressed((int)ScaleGUI_HAIRMOD_PROTECTED.scaleX(mousePosition.x), (int)ScaleGUI_HAIRMOD_PROTECTED.scaleX((float)Screen.height - mousePosition.y));
			lastMousePos.x = mousePosition.x;
			lastMousePos.y = mousePosition.y;
		}
		if (Input.GetMouseButton(0))
		{
			Vector3 mousePosition2 = Input.mousePosition;
			if (mousePosition2.x < lastMousePos.x - 5f || mousePosition2.x > lastMousePos.x + 5f || mousePosition2.y < lastMousePos.y - 5f || mousePosition2.y > lastMousePos.y + 5f)
			{
				Canvas_HAIRMOD_PROTECTED.isPointerDrange = true;
			}
			if (Canvas_HAIRMOD_PROTECTED.isPointerDrange)
			{
				canvas.pointerDragged((int)ScaleGUI_HAIRMOD_PROTECTED.scaleX(mousePosition2.x), (int)ScaleGUI_HAIRMOD_PROTECTED.scaleX((float)Screen.height - mousePosition2.y));
				lastMousePos.x = mousePosition2.x;
				lastMousePos.y = mousePosition2.y;
			}
		}
		if (Input.GetMouseButtonUp(0))
		{
			Vector3 mousePosition3 = Input.mousePosition;
			canvas.pointerReleased((int)ScaleGUI_HAIRMOD_PROTECTED.scaleX(mousePosition3.x), (int)ScaleGUI_HAIRMOD_PROTECTED.scaleX((float)Screen.height - mousePosition3.y));
			lastMousePos.x = mousePosition3.x;
			lastMousePos.y = mousePosition3.y;
		}
		if (Event.current.type == EventType.KeyDown)
		{
			int num = MyKeyMap_HAIRMOD_PROTECTED.map(Event.current.keyCode);
			if (num != 0)
			{
				canvas.onKeyPressed(num);
			}
		}
		if (Event.current.type == EventType.KeyUp)
		{
			int num2 = MyKeyMap_HAIRMOD_PROTECTED.map(Event.current.keyCode);
			if (num2 != 0)
			{
				canvas.onKeyReleased(num2);
			}
		}
	}

	public void OnApplicationQuit()
	{
		exit();
	}

	private void FixedUpdate()
	{
		yy++;
		if (yy < 10)
		{
			return;
		}
		if (!started)
		{
			Screen.orientation = ScreenOrientation.LandscapeLeft;
			Application.runInBackground = true;
			Application.targetFrameRate = 60;
			base.useGUILayout = false;
			isCompactDevice = detectCompactDevice();
			if (main == null)
			{
				main = this;
			}
			started = true;
			ScaleGUI_HAIRMOD_PROTECTED.initScaleGUI();
			if (Thread.CurrentThread.Name != "Main")
			{
				Thread.CurrentThread.Name = "Main";
			}
			mainThreadName = Thread.CurrentThread.Name;
			if (isPC)
			{
				Screen.fullScreen = false;
			}
			loadDataGame();
			g = new MyGraphics_HAIRMOD_PROTECTED();
			midlet = new GameMidlet_HAIRMOD_PROTECTED();
			canvas = new Canvas_HAIRMOD_PROTECTED();
			canvas.start();
			midlet.load();
			Res_HAIRMOD_PROTECTED.load = new LoadData_HAIRMOD_PROTECTED();
			Res_HAIRMOD_PROTECTED.load.isInitGame = true;
			if (!isPC && Canvas_HAIRMOD_PROTECTED.loginScr.isRememPass)
			{
				ServerListScr_HAIRMOD_PROTECTED.loadIPAuTo();
				ServerListScr_HAIRMOD_PROTECTED.doLoginAuTo();
				Canvas_HAIRMOD_PROTECTED.loginScr.doLoginAuTo();
			}
		}
		countUpdate++;
		if (Res_HAIRMOD_PROTECTED.load != null)
		{
			Res_HAIRMOD_PROTECTED.load.run();
		}
		if (Main.tab == TabType.tab2)
		{
			canvas.update();
			RMS_HAIRMOD_PROTECTED.update();
			DataInputStream_HAIRMOD_PROTECTED.update();
			Image_HAIRMOD_PROTECTED.update();
			Canvas_HAIRMOD_PROTECTED.isPointerClick = false;
			ipKeyboard_HAIRMOD_PROTECTED.update();
			Net_HAIRMOD_PROTECTED.update();
		}
	}

	private void Update()
	{
	}

	public static void exit()
	{
        //Session_ME_HAIRMOD_PROTECTED.gI().close();
    }

	public static bool detectCompactDevice()
	{
		if (iPhoneSettings_HAIRMOD_PROTECTED.generation == iPhoneGeneration_HAIRMOD_PROTECTED.iPhone || iPhoneSettings_HAIRMOD_PROTECTED.generation == iPhoneGeneration_HAIRMOD_PROTECTED.iPhone3G || iPhoneSettings_HAIRMOD_PROTECTED.generation == iPhoneGeneration_HAIRMOD_PROTECTED.iPodTouch1Gen || iPhoneSettings_HAIRMOD_PROTECTED.generation == iPhoneGeneration_HAIRMOD_PROTECTED.iPodTouch2Gen)
		{
			return false;
		}
		return true;
	}

	public static bool checkCanSendSMS()
	{
		if (iPhoneSettings_HAIRMOD_PROTECTED.generation == iPhoneGeneration_HAIRMOD_PROTECTED.iPhone3GS || iPhoneSettings_HAIRMOD_PROTECTED.generation == iPhoneGeneration_HAIRMOD_PROTECTED.iPhone4 || iPhoneSettings_HAIRMOD_PROTECTED.generation > iPhoneGeneration_HAIRMOD_PROTECTED.iPodTouch4Gen)
		{
			return true;
		}
		return false;
	}
}
