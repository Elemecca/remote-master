package com.hifiremote.jp1;

// TODO: Auto-generated Javadoc
/**
 * The Class AddressRange.
 */
public class AddressRange
{
  
  /**
   * Instantiates a new address range.
   * 
   * @param start the start
   * @param end the end
   */
  public AddressRange( int start, int end )
  {
    this.start = start;
    this.end = end;
  }
  
  /**
   * Gets the start.
   * 
   * @return the start
   */
  public int getStart(){ return start; }
  
  /**
   * Gets the end.
   * 
   * @return the end
   */
  public int getEnd(){ return end; }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return "$" + Integer.toHexString( start ) +
           "..$" + Integer.toHexString( end );
  }

  /** The start. */
  private int start;
  
  /** The end. */
  private int end;
}
