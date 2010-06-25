package com.hifiremote.jp1.translate;

import com.hifiremote.jp1.DeviceParameter;
import com.hifiremote.jp1.Hex;
import com.hifiremote.jp1.Value;

// TODO: Auto-generated Javadoc
/**
 * The Class Pioneer4DevImporter.
 */
public class Pioneer4DevImporter extends Translator
{

  /** The dev index. */
  private int devIndex = 0;

  /** The obc2 index. */
  @SuppressWarnings( "unused" )
  private int obc2Index = 1;

  /**
   * Instantiates a new pioneer4 dev importer.
   * 
   * @param textParms
   *          the text parms
   */
  public Pioneer4DevImporter( String[] textParms )
  {
    super( textParms );
  }

  /**
   * Sets the device bit.
   * 
   * @param obc
   *          the obc
   * @param flag
   *          the flag
   * @return the int
   */
  public int setDeviceBit( int obc, int flag )
  {
    if ( flag != 0 )
    {
      obc |= 0x20;
    }
    else
    {
      obc &= 0xDF;
    }
    return obc;
  }

  /**
   * Sets the hex.
   * 
   * @param obc1
   *          the obc1
   * @param obc2
   *          the obc2
   * @param device
   *          the device
   * @param hex
   *          the hex
   */
  private void setHex( int obc1, int obc2, int device, short[] hex )
  {
    obc1 = setDeviceBit( obc1, device & 2 );
    hex[ 0 ] = ( short )reverse( obc1 );

    obc2 = setDeviceBit( obc2, device & 1 );
    hex[ 1 ] = ( short )reverse( obc2 );
  }

  /**
   * Gets the device.
   * 
   * @param hex
   *          the hex
   * @return the device
   */
  private int getDevice( short[] hex )
  {
    int device = 0;
    if ( ( hex[ 0 ] & 0x04 ) > 0 )
    {
      device += 2;
    }
    if ( ( hex[ 1 ] & 0x04 ) > 0 )
    {
      device += 1;
    }

    return device;
  }

  /**
   * Gets the device.
   * 
   * @param parms
   *          the parms
   * @return the device
   */
  private int getDevice( Value[] parms )
  {
    int device = 0;
    if ( parms[ devIndex ] != null && parms[ devIndex ].getValue() != null )
    {
      device = ( ( Number )parms[ devIndex ].getValue() ).intValue() - 1;
    }
    return device;
  }

  /**
   * Gets the obc.
   * 
   * @param hex
   *          the hex
   * @param index
   *          the index
   * @return the obc
   */
  private int getObc( short[] hex, int index )
  {
    return reverse( hex[ index ] ) & 0xDF;
  }

  /**
   * Gets the obc.
   * 
   * @param parms
   *          the parms
   * @param index
   *          the index
   * @param value
   *          the value
   * @return the obc
   */
  private int getObc( Value[] parms, int index, int value )
  {
    if ( parms[ index ] != null && parms[ index ].getValue() != null )
    {
      System.err.println( "parms[index].getValue() is a " + parms[ index ].getValue().getClass() );
      value = ( ( Number )parms[ index ].getValue() ).intValue();
    }
    return value;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.Translator#in(com.hifiremote.jp1.Value[], com.hifiremote.jp1.Hex,
   * com.hifiremote.jp1.DeviceParameter[], int)
   */
  @Override
  public void in( Value[] parms, Hex hexData, DeviceParameter[] devParms, int onlyIndex )
  {
    short[] hex = hexData.getData();
    if ( onlyIndex == 0 ) // device
    {
      int device = getDevice( parms );
      int obc1 = getObc( hex, 0 );
      setHex( obc1, obc1, device, hex );
    }
    else if ( onlyIndex == 1 ) // obc2
    {
      int device = getDevice( hex );
      int obc1 = getObc( hex, 0 );
      int obc2 = getObc( parms, 1, obc1 );
      setHex( obc1, obc2, device, hex );
    }
    else
    {
      return;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.Translator#out(com.hifiremote.jp1.Hex, com.hifiremote.jp1.Value[],
   * com.hifiremote.jp1.DeviceParameter[])
   */
  @Override
  public void out( Hex hexData, Value[] parms, DeviceParameter[] devParms )
  {}
}
