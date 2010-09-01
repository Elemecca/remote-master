package com.hifiremote.jp1;

import java.util.Properties;

// TODO: Auto-generated Javadoc
/**
 * The Class DSMFunction.
 */
public class DSMFunction extends SpecialProtocolFunction
{

  /**
   * Instantiates a new dSM function.
   * 
   * @param keyMove
   *          the key move
   */
  public DSMFunction( KeyMove keyMove )
  {
    super( keyMove );
  }
  
  public DSMFunction( Macro macro )
  {
    super( macro );
  }

  /**
   * Instantiates a new dSM function.
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
  public DSMFunction( int keyCode, int deviceButtonIndex, int deviceType, int setupCode, Hex cmd, String notes )
  {
    super( keyCode, deviceButtonIndex, deviceType, setupCode, cmd, notes );
  }

  /**
   * Instantiates a new dSM function.
   * 
   * @param props
   *          the props
   */
  public DSMFunction( Properties props )
  {
    super( props );
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
    Remote remote = remoteConfig.getRemote();
    StringBuilder buff = new StringBuilder();
    short[] keys = getCmd().getData();
    for ( int i = 0; i < keys.length; ++i )
    {
      if ( i != 0 )
        buff.append( ';' );
      buff.append( remote.getButtonName( keys[ i ] ) );
    }
    return buff.toString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.SpecialProtocolFunction#update(com.hifiremote.jp1.SpecialFunctionDialog)
   */
  public void update( SpecialFunctionDialog dlg )
  {
    short[] keys = getCmd().getData();
    Integer[] temp = new Integer[ keys.length ];
    for ( int i = 0; i < temp.length; ++i )
      temp[ i ] = new Integer( keys[ i ] );

    dlg.setFirstMacroButtons( temp );
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
    Integer[] temp = dlg.getFirstMacroButtons();
    short[] data = new short[ temp.length ];
    for ( int i = 0; i < temp.length; ++i )
      data[ i ] = temp[ i ].shortValue();
    return new Hex( data );
  }
}
