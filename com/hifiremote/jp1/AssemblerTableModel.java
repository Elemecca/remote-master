package com.hifiremote.jp1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.hifiremote.jp1.AssemblerOpCode.AddressMode;
import com.hifiremote.jp1.assembler.S3C80data;

public class AssemblerTableModel extends JP1TableModel< AssemblerItem >
{
  private Hex hex = null;
  private List< AssemblerItem > itemList = new ArrayList< AssemblerItem >();
  
  private static final String[] colNames =
  {
      "Addr", "Code", "Label", "Op", "Op Args", "Comments"
  };
  
  private static final String[] colPrototypeNames =
  {
      "0000", "00 00 00 00", "XMITIR_", "AAAAA", "DCBUF+1, DCBUF+2_", "99.999kHz, 99.99%_"
  };
  
  public AssemblerTableModel()
  {
    setData( itemList );
  }

  @Override
  public String getColumnName( int col )
  {
    return colNames[ col ];
  }
  
  @Override
  public String getColumnPrototypeName( int col )
  {
    return colPrototypeNames[ col ];
  }

  @Override
  public int getColumnCount()
  {
    return 6;
  }
  
  @Override
  public boolean isColumnWidthFixed( int col )
  {
    return col <= 1;
  }
  
  @Override
  public Object getValueAt( int row, int column )
  {
    AssemblerItem item = getRow( row );
    switch ( column )
    {
      case 0:
        if ( item.getAddress() == 0 )
          return "";
        String hexStr = Integer.toHexString( item.getAddress() );
        hexStr = "0000".substring( hexStr.length() ) + hexStr;
        return hexStr.toUpperCase();
      case 1:
        return item.getHex();
      case 2:
        return item.getLabel();
      case 3:
        return item.getOperation();
      case 4:
        return item.getArgumentText();
      case 5:
        return item.getComments();
      default:
        return null;
    }
  }

  public Hex getHex()
  {
    return hex;
  }
  
  public class DisasmState
  {
    public int index = 0;             // current index into data
    public boolean shiftFlag = false; // flags a one-bit right shift of nibble value
    public int shiftPos = 0;          // position of nibble to be shifted or masked
    public int nMask = 0;             // if non-zero, mask to be applied to nibble value
    public int bMask = 0;             // if non-zero, mask to be applied to first byte argument
  }

  public void disassemble( Protocol protocol, Processor processor )
  {
    itemList.clear();
    hex = protocol.getCode( processor );
    List< Integer > labelAddresses = new ArrayList< Integer >();
    if ( hex != null )
    {
      short[] data = hex.getData();
      int addr = processor.getRAMAddress();      
      if ( processor instanceof S3C80Processor 
          && ( ( S3C80Processor )processor ).testCode( hex ) == S3C80Processor.CodeType.NEW )
      {
        addr = S3C80Processor.newRAMAddress;  // S3C8+ code
        processor.setAbsLabels( S3C80data.absLabels_F80 );
      }
      DisasmState state = new DisasmState();
      
      dbOut( 0, processor.getStartOffset(), addr, processor );
      state.index = processor.getStartOffset();

      // Find addresses that need labelling, from all relative addressing modes
      while ( state.index < hex.length() )
      {
        AssemblerOpCode oc  = processor.getOpCode( data, state );
        AddressMode mode = oc.getMode();

        for ( int i = 1; i < 3; i++ )
        {
          if ( ( mode.relMap & i ) == i )
          {
            int n = mode.nibbleBytes + state.index + i;
            if ( n <= data.length )
            {
              int newAddr = addr + data[ n - 1 ] + n - ( data[ n - 1 ] > 0x7F ? 0x100 : 0 );
              if ( !labelAddresses.contains( newAddr ) )
              {
                labelAddresses.add( newAddr );
              }
            }
          }
        }
        state.index += mode.length;

        if ( state.index == processor.getStartOffset() + 2 && ( oc.getName() == "JR" || oc.getName() == "BRA" ) )
        {
          state.index += data[ processor.getStartOffset() + 1 ];
        }

      }
      Collections.sort(  labelAddresses );
      
      boolean used[] = new boolean[ labelAddresses.size() ];
      Arrays.fill( used, false );
      List< Integer > absUsed = new ArrayList< Integer >();
      List< Integer > zeroUsed = new ArrayList< Integer >();
   
      state.index = processor.getStartOffset();
      
      // Disassemble
      while ( state.index < hex.length() )
      {
        AssemblerItem item = new AssemblerItem();
        item.setAddress( addr + state.index );
        int start = state.index;
        
        AssemblerOpCode oc  = processor.getOpCode( data, state );
        AddressMode mode = oc.getMode();
        
        if ( state.index + mode.length > hex.length() )
        {
          dbOut( start, hex.length(), addr, processor );
          break;
        }

        String format = mode.format;
        Object obj[] = { null, null, null, null };
        
        if ( oc.getIndex() == 0 )
        {
          int nibbleArgs = 0;
          int argCount = 0;
          item.setOperation( oc.getName() );
          
          // Get format args that are nibble values
          for ( int i = 0; mode.nibbleMap >> i != 0; i++ )
          {
            if ( ( ( mode.nibbleMap >> i ) & 1 ) == 1 )
            {
              int val = data[ start + i / 2 ];
              val = ( ( i & 1 ) == 0 ) ? val >> 4 : val & 0x0F;
              obj[ argCount++ ] = ( state.shiftFlag && i == state.shiftPos ) ? val >> 1 :
                ( state.nMask != 0 && i == state.shiftPos ) ? val & state.nMask : val;
              nibbleArgs++;
            }
          }
          
          // Get format args that are byte values
          for ( int i = 0; i < mode.length - mode.nibbleBytes; i++ )
          {
            int val = data[ state.index + mode.nibbleBytes + i ];
            obj[ argCount++ ] = ( state.bMask != 0 && i == 0 ) ? val & state.bMask : val;
          }
          
          // Replace relative addresses by labels where they exist (which should be in all cases)
          for ( int i = 0; ( mode.relMap >> i ) != 0; i++ )
          {
            if ( ( ( mode.relMap >> i ) & 1 ) == 1 && nibbleArgs + i < argCount )
            {
              int argIndex = nibbleArgs + i;
              int n = ( ( Integer )obj[ argIndex ] );
              n += addr + state.index + mode.nibbleBytes + i + 1 - ( n > 0x7F ? 0x100 : 0 );
              obj[ argIndex ] = n;
              int index = labelAddresses.indexOf( n );
              if ( index >= 0 )
              {
                format = formatForLabel( format, argIndex, false );
                obj[ argIndex ] = "L" + index;
              }
            }
          }
          
          // Replace absolute addresses by labels where they exist
          for ( int i = 0; ( mode.absMap >> i ) != 0; i++ )
          {
            if ( ( ( mode.absMap >> i ) & 1 ) == 1 && nibbleArgs + i < argCount )
            {
              int argIndex = nibbleArgs + i;
              int[][] formatStarts = getFormatStarts( format );
              boolean littleEndian = ( formatStarts[ argIndex ][ 0 ] > formatStarts[ argIndex + 1 ][ 0 ] );
              int n = ( ( Integer )obj[ argIndex ] ) * ( littleEndian ? 1 : 0x100 );
              n += ( ( Integer )obj[ argIndex + 1 ] ) * ( littleEndian ? 0x100 : 1 );
              String label = processor.getAbsLabels().get( n );
              if ( label != null )
              {
                if ( !absUsed.contains( n ) )
                {
                  absUsed.add( n );
                }
                format = formatForLabel( format, argIndex, true );
                obj[ argIndex ] = label;
                obj[ argIndex + 1 ] = "";
              }
            }
          }
          
       // Replace zero-page or register addresses by labels where they exist
          for ( int i = 0; ( mode.zeroMap >> i ) != 0; i++ )
          {
            if ( ( ( mode.zeroMap >> i ) & 1 ) == 1 && nibbleArgs + i < argCount )
            {
              int argIndex = nibbleArgs + i;
              int n = ( Integer )obj[ argIndex ];
              String label = getZeroLabel( processor, n, zeroUsed );
              if ( label != null )
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
              obj[ i ] = processor.getConditionCode( ( Integer )obj[ i ] );
            }
          }
        
          
          // Create the formatted opcode argument
          item.setArgumentText( String.format( format, obj[ 0 ], obj[ 1 ], obj[ 2 ], obj[ 3 ] ) );
          state.index += mode.length;

          // Label the item where required
          int index = labelAddresses.indexOf( item.getAddress() );
          if ( index >= 0 )
          {
            item.setLabel( "L" + index + ":" );
            used[ index ] = true;
          }
        }
        else
        {
          item.setOperation( "<Error>" );
        }
        
        item.setHex( hex.subHex( start, Math.min( hex.length() - start, state.index - start ) ) );
        itemList.add( item );
      
        if ( state.index == processor.getStartOffset() + 2 && ( oc.getName() == "JR" || oc.getName() == "BRA" ) )
        {
          dbOut( state.index, state.index + data[ processor.getStartOffset() + 1 ], addr, processor);
          state.index += data[ processor.getStartOffset() + 1 ];
        }
      }
      
      // Create EQU statements for any unidentified labels (which are likely to be errors)
      for ( int i = used.length - 1; i >= 0; i-- )
      {
        if ( !used[ i ] )
        {
          AssemblerItem item = new AssemblerItem();
          item.setLabel( "L" + i + ":" );
          item.setOperation( "EQU" );
          String format = processor.getAddressModes().get( "EQU" ).format;
          item.setArgumentText( String.format( format, labelAddresses.get( i ) ) );
          itemList.add( 0, item );
        }
      }
      // Create EQU statements for any used absolute address labels
      Collections.sort( absUsed );
      for ( int i = absUsed.size() - 1; i >= 0; i-- )
      {
        AssemblerItem item = new AssemblerItem();
        item.setLabel( processor.getAbsLabels().get( absUsed.get( i ) ) + ":" );
        item.setOperation( "EQU" );
        String format = processor.getAddressModes().get( "EQU4" ).format;
        item.setArgumentText( String.format( format, absUsed.get( i ) ) );
        itemList.add( 0, item );
      }
      // Create EQU statements for any used zero-page or register address labels
      Collections.sort( zeroUsed );
      for ( int i = zeroUsed.size() - 1; i >= 0; i-- )
      {
        AssemblerItem item = new AssemblerItem();
        item.setLabel( getZeroLabel( processor, zeroUsed.get( i ), null ) + ":" );
        item.setOperation( "EQU" );
        String format = null;
        if ( processor.getAddressModes().get( "EQUR" ) == null )
        {
          format = processor.getAddressModes().get( "EQU2" ).format;
        }
        else
        {
          format = processor.getAddressModes().get( "EQUR" ).format;
        }  
        item.setArgumentText( String.format( format, zeroUsed.get( i ) ) );
        itemList.add( 0, item );
      }
      
    }

    fireTableDataChanged();
  }

  private void dbOut( int start, int end, int ramAddress, Processor p )
  {
    short[] data = hex.getData();
    int pfIndex = 5;
    int pdIndex = 0;
    for ( int i = start; i < end;  )
    {
      AssemblerItem item = new AssemblerItem();
      item.setOperation( "DB" );
      int n = Math.min( 4, end - i );
      String comments = null;
      if ( ( i == 0 && p.getStartOffset() == 3 || i == 2 && p.getStartOffset() == 0 ) && i < end - 1 )
      {
        n = 2;
        comments = getFrequency( p, data[ i ], data[ i + 1 ] );
        if ( data[ i ] > 0 && data[ i + 1 ] > 0 )
        {
          comments += "kHz, " + getDutyCycle( p, data[ i ], data[ i + 1 ] ) + "%";
        }
      }
      else if ( ( i == 2 && p.getStartOffset() == 3 || i == 4 && p.getStartOffset() == 0 ) )
      {
        n = 1;
        comments = "dev " + ( data[ i ] >> 4 ) + ", cmd " + ( data[ i ] & 0x0F ) + " bytes";
      }
      else if ( i == pfIndex )
      {
        n = 1;
        comments = "pf" + ( i - 5 );
        if ( data[ i ] >> 7 == 1 )
        {
          pfIndex++;  // Another pf follows
        }
        pdIndex = i + 1;
        if ( pfIndex > p.getZeroSizes().get( "PF0" ) + 4 )
        {
          comments = "** Error **";   // Too many pf's
          pfIndex--;
        }
      }
      else if ( i == pdIndex )
      {
        if ( /* pdIndex <= pfIndex + 2 ||*/ pdIndex == pfIndex + p.getZeroSizes().get( "PD00" ) )
        {
          n = 1;
          comments = "pd" + String.format( "%02X", pdIndex - pfIndex - 1 );
          pdIndex++;
        }
        else
        {
          n = 2;
          item.setOperation( "DW" );
          comments = String.format( "pd%02X/pd%02X", pdIndex - pfIndex - 1, pdIndex - pfIndex );
          pdIndex += 2;
        }
        if ( pdIndex > pfIndex + p.getZeroSizes().get( "PD00" ) )
        {
          pdIndex--;
        }
      }

      String argText = "";
      item.setAddress( ramAddress + i );
      item.setHex( hex.subHex( i, n ) );
      item.setComments( comments );
      if ( item.getOperation().equals( "DB" ) )
      {
        for ( int j = 0; j < n; j++ )
        {
          if ( j > 0 )
          {
            argText += ", ";
          }
          argText += String.format( p.getAddressModes().get( "EQU2" ).format, data[ i + j ] );
        }
      }
      else // DW
      {
        argText = String.format( p.getAddressModes().get( "EQU4" ).format, hex.get( i ) );
      }
      item.setArgumentText( argText );
      i += n;
      itemList.add(  item  );
    }
  }
  
  private int[][] getFormatStarts( String format )
  {
    int[][] starts = new int[ 4 ][ 2 ];
    for ( int i = 0, j = 0; i < format.length(); i++ )
    {
      if ( format.substring( i, i + 1 ).equals( "%" ) )
      {
        int pos = j++;  // implicit arg index
        int start = i;
        int k = i + 1;
        for ( ; k < format.length() && Character.isDigit( format.charAt( k ) ); k++ );
        {
          if ( k < format.length() && format.substring( k, k + 1 ).equals( "$" ) )
          {
            pos = Integer.parseInt( format.substring( i + 1, k ) ) - 1; // explicit arg index
            start = k;
          }
        }
        starts[ pos ][ 0 ] = i;
        starts[ pos ][ 1 ] = start;
      }
    }
    return starts;
  }

  private String replacePart( String s, int start, int end, String insert )
  {
    return s.substring( 0, start ) + insert + s.substring( end );
  }
  
  private String formatForLabel( String format, int argIndex, boolean word )
  {
    boolean littleEndian = false;
    int[][] formatStarts = getFormatStarts( format );
    if ( word )
    {
      littleEndian = ( formatStarts[ argIndex ][ 0 ] > formatStarts[ argIndex + 1 ][ 0 ] );
    }
    int fStart0 = formatStarts[ argIndex + ( littleEndian ? 1 : 0 ) ][ 0 ];
    int fStart1 = formatStarts[ argIndex + ( littleEndian ? 1 : 0 ) ][ 1 ];
    boolean preSymbol = fStart0 > 0 && ( format.substring( fStart0 - 1, fStart0 ).equals( "$" )
        || format.substring( fStart0 - 1, fStart0 ).equals( "R" ) );
    if ( word )
    {
      int fStart3 = formatStarts[ argIndex + ( littleEndian ? 0 : 1 ) ][ 1 ];
      format = replacePart( format, fStart3 + 1, fStart3 + ( preSymbol ? 4 : 5 ), "s" );
    }
    format = replacePart( format, fStart1 + 1, fStart1 + ( preSymbol || word ? 4 : 5 ), "s" );
    if ( preSymbol )
    {
      format = replacePart( format, fStart0 - 1, fStart0, "" ); // remove $ or R
    }
    return format;
  }
  
  private String getZeroLabel( Processor p, int address, List< Integer > addrList )
  {
    for ( String label : p.getZeroSizes().keySet() )
    {
      int addr = p.getZeroAddresses().get( label );
      int size = p.getZeroSizes().get( label );
      String labelBody = p.getZeroLabels().get( addr )[ 1 ];
      if ( address > addr && address < addr + size )
      {
        if ( labelBody.length() >= label.length() )
        {
          if ( addrList != null && !addrList.contains( addr ) )
          {
            addrList.add( addr );
          }
          return labelBody + ( address - addr );
        }
        else
        {
          if ( addrList != null && !addrList.contains( address ) )
          {
            addrList.add( address );
          }
          String format = "%0" + ( label.length() - labelBody.length() ) + "X";
          return labelBody + String.format( format, address - addr );
        }
      }
    }
    if ( p.getZeroLabels().get( address ) != null )
    {
      if ( addrList != null && !addrList.contains( address ) )
      {
        addrList.add( address );
      }
      return p.getZeroLabels().get( address )[ 0 ];
    }
    return null;
  }
  
  private String getFrequency( Processor p, int on, int off )
  {
    if ( on > 0 && off > 0 )
    {
      double f = p.getOscillatorFreq()/( on + off + p.getCountOffset() );
      return String.format( "%.3f", f/1000 );
    }
    else if ( on == 0 && off == 0 )
    {
      return "No carrier";
    }
    else
    {
      return "** Error **";
    }
  }
  
  private String getDutyCycle( Processor p, int on, int off )
  {
    int ctOffset = p.getCountOffset();
    int onOffset = ( ctOffset + 2 ) / 3;
    if ( on > 0 && off > 0 )
    {
      double dc = 100.0 * ( on + onOffset ) / ( on + off + ctOffset );
      return String.format( "%.2f", dc );
    }
    else    // Error case handled by dbOut()
    {
      return "";
    }
  }
}
