public class Command_HAIRMOD_PROTECTED
{
	public string caption = string.Empty;

	public ActionPerform_HAIRMOD_PROTECTED action;

	public ActionPaintCmd_HAIRMOD_PROTECTED actionPaint;

	public ActionChat_HAIRMOD_PROTECTED actionChat;

	public int idMap = -1;

	public Command_HAIRMOD_PROTECTED(string caption)
	{
		this.caption = caption;
	}

	public Command_HAIRMOD_PROTECTED()
	{
	}

	public void perform()
	{
		if (action != null)
		{
			action();
		}
	}

	public void paint(MyGraphics_HAIRMOD_PROTECTED g, int x, int y)
	{
		if (actionPaint != null)
		{
			actionPaint(g, x, y);
		}
	}

	public void perform(string str)
	{
		if (actionChat != null)
		{
			actionChat(str);
		}
	}
}
