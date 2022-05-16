package net.haspamelodica.charon.mockclasses;

import java.util.List;

import net.bytebuddy.description.type.TypeDefinition;

public record ConstructorInterface(List<TypeDefinition> parameterTypes)
{}
