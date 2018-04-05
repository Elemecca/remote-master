package com.hifiremote.jp1.translate;

import com.hifiremote.jp1.DeviceParameter;
import com.hifiremote.jp1.Hex;
import com.hifiremote.jp1.Value;

// TODO: Auto-generated Javadoc
/**
 * The Class PanasonicMixTranslator.
 */
public class PanasonicMixTranslator extends Translate
{

  /**
   * Instantiates a new panasonic mix translator.
   * 
   * @param textParms
   *          the text parms
   */
  public PanasonicMixTranslator( String[] textParms )
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
    int iDev;
    if ( onlyIndex > 1 )
    {
      return;
    }
    Value dev = parms[ 0 ];
    Value sub = parms[ 1 ];
    if ( dev == null )
    {
      int v = hexData.getData()[ 1 ] & 0x3F;
      for ( iDev = 6; iDev > 0 && v > 0x20 >> iDev; iDev-- )
      {
        ;
      }
    }
    else
    {
      iDev = ( ( Number )dev.getValue() ).intValue();
    }
    iDev = 0x20 >> iDev;

    int iSub;
    if ( sub == null )
    {
      int v = hexData.getData()[ 1 ] & 0x3F;
      for ( iSub = 5; iSub > 0 && ( v & 0x10 >> iSub ) == 0; iSub-- )
      {
        ;
      }
      if ( v == 0x10 >> iSub )
      {
        iSub = 0;
      }
    }
    else
    {
      iSub = ( ( Number )sub.getValue() ).intValue();
    }
    if ( iSub != 0 )
    {
      iDev |= 0x10 >> iSub;
    }
    insert( hexData, 10, 6, iDev );
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.Translate#out(com.hifiremote.jp1.Hex, com.hifiremote.jp1.Value[],
   * com.hifiremote.jp1.DeviceParameter[])
   */
  @Override
  public void out( Hex hex, Value[] parms, DeviceParameter[] devParms )
  {
    int v = hex.getData()[ 1 ] & 0x3F;
    int d;
    for ( d = 0; d < 5 && v < 0x20 >> d; d++ )
    {
      ;
    }
    v -= 0x20 >> d;
    int s;
    for ( s = 4; s > 0 && v != 0x10 >> s; s-- )
    {
      ;
    }
    parms[ 0 ] = new Value( new Integer( d ), null );
    parms[ 1 ] = new Value( new Integer( s ), null );
  }

}
