package com.hifiremote.jp1;

public class RC5Importer
  extends Importer
{
  public RC5Importer( String[] textParms )
  {
    super( textParms );
  }

  public Value[] convertParms( Value[] parms )
  {
    Value[] outParms = new Value[ 6 ];
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
          int parm = Integer.parseInt(( String )value );
          if ( parm >= 100 )
          {
            val.setValue( Integer.toString( parm - 100 ));
            outParms[ outIndex ] = val;
            outParms[ outIndex + 1 ] = new Value( new Integer( 1 ));
          }
          else
          {
            outParms[ outIndex ] = val;
            outParms[ outIndex + 1 ] = new Value( new Integer( 0 ));
          }
        }
      }
    }
    return outParms;
  }
}
