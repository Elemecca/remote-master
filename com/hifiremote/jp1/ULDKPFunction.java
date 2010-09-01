package com.hifiremote.jp1;

import java.util.Properties;

// TODO: Auto-generated Javadoc
/**
 * The Class ULDKPFunction.
 */
public class ULDKPFunction extends SpecialProtocolFunction
{

  /**
   * Instantiates a new uLDKP function.
   * 
   * @param keyMove
   *          the key move
   */
  public ULDKPFunction( KeyMove keyMove )
  {
    super( keyMove );
  }
  
  public ULDKPFunction( Macro macro )
  {
    super( macro );
  }

  /**
   * Instantiates a new uLDKP function.
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
  public ULDKPFunction( int keyCode, int deviceButtonIndex, int deviceType, int setupCode, Hex cmd, String notes )
  {
    super( keyCode, deviceButtonIndex, deviceType, setupCode, cmd, notes );
  }

  /**
   * Instantiates a new uLDKP function.
   * 
   * @param props
   *          the props
   */
  public ULDKPFunction( Properties props )
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
    return getCmd().getData()[ 0 ] & 0x0f;
  }

  /**
   * Gets the style.
   * 
   * @return the style
   */
  public int getStyle()
  {
    return getCmd().getData()[ 0 ] >> 4;
  }

  /**
   * Gets the first key code.
   * 
   * @return the first key code
   */
  public int getFirstKeyCode()
  {
    return getCmd().getData()[ 1 ];
  }

  /**
   * Gets the second key code.
   * 
   * @return the second key code
   */
  public int getSecondKeyCode()
  {
    return getCmd().getData()[ 2 ];
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.SpecialProtocolFunction#getDisplayType()
   */
  public String getDisplayType( RemoteConfiguration remoteConfig )
  {
    int duration = getDuration();
    int style = getStyle();
    StringBuilder buff = new StringBuilder();
    buff.append( getUserFunctions( remoteConfig )[ style ] );
    if ( style == DSM )
      return buff.toString();

    buff.append( '(' );
    buff.append( Integer.toString( duration ) );
    buff.append( ')' );
    return buff.toString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.SpecialProtocolFunction#getType()
   */
  public String getType( RemoteConfiguration remoteConfig )
  {
    return getUserFunctions( remoteConfig )[ getStyle() ];
//    return typeStrings[ getStyle() ];
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.KeyMove#getValueString(com.hifiremote.jp1.RemoteConfiguration)
   */
  public String getValueString( RemoteConfiguration remoteConfig )
  {
    Remote remote = remoteConfig.getRemote();
    int style = getStyle();
    int macroKey = getFirstKeyCode();
    String keyName = remote.getButtonName( macroKey );
    if ( style == DSM )
      return keyName;

    StringBuilder buff = new StringBuilder();

    buff.append( '[' );
    buff.append( firstStrings[ style ] );
    buff.append( "]:" );
    buff.append( keyName );
    buff.append( " [" );
    buff.append( secondStrings[ style ] );
    buff.append( "]:" );
    buff.append( remote.getButtonName( getSecondKeyCode() ) );

    return buff.toString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.SpecialProtocolFunction#update(com.hifiremote.jp1.SpecialFunctionDialog)
   */
  public void update( SpecialFunctionDialog dlg )
  {
    int style = getStyle();
    int keyCode = getFirstKeyCode();
    if ( style == DSM )
    {
      dlg.setMacroKey( keyCode );
      return;
    }
    dlg.setFirstMacroKey( keyCode );
    dlg.setSecondMacroKey( getSecondKeyCode() );
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
    String type = dlg.getType();
    int style = DSM;
    for ( int i = 0; i < styleStrings.length; ++i )
    {
      if ( styleStrings[ i ].equals( type ) )
      {
        style = i;
        break;
      }
    }

    short[] temp = new short[ 3 ];
    temp[ 0 ] = ( short )( style << 4 );
    if ( style == DSM )
    {
      temp[ 1 ] = ( short )dlg.getMacroKey();
      temp[ 2 ] = ( short )0;
      return new Hex( temp );
    }

    temp[ 0 ] |= ( short )dlg.getULDKPDuration();
    temp[ 1 ] = ( short )dlg.getFirstMacroKey();
    temp[ 2 ] = ( short )dlg.getSecondMacroKey();

    return new Hex( temp );
  }

  /** The DSM. */
  public static int DSM = 0;

  /** The LKP. */
  public static int LKP = 1;

  /** The DKP. */
  public static int DKP = 2;

  /** The Constant typeStrings. */
  public final static String[] typeStrings =
  {
      "UDSM", "ULKP", "UDKP"
  };

  /** The Constant styleStrings. */
  public final static String[] styleStrings =
  {
      "DSM", "LKP", "DKP"
  };

  /** The Constant firstStrings. */
  public final static String[] firstStrings =
  {
      null, "Short", "Single"
  };

  /** The Constant secondStrings. */
  public final static String[] secondStrings =
  {
      null, "Long", "Double"
  };
}
