package net.thedarktide.sifflion.darktape;

/**
 * A very simple cooldown datastructure
 * 
 * @author Xolsom
 */
public class Cooldown
{

	private final long end;
	private final long start;

	public Cooldown(long start, long duration)
	{
		this.end = start + duration;
		this.start = start;
	}

	public long getEnd()
	{
		return this.end;
	}

	public long getStart()
	{
		return this.start;
	}

	public long getTimeLeft(long time)
	{
		long timeLeft = this.end - time;
		return (timeLeft < 0 ? 0 : timeLeft);
	}

	public boolean isActive(long time)
	{
		return (this.end > time);
	}

}