package com.hifiremote.jp1;

public class ReorderImporter
  extends Importer
{
  public ReorderImporter( String[] textParms )
  {
    super( textParms );
    int len = 0;
    while (( len < textParms.length ) && ( textParms[ len ] != null ))
      len++;
    reorderedIndexes = new int[ len ];
    for ( int i = 0; i < len; i++ )
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
