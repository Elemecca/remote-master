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
    int[] hex = hexData.getData();

    int flag0 = (( Integer )parms[ 1 ].getValue()).intValue();
    int flag1 = (parms[ 2 ].getUserValue() == null)
                  ? ( 1 - flag0 )
                  : (( Integer )parms[ 3 ].getValue()).intValue();
    int flag2 = (parms[ 4 ].getUserValue() == null)
                  ? ( 1 - flag1 )
                  : (( Integer )parms[ 5 ].getValue()).intValue();

    hex[0] = (hex[0] & 0xBF) | (flag0 * 0x40);
    hex[1] = (hex[1] & 0xBF) | (flag1 * 0x40);
    hex[2] = (hex[2] & 0xBF) | (flag2 * 0x40);
  }

  public void out( Hex hexData, Value[] parms, DeviceParameter[] devParms )
  {
    int[] hex = hexData.getData();

    int flag0 = hex[ 0 ] & 0x40;
    int flag1 = hex[ 1 ] & 0x40;
    int flag2 = hex[ 2 ] & 0x40;
    Value one = new Value( new Integer( 1 ));
    Value zero = new Value( new Integer( 0 ));
    parms[ 1 ] = ( flag0 == 0 ) ? zero : one;
    parms[ 3 ] = ( flag1 == 0 ) ? zero : one;
    parms[ 5 ] = ( flag2 == 0 ) ? zero : one;
  }


}
