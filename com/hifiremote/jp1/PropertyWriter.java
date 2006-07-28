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
  
  public void printHeader( String name )
  {
    if ( !fresh )
      writer.println();
    fresh = false;
    writer.print( '[' );
    writer.print( name );
    writer.println( ']' );
  }

  public void print( String name, int value )
  {
    print( name, Integer.toString( value ));
  }
  
  public void print( String name, Object obj )
  {
    print( name, obj.toString());
  }
  
  public void print( String name, String value )
  {
    fresh = false;
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

  private PrintWriter writer = null;
  private boolean fresh = true;
}
