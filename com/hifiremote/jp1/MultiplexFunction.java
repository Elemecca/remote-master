package com.hifiremote.jp1;

import java.util.*;

public class MultiplexFunction
  extends SpecialProtocolFunction
{
  public MultiplexFunction( KeyMove keyMove )
  {
    super( keyMove );
  }
  
  public MultiplexFunction( int keyCode, int deviceButtonIndex, int deviceType, int setupCode, Hex cmd, String notes )
  {
    super( keyCode, deviceButtonIndex, deviceType, setupCode, cmd, notes );
  }    
  
  public MultiplexFunction( Properties props )
  {
    super( props );
  }
  
  public int getNewDeviceTypeIndex()
  {
    return data.getData()[ 0 ] >> 4;
  }
  
  public int getNewSetupCode()
  {
    return (( data.getData()[ 0 ] & 0x0F ) << 8 ) | data.getData()[ 1 ];
  }
  
  public String getType(){ return "Multiplex"; }
  public String getDisplayType(){ return "Multiplex"; }
  
  public String getValueString( RemoteConfiguration remoteConfig )
  {
    StringBuffer buff = new StringBuffer();
    buff.append( remoteConfig.getRemote().getDeviceTypeByIndex( getNewDeviceTypeIndex()).getName());
    buff.append( ':' );
    buff.append( SetupCode.toString( getNewSetupCode()));
    
    return buff.toString();
  }
}
