package com.hifiremote.jp1;

import java.util.*;

public class CombinerDevice
{
  public CombinerDevice( Protocol p, Value[] values )
  {
    protocol = p;
    this.values = values;
  }

  public CombinerDevice( String text, Remote remote )
  { 
    StringTokenizer st = new StringTokenizer( text , ":." );
    String token = st.nextToken();
    protocol = 
      ProtocolManager.getProtocolManager().findProtocolForRemote( remote, token );

    int count = st.countTokens();
    values = new Value[ count ];
    DeviceParameter[] parms = protocol.getDeviceParameters();
    for ( int i = 0; i < count; i++ )
    {
       parms[ i ].setValue( st.nextToken());
    }
    values = protocol.getDeviceParmValues();
  }

  public void setProtocol( Protocol p )
  {
    protocol = p; 
  }
  
  public Protocol getProtocol(){ return protocol; }
  
  public void setValues( Value[] values )
  {
    this.values = values;
  }

  public Value[] getValues(){ return values; }

  public Hex getFixedData()
  {
    protocol.setDeviceParms( values );
    return protocol.getFixedData();
  }

  public String toString()
  {
    StringBuffer buff = new StringBuffer();
    buff.append( protocol.getName());
    if (( values != null ) && ( values.length != 0 ))
    {
      buff.append( ':' );
      buff.append( DeviceUpgrade.valueArrayToString( values ));
    }
    return buff.toString();
  }

  private Protocol protocol = null;
  private Value[] values = null;
}
