public class SkillClan_HAIRMOD_PROTECTED
{
	public short time;

	public long timeCur;

	public string info;

	public sbyte idIcon;

	public sbyte lv;

	public SkillClan_HAIRMOD_PROTECTED()
	{
		timeCur = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() / 1000;
	}

	public void updateTime()
	{
		if (mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() / 1000 - timeCur >= time)
		{
			time = 0;
		}
	}
}
