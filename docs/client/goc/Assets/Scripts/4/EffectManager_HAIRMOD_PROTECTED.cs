public class EffectManager_HAIRMOD_PROTECTED : mVector_HAIRMOD_PROTECTED
{
	public static EffectManager_HAIRMOD_PROTECTED lowEffects = new EffectManager_HAIRMOD_PROTECTED();

	public static EffectManager_HAIRMOD_PROTECTED hiEffects = new EffectManager_HAIRMOD_PROTECTED();

	public void updateAll()
	{
		for (int num = size() - 1; num >= 0; num--)
		{
			Effect_HAIRMOD_PROTECTED effect_HAIRMOD_PROTECTED = (Effect_HAIRMOD_PROTECTED)elementAt(num);
			if (effect_HAIRMOD_PROTECTED != null)
			{
				effect_HAIRMOD_PROTECTED.update();
				if (effect_HAIRMOD_PROTECTED.wantDetroy)
				{
					effect_HAIRMOD_PROTECTED = null;
					removeElementAt(num);
				}
			}
		}
	}

	public void paintAll(MyGraphics_HAIRMOD_PROTECTED g)
	{
		for (int i = 0; i < size(); i++)
		{
			((Effect_HAIRMOD_PROTECTED)elementAt(i))?.paint(g);
		}
	}

	public static void addHiEffect(int x, int y, int type)
	{
		hiEffects.addElement(new Effect_HAIRMOD_PROTECTED(x, y, type));
	}

	public static void addHiEffect(Effect_HAIRMOD_PROTECTED e)
	{
		hiEffects.addElement(e);
	}

	public static void addLowEffect(int x, int y, int type)
	{
		lowEffects.addElement(new Effect_HAIRMOD_PROTECTED(x, y, type));
	}

	public static void addLowEffect(Effect_HAIRMOD_PROTECTED ef)
	{
		lowEffects.addElement(ef);
	}
}
