public abstract class MainJ2ME_HAIRMOD_PROTECTED
{
	public Command_HAIRMOD_PROTECTED left;

	public Command_HAIRMOD_PROTECTED center;

	public Command_HAIRMOD_PROTECTED right;

	public static int hTab = 45;

	public static int ITEM_HEIGHT = 40;

	public static int CHAR_HEIGHT = 20;

	private static bool isQwerty;

	public virtual void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		int num = GameMidlet_HAIRMOD_PROTECTED.hDrawStringCmd - 2;
		Canvas_HAIRMOD_PROTECTED.resetTrans(g);
		if (left != null && left.caption != string.Empty)
		{
			if (Canvas_HAIRMOD_PROTECTED.pointCmdBar() != 1)
			{
				g.drawImage(Res_HAIRMOD_PROTECTED.imgButtonBar, 4, Canvas_HAIRMOD_PROTECTED.h - hTab + (hTab - Res_HAIRMOD_PROTECTED.imgButtonBar.getHeight()) / 2 + 2, 0);
			}
			else
			{
				g.drawImage(Res_HAIRMOD_PROTECTED.imgButtonBar1, 4, Canvas_HAIRMOD_PROTECTED.h - hTab + (hTab - Res_HAIRMOD_PROTECTED.imgButtonBar1.getHeight()) / 2 + 2, 0);
			}
			MFont_HAIRMOD_PROTECTED.borderFont.drawString(g, left.caption, 4 + Res_HAIRMOD_PROTECTED.imgButtonBar.getWidth() / 2, num + 2, 2);
		}
		if (center != null && center.caption != string.Empty)
		{
			if (Canvas_HAIRMOD_PROTECTED.pointCmdBar() != 2)
			{
				g.drawImage(Res_HAIRMOD_PROTECTED.imgButtonBar, Canvas_HAIRMOD_PROTECTED.w / 2 - Res_HAIRMOD_PROTECTED.imgButtonBar.getWidth() / 2, Canvas_HAIRMOD_PROTECTED.h - hTab + (hTab - Res_HAIRMOD_PROTECTED.imgButtonBar.getHeight()) / 2 + 2, 0);
			}
			else
			{
				g.drawImage(Res_HAIRMOD_PROTECTED.imgButtonBar1, Canvas_HAIRMOD_PROTECTED.w / 2 - Res_HAIRMOD_PROTECTED.imgButtonBar1.getWidth() / 2, Canvas_HAIRMOD_PROTECTED.h - hTab + (hTab - Res_HAIRMOD_PROTECTED.imgButtonBar.getHeight()) / 2 + 2, 0);
			}
			MFont_HAIRMOD_PROTECTED.borderFont.drawString(g, center.caption, Canvas_HAIRMOD_PROTECTED.hw, num + 2, 2);
		}
		if (right != null && right.caption != string.Empty)
		{
			if (Canvas_HAIRMOD_PROTECTED.pointCmdBar() != 3)
			{
				g.drawImage(Res_HAIRMOD_PROTECTED.imgButtonBar, Canvas_HAIRMOD_PROTECTED.w - Res_HAIRMOD_PROTECTED.imgButtonBar.getWidth() - 4, Canvas_HAIRMOD_PROTECTED.h - hTab + (hTab - Res_HAIRMOD_PROTECTED.imgButtonBar.getHeight()) / 2 + 2, 0);
			}
			else
			{
				g.drawImage(Res_HAIRMOD_PROTECTED.imgButtonBar1, Canvas_HAIRMOD_PROTECTED.w - Res_HAIRMOD_PROTECTED.imgButtonBar1.getWidth() - 4, Canvas_HAIRMOD_PROTECTED.h - hTab + (hTab - Res_HAIRMOD_PROTECTED.imgButtonBar.getHeight()) / 2 + 2, 0);
			}
			MFont_HAIRMOD_PROTECTED.borderFont.drawString(g, right.caption, Canvas_HAIRMOD_PROTECTED.w - Res_HAIRMOD_PROTECTED.imgButtonBar.getWidth() / 2 - 4, num + 2, 2);
		}
	}

	public virtual void updateKey()
	{
		if (Canvas_HAIRMOD_PROTECTED.isPointerClick && Math_HAIRMOD_PROTECTED.abs(Canvas_HAIRMOD_PROTECTED.pyLast - Canvas_HAIRMOD_PROTECTED.py) <= 10 && Math_HAIRMOD_PROTECTED.abs(Canvas_HAIRMOD_PROTECTED.pxLast - Canvas_HAIRMOD_PROTECTED.px) <= 10)
		{
			switch (Canvas_HAIRMOD_PROTECTED.collisionCmdBar())
			{
			case 0:
				if (left != null && left.action != null)
				{
					left.perform();
					Canvas_HAIRMOD_PROTECTED.isPointerClick = false;
				}
				break;
			case 1:
				if (center != null && center.action != null)
				{
					Canvas_HAIRMOD_PROTECTED.endDlg();
					center.perform();
					Canvas_HAIRMOD_PROTECTED.isPointerClick = false;
				}
				break;
			case 2:
				if (right != null && right.action != null)
				{
					right.perform();
					Canvas_HAIRMOD_PROTECTED.isPointerClick = false;
				}
				break;
			}
		}
		if (Canvas_HAIRMOD_PROTECTED.keyPressed[5])
		{
			if (center != null && center.action != null)
			{
				center.perform();
				Canvas_HAIRMOD_PROTECTED.keyPressed[5] = false;
			}
		}
		else if (Canvas_HAIRMOD_PROTECTED.keyPressed[12])
		{
			if (left != null && left.action != null)
			{
				left.perform();
				Canvas_HAIRMOD_PROTECTED.keyPressed[12] = false;
			}
		}
		else if (Canvas_HAIRMOD_PROTECTED.keyPressed[13] && right != null && right.action != null)
		{
			right.perform();
			Canvas_HAIRMOD_PROTECTED.keyPressed[13] = false;
		}
	}
}
