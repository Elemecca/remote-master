/**
 * 
 */
package com.hifiremote.jp1.clipboard;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Greg
 */
public class HTMLClipboardReader extends ClipboardReader
{
  public HTMLClipboardReader( Reader in )
  {
    rdr = new PushbackReader( in );
  }

  private PushbackReader rdr = null;
  boolean ignore = true;

  protected String nextToken() throws IOException
  {
    StringBuffer buff = new StringBuffer();
    int i = rdr.read();
    if ( i < 0 )
    {
      return null;
    }
    if ( i >= 0 )
    {
      char ch = ( char )i;
      buff.append( ch );
      boolean pushback = false;
      char term = '>';

      if ( i != '<' )
      {
        term = '<';
        pushback = true;
      }
      for ( i = rdr.read(); i > 0; i = rdr.read() )
      {
        ch = ( char )i;
        if ( ch == term )
        {
          if ( pushback )
          {
            rdr.unread( i );
          }
          else
          {
            buff.append( ch );
          }
          break;
        }
        else
        {
          buff.append( ch );
        }
      }
    }
    return buff.toString().trim();
  }

  public List< String > readNextLine() throws IOException
  {
    String token = null;
    List< String > tokens = new ArrayList< String >();
    boolean inRow = false;
    boolean inCell = false;
    while ( ( token = nextToken() ) != null )
    {
      String lowerToken = token.toLowerCase();
      if ( lowerToken.equals( "<!--startfragment-->" ) )
      {
        ignore = false;
        continue;
      }
      if ( ignore )
      {
        continue;
      }
      if ( lowerToken.equals( "<!--endfragment-->" ) )
      {
        ignore = true;
        continue;
      }
      if ( lowerToken.startsWith( "<tr" ) )
      {
        inRow = true;
        continue;
      }
      if ( !inRow )
      {
        continue;
      }
      if ( lowerToken.equals( "</tr>" ) )
      {
        inRow = false;
        break;
      }
      if ( lowerToken.startsWith( "<td" ) || lowerToken.startsWith( "<th" ) )
      {
        inCell = true;
        continue;
      }
      if ( lowerToken.equals( "</td>" ) || lowerToken.equals( "</th>" ) )
      {
        inCell = false;
        continue;
      }
      if ( inCell )
      {
        tokens.add( token );
      }
    }
    if ( token == null )
    {
      return null;
    }
    return tokens;
  }

  public void close() throws IOException
  {
    rdr.close();
  }

}
