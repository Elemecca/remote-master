package com.hifiremote.jp1;

public class XorCheck
  extends Translate
{
  public XorCheck( String[] textParms )
  {
    super( textParms );
    int parmIndex = 0;
    for ( int i = 0; i < textParms.length; i ++ )
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
          sourceOffset = destOffset - val*bits;
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
      parmIndex++;
    }
    
  }

  public void in( Value[] parms, byte[] hex, DeviceParameter[] devParms, int onlyIndex )
  {
    // System.err.println("XorCheck(" + bits +","+ destOffset +","+ seed +","+ count +","+ sourceOffset +","+ step +").in(" + hex.length +")");
    int v = seed;
	int s = sourceOffset;
	for (int i=0; i<count; i++)
	{
		v ^= extract( hex, s, bits );
		s += step;		
	}
	insert( hex, destOffset, bits, v );
  }

  public void out( byte[] hex, Value[] parms, DeviceParameter[] devParms )
  {
  }

  private int bits = 8;
  private int destOffset = 8;
  private int seed = 0;
  private int count = 1;
  private int sourceOffset = 0;
  private int step = 8;

  private final static int bitsIndex = 0;
  private final static int destOffsetIndex = 1;
  private final static int seedIndex = 2;
  private final static int countIndex = 3;
  private final static int sourceOffsetIndex = 4;
  private final static int stepIndex = 5;
}


