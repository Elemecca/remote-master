package com.hifiremote.jp1;

public class Processor
{
  public Processor( String name )
  {
    this( name, null );
  }

  public Processor( String name, String version )
  {
    this.name = name;
    this.version = version;
  }

  public void setVectorEditData( int[] opcodes, int[] addresses )
  {
    this.opCodes = opcodes;
    this.addresses = addresses;
  }
  

  public void setDataEditData( int min, int max )
  {
    minDataAddress = min;
    maxDataAddress = max;
  }

  public String getName(){ return name; }
  public String getVersion(){ return version; }
  public String getFullName()
  {
    if ( version == null )
      return name;
    else
      return name + '-' + version;
  }

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

  private int getInt( int[] data, int offset )
  {
    return (( data[ offset ] & 0xFF ) << 8 ) + ( data[ offset + 1 ] & 0xFF );
  }

  private void putInt( int[] data, int offset, int val )
  {
    data[ offset ] = val >> 8;
    data[ offset + 1 ] = ( val & 0xFF );
  }

  private void doVectorEdit( Hex hex, int vectorOffset )
  {
    int[] data = hex.getData();
    for ( int i = 0; i < data.length; i++ )
    {
      int opCode = data[ i ];
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
              putInt( data, i + 1, address );
              break;
            }
          }
          i += 2;
          break;
        }
      }
    }
  }

  private void doDataEdit( Hex hex, int dataOffset )
  {
    int[] data = hex.getData();

    for ( int i = 0; i < data.length - 1; i++ )
    {
      if ((( data[ i ] & hex.ADD_OFFSET ) != 0 ) && 
          (( data[ i + 1 ] & hex.ADD_OFFSET ) != 0 ))
      {
        int temp = getInt( data, i );
        if (( temp < minDataAddress ) || ( temp > maxDataAddress ))
          continue;
        temp += dataOffset;
        putInt( data, i, temp );
        i++;
      }
    }

    for ( int i = 0; i < data.length; i++ )
    {
      int temp = data[ i ];
      if (( temp & hex.ADD_OFFSET ) != 0 )
      {
        temp &= 0xFF;
        temp += dataOffset;
        data[ i ] = ( temp & 0xFF );
      }
    }
  }

  private String name = null;
  private String version = null;
  private int[] opCodes = new int[ 0 ];
  private int[] addresses = new int[ 0 ];
  private int minDataAddress = 0x64;
  private int maxDataAddress = 0x80;
}
