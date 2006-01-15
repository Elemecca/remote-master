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
    int numFlags = hex.length;
    int flag = 0;
    for ( int i = 0; i < numFlags; i++ )
    {
      int parmIndex = 2 * i;
      Object val = parms[ parmIndex ].getUserValue();
      int thisFlag = (( Integer )parms[ parmIndex + 1 ].getValue()).intValue();
      if (( i != 0 ) && ( val == null ))
        thisFlag = 1 - flag;
      
      hex[ i ] = ( hex[ i ] & 0xBF) | ( thisFlag * 0x40 );
      flag = thisFlag;
    }
  }

  public void out( Hex hexData, Value[] parms, DeviceParameter[] devParms )
  {
    int[] hex = hexData.getData();

    Value one = new Value( new Integer( 1 ));
    Value zero = new Value( new Integer( 0 ));
    for ( int i = 0; i < hex.length; i++ )
    {
      int flag = hex[ i ] & 0x40;
      int parmIndex = ( 2 * i ) + 1;
      parms[ parmIndex ] = ( flag == 0 ) ? zero : one;
    }
  }
}
