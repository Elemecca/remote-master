package com.hifiremote.jp1;

import java.util.StringTokenizer;

public class Hex
  implements Cloneable, Comparable
{
  public Hex()
  {
    data = new int[ 0 ];
  }

  public Hex( int length )
  {
    data = new int[ length ];
  }

  public Hex( String text )
  {
    data = parseHex( text );
  }

  public Hex( int[] data )
  {
    this.data = data;
  }

  public Hex( Hex h )
  {
    data = new int[ h.data.length ];
    for ( int i = 0; i < h.data.length; i++ )
      data[ i ] = h.data[ i ];
  }

  public int length()
  {
    return data.length;
  }

  public int[] getData()
  {
    return data;
  }

  public void set( int[] data )
  {
    this.data = data;
  }

  public void set( String text )
  {
    data = parseHex( text );
  }

  public static int[] parseHex( String text )
  {
    int[] rc = null;
    int length = 0;
    int space = text.indexOf( ' ' );
    if (( space == -1 ) && ( text.length() > 3 ))
    {
      length = text.length() / 2;
      rc = new int[ length ];
      for ( int i = 0; i < length; i++ )
      {
        int offset = i * 2;
        String temp = text.substring( offset, offset + 2 );
        rc[ i ] = Integer.parseInt( temp, 16 );
      }
    }
    else
    {
      StringTokenizer st = new StringTokenizer( text, " _.$h\n\r" );
      length = st.countTokens();
      rc = new int[ length ];
      st = new StringTokenizer( text, " _.$h\n\r", true );
      int i = 0;
      int value = 0;
      while ( st.hasMoreTokens())
      {
        String token = st.nextToken();
        if ( token.equals( " " ) || token.equals( "$" ) || token.equals( "h" ) || token.equals( "\n") || token.equals( "\r" ))
          value = 0;
        else if ( token.equals( "_" ))
          value = ADD_OFFSET;
        else if ( token.equals( "." ))
          value = NO_MATCH;
        else
          rc[ i++ ] = value + Integer.parseInt( token, 16 );
      }
    }

    return rc;
  }

  public static String asString( int value )
  {
    StringBuffer buff = new StringBuffer( 2 );
    String str = Integer.toHexString( value & 0xFF );
    if ( str.length() < 2 )
      buff.append( '0' );
    buff.append( str );
    return buff.toString();
  }

  public String toRawString()
  {
    if ( data == null )
      return null;

    StringBuffer rc = new StringBuffer( 3 * data.length );
    for ( int i = 0; i < data.length; i++ )
    {
      int val = data[ i ];
      int masked = val & 0x00FF;

      if ( i > 0 )
      {
        char sep = ' ';
        int flag = val & 0xFF00;
        if ( flag == NO_MATCH )
          sep = ',';
        else if ( flag == ADD_OFFSET )
          sep = '_';

        rc.append( sep );
      }

      String str = Integer.toHexString( masked );
      if ( masked < 16 )
        rc.append( '0' );
      rc.append( str );
    }
    return rc.toString();
  }

  public static String toString( int[] data )
  {
    return toString( data, -1 );
  }

  public static String toString( int[] data, int breakAt )
  {
    if ( data == null )
      return null;

    StringBuffer rc = new StringBuffer( 4 * data.length );
    int breakCount = breakAt;
    for ( int i = 0; i < data.length; i++ )
    {
      if ( breakCount == 0 )
      {
        rc.append( '\n' );
        breakCount = breakAt;
      }
      --breakCount;

      if ( i > 0 )
        rc.append( ' ' );

      String str = Integer.toHexString( data[ i ] & 0xFF );
      if ( str.length() < 2  )
        rc.append( '0' );
      rc.append( str );
    }
    return rc.toString();
  }

  public String toString()
  {
    return toString( data );
  }

  public String toString( int breakAt )
  {
    return toString( data, breakAt );
  }

  public boolean equals( Object obj )
  {
    Hex aHex = ( Hex )obj;
    if ( this ==  aHex )
      return true;

    if ( data.length != aHex.data.length )
      return false;

    for ( int i = 0; i < data.length; i++ )
      if (( data[ i ] & 0xFF ) != ( aHex.data[ i ] & 0xFF ))
        return false;

    return true;
  }

  public int hashCode()
  {
    int rc = 0;
    if ( data.length == 0 )
      return 0;

    int multiplier = ( int )Math.pow( 31, data.length - 1);

    for ( int i = 0; i < data.length; i++ )
    {
      rc += ( data[ i ] & 0xFF ) * multiplier;
      multiplier /= 31;
    }
    return rc;
  }

  public int compareTo( Object o )
  {
    int rc;
    int compareLen;
    int[] otherData = (( Hex )o ).data;
    if ( data.length < otherData.length )
    {
      compareLen = data.length;
      rc = -1;
    }
    else if ( data.length == otherData.length )
    {
      compareLen = data.length;
      rc = 0;
    }
    else
    {
      compareLen = otherData.length;
      rc = 1;
    }

    for ( int i = 0; i < compareLen; i++ )
    {
      int v1 = data[ i ] & 0xFF;
      int v2 = otherData[ i ] & 0xFF;
      if ( v1 < v2 )
      {
        rc = -1;
        break;
      }
      else if ( v1 > v2 )
      {
        rc = 1;
        break;
      }
    }

    return rc;
  }

  public int indexOf( Hex needle )
  {
    return indexOf( needle.data );
  }

  public int indexOf( Hex needle, int start )
  {
    return indexOf( needle.data, start );
  }

  public int indexOf( int[] needle )
  {
    return indexOf( needle, 0 );
  }

  public int indexOf( int[] needle, int start )
  {
    int index = start;
    int last = data.length - needle.length;
    while ( index < last )
    {
      boolean match = true;
      for ( int i = 0; i < needle.length; i++ )
      {
        if ( needle[ i ] != data[ index + i ] )
        {
          match = false;
          break;
        }
      }
      if ( match )
        return index;
      index++;
    }
    return -1;
  }

  protected Object clone()
    throws CloneNotSupportedException
  {
    Hex rc = ( Hex )super.clone();
    rc.data = ( int[] )data.clone();
    return rc;
  }

  private int[] data = null;

  public static int NO_MATCH = 0x100;
  public static int ADD_OFFSET = 0x200;
}
