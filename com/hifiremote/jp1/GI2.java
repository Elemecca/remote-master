package com.hifiremote.jp1;

public class GI2
  extends Translate
{
  public GI2( String[] textParms )
  {
    super( textParms );
  }

  public void in( Value[] parms, Hex hexData, DeviceParameter[] devParms, int onlyIndex )
  {
    int[] hex = hexData.getData();
    int dev = (( Integer )devParms[0].getValueOrDefault()).intValue();
    int obc = (( Integer )parms[ 0 ].getUserValue()).intValue();
    int cmd = (dev<<6)+obc;
    hex[0] = reverse( 63 - obc );
    int check = (cmd<<6) ^ (cmd<<3) ^ (cmd<<2) ^ cmd ^ (cmd>>2) ^ (cmd>>3) ^ (cmd>>4);
    hex[1] = reverse( 63 - dev - (check&56) - ((check>>4)&4) );
  }

  public void out( Hex hex, Value[] parms, DeviceParameter[] devParms )
  {
     parms[0] = new Value( new Integer( 63 - ( 63 & reverse( hex.getData()[0] ) ) ), null );
  }

}

