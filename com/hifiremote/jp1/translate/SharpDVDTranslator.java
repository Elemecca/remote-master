package com.hifiremote.jp1.translate;

import com.hifiremote.jp1.DeviceParameter;
import com.hifiremote.jp1.Hex;
import com.hifiremote.jp1.Value;

// TODO: Auto-generated Javadoc
/**
 * The Class SharpDVDTranslator.
 */
public class SharpDVDTranslator extends Translate
{

  /**
   * Instantiates a new sharp dvd translator.
   * 
   * @param textParms
   *          the text parms
   */
  public SharpDVDTranslator( String[] textParms )
  {
    super( textParms );
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.Translate#in(com.hifiremote.jp1.Value[], com.hifiremote.jp1.Hex,
   * com.hifiremote.jp1.DeviceParameter[], int)
   */
  @Override
  public void in( Value[] parms, Hex hex, DeviceParameter[] devParms, int onlyIndex )
  {
    int d, s, sLo, sHi;
    d = reverse( ( ( Number )devParms[ 0 ].getValueOrDefault() ).intValue(), 4);
    s = reverse( ( ( Number )devParms[ 1 ].getValueOrDefault() ).intValue(), 8);
    sLo = (s & 0x0F);
    sHi = (s & 0xF0) >> 4;
    int data = 0x70;
    data ^= d ^ sHi ^ sLo ^ extract( hex, 0, 4 ) ^ extract( hex, 4, 4 ) ^ extract( hex, 8, 4 );
    insert( hex, 8, 8, data );
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.Translate#out(com.hifiremote.jp1.Hex, com.hifiremote.jp1.Value[],
   * com.hifiremote.jp1.DeviceParameter[])
   */
  @Override
  public void out( Hex hex, Value[] parms, DeviceParameter[] devParms )
  {}

}
