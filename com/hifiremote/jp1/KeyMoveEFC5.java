package com.hifiremote.jp1;

import java.util.Properties;

public class KeyMoveEFC5
  extends KeyMove
{
  public KeyMoveEFC5( int keyCode, int deviceButtonIndex, Hex data, String notes )
  {
    super( keyCode, deviceButtonIndex, data, notes );
  }

  public KeyMoveEFC5( int keyCode, int deviceButtonIndex, int deviceType, int setupCode, int efc, String notes )
  {
    super( keyCode, deviceButtonIndex, deviceType, setupCode, new Hex( 2 ), notes );
    setEFC( efc );
  }
  
  public KeyMoveEFC5( Properties props )
  {
    super( props );
  }

  public Object clone()
  {
    return new KeyMoveEFC5( getKeyCode(), getDeviceButtonIndex(), getDeviceType(), getSetupCode(), getEFC().getValue(), getNotes());
  }
  
  public EFC getEFC()
  {
    return new EFC5( data.get( 0 ));
  }

  public void setEFC( EFC efc )
  {
    setEFC((( EFC5 )efc ).getValue());
  }
  
  public void setEFC( int efc )
  {
    data.put( efc, 0 );
  }

  public Hex getCmd()
  {
    return EFC5.toHex( data.get( 0 ));
  }
  
  public void setCmd( Hex hex )
  {
    data.put( EFC5.parseHex( hex ), 0 );
  }
}
