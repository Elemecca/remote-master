package com.hifiremote.jp1;

public class ZenithCmdTranslator
  extends Translate
{
  public ZenithCmdTranslator( String[] textParms )
  {
    super( textParms );
  }

  public void in( Value[] parms, Hex hex, DeviceParameter[] devParms, int onlyIndex )
  {
    Value obcVal = parms[ 0 ];
    if ( obcVal == null )
      return;
    Number obcInt = ( Number )obcVal.getValue();
    if ( obcInt == null )
      return;

    int val = obcInt.intValue();

    int bits = 0;
    Integer bitsInt = ( Integer )devParms[ 0 ].getValue();
    if ( bitsInt == null )
    {
      bitsInt = ( Integer )devParms[ 0 ].getValueOrDefault();
      bits = bitsInt.intValue() - 1;
    }
    else
      bits = bitsInt.intValue();

    insert( hex, 1, bits, val );
  }

  public void out( Hex hex, Value[] parms, DeviceParameter[] devParms )
  {
    int bits = 0;
    Integer bitsInt = ( Integer )devParms[ 0 ].getValue();
    if ( bitsInt == null )
    {
      bitsInt = ( Integer )devParms[ 0 ].getValueOrDefault();
      bits = bitsInt.intValue() - 1;
    }
    else
      bits = bitsInt.intValue();

    int val = extract( hex, 1, bits );

    parms[ 0 ] = new Value( new Integer( val ), null );
  }
}
