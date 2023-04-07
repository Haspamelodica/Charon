package net.haspamelodica.charon.marshaling;

public record StudentSideType<TYPEREF, T>(Class<T> localType, String studentSideCN, TYPEREF studentSideType)
{}
