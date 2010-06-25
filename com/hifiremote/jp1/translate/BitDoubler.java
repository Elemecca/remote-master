package com.hifiremote.jp1.translate;

import com.hifiremote.jp1.DeviceParameter;
import com.hifiremote.jp1.Hex;
import com.hifiremote.jp1.Value;

// TODO: Auto-generated Javadoc
/**
 * Description of the Class.
 * 
 * @author Greg
 * @created December 1, 2006
 */
public class BitDoubler extends Translate
{

  /**
   * Constructor for the BitDoubler object.
   * 
   * @param textParms
   *          the text parms
   */
  public BitDoubler( String[] textParms )
  {
    super( textParms );
    parmIndex = Integer.parseInt( textParms[ 0 ] );
    bits = Integer.parseInt( textParms[ 1 ] );
    offset = Integer.parseInt( textParms[ 2 ] );
  }

  /**
   * Description of the Method.
   * 
   * @param value
   *          the value
   * @return Description of the Return Value
   */
  private int doubleIt( int value )
  {
    int doubled = 0;
    int test = 1;
    int zero = 1;
    int one = 2;
    for ( int bit = 0; bit < bits; ++bit )
    {
      if ( ( value & test ) == 0 )
      {
        doubled |= zero;
      }
      else
      {
        doubled |= one;
      }
      test <<= 1;
      zero <<= 2;
      one <<= 2;
    }
    return doubled;
  }

  /**
   * Description of the Method.
   * 
   * @param parms
   *          the parms
   * @param hexData
   *          the hex data
   * @param devParms
   *          the dev parms
   * @param onlyIndex
   *          the only index
   */
  public void in( Value[] parms, Hex hexData, DeviceParameter[] devParms, int onlyIndex )
  {
    int value = ( ( Number )parms[ parmIndex ].getValue() ).intValue();
    int doubled = doubleIt( value );
    insert( hexData, offset, bits * 2, doubled );
  }

  /**
   * Description of the Method.
   * 
   * @param value
   *          the value
   * @return Description of the Return Value
   */
  private int halveIt( int value )
  {
    int test = 3;
    int rc = 0;
    int one = 1;
    for ( int bit = 0; bit < bits; ++bit )
    {
      int val = value & test;
      if ( val == 2 )
      {
        rc |= one;
      }
      else if ( val == 0 || val == 3 )
      {
        System.err.println( "Error in halveIt: can't halve " + val );
      }
      value >>= 2;
      one <<= 1;
    }
    return rc;
  }

  /**
   * Description of the Method.
   * 
   * @param hexData
   *          the hex data
   * @param parms
   *          the parms
   * @param devParms
   *          the dev parms
   */
  public void out( Hex hexData, Value[] parms, DeviceParameter[] devParms )
  {
    int doubled = extract( hexData, offset, bits * 2 );
    int halved = halveIt( doubled );
    parms[ parmIndex ] = new Value( new Integer( halved ) );
  }

  /** The parm index. */
  private int parmIndex = 0;

  /** The bits. */
  private int bits = 0;

  /** The offset. */
  private int offset = 0;
}
