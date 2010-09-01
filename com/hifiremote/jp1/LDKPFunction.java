package com.hifiremote.jp1;

import java.util.Properties;

// TODO: Auto-generated Javadoc
/**
 * Description of the Class.
 * 
 * @author Greg
 * @created December 2, 2006
 */
public class LDKPFunction extends SpecialProtocolFunction
{

  /**
   * Constructor for the LDKPFunction object.
   * 
   * @param keyMove
   *          the key move
   */
  public LDKPFunction( KeyMove keyMove )
  {
    super( keyMove );
  }
  
  public LDKPFunction( Macro macro )
  {
    super( macro );
  }

  /**
   * Constructor for the LDKPFunction object.
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
  public LDKPFunction( int keyCode, int deviceButtonIndex, int deviceType, int setupCode, Hex cmd, String notes )
  {
    super( keyCode, deviceButtonIndex, deviceType, setupCode, cmd, notes );
  }

  /**
   * Constructor for the LDKPFunction object.
   * 
   * @param props
   *          the props
   */
  public LDKPFunction( Properties props )
  {
    super( props );
  }

  /**
   * Gets the duration attribute of the LDKPFunction object.
   * 
   * @return The duration value
   */
  public int getDuration()
  {
    return getCmd().getData()[ 0 ] >> 4;
  }

  /**
   * Gets the style attribute of the LDKPFunction object.
   * 
   * @return The style value
   */
  public int getStyle()
  {
    return ( getCmd().getData()[ 0 ] & 8 ) >> 3;
  }

  /**
   * Gets the firstLength attribute of the LDKPFunction object.
   * 
   * @return The firstLength value
   */
  public int getFirstLength()
  {
    return getCmd().getData()[ 0 ] & 7;
  }

  /**
   * Gets the type attribute of the LDKPFunction object.
   * 
   * @return The type value
   */
  public String getType( RemoteConfiguration remoteConfig )
  {
    return getUserFunctions( remoteConfig )[ getStyle() ];
//    return styleStrings[ getStyle() ];
  }

  /**
   * Gets the displayType attribute of the LDKPFunction object.
   * 
   * @return The displayType value
   */
  public String getDisplayType( RemoteConfiguration remoteConfig )
  {
    int style = getStyle();
    StringBuilder buff = new StringBuilder();
    buff.append( getUserFunctions( remoteConfig )[ style ] );
    buff.append( '(' );
    buff.append( Integer.toString( getDuration() ) );
    buff.append( ')' );
    return buff.toString();
  }

  /**
   * Gets the valueString attribute of the LDKPFunction object.
   * 
   * @param remoteConfig
   *          the remote config
   * @return The valueString value
   */
  public String getValueString( RemoteConfiguration remoteConfig )
  {
    Remote remote = remoteConfig.getRemote();
    StringBuilder buff = new StringBuilder();
    short[] vals = getCmd().getData();
    int style = getStyle();
    buff.append( '[' );
    buff.append( firstStrings[ style ] );
    buff.append( "]:" );
    int firstLength = getFirstLength();
    if ( firstLength == 0 )
      buff.append( "<none>" );
    int i = 0;
    for ( ; i < firstLength; ++i )
    {
      if ( i != 0 )
        buff.append( ';' );
      buff.append( remote.getButtonName( vals[ i + 1 ] ) );
    }
    buff.append( " [" );
    buff.append( secondStrings[ style ] );
    buff.append( "]:" );
    if ( i == ( vals.length - 1 ) )
      buff.append( "<none>" );
    for ( ; i + 1 < vals.length; ++i )
    {
      if ( i != firstLength )
        buff.append( ';' );
      buff.append( remote.getButtonName( vals[ i + 1 ] ) );
    }

    return buff.toString();
  }

  /**
   * Description of the Method.
   * 
   * @param dlg
   *          the dlg
   */
  public void update( SpecialFunctionDialog dlg )
  {
    dlg.setDuration( getDuration() );
    short[] vals = getCmd().getData();
    int firstLength = vals[ 0 ] & 7;
    int secondLength = vals.length - firstLength - 1;
    int offset = 1;

    Integer[] temp = new Integer[ firstLength ];
    for ( int i = 0; i < firstLength; ++i )
      temp[ i ] = new Integer( vals[ offset++ ] );
    dlg.setFirstMacroButtons( temp );

    temp = new Integer[ secondLength ];
    for ( int i = 0; i < secondLength; ++i )
      temp[ i ] = new Integer( vals[ offset++ ] );
    dlg.setSecondMacroButtons( temp );
  }

  /**
   * Description of the Method.
   * 
   * @param dlg
   *          the dlg
   * @return Description of the Return Value
   */
  public static Hex createHex( SpecialFunctionDialog dlg )
  {
    String type = dlg.getType();
    int style = LKP;
    for ( int i = 0; i < styleStrings.length; ++i )
      if ( styleStrings[ i ].equals( type ) )
      {
        style = i;
        break;
      }

    Integer[] firstKeyCodes = dlg.getFirstMacroButtons();
    Integer[] secondKeyCodes = dlg.getSecondMacroButtons();

    short[] temp = new short[ 1 + firstKeyCodes.length + secondKeyCodes.length ];
    temp[ 0 ] = ( short )( ( dlg.getDuration() << 4 ) | ( style << 3 ) | firstKeyCodes.length );
    int offset = 1;
    for ( int i = 0; i < firstKeyCodes.length; ++i )
      temp[ offset++ ] = firstKeyCodes[ i ].shortValue();
    for ( int i = 0; i < secondKeyCodes.length; ++i )
      temp[ offset++ ] = secondKeyCodes[ i ].shortValue();

    return new Hex( temp );
  }

  /** Description of the Field. */
  public static int LKP = 0;

  /** Description of the Field. */
  public static int DKP = 1;

  /** Description of the Field. */
  public static String[] styleStrings =
  {
      "LKP", "DKP"
  };

  /** Description of the Field. */
  public static String[] firstStrings =
  {
      "Short", "Single"
  };

  /** Description of the Field. */
  public static String[] secondStrings =
  {
      "Long", "Double"
  };
}
