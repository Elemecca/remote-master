package com.hifiremote.jp1;

import java.text.*;

public class HexFormat
  extends Format
{
  public HexFormat()
  {
    this( -1, -1 );
  }

  public HexFormat( int minLength, int maxLength )
  {
    this.minLength = minLength;
    this.maxLength = maxLength;
  }

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

  private int minLength;
  private int maxLength;
}

