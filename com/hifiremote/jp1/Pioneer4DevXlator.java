package com.hifiremote.jp1;

public class Pioneer4DevXlator
  extends Translator
{
  private int devIndex = 0;
  private int obcIndex = 0;
  private int obc2Index = 0;

  public Pioneer4DevXlator( String[] textParms )
  {
    super( textParms );
    devIndex = Integer.parseInt( textParms[ 0 ]);
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

  public int setDeviceBit( int obc, int flag )
  {
    if ( flag != 0 )
      obc |= 0x20;
    else
      obc &= 0xDF;
    return obc;
  }

  private void setHex( int obc1, int obc2, int device, short[] hex )
  {
    obc1 = setDeviceBit( obc1, device & 2 );
    hex[ 0 ] = ( short )reverse( obc1 );

    obc2 = setDeviceBit( obc2, device & 1 );
    hex[ 1 ] = ( short )reverse( obc2 );
  }

  private int getDevice( short[] hex )
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
      device = (( Integer )parms[ devIndex ].getValue()).intValue();
    return device;
  }

  private short getObc( short[] hex, int index )
  {
    return ( short )( reverse( hex[ index ] ) & 0xDF );
  }

  private short getObc( Value[] parms, int index, int value )
  {
    int i;
    if ( index == 0 )
      i = obcIndex;
    else
      i = obc2Index;

    if (( parms[ i ] != null ) && ( parms[ i ].getValue() != null ))
    {
      System.err.println( "parms[i].getValue() is a " + parms[ i ].getValue().getClass());
      value = (( Integer )parms[ i ].getValue()).intValue();
    }
    return ( short )value;
  }

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
        obc2 = obc1;
      setHex( obc1, obc2, device, hex );
    }
    else if ( onlyIndex == obc2Index )
    {
      int device = getDevice( hex );
      int obc1 = getObc( hex, 0 );
      int obc2 = getObc( parms, 1, obc1 );
      setHex( obc1, obc2, device, hex );
    }
    else // ALL
    {
      System.err.println( "onlyIndex=" + onlyIndex );
      int device = getDevice( parms );
      int obc1 = getObc( parms, 0, 0 );
      int obc2 = getObc( parms, 1, obc1 );
      setHex( obc1, obc2, device, hex );
    }
  }

  public void out( Hex hexData, Value[] parms, DeviceParameter[] devParms )
  {
    short[] hex = hexData.getData();
    int obc1 = getObc( hex, 0 );
    int obc2 = getObc( hex, 1 );
    parms[ devIndex ] = new Value( new Integer( getDevice( hex )));
    Integer obcInt = new Integer( obc1 );
    parms[ obcIndex ] = new Value( obcInt );
    if ( obc2 != obc1 )
      parms[ obc2Index ] = new Value( new Integer( obc2 ));
    else
      parms[ obc2Index ] = new Value( null );
  }

}
