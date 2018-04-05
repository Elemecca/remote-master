package com.hifiremote.jp1.translate;

import com.hifiremote.jp1.DeviceParameter;
import com.hifiremote.jp1.Hex;
import com.hifiremote.jp1.Value;

// TODO: Auto-generated Javadoc
/**
 * The Class TranslatorFromDev.
 */
public class TranslatorFromDev extends Translator
{

  /**
   * Instantiates a new translator from dev.
   * 
   * @param textParms
   *          the text parms
   */
  public TranslatorFromDev( String[] textParms )
  {
    super( textParms );
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.Translator#in(com.hifiremote.jp1.Value[], com.hifiremote.jp1.Hex,
   * com.hifiremote.jp1.DeviceParameter[], int)
   */
  @Override
  public void in( Value[] parms, Hex hexData, DeviceParameter[] devParms, int onlyIndex )
  {
    if ( index >= devParms.length )
    {
      System.err.println( "TranslatorFromDev.in() index=" + index + " exceeds " + devParms.length + " item buffer" );
      return;
    }
    int w = 0;

    Number i = ( Number )devParms[ index ].getValueOrDefault();
    if ( i == null )
    {
      System.err.println( "TranslatorFromDev.in() index=" + index + " missing parameter value" );
    }
    else
    {
      w = i.intValue() + adjust >> lsbOffset;
    }

    if ( comp )
    {
      w = 0xFFFFFFFF - w;
    }

    if ( lsb )
    {
      w = reverse( w, bits );
    }

    insert( hexData, bitOffset, bits, w );
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.Translator#out(com.hifiremote.jp1.Hex, com.hifiremote.jp1.Value[],
   * com.hifiremote.jp1.DeviceParameter[])
   */
  @Override
  public void out( Hex hexData, Value[] parms, DeviceParameter[] devParms )
  {}
}
