package com.hifiremote.jp1;

public class EFC
  implements Comparable
{
  public EFC( String text )
  {
    value = Integer.parseInt( text ) & 0xFF;
  }

  public EFC( int value )
  {
    this.value = value & 0xFF;
  }

  public int getValue(){ return value; }

  public String toString()
  {
    StringBuffer buff = new StringBuffer( 3 );
    String temp = Integer.toString( value );
    if ( value < 100 )
      buff.append( '0' );
    if ( value < 10 )
      buff.append( '0' );
    buff.append( Integer.toString( value ));
    return buff.toString();
  }

  public int compareTo( Object o )
  {
    int other = (( EFC )o ).value;
    if ( value < other )
      return -1;
    else if ( value == other )
      return 0;
    else
      return 1;
  }

  private int value = 0;
}
