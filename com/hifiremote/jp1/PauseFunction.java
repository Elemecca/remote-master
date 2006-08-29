package com.hifiremote.jp1;

import java.util.*;

public class PauseFunction
  extends SpecialProtocolFunction
{
  public PauseFunction( KeyMove keyMove )
  {
    super( keyMove );
  }
  
  public PauseFunction( int keyCode, int deviceButtonIndex, int deviceType, int setupCode, Hex cmd, String notes )
  {
    super( keyCode, deviceButtonIndex, deviceType, setupCode, cmd, notes );
  }    
  
  public PauseFunction( Properties props )
  {
    super( props );
  }

  public int getDuration()
  {
    return data.getData()[ 0 ];
  }
  
  public String getType(){ return "Pause"; }
  public String getDisplayType(){ return "Pause"; }
  
  public String getValueString( RemoteConfiguration remoteConfig )
  {
    /*
    StringBuilder buff = new StringBuilder();
    buff.append( Integer.toString( getDuration()));
    buff.append( " ($" );
    buff.append( data.toString());
    buff.append( ')' );
    return buff.toString();
    */
    return Integer.toString( getDuration());
  }
  
  public void update( SpecialFunctionDialog dlg )
  {
    dlg.setDuration( getDuration());
  }
  
  public static Hex createHex( SpecialFunctionDialog dlg )
  {
    short[] hex = new short[ 1 ];
    hex[ 0 ] = ( short )dlg.getDuration();
    return new Hex( hex );
  }
}
