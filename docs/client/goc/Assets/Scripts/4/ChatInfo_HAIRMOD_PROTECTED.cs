public class ChatInfo_HAIRMOD_PROTECTED
{
	public string name;

	public int ID;

	public string content;

	public ChatInfo_HAIRMOD_PROTECTED()
	{
	}

	public ChatInfo_HAIRMOD_PROTECTED(string from, string content, int color)
	{
		name = from;
		this.content = content.ToLower();
	}
}
