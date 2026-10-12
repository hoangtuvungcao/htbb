public class Effect_XayBot_HAIRMOD_PROTECTED : Effect_HAIRMOD_PROTECTED
{
	private sbyte frame;

	private sbyte count;

	private sbyte id;

	private sbyte vx;

	private sbyte loai;

	private sbyte[] arr_frame = new sbyte[4] { 0, 1, 2, 3 };

	public int idimg;

	public int time;

	public int yto;

	public int tung = 1;

	public int va;

	private sbyte[] arr = new sbyte[5] { -2, -1, 0, 1, 2 };

	private sbyte[] arr1 = new sbyte[3] { 2, 10, 6 };

	public Effect_XayBot_HAIRMOD_PROTECTED(int x, int y, int time, int yto, int loai)
	{
		base.x = x;
		base.y = y;
		this.time = time;
		this.yto = yto;
		id = 54;
		type = -1;
		this.loai = (sbyte)loai;
		if (loai == -1)
		{
			tung = Canvas_HAIRMOD_PROTECTED.random(5, 15);
			vx = arr[Math_HAIRMOD_PROTECTED.abs(Canvas_HAIRMOD_PROTECTED.r.nextInt() % arr.Length)];
		}
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		g.setColor(15267833);
		g.fillRect(x, y, 2, 2);
	}

	public override void update()
	{
		if (time >= 0)
		{
			time--;
		}
		frame = arr_frame[Math_HAIRMOD_PROTECTED.abs(Canvas_HAIRMOD_PROTECTED.r.nextInt() % (arr_frame.Length - 1))];
		if (time < 0)
		{
			y += va;
		}
		x += vx;
		if (va < 10)
		{
			va++;
		}
		if (tung != 0)
		{
			x += arr[Math_HAIRMOD_PROTECTED.abs(Canvas_HAIRMOD_PROTECTED.r.nextInt() % arr.Length)];
			va = -tung;
			tung = 0;
		}
		if (y >= yto)
		{
			wantDetroy = true;
		}
	}
}
