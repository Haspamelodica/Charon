package net.haspamelodica.studentcodeseparator;

public interface StudentSide
{
	public <SO extends StudentSideObject, SP extends StudentSidePrototype<SO>> SP createPrototype(Class<SP> prototypeClass);
}
