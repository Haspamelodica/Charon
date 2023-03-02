package net.haspamelodica.charon.communicator.impl.reftranslating;

import net.haspamelodica.charon.communicator.StudentSideCommunicator;

public class UntranslatedRef
{
	private final UntranslatedRefInner<?> inner;

	public <REF> UntranslatedRef(StudentSideCommunicator<REF> communicator, REF ref)
	{
		this.inner = new UntranslatedRefInner<>(communicator, ref);
	}

	public String getClassname()
	{
		return inner.getClassname();
	}

	private static class UntranslatedRefInner<REF>
	{
		private final StudentSideCommunicator<REF>	communicator;
		private final REF							ref;

		public UntranslatedRefInner(StudentSideCommunicator<REF> communicator, REF ref)
		{
			this.communicator = communicator;
			this.ref = ref;
		}

		public String getClassname()
		{
			return communicator.getClassname(ref);
		}
	}
}