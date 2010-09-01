package com.hifiremote.jp1;

import java.util.Properties;

// TODO: Auto-generated Javadoc
/**
 * The Class ModeNameFunction.
 */
public class ModeNameFunction extends SpecialProtocolFunction
{

  /**
   * Instantiates a new mode name function.
   * 
   * @param keyMove
   *          the key move
   */
  public ModeNameFunction( KeyMove keyMove )
  {
    super( keyMove );
  }

  public ModeNameFunction( Macro macro )
  {
    super( macro );
  }
  /**
   * Instantiates a new mode name function.
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
  public ModeNameFunction( int keyCode, int deviceButtonIndex, int deviceType, int setupCode, Hex cmd, String notes )
  {
    super( keyCode, deviceButtonIndex, deviceType, setupCode, cmd, notes );
  }

  /**
   * Instantiates a new mode name function.
   * 
   * @param props
   *          the props
   */
  public ModeNameFunction( Properties props )
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
    short[] bytes = getCmd().getData();
    char[] chars = new char[ bytes.length + 2 ];
    chars[ 0 ] = '"';
    int i = 0;
    for ( ; i < bytes.length; ++i )
      chars[ i + 1 ] = ( char )bytes[ i ];
    chars[ i + 1 ] = '"';

    return new String( chars );
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.SpecialProtocolFunction#update(com.hifiremote.jp1.SpecialFunctionDialog)
   */
  public void update( SpecialFunctionDialog dlg )
  {
    short[] bytes = getCmd().getData();
    char[] chars = new char[ bytes.length ];
    for ( int i = 0; i < bytes.length; ++i )
      chars[ i ] = ( char )bytes[ i ];

    String text = new String( chars );
    dlg.setModeName( text );

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
    String temp = dlg.getModeName();
    short[] hex = new short[ temp.length() ];
    for ( int i = 0; i < hex.length; ++i )
      hex[ i ] = ( short )temp.charAt( i );
    return new Hex( hex );
  }
}
