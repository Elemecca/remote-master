package com.hifiremote.jp1;

public class LDKPSpecialProtocol extends SpecialProtocol
{
  public LDKPSpecialProtocol( String name, Hex pid )
  {
    super( name, pid );
  }
  
  public SpecialProtocolFunction createFunction( KeyMove keyMove )
  {
    return new LDKPFunction( keyMove );
  }
  
  public Hex createHex( SpecialFunctionDialog dlg )
  {
    return LDKPFunction.createHex( dlg );
  }
  
  public String[] getFunctions(){ return functions; }
  
  private static final String[] functions = { "DKP", "LKP" };
}
