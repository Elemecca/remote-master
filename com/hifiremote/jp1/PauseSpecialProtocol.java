package com.hifiremote.jp1;

public class PauseSpecialProtocol extends SpecialProtocol
{
  public PauseSpecialProtocol( String name, Hex pid )
  {
    super( name, pid );
  }
  
  public SpecialProtocolFunction createFunction( KeyMove keyMove )
  {
    return new PauseFunction( keyMove );
  }
  
  public Hex createHex( SpecialFunctionDialog dlg )
  {
    return PauseFunction.createHex( dlg );
  }
  
  public String[] getFunctions(){ return functions; }
  
  private static final String[] functions = { "Pause" };
}
