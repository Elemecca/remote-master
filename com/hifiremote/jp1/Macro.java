package com.hifiremote.jp1;

import java.util.List;
import java.util.Properties;

import com.hifiremote.jp1.RemoteConfiguration.KeySpec;

// TODO: Auto-generated Javadoc
/**
 * The Class Macro.
 */
public class Macro extends AdvancedCode
{
  
  public Macro( Macro macro )
  {
    this( macro.keyCode, macro.data, macro.deviceButtonIndex, macro.sequenceNumber, macro.notes );
    this.setSegmentFlags( macro.getSegmentFlags() );
  }

  /**
   * Instantiates a new macro.
   * 
   * @param keyCode
   *          the key code
   * @param keyCodes
   *          the key codes
   * @param notes
   *          the notes
   */
  public Macro( int keyCode, Hex keyCodes, String notes )
  {
    super( keyCode, keyCodes, notes );
    deviceButtonIndex = 0x0F;
  }

  public Macro( int keyCode, Hex keyCodes, int deviceButtonIndex, int sequenceNumber, String notes )
  {
    super( keyCode, keyCodes, notes );
    this.deviceButtonIndex = deviceButtonIndex;
    this.sequenceNumber = sequenceNumber;
  }
  
  /**
   * Instantiates a new macro.
   * 
   * @param props
   *          the props
   */
  public Macro( Properties props )
  {
    super( props );
    String temp = props.getProperty( "Name" );
    if ( temp != null )
    {
      name = temp;
    }
    try
    {
      temp = props.getProperty( "SequenceNumber" );
      if ( temp != null )
      {
        sequenceNumber = Integer.parseInt( temp );
      }
      temp = props.getProperty( "DeviceIndex" );
      if ( temp != null )
      {
        deviceButtonIndex = Integer.parseInt( temp );
      }
      else
      {
        deviceButtonIndex = 0x0F;
      }
      temp = props.getProperty( "Serial" );
      if ( temp != null )
      {
        serial = Integer.parseInt( temp );
      }
    }
    catch ( NumberFormatException nfe )
    {
      nfe.printStackTrace( System.err );
    }
  }

  /**
   * Gets the value.
   * 
   * @return the value
   */
  public Object getValue()
  {
    if ( items != null )
    {
      return items;
    }
    return getData();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.AdvancedCode#getValueString(com.hifiremote.jp1.RemoteConfiguration)
   */
  @Override
  public String getValueString( RemoteConfiguration remoteConfig )
  {
    if ( items != null )
    {
      return getValueString( items );
    }
    else
    {
      return getValueString( data, remoteConfig );
    }
  }
  
  public static String getValueString( List< KeySpec > items )
  {
    StringBuilder buff = new StringBuilder();
    for ( int i = 0; i < items.size(); ++i )
    {
      if ( i != 0 )
      {
        buff.append( ';' );
      }
      buff.append( items.get( i ) );
//      KeySpec ks = items.get( i );
//      buff.append( ks.db.getName() + ";" );
//      if ( ks.duration >= 0 )
//      {
//        buff.append( "Hold(" +  ks.duration / 10 + "." + ks.duration % 10 + ");" );
//      }
//      Button btn = ks.fn == null ? ks.btn : ks.fn.getUsers().isEmpty() ? null : ks.fn.getUsers().get( 0 ).button;
//      if ( btn != null )
//      {
//        buff.append( btn.getName() );
//      }
//      else if ( ks.fn != null )
//      {
//        buff.append( "Fn(" + ks.fn.getName() + ")" );
//      }
//      if ( ks.delay != 0 )
//      {
//        buff.append( "(" +  ks.delay / 10 + "." + ks.delay % 10 + ")" );
//      }
    }
    return buff.toString();
  }
  
  public static String getValueString( Hex hex, RemoteConfiguration remoteConfig )
  {
    Remote remote = remoteConfig.getRemote();
    StringBuilder buff = new StringBuilder();
    DeviceButton db = null;
    short[] data = hex.getData();

    for ( int i = 0; i < hex.length(); ++i )
    {
      if ( i != 0 )
      {
        buff.append( ';' );
      }
      int keyCode = data[ i ] & 0xFF;
      DeviceButton temp = remote.getDeviceButton( keyCode );
      if ( temp != null )
      {
        db = temp;
      }
      String name = remote.getButtonName( data[ i ] & 0xFF );
      buff.append( name );
      int duration = ( data[ i ] >> 8 ) & 0xFF;
      if ( duration > 0 )
      {
        if ( duration == 0xFF )
        {
          duration = 0;
        }
        buff.append( "(" +  duration / 10 + "." + duration % 10 + ")" );
      }
    }
    return buff.toString();
  }

  /**
   * Sets the value.
   * 
   * @param value
   *          the new value
   */
  @SuppressWarnings( "unchecked" )
  public void setValue( Object value )
  {
    if ( value instanceof Hex )
    {
      setData( ( Hex )value );
    }
    else if ( value instanceof List< ? >)
    {
      setItems( ( List< KeySpec > )value );
    }
  }

  public int store( short[] buffer, int offset, Remote remote )
  {
    buffer[ offset++ ] = ( short )getKeyCode();
    if ( remote.getAdvCodeBindFormat() == BindFormat.NORMAL )
    {
      buffer[ offset ] = 0x10;
    }
    else if ( remote.getMacroCodingType().getType() == 2 )
    {
      // With deviceIndex $F this allows for MultiMacro types $4, $5, $6, $7 (value in high
      // nibble) for type 2 coding even though no remote yet implements them.  With deviceIndex
      // other than $F these represent internal special protocols.
      buffer[ offset ] = ( short )( ( 0x30 + ( sequenceNumber << 4 ) + deviceButtonIndex ) & 0xFF );
      buffer[ ++offset ] = 0;
    }
    else
    {
      // High nibbles $9, $A, $B, $C, $D correspond to MultiMacros if deviceIndex is $F,
      // for other values of deviceIndex they correspond to internal special protocols.
      buffer[ offset ] = ( short )( ( 0x80 | ( sequenceNumber << 4 ) | deviceButtonIndex ) & 0xFF );
      buffer[ ++offset ] = 0;
    }
    int dataLength = data.length();
    buffer[ offset++ ] |= ( short )dataLength;
    Hex.put( data, buffer, offset );

    return offset + dataLength;
  }

  public void store( PropertyWriter pw )
  {
//    if ( getSegment().getType() == 3 || getSegment().getType() == 0x1E )
    if ( name != null )  // XSight remotes
    {
      int segmentFlags = getSegmentFlags();
      if ( segmentFlags > 0 )
      {
        pw.print( "SegmentFlags", segmentFlags );
      }
      int dataLen = data.length();
      Hex hex = new Hex( 2 * dataLen );
      for ( int i = 0; i < dataLen; i++ )
      {
        int val = data.getData()[ i ];
        hex.set( ( short )( val & 0xFF ), i );
        hex.set( ( short )( ( val >> 8 ) & 0xFF ), dataLen + i );
      }
      pw.print( "Name", name );
      pw.print( "DeviceIndex", deviceButtonIndex );
      pw.print( "KeyCode", keyCode );
      pw.print( "Data", hex );
      pw.print( "Serial", serial );
      if ( notes != null && notes.length() > 0 )
      {
        pw.print( "Notes", notes );
      }
      return;
    }
    
    super.store( pw );
    if ( sequenceNumber != 0 )
    {
      pw.print( "SequenceNumber", sequenceNumber );
    }
    if ( deviceButtonIndex != 0x0F )
    {
      pw.print( "DeviceIndex", deviceButtonIndex );
    }
  }

  private int sequenceNumber = 0;

  public int getSequenceNumber()
  {
    return sequenceNumber;
  }

  public void setSequenceNumber( int sequenceNumber )
  {
    this.sequenceNumber = sequenceNumber;
  }

  public DeviceButton getDeviceButton( RemoteConfiguration config )
  {
    return config.getRemote().getDeviceButton( deviceButtonIndex );
  }

  private short[] durations = null;

  public short[] getDurations()
  {
    return durations;
  }

  public void setDurations( short[] durations )
  {
    this.durations = durations;
  }
  
  private boolean systemMacro = false;

  public boolean isSystemMacro()
  {
    return systemMacro;
  }
  
  public void setSystemMacro( boolean systemMacro )
  {
    this.systemMacro = systemMacro;
  }
  
  private Activity activity = null;
  
  public Activity getActivity()
  {
    return activity;
  }

  public void setActivity( Activity activity )
  {
    this.activity = activity;
  }

  public String toString()
  {
    return name != null && !name.isEmpty() ? name : super.toString();  
  }
  
  protected static MacroCodingType macroCodingType = null;

  public static void setMacroCodingType( MacroCodingType aMacroCodingType )
  {
    macroCodingType = aMacroCodingType;
  }

  public static MacroCodingType getMacroCodingType()
  {
    return macroCodingType;
  }
  
}
