/**
 * 
 */
package com.hifiremote.jp1;

import java.util.List;

/**
 * @author Greg
 */
public class PauseParameters extends RDFParameter
{
  private String userName = "";
  @SuppressWarnings( "unused" )
  private int dataLength = 1;
  @SuppressWarnings( "unused" )
  private int offset = 0;
  @SuppressWarnings( "unused" )
  private int bytesUsed = 1;
  @SuppressWarnings( "unused" )
  private boolean lsb = false;
  @SuppressWarnings( "unused" )
  private float multiplier = 1f;

  public void parse( String text ) throws Exception
  {
    List< String > parms = ParameterTokenizer.getTokens( text );
    userName = parms.get( 0 );
    String value = parms.get( 1 );
    if ( value.equals( "1" ) )
    {
      dataLength = 1;
      offset = 0;
      bytesUsed = 1;
      lsb = false;
    }
    else if ( value.equals( "2/1" ) )
    {
      dataLength = 2;
      offset = 0;
      bytesUsed = 1;
      lsb = false;
    }
    else if ( value.equals( "2/2" ) )
    {
      dataLength = 2;
      offset = 1;
      bytesUsed = 1;
      lsb = false;
    }
    else if ( value.equals( "2/B" ) )
    {
      dataLength = 2;
      offset = 0;
      bytesUsed = 2;
      lsb = false;
    }
    else if ( value.equals( "2/L" ) )
    {
      dataLength = 2;
      offset = 0;
      bytesUsed = 2;
      lsb = true;
    }

    multiplier = Float.parseFloat( parms.get( 2 ) );
  }

  public String getUserName()
  {
    return userName;
  }
}
