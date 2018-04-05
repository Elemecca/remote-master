package com.hifiremote.jp1.translate;

import com.hifiremote.jp1.DeviceParameter;
import com.hifiremote.jp1.Hex;
import com.hifiremote.jp1.Value;

public class NibbleSumChk extends Translate
{
  /**
   * Instantiates a new sum check.
   *
   * @param textParms
   *          the text parms
   */
  public NibbleSumChk( String[] textParms )
  {
    super( textParms );
    int parmIndex = 0;
    for ( int i = 0; i < textParms.length; i++ )
    {
      String text = textParms[ i ];
      if ( text.equalsIgnoreCase( "comp" ) ) comp = true;
      else
      {
          switch ( parmIndex )
          {
            case maskIndex:
              mask = Integer.parseInt( text, 16 );
              break;
            case destOffsetIndex:
              destOffset = Integer.parseInt( text );
              break;
            case seedIndex:
              seed = Integer.parseInt( text );
              break;
          }
          parmIndex++ ;
      }
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see com.hifiremote.jp1.Translate#in(com.hifiremote.jp1.Value[], com.hifiremote.jp1.Hex,
   * com.hifiremote.jp1.DeviceParameter[], int)
   */
  @Override
  public void in( Value[] parms, Hex hexData, DeviceParameter[] devParms, int onlyIndex )
  {
    int v = seed;
    int s = 0;
    int andBit = 0x8000;
    for ( int i = 0; i < 16; i++ )
    {
      if ( ( mask & andBit ) == andBit )
      {
        v += extract( hexData, s, 4 );
      }
      s += 4;
      andBit /= 2;
    }
    if ( comp )  v = ~v;
    v &= 0xF;
    insert( hexData, destOffset, 4, v );
  }

  @Override
  public void out( Hex hex, Value[] parms, DeviceParameter[] devParms )
  {}

  private int mask = 0xBF00;
  private int destOffset = 4;
  private int seed = 0;
  private boolean comp = false;
  private final static int maskIndex = 0;
  private final static int destOffsetIndex = 1;
  private final static int seedIndex = 2;

}
