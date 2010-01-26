package com.hifiremote.jp1;

import java.util.Properties;

// TODO: Auto-generated Javadoc
/**
 * The Class Macro.
 */
public class Macro extends AdvancedCode
{

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

  /**
   * Instantiates a new macro.
   * 
   * @param props
   *          the props
   */
  public Macro( Properties props )
  {
    super( props );
    String temp = props.getProperty( "SequenceNumber" );
    if ( temp != null )
    {
      try
      {
        sequenceNumber = Integer.parseInt( temp );
      }
      catch ( NumberFormatException nfe )
      {
        nfe.printStackTrace( System.err );
      }
    }
    temp = props.getProperty( "DeviceIndex" );
    if ( temp != null )
    {
      try
      {
        deviceIndex = Integer.parseInt( temp );
      }
      catch ( NumberFormatException nfe )
      {
        nfe.printStackTrace( System.err );
      }
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
  public String getValueString( RemoteConfiguration remoteConfig )
  {
    Remote remote = remoteConfig.getRemote();
    StringBuilder buff = new StringBuilder();
    short[] keys = data.getData();
    for ( int i = 0; i < keys.length; ++i )
    {
      if ( i != 0 )
        buff.append( ';' );
      if ( keys[ i ] == 0 )
      {
        buff.append( "{Pause}" );
        while ( ( i < keys.length ) && ( keys[ i ] == 0 ) )
          ++i;
      }
      else
        buff.append( remote.getButtonName( keys[ i ] ) );
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
      buffer[ offset ] = 0x3F;
      buffer[ ++offset ] = 0;
    }
    else
    {
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
    super.store( pw );
    if ( sequenceNumber != 0 )
    {
      pw.print( "SequenceNumber", sequenceNumber );
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

  public void setDeviceIndex( int deviceIndex )
  {
    this.deviceIndex = deviceIndex;
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
