package com.hifiremote.jp1.translate;

import com.hifiremote.jp1.DeviceParameter;
import com.hifiremote.jp1.Hex;
import com.hifiremote.jp1.Value;

// TODO: Auto-generated Javadoc
/**
 * The Class GI3.
 */
public class GI3 extends Translate
{

  /**
   * Instantiates a new GI3.
   * 
   * @param textParms
   *          the text parms
   */
  public GI3( String[] textParms )
  {
    super( textParms );
  }
  
  /*
   * (non-Javadoc)
   *
   */
  private int parity(int v) {  // count the number of bits set in v.  v must be positive--i.e.bit 31 must be 0
    int c; // c accumulates the total bits set in v
    for (c = 0; v != 0; c++)  {
      v &= v - 1; // clear the least significant bit set
    }
    return c & 1;
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
    int unit= 0x07 & ( ( Number )devParms[ 0 ].getValueOrDefault() ).intValue();
    int unit2_rev_comp = 0x03 ^ reverse((unit & 0x03), 2);
    int obc = ((Number)parms[ 0 ].getUserValue()).intValue() & 0x3F;
    int obc_rev_comp = reverse(0x3F ^ obc, 6) << 2;
    hex[ 0 ] = ( short ) obc_rev_comp;
    int cmd8 = 0xFF ^ (obc_rev_comp + unit2_rev_comp);
    //check satisfies a Hamming code for the 8 bits of OBC and dev
    int check = (parity(cmd8 & 0x9A) << 3) + (parity(cmd8 & 0xD7) << 2) 
              + (parity(cmd8 & 0x6B) << 1) + parity(cmd8 & 0x35);
    if ( (unit & 4) != 4) check ^= 0x0F;
    hex[ 1 ] = (short)((unit2_rev_comp << 6) + (check << 2));
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.Translate#out(com.hifiremote.jp1.Hex, com.hifiremote.jp1.Value[],
   * com.hifiremote.jp1.DeviceParameter[])
   */
  public void out( Hex hex, Value[] parms, DeviceParameter[] devParms )
  {
    parms[ 0 ] = new Value( new Integer( 0x3F ^ ( 0x3F & reverse( hex.getData()[ 0 ] ) ) ), null );
  }

}
