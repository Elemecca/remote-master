package com.hifiremote.jp1;

import java.text.*;

public class HexFormat
  extends Format
{
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

  public Object parseObject( String text, ParsePosition pos )
  {
    try
    {
      return parseObject( text );
    }
    catch ( Exception e )
    {
      e.printStackTrace( System.err );
    }
    return null;
  }

  private int minLength;
  private int maxLength;
}

