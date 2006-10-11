package com.hifiremote.jp1;

import java.util.StringTokenizer;

public class Hex
  implements Cloneable, Comparable
{
  public Hex()
  {
    data = new short[ 0 ];
  }

  public Hex( int length )
  {
    data = new short[ length ];
  }

  public Hex( String text )
  {
    data = parseHex( text );
  }

  public Hex( short[] data )
  {
    this.data = data;
  }

  public Hex( short[] newData, int offset, int length )
  {
    data = new short[ length ];
    System.arraycopy( newData, offset, data, 0, length );
  }

  public Hex( Hex h )
  {
    data = new short[ h.data.length ];
    System.arraycopy( h.data, 0, data, 0, data.length );
  }

  public int length()
  {
    return data.length;
  }

  public short[] getData()
  {
    return data;
  }

  public void set( short[] data )
  {
    this.data = data;
  }

  public void set( String text )
  {
    data = parseHex( text );
  }

  public int get( int offset )
  {
    return get( data, offset );
  }

  public static int get( short[] data, int offset )
  {
    return ( data[ offset ] << 8 ) | data[ offset  + 1  ];
  }

  public void put( int value, int offset )
  {
    put( value, data, offset );
  }

  public static void put( int value, short[] data, int offset )
  {
    data[ offset ] = ( short )(( value >> 8 ) & 0xFF );
    data[ offset + 1 ] = ( short )( value & 0xFF );
  }

  public void put( Hex src )
  {
    put( src, 0 );
  }

  public void put( Hex src, int index )
  {
    put( src.data, index );
  }

  public void put( short[] src, int index )
  {
    put( src, data, index );
  }

  public static void put( Hex src, short[] dest, int index )
  {
    put( src.data, dest, index );
  }

  public static void put( short[] src, short[] dest, int index )
  {
    int length = src.length;
    if (( index + length ) > dest.length )
      length = dest.length - index;
    System.arraycopy( src, 0, dest, index, length );
  }

  public static short[] parseHex( String text )
  {
    short[] rc = null;
    int length = 0;
    int space = text.indexOf( ' ' );
    if (( space == -1 ) && ( text.length() > 3 ))
    {
      length = text.length() / 2;
      rc = new short[ length ];
      for ( int i = 0; i < length; i++ )
      {
        int offset = i * 2;
        String temp = text.substring( offset, offset + 2 );
        rc[ i ] = Short.parseShort( temp, 16 );
      }
    }
    else
    {
      StringTokenizer st = new StringTokenizer( text, " _.$h\n\r" );
      length = st.countTokens();
      rc = new short[ length ];
      parseHex( text, rc, 0 );
    }
    return rc;
  }

  public static void parseHex( String text, short[] data,  int offset )
  {
    StringTokenizer st = new StringTokenizer( text, " _.$h\n\r", true );
    short value = 0;
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
        data[ offset++ ] = ( short )( value | Short.parseShort( token, 16 ));
    }
  }

  public static String asString( int value )
  {
    StringBuilder buff = new StringBuilder( 2 );
    String str = Integer.toHexString( value & 0xFF );
    if ( str.length() < 2 )
      buff.append( '0' );
    buff.append( str );
    return buff.toString().toUpperCase();
  }

  public String toRawString()
  {
    if ( data == null )
      return null;

    StringBuilder rc = new StringBuilder( 3 * data.length );
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
    return rc.toString().toUpperCase();
  }

  public static String toString( short[] data )
  {
    return toString( data, -1 );
  }

  public static String toString( short[] data, int breakAt )
  {
    return toString( data, breakAt, 0, data.length );
  }

  public static String toString( short[] data, int breakAt, int offset, int length )
  {
    if ( data == null )
      return null;

    StringBuilder rc = new StringBuilder( 4 * data.length );
    int breakCount = breakAt;
    int last = offset + length;
    for ( int i = offset; i < last; ++i )
    {
      if ( breakCount == 0 )
      {
        rc.append( '\n' );
        breakCount = breakAt;
      }
      --breakCount;

      if ( i > offset )
        rc.append( ' ' );

      String str = Integer.toHexString( data[ i ] & 0xFF );
      if ( str.length() < 2  )
        rc.append( '0' );
      rc.append( str );
    }
    return rc.toString().toUpperCase();
  }

  public static String toString( int[] data )
  {
    if ( data == null )
      return null;

    StringBuilder rc = new StringBuilder( 4 * data.length );
    for ( int i = 0; i < data.length; ++i )
    {
      if ( i > 0 )
        rc.append( ' ' );

      String str = Integer.toHexString( data[ i ]);
      if ( str.length() < 2  )
        rc.append( '0' );
      rc.append( str );
    }
    return rc.toString().toUpperCase();
  }

  public String toString()
  {
    return toString( data );
  }

  public String toString( int breakAt )
  {
    return toString( data, breakAt );
  }

  public String toString( int offset, int length )
  {
    return toString( data, -1, offset, length );
  }

  public static String toString( short[] data, int offset, int length )
  {
    return toString( data, -1, offset, length );
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
    short[] otherData = (( Hex )o ).data;
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

  public int indexOf( short[] needle )
  {
    return indexOf( needle, 0 );
  }

  public int indexOf( short[] needle, int start )
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

  public Hex subHex( int index )
  {
    return subHex( index, data.length - index );
  }

  public Hex subHex( int index, int len )
  {
    return subHex( data, index, len );
  }

  public static Hex subHex( short[] src, int index, int len )
  {
    short[] dest = new short[ len ];
    System.arraycopy( src, index, dest, 0, len );
    return new Hex( dest );
  }

  protected Object clone()
    throws CloneNotSupportedException
  {
    Hex rc = ( Hex )super.clone();
    rc.data = ( short[] )data.clone();
    return rc;
  }

  private short[] data = null;

  public static short NO_MATCH = 0x100;
  public static short ADD_OFFSET = 0x200;
}
