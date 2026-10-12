public class Message_HAIRMOD_PROTECTED
{
	public byte FLAGS;

	public sbyte command;

	private myReader_HAIRMOD_PROTECTED dis;

	private myWriter_HAIRMOD_PROTECTED dos;

	public Message_HAIRMOD_PROTECTED(int command)
	{
		this.command = (sbyte)command;
		dos = new myWriter_HAIRMOD_PROTECTED();
	}

	public Message_HAIRMOD_PROTECTED()
	{
		dos = new myWriter_HAIRMOD_PROTECTED();
	}

	public Message_HAIRMOD_PROTECTED(sbyte command)
	{
		this.command = command;
		dos = new myWriter_HAIRMOD_PROTECTED();
	}

	public Message_HAIRMOD_PROTECTED(sbyte command, sbyte[] data)
	{
		this.command = command;
		dis = new myReader_HAIRMOD_PROTECTED(data);
	}

	public sbyte[] getData()
	{
		return dos.getData();
	}

	public myReader_HAIRMOD_PROTECTED reader()
	{
		return dis;
	}

	public myWriter_HAIRMOD_PROTECTED writer()
	{
		return dos;
	}

	public void cleanup()
	{
	}
}
