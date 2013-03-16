package com.hifiremote.jp1;

import java.util.Properties;

// TODO: Auto-generated Javadoc
/**
 * The Class KeyMoveKey.
 */
public class KeyMoveKey extends KeyMove
{

  /**
   * Instantiates a new key move key.
   * 
   * @param keyCode
   *          the key code
   * @param deviceButtonIndex
   *          the device button index
   * @param data
   *          the data
   * @param notes
   *          the notes
   */
  public KeyMoveKey( int keyCode, int deviceButtonIndex, Hex data, String notes )
  {
    super( keyCode, deviceButtonIndex, data, notes );
  }

  /**
   * Instantiates a new key move key.
   * 
   * @param keyCode
   *          the key code
   * @param deviceButtonIndex
   *          the device button index
   * @param deviceType
   *          the device type
   * @param setupCode
   *          the setup code
   * @param movedKeyCode
   *          the moved key code
   * @param notes
   *          the notes
   */
  public KeyMoveKey( int keyCode, int deviceButtonIndex, int deviceType, int setupCode, int movedKeyCode, String notes )
  {
    super( keyCode, deviceButtonIndex, deviceType, setupCode, new Hex( 1 ), notes );
    setMovedKeyCode( ( short )movedKeyCode );
  }

  /**
   * Instantiates a new key move key.
   * 
   * @param props
   *          the props
   */
  public KeyMoveKey( Properties props )
  {
    super( props );
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.KeyMove#clone()
   */
  @Override
  public Object clone()
  {
    return new KeyMoveKey( getKeyCode(), getDeviceButtonIndex(), getDeviceType(), getSetupCode(), getMovedKeyCode(),
        getNotes() );
  }

  /**
   * Gets the moved key code.
   * 
   * @return the moved key code
   */
  public short getMovedKeyCode()
  {
    return getCmd().getData()[ 0 ];
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.KeyMove#getValueString(com.hifiremote.jp1.RemoteConfiguration)
   */
  @Override
  public String getValueString( RemoteConfiguration remoteConfig )
  {
    return "key: " + ( name != null ? name : remoteConfig.getRemote().getButtonName( getMovedKeyCode() ) );
  }

  /**
   * Sets the moved key code.
   * 
   * @param keyCode
   *          the new moved key code
   */
  public void setMovedKeyCode( short keyCode )
  {
    Hex hex = getCmd();
    hex.set( keyCode, 0 );
    setCmd( hex );
  }

  /**
   * Sets the value.
   * 
   * @param value
   *          the new value
   */
  public void setValue( Object value )
  {
    setMovedKeyCode( ( ( Short )value ).shortValue() );
  }
}
