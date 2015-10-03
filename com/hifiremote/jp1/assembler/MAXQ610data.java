package com.hifiremote.jp1.assembler;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import com.hifiremote.jp1.AssemblerItem;
import com.hifiremote.jp1.AssemblerOpCode.AddressMode;
import com.hifiremote.jp1.AssemblerTableModel.DisasmState;
import com.hifiremote.jp1.AssemblerOpCode;
import com.hifiremote.jp1.Hex;
import com.hifiremote.jp1.Processor;
import com.hifiremote.jp1.ProcessorManager;
import com.hifiremote.jp1.Protocol;
import com.hifiremote.jp1.ProtocolManager;
import com.sun.org.apache.xpath.internal.axes.HasPositionalPredChecker;

public class MAXQ610data
{
//  private boolean show = false;
  private PrintWriter pw = null;
  private String s = null;
  private Executor e = null;
  private IRPstruct irpStruct = null;
  private Processor proc = null;
  private int carrier = 0;
  private int altCarrier = 0;
  private int fix = 0;
  private int var = 0;
  private int[] pf = null;
  private int[] pfNew = null;
  private int[] pfChanges = null;
  private short[] tbLengths = null;
  private int[] tbDurations = null;
  private int tbUsed = 0;
  private int maxBlocks = 1;
  private int blockCount = 0;
  private int pbIndex = 0;
  private int sbIndex = 0;
  private List< Executor > execs = new ArrayList< Executor >();
  private List< AssemblerItem > itemList = new ArrayList< AssemblerItem >();
  private List< AssemblerItem > fullItemList = null;
  private List< AssemblerItem > completeItemList = null;
  private LinkedHashMap< String, Integer > labelIndex = new LinkedHashMap< String, Integer >();
  private List< Integer > labelAddresses = new ArrayList< Integer >();
  private LinkedHashMap< Integer, String > labels = new LinkedHashMap< Integer, String >();
  private LinkedHashMap< Integer, Node > nodeList = new LinkedHashMap< Integer, Node >();
  private String irp = null;
  private String irpErr = null;
  private double unit = 0;
  private boolean hasNativeCode = false;
  private boolean has2usOff = false;
  private boolean changesFreq = false;
  private int initialCodeSpec = -1;
  private boolean[] choices = null;
  private String[] irpParts = new String[ 20 ];
  private int brackets = 0;
  private int minRpts = 0;
  private boolean dataChangeOnly = true;
  private boolean dataTimingChangeOnly = true;
  
  /*  
   *  Choices[] and irpParts[] elements:
   *  0  lead-in
   *  1  lead-out
   *  2  oneOn
   *  3  midFrame
   *  4  endFrame
   *  5  altLeadIn
   *  6  altLeadOut
   *  7  altEnd
   *  8  no lead-in on repeats
   *  9  halved-style repeats
   * 10  total-time lead-out
   * 11  data
   * 12  repeats on key held
   * 13  after repeats, if key held repeat protocol block
   *     (if false, key held action determined by 14)
   * 14  after repeats, if key held, continue to next signal block
   *     (if 13 and 14 false, terminate)
   * 15  toggle
   * 16  after repeats, re-execute current signal block
   * 17  assignments
   * 18  after repeats, if key not held repeat protocol block
   *     (if false, key not held action determined by 19)
   * 19  after repeats, if key not held, continue to next signal block
   *     (if 18 and 19 false, terminate)
   */
  
  private class Executor
  {
    public int index = 0;
    public List< String > names = new ArrayList< String >();
    public Hex hex = null;
    public String description = null;
    public List< IRPstruct > irps = new ArrayList< IRPstruct >();
    public boolean hasPBcode = false;
    public boolean hasSBcode = false;
    public boolean pbHandled = true;
  }
  
  private class IRPstruct
  {
    public String generalSpec = null;
    public String bitSpec= null;
    public String irStream = null;
    public int unit = 0;
    
    @Override
    public boolean equals( Object obj )
    {
      if ( obj == null || generalSpec == null || bitSpec == null || irStream == null )
        return false;
      IRPstruct is = ( IRPstruct )obj;
      if ( !generalSpec.equals( is.generalSpec ) )
        return false;
      if ( !bitSpec.equals( is.bitSpec ) )
        return false;
      if ( !irStream.equals( is.irStream ) )
        return false;

      return true;
    }
  }
  
  private class IRPIndexItem
  {
    IRPstruct irp = null;
    int[] location = new int[ 2 ];
  }
  
  private class TimingStruct
  {
    public TimingStruct()
    {
      this.carriers = new int[ 3 ];
      this.durations = new int[ 0x40 ];
      this.pf = new int[ 0x10 ];
    }
    
    public TimingStruct( int[] carriers, int[] durations, int[] pf )
    {
      this.carriers = carriers.clone();
      this.durations = Arrays.copyOf( durations, 0x40 );
      this.pf = Arrays.copyOf( pf, 0x10 );
    }
    
    public TimingStruct clone()
    {
      TimingStruct ts = new TimingStruct( carriers, durations, pf );
      LinkedHashMap< Integer, int[] > sbp = new LinkedHashMap< Integer, int[] >();
      for ( int n : sbPaths.keySet() )
      {
        sbp.put(  n, sbPaths.get( n  ).clone() );
      }
      ts.sbPaths = sbp;
      ts.pbPath = pbPath;
//      ts.minRpts = minRpts;
      return ts;
    }
 
    public int[] carriers = null;
    public int[] durations = null;
    public int[] pf = null;
//    public int minRpts = 0;
    public boolean changed = false;
    public Integer pbPath = null;
    public LinkedHashMap< Integer, int[] > sbPaths = new LinkedHashMap< Integer, int[] >();
  }
  
  private class Node
  {
    public int start = 0;
    public int[] branch = null;
    /*  branch elements are:
        0 = end
        1 = start for left branch (-1 if no branch)
        2 = start for right branch (-1 if no branch)
    */
    
    public Node( int start )
    {
      this.start = start;
    }
  }
  
  public void analyze()
  {
    int execCount = 0;
    int execNoCodeCount = 0;
    int execNoSBCodeCount = 0;
    int execNoSBDataChPBCount = 0;
    int execNoSBDataTimeChPBCount = 0;
    proc = ProcessorManager.getProcessor( "MAXQ610" );
    LinkedHashMap< Hex, Executor > execMap = new LinkedHashMap< Hex, Executor >();
    ProtocolManager pm = ProtocolManager.getProtocolManager();
    for ( String name : pm.getNames() )
    {
      for ( Protocol p : pm.findByName( name ) )
      {
        Hex hex = p.getCode().get( "MAXQ610");
        if ( hex != null )
        {
          String eName = p.getName() + ": PID=" + p.getID().toString().replaceAll( "\\s", "" );
          String var = p.getVariantName();
          if ( var != null && !var.isEmpty() )
          {
            eName += "." + var;
          }
          Executor e = execMap.get( hex );
          if ( e != null )
          {
            e.names.add( eName );     
          }
          else
          {
            e = new Executor();
            e.names.add( eName );
            e.hex = hex;
            execMap.put( hex, e );
            execs.add( e );
          }
        }
      }
    }
    
    int eIndex = 0;
    for ( Executor e : execs )
    {
      this.e = e;
      e.index = eIndex++;
      Hex hex = e.hex;
      irpStruct = new IRPstruct();
      execCount++;
      s = "";
      labels.clear();
      fullItemList = new ArrayList< AssemblerItem >();
      completeItemList = null;
      labelAddresses.clear();
      initialCodeSpec = -1;
      changesFreq = false;
      tbUsed = 0;
      pf = new int[ 16 ];
      pfChanges = new int[ 16 ];
      Arrays.fill( pfChanges, 0 );
      pbIndex = 0;
      sbIndex = 0;
      analyzeExecutor( hex );
      labelIndex.clear();
      nodeList.clear();
      completeItemList = fullItemList;
      fullItemList = null;
      s = "";
      labels.clear();
      pbIndex = 0;
      sbIndex = 0;
      irpStruct = new IRPstruct();
      e.irps.clear();
      analyzeExecutor( hex );
      e.description = s;
      if ( !e.hasSBcode )
      {
        execNoSBCodeCount++;
        if ( !e.hasPBcode )
        {
          execNoCodeCount++;
        }
        else if ( dataChangeOnly )
        {
          execNoSBDataChPBCount++;
          execNoSBDataTimeChPBCount++;
        }
        else if ( dataTimingChangeOnly )
        {
          execNoSBDataTimeChPBCount++;
        }
      }
    }
    
    List< IRPIndexItem > irpIndex = new ArrayList< IRPIndexItem >();
    for ( Executor e : execs )
    {
      for ( int i = 0; i < e.irps.size(); i++ )
      {
        IRPIndexItem iii = new IRPIndexItem();
        iii.irp = e.irps.get( i );
        iii.location[ 0 ] = e.index;
        iii.location[ 1 ] = i;
        if ( iii.irp.generalSpec != null && iii.irp.bitSpec != null && iii.irp.irStream != null )
        {
          irpIndex.add( iii );
        }
      }
    }
    
    Collections.sort( irpIndex, new Comparator< IRPIndexItem >()
    {
      @Override
      public int compare( IRPIndexItem o1, IRPIndexItem o2 )
      {
        IRPstruct irp1 = o1.irp;
        IRPstruct irp2 = o2.irp;
        int result = irp1.bitSpec.compareTo( irp2.bitSpec );
        if ( result == 0 )
        {
          result = irp1.unit - irp2.unit;
        }
        if ( result == 0 )
        {
          result = irp1.irStream.compareTo( irp2.irStream );
        }
        return result;
      }
    });
    
    int fullCount = 0;
    int shownCount = 0;
    try
    {
      FileWriter fw = new FileWriter( "MAXQirps.txt" );
      pw = new PrintWriter( fw );
      for ( IRPIndexItem iii : irpIndex )
      {
        fullCount++;
        Executor ex = execs.get( iii.location[ 0 ] );
        if ( ex.hasSBcode || !ex.pbHandled )
        {
          continue;
        }
        shownCount++;
        s = iii.irp.generalSpec + iii.irp.bitSpec + iii.irp.irStream + " : " + iii.location[ 1 ];
        s += "\n   ";
        s += ex.names.get( 0 );
        s += "\n\n";
        pw.print( s.replaceAll( "\\n", System.getProperty("line.separator" ) ) );
      }
      s = "IRP values shown: " + shownCount + " of " + fullCount + "\n";
      pw.print( s.replaceAll( "\\n", System.getProperty("line.separator" ) ) );
      pw.close();
      fw.close();
    }
    catch ( IOException e )
    {
      e.printStackTrace();
    }

    try
    {
      FileWriter fw = new FileWriter( "MAXQprotocols.txt" );
      pw = new PrintWriter( fw );
      for ( Executor e : execs )
      {
        s = "";
        for ( String eName : e.names )
        {
          s += eName + "\n";
        }
        s += e.description;
        s += "--------------------\n\n";
        pw.print( s.replaceAll( "\\n", System.getProperty("line.separator" ) ) );
      }

      s = "Count of executors: " + execCount + "\n";
      s += "Count of executors without code: " + execNoCodeCount + "\n";
      s += "Count of executors without SB code: " + execNoSBCodeCount + "\n";
      s += "Count of executors without SB code and only data changing PB code: " + execNoSBDataChPBCount + "\n";
      s += "Count of executors without SB code and only data and time changing PB code: " + execNoSBDataTimeChPBCount + "\n";
      s += "Unlabelled addresses:";
      Collections.sort( AssemblerItem.unlabelled );
      for ( int i : AssemblerItem.unlabelled )
      {
        s += String.format( " $%02X", i );
      }
      pw.print( s.replaceAll( "\\n", System.getProperty("line.separator" ) ) );
      pw.close();
      fw.close();
    }
    catch ( IOException e )
    {
      e.printStackTrace();
    }
  }
  
  /**
   *   ONLY EVER CALLED WITH altTimings == null
   *   
   *   itemList is cleared and gets list of all AssemblerItems in this pseudocode block.
   *   labelAddresses gets augmented by addresses of any destinations from relative addressing
   *     not already present, entire list then sorted to numerical order.
   *   labels maps all values in labelAddresses to a label Ln, where n runs from 1 upwards.
   *   entries in itemList at all addresses in labelAddresses get the Ln label in their label field.
   *   
   *   comments added to entries in itemList that change frequency or timings, with items being processed
   *     in blocks between label entries so that any frequency change is used in timing calculations
   *     even if frequency change occurs after the timing change.
   *   returns assembler code listing.
   */
  private String disassemblePseudocode( int addr, Hex hex, String prefix, boolean[] flags, LinkedList< TimingStruct > altTimings )
  {
    Arrays.fill( flags, false );
    proc.setRelativeToOpStart( true );
    proc.setOneByteAbsoluteAddresses( true );
    String codeStr = "";
    if ( hex == null )
    {
      codeStr += "*** Code block invalid\n";
      return codeStr;
    }
    Hex pHex = new Hex( hex, 0, hex.length() + 4 );
    pHex.set( ( short )0x5F, pHex.length() - 4 );
    short[] data = pHex.getData();
    int index = 0;
    itemList.clear();
    DisasmState state = new DisasmState();
    while ( index < pHex.length() )
    {
      AssemblerOpCode oc  = proc.getOpCode( pHex.subHex( index ) );
      AddressMode mode = oc.getMode();

      for ( int i = 0; ( mode.relMap >> i ) != 0; i++ )
      {
        if ( ( ( mode.relMap >> i ) & 1 ) == 1 )
        {
          int n = index + oc.getLength() + i;
          if ( n < data.length )
          {
            int newAddr = addr + index + data[ n ] - ( data[ n ] > 0x7F ? 0x100 : 0 );
            if ( !labelAddresses.contains( newAddr ) )
            {
              labelAddresses.add( newAddr );
            }
          }
        }
      }

      index += oc.getLength() + mode.length;
    }
    Collections.sort( labelAddresses );
    for ( int i = 0; i < labelAddresses.size(); i++ )
    {
      labels.put( labelAddresses.get( i ), "L" + i );
    }
    
    // Disassemble
    index = 0;
    Arrays.fill( flags, false );
    while ( index < pHex.length() )
    {
      AssemblerItem item = new AssemblerItem( addr + index, pHex.subHex( index ) );
      int opLength = item.disassemble( proc, labels, state );
      itemList.add( item );
      index += opLength;
    }
    for ( int i = 0; i < itemList.size(); )
    {    
      int j = i;
      do { j++; } while ( j < itemList.size() && itemList.get( j ).getLabel().isEmpty() );
      int[] carriers = new int[]{ carrier, has2usOff ? 12 : carrier, altCarrier };
      boolean[] freqFlags = new boolean[]{ false, false };
      TimingStruct ts = new TimingStruct( carriers, tbDurations, pf );
      pfNew = pf.clone();
      
      for ( int k = i; k < j; k++ )
      {
        freqFlags[ 0 ] = false;
        
        AssemblerItem item = itemList.get( k );
        addItemComments( item, carriers, freqFlags, ts );
        changesFreq = changesFreq | freqFlags[ 1 ];
      }
      if ( altTimings != null && ts.changed )
      {
        altTimings.add( ts );
      }
      // carriers will now be set for current item group, so repeat for corrected comment
      freqFlags[ 0 ] = changesFreq;
      for ( int k = i; k < j; k++ )
      {
        AssemblerItem item = itemList.get( k );
        int itemType = addItemComments( item, carriers, freqFlags, null );
        item.setType( itemType );
        if ( itemType >= 0 )
        {
          flags[ itemType ] = true;
        }
        String str = prefix + item.getLabel() + "\t" + item.getOpCode().getName() + "\t" + item.getArgumentText();
        String comments = item.getComments();
        if ( comments != null && !comments.isEmpty() )
        {
          str += "\t" + comments;
        }
        codeStr += str + "\n";
      }
      i = j;
    }
    return codeStr;
  }
  
  /**
   *   On first call, fullItemList != null, completeItemList == null.
   *   Value of s from this call is discarded.
   *   On second call, fullItemList == null, completeItemList != null.
   */
  private void analyzeExecutor( Hex hex )
  {
    short[] data = hex.getData();
    data = hex.getData();
    int pos = 0;
    pos += 3; // skip header
    
    int tbHeader = data[ pos++ ];
    boolean tbHasSpec = ( tbHeader & 0x80 ) != 0;
    int tbSize = tbHeader & 0x3F;
    pos += ( tbHasSpec ? 2 : 0 ) + tbSize;
    
    int dbHeader = data[ pos++ ];
    int dbSize = dbHeader & 0x07;
    boolean hasAltExec = ( dbHeader & 0x20 ) != 0;
    int dirOffset = dbSize > 2 ? data[ pos + 2 ] : 0;
    pos += dbSize;
    int altExecStart = hasAltExec && dirOffset > 0 ? pos + dirOffset : 0;
    if ( altExecStart > 0 )
    {
      // THIS WON'T WORK DUE TO DUAL CALLING.
      s += "First executor\n";
      analyzeSingleExec( hex.subHex( 0, altExecStart ) );
      s += "\nSecond executor\n";
      analyzeSingleExec( hex.subHex( altExecStart )  );
    }
    else
    {
      analyzeSingleExec( hex );
    }   
  }
  
  /**
   *   On first call, fullItemList != null, completeItemList == null.
   *   Value of s from this call is discarded.
   *   On second call, fullItemList == null, completeItemList != null.
   */
  private void analyzeSingleExec( Hex hex )
  {
    short[] data = hex.getData();
    analyzeHeader( hex.subHex( 0,  3 ) );
    int pos = 0;
    pos += 3; // skip header
    
    int tbHeader = data[ pos ];
    boolean tbHasSpec = ( tbHeader & 0x80 ) != 0;
    int tbSize = ( tbHeader & 0x3F ) + ( tbHasSpec ? 3 : 1 );
    int tbStart = pos;
    pos += tbSize;
    for ( String str : analyzeTimingBlock( hex.subHex( tbStart, tbSize ), true, 0, 0x40, null ) )
    {
      s += str + "\n";
    }
    int carrierSaved = carrier;
    int[] durationsSaved = tbDurations;
    int dbHeader = data[ pos++ ];
    int dbSize = dbHeader & 0x07;
    hasNativeCode = ( dbHeader & 0x80 ) != 0;
    has2usOff = ( dbHeader & 0x40 ) != 0;
    boolean hasAltExec = ( dbHeader & 0x20 ) != 0;
    int dbSwitch = !hasAltExec && dbSize > 1 ? hex.get( pos ) : 0;
    int altMask = dbSwitch & 0xFF;
    int altIndex = ( dbSwitch >> 12 ) & 0x0F;
    int altCount = ( dbSwitch >> 8 ) & 0x0F;
    int shift = 0;
    int maxCount = 1;
    for ( int i = 0; i < 8; i++ )
    {
      if ( ( ( altMask >> i ) & 0x01 ) != 0 )
      {
        maxCount *= 2;
      }
    }
    if ( altCount >= maxCount )
    {
      s += "\n*** Number of protocol alternates greater than mask permits, so reset from ";
      s += String.format( "%d to %d", altCount, maxCount - 1 ) + " ***\n";
      altCount = maxCount - 1;
    }
    
    if ( altMask > 0 )
    {
      for ( ; ( ( altMask >> shift ) & 0x01 ) == 0; shift++ ){};
    }
    
    String byteName = altCount > 0 ? getZeroLabel( 0xD0 + altIndex ) : "";
    pos += dbSize;  // pos points to first (or only) protocol block
    String irpInit = irp;
    for ( int i = 0; i < maxCount; i++ )
    {
      irp = irpInit;
      LinkedList< TimingStruct > altTimings = new LinkedList< TimingStruct >();
      int pbOptionsSize = data[ pos ] & 0x0F;
      int pbOffset = pbOptionsSize > 0 ? data[ pos + 1 ] : 0;
      if ( altCount > 0 )
      {
        s += "\nProtocol block when (" + byteName + " & #$" + Integer.toHexString( altMask )
            + ")=$" + Integer.toHexString( i << shift ) + "\n";
      }
      int pbIndexSaved = pbIndex;
      analyzeProtocolBlock( pos, pbOffset > 0 ? hex.subHex( pos, pbOffset ) : hex.subHex( pos ), altTimings, null );
      irp = "";
      while ( !altTimings.isEmpty() )
      {
        TimingStruct ts = altTimings.pop();
        String sSaved = s;
        irpStruct = new IRPstruct();
        String irpSaved = irp;
        s = "";
        pbIndex = pbIndexSaved;     
        boolean changed = analyzeProtocolBlock( pos, pbOffset > 0 ? hex.subHex( pos, pbOffset ) : hex.subHex( pos ), altTimings, ts );
        s = sSaved;
        if ( changed )
        {
          continue;
        }
        carrier = carrierSaved;
        tbDurations = durationsSaved;
        if ( !e.irps.contains( irpStruct ) )
        {
          e.irps.add( irpStruct );
        }
        irp = (irpSaved.isEmpty() ? "" : irpSaved + "\n" ) + irp;
      }
      s += "\n" +irp + "\n";
      if ( pbOffset > 0 )
      {
        s += "- - - - - - - -\n";
        pos += pbOffset;
        if ( i > altCount - 1 )
        {
          s += "\n*** More than specified number " + ( altCount + 1 ) + "  of protocol blocks ***\n";
        }
      }
      else
      {
        if ( i < altCount )
        {
          s += "\n*** Fewer than specified number " + ( altCount + 1 ) + " of protocol blocks ***\n";
        }
        break;
      }
    }
  }

  private void analyzeHeader( Hex hex )
  {
    short[] data = hex.getData();
    irp = "{";
    int on = data[ 0 ];
    int off = data[ 1 ];
    if ( on == 0 && off == 0 )
    {
      on = 6;
      off = 6;
      carrier = on + off;
      s += "Carrier is unmodulated\n";
      irp += "0k,";
    }
    else
    {
      on++;
      off++;
      carrier = on + off;
      s += String.format( "%.2fkHz, duty cycle %.1f", 6000.0 / carrier, ( 100.0 * on )/ carrier ) + "%\n";
      irp += String.format( "%.1fk,", 6000.0 / carrier );
    }
    int args = data[ 2 ];
    fix = ( args >> 4 ) & 0x0F;
    var = args & 0x0F;
    s += "" + fix + " fixed, " + var + " variable bytes\n";
    List< String[] > allZeroLabels = new ArrayList< String[] >();
    int start = 0xD0;
    if ( fix > 0 )
    {
      allZeroLabels.add( new String[]{ "Fix0", Integer.toHexString( start ), "Fix", Integer.toHexString( fix ) } );
    }
    start += fix;
    if ( var > 0 )
    {
      allZeroLabels.add( new String[]{ "Var0", Integer.toHexString( start ), "Var", Integer.toHexString( var ) } );
    }
    start += var;
    if ( start < 0xE0 )
    {
      allZeroLabels.add( new String[]{ "Calc0", Integer.toHexString( start ), "Calc", Integer.toHexString( 16 - fix - var ) } );
    }
    allZeroLabels.addAll( Arrays.asList( zeroLabels ) );
    proc.setZeroLabels( allZeroLabels.toArray( new String[ 0 ][ 0 ]) ); 
  }
  
  /**
   *   If heads==true and ts==null, tbLengths and tbDurations will be set from timing block
   *     but if heads==true and ts!=null, tbDurations is set from ts.durations.
   *   Value of carrier is used to convert durations in clock cycles to values in microseconds.
   *   If heads==true then IRP unit is calculated and used to complete GeneralSpec and add BitSpec
   *     based on initialCodeSpec for format.
   *   Relevant irpParts entries set and descriptive text returned for timing items whose
   *     first duration lies between start and end parameters.
   */
  private List< String > analyzeTimingBlock( Hex hex, boolean heads, int start, int end, TimingStruct ts )
  {
    String str = "";
    List< String > list = new ArrayList< String >();
    if ( heads )
    {
      if ( ts == null )
      {
        short[] data = hex.getData();
        int tbHeader = data[ 0 ];
        int spec = ( tbHeader & 0x80 ) != 0 ? hex.get( 1 ) : 0;
        tbLengths = new short[]{ 1,1,1,1,1,1,1,1 };
        for ( int i = 0; i < 8; i++ )
        {
          tbLengths[ i ] += spec & 0x03;
          spec >>= 2;
        }
        int tbSize = ( tbHeader & 0x3F )/2;
        int pos = ( tbHeader & 0x80 ) != 0 ? 3 : 1;
        tbDurations = new int[ tbSize ];
        for ( int i = 0; i < tbSize; i++ )
        {
          tbDurations[ i ] = proc.getInt( data, pos );
          pos += 2;
        }
      }
      if ( ts != null )
      {
        tbDurations = ts.durations;
      }
      int tbSize = tbDurations.length;
      str = "Raw timing data PD00-PD" + String.format( "%02X: ", tbSize-1 );
      for ( int i = 0; i < tbSize; i++ )
      {
        str += ( i > 0 ? ", " : "" ) + tbDurations[ i ];
      }
      list.add( str );
      if ( has2usOff )
      {
        list.add( "Raw OFF times are in units of 2us" );
      }
      int min = 0x10000;
      int tbMax = Math.min( 2 * ( tbLengths[ 0 ] + tbLengths[ 1 ] ) , tbDurations.length );
      for ( int i = 0; i < tbMax; i++ )
      {
        int mult = ( i & 1 ) == 0 | !has2usOff ? carrier : 12;
        int val = ( tbDurations[ i ] * mult + 3 )/6;
        if ( val > 0 )
        {
          min = Math.min( min, val );
        }
      }
      double best = 10.0;
      double bestF = 0;
      double worst = 0;
      for ( double f = 0.9; f <= 1.1; f += 0.01 )
      {
        double max = 0;
        double err = 0;
        for ( int i = 0; i < tbMax; i++ )
        {
          int mult = ( i & 1 ) == 0 | !has2usOff ? carrier : 12;
          if ( tbDurations[ i ] > 0 )
          {
            double val = ( tbDurations[ i ] * mult )/6.0;
            double x = val/( min * f );
            double var = Math.abs( x - Math.round( x ) );
            max = Math.max( max, var );
            err = Math.max( err, var / x );
          }
        }
        if ( max < best )
        {
          best = max;
          bestF = f;
          worst = err;
        }
      }
      unit = Math.round( min * bestF );
      if ( worst > 0.05 )
      {
        s += "*** Max error " + String.format( "%.2f", 100*worst ) +"%\n";
      }
      irp += (int)unit + "}";
      irpStruct.unit = ( int )unit;
      irpStruct.generalSpec = irp;
    }
    
    List< Integer > irpVals = heads || ( start == 0 && end == 0x40 ) ? new ArrayList< Integer >() : null;
    int[] limits = new int[]{ 0, 0 };
    int codeSpec = initialCodeSpec == -1 ? 0 : initialCodeSpec;
    int codeSelector = codeSpec & 0x0F;
    codeSelector = codeSelector > 5 ? 0 : codeSelector;
    if ( ( codeSpec & 0x10 ) != 0 )
    {
      // Set altCarrier
      getTimingItem( 7, limits, null );
    }
    else
    {
      altCarrier = 0;
    }
    
    int irpLen = irp.length();
    if ( codeSelector == 1 )
    {
      int size = ( tbLengths[ 0 ] + tbLengths[ 1 ] ) > 7 ? 2 : 1;
      int mult = has2usOff ? 12 : carrier;
      int n = 0;

      if ( heads )
      { 
        irp += "<";
      }
      
      for ( int i = 0; i < 4; i++ )
      {
        str = "";
        for ( int j = 0; j < size; j++ )
        {
          int val = ( tbDurations[ n++ ]*carrier + 3 )/6;  // convert carrier cycles to us
          if ( val > 0 )
          {
            str += str.isEmpty() ? "" : ",";
            str += "+" + val;
            if ( heads )
            {
              irp += getIRPduration( val ) + ",";
            }
          }
          val = ( tbDurations[ n++ ]*mult + 3 )/6;  // convert carrier cycles to us
          if ( val > 0 )
          {
            str += str.isEmpty() ? "" : ",";
            str += "-" + val;
            if ( heads )
            {
              irp += - getIRPduration( val ) + ",";
            }
          }
        }
        if ( heads )
        {
          irp = irp.substring( 0, irp.length() - 1 ) + "|";
        }
        String range = String.format( "(PD%02X-PD%02X) ", 2*i*size, 2*(i+1)*size - 1 );
        if ( start <= 2*i*size && 2*i*size <= end )
        {
          list.add( "Bursts for bit-pair " + ( new String[]{ "00", "01", "10", "11" } )[ i ] + " (us): " + range + str );
        }
      }
      if ( heads )
      {
        irp = irp.substring( 0, irp.length() - 1 ) + ">";
        irpStruct.bitSpec = irp.substring( irpLen );
      }
    }
    else if ( codeSelector == 5 && ( tbLengths[ 0 ] + tbLengths[ 1 ] ) == 2 
        && tbDurations[ 2 ] == 0 )
    {
      int mult = has2usOff ? 12 : carrier;
      int on = ( tbDurations[ 0 ]*carrier + 3 )/6;
      int off = ( tbDurations[ 1 ]*mult + 3 )/6;
      int incr = ( tbDurations[ 3 ]*mult + 3 )/6;
      if ( 0 == start )
      {
        list.add( "Data uses base 16 encoding: burst for 4-bit group\nwith value n is (us): (PD0-PD3) "
            + "+" + on + ", -(" + off + " + n*" + incr + ") )" );
      }
    }
    else
    {
      if ( heads && codeSelector == 5 )
      {
        list.add( "Data uses base 16 encoding, 4-bit group with value n being converted\n"
            + "  for transmission to a 1 followed by n 0's " );
      }
      
      str = getTimingItem( 0, limits, irpVals );
      String oneStr = "";
      if ( !str.isEmpty() && start <= limits[ 0 ] && limits[ 0 ] <= end )
      {
        list.add( "1-bursts (us): " + str );
        s += irpErr;
        if ( irpVals != null )
        {
          for ( int n : irpVals )
          {
            oneStr += n + ",";
          }
          irpParts[ 2 ] = irpVals.get( 0 ) + ",";
        }
      }
      str = getTimingItem( 1, limits, irpVals );
      if ( !str.isEmpty() && start <= limits[ 0 ] && limits[ 0 ] <= end )
      {
        list.add( "0-bursts (us): " + str );
        s += irpErr;
        if ( heads && ( codeSpec & 0x20 ) != 0 )
        {
          list.add( "Only first burst-pair of 0-burst is sent if an odd number of bits precede it" );
        }
        if ( heads && irpVals != null )
        {
          irp += "<";
          for ( int n : irpVals )
          {
            irp += n + ",";
          }
          irp = irp.substring( 0, irp.length() -1 ) + "|" + oneStr.substring( 0, oneStr.length() -1 ) + ">";
          irpStruct.bitSpec = irp.substring( irpLen );
        }
      }
    }

    str = getTimingItem( 2, limits, irpVals );
    if ( !str.isEmpty() && ( tbUsed & 0x04 ) != 0 && start <= limits[ 0 ] && limits[ 0 ] <= end )
    {
      list.add( "Lead-out (us): " + str );
      s += irpErr;
      if ( irpVals != null )
      {
        irpParts[ 1 ] = irpVals.get( 0 ) + ",";
      }
    }
    str = getTimingItem( 3, limits, irpVals );
    if ( !str.isEmpty() && ( tbUsed & 0x08 ) != 0 && start <= limits[ 0 ] && limits[ 0 ] <= end )
    {
      list.add( "Lead-in (us): " + str );
      s += irpErr;
      if ( irpVals != null )
      {
        irpParts[ 0 ] = "";
        if ( irpVals.size() > 1 )
        {
          for ( int n : irpVals )
          {
            irpParts[ 0 ] += n + ",";
          }
          irpParts[ 9 ] = irpVals.get( 0 ) + "," + ( irpVals.get( 1 ) / 2 ) + ",";
        }
      }
    }
    str = getTimingItem( 4, limits, irpVals );
    if ( !str.isEmpty() && ( tbUsed & 0x10 ) != 0 && start <= limits[ 0 ] && limits[ 0 ] <= end )
    {
      list.add( "Alternate lead-out (us): " + str );
      s += irpErr;
      if ( irpVals != null )
      {
        irpParts[ 6 ] = irpVals.get( 0 ) + ",";
      }
    }
    str = getTimingItem( 5, limits, irpVals );
    if ( !str.isEmpty() && ( tbUsed & 0x20 ) != 0 && start <= limits[ 0 ] && limits[ 0 ] <= end )
    {
      list.add( "Alternate lead-in (us): " + str );
      s += irpErr;
      if ( irpVals != null )
      {
        irpParts[ 5 ] = "";
        for ( int n : irpVals )
        {
          irpParts[ 5 ] += n + ",";
        }
      }
    }
    str = getTimingItem( 6, limits, irpVals );
    if ( !str.isEmpty() && ( tbUsed & 0x40 ) != 0 && start <= limits[ 0 ] && limits[ 0 ] <= end )
    {
      list.add( "Mid-frame burst (us): " + str );
      s += irpErr;
      if ( irpVals != null )
      {
        irpParts[ 3 ] = "";
        for ( int n : irpVals )
        {
          irpParts[ 3 ] += n + ",";
        }
      }
    }
    if ( ( codeSpec & 0x10 ) != 0 )
    {
      str = getTimingItem( 7, limits, null );
      if ( !str.isEmpty() && start <= limits[ 0 ] && limits[ 0 ] <= end )
      {
        list.add( "0-burst carrier times (units of 1/6us): " + str );
      }
    }
    return list;
  }
  
  /**
   * Convert duration in microseconds (us) to integer multiple of IRP unit
   */
  private int getIRPduration( int usDuration )
  {
    double ratio = usDuration / unit;
    double delta = Math.abs( ratio - Math.round( ratio ) );
    if ( delta > 0.05 * ratio )
    {
      irpErr = String.format( "\n*** Diff %.2f", 100*delta/ratio ) + "%" + String.format(" in converting %d with unit %d\n", usDuration, (int)unit );
    }
    else
    {
      irpErr = "";
    }
    return ( int )Math.round( ratio );
  }
  
  private String getTimingItem( int n, int[] limits, List< Integer > irpVals )
  {
    int itemCarrier = carrier;
    if ( n == 1 && altCarrier != 0 )
    {
      itemCarrier = altCarrier;
    }
    int[] durations = tbDurations;
    return getTimingItem( n, itemCarrier, durations, limits, irpVals );
  }
  
  private String getTimingItem( int n, int itemCarrier, int[] durations, int[] limits, List< Integer > irpVals )
  {
    if ( irpVals != null )
    {
      irpVals.clear();
    }
    irpErr = "";
    int pos = 0;
    int[] sizes = new int[]{ 2, 2, 1, 2, 1, 2, 2, 1 };
    int mult = has2usOff ? 12 : itemCarrier;
    String str = "";
    for ( int i = 0; i < n; i++ )
    {
      pos += tbLengths[ i ] * sizes[ i ];
    }
    if ( ( pos + tbLengths[ n ] * sizes[ n ] ) > durations.length )
    {
      return str;
    }

    limits[ 0 ] = pos;
    limits[ 1 ] = pos + tbLengths[ n ]*sizes[ n ] - 1;
    String range = String.format( "(PD%02X", pos );
    if ( limits[ 1 ] > limits[ 0 ] )
    {
      range += String.format( "-PD%02X", limits[ 1 ] );
    }
    range += ") ";
    for ( int i = 0; i < tbLengths[ n ]; i++ )
    {
      if ( sizes[ n ] == 2 )
      {
        int val = ( durations[ pos++ ]*itemCarrier + 3 )/6;  // convert carrier cycles to us
        if ( val > 0 )
        {
          str += str.isEmpty() ? "" : ",";
          str += "+" + val;
          if ( irpVals != null )
          {
            irpVals.add( getIRPduration( val ) );
          }
        }
        val = ( durations[ pos++ ]*mult + 3 )/6;  // convert carrier cycles to us
        if ( val > 0 )
        {
          str += str.isEmpty() ? "" : ",";
          str += "-" + val;
          if ( irpVals != null )
          {
            irpVals.add( - getIRPduration( val ) );
          }
        }
      }
      else
      {
        int val = 0;
        for ( ; i < tbLengths[ n ]; i++ )
        {
          val = ( val << 16 ) + durations[ pos++ ];
        }
        if ( n != 7 )
        {
          val = ( val * mult + 3 )/6;
          if ( val > 0 )
          {
            str += "-" + val;
            if ( irpVals != null )
            {
              irpVals.add( - getIRPduration( val ) );
            }
          }
        }
        else if ( val > 0 )
        {
          altCarrier = ( val & 0xFF ) + ( ( val >> 8 ) & 0xFF ) + 2;
          str += "+" + ( val & 0xFF ) + ",-" + ( ( val >> 8 ) & 0xFF );
        }
      }
    }
    return !str.isEmpty() ? range + str : str;
  }
  
  private void reverseByte( int n, int[] togData )
  {
    togData[ 3 ] = -1;
    for ( int i = 0; i < 8; i++ )
    {
      int b = n & 1;
      togData[ 0 ] <<= 1;
      togData[ 1 ] += b;    // count of 1-bits
      togData[ 0 ] |= b;    // bit-reversed n
      if ( b == 1 )
      {
        togData[ 2 ] = 7 - i;   // index of least sig 1-bit in reversed n
        if ( togData[ 3 ] < 0 )
        {
          togData[ 3 ] = 7 - i; // index of most sig 1-bit in reversed n
        }
      }
      n >>= 1;
    }
  }
  
  private String analyzeToggle( int toggle, int[] togData )
  {
    String togStr = "";
    int type = ( toggle >> 12 ) & 0x0F;
    int index = ( toggle >> 8 ) & 0x0F;
    int mask = toggle & 0xFF;
    String label = getZeroLabel( 0xD0 + index );
    if ( ( type & 0x04 ) != 0 )
    {
      togStr += "When toggle counter T is odd, XOR " + label + " with mask";
      if ( mask > 0 )
      {
        togStr += String.format( " #$%02X\n", mask );
      }
    }
    else
    {
      togStr += "Replace the bits of " + label + " selected by mask ";
      if ( mask > 0 )
      {
        togStr += String.format( " #$%02X\n", mask );
      }
      togStr += "with " + ( ( type & 0x08 ) != 0 ? "the complement of " : "" );
      togStr += "the least significant bits of toggle counter T";
    }
    
    if ( togData != null )
    {
      reverseByte( mask, togData );
      togData[ 4 ] = index;
      togData[ 5 ] = type & 0x0C;
    }
    return togStr;
  }
  
  private Node makeBranch( Node node, List< Integer > validTypes, int limit )
  {
    for ( int i = node.start; i <= limit; i++ )
    {
      AssemblerItem item = completeItemList.get( i );
      int type = item.getType();
      if ( !validTypes.contains( type ) )
      {
        s += "Irregular instruction  at no. " + i + "/" + limit;
        e.pbHandled = false;
        return null;
      }
      if ( type == 2 || type == 7 || type == 8 )
      {
        // type 2 = branch on test of data, type 7 = branch on test of first frame
        // type 8 = branch on true/false test
        int[] branch = new int[ 3 ];
        branch[ 0 ] = i;
        String args = item.getArgumentText();
        int lStart = args.indexOf( 'L' );
        int lEnd = args.indexOf( ',', lStart );
        String destLabel = null;
        int destIndex = 0;
        if ( lEnd > lStart )
        {
          // conditional branch instruction
          // make branch[ 1 ] hold destination on zero, branch[ 2 ] on nonzero
          //   or branch[ 1 ] hold destination on true, branch[ 2 ] on false
          // set default values for BRA Z, ...  or BRA T, ...
          int yes = 1;  // index to store destination on branch (continue at specified instruction )
          int no = 2;   // index to store destination on no branch (continue at next instruction)          
          if ( args.startsWith( "NZ" ) || args.startsWith( "F" ) )
          {
            yes = 2;
            no = 1;
          }

          destLabel = args.substring( lStart, lEnd );
          destIndex = labelIndex.get( destLabel );
          branch[ no ] = i + 1;
          branch[ yes ] = destIndex;

          if ( type == 8 )
          {
            // For true/false branch, only follow True
            branch[ 2 ] = -1;
          }

          node.branch = branch;
          return node;
        }
        else
        {
          // unconditional branch instruction
          destLabel = args.substring( lStart );
          destIndex = labelIndex.get( destLabel );
          branch[ 1 ] = destIndex;
          branch[ 2 ] = -1;
          node.branch = branch;
          return node;
        }
      }
      else if ( i == limit )
      {
        int[] branch = new int[ 3 ];
        branch[ 0 ] = limit+1;
        branch[ 1 ] = -1;
        branch[ 2 ] = -1;
        node.branch = branch;
        return node;
      }
    }
    return null;
  }
  
/**
 * On first call for each protocol block, fullItemList != null, completeItemList == null,
 * ts == null, altTiming is empty.  It sets initialCodeSpec from PB options.  fullItemList
 * gets AssemblerItems for PB and each SB block with code, separated by an item consisting
 * only of label of form PBn or SBn.  At this stage the label entries in the items may be
 * wrong as labelAddresses is added to for each code block, so cross-referencing labels will
 * be missing.  IT LOOKS AS IF ALTTIMINGS CAN BE NULL ON THIS INITIAL CALL, AS IT IS NOT
 * REFERENCED.
 * 
 * On second call, completeItemList is fullItemList from first call and fullItemList == null.
 * Still ts==null and altTimings is empty.  The label entries in the items in completeItemList
 * are updated to final form and labelIndex is constructed to map labels to position in
 * completeItemList.  Paths through PB code block are determined and for each path, a
 * TimingStruct is added to altTimings with pbPath set to path value.  Then for each SB
 * code block, its paths are determined and each altTimings entry is duplicated for each
 * SB path with an sbPaths entry added.  If there was no PB code then it will have pbPath=0.
 * If no SB code then sbPaths will be empty.
 * 
 * On third call, completeItemList is now correct, fullItemList==null and it is called with
 * ts being in turn the next entry in altTimings.
 */
  private boolean analyzeProtocolBlock( int addr, Hex hex, LinkedList< TimingStruct > altTimings, TimingStruct ts )
  {
    boolean changed = false;
    int pos = 0;
    short[] data = hex.getData();
    int pbHeader = data[ pos++ ];
    int pbOptSize = pbHeader & 0x0F;
    int pbSwitchSize = pbOptSize > 2 ? data[ pos + 1 ] & 0x0F : 0;
    int codeSpec = pbOptSize > 3 ? data[ pos + 3 ] : 0;
    int toggle = pbOptSize > 5 ? hex.get( pos + 4 ) : 0;
    int codeSelector = codeSpec & 0x0F;
    int irpLen = irp.length();

    if ( initialCodeSpec == -1 )
    {
      initialCodeSpec = codeSpec;
    }
    else if ( initialCodeSpec != codeSpec )
    {
      s += "Codespec changed " + String.format( "from $%02X to $%02X", initialCodeSpec, codeSpec );
    }

    if ( codeSelector > 5 && codeSelector < 12 )
    {
      s += "Data is sent with asynchronous coding, with one start bit (1), ";
      s += ( new String[]{ "no", "even", "odd" } )[ codeSelector % 3 ] + " parity bit, ";
      s += codeSelector < 9 ? "1 stop bit (0)\n" : "2 stop bits (00)\n";
      e.pbHandled = false;
    }

    pos += pbOptSize;   // pos points to code block if present, else signal block
    boolean pbHasCode = ( pbHeader & 0x80 ) != 0;
    if ( !pbHasCode )
    {
      if ( completeItemList != null && ts == null )
      {
        altTimings.add( new TimingStruct() );
      }
    }
    else
    {
      pbIndex++;
      boolean[] flags = new boolean[ 20 ];
      e.hasPBcode = true;
      s += "\nProtocol block code (run once per Signal block, after PF bytes read):\n";
      int cbSize = data[ pos++ ];
      s += disassemblePseudocode( addr + pos, hex.subHex( pos, cbSize ), "", flags, null );
      if ( ts == null && fullItemList != null )
      {
        AssemblerItem item = new AssemblerItem();
        item.setLabel( "PB" + pbIndex );
        fullItemList.add( item );
        fullItemList.addAll( itemList );
      }
      else if ( completeItemList != null && ts == null )
      {
        for ( int i = 0; i < completeItemList.size(); i++ )
        {
          AssemblerItem ai = completeItemList.get( i );
          String label = ai.getLabel();
          if ( !label.isEmpty() && ( label.startsWith( "PB" ) || label.startsWith( "SB" ) ) )
          {
            labelIndex.put(  label, i );
          }
          else
          {
            int ad = ai.getAddress();
            label = labels.get( ad );
            if ( label != null )
            {
              ai.setLabel( label );
              labelIndex.put(  label, i );
            }
          }
        }

        int start = labelIndex.get( "PB" + pbIndex );
        List< Integer > paths = createCodePaths( start );
        if ( paths.isEmpty() )
        {
          altTimings.add( new TimingStruct() );
        }
        else
        {
          for ( int p : paths )
          {
            TimingStruct ts2 = new TimingStruct();
            ts2.pbPath = p;
            altTimings.add( ts2 );
          }
        }
        
        if ( !nodeList.isEmpty() )
        {
          s += "\nPB Nodes:\n";
        }
        for ( int n : nodeList.keySet() )
        {
          int[] b = nodeList.get( n ).branch;
          s += n + " " + b[ 0 ] + " " + b[ 1 ] + " " + b[ 2 ] + "\n";
        }
      }

      pos += cbSize;
      dataChangeOnly = true;
      dataTimingChangeOnly = true;
      for ( int i = 0; i < flags.length; i++ )
      {
        if ( flags[ i ] && i != 1 && i != 2 )
        {
          dataChangeOnly = false;
          if ( i != 3 )
          {
            dataTimingChangeOnly = false;
            e.pbHandled = false;
          }
        }
      }
      if ( dataChangeOnly )
      {
        s += "\nCode changes data only\n";
      }
      else if ( dataTimingChangeOnly )
      {
        s += "\nCode changes data and timing only\n";
      }
    }
    
    int[] togData = null;
    if ( toggle > 0 )
    {
      togData = new int[ 6 ];
      s += pbHasCode ? "After protocol block code is run, apply toggle:\n  " : "Toggle: ";
      s += analyzeToggle( toggle, togData );
      irpParts[ 15 ] = "T=T+1,";
      if ( ( togData[ 5 ] & 0x04 ) > 0 && togData[ 1 ] > 1 )
      {
        String label = getZeroLabel( 0xD0 + togData[ 4 ] );
        label = irpLabel( label );
        irpParts[ 15 ] += label + "=" + label + "^(" + togData[ 0 ] + "*T:1),";
      }
      else if ( togData[ 5 ] == 0 && togData[ 1 ] != togData[ 3 ] - togData[ 2 ] + 1 )
      {
        // replacement-type toggle with non-consecutive bits
        irpParts[ 15 ] += "unknown toggle,";
        e.pbHandled = false;
      }
    }
    
    // pos now points to signal block
    choices = new boolean[ 20 ];
    Arrays.fill( choices, false );
    blockCount = 0;
    maxBlocks = 1;
    brackets = 0;
    boolean more = true;
    sbIndex = 0;

    while ( more )
    {
      blockCount++;  
      Arrays.fill( choices, false );;
      choices[ 15 ] = toggle > 0;
      int sigPtr = pos;
      if ( pos >= data.length )
      {
        s += "*** Signal block missing ***\n";
        return false;
      }
      if ( blockCount > maxBlocks )
      {
        s += "*** Unreachable signal block\n";
      }
      Arrays.fill( pf, 0 );
      pf[ 0 ] = data[ pos++ ];
      int formatLen = pf[ 0 ] & 0x07;
      for ( int i = 1; i <= formatLen; i++ )
      {
        pf[ i ] = data[ sigPtr + i ];
      }
      boolean sbHasAlternates = ( pf[ 5 ] & 0xF0 ) > 0;
      boolean sbHasCode = ( pf[ 0 ] & 0x80 ) != 0;
      boolean sbCodeBeforeTX = ( pf[ 6 ] & 0x80 ) != 0;
      minRpts = pf[ 3 ] & 0x3F; 
      int txCount = pf[ 2 ] & 0x1F;
      
      if ( sbHasAlternates )
      {
        s += "Signal block " + blockCount + ":\n";
      }
      else if ( blockCount < 4 )
      {
        String[] order = new String[]{ "Initial", "Second", "Third" };
        s += "\n" + order[ blockCount - 1 ] + " signal block:\n";
      }
      
      s += "  Raw format data PF0-PF" + formatLen + ": ";
      for ( int i = 0; i <= formatLen; i++ )
      {
        s += String.format( "$%02X", pf[ i ] );
        s += i < formatLen ? ", " : "\n";
      }
      
      pfNew = pf.clone(); 
      int[][] pfDescs = new int[][]{ {-1,1,6}, {0x80,4,0 }, {0x08,1,3}, {0x04,1,2},
          {-1,0,6}, {0x3F,3,0}, {-1,1,4}, {0xE0,2,5}, {0x80,3,7}, {0xFF,5,0} };
      for ( int[] pfDesc : pfDescs )
      {
        if ( pfDesc[ 0 ] < 0 || ( pf[ pfDesc[ 1 ] ] & pfDesc[ 0 ]) > 0 )
        {
          s += "  " + getPFdescription( pfDesc[ 1 ] , pfDesc[ 2 ], choices ) + "\n";
        }
      }

      pos += formatLen;
      String txStr = "";
      for ( int i = 0; i <= pbSwitchSize; i++ )
      {
        Hex txBytes = hex.subHex( pos, txCount );
        txStr += "\n  Data translation";
        if ( pbSwitchSize > 0 )
        {
          txStr += " (alternative " + i + ")";
        }
        if ( txBytes.length() > 0 )
        {
          txStr += ", sets data bytes TXDn to send and number of bits TXBn from each byte (n=0 to "
              + ( txBytes.length() - 1 ) + "):\n    ";
        }
        else
        {
          txStr += ": ";
        }
        txStr += analyzeTXBytes( txBytes, togData );
        pos += txCount;
        if ( i < pbSwitchSize )
        {
          txCount = data[ pos++ ];
        }
      }
      
      if ( pbSwitchSize > 0 )
      {
        if ( pos != data.length )
        {
          txStr += "*** Excess data in protocol block ***\n";
        }
        s += txStr;
        return false;
      }

      more = sbHasAlternates;
      if ( more )
      {
        String stream = makeIRStream();
        irp += stream;
        continue;
      }
      
      String codeStr = "";
      if ( sbHasCode )
      {
        sbIndex++;
        e.hasSBcode = true;
        boolean[] flags = new boolean[ 20 ];
        codeStr += "\n  Signal block code";
        if ( txCount > 0 )
        { 
            codeStr += " (run " + ( sbCodeBeforeTX ? "before" : "after") + " data translation)";
        }
        codeStr += ":\n";
        int cbSize = data[ pos++ ];
        codeStr += disassemblePseudocode( addr + pos, hex.subHex( pos, cbSize ), "  ", flags, null );

        if (  ts == null && fullItemList != null )
        {
          AssemblerItem item = new AssemblerItem();
          item.setLabel( "SB" + sbIndex );
          fullItemList.add( item );
          fullItemList.addAll( itemList );
        }
        else if ( completeItemList != null && ts == null )
        {
          for ( int i = 0; i < completeItemList.size(); i++ )
          {
            AssemblerItem ai = completeItemList.get( i );
            String label = ai.getLabel();
            if ( !label.isEmpty() && ( label.startsWith( "PB" ) || label.startsWith( "SB" ) ) )
            {
              labelIndex.put(  label, i );
            }
            else
            {
              int ad = ai.getAddress();
              label = labels.get( ad );
              if ( label != null )
              {
                ai.setLabel( label );
                labelIndex.put(  label, i );
              }
            }
          }

          int start = labelIndex.get( "SB" + sbIndex );
          List< Integer > paths = createCodePaths( start );
          if ( !paths.isEmpty() )
          {
            altTimings.add( null );
            while ( altTimings.peek() != null )
            {
              TimingStruct ts2 = altTimings.pop();
              for ( int p : paths )
              {
                TimingStruct ts3 = ts2.clone();
                ts3.sbPaths.put( sbIndex, new int[]{p,p} );
                altTimings.add( ts3 );
              }
            }
            altTimings.pop();
          }
          changed = true;
        }
        pos += cbSize;
      }
        
      if ( sbHasCode && completeItemList != null && ts != null )
      {
        minRpts = pf[ 3 ] & 0x3F;   // set it as value from pf[ 3 ]
        int start = labelIndex.get( "SB" + sbIndex );
        if ( ts != null && ts.sbPaths.get( sbIndex ) != null)
        {
          int p = ts.sbPaths.get( sbIndex )[ 0 ];
          List< Integer > list1 = new ArrayList< Integer >();
          changed = createPathSequence( start, p, 1, list1 );
          p = ts.sbPaths.get( sbIndex )[ 1 ];
          List< Integer > list2 = new ArrayList< Integer >();
          changed = createPathSequence( start, p, 2, list2 ) || changed;
          if ( changed )
          {
            for ( int i : list1 )
            {
              for ( int j : list2 )
              {
                TimingStruct ts3 = ts.clone();
                ts3.sbPaths.put( sbIndex, new int[]{i,j} );
                altTimings.add( ts3 );
              }
            }
          }
        }
      }

      if ( completeItemList != null && !changed & ts != null )
      {
        List< Integer > paths = new ArrayList< Integer >();
        if ( ts.sbPaths.get( sbIndex ) == null )
        {
          paths.add( 0 );
        }
        else
        {
          int[] pp = ts.sbPaths.get( sbIndex );
          paths.add( pp[ 0 ] );
          if ( pp[ 0 ] != pp[ 1 ] )
          {
            paths.add( pp[ 1 ] );
          }
        }

        int[] carriers = new int[]{ carrier, has2usOff ? 12 : carrier, altCarrier };
        TimingStruct ts3 = ts != null ? ts.clone() : new TimingStruct();
        ts3.carriers = carriers;
        ts3.pf = pf.clone();
        ts3.durations = Arrays.copyOf( tbDurations, 0x40 );
        irpParts[ 17 ] = "";
        choices[ 17 ] = false; 
        if ( irpStruct.generalSpec == null )
        {
          // Condition emulates the fact that the PB code is run on first frame only       
          if ( ts3.pbPath != null )
          {
            int pbStart = labelIndex.get( "PB" + pbIndex );
            int p3 = ts3.pbPath;
            boolean[] freqFlags = new boolean[]{ false, false };
            Node n = nodeList.get( pbStart + 1 );
            while ( n != null )
            {
              int[] b = n.branch;
              for ( int i = n.start; i <= b[0]; i++ )
              {
                AssemblerItem item = completeItemList.get( i );
                addItemComments( item, carriers, freqFlags, ts3 );
              }
              n = p3 != 0 ? nodeList.get( b[p3&3] ) : null;
              p3 >>= 2;
            }
          }
          carrier = ts3.carriers[ 0 ];
          irp = String.format( "{%.1fk,", 6000.0 / carrier );
          analyzeTimingBlock( null, true, 0, 0x40, ts3 );
        }

        for ( int p : paths )
        {    
          if ( sbHasCode )
          {
            int start = labelIndex.get( "SB" + sbIndex );
            boolean[] freqFlags = new boolean[]{ false, false };
            Node n = nodeList.get( start + 1 );
            while ( n != null )
            {
              int[] b = n.branch;
              for ( int i = n.start; i <= b[0]; i++ )
              {
                AssemblerItem item = completeItemList.get( i );
                addItemComments( item, carriers, freqFlags, ts3 );
              }
              n = p != 0 ? nodeList.get( b[p&3] ) : null;
              p >>= 2;
            }
          }

          pf = ts3.pf;
          tbDurations = ts3.durations;

          for ( int[] pfDesc : pfDescs )
          {
            if ( pfDesc[ 0 ] < 0 || ( pf[ pfDesc[ 1 ] ] & pfDesc[ 0 ]) > 0 || pfDesc[ 1 ] == 2 )
            {
              getPFdescription( pfDesc[ 1 ] , pfDesc[ 2 ], choices );
            }
          }
          analyzeTimingBlock( null, false, 0, 0x40, null );
          String stream = makeIRStream();
          irp += stream;
          irpParts[ 17 ] = "";
          choices[ 17 ] = false;
          if ( choices[ 12 ] || !choices[ 16 ] )
          {
            // keep only IRstream for first frame if no repeat of current signal block 
            break;
          }
        }
      }

      s += sbCodeBeforeTX ? codeStr + txStr : txStr + codeStr;
      if ( txCount > 0 )
      {
        s += "\n  IR sent (TXBn bits of byte TXDn for each n) after ";
        s += sbHasCode && !sbCodeBeforeTX ? "signal block code run\n" : "data translation\n";
      }
      
      more = !hasNativeCode && pos < data.length;
      if ( more )
      {
        if ( pos == data.length - 1 )
        {
          s += "*** Apparent spurious extra byte at end of protocol block ***\n";
          more = false;
        }
      }
      if ( !more && blockCount < maxBlocks )
      {
        s += "*** Repeat signal block missing\n";
      }
      more &= choices[ 14 ] || choices[ 19 ];
    }
    
    if ( irp.length() > 0 )
    {
    irp = irp.substring( 0, irp.length()-1 );
    for ( int i= 0; i < brackets; i++ )
    {
      irp += ")";
    }
    if ( toggle > 0 )
    {
      int index = irp.indexOf( '(' );
      if ( index != -1 )
      {
        irp = irp.substring( 0, index ) + "(" + irpParts[ 15 ] + irp.substring( index ) + ")";
      }
    }

    if ( irp.length() > irpLen )
    {
      irpStruct.irStream = irp.substring( irpLen );
    }
    }
    
    if ( hasNativeCode )
    {
      e.pbHandled = false;
      s += "Native code block (run after IR sent):\n";
      pos += ( pos & 1 ) == 1 ? 1 : 0;  // round to word boundary
      Hex nCode = hex.subHex( pos );
      s += nCode.toString() + "\n";
      pos += nCode.length();
    }
    if ( pos != data.length )
    {
      s += "**** Parsing error ****\n";
    }
    return changed;
  }
  
  private String makeIRStream()
  {    
    if ( blockCount > 1 && brackets == 0 )
    {
      int index = irp.indexOf( '>' );
      irp = irp.substring( 0, index + 1 ) + "(" + irp.substring( index + 1 );
      brackets++;
    }
    String irp = "";
    irp += "(";
    brackets++;
    if ( choices[ 17 ] )
    {
      irp += irpParts[ 17 ];
    }
    if ( choices[ 0 ] )
    {
      irp += choices[ 5 ] ? irpParts[ 5 ] : irpParts[ 0 ];
    }
    if ( irpParts[ 11 ] != null )
    {
      irp += irpParts[ 11 ];
    }
    if ( choices[ 4 ] )
    {
      irp += choices[ 7 ] ? irpParts[ 5 ] : irpParts[ 0 ];
    }
    if ( choices[ 2 ] )
    {
      irp += irpParts[ 2 ];
    }
    if ( choices[ 1 ] )
    {
      String out = choices[ 6 ] ? irpParts[ 6 ] : irpParts[ 1 ];
      if ( out != null && choices[ 10 ] )
      {
        out = "^" + out.substring( 1 );
      }
      irp += out;
    }
    if ( choices[ 12 ] || minRpts > 0 )
    {
      // repeat on key held, and/or has mandatory repeats
      if ( choices[ 9 ] )
      {
        // has halved-style repeats, so needs new IRstream
        irp += "(" + irpParts[ 9 ];
        if ( choices[ 4 ] )
        {
          irp += choices[ 7 ] ? irpParts[ 5 ] : irpParts[ 0 ];
        }
        if ( choices[ 2 ] )
        {
          irp += irpParts[ 2 ];
        }
        if ( choices[ 1 ] )
        {
          String out = choices[ 6 ] ? irpParts[ 6 ] : irpParts[ 1 ];
          if ( out != null && choices[ 10 ] )
          {
            out = "^" + out.substring( 1 );
          }
          irp += out;
        }
        irp = irp.substring( 0, irp.length()-1 ) + ")";
        irp += minRpts > 1 ? minRpts : "";
        irp += choices[ 12 ] ? minRpts > 0 ? "+," : "*," : ",";
      }
      else
      {
        // repeat current IRstream
        irp = irp.substring( 0, irp.length()-1 ) + ")";
        // for mandatory repeats, add numeric repeat count
        irp += minRpts > 0 ? ( minRpts + 1 ) : "";
        // if mandatory repeats, add "+"
        // if repeat while key held, add "*"
        // if mandatory repeats not continuing while key held, no further marker
        irp += choices[ 12 ] ? minRpts > 0 || brackets == 1 ? "+," : "*," : ",";
        brackets--;
      }
    }
    if ( !choices[ 12 ] && choices[ 13 ] || choices[ 18 ] )
    {
      // re-execute protocol block
      if ( brackets > 1 && !choices[ 10 ] && ( pf[ 3 ] & 0x3F ) == 0 )
      {
        // Nested IRstreams with current one having gap lead-out and no repeat marker
        // so remove unnecessary opening bracket.  Note that with total time lead-out 
        // the bracket is necessary as it marks the start of the lead-out timing.
        int index = irp.lastIndexOf( '(' );
        irp = irp.substring( 0, index ) + irp.substring( index + 1 );
        brackets--;
      }
      
      // remove trailing comma and close all brackets, so enclosing complete protocol block
      irp = irp.substring( 0, irp.length()-1 );
      for ( int i= 0; i < brackets; i++ )
      {
        irp += ")";
      }
      brackets = 0;
      // mark the repetition; if choices[ 13 ] (if key held) then it is at least once,
      // else it is when key not held, when it will be exactly one repeat
      irp += choices[ 13 ] ? "+," : "2,";
    }
    return irp;
  }
  
  private List< Integer > createCodePaths( int start )
  {
    int last = 0;
    List< String > labelsUsed = new ArrayList< String >();
    String source = completeItemList.get( start ).getLabel();
    List< Integer > validTypes = null;
    if ( source.startsWith( "PB" ) )
    {
      // valid types for protocol block
      validTypes = Arrays.asList( -1,1,2,3,4 );
    }
    else
    {
      // valid types for signal block
      validTypes = Arrays.asList( -1,1,2,3,4,7,8,9,10 );
    }
    
    for ( int i = start+1; i < completeItemList.size(); i++ )
    {
      AssemblerItem item = completeItemList.get( i );
      String label = item.getLabel();
      if ( label.startsWith( "PB" ) || label.startsWith( "SB" ) )
      {
        break;
      }
      if ( !label.isEmpty() )
      {
        labelsUsed.add( label );
      }
      int type = item.getType();
      if ( source.startsWith( "PB" ) )
      {
        if ( ( type == 3 || type == 4 ) && validTypes.contains( type ) )
        {
          last = i;
        }
      }
      else
      {
        // for signal block, continue till instruction preceding END
        if ( item.getOperation().equals( "END" ) )
        {
          last = i - 1;
        }
      }
    }
    
    for ( int i = 0; i < completeItemList.size(); i++ )
    {
      if ( i > start || i <= last )
      {
        continue;
      }
      String args = completeItemList.get( i ).getArgumentText();
      for ( String label : labelsUsed )
      {
        if ( args.endsWith( label ) || args.contains( label + "," ) )
        {
          // there is a jump into timing region from outside
          e.pbHandled = false;
        }
      }
    }
    
    LinkedList< Integer > q = new LinkedList< Integer >();
    Node node = new Node( start + 1 );
    node =  makeBranch( node, validTypes, last );
    if ( node != null )
    {
      q.push( start + 1 );
      nodeList.put( start + 1, node );
    }
    while ( !q.isEmpty() )
    {
      int key = q.pop();
      node = nodeList.get( key );
      for ( int i = 1; i < 3; i++ )
      {
        int val = node.branch[ i ];
        if ( val >= 0 && val <= last && !nodeList.keySet().contains( val ) )
        {
          Node n = new Node( val );
          n = makeBranch( n, validTypes, last );
          if ( n != null )
          {
            q.push( val );
            nodeList.put( val, n );
          }
        }
      }
    }
    
    List< Integer > paths = new ArrayList< Integer >();
    q.push( 0 );
    while ( !q.isEmpty() )
    {
      int p = q.pop();
      int level = 0;
      int m = p;
      Node n = nodeList.get( start + 1 );
      while ( ( m & 3 ) > 0 )
      {
        n = nodeList.get( n.branch[ m & 3 ] );
        level++;
        m >>= 2;
      }
      if ( n != null )
      {
        boolean added = false;
        if ( completeItemList.get( n.start).getType() == 7 )
        {
          paths.add( p + ( 3 << 2*level ) );
          return paths;
        }

        if ( n.branch[ 2 ] > 0 )
        {
          q.push( p + ( 2 << 2*level ) );
          added = true;
        }
        if ( n.branch[ 1 ] > 0 )
        {
          q.push( p + ( 1 << 2*level ) );
          added = true;
        }
        if ( !added )
        {
          paths.add( p );
        }
      }
      else if ( p != 0 )
      {
        paths.add( p );
      }
    }

    if ( !paths.isEmpty() )
    {
      s += "\nPaths: ";
      for ( int p : paths )
      {
        s += Integer.toString( p, 4 ) + " ";
      }
      s += "\n";
    }
    
    return paths;
  }
  
  private boolean createPathSequence( int start, int p, int index, List< Integer > list )
  {
    int m = p;
    int next = start + 1;
    int level = 0;
    Node n = nodeList.get( next );
    while ( n != null && ( (m&3)==1 || (m&3)==2 ) )
    {
      level++;
      next = n.branch[m&3];
      n = nodeList.get( next );
      m >>= 2;
    }
    if ( n == null || m == 0 )
    {
      list.add( p );
      return false;
    }
    // (m&3)==3

    p &= (1<<(2*level))-1;      // remove the final 3
    p += index<<(2*level);      // replace it by index
    List< Integer > cp = n.branch[index] > 0 ? createCodePaths( n.branch[index]-1 ) : null;
    if ( cp == null || cp.isEmpty() )
    {
      list.add( p );
    }
    else
    {
      for ( int r : cp )
      {
        list.add( p + (r<<(2*(level+1))));
      }
    }
    return true;
  }

  private String getPFdescription( int pfn, int start, boolean[] choices )
  {
    String desc = "";
    if ( choices == null )
    {
      choices = new boolean[ 20 ];
    }
    int val = pfNew[ pfn ];
    if ( pfn == 0 )
    {
      switch ( start )
      {
        case 5:
        case 6:
          String type = ( pfNew[ 1 ] & 0x02 ) != 0 ? "alternate" : "normal";
          switch ( ( val >> 5 ) & 0x03 ) 
          {
            case 0:
            case 1:
              return "No lead-out";
            case 2:
              desc = "gap";
              break;
            case 3:
              desc = "total";
              choices[ 10 ] = true;
              break;
          }
          choices[ 1 ] = true;
          choices[ 6 ] = ( pfNew[ 1 ] & 0x02 ) != 0;
          desc = "Use " + type + " lead-out as " + desc + " time";
          tbUsed |= ( ( pfNew[ 1 ] | pfChanges[ 1 ] ) & 0x02 ) != 0 ? 0x10 : 0;
          tbUsed |= ( ( ~pfNew[ 1 ] | pfChanges[ 1 ] ) & 0x02 ) != 0 ? 0x04 : 0;
          break;
      }
    }
    else if ( pfn == 1 )
    {
      switch ( start )
      {
        case 1:
          desc = "Set lead-out type to " + ( ( val & 0x02 ) != 0 ? "alternate" : "normal" );
          if ( ( ( pfNew[ 0 ] | pfChanges[ 0 ] ) & 0x40 ) != 0 )
          {
            tbUsed |= ( val & 0x02 ) != 0 ? 0x10 : 0x04;
          }
          choices[ 6 ] = ( val & 0x02 ) != 0;
          break;
        case 2:
          desc = ( val & 0x04 ) != 0 ? "One-ON precedes lead-out" : "No One-ON before lead-out";
          choices[ 2 ] = ( val & 0x04 ) != 0;
          break;
        case 3:
          String type = ( pfNew[ 3 ] & 0x40 ) != 0 ? "alternate" : "normal";
          desc = ( val & 0x08 ) != 0 ? "Use " + type + " lead-in as end-frame burst following data" : "No end-frame burst";
          if ( ( val & 0x08 ) != 0 )
          {
            tbUsed |= ( ( pfNew[ 3 ] | pfChanges[ 3 ] ) & 0x40 ) != 0 ? 0x20 : 0;
            tbUsed |= ( ( ~pfNew[ 3 ] | pfChanges[ 3 ] ) & 0x40 ) != 0 ? 0x08 : 0;
          }
          choices[ 4 ] = true;
          choices[ 7 ] = ( pfNew[ 3 ] & 0x40 ) != 0;
          break;
        case 4:
        case 5:
          switch ( ( val >> 4 ) & 0x03 )
          {
            case 0:
              desc = "No repeat on held keypress";
              break;
            case 1:
              desc = "All buttons repeat on held keypress";
              choices[ 12 ] = true;
              break;
            case 2:
              desc = "Only Vol+/-, Ch+/-, FF, Rew repeat on held keypress";
              choices[ 12 ] = true;
              break;
            case 3:
              desc = "Send One-ON in place of first repeat, nothing on later repeats";
              break;
          }
          break;
        case 0:
        case 6:
        case 7:
          switch ( ( val >> 6 ) & 0x03 )
          {
            case 0:
              desc = "No lead-in";
              break;
            case 1:
              desc = "Use " + ( ( val & 0x01 ) != 0 ? "alternate" : "normal" ) + " lead-in on all frames";
              tbUsed |= ( val & 0x01 ) != 0 ? 0x20 : 0x08;
              choices[ 0 ] = true;
              choices[ 5 ] = ( val & 0x01 ) != 0;
              break;
            case 2:
              desc = "Use normal lead-in on first frame, no lead-in on repeat frames";
              tbUsed |= 0x08;
              choices[ 0 ] = true;
              choices[ 8 ] = true;
              break;
            case 3:
              desc = "Use normal lead-in but on repeat frames halve the OFF duration "
                  + "and omit data bits";
              tbUsed |= 0x08;
              choices[ 0 ] = true;
              choices[ 9 ] = true;
          }
          break;
      }
    }
    else if ( pfn == 2 )
    {
      if ( start >= 5 )
      {
        choices[ 13 ] = choices[ 14 ] = choices[ 16 ] = choices[ 18 ] = choices[ 19 ] = false;
        switch ( val >> 5 )
        {
          case 0:
          case 7:
            desc = "After repeats, terminate";
            break;
          case 1:
            desc = "After repeats, execute next signal block";
            maxBlocks = blockCount + 1;
            choices[ 14 ] = choices[ 19 ] = true;
            break;
          case 2:
            desc = "After repeats, if key held then execute next signal block";
            maxBlocks = blockCount + 1;
            choices[ 14 ] = true;
            break;
          case 3:
            desc = "After repeats, if key held then re-execute current protocol block";
            maxBlocks = blockCount;
            choices[ 13 ] = true;
            break;
          case 4: 
            desc = "After repeats, if key held then re-execute current protocol block, "
                + "else execute next signal block";
            maxBlocks = blockCount + 1;
            choices[ 13 ] = choices[ 19 ] = true;
            break;
          case 5:
            desc = "After repeats, re-execute current protocol block";
            maxBlocks = blockCount;
            choices[ 13 ] = choices[ 18 ] = true;
            break;
          case 6:
            desc = "After repeats, re-execute current signal block";
            choices[ 16 ] = true;
            maxBlocks = blockCount;
            break;
          default:
            maxBlocks = blockCount;
        }
      }
    }
    else if ( pfn == 3 )
    {
      switch ( start )
      {
        case 0:
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
          desc = "Send " + ( val & 0x3F ) + " mandatory repeats";
//          minRpts = val & 0x3F;
          break;
        case 6:
          desc = "End-frame burst is " + ( ( val & 0x40 ) != 0 ? "alternate" : "normal" ) + "lead-in";
          if ( ( ( pfNew[ 1 ] | pfChanges[ 1 ] ) & 0x08 ) != 0 )
          {
            tbUsed |= ( val & 0x40 ) != 0 ? 0x20 : 0x08;
          }
          choices[ 7 ] = ( val & 0x40 ) != 0;
          break;
        case 7:
          desc = ( val & 0x80 ) != 0 ? "Disable IR when repeats from held keypress end" : "";
          break;
      }
    }
    else if ( pfn == 4 )
    {
      if ( ( val & 0x80 ) != 0 )
      {
        desc = "Mid-frame burst follows first " + ( val & 0x7F ) + " data bits";
        tbUsed |= 0x40;
        choices[ 3 ] = true;
      }
      else
      {
        desc = "There is no mid-frame burst";
      } 
    }
    else if ( pfn == 5 )
    {
      desc = "PF5 value " + val;
    }
    else if ( pfn == 6 )
    {
      desc = "Signal block code sent " + ( ( val & 0x80 ) != 0 ? "before" : "after" ) + " IR transmission";
    }
    if ( maxBlocks > 3 )
    {
      maxBlocks = 3;
    }
    return desc;
  }
  
  private int addItemComments( AssemblerItem item, int[] carriers, boolean[] freqFlags, TimingStruct ts )
  {
    /* Instruction types:
     * -1= Wild card, can count as any type
     * 0 = Other
     * 1 = Data byte changes
     * 2 = Branch (unconditional or on test of data bytes)
     * 3 = Timing changes
     * 4 = Format byte changes
     * 5 = Toggle changes
     * 6 = Function call (CALL instructions)
     * 7 = Branch on test of first frame
     * 8 = Branch on true/false test
     * 9 = Branch to subroutine
     *10 = Minimum repeat change 
     */
    
    
    if ( ts == null )
    {
      ts = new TimingStruct();
    }
    freqFlags[ 1 ] = false;  // set for instruction that changes frequency
    String opName = item.getOperation();
    if ( opName.equals( "END" ) )
    {
      return -1;
    }
    List< String > opList = Arrays.asList( "MOV", "AND", "OR", "BRA", 
        "MOVN", "MOVW", "CARRIER", "TIMING", "CALL", "XOR", "BSR" );
    int opIndex = opList.indexOf( opName );
    
    String args = item.getArgumentText();
    int oldVal = 0;
    int newVal = 0;
    int xorVal = 0;
    int itemType = 0;
    List< String > argList = new ArrayList< String >();
    StringTokenizer st = new StringTokenizer( args, "," );
    while ( st.hasMoreTokens() )
    {
      String token = st.nextToken().trim();
      if ( token.startsWith( "#$" ) )
      {
        int[] rev = new int[ 4 ];
        int val = Integer.parseInt( token.substring( 2 ), 16 );
        reverseByte( val, rev );
        token = "" + rev[ 0 ];
      }
      else
      {
        token = irpLabel( token );
      }
      if ( token != null )
      {
        argList.add( token );
      }
    }
    
    for ( int i = 0; i < 3; i++ )
    {
      String pre = i == 0 ? "" : i == 1 ? "Z,.*, " : "NZ,.*, ";
      if ( Pattern.matches( pre + "(Fix|Var|Calc).*", args ) )
      {
        itemType = i == 0 ? 1 : 2;
        if ( itemType == 1 )
        {
          switch ( opIndex )
          {
            case 0:
              if ( argList.size() > 1 )
              {
                irpParts[ 17 ] += argList.get( 0 ) + "=" + argList.get( 1 ) + ",";
              }
              break;
            case 1:
              if ( argList.size() > 2 )
              {
                irpParts[ 17 ] += argList.get( 0 ) + "=" + argList.get( 1 ) + "&" + argList.get( 2 ) + ",";
              }
              break;
            case 2:
              if ( argList.size() > 2 )
              {
                irpParts[ 17 ] += argList.get( 0 ) + "=" + argList.get( 1 ) + "|" + argList.get( 2 ) + ",";
              }
              break;
            case 9:
              if ( argList.size() > 2 )
              {
                irpParts[ 17 ] += argList.get( 0 ) + "=" + argList.get( 1 ) + "^" + argList.get( 2 ) + ",";
              }
              break;
          }
          if ( !irpParts[ 17 ].isEmpty() )
          {
            choices[ 17 ] = true;
          }
        }
        break;
      }
      else if ( Pattern.matches( pre + "Tmp.*", args ) )
      {
        itemType = -1;
        break;
      }
    }
    
    if ( opIndex < 0 )
    {
      return itemType;
    }

    if ( opIndex == 3 )
    {
      if ( Pattern.matches( "L\\d*", args ) )
      {
        // Unconditional branch
        itemType = 2;
      }
      else if ( Pattern.matches( "(T|F),.*", args ) )
      {
        // Branch on true/false test
        itemType = 8;
      }
    }
    else if ( opIndex == 10 )
    {
      // Subroutine branch
      itemType = 9;
    }


    String str = "";
    str = opIndex == 3 && args.contains( "$BA, #$01" ) ? args.contains( "NZ" ) 
        ? "Branch if not first frame" : "Branch if first frame"
        : opIndex == 1 && args.equals( "$BA, $BA, #$FE" ) ? "Reset to no frames sent"
        : opIndex == 2 && args.equals( "$BB, $BB, #$01" ) ? "Suppress IR transmission"
        : opIndex == 1 && args.equals( "$BB, $BB, #$FE" ) ? "Resume IR transmission" : "";
    
    if ( !str.isEmpty() )
    {
      item.setComments( "; " + str );
      if ( str.startsWith( "Branch if" ) )
      {
        itemType = 7;
      }
      return itemType;
    }
    
    List< String > comments = new ArrayList< String >();
    
    if ( opIndex == 6 || opIndex == 7 )
    {
      itemType = 3;
      int on = Integer.parseInt( args.substring( 2, 4 ), 16 ) + 1;
      int off = Integer.parseInt( args.substring( 8, 10 ), 16 ) + 1;
      int total = on + off;
      carriers[ 0 ] = total;
      ts.carriers[ 0 ] = total;
      ts.changed = true;
      if ( opIndex == 7 )
      {
        carriers[ 1 ] = has2usOff ? 12 : total;
        ts.carriers[ 1 ] = carriers[ 1 ];
      }
      str = String.format( "Set %.2fkHz, duty cycle %.1f", 6000.0 / total, ( 100.0 * on )/ total ) + "%";
      str +=  opIndex == 6 ? " for MARK; copy to IRCA" : "";
      comments.add( str );
      freqFlags[ 1 ] = true;
    }
    
    if ( opIndex == 5 && args.startsWith( "$04, #$" ) )
    {
      itemType = 3;
      int val = Integer.parseInt( args.substring( 7, 11 ), 16 );
      int on = ( val >> 8 ) + 1;
      int off = ( val & 0xFF ) + 1;
      int total = on + off;
      carriers[ 1 ] = total;
      ts.carriers[ 1 ] = total;
      str = String.format( "Set %.2fkHz", 6000.0 / total ) + " for SPACE";
      item.setComments( "; " + str );
      return itemType;
    }
    else if ( opIndex == 4 && Pattern.matches( "PD[0-9A-F]{2}, PD[0-9A-F]{2}, #\\$..", args )
        || opIndex == 5 && Pattern.matches( "PD[0-9A-F]{2}, PD[0-9A-F]{2}", args )
        || opIndex == 7 )
    {
      itemType = 3;
      int dest = 0;
      int source = 0;
      int len = 0;
      if ( opIndex == 7 )
      {
        len = 2 * ( tbLengths[ 0 ] + tbLengths[ 1 ] + tbLengths[ 3 ] ) + tbLengths[ 2 ];
        source = len;
        comments.add( String.format( "PD%02X-PD%02X copied to PD%02X-PD%02X", source, source+len-1, dest, dest+len-1 ) );
      }
      else
      {
        dest = Integer.parseInt( args.substring( 2, 4 ), 16 );
        source = Integer.parseInt( args.substring( 8, 10 ), 16 );
        len = opIndex == 4 ? getImmValue( args )/2 : 1;
      }
      int[] durations = Arrays.copyOf( tbDurations, 0x40 );
      for ( int i = 0; i < len; i++ )
      {
        durations[ dest + i ] = durations[ source + i ];
        ts.durations[ dest + i ] = ts.durations[ source + i ];
      }
      ts.changed = true;
      int[] savedDurations = tbDurations;
      tbDurations = durations;
      int savedCarrier = carrier;
      carrier = carriers[ 0 ];
      List< String > timingComments = analyzeTimingBlock( null, false, dest, dest + len - 1, null );
      double f = 6000.0 / carrier;
      if ( timingComments.size() > 0 )
      {
        if ( !has2usOff && carriers[ 0 ] != carriers[ 1 ] )
        {
          comments.add( "Carrier frequencies for MARK and SPACE differ" );
        }

        if ( freqFlags[ 0 ] )
        {
          comments.add( String.format( "Timings for %.2fkHz", f ) );
          freqFlags[ 0 ] = false;
        }
      }
      else if ( opIndex == 5 )
      {
        int duration = ( durations[ dest ] * carrier + 3 ) / 6;
        str = String.format( "PD%02X at %.2fkHz: %dus" , dest, f, duration );
        timingComments.add( str );
      }
      comments.addAll( timingComments );
      tbDurations = savedDurations;
      carrier = savedCarrier;
    }
    else if ( opIndex == 0 && Pattern.matches( "PF\\d, #\\$..", args ) 
        || opIndex < 3 && Pattern.matches( "PF(\\d), PF\\1, #\\$..", args )
        || opIndex == 5 && Pattern.matches( "PF\\d, #\\$....", args ) )
    {
      int n = args.charAt( 2 ) - 0x30;
      if ( n < 0 || n > 6 )
      {
        return itemType;
      }
      itemType = 4;
      int immValFull = getImmValue( args );
      int end = opIndex == 5 ? 2 : 1;
      ts.changed = true;
      for ( int k = 0; k < end; k++ )
      {
        oldVal = pf[ n + k ];
        newVal = pfNew[ n + k ];
        int immVal = immValFull & 0xFF;
        newVal = opIndex == 0 || opIndex == 5 ? immVal : opIndex == 1 ? newVal & immVal : newVal | immVal;
        xorVal = newVal ^ oldVal;
        pfNew[ n + k ] = newVal;
        ts.pf[ n + k ] = newVal;
        pfChanges[ n + k ] |= xorVal;
        xorVal = opIndex == 0 || opIndex == 5 ? pfChanges[ n + k ] : opIndex == 1 ? pfChanges[ n + k ] & ~immVal : pfChanges[ n + k ] & immVal;
        if ( n == 0 && k == 1 && ( pfNew[ 0 ] & 0x06 ) != 0 )
        {
          // Don't duplicate comment on change of lead-out type
          xorVal &= 0xFD;
        }
        for ( int i = 0; i < 8; i++ )
        {
          if ( ( xorVal & 1 ) == 1 )
          {
            // bit i has changed
            str = getPFdescription( n + k, i, null );
            if ( !str.isEmpty() && !comments.contains( str ) )
            {
              comments.add( str );
            }
          }
          xorVal >>= 1;
        }
        immValFull >>= 8;
      }
    }
    else if ( opIndex == 0 && Pattern.matches( "Tog.*, #\\$..", args) )
    {
      itemType = 5;
      int val = getImmValue( args );
      if ( args.startsWith( "TogMask" ) )
      {
        comments.add( String.format( "Toggle mask = $%02X", val ) );
      }
      else
      {
        comments.add( "Toggle type: " + analyzeToggle( val << 8, null ) );
      }
    }
    else if ( opIndex == 8 )
    {
      itemType = 6;
      int pos = args.indexOf( ',' );
      String label = pos >= 0 ? args.substring( 0, pos ) : args;
      Integer val = proc.getAbsData().get( label );
      if ( val != null )
      {
        tbUsed |= 1 << val;
      }
    }
    else if ( opIndex == 0 && args.startsWith( "MinRpts" ) )
    {
      itemType = 10;
      minRpts = getImmValue( args );
    }
    
    st = new StringTokenizer( args, "," );
    while ( st.hasMoreTokens() )
    {
      String token = st.nextToken().trim();
      String comment = proc.getLblComments().get( token );
      if ( comment != null )
      {
        comments.add( comment );
      }
    }
        
    str = "";
    for ( int i = 0; i < comments.size(); i++ )
    {
      str += ( i > 0 ? "\n\t\t\t\t; " : "; " ) + comments.get( i );
    }
    item.setComments( str );
    return itemType;
  }

  private int getImmValue( String args )
  {
    int ndx = args.indexOf( "#$" );
    if ( ndx >= 0 )
    {
      return Integer.parseInt( args.substring( ndx + 2 ), 16 );
    }
    else
    {
      return 0;
    }
  }

  private String analyzeTXBytes( Hex hex, int[] togData )
  {
    int togIndex = 0x100;
    int togPos = 0x100;
    int togEnd = 0x100;
    int togCount = 0;
    String txStr = "";
    if ( hex == null || hex.length() == 0 )
    {
      irpParts[ 11 ] = null;
      return " <none>\n";
    }
    irpParts[ 11 ] = "";
    short[] data = hex.getData();
    int bitCount = 0;
    int mid = 0x100;
    if ( ( pf[ 4 ] & 0x80 ) != 0 )
    {
      mid = pf[ 4 ] & 0x7F;
    }
    for ( short val : data )
    {
      int n = ( val >> 4 ) & 0x07;
      n++;
      bitCount += n;     
      int rem = -1;
      if ( togData != null && ( togData[ 1 ] == 1 
          || ( togData[ 5 ] & 0x04 ) == 0 && togData[ 1 ] == togData[ 3 ] - togData[ 2 ] + 1 ) )
      {
        togIndex = togData[ 4 ];
        togPos = togData[ 2 ];
        togEnd = togData[ 3 ];
        togCount = togData[ 1 ];
      }
      if ( togPos < n )
      {
        togEnd = Math.min( togEnd, n-1 );
        togCount = togEnd-togPos+1;
      }
      if ( bitCount >= mid )
      {
        rem = bitCount - mid;
        n -= rem;
      }
      boolean flag = ( val & 0x80 ) > 0;
      boolean togComp = togData != null ? ( togData[ 5 ] & 0x08 ) > 0 : false;
      togComp = togCount == 1 ? false : flag && !togComp || !flag && togComp;
      int addr = 0xD0 + ( val & 0x0F );
      String label = getZeroLabel( addr );
      txStr += " " + ( flag ? "~" : "" ) + label + ":" + ( n + Math.max( 0, rem ) );
      String valStr = ( flag ? "~" : "" ) + irpLabel( label );
      int togBits = Math.max( Math.min( togEnd+1, n ) - togPos, 0 );
      if ( n > 0 )
      {
        if ( togIndex == ( val & 0x0F ) && togPos < n )
        {
          if ( togPos > 0 )
          {
            irpParts[ 11 ] += valStr + ":" + togPos + ",";
          }
          irpParts[ 11 ] += ( togComp ? "~" : "" ) + "T:" + ( togBits > 1 ? "-" : "" ) + togBits + ",";
          if ( togPos + togBits < n )
          {
            irpParts[ 11 ] += valStr + ":" + (n-togPos-togBits) + ":" + (togPos+togBits) +",";
          }
        }
        else
        {
          irpParts[ 11 ] += valStr + ":" + n + ",";
        }
        mid = 0x100;
      }
      if ( rem >= 0 )
      {
        irpParts[ 11 ] += irpParts[ 3 ];
        int togRem = togCount - togBits;
        if ( togIndex == ( val & 0x0F ) && togEnd + 1 > n && togPos < n+rem )
        {
          if ( togPos > n )
          {
            irpParts[ 11 ] += valStr + ":" + (togPos-n);
            if ( n > 0 )
            {
              irpParts[ 11 ] += ":" + n;
            }
            irpParts[ 11 ] += ",";
          }
          irpParts[ 11 ] += ( togComp ? "~" : "" ) + "T:" + ( togRem > 1 ? "-" : "" ) + togRem;
          irpParts[ 11 ] += togBits > 0 ? ":" + togBits + "," : ",";
          if ( togEnd + 1 < n + rem )
          {
            irpParts[ 11 ] += valStr + ":" + (n+rem-togEnd-1) + ":" + (togEnd+1) +",";
          }
        }
        else if ( rem > 0 )
        {
          irpParts[ 11 ] += valStr + ":" + rem;
          if ( n > 0 )
          {
            irpParts[ 11 ] += ":" + n;
          }
          irpParts[ 11 ] += ",";
        }      
        mid = 0x100;
      }
    }
    txStr += "\n";
    return txStr;
  }
  
  private String irpLabel( String label )
  {
    try
    {
    if ( label.startsWith( "Fix" ) )
    {
      int k = Integer.parseInt( label.substring( 3 ), 16 );
      return "" + "ABCDEFGHIJ".charAt( k );
    }
    else if ( label.startsWith( "Var" ) )
    {
      int k = Integer.parseInt( label.substring( 3 ), 16 );
      return "" + "XYZW".charAt( k );
    }
    else if ( label.startsWith( "Calc" ) )
    {
      int k = Integer.parseInt( label.substring( 4 ), 16 );
      return "" + "N" + k;
    }
    }
    catch ( NumberFormatException e ){};

    return null;
  }
  
  private String getZeroLabel( int addr )
  {
    AssemblerItem item = new AssemblerItem();
    item.setZeroLabel( proc, addr, new ArrayList< Integer >(), "" );
    return item.getLabel();
  }
  
  public static final String[][] AddressModes = {
    { "Dir3", "B3Z7", "$%02X, $%02X, $%02X" },
    { "Imm3", "B3Z3", "$%02X, $%02X, #$%02X" },
    { "Ind2", "B3Z3", "$%02X, ($%02X)" },
    { "Dir2", "B3Z3", "$%02X, $%02X" },
    { "Imm2", "B3Z1", "$%02X, #$%3$02X" },
    { "Immw", "B3Z1", "$%02X, #$%3$02X%2$02X" },
    { "Ind1", "B3Z3", "($%02X), $%02X" },
    { "Indx",  "B3Z7", "$%02X, $%02X[$%02X]" },
    { "BrNZ", "B3Z2R1", "NZ, $%02X, $%02X, #$%02X" },
    { "BrZ",  "B3Z2R1", "Z, $%02X, $%02X, #$%02X" },
    { "Rel1", "B3R1",   "$%02X" },
    { "Rel2", "B3Z2R1", "$%02X, $%02X" },
    { "Fun1", "B3A1", "$%02X" },
    { "Fun1B", "B3A1", "$%02X, #$%3$02X" },
    { "Fun1W", "B3A1M1", "$%02X, #$%02X%02X" },
    { "Immd", "B3", "#$%02X, #$%02X" },
    { "BrT", "B3R1A2", "T, $%02X, $%02X" },
    { "BrF", "B3R1A2", "F, $%02X, $%02X" },
    { "Nil", "B3", "" }
    
  };
  
  public static final String[][] Instructions = {
    { "MOV", "Dir2" },           { "LSL", "Dir3" },
    { "LSR", "Dir3" },           { "ADD", "Dir3" },
    { "SUB", "Dir3" },           { "OR", "Dir3" },
    { "AND", "Dir3" },           { "XOR", "Dir3" },
    { "MULT", "Dir3" },          { "DIV", "Dir3" },
    { "???", "Nil" },                  { "???", "Nil" },
    { "???", "Nil" },                  { "???", "Nil" },
    { "???", "Nil" },                  { "???", "Nil" },
    
    { "MOV", "Imm2" },           { "LSL", "Imm3" },
    { "LSR", "Imm3" },           { "ADD", "Imm3" },
    { "SUB", "Imm3" },           { "OR", "Imm3" },
    { "AND", "Imm3" },           { "XOR", "Imm3" },
    { "MULT", "Imm3" },          { "DIV", "Imm3" },
    { "???", "Nil" },                  { "???", "Nil" },
    { "???", "Nil" },                  { "???", "Nil" },
    { "???", "Nil" },                  { "???", "Nil" },
    
    { "MOVW", "Dir2" },          { "???", "Nil" },
    { "???", "Nil" },                  { "???", "Nil" },
    { "???", "Nil" },                  { "???", "Nil" },
    { "???", "Nil" },                  { "???", "Nil" },
    { "???", "Nil" },                  { "???", "Nil" },
    { "???", "Nil" },                  { "???", "Nil" },
    { "???", "Nil" },                  { "???", "Nil" },
    { "???", "Nil" },                  { "???", "Nil" },

    { "MOVW", "Immw" },          { "???", "Nil" },
    { "???", "Nil" },                  { "???", "Nil" },
    { "???", "Nil" },                  { "???", "Nil" },
    { "???", "Nil" },                  { "???", "Nil" },
    { "???", "Nil" },                  { "???", "Nil" },
    { "???", "Nil" },                  { "???", "Nil" },
    { "???", "Nil" },                  { "???", "Nil" },
    { "???", "Nil" },                  { "???", "Nil" },
    
    { "MOV", "Ind2" },          { "MOVI", "Ind2" },
    { "???", "Nil" },                  { "???", "Nil" },
    { "???", "Nil" },                  { "LSL", "Ind2" },
    { "LSR", "Ind2" },           { "ADD", "Ind2" },
    { "SUB", "Ind2" },           { "OR", "Ind2" },
    { "AND", "Ind2" },           { "XOR", "Ind2" },
    { "MOV", "Ind1" },           { "???", "Nil" },
    { "???", "Nil" },                  { "???", "Nil" },
    
    { "DBBC", "Dir3" },          { "SWAP", "Dir2" },
    { "MOVN", "Imm3" },          { "MOV", "Indx" },
    { "CARRIER", "Immd" },       { "BRA", "BrNZ" },
    { "BRA", "BrZ" },            { "BRA", "Rel1" },
    { "DBNZ", "Rel2" },          { "BSR", "Rel1" },
    { "CALL", "Fun1W" },          { "BRA", "BrT" },
    { "BRA", "BrF" },            { "RTS", "Nil" },
    { "TIMING", "Immd" },       { "END", "Nil" }   
  };
  
  public static final String[][] absLabels = {
    { "1-burst", "00", "Send Data Signal A" },
    { "0-burst", "01", "Send Data Signal B" },
    { "NormLeadIn", "02", "Send normal lead-in", "3" },
    { "AltLeadIn", "03", "Send alternate lead-in", "5" },
    { "MidFrame", "04", "Send mid-frame burst", "6" },
    { "NormLeadOut", "05", "Send normal lead-out", "2" },
    { "AltLeadOut", "06", "Send alternate lead-out", "4" },
    { "SendMARK", "07" },       // Sends MARK for duration at pointer ($AB:$AA)
    { "SendSPACE", "08" },      // Sends SPACE for duration at pointer ($AB:$AA)
    { "SendBURST", "09" },      // Sends MARK/SPACE burst pair for durations at pointer ($AB:$AA)
    { "WaitTXend", "0A" },      // Waits for current IR transmission to end
    { "GetCarrier10ms", "0B" }, // $A9:$A8 <- number of carrier cycles in 10ms
    { "GetLeadout", "0C" },     // $A7:..:$A4 <- 32-bit lead-out duration with current PF settings
    { "WaitRestartTotLeadOut", "0D", "If lead-out is total-time, wait for timer to stop, then restart it" },
    { "SendFrameStart", "0E", "Send lead-in with current settings" },
    { "SendFrameEnd", "0F", "Send end-frame burst, 1-ON and lead-out with current settings" },
    { "SendFullFrame", "10", "Send one frame from lead-in to lead-out according to settings and data bytes" }, 
    { "SendFrameData", "11", "Send data bytes with current settings, including mid-frame burst if set" }, 
    { "ResetIRControl", "12" }, // Resets IRCN, IRCNB ready for next IR transmission
    { "WaitLeadOutTimer", "13" }, // Wait for total-time lead-out timer to finish
    { "InitTimingItems", "14" },// Initialize timing item sizes to default values
    { "ClearPFregister", "15" },// Clears $94-$A3, PF byte register, to 00's
    { "ProcessExecHdrTimDir", "16" }, // Process header, timing and directive blocks of executor
    { "ProcessExecHdrTim", "17" }, // Process header and timing block of executor
    { "ProcessProtBlk", "18" }, // Process protocol block
    { "ProcessSigBlk", "19" },  // Process signal block
    { "SetRptFromPF3", "1A" },  // $B8, inner repeat counter, gets bits 0-5 of PF3
    { "ProcessSigSpec", "1B" }, // Process signal spec
    { "DoToggle", "1C", "Apply toggle of protocol block" },
    { "WaitTXendDisableIR", "1D", "Wait for IR transmission to end then disable IR module" },
    { "RunNativeCodeA6", "1E" }, // Run native code block with pointer at $A6
    { "RunPseudocodeA6", "1F" }, // Run pseudocode block with pointer at $A6
    { "RunProtPseudocode", "20" }, // Run pseudocode of active protocol block
    { "RunSigPseudocode", "21" },  // Run pseudocode of active signal block
    
    { "TestRecordKey", "50" },  // Test if current key is Record
    { "TestPowerKey", "51" },   // Test if current key is Power
    { "TestRepeatingKey", "52" },  // Test if current key is a repeating one, Vol+/-, Ch+/-, FF/Rew, SkipFwd/Back
    { "TestVolKey", "53" },     // Test if current key is Vol+/-
    { "TestKeyHeld", "54" },    // Test if current key is still held (including simulated holds in macros)
    { "TestRepeatReqd", "55" },  // Test if repeat required according to PF settings
    { "TestLeadOutRunning", "56" },  // Test if total time lead-out timer still running
    { "TestTimeExceeded", "57" },  // Test if max repeat timer (timer 2) expired
    { "TestNextByte", "58" },    // Test next executor byte (True if 1, False if 0)
    { "TestKeyAwaiting", "59" }, // Test if there is a key awaiting processing
   
    { "TimingItem", "70" },     // Sends timing item with index op3
    { "TimingItemAddr", "71" }, // $AB:$AA <- word address of timing item with index op3
    { "Branch", "72" },         // Same as BRA #op3
    { "SetIRCA", "80" }         // IRCA <- op2:op3
  };
  
  public static final String[][] zeroLabels = {
    { "PF0", "94", "PF", "10" },
    { "PD00", "30", "PD", "40", "2" },
    { "TXD0", "10", "TXD", "10" },
    { "TXB0", "20", "TXB", "10" },
    { "Tmp0", "BE", "Tmp", "0A" }, // probably BE-CF all available as temp store
    { "TogType", "89" },
    { "TogMask", "8A" },
    { "TXBcount", "B6", "TXBcount is total number of bits to send (but apparently not used)" },
    { "TXDcount", "B7", "TXDcount is number of TXD bytes to send" },
    { "MinRpts", "B8", "At least MinRpts frames sent after the first frame, more only if key held" },
    { "ToggleCtr", "E0", "ToggleCtr is incremented on each keypress" },
    { "MinKeyHeld", "E3", "Key is treated as held until MinKeyHeld frames have been sent" }
  };
}
