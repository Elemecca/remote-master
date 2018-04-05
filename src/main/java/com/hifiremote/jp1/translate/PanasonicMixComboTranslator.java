package com.hifiremote.jp1.translate;

import com.hifiremote.jp1.DeviceParameter;
import com.hifiremote.jp1.Hex;
import com.hifiremote.jp1.Value;

// TODO: Auto-generated Javadoc
/**
 * The Class PanasonicMixComboTranslator.
 */
public class PanasonicMixComboTranslator extends Translate
{

  /**
   * Instantiates a new panasonic mix combo translator.
   * 
   * @param textParms
   *          the text parms
   */
  public PanasonicMixComboTranslator( String[] textParms )
  {
    super( textParms );
  }

  /** The bit length. */
  private static int bitLength = 6;

  /**
   * Gets the device.
   * 
   * @param value
   *          the value
   * @return the device
   */
  private static int getDevice( int value )
  {
    int rc = -1;
    int mask = 1 << bitLength - 1;
    for ( int i = 0; i < bitLength; i++ )
    {
      if ( ( value & mask ) != 0 )
      {
        rc = i;
        break;
      }
      mask >>= 1;
    }
    System.err.println( "getDevice(" + Integer.toHexString( value ) + ") = " + rc );
    return rc;
  }

  /**
   * Gets the sub device.
   * 
   * @param value
   *          the value
   * @return the sub device
   */
  private static int getSubDevice( int value )
  {
    int rc = -1;
    int mask = 1;
    for ( int i = bitLength; i >= 0; i-- )
    {
      if ( ( value & mask ) != 0 )
      {
        rc = i - 1;
        break;
      }
      mask <<= 1;
    }
    System.err.println( "getSubDevice(" + Integer.toHexString( value ) + ") = " + rc );
    return rc - 1;
  }

  /**
   * Combine values.
   * 
   * @param device
   *          the device
   * @param subDevice
   *          the sub device
   * @return the int
   */
  private int combineValues( int device, int subDevice )
  {
    if ( subDevice <= device )
    {
      subDevice = device + 1;
    }
    int rc = 1 << bitLength - 1 - device;
    rc |= 1 << bitLength - 1 - subDevice;
    System.err.println( "combineValues(" + device + "," + subDevice + ") = " + Integer.toHexString( rc ) );
    return rc;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.Translate#in(com.hifiremote.jp1.Value[], com.hifiremote.jp1.Hex,
   * com.hifiremote.jp1.DeviceParameter[], int)
   */
  @Override
  public void in( Value[] parms, Hex hexData, DeviceParameter[] devParms, int onlyIndex )
  {
    int device = 0;
    int subDevice = 0;
    int byte2 = 0;
    System.err.println( "in(), onlyIndex=" + onlyIndex );
    if ( onlyIndex == 1 ) // device
    {
      // User has specified which device to use.
      device = ( ( Number )parms[ 1 ].getValue() ).intValue();
      byte2 = hexData.getData()[ 1 ];
      subDevice = getSubDevice( byte2 );
      int value = combineValues( device, subDevice );
      insert( hexData, 10, bitLength, value );
    }
    else if ( onlyIndex == 2 ) // sub device
    {
      byte2 = hexData.getData()[ 1 ];
      device = getDevice( byte2 );
      subDevice = ( ( Number )parms[ 2 ].getValue() ).intValue() + 1;
      int value = combineValues( device, subDevice );
      insert( hexData, 10, bitLength, value );
    }
    else if ( onlyIndex == -1 )// both device and subdevice
    {
      if ( parms[ 1 ] == null || parms[ 1 ].getValue() == null )
      {
        return;
      }
      if ( parms[ 2 ] == null || parms[ 2 ].getValue() == null )
      {
        return;
      }
      device = ( ( Number )parms[ 1 ].getValue() ).intValue();
      subDevice = ( ( Number )parms[ 2 ].getValue() ).intValue() + 1;
      insert( hexData, 10, bitLength, combineValues( device, subDevice ) );
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.Translate#out(com.hifiremote.jp1.Hex, com.hifiremote.jp1.Value[],
   * com.hifiremote.jp1.DeviceParameter[])
   */
  @Override
  public void out( Hex hex, Value[] parms, DeviceParameter[] devParms )
  {
    int byte2 = hex.getData()[ 1 ];
    System.err.println( "out( " + Integer.toHexString( byte2 ) + " )" );
    parms[ 1 ] = new Value( new Integer( getDevice( byte2 ) ) );
    parms[ 2 ] = new Value( new Integer( getSubDevice( byte2 ) ) );
  }
}
