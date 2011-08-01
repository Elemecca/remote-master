package com.hifiremote.jp1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.hifiremote.jp1.AssemblerOpCode.AddressMode;

public class AssemblerTableModel extends JP1TableModel< AssemblerItem >
{
  private Hex hex = null;
  private List< AssemblerItem > itemList = new ArrayList< AssemblerItem >();
  
  private static final String[] colNames =
  {
      "Addr", "Code", "Label", "Op", "Op Args"
  };
  
  private static final String[] colPrototypeNames =
  {
      "0000", "00 00 00 00", "LXX", "AAAAA", "AAA, AAA, AAA, AAA"
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
    return 5;
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
      for ( int i = 0; i < used.length; i++ )
      {
        used[ i ] = false;
      }
      
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
          int j = 0;
          item.setOperation( oc.getName() );
          
          // Get format args that are nibble values
          int n = 0;
          for ( ; mode.nibbleMap >> n != 0; n++ )
          {
            if ( ( ( mode.nibbleMap >> n ) & 1 ) == 1 )
            {
              int val = data[ start + n / 2 ];
              val = ( ( n & 1 ) == 0 ) ? val >> 4 : val & 0x0F;
              obj[ j++ ] = ( state.shiftFlag && n == state.shiftPos ) ? val >> 1 :
                ( state.nMask != 0 && n == state.shiftPos ) ? val & state.nMask : val;
              nibbleArgs++;
            }
          }
          
          // Get format args that are byte values
          if ( mode.length > mode.nibbleBytes )
          {
            for ( n = 0; n < mode.length - mode.nibbleBytes; n++ )
            {
              int val = data[ state.index + mode.nibbleBytes + n ];
              obj[ j++ ] = ( state.bMask != 0 && n == 0 ) ? val & state.bMask : val;
            }
          }
          
          // Replace relative addresses by labels where they exist (which should be in all cases)
          for ( int i = 1; i < 3; i++ )
          {
            if ( ( mode.relMap & i ) == i && j > nibbleArgs + i - 1 )
            {
              n = ( ( Integer )obj[ nibbleArgs + i - 1 ] );
              n += addr + state.index + mode.nibbleBytes + i - ( n > 0x7F ? 0x100 : 0 );
              obj[ nibbleArgs + i - 1 ] = n;
              int index = labelAddresses.indexOf( n );
              if ( index >= 0 )
              {
                format = format.replaceFirst( "\\$%04X", "%s" );
                format = format.replaceFirst( "04XH", "s" );
                obj[ nibbleArgs + i - 1 ] = "L" + index;
              }
            }
          }
          
          // Replace numeric args by condition codes where required
          if ( mode.ccMap > 0 )
          {
            for ( n = 0; ( mode.ccMap >> n ) != 0; n++ )
            {
              if ( ( ( mode.ccMap >> n ) & 1 ) == 1 )
              {
                obj[ n ] = processor.getConditionCode( ( Integer )obj[ n ] );
              }
            }
          }
          
          // Create the formatted opcode argument
          item.setArgumentText( String.format( format, obj[ 0 ], obj[ 1 ], obj[ 2 ], obj[ 3 ] ) );
          state.index += mode.length;

          // Label the item where required
          int index = labelAddresses.indexOf( item.getAddress() );
          if ( index >= 0 )
          {
            item.setLabel( "L" + index );
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
      for ( int i = 0; i < used.length; i++ )
      {
        if ( !used[ i ] )
        {
          AssemblerItem item = new AssemblerItem();
          item.setLabel( "L" + i );
          item.setOperation( "EQU" );
          String format = processor.getAddressModes().get( "EQU" ).format;
          item.setArgumentText( String.format( format, labelAddresses.get( i ) ) );
          itemList.add( 0, item );
        }
      }
      
    }

    fireTableDataChanged();
  }

  private void dbOut( int start, int end, int ramAddress, Processor p )
  {
    for ( int i = start; i < end;  )
    {
      AssemblerItem item = new AssemblerItem();
      int n = Math.min( 4, end - i );
      String argText = "";
      item.setOperation( "DB" );
      item.setAddress( ramAddress + i );
      item.setHex( hex.subHex( i, n ) );
      for ( int j = 0; j < n; j++ )
      {
        if ( j > 0 )
        {
          argText += ", ";
        }
        if ( ! ( p instanceof S3C80Processor ) )
        {
          argText += "$";
        }
        argText += hex.subHex( i + j, 1 ).toString();
        if ( p instanceof S3C80Processor )
        {
          argText += "H";
        }
      }
      item.setArgumentText( argText );
      i += n;
      itemList.add(  item  );
    }
  }

  
}
