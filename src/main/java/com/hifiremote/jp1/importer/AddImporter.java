package com.hifiremote.jp1.importer;

import com.hifiremote.jp1.Value;

public class AddImporter extends Importer
{
  int index = -1;
  int offset = 0;

  public AddImporter( String[] parms )
  {
    super( parms );
    index = Integer.parseInt( parms[ 0 ] );
    offset = Integer.parseInt( parms[ 1 ] );
  }

  @Override
  public Value[] convertParms( Value[] parms )
  {
    if ( parms[ index ] != null && parms[ index ].getValue() != null )
    {
      parms[ index ].setValue( Integer.parseInt( ( String )parms[ index ].getValue() ) + offset );
    }
    return parms;
  }
}
