package com.hifiremote.jp1;

public class SonyComboImporter
  extends Translate
{
  public SonyComboImporter( String[] textParms )
  {
    super( textParms );
  }

  public void in( Value[] parms, Hex hexData, DeviceParameter[] devParms, int parmToSet )
  {
    if ( parmToSet < 0 )
      return;

    int[] hex = hexData.getData();

    int device = 0;
    int protocol = 0;
    int subDevice = 0;
    if ( parms.length == 1 )
    {
      device = (( Integer )parms[ 0 ].getValue()).intValue();
      if ( device > 31 )
        protocol = Sony15;
      else
        protocol = Sony12;
    }
    else
    {
      device = (( Integer )parms[ 1 ].getValue()).intValue();
      int temp  = (( Integer )parms[ 0 ].getValue()).intValue();
      if (( temp > 0 ) && ( temp < 5 )) // Sony20
      {
        protocol = Sony20;
        subDevice = temp - 1;
      }
      else if ( temp == 5 ) // force Sony12
      {
        protocol = Sony12;
      }
      else  // force Sony15
      {
        protocol = Sony15;
      }
    }

    if ( protocol == Sony12 )
    {
      insert( hexData, 7, 1, 0 );  // clear Sony15 bit
      insert( hexData, 13, 3, 0 ); // clear Sony20 bit and index
      insert( hexData, 8, 5, reverse( device, 5 ));  // store device as 5 bits
    }
    else if ( protocol == Sony15 )
    {
      insert( hexData, 7, 1, 1 ); // set Sony15 bit
      insert( hexData, 8, 8, reverse( device, 8 )); // store device as 8 bits
    }
    else // protocol == Sony20
    {
      insert( hexData, 7, 1, 0 ); // clear Sony15 bit
      insert( hexData, 15, 1, 1 ); // set Sony20 bit
      insert( hexData, 8, 5, reverse( device, 5 ));  // store device as 5 bits
      insert( hexData, 13, 2, subDevice );  // store subdevice
    }
  }

  public void out( Hex hex, Value[] parms, DeviceParameter[] devParms )
  {}

  private static int Sony12 = 0;
  private static int Sony15 = 1;
  private static int Sony20 = 2;
}
