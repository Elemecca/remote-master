package com.hifiremote.jp1.translate;

import com.hifiremote.jp1.DeviceParameter;
import com.hifiremote.jp1.Hex;
import com.hifiremote.jp1.Value;

// TODO: Auto-generated Javadoc
/**
 * The Class GI2.
 */
public class GI2 extends Translate
{

  /**
   * Instantiates a new g i2.
   * 
   * @param textParms
   *          the text parms
   */
  public GI2( String[] textParms )
  {
    super( textParms );
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.Translate#in(com.hifiremote.jp1.Value[], com.hifiremote.jp1.Hex,
   * com.hifiremote.jp1.DeviceParameter[], int)
   */
  public void in( Value[] parms, Hex hexData, DeviceParameter[] devParms, int onlyIndex )
  {
    short[] hex = hexData.getData();
    int dev = ( ( Number )devParms[ 0 ].getValueOrDefault() ).intValue();
    int obc = ( ( Number )parms[ 0 ].getUserValue() ).intValue();
    int cmd = ( dev << 6 ) + obc;
    hex[ 0 ] = ( short )reverse( 63 - obc );
    int check = cmd << 6 ^ cmd << 3 ^ cmd << 2 ^ cmd ^ cmd >> 2 ^ cmd >> 3 ^ cmd >> 4;
    hex[ 1 ] = ( short )reverse( 63 - dev - ( check & 56 ) - ( check >> 4 & 4 ) );
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.Translate#out(com.hifiremote.jp1.Hex, com.hifiremote.jp1.Value[],
   * com.hifiremote.jp1.DeviceParameter[])
   */
  public void out( Hex hex, Value[] parms, DeviceParameter[] devParms )
  {
    parms[ 0 ] = new Value( new Integer( 63 - ( 63 & reverse( hex.getData()[ 0 ] ) ) ), null );
  }

}
