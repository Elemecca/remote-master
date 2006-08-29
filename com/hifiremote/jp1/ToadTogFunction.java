package com.hifiremote.jp1;

import java.util.*;

public class ToadTogFunction
  extends SpecialProtocolFunction
{
  public ToadTogFunction( KeyMove keyMove )
  {
    super( keyMove );
  }
  
  public ToadTogFunction( int keyCode, int deviceButtonIndex, int deviceType, int setupCode, Hex cmd, String notes )
  {
    super( keyCode, deviceButtonIndex, deviceType, setupCode, cmd, notes );
  }    
  
  public ToadTogFunction( Properties props )
  {
    super( props );
  }
  
  public int getToggleNumber()
  {
    return ( data.getData()[ 0 ] & 0x70 ) >> 4;
  }
  
  public int getOnLength()
  {
    return data.getData()[ 0 ]& 0x07;
  }
  
  public int getStyle()
  {
    int val = data.getData()[ 0 ];
    int style = ( val & 0x80 ) >> 6;
    style |= ( val & 0x08 ) >> 3;
    return style;
  }
  
  public String getType(){ return "ToadTog"; }
  
  public String getDisplayType()
  {
    int style = getStyle();
    
    StringBuilder buff = new StringBuilder();
    buff.append( "ToadTog" );
    buff.append( '(' );
    buff.append( Integer.toString( getToggleNumber()));
    buff.append( ',' );
    buff.append( styleStrings[ style ]);
    buff.append( ')' );
    return buff.toString();
  }
  
  public String getValueString( RemoteConfiguration remoteConfig )
  {
    Remote remote = remoteConfig.getRemote();
    int style = getStyle();
    short[] keyCodes = data.getData();
    
    StringBuilder buff = new StringBuilder();
    buff.append( '[' );
    buff.append( onStrings[ style ]);
    buff.append( "]:" );
    if ( getOnLength() == 0)
      buff.append( "<blank>" );
    int i = 0;
    boolean first = true;
    while ( i < getOnLength())
    {
      if ( first )
        first = false;
      else
        buff.append( ';' );
      buff.append( remote.getButtonName( keyCodes[ i + 1 ]));
      ++i;
    }

    buff.append( " [" );
    buff.append( offStrings[ style ]);
    buff.append( "]:" );
    if ( i == keyCodes.length - 1 )
      buff.append( "<blank>" );
    first = false;
    while ( i < keyCodes.length - 1 )
    {
      if ( first )
        first = false;
      else
        buff.append( ';' );
      buff.append( remote.getButtonName( keyCodes[ i + 1 ]));
      ++i;
    }
    
    return buff.toString();
  }

  public void update( SpecialFunctionDialog dlg )
  {
    dlg.setToggle( getToggleNumber());
    dlg.setCondition( getStyle());
    
    short[] keyCodes = data.getData();

    int length = getOnLength();
    Integer[] temp = new Integer[ length ];    
    int offset = 1;
    for ( int i = 0; i < length; ++i )
      temp[ i ] = new Integer( keyCodes[ offset++ ]);
    dlg.setFirstMacroButtons( temp );

    length = keyCodes.length - length - 1;
    temp = new Integer[ length ];    
    for ( int i = 0; i < length; ++i )
      temp[ i ] = new Integer( keyCodes[ offset++ ]);
    dlg.setSecondMacroButtons( temp );
  }
  
  public static Hex createHex( SpecialFunctionDialog dlg )
  {
    int toggle = dlg.getToggle();
    int condition = dlg.getCondition();
    
    Integer[] firstKeyCodes = dlg.getFirstMacroButtons();
    Integer[] secondKeyCodes = dlg.getSecondMacroButtons();
    
    short[] temp = new short[ 1 + firstKeyCodes.length + secondKeyCodes.length ];
    temp[ 0 ] = ( short )((( condition & 2 ) << 6 ) | (( condition & 1 ) << 3 ) |
                          ( toggle << 4 ) | firstKeyCodes.length );
    int offset = 1;
    for ( int i = 0; i < firstKeyCodes.length; ++i )
      temp[ offset++ ] = firstKeyCodes[ i ].shortValue();
    for ( int i = 0; i < secondKeyCodes.length; ++i )
      temp[ offset++ ] = secondKeyCodes[ i ].shortValue();
    
    return new Hex( temp );
  }
  
  public static int TOGGLE = 0;
  public static int FORCE_OFF = 1;
  public static int TEST_ONLY = 2;
  public static int FORCE_ON = 3;
  public static String[] styleStrings = 
  { 
    "Toggle", "ForceOff", "TestOnly", "ForceOn"
  };
  public static String[] onStrings = 
  {
    "On/Off", "On/Off", "On", "Already On"
  };
  public static String[] offStrings = 
  {
    "Off/On", "Already Off", "Off", "On/Off"
  };
}
