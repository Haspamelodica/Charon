package net.haspamelodica.charon.mockclasses;

import java.util.List;

import net.bytebuddy.description.type.TypeDefinition;

public record MethodInterface(String name, TypeDefinition returnType, boolean isStatic, List<TypeDefinition> parameters)
{}