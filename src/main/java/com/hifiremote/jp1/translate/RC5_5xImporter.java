package com.hifiremote.jp1.translate;

import com.hifiremote.jp1.DeviceParameter;
import com.hifiremote.jp1.Hex;
import com.hifiremote.jp1.Value;

// TODO: Auto-generated Javadoc
/**
 * The Class RC5_5xImporter.
 */
public class RC5_5xImporter extends Translate
{

  /**
   * Instantiates a new r c5_5x importer.
   * 
   * @param textParms
   *          the text parms
   */
  public RC5_5xImporter( String[] textParms )
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
  public void in( Value[] parms, Hex hexData, DeviceParameter[] devParms, int parmToSet )
  {
    if ( parmToSet < 0 )
    {
      return;
    }

    if ( parms.length == 1 ) // RC-5
    {
      insert( hexData, 15, 1, 0 ); // Clear RC-5x bit
      int device = ( ( Number )parms[ 0 ].getValue() ).intValue();
      insert( hexData, 3, 5, complement( device, 5 ) ); // insert RC-5 Device
    }
    else
    // RC-5x
    {
      if ( parmToSet == 0 )
      {
        insert( hexData, 15, 1, 1 ); // Set RC-5x bit
        insert( hexData, 6, 6, extract( hexData, 8, 6 ) ); // extract RC-5 OBC and insert as RC-5x OBC
        insert( hexData, 13, 2, ( ( Number )parms[ 0 ].getValue() ).intValue() - 1 ); // insert RC-5x device index
      }
      else
      {
        int subDev = ( ( Number )parms[ 1 ].getValue() ).intValue();
        insert( hexData, 0, 6, complement( subDev, 6 ) );
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.Translate#out(com.hifiremote.jp1.Hex, com.hifiremote.jp1.Value[],
   * com.hifiremote.jp1.DeviceParameter[])
   */
  @Override
  public void out( Hex hex, Value[] parms, DeviceParameter[] devParms )
  {}

  /** The Sony12. */
  @SuppressWarnings( "unused" )
  private static int Sony12 = 0;

  /** The Sony15. */
  @SuppressWarnings( "unused" )
  private static int Sony15 = 1;

  /** The Sony20. */
  @SuppressWarnings( "unused" )
  private static int Sony20 = 2;
}
