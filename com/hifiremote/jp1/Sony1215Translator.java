package com.hifiremote.jp1;

public class Sony1215Translator
  extends Translate
{
  public Sony1215Translator( String[] textParms )
  {
    super( textParms );
  }

  public void in( Value[] parms, Hex hexData, DeviceParameter[] devParms, int onlyIndex )
  {
    byte[] hex = hexData.getData();

    int device1 = (( Integer )parms[ 0 ].getValue()).intValue();
    boolean force1 = (( Boolean )parms[ 1 ].getValue()).booleanValue();
    int device2 = (( Integer )parms[ 2 ].getValue()).intValue();
    boolean force2 = (( Boolean )parms[ 3 ].getValue()).booleanValue();

    if (( device1 > 31 ) || force1 )
      hex[ 2 ] = ( byte )0x80;
    else
      hex[ 2 ] = 0;  

    if (( device2 > 31 ) || force2 )
      hex[ 2 ] |= ( byte )0x40;
  }

  public void out( Hex hex, Value[] parms, DeviceParameter[] devParms )
  {}

  
}
