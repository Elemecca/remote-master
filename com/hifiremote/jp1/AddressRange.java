package com.hifiremote.jp1;

public class AddressRange
{
  public AddressRange( int start, int end )
  {
    this.start = start;
    this.end = end;
  }
  public int getStart(){ return start; }
  public int getEnd(){ return end; }
  public String toString()
  {
    return "$" + Integer.toHexString( start ) +
           "..$" + Integer.toHexString( end );
  }

  private int start;
  private int end;
}
