package com.hifiremote.jp1;

import java.awt.Dimension;
import java.util.StringTokenizer;

public class ParameterFactory
{
  public static Parameter createParameter( String string )
  {
    Parameter rc = null;

    StringTokenizer st = new StringTokenizer( string, ":=", true );
    int defaultValue = -1;
    int bits = -1;
    String name = st.nextToken();
    String[] choices = null;
    Dimension d = null;
    while ( st.hasMoreTokens())
    {
      String sep = st.nextToken();
      if ( sep.equals( "=" ))
      {
        defaultValue = Integer.parseInt( st.nextToken());
      }
      else if ( sep.equals( ":" ))
      {
        String str = st.nextToken();
        if ( str.indexOf( '|' ) != -1 )
        {
          StringTokenizer st2 = new StringTokenizer( str, "|" );
          int numChoices = st2.countTokens();
          choices = new String[ numChoices + 1 ];
          choices[ 0 ] = "";
          for ( int j = 0; j < numChoices; j++ )
            choices[ j + 1 ] = st2.nextToken();
        }
        else if ( str.indexOf( '-' ) != -1 )
        {
          StringTokenizer st3 = new StringTokenizer( str, "-" );
          d = new Dimension( Integer.parseInt( st3.nextToken()),
                             Integer.parseInt( st3.nextToken()));

        }
        else
        {
          bits = Integer.parseInt( str );
        }
      }
    }
    if ( choices != null )
      rc = new ChoiceParameter( name, defaultValue, choices );
    else if ( bits != -1 )
      rc = new NumberParameter( name, defaultValue, bits );
    else if ( d != null )
      rc = new NumberParameter( name, defaultValue, d.width, d.height );
    else
      rc = new NumberParameter( name, defaultValue );

    return rc;
  }
}
