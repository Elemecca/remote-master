package com.hifiremote.jp1;

public class ULDKPSpecialProtocol extends SpecialProtocol
{
  public ULDKPSpecialProtocol( String name, Hex pid )
  {
    super( name, pid );
  }
  
  public SpecialProtocolFunction createFunction( KeyMove keyMove )
  {
    return new ULDKPFunction( keyMove );
  }
  
  public Hex createHex( SpecialFunctionDialog dlg )
  {
    return ULDKPFunction.createHex( dlg );
  }
  
  public String[] getFunctions(){ return functions; }
  
  private static final String[] functions = { "UDSM", "UDKP", "ULKP" };
}
