package com.hifiremote.jp1;

import java.util.Properties;

public class Macro
  extends AdvancedCode
{
  public Macro( int keyCode, Hex keyCodes, String notes )
  {
    super( keyCode, keyCodes, notes );
  }
  
  public Macro( Properties props )
  {
    super( props );
  }

  public Object getValue()
  {
    return getData();
  }
  
  public String getValueString( RemoteConfiguration remoteConfig )
  {
    Remote remote = remoteConfig.getRemote();
    StringBuffer buff = new StringBuffer();
    short[] keys = data.getData();
    for ( int i = 0; i < keys.length; ++i )
    {
      if ( i != 0 )
        buff.append( ';' );
      buff.append( remote.getButtonName( keys[ i ]));
    }
    return buff.toString();
  }

  public void setValue( Object value )
  {
    setData(( Hex )value );
  }
}
