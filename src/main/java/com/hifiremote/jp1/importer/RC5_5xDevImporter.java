package com.hifiremote.jp1.importer;

import com.hifiremote.jp1.Value;

// TODO: Auto-generated Javadoc
/**
 * The Class RC5_5xDevImporter.
 */
public class RC5_5xDevImporter extends Importer
{

  /**
   * Instantiates a new r c5_5x dev importer.
   * 
   * @param textParms
   *          the text parms
   */
  public RC5_5xDevImporter( String[] textParms )
  {
    super( textParms );
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.Importer#convertParms(com.hifiremote.jp1.Value[])
   */
  public Value[] convertParms( Value[] parms )
  {
    Value[] outParms = new Value[ 2 * parms.length ];
    Value nullValue = new Value( null );
    Value zero = new Value( new Integer( 0 ) );
    Value one = new Value( new Integer( 1 ) );
    for ( int i = 0; i < parms.length; i++ )
    {
      Value val = parms[ i ];
      int outIndex = 2 * i;
      if ( val == null )
      {
        outParms[ outIndex ] = nullValue;
        outParms[ outIndex + 1 ] = nullValue;
      }
      else
      {
        Object value = val.getUserValue();
        if ( value != null )
        {
          boolean match = false;
          outParms[ outIndex ] = val;
          Value flag = zero;
          for ( int j = i - 1; j >= 0 && !match; j-- )
          {
            if ( parms[ j ] == null )
            {
              continue;
            }
            if ( parms[ j ].getValue().equals( value ) )
            {
              match = true;
              if ( outParms[ 2 * j + 1 ] == zero )
              {
                flag = one;
              }
              else
              {
                flag = zero;
              }
            }
          }
          outParms[ outIndex + 1 ] = flag;
        }
      }
    }
    return outParms;
  }
}
