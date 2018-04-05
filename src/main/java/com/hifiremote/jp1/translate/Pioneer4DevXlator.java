package com.hifiremote.jp1.translate;

import com.hifiremote.jp1.DeviceParameter;
import com.hifiremote.jp1.Hex;
import com.hifiremote.jp1.Value;

// TODO: Auto-generated Javadoc
/**
 * The Class Pioneer4DevXlator.
 */
public class Pioneer4DevXlator extends Translator
{

  /** The dev index. */
  private int devIndex = 0;

  /** The obc index. */
  private int obcIndex = 0;

  /** The obc2 index. */
  private int obc2Index = 0;

  /**
   * Instantiates a new pioneer4 dev xlator.
   * 
   * @param textParms
   *          the text parms
   */
  public Pioneer4DevXlator( String[] textParms )
  {
    super( textParms );
    devIndex = Integer.parseInt( textParms[ 0 ] );
    if ( devIndex == 0 )
    {
      obcIndex = 1;
      obc2Index = 2;
    }
    else if ( devIndex == 1 )
    {
      obcIndex = 0;
      obc2Index = 2;
    }
    else
    {
      obcIndex = 0;
      obc2Index = 1;
    }
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
      device = ( ( Number )parms[ devIndex ].getValue() ).intValue();
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
  private short getObc( short[] hex, int index )
  {
    return ( short )( reverse( hex[ index ] ) & 0xDF );
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
  private short getObc( Value[] parms, int index, int value )
  {
    int i;
    if ( index == 0 )
    {
      i = obcIndex;
    }
    else
    {
      i = obc2Index;
    }

    if ( parms[ i ] != null && parms[ i ].getValue() != null )
    {
      System.err.println( "parms[i].getValue() is a " + parms[ i ].getValue().getClass() );
      value = ( ( Number )parms[ i ].getValue() ).intValue();
    }
    return ( short )value;
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
    if ( onlyIndex == devIndex )
    {
      int device = getDevice( parms );
      int obc1 = getObc( hex, 0 );
      int obc2 = getObc( hex, 1 );
      setHex( obc1, obc2, device, hex );
    }
    else if ( onlyIndex == obcIndex )
    {
      int device = getDevice( hex );
      int obc1 = getObc( parms, 0, 0 );
      int oldObc = getObc( hex, 0 );
      int obc2 = getObc( hex, 1 );
      if ( obc2 == oldObc )
      {
        obc2 = obc1;
      }
      setHex( obc1, obc2, device, hex );
    }
    else if ( onlyIndex == obc2Index )
    {
      int device = getDevice( hex );
      int obc1 = getObc( hex, 0 );
      int obc2 = getObc( parms, 1, obc1 );
      setHex( obc1, obc2, device, hex );
    }
    else
    // ALL
    {
      System.err.println( "onlyIndex=" + onlyIndex );
      int device = getDevice( parms );
      int obc1 = getObc( parms, 0, 0 );
      int obc2 = getObc( parms, 1, obc1 );
      setHex( obc1, obc2, device, hex );
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
  {
    short[] hex = hexData.getData();
    int obc1 = getObc( hex, 0 );
    int obc2 = getObc( hex, 1 );
    parms[ devIndex ] = new Value( new Integer( getDevice( hex ) ) );
    Integer obcInt = new Integer( obc1 );
    parms[ obcIndex ] = new Value( obcInt );
    if ( obc2 != obc1 )
    {
      parms[ obc2Index ] = new Value( new Integer( obc2 ) );
    }
    else
    {
      parms[ obc2Index ] = new Value( null );
    }
  }

}
