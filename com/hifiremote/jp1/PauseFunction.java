package com.hifiremote.jp1;

import java.util.Properties;

// TODO: Auto-generated Javadoc
/**
 * The Class PauseFunction.
 */
public class PauseFunction extends SpecialProtocolFunction
{

  /**
   * Instantiates a new pause function.
   * 
   * @param keyMove
   *          the key move
   */
  public PauseFunction( KeyMove keyMove )
  {
    super( keyMove );
  }
  
  public PauseFunction( Macro macro )
  {
    super( macro );
  }

  /**
   * Instantiates a new pause function.
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
  public PauseFunction( int keyCode, int deviceButtonIndex, int deviceType, int setupCode, Hex cmd, String notes )
  {
    super( keyCode, deviceButtonIndex, deviceType, setupCode, cmd, notes );
  }

  /**
   * Instantiates a new pause function.
   * 
   * @param props
   *          the props
   */
  public PauseFunction( Properties props )
  {
    super( props );
  }

  /**
   * Gets the duration.
   * 
   * @return the duration
   */
  public int getDuration()
  {
    return getCmd().getData()[ 0 ];
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.SpecialProtocolFunction#getType()
   */
  public String getType()
  {
    return "Pause";
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
    /*
     * StringBuilder buff = new StringBuilder(); buff.append( Integer.toString( getDuration())); buff.append( " ($" );
     * buff.append( data.toString()); buff.append( ')' ); return buff.toString();
     */
    return Integer.toString( getDuration() );
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.SpecialProtocolFunction#update(com.hifiremote.jp1.SpecialFunctionDialog)
   */
  public void update( SpecialFunctionDialog dlg )
  {
    dlg.setDuration( getDuration() );
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
    hex[ 0 ] = ( short )dlg.getDuration();
    return new Hex( hex );
  }
}
