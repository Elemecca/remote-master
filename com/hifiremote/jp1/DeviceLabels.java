/**
 * 
 */
package com.hifiremote.jp1;

import java.util.List;

import javax.swing.JOptionPane;

/**
 * @author Greg
 */
public class DeviceLabels extends RDFParameter
{
  private int addr = 0;
  private int length = 0;
  private short fill = 0x20;
  private int defaultsAddr = 0;
  public static String columnName = "Label";
  public static String columnName2 = null;

  public void parse( String text, Remote remote ) throws Exception
  {
    List< String > settings = ParameterTokenizer.getTokens( text );
    addr = RDFReader.parseNumber( settings.get( 0 ) );
    length = RDFReader.parseNumber( settings.get( 1 ) );
    columnName = "Label";
    if ( settings.size() > 2 )
    {
      String token = settings.get( 2 );
      if ( token != null )
      {
        fill = ( short )( RDFReader.parseNumber( token ) & 0xFF );
      }
    }
    if ( settings.size() > 3 )
    {
      String token = settings.get( 3 );
      if ( token != null )
      {
        defaultsAddr = RDFReader.parseNumber( token );
      }
    }
    if ( settings.size() > 4 )
    {
      String token = settings.get( 4 );
      if ( token != null )
      {
        columnName = token;
      }
    }
    if ( settings.size() > 5 )
    {
      String token = settings.get( 5 );
      if ( token != null )
      {
        columnName2 = token;
      }
    }
  }

  public int getAddr()
  {
    return addr;
  }

  public int getLength()
  {
    return length;
  }

  public short getFill()
  {
    return fill;
  }

  public int getDefaultsAddr()
  {
    return defaultsAddr;
  }
  
  public boolean usesDefaultLabels()
  {
    return defaultsAddr != 0;
  }
  

  public String getDefaultText( short[] data, int index )
  {
    if ( defaultsAddr == 0 )
    {
      return "";
    }

    int offset = defaultsAddr + length * index;
    char[] text = new char[ length ];

    // copy from data
    for ( int i = 0; i < length; i++ )
    {
      text[ i ] = ( char )data[ offset + i ];
    }

    // now trim fill bytes from the end
    int pos = length;
    while ( ( pos > 0 ) && ( text[ pos - 1 ] == fill ) )
    {
      --pos;
    }

    return new String( text, 0, pos );
  }
  
  public void setDefaultText( String text, int index, short[] data )
  {
    if ( defaultsAddr == 0 )
      return;
    if ( text == null )
    {
      text = "";
    }
    text = text.trim();

    int offset = defaultsAddr + length * index;
    int i = 0;
    int len = Math.min( length, text.length() );

    while ( i < len )
    {
      data[ offset + i ] = ( short )text.charAt( i );
      ++i;
    }

    // fill
    while ( i < length )
    {
      data[ offset + i ] = fill;
      ++i;
    }
  }


  public String getText( short[] data, int index )
  {
    if ( addr == 0 || addr >= data.length )
    {
      return "";
    }
    int length = this.length;
    int offset = addr + length * index;
    if ( length == 0 )
    {
      // length is given as first byte of label data
      length = data[ addr - 1 ];
//      offset++;
    }
    
    char[] text = new char[ length ];

    // copy from data
    for ( int i = 0; i < length; i++ )
    {
      text[ i ] = ( char )data[ offset + i ];
    }

    // now trim fill bytes from the end
    int pos = length;
    while ( ( pos > 0 ) && ( text[ pos - 1 ] == fill ) )
    {
      --pos;
    }

    return new String( text, 0, pos );
  }
  
  //* Name of model for XSight Touch follows label which is brand name */
  public String getText2( short[] data )
  {
    int length = data[ addr - 1 ];
    int offset = addr + length + 1;
    if ( offset > data.length || data[ offset - 1 ] == 0 )
    {
      return null;
    }
    int length2 = Math.min( data[ offset - 1 ], data.length - offset );
    char[] text = new char[ length2 ];
    for ( int i = 0; i < length2; i++ )
    {
      text[ i ] = ( char )data[ offset + i ];
    }
    return new String( text );
  }

  public void setText( String text, int index, short[] data )
  {
    if ( text == null || text.equals( "" ) )
    {
      text = getDefaultText( data, index );
    }
    text = text.trim();
    
    int length = this.length;
    boolean allowInvalidChars = false;
    
    if ( length == 0 && addr <= data.length )
    {
      // length is given as first byte of label data
      length = data[ addr - 1 ];
      allowInvalidChars = true;
    }
    else
    {
      // remotes with fixed label length only use upper case
      text = text.toUpperCase();
    }

    int offset = addr + length * index;
    int i = 0, j = 0;
    int len = Math.min( length, text.length() );

    boolean invalidChar = false;
    while ( i < len )
    {
      Character ch = text.charAt( i );
      // Skip invalid characters
      if ( allowInvalidChars || Character.isLetterOrDigit(ch) || ch.equals( ' ' ) || ch.equals( '.' ) )
      {
        data[ offset + j++ ] = ( short )text.charAt( i );
      }
      else
      {
        invalidChar = true;
      }
      i++;
    }

    // fill
    while ( j < length )
    {
      data[ offset + j++ ] = fill;
    }
    
    if ( invalidChar )
    {        
      String message = "One or more invalid characters have been\r\n"
        + "deleted.  The only allowed characters are\r\n"
        + "letters, digits, space and full stop.";
      JOptionPane.showMessageDialog( null, message );
    }  
  }

  public void setText2( String text, short[] data )
  {
    Hex.put( new Hex( text.trim(), 8 ).getData(), data, 14 + data[ 12 ] );
  }
}
