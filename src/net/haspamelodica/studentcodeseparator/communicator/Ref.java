package net.haspamelodica.studentcodeseparator.communicator;

public abstract class Ref<ATTACHMENT>
{
	private ATTACHMENT attachment;

	public final ATTACHMENT getAttachment()
	{
		return attachment;
	}
	public final void setAttachment(ATTACHMENT attachment)
	{
		this.attachment = attachment;
	}
}
