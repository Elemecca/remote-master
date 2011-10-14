package com.hifiremote.jp1;

import java.util.Arrays;
import java.util.List;

import com.hifiremote.jp1.AssemblerOpCode.AddressMode;
import com.hifiremote.jp1.AssemblerOpCode.OpArg;
import com.hifiremote.jp1.AssemblerOpCode.Token;
import com.hifiremote.jp1.AssemblerOpCode.TokenType;

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
  
  private static String conditionCodes[] = { "F", "LT", "LE", "ULE", "OV", "MI",
      "EQ", "C", "" /*"T"*/, "GE", "GT", "UGT", "NOV", "PL", "NE", "NC" };
  
  @Override
  public String getConditionCode( int n )
  {
    return ( conditionCodes[ n ] == "" ) ? "" : conditionCodes[ n ] + ", ";
  }
  
  @Override
  public int getConditionIndex( String cc )
  {
    if ( cc.equals( "T" ) ) cc = "";
    return Arrays.asList( conditionCodes ).indexOf( cc );
  }
  
  @Override
  public void disasmModify( AddressMode mode, Object[] obj )
  {
    int modifier = mode.modifier;
    switch ( modifier )
    {
      case 1:
        obj[ 1 ] = ( Integer )obj[ 1 ] >> 1;
        break;
      case 2:
      case 3:
      case 8:
        obj[ 1 ] = ( Integer )obj[ 1 ] & 0x0E;
        if ( modifier == 8 )
        {
          Integer val = ( Integer )obj[ 2 ];
          if ( val != null && val > 127 ) obj[ 2 ] = val - 256;
        }
        break;
      case 4:
        obj[ 0 ] = ( Integer )obj[ 0 ] & 0xFC;
        break;
      case 5:
        int n = mode.argMap[ 0 ] - 1;
        if ( ( ( ( Integer )obj[ n ] ) & 1 ) == 1 ) 
        {
          obj[ 0 ] = "*";   // Invalid args
        }
        break;
      case 6:
        if ( ( ( ( Integer )obj[ 0 ] ) & 1 ) == 1 || ( ( ( Integer )obj[ 1 ] ) & 1 ) == 1 ) 
        {
          obj[ 0 ] = "*";   // Invalid args
        }
        break;
    }
  }
  
  @Override
  public void asmModify( int modifier, int[] obj )
  {
    switch ( modifier )
    {
      case 1:
        obj[ 1 ] = obj[ 1 ] << 1;
        break;
    }
  }
  
  @Override
  public boolean checkModifier( AddressMode mode, OpArg args )
  {
    for ( int i = 0; i < args.size(); i++ )
    {
      Token t = args.get( i );
      // If numeric, check against limits
      if ( t.type == TokenType.NUMBER )
      {
        int val = t.value;
        if ( mode.modifier == 8 && ( val < -128 || val > 127 ) ) return false;
        if ( mode.modifier != 8 && ( val < 0 || val > mode.argLimits[ i ] ) ) return false;
      }
    }
    String simpleOutline = null;
    switch ( mode.modifier )
    {
      case 2:
      case 8:
        simpleOutline = args.outline.replace( "WW", "W" );
        break;
      case 5:
        // Only allow replacement in the first argument
        simpleOutline = args.outline + ",";
        int index = simpleOutline.indexOf( "," );
        String start = simpleOutline.substring( 0, index );
        String end = simpleOutline.substring( index, simpleOutline.length() - 1 );
        simpleOutline = start.replace( "RR", "R" ) + end;
        break;
      case 6:
        simpleOutline = args.outline.replace( "RR", "R" );
        break;
      default:
        simpleOutline = args.outline;  
    }
    if ( !mode.outline.equals( simpleOutline ) ) return false;
    switch ( mode.modifier )
    {
      case 2:
      case 3:
      case 8:
        int n = Arrays.asList( mode.argMap ).indexOf( 2 ); // argMap values based at 1
        if ( n < 0 ) return false;
        Integer val = args.get( n ).value;
        return val != null && ( val & 1 ) == 0 && ( mode.modifier != 2 || val != 0 );
      case 4:
        val = args.get( 0 ).value;
        return val != null && ( val & 3 ) == 0;
      case 5:
        val = args.get( 0 ).value;
        return val != null && ( val & 1 ) == 0;
      case 6:
        val = args.get( 0 ).value;
        Integer val2 = args.get( 1 ).value;
        return val != null && val2 != null && ( val & 1 ) == 0 && ( val2 & 1 ) == 0;
      default:
        return true;
    }
  }
  
  @Override
  public void addToMap( AssemblerOpCode op )
  {
    AssemblerOpCode op1 = op.clone();
    int index = op.getIndex();
    switch ( index )
    {
      case 1:
        op1.setHex( new Hex( op1.getHex(), 0, 2 ) );
        op1.getHex().set( (short)1, 1 );
        // run through
      case 4:
        op1.setMode( getAddressModes().get( op.getMode().name + "Z" ) );
        super.addToMap( op1 );
        // run through
      case 0:
      case 5:
        super.addToMap( op );
        if ( index < 2 ) break;
        AssemblerOpCode op2 = op.clone();
        op2.setName( op2.getName().replaceFirst( "C", "E" ) );
        op2.setHex( new Hex( op2.getHex(), 0, 2 ) );
        op2.getHex().set( (short)1, 1 );
        super.addToMap( op2 );
        if ( index == 5 ) break;
        op2 = op1.clone();
        op2.setName( op2.getName().replaceFirst( "C", "E" ) );
        op2.setHex( new Hex( op2.getHex(), 0, 2 ) );
        op2.getHex().set( (short)1, 1 );
        super.addToMap( op2 );
        break;
      case 2:
      case 3:
        op1.setName( op.getName() + ( ( index == 2 ) ? "F" : "R" ) );
        super.addToMap( op1 );
        op1 = op.clone();
        op1.setName( op.getName() + ( ( index == 2 ) ? "T" : "S" ) );
        op1.setHex( new Hex( op1.getHex(), 0, 2 ) );
        op1.getHex().set( (short)1, 1 );
        super.addToMap( op1 );
        break;
      case 6:
        super.addToMap( op );
        op1.setName( op.getName() + "0" );
        op1.setHex( new Hex( op1.getHex(), 0, 2 ) );
        op1.getHex().set( (short)2, 1 );
        super.addToMap( op1 );
        op1 = op.clone();
        op1.setName( op.getName() + "1" );
        op1.setHex( new Hex( op1.getHex(), 0, 2 ) );
        op1.getHex().set( ( short )1, 1 );
        super.addToMap( op1 );
        break;  
    }
    if ( op.getMode().modifier == 7 )
    {
      op1 = op.clone();
      op1.setMode( getAddressModes().get( op.getMode().name + "Z" ) );
      op1.getHex().set( ( short )( op1.getHex().getData()[ 0 ] | ( getConditionIndex( "T" ) << 4 ) ), 0 );
      super.addToMap( op1 );
    }
  }

  @Override
  public AssemblerOpCode getOpCode( Hex hex )
  {
    if ( hex == null || hex.length() == 0 ) return null;
    AssemblerOpCode opCode = getInstructions().get( 0 )[ hex.getData()[ 0 ] ].clone();
    int index = opCode.getIndex();
    if ( index > 0 && hex.length() == 1 ) return new AssemblerOpCode();
    short byte2 = ( index > 0 ) ? hex.getData()[ 1 ] : 0;
    switch ( index )
    {
      case 1:
        if ( ( byte2 & 1 ) == 1 )
        {
          opCode.setMode( getAddressModes().get( opCode.getMode().name + "Z" ) );
        }
        break;
      case 2:
        opCode.setName( opCode.getName() + ( ( ( byte2 & 1 ) == 0 ) ? "F" : "T" ) );
        break;
      case 3:
        opCode.setName( opCode.getName() + ( ( ( byte2 & 1 ) == 0 ) ? "R" : "S" ) );
        break;
      case 4:
        if ( ( byte2 & 0x0E ) == 0 )
        {
          opCode.setMode( getAddressModes().get( opCode.getMode().name + "Z" ) );
        }
        // run through
      case 5:
        if ( ( byte2 & 1 ) == 1 )
        {
          opCode.setName( opCode.getName().replaceFirst( "C", "E" ) );
        }
        break;
      case 6:
        opCode.setName( opCode.getName() + ( ( ( byte2 & 3 ) == 1 ) ? "1" :
          ( ( byte2 & 3 ) == 2 ) ? "0" : "" ) );
        if ( ( byte2 & 3 ) == 3 )
        {
          opCode = new AssemblerOpCode();  // Error
          opCode.getMode().length = 1;
        }
        break;
      default:
        break;
    }
    if ( opCode.getMode() == null )
    {
      opCode.setMode( new AddressMode() ) ; // Error
    }
    if ( opCode.getIndex() > 0 )
    {
      opCode.setIndex( 0 );
    }
    return opCode;
  }
  
  public CodeType testCode( Hex hex )
  {
    if ( hex.length() < 5 ) return CodeType.UNKNOWN;  
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
  
  @Override
  public String getRegisterPrefix()
  {
    return "R";
  }
  
  @Override
  public List< String > getHexPrefixes()
  {
    List< String > list = super.getHexPrefixes();
    list.addAll( Arrays.asList( "R", "RR", "W", "WW" ) );
    return list;
  }
  
  @Override
  public String simplifyOutline( String outline )
  {
    outline = outline.replace( "RR%", "R%" );
    outline = outline.replace( "WW%", "W%" );
    return outline;
  }

}
