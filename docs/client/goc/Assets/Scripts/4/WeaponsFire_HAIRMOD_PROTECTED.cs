public class WeaponsFire_HAIRMOD_PROTECTED : IArrow_HAIRMOD_PROTECTED
{
	private int va;

	private int vx;

	private int vy;

	private int angle;

	private int x;

	private int y;

	private int type;

	private int dx;

	private int dy;

	private int xTo;

	private int yTo;

	private int himg;

	private LiveActor_HAIRMOD_PROTECTED live_To;

	private int frame;

	private int transform;

	private EffectLeg_HAIRMOD_PROTECTED effleg;

	private int frameEff;

	private int saveDir;

	private bool isStartEff;

	public WeaponsFire_HAIRMOD_PROTECTED(LiveActor_HAIRMOD_PROTECTED live_From, LiveActor_HAIRMOD_PROTECTED live_To, int type)
	{
		this.type = type;
		this.live_To = live_To;
		x = live_From.x;
		y = live_From.y - 54;
		xTo = live_To.x;
		yTo = live_To.y;
		angle = Util_HAIRMOD_PROTECTED.angle(x - live_To.x, live_To.y - (live_To.height >> 1) - y);
		va = 24;
		vx = va * Util_HAIRMOD_PROTECTED.Cos(angle) >> 10;
		vy = va * Util_HAIRMOD_PROTECTED.Sin(angle) >> 10;
		himg = live_To.height;
		if (type < 6)
		{
			effleg = new EffectLeg_HAIRMOD_PROTECTED(xTo, yTo, (type == 3) ? 1 : 0, 0, 0, 0, false, false);
			EffectManager_HAIRMOD_PROTECTED.lowEffects.addElement(effleg);
		}
		y = live_From.y - 31;
		if (live_From.dir == Char_HAIRMOD_PROTECTED.LEFT)
		{
			x = live_From.x - 30;
		}
		else if (live_From.dir == Char_HAIRMOD_PROTECTED.RIGHT)
		{
			x = live_From.x + 30;
		}
		int num = Arrow_HAIRMOD_PROTECTED.findDirIndexFromAngle(Util_HAIRMOD_PROTECTED.angle(live_To.x - x, -(live_To.y + (live_To.height >> 1) - y)));
		frame = Arrow_HAIRMOD_PROTECTED.FRAME[num];
		transform = Arrow_HAIRMOD_PROTECTED.TRANSFORM[num];
	}

	public override void onArrowTouchTarget()
	{
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (!isStartEff)
		{
			if (Res_HAIRMOD_PROTECTED.getImgArrow(type) != null && type != 6 && type != 7)
			{
				g.drawRegion(Res_HAIRMOD_PROTECTED.imgArrow[type], 0, frame * Arrow_HAIRMOD_PROTECTED.ARROWSIZE[1][type], Arrow_HAIRMOD_PROTECTED.ARROWSIZE[0][type], Arrow_HAIRMOD_PROTECTED.ARROWSIZE[1][type], transform, x, y, 3);
			}
		}
		else if (Res_HAIRMOD_PROTECTED.imgEffect[38] != null)
		{
			g.drawRegion(Res_HAIRMOD_PROTECTED.imgEffect[38], 0, frameEff * 27, 27, 27, 0, xTo, yTo, 33);
		}
	}

	public void set(int type, int x, int y, int power, byte effect, LiveActor_HAIRMOD_PROTECTED owner, LiveActor_HAIRMOD_PROTECTED target)
	{
	}

	public override void setAngle(int angle)
	{
	}

	public override void update()
	{
		if (live_To != null)
		{
			xTo = live_To.x;
			yTo = live_To.y;
		}
		dx = xTo - x;
		dy = yTo - (himg >> 1) - y;
		angle = Util_HAIRMOD_PROTECTED.angle(dx, dy);
		vx = va * Util_HAIRMOD_PROTECTED.Cos(angle) >> 10;
		vy = va * Util_HAIRMOD_PROTECTED.Sin(angle) >> 10;
		x += vx;
		y += vy;
		bool flag = false;
		bool flag2 = false;
		if (!isStartEff)
		{
			if (type < 6)
			{
				EffectManager_HAIRMOD_PROTECTED.addHiEffect(x, y, (type != 3) ? 6 : 8);
			}
			else if (type == 6)
			{
				EffectManager_HAIRMOD_PROTECTED.addHiEffect(x, y, 46);
			}
		}
		if (Res_HAIRMOD_PROTECTED.isDestroy(x - 20, x + 20, xTo - himg / 2, xTo + himg / 2, y - 20, y + 20, yTo - himg / 2, yTo + himg / 2))
		{
			if (!isStartEff)
			{
				if (type == 3)
				{
					creatMyEff(live_To, 0, 0, 18);
					creatMyEff(live_To, 1, 0, 24);
					creatMyEff(live_To, 2, 0, 28);
					creatMyEff(live_To, 3, 0, 32);
				}
				else if (type < 6)
				{
					EffectManager_HAIRMOD_PROTECTED.addHiEffect(xTo, yTo - 10, 30);
					if (Canvas_HAIRMOD_PROTECTED.gameScr.arrowsDown.conTains(this))
					{
						Canvas_HAIRMOD_PROTECTED.gameScr.arrowsDown.removeElement(this);
					}
					if (GameScr_HAIRMOD_PROTECTED.arrowsUp.conTains(this))
					{
						GameScr_HAIRMOD_PROTECTED.arrowsUp.removeElement(this);
					}
					live_To.doublejump();
					creatMyEff(live_To, 0, 1, 40);
					creatMyEff(live_To, 1, 1, 40);
					creatMyEff(live_To, 2, 1, 40);
					creatMyEff(live_To, 3, 1, 40);
				}
				else if (type == 6)
				{
					if (Canvas_HAIRMOD_PROTECTED.gameScr.arrowsDown.conTains(this))
					{
						Canvas_HAIRMOD_PROTECTED.gameScr.arrowsDown.removeElement(this);
					}
					if (GameScr_HAIRMOD_PROTECTED.arrowsUp.conTains(this))
					{
						GameScr_HAIRMOD_PROTECTED.arrowsUp.removeElement(this);
					}
					live_To.doublejump();
				}
			}
			isStartEff = true;
		}
		if (!isStartEff || Canvas_HAIRMOD_PROTECTED.gameTick % 2 != 0)
		{
			return;
		}
		frameEff++;
		if (frameEff > 4)
		{
			if (Canvas_HAIRMOD_PROTECTED.gameScr.arrowsDown.conTains(this))
			{
				Canvas_HAIRMOD_PROTECTED.gameScr.arrowsDown.removeElement(this);
			}
			if (GameScr_HAIRMOD_PROTECTED.arrowsUp.conTains(this))
			{
				GameScr_HAIRMOD_PROTECTED.arrowsUp.removeElement(this);
			}
			isStartEff = false;
			frameEff = 0;
		}
	}

	public void creatMyEff(LiveActor_HAIRMOD_PROTECTED l_To, int dir, int type, int va)
	{
		EffectTarget_HAIRMOD_PROTECTED o = new EffectTarget_HAIRMOD_PROTECTED(l_To, dir, type, va);
		EffectManager_HAIRMOD_PROTECTED.hiEffects.addElement(o);
	}
}
