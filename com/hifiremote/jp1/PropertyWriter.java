package com.hifiremote.jp1;

import java.io.*;

public class PropertyWriter
  extends FilterWriter
{
  public PropertyWriter( PrintWriter writer )
  {
    super( writer );
    this.writer = writer;
  }

  public void print( String name, String value )
  {
    writer.print( name );
    writer.print( '=' );
    
    if ( value != null )
    {
      boolean escapeSpace = true;
      for ( int i = 0; i < value.length(); i++ )
      {
        char ch = value.charAt( i );
        if ( ch == ' ' )
        {
          if ( escapeSpace )
            writer.print( "\\ " );
          else
            writer.print( ch );
        }
        else
        {
          escapeSpace = false;
          switch ( ch )
          {
            case '\\':
              writer.print( "\\\\" );
              break;
            case '\t':
              writer.print( "\\t" );
              break;
            case '\n':
              writer.print( "\\n" );
              break;
            case '\r':
              writer.print( "\\r" );
              break;
            case '#':
              writer.print( "\\#" );
              break;
            case '!':
              writer.print( "\\!" );
              break;
            case '=':
              writer.print( "\\=" );
              break;
            case ':':
              writer.print( "\\:" );
              break;
            default:
              writer.print( ch );
              break;
          }
        }
      }
    }
    writer.println();
  }

  PrintWriter writer = null;
}
