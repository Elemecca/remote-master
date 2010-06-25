package com.hifiremote.jp1.translate;

import com.hifiremote.jp1.DeviceParameter;
import com.hifiremote.jp1.Hex;
import com.hifiremote.jp1.Value;

// TODO: Auto-generated Javadoc
/**
 * The Class LutronTranslator.
 */
public class LutronTranslator extends Translate
{

  /**
   * Instantiates a new lutron translator.
   * 
   * @param textParms
   *          the text parms
   */
  public LutronTranslator( String[] textParms )
  {
    super( textParms );
    deviceOrCommand = Integer.parseInt( textParms[ 0 ] );
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.Translate#in(com.hifiremote.jp1.Value[], com.hifiremote.jp1.Hex,
   * com.hifiremote.jp1.DeviceParameter[], int)
   */
  public void in( Value[] parms, Hex hexData, DeviceParameter[] devParms, int onlyIndex )
  {
    if ( deviceOrCommand == DEVICE )
    {
      int device = ( ( Number )parms[ 0 ].getValue() ).intValue();
      int temp = ( device & 0xE0 ) >> 5;
      insert( hexData, 8, 4, encode[ temp ] );

      temp = ( device & 0x1C ) >> 2;
      insert( hexData, 12, 4, encode[ temp ] );
    }
    else
    {
      int device = ( ( Number )devParms[ 0 ].getValueOrDefault() ).intValue();
      if ( parms[ 1 ] != null && parms[ 1 ].getValue() != null )
      {
        device &= 0xFC;
        device |= ( ( Number )parms[ 1 ].getValue() ).intValue();
      }
      int temp = device & 3; // get last 2 bits
      temp <<= 1; // shift left 1

      int obc = ( ( Number )parms[ 0 ].getValue() ).intValue(); // get the OBC

      temp |= ( obc & 0x80 ) >> 7; // add in bit 0 of the OBC
      insert( hexData, 0, 4, encode[ temp ] ); // encode it and store it in the hex at bit offest 0

      temp = ( obc & 0x70 ) >> 4; // get bits 1-3 of the OBC
      insert( hexData, 4, 4, encode[ temp ] ); // encode it and store it in the hex at bit offset 4

      temp = ( obc & 0x0E ) >> 1; // get bits 4-6 of the OBC
      insert( hexData, 8, 4, encode[ temp ] ); // encode it and store it in the hex at bit offset 8

      temp = device ^ obc;
      int checksum = 0;
      checksum ^= temp & 0x03;
      temp >>= 2;
      checksum ^= temp & 0x03;
      temp >>= 2;
      checksum ^= temp & 0x03;
      temp >>= 2;
      checksum ^= temp & 0x03;

      temp = ( obc & 0x01 ) << 2; // get bit 7 of the OBC
      temp |= checksum; // add the checksum bits
      insert( hexData, 12, 4, encode[ temp ] );
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.Translate#out(com.hifiremote.jp1.Hex, com.hifiremote.jp1.Value[],
   * com.hifiremote.jp1.DeviceParameter[])
   */
  public void out( Hex hexData, Value[] parms, DeviceParameter[] devParms )
  {
    if ( deviceOrCommand == DEVICE )
    {
      int temp = decode( extract( hexData, 8, 4 ) );
      int device = temp << 5;
      temp = decode( extract( hexData, 12, 3 ) );
      device |= temp << 2;
      parms[ 0 ] = new Value( new Integer( device ) );
    }
    else
    {
      int temp = decode( extract( hexData, 0, 4 ) );
      int obc = ( temp & 1 ) << 7;
      int device = temp >> 1;
      temp = decode( extract( hexData, 4, 4 ) );
      obc |= temp << 4;
      temp = decode( extract( hexData, 8, 4 ) );
      obc |= temp << 1;
      temp = decode( extract( hexData, 12, 4 ) );
      obc |= temp >> 2;
      parms[ 0 ] = new Value( new Integer( obc ) );

      parms[ 1 ] = new Value( new Integer( device ) );
    }
  }

  /**
   * Decode.
   * 
   * @param val
   *          the val
   * @return the int
   */
  private int decode( int val )
  {
    for ( int i = 0; i < encode.length; i++ )
    {
      if ( encode[ i ] == val )
      {
        return i;
      }
    }
    System.err.println( "LutronTranslator.decode( " + val + " ) failed!" );
    return 0;
  }

  /** The encode. */
  private static int[] encode =
  {
      1, 2, 7, 4, 13, 14, 11, 8
  };

  /** The device or command. */
  private int deviceOrCommand = 0;

  /** The Constant DEVICE. */
  private final static int DEVICE = 0;

  /** The Constant COMMAND. */
  @SuppressWarnings( "unused" )
  private final static int COMMAND = 1;
}
