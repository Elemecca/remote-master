package com.hifiremote.jp1;

public class NECParmTranslator
  extends Translate
{
  public NECParmTranslator( String[] textParms )
  {
    super( textParms );
    initialDefaultParm = Integer.parseInt( textParms[ 0 ], 16 );
  }

  public void in( Value[] parms, Hex hexData, DeviceParameter[] devParms, int onlyIndex )
  {
    int[] hex = hexData.getData();
    Integer deviceNumber = ( Integer )parms[ 0 ].getUserValue();
    Integer subDevice = ( Integer )parms[ 1 ].getUserValue();
    Integer parm = ( Integer )parms[ 2 ].getUserValue();

    if ( parm != null )
      hex[ 0 ] = parm.intValue();
    else
    {
      if ( subDevice == null )
        hex[ 0 ] = initialDefaultParm;
      else
        hex[ 0 ] = initialDefaultParm + 0x20;
    }

    if ( deviceNumber == null )
      deviceNumber = new Integer( 0 );
    hex[ 1 ] = reverse( complement( deviceNumber.intValue()));

    if ( subDevice == null )
    {
      if (( hex[ 0 ] & 0x20 ) == 0 )
        hex[ 2 ] = hex[ 1 ];
      else
        hex[ 2 ] = complement( hex[ 1 ]);
    }
    else
      hex[ 2 ] = reverse( complement( subDevice.byteValue()));
  }

  public void out( Hex hexData, Value[] parms, DeviceParameter[] devParms )
  {
    Integer deviceNumber = null;
    Integer subDevice = null;
    Integer parm = null;

    int[] hex = hexData.getData();
    int temp = reverse( complement( hex[ 1 ])) & 0xFF ;
    if ( temp == 0 )
      deviceNumber = null;
    else
      deviceNumber = new Integer( temp );

    if (( hex[ 2 ] != hex[ 1 ]) && ( hex[ 2 ] != complement(hex[ 1 ])))
      subDevice = new Integer( reverse( complement(hex[ 2 ])));

    if (( hex[ 0 ] != initialDefaultParm ) &&
        ( hex[ 0 ] != ( initialDefaultParm + 0x20 )))
        parm = new Integer( hex[ 0 ] & 0xFF );

    parms[ 0 ] = new Value( deviceNumber, null );
    parms[ 1 ] = new Value( subDevice, null );
    parms[ 2 ] = new Value( parm, null );
  }

  private int initialDefaultParm;
}
