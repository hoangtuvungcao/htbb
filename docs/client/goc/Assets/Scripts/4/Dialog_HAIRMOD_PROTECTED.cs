public abstract class Dialog_HAIRMOD_PROTECTED : MainJ2ME_HAIRMOD_PROTECTED
{
	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		base.paint(g);
	}

	public override void updateKey()
	{
		base.updateKey();
	}

	public virtual void keyPress(int keyCode)
	{
	}

	public virtual void update()
	{
		base.updateKey();
	}

	public void show()
	{
		Canvas_HAIRMOD_PROTECTED.currentDialog = this;
	}
}
