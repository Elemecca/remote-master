package com.hifiremote.jp1;

import com.hifiremote.jp1.AssemblerTableModel.DisasmState;

// TODO: Auto-generated Javadoc
/**
 * The Class S3C80Processor.
 */
public class S3C80Processor
  extends BigEndianProcessor
{
  public enum CodeType
  {
    OLD, NEW, UNKNOWN
  }
  
  public static final int newRAMAddress = 0xFF00; // value for S3C8+ processor
   
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
    setRAMAddress( 0x8000 );
  }
  
  @Override
  public String getConditionCode( int n )
  {
    String ccs[] = { "F", "LT", "LE", "ULE", "OV", "MI",
    "EQ", "C", "" /*"T"*/, "GE", "GT", "UGT", "NOV", "PL", "NE", "NC" };
    return ( ccs[ n ] == "" ) ? "" : ccs[ n ] + ", ";
  }

  @Override
  public AssemblerOpCode getOpCode( short[] data, DisasmState state )
  {
    AssemblerOpCode opCode = getInstructions().get( 0 )[ data[ state.index++ ] ].clone();
    state.shiftFlag = false;
    state.shiftPos = 3;
    state.nMask = 0;
    state.bMask = 0;
    switch ( opCode.getIndex() )
    {
      case 1:
        state.shiftFlag = true;
        if ( ( data[ state.index ] & 1 ) == 1 )
        {
          opCode.setMode( getAddressModes().get( opCode.getMode().name + "Z" ) );
        }
        if ( opCode.getMode() == null )
        {
          opCode.setIndex( -1 );  // Error
        }
        break;
      case 2:
        state.shiftFlag = true;
        opCode.setName( opCode.getName() + ( ( ( data [ state.index ] & 1 ) == 0 ) ? "F" : "T" ) );
        break;
      case 3:
        state.shiftFlag = true;
        opCode.setName( opCode.getName() + ( ( ( data [ state.index ] & 1 ) == 0 ) ? "R" : "S" ) );
        break;
      case 4:
        if ( ( data[ state.index ] & 0x0E ) == 0 )
        {
          opCode.setMode( getAddressModes().get( opCode.getMode().name + "Z" ) );
        }
        // run through
      case 5:
        state.nMask = 0x0E;
        if ( ( data[ state.index ] & 1 ) == 1 )
        {
          opCode.setName( opCode.getName().replaceFirst( "C", "E" ) );
        }
        break;
      case 6:
        state.bMask = 0xFC;
        opCode.setName( opCode.getName() + ( ( ( data [ state.index ] & 1 ) == 1 ) ? "1" :
          ( ( data [ state.index ] & 2 ) == 2 ) ? "0" : "" ) );
        if ( ( data [ state.index ] & 3 ) == 3 )
        {
          opCode.setIndex( -1 );  // Error
        }
        break;
      default:
        break;
    }
    if ( opCode.getIndex() > 0 )
    {
      opCode.setIndex( 0 );
    }
    if ( opCode.getName() == "*" )
    {
      opCode.setIndex( -1 );  // Invalid op code
    }
    return opCode;
  }
  
  public CodeType testCode( Hex hex )
  {
    int oldCount = 0;
    int newCount = 0;
    int bothCount = 0;
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
          newCount++;
        }
        else if ( second == 0x80 )
        {
          oldCount++;
        }
        else if ( second == 0x01 )
        {
          int third = data[ ++i ] & 0xFF;
          if ( third == 0x33 )
          {
            bothCount++;
          }
          else
          {
            int temp1 = third - 0x2C;
            int temp2 = third - 0x46;
            for (int j = 0 ; j < 2; j++ )
            {
              if ((( 0 <= temp1 ) && ( temp1 <= 0x0E ) && ( temp1 % 7 == 0 )) ||
                  (( 0 <= temp2 ) && ( temp2 <= 0x2D ) && ( temp2 % 3 == 0 )))
              {
                oldCount += j;
                newCount += 1 - j;
                break;
              }
              temp1 += 0x13;
              temp2 += 0x13;
            }
          }
        }
      }
    }
    // If 0x0133 is the only address used, newCount and oldCount will both be 0,
    // but as this is the address of the IR engine in an old processor, take it as old.
    // Types returned as UNKNOWN will not be translated.
    if ( newCount > oldCount ) return CodeType.NEW;
    else if ( oldCount > newCount ) return CodeType.OLD;
    else if ( oldCount == 0 && newCount == 0 && bothCount > 0 ) return CodeType.OLD;
    else return CodeType.UNKNOWN;   
  }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.Processor#translate(com.hifiremote.jp1.Hex, com.hifiremote.jp1.Remote)
   */
  public Hex translate( Hex hex, Remote remote )
  {
    if ( hex.length() < 4 )
    {
      // Too short to test
      return hex;
    }
    CodeType codeType = testCode( hex );
    if ( ! ( ( remote.getRAMAddress() == 0x8000 && codeType == CodeType.NEW )
        || ( remote.getRAMAddress() == 0xFF00 && codeType == CodeType.OLD ) ) )
    {
      // Doesn't need translation
      return hex;
    }
    
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
        if ( codeType == CodeType.NEW && second == 0xFF )
        {
          data[ i ] = ( short )0x80;
        }
        else if ( codeType == CodeType.OLD && second == 0x80 )
        {
          data[ i ] = ( short )0xFF;
        }
        else if ( second == 0x01 )
        {
          int third = data[ ++i ] & 0xFF;
          data[ i ] = ( short )adjust( third, codeType );
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
  private int adjust( int val, CodeType codeType )
  {
    int type = ( codeType == CodeType.NEW ) ? 0 : 1;
    int temp1 = val - 0x2C + 0x13 * type;
    int temp2 = val - 0x46 + 0x13 * type;
    if ((( 0 <= temp1 ) && ( temp1 <= 0x0E ) && ( temp1 % 7 == 0 )) ||
        (( 0 <= temp2 ) && ( temp2 <= 0x2D ) && ( temp2 % 3 == 0 )))
    {
      val += 0x13 * ( 2 * type - 1 );
    }
    return val;
  }

}
