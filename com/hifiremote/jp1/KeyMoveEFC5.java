package com.hifiremote.jp1;

import java.util.Properties;

// TODO: Auto-generated Javadoc
/**
 * The Class KeyMoveEFC5.
 */
public class KeyMoveEFC5
  extends KeyMove
{
  
  /**
   * Instantiates a new key move ef c5.
   * 
   * @param keyCode the key code
   * @param deviceButtonIndex the device button index
   * @param data the data
   * @param notes the notes
   */
  public KeyMoveEFC5( int keyCode, int deviceButtonIndex, Hex data, String notes )
  {
    super( keyCode, deviceButtonIndex, data, notes );
  }

  /**
   * Instantiates a new key move ef c5.
   * 
   * @param keyCode the key code
   * @param deviceButtonIndex the device button index
   * @param deviceType the device type
   * @param setupCode the setup code
   * @param efc the efc
   * @param notes the notes
   */
  public KeyMoveEFC5( int keyCode, int deviceButtonIndex, int deviceType, int setupCode, int efc, String notes )
  {
    super( keyCode, deviceButtonIndex, deviceType, setupCode, new Hex( 3 ), notes );
    setEFC( efc );
  }
  
  /**
   * Instantiates a new key move ef c5.
   * 
   * @param props the props
   */
  public KeyMoveEFC5( Properties props )
  {
    super( props );
  }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.KeyMove#clone()
   */
  public Object clone()
  {
    return new KeyMoveEFC5( getKeyCode(), getDeviceButtonIndex(), getDeviceType(), getSetupCode(), getEFC5().getValue(), getNotes());
  }
  
  @Override
  public EFC5 getEFC5()
  {
    int efc = data.get( getCmdIndex() == 2 ? 2 : 4 );
    if ( getCmdIndex() == 3 )
    {
      efc += data.getData()[ 3 ] << 16;
    }
    return new EFC5( efc );
  }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.KeyMove#setEFC(com.hifiremote.jp1.EFC)
   */
  public void setEFC( EFC efc )
  {
    setEFC( ( ( EFC5 )efc ).getValue() );
  }
  
  /**
   * Sets the eFC.
   * 
   * @param efc the new eFC
   */
  public void setEFC( int efc )
  {
    // If remote does not have segments, EFC values > 0xFFFF must be stored modulo 0x10000,
    // but for remotes with segments that store EFC values, three bytes are available.
    if ( getCmdIndex() == 3 )
    {
      data.getData()[ 3 ] = ( short )( ( efc >> 16 ) & 0xFF );
    }
    data.put( efc & 0xFFFF, getCmdIndex() == 2 ? 2 : 4 );
  }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.KeyMove#getCmd()
   */
  public Hex getCmd()
  {
    return EFC5.toHex( getEFC5().getValue() );
  }
  
  /* (non-Javadoc)
   * @see com.hifiremote.jp1.KeyMove#setCmd(com.hifiremote.jp1.Hex)
   */
  public void setCmd( Hex hex )
  {
    setEFC( EFC5.parseHex( hex ) );
  }
}
