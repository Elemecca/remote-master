package com.hifiremote.jp1;

public class DSMSpecialProtocol extends SpecialProtocol
{
  public DSMSpecialProtocol( String name, Hex pid )
  {
    super( name, pid );
  }
  
  public SpecialProtocolFunction createFunction( KeyMove keyMove )
  {
    return new DSMFunction( keyMove );
  }
  
  public Hex createHex( SpecialFunctionDialog dlg )
  {
    return DSMFunction.createHex( dlg );
  }
  
  public String[] getFunctions(){ return functions; }
  
  private static final String[] functions = { "DSM" };
}
