package com.hifiremote.jp1;

public class RC5_5xImporter
  extends Translate
{
  public RC5_5xImporter( String[] textParms )
  {
    super( textParms );
  }

  public void in( Value[] parms, Hex hexData, DeviceParameter[] devParms, int parmToSet )
  {
    if ( parmToSet < 0 )
      return;

    if ( parms.length == 1 ) // RC-5
    {
      insert( hexData, 15, 1, 0 ); // Clear RC-5x bit
      int device = (( Integer )parms[ 0 ].getValue()).intValue();
      insert( hexData, 3, 5, complement( device, 5 )); // insert RC-5 Device
    }
    else // RC-5x
    {
      if ( parmToSet == 0 ) 
      {
        insert( hexData, 15, 1, 1 ); // Set RC-5x bit
        insert( hexData, 6, 6, extract( hexData, 8, 6 )); // extract RC-5 OBC and insert as RC-5x OBC
        insert( hexData, 13, 2, (( Integer )parms[ 0 ].getValue()).intValue() - 1 ); // insert RC-5x device index
      }
      else
      {
        int subDev = (( Integer )parms[ 1 ].getValue()).intValue();
        insert( hexData, 0, 6, complement( subDev, 6 ));
      }
    }
  }

  public void out( Hex hex, Value[] parms, DeviceParameter[] devParms )
  {}

  private static int Sony12 = 0;
  private static int Sony15 = 1;
  private static int Sony20 = 2;
}
