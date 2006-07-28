package com.hifiremote.jp1;

import java.util.Properties;

public class KeyMoveEFC
  extends KeyMove
{
  public KeyMoveEFC( int keyCode, int deviceButtonIndex, Hex data, String notes )
  {
    super( keyCode, deviceButtonIndex, data, notes );
  }
  
  public KeyMoveEFC( int keyCode, int deviceButtonIndex, int deviceType, int setupCode, int efc, String notes )
  {
    super( keyCode, deviceButtonIndex, deviceType, setupCode, new Hex( 2 ), notes );
    setEFC( new EFC(( short )efc ));
  }

  public KeyMoveEFC( Properties props )
  {
    super( props );
  }
  
  public Object clone()
  {
    return new KeyMoveEFC( getKeyCode(), getDeviceButtonIndex(), getDeviceType(), getSetupCode(), getEFC().getValue(), getNotes());
  }
  
  public EFC getEFC()
  {
    return new EFC(( short )data.get( 0 ));
  }

  public void setEFC( EFC value )
  {
    data.put( value.getValue(), 0 );
  }
  
  public Hex getCmd()
  {
    return EFC.toHex( data.get( 0 ));
  }
  
  public void setCmd( Hex hex )
  {
    data.put( EFC.parseHex( hex ), 0 );
  }
}
