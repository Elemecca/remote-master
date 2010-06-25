/**
 * 
 */
package com.hifiremote.jp1.importer;

import com.hifiremote.jp1.Value;

/**
 * @author Greg
 */
public class AiwaDeviceImporter extends Importer
{
  /**
   * @param parms
   */
  public AiwaDeviceImporter( String[] parms )
  {
    super( parms );
    // TODO Auto-generated constructor stub
  }

  @Override
  public Value[] convertParms( Value[] parms )
  {
    Value[] newParms = new Value[ 2 ];
    int device = Integer.parseInt( ( String )parms[ 0 ].getValue() );
    newParms[ 0 ] = new Value( device & 0x00FF );
    newParms[ 1 ] = new Value( device >> 8 );
    return newParms;
  }
}
