public class Party_HAIRMOD_PROTECTED
{
	public short idParty = -1;

	private mVector_HAIRMOD_PROTECTED userParty = new mVector_HAIRMOD_PROTECTED();

	public Party_HAIRMOD_PROTECTED()
	{
	}

	public Party_HAIRMOD_PROTECTED(short id)
	{
		idParty = id;
	}

	public void addUser(PartyInfo_HAIRMOD_PROTECTED user)
	{
		if (!userParty.conTains(user))
		{
			userParty.addElement(user);
		}
	}

	public void removeUser(PartyInfo_HAIRMOD_PROTECTED user)
	{
		if (userParty.conTains(user))
		{
			userParty.removeElement(user);
		}
	}

	public int getSize()
	{
		return userParty.size();
	}

	public mVector_HAIRMOD_PROTECTED getChar()
	{
		return userParty;
	}
}
