package com.hifiremote.jp1;

public class ReorderImporter
  extends Importer
{
  public ReorderImporter( String[] textParms )
  {
    super( textParms );
    reorderedIndexes = new int[ textParms.length ];
    for ( int i = 0; i < textParms.length; i++ )
      reorderedIndexes[ i ] = Integer.parseInt( textParms[ i ]);
  }

  public Value[] convertParms( Value[] parms )
  {
    Value[] outParms = new Value[ parms.length ];
    for ( int i = 0; i < reorderedIndexes.length; i++ )
    {
      outParms[ reorderedIndexes[ i ]] = parms[ i ];
    }
    return outParms;
  }

  private int[] reorderedIndexes = null;
}
