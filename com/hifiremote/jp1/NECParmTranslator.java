package com.hifiremote.jp1;

public class NECParmTranslator
  extends Translate
{
  public NECParmTranslator( String[] textParms )
  {
    super( textParms );
    initialDefaultParm = Integer.parseInt( textParms[ 0 ]);
  }

  public void in( Value[] parms, Hex hexData, DeviceParameter[] devParms, int onlyIndex )
  {
    byte[] hex = hexData.getData();
    Integer deviceNumber = ( Integer )parms[ 0 ].getUserValue();
    Integer subDevice = ( Integer )parms[ 1 ].getUserValue();
    Integer parm = ( Integer )parms[ 2 ].getUserValue();

    if ( parm != null )
      hex[ 0 ] = parm.byteValue();
    else
    {
      if ( subDevice == null )
        hex[ 0 ] = ( byte )initialDefaultParm;
      else
        hex[ 0 ] = ( byte )( initialDefaultParm + 0x20 );
    }

    if ( deviceNumber == null )
      deviceNumber = new Integer( 0 );
    hex[ 1 ] = ( byte )reverse(( byte )~deviceNumber.byteValue());

    if ( subDevice == null )
    {
      if (( hex[ 0 ] & 0x20 ) == 0 )
        hex[ 2 ] = hex[ 1 ];
      else
        hex[ 2 ] = ( byte )~hex[ 1 ];
    }
    else
      hex[ 2 ] = ( byte )reverse(( byte )~subDevice.byteValue());

  }

  public void out( Hex hex, Value[] parms, DeviceParameter[] devParms )
  {}

  private int initialDefaultParm;
}
