public class ItemQsScreen_HAIRMOD_PROTECTED : MyScreen_HAIRMOD_PROTECTED
{
	public static ItemQsScreen_HAIRMOD_PROTECTED me;

	public MyScreen_HAIRMOD_PROTECTED last;

	public Scroll_HAIRMOD_PROTECTED mScroll = new Scroll_HAIRMOD_PROTECTED();

	public static sbyte totalItemSelect = 0;

	public static mVector_HAIRMOD_PROTECTED allItemLucky = new mVector_HAIRMOD_PROTECTED();

	public static int xBg;

	public static int yBg;

	public static int w;

	public static int h;

	public static int index;

	public static int nC = 8;

	public static int nH;

	public static short total = 32;

	private int sizeCell = 20;

	public ItemQsScreen_HAIRMOD_PROTECTED()
	{
		xBg = Canvas_HAIRMOD_PROTECTED.w / 2 - 80 - 10;
		yBg = Canvas_HAIRMOD_PROTECTED.hh - 40 - 10;
		w = 170;
		h = 90;
	}

	public static ItemQsScreen_HAIRMOD_PROTECTED gI()
	{
		return (me != null) ? me : (me = new ItemQsScreen_HAIRMOD_PROTECTED());
	}

	public void resetData()
	{
		mScroll.clear();
		totalItemSelect = 0;
		allItemLucky = new mVector_HAIRMOD_PROTECTED();
		total = 32;
		index = 0;
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (last != null)
		{
			last.paint(g);
		}
		Canvas_HAIRMOD_PROTECTED.resetTrans(g);
		Res_HAIRMOD_PROTECTED.paintDlgDragonFull(g, xBg, yBg, w, h);
		int num = total;
		mScroll.setStyle(num / nC, sizeCell, xBg + 5, yBg + 5, w, h - 5, true, 8);
		mScroll.setClip(g, xBg + 5, yBg + 5, w, h - 3);
		int num2 = index / nC;
		int num3 = index - num2 * nC;
		g.setColor(10595790);
		g.fillRect(xBg + num3 * sizeCell + 5, yBg + num2 * sizeCell + 5, sizeCell, sizeCell);
		sbyte b = 0;
		sbyte b2 = 0;
		for (int i = 0; i < allItemLucky.size(); i++)
		{
			ItemLuckyDraw_HAIRMOD_PROTECTED itemLuckyDraw_HAIRMOD_PROTECTED = (ItemLuckyDraw_HAIRMOD_PROTECTED)allItemLucky.elementAt(i);
			if (itemLuckyDraw_HAIRMOD_PROTECTED.isSelect == 1)
			{
				itemLuckyDraw_HAIRMOD_PROTECTED.paint(g, xBg + b * sizeCell + sizeCell / 2 + 5, yBg + b2 * sizeCell + sizeCell / 2 + 5);
				b++;
				if (b >= nC)
				{
					b = 0;
					b2++;
				}
				continue;
			}
			break;
		}
		for (int j = 0; j < nH; j++)
		{
			for (int k = 0; k < nC; k++)
			{
				g.setColor(8684162);
				g.drawRect(xBg + k * sizeCell + 5, yBg + j * sizeCell + 5, sizeCell, sizeCell);
			}
		}
		base.paint(g);
	}

	public override void switchToMe()
	{
		base.switchToMe();
		ActionPerform_HAIRMOD_PROTECTED action = delegate
		{
			doselect();
		};
		center = new Command_HAIRMOD_PROTECTED("Chọn");
		center.action = action;
		ActionPerform_HAIRMOD_PROTECTED action2 = delegate
		{
			if (index < allItemLucky.size() && index >= 0)
			{
				ItemLuckyDraw_HAIRMOD_PROTECTED itemLuckyDraw_HAIRMOD_PROTECTED = (ItemLuckyDraw_HAIRMOD_PROTECTED)allItemLucky.elementAt(index);
				if (itemLuckyDraw_HAIRMOD_PROTECTED.isSelect == 1)
				{
					DialLuckyScr_HAIRMOD_PROTECTED.gI().switchToMe();
					DialLuckyScr_HAIRMOD_PROTECTED.gI().lastScr = gI().last;
					DialLuckyScr_HAIRMOD_PROTECTED.gI().degreeKim = 90;
					DialLuckyScr_HAIRMOD_PROTECTED.gI().isStart = false;
				}
				else
				{
					Canvas_HAIRMOD_PROTECTED.startOKDlg("Vui lòng chọn 1 vật phẩm để quay số.");
				}
			}
		};
		left = new Command_HAIRMOD_PROTECTED("Quay số");
		left.action = action2;
		ActionPerform_HAIRMOD_PROTECTED action3 = delegate
		{
			resetData();
			last.switchToMe();
		};
		right = new Command_HAIRMOD_PROTECTED("Đóng");
		right.action = action3;
		mScroll.clear();
	}

	protected void doselect()
	{
		if (index < allItemLucky.size() && index >= 0)
		{
			ItemLuckyDraw_HAIRMOD_PROTECTED itemLuckyDraw_HAIRMOD_PROTECTED = (ItemLuckyDraw_HAIRMOD_PROTECTED)allItemLucky.elementAt(index);
			if (itemLuckyDraw_HAIRMOD_PROTECTED.isSelect == 1)
			{
				Canvas_HAIRMOD_PROTECTED.startOKDlg(itemLuckyDraw_HAIRMOD_PROTECTED.getDecript());
			}
		}
	}

	public override void update()
	{
		if (last != null)
		{
			last.update();
		}
		if (Canvas_HAIRMOD_PROTECTED.currentDialog == null)
		{
			ScrollResult_HAIRMOD_PROTECTED scrollResult_HAIRMOD_PROTECTED = mScroll.updateKey();
			if (scrollResult_HAIRMOD_PROTECTED.isDowning || scrollResult_HAIRMOD_PROTECTED.isFinish)
			{
				index = scrollResult_HAIRMOD_PROTECTED.selected;
			}
			nH = total / nC;
			mScroll.updatecm();
		}
		base.update();
	}

	public override void updateKey()
	{
		if (Canvas_HAIRMOD_PROTECTED.keyPressed[2])
		{
			Canvas_HAIRMOD_PROTECTED.keyPressed[2] = false;
			if (index >= 0 && index < nC)
			{
				index = 0;
			}
			else if (index - nC >= 0)
			{
				index -= nC;
			}
			mScroll.moveTo(index / nC * sizeCell);
		}
		else if (Canvas_HAIRMOD_PROTECTED.keyPressed[8])
		{
			Canvas_HAIRMOD_PROTECTED.keyPressed[8] = false;
			if (index + nC <= total - 1)
			{
				index += nC;
			}
			mScroll.moveTo(index / nC * sizeCell);
		}
		else if (Canvas_HAIRMOD_PROTECTED.keyPressed[4])
		{
			Canvas_HAIRMOD_PROTECTED.keyPressed[4] = false;
			index--;
			if (index < 0)
			{
				index = total - 1;
			}
			mScroll.moveTo(index / nC * sizeCell);
		}
		else if (Canvas_HAIRMOD_PROTECTED.keyPressed[6])
		{
			Canvas_HAIRMOD_PROTECTED.keyPressed[6] = false;
			index++;
			if (index > total)
			{
				index = 0;
			}
			mScroll.moveTo(index / nC * sizeCell);
		}
		base.updateKey();
	}
}
