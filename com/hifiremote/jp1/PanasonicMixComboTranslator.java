package com.hifiremote.jp1;

public class PanasonicMixComboTranslator
  extends Translate
{
  public PanasonicMixComboTranslator( String[] textParms )
  {
    super( textParms );
  }

  private static int bitLength = 6;

  private static int getDevice( int value )
  {
    int rc = -1;
    int mask = 1 << ( bitLength - 1 );
    for ( int i = 0; i < bitLength; i++ )
    {
      if (( value & mask ) != 0 )
      {
        rc = i;
        break;
      }
      mask >>= 1;
    }
    System.err.println( "getDevice(" + Integer.toHexString( value ) + ") = " + rc );
    return rc;
  }

  private static int getSubDevice( int value )
  {
    int rc = -1;
    int mask = 1;
    for ( int i = bitLength; i >= 0; i-- )
    {
      if (( value & mask ) != 0 )
      {
        rc = i - 1;
        break;
      }
      mask <<= 1;
    }
    System.err.println( "getSubDevice(" + Integer.toHexString( value ) + ") = " + rc );
    return rc - 1;
  }

  private int combineValues( int device, int subDevice )
  {
    if ( subDevice <= device )
        subDevice = device + 1;
    int rc = 1 << ( bitLength - 1 - device );
    rc |= ( 1 << ( bitLength - 1 - subDevice ));
    System.err.println( "combineValues(" + device + "," + subDevice + ") = " + Integer.toHexString( rc ));
    return rc;
  }

  public void in( Value[] parms, Hex hexData, DeviceParameter[] devParms, int onlyIndex )
  {
    int device = 0;
    int subDevice = 0;
    int byte2 = 0;
    System.err.println( "in(), onlyIndex=" + onlyIndex );
    if ( onlyIndex == 1 ) // device
    {
      // User has specified which device to use.
      device = (( Integer )parms[ 1 ].getValue()).intValue();
      byte2 = hexData.getData()[ 1 ];
      subDevice = getSubDevice( byte2 );
      int value = combineValues( device, subDevice );
      insert( hexData, 10, bitLength, value );
    }
    else if ( onlyIndex == 2 ) // sub device
    {
      byte2 = hexData.getData()[ 1 ];
      device = getDevice( byte2 );
      subDevice = (( Integer )parms[ 2 ].getValue()).intValue() + 1;
      int value = combineValues( device, subDevice );
      insert( hexData, 10, bitLength, value );
    }
    else if ( onlyIndex == -1 )// both device and subdevice
    {
      if (( parms[ 1 ] == null ) || ( parms[ 1 ].getValue() == null ))
        return;
      if (( parms[ 2 ] == null ) || ( parms[ 2 ].getValue() == null ))
        return;
      device = (( Integer )parms[ 1 ].getValue()).intValue();
      subDevice = (( Integer )parms[ 2 ].getValue()).intValue() + 1;
      insert( hexData, 10, bitLength, combineValues( device, subDevice ));
    }
  }

  public void out( Hex hex, Value[] parms, DeviceParameter[] devParms )
  {
    int byte2 = hex.getData()[ 1 ];
    System.err.println( "out( " + Integer.toHexString( byte2 ) + " )" );
    parms[ 1 ] = new Value( new Integer( getDevice( byte2 )));
    parms[ 2 ] = new Value( new Integer( getSubDevice( byte2 )));
  }
}

