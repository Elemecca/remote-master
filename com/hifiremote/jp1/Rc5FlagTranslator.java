package com.hifiremote.jp1;

public class Rc5FlagTranslator
  extends Translate
{
  public Rc5FlagTranslator( String[] textParms )
  {
    super( textParms );
  }

  public void in( Value[] parms, Hex hexData, DeviceParameter[] devParms, int onlyIndex )
  {
    byte[] hex = hexData.getData();

    int flag0 = (( Integer )parms[ 1 ].getValue()).intValue();
    int flag1 = (parms[ 2 ].getUserValue() == null)
                  ? ( 1 - flag0 )
		  : (( Integer )parms[ 3 ].getValue()).intValue();
    int flag2 = (parms[ 4 ].getUserValue() == null)
                  ? ( 1 - flag1 )
		  : (( Integer )parms[ 5 ].getValue()).intValue();

    hex[0] = (byte) ( (hex[0] & 0xBF) | (flag0 * 0x40) );
    hex[1] = (byte) ( (hex[1] & 0xBF) | (flag1 * 0x40) );
    hex[2] = (byte) ( (hex[2] & 0xBF) | (flag2 * 0x40) );
  }

  public void out( Hex hex, Value[] parms, DeviceParameter[] devParms )
  {}

  
}
