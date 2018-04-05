package com.hifiremote.jp1.translate;

import com.hifiremote.jp1.DeviceParameter;
import com.hifiremote.jp1.Hex;
import com.hifiremote.jp1.Value;

// TODO: Auto-generated Javadoc
/**
 * The Class Sony1215Translator.
 */
public class Sony1215Translator extends Translate
{

  /**
   * Instantiates a new sony1215 translator.
   * 
   * @param textParms
   *          the text parms
   */
  public Sony1215Translator( String[] textParms )
  {
    super( textParms );
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.Translate#in(com.hifiremote.jp1.Value[], com.hifiremote.jp1.Hex,
   * com.hifiremote.jp1.DeviceParameter[], int)
   */
  @Override
  public void in( Value[] parms, Hex hexData, DeviceParameter[] devParms, int onlyIndex )
  {
    short[] hex = hexData.getData();

    int device1 = ( ( Number )parms[ 0 ].getValue() ).intValue();
    boolean force1 = ( ( Number )parms[ 1 ].getValue() ).intValue() != 0;
    int device2 = ( ( Number )parms[ 2 ].getValue() ).intValue();
    boolean force2 = ( ( Number )parms[ 3 ].getValue() ).intValue() != 0;

    if ( device1 > 31 || force1 )
    {
      hex[ 2 ] = 0x80;
    }
    else
    {
      hex[ 2 ] = 0;
    }

    if ( device2 > 31 || force2 )
    {
      hex[ 2 ] |= 0x40;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.Translate#out(com.hifiremote.jp1.Hex, com.hifiremote.jp1.Value[],
   * com.hifiremote.jp1.DeviceParameter[])
   */
  @Override
  public void out( Hex hexData, Value[] parms, DeviceParameter[] devParms )
  {
    short[] hex = hexData.getData();
    int device1 = ( ( Number )parms[ 0 ].getValue() ).intValue();
    int device2 = ( ( Number )parms[ 2 ].getValue() ).intValue();
    int force1 = 0;
    int force2 = 0;

    if ( ( hex[ 2 ] & 0x80 ) != 0 && device1 < 32 )
    {
      force1 = 1;
    }

    if ( ( hex[ 2 ] & 0x40 ) != 0 && device2 < 32 )
    {
      force2 = 1;
    }

    parms[ 1 ] = new Value( new Integer( force1 ), null );
    parms[ 3 ] = new Value( new Integer( force2 ), null );
  }

}
