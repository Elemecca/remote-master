package com.hifiremote.jp1;

public class FieldCopier
  extends Translate
{
  // This class is used to copy bitfields from one byte to another in the
  // hexdata
  // parms are:
  //   "lsb" - bits should be reversed when copied
  //   "comp" - bits should be complmented when copied
  //   srcOffset - msbOffset of the first bit of the field to copy
  //   destOffset - msbOffset where field should be copied to
  //   bits - the bit length of the field

  public FieldCopier( String[] textParms )
  {
    super( textParms );
    int parmIndex = 0;
    for ( int i = 0; i < textParms.length; i ++ )
    {
      String text = textParms[ i ];
      if ( text.equalsIgnoreCase( "lsb" ))
        lsb = true;
      else if ( text.equalsIgnoreCase( "comp" ))
        comp = true;
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
        parmIndex++;
      }
    }
  }

  // called to store parms into hex data
  public void in( Value[] parms, Hex hex, DeviceParameter[] devParms, int onlyIndex )
  {
    int val = extract( hex, srcOffset, bits );
    if ( lsb )
      val = reverse( val, bits );
    if ( comp )
      val = complement( val, bits );
    insert( hex, destOffset, bits, val ); 
  }

  // called to extract parms from hex data
  public void out( Hex hex, Value[] parms, DeviceParameter[] devParms )
  {
    ;
  }

  private boolean comp = false;
  private boolean lsb = false;
  private int srcOffset;
  private int destOffset;
  private int bits;

  private final static int srcOffsetIndex = 0;
  private final static int destOffsetIndex = 1;
  private final static int bitsIndex = 2;
}
