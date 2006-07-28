package com.hifiremote.jp1;

import java.util.Properties;

public class KeyMoveKey
  extends KeyMove
{
  public KeyMoveKey( int keyCode, int deviceButtonIndex, Hex data, String notes )
  {
    super( keyCode, deviceButtonIndex, data, notes );
  }
  
  public KeyMoveKey( int keyCode, int deviceButtonIndex, int deviceType, int setupCode, int movedKeyCode, String notes )
  {
    super( keyCode, deviceButtonIndex, deviceType, setupCode, new Hex( 1 ), notes );
    data.getData()[ 0 ] = ( short )movedKeyCode;
  }
  
  public KeyMoveKey( Properties props )
  {
    super( props );
  }
  
  public Object clone()
  {
    return new KeyMoveKey( getKeyCode(), getDeviceButtonIndex(), getDeviceType(), getSetupCode(), getMovedKeyCode(), getNotes());
  }
  
  public short getMovedKeyCode()
  {
    return data.getData()[ 0 ];
  }

  public EFC getEFC()
  {
    return null;
  }
  
  public void setEFC( EFC efc )
  {}
  
  public Hex getCmd()
  {
    return null;
  }

  
  public void setCmd( Hex cmd )
  {}
  
  public String getValueString( Remote remote )
  {
    return remote.getButtonName( getMovedKeyCode());
  }

  public void setMovedKeyCode( short keyCode )
  {
    data.getData()[ 0 ] = keyCode;
  }
  
  public void setValue( Object value )
  {
    setMovedKeyCode((( Short )value ).shortValue());
  }
}
