using System;

public class TuBinhScr_HAIRMOD_PROTECTED : MyScreen_HAIRMOD_PROTECTED
{
	public class ObjTuBinh
	{
		public int x;

		public int y;

		public string name = string.Empty;

		public short idImg;

		public string[] arrayInfo = new string[1] { string.Empty };

		public string infoDetail = string.Empty;

		public void paint(MyGraphics_HAIRMOD_PROTECTED g)
		{
			int num = 0;
			g.setColor(8023616);
			g.fillRect(x, y + hImg + 1, wCell, 1);
			ImageIcon_HAIRMOD_PROTECTED imgIcon = GameData_HAIRMOD_PROTECTED.getImgIcon((short)(idImg + 8500));
			if (imgIcon != null && imgIcon.img != null)
			{
				num = imgIcon.img.getWidth() + 2;
			}
			GameData_HAIRMOD_PROTECTED.paintIconTuBinh(g, (short)(idImg + 8500), x, y, 0);
			for (int i = 0; i < arrayInfo.Length; i++)
			{
				MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, arrayInfo[i], x + num, y + i * (MyScreen_HAIRMOD_PROTECTED.ITEM_HEIGHT - 3) - 2, 0);
			}
		}
	}

	public static TuBinhScr_HAIRMOD_PROTECTED me;

	public static int hImg = 80;

	public static int wImg = 40;

	public static int wCell;

	public static int hCell;

	public static int xDr;

	public static int yDr;

	public static mVector_HAIRMOD_PROTECTED allTuBinh = new mVector_HAIRMOD_PROTECTED();

	public Scroll_HAIRMOD_PROTECTED mScroll = new Scroll_HAIRMOD_PROTECTED();

	public Scroll_HAIRMOD_PROTECTED mScrollTab = new Scroll_HAIRMOD_PROTECTED();

	public Scroll_HAIRMOD_PROTECTED mScrollShowText = new Scroll_HAIRMOD_PROTECTED();

	public int index = -1;

	public int xSh;

	public int ySh;

	public int wSh;

	public int hSh;

	public int wTab = 80;

	public int indexTab;

	public int saveIndexTab;

	public MyScreen_HAIRMOD_PROTECTED last;

	public string[] arrShowText = new string[1] { string.Empty };

	public string[] arraySubTile = new string[4] { "Tab 1", "Tab 2", "Tab 3", "Tab 4" };

	public string tile = "Cổ vật";

	public bool isShowText;

	private Command_HAIRMOD_PROTECTED cmdClose;

	private Command_HAIRMOD_PROTECTED cmdErr;

	private Command_HAIRMOD_PROTECTED cmdView;

	public TuBinhScr_HAIRMOD_PROTECTED()
	{
		wCell = Canvas_HAIRMOD_PROTECTED.w - 10;
		hCell = Canvas_HAIRMOD_PROTECTED.h - MyScreen_HAIRMOD_PROTECTED.ITEM_HEIGHT - 10;
		if (wCell > 176)
		{
			wCell = 176;
		}
		if (hCell > 200)
		{
			hCell = 200;
		}
		if (Canvas_HAIRMOD_PROTECTED.w >= 320 && Canvas_HAIRMOD_PROTECTED.isSmartPhone)
		{
			wCell = 300;
		}
		xDr = Canvas_HAIRMOD_PROTECTED.w / 2 - wCell / 2;
		yDr = (Canvas_HAIRMOD_PROTECTED.h - MyScreen_HAIRMOD_PROTECTED.ITEM_HEIGHT) / 2 - hCell / 2 - 5;
		if (yDr < 5)
		{
			yDr = 5;
		}
		if (xDr < 5)
		{
			xDr = 5;
		}
		wSh = wCell;
		if (Canvas_HAIRMOD_PROTECTED.isSmartPhone && wCell == 300)
		{
			xSh = xDr + wCell / 2;
			ySh = yDr + 55;
			wSh = wCell / 2 - 8;
			hSh = hCell - 54;
		}
		Out_HAIRMOD_PROTECTED.println("H cell = " + hCell + " Hcanvas = " + Canvas_HAIRMOD_PROTECTED.h);
		cmdClose = new Command_HAIRMOD_PROTECTED("Đóng");
		cmdClose.action = delegate
		{
			if (isShowText)
			{
				isShowText = false;
				center = cmdView;
				mScrollShowText.clear();
				arrShowText = new string[1] { string.Empty };
			}
			else
			{
				last.switchToMe();
			}
		};
		cmdView = new Command_HAIRMOD_PROTECTED((!Canvas_HAIRMOD_PROTECTED.isSmartPhone) ? "Xem" : string.Empty);
		cmdView.action = delegate
		{
			doView();
		};
		cmdErr = new Command_HAIRMOD_PROTECTED(string.Empty);
		cmdErr.action = delegate
		{
		};
		right = cmdClose;
		center = cmdView;
		if (!Canvas_HAIRMOD_PROTECTED.isSmartPhone)
		{
			index = 0;
		}
		setData(null);
	}

	public static TuBinhScr_HAIRMOD_PROTECTED gI()
	{
		return (me != null) ? me : (me = new TuBinhScr_HAIRMOD_PROTECTED());
	}

	public void setData(Message_HAIRMOD_PROTECTED msg)
	{
		allTuBinh = new mVector_HAIRMOD_PROTECTED();
		ObjTuBinh objTuBinh = null;
		int x = xDr + 6;
		int num = yDr + 55;
		try
		{
			sbyte b = msg.reader().readByte();
			arraySubTile = new string[b];
			for (int i = 0; i < b; i++)
			{
				arraySubTile[i] = msg.reader().readUTF();
				sbyte b2 = msg.reader().readByte();
				mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
				for (int j = 0; j < b2; j++)
				{
					objTuBinh = new ObjTuBinh();
					objTuBinh.name = msg.reader().readUTF();
					objTuBinh.infoDetail = objTuBinh.name + "|" + msg.reader().readUTF();
					string[] array = Util_HAIRMOD_PROTECTED.split(msg.reader().readUTF(), "|");
					objTuBinh.arrayInfo = new string[array.Length + 1];
					objTuBinh.arrayInfo[0] = objTuBinh.name;
					for (int k = 0; k < array.Length; k++)
					{
						objTuBinh.arrayInfo[k + 1] = array[k];
					}
					objTuBinh.idImg = msg.reader().readByte();
					objTuBinh.x = x;
					objTuBinh.y = num + j * (hImg + 3);
					mVector_HAIRMOD_PROTECTED2.addElement(objTuBinh);
				}
				allTuBinh.addElement(mVector_HAIRMOD_PROTECTED2);
			}
		}
		catch (Exception)
		{
		}
	}

	public void resetData()
	{
		mScroll.clear();
		mScrollTab.clear();
		mScrollShowText.clear();
		allTuBinh = new mVector_HAIRMOD_PROTECTED();
		index = (Canvas_HAIRMOD_PROTECTED.isSmartPhone ? (-1) : 0);
		indexTab = 0;
	}

	protected void doView()
	{
		if (allTuBinh.size() == 0 || saveIndexTab <= -1)
		{
			return;
		}
		mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = (mVector_HAIRMOD_PROTECTED)allTuBinh.elementAt(saveIndexTab);
		if (index < 0 || index > mVector_HAIRMOD_PROTECTED2.size() - 1)
		{
			return;
		}
		ObjTuBinh objTuBinh = (ObjTuBinh)mVector_HAIRMOD_PROTECTED2.elementAt(index);
		arrShowText = Util_HAIRMOD_PROTECTED.split(objTuBinh.infoDetail, "|");
		if (!Canvas_HAIRMOD_PROTECTED.isSmartPhone)
		{
			if (arrShowText.Length == 1)
			{
				hSh = 35;
				ySh = Canvas_HAIRMOD_PROTECTED.h / 2 - MFont_HAIRMOD_PROTECTED.normalFont[0].getHeight() / 2;
			}
			else
			{
				hSh = arrShowText.Length * (MyScreen_HAIRMOD_PROTECTED.ITEM_HEIGHT - 3) + 5;
				ySh = Canvas_HAIRMOD_PROTECTED.h / 2 - arrShowText.Length * (MyScreen_HAIRMOD_PROTECTED.ITEM_HEIGHT - 3) / 2;
			}
			if (ySh < yDr)
			{
				ySh = yDr;
			}
			if (hSh > 150)
			{
				hSh = 150;
			}
			xSh = xDr;
		}
		isShowText = true;
		if (!Canvas_HAIRMOD_PROTECTED.isSmartPhone)
		{
			center = cmdErr;
		}
	}

	public void paintCell(MyGraphics_HAIRMOD_PROTECTED g)
	{
		g.setColor(25695);
		g.fillRect(xSh, ySh + ((!Canvas_HAIRMOD_PROTECTED.isSmartPhone) ? 10 : 0), wSh, hSh - 10);
		g.setColor(16774720);
		g.drawRect(xSh, ySh + ((!Canvas_HAIRMOD_PROTECTED.isSmartPhone) ? 10 : 0), wSh, hSh - 10);
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
		mScrollShowText.setClip(g, xSh, ySh + ((!Canvas_HAIRMOD_PROTECTED.isSmartPhone) ? 10 : 2), wSh - 2, hSh - 15);
		for (int i = 0; i < arrShowText.Length; i++)
		{
			MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, arrShowText[i], xSh + 4, ySh + i * (MyScreen_HAIRMOD_PROTECTED.ITEM_HEIGHT - 3) + ((!Canvas_HAIRMOD_PROTECTED.isSmartPhone) ? 14 : 6), 0);
		}
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (last != null)
		{
			last.paint(g);
		}
		g.setColor(0, 0.5f);
		g.fillRect(0, 0, Canvas_HAIRMOD_PROTECTED.w, Canvas_HAIRMOD_PROTECTED.h);
		int num = ((!Canvas_HAIRMOD_PROTECTED.isSmartPhone || wCell != 300) ? wCell : (wCell / 2));
		Res_HAIRMOD_PROTECTED.paintDlgFull(g, xDr, yDr, wCell, hCell, 25, tile, false, 0);
		Canvas_HAIRMOD_PROTECTED.resetTrans(g);
		mScrollTab.setStyle(arraySubTile.Length, wTab + 2, xDr, yDr + 22, wCell - 2, 25, false, 0);
		mScrollTab.setClip(g, xDr + 2, yDr + 25, wCell - 4, 25);
		for (int i = 0; i < arraySubTile.Length; i++)
		{
			if (indexTab == i)
			{
				g.setColor(6513507);
				g.fillRect(xDr + wTab * i, yDr + 26, wTab, 25);
			}
			MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, arraySubTile[i], xDr + wTab * i + wTab / 2, yDr + 30, 2);
			g.setColor(14527502);
			g.fillRect(xDr + wTab * i, yDr + 25, 1, 25);
		}
		g.fillRect(xDr + wTab * arraySubTile.Length, yDr + 25, 1, 25);
		Canvas_HAIRMOD_PROTECTED.resetTrans(g);
		if (allTuBinh.size() > 0)
		{
			if (saveIndexTab != indexTab && indexTab > -1 && indexTab < allTuBinh.size())
			{
				saveIndexTab = indexTab;
			}
			mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = (mVector_HAIRMOD_PROTECTED)allTuBinh.elementAt(saveIndexTab);
			mScroll.setStyle(mVector_HAIRMOD_PROTECTED2.size(), hImg + 2, xDr, yDr + 55, num - 2, hCell - 65, true, 0);
			mScroll.setClip(g, xDr, yDr + 52, num - 2, hCell - 55);
			for (int j = 0; j < mVector_HAIRMOD_PROTECTED2.size(); j++)
			{
				ObjTuBinh objTuBinh = (ObjTuBinh)mVector_HAIRMOD_PROTECTED2.elementAt(j);
				if (objTuBinh != null)
				{
					if (index == j)
					{
						g.setColor(6513507);
						g.fillRect(xDr + 4, objTuBinh.y, num, hImg);
					}
					objTuBinh.paint(g);
				}
			}
		}
		paintShowText(g);
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
			if (Canvas_HAIRMOD_PROTECTED.isSmartPhone)
			{
				ScrollResult_HAIRMOD_PROTECTED scrollResult_HAIRMOD_PROTECTED = mScroll.updateKey();
				if (scrollResult_HAIRMOD_PROTECTED.isDowning || scrollResult_HAIRMOD_PROTECTED.isFinish)
				{
					if (isShowText)
					{
						right.perform();
					}
					index = scrollResult_HAIRMOD_PROTECTED.selected;
					mScroll.moveTo(index * (hImg + 2));
					if (!mScroll.pointerIsDowning && !isShowText && center != null)
					{
						center.perform();
					}
				}
				mScroll.updatecm();
				ScrollResult_HAIRMOD_PROTECTED scrollResult_HAIRMOD_PROTECTED2 = mScrollTab.updateKey();
				if (scrollResult_HAIRMOD_PROTECTED2.isDowning || scrollResult_HAIRMOD_PROTECTED2.isFinish)
				{
					indexTab = scrollResult_HAIRMOD_PROTECTED2.selected;
					mScrollTab.moveTo(indexTab * (wTab + 2));
					if (right != null && isShowText)
					{
						right.perform();
					}
				}
				mScrollTab.updatecm();
				ScrollResult_HAIRMOD_PROTECTED scrollResult_HAIRMOD_PROTECTED3 = mScrollShowText.updateKey();
				mScrollShowText.updatecm();
			}
			else if (!isShowText)
			{
				ScrollResult_HAIRMOD_PROTECTED scrollResult_HAIRMOD_PROTECTED4 = mScroll.updateKey();
				if (scrollResult_HAIRMOD_PROTECTED4.isDowning || scrollResult_HAIRMOD_PROTECTED4.isFinish)
				{
					index = scrollResult_HAIRMOD_PROTECTED4.selected;
				}
				mScroll.updatecm();
				ScrollResult_HAIRMOD_PROTECTED scrollResult_HAIRMOD_PROTECTED5 = mScrollTab.updateKey();
				if (scrollResult_HAIRMOD_PROTECTED5.isDowning || scrollResult_HAIRMOD_PROTECTED5.isFinish)
				{
					indexTab = scrollResult_HAIRMOD_PROTECTED5.selected;
					mScrollTab.moveTo(indexTab * (wTab + 2));
				}
				mScrollTab.updatecm();
			}
			else
			{
				ScrollResult_HAIRMOD_PROTECTED scrollResult_HAIRMOD_PROTECTED6 = mScrollShowText.updateKey();
				mScrollShowText.updatecm();
			}
		}
		base.update();
	}

	public override void updateKey()
	{
		if (isShowText)
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
		else
		{
			mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = null;
			if (indexTab > -1 && indexTab < allTuBinh.size())
			{
				mVector_HAIRMOD_PROTECTED2 = (mVector_HAIRMOD_PROTECTED)allTuBinh.elementAt(indexTab);
				if (Canvas_HAIRMOD_PROTECTED.keyPressed[2])
				{
					Canvas_HAIRMOD_PROTECTED.keyPressed[2] = false;
					index--;
					if (index < 0)
					{
						index = mVector_HAIRMOD_PROTECTED2.size() - 1;
					}
					mScroll.moveTo(index * (hImg + 2));
				}
				else if (Canvas_HAIRMOD_PROTECTED.keyPressed[8])
				{
					Canvas_HAIRMOD_PROTECTED.keyPressed[8] = false;
					index++;
					if (index > mVector_HAIRMOD_PROTECTED2.size() - 1)
					{
						index = 0;
					}
					mScroll.moveTo(index * (hImg + 2));
				}
			}
			if (Canvas_HAIRMOD_PROTECTED.keyPressed[4])
			{
				Canvas_HAIRMOD_PROTECTED.keyPressed[4] = false;
				indexTab--;
				if (indexTab < 0)
				{
					indexTab = arraySubTile.Length - 1;
				}
				mScrollTab.moveTo(indexTab * (wTab + 2));
			}
			else if (Canvas_HAIRMOD_PROTECTED.keyPressed[6])
			{
				Canvas_HAIRMOD_PROTECTED.keyPressed[6] = false;
				indexTab++;
				if (indexTab > arraySubTile.Length - 1)
				{
					indexTab = 0;
				}
				mScrollTab.moveTo(indexTab * (wTab + 2));
			}
		}
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
}
