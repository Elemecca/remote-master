package com.hifiremote.jp1;

import java.util.*;

public class LDKPFunction
  extends SpecialProtocolFunction
{
  public LDKPFunction( KeyMove keyMove )
  {
    super( keyMove );
  }
  
  public LDKPFunction( int keyCode, int deviceButtonIndex, int deviceType, int setupCode, Hex cmd, String notes )
  {
    super( keyCode, deviceButtonIndex, deviceType, setupCode, cmd, notes );
  }    
  
  public LDKPFunction( Properties props )
  {
    super( props );
  }
  
  public int getDuration()
  {
    return data.getData()[ 0 ] >> 4;
  }
  
  public int getStyle()
  {
    return ( data.getData()[ 0 ] & 8 ) >> 3;
  }
  
  public int getFirstLength()
  {
    return data.getData()[ 0 ] & 7;
  }
  
  public String getType()
  {
    return styleStrings[ getStyle() ];
  }
  
  public String getDisplayType()
  {
    short val = data.getData()[ 0 ];
    int duration = getDuration();
    int style = getStyle();
    StringBuilder buff = new StringBuilder();
    buff.append( styleStrings[ style ]);
    buff.append( '(' );
    buff.append( Integer.toString( getDuration()));
    buff.append( ')' );
    return buff.toString();
  }
  
  public String getValueString( RemoteConfiguration remoteConfig )
  {
    Remote remote = remoteConfig.getRemote();
    StringBuilder buff = new StringBuilder();
    short[] vals = data.getData();
    int style = getStyle();
    buff.append( '[' );
    buff.append( firstStrings[ style ]);
    buff.append( "]:" );
    int firstLength = getFirstLength();
    int i = 0;
    for ( ; i < firstLength; ++i )
    {
      if ( i != 0 )
        buff.append( ';' );
      buff.append( remote.getButtonName( vals[ i + 1 ]));
    }
    buff.append( " [" );
    buff.append( secondStrings[ style ]);
    buff.append( "]:" );
    for ( ; i + 1 < vals.length; ++i )
    {
      if ( i != firstLength )
        buff.append( ';' );
      buff.append( remote.getButtonName( vals[ i + 1 ]));
    }
    
    return buff.toString();
  }
  
  public void update( SpecialFunctionDialog dlg )
  {
    dlg.setDuration( getDuration());
    short[] vals = data.getData();
    int firstLength = vals[ 0 ] & 7;
    int secondLength = vals.length - firstLength - 1;
    int offset = 1;
    
    Integer[] temp = new Integer[ firstLength ];
    for ( int i = 0; i < firstLength; ++i )
      temp[ i ] = new Integer( vals[ offset++ ]);
    dlg.setFirstMacroButtons( temp );

    temp = new Integer[ secondLength ];
    for ( int i = 0; i < secondLength; ++i )
      temp[ i ] = new Integer( vals[ offset++ ]);
    dlg.setSecondMacroButtons( temp );
  }
  
  public static Hex createHex( SpecialFunctionDialog dlg )
  {
    String type = dlg.getType();
    int style = LKP;
    for ( int i = 0; i < styleStrings.length; ++i )
    {
      if ( styleStrings[ i ].equals( type ))
      {
        style = i;
        break;
      }
    }
    
    Integer[] firstKeyCodes = dlg.getFirstMacroButtons();
    Integer[] secondKeyCodes = dlg.getSecondMacroButtons();
    
    short[] temp = new short[ 1 + firstKeyCodes.length + secondKeyCodes.length ];
    temp[ 0 ] = ( short )(( dlg.getDuration() << 4 ) | ( style << 3 ) | firstKeyCodes.length );
    int offset = 1;
    for ( int i = 0; i < firstKeyCodes.length; ++i )
      temp[ offset++ ] = firstKeyCodes[ i ].shortValue();
    for ( int i = 0; i < secondKeyCodes.length; ++i )
      temp[ offset++ ] = secondKeyCodes[ i ].shortValue();
    
    return new Hex( temp );
  }
  
  public static int LKP = 0;
  public static int DKP = 1;
  
  public static String[] styleStrings = 
  { 
    "LKP", "DKP"
  };
  public static String[] firstStrings = 
  {
    "Short", "Single"
  };
  public static String[] secondStrings = 
  {
    "Long", "Double"
  };
}
