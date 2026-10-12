public class ChatTab_HAIRMOD_PROTECTED
{
	public Command_HAIRMOD_PROTECTED center;

	public Command_HAIRMOD_PROTECTED right;

	public int count;

	public string name;

	public mVector_HAIRMOD_PROTECTED text;

	public bool isInput;

	public bool isOpen;

	public ChatTab_HAIRMOD_PROTECTED(string name, Command_HAIRMOD_PROTECTED cen, Command_HAIRMOD_PROTECTED right, bool isInput)
	{
		this.name = name;
		center = cen;
		this.right = right;
		this.isInput = isInput;
		isOpen = true;
		count = Res_HAIRMOD_PROTECTED.rnd(10);
	}
}
