public class EffectObj_HAIRMOD_PROTECTED : Actor_HAIRMOD_PROTECTED
{
	public new short ID;

	public short dx;

	public short dy;

	public short idPlayer;

	public sbyte style;

	public sbyte index;

	public EffectObj_HAIRMOD_PROTECTED()
	{
		dx = (dy = 0);
		catagory = 100;
		index = 0;
	}

	public override void update()
	{
		EffectData_HAIRMOD_PROTECTED effect = EffectServerManager_HAIRMOD_PROTECTED.getEffect(ID);
		if (effect != null)
		{
			index++;
			if (index >= effect.arrFrame.Length)
			{
				remove();
			}
		}
		else
		{
			remove();
		}
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		EffectData_HAIRMOD_PROTECTED effect = EffectServerManager_HAIRMOD_PROTECTED.getEffect(ID);
		if (effect == null)
		{
			return;
		}
		if (style == 0)
		{
			Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED = Canvas_HAIRMOD_PROTECTED.gameScr.findActorByID(idPlayer);
			if (actor_HAIRMOD_PROTECTED != null)
			{
				x = (short)(actor_HAIRMOD_PROTECTED.x + dx);
				y = (short)(actor_HAIRMOD_PROTECTED.y + dy);
			}
		}
		effect.paint(g, x, y, index);
	}

	private void remove()
	{
		Canvas_HAIRMOD_PROTECTED.gameScr.actors.removeElement(this);
	}

	public override void setPosTo(short x, short y)
	{
	}
}
