package com.hifiremote.jp1;

public class StatusBit
{
  public StatusBit( int addr, int bit, int onValue )
  {
    this.address = addr;
    this.bit = bit;
    this.onValue = onValue;
  }

  public int getAddress(){ return address; }
  public int getBit(){ return bit; }
  public int getOnValue(){ return onValue; }
  public String toString()
  {
    StringBuffer temp = new StringBuffer( 20 );
    temp.append( '$' ).append( Integer.toHexString( address ))
        .append( '.' ).append( bit );
    if ( onValue == 0 )
      temp.append( ".0" );
    return temp.toString();
  }

  private int address;
  private int bit;
  private int onValue;
}
