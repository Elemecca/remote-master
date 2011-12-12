package com.hifiremote.jp1;

import java.util.List;

public class Segment extends Highlight
{
  private int type = 0;
  private Hex hex = null;
  private int address = 0;
//  private Highlight object = null;
  
//  public Segment( int type, int size )
//  {
//    setMemoryUsage( size );
//    hex = new Hex( size - 3 );
//    this.type = type;
//  }
  
  public Segment( int type, Hex hex )
  {
    setMemoryUsage( hex.length() + 3 );
    this.hex = hex;
    this.type = type;
  }
  
  public Segment( int type, Hex hex, Highlight object )
  {
    this( type, hex );
    object.setSegment( this );
  }
  
  public Segment( int type, Hex hex, List< ? extends Highlight > list )
  {
    this( type, hex );
    int index = 0;
    for ( Highlight object : list )
    {
      object.setSegment( this, index++ ); 
    }
  }

  public int getType()
  {
    return type;
  }

  public Hex getHex()
  {
    return hex;
  }

//  public Highlight getObject()
//  {
//    return object;
//  }

  public void setObject( Highlight object )
  {
//    this.object = object;
    object.setSegment( this );
  }

  public int getAddress()
  {
    return address;
  }

  public void setAddress( int address )
  {
    this.address = address;
  }
  
}
