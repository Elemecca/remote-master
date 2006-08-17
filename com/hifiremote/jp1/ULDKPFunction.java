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
    StringBuffer buff = new StringBuffer();
    if( style == DSM )
      buff.append( "DSM" );
    else
    {
      if ( style == LKP )
        buff.append( "LKP(" );
      else
        buff.append( "DKP(" );
      buff.append( Integer.toString( duration ));
      buff.append( ')' );
    }
    return buff.toString();
  }

  public String getType()
  {
    int style = getStyle();
    if( style == DSM )
      return "UDSM";
    if ( style == LKP )
      return "ULKP";
    return( "UDKP" );
  }
  
  public String getValueString( RemoteConfiguration remoteConfig )
  {
    Remote remote = remoteConfig.getRemote();
    int style = getStyle();
    int macroKey = getFirstKeyCode();
    String keyName = remote.getButtonName( macroKey );
    if ( style == DSM )
      return keyName;
    
    StringBuffer buff = new StringBuffer();
    
    if ( style == LKP )
      buff.append( "[Short]:" );
    else
      buff.append( "[Single]:" );
    buff.append( keyName );
    buff.append( ' ' );
    if ( style == LKP )
      buff.append( "[Long]:" );
    else
      buff.append( "[Double]:" );
    buff.append( remote.getButtonName( getSecondKeyCode()));
    
    return buff.toString();
  }
  
  public static int DSM = 0;
  public static int LKP = 1;
  public static int DKP = 2;
}
