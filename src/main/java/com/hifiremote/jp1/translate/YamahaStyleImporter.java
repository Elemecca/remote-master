package com.hifiremote.jp1.translate;

import com.hifiremote.jp1.DeviceParameter;
import com.hifiremote.jp1.Hex;
import com.hifiremote.jp1.Value;

// TODO: Auto-generated Javadoc
/**
 * The Class YamahaStyleImporter.
 */
public class YamahaStyleImporter extends Translator
{

  /** The style parm index. */
  int styleParmIndex = 0;

  /**
   * Instantiates a new Yamaha style importer.
   * 
   * @param textParms
   *          the text parms
   */
  public YamahaStyleImporter( String[] textParms )
  {
    super( textParms );
    styleParmIndex = Integer.parseInt( textParms[ 0 ] );
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
    System.err.println( "YamahaStyleImporter.in" );
    System.err.println( "  parms:" );
    for ( int i = 0; i < parms.length; ++i )
    {
      Object obj = parms[ i ].getUserValue();
      System.err.println( "    ( " + obj.getClass().getName() + " )" + obj );
    }

    int styleIndex = 0;
    if ( styleParmIndex < parms.length )
    {
      Object obj = parms[ styleParmIndex ].getUserValue();
      if ( obj.getClass() == Integer.class )
      {
        styleIndex = ( ( Integer )obj ).intValue() - 1;
      }
      else
      {
        String styleStr = ( String )obj;
        if ( styleStr.equalsIgnoreCase( "Y1" ) )
        {
          styleIndex = 1;
        }
        else if ( styleStr.equalsIgnoreCase( "Y2" ) )
        {
          styleIndex = 2;
        }
        else
        {
          styleIndex = 3;
        }
      }
    }

    System.err.println( "  styleIndex=" + styleIndex );

    insert( hexData, 13, 1, styleIndex >> 1 );
    insert( hexData, 14, 1, styleIndex & 1 );
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
