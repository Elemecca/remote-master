package com.hifiremote.jp1;

import java.util.List;
import java.util.Properties;

// TODO: Auto-generated Javadoc
/**
 * The Class Macro.
 */
public class Macro extends AdvancedCode
{
  
  public Macro( Macro macro )
  {
    this(macro.keyCode, macro.data, macro.deviceIndex, macro.sequenceNumber, macro.notes );
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
  }

  public Macro( int keyCode, Hex keyCodes, int deviceIndex, int sequenceNumber, String notes )
  {
    super( keyCode, keyCodes, notes );
    this.deviceIndex = deviceIndex;
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
        deviceIndex = Integer.parseInt( temp );
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
    return getValueString( data, remoteConfig );
  }
  
  public static String getValueString( Hex hex, RemoteConfiguration remoteConfig )
  {
    Remote remote = remoteConfig.getRemote();
    StringBuilder buff = new StringBuilder();
    DeviceButton db = null;
//    int keyCount = hex.length() / ( remote.usesEZRC() ? 2 : 1 );
//    int keyCount = hex.length();
    short[] data = hex.getData();

//    short[] keys = hex.subHex( 0, keyCount ).getData();
//    short[] durations = hex.subHex( keyCount, keyCount ).getData(); // gives null if no durations
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
      String name = null;
      if ( ( keyCode & remote.getFunctionMask() ) != 0 && keyCode < 0xF0 && db != null && db.getUpgrade() != null )
      {
        DeviceUpgrade upg = db.getUpgrade();
        Function f = upg.getFunction( data[ i ] & 0xFF );
        name = "Fn(" + f.getName() + ")";
      }
      else
      {
        name = remote.getButtonName( data[ i ] & 0xFF );
      }
      buff.append( name );
      int duration = ( data[ i ] >> 8 ) & 0xFF;
      if ( duration > 0 )
      {
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
  public void setValue( Object value )
  {
    setData( ( Hex )value );
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
      buffer[ offset ] = ( short )( ( 0x30 + ( sequenceNumber << 4 ) + deviceIndex ) & 0xFF );
      buffer[ ++offset ] = 0;
    }
    else
    {
      // High nibbles $9, $A, $B, $C, $D correspond to MultiMacros if deviceIndex is $F,
      // for other values of deviceIndex they correspond to internal special protocols.
      buffer[ offset ] = ( short )( ( 0x80 | ( sequenceNumber << 4 ) | deviceIndex ) & 0xFF );
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
      pw.print( "DeviceIndex", deviceIndex );
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
    if ( deviceIndex != 0x0F )
    {
      pw.print( "DeviceIndex", deviceIndex );
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

  private int deviceIndex = 0x0F;

  public int getDeviceIndex()
  {
    return deviceIndex;
  }
  
  public DeviceButton getDeviceButton( RemoteConfiguration config )
  {
    return config.getRemote().getDeviceButton( deviceIndex );
  }

  public void setDeviceIndex( int deviceIndex )
  {
    this.deviceIndex = deviceIndex;
  }
  
  private int serial = 0;
  
  public int getSerial()
  {
    return serial;
  }

  public void setSerial( int serial )
  {
    this.serial = serial;
  }

  private String name = null;

  public String getName()
  {
    return name;
  }

  public void setName( String name )
  {
    this.name = name;
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
  
//  public boolean isSystemMacro( Remote remote )
//  {
////    if ( !remote.isSSD() )
//    {
//      return false;
//    }
////    if ( data == null )
////    {
////      return true;
////    }
////    short[] vals = data.getData();
////    int i = 1;
////    int keyCode = vals[ i++ ] & 0xFF;
////    List< Button > holds = remote.getButtonGroups() == null ? null : remote.getButtonGroups().get( "Hold" );
////    Button btn = remote.getButton( keyCode );
////    if ( holds != null && btn != null && holds.contains( btn ) )
////    {
////      i++;
////    }
////    return data.length() == i;
//  }

  public boolean isSystemMacro()
  {
    return systemMacro;
  }

  public void setSystemMacro( boolean systemMacro )
  {
    this.systemMacro = systemMacro;
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
