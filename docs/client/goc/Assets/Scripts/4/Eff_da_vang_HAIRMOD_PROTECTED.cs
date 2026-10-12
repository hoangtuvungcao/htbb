public class Eff_da_vang_HAIRMOD_PROTECTED : Effect_HAIRMOD_PROTECTED
{
	public int vx;

	public int vy;

	public int t;

	public int y_changspeed;

	public int tt;

	private int[] arry = new int[3] { -10, 12, 0 };

	public int[] time = new int[2] { 0, 1 };

	public Image_HAIRMOD_PROTECTED img;

	public Eff_da_vang_HAIRMOD_PROTECTED(int x, int y)
	{
		base.x = x;
		base.y = y;
		vx = Canvas_HAIRMOD_PROTECTED.random(-1, 3);
		t = Canvas_HAIRMOD_PROTECTED.random(5, 10);
		y_changspeed = y + arry[Math_HAIRMOD_PROTECTED.abs(Canvas_HAIRMOD_PROTECTED.r.nextInt() % arry.Length)];
		tt = time[Math_HAIRMOD_PROTECTED.abs(Canvas_HAIRMOD_PROTECTED.r.nextInt() % time.Length)];
		img = Res_HAIRMOD_PROTECTED.imgEffect[60];
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		g.drawImage(img, x, y, 3);
	}

	public override void update()
	{
		if (tt >= 0)
		{
			tt--;
		}
		if (tt < 0)
		{
			x += vx;
			y += vy;
			if (vy < 10)
			{
				vy += 2;
			}
			if (y >= y_changspeed)
			{
				vy = -t;
				t /= 3;
			}
			if (t <= 2)
			{
				vx = 0;
				wantDetroy = true;
			}
		}
	}
}
