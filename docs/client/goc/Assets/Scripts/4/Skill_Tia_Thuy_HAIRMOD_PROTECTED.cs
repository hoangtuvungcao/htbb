public class Skill_Tia_Thuy_HAIRMOD_PROTECTED : Effect_HAIRMOD_PROTECTED
{
	private LiveActor_HAIRMOD_PROTECTED to;

	private int angle;

	private int index;

	public Skill_Tia_Thuy_HAIRMOD_PROTECTED(LiveActor_HAIRMOD_PROTECTED to, int xFrom, int yFrom)
	{
		this.to = to;
		x = xFrom;
		y = yFrom;
	}

	public override void update()
	{
		angle = Util_HAIRMOD_PROTECTED.angle(to.x - x, -(to.y - y));
		int num = 10 * Util_HAIRMOD_PROTECTED.Cos(Util_HAIRMOD_PROTECTED.fixangle(angle)) >> 10;
		int num2 = -(10 * Util_HAIRMOD_PROTECTED.Sin(Util_HAIRMOD_PROTECTED.fixangle(angle))) >> 10;
		x += num;
		y += num2;
		if (Util_HAIRMOD_PROTECTED.distance(x, y, to.x, to.y) <= 20)
		{
			EffectManager_HAIRMOD_PROTECTED.addHiEffect(to.x, to.y - 10, 30);
			EffectManager_HAIRMOD_PROTECTED.hiEffects.removeElement(this);
		}
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		Res_HAIRMOD_PROTECTED.paintImgEff(g, 19, 0, index % 3 * 14, 25, 14, x, y, 3);
		index++;
	}
}
