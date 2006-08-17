package com.hifiremote.jp1;

import java.util.*;

public class DSMFunction
  extends SpecialProtocolFunction
{
  public DSMFunction( KeyMove keyMove )
  {
    super( keyMove );
  }
  
  public DSMFunction( int keyCode, int deviceButtonIndex, int deviceType, int setupCode, Hex cmd, String notes )
  {
    super( keyCode, deviceButtonIndex, deviceType, setupCode, cmd, notes );
  }    
  
  public DSMFunction( Properties props )
  {
    super( props );
  }
  
  public String getType(){ return "DSM"; }
  public String getDisplayType(){ return "DSM"; }
  
  public String getValueString( RemoteConfiguration remoteConfig )
  {
    Remote remote = remoteConfig.getRemote();
    StringBuffer buff = new StringBuffer();
    short[] keys = data.getData();
    for ( int i = 0; i < keys.length; ++i )
    {
      if ( i != 0 )
        buff.append( ';' );
      buff.append( remote.getButtonName( keys[ i ]));
    }
    return buff.toString();
  }
}
