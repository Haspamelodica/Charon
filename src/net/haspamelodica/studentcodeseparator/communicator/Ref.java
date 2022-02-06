package net.haspamelodica.studentcodeseparator.communicator;

import net.haspamelodica.studentcodeseparator.StudentSideInstance;

public abstract class Ref
{
	private StudentSideInstance studentSideInstance;

	public final StudentSideInstance getStudentSideInstance()
	{
		return studentSideInstance;
	}
	public final void setStudentSideInstance(StudentSideInstance studentSideInstance)
	{
		this.studentSideInstance = studentSideInstance;
	}
}
