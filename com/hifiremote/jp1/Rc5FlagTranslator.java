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

    boolean flag0 = (( Boolean )parms[ 1 ].getValue()).booleanValue();
    boolean flag1 = (parms[ 2 ].getUserValue() == null)
                  ? ( ! flag0 )
		  : (( Boolean )parms[ 3 ].getValue()).booleanValue();
    boolean flag2 = (parms[ 4 ].getUserValue() == null)
                  ? ( ! flag1 )
		  : (( Boolean )parms[ 5 ].getValue()).booleanValue();

    hex[0] = (byte) ( (hex[0] & 0xBF) | (flag0 ? 0x40 : 0) );
    hex[1] = (byte) ( (hex[1] & 0xBF) | (flag1 ? 0x40 : 0) );
    hex[2] = (byte) ( (hex[2] & 0xBF) | (flag2 ? 0x40 : 0) );
  }

  public void out( Hex hex, Value[] parms, DeviceParameter[] devParms )
  {}

  
}
