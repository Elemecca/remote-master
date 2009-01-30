package com.hifiremote.jp1;

import java.util.*;

// TODO: Auto-generated Javadoc
/**
 * The Class CombinerDevice.
 */
public class CombinerDevice
{
  
  /**
   * Instantiates a new combiner device.
   * 
   * @param p the p
   * @param values the values
   * @param notes the notes
   */
  public CombinerDevice( Protocol p, Value[] values, String notes )
  {
    protocol = p;
    if ( values == null )
      values = new Value[ 0 ];
    this.values = values;
    this.notes = notes;
  }

  /**
   * Instantiates a new combiner device.
   * 
   * @param p the p
   * @param values the values
   */
  public CombinerDevice( Protocol p, Value[] values )
  {
    this( p, values, null );
  }

  /**
   * Instantiates a new combiner device.
   * 
   * @param dev the dev
   */
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

  /**
   * Instantiates a new combiner device.
   * 
   * @param text the text
   * @param remote the remote
   */
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

  /**
   * Instantiates a new combiner device.
   * 
   * @param p the p
   * @param fixedData the fixed data
   */
  public CombinerDevice( Protocol p, Hex fixedData )
  {
    protocol = p;
    values = p.importFixedData( fixedData );
  }

  /**
   * Sets the protocol.
   * 
   * @param p the new protocol
   */
  public void setProtocol( Protocol p )
  {
    protocol = p; 
  }
  
  /**
   * Gets the protocol.
   * 
   * @return the protocol
   */
  public Protocol getProtocol(){ return protocol; }
  
  /**
   * Sets the values.
   * 
   * @param values the new values
   */
  public void setValues( Value[] values )
  {
    this.values = values;
  }

  /**
   * Gets the values.
   * 
   * @return the values
   */
  public Value[] getValues(){ return values; }

  /**
   * Gets the fixed data.
   * 
   * @return the fixed data
   */
  public Hex getFixedData()
  {
//    protocol.reset();
//    protocol.setDeviceParms( values );
    return protocol.getFixedData( values );
  }

  /**
   * Gets the notes.
   * 
   * @return the notes
   */
  public String getNotes(){ return notes; }

  /**
   * Sets the notes.
   * 
   * @param text the new notes
   */
  public void setNotes( String text )
  {
    notes = text;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    if (( notes != null ) && ( notes.length() > 0 ))
      return notes;

    StringBuilder buff = new StringBuilder();
    buff.append( protocol.getName());
    if (( values != null ) && ( values.length != 0 ))
    {
      buff.append( ':' );
      buff.append( DeviceUpgrade.valueArrayToString( values ));
    }
    return buff.toString();
  }

  /** The protocol. */
  private Protocol protocol = null;
  
  /** The values. */
  private Value[] values = null;
  
  /** The notes. */
  private String notes = null;
}
