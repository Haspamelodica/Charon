package net.haspamelodica.charon.mockclasses;

import java.util.List;

public record ClassInterface(String name, List<MethodInterface> methods, List<ConstructorInterface> constructors)
{}
