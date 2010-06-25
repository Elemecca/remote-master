package com.hifiremote.jp1.translate;

import com.hifiremote.jp1.DeviceParameter;
import com.hifiremote.jp1.Hex;
import com.hifiremote.jp1.Value;

// TODO: Auto-generated Javadoc
/**
 * The Class Sony1220Translator.
 */
public class Sony1220Translator extends Translate
{

  /**
   * Instantiates a new sony1220 translator.
   * 
   * @param textParms
   *          the text parms
   */
  public Sony1220Translator( String[] textParms )
  {
    super( textParms );
    if ( textParms == null || textParms.length == 0 )
    {
      return;
    }

    deviceIndex = Integer.parseInt( textParms[ 0 ] );
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
    int device = 0;
    int flag = 1;

    Value val = parms[ deviceIndex ];
    if ( val.hasUserValue() )
    {
      flag = 0;
      device = ( ( Number )val.getUserValue() ).intValue();
    }

    insert( hexData, 7, 1, flag );
    insert( hexData, 8, 8, reverse( device ) );
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
    int flag = extract( hexData, 7, 1 );
    Integer device = null;
    if ( flag == 0 )
    {
      device = new Integer( reverse( extract( hexData, 8, 8 ) ) );
    }

    parms[ deviceIndex ] = new Value( device, null );
  }

  /** The device index. */
  private int deviceIndex = 1;
}
