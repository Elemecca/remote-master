package com.hifiremote.jp1;

import java.util.*;

public class UDSMFunction
  extends SpecialProtocolFunction
{
  public UDSMFunction( KeyMove keyMove )
  {
    super( keyMove );
  }
  
  public UDSMFunction( int keyCode, int deviceButtonIndex, int deviceType, int setupCode, Hex cmd, String notes )
  {
    super( keyCode, deviceButtonIndex, deviceType, setupCode, cmd, notes );
  }    
  
  public UDSMFunction( Properties props )
  {
    super( props );
  }

  public int getMacroKeyCode()
  {
    return data.getData()[ 0 ];
  }
  
  public String getType(){ return "UDSM"; }
  public String getDisplayType(){ return "DSM"; }
  
  public String getValueString( RemoteConfiguration remoteConfig )
  {
    Remote remote = remoteConfig.getRemote();
    StringBuffer buff = new StringBuffer();
    int keyCode = getMacroKeyCode();
    buff.append( remote.getButton( keyCode ));
    for ( Macro m : remoteConfig.getMacros())
    {
      if ( m.getKeyCode() == keyCode )
      {
        buff.append( ": (" );
        buff.append( m.getValueString( remoteConfig ));
        buff.append( ')' );
        break;
      }
    }
    return buff.toString();
  }
}
