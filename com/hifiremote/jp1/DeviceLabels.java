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

  public void parse( String text, Remote remote ) throws Exception
  {
    List< String > settings = ParameterTokenizer.getTokens( text );
    addr = RDFReader.parseNumber( settings.get( 0 ) );
    length = RDFReader.parseNumber( settings.get( 1 ) );
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
    int offset = addr + length * index;
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

  public void setText( String text, int index, short[] data )
  {
    if ( text == null || text.equals( "" ) )
    {
      text = getDefaultText( data, index );
    }
    text = text.trim().toUpperCase();

    int offset = addr + length * index;
    int i = 0, j = 0;
    int len = Math.min( length, text.length() );

    boolean invalidChar = false;
    while ( i < len )
    {
      Character ch = text.charAt( i );
      // Skip invalid characters
      if ( Character.isLetterOrDigit(ch) || ch.equals( ' ' ) || ch.equals( '.' ) )
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
}
