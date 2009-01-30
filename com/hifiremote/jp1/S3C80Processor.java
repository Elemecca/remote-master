package com.hifiremote.jp1;

// TODO: Auto-generated Javadoc
/**
 * The Class S3C80Processor.
 */
public class S3C80Processor
  extends BigEndianProcessor
{
  
  /**
   * Instantiates a new s3 c80 processor.
   */
  public S3C80Processor()
  {
    this( "S3C80" );
  }

  /**
   * Instantiates a new s3 c80 processor.
   * 
   * @param name the name
   */
  protected S3C80Processor( String name )
  {
    super( name, null );
  }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.Processor#translate(com.hifiremote.jp1.Hex, com.hifiremote.jp1.Remote)
   */
  public Hex translate( Hex hex, Remote remote )
  {
    if ( remote.getRAMAddress() == 0x8000 )
    {
      try
      {
        hex = ( Hex )hex.clone();
      }
      catch ( CloneNotSupportedException ex )
      {
        ex.printStackTrace( System.err );
      }
      short[] data = hex.getData();
      int offset = 3;
      if (( data[ 3 ] & 0xFF ) == 0x8B )
      {
        offset = ( data[ 4 ] & 0xFF ) + 5;
      }
      for ( int i = offset; i < data.length; i++ )
      {
        int first = data[ i ] & 0xFF;
        if (( first == 0xF6 ) || ( first == 0x8D ))
        {
          int second = data[ ++i ] & 0xFF;
          if ( second == 0xFF )
          {
            data[ i ] = ( byte )0x80;
          }
          else if ( second == 0x01 )
          {
            int third = data[ ++i ] & 0xFF;
            data[ i ] = ( byte )adjust( third );
          }
        }
      }
    }
    return hex;
  }

  /**
   * Adjust.
   * 
   * @param val the val
   * 
   * @return the int
   */
  private int adjust( int val )
  {
    int temp1 = val - 0x2C;
    int temp2 = val - 0x46;
    if ((( 0 <= temp1 ) && ( temp1 <= 0x0E ) && ( temp1 % 7 == 0 )) ||
        (( 0 <= temp2 ) && ( temp2 <= 0x2D ) && ( temp2 % 3 == 0 )))
    {
      val -= 0x13;
    }
    return val;
  }
}
