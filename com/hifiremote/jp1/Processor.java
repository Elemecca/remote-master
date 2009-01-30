package com.hifiremote.jp1;

// TODO: Auto-generated Javadoc
/**
 * The Class Processor.
 */
public abstract class Processor
{
  
  /**
   * Instantiates a new processor.
   * 
   * @param name the name
   */
  public Processor( String name )
  {
    this( name, null, false );
  }
  
  /**
   * Instantiates a new processor.
   * 
   * @param name the name
   * @param reverse the reverse
   */
  public Processor( String name, boolean reverse )
  {
	  this( name, null, reverse );
  }

  /**
   * Instantiates a new processor.
   * 
   * @param name the name
   * @param version the version
   */
  public Processor( String name, String version )
  {
	this( name, version, false );
  }
  
  /**
   * Instantiates a new processor.
   * 
   * @param name the name
   * @param version the version
   * @param reverse the reverse
   */
  public Processor( String name, String version, boolean reverse )
  {
    this.name = name;
    this.version = version;
  }

  /**
   * Sets the vector edit data.
   * 
   * @param opcodes the opcodes
   * @param addresses the addresses
   */
  public void setVectorEditData( int[] opcodes, int[] addresses )
  {
    this.opCodes = opcodes;
    this.addresses = addresses;
  }


  /**
   * Sets the data edit data.
   * 
   * @param min the min
   * @param max the max
   */
  public void setDataEditData( int min, int max )
  {
    minDataAddress = min;
    maxDataAddress = max;
  }

  /**
   * Gets the name.
   * 
   * @return the name
   */
  public String getName(){ return name; }
  
  /**
   * Gets the version.
   * 
   * @return the version
   */
  public String getVersion(){ return version; }
  
  /**
   * Gets the full name.
   * 
   * @return the full name
   */
  public String getFullName()
  {
    if ( version == null )
      return name;
    else
      return name + '-' + version;
  }

  /**
   * Gets the equivalent name.
   * 
   * @return the equivalent name
   */
  public String getEquivalentName()
  {
    return getFullName();
  }

  /**
   * Translate.
   * 
   * @param hex the hex
   * @param remote the remote
   * 
   * @return the hex
   */
  public Hex translate( Hex hex, Remote remote )
  {
    int vectorOffset = remote.getProtocolVectorOffset();
    int dataOffset = remote.getProtocolDataOffset();
    if (( vectorOffset != 0 ) || ( dataOffset != 0 ))
    {
      try
      {
        hex = ( Hex )hex.clone();
      }
      catch ( CloneNotSupportedException ex )
      {
        ex.printStackTrace( System.err );
      }
    }
    if ( vectorOffset != 0 )
      doVectorEdit( hex, vectorOffset );
    if ( dataOffset != 0 )
      doDataEdit( hex, dataOffset );
    return hex;
  }

  /**
   * Import code.
   * 
   * @param code the code
   * @param processorName the processor name
   * 
   * @return the hex
   */
  public Hex importCode( Hex code, String processorName )
  {
    return code;
  }

  /**
   * Gets the int.
   * 
   * @param data the data
   * @param offset the offset
   * 
   * @return the int
   */
  public abstract int getInt( short[] data, int offset );
  
  /**
   * Put int.
   * 
   * @param val the val
   * @param data the data
   * @param offset the offset
   */
  public abstract void putInt( int val, short[] data, int offset );
  
  /**
   * Do vector edit.
   * 
   * @param hex the hex
   * @param vectorOffset the vector offset
   */
  private void doVectorEdit( Hex hex, int vectorOffset )
  {
    short[] data = hex.getData();
    for ( int i = 0; i < data.length; i++ )
    {
      short opCode = data[ i ];
      for ( int j = 0; j < opCodes.length; j++ )
      {
        if ( opCode == opCodes [ j ])
        {
          int address = getInt( data, i + 1 );
          for ( int k = 0; k < addresses.length; k++ )
          {
            if ( addresses[ k ] == address )
            {
              address += vectorOffset;
              putInt( address, data, i + 1 );
              break;
            }
          }
          i += 2;
          break;
        }
      }
    }
  }

  /**
   * Do data edit.
   * 
   * @param hex the hex
   * @param dataOffset the data offset
   */
  private void doDataEdit( Hex hex, int dataOffset )
  {
    short[] data = hex.getData();

    for ( int i = 0; i < data.length - 1; i++ )
    {
      if ((( data[ i ] & Hex.ADD_OFFSET ) != 0 ) &&
          (( data[ i + 1 ] & Hex.ADD_OFFSET ) != 0 ))
      {
        int temp = getInt( data, i );
        if (( temp < minDataAddress ) || ( temp > maxDataAddress ))
          continue;
        temp += dataOffset;
        putInt( temp, data, i);
        i++;
      }
    }

    for ( int i = 0; i < data.length; i++ )
    {
      int temp = data[ i ];
      if (( temp & Hex.ADD_OFFSET ) != 0 )
      {
        temp &= 0xFF;
        temp += dataOffset;
        data[ i ] = ( short )( temp & 0xFF );
      }
    }
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return getFullName();
  }

  /** The name. */
  private String name = null;
  
  /** The version. */
  private String version = null;
  
  /** The op codes. */
  private int[] opCodes = new int[ 0 ];
  
  /** The addresses. */
  private int[] addresses = new int[ 0 ];
  
  /** The min data address. */
  private int minDataAddress = 0x64;
  
  /** The max data address. */
  private int maxDataAddress = 0x80;
}
