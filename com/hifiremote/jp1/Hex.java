package com.hifiremote.jp1;

import java.util.StringTokenizer;

public class Hex
  implements Cloneable, Comparable
{
  public Hex()
  {
    data = new byte[ 1 ];
  }

  public Hex( String text )
  {
    data = parseHex( text );
  }

  public Hex( byte[] data )
  {
    this.data = data;
  }

  public int length()
  {
    return data.length;
  }

  public byte[] getData()
  {
    return data;
  }

  public void set( byte[] data )
  {
    this.data = data;
  }
  
  public void set( String text )
  {
    data = parseHex( text );
  }

  public static byte[] parseHex( String text )
  {
    StringTokenizer st = new StringTokenizer( text, " " );
    int length = st.countTokens();
    byte[] rc = new byte[ length ];
    for ( int i = 0; i < length; i++ )
      rc[ i ] = ( byte )Integer.parseInt( st.nextToken(), 16 );

    return rc;
  }

  public static String toString( byte value )
  {
    StringBuffer buff = new StringBuffer( 2 );
    String str = Integer.toHexString( value & 0xFF );
    if ( str.length() < 2 )
      buff.append( '0' );
    buff.append( str );
    return buff.toString();
  }

  public static String toString( byte[] data )
  {
    return toString( data, -1 );
  }

  public static String toString( byte[] data, int breakAt )
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

  public boolean equals( Hex aHex )
  {
    boolean rc = true;
    if ( data.length != aHex.data.length )
      rc = false;
    else
      for ( int i = 0; i < data.length; i++ )
        if ( data[ i ] != aHex.data[ i ])
        {
          rc = false;
          break;
        }
    return rc;
  }

  public int compareTo( Object o )
  {
    int rc;
    int compareLen;
    byte[] otherData = (( Hex )o ).data;
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
      int v1 = Translate.byte2int( data[ i ]);
      int v2 = Translate.byte2int( otherData[ i ]);
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

  protected Object clone()
    throws CloneNotSupportedException
  {
    Hex rc = ( Hex )super.clone();
    rc.data = ( byte[] )data.clone();
    return rc;
  }

  private byte[] data = null;
}
