package com.hifiremote.jp1;

import java.util.Properties;

// TODO: Auto-generated Javadoc
/**
 * The Class UDSMFunction.
 */
public class UDSMFunction extends SpecialProtocolFunction
{

  /**
   * Instantiates a new uDSM function.
   * 
   * @param keyMove
   *          the key move
   */
  public UDSMFunction( KeyMove keyMove )
  {
    super( keyMove );
  }

  public UDSMFunction( Macro macro )
  {
    super( macro );
  }
  
  /**
   * Instantiates a new uDSM function.
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
  public UDSMFunction( int keyCode, int deviceButtonIndex, int deviceType, int setupCode, Hex cmd, String notes )
  {
    super( keyCode, deviceButtonIndex, deviceType, setupCode, cmd, notes );
  }

  /**
   * Instantiates a new uDSM function.
   * 
   * @param props
   *          the props
   */
  public UDSMFunction( Properties props )
  {
    super( props );
  }

  /**
   * Gets the macro key code.
   * 
   * @return the macro key code
   */
  public int getMacroKeyCode()
  {
    return getCmd().getData()[ 0 ];
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
    int keyCode = getMacroKeyCode();
    buff.append( remote.getButtonName( keyCode ) );
    for ( Macro m : remoteConfig.getMacros() )
    {
      if ( m.getKeyCode() == keyCode )
      {
        buff.append( ": (" );
        buff.append( m.getValueString( remoteConfig ) );
        buff.append( ')' );
        break;
      }
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
    dlg.setMacroKey( getMacroKeyCode() );
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
    short[] hex = new short[ 1 ];
    hex[ 0 ] = ( short )dlg.getMacroKey();
    return new Hex( hex );
  }
}
