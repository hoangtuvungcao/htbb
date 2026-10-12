using System;

public class Tree2014_HAIRMOD_PROTECTED : MyScreen_HAIRMOD_PROTECTED
{
	public class ObjTree
	{
		public int x;

		public int y;

		public int idIconImg;

		public sbyte status;

		public string name = string.Empty;

		public string info = string.Empty;

		public void paint(MyGraphics_HAIRMOD_PROTECTED g)
		{
			GameData_HAIRMOD_PROTECTED.paintIconTuBinh(g, (short)(idIconImg + 8200), x, y, 0);
		}
	}

	public static Tree2014_HAIRMOD_PROTECTED me;

	public MyScreen_HAIRMOD_PROTECTED last;

	public string name = string.Empty;

	private int[] xTrai;

	private int[] yTrai;

	private int index;

	private int xSh;

	private int ySh;

	private int wSh;

	private int hSh;

	private int wCell;

	private int hCell;

	private int xDr;

	private int yDr;

	private int wTree = 76;

	public Scroll_HAIRMOD_PROTECTED mScrollShowText = new Scroll_HAIRMOD_PROTECTED();

	public mVector_HAIRMOD_PROTECTED allTree = new mVector_HAIRMOD_PROTECTED();

	public string[] arrShowText = new string[1] { string.Empty };

	private Command_HAIRMOD_PROTECTED cmdView;

	private Command_HAIRMOD_PROTECTED cmdTuoi;

	private Command_HAIRMOD_PROTECTED cmdThuHoach;

	private Command_HAIRMOD_PROTECTED cmdClose;

	private Command_HAIRMOD_PROTECTED cmdMenu;

	private Command_HAIRMOD_PROTECTED cmdTangToc;

	private bool isShowText;

	private Command_HAIRMOD_PROTECTED cmdErr;

	public string str;

	public Tree2014_HAIRMOD_PROTECTED()
	{
		resetData();
		cmdClose = new Command_HAIRMOD_PROTECTED("Đóng");
		cmdClose.action = delegate
		{
			if (isShowText && !Canvas_HAIRMOD_PROTECTED.isSmartPhone)
			{
				isShowText = false;
				center = cmdView;
				left = cmdMenu;
				mScrollShowText.clear();
			}
			else
			{
				last.switchToMe();
			}
		};
		cmdThuHoach = new Command_HAIRMOD_PROTECTED("Thu hoạch");
		cmdThuHoach.action = delegate
		{
			doThuhoach();
		};
		cmdTangToc = new Command_HAIRMOD_PROTECTED("Tăng tốc");
		cmdTangToc.action = delegate
		{
			doTuoi();
		};
		cmdView = new Command_HAIRMOD_PROTECTED((!Canvas_HAIRMOD_PROTECTED.isSmartPhone) ? "Xem" : string.Empty);
		cmdView.action = delegate
		{
			doView();
		};
		cmdMenu = new Command_HAIRMOD_PROTECTED("Menu");
		cmdMenu.action = delegate
		{
			mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
			if (allTree.size() > 0)
			{
				ObjTree objTree = (ObjTree)allTree.elementAt(index);
				if (objTree.status == 0)
				{
					mVector_HAIRMOD_PROTECTED2.addElement(cmdTangToc);
				}
				else if (objTree.status == 2 || objTree.status == 3)
				{
					mVector_HAIRMOD_PROTECTED2.addElement(cmdThuHoach);
				}
				Canvas_HAIRMOD_PROTECTED.menu.startAt(mVector_HAIRMOD_PROTECTED2, 0);
			}
		};
		cmdErr = new Command_HAIRMOD_PROTECTED(string.Empty);
		cmdErr.action = delegate
		{
		};
		right = cmdClose;
		center = cmdView;
		left = cmdMenu;
		wCell = 300;
		hCell = 180;
		if (!Canvas_HAIRMOD_PROTECTED.isSmartPhone)
		{
			wCell = 180;
		}
		xDr = Canvas_HAIRMOD_PROTECTED.w / 2 - wCell / 2;
		yDr = Canvas_HAIRMOD_PROTECTED.h / 2 - hCell / 2;
		wSh = 140;
		if (Canvas_HAIRMOD_PROTECTED.isSmartPhone && wCell == 300)
		{
			xSh = xDr + wCell / 2;
			ySh = yDr + 25;
			wSh = wCell / 2 - 6;
			hSh = hCell - 36;
		}
	}

	public static Tree2014_HAIRMOD_PROTECTED gI()
	{
		return (me != null) ? me : (me = new Tree2014_HAIRMOD_PROTECTED());
	}

	protected void doView()
	{
		if (index >= 0 && index <= allTree.size() - 1)
		{
			GameService_HAIRMOD_PROTECTED.gI().sendInfoFruit(index, 1);
			ObjTree objTree = (ObjTree)allTree.elementAt(index);
		}
	}

	public void setShowText(string info)
	{
		int lineWidth = ((!Canvas_HAIRMOD_PROTECTED.isSmartPhone) ? 120 : (wSh - 16));
		string[] array = Util_HAIRMOD_PROTECTED.split(info, "|");
		int num = 0;
		int num2 = 0;
		for (int i = 0; i < array.Length; i++)
		{
			string[] array2 = MFont_HAIRMOD_PROTECTED.normalFont[0].splitFontBStrInLine(array[i], lineWidth);
			num += array2.Length;
		}
		string[] array3 = new string[num];
		for (int j = 0; j < array.Length; j++)
		{
			string[] array4 = MFont_HAIRMOD_PROTECTED.normalFont[0].splitFontBStrInLine(array[j], lineWidth);
			for (int k = 0; k < array4.Length; k++)
			{
				array3[num2] = array4[k];
				num2++;
			}
		}
		arrShowText = array3;
		if (!Canvas_HAIRMOD_PROTECTED.isSmartPhone)
		{
			if (arrShowText.Length == 1)
			{
				hSh = 35;
				ySh = Canvas_HAIRMOD_PROTECTED.h / 2 - MFont_HAIRMOD_PROTECTED.normalFont[0].getHeight() / 2;
			}
			else
			{
				hSh = arrShowText.Length * 24;
				ySh = Canvas_HAIRMOD_PROTECTED.h / 2 - arrShowText.Length * 24 / 2;
			}
			if (ySh < 5)
			{
				ySh = 5;
			}
			if (hSh > 150)
			{
				hSh = 150;
			}
			xSh = Canvas_HAIRMOD_PROTECTED.w / 2 - 75;
		}
		isShowText = true;
		if (!Canvas_HAIRMOD_PROTECTED.isSmartPhone)
		{
			center = cmdErr;
			left = cmdErr;
		}
	}

	protected void doTuoi()
	{
		GameService_HAIRMOD_PROTECTED.gI().sendInfoFruit(index, 2);
	}

	protected void doThuhoach()
	{
		GameService_HAIRMOD_PROTECTED.gI().sendInfoFruit(index, 3);
	}

	public void resetData()
	{
		xTrai = new int[8];
		yTrai = new int[8];
		int num = Canvas_HAIRMOD_PROTECTED.w / 2 - 75;
		int num2 = Canvas_HAIRMOD_PROTECTED.h / 2 - 75;
		xTrai[0] = 10;
		yTrai[0] = 72;
		xTrai[1] = 43;
		yTrai[1] = 62;
		xTrai[2] = 20;
		yTrai[2] = 28;
		xTrai[3] = 52;
		yTrai[3] = 17;
		xTrai[4] = 70;
		yTrai[4] = 56;
		xTrai[5] = 82;
		yTrai[5] = 7;
		xTrai[6] = 106;
		yTrai[6] = 22;
		xTrai[7] = 112;
		yTrai[7] = 64;
		num2 += 12;
		if (!Canvas_HAIRMOD_PROTECTED.isSmartPhone)
		{
			wTree = 0;
		}
		for (int i = 0; i < xTrai.Length; i++)
		{
			xTrai[i] += num - wTree;
			yTrai[i] += num2;
		}
		mScrollShowText.clear();
		allTree = new mVector_HAIRMOD_PROTECTED();
		index = 0;
		arrShowText = new string[1] { string.Empty };
	}

	public void setInfoTree(Message_HAIRMOD_PROTECTED msg)
	{
		try
		{
			switch (msg.reader().readByte())
			{
			case 0:
			{
				resetData();
				allTree = new mVector_HAIRMOD_PROTECTED();
				ObjTree objTree2 = null;
				name = msg.reader().readUTF();
				sbyte b2 = msg.reader().readByte();
				for (int i = 0; i < b2; i++)
				{
					string text = msg.reader().readUTF();
					sbyte idIconImg = msg.reader().readByte();
					sbyte status = msg.reader().readByte();
					objTree2 = new ObjTree();
					objTree2.idIconImg = idIconImg;
					objTree2.name = text;
					objTree2.status = status;
					objTree2.x = xTrai[i];
					objTree2.y = yTrai[i];
					allTree.addElement(objTree2);
				}
				break;
			}
			case 1:
			{
				sbyte b = msg.reader().readByte();
				ObjTree objTree = (ObjTree)allTree.elementAt(b);
				objTree.info = msg.reader().readUTF();
				setShowText(objTree.info);
				break;
			}
			}
		}
		catch (Exception)
		{
		}
	}

	public void paintCell(MyGraphics_HAIRMOD_PROTECTED g)
	{
		g.setColor(25695);
		g.fillRect(xSh - 5, ySh + 10, wSh, hSh - 10);
		g.setColor(16774720);
		g.drawRect(xSh - 5, ySh + 10, wSh, hSh - 10);
		if (!isShowText && str != null && Canvas_HAIRMOD_PROTECTED.isSmartPhone)
		{
			string[] array = Util_HAIRMOD_PROTECTED.split(str, "|");
			for (int i = 0; i < array.Length; i++)
			{
				MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, array[i], xSh, ySh + i * 14 + 14, 0);
			}
		}
	}

	public void paintShowText(MyGraphics_HAIRMOD_PROTECTED g)
	{
		Canvas_HAIRMOD_PROTECTED.resetTrans(g);
		if (wCell >= 300)
		{
			paintCell(g);
			if (!isShowText)
			{
				return;
			}
		}
		else
		{
			if (!isShowText)
			{
				return;
			}
			paintCell(g);
		}
		mScrollShowText.setStyle(arrShowText.Length, MyScreen_HAIRMOD_PROTECTED.ITEM_HEIGHT, xSh, ySh, wSh - 2, hSh - 10, true, 0);
		mScrollShowText.setClip(g, xSh - 3, ySh + 10, wSh - 2, hSh - 15);
		for (int i = 0; i < arrShowText.Length; i++)
		{
			MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, arrShowText[i], xSh, ySh + i * (MyScreen_HAIRMOD_PROTECTED.ITEM_HEIGHT - 3) + 14, 0);
		}
	}

	public void paintNameTree(MyGraphics_HAIRMOD_PROTECTED g, string clockInfo, int x, int y)
	{
		string[] array = Util_HAIRMOD_PROTECTED.split(clockInfo, "|");
		int width = MFont_HAIRMOD_PROTECTED.normalFont[0].getWidth(array[0]);
		int width2 = MFont_HAIRMOD_PROTECTED.normalFont[0].getWidth(array[1]);
		width = ((width >= width2) ? width : width2);
		g.setColor(1593912);
		g.fillRect(x + 16 - width / 2 - 3, y - 16, width + 5, 30);
		g.setColor(16772608);
		g.drawRect(x + 16 - width / 2 - 3, y - 16, width + 5, 30);
		MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, array[0], x + 16, y - 15, 2);
		MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, array[1], x + 16, y, 2);
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (last != null)
		{
			last.paint(g);
		}
		Res_HAIRMOD_PROTECTED.paintDlgFull(g, xDr, yDr, wCell, hCell, 0, name, false, 0);
		int num = ((!Canvas_HAIRMOD_PROTECTED.isSmartPhone) ? (wCell / 2) : wTree);
		GameData_HAIRMOD_PROTECTED.paintIconTuBinh(g, 8214, xDr + num, Canvas_HAIRMOD_PROTECTED.h / 2 + 12, MyGraphics_HAIRMOD_PROTECTED.VCENTER | MyGraphics_HAIRMOD_PROTECTED.HCENTER);
		str = null;
		int x = 0;
		int y = 0;
		for (int i = 0; i < allTree.size(); i++)
		{
			ObjTree objTree = (ObjTree)allTree.elementAt(i);
			if (objTree == null)
			{
				continue;
			}
			if (index == i)
			{
				g.setColor(25695);
				g.fillRect(objTree.x, objTree.y, 16, 30);
				g.setColor(16774720);
				g.drawRect(objTree.x, objTree.y, 16, 30);
				x = objTree.x;
				y = objTree.y;
				string text = string.Empty;
				if (objTree.status == 1)
				{
					text = "héo";
				}
				if (objTree.status == 0)
				{
					text = "xanh";
				}
				if (objTree.status == 2)
				{
					text = "chín";
				}
				str = objTree.name + "|Trạng thái: " + text;
			}
			objTree.paint(g);
		}
		paintShowText(g);
		if (str != null && !Canvas_HAIRMOD_PROTECTED.isSmartPhone && !isShowText)
		{
			paintNameTree(g, str, x, y);
		}
		base.paint(g);
	}

	public override void update()
	{
		if (last != null)
		{
			last.update();
		}
		if (Canvas_HAIRMOD_PROTECTED.currentDialog == null)
		{
			ScrollResult_HAIRMOD_PROTECTED scrollResult_HAIRMOD_PROTECTED = mScrollShowText.updateKey();
			mScrollShowText.updatecm();
		}
		base.update();
	}

	public void updateKeyShowText()
	{
		if (Canvas_HAIRMOD_PROTECTED.keyPressed[2])
		{
			Canvas_HAIRMOD_PROTECTED.keyPressed[2] = false;
			mScrollShowText.cmtoY -= 50;
			if (mScrollShowText.cmtoY < 0)
			{
				mScrollShowText.cmtoY = 0;
			}
		}
		else if (Canvas_HAIRMOD_PROTECTED.keyPressed[8])
		{
			Canvas_HAIRMOD_PROTECTED.keyPressed[8] = false;
			mScrollShowText.cmtoY += 50;
			if (mScrollShowText.cmtoY > mScrollShowText.cmyLim)
			{
				mScrollShowText.cmtoY = mScrollShowText.cmyLim;
			}
		}
	}

	public override void updateKey()
	{
		if (isShowText && !Canvas_HAIRMOD_PROTECTED.isSmartPhone)
		{
			updateKeyShowText();
		}
		else
		{
			if (Canvas_HAIRMOD_PROTECTED.keyPressed[4])
			{
				Canvas_HAIRMOD_PROTECTED.keyPressed[4] = false;
				index--;
				if (index < 0)
				{
					index = allTree.size() - 1;
				}
				if (Canvas_HAIRMOD_PROTECTED.isSmartPhone)
				{
					isShowText = false;
					mScrollShowText.clear();
				}
			}
			if (Canvas_HAIRMOD_PROTECTED.keyPressed[6])
			{
				Canvas_HAIRMOD_PROTECTED.keyPressed[6] = false;
				index++;
				if (index > allTree.size() - 1)
				{
					index = 0;
				}
				if (Canvas_HAIRMOD_PROTECTED.isSmartPhone)
				{
					isShowText = false;
					mScrollShowText.clear();
				}
			}
			if (Canvas_HAIRMOD_PROTECTED.isSmartPhone)
			{
				updateKeyShowText();
			}
		}
		pointer();
		base.updateKey();
	}

	public override void switchToMe()
	{
		base.switchToMe();
	}

	public override void switchToMe(MyScreen_HAIRMOD_PROTECTED lastSCR)
	{
		last = lastSCR;
		base.switchToMe(lastSCR);
	}

	public void pointer()
	{
		if ((!Canvas_HAIRMOD_PROTECTED.isSmartPhone && isShowText) || !Canvas_HAIRMOD_PROTECTED.isPointerClick)
		{
			return;
		}
		for (int i = 0; i < xTrai.Length; i++)
		{
			if (Canvas_HAIRMOD_PROTECTED.px >= xTrai[i] && Canvas_HAIRMOD_PROTECTED.px <= xTrai[i] + 15 && Canvas_HAIRMOD_PROTECTED.py >= yTrai[i] && Canvas_HAIRMOD_PROTECTED.py <= yTrai[i] + 30)
			{
				Canvas_HAIRMOD_PROTECTED.isPointerClick = false;
				isShowText = false;
				mScrollShowText.clear();
				index = i;
				if (!isShowText && center != null)
				{
					center.perform();
				}
				break;
			}
		}
	}
}
