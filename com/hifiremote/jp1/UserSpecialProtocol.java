package com.hifiremote.jp1;

public class UserSpecialProtocol extends SpecialProtocol
{
  public UserSpecialProtocol( String name, Hex pid )
  {
    super( name, pid );
    functions[ 0 ] = name;
  }
  
  @Override
  public SpecialProtocolFunction createFunction( KeyMove keyMove )
  {
    return new UserSpecialFunction( functions[ 0 ], keyMove );
  }

  @Override
  public SpecialProtocolFunction createFunction( Macro macro )
  {
    return new UserSpecialFunction( functions[ 0 ], macro );
  }

  @Override
  public Hex createHex( SpecialFunctionDialog dlg )
  {
    return UserSpecialFunction.createHex( dlg );
  }

  @Override
  public String[] getFunctions()
  {
    return functions;
  }
  
  private String[] functions = new String[ 1 ];

}
