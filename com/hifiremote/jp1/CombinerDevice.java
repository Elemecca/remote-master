package com.hifiremote.jp1;

import java.util.*;

public class CombinerDevice
{
  public CombinerDevice( Protocol p, Value[] values, String notes )
  {
    protocol = p;
    if ( values == null )
      values = new Value[ 0 ];
    this.values = values;
    this.notes = notes;
  }

  public CombinerDevice( Protocol p, Value[] values )
  {
    this( p, values, null );
  }

  public CombinerDevice( CombinerDevice dev )
  {
    protocol = dev.protocol;
    if ( protocol.getClass() == ManualProtocol.class )
      protocol = new ManualProtocol(( ManualProtocol )protocol );
    values = new Value[ dev.values.length ];
    for ( int i = 0; i < values.length; i++ )
      values[ i ] = dev.values[ i ];
    notes = dev.notes;
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

  public CombinerDevice( Protocol p, Hex fixedData )
  {
    protocol = p;
    values = p.importFixedData( fixedData );
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
//    protocol.reset();
//    protocol.setDeviceParms( values );
    return protocol.getFixedData( values );
  }

  public String getNotes(){ return notes; }

  public void setNotes( String text )
  {
    notes = text;
  }

  public String toString()
  {
    if (( notes != null ) && ( notes.length() > 0 ))
      return notes;

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
  private String notes = null;
}
