package com.hifiremote.jp1;

import java.text.*;

// TODO: Auto-generated Javadoc
/**
 * The Class HexFormat.
 */
public class HexFormat
  extends Format
{
  
  /**
   * Instantiates a new hex format.
   */
  public HexFormat()
  {
    this( -1, -1 );
  }

  /**
   * Instantiates a new hex format.
   * 
   * @param minLength the min length
   * @param maxLength the max length
   */
  public HexFormat( int minLength, int maxLength )
  {
    this.minLength = minLength;
    this.maxLength = maxLength;
  }

  /* (non-Javadoc)
   * @see java.text.Format#format(java.lang.Object, java.lang.StringBuffer, java.text.FieldPosition)
   */
  public StringBuffer format( Object obj,
                              StringBuffer buff,
                              FieldPosition pos )
  {
    if ( obj instanceof Hex )
    {
      buff.append((( Hex )obj ).toString());
    }
    return buff;
  }

  /* (non-Javadoc)
   * @see java.text.Format#parseObject(java.lang.String)
   */
  public Object parseObject( String text )
    throws ParseException
  {
    try 
    {
      Hex hex = new Hex( text );
      int len = hex.length();
      if ( minLength != -1  )
      {
        if ( len < minLength )
          throw new ParseException( "Too short", 0 );
        {
        }
      }
      if ( maxLength != -1 )
      {
        if ( len > maxLength )
          throw new ParseException( "Too long", text.length());
      }
      return hex;
    }
    catch ( NumberFormatException e )
    {
      throw new ParseException( e.getMessage(), 0 );
    }
  }

  /* (non-Javadoc)
   * @see java.text.Format#parseObject(java.lang.String, java.text.ParsePosition)
   */
  public Object parseObject( String text, ParsePosition pos )
  {
    int index = pos.getIndex();
    int i = 0;
    for ( i = index; i < text.length(); i++ )
    {
      char ch = text.charAt( i );
      if ( Character.digit( ch, 16 ) == -1 )
      {
        pos.setErrorIndex( i );
        return null;
      }
    }
    pos.setIndex( i );
    return new Hex( text.substring( index ));
  }

  /** The min length. */
  private int minLength;
  
  /** The max length. */
  private int maxLength;
}

