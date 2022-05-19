package net.haspamelodica.charon.mockclasses.dynamicclasses;

import net.bytebuddy.description.type.TypeDefinition;

public interface DynamicInterfaceProvider
{
	public TypeDefinition typeDefinitionFor(String classname);
}
