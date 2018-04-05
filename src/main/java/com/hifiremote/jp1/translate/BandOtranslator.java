package com.hifiremote.jp1.translate;

import com.hifiremote.jp1.DeviceParameter;
import com.hifiremote.jp1.Hex;
import com.hifiremote.jp1.Value;

// TODO: Auto-generated Javadoc
/**
 * The Class BandOtranslator.
 */
public class BandOtranslator extends Translate
{

  /**
   * Instantiates a new Bang & Olafsen translator.
   * 
   * @param textParms
   *          the text parms
   */
  public BandOtranslator( String[] textParms )
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
    int devHi, numHiBits, devLo = 0;
    if ( deviceOrCommand == DEVICE || deviceOrCommand == DEVICE_V4)
    {
      numHiBits = ( ( Number )parms[ 2 ].getValue() ).intValue();
      if (numHiBits > 4 || numHiBits < 1)  {
        numHiBits = 1;
        parms[ 2 ].setValue( ( Number ) numHiBits );
      }
      insert( hexData, 0, 8, numHiBits );
      devHi = ( ( Number )parms[ 0 ].getValue() ).intValue();
      devHi &= (0x01 <<  numHiBits) - 1;
      int temp = devHi << (8 - numHiBits);
      insert( hexData, 8 + deviceOrCommand * 8, 8, temp );        //00EB:4 is at offset 16
      parms[ 0 ].setValue( devHi );
      devLo = ( ( Number )parms[ 1 ].getValue() ).intValue();
      insert( hexData, 16 + deviceOrCommand * 8, 8, devLo);       //00EB:4 is at offset 24
      if ( deviceOrCommand == DEVICE_V4)  {
        int rptMask = ( ( Number )parms[ 3 ].getValue() ).intValue() << 5;
        insert( hexData, 8, 8, rptMask);
      }
    }
    else if (deviceOrCommand == COMMAND || deviceOrCommand ==  COMMAND_V4) 
    {
      boolean isOdd = false;
      int temp;
      int obc = ( ( Number )parms[ 0 ].getValue() ).intValue();
      temp = (obc ^ (obc >> 1)) & 0xFF;
      devLo = ( ( Number )devParms[ 1 ].getValueOrDefault() ).intValue();
      if ((devLo & 0x01) == 0x01) {
          temp |= 0x80;
          isOdd = true;
      }
      insert( hexData, 0, 8, temp); // encode it and store it in the hex at bit offset 0
      if (deviceOrCommand == COMMAND)  {  
        int obc2 = ( ( Number )parms[ 1 ].getValue() ).intValue(); 
        temp = obc2 ^ (obc2 >> 1);
        if (isOdd)
          temp |= 0x80;
        insert( hexData, 8, 8, temp); // encode it and store it in the hex at bit offset 8
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
    int devHi, numHiBits, devLo, rptMask;
    if ( deviceOrCommand == DEVICE || deviceOrCommand == DEVICE_V4)
    {
      numHiBits = extract( hexData, 0, 8 );
      if (numHiBits > 4 || numHiBits < 1)
        numHiBits = 1;
      parms[ 2 ] = new Value( new Integer( numHiBits ) ) ;
      devHi = extract( hexData, 8 + deviceOrCommand * 8, 8 ) >> (8 - numHiBits);  //00EB:4 is at offset 16
      devHi &= ( (0x01 <<  numHiBits) - 1 );
      parms[ 0 ].setValue( devHi );
      devLo = extract( hexData, 16 + deviceOrCommand * 8, 8 );                    //00EB:4 is at offset 24
      parms[ 1 ] = new Value( new Integer( devLo ) );
      if ( deviceOrCommand == DEVICE_V4) {
        rptMask = extract( hexData, 8, 8 ) >> 5;
        parms[ 3 ] = new Value( new Integer( rptMask ) );
      }
    }
    else if (deviceOrCommand == COMMAND || deviceOrCommand ==  COMMAND_V4) 
    {
      boolean isOdd = false;
      int obc, obc2, temp;
      temp = extract( hexData, 0, 8 );
      devLo = ( ( Number )devParms[ 1 ].getValueOrDefault() ).intValue();
      isOdd = (devLo & 0x01) == 0x01;
      obc = hexToBandO(temp, isOdd);           //obc depends on whether devLo is even or odd
      parms[ 0 ] = new Value( new Integer( obc ) );
      if (deviceOrCommand == COMMAND)  {  
        temp = extract( hexData, 8, 8 );
        obc2 = hexToBandO(temp, isOdd);
        parms[ 1 ] = new Value( new Integer( obc2 ) );
      }
    }
  }

  int hexToBandO(int hexData, boolean isOdd)  {
    int temp;
    temp = isOdd ? 0xFF : 0x00;
    if( (hexData & 0x80) == 0x80) temp ^= 0xFF; 
    if( (hexData & 0x40) == 0x40) temp ^= 0x7F; 
    if( (hexData & 0x20) == 0x20) temp ^= 0x3F; 
    if( (hexData & 0x10) == 0x10) temp ^= 0x1F; 
    if( (hexData & 0x08) == 0x08) temp ^= 0x0F; 
    if( (hexData & 0x04) == 0x04) temp ^= 0x07; 
    if( (hexData & 0x02) == 0x02) temp ^= 0x03; 
    if( (hexData & 0x01) == 0x01) temp ^= 0x01; 
    return temp;
  }

  /** The device or command. */
  private int deviceOrCommand = 0;

  /** The Constant DEVICE. */
  private final static int DEVICE = 0;
  private final static int DEVICE_V4 = 1;
  private final static int COMMAND = 2;
  private final static int COMMAND_V4 = 3;
}
