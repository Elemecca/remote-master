package com.hifiremote.jp1.translate;

import com.hifiremote.jp1.DeviceParameter;
import com.hifiremote.jp1.Hex;
import com.hifiremote.jp1.Value;

// TODO: Auto-generated Javadoc
/**
 * Description of the Class.
 * 
 * @author Graham
 * @created December 31, 2009
 */
public class GrundigXlator extends Translate
{
  /**
   * Constructor for the GrundigXlator object.
   * 
   * @param textParms
   *          the text parms
   */
  public GrundigXlator( String[] textParms )
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
  private int DataToHex( int value )
  {
    int hex = 0;
    int test = 2;
    for ( int bit = 0; bit < bits; bit += 2 )
    {
      hex |= ( value & test >> 1 ) << 1 | ( value & test ) >> 1 ^ value & test >> 1;
      test <<= 2;
    }
    return hex;
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
    // System.err.println( "GrundigXlator.in " + parmIndex +":" + bits + ":" + offset );
    int value = ( ( Number )parms[ parmIndex ].getValue() ).intValue();
    int dev = ( ( Number )devParms[ 0 ].getValueOrDefault() ).intValue();
    int bitsOut = bits;
    if ( ( bits & 1 ) == 1 )
    {
      value = value << 1 | dev >> 6;
      bitsOut += 1;
    }
    int hex = DataToHex( value );
    insert( hexData, offset, bitsOut, hex );
  }

  /**
   * Description of the Method.
   * 
   * @param value
   *          the value
   * @return Description of the Return Value
   */
  private int HexToData( int value )
  {
    int data = 0;
    int test = 1;
    for ( int bit = 0; bit < bits; bit += 2 )
    {
      data |= ( value & test << 1 ) >> 1 | ( value & test ) << 1 ^ value & test << 1;
      test <<= 2;
    }
    return data;
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
    int bitsIn = bits;
    int dev = ( ( Number )devParms[ 0 ].getValueOrDefault() ).intValue();
    if ( ( bits & 1 ) == 1 )
    {
      bitsIn += 1;
      insert( hexData, bits - 1, 1, dev >> 6 );
    }
    int hex = extract( hexData, offset, bitsIn );
    int data = HexToData( hex ) >> 1;
    parms[ parmIndex ] = new Value( new Integer( data ) );
  }

  /** The parm index. */
  private int parmIndex = 0;

  /** The bits. */
  private int bits = 0;

  /** The offset. */
  private int offset = 0;
}
