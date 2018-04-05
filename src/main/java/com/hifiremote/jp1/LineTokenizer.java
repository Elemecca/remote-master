/**
 * 
 */
package com.hifiremote.jp1;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author Greg
 */
public class LineTokenizer
{
  public static List< String > tokenize( String line, String delim )
  {
    StringTokenizer st = new StringTokenizer( line, delim, true );
    List< String > rc = new ArrayList< String >( st.countTokens() );
    while ( st.hasMoreTokens() )
    {
      String token = st.nextToken();
      if ( token.equals( delim ) )
      {
        rc.add( null );
        if ( !st.hasMoreTokens() )
        {
          rc.add( null );
        }
      }
      else
      {
        if ( token.startsWith( "\"" ) )
        {
          if ( token.endsWith( "\"" ) )
          {
            token = token.substring( 1, token.length() - 1 ).replaceAll( "\"\"", "\"" );
          }
          else
          {
            StringBuilder buff = new StringBuilder( 200 );
            buff.append( token.substring( 1 ) );
            while ( true )
            {
              token = st.nextToken(); // skip delim
              buff.append( delim );
              token = st.nextToken();
              if ( token.endsWith( "\"" ) )
              {
                buff.append( token.substring( 0, token.length() - 1 ) );
                break;
              }
              else
                buff.append( token );
            }
            token = buff.toString().replaceAll( "\"\"", "\"" );
          }
        }
        rc.add( token );
        if ( st.hasMoreTokens() )
        {
          st.nextToken(); // skip delim
          if ( !st.hasMoreTokens() )
          {
            rc.add( null );
          }
        }
      }
    }

    return rc;
  }
}
