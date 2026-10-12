public class Explode1_HAIRMOD_PROTECTED : Exp_HAIRMOD_PROTECTED
{
	private static sbyte[] frame = new sbyte[8] { 0, 1, 2, 1, 0, 1, 2, 1 };

	private int f;

	private int degree;

	private Image_HAIRMOD_PROTECTED img;

	private int xt;

	private int yt;

	private int d;

	private int delay = 10;

	private int delta = 10;

	public Explode1_HAIRMOD_PROTECTED(Image_HAIRMOD_PROTECTED img, int x, int y, int degree)
	{
		this.img = img;
		base.x = x;
		base.y = y;
		this.degree = degree;
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (img != null)
		{
			g.drawImage(img, x + xt, y + yt, 20);
			Res_HAIRMOD_PROTECTED.paintImgEff(g, 15, 0, frame[f] * Effect_HAIRMOD_PROTECTED.HEIGHT[15], Effect_HAIRMOD_PROTECTED.WIDTH[15], Effect_HAIRMOD_PROTECTED.HEIGHT[15], x + xt + img.getWidth() / 2, y + yt + img.getHeight() / 2, MyGraphics_HAIRMOD_PROTECTED.BOTTOM | MyGraphics_HAIRMOD_PROTECTED.HCENTER);
			Res_HAIRMOD_PROTECTED.paintImgEff(g, 15, 0, frame[f] * Effect_HAIRMOD_PROTECTED.HEIGHT[15], Effect_HAIRMOD_PROTECTED.WIDTH[15], Effect_HAIRMOD_PROTECTED.HEIGHT[15], x + xt + img.getWidth() / 2 + 5, y + yt + img.getHeight() / 2, MyGraphics_HAIRMOD_PROTECTED.BOTTOM | MyGraphics_HAIRMOD_PROTECTED.HCENTER);
			Res_HAIRMOD_PROTECTED.paintImgEff(g, 15, 0, frame[f] * Effect_HAIRMOD_PROTECTED.HEIGHT[15], Effect_HAIRMOD_PROTECTED.WIDTH[15], Effect_HAIRMOD_PROTECTED.HEIGHT[15], x + xt + img.getWidth() / 2 - 5, y + yt + img.getHeight() / 2, MyGraphics_HAIRMOD_PROTECTED.BOTTOM | MyGraphics_HAIRMOD_PROTECTED.HCENTER);
			Res_HAIRMOD_PROTECTED.paintImgEff(g, 15, 0, frame[f] * Effect_HAIRMOD_PROTECTED.HEIGHT[15], Effect_HAIRMOD_PROTECTED.WIDTH[15], Effect_HAIRMOD_PROTECTED.HEIGHT[15], x + xt + img.getWidth() / 2 + 5, y + yt + img.getHeight() / 2 + 5, MyGraphics_HAIRMOD_PROTECTED.BOTTOM | MyGraphics_HAIRMOD_PROTECTED.HCENTER);
			Res_HAIRMOD_PROTECTED.paintImgEff(g, 15, 0, frame[f] * Effect_HAIRMOD_PROTECTED.HEIGHT[15], Effect_HAIRMOD_PROTECTED.WIDTH[15], Effect_HAIRMOD_PROTECTED.HEIGHT[15], x + xt + img.getWidth() / 2 - 5, y + yt + img.getHeight() / 2 + 5, MyGraphics_HAIRMOD_PROTECTED.BOTTOM | MyGraphics_HAIRMOD_PROTECTED.HCENTER);
		}
	}

	public override void update()
	{
		if (d < 100)
		{
			d += delta;
			xt = Util_HAIRMOD_PROTECTED.Cos(degree) * d / 1024;
			yt = Util_HAIRMOD_PROTECTED.Sin(degree) * d / 1024;
			delta += 10;
		}
		else
		{
			destroy = true;
		}
		if (Canvas_HAIRMOD_PROTECTED.gameTick % 2 == 0)
		{
			f = (f + 1) % frame.Length;
		}
	}
}
