using UnityEngine;

public class MyAudioClip_HAIRMOD_PROTECTED
{
	public string name;

	public AudioClip clip;

	public long timeStart;

	public MyAudioClip_HAIRMOD_PROTECTED(string filename)
	{
		clip = (AudioClip)Resources.Load(filename);
		name = filename;
	}

	public void Play()
	{
		Main_HAIRMOD_PROTECTED.main.GetComponent<AudioSource>().PlayOneShot(clip);
		timeStart = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis();
	}

	public bool isPlaying()
	{
		return false;
	}
}
