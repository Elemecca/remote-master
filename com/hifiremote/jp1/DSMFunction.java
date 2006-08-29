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
    StringBuilder buff = new StringBuilder();
    short[] keys = data.getData();
    for ( int i = 0; i < keys.length; ++i )
    {
      if ( i != 0 )
        buff.append( ';' );
      buff.append( remote.getButtonName( keys[ i ]));
    }
    return buff.toString();
  }
  
  public void update( SpecialFunctionDialog dlg )
  {
    short[] keys = data.getData();
    Integer[] temp = new Integer[ keys.length ];
    for ( int i = 0; i < temp.length; ++i )
      temp[ i ] = new Integer( keys[ i ]);
    
    dlg.setFirstMacroButtons( temp );  
  }
  
  public static Hex createHex( SpecialFunctionDialog dlg )
  {
    Integer[] temp = dlg.getFirstMacroButtons();
    short[] data = new short[ temp.length ];
    for ( int i = 0; i < temp.length; ++i )
      data[ i ] = temp[ i ].shortValue();
    return new Hex( data );
  }
}
