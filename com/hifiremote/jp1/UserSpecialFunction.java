package com.hifiremote.jp1;

import java.util.Properties;

public class UserSpecialFunction extends SpecialProtocolFunction
{
  public UserSpecialFunction( String type, KeyMove keyMove )
  {
    super( keyMove );
    this.type = type;
  }

  public UserSpecialFunction( String type, Macro macro )
  {
    super( macro );
    this.type = type;
  }
  
  public UserSpecialFunction( Properties props )
  {
    super( props );
    type = props.getProperty( "Function" );
  }

  @Override
  public void update( SpecialFunctionDialog dlg )
  {
    dlg.setHexField( getCmd() );
  }

  @Override
  public String get_Type( RemoteConfiguration remoteConfig )
  {
    return type;
  }

  @Override
  public String getDisplayType( RemoteConfiguration remoteConfig )
  {
    return getUserFunctions( remoteConfig )[ 0 ];
  }
  
  @Override
  public String getValueString( RemoteConfiguration remoteConfig )
  {
    return "Hex: " + getCmd();
  }
  
  public String get_Type()
  {
    return type;
  }
  
  public static Hex createHex( SpecialFunctionDialog dlg )
  {
    String val = dlg.getHexString();
    return new Hex( val );
  }
  
  String type = null;

}
