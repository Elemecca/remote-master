package com.hifiremote.jp1;

public class ModeNameSpecialProtocol extends SpecialProtocol
{
  public ModeNameSpecialProtocol( String name, Hex pid )
  {
    super( name, pid );
  }
  
  public SpecialProtocolFunction createFunction( KeyMove keyMove )
  {
    return new ModeNameFunction( keyMove );
  }
  
  public Hex createHex( SpecialFunctionDialog dlg )
  {
    return ModeNameFunction.createHex( dlg );
  }
  
  public String[] getFunctions(){ return functions; }
  
  private static final String[] functions = { "ModeName" };
}
