package com.hifiremote.jp1;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import com.hifiremote.jp1.AssemblerOpCode.AddressMode;
import com.hifiremote.jp1.AssemblerOpCode.OpArg;
import com.hifiremote.jp1.AssemblerOpCode.Token;
import com.hifiremote.jp1.AssemblerOpCode.TokenType;
import com.hifiremote.jp1.AssemblerTableModel.DisasmState;

public class AssemblerItem
{
  private int address = 0;
  private Hex hex = null;
  private String label = "";
  private String operation = "";
  private String argumentText = "";
  private String comments = "";
  private AssemblerOpCode opCode = null;
  private int errorCode = 0;
  
  public AssemblerItem(){};
  
  public AssemblerItem( int address, String operation, String argumentText )
  {
    this.address = address;
    this.operation = operation;
    this.argumentText = argumentText;
  }
  
  public AssemblerItem( int address, Hex hex )
  {
    this.address = address;
    this.hex = hex;
  }
  
  public int disassemble( Processor p, LinkedHashMap< Integer, String > labels, DisasmState state  )
  {
    opCode = p.getOpCode( hex );
    operation = opCode.getName();
    AddressMode mode = opCode.getMode();
    if ( opCode.getLength() + mode.length > hex.length() ) return 0;
    
    int length = opCode.getLength() + mode.length;
    hex = new Hex( hex, 0, length );
    String format = mode.format;
    short[] data = hex.getData();
    Object obj[] = { null, null, null, null };
    int argCount = 0;
    
    // Get format args that are nibble values
    for ( int i = 0; mode.nibbleMap >> i != 0; i++ )
    {
      if ( ( ( mode.nibbleMap >> i ) & 1 ) == 1 )
      {
        int val = data[ i / 2 ];
        val = ( ( i & 1 ) == 0 ) ? val >> 4 : val & 0x0F;
        obj[ argCount++ ] = val;
      }
    }
    
    // Get format args that are byte values
    for ( int i = 0; i < mode.length - mode.nibbleBytes; i++ )
    {
      int val = data[ opCode.getLength() + mode.nibbleBytes + i ];
      obj[ argCount++ ] = val & 0xFF;
    }

    // Apply modifier to args
    p.disasmModify( mode, obj );
    if ( obj[ 0 ] instanceof String && ( ( String )obj[ 0 ] ).equals( "*" ) )
    {
      // Format error discovered by disasmModify()
      format = "%s";
    }
    else
    {
      // Replace relative addresses by labels where they exist (which should be in all cases)
      for ( int i = 0; ( mode.relMap >> i ) != 0; i++ )
      {
        if ( ( ( mode.relMap >> i ) & 1 ) == 1 && mode.nibbleArgs + i < argCount )
        {
          int argIndex = mode.nibbleArgs + i;
          int n = ( ( Integer )obj[ argIndex ] );
          n += address + opCode.getLength() + mode.nibbleBytes + i + 1 - ( n > 0x7F ? 0x100 : 0 );
          obj[ argIndex ] = n;
          String label = labels.get( n );
          if ( label != null )
          {
            format = formatForLabel( format, argIndex, false );
            obj[ argIndex ] = label;
          }
        }
      }

      // Replace absolute addresses by labels where they exist
      for ( int i = 0; ( mode.absMap >> i ) != 0; i++ )
      {
        if ( ( ( mode.absMap >> i ) & 1 ) == 1 && mode.nibbleArgs + i < argCount )
        {
          int argIndex = mode.nibbleArgs + i;
          int[][] formatStarts = AssemblerOpCode.getFormatStarts( format );
          boolean twoByte = format.startsWith( "02", formatStarts[ argIndex ][ 1 ] + 1 );
          boolean littleEndian = !twoByte || formatStarts[ argIndex ][ 0 ] > formatStarts[ argIndex + 1 ][ 0 ];
          int n = ( ( Integer )obj[ argIndex ] ) * ( littleEndian ? 1 : 0x100 );
          if ( twoByte ) n += ( ( Integer )obj[ argIndex + 1 ] ) * ( littleEndian ? 0x100 : 1 );
          String label = p.getAbsLabels().get( n );
          if ( label != null )
          {
            if ( !state.absUsed.contains( n ) )
            {
              state.absUsed.add( n );
            }
            if ( state.useFunctionConstants )
            {
              format = formatForLabel( format, argIndex, twoByte );
              obj[ argIndex ] = label;
              if ( twoByte ) obj[ argIndex + 1 ] = "";
            }
          }
        }
      }

      // Replace zero-page or register addresses by labels where they exist
      for ( int i = 0; ( mode.zeroMap >> i ) != 0; i++ )
      {
        if ( ( ( mode.zeroMap >> i ) & 1 ) == 1 && mode.nibbleArgs + i < argCount )
        {
          int argIndex = mode.nibbleArgs + i;
          int n = ( Integer )obj[ argIndex ];
          if ( setZeroLabel( p, n, state.zeroUsed, "" ) && state.useRegisterConstants )
          {
            format = formatForLabel( format, argIndex, false );
            obj[ argIndex ] = label;
          }
        }
      }

      // Replace numeric args by condition codes where required
      for ( int i = 0; ( mode.ccMap >> i ) != 0; i++ )
      {
        if ( ( ( mode.ccMap >> i ) & 1 ) == 1 && i < argCount )
        {
          obj[ i ] = p.getConditionCode( ( Integer )obj[ i ] );
        }
      }

      // Perform switch of Wn to RCn or vice versa for S3C80
      if ( p instanceof S3C80Processor )
      {
        if ( state.toRC )
        {
          for ( int i = 0; i < mode.nibbleArgs; i++ )
          {
            if ( obj[ i ] instanceof Integer )
            {
              format = formatWvRC( format, i, obj, false );
            }
          }              
        }
        else if ( state.toW )
        {
          for ( int i = 0; ( mode.zeroMap >> i ) != 0; i++ )
          {
            if ( ( ( mode.zeroMap >> i ) & 1 ) == 1 && mode.nibbleArgs + i < argCount )
            {
              int argIndex = mode.nibbleArgs + i;
              if ( obj[ argIndex ] instanceof Integer )
              {
                format = formatWvRC( format, argIndex, obj, true );
              }
            }
          }
        }
      }
      
      if ( mode.modifier == 8 )
      {
        Integer val = ( Integer )obj[ 2 ];
        if ( val != null && val < 0 )
        {
          obj[ 2 ] = - val;
          format = format.replace( "#", "#-" );
        }
      }
    }

    // Create the formatted opcode argument
    argumentText = String.format( format, obj[ 0 ], obj[ 1 ], obj[ 2 ], obj[ 3 ] );

    // Label the item where required
    label = labels.get( address );
    if ( label == null )
    {
      label = "";
    }
    else 
    {
      label += ":";
      if ( !state.relUsed.contains( address ) ) state.relUsed.add( address );
    }
    return length;

  }
  
  public void assemble( Processor p, LinkedHashMap< String, String > labels, boolean checkOffset )
  {
    hex = null;
    opCode = new AssemblerOpCode();
    errorCode = 0;
    LinkedHashMap< String, AssemblerOpCode > opMap = p.getOpMap().get( operation );
    if ( opMap == null )
    {
      errorCode = 1;
      return;
    }
    Set< String > opModes = opMap.keySet();
    OpArg args = p.getArgs( argumentText, labels );
    List< String > argModes = p.getAddressModes( args );
    Iterator< String > it = argModes.iterator();
    while ( it.hasNext() )
    {
      if ( !opModes.contains( it.next() ) ) it.remove();
    }
    
    if ( argModes.size() == 0 )
    {
      errorCode = 2;
      return;
    }
    // If size > 1, precedence is determined by order in AddressModes data for processor
    opCode = opMap.get( argModes.get( 0 ) );
    if ( opCode.getMode().relMap != 0 && !checkOffset ) return;
    
    AddressMode mode = opCode.getMode();
    Integer[] argMap = mode.argMap;
    int[] obj = { 0, 0, 0, 0 };
    for ( int i = 0; i < argMap.length && argMap[ i ] > 0; i++ )
    {
      Token t = args.get( i );
      if ( argMap[ i ] < 5 )
      {
        int n = argMap[ i ] - 1;
        int nArg = n - mode.nibbleArgs;
        if ( nArg >= 0 && ( ( mode.relMap >> nArg ) & 1 ) == 1 && t.type == TokenType.NUMBER )
        {
          t.type = TokenType.OFFSET;
          t.value -= address + opCode.getLength() + mode.length;
        }
        if ( t.type == TokenType.OFFSET && ( t.value < -128 || t.value > 127 ) )
        {
          errorCode = 3;
          return;
        }
        obj[ n ] = ( t.type == TokenType.CONDITION_CODE ) ? p.getConditionIndex( t.text ) : t.value;
      }
      else if ( argMap[ i ] > 0x10 )
      {
        if ( t.type != TokenType.NUMBER ) 
        {
          errorCode = 4;  // This error should not occur
          return;
        }
        obj[ ( argMap[ i ] >> 4 ) - 1 ] = t.value >> 8;
        obj[ ( argMap[ i ] & 0xF ) - 1 ] = t.value & 0xFF;
      }
    }
    p.asmModify( mode.modifier, obj );
    hex = new Hex( opCode.getHex(), 0, opCode.getLength() + mode.length );
    int n = 0;
    for ( int i = 0; ( mode.nibbleMap >> i ) > 0; i++ )
    {
      if ( ( ( mode.nibbleMap >> i ) & 1 ) == 1 )
      {
        hex.set( ( short )( hex.getData()[ i/2 ] | ( obj[ n++ ] & 0xF ) << ( 4 * ( 1 - i%2 ) ) ) , i/2 );
      }
    }
    for ( int i = opCode.getLength() + mode.nibbleBytes; i < opCode.getLength() + mode.length; i++ )
    {
      hex.set( ( short )( hex.getData()[ i ] | ( obj[ n++ ] & 0xFF ) ), i );
    }
    
  }
  
  private String replacePart( String s, int start, int end, String insert )
  {
    return s.substring( 0, start ) + insert + s.substring( end );
  }

  private String formatForLabel( String format, int argIndex, boolean word )
  {
    boolean littleEndian = false;
    int[][] formatStarts = AssemblerOpCode.getFormatStarts( format );
    if ( word )
    {
      littleEndian = ( formatStarts[ argIndex ][ 0 ] > formatStarts[ argIndex + 1 ][ 0 ] );
    }
    int fStart0 = formatStarts[ argIndex + ( littleEndian ? 1 : 0 ) ][ 0 ];
    int fStart1 = formatStarts[ argIndex + ( littleEndian ? 1 : 0 ) ][ 1 ];
    int preLength = 1;
    boolean preSymbol = fStart0 > 0 && ( format.substring( fStart0 - 1, fStart0 ).equals( "$" )
        || format.substring( fStart0 - 1, fStart0 ).equals( "R" ) );
    if ( fStart0 > 1 && format.substring( fStart0 - 2, fStart0 ).equals( "RR" ) )
    {
      preLength = 2;
    }
    if ( word )
    {
      int fStart3 = formatStarts[ argIndex + ( littleEndian ? 0 : 1 ) ][ 1 ];
      format = replacePart( format, fStart3 + 1, fStart3 + ( preSymbol ? 4 : 5 ), "s" );
    }
    format = replacePart( format, fStart1 + 1, fStart1 + ( preSymbol || word ? 4 : 5 ), "s" );
    if ( preSymbol )
    {
      format = replacePart( format, fStart0 - preLength, fStart0, "" ); // remove $, R or RR
    }
    return format;
  }

  private String formatWvRC( String format, int argIndex, Object[] args, boolean toW )
  {
    int[][] formatStarts = AssemblerOpCode.getFormatStarts( format );
    int fStart0 = formatStarts[ argIndex ][ 0 ];
    int fStart1 = formatStarts[ argIndex ][ 1 ];
    boolean preR = fStart0 > 0 && format.substring( fStart0 - 1, fStart0 ).equals( "R" );
    boolean preW = fStart0 > 0 && format.substring( fStart0 - 1, fStart0 ).equals( "W" );
    boolean preRR = fStart0 > 1 && format.substring( fStart0 - 2, fStart0 ).equals( "RR" );
    boolean preWW = fStart0 > 1 && format.substring( fStart0 - 2, fStart0 ).equals( "WW" );
    int arg = ( Integer )args[ argIndex ];
    if ( preRR && toW && ( arg & 0xF0 ) == 0xC0 )
    {
      format = replacePart( format, fStart0 - 2, fStart0, "WW" );
      format = replacePart( format, fStart1 + 1, fStart1 + 4, "X" );
      args[ argIndex ] = arg & 0x0F;
    }
    else if ( preR && toW && ( arg & 0xF0 ) == 0xC0 )
    {
      format = replacePart( format, fStart0 - 1, fStart0, "W" );
      format = replacePart( format, fStart1 + 1, fStart1 + 4, "X" );
      args[ argIndex ] = arg & 0x0F;
    }
    else if ( preWW && !toW )
    {
      format = replacePart( format, fStart0 - 2, fStart0, "RR" );
      format = replacePart( format, fStart1 + 1, fStart1 + 2, "02X" );
      args[ argIndex ] = arg | 0xC0;
    }
    else if ( preW && !toW )
    {
      format = replacePart( format, fStart0 - 1, fStart0, "R" );
      format = replacePart( format, fStart1 + 1, fStart1 + 2, "02X" );
      args[ argIndex ] = arg | 0xC0;
    }
    return format;
  }

  public boolean setZeroLabel( Processor p, int address, List< Integer > addrList, String suffix )
  {
    for ( String text : p.getZeroSizes().keySet() )
    {
      int addr = p.getZeroAddresses().get( text );
      int size = p.getZeroSizes().get( text );
      String labelBody = p.getZeroLabels().get( addr )[ 1 ];
      if ( address > addr && address < addr + size )
      {
        if ( labelBody.length() >= text.length() )
        {
          if ( addrList != null && !addrList.contains( addr ) )
          {
            addrList.add( addr );
          }
          label = labelBody + ( address - addr ) + suffix;
          return true;
        }
        else
        {
          if ( addrList != null && !addrList.contains( address ) )
          {
            addrList.add( address );
          }
          String format = "%0" + ( text.length() - labelBody.length() ) + "X";
          label = labelBody + String.format( format, address - addr ) + suffix;
          return true;
        }
      }
    }
    if ( p.getZeroLabels().get( address ) != null )
    {
      if ( addrList != null && !addrList.contains( address ) )
      {
        addrList.add( address );
      }
      label = p.getZeroLabels().get( address )[ 0 ] + suffix;
      return true;
    }
    return false;
  }

  public int getLength()
  {
    if ( opCode == null || isCommentedOut() ) return 0;
    return opCode.getLength() + opCode.getMode().length;
  }
  
  public boolean isCommentedOut()
  {
    return label.startsWith( ";" );
  }
  
  public int getAddress()
  {
    return address;
  }
  public void setAddress( int address )
  {
    this.address = address;
  }
  public Hex getHex()
  {
    return hex;
  }
  public void setHex( Hex hex )
  {
    this.hex = hex;
  }
  public String getLabel()
  {
    return label;
  }
  public void setLabel( String label )
  {
    this.label = label;
  }
  public String getOperation()
  {
    return operation;
  }
  public void setOperation( String operation )
  {
    this.operation = operation;
  }
  public String getArgumentText()
  {
    return argumentText;
  }
  public void setArgumentText( String argumentText )
  {
    this.argumentText = argumentText;
  }
  public String getComments()
  {
    return comments;
  }
  public void setComments( String comments )
  {
    this.comments = comments;
  }

  public AssemblerOpCode getOpCode()
  {
    return opCode;
  }

  public void setOpCode( AssemblerOpCode opCode )
  {
    this.opCode = opCode;
  }

  public int getErrorCode()
  {
    return errorCode;
  }

  public void setErrorCode( int errorCode )
  {
    this.errorCode = errorCode;
  }
  
  public static String getError( int errorCode )
  {
    switch ( errorCode )
    {
      case 0:
        return "";
      case 1:
        return "Bad op code";
      case 2:
        return "Bad argument";
      case 3:
        return "Out of range";
      case 4:
        return "Assembler error";
      case 5:
        return "Bad value";
      case 6:
        return "ERRORS ARE PRESENT";
      default:
        return "Unknown error";
    }
  }
}
