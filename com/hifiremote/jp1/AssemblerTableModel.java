package com.hifiremote.jp1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultComboBoxModel;

import com.hifiremote.jp1.AssemblerOpCode.AddressMode;
import com.hifiremote.jp1.assembler.CommonData;
import com.hifiremote.jp1.assembler.S3C80data;

public class AssemblerTableModel extends JP1TableModel< AssemblerItem >
{
  private Hex hex = null;
  private List< AssemblerItem > itemList = new ArrayList< AssemblerItem >();
  private int burstUnit = 0;
  private int pfCount = 0;
  private int pdCount = 0;
  private short[] data = null;
  private Integer[] pf = new Integer[ 5 ];
  
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
    Arrays.fill( dialog.getPfValues(), null );

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
          
          if ( dialog.useAddressConstants.isSelected() )
          {
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
          }
          
          if ( dialog.useRegisterConstants.isSelected() )
          {
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
          }
          
          // Replace numeric args by condition codes where required
          for ( int i = 0; ( mode.ccMap >> i ) != 0; i++ )
          {
            if ( ( ( mode.ccMap >> i ) & 1 ) == 1 && i < argCount )
            {
              obj[ i ] = processor.getConditionCode( ( Integer )obj[ i ] );
            }
          }
        
          // Perform switch of Wn to RCn or vice versa for S3C80
          if ( processor instanceof S3C80Processor )
          {
            if ( dialog.rcButton.isSelected() )
            {
              for ( int i = 0; i < nibbleArgs; i++ )
              {
                if ( obj[ i ] instanceof Integer )
                {
                  format = formatWvRC( format, i, obj, false );
                }
              }              
            }
            else if ( dialog.wButton.isSelected() )
            {
              for ( int i = 0; ( mode.zeroMap >> i ) != 0; i++ )
              {
                if ( ( ( mode.zeroMap >> i ) & 1 ) == 1 && nibbleArgs + i < argCount )
                {
                  int argIndex = nibbleArgs + i;
                  if ( obj[ argIndex ] instanceof Integer )
                  {
                    format = formatWvRC( format, argIndex, obj, true );
                  }
                }
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
          int length = data[ processor.getStartOffset() + 1 ];
          pfCount = dbOut( state.index, state.index + length, addr, processor);
          pdCount = length + processor.getStartOffset() - pfCount - 3;
          interpretPFPD( processor, addr );
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

  private int dbOut( int start, int end, int ramAddress, Processor p )
  {
    data = hex.getData();
    int pfIndex = 5;
    int pdIndex = 0;
    for ( int i = start; i < end;  )
    {
      AssemblerItem item = new AssemblerItem();
      item.setOperation( "DB" );
      int n = Math.min( 4, end - i );
      String comments = null;
      String rp = p.getRegisterPrefix();
      if ( ( i == 0 && p.getStartOffset() == 3 || i == 2 && p.getStartOffset() == 0 ) && i < end - 1 )
      {
        n = 2;
        String val = getFrequency( p, data[ i ], data[ i + 1 ] );
        comments = val;
        dialog.frequency.setText( val );
        
        if ( data[ i ] > 0 && data[ i + 1 ] > 0 )
        {
          val = getDutyCycle( p, data[ i ], data[ i + 1 ] );
          comments += "kHz, " + val + "%";
          dialog.dutyCycle.setText( val );
        }
        else
        {
          dialog.dutyCycle.setText( null );
        }
      }
      else if ( ( i == 2 && p.getStartOffset() == 3 || i == 4 && p.getStartOffset() == 0 ) )
      {
        n = 1;
        comments = "dev " + ( data[ i ] >> 4 ) + ", cmd " + ( data[ i ] & 0x0F ) + " bytes";
        dialog.devBytes.setSelectedIndex( data[ i ] >> 4 );
        dialog.cmdBytes.setSelectedIndex( data[ i ] & 0x0F );
      }
      else if ( i == pfIndex )
      {
        n = 1;
        if ( i < dialog.getPfValues().length + 5 )
        {
          dialog.getPfValues()[ i - 5 ] = ( int )data[ i ];
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
        if ( /* pdIndex <= pfIndex + 2 ||*/ i == pfIndex + p.getZeroSizes().get( "PD00" ) )
        {
          n = 1;
          comments = String.format( "pd%02X: %s%02X", val, rp, za + val );
        }
        else
        {
          n = 2;
          item.setOperation( "DW" );
          comments = String.format( "pd%02X/pd%02X: %s%02X/%s%02X", val, val + 1, rp, za + val, rp, za + val + 1 );
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
    return pfIndex - 4;   // Count of PF values when processed
  }
  
  private void interpretPFPD( Processor p, int ramAddress )
  {
    // DataStyle values:
    //   0 = S3C80
    //   1 = HCS08
    //   2 = 6805-RC16/18
    //   3 = 6805-C9
    //   4 = P8/740
    int dataStyle = p.getDataStyle();
    int pd = 5 + pfCount;
    Arrays.fill( pf, null );
    for ( int i = 0; i < pfCount; i++ )
    {
      pf[ i ] = ( int )data[ i + 5 ];
    }
    
    if ( ( (DefaultComboBoxModel )dialog.devBits1.getModel() ).getSize() == 0 )
    {
      // Populate those combo boxes whose content is fixed
      dialog.populateComboBox( dialog.devBits1, CommonData.to8 );
      dialog.populateComboBox( dialog.cmdBits1, CommonData.to8 );
      dialog.populateComboBox( dialog.xmit0rev, CommonData.noYes );
      dialog.populateComboBox( dialog.leadInStyle, CommonData.leadInStyle );
      dialog.populateComboBox( dialog.offAsTotal, CommonData.noYes );
      dialog.populateComboBox( dialog.useAltLeadOut, CommonData.noYes );
    }
    
    boolean is2 = dialog.devBytes.getSelectedIndex() == 2;
    dialog.devBits1lbl.setText( is2 ? "Bits/Dev1" : "Bits/Dev" );
    dialog.devBits2lbl.setVisible( is2 );
    dialog.populateComboBox( dialog.devBits2, is2 ? CommonData.to8 : null );
    dialog.devBits2.setEnabled( is2 );

    is2 = dialog.cmdBytes.getSelectedIndex() == 2 && ( dataStyle < 3 );
    dialog.cmdBits1lbl.setText( is2 ? "Bits/Cmd1" : "Bits/Cmd" );
    dialog.cmdBits2lbl.setVisible( is2 );
    dialog.populateComboBox( dialog.cmdBits2, is2 ? CommonData.to8 : null );
    dialog.cmdBits2.setEnabled( is2 );
    
    dialog.burstMidFrameLbl.setVisible( dataStyle < 3 );
    dialog.burstMidFrame.setEnabled( dataStyle < 3 );
    dialog.populateComboBox( dialog.burstMidFrame, ( dataStyle < 3 ) ? CommonData.noYes : null );
    dialog.afterBitsLbl.setVisible( dataStyle < 3 );
    dialog.afterBits.setEnabled( dataStyle < 3 );
    dialog.populateComboBox( dialog.leadOutStyle, ( dataStyle < 3 ) ? CommonData.leadOutStyle012 : CommonData.leadOutStyle34 );
    dialog.altFreqLbl.setVisible( dataStyle < 3 );
    dialog.altFreq.setEnabled( dataStyle < 3 );
    dialog.altDutyLbl.setVisible( dataStyle < 3 );
    dialog.altDuty.setEnabled( dataStyle < 3 );

    if ( dataStyle < 3 )
    {
      dialog.populateComboBox( dialog.sigStruct, CommonData.sigStructs012 );
      dialog.populateComboBox( dialog.devBitDbl, CommonData.bitDouble012 );
      dialog.populateComboBox( dialog.cmdBitDbl, CommonData.bitDouble012 );
      dialog.populateComboBox( dialog.rptType, CommonData.repeatType012 );
      dialog.populateComboBox( dialog.rptHold, CommonData.repeatHeld012 );
      
      dialog.devBits1.setSelectedIndex( ( pdCount > 0 ) ? data[ pd + 0 ] : 0 );
      dialog.cmdBits1.setSelectedIndex( ( pdCount > 1 ) ? data[ pd + 1 ] : 0 );
      if ( dialog.devBits2.isEnabled() )
      {
        int n = ( dataStyle < 2 ) ? 0x10 : 0x0E;
        dialog.devBits2.setSelectedIndex( ( pdCount > n ) ? data[ pd + n ] : 0 );
      }
      if ( dialog.cmdBits2.isEnabled() )
      {
        int n = ( dataStyle < 2 ) ? 0x12 : 0x10;
        dialog.cmdBits2.setSelectedIndex( ( pdCount > n ) ? data[ pd + n ] : 0 );
      }
      dialog.sigStruct.setSelectedIndex( ( pf[ 0 ] >> 4 ) & 0x03 );
      dialog.devBitDbl.setSelectedIndex( ( pfCount > 2 ) ? pf[ 2 ] & 3 : 0 );
      dialog.cmdBitDbl.setSelectedIndex( ( pfCount > 2 ) ? ( pf[ 2 ] >> 2 ) & 3 : 0 );
      int n = ( dataStyle < 2 ) ? 0x11 : 0x0F;
      dialog.rptType.setSelectedIndex( ( pfCount > 1 && ( ( pf[ 1 ] & 0x10 ) != 0 ) && pdCount > n && data[ pd + n ] != 0xFF  ) ? 0 : 1 );
      dialog.rptValue.setText( ( dialog.rptType.getSelectedIndex() == 0 ) ? "" + data[ pd + n ] : "" );
      dialog.rptHold.setSelectedIndex( ( pfCount > 1 ) ? pf[ 1 ] & 0x03 : 0 );
      dialog.xmit0rev.setSelectedIndex( ( pfCount > 2 ) ? ( pf[ 2 ] >> 4 ) & 1 : 0 );
      dialog.leadInStyle.setSelectedIndex( ( pfCount > 1 ) ? ( pf[ 1 ] >> 2 ) & 3 : 0 );
      n = pd + pdCount; // start of code
      n = dataStyle + ( ( dataStyle == 0 && ramAddress == 0x8000 ) ? 0 : 1 );
      Hex setMidFrame1 = new Hex( CommonData.midFrameCode1[ n ] );
      Hex setMidFrame2 = new Hex( CommonData.midFrameCode2[ n ] );
      n = pd + pdCount; // start of code
      boolean b = hex.indexOf( setMidFrame1, n ) >= 0 || hex.indexOf( setMidFrame2, n ) >= 0;
      dialog.burstMidFrame.setSelectedIndex( b ? 1 : 0 );
      dialog.afterBits.setText( ( b && pdCount > 0x13 ) ? "" + ( data[ pd + 0x13 ] - 1 ) : "" );
      dialog.leadOutStyle.setSelectedIndex( ( pfCount > 1 ) ? ( pf[ 1 ] >> 5 ) & 3 : 0 );
      dialog.offAsTotal.setSelectedIndex( ( pf[ 0 ] >> 6 ) & 1 );
      dialog.useAltLeadOut.setSelectedIndex( ( pfCount > 3 ) ? ( pf[ 3 ] >> 5 ) & 1 : 0 );
      b = pfCount > 3 && ( pf[ 3 ] & 0x40 ) == 0x40 && pdCount > 0x14 && hex.get( pd + 0x13 ) != 0xFFFF;
      dialog.altFreq.setText( b ?  getFrequency( p, data[ 0x13 ], data[ 0x14 ] ) : "" );
      dialog.altDuty.setText( b ? getDutyCycle( p, data[ 0x13 ], data[ 0x14 ] ) : "" );
      
      if ( dataStyle < 2 )
      {
        dialog.burst1On.setText( ( pdCount > 3 && hex.get( pd + 2 ) > 0 ) ? "" + hex.get( pd + 2 ) * 2 : "" );
        dialog.burst1Off.setText( ( pdCount > 5 && hex.get( pd + 4 ) > 0 ) ? "" + ( hex.get( pd + 4 ) * 2 + ( ( dataStyle == 0 ) ? 40 : 0 ) ) : "" );
        dialog.burst0On.setText( ( pdCount > 7 && hex.get( pd + 6 ) > 0 ) ? "" + hex.get( pd + 6 ) * 2 : "" );
        dialog.burst0Off.setText( ( pdCount > 9 && hex.get( pd + 8 ) > 0 ) ? "" + ( hex.get( pd + 8 ) * 2 + ( ( dataStyle == 0 ) ? 40 : 0 ) ) : "" );
        dialog.leadInOn.setText( ( dialog.leadInStyle.getSelectedIndex() > 0 && pdCount > 0x0D && hex.get( pd + 0x0C ) != 0xFFFF ) ?  "" + hex.get( pd + 0x0C ) * 2 : "" );
        dialog.leadInOff.setText( ( dialog.leadInStyle.getSelectedIndex() > 0 && pdCount > 0x0F && hex.get( pd + 0x0E ) != 0xFFFF ) ?  "" + ( hex.get( pd + 0x0E ) * 2 + ( ( dataStyle == 0 ) ? 40 : 0 ) ) : "" );
        dialog.leadOutOff.setText( ( pdCount > 0x0B && hex.get( pd + 0x0A ) > 0 ) ?  "" + hex.get( pd + 0x0A ) * 2 : "" );
        dialog.altLeadOut.setText( ( dialog.useAltLeadOut.getSelectedIndex() == 1 && pdCount > 0x14 && hex.get( pd + 0x13 ) > 0 ) ? "" + hex.get( pd + 0x13 ) * 2 : ""  );

      }
      else
      {
        int t = ( pdCount > 3 ) ? ( data[ pd + 2 ] >> 4 ) * 0x100 + data[ pd + 3 ] : 0;
        dialog.burst1On.setText( t > 0 ? "" + 4 * ( t + 1 ) : "" );
        t = ( pdCount > 4 ) ? ( data[ pd + 2 ] & 0x0F ) * 0x100 + data[ pd + 4 ] : 0;
        dialog.burst1Off.setText( t > 0 ? "" + 4 * t : "" );
        t = ( pdCount > 6 ) ? ( data[ pd + 5 ] >> 4 ) * 0x100 + data[ pd + 6 ] : 0;
        dialog.burst0On.setText( t > 0 ? "" + 4 * ( t + 1 ) : "" );
        t = ( pdCount > 7 ) ? ( data[ pd + 5 ] & 0x0F ) * 0x100 + data[ pd + 7 ] : 0;
        dialog.burst0Off.setText( t > 0 ? "" + 4 * t : "" );
        t = ( pdCount > 0x0C ) ? ( data[ pd + 0x0B ] >> 4 ) * 0x100 + data[ pd + 0x0C ] : 0;
        dialog.leadInOn.setText( dialog.leadInStyle.getSelectedIndex() > 0 && t > 0 ? "" + 4 * ( t + 1 ) : "" );
        t = ( pdCount > 0x0D ) ? ( data[ pd + 0x0B ] & 0x0F ) * 0x100 + data[ pd + 0x0D ] : 0;
        dialog.leadInOff.setText( dialog.leadInStyle.getSelectedIndex() > 0 && t > 0 ? "" + 4 * t : "" );
        t = ( pdCount > 9 ) ? hex.get( pd + 8 )- 10 : 0; 
        dialog.leadOutOff.setText( t > 0 ? "" + 4 * t : "" );
        t = ( pdCount > 0x12 ) ? hex.get( pd + 0x11 )- 10 : 0;
        dialog.altLeadOut.setText( dialog.useAltLeadOut.getSelectedIndex() == 1 && t > 0 ? "" + 4 * t : "" );
      }

    }
    else
    {
      dialog.populateComboBox( dialog.sigStruct, CommonData.sigStructs34 );
      dialog.populateComboBox( dialog.devBitDbl, CommonData.bitDouble34 );
      dialog.populateComboBox( dialog.cmdBitDbl, CommonData.bitDouble34 );
      dialog.populateComboBox( dialog.rptType, CommonData.repeatType34 );
      dialog.populateComboBox( dialog.rptHold, CommonData.noYes );
      if ( ( ( pf[ 0 ] & 0x58 ) == 0x08 ) )
      {
        dialog.devBits1.setSelectedIndex( ( pdCount > 0x0D ) ? data[ pd + 0x0D ] : 0 );
        if ( dialog.devBits2.isEnabled() )
        {
          dialog.devBits2.setSelectedIndex( 0 );
        }
      }
      else
      {
        dialog.devBits1.setSelectedIndex( ( pdCount > 1 ) ? data[ pd + 1 ] : 0 );
        if ( dialog.devBits2.isEnabled() )
        {
          dialog.devBits2.setSelectedIndex( ( pdCount > 0x0D ) ? data[ pd + 0x0D ] : 0 );
        }
      }
      dialog.cmdBits1.setSelectedIndex( ( pdCount > 2 ) ? data[ pd + 2 ] : 0 );
      String sig = "";
      String items[] = { "devs", "dev", "cmd", "!dev", "dev2", "cmd", "!cmd" };
      int key = ( ( pf[ 0 ] >> 1 ) & 0x3C ) | ( ( pf[ 0 ] >> 2 ) & 1 );
      if ( ( pf[ 0 ] & 0x41 ) == 0x41 )
      {
        key ^= 0x60;  // replace bit for "dev" by that for "devs"
      }
      if ( ( pf[ 0 ] & 0x22 ) == 0x22 )
      {
        key ^= 0x12;  // replace bit for first "cmd" by that for second one
      }
      for ( int i = 0; i < 7; i++ )
      {
        if ( ( ( key << i ) & 0x40 ) == 0x40 )
        {
          sig += items[ i ] + "-";
        }
      }
      sig = sig.substring( 0, sig.length() - 1 );
      dialog.sigStruct.setSelectedItem( sig );
      dialog.devBitDbl.setSelectedIndex( ( pfCount > 2 ) ? ( pf[ 2 ] >> 1 ) & 1 : 0 );
      dialog.cmdBitDbl.setSelectedIndex( ( pfCount > 2 ) ? ( pf[ 2 ] >> 1 ) & 1 : 0 );
      dialog.rptType.setSelectedIndex( ( pfCount > 1 && ( ( pf[ 1 ] & 0x02 ) != 0 ) ) ? 0 : 1 );
      dialog.rptValue.setText( "" );
      dialog.rptHold.setSelectedIndex( ( pfCount > 1 && ( ( pf[ 1 ] & 0x02 ) != 0 ) ) ? 1 : 0 );
      dialog.burst1On.setText( getONtime34( 0, null ) );
      dialog.burst0On.setText( ( pfCount > 2  && ( pf[ 2 ] & 0x08 ) == 0x08 ) ? getONtime34( 0x0E, null ) : getONtime34( 0, null ) );
      dialog.burst1Off.setText( getOFFtime34( 3, CommonData.burstOFFoffsets34, dataStyle ) );
      dialog.burst0Off.setText( getOFFtime34( 5, CommonData.burstOFFoffsets34, dataStyle ) );
      dialog.xmit0rev.setSelectedIndex( ( pfCount > 2 && ( pf[ 2 ] & 0x1C ) == 0x04 ) ? 1 : 0 );
      dialog.leadInStyle.setSelectedIndex( ( pfCount > 1 && (( pf[ 1 ] & 0x10 ) == 0x10 ) ) ? 
         (  ( pf[ 1 ] & 0x04 ) == 0x04 && pdCount > 0x11 && hex.get( pd + 0x10 ) != hex.get( pd + 0x0A ) ) ? 3 : 1 : 0 );
      dialog.leadInOn.setText( dialog.leadInStyle.getSelectedIndex() > 0 ? getONtime34( 9, 0x0C ) : "" );
      dialog.leadInOff.setText( dialog.leadInStyle.getSelectedIndex() > 0 ? getOFFtime34( 0x0A, CommonData.leadinOFFoffsets34, dataStyle ) : "" );
      dialog.offAsTotal.setSelectedIndex( ( dataStyle == 4 && pfCount > 2 ) ? pf[ 2 ] & 1 : 0 );
      dialog.leadOutStyle.setSelectedIndex( ( pfCount > 1 ) ? ( dialog.offAsTotal.getSelectedIndex() == 1 ? 2 : ( pf[ 1 ] >> 5 ) & 2 ) + ( ( pf[ 1 ] >> 5 ) & 1 ) : 0 );
      
      dialog.leadOutOff.setText( ( dataStyle == 3 ) ? getOFFtime34( 7, CommonData.leadinOFFoffsets34, dataStyle ) : ( pdCount > 8 && hex.get( pd + 7 ) > 0 ) ? "" + ( hex.get( pd + 7 ) * 4 - 40 ) : "" );
      
      dialog.useAltLeadOut.setSelectedIndex( ( pfCount > 2 && ( pf[ 1 ] & 4 ) == 4 && ( pf[ 2 ] & 8 ) == 0 && pdCount > 0x0F && hex.get( pd + 0x0E ) != hex.get( pd + 0x07 ) ) ? 1 : 0 );
      dialog.altLeadOut.setText( ( dialog.useAltLeadOut.getSelectedIndex() == 1  ) ? getOFFtime34( 0x0E, CommonData.altLeadoutOffsets34, dataStyle ) : "" );
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
  
  private String formatWvRC( String format, int argIndex, Object[] args, boolean toW )
  {
    int[][] formatStarts = getFormatStarts( format );
    int fStart0 = formatStarts[ argIndex ][ 0 ];
    int fStart1 = formatStarts[ argIndex ][ 1 ];
    boolean preR = fStart0 > 0 && format.substring( fStart0 - 1, fStart0 ).equals( "R" );
    boolean preW = fStart0 > 0 && format.substring( fStart0 - 1, fStart0 ).equals( "W" );
    int arg = ( Integer )args[ argIndex ];
    if ( preR && toW && ( arg & 0xF0 ) == 0xC0 )
    {
      format = replacePart( format, fStart0 - 1, fStart0, "W" );
      format = replacePart( format, fStart1 + 1, fStart1 + 4, "X" );
      args[ argIndex ] = arg & 0x0F;
    }
    else if ( preW && !toW )
    {
      format = replacePart( format, fStart0 - 1, fStart0, "R" );
      format = replacePart( format, fStart1 + 1, fStart1 + 2, "02X" );
      args[ argIndex ] = arg | 0xC0;
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
    burstUnit = 0;
    if ( on > 0 && off > 0 )
    {
      double f = p.getOscillatorFreq()/( on + off + p.getCarrierTotalOffset() );
      burstUnit = ( int )( Math.round( 1000000000 / f ) );
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
    int totOffset = p.getCarrierTotalOffset();
    int onOffset = p.getCarrierOnOffset();
    if ( on > 0 && off > 0 )
    {
      double dc = 100.0 * ( on + onOffset ) / ( on + off + totOffset );
      return String.format( "%.2f", dc );
    }
    else    // Error case handled by dbOut()
    {
      return "";
    }
  }
  
  private String getONtime34( int pdIndex1, Integer pdIndex2 )
  {
    int pd = 5 + pfCount;
    if ( pdCount <= pdIndex1 )
    {
      return "";
    }
    else if ( pfCount > 2 && ( pf[ 2 ] & 0x7C ) == 0x40 )
    {
      int t = ( data[ pd + pdIndex1 ] + 255 ) % 256 + 1;
      return "" + ( 3 * t + 2 );
    }
    else
    {
      int t = ( data[ pd + pdIndex1 ] + 255 ) % 256 + 1;
      if ( pdIndex2 != null && pfCount > 1 && ( pf[ 1 ] & 0x08 ) == 0x08 && pdCount > pdIndex2 )
      {
        t += ( ( data[ pd + pdIndex2 ] + 255 ) % 256 ) * 256;
      }
      return "" + burstUnit * t / 1000;
    }
  }

  private String getOFFtime34( int pdIndex, int[] offsets, int dataStyle )
  {
    int pd = 5 + pfCount;
    if ( pdCount < pdIndex + 1 )
    {
      return "";
    }
    else
    {
      int t = ( data[ pd + pdIndex + 1 ] + 255 ) % 256;
      t += ( ( data[ pd + pdIndex ] + 255 ) % 256 ) * ( ( dataStyle == 3 ) ? 257 : 257.5 );
      t = ( dataStyle == 3 ) ? 3 * t + offsets[ 0 ] : 2 * t + offsets[ 1 ];
      return "" + t;
    }
  }
}
