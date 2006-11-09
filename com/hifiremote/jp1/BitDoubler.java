package com.hifiremote.jp1;

public class BitDoubler
  extends Translate
{
  public BitDoubler( String[] textParms )
  {
    super( textParms );
    parmIndex = Integer.parseInt( textParms[ 0 ]);
    bits = Integer.parseInt( textParms[ 1 ]);
    offset = Integer.parseInt( textParms[ 2 ]);
  }
  
  private int doubleIt( int value )
  {
    int doubled = 0;
    int test = 1;
    int zero = 1;
    int one = 2;
    for ( int bit = 0; bit < bits; ++ bit )
    {
      if (( value & test ) == 0 )
        doubled |= zero;
      else
        doubled |= one;
      test <<= 1;
      zero <<= 2;
      one <<= 2;
    }
    return doubled;  
  }

  public void in( Value[] parms, Hex hexData, DeviceParameter[] devParms, int onlyIndex )
  {
    int value = (( Number )parms[ parmIndex ].getValue()).intValue();
    int doubled = doubleIt( value );
    insert( hexData, offset, bits * 2, doubled );
  }
  
  private int halveIt( int value )
  {
    int halved = 0;
    int test = 3;
    int rc = 0;
    int one = 1;
    for ( int bit = 0; bit < bits; ++bit )
    {
      int val = value & test;
      if ( val == 2 )
        rc |= one;
      else if (( val == 0 ) || ( val == 3 ))
        System.err.println( "Error in halveIt: can't halve " + val );
      test <<= 2;
      one <<= 1;
    }
    return rc;
  }

  public void out( Hex hexData, Value[] parms, DeviceParameter[] devParms )
  {
    int doubled = extract( hexData, offset, bits * 2 );
    int halved = halveIt( doubled ); 
    parms[ parmIndex ] = new Value( new Integer( halved ));
  }

  private int parmIndex = 0;
  private int bits = 0;
  private int offset = 0;
}
