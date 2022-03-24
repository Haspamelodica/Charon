package net.haspamelodica.studentcodeseparator.communicator.impl;

import java.util.List;
import java.util.stream.Collectors;

import net.haspamelodica.studentcodeseparator.communicator.StudentSideCommunicator;
import net.haspamelodica.studentcodeseparator.refs.Ref;

public class LoggingCommunicator<ATTACHMENT, REF extends Ref<ATTACHMENT>,
		COMM extends StudentSideCommunicator<ATTACHMENT, REF>> implements StudentSideCommunicator<ATTACHMENT, REF>
{
	protected final COMM communicator;

	private final String prefix;

	public LoggingCommunicator(COMM communicator)
	{
		this(communicator, "");
	}
	public LoggingCommunicator(COMM communicator, String prefix)
	{
		this.communicator = communicator;
		this.prefix = prefix;
	}

	public static <ATTACHMENT, REF extends Ref<ATTACHMENT>> StudentSideCommunicator<ATTACHMENT, REF>
			maybeWrapLogging(StudentSideCommunicator<ATTACHMENT, REF> communicator, String prefix, boolean logging)
	{
		if(logging)
			return new LoggingCommunicator<>(communicator, prefix);
		return communicator;
	}
	public static <ATTACHMENT, REF extends Ref<ATTACHMENT>> StudentSideCommunicator<ATTACHMENT, REF>
			maybeWrapLogging(StudentSideCommunicator<ATTACHMENT, REF> communicator, boolean logging)
	{
		if(logging)
			return new LoggingCommunicator<>(communicator);
		return communicator;
	}

	@Override
	public String getStudentSideClassname(REF ref)
	{
		log("classname " + ref);
		return communicator.getStudentSideClassname(ref);
	}

	@Override
	public REF callConstructor(String cn, List<String> params, List<REF> argRefs)
	{
		log("new " + cn + params.stream().collect(Collectors.joining(", ", "(", ")")) + ": " + argRefs.toString());
		return communicator.callConstructor(cn, params, argRefs);
	}

	@Override
	public REF callStaticMethod(String cn, String name, String returnClassname, List<String> params, List<REF> argRefs)
	{
		log(returnClassname + " " + cn + "." + name + params.stream().collect(Collectors.joining(", ", "(", ")")) + ": " + argRefs.toString());
		return communicator.callStaticMethod(cn, name, returnClassname, params, argRefs);
	}
	@Override
	public REF getStaticField(String cn, String name, String fieldClassname)
	{
		log(fieldClassname + " " + cn + "." + name);
		return communicator.getStaticField(cn, name, fieldClassname);
	}
	@Override
	public void setStaticField(String cn, String name, String fieldClassname, REF valueRef)
	{
		log(fieldClassname + " " + cn + "." + name + " = " + valueRef);
		communicator.setStaticField(cn, name, fieldClassname, valueRef);
	}

	@Override
	public REF callInstanceMethod(String cn, String name, String returnClassname, List<String> params, REF receiverRef, List<REF> argRefs)
	{
		log(returnClassname + " " + cn + "." + name + params.stream().collect(Collectors.joining(", ", "(", ")")) + ": " + receiverRef + ", " + argRefs.toString());
		return communicator.callInstanceMethod(cn, name, returnClassname, params, receiverRef, argRefs);
	}
	@Override
	public REF getInstanceField(String cn, String name, String fieldClassname, REF receiverRef)
	{
		log(fieldClassname + " " + cn + "." + name + ": " + receiverRef);
		return communicator.getInstanceField(cn, name, fieldClassname, receiverRef);
	}
	@Override
	public void setInstanceField(String cn, String name, String fieldClassname, REF receiverRef, REF valueRef)
	{
		log(fieldClassname + " " + cn + "." + name + ": " + receiverRef + " = " + valueRef);
		communicator.setInstanceField(cn, name, fieldClassname, receiverRef, valueRef);
	}

	protected void log(String message)
	{
		System.err.println(prefix + message);
	}
}
