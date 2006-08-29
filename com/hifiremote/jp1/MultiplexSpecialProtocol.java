package com.hifiremote.jp1;

public class MultiplexSpecialProtocol extends SpecialProtocol
{
  public MultiplexSpecialProtocol( String name, Hex pid )
  {
    super( name, pid );
  }
  
  public SpecialProtocolFunction createFunction( KeyMove keyMove )
  {
    return new MultiplexFunction( keyMove );
  }
  
  public Hex createHex( SpecialFunctionDialog dlg )
  {
    return MultiplexFunction.createHex( dlg );
  }
  
  public String[] getFunctions(){ return functions; }
  
  private static final String[] functions = { "Multiplex" };
}
