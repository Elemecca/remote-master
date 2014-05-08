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
    int device = 0;
    if ( deviceOrCommand == DEVICE )
    {
      device = ( ( Number )parms[ 0 ].getValue() ).intValue();
      int temp = ( device & 0xE0 ) >> 5;
      insert( hexData, 8, 4, encode[ temp ] );
      temp = ( device & 0x1C ) >> 2;
      insert( hexData, 12, 4, encode[ temp ] );
    }
    else if ( deviceOrCommand == DEVICE_V4) {
      device = ( ( Number )parms[ 0 ].getValue() ).intValue();
      int temp = ( device & 0xE0 ) >> 5;
      insert( hexData, 0, 4, encode[ temp ] );
      temp = ( device & 0x1C ) >> 2;
      insert( hexData, 4, 4, encode[ temp ] );
      calcV4FixedData(device, devParms );
      for (int f=0; f<4; f++) {
        insert( hexData, f*8,     4,  (fd[f] >> 4) & 0x0F );
        insert( hexData, f*8 + 4, 4,   fd[f]       & 0x0F );
      }
    }
    else if (deviceOrCommand == COMMAND) 
    {
      device = ( ( Number )devParms[ 0 ].getValueOrDefault() ).intValue();
      if ( parms[ 1 ] != null && parms[ 1 ].getValue() != null )
      {
        device &= 0xFC;
        device |= ( ( Number )parms[ 1 ].getValue() ).intValue();
      }
      int temp = device & 3; // get last 2 bits
      temp <<= 1; // shift left 1
      int obc = ( ( Number )parms[ 0 ].getValue() ).intValue(); // get the OBC
      temp |= ( obc & 0x80 ) >> 7; // add in bit 7 of the OBC
      insert( hexData, 0, 4, encode[ temp ] ); // encode it and store it in the hex at bit offset 0
      temp = ( obc & 0x70 ) >> 4; // get bits 4-6 of the OBC
      insert( hexData, 4, 4, encode[ temp ] ); // encode it and store it in the hex at bit offset 4
      temp = ( obc & 0x0E ) >> 1; // get bits 1-3 of the OBC
      insert( hexData, 8, 4, encode[ temp ] ); // encode it and store it in the hex at bit offset 8
      temp = calcLastOctalNibble(device, obc);
      insert( hexData, 12, 4, encode[ temp ] );
    }
    else   //using variant 4 executor
    {
      device = ( ( Number )devParms[ 0 ].getValueOrDefault() ).intValue();
      calcV4FixedData(device, devParms );
      if ( parms[ 0 ] != null && parms[ 0 ].getValue() != null )
      {
        int obc = ( ( Number )parms[ 0 ].getValue() ).intValue();
        int temp = findFixedDataForGrp( obc >> 4);
        if (temp < 0)   // no matching FD for this OBC
          obc = (( Number )devParms[ 1 ].getValueOrDefault() ).intValue() << 4;  
        insert( hexData, 0, 4, encode[ (obc & 0x0E) >> 1 ] ); //bits 1-3 of the OBC
        temp = calcLastOctalNibble(device, obc);
        insert( hexData, 4, 4, encode[ temp ] );
        temp = findFixedDataForGrp( obc >> 4);
        insert( hexData, 14, 2, temp - 1 );  //bits 0 and 1 of 2nd cmd byte are selectors for which Fixed Data to use 
      }
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
    else if ( deviceOrCommand == DEVICE_V4) {
      int temp = decode( extract( hexData, 0, 4 ) );
      int device = temp << 5;
      temp = decode( extract( hexData, 4, 4 ) );
      device |= temp << 2;
      temp = decode( extract( hexData, 8, 4 ) );
      device |= temp >> 1; 
      parms[ 0 ] = new Value( new Integer( device ) );
      int grp;
      for (int p=1; p<4; p++) {
        temp = decode( extract( hexData, p*8, 4 ) );
        grp = (temp &0x01) << 3;
        temp = decode( extract( hexData, p*8+4, 4 ) );
        grp |= temp;
        parms[ p ] = new Value( new Integer( grp ) );
      }
    }
    else if (deviceOrCommand == COMMAND) 
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
    else  // Command_V4
    {
      int device = ( ( Number )devParms[ 0 ].getValueOrDefault() ).intValue();
      calcV4FixedData(device, devParms );
      int fdIdx = extract(hexData, 14,2) + 1;
      int obc = OBCgrp[fdIdx] << 4;
      int temp = extract( hexData, 0, 4 );
      if (temp == 0) 
        temp = 1;    // at initial usage hexData is zero, which is an invalid value
      temp = decode( temp ); 
      obc |= temp << 1;
      temp = extract( hexData, 4, 4 );
      if (temp == 0) 
        temp = 1;
      temp = decode( temp ); 
      obc |= (temp & 4) >> 2;
      parms[ 0 ] = new Value( new Integer( obc ) );
      temp = extract( hexData, 8, 2 );
      parms[ 1 ] = new Value( new Integer( temp ) );
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
  private static int[] encode =  //encodes to 3 bit Gray code and appends a parity bit for odd parity
  {
      1, 2, 7, 4, 13, 14, 11, 8
  };
  
  private void calcV4FixedData(int device, DeviceParameter[] devParms ) {
    int temp;
    fd[0] = (encode[ (device & 0xE0) >> 5 ] << 4) + encode[( device & 0x1C ) >> 2];
    for (int p=1; p<4; p++) {
      if ( devParms[ p ] != null && devParms[ p ].getValueOrDefault() != null ) {
        OBCgrp[p] = ( ( Number )devParms[ p ].getValueOrDefault() ).intValue();
        temp = ( (device & 0x03) << 1 ) + ((OBCgrp[p] >> 3) & 0x01);
        fd[p] = (encode[ temp ] << 4) + encode[ (OBCgrp[p] & 0x07) ];
      }
    }
  }
  
  private int calcLastOctalNibble(int device, int obc) {
    int temp = device ^ obc;
    int checksum = 0;
    checksum ^= temp & 0x03;
    temp >>= 2;
    checksum ^= temp & 0x03;
    temp >>= 2;
    checksum ^= temp & 0x03;
    temp >>= 2;
    checksum ^= temp & 0x03;
    temp = ( obc & 0x01 ) << 2; // get bit 0 of the OBC
    temp |= checksum; // add the checksum bits
    return temp;
  }
  
  private int findFixedDataForGrp(int grp) {
    int i;
    for (i=1; i<4; i++) {
      if( OBCgrp[i] == grp)
        break;
    }
    if (i < 4 ) 
      return i;
    else 
      return -1; // grp not found
  }

  /** The device or command. */
  private int deviceOrCommand = 0;
  private  int[] fd = {0x11,0x12,0x13,0x14};
  private  int[] OBCgrp = {0x21,0x22,0x23,0x24};

  /** The Constant DEVICE. */
  private final static int DEVICE = 0;
  private final static int COMMAND = 1;
  private final static int DEVICE_V4 = 2;
  /** The Constant COMMAND. */
  @SuppressWarnings( "unused" )
  
  private final static int COMMAND_V4 = 3;
}
