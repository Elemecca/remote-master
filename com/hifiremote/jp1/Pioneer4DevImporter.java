package com.hifiremote.jp1;

public class Pioneer4DevImporter
  extends Translator
{
  private int devIndex = 0;
  private int obc2Index = 1;

  public Pioneer4DevImporter( String[] textParms )
  {
    super( textParms );
  }

  public int setDeviceBit( int obc, int flag )
  {
    if ( flag != 0 )
      obc |= 0x20;
    else
      obc &= 0xDF;
    return obc;
  }

  private void setHex( int obc1, int obc2, int device, int[] hex )
  {
    obc1 = setDeviceBit( obc1, device & 2 );
    hex[ 0 ] = reverse( obc1 );

    obc2 = setDeviceBit( obc2, device & 1 );
    hex[ 1 ] = reverse( obc2 );
  }

  private int getDevice( int[] hex )
  {
    int device = 0;
    if (( hex[ 0 ] & 0x04 ) > 0 )
      device += 2;
    if (( hex[ 1 ] & 0x04 ) > 0 )
      device += 1;

    return device;
  }

  private int getDevice( Value[] parms )
  {
    int device = 0;
    if (( parms[ devIndex ] != null ) && ( parms[ devIndex ].getValue() != null ))
      device = (( Integer )parms[ devIndex ].getValue()).intValue() - 1;
    return device;
  }

  private int getObc( int[] hex, int index )
  {
    return ( reverse( hex[ index ] ) & 0xDF );
  }

  private int getObc( Value[] parms, int index, int value )
  {
    if (( parms[ index ] != null ) && ( parms[ index ].getValue() != null ))
    {
      System.err.println( "parms[index].getValue() is a " + parms[ index ].getValue().getClass());
      value = (( Integer )parms[ index ].getValue()).intValue();
    }
    return value;
  }

  public void in( Value[] parms, Hex hexData, DeviceParameter[] devParms, int onlyIndex )
  {
    int[] hex = hexData.getData();
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
      return;
  }

  public void out( Hex hexData, Value[] parms, DeviceParameter[] devParms )
  {}
}
