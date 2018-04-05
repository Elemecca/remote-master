package com.hifiremote.jp1.importer;

import com.hifiremote.jp1.Value;

// TODO: Auto-generated Javadoc
/**
 * The Class ReorderImporter.
 */
public class ReorderImporter extends Importer
{

  /**
   * Instantiates a new reorder importer.
   * 
   * @param textParms
   *          the text parms
   */
  public ReorderImporter( String[] textParms )
  {
    super( textParms );
    int len = 0;
    while ( len < textParms.length && textParms[ len ] != null )
    {
      len++ ;
    }
    reorderedIndexes = new int[ len ];
    for ( int i = 0; i < len; i++ )
    {
      reorderedIndexes[ i ] = Integer.parseInt( textParms[ i ] );
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.Importer#convertParms(com.hifiremote.jp1.Value[])
   */
  public Value[] convertParms( Value[] parms )
  {
    Value[] outParms = new Value[ parms.length ];
    for ( int i = 0; i < reorderedIndexes.length; i++ )
    {
      outParms[ reorderedIndexes[ i ] ] = parms[ i ];
    }
    return outParms;
  }

  /** The reordered indexes. */
  private int[] reorderedIndexes = null;
}
