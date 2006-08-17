package com.hifiremote.jp1;

import java.util.*;

public class ModeNameFunction
  extends SpecialProtocolFunction
{
  public ModeNameFunction( KeyMove keyMove )
  {
    super( keyMove );
  }
  
  public ModeNameFunction( int keyCode, int deviceButtonIndex, int deviceType, int setupCode, Hex cmd, String notes )
  {
    super( keyCode, deviceButtonIndex, deviceType, setupCode, cmd, notes );
  }    
  
  public ModeNameFunction( Properties props )
  {
    super( props );
  }
  
  public String getType(){ return "ModeName"; }
  public String getDisplayType(){ return "ModeName"; }
  
  public String getValueString( RemoteConfiguration remoteConfig )
  {
    short[] bytes = data.getData();
    char[] chars = new char[ bytes.length + 2 ];
    chars[ 0 ] = '"';
    int i = 0;
    for ( ; i < bytes.length; ++i )
      chars[ i + 1 ] = ( char )bytes[ i ];
    chars[ i + 1 ] = '"';  
      
    return new String( chars );
  }
}
