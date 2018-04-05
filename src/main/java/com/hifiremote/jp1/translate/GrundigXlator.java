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
    int bitsOut = bits;
    if ( ( bits & 1 ) == 1 )
    {
      // Command parameter.  If dev6 is bit 6 of 7-bit device code then DataToHex() maps 8
      // bits of value ( OBC << 1 ) | dev6 to command hex, with dev6 preserved as bit 1 of 
      // this hex.  Note that the value of dev6 affects the mapping between OBC and
      // the other 7 bits of command hex.
      bitsOut += 1;
      value = value << 1 | hexData.getData()[ 0 ] >> 1 & 1;
    }
    // else: Device code.  DataToHex maps bits 0-5 of device code to bits 0-5 of device hex.
    // Bit 7 of device hex is bit 7 of OBC, so a given fixed byte supports OBC values either
    // < 128 or >= 128.
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
    int bitsIn = bits + ( bits & 1 );
    int hex = extract( hexData, offset, bitsIn );
    int value = HexToData( hex );
    if ( ( bits & 1 ) == 1 )
    {
      // Command byte.  Remove bit 0, which is bit 6 of device code, to leave lowest 7 bits
      // of OBC.
      value >>= 1;
      // Set top bit of 8-bit OBC from device parameter flag.
      int obcTop = ( ( Number )devParms[ 1 ].getValueOrDefault() ).intValue();
      value += obcTop << bits;
    }
    parms[ parmIndex ]= insert( parms[ parmIndex ], 0, bitsIn, value );
  }

  /** The parm index. */
  private int parmIndex = 0;

  /** The bits. */
  private int bits = 0;

  /** The offset. */
  private int offset = 0;
}
