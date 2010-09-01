package com.hifiremote.jp1;

import java.util.Properties;

// TODO: Auto-generated Javadoc
/**
 * The Class MultiplexFunction.
 */
public class MultiplexFunction extends SpecialProtocolFunction
{

  /**
   * Instantiates a new multiplex function.
   * 
   * @param keyMove
   *          the key move
   */
  public MultiplexFunction( KeyMove keyMove )
  {
    super( keyMove );
  }
  
  public MultiplexFunction( Macro macro )
  {
    super( macro );
  }

  /**
   * Instantiates a new multiplex function.
   * 
   * @param keyCode
   *          the key code
   * @param deviceButtonIndex
   *          the device button index
   * @param deviceType
   *          the device type
   * @param setupCode
   *          the setup code
   * @param cmd
   *          the cmd
   * @param notes
   *          the notes
   */
  public MultiplexFunction( int keyCode, int deviceButtonIndex, int deviceType, int setupCode, Hex cmd, String notes )
  {
    super( keyCode, deviceButtonIndex, deviceType, setupCode, cmd, notes );
  }

  /**
   * Instantiates a new multiplex function.
   * 
   * @param props
   *          the props
   */
  public MultiplexFunction( Properties props )
  {
    super( props );
  }

  /**
   * Gets the new device type index.
   * 
   * @return the new device type index
   */
  public int getNewDeviceTypeIndex()
  {
    return getCmd().getData()[ 0 ] >> 4;
  }

  /**
   * Gets the new setup code.
   * 
   * @return the new setup code
   */
  public int getNewSetupCode()
  {
    short[] hex = getCmd().getData();
    return ( ( hex[ 0 ] & 0x0F ) << 8 ) | hex[ 1 ];
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.SpecialProtocolFunction#getType()
   */
  public String getType( RemoteConfiguration remoteConfig )
  {
    return getUserFunctions( remoteConfig )[ 0 ];
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.SpecialProtocolFunction#getDisplayType()
   */
  public String getDisplayType( RemoteConfiguration remoteConfig )
  {
    return getUserFunctions( remoteConfig )[0];
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.KeyMove#getValueString(com.hifiremote.jp1.RemoteConfiguration)
   */
  public String getValueString( RemoteConfiguration remoteConfig )
  {
    StringBuilder buff = new StringBuilder();
    buff.append( remoteConfig.getRemote().getDeviceTypeByIndex( getNewDeviceTypeIndex() ).getName() );
    buff.append( ':' );
    buff.append( SetupCode.toString( getNewSetupCode() ) );

    return buff.toString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.SpecialProtocolFunction#update(com.hifiremote.jp1.SpecialFunctionDialog)
   */
  public void update( SpecialFunctionDialog dlg )
  {
    dlg.setDeviceType( getNewDeviceTypeIndex() );
    dlg.setSetupCode( getNewSetupCode() );
  }

  /**
   * Creates the hex.
   * 
   * @param dlg
   *          the dlg
   * @return the hex
   */
  public static Hex createHex( SpecialFunctionDialog dlg )
  {
    short[] hex = new short[ 2 ];
    int setupCode = dlg.getSetupCode();
    hex[ 0 ] = ( short )( ( dlg.getDeviceType() << 4 ) | ( setupCode >> 8 ) );
    hex[ 1 ] = ( short )( setupCode & 0xFF );
    return new Hex( hex );
  }
}
