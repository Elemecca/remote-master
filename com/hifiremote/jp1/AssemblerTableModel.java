package com.hifiremote.jp1;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import com.hifiremote.jp1.AssemblerOpCode.AddressMode;
import com.hifiremote.jp1.AssemblerOpCode.OpArg;
import com.hifiremote.jp1.AssemblerOpCode.Token;
import com.hifiremote.jp1.assembler.CommonData;

public class AssemblerTableModel extends JP1TableModel< AssemblerItem >
{
  private Hex hex = null;
  private List< AssemblerItem > itemList = new ArrayList< AssemblerItem >();
//  private int burstUnit = 0;
  private int pfCount = 0;
  private int pdCount = 0;
  private int codeIndex = 0;
  private int midFrameIndex = 0;
  private int forcedRptCount = 0;
  private short[] data = null;
  
  private static final String[] colNames =
  {
      "Addr", "Code", "Label", "Op", "Op Args", "Comments"
  };
  
  private static final String[] colPrototypeNames =
  {
      "0000", "00 00 00 00_", "XMITIR_", "AAAAA", "DCBUF+1, DCBUF+2_", "Carrier OFF: 99.999 uSec"
  };
  
  public AssemblerTableModel()
  {
    setData( itemList );
    selectAllEditor.setClickCountToStart( 1 );
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
  public boolean isCellEditable( int row, int col )
  {
    return ( col > 1 && dialog.asmButton.isSelected() );
  }
  
  @Override
  public TableCellEditor getColumnEditor( int col )
  {
    return selectAllEditor;
  }
  
  @Override
  public TableCellRenderer getColumnRenderer( int col )
  {
    return assemblerCellRenderer;
  }
  
  public ManualSettingsDialog dialog = null;
  
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
        int n = item.getErrorCode();
        return n > 0 ? AssemblerItem.getError( n ) : item.getComments();
      default:
        return null;
    }
  }
  
  @Override
  public void setValueAt( Object value, int row, int column )
  {
    if ( row == itemList.size() - 1 )
    {
      itemList.add( new AssemblerItem() );
    }
    AssemblerItem item = getRow( row );
    String text = ( String )value;
    switch ( column )
    {
      case 1:
        item.setHex( new Hex( text ) );
        return;
      case 2:
        item.setLabel( text );
        return;
      case 3:
        item.setOperation( text );
        return;
      case 4:
        item.setArgumentText( text );
        return;
      case 5:
        item.setComments( text );
        return;
      default:
        return;
    }
  }

  public Hex getHex()
  {
    return hex;
  }
  
  public void setHex( Hex hex )
  {
    this.hex = hex;
  }

  public static class DisasmState
  {
    public boolean useFunctionConstants = true;
    public boolean useRegisterConstants = true;
    public List< Integer > absUsed = new ArrayList< Integer >();
    public List< Integer > zeroUsed = new ArrayList< Integer >();
    public List< Integer > relUsed = new ArrayList< Integer >();
    public boolean toRC = false;
    public boolean toW = false;
  }
  
  public Hex assemble( Processor processor )
  {
    pfCount = 0;
    pdCount = 0;
    codeIndex = 0;
    midFrameIndex = 0;
    forcedRptCount = 0;
    int addr = processor.getRAMAddress();
    
    // Get start address and check S3C80 versus S3F80
    for ( AssemblerItem item : itemList )
    {
      if ( item.isCommentedOut() ) continue;
      if ( item.getOperation().equals( "ORG" ) )
      {
        for ( Token t : OpArg.getArgs( item.getArgumentText(), null, null ) ) addr = t.value;
      }
    }
    if ( processor instanceof S3C80Processor )
    {
      processor = ProcessorManager.getProcessor( ( addr & 0xC000  ) == 0xC000 ? "S3F80" : "S3C80" );
    }
    dialog.setProcessor( processor, addr );

    LinkedHashMap< String, String > asmLabels = processor.getAsmLabels();
    
    // Locate all labels
    for ( AssemblerItem item : itemList )
    {
      if ( item.isCommentedOut() ) continue;
      if ( !item.getLabel().isEmpty() )
      {
        String lbl = item.getLabel().trim().toUpperCase();
        String txt = null;
        if ( lbl.endsWith( ":" )) lbl = lbl.substring( 0, lbl.length() - 1 );
        if ( item.getOperation().equals( "EQU" ) )
        {
          txt = item.getArgumentText().toUpperCase();
          AssemblerOpCode opCode = new AssemblerOpCode();
          opCode.setName( "EQU" );
          opCode.setLength( 0 );
          item.setOpCode( opCode );
        }
        else
        {
          // Set dummy value where final value not yet known
          txt = "FFFFH";
        }
        asmLabels.put( lbl, txt );
      }
    }
    
    // Main assembly loop handles all items not involving relative addresses
    int length = 0;
    AssemblerItem orgItem = null;
    for ( AssemblerItem item : itemList )
    {
      if ( item.isCommentedOut() )
      {
        item.setAddress( 0 );
        item.setHex( new Hex() );
        continue;
      }
      String op = item.getOperation();
      if ( op.isEmpty() )
      {
        continue;
      }
      else if ( Arrays.asList( "DB", "DW", "ORG" ).contains( op ) )
      {
        item.setErrorCode( 0 );
        if ( op.equals( "ORG" ) && orgItem == null ) orgItem = item;
        OpArg args = OpArg.getArgs( item.getArgumentText(), null, asmLabels );
        AssemblerOpCode opCode = new AssemblerOpCode();
        opCode.setName( op );
        opCode.setLength( 0 );
        AddressMode mode = opCode.getMode();
        mode.length = op.equals( "ORG" ) ? 0 : op.equals( "DB" ) ? args.size() : 2 * args.size();
        for ( int i = 0; i < args.size() - 1; i++ ) mode.outline += "%X, ";
        if ( args.size() > 0 ) mode.outline += "%X";
        if ( !args.outline.equals( mode.outline ) || op.equals( "ORG" ) && args.size() > 1 ) item.setErrorCode( 2 );
        Hex hx = new Hex( mode.length );
        
        for ( Token t : args )
        {
          if ( t.value < 0 || t.value > ( op.equals( "DB" ) ? 0xFF : 0xFFFF ) ) item.setErrorCode( 5 );
        }
        
        int n = 0;
        if ( item.getErrorCode() == 0 ) for ( Token t : args )
        {
          if ( op.equals( "ORG" ) )
          {
            addr = t.value;
          }
          else if ( op.equals( "DB" ) )
          {
            hx.set( ( short )( int )t.value, n++ );
          }
          else // "DW"
          {
            hx.put( t.value, n );
            n += 2;
          }
        }
        item.setHex( hx );
        item.setOpCode( opCode );
        if ( !op.equals( "ORG" ) ) item.setAddress( addr );
      }
      else if ( !op.equals( "EQU" ) )
      {
        item.setAddress( addr );
        item.assemble( processor, asmLabels, false );
        // Replace dummy label values with final ones
        if ( !item.getLabel().isEmpty() )
        {
          String txt = item.getLabel();
          if ( txt.endsWith( ":" )) txt = txt.substring( 0, txt.length() - 1 );
          asmLabels.put( txt, Integer.toString( addr ) );
        }
      }
      addr += item.getLength();
      length += item.getLength();
    }
    
    // Assemble those items that do involve relative addresses
    boolean valid = true;
    Hex hexOut = new Hex( length );
    int n = 0;
    for ( AssemblerItem item : itemList )
    {
      if ( item.isCommentedOut() ) continue;
      if ( item.getOpCode() == null ) continue;
      if ( item.getOpCode().getMode().relMap != 0 )
      {
        item.assemble( processor, asmLabels, true );
      }
      valid = valid && ( item.getErrorCode() == 0 );
      if ( valid && item.getHex() != null ) 
      {
        hexOut.put( item.getHex(), n );
        String op = item.getOperation();
        if ( n == processor.getStartOffset() && ( op.equals( "JR" ) || op.equals( "BRA" ) ) )
        {
          pdCount = item.getHex().getData()[ 1 ] + processor.getStartOffset() - 3;
        }
      }
      if ( pdCount == 0 || n < pdCount + 5 ) codeIndex++;
      n += item.getLength();
    }
    if ( valid )
    {
      if ( pdCount > 0 )
      {
        pfCount = 1;
        for ( ; pfCount < pdCount && pfCount < processor.getZeroSizes().get( "PF0" ) && hexOut.getData()[ pfCount + 4 ] >> 7 == 1; pfCount++ );
        pdCount -= pfCount;
      }
      data = hexOut.getData();
      if ( orgItem != null ) orgItem.setComments( "Byte count = " + hexOut.length() );
      midFrameIndex = seekBurstMidFrame( processor );
      forcedRptCount = seekForcedRepeat( processor );
      return hexOut;
    }
    else
    { 
      if ( orgItem != null )
      {
        orgItem.setErrorCode( 6 );
      }
      pdCount = 0;
      return null;
    }
  }

  public void disassemble( Hex hexD, Processor processor )
  {
    itemList.clear();
    this.hex = ( hexD == null ) ? new Hex( 0 ) : new Hex( hexD );
    List< Integer > labelAddresses = new ArrayList< Integer >();
    Arrays.fill( dialog.getBasicValues(), null );
    Arrays.fill( dialog.getPfValues(), null );
    Arrays.fill( dialog.getPdValues(), null );
    pfCount = 0;
    pdCount = 0;
    codeIndex = 0;
    midFrameIndex = 0;
    forcedRptCount = 0;
    int addr = processor.getRAMAddress(); 
    dialog.setDataStyle( processor.getDataStyle() );
    dialog.setProcessor( processor, addr );
    DisasmState state = new DisasmState();
    state.useFunctionConstants = dialog.useFunctionConstants.isSelected();
    state.useRegisterConstants = dialog.useRegisterConstants.isSelected();
    state.toRC = dialog.rcButton.isSelected();
    state.toW = dialog.wButton.isSelected();
    dialog.setAbsUsed( state.absUsed );
    dialog.setZeroUsed( state.zeroUsed );

    if ( hex != null && hex.length() > 0 )
    {
      if ( processor instanceof S3C80Processor 
          && ( ( S3C80Processor )processor ).testCode( hex ) == S3C80Processor.CodeType.NEW )
      {
        addr = S3C80Processor.newRAMAddress;  // S3C8+ code
        processor = ProcessorManager.getProcessor( "S3F80" );
        dialog.setProcessor( processor, addr );
      }
      
//      // Add ORG statement
      dbOut( 0, processor.getStartOffset(), addr, 0, processor );
      Hex pHex = hex.subHex( processor.getStartOffset() );
      short[] data = pHex.getData();
      addr += processor.getStartOffset();

      // Find addresses that need labelling, from all relative addressing modes
      int index = 0;
      while ( index < pHex.length() )
      {
        AssemblerOpCode oc  = processor.getOpCode( pHex.subHex( index ) );
        AddressMode mode = oc.getMode();

        for ( int i = 0; ( mode.relMap >> i ) != 0; i++ )
        {
          if ( ( ( mode.relMap >> i ) & 1 ) == 1 )
          {
            int n = index + oc.getLength() + mode.nibbleBytes + i;
            if ( n < data.length )
            {
              int newAddr = addr + data[ n ] + n + 1 - ( data[ n ] > 0x7F ? 0x100 : 0 );
              if ( !labelAddresses.contains( newAddr ) )
              {
                labelAddresses.add( newAddr );
              }
            }
          }
        }

        if ( index == 0 && ( oc.getName().equals( "JR" ) || oc.getName().equals( "BRA" ) ) )
        {
          index += data[ 1 ];
        }
        index += oc.getLength() + mode.length;
      }
      Collections.sort( labelAddresses );
      LinkedHashMap< Integer, String > labels = new LinkedHashMap< Integer, String >();
      for ( int i = 0; i < labelAddresses.size(); i++ )
      {
        labels.put( labelAddresses.get( i ), "L" + i );
      }
      
      // Disassemble
      index = 0;
      while ( index < pHex.length() )
      {
        AssemblerItem item = new AssemblerItem( addr + index, pHex.subHex( index ) );
        int opLength = item.disassemble( processor, labels, state );
        
        if ( opLength == 0 )  // Instruction incomplete due to hex ending prematurely
        {
          dbOut( index, pHex.length(), addr, processor.getStartOffset(), processor );
          break;
        }

        itemList.add( item );
      
        if ( index == 0 && ( item.getOperation().equals( "JR" ) || item.getOperation().equals( "BRA" ) ) )
        {
          int skip = data[ 1 ];
          pfCount = dbOut( index + 2, index + 2 + skip, addr, processor.getStartOffset(), processor );
          pdCount = skip + processor.getStartOffset() - pfCount - 3;
          codeIndex = itemList.size();
          dialog.interpretPFPD();
          index += data[ 1 ];
        }
        index += opLength;
      }
      
      // Insert EQU statements for any unidentified labels (which are likely to be errors)
      int n = 0;
      for ( Integer address : labels.keySet() )
      {
        if ( state.relUsed.contains( address ) ) continue;
        AssemblerItem item = new AssemblerItem();
        item.setLabel( labels.get( address) + ":" );
        item.setOperation( "EQU" );
        String format = processor.getAddressModes().get( "EQU4" ).format;
        item.setArgumentText( String.format( format, address ) );
        itemList.add( n++, item );
      }
      
      // Insert EQU statements for any used zero-page, register or absolute address labels
      codeIndex += insertEQU( 0, processor, state ) + n;
      
      // Insert ORG statement
      insertORG( 0, processor.getRAMAddress(), processor );
      itemList.get( 0 ).setComments( "Byte count = " + hex.length() );
    }
    itemList.add( new AssemblerItem() );  // Adds blank line at end
    midFrameIndex = seekBurstMidFrame( processor );
    forcedRptCount = seekForcedRepeat( processor );
    fireTableDataChanged();
  }
  
  public void insertORG( int index, int address, Processor processor )
  {
    AssemblerItem item = new AssemblerItem();
    item.setOperation( "ORG" );
    AssemblerOpCode opCode = new AssemblerOpCode();
    opCode.setName( "ORG" );
    opCode.setLength( 0 );
    opCode.getMode().length = 0;
    item.setOpCode( opCode );
    String format = processor.getAddressModes().get( "EQU4" ).format;
    item.setArgumentText( String.format( format, address ) );
    itemList.add( index, item );
  }
  
  public int insertEQU( int index, Processor processor, DisasmState state )
  {
    int count = 0;
    Collections.sort( state.zeroUsed );
    if ( state.useRegisterConstants )
    {
      for ( int address : state.zeroUsed )
      {
        AssemblerItem item = new AssemblerItem();
        item.setZeroLabel( processor, address, null, ":" );
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
        item.setArgumentText( String.format( format, address ) );
        itemList.add( index++, item );
        count++;
      }
    }
    Collections.sort( state.absUsed );
    if ( state.useFunctionConstants )
    {
      for ( int address : state.absUsed )
      {
        AssemblerItem item = new AssemblerItem();
        item.setLabel( processor.getAbsLabels().get( address ) + ":" );
        item.setOperation( "EQU" );
        String format = processor.getAddressModes().get( "EQU4" ).format;
        item.setArgumentText( String.format( format, address ) );
        itemList.add( index++, item );
        count++;
      }
    }
    return count;
  }
  
  public int seekBurstMidFrame( Processor p )
  {
    for ( int index = codeIndex; index < itemList.size(); index++ )
    {
      AssemblerItem item = itemList.get( index );
      if ( item.isCommentedOut() ) continue;
      if ( Arrays.asList( "JP", "CALL", "JMP", "JSR" ).contains( item.getOperation() ) 
          && p.getAbsAddresses().get( "XmitSplitIR") != null 
          && p.getAbsAddresses().get( "XmitSplitIR") == item.getHex().get( 1 ) )
      {
        return index;
      }
    }
    return 0;
  }
  
  public int seekForcedRepeat( Processor p )
  {
    short[] rptData = CommonData.forcedRptCode[ p.getDataStyle() ].getData();
    int n = ( rptData[ 1 ] == 0 ) ? 2 : 1;
    for ( int index = codeIndex; index < itemList.size(); index++ )
    {
      AssemblerItem item = itemList.get( index );
      if ( item.isCommentedOut() ) continue;
      if ( item.getHex() == null ) continue;
      short[] opData = item.getHex().getData();
      switch ( rptData.length )
      {
        case 3:
          if ( opData[ 0 ] == rptData[ 0 ] && opData[ n ] == rptData[ n ] )
          {
            return opData[ 3 - n ] - ( p.getDataStyle() == 4 ? 1 : 0 );
          }
          break;
        case 4:
          if ( opData[ 0 ] == rptData[ 0 ] && index < itemList.size() - 1 )
          {
            short[] opData2 = itemList.get( index + 1 ).getHex().getData();
            if ( opData2[ 0 ] == rptData[ 2 ] && opData[ 1 ] == rptData[ 3 ] ) return opData[ 1 ];
          }
          break;
        case 2:
          if ( opData[ 0 ] == rptData[ 0 ] )
          {
//            int index2 = index + 1 + opData[ 1 ] / 2;
//            if ( opData[ 1 ] % 2 == 1 || index2 >= itemList.size() ) break;
            int destAddr = item.getAddress() + opData[ 1 ] + 2;
            int index2 = index + 1;
            while ( index2 < itemList.size() && itemList.get( index2 ).getAddress() > 0 && itemList.get( index2 ).getAddress() <= destAddr ) 
              index2++;
            AssemblerItem item2 = itemList.get( --index2 );
            if ( item2 == null || item2.getAddress() != destAddr 
                || !Arrays.asList( "JMP", "JSR" ).contains( item2.getOperation() )
                || p.getAbsAddresses().get( "XmitIR") != item2.getHex().get( 1 ) ) break;
            int i = 1;
            for ( ; i < index2 - index; i++ )
            {
              short[] opData2 = itemList.get( index + i ).getHex().getData();
              if ( opData2[ 0 ] != rptData[ 0 ] || opData2[ 1 ] != opData[ 1 ] - 2*i ) break;
            }
            if ( i != index2 - index ) break;
            return opData[ 1 ] / 2 + 2;
          }  
      }
    }
    return 0;
  }

  public int dbOut( int start, int end, int ramAddress, int offset, Processor p )
  {
    // Set addresses and indexes to correspond to full hex code rather than subhex
    // used by disassemble().
    ramAddress -= offset;
    start += offset;
    end += offset;
    data = hex.getData();
    int pfIndex = 5;
    int pdIndex = 0;
    for ( int i = start; i < end;  )
    {
      AssemblerItem item = new AssemblerItem();
      item.setErrorCode( 0 );
      AssemblerOpCode opCode = new AssemblerOpCode();
      item.setOpCode( opCode );
      opCode.setLength( 0 );
      AddressMode mode = opCode.getMode();
      item.setOperation( "DB" );
      int n = Math.min( 4, end - i );
      String comments = null;
      String rp = p.getRegisterPrefix();
      if ( i < 5 )
      {
        n = 1;
        double time = 0;
        switch ( i - ( p.getStartOffset() == 0 ? 2 : 0 ) )
        {
          case 0:
            dialog.getBasicValues()[ 0 ] = data[ i ];
            time = ( data[ i ] + p.getCarrierOnOffset() ) * 1000000.0 / p.getOscillatorFreq();
            comments = data[ i ] == 0 ? "Unmodulated" : "Carrier ON: " + String.format( "%.3f", time ) + "uSec";
            break;
          case 1:
            dialog.getBasicValues()[ 1 ] = data[ i ];
            time = ( data[ i ] + p.getCarrierTotalOffset() - p.getCarrierOnOffset() ) * 1000000.0 / p.getOscillatorFreq();
            comments = data[ i ] == 0 ? "" : "Carrier OFF: " + String.format( "%.3f", time ) + "uSec";
            break;
          case 2:
            dialog.getBasicValues()[ 2 ] = data[ i ];
            comments = "dev " + ( data[ i ] >> 4 ) + ", cmd " + ( data[ i ] & 0x0F ) + " bytes";
            break;
        }
      }
      else if ( i == pfIndex )
      {
        n = 1;
        if ( i < dialog.getPfValues().length + 5 )
        {
          dialog.getPfValues()[ i - 5 ] = data[ i ];
        }
        comments = String.format( "pf%X: %s%02X", i - 5, rp, p.getZeroAddresses().get( "PF0" ) + i - 5 );
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
        int val = i - pfIndex - 1;
        int za = p.getZeroAddresses().get( "PD00" );
        int pdLimit = dialog.getPdValues().length;
        int pdSize = p.getZeroSizes().get( "PD00" );
        if ( val < pdLimit ) dialog.getPdValues()[ val ] = data[ i ];
        if ( val == pdSize - 1 || i == end - 1 )
        {
          n = 1;
          if ( val < pdSize ) comments = String.format( "pd%02X: %s%02X", val, rp, za + val );
        }
        else
        {
          n = 2;
          item.setOperation( "DW" );
          if ( val < pdSize - 1 ) comments = String.format( "pd%02X/pd%02X: %s%02X/%s%02X", val, val + 1, rp, za + val, rp, za + val + 1 );
          if ( val < pdLimit - 1 ) dialog.getPdValues()[ val + 1 ] = data[ i + 1 ];
        }
        pdIndex += n;
        if ( pdIndex > pfIndex + p.getZeroSizes().get( "PD00" ) )
        {
          pdIndex--;
        }
      }

      String argText = "";
      item.setAddress( ramAddress + i );
      item.setHex( hex.subHex( i, n ) );
      item.setComments( comments );
      opCode.setName( item.getOperation() );
      if ( item.getOperation().equals( "DB" ) )
      {
        mode.length = 1;
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
        mode.length = 2;
        argText = String.format( p.getAddressModes().get( "EQU4" ).format, hex.get( i ) );
      }
      item.setArgumentText( argText );
      i += n;
      itemList.add(  item  );
    }
    return pfIndex - 4;   // Count of PF values when processed
  }
  
  public int getPfCount()
  {
    return pfCount;
  }

  public int getPdCount()
  {
    return pdCount;
  }

  public int getMidFrameIndex()
  {
    return midFrameIndex;
  }

  public void setMidFrameIndex( int midFrameIndex )
  {
    this.midFrameIndex = midFrameIndex;
  }

  public int getForcedRptCount()
  {
    return forcedRptCount;
  }

  public void setForcedRptCount( int forcedRptCount )
  {
    this.forcedRptCount = forcedRptCount;
  }

  public void setPfCount( int pfCount )
  {
    this.pfCount = pfCount;
  }

  public void setPdCount( int pdCount )
  {
    this.pdCount = pdCount;
  }

  private static class AssemblerCellRenderer extends DefaultTableCellRenderer
  {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int col ) 
    {
      Component c = super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, col );
      AssemblerTableModel model = ( AssemblerTableModel )table.getModel();
      c.setForeground( model.getRow( row ).getErrorCode() == 0 ? Color.BLACK : Color.RED );
      return c;
    }
  }
  
  public List< AssemblerItem > getItemList()
  {
    return itemList;
  }
  
  public void setItemList( List< AssemblerItem > itemList )
  {
    this.itemList = itemList;
//    setData( itemList );
//    fireTableDataChanged();
  }
  
  public boolean testBuildMode( Processor processor )
  {
    int length = 0;
    for ( AssemblerItem item : itemList )
    {
      if ( item.isCommentedOut() ) continue;
      length += item.getLength();
      if ( length > processor.getStartOffset() )
      {
        return false;
      }
    }
    return true;
  }

  private SelectAllCellEditor selectAllEditor = new SelectAllCellEditor();
  private AssemblerCellRenderer assemblerCellRenderer = new AssemblerCellRenderer();
  
}
