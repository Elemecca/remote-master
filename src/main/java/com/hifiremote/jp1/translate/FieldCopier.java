package com.hifiremote.jp1.translate;

import com.hifiremote.jp1.DeviceParameter;
import com.hifiremote.jp1.Hex;
import com.hifiremote.jp1.Value;

// TODO: Auto-generated Javadoc
/**
 * The Class FieldCopier.
 */
public class FieldCopier extends Translate
{
  // This class is used to copy bitfields from one byte to another in the
  // hexdata
  // parms are:
  // "lsb" - bits should be reversed when copied
  // "comp" - bits should be complmented when copied
  // srcOffset - msbOffset of the first bit of the field to copy
  // destOffset - msbOffset where field should be copied to
  // bits - the bit length of the field

  /**
   * Instantiates a new field copier.
   * 
   * @param textParms
   *          the text parms
   */
  public FieldCopier( String[] textParms )
  {
    super( textParms );
    int parmIndex = 0;
    for ( int i = 0; i < textParms.length; i++ )
    {
      String text = textParms[ i ];
      if ( text.equalsIgnoreCase( "lsb" ) )
      {
        lsb = true;
      }
      else if ( text.equalsIgnoreCase( "comp" ) )
      {
        comp = true;
      }
      else
      {
        int val = Integer.parseInt( text );
        switch ( parmIndex )
        {
          case srcOffsetIndex:
            srcOffset = val;
            break;
          case destOffsetIndex:
            destOffset = val;
            break;
          case bitsIndex:
            bits = val;
            break;
          default:
            break;
        }
        parmIndex++ ;
      }
    }
  }

  // called to store parms into hex data
  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.Translate#in(com.hifiremote.jp1.Value[], com.hifiremote.jp1.Hex,
   * com.hifiremote.jp1.DeviceParameter[], int)
   */
  @Override
  public void in( Value[] parms, Hex hex, DeviceParameter[] devParms, int onlyIndex )
  {
    int val = extract( hex, srcOffset, bits );
    if ( lsb )
    {
      val = reverse( val, bits );
    }
    if ( comp )
    {
      val = complement( val, bits );
    }
    insert( hex, destOffset, bits, val );
  }

  // called to extract parms from hex data
  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.Translate#out(com.hifiremote.jp1.Hex, com.hifiremote.jp1.Value[],
   * com.hifiremote.jp1.DeviceParameter[])
   */
  @Override
  public void out( Hex hex, Value[] parms, DeviceParameter[] devParms )
  {
    ;
  }

  /** The comp. */
  private boolean comp = false;

  /** The lsb. */
  private boolean lsb = false;

  /** The src offset. */
  private int srcOffset;

  /** The dest offset. */
  private int destOffset;

  /** The bits. */
  private int bits;

  /** The Constant srcOffsetIndex. */
  private final static int srcOffsetIndex = 0;

  /** The Constant destOffsetIndex. */
  private final static int destOffsetIndex = 1;

  /** The Constant bitsIndex. */
  private final static int bitsIndex = 2;
}
