package com.hifiremote.jp1.translate;

import com.hifiremote.jp1.DeviceParameter;
import com.hifiremote.jp1.Hex;
import com.hifiremote.jp1.Value;

// TODO: Auto-generated Javadoc
/**
 * The Class ZenithTranslator.
 */
public class ZenithTranslator extends Translate
{

  /**
   * Instantiates a new zenith translator.
   * 
   * @param textParms
   *          the text parms
   */
  public ZenithTranslator( String[] textParms )
  {
    super( textParms );
  }

  // Convert parms to hex
  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.Translate#in(com.hifiremote.jp1.Value[], com.hifiremote.jp1.Hex,
   * com.hifiremote.jp1.DeviceParameter[], int)
   */
  @Override
  public void in( Value[] parms, Hex hex, DeviceParameter[] devParms, int onlyIndex )
  {
    if ( parms[ 0 ] == null || parms[ 0 ].getValue() == null )
    {
      return;
    }

    Value val = parms[ 0 ];
    int i = ( ( Number )val.getValue() ).intValue();
    if ( val.hasUserValue() )
    {
      i++ ;
    }

    insert( hex, 4, 4, i );
  }

  // convert hex to parms
  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.Translate#out(com.hifiremote.jp1.Hex, com.hifiremote.jp1.Value[],
   * com.hifiremote.jp1.DeviceParameter[])
   */
  @Override
  public void out( Hex hex, Value[] parms, DeviceParameter[] devParms )
  {
    Integer val = null;
    int temp = extract( hex, 4, 4 );
    if ( temp != ( ( Number )devParms[ 0 ].getDefaultValue().value() ).intValue() )
    {
      temp-- ;
      val = new Integer( temp );
    }
    parms[ 0 ] = new Value( val, null );
  }
}
