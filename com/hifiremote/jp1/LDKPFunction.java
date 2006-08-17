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
  
  public int getFistLength()
  {
    return data.getData()[ 0 ] & 7;
  }
  
  public String getType()
  {
    short val = data.getData()[ 0 ];
    int duration = val >> 4;
    int style = ( val & 8 ) >> 3;
    if ( style == LKP )
      return( "LKP" );
    else
      return( "DKP" );
  }
  
  public String getDisplayType()
  {
    short val = data.getData()[ 0 ];
    int duration = val >> 4;
    int style = ( val & 8 ) >> 3;
    StringBuffer buff = new StringBuffer();
    if ( style == LKP )
      buff.append( "LKP(" );
    else
      buff.append( "DKP(" );
    buff.append( Integer.toString( duration ));
    buff.append( ')' );
    return buff.toString();
  }
  
  public String getValueString( RemoteConfiguration remoteConfig )
  {
    Remote remote = remoteConfig.getRemote();
    StringBuffer buff = new StringBuffer();
    short[] vals = data.getData();
    int style = ( vals[ 0 ] & 8 ) >> 3;
    if ( style == LKP )
      buff.append( "[Short]:" );
    else
      buff.append( "[Single]:" );
    int firstLength = vals[ 0 ] & 7;
    int i = 0;
    for ( ; i < firstLength; ++i )
    {
      if ( i != 0 )
        buff.append( ';' );
      buff.append( remote.getButtonName( vals[ i + 1 ]));
    }
    buff.append( ' ' );
    if ( style == LKP )
      buff.append( "[Long]:" );
    else
      buff.append( "[Double]:" );
    for ( ; i + 1 < vals.length; ++i )
    {
      if ( i != firstLength )
        buff.append( ';' );
      buff.append( remote.getButtonName( vals[ i + 1 ]));
    }
    
    return buff.toString();
  }
  
  public static int LKP = 0;
  public static int DKP = 1;
}
