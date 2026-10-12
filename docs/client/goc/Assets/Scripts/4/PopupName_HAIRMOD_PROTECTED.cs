public class PopupName_HAIRMOD_PROTECTED : Tree_HAIRMOD_PROTECTED
{
	public string name;

	public byte num;

	public byte iPrivate;

	public static FrameImage_HAIRMOD_PROTECTED imgArrow;

	public PopupName_HAIRMOD_PROTECTED(string name, int x, int y)
		: base(x, y, 0)
	{
		base.x = (short)x;
		base.y = (short)y;
		this.name = name;
		num = (byte)Res_HAIRMOD_PROTECTED.rnd(8);
	}

	public new void update()
	{
		num++;
		if (num >= 8)
		{
			num = 0;
		}
	}

	public bool canPaint(int x, int y, int w, int h)
	{
		if (x - w / 2 > GameScr_HAIRMOD_PROTECTED.cmx + Canvas_HAIRMOD_PROTECTED.w || x + w / 2 < GameScr_HAIRMOD_PROTECTED.cmx || y < GameScr_HAIRMOD_PROTECTED.cmy || y + h > GameScr_HAIRMOD_PROTECTED.cmy + Canvas_HAIRMOD_PROTECTED.h)
		{
			return false;
		}
		return true;
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		g.drawImage(Res_HAIRMOD_PROTECTED.imgShadow, x, y, 3);
		if (imgArrow != null)
		{
			imgArrow.drawFrame(0, x, y - 10 + num / 2, 0, MyGraphics_HAIRMOD_PROTECTED.BOTTOM | MyGraphics_HAIRMOD_PROTECTED.HCENTER, g);
		}
		MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, name, x, y - 32 + num / 2 - 2, 2);
	}
}
