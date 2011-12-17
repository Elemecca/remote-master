package com.hifiremote.jp1;

import java.util.List;

public class Segment extends Highlight
{
  private int type = 0;
  private int flags = 0xFF;
  private int activity = 0;
  private Hex hex = null;
  private int address = 0;
//  private Highlight object = null;
  
//  public Segment( int type, int size )
//  {
//    setMemoryUsage( size );
//    hex = new Hex( size - 3 );
//    this.type = type;
//  }
  
  public Segment( int type, int flags, Hex hex )
  {
    setMemoryUsage( hex.length() + 3 );
    this.hex = hex;
    this.type = type;
    this.flags = flags;
  }
  
  public Segment( int type, int flags, Hex hex, Highlight object )
  {
    this( type, flags, hex );
    object.setSegment( this );
  }
  
  public Segment( int type, int flags, Hex hex, List< ? extends Highlight > list )
  {
    this( type, flags, hex );
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

  public int getFlags()
  {
    return flags;
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
