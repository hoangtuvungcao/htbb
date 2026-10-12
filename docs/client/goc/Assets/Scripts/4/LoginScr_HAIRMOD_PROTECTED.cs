using System;

public class LoginScr_HAIRMOD_PROTECTED : MyScreen_HAIRMOD_PROTECTED
{
	public TField_HAIRMOD_PROTECTED tfUser;

	public TField_HAIRMOD_PROTECTED tfPass;

	private int focus;

	private int xC;

	private int wC;

	private int yL;

	private int defYL;

	public bool isRememPass;

	private Command_HAIRMOD_PROTECTED cmdRemem;

	private Command_HAIRMOD_PROTECTED cmdLogin;

	public static string nameServer = string.Empty;

	public static string userAuto = string.Empty;

	public static string passAuTo = string.Empty;

	private ActionPerform_HAIRMOD_PROTECTED acRemen;

	private int xTo;

	private int yTo;

	private int centerX = 160;

	private int centerY = 160;

	private int radius = 128;

	private int goc;

	private int index;

	private int tick;

	public LoginScr_HAIRMOD_PROTECTED()
	{
		wC = 160;
		xC = (Canvas_HAIRMOD_PROTECTED.w - wC >> 1) + 45;
		tfUser = new TField_HAIRMOD_PROTECTED();
		tfUser.height = MyScreen_HAIRMOD_PROTECTED.ITEM_HEIGHT + 2;
		tfUser.x = xC;
		tfUser.y = Canvas_HAIRMOD_PROTECTED.h / 2 - 90;
		tfUser.width = wC;
		tfUser.setText(string.Empty);
		tfUser.isFocus = true;
		tfUser.showSubTextField = false;
		tfUser.setIputType(TField_HAIRMOD_PROTECTED.INPUT_ALPHA_NUMBER_ONLY);
		tfPass = new TField_HAIRMOD_PROTECTED();
		tfPass.y = Canvas_HAIRMOD_PROTECTED.h / 2 - 45;
		tfPass.height = MyScreen_HAIRMOD_PROTECTED.ITEM_HEIGHT + 2;
		tfPass.isFocus = false;
		tfPass.setText(string.Empty);
		tfPass.x = xC;
		tfPass.width = wC;
		tfPass.showSubTextField = false;
		tfPass.setIputType(TField_HAIRMOD_PROTECTED.INPUT_TYPE_PASSWORD);
		loadPass();
		cmdLogin = new Command_HAIRMOD_PROTECTED("Đăng Nhập");
		ActionPerform_HAIRMOD_PROTECTED action = delegate
		{
			doLogin();
		};
		cmdLogin.action = action;
		center = cmdLogin;
		cmdRemem = new Command_HAIRMOD_PROTECTED("Nhớ");
		ActionPerform_HAIRMOD_PROTECTED action2 = (acRemen = delegate
		{
			isRememPass = !isRememPass;
		});
		left = new Command_HAIRMOD_PROTECTED("Máy chủ");
		ActionPerform_HAIRMOD_PROTECTED action3 = delegate
		{
			ServerListScr_HAIRMOD_PROTECTED.me.switchToMe();
			Session_ME_HAIRMOD_PROTECTED.connected = false;
			Session_ME_HAIRMOD_PROTECTED.connecting = false;
		};
		left.action = action3;
		right = new Command_HAIRMOD_PROTECTED("Đăng ký");
		cmdRemem.action = action2;
		ActionPerform_HAIRMOD_PROTECTED action4 = delegate
		{
			doRegister();
		};
		right.action = action4;
		GameMidlet_HAIRMOD_PROTECTED.numberSupport = loadnumber();
	}

	public override void init()
	{
	}

	private string loadnumber()
	{
		string text = RMS_HAIRMOD_PROTECTED.loadRMSString("numbersupport");
		if (text == null)
		{
			return GameMidlet_HAIRMOD_PROTECTED.numberSupport;
		}
		return text;
	}

	public void doLogin()
	{
		string text = tfUser.getText().ToLower().Trim();
		string text2 = tfPass.getText();
		userAuto = string.Empty;
		passAuTo = string.Empty;
		if (!text.Equals(string.Empty))
		{
			if (text2.Equals(string.Empty))
			{
				focus = 1;
				tfUser.isFocus = false;
				tfPass.isFocus = true;
				return;
			}
			userAuto = text.ToLower();
			passAuTo = text2;
			Canvas_HAIRMOD_PROTECTED.startWaitDlg("Đang kết nối", true);
			Canvas_HAIRMOD_PROTECTED.connect();
			GameService_HAIRMOD_PROTECTED.gI().setClientType();
			GameService_HAIRMOD_PROTECTED.gI().login(text.ToLower(), text2, GameMidlet_HAIRMOD_PROTECTED.version);
			GameScr_HAIRMOD_PROTECTED.saveAutoFight = false;
			GameScr_HAIRMOD_PROTECTED.reSetQuest();
		}
	}

	public void reloadData()
	{
		tfUser.y = Canvas_HAIRMOD_PROTECTED.hh - 5;
		tfPass.y = Canvas_HAIRMOD_PROTECTED.hh + 33;
	}

	protected void doRegister()
	{
		string text = tfUser.getText().ToLower().Trim();
		string text2 = tfPass.getText();
		if (text.Equals(string.Empty))
		{
			Canvas_HAIRMOD_PROTECTED.startOKDlg("Vui lòng nhập Game ID muốn đăng ký vào ô trên.");
			return;
		}
		if (text2.Equals(string.Empty))
		{
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED("OK");
			ActionPerform_HAIRMOD_PROTECTED action = delegate
			{
				focus = 1;
				tfUser.isFocus = false;
				tfPass.isFocus = true;
				Canvas_HAIRMOD_PROTECTED.endDlg();
			};
			command_HAIRMOD_PROTECTED.action = action;
			Canvas_HAIRMOD_PROTECTED.msgdlg.setInfo("Bạn phải nhập password đăng ký.", null, command_HAIRMOD_PROTECTED, null);
			Canvas_HAIRMOD_PROTECTED.msgdlg.show();
			return;
		}
		Canvas_HAIRMOD_PROTECTED.msgdlg.isIcon = false;
		Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED2 = new Command_HAIRMOD_PROTECTED("Có");
		ActionPerform_HAIRMOD_PROTECTED action2 = delegate
		{
			doSendRegisterInfo();
		};
		command_HAIRMOD_PROTECTED2.action = action2;
		Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED3 = new Command_HAIRMOD_PROTECTED("Không");
		ActionPerform_HAIRMOD_PROTECTED action3 = delegate
		{
			Canvas_HAIRMOD_PROTECTED.endDlg();
		};
		command_HAIRMOD_PROTECTED3.action = action3;
		Canvas_HAIRMOD_PROTECTED.msgdlg.setInfo("Bạn có muốn đăng ký tài khoản: " + text + " không?", command_HAIRMOD_PROTECTED2, null, command_HAIRMOD_PROTECTED3);
		Canvas_HAIRMOD_PROTECTED.currentDialog = Canvas_HAIRMOD_PROTECTED.msgdlg;
	}

	protected void doSendRegisterInfo()
	{
		if (!Session_ME_HAIRMOD_PROTECTED.connected)
		{
			Canvas_HAIRMOD_PROTECTED.startWaitDlg("Vui lòng thoát game và chờ trong ít phút", true);
			Canvas_HAIRMOD_PROTECTED.connect();
		}
		else
		{
			Canvas_HAIRMOD_PROTECTED.startWaitDlg("Vui lòng thoát game và chờ trong ít phút", true);
		}
		GameService_HAIRMOD_PROTECTED.gI().requestRegister(tfUser.getText().ToLower(), tfPass.getText());
	}

	public override bool keyPress(int keyCode)
	{
		if (tfUser.isFocus)
		{
			if (tfUser.keyPressed(keyCode))
			{
				return true;
			}
		}
		else if (tfPass.isFocus && tfPass.keyPressed(keyCode))
		{
			return true;
		}
		return base.keyPress(keyCode);
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		ServerListScr_HAIRMOD_PROTECTED.gI().paintBg(g);
		Canvas_HAIRMOD_PROTECTED.resetTrans(g);
		MFont_HAIRMOD_PROTECTED.borderFont.drawString(g, nameServer, 4, 2, 0);
		paintPanel(g);
	}

	private void paintPanel(MyGraphics_HAIRMOD_PROTECTED g)
	{
		Res_HAIRMOD_PROTECTED.paintDlgDragonFull(g, Canvas_HAIRMOD_PROTECTED.w / 2 - 150, Canvas_HAIRMOD_PROTECTED.h / 2 - 120, 300, 190);
		MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, "Game ID:", tfUser.x - 85, tfUser.y + 5, 0);
		MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, "Password:", tfPass.x - 85, tfPass.y + 5, 0);
		tfUser.paint(g);
		g.setClip(0, 0, Canvas_HAIRMOD_PROTECTED.w, Canvas_HAIRMOD_PROTECTED.h);
		tfPass.paint(g);
		g.setClip(0, 0, Canvas_HAIRMOD_PROTECTED.w, Canvas_HAIRMOD_PROTECTED.h);
		g.drawRegion(Res_HAIRMOD_PROTECTED.imgCheck, 0, ((focus == 2) ? 1 : 0) * 11, 18, 11, 0, tfUser.x + 6, Canvas_HAIRMOD_PROTECTED.hh + 19, 27);
		if (isRememPass)
		{
			g.drawRegion(Res_HAIRMOD_PROTECTED.imgCheck, 0, 22, 18, 11, 0, tfUser.x + 6, Canvas_HAIRMOD_PROTECTED.hh + 19, 27);
		}
		MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, "Nhớ mật khẩu", tfUser.x + 10, Canvas_HAIRMOD_PROTECTED.h / 2 + 12, 0);
		MFont_HAIRMOD_PROTECTED.borderFont.drawString(g, "Hotline: " + GameMidlet_HAIRMOD_PROTECTED.numberSupport, Canvas_HAIRMOD_PROTECTED.hw, Canvas_HAIRMOD_PROTECTED.hh + 50, 2);
		base.paint(g);
	}

	public override void switchToMe()
	{
		base.switchToMe();
		init();
	}

	public override void update()
	{
		updatePoinTer();
		tfUser.update();
		tfPass.update();
		if (tick < 20)
		{
			tick++;
			if (tick == 20)
			{
				Canvas_HAIRMOD_PROTECTED.gameScr.tfChat.x = 2;
				Canvas_HAIRMOD_PROTECTED.gameScr.tfChat.y = Canvas_HAIRMOD_PROTECTED.h - 40;
			}
		}
		if (Canvas_HAIRMOD_PROTECTED.keyPressed[2])
		{
			index++;
			if (index >= 52)
			{
				index = 0;
			}
		}
		bool flag = false;
		if (Canvas_HAIRMOD_PROTECTED.isKeyPressed(2) || Canvas_HAIRMOD_PROTECTED.isKeyPressed(28))
		{
			focus--;
			if (focus < 0)
			{
				focus = 2;
			}
			flag = true;
		}
		else if (Canvas_HAIRMOD_PROTECTED.isKeyPressed(8) || Canvas_HAIRMOD_PROTECTED.isKeyPressed(28))
		{
			focus++;
			if (focus > 2)
			{
				focus = 0;
			}
			flag = true;
		}
		if (flag)
		{
			if (focus == 1)
			{
				tfUser.isFocus = false;
				tfPass.isFocus = true;
				center = cmdLogin;
			}
			else if (focus == 0)
			{
				tfUser.isFocus = true;
				tfPass.isFocus = false;
				center = cmdLogin;
			}
			else
			{
				tfUser.isFocus = false;
				tfPass.isFocus = false;
				center = cmdRemem;
			}
		}
		updateCamera();
		base.update();
	}

	public void updatePoinTer()
	{
		if (Canvas_HAIRMOD_PROTECTED.isPointerClick)
		{
			int num = 0;
			if (Canvas_HAIRMOD_PROTECTED.w > 200)
			{
				num = 10;
			}
			if (Canvas_HAIRMOD_PROTECTED.isPointer(xC - 30 + num, Canvas_HAIRMOD_PROTECTED.hh + 11, 100, 60))
			{
				isRememPass = !isRememPass;
			}
		}
	}

	public void updateCamera()
	{
		goc++;
		if (goc > 360)
		{
			goc = 0;
		}
		xTo = Util_HAIRMOD_PROTECTED.Cos(goc) * radius >> 10;
		yTo = Util_HAIRMOD_PROTECTED.Sin(goc) * radius >> 10;
		GameScr_HAIRMOD_PROTECTED.cmtoX = xTo + centerX;
		GameScr_HAIRMOD_PROTECTED.cmtoY = yTo + centerY;
		Canvas_HAIRMOD_PROTECTED.gameScr.updateCamera();
	}

	public void savePass()
	{
		DataOutputStream_HAIRMOD_PROTECTED dataOutputStream_HAIRMOD_PROTECTED = new DataOutputStream_HAIRMOD_PROTECTED();
		try
		{
			dataOutputStream_HAIRMOD_PROTECTED.writeBoolean(isRememPass);
			if (isRememPass)
			{
				dataOutputStream_HAIRMOD_PROTECTED.writeUTF(tfUser.getText());
				dataOutputStream_HAIRMOD_PROTECTED.writeUTF(tfPass.getText());
			}
			RMS_HAIRMOD_PROTECTED.saveRMS("nqshlogin", dataOutputStream_HAIRMOD_PROTECTED.toByteArray());
			dataOutputStream_HAIRMOD_PROTECTED.close();
		}
		catch (Exception ex)
		{
			Out_HAIRMOD_PROTECTED.println(ex.StackTrace);
		}
	}

	public void doLoginAuTo()
	{
		if (!ServerListScr_HAIRMOD_PROTECTED.addressAuTo.Equals(string.Empty) && !ServerListScr_HAIRMOD_PROTECTED.nameSvAuto.Equals(string.Empty) && ServerListScr_HAIRMOD_PROTECTED.portAuTo != -1)
		{
			string text = userAuto.ToLower().Trim();
			string text2 = passAuTo;
			Out_HAIRMOD_PROTECTED.println("user : " + text + "  pass " + text2);
			if (!text.Equals(string.Empty) && !text2.Equals(string.Empty))
			{
				Canvas_HAIRMOD_PROTECTED.startWaitDlg("Đang kết nối", true);
				Canvas_HAIRMOD_PROTECTED.connectAuTo();
				GameService_HAIRMOD_PROTECTED.gI().setClientType();
				GameService_HAIRMOD_PROTECTED.gI().login(text.ToLower(), text2, GameMidlet_HAIRMOD_PROTECTED.version);
			}
		}
	}

	private void loadPass()
	{
		sbyte[] array = RMS_HAIRMOD_PROTECTED.loadRMS("nqshlogin");
		if (array == null)
		{
			return;
		}
		DataInputStream_HAIRMOD_PROTECTED dataInputStream_HAIRMOD_PROTECTED = new DataInputStream_HAIRMOD_PROTECTED(array);
		try
		{
			isRememPass = dataInputStream_HAIRMOD_PROTECTED.readBoolean();
			if (isRememPass)
			{
				tfUser.setText(dataInputStream_HAIRMOD_PROTECTED.readUTF());
				tfUser.setOffset();
				tfPass.setText(dataInputStream_HAIRMOD_PROTECTED.readUTF());
				tfPass.setOffset();
				userAuto = tfUser.getText();
				passAuTo = tfPass.getText();
			}
			dataInputStream_HAIRMOD_PROTECTED.close();
		}
		catch (Exception ex)
		{
			Out_HAIRMOD_PROTECTED.println(ex.StackTrace);
		}
	}
}
