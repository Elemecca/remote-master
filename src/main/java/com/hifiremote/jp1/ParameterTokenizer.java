/**
 * 
 */
package com.hifiremote.jp1;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author Greg
 * 
 */
public class ParameterTokenizer
{
  public static List< String > getTokens( String text )
  {
    StringTokenizer st = new StringTokenizer( text, ",", true );
    ArrayList< String > result = new ArrayList< String >();
    while ( st.hasMoreTokens() )
    {
      String token = st.nextToken().trim();
      if ( token.equals( "," ) )
      {
        result.add( null );
      }
      else
      {
        result.add( token );
        if ( st.hasMoreTokens() )
        {
          st.nextToken();
        }
      }
    }
    return result;
  }
}
