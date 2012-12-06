package com.hifiremote.jp1;

import java.util.StringTokenizer;

// TODO: Auto-generated Javadoc
/**
 * The Class AddressRange.
 */
public class AddressRange extends RDFParameter
{
  public AddressRange()
  {
    super();
  }
  
  public AddressRange( String text, Remote remote ) throws Exception
  {
    super();
    parse( text, remote );
  }
  
  public AddressRange( int start, int end )
  {
    this.start = start;
    this.end = end;
    freeStart = start;
    freeEnd = end;
  }
  
  public void parse( String text, Remote remote ) throws Exception
  {
    StringTokenizer st = new StringTokenizer( text, ".=" );
    start = RDFReader.parseNumber( st.nextToken() );
    end = RDFReader.parseNumber( st.nextToken() );
    freeStart = start;
    freeEnd = end;
  }
  
  /**
   * Gets the start.
   * 
   * @return the start
   */
  public int getStart()
  {
    return start;
  }

  /**
   * Gets the end.
   * 
   * @return the end
   */
  public int getEnd()
  {
    return end;
  }

  public void setStart( int start )
  {
    this.start = start;
  }

  public void setEnd( int end )
  {
    this.end = end;
  }

  public int getFreeStart()
  {
    return freeStart;
  }

  public void setFreeStart( int freeStart )
  {
    this.freeStart = freeStart;
  }

  public int getFreeEnd()
  {
    return freeEnd;
  }

  public void setFreeEnd( int freeEnd )
  {
    this.freeEnd = freeEnd;
  }

  public int getSize()
  {
    return end + 1 - start;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return "$" + Integer.toHexString( start ) + "..$" + Integer.toHexString( end );
  }

  /** The start. */
  private int start;

  /** 
   * The upper part of an address range may be borrowed for upgrade overflow.  The
   * end value is the address of the last byte of the range, but the address of the
   * last available, ie non-borrowed, byte is given by the freeEnd property.
   */
  private int end;
  
  /**
   * The upper part of an address range may be borrowed for upgrade overflow.  The
   * freeEnd value is the address of the last byte that is not so borrowed.
   */
  private int freeEnd;
  
  /**
   * The upper part of an address range may be borrowed for upgrade overflow.  The
   * freeStart value is the address of the first byte that can be used for borrowing.
   */
  private int freeStart;
}
