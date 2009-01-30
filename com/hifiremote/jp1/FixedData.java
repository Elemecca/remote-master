package com.hifiremote.jp1;

// TODO: Auto-generated Javadoc
/**
 * The Class FixedData.
 */
public class FixedData
{
  
  /**
   * Instantiates a new fixed data.
   * 
   * @param addr the addr
   * @param bytes the bytes
   */
  public FixedData( int addr, byte[] bytes )
  {
    address = addr;
    data = bytes;
  }

  /**
   * Gets the address.
   * 
   * @return the address
   */
  public int getAddress(){ return address; }
  
  /**
   * Gets the data.
   * 
   * @return the data
   */
  public byte[] getData() { return data; }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    StringBuilder temp = new StringBuilder( 200 );
    temp.append( '$' ).append( Integer.toHexString( address ))
        .append( " =" );
    for ( int i = 0; i < data.length ; i++ )
    {
      temp.append( " $" );
      String str = Integer.toHexString( data[ i ]);
      int len = str.length();
      if ( len > 2 )
        str = str.substring( len - 2 );
      if ( len < 2  )
        temp.append( '0' );
      temp.append( str );
    }
    return temp.toString();
  }

  /** The address. */
  private int address;
  
  /** The data. */
  private byte[] data;
}
