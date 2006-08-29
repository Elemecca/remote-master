package com.hifiremote.jp1;

import java.util.*;

public class ULDKPFunction
  extends SpecialProtocolFunction
{
  public ULDKPFunction( KeyMove keyMove )
  {
    super( keyMove );
  }
  
  public ULDKPFunction( int keyCode, int deviceButtonIndex, int deviceType, int setupCode, Hex cmd, String notes )
  {
    super( keyCode, deviceButtonIndex, deviceType, setupCode, cmd, notes );
  }    
  
  public ULDKPFunction( Properties props )
  {
    super( props );
  }
  
  public int getDuration()
  {
    return data.getData()[ 0 ] & 0x0f;
  }
  
  public int getStyle()
  {
    return data.getData()[ 0 ] >> 4;
  }
  
  public int getFirstKeyCode()
  {
    return data.getData()[ 1 ];
  }
  
  public int getSecondKeyCode()
  {
    return data.getData()[ 2 ];
  }
  
  public String getDisplayType()
  {
    int duration = getDuration();
    int style = getStyle();
    StringBuilder buff = new StringBuilder();
    buff.append( styleStrings[ style ]);
    if ( style == DSM )
      return buff.toString();
    
    buff.append( '(' );
    buff.append( Integer.toString( duration ));
    buff.append( ')' );
    return buff.toString();
  }

  public String getType()
  {
    return typeStrings[ getStyle()];
  }
  
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
    buff.append( firstStrings[ style ]);
    buff.append( "]:" );
    buff.append( keyName );
    buff.append( " [" );
    buff.append( secondStrings[ style ]);
    buff.append( "]:" );
    buff.append( remote.getButtonName( getSecondKeyCode()));
    
    return buff.toString();
  }
  
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
    dlg.setSecondMacroKey( getSecondKeyCode());
  }
  
  public static Hex createHex( SpecialFunctionDialog dlg )
  {
    String type = dlg.getType();
    int style = DSM;
    for ( int i = 0; i < styleStrings.length; ++i )
    {
      if ( styleStrings[ i ].equals( type ))
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
  
  public static int DSM = 0;
  public static int LKP = 1;
  public static int DKP = 2;
  
  public final static String[] typeStrings = 
  {
    "UDSM", "ULKP", "UDKP"
  };
  public final static String[] styleStrings = 
  {
    "DSM", "LKP", "DKP"
  };
  public final static String[] firstStrings =
  {
    null, "Short", "Single"
  };
  public final static String[] secondStrings =
  {
    null, "Long", "Double"
  };
}
