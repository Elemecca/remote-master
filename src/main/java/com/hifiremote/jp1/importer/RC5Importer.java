package com.hifiremote.jp1.importer;

import com.hifiremote.jp1.Value;

// TODO: Auto-generated Javadoc
/**
 * The Class RC5Importer.
 */
public class RC5Importer extends Importer
{

  /**
   * Instantiates a new r c5 importer.
   * 
   * @param textParms
   *          the text parms
   */
  public RC5Importer( String[] textParms )
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
    for ( int i = 0; i < parms.length; i++ )
    {
      Value val = parms[ i ];
      int outIndex = 2 * i;
      if ( val == null )
      {
        outParms[ outIndex ] = null;
        outParms[ outIndex + 1 ] = null;
      }
      else
      {
        Object value = val.getUserValue();
        if ( value != null )
        {
          int parm = Integer.parseInt( ( String )value );
          if ( parm >= 100 )
          {
            val.setValue( Integer.toString( parm - 100 ) );
            outParms[ outIndex ] = val;
            outParms[ outIndex + 1 ] = new Value( new Integer( 1 ) );
          }
          else
          {
            outParms[ outIndex ] = val;
            outParms[ outIndex + 1 ] = new Value( new Integer( 0 ) );
          }
        }
      }
    }
    return outParms;
  }
}
