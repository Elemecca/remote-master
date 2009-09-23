package com.hifiremote.jp1;

import java.io.BufferedReader;
import java.io.IOException;

// TODO: Auto-generated Javadoc
/**
 * The Class PropertyReader.
 */
public class PropertyReader
{

  /**
   * Instantiates a new property reader.
   * 
   * @param reader
   *          the reader
   */
  public PropertyReader( BufferedReader reader )
  {
    this.reader = reader;
  }

  /**
   * Next property.
   * 
   * @return the property
   */
  public Property nextProperty()
  {
    Property property = new Property();
    try
    {
      String line;
      do
      {
        line = reader.readLine();
        if ( line == null )
          return null;
        line = line.trim();
      }
      while ( ( line.length() != 0 ) && ( ( line.charAt( 0 ) == '#' ) || ( line.charAt( 0 ) == '!' ) ) );

      int pos = line.indexOf( '=' );
      if ( pos == -1 )
        pos = line.indexOf( ':' );
      if ( pos == -1 )
      {
        property.name = line;
        property.value = "";
        return property;
      }
      property.name = line.substring( 0, pos );
      if ( property.name.charAt( pos - 1 ) == ':' )
        property.name = property.name.substring( 0, pos - 1 );
      line = line.substring( pos + 1 );
      while ( line.endsWith( "\\" ) )
        line = line.substring( 0, line.length() - 1 ).trim() + reader.readLine().trim();
      property.value = decode( line );
    }
    catch ( IOException e )
    {
      e.printStackTrace( System.err );
    }
    return property;
  }

  /**
   * Decode.
   * 
   * @param text
   *          the text
   * @return the string
   */
  public static String decode( String text )
  {
    StringBuilder buff = new StringBuilder( text.length() );
    char[] chars = text.toCharArray();
    for ( int i = 0; i < chars.length; i++ )
    {
      char ch = chars[ i ];
      if ( ch == '\\' )
      {
        ch = chars[ ++i ];
        if ( ch == 'n' )
          buff.append( '\n' );
        else if ( ch == 't' )
          buff.append( '\t' );
        else if ( ch == 'r' )
          buff.append( '\r' );
        else if ( ch == 'u' )
        {
          String val = new String( chars, ++i, 4 );
          buff.append( ( char )Integer.parseInt( val ) );
          i += 4;
        }
        else
          buff.append( ch );
      }
      else if ( ch == '\u00AE' )
        buff.append( '\n' );
      else
        buff.append( ch );
    }
    return buff.toString();
  }

  /**
   * Next section.
   * 
   * @return the ini section
   */
  public IniSection nextSection()
  {
    Property p = nextProperty();
    // skip empty lines
    while ( ( p != null ) && ( p.name.length() == 0 ) )
      p = nextProperty();

    if ( p == null )
      return null;

    IniSection section = new IniSection();
    if ( p.name.charAt( 0 ) == '[' )
    {
      section.setName( p.name.substring( 1, p.name.length() - 1 ) );
      p = nextProperty();
    }
    while ( ( p != null ) && ( p.name.length() != 0 ) )
    {
      section.add( p );
      p = nextProperty();
    }
    return section;
  }

  /** The reader. */
  private BufferedReader reader = null;
}
