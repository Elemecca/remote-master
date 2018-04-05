package com.hifiremote.jp1;

public class Xor16CheckSum extends CheckSum
{

  public Xor16CheckSum( int addr, AddressRange range, boolean comp )
  {
    super( addr, range, comp );
  }

  public String toString()
  {
    return "*" + super.toString();
  }

  @Override
  public short calculateCheckSum( short[] data, int start, int end )
  {
    // For remotes with segments, UpdateImage() updates the checksum range end to
    // point to the last byte written.  The number of bytes covered by the checksum
    // needs to be rounded up to a multiple of 4.
    //
    // TESTED:  Round to multiple of 2 fails for remote with signature 257302
//    end = end + 1 - ( end & 1 );
    end = end + 3 - ( end & 3 );
    short sum = 0;
    for ( int i = start; i <= end - 1; i += 2 )
    {
      sum ^= ( ( data[ i ] << 8 ) |  data[ i + 1 ] ) & 0xFFFF;
    }
    return sum;
  }
  
  @Override
  public void setCheckSum( short[] data )
  {
    short sum = calculateCheckSum( data, addressRange.getStart(), addressRange.getEnd() );
    if ( complement )
    {
      sum = ( short )( ~sum & 0xFFFF );
    }
    data[ checkSumAddress ] = ( short )( ( sum >> 8 ) & 0xFF );
    data[ checkSumAddress + 1 ] = ( short )( ~sum & 0xFF );
  }
}

