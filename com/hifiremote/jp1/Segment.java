package com.hifiremote.jp1;

import java.util.List;

public class Segment extends Highlight
{
  private int type = 0;
  private int flags = 0xFF;
  private Hex hex = null;
  private int address = 0;

  public static int writeData( List< Segment > list, short[] data, int pos )
  {
    for ( Segment seg : list )
    {
      Hex hex = seg.getHex();
      Hex.put( hex.length() + 4, data, pos );
      data[ pos + 2 ] = ( short )seg.get_Type();
      data[ pos + 3 ] = ( short )seg.getFlags();
      Hex.put( hex, data, pos + 4 );
      seg.setAddress( pos );
      pos += hex.length() + 4;
    }
    return pos;
  }
  
  
  public Segment( int type, int flags, Hex hex )
  {
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

  public int get_Type()
  {
    return type;
  }

  public void set_Type( int type )
  {
    this.type = type;
  }

  public int getFlags()
  {
    return flags;
  }

  public void setFlags( int flags )
  {
    this.flags = flags;
  }

  public Hex getHex()
  {
    return hex;
  }

  public void setObject( Highlight object )
  {
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

  public void setHex( Hex hex )
  {
    this.hex = hex;
  }
  
}
