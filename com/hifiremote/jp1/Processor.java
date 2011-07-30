package com.hifiremote.jp1;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.hifiremote.jp1.AssemblerOpCode.AddressMode;
import com.hifiremote.jp1.AssemblerTableModel.DisasmState;

// TODO: Auto-generated Javadoc
/**
 * The Class Processor.
 */
public abstract class Processor
{

  /**
   * Instantiates a new processor.
   * 
   * @param name
   *          the name
   */
  public Processor( String name )
  {
    this( name, null, false );
  }

  /**
   * Instantiates a new processor.
   * 
   * @param name
   *          the name
   * @param reverse
   *          the reverse
   */
  public Processor( String name, boolean reverse )
  {
    this( name, null, reverse );
  }

  /**
   * Instantiates a new processor.
   * 
   * @param name
   *          the name
   * @param version
   *          the version
   */
  public Processor( String name, String version )
  {
    this( name, version, false );
  }

  /**
   * Instantiates a new processor.
   * 
   * @param name
   *          the name
   * @param version
   *          the version
   * @param reverse
   *          the reverse
   */
  public Processor( String name, String version, boolean reverse )
  {
    this.name = name;
    this.version = version;
  }

  /**
   * Sets the vector edit data.
   * 
   * @param opcodes
   *          the opcodes
   * @param addresses
   *          the addresses
   */
  public void setVectorEditData( int[] opcodes, int[] addresses )
  {
    this.opCodes = opcodes;
    this.addresses = addresses;
  }

  /**
   * Sets the data edit data.
   * 
   * @param min
   *          the min
   * @param max
   *          the max
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
  public String getName()
  {
    return name;
  }

  /**
   * Gets the version.
   * 
   * @return the version
   */
  public String getVersion()
  {
    return version;
  }

  /**
   * Gets the full name.
   * 
   * @return the full name
   */
  public String getFullName()
  {
    if ( version == null )
    {
      return name;
    }
    else
    {
      return name + '-' + version;
    }
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
   * @param hex
   *          the hex
   * @param remote
   *          the remote
   * @return the hex
   */
  public Hex translate( Hex hex, Remote remote )
  {
    int vectorOffset = remote.getProtocolVectorOffset();
    int dataOffset = remote.getProtocolDataOffset();
    if ( vectorOffset != 0 || dataOffset != 0 )
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
    {
      doVectorEdit( hex, vectorOffset );
    }
    if ( dataOffset != 0 )
    {
      doDataEdit( hex, dataOffset );
    }
    return hex;
  }

  /**
   * Import code.
   * 
   * @param code
   *          the code
   * @param processorName
   *          the processor name
   * @return the hex
   */
  public Hex importCode( Hex code, String processorName )
  {
    return code;
  }

  /**
   * Gets the int.
   * 
   * @param data
   *          the data
   * @param offset
   *          the offset
   * @return the int
   */
  public abstract int getInt( short[] data, int offset );

  /**
   * Put int.
   * 
   * @param val
   *          the val
   * @param data
   *          the data
   * @param offset
   *          the offset
   */
  public abstract void putInt( int val, short[] data, int offset );

  /**
   * Do vector edit.
   * 
   * @param hex
   *          the hex
   * @param vectorOffset
   *          the vector offset
   */
  private void doVectorEdit( Hex hex, int vectorOffset )
  {
    short[] data = hex.getData();
    for ( int i = 0; i < data.length; i++ )
    {
      short opCode = data[ i ];
      for ( int j = 0; j < opCodes.length; j++ )
      {
        if ( opCode == opCodes[ j ] )
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
   * @param hex
   *          the hex
   * @param dataOffset
   *          the data offset
   */
  private void doDataEdit( Hex hex, int dataOffset )
  {
    short[] data = hex.getData();

    for ( int i = 0; i < data.length - 1; i++ )
    {
      if ( ( data[ i ] & Hex.ADD_OFFSET ) != 0 && ( data[ i + 1 ] & Hex.ADD_OFFSET ) != 0 )
      {
        int temp = getInt( data, i );
        if ( temp < minDataAddress || temp > maxDataAddress )
        {
          continue;
        }
        temp += dataOffset;
        putInt( temp, data, i );
        i++ ;
      }
    }

    for ( int i = 0; i < data.length; i++ )
    {
      int temp = data[ i ];
      if ( ( temp & Hex.ADD_OFFSET ) != 0 )
      {
        temp &= 0xFF;
        temp += dataOffset;
        data[ i ] = ( short )( temp & 0xFF );
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    return getFullName();
  }

  public int getRAMAddress()
  {
    return RAMAddress;
  }

  public void setRAMAddress( int address )
  {
    RAMAddress = address;
  }

  public List< AssemblerOpCode[] > getInstructions()
  {
    return instructions;
  }

  public LinkedHashMap< String, AddressMode > getAddressModes()
  {
    return addressModes;
  }
  
  public void setAddressModes( String[][] modeArray )
  {
    for ( int i = 0; i < modeArray.length; i++ )
    {
      addressModes.put( modeArray[i][0], new AddressMode( modeArray[i] ) );
    }
  }
  
  public void setInstructions( String[][][] instArray )
  {
    for ( int i = 0; i < instArray.length; i++ )
    {
      AssemblerOpCode[] assCodes = new AssemblerOpCode[ instArray[ i ].length ];
      for ( int j = 0; j < instArray[ i ].length; j++ )
      {
        assCodes[ j ] = new AssemblerOpCode( this, instArray[ i ][ j ] );
      }
      instructions.add( assCodes );
    }
  }
  
  public AssemblerOpCode getOpCode( short[] data, DisasmState state )
  {
    // This code handles 6805, 740 and HCS08 processors.  S3C80Processor class has an override.
    AssemblerOpCode opCode = instructions.get( 0 )[ data[ state.index++ ] ].clone();
    state.shiftFlag = false;
    state.shiftPos = 0;
    state.nMask = 0;
    state.bMask = 0;
    if ( opCode.getIndex() > 0 )
    {
      if ( opCode.getIndex() < instructions.size() )
      {
        opCode = instructions.get( opCode.getIndex() )[ data[ state.index++ ] ];
      }
      else
      {
        state.shiftFlag = true;
        opCode.setIndex( 0 );
      }
    }
    if ( opCode.getName() == "*" )
    {
      opCode.setIndex( -1 );  // Invalid op code
    }
    return opCode;
  }
  
  
  public String getConditionCode( int n )
  {
    return null;
  }

  public int getStartOffset()
  {
    return startOffset;
  }

  public void setStartOffset( int startOffset )
  {
    this.startOffset = startOffset;
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
  
  /** The default RAM address. */
  private int RAMAddress = 0x0100;
  
  /** Offset of first op code in protocol */
  private int startOffset = 3;
  
  private List< AssemblerOpCode[] > instructions = new ArrayList< AssemblerOpCode[] >();
  
  private LinkedHashMap< String, AddressMode > addressModes = new LinkedHashMap< String, AddressMode >();
}
