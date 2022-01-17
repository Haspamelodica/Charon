package net.haspamelodica.studentcodeseparator.communicator;

public interface StudentSideCommunicator<REF>
{
	public REF callConstructor(String classname, Class<?>[] paramTypes, Object... args);

	public <R> R callStaticMethod(String classname, String name, Class<R> returnType, Class<?>[] paramTypes, Object... args);
	public <F> F getStaticField(String classname, String name, Class<F> fieldType);
	public <F> void setStaticField(String classname, String name, Class<F> fieldType, F value);

	public <R> R callInstanceMethod(String classname, String name, Class<R> returnType, Class<?>[] paramTypes, REF ref, Object... args);
	public <F> F getField(String classname, String name, Class<F> fieldType, REF ref);
	public <F> void setField(String classname, String name, Class<F> fieldType, REF ref, F value);
}
