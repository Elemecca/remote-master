package com.hifiremote.jp1.translate;

import com.hifiremote.jp1.DeviceParameter;
import com.hifiremote.jp1.Hex;
import com.hifiremote.jp1.Value;

// TODO: Auto-generated Javadoc
/**
 * The Class TranslatorWithDevBool.
 */
public class TranslatorWithDevBool extends Translator
{

  /** The dev index. */
  private int devIndex = 0;

  /**
   * Instantiates a new translator with dev bool.
   * 
   * @param textParms
   *          the text parms
   */
  public TranslatorWithDevBool( String[] textParms )
  {
    super( new String[ 0 ] );
    int parmIndex = 0;
    for ( int i = 0; i < textParms.length; i++ )
    {
      String text = textParms[ i ];
      if ( text.equalsIgnoreCase( "lsb" ) )
      {
        lsb = true;
      }
      else if ( text.equalsIgnoreCase( "comp" ) )
      {
        comp = true;
      }
      else
      {
        int val = Integer.parseInt( text );
        switch ( parmIndex )
        {
          case IndexIndex:
            devIndex = val;
            break;
          case IndexIndex + 1:
            index = val;
            break;
          case BitsIndex + 1:
            bits = val;
            break;
          case BitOffsetIndex + 1:
            bitOffset = val;
            break;
          case LsbOffsetIndex + 1:
            lsbOffset = val;
            break;
          case AdjustOffset + 1:
          {
            adjust = val;
            break;
          }
          default:
            break;
        }
        parmIndex++ ;
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.Translator#out(com.hifiremote.jp1.Hex, com.hifiremote.jp1.Value[],
   * com.hifiremote.jp1.DeviceParameter[])
   */
  @Override
  public void out( Hex hexData, Value[] parms, DeviceParameter[] devParms )
  {
    super.out( hexData, parms, devParms );
    Number v = ( Number )parms[ index ].getValue();
    Number i = ( Number )devParms[ devIndex ].getValueOrDefault();
    int val = ( i.intValue() << bits ) + v.intValue();
    parms[ index ] = new Value( new Integer( val ) );
  }
}
