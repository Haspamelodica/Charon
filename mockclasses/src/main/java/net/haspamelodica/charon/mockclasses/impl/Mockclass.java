package net.haspamelodica.charon.mockclasses.impl;

/** Marker interface for all dynamically-generated mock classes. <b>Users should never implement this interface.</b> */
public interface Mockclass<REF>
{
	String GET_REF_METHOD_NAME = "getRef";

	// When renaming, don't forget to update corresponding name constant
	public REF getRef();
}
