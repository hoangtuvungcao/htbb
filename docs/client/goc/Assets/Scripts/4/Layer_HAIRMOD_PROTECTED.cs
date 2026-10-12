public class Layer_HAIRMOD_PROTECTED
{
	public ActionPerform_HAIRMOD_PROTECTED isUpdate;

	public ActionKey_HAIRMOD_PROTECTED isKeyCode;

	public ActionPaintCmd_HAIRMOD_PROTECTED isPaint;

	public void update()
	{
		isUpdate();
	}

	public void paint(MyGraphics_HAIRMOD_PROTECTED g, int x, int y)
	{
		isPaint(g, x, y);
	}

	public void keyPress(int keyCode)
	{
		isKeyCode(keyCode);
	}
}
