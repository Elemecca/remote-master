package com.hifiremote.jp1.translate;

import com.hifiremote.jp1.DeviceParameter;
import com.hifiremote.jp1.Hex;
import com.hifiremote.jp1.Value;

// TODO: Auto-generated Javadoc
/**
 * The Class XorCheck.
 */
public class XorCheck extends Translate
{

  /**
   * Instantiates a new xor check.
   * 
   * @param textParms
   *          the text parms
   */
  public XorCheck( String[] textParms )
  {
    super( textParms );
    int parmIndex = 0;
    for ( int i = 0; i < textParms.length; i++ )
    {
      String text = textParms[ i ];
      int val = Integer.parseInt( text );
      switch ( parmIndex )
      {
        case bitsIndex:
          bits = val;
          step = val;
          break;
        case destOffsetIndex:
          destOffset = val;
          sourceOffset = val - bits;
          break;
        case seedIndex:
          seed = val;
          break;
        case countIndex:
          count = val;
          sourceOffset = destOffset - val * bits;
          break;
        case sourceOffsetIndex:
          sourceOffset = val;
          break;
        case stepIndex:
          step = val;
          break;
        default:
          break;
      }
      parmIndex++ ;
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
    // System.err.println("XorCheck(" + bits +","+ destOffset +","+ seed +","+ count +","+ sourceOffset +","+ step
    // +").in(" + hex.length +")");
    int v = seed;
    int s = sourceOffset;
    for ( int i = 0; i < count; i++ )
    {
      v ^= extract( hexData, s, bits );
      s += step;
    }
    insert( hexData, destOffset, bits, v );
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

  /** The bits. */
  private int bits = 8;

  /** The dest offset. */
  private int destOffset = 8;

  /** The seed. */
  private int seed = 0;

  /** The count. */
  private int count = 1;

  /** The source offset. */
  private int sourceOffset = 0;

  /** The step. */
  private int step = 8;

  /** The Constant bitsIndex. */
  private final static int bitsIndex = 0;

  /** The Constant destOffsetIndex. */
  private final static int destOffsetIndex = 1;

  /** The Constant seedIndex. */
  private final static int seedIndex = 2;

  /** The Constant countIndex. */
  private final static int countIndex = 3;

  /** The Constant sourceOffsetIndex. */
  private final static int sourceOffsetIndex = 4;

  /** The Constant stepIndex. */
  private final static int stepIndex = 5;
}
