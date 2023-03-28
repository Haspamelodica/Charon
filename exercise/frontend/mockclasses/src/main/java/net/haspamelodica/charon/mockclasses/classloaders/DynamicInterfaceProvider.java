package net.haspamelodica.charon.mockclasses.classloaders;

import net.bytebuddy.description.type.TypeDefinition;

public interface DynamicInterfaceProvider
{
	public TypeDefinition typeDefinitionFor(String classname);
}
