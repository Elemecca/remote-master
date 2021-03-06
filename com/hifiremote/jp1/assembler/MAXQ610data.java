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

public class MAXQ610data
{
//  public static List< String > irpVariables = null;
  
//  private boolean show = false;
  private PrintWriter pw = null;
  private static Executor e = null;
  private static ProtocolBlock prb = null;
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
  private LinkedHashMap< String, Integer > loopIndex = new LinkedHashMap< String, Integer >();
  private LinkedHashMap< Integer, CodeTree > loopDone = new LinkedHashMap< Integer, CodeTree >();
  private LinkedHashMap< String, Function > functionIndex = new LinkedHashMap< String, Function >();
  private List< Integer > labelAddresses = new ArrayList< Integer >();
  private LinkedHashMap< Integer, String > labels = new LinkedHashMap< Integer, String >();
  private LinkedHashMap< Integer, Node > nodeList = new LinkedHashMap< Integer, Node >();
  private int treeRoot = 0;
  private double unit = 0;
  private boolean hasNativeCode = false;
  private boolean has2usOff = false;
  private boolean changesFreq = false;
  private int initialCodeSpec = -1;
  private boolean[] choices = null;
  private String[] irpParts = new String[ 30 ];
  private int brackets = 0;
  private int minRpts = 0;
  private boolean firstFrame = true;
  private OpTree[] txd = new OpTree[ 16 ];
  private int[] txdIndex = new int[ 16 ];
  private int[] txb = new int[ 16 ];
  private int txdCount = 0;
  
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
   * 20  SB code run before TX byte encoding
   * 21  IR transmission suspended 
   * 22  PB postamble when not commutable
   * 23  repeat signal block on key held (TestRepeatRequired=yes)
   * 24  only vol, ch, ff/rew keys repeat
   */
  
  private class Executor
  {
    public int index = 0;
    public List< String > names = new ArrayList< String >();
    public Hex hex = null;
    public String description = null;
    public List< String > errors = new ArrayList< String >();
    public List< String > warnings = new ArrayList< String >();
    public List< ProtocolBlock > pbList = new ArrayList< ProtocolBlock >();
  }
  
  private class ProtocolBlock
  {
    public String description = null;
    public String condition = null;  // condition under which this block is selected
    public String sigSelector = null; // variable used to select from alternate signal specs
    public CodeTree preamble = null;  // code assignments common to all IRPs
    public CodeTree postamble = null;
    public List< Function > functions = new ArrayList< Function >();
    public List< IRPstruct > irps = new ArrayList< IRPstruct >();
    public List< String > irpParts11 = new ArrayList< String >();
    public boolean hasPBcode = false;
    public boolean hasSBcode = false;
    public List< String > sbVars = new ArrayList< String >();
    public List< String > errors = new ArrayList< String >();
    public List< String > warnings = new ArrayList< String >();
    public boolean timingBlockHasGaps = false;
    public boolean postambleCommutable = true;
    public Executor e = null;
    public int startTiming = 0;
    public int lastTiming = 0;
  }
  
  private static class CodeTree
  {
    public LinkedHashMap< String , String > assignments = new LinkedHashMap< String, String >();
    public String[] branch = new String[ 2 ];
    public String[] loop = new String[ 2 ];
    public CodeTree[] next = new CodeTree[ 2 ];
    public CodeTree parent = null;
    public Node node = null;
    public List< String > thisDests = new ArrayList< String >();
    public List< String > thisSources = new ArrayList< String >();
    private List< String > allSources = null;
    private List< String > allDests = null;
    private List< String > allParentDests = null;
    private static Node loopNode = null;
    private static String forStart = "0";
    
    public CodeTree(){}
    
    public CodeTree( CodeTree parent )
    {
      this.parent = parent;
    }
    
    public List< String > getAllSources()
    {
      if ( allSources != null )
      {
        return allSources;
      }
      allSources = new ArrayList< String >();
      
      for ( String var : thisSources )
      {
        if ( inLoop() || parent == null || !parent.getAllParentDests().contains( var ) )
        {
          allSources.add( var );
        }
      }
      
      CodeTree p = parent;
      while ( p != null && p.node.isDataBranch() )
      {
        for ( String var : p.thisSources )
        {
          if ( !allSources.contains( var ) )
          {
            allSources.add( var );
          }
        }
        p = p.parent;
      }
      
      if ( next[ 0 ] != null )
      {
        for ( String var : next[ 0 ].getAllSources() )
        {
          if ( !allSources.contains( var ) )
          {
            allSources.add( var );
          }
        }
      }
      if ( next[ 1 ] != null )
      {
        for ( String var : next[ 1 ].getAllSources() )
        {
          if ( !allSources.contains( var ) )
          {
            allSources.add( var );
          }
        }
      }
      return allSources;
    }
    
    public List< String > getAllDests()
    {
      if ( allDests != null )
      {
        return allDests;
      }
      allDests = new ArrayList< String >();
      
      for ( String var : thisDests )
      {
        allDests.add( var );
      }
      
      if ( next[ 0 ] != null )
      {
        for ( String var : next[ 0 ].getAllDests() )
        {
          if ( !allDests.contains( var ) )
          {
            allDests.add( var );
          }
        }
      }
      if ( next[ 1 ] != null )
      {
        for ( String var : next[ 1 ].getAllDests() )
        {
          if ( !allDests.contains( var ) )
          {
            allDests.add( var );
          }
        }
      }
      return allDests;
    }
    
    public List< String > getAllParentDests()
    {
      if ( allParentDests != null )
      {
        return allParentDests;
      }
      allParentDests = new ArrayList< String >();

      if ( parent != null )
      {
        for ( String var : parent.thisDests )
        {
          if ( !allParentDests.contains( var ) )
          {
            allParentDests.add( var );
          }
        }
        for ( String var : parent.getAllParentDests() )
        {
          if ( !allParentDests.contains( var ) )
          {
            allParentDests.add( var );
          }
        }
      }
      return allParentDests;
    }
    
    private boolean inLoop()
    {
      if ( parent == null )
      {
        return false;
      }
      if ( parent.branch[ 0 ].equals( "loop" ) )
      {
        return this == parent.next[ 0 ];
      }
      return parent.inLoop();
    }
    
    private List< String > getStringList( LinkedHashMap< String , String > map )
    {
      List< String > list = new ArrayList< String >();
      for ( String var : map.keySet() )
      {
        // prb.sbVars is a list TX bytes plus all other variables referenced by
        // signal block code.  Variables of form "A[xx]" need to be treated as
        // TX bytes as we don't know which fixed/variable bytes they correspond to.
        if ( prb.sbVars.contains( var ) || var.contains( "[" )
            || getAllSources().contains( var ) )
        {
          list.add( var + "=" + map.get( var ) );
        }
      }
      return list;
    }
    
    public List< String > description()
    {
      List< String > list = new ArrayList< String >();
      if ( branch[ 0 ].equals( "preloop" ) )
      {
        for ( String var : next[ 0 ].assignments.keySet() )
        {
          assignments.put(  var, next[ 0 ].assignments.get(  var ) );
        }
        assignments.remove( next[ 0 ].loop[ 0 ] );
        list.addAll( getStringList( assignments ) );
        list.addAll( next[ 0 ].description() );
      }
      else if ( branch[ 0 ].equals( "next" ) )
      {
        list.addAll( getStringList( assignments ) );
        if ( loopNode == null && next[ 1 ].branch[ 0 ].equals( "loop" ) )
        {
          forStart = "1";
          list.addAll( next[ 1 ].description() );
        }
//        list.add( "next " + loop[ 0 ] );
      }
      else if ( branch[ 0 ].equals( "loop" ) )
      {
        loopNode = node;
        list.add( "for n" /*+ loop[ 0 ]*/ + "=" + forStart + " to " + loop[ 1 ] + " {" );
        forStart = "0";
        List< String > list2 = next[ 0 ].description();
        for ( String s : list2 )
        {
          list.add( "  " + s );
        }
        list.add( "}" );
        loopNode = null;
        if ( next[ 1 ] != null )
        {
          list.addAll( next[ 1 ].description() );
        }
      }
      else if ( branch[ 0 ].equals( "while" ) )
      {
        loopNode = node;
        list.add( "while " + loop[ 0 ] + " {" );
        List< String > list2 = next[ 0 ].description();
        for ( String s : list2 )
        {
          list.add( "  " + s );
        }
        list.add( "}" );
        loopNode = null;
        if ( next[ 1 ] != null )
        {
          list.addAll( next[ 1 ].description() );
        }
      }
      else if ( branch[ 0 ].equals( "call" ) )
      {
        list.addAll( getStringList( assignments ) );
        list.add( branch[ 1 ] + "()" );
        if ( next[ 0 ] != null )
        {
          list.addAll( next[ 0 ].description() );
        }
      }
      else if ( loopNode != null && node.start == loopNode.branch[ 2 ] )
      {
        // branch in loop exits at first instruction beyond loop
        list.add( "break" );
      }
      else if ( loopNode != null && ( node.start > loopNode.branch[ 2 ] || node.start < loopNode.branch[ 1 ] )
          && ( node.branch[ 1 ] < loopNode.branch[ 0 ] || node.branch[ 1 ] > loopNode.branch[ 2 ]
              || node.branch[ 2 ] > 0 && ( node.branch[ 2 ] < loopNode.branch[ 0 ] || node.branch[ 2 ] > loopNode.branch[ 2 ] ) ) )
      {
        // branch in loop exits loop and does not return to it
        list.add( "<ERROR>" );
      }
      else if ( loopNode != null && node.branch[ 2 ] == loopNode.start )
      {
        // branch returns to start of loop, so no need to loop back explicitly
        list.addAll( getStringList( assignments ) );
      }
      else if ( !branch[0].isEmpty() && next[ 1 ] != null && next[ 1 ].branch[ 0 ].equals( "while" ) )
      {
        // conditional branch that loops back to start of while loop
        list.addAll( getStringList( assignments ) );
        list.addAll( next[ 1 ].description() );
      }
      else
      {
        // if within logical loop but branch start is outside loop boundaries, then we know
        // that the branch returns are within those boundaries, so no special treatment
        // is required
//        list.addAll( getStringList( assignments ) );
        if ( !branch[0].isEmpty() )
        {
          // conditional branch
          List< String > list2 = next[ 0 ] == null ? getStringList( assignments ) : next[ 0 ].description();
          List< String > list3 = next[ 1 ] == null ? getStringList( assignments ) : next[ 1 ].description();
          if ( !list2.isEmpty() )
          {
            list.add( "if (" + branch[ 0 ] + ") {" );
            for ( String s : list2 )
            {
              list.add( "  " + s );
            }
            list.add( "}" );
          }
          if ( !list3.isEmpty() )
          {
            list.add( ( !list2.isEmpty() ? "else " : "" ) + "if (" + branch[ 1 ] + ") {" );
            for ( String s : list3 )
            {
              list.add( "  " + s );
            }
            list.add( "}" );
          }
        }
        else if ( next[ 0 ] != null && next[ 1 ] == null )
        {
          // unconditional branch
          if ( next[ 0 ].branch[ 0 ].equals( "next" ) && loopNode == null )
          {
            // branches into loop, so remove loop control variable as its
            // value will be displayed in loop heading
            next[ 0 ].assignments.remove( next[ 0 ].loop[ 0 ] );
          }
          list.addAll( next[ 0 ].description() );     
        }
        else if ( loopNode != null && node.start != loopNode.branch[ 2 ] )
        {
          // branch not RTS
          list.add( "<ERROR>" );
        }
        else
        {
          list.addAll( getStringList( assignments ) );
        }
      }     
      return list;
    }

    @Override
    public String toString()
    {
      String str = "";
      for ( String s : description() )
      {
        str += s + "\n";
      }
      return str;
    }
  }

  private class IRPstruct
  {
    public String generalSpec = "";
    public String bitSpec= "";
    public boolean base16 = false;
    public String irStream = "";
    public int unit = 0;
    public List< String > comments = new ArrayList< String >();
    
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
      if ( base16 != is.base16 )
        return false;
      if ( !irStream.equals( is.irStream ) )
        return false;
      if ( !Arrays.deepEquals( comments.toArray( new String[ 0 ] ), is.comments.toArray( new String[ 0 ] ) ) )
        return false;
      return true;
    }
    
    @Override
    public IRPstruct clone()
    {
      IRPstruct struct = new IRPstruct();
      struct.generalSpec = generalSpec;
      struct.bitSpec = bitSpec;
      struct.base16 = base16;
      struct.irStream = irStream;
      struct.unit = unit;
      struct.comments = new ArrayList< String >();
      for ( String s : comments )
      {
        struct.comments.add( s );
      }
      return struct;
    }
    
    @Override 
    public String toString()
    {
      String s = generalSpec + bitSpec;
      if ( base16 )
      {
        s += "(<1:1|";
        for ( int i = 1; i < 16; i++ )
        {
          s += "1:1,0:" + i + ( i < 15 ? "|" : ">" );
        }
      }
      s += irStream + ( base16 ? ")" : "" );
      return s;
    }
  }
  
  private class OpTree
  {
    public String op = null;
    public Value value = null;
    public ArrayList< OpTree > opArgs = null;
    
    public OpTree()
    { }
    
    public OpTree( String var )
    {
      this.value = new Value( var );
    }
    
    public OpTree clone()
    {
      OpTree newTree = new OpTree();
      newTree.op = op;
      if ( value != null )
      {
        newTree.value = value.clone();
      }
      if ( opArgs != null )
      {
        newTree.opArgs = new ArrayList< OpTree >();
        for ( OpTree ot : opArgs )
        {
          newTree.opArgs.add( ot.clone() );
        }
      }
      return newTree;
    }
    
    public OpTree doOp( String os, OpTree arg )
    {      
      int n = -1;
      int nRev = -1;
      int v = -1;
      int vRev = -1;
      boolean consecutive = false;
      try
      {
        if ( arg != null && arg.value != null )
        {
          n = Integer.parseInt( arg.value.lsbEvaluate() );
          int rev[] = new int[ 4 ];
          reverseByte( n, rev );
          consecutive = rev[ 1 ] == rev[ 3 ] - rev[ 2 ] + 1;
          nRev = rev[ 0 ];
        }
      }
      catch ( NumberFormatException nfe )
      {
        n = -1;
      }
      
      try
      {
        if ( value != null )
        {
          v = Integer.parseInt( value.lsbEvaluate() );
          int rev[] = new int[ 4 ];
          reverseByte( v, rev );
          vRev = rev[ 0 ];
        }
      }
      catch ( NumberFormatException nfe )
      {
        v = -1;
      }

      if ( Arrays.asList( "OR", "XOR", "ADD" ).contains( os ) 
          && value != null && value.var.equals( "0" ) )
      {
        return arg.clone();
      }
      else if ( Arrays.asList( "AND", "REV", "MULT" ).contains( os ) && value != null && value.var.equals( "0" ) )
      {
        return new OpTree( "0" );
      }
      else if ( n >= 0 && Arrays.asList( "LSR", "LSL", "AND", "OR" ).contains( os ) 
          && ( !os.equals( "AND" ) || consecutive ))
      {
        if ( value != null )
        {
          OpTree ot = new OpTree();
          ot.value = value.doNumOp( os, n );
          return ot;
        }
        else if ( Arrays.asList( "AND", "OR" ).contains( op ) 
            || op.equals( "XOR" ) && Arrays.asList( "LSR", "LSL", "AND" ).contains( os ) )
        {
          OpTree ot = new OpTree();
          ot.op = op;
          ot.opArgs = new ArrayList< OpTree >();
          for ( OpTree o : opArgs )
          {
            ot.opArgs.add( o.doOp( os, arg ) );
          }
          return ot;
        }
        else if ( op.equals( "REV" ) )
        {
          OpTree ot1 = new OpTree();
          ot1.opArgs = new ArrayList< OpTree >();
          if ( os.equals( "REV" ) )
          {
            return opArgs.get( 0 );
          }
          else if ( os.equals( "LSR" ) || os.equals( "LSL" ) )
          {
            ot1 = opArgs.get( 0 ).doOp( os.equals( "LSR" ) ? "LSL" : "LSR", arg );
            OpTree ot = new OpTree();
            ot.op = "REV";
            ot.opArgs = new ArrayList< OpTree >();
            ot.opArgs.add( ot1 );
            return ot;
          }
          else if ( os.equals( "AND" ) || os.equals( "OR" ) )
          {
            ot1 = opArgs.get( 0 ).doOp( os, new OpTree( ""+nRev ) );
            OpTree ot = new OpTree();
            ot.op = "REV";
            ot.opArgs = new ArrayList< OpTree >();
            ot.opArgs.add( ot1 );
            return ot;
          }
        }
        else
        {
          return (new OpTree( "(" + lsbEvaluate() + ")" ) ).doOp( os, arg );
        }
      }
      else if ( n >= 0 && v >= 0 && Arrays.asList( "ADD", "SUB", "MULT" ).contains( os ) )
      {
        v = os.equals( "ADD" ) ? vRev+nRev : os.equals( "SUB" ) ? vRev-nRev : vRev*nRev;
        int rev[] = new int[ 4 ];
        reverseByte( v, rev );
        return new OpTree( ""+rev[0] );
      }
      else if ( os.equals( "CPL" ) )
      {
        if ( value != null )
        {
          OpTree ot = new OpTree();
          ot.value = value.doNumOp( os, 0 );
          return ot;
        }
        else if ( Arrays.asList( "AND", "OR" ).contains( op ) )
        {
          OpTree ot = new OpTree();
          ot.op = op.equals( "AND" ) ? "OR" : "AND";
          ot.opArgs = new ArrayList< OpTree >();
          for ( OpTree o : opArgs )
          {
            ot.opArgs.add( o.doOp( os, arg ) );
          }
          return ot;
        }
        else if ( op.equals( "XOR" ) && opArgs.size() == 2 )
        {
          OpTree ot1 = new OpTree();
          ot1.op = "AND";
          ot1.opArgs = new ArrayList< OpTree >();
          ot1.opArgs.add( opArgs.get( 0 ).doOp( "CPL", null ) );
          ot1.opArgs.add( opArgs.get( 1 ).doOp( "CPL", null ) );
          OpTree ot2 = new OpTree();
          ot2.op = "AND";
          ot2.opArgs = new ArrayList< OpTree >();
          ot2.opArgs.add( opArgs.get( 0 ) );
          ot2.opArgs.add( opArgs.get( 1 ) );
          OpTree ot = new OpTree();
          ot.op = "OR";
          ot.opArgs = new ArrayList< OpTree >();
          ot.opArgs.add( ot1 );
          ot.opArgs.add( ot2 );
          return ot;
        }
        else
        {
          return null;
        }
      }
      else if ( Arrays.asList( "AND", "OR", "XOR" ).contains( os ) )
      {
        if ( value == null && op.equals( os ) )
        {
          OpTree ot = clone();
          ot.opArgs.add( arg );
          return ot;
        }

        OpTree ot = new OpTree();
        ot.op = os;
        ot.opArgs = new ArrayList< OpTree >();
        ot.opArgs.add( this );
        ot.opArgs.add( arg );
        return ot;
      }
      else if ( os.equals( "SWAP" ) )
      {
        OpTree ot1 = doOp( "LSL",  new OpTree( "" + 0x20 ) );
        OpTree ot2 = doOp( "LSR",  new OpTree( "" + 0x20 ) );
        OpTree ot = new OpTree();
        ot.op = "OR";
        ot.opArgs = new ArrayList< OpTree >();
        ot.opArgs.add( ot1 );
        ot.opArgs.add( ot2 );
        return ot;
      }
      else if ( os.equals( "REV" ) )
      {
        if ( value == null && op.equals( "REV" ) )
        {
          return opArgs.get( 0 ).clone();
        }
        else
        {
          OpTree ot = new OpTree();
          ot.op = "REV";
          ot.opArgs = new ArrayList< OpTree >();
          ot.opArgs.add( this );
          return ot;
        }
      }
      else if ( Arrays.asList( "MULT", "ADD", "SUB" ).contains( os ) )
      {
        OpTree ot1 = doOp( "REV", null );
        OpTree ot2 = arg.doOp( "REV", null );
        
        if ( ot1.value == null && ot1.op.equals( os ) )
        {
          OpTree ot = ot1.clone();
          ot.opArgs.add( ot2 );
          return ot.doOp( "REV", null );
        }
        
        OpTree ot = new OpTree();
        ot.op = os;
        ot.opArgs = new ArrayList< OpTree >();
        ot.opArgs.add( ot1 );
        ot.opArgs.add( ot2 );
        return ot.doOp( "REV", null );
      }
      return null;
    }
    
    public String lsbEvaluate()
    {
      return evaluate( false );
    }
    
    private String evaluate( boolean msb )
    {
      String str = "";
      if ( value != null )
      {
        str = value.evaluate( msb );
      }
      else if ( op.equals( "REV" ) )
      {
        str = opArgs.get( 0 ).evaluate( !msb );
      }
      else if ( op.equals( "VAR" ) )
      {
        str = opArgs.get( 0 ).evaluate( true );
        try
        {
          int ref = Integer.parseInt( str );
          str = irpLabel( ref );
        }
        catch ( NumberFormatException nfe )
        {
          str = "VAR(" + str + ")";
        }
        str += "[" + opArgs.get( 1 ).evaluate( false ) + "]";
        if ( msb )
        {
          str += ":-8";
        }
      }
      else if ( !msb || Arrays.asList( "OR", "AND", "XOR" ).contains( op ))
      {
        for ( int i = 0; i < opArgs.size(); i++ )
        {
          OpTree ot = opArgs.get( i );
          String eval = ot.evaluate( msb );
          if ( eval.equals( "0" ) && Arrays.asList( "OR", "XOR", "ADD" ).contains( op ) && ( !str.isEmpty() || i < opArgs.size() - 1 ) )
          {
            continue;
          }
          String opSymbol = op.equals( "AND" ) ? "&" : op.equals( "OR" ) ? "|" 
              : op.equals( "XOR" ) ? "^" : op.equals( "ADD" ) ? "+" : op.equals( "SUB" ) ? "-"
                  : op.equals( "MULT" ) ? "*" : "??";
          str += str.isEmpty() ? "" : opSymbol;

          if ( Pattern.matches( "[A-Za-z0-9\\[\\]\\-\\:\\" + opSymbol + "]+", eval ) )
          {
            // Check if any minus is preceded by colon or is in a subexpression
            // enclosed in brackets
            int ndx = -1;
            while ( true )
            {
              ndx = eval.indexOf( "-", ndx + 1 );
              if ( ndx <= 0 || eval.charAt( ndx - 1 ) != ':' 
                  && !( eval.substring( 0, ndx ).contains( "[" ) 
                      && eval.substring( ndx ).contains( "]" ) ) )
              {
                break;
              }
            }
            if ( ndx >= 0 )
            {
              str += "(" + eval + ")";
            }
            else
            {
              str += eval;
            }
          }
          else
          {
            str += "(" + eval + ")";
          }
        }
      }
      else  // other msb cases 
      {
        str = "(" + evaluate( false ) + "):-8";
      }
      if ( Pattern.matches( "\\([A-Za-z0-9\\[\\]\\-\\:\\&\\|\\^]+\\)", str ) )
      {
        // there is only one set of brackets, around the outside, so remove them
        str = str.substring( 1, str.length() - 1 );
      }
      return str;
    }
    
    public String msbEvaluate()
    {
      return evaluate( true );
    }
  }
  
  private class Value
  {    
    // represents ((var>>shift)& and ) | or    
    public Value( String var )
    {
      this.var = var;
    }
    
    public Value( String var, int shift, int and, int or )
    {
      this.var = var;
      this.shift = shift;
      this.and = and;
      this.or = or;
    }
    
    public Value doNumOp( String op, int n )
    {
      // All arguments are passed in lsb form, but n for shifts must be converted
      // back to its true msb value.  Also LSR, LSL refer to right and left of
      // msb values, so left and right are interchanged for lsb forms.
      int[] rev = new int[ 4 ];
      reverseByte( n, rev );
      if ( op.equals( "LSR" ) )
      {
        if ( var.equals( "0" ) || shift+rev[0] > 7 || shift+rev[0] < -7 || ((and<<rev[0])&0xFF)==0 )
        {
          return new Value( "" + 0 );
        }
        return new Value( var, shift+rev[0], (and<<rev[0])&0xFF, (or<<rev[0])&0xFF );
      }
      else if ( op.equals( "LSL" ) )
      {
        if ( var.equals( "0" ) || shift-rev[0] > 7 || shift-rev[0] < -7 || ((and>>rev[0])&0xFF)==0 )
        {
          return new Value( "" + 0 );
        }
        return new Value( var, shift-rev[0], and>>rev[0], or>>rev[0] );
      }
      else if ( op.equals( "AND" ) )
      {
        if ( var.equals( "0" ) || n == 0 )
        {
          return new Value( "" + 0 );
        }
        return new Value( var, shift, and&n, or&n );
      }
      else if ( op.equals( "OR" ) )
      {
        if ( n == 255 )
        {
          return new Value( "" + 255 );
        }
        return new Value( var, shift, and, or|n );
      }
      else if ( op.equals( "CPL" ) )
      {
        String newVar = var.startsWith( "~" ) ? var.substring( 1 ) : "~" + var;
        int newAnd = (~or) & 0xFF;
        int newOr = (~and) & (~or) & 0xFF;
        return new Value( newVar, shift, newAnd, newOr );
      }
      return null;
    }
    
    private String msbEvaluate()
    {
      // Return string corresponding to the msb form of the value
      int[] revAnd = new int[ 4 ];
      int[] revOr = new int[ 4 ];
      reverseByte( and, revAnd );
      reverseByte( or, revOr );
      int num = 0;
      String str = "";
      
      if ( revAnd[ 1 ] == revAnd[ 3 ] - revAnd[ 2 ] + 1 )
      {
        // The "and" value has 1's in consecutive positions
        try
        {
          // Case where var is string form of decimal integer representing an lsb byte
          int[] revVar = new int[ 4 ];
          num = Integer.parseInt( var );
          reverseByte( num, revVar );
          num = revVar[0];
          num >>= shift;
          num &= revAnd[0];
          num |= revOr[0];
          str += num;
        }
        catch ( NumberFormatException nex )
        {
          // Case where var is name of a variable
          if ( revAnd[2]>0 )
          {
            // Number of zeroes at left end of "and" mask is revAnd[2].  These
            // correspond to left shifts, i.e. multiplications by 2, of value
            // selected by "and" mask.
            str += ( (int)Math.pow( 2, revAnd[2] ) )+ "*";
          }
          // Now build the bitfield
          str += var;                   // this may have a prefix "~"
          int disc = ( 7 - revAnd[ 3 ] - shift );  // number of bits to discard (see below)
          int keep = revAnd[ 1 ] - (disc < 0 ? -disc : 0 );
          str += ( keep > 1 ? ":-" : ":" ) + keep;    // the number of bits, with "-" as the bit order must be reversed
          // Number of zeroes at right end of "and" mask is 7-revAnd[3]. These
          // correspond to bits to be discarded from shifted value before selecting 
          // next rev[1] bits, but mapped to unshifted value (shift=count of left shifts)
          // this corresponds to discarded "shift" fewer bits, leaving "disc".  If disc
          // is < 0, it reduces the number of bits to keep, taken into account above
          if ( disc > 0 )
          {
            str += ":" + disc;
          }
          if ( keep == 0 )
          {
            str = "" + revOr[0];
          }
          else if ( or > 0 )
          {
            str = "(" + str + ")|" + revOr[0];
          }
        }
        return str;
      }
      return null;
    }
    
    private String lsbEvaluate()
    {
      // Return string corresponding to the lsb form of the value
      if ( and == 0 )
      {
        return "" + or;
      }
      int[] revAnd = new int[ 4 ];
      int[] revOr = new int[ 4 ];
      reverseByte( and, revAnd );
      reverseByte( or, revOr );
      int num = 0;
      String str = "";
      if ( revAnd[ 1 ] == revAnd[ 3 ] - revAnd[ 2 ] + 1 )
      {
        // The "and" value has 1's in consecutive positions
        try
        {
          // Case where var is string form of decimal integer representing an lsb byte
          num = Integer.parseInt( var );
          num <<= shift;
          num &= and;
          num |= or;
          str += num;
        }
        catch ( NumberFormatException nex )
        {
          // Case where var is name of a variable
          if ( revAnd[3]<7 )
          {
            // Number of zeroes at right end of "and" mask is 7-revAnd[3].  These
            // correspond to left shifts, i.e. multiplications by 2, of value
            // selected by "and" mask.
            str += ( (int)Math.pow( 2, 7-revAnd[3] ) )+ "*";
          }
          // Now build the bitfield
          str += var;                   // this may have a prefix "~"
          int disc = ( 7 - revAnd[ 3 ] - shift );  // number of bits to discard (see below)
          if ( revAnd[ 1 ] < 8 )
          {
            str += ":" + ( revAnd[ 1 ] - (disc < 0 ? -disc : 0 ) );    // the number of bits
            // Number of zeroes at right end of "and" mask is 7-revAnd[3]. These
            // correspond to bits to be discarded from shifted value before selecting 
            // next rev[1] bits, but mapped to unshifted value (shift=count of left shifts)
            // this corresponds to discarded "shift" fewer bits, leaving "disc".  If disc
            // is < 0, it reduces the number of bits to keep, taken into account above
            if ( disc > 0 )
            {
              str += ":" + disc;
            }
          }
          if ( or > 0 )
          {
            if ( revAnd[1] < 8 )
            {
              str = "(" + str + ")";
            }
            str += "|" + or;
          }
        }
        return str;
      }
      return null;
    }
    
    public String evaluate( boolean msb )
    {
      return msb ? msbEvaluate() : lsbEvaluate();
    }
    
    public Value clone()
    {
      return new Value( var, shift, and, or );
    }

    public String var = null;
    public int shift = 0;   // number of left shifts (of lsb form, corresponding to right shifts of msb
    public int and = 0xFF;
    public int or = 0;
  }
  
  private class IRPIndexItem
  {
    IRPstruct irp = null;
    int[] location = new int[ 3 ];
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
      return ts;
    }
    
    public boolean same( TimingStruct ts, int sbIndex )
    {
      if ( ts.pbPath != pbPath )
      {
        return false;
      }
      for ( int i = 0; i <= sbIndex; i++ )
      {
        int[] p1 = ts.sbPaths.get( i );
        int[] p2 = sbPaths.get( i );
        if ( p1 == null && p2 != null || p1 != null && p2 == null )
        {
          return false;
        }
        if ( p1 != null && p2 != null && ( p1[ 0 ] != p2[ 0 ] || p1[ 1 ] != p2[ 1 ]) )
        {
          return false;
        }
      }
      return true;
    }
 
    public int[] carriers = null;
    public int[] durations = null;
    public int[] pf = null;
    public Integer pbPath = null;
    public String pbCondition = null;
    public LinkedHashMap< Integer, int[] > sbPaths = new LinkedHashMap< Integer, int[] >();
  }
  
  private class Node
  {
    public int start = 0;
    public int[] branch = null;
    public String branchVar = "";
    public int branchType = -1; 
//    public String[] comments = new String[ 2 ];
    /*  branch elements are:
          0 = end
          1 = start for left branch (on zero or true, -1 if no branch)
                or start of loop for DBNZ loop ( = branch[ 0 ] in this case
                as there is actually no loop-start instructions
          2 = start for right branch (on nonzero or false, -1 if no branch)
                or end of loop (the DBNZ instruction) for DBNZ loop
        branch type is
         -1   : unknown type
          0-7 : branch on this bit of branchVar being zero/nonzero
          8   : branch on branchVar being true/false
          9   : system branch (first frame / key held)
         10   : unconditional
         11   : branch envelops loop
         12   : body of DBNZ loop
         13   : branch ends at start of loop
         14   : RTS instruction
         15   : BSR instruction 
         16   : in-line function
       $1xx   : branch on ( branchVar & xx ) being zero/nonzero
    */
    
    public Node( int start )
    {
      this.start = start;
    }
    
    public boolean isDataBranch()
    {
      return branchType == 10 || branchType >=0 && branchType < 8;
    }
    
    public String getComments( int val, String prefix )
    {
      String str = "";
      if ( branchType >= 0 && branchType < 8 )
      {
        str = branchVar + ":1:" + branchType + "=" + val; 
      }
      else if ( ( branchType & 0xFF00 ) == 0x100 )
      {
        int mask = branchType & 0xFF;
        if ( mask == 0xFF )
        {
          str = branchVar + ( val == 0 ? "=0" : "!=0" );
        }
        else
        {
          str = branchVar + "&" + mask + ( val == 0 ? "=0" : "!=0" );
        }
      }
      else if ( branchType == 8 )
      {
        str = prefix + "key is ";
        str += val == 0 ? "" : "not ";
        str += branchVar;
      }
      else if ( branchType == 11 )
      {
        str = val == 0 ? completeItemList.get( branch[ 2 ] - 1 ).getOperation().equals( "DBNZ" ) ? "loop" : "while" : branchVar;
      }
      else if ( branchType == 12 )
      {
        str = val == 0 ? "next" : branchVar;
      }
      else if ( branchType == 13 )
      {
        str = val == 0 ? "preloop" : branchVar;
      }
      else if ( branchType == 15 )
      {
        AssemblerItem ai = completeItemList.get( branch[ 0 ] );
        int offset = ai.getHex().getData()[ 1 ];
        String label = labels.get( ai.getAddress() + offset - ( offset >= 128 ? 0x100 : 0 ) );
        String function = functionIndex.get( label ).name;
        str = val == 0 ? "call" : function;
      }
      else if ( branchType == 16 )
      {
        AssemblerItem ai = completeItemList.get( branch[ 0 ] );
        int offset = ai.getHex().getData()[ 1 ];
        String label = labels.get( ai.getAddress() + offset - ( offset >= 128 ? 0x100 : 0 ) );
        String function = functionIndex.get( label ).name;
        str = val == 0 ? "call" : function;
      }
      return str;
    }
  }
  
  private class Function
  {
    public String name = null;
    public CodeTree code = new CodeTree();
    
    public Function( String name )
    {
      this.name= name;
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
      MAXQ610data.e = e;
      e.index = eIndex++;
      Hex hex = e.hex;
      irpStruct = new IRPstruct();
      execCount++;
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
      functionIndex.clear();
      prb = null;
      analyzeExecutor( hex );
      labelIndex.clear();
      loopIndex.clear();
      loopDone.clear();
      functionIndex.clear();
      nodeList.clear();
      completeItemList = expandLabels( fullItemList );
      fullItemList = null;
      labels.clear();
      pbIndex = 0;
      sbIndex = 0;
      irpStruct = new IRPstruct();
      for ( ProtocolBlock pb : e.pbList )
      {
        pb.irps.clear();
        pb.warnings.clear();
      }
      prb = null;
      analyzeExecutor( hex );
//      e.description = s;

//      if ( !e.hasSBcode )
//      {
//        execNoSBCodeCount++;
//        if ( !e.hasPBcode )
//        {
//          execNoCodeCount++;
//        }
//        else if ( dataChangeOnly )
//        {
//          execNoSBDataChPBCount++;
//          execNoSBDataTimeChPBCount++;
//        }
//        else if ( dataTimingChangeOnly )
//        {
//          execNoSBDataTimeChPBCount++;
//        }
//      }
    }
    
    List< IRPIndexItem > irpIndex = new ArrayList< IRPIndexItem >();
    for ( Executor e : execs )
    {
      for ( int i = 0; i < e.pbList.size(); i++ )
      {
        ProtocolBlock pb = e.pbList.get( i );
        for ( int j = 0; j < pb.irps.size(); j++ )
        {
          IRPIndexItem iii = new IRPIndexItem();
          iii.irp = pb.irps.get( j );
          iii.location[ 0 ] = e.index;
          iii.location[ 1 ] = i;
          iii.location[ 2 ] = j;
          if ( iii.irp.generalSpec != null && iii.irp.bitSpec != null && iii.irp.irStream != null )
          {
            irpIndex.add( iii );
          }
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
        ProtocolBlock pb = ex.pbList.get( iii.location[ 1 ] );
        String s = null;
        if ( !ex.errors.isEmpty() || !pb.errors.isEmpty() )
        {
          s = iii.irp.toString();
          for ( String err : ex.errors )
          {
            s += "\n  " + err;
          }
          for ( String err : pb.errors )
          {
            s += "\n  " + err;
          }
        }
        else
        {
          s = iii.irp.toString();
          shownCount++;
        }
        
        s += " : " + iii.location[ 1 ] + "/" + iii.location[ 2 ];
        s += "\n   ";
        s += ex.names.get( 0 );
        s += "\n\n";
        pw.print( s.replaceAll( "\\n", System.getProperty("line.separator" ) ) );
      }
      String s = "IRP values shown: " + shownCount + " of " + fullCount + "\n";
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
        MAXQ610data.e = e;
        String s = "";
        for ( String eName : e.names )
        {
          s += eName + "\n";
        }
        s += e.description;
        String pbDesc = "";
        for ( ProtocolBlock pb : e.pbList )
        {
          prb = pb;
          if ( !pbDesc.isEmpty() )
          {
            pbDesc += "\n- - - - - - - - - - - -\n";
          }
          pbDesc += pb.description;
          
          for ( String str : pb.warnings )
          {
            pbDesc += str + "\n";
          }
          
          pbDesc += "\nPreamble:\n";
          
          if ( pb.preamble != null || pb.postamble != null && pb.postambleCommutable )
          {
            pbDesc += pb.preamble != null ? pb.preamble : "";
            if ( pb.postamble != null && pb.postambleCommutable )
            {
              pbDesc += pb.postamble;
            }
            pbDesc += "\n";
          }
          else
          {
            pbDesc += "<none>\n";
          }

          for ( Function f : pb.functions )
          {
            pbDesc += "\nProcedure " + f.name + ":\n";
            pbDesc += f.code;
          }

          if ( !pb.sbVars.isEmpty() )
          {
            pbDesc += "\nSB referenced variables:\n";
            for ( String str : pb.sbVars )
            {
              pbDesc += str + "\n";
            }
          }
          
          for ( IRPstruct irp : pb.irps )
          {
            pbDesc += irp + getCondition( irp ) + "\n";
          }
        }
        s += pbDesc;
        s += "\n--------------------\n\n";
        pw.print( s.replaceAll( "\\n", System.getProperty("line.separator" ) ) );
      }

      String s = "Count of executors: " + execCount + "\n";
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
    
    try
    {
      FileWriter fw = new FileWriter( "MAXQtranslations.txt" );
      pw = new PrintWriter( fw );
      fullCount = 0;
      shownCount = 0; 
      for ( Executor e : execs )
      {
        fullCount++;
        MAXQ610data.e = e;
        boolean hasErrors = !e.errors.isEmpty();
        for ( ProtocolBlock pb : e.pbList )
        {
          hasErrors = hasErrors || !pb.errors.isEmpty();
        }
        if ( hasErrors )
        {
          continue;
        }
        
        shownCount++;
        int nums = e.hex.getData()[ 2 ];
        String s = "";
        for ( String eName : e.names )
        {
          s += eName + "\n";
        }
        int fix = ( nums >> 4 ) & 0x0F;
        s += "\n"+ fix + " fixed byte" + ( fix != 1 ? "s" : "" );
        if ( fix > 0 )
        {
          s += ": bit-reversed";
        }
        for ( int i = 0; i < fix; i++ )
        {
          s += " " + "ABCDEFGHIJ".charAt( i );
        }
        int var = nums & 0x0F;
        s += "\n" + var + " variable byte" + ( var != 1 ? "s" : "" );
        if ( var > 0 )
        {
          s += ": bit-reversed";
        }
        for ( int i = 0; i < var; i++ )
        {
          s += " " + "XYZW".charAt( i );
        }
        s += "\n\n";

        String pbDesc = "";
        for ( ProtocolBlock pb : e.pbList )
        {
          prb = pb;
          if ( pb.condition != null )
          {
            pbDesc += "If " + pb.condition + "\n\n";
          }
          
          for ( String str : pb.warnings )
          {
            pbDesc += "***" + str + "\n";
          }
          
          String pre = pb.preamble != null ? pb.preamble.toString() : "";
          String post = pb.postamble != null ? pb.postamble.toString() : "";
          
          if ( !pre.isEmpty() || !post.isEmpty() && pb.postambleCommutable )
          {
            pbDesc += "Preamble:\n";
            pbDesc += pre; //pb.preamble != null ? pb.preamble : "";
            if ( !post.isEmpty() && pb.postambleCommutable ) //&& !pb.postamble.assignments.isEmpty() )
            {
              if ( !pre.isEmpty() )
              {
                pbDesc += "- - -\n";
              }     
              pbDesc += post;
            }
            pbDesc += "\n";
          }

          for ( Function f : pb.functions )
          {
            pbDesc += "Procedure " + f.name + ":\n";
            pbDesc += f.code + "\n";
          }
          
          for ( IRPstruct irp : pb.irps )
          {
            pbDesc += irp + getCondition( irp ) + "\n\n";
          }
        }
        s += pbDesc;
        s += "--------------------\n\n";
        pw.print( s.replaceAll( "\\n", System.getProperty("line.separator" ) ) );
      }
      String s = "Executors shown: " + shownCount + " of " + fullCount + "\n";
//      String s = "Count of executors: " + execCount + "\n";
//      s += "Count of executors without code: " + execNoCodeCount + "\n";
//      s += "Count of executors without SB code: " + execNoSBCodeCount + "\n";
//      s += "Count of executors without SB code and only data changing PB code: " + execNoSBDataChPBCount + "\n";
//      s += "Count of executors without SB code and only data and time changing PB code: " + execNoSBDataTimeChPBCount + "\n";
//      s += "Unlabelled addresses:";
//      Collections.sort( AssemblerItem.unlabelled );
//      for ( int i : AssemblerItem.unlabelled )
//      {
//        s += String.format( " $%02X", i );
//      }
      pw.print( s.replaceAll( "\\n", System.getProperty("line.separator" ) ) );
      pw.close();
      fw.close();
    }
    catch ( IOException e )
    {
      e.printStackTrace();
    }
  }
  
  private String getCondition( IRPstruct irp )
  {
    String condition = "";
    if ( !irp.comments.isEmpty() )
    {
      boolean whenDone = false;
      condition += "  //";
      LinkedHashMap< String, String > cMap = new LinkedHashMap< String, String >();
      for ( String c : irp.comments )
      {
        int pos = c.indexOf( ":1:" );
        {
          if ( pos > 0 )
          {
            String var = c.substring( 0, pos );
            int ndx = 7 - Integer.parseInt( c.substring( pos+3, pos+4 ) );
            String n = c.substring( pos+5, pos+6 );
            String val = cMap.get( var );
            if ( val == null )
            {
              val = "xxxxxxxx";
            }
            val = val.substring( 0, ndx ) + n + val.substring( ndx + 1 );
            cMap.put( var, val );
          }
        }
      }
      if ( cMap.size() > 0 )
      {
        if ( !whenDone )
        {
          condition += " when";
          whenDone = true;
        }
        else
        {
          condition += " and";
        }
        for ( String c : cMap.keySet() )
        {
          condition += " " + c + "=" + cMap.get( c );
        }
      }
      
      for ( String c : irp.comments )
      {
        if ( !c.contains( ":1:" ) )
        {
          if ( c.contains( "=" ) && !whenDone )
          {
            condition += " when";
            whenDone = true;
          }
          else if ( c.startsWith( "when " ) && whenDone )
          {
            c = "and" + c.substring( 4 );
          }    
          condition += " " + c + ";";
        }
      }
    }
    return condition;
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
  private String disassemblePseudocode( int addr, Hex hex, String prefix, boolean[] flags )
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
      TimingStruct ts2 = new TimingStruct( carriers, tbDurations, pf );
      pfNew = pf.clone();  
      
      for ( int k = i; k < j; k++ )
      {
        freqFlags[ 0 ] = false;
        
        AssemblerItem item = itemList.get( k );
        addItemComments( item, carriers, freqFlags, ts2 );
        changesFreq = changesFreq | freqFlags[ 1 ];
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
//      s += "First executor\n";
      analyzeSingleExec( hex.subHex( 0, altExecStart ) );
//      s += "\nSecond executor\n";
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
    e.description = analyzeHeader( hex.subHex( 0,  3 ) );
    int pos = 0;
    pos += 3; // skip header  
    int tbHeader = data[ pos ];
    boolean tbHasSpec = ( tbHeader & 0x80 ) != 0;
    int tbSize = ( tbHeader & 0x3F ) + ( tbHasSpec ? 3 : 1 );
    int tbStart = pos;
    pos += tbSize;
    int dbHeader = data[ pos++ ];
    has2usOff = ( dbHeader & 0x40 ) != 0;
    for ( String str : analyzeTimingBlock( hex.subHex( tbStart, tbSize ), true, 0, 0x40, null ) )
    {
      e.description += str + "\n";
    }
    int carrierSaved = carrier;
    int[] durationsSaved = tbDurations;    
    int dbSize = dbHeader & 0x07;
    hasNativeCode = ( dbHeader & 0x80 ) != 0;    
    boolean hasAltExec = ( dbHeader & 0x20 ) != 0;
    int dbSwitch = !hasAltExec && dbSize > 1 ? hex.get( pos ) : 0;
    pos += dbSize;  // pos points to first (or only) protocol block
    
    if ( completeItemList == null )
    {
      while ( true )
      {
        prb = new ProtocolBlock();
        prb.e = e;
        e.pbList.add( prb );
        int pbOptionsSize = data[ pos ] & 0x0F;
        int pbOffset = pbOptionsSize > 0 ? data[ pos + 1 ] : 0;
        analyzeProtocolBlock( pos, pbOffset > 0 ? hex.subHex( pos, pbOffset ) : hex.subHex( pos ), null );
        if ( pbOffset > 0 )
        {
          pos += pbOffset;
        }
        else
        {
          break;
        }
      }
      return;
    }
    
    int altMask = dbSwitch & 0xFF;
    int altIndex = ( dbSwitch >> 12 ) & 0x0F;
    int altCount = ( dbSwitch >> 8 ) & 0x0F;
    
    int[] rev = new int[ 4 ];
    reverseByte( altMask, rev );
    if ( rev[ 1 ] != rev[ 3 ] - rev[ 2 ] + 1 )
    {
      e.errors.add( "Bits in directive switch mask are not consecutive" );
    }
    
    
//    int shift = 0;
    int maxCount = 1;
    for ( int i = 0; i < rev[ 1 ]; i++ )
    {
      maxCount *= 2;
    }
    if ( altCount >= maxCount )
    {
      String err = "Number of protocol alternates greater than mask permits, so reset from ";
      err += String.format( "%d to %d", altCount, maxCount - 1 );
      e.warnings.add( err );
      altCount = maxCount - 1;
    }
    
//    if ( altMask > 0 )
//    {
//      for ( ; ( ( altMask >> shift ) & 0x01 ) == 0; shift++ ){};
//    }
    
    String irpName = altCount > 0 ? irpLabel( 0xD0 + altIndex ) : "";
    for ( int i = 0; i < maxCount; i++ )
    {
      prb = e.pbList.get( i );
      prb.description = "";
      LinkedList< TimingStruct > altTimings = new LinkedList< TimingStruct >();
      int pbOptionsSize = data[ pos ] & 0x0F;
      int pbOffset = pbOptionsSize > 0 ? data[ pos + 1 ] : 0;
      if ( altCount > 0 )
      {
        prb.condition = irpName + ":" + ( rev[ 1 ] > 1 ? "-" : "" ) + rev[ 1 ] + ( rev[ 2 ] > 0 ? ":" + rev[ 2 ] : "" ) + "=" + i;
        prb.description += "\nProtocol block when " + prb.condition + "\n";
      }
      int pbIndexSaved = pbIndex;
      prb.description += analyzeProtocolBlock( pos, pbOffset > 0 ? hex.subHex( pos, pbOffset ) : hex.subHex( pos ), altTimings );
      while ( !altTimings.isEmpty() )
      {
        TimingStruct ts = altTimings.pop();
        irpStruct = new IRPstruct();
        prb.warnings.clear();
        prb.irpParts11.clear();
        pbIndex = pbIndexSaved;  
        boolean changed = runIREngine( pos, pbOffset > 0 ? hex.subHex( pos, pbOffset ) : hex.subHex( pos ), altTimings, ts );
        if ( changed )
        {
          continue;
        }
        carrier = carrierSaved;
        tbDurations = durationsSaved;
        for ( int k = 0; k < Math.max( prb.irpParts11.size(), 1 ); k++ )
        {         
          IRPstruct struct = irpStruct;
          if ( prb.irpParts11.size() > 0 )
          {
            String irpPart11 = prb.irpParts11.get( k );
            struct = irpStruct.clone();
            // make replacement of string without trailing comma
            struct.irStream = struct.irStream.replaceAll( "irpPart11", irpPart11.substring( 0, irpPart11.length() - 1 ) );
            struct.comments.clear();
            struct.comments.add( prb.sigSelector + "=" + k );
          }
          
          if ( testIRPstruct( struct ) )
          {
            if ( !prb.irps.contains( struct ) )
            {
              prb.irps.add( struct );
            }
          }
          else
          {
            if ( prb.irps.contains( struct ) )
            {
              prb.irps.remove( struct );
            }
          }
        }
      }
      if ( pbOffset > 0 )
      {
        pos += pbOffset;
        if ( i > altCount - 1 )
        {
          e.errors.add( "More than specified number " + ( altCount + 1 ) + "  of protocol blocks" );
        }
      }
      else
      {
        if ( i < altCount )
        {
          e.errors.add( "Fewer than specified number " + ( altCount + 1 ) + " of protocol blocks" );
        }
        break;
      }
    }
  }

  private String analyzeHeader( Hex hex )
  {
    String s = "";
    short[] data = hex.getData();
    int on = data[ 0 ];
    int off = data[ 1 ];
    if ( on == 0 && off == 0 )
    {
      on = 6;
      off = 6;
      carrier = on + off;
      s += "Carrier is unmodulated\n";
      irpStruct.generalSpec = "{0k,";
    }
    else
    {
      on++;
      off++;
      carrier = on + off;
      s += String.format( "%.2fkHz, duty cycle %.1f", 6000.0 / carrier, ( 100.0 * on )/ carrier ) + "%\n";
      irpStruct.generalSpec = String.format( "{%.1fk,", 6000.0 / carrier );
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
    return s;
  }
  
  private int calculateUnit( int provUnit, int tolerance )
  {
    // add data signals to tbUsed and mask out lead-outs and alt carrier timing
    int tbFlags = ( tbUsed | 0x03 ) & 0x6B;
    int[] sizes = new int[]{ 2, 2, 1, 2, 1, 2, 2, 1 };
    int[] usDurations = new int[ tbDurations.length ];

    for ( int i = 0, n = 0; i < 7; i++ )
    {
      // convert durations to microseconds, omitting item 8, alt carrier timing
      for ( int j = 0; j < tbLengths[ i ] * sizes[ i ] && n < tbDurations.length; j++ )
      {
        int mult = ( j & 1 ) == 0 && sizes[ i ] == 2 || !has2usOff ? carrier : 12;
        int val = ( tbDurations[ n ] * mult + 3 ) / 6;
        usDurations[ n++ ] = val;
      }
    }
    
    // Set limits on test range for unit as provUnit +/- (10% plus 3).
    // The extra 1 (was 3) allows for very small units (e.g. pid-002A, where 
    // unit is 8u) as a small change in unit is a large percentage change.
    int nmax = ( int )Math.round( 1.10 * provUnit + 1 );
    int nmin = ( int )Math.round( 0.90 * provUnit - 1 );
    
    double fracmax = 0, fracminmax = 1.0;
    for ( int testUnit = nmin; testUnit <= nmax; testUnit++ )
    {
      fracmax = 0;     
      for ( int i = 0, n = 0; i < 7; i++ )
      {
        for ( int j = 0; j < tbLengths[ i ] * sizes[ i ] && n < usDurations.length; j++ )
        {
          double val = usDurations[ n++ ];
          if ( val > 0 && ( ( tbFlags >> i ) & 1 ) == 1 )
          {
            double ratio = val / testUnit;
            double fracpart = Math.abs( ratio - Math.rint( ratio ) );
            fracmax = Math.max( fracmax, fracpart/ratio );           
          }
        }
      }     
         
      if ( fracmax < fracminmax ) 
      {
        fracminmax = fracmax;
        unit = testUnit;
      }
    }
    
    int no = 0;   // number out of tolerance range
    for ( int i = 0, n = 0; i < 7; i++ )
    {
      for ( int j = 0; j < tbLengths[ i ] * sizes[ i ] && n < usDurations.length; j++ )
      {
        double val = usDurations[ n++ ];
        if ( val > 0 && ( ( tbFlags >> i ) & 1 ) == 1 )
        {
          double ratio = val / unit;
          double fracpart =  Math.abs( ratio - Math.rint( ratio ) );
          if ( fracpart/ratio > tolerance / 100.0 )
          {
            no++;
          }
        }
      }
    }
    return no;
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
    List< String > warn = new ArrayList< String >();
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
      
//      int tbMax = Math.min( 2 * ( tbLengths[ 0 ] + tbLengths[ 1 ] ) , tbDurations.length );
      int min = 10000;
      int[] sizes = new int[]{ 2, 2, 1, 2, 1, 2, 2, 1 };
      int tbFlags = ( tbUsed | 0x03 ) & 0x6B; 
      for ( int i = 0, n = 0; i < 7; i++ )
      {
        // convert durations to microseconds, omitting item 8, alt carrier timing
        for ( int j = 0; j < tbLengths[ i ] * sizes[ i ] && n < tbDurations.length; j++ )
        {
          int mult = ( j & 1 ) == 0 && sizes[ i ] == 2 || !has2usOff ? carrier : 12;
          int val = ( tbDurations[ n++ ] * mult + 3 ) / 6;
          if ( val > 0 && ( ( tbFlags >> i ) & 1 ) == 1 )
          {
            // timing item is used and is not a lead-out
            min = Math.min( min, val );
          }
        }
      }
      
      if ( tbUsed != 0 )
      {
        // tbUsed == 0 should only occur in preliminary stages whose results are discarded
        int nonint = calculateUnit(min, 5 );    
        if (nonint > 0) 
        {
          nonint = calculateUnit(min/2, 5 );
        }
        if (nonint > 0) 
        {
          nonint = calculateUnit(min/3, 5 );
        }   
        if (nonint > 0) 
        {
          // Revert to original calculation if greater accuracy not achieved.
          nonint = calculateUnit(min, 5 );
        }
        if ( nonint > 0 )
        {
          // revert to values in us in this case
          unit = 1;
        }
      }
      else if ( tbUsed == 0 )
      {
        unit = min;
      }

      
//      if ( prb != null && worst > 0.05 )
//      {
//        prb.warnings.add( "Max irp timing error " + String.format( "%.2f", 100*worst ) +"%\n" );
//      }
      irpStruct.generalSpec += unit == 1 ? "}" : (int)unit + "}";
      irpStruct.unit = ( int )unit;
    }

    List< Integer > irpVals = heads || ( start == 0 && end == 0x40 ) ? new ArrayList< Integer >() : null;
    int[] limits = new int[]{ 0, 0 };
    int codeSpec = initialCodeSpec == -1 ? 0 : initialCodeSpec;
    int codeSelector = codeSpec & 0x0F;
    codeSelector = codeSelector > 5 ? 0 : codeSelector;
    if ( ( codeSpec & 0x10 ) != 0 )
    {
      // Set altCarrier
      getTimingItem( 7, limits, null, warn );
      warn.clear();
    }
    else
    {
      altCarrier = 0;
    }
    
    if ( codeSelector == 1 )
    {
      int size = ( tbLengths[ 0 ] + tbLengths[ 1 ] ) > 7 ? 2 : 1;
      int mult = has2usOff ? 12 : carrier;
      int n = 0;

      if ( heads )
      { 
        irpStruct.bitSpec = "<";
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
              irpStruct.bitSpec += getIRPduration( val, warn ) + ",";
              if ( prb != null )
              {
                prb.warnings.addAll( warn );
              }
              warn.clear();
            }
          }
          val = ( tbDurations[ n++ ]*mult + 3 )/6;  // convert carrier cycles to us
          if ( val > 0 )
          {
            str += str.isEmpty() ? "" : ",";
            str += "-" + val;
            if ( heads )
            {
              irpStruct.bitSpec += - getIRPduration( val, warn ) + ",";
              if ( prb != null )
              {
                prb.warnings.addAll( warn );
              }
              warn.clear();
            }
          }
        }
        if ( heads )
        {
          irpStruct.bitSpec = irpStruct.bitSpec.substring( 0, irpStruct.bitSpec.length() - 1 ) + "|";
        }
        String range = String.format( "(PD%02X-PD%02X) ", 2*i*size, 2*(i+1)*size - 1 );
        if ( start <= 2*i*size && 2*i*size <= end )
        {
          list.add( "Bursts for bit-pair " + ( new String[]{ "00", "01", "10", "11" } )[ i ] + " (us): " + range + str );
        }
      }
      if ( heads )
      {
        irpStruct.bitSpec = irpStruct.bitSpec.substring( 0, irpStruct.bitSpec.length() - 1 ) + ">";
      }
    }
    else
    {
      if ( heads && codeSelector == 5 )
      {
        list.add( "Data uses base 16 encoding, 4-bit group with value n being converted\n"
            + "  for transmission to a 1 followed by n 0's " );
        irpStruct.base16 = true;
      }
      
      str = getTimingItem( 0, limits, irpVals, warn );
      if ( prb != null )
      {
        prb.warnings.addAll( warn );
      }
      warn.clear();
      String oneStr = "";
      if ( !str.isEmpty() && start <= limits[ 0 ] && limits[ 0 ] <= end )
      {
        list.add( "1-bursts (us): " + str );
        if ( irpVals != null )
        {
          for ( int n : irpVals )
          {
            oneStr += n + ",";
          }
          irpParts[ 2 ] = irpVals.get( 0 ) + ",";
        }
      }
      str = getTimingItem( 1, limits, irpVals, warn );
      if ( prb != null )
      {
        prb.warnings.addAll( warn );
      }
      warn.clear();
      if ( !str.isEmpty() && start <= limits[ 0 ] && limits[ 0 ] <= end )
      {
        list.add( "0-bursts (us): " + str );
        if ( heads && ( codeSpec & 0x20 ) != 0 )
        {
          list.add( "Only first burst-pair of 0-burst is sent if an odd number of bits precede it" );
        }
        if ( heads && irpVals != null )
        {
          irpStruct.bitSpec = "<";
          for ( int n : irpVals )
          {
            irpStruct.bitSpec += n + ",";
          }
          irpStruct.bitSpec = irpStruct.bitSpec.substring( 0, irpStruct.bitSpec.length() -1 ) + "|" + oneStr.substring( 0, oneStr.length() -1 ) + ">";
        }
      }
    }

    str = getTimingItem( 2, limits, irpVals, warn );
    if ( !str.isEmpty() && ( tbUsed & 0x04 ) != 0 && start <= limits[ 0 ] && limits[ 0 ] <= end )
    {
      list.add( "Lead-out (us): " + str );
      if ( irpVals != null )
      {
        int value = irpVals.get( 0 );
        if ( value > 0 )
        {
          // value is in microseconds
          if ( value < 50000 )
          {
            irpParts[ 1 ] = ( - value ) + "u,";
          }
          else
          {
            value = ( value + 500 ) / 1000;
            irpParts[ 1 ] = ( - value ) + "m,";
          }
        }
        else
        {
          irpParts[ 1 ] = irpVals.get( 0 ) + ",";
          if ( prb != null )
          {
            prb.warnings.addAll( warn );
          }
        }
      }
    }
    warn.clear();
    
    str = getTimingItem( 3, limits, irpVals, warn );
    if ( !str.isEmpty() && ( tbUsed & 0x08 ) != 0 && start <= limits[ 0 ] && limits[ 0 ] <= end )
    {
      list.add( "Lead-in (us): " + str );
      if ( irpVals != null )
      {
        irpParts[ 0 ] = "";
        if ( irpVals.size() > 0 )
        {
          for ( int n : irpVals )
          {
            irpParts[ 0 ] += n + ",";
          }
          irpParts[ 9 ] = irpVals.get( 0 ) + "," + ( irpVals.size() > 1 ? ( irpVals.get( 1 ) / 2 ) + "," : "" );
        }
        if ( prb != null )
        {
          prb.warnings.addAll( warn );
        }
      }
    }
    warn.clear();
    
    str = getTimingItem( 4, limits, irpVals, warn );
    if ( !str.isEmpty() && ( tbUsed & 0x10 ) != 0 && start <= limits[ 0 ] && limits[ 0 ] <= end )
    {
      list.add( "Alternate lead-out (us): " + str );

      if ( irpVals != null )
      {
        int value = irpVals.get( 0 );
        if ( value > 0 )
        {
          // value is in microseconds
          if ( value < 50000 )
          {
            irpParts[ 6 ] = ( - value ) + "u,";
          }
          else
          {
            value = ( value + 500 ) / 1000;
            irpParts[ 6 ] = ( - value ) + "m,";
          }
        }
        else
        {
          irpParts[ 6 ] = irpVals.get( 0 ) + ",";
          if ( prb != null )
          {
            prb.warnings.addAll( warn );
          }
        }
      }
    }
    warn.clear();
    str = getTimingItem( 5, limits, irpVals, warn );
    if ( !str.isEmpty() && ( tbUsed & 0x20 ) != 0 && start <= limits[ 0 ] && limits[ 0 ] <= end )
    {
      list.add( "Alternate lead-in (us): " + str );
      if ( irpVals != null )
      {
        irpParts[ 5 ] = "";
        for ( int n : irpVals )
        {
          irpParts[ 5 ] += n + ",";
        }
        if ( prb != null )
        {
          prb.warnings.addAll( warn );
        }
      }
    }
    warn.clear();
    str = getTimingItem( 6, limits, irpVals, warn );
    if ( !str.isEmpty() && ( tbUsed & 0x40 ) != 0 && start <= limits[ 0 ] && limits[ 0 ] <= end )
    {
      list.add( "Mid-frame burst (us): " + str );
      // IRP contribution is handled in analyzeTXBytes
    }
    warn.clear();
    if ( ( codeSpec & 0x10 ) != 0 )
    {
      str = getTimingItem( 7, limits, null, warn );
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
  private int getIRPduration( int usDuration, List< String > warn )
  {
    double ratio = usDuration / unit;
    double delta = Math.abs( ratio - Math.round( ratio ) );
    
    if ( delta > 0.05 * ratio )
    {
      String w = String.format( "*** Diff %.2f", 100*delta/ratio ) + "%" + String.format(" in converting %d with unit %d", usDuration, (int)unit );
      warn.add( w );
    }
    return ( int )Math.round( ratio );
  }
  
  private String getTimingItem( int n, int[] limits, List< Integer > irpVals, List< String > warn )
  {
    int itemCarrier = carrier;
    if ( n == 1 && altCarrier != 0 )
    {
      itemCarrier = altCarrier;
    }
    int[] durations = tbDurations;   
    return getTimingItem( n, itemCarrier, durations, limits, irpVals, warn );
  }
  
  private String getTimingItem( int n, int itemCarrier, int[] durations, int[] limits, List< Integer > irpVals, List< String > warn )
  {
    if ( irpVals != null )
    {
      irpVals.clear();
    }
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
            irpVals.add( getIRPduration( val, warn ) );
          }
        }
        val = ( durations[ pos++ ]*mult + 3 )/6;  // convert carrier cycles to us
        if ( val > 0 )
        {
          str += str.isEmpty() ? "" : ",";
          str += "-" + val;
          if ( irpVals != null )
          {
            irpVals.add( - getIRPduration( val, warn ) );
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
              int wSize = warn.size();
              int irpDur = getIRPduration( val, warn );
              if ( irpDur <= 50 && warn.size() == wSize )
              {
                irpVals.add( - irpDur );
              }
              else
              {
                irpVals.add( val );  // positive to indicate it is in microseconds
              }
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
    for ( int i = node.start; i < limit; i++ )
    {
      AssemblerItem item = completeItemList.get( i );
      int type = item.get_Type();
      if ( !validTypes.contains( type ) )
      {
        prb.errors.add( "Irregular instruction  at no. " + i + "/" + limit );
      }
      if ( item.getLabel() != null && loopIndex.get( item.getLabel() ) != null )
      {
        // item is start of loop
        if ( i > node.start )
        {
          // branch ends at start of loop
          int[] branch = new int[ 3 ];
          branch[ 0 ] = i;
          branch[ 1 ] = i;;
          branch[ 2 ] = -1;
          node.branch = branch;
          node.branchType = 13;  // ends at loop
          return node;
        }
        else
        {
          // branch surrounds loop
          int loopCtrl = loopIndex.get( item.getLabel() );
          int[] branch = new int[ 3 ];
          branch[ 0 ] = i;
          branch[ 1 ] = i + 1;
          branch[ 2 ] = loopIndex.get( item.getLabel() ) + 1;
          node.branch = branch;
          AssemblerItem ctrlItem = completeItemList.get( loopCtrl );
          String ctrlVar = irpLabel( ctrlItem.getHex().getData()[ 2 ] );
          node.branchVar = ctrlVar;
          node.branchType = 11;  // surrounds loop
          return node;
        }
      }
      
      if ( item.getOperation().equals( "DBNZ" ) )
      {
        String label = labels.get( item.getAddress() + item.getHex().getData()[ 1 ] - 0x100 );
        String ctrlVar = irpLabel( item.getHex().getData()[ 2 ] );
        int[] branch = new int[ 3 ];
        branch[ 0 ] = i;
        branch[ 1 ] = i + 1;
        branch[ 2 ] = labelIndex.get( label ) != null ? labelIndex.get( label ) : -1;
        node.branch = branch;
        node.branchVar = ctrlVar;
        node.branchType = 12;  // body of DBNZ loop
        return node;
      }
      
      if ( item.getOperation().equals( "BSR" ) )
      {
        node.branch = new int[]{i,i+1,-1};
        node.branchType = 15;
        return node;
      }
      
      if ( item.getOperation().equals( "RTS" ) )
      {
        node.branch = new int[]{i,-1,-1};
        node.branchType = 14;
        return node;
      }
      
      if ( type == 2 || type == 7 || type == 8 || type == 13 || type == 16 )
      {
        // type 2 = branch on test of data, type 7 = branch on test of first frame
        // type 8 = branch on true/false test, type 13 = branch on test of key held
        // type 16 = branch on test of repeat required
        int[] branch = new int[ 3 ];
        branch[ 0 ] = i;
        String args = item.getArgumentText();
        String[] argArray = new String[ 4 ];
        StringTokenizer st = new StringTokenizer( args, "," );
        int index = 0;
        while ( st.hasMoreTokens() )
        {
          argArray[ index++ ] = st.nextToken().trim();
        }
        String destLabel = argArray[ 1 ] == null ? argArray[ 0 ] : argArray[ 1 ];
        int destIndex = 0;
        if ( argArray[ 1 ] != null )
        {
          // conditional branch instruction
          // make branch[ 1 ] hold destination on zero, branch[ 2 ] on nonzero
          //   or branch[ 1 ] hold destination on true, branch[ 2 ] on false
          // set default values for BRA Z, ...  or BRA T, ...
          int yes = 1;  // index to store destination on branch (continue at specified instruction )
          int no = 2;   // index to store destination on no branch (continue at next instruction)          
          if ( argArray[ 0 ].equals( "NZ" ) || argArray[ 0 ].equals( "F" ) )
          {
            yes = 2;
            no = 1;
          }
          
          destIndex = labelIndex.get( destLabel );
          branch[ no ] = i + 1;
          branch[ yes ] = destIndex;

          if ( type == 2 )
          {
            String v = irpLabel( argArray[ 2 ] ) != null ? irpLabel( argArray[ 2 ] ) : argArray[ 2 ];
            int a = Integer.parseInt( argArray[3].substring( 2 ), 16 );
            int[] rev = new int[ 4 ];
            reverseByte( a, rev );
            if ( rev[1] == 1 )
            {
              node.branchType = rev[2];  // this bit zero/nonzero
              node.branchVar = v;
            }
            else
            {
              node.branchType = 0x100 + rev[0];  // value masked with rev[0] zero/nonzero
              node.branchVar = v;
            }
          }
          else if ( type == 8 )
          {
            String cond = argArray[ 2 ];
            String opComment = proc.getLblComments().get(  cond );
            if ( opComment != null && opComment.startsWith( "Test if" ) )
            {
              node.branchType = 8; // true/false
              node.branchVar = opComment.substring( 8 );
            }
          }
          else
          {
            node.branchType = 9;  // system branch
          }

          node.branch = branch;
          return node;
        }
        else
        {
          // unconditional branch instruction
          destLabel = argArray[ 0 ];  //args.substring( lStart );
          destIndex = labelIndex.get( destLabel );
          branch[ 1 ] = destIndex;
          branch[ 2 ] = -1;
          node.branch = branch;
          node.branchType = 10;  // unconditional
          return node;
        }
      }
      else if ( /*i > node.start &&*/ item.getLabel() != null 
          && labelIndex.get( item.getLabel() ) != null
          && labelIndex.get( item.getLabel() )!= treeRoot
          && functionIndex.get( item.getLabel() ) != null )
      {
        // label begins an in-line function
        node.branch = new int[]{i,-1,-1};
        node.branchType = 16;
        return node;
      }
      else if ( i == limit - 1 )
      {
        int[] branch = new int[ 3 ];
        branch[ 0 ] = i;
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
 * altTiming is empty.  It sets initialCodeSpec from PB options.  fullItemList
 * gets AssemblerItems for PB and each SB block with code, separated by an item consisting
 * only of label of form PBn or SBn.  At this stage the label entries in the items may be
 * wrong as labelAddresses is added to for each code block, so cross-referencing labels will
 * be missing.  IT LOOKS AS IF ALTTIMINGS CAN BE NULL ON THIS INITIAL CALL, AS IT IS NOT
 * REFERENCED.
 * 
 * On second call, completeItemList is fullItemList from first call and fullItemList == null.
 * Still altTimings is empty.  The label entries in the items in completeItemList
 * are updated to final form and labelIndex is constructed to map labels to position in
 * completeItemList.  Paths through PB code block are determined and for each path, a
 * TimingStruct is added to altTimings with pbPath set to path value.  Then for each SB
 * code block, its paths are determined and each altTimings entry is duplicated for each
 * SB path with an sbPaths entry added.  If there was no PB code then it will have pbPath=0.
 * If no SB code then sbPaths will be empty.
 * 
 */
  private String analyzeProtocolBlock( int addr, Hex hex, LinkedList< TimingStruct > altTimings )
  {
    String s = "";
    int pos = 0;
    short[] data = hex.getData();
    int pbHeader = data[ pos++ ];
    int pbOptSize = pbHeader & 0x0F;
    int pbSwitch = pbOptSize > 2 ? hex.get( pos + 1 ) : 0;
    int pbSwitchMask = pbSwitch & 0xFF;
    int pbSwitchIndex = ( pbSwitch >> 12 ) & 0x0F;
    int pbSwitchSize = ( pbSwitch >> 8 ) & 0x0F;
    int codeSpec = pbOptSize > 3 ? data[ pos + 3 ] : 0;
    int toggle = pbOptSize > 5 ? hex.get( pos + 4 ) : 0;
    int codeSelector = codeSpec & 0x0F;

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
      prb.errors.add( "Uses asynchronous coding, not yet supported" );
    }

    pos += pbOptSize;   // pos points to code block if present, else signal block
    boolean pbHasCode = ( pbHeader & 0x80 ) != 0;
    if ( !pbHasCode )
    {
      if ( completeItemList != null )
      {
        altTimings.add( new TimingStruct() );
      }
    }
    else
    {
      pbIndex++;
      boolean[] flags = new boolean[ 20 ];
      prb.hasPBcode = true;
      s += "\nProtocol block code (run on first frame only, after PF bytes read):\n";
      int cbSize = data[ pos++ ];
      s += disassemblePseudocode( addr + pos, hex.subHex( pos, cbSize ), "", flags );
      if ( fullItemList != null )
      {
        AssemblerItem item = new AssemblerItem();
        item.setLabel( "PB" + pbIndex );
        fullItemList.add( item );
        fullItemList.addAll( itemList );
      }
      else if ( completeItemList != null )
      {
        for ( int i = 0; i < completeItemList.size(); i++ )
        {
          AssemblerItem ai = completeItemList.get( i );
          String label = ai.getLabel();
          if ( !label.isEmpty() )
          {
            labelIndex.put( label, i );
          }
          if ( ai.getOperation().equals( "DBNZ" )
              || ai.getHex() != null && ai.getHex().getData()[ 0 ] == 0x55 && ai.getHex().getData()[ 1 ] >= 0x80 )
          {
            // first case is for-next loop, second is BRA NZ to earlier address, forming a while-do loop           
            label = labels.get( ai.getAddress() + ai.getHex().getData()[ 1 ] - 0x100 );
            loopIndex.put( label, i );
          }
          else if ( ai.getOperation().equals( "BSR" ) )
          {
            int offset = ai.getHex().getData()[ 1 ];
            label = labels.get( ai.getAddress() + offset - ( offset >= 128 ? 0x100 : 0 ) );

            if ( functionIndex.get( label ) == null )
            {
              String name = "Proc" + functionIndex.size();
              Function fn = new Function( name );
              functionIndex.put( label, fn );
            }
          }
        }

        int start = labelIndex.get( "PB" + pbIndex );
        List< Integer > paths = createCodePaths( start, 0 );
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
        
        String interp = interpretPB( start );
        if ( !interp.isEmpty() )
        {
          s += "\n" + interp + "\n";
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
//      dataChangeOnly = true;
//      dataTimingChangeOnly = true;
//      for ( int i = 0; i < flags.length; i++ )
//      {
//        if ( flags[ i ] && i != 1 && i != 2 )
//        {
//          dataChangeOnly = false;
//          if ( i != 3 )
//          {
//            dataTimingChangeOnly = false;
//            prb.pbHandled = false;
//          }
//        }
//      }
//      if ( dataChangeOnly )
//      {
//        s += "\nCode changes data only\n";
//      }
//      else if ( dataTimingChangeOnly )
//      {
//        s += "\nCode changes data and timing only\n";
//      }
    }
    
    int[] togData = null;
    if ( toggle > 0 )
    {
      togData = new int[ 6 ];
      s += pbHasCode ? "After protocol block code is run, apply toggle:\n  " : "Toggle: ";
      s += analyzeToggle( toggle, togData );
    }
    
    // pos now points to signal block
    choices = new boolean[ 30 ];
    Arrays.fill( choices, false );
    blockCount = 0;
    maxBlocks = 1;
    brackets = 0;
    boolean more = true;
    sbIndex = 0;
    firstFrame = true;
       
    if ( pbSwitchSize > 0 )
    {
      int[] rev = new int[ 4 ];
      reverseByte( pbSwitchMask, rev );
      if ( rev[ 1 ] != rev[ 3 ] - rev[ 2 ] + 1 )
      {
        prb.errors.add( "Bits in protocol switch mask are not consecutive" );
      }
      String irpName = irpLabel( 0xD0 + pbSwitchIndex );
      prb.sigSelector = irpName + ":" + ( rev[ 1 ] > 1 ? "-" : "" ) + rev[ 1 ] + ( rev[ 2 ] > 0 ? ":" + rev[ 2 ] : "" );
    }

    while ( more )
    {
      blockCount++;  
      Arrays.fill( choices, false );;
      choices[ 15 ] = toggle > 0;
      int sigPtr = pos;
      if ( pos >= data.length )
      {
        s += "*** Signal block missing ***\n";
        return s;
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
          txStr += " (when " + prb.sigSelector + "=" + i + ")";
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
        txStr += translateTXBytes( txBytes, prb.sbVars );
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
        return s;
      }

      more = sbHasAlternates;
      if ( more )
      {
        continue;
      }

      String codeStr = "";
      if ( sbHasCode )
      {
        sbIndex++;
        prb.hasSBcode = true;
        boolean[] flags = new boolean[ 30 ];
        codeStr += "\n  Signal block code";
        if ( txCount > 0 )
        { 
            codeStr += " (run " + ( sbCodeBeforeTX ? "before" : "after") + " data translation)";
        }
        codeStr += ":\n";
        int cbSize = data[ pos++ ];
        codeStr += disassemblePseudocode( addr + pos, hex.subHex( pos, cbSize ), "  ", flags );

        if (  fullItemList != null )
        {
          AssemblerItem item = new AssemblerItem();
          item.setLabel( "SB" + sbIndex );
          fullItemList.add( item );
          fullItemList.addAll( itemList );
          List< String > destList = new ArrayList< String >();
          List< String > srcList = new ArrayList< String >();
          for ( AssemblerItem it : itemList )
          {
            if ( !getReferenced( it, destList, srcList, true ) )
            {
              prb.errors.add( "Indeterminate source in SB code" );
            }
          }
          for ( String var : srcList )
          {
            if ( !prb.sbVars.contains( var ) )
            {
              prb.sbVars.add( var );
            }
          }
        }
        else if ( completeItemList != null )
        {
          for ( int i = 0; i < completeItemList.size(); i++ )
          {
            AssemblerItem ai = completeItemList.get( i );
            String label = ai.getLabel();
            if ( !label.isEmpty() /* && ( label.startsWith( "PB" ) || label.startsWith( "SB" ) )*/ )
            {
              labelIndex.put(  label, i );
            }
            if ( ai.getOperation().equals( "DBNZ" )
                || ai.getHex() != null && ai.getHex().getData()[ 0 ] == 0x55 && ai.getHex().getData()[ 1 ] >= 0x80 )
            {
              // first case is for-next loop, second is BRA NZ to earlier address, forming a while-do loop           
              label = labels.get( ai.getAddress() + ai.getHex().getData()[ 1 ] - 0x100 );
              loopIndex.put( label, i );
            }
            else if ( ai.getOperation().equals( "BSR" ) )
            {
              int offset = ai.getHex().getData()[ 1 ];
              label = labels.get( ai.getAddress() + offset - ( offset >= 128 ? 0x100 : 0 ) );

              if ( functionIndex.get( label ) == null )
              {
                String name = "Proc" + functionIndex.size();
                Function fn = new Function( name );
                functionIndex.put( label, fn );
              }
            }
          }
          
          int start = labelIndex.get( "SB" + sbIndex );
          List< Integer > paths = createCodePaths( start, 0 );
          if ( !paths.isEmpty() )
          {
            altTimings.add( null );
            while ( altTimings.peek() != null )
            {
              TimingStruct ts2 = altTimings.pop();
              for ( int p : paths )
              {
                TimingStruct ts3 = ts2.clone();
                ts3.sbPaths.put( sbIndex, new int[]{p,p,p} );
                altTimings.add( ts3 );
              }
            }
            altTimings.pop();
          }
        }
        pos += cbSize;
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
    }
    
    if ( hasNativeCode )
    {
      prb.errors.add( "Has native code block, not supported" );
      s += "Native code block (run after IR sent):\n";
      pos += ( pos & 1 ) == 1 ? 1 : 0;  // round to word boundary
      Hex nCode = hex.subHex( pos );
      s += nCode.toString() + "\n";
      pos += nCode.length();
    }
    if ( ( completeItemList == null || choices[ 14 ] || choices[ 19 ] ) && pos != data.length )
    {
      s += "**** Parsing error ****\n";
    }
    return s;
  }
  
  private int[] interpretToggle( int toggle )
  {
    int[] togData = null;
    if ( toggle > 0 )
    {
      togData = new int[ 6 ];
      analyzeToggle( toggle, togData );
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
        prb.errors.add( "Uses unsupported toggle type" );
      }
    }
    return togData;
  }
  
  private boolean updateAltTimings( LinkedList< TimingStruct > altTimings, TimingStruct ts )
  {
    boolean changed = false;
    int start = labelIndex.get( "SB" + sbIndex );
    int[] sbPath = null;
    if ( ( sbPath = ts.sbPaths.get( sbIndex ) ) != null )
    {
      int p = sbPath[ 0 ];
      List< Integer > list1 = new ArrayList< Integer >();
      List< Integer > list2 = new ArrayList< Integer >();
      int changeType = createPathSequence( start, p, 1, list1 );
      if ( changeType > 0 )
      {
        changed = true;
        createPathSequence( start, p, 2, list2 );
        for ( int i : list1 )
        {
          for ( int j : list2 )
          {
            TimingStruct ts3 = ts.clone();
            ts3.sbPaths.put( sbIndex, new int[]{i, changeType==1 ? i : j, j} );
            altTimings.add( ts3 );
          }
        }
      }
      else if ( sbPath[1] != sbPath[0] )
      {
        p = sbPath[ 1 ];
        list1.clear();
        list2.clear();
        changeType = createPathSequence( start, p, 1, list1 );
        if ( changeType == 1 )
        {
          changed = true;
          createPathSequence( start, p, 2, list2 );
          for ( int i : list1 )
          {
            for ( int j : list2 )
            {
              TimingStruct ts3 = ts.clone();
              ts3.sbPaths.put( sbIndex, new int[]{sbPath[ 0 ], i, j} );
              altTimings.add( ts3 );
            }
          }
        }
      } 
    }
    return changed;
  }
  
  private boolean runIREngine( int addr, Hex hex, LinkedList< TimingStruct > altTimings, TimingStruct ts )
  {
    boolean changed = false;
    int pos = 0;
    short[] data = hex.getData();
    int pbHeader = data[ pos++ ];
    int pbOptSize = pbHeader & 0x0F;
    int pbSwitchSize = pbOptSize > 2 ? data[ pos + 1 ] & 0x0F : 0;
    int toggle = pbOptSize > 5 ? hex.get( pos + 4 ) : 0;
    int[] togData = interpretToggle( toggle );
    pos += pbOptSize;
    
    // pos now points to code block if present, else signal block
    boolean pbHasCode = ( pbHeader & 0x80 ) != 0;
    if ( pbHasCode )
    {
      pbIndex++;
      int cbSize = data[ pos++ ];
      pos += cbSize;
    }

    // pos now points to signal block
    choices = new boolean[ 30 ];
    Arrays.fill( choices, false );
    blockCount = 0;
    maxBlocks = 1;
    brackets = 0;
    boolean more = true;
    sbIndex = 0;
    firstFrame = true;
    irpParts[ 22 ] = "";
    choices[ 22 ] = false;
    
    while ( more )
    {
      blockCount++;
      boolean choices22 = choices[ 22 ];
      Arrays.fill( choices, false );;
      choices[ 15 ] = toggle > 0;
      choices[ 22 ] = choices22;
      int sigPtr = pos;
      if ( pos >= data.length )
      {
        return false;
      }
      Arrays.fill( pf, 0 );
      pf[ 0 ] = data[ pos++ ];
      int formatLen = pf[ 0 ] & 0x07;
      pos += formatLen;
      int txPos = pos;
      
      for ( int i = 1; i <= formatLen; i++ )
      {
        pf[ i ] = data[ sigPtr + i ];
      }
      boolean sbHasAlternates = ( pf[ 5 ] & 0xF0 ) > 0;
      boolean sbHasCode = ( pf[ 0 ] & 0x80 ) != 0;
      choices[ 20 ] = ( pf[ 6 ] & 0x80 ) != 0;
//      boolean sbCodeBeforeTX = ( pf[ 6 ] & 0x80 ) != 0;
      minRpts = pf[ 3 ] & 0x3F; 
      int txCount = pf[ 2 ] & 0x1F;
      
      pfNew = pf.clone(); 
      int[][] pfDescs = new int[][]{ {-1,1,6}, {0x80,4,0 }, {0x08,1,3}, {0x04,1,2},
          {-1,0,6}, {0x3F,3,0}, {-1,1,4}, {0xE0,2,5}, {0x80,3,7}, {0xFF,5,0} };
      for ( int[] pfDesc : pfDescs )
      {
        if ( pfDesc[ 0 ] < 0 || ( pf[ pfDesc[ 1 ] ] & pfDesc[ 0 ]) > 0 )
        {
          getPFdescription( pfDesc[ 1 ] , pfDesc[ 2 ], choices );
        }
      }
      
      // When choices[ 20 ]==false, i.e. SB code processed AFTER signal spec applied
      // to fixed and variable bytes, TX bytes (i.e. signal spec) must be translated 
      // now, before SB code processed.  Note, however, that SB code can only be present
      // when sbHasAlternates==false and pbSwitchSize==0.  Translation of alternate
      // signal specs when pbSwitchSize>0 takes place later.
      if ( !choices[ 20 ] )
      {
        Hex txBytes = hex.subHex( txPos, txCount );
        translateTXBytes( txBytes, null );
      }

      for ( int i = 0; i <= pbSwitchSize; i++ )
      {
        pos += txCount;
        if ( i < pbSwitchSize )
        {
          txCount = data[ pos++ ];
        }
      }

      more = sbHasAlternates;
      if ( more )
      {
        String stream = makeIRStream();
        irpStruct.irStream += stream;
        e.errors.add( "Support for alternate signal blocks not tested" );
        continue;
      }

      if ( sbHasCode )
      {
        sbIndex++;
        int cbSize = data[ pos++ ];
        pos += cbSize;
        // TimingStructs already created for each PB and SB path in analyzeProtocolBlock()
        // but SB paths allow a value 3, meaning both branches are to be followed (other
        // values are 0 = none, 1=left branch, 2=right branch.  Now expand the "both" entries.
        changed = updateAltTimings( altTimings, ts );
      }

      if ( !changed )
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
          if ( pp[ 0 ] != pp[ 1 ] || pp[ 1 ] != pp[ 2 ] )
          {
            paths.add( pp[ 1 ] );
          }
          if ( pp[ 1 ] != pp[ 2 ] )
          {
            paths.add( pp[ 2 ] );
          }
        }

        int[] carriers = new int[]{ carrier, has2usOff ? 12 : carrier, altCarrier };
        TimingStruct ts3 = ts != null ? ts.clone() : new TimingStruct();
        ts3.carriers = carriers;
        ts3.pf = pf.clone();
        ts3.durations = Arrays.copyOf( tbDurations, 0x40 );
        irpParts[ 17 ] = "";
        choices[ 17 ] = false;
        if ( firstFrame )
        {
          // the PB code is run on first frame only       
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
                if ( !isAssignmentItem( item ) )
                {
                  addItemComments( item, carriers, freqFlags, ts3 );
                }
              }
              
              int br = p3&3;
              String comment = null;
              if ( ( br == 1 || br == 2 ) && ( comment = n.getComments( br - 1, "when " ) ) != null
                  && ( comment.contains( "=" ) || n.branchType == 8 ) 
                  && b[0] >= prb.startTiming && b[0] <= prb.lastTiming )
              {
                ts3.pbCondition = comment;
                irpStruct.comments.add( comment );
              }
              n = p3 != 0 ? nodeList.get( b[br] ) : null;
              p3 >>= 2;
            }
          }
          carrier = ts3.carriers[ 0 ];
          irpStruct.generalSpec = e.hex.get( 0 ) == 0 ? "{0k," : String.format( "{%.1fk,", 6000.0 / carrier );
          analyzeTimingBlock( null, true, 0, 0x40, ts3 );
          if ( !prb.postambleCommutable && prb.postamble != null )
          {
            Function f = prb.functions.get( prb.functions.size() - 1 );
            // f should be the postamble function, but check this
            if ( f.code == prb.postamble )
            {
              irpParts[ 22 ] = f.name + "(),";
            }
            else
            {
              for ( String str : prb.postamble.description() )
              {
                irpParts[ 22 ] += str + ",";
              }
            }
            choices[ 22 ]= true;
          }
        }
        
        for ( int m = 0; m < paths.size(); m++ )
        {
          int p = paths.get( m );    
          if ( sbHasCode )
          {
            int start = labelIndex.get( "SB" + sbIndex );
            boolean[] freqFlags = new boolean[]{ false, false };
            
            LinkedHashMap< String, OpTree > varMap = new LinkedHashMap< String, OpTree >();
            List< String > refList = new ArrayList< String >();
            loopDone.clear();
            
            Node n = nodeList.get( start + 1 );
            while ( n != null )
            {
              int[] b = n.branch;
              for ( int i = n.start; i <= b[0]; i++ )
              {
                AssemblerItem item = completeItemList.get( i );
                addItemComments( item, carriers, freqFlags, ts3 );
              }
              varMap.clear();
              refList.clear();
              LinkedHashMap< Integer, Node > nl = new LinkedHashMap< Integer, Node >();
              nl.put( start + 1,  n );
              CodeTree code = makeCTree( null, nl, varMap, refList, start + 1 );
              for ( String str : code.description() )
              {
                irpParts[ 17 ] += str+ ",";
                choices[ 17 ] = true;
              }
              
              int br = p&3;
              String comment = null;
              if ( ( br == 1 || br == 2 ) && ( comment = n.getComments( br - 1, "when " ) ) != null
                  && ( comment.contains( "=" ) || n.branchType == 8 ) )
              {
                irpStruct.comments.add( comment );
              }
              n = p != 0 ? nodeList.get( b[br] ) : null;
              p >>= 2;
            }
            if ( m == 2 && choices[ 23 ] && ( pf[ 2 ] & 0xE0 ) == 0 )
            {
              // m == 2 corresponds to key released; clear choices[ 23 ] if terminate after key released
              choices[ 23 ] = false;
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
          txCount = pf[ 2 ] & 0x1F;
          int txp = txPos;
          for ( int i = 0; i <= pbSwitchSize; i++ )
          {
            // If pbSwitchSize > 0 then there can be no SB code, so the results of
            // translateTXBytes cannot be affected by code.  They are also unaffected
            // when choicdes[ 20 ]==true.
            if ( choices[ 20 ] || i > 0 )
            {
              Hex txBytes = hex.subHex( txp, txCount );
              translateTXBytes( txBytes, null );
            }
            analyzeTXBytes( togData );
            txp += txCount;
            if ( pbSwitchSize > 0 )
            {
              prb.irpParts11.add( irpParts[ 11 ] );
            }
            if ( i < pbSwitchSize )
            {
              txCount = data[ txp++ ];
            }
          }
          
          if ( pbSwitchSize > 0 )
          {
            irpParts[ 11 ] = "irpPart11,";
          }
          
          if ( !choices[ 21 ] )
          {
            String stream = makeIRStream();
            irpStruct.irStream += stream;
          }
          irpParts[ 17 ] = "";
          choices[ 17 ] = false;
//          irpParts[ 22 ] = "";
//          choices[ 22 ] = false;
          if ( choices[ 12 ] || !choices[ 16 ] )
          {
            // keep only IRstream for first frame if no repeat of current signal block 
            break;
          }
        }
      }

      more = !hasNativeCode && pos < data.length - 1;
      if ( more && !choices[ 14 ] && !choices[ 19 ] )
      {
        TimingStruct next = altTimings.peek();
        if ( next != null && next.same( ts, blockCount ) )
        {
          altTimings.pop();
        }
      }
      more &= choices[ 14 ] || choices[ 19 ];     
    }
    
    if ( irpStruct.irStream.length() > 0 )
    {
      irpStruct.irStream = irpStruct.irStream.substring( 0, irpStruct.irStream.length()-1 );
      for ( int i= 0; i < brackets; i++ )
      {
        irpStruct.irStream += ")";
      }
      if ( choices[ 15 ] || choices[ 22 ] )
      {
        int index = irpStruct.irStream.indexOf( '(' );
        if ( index != -1 )
        {
          String ir1 = irpStruct.irStream.substring( 0, index );
          String ir2 = irpStruct.irStream.substring( index );
          irpStruct.irStream = ir1 + "(";
          irpStruct.irStream += choices[ 15 ] ?  irpParts[ 15 ] : "";
          irpStruct.irStream += choices[ 22 ] ?  irpParts[ 22 ] : "";
          if ( ir2.substring( 1 ).startsWith( "(" ) )
          {
            irpStruct.irStream += ir2.substring( 1 );
          }
          else
          {
            irpStruct.irStream += ir2 + ")";
          }
        }
      }
    }

    if ( hasNativeCode )
    {
      pos += ( pos & 1 ) == 1 ? 1 : 0;  // round to word boundary
      Hex nCode = hex.subHex( pos );
      pos += nCode.length();
    }
    return changed;
  }
  
  
  
  
  private String makeIRStream()
  {    
    if ( ( blockCount > 1 || choices[ 17 ] && choices[ 20 ] ) && brackets == 0 )
    {
      irpStruct.irStream = "(" + irpStruct.irStream;
      brackets++;
    }
    String irp = "";
    int lenAfterLeadIn = 0;
    
    if ( choices[ 12 ] )
    {
      choices[ 23 ] = false;
    }
    
    if ( choices[ 17 ] && choices[ 20 ] )
    {
      irp += ( choices[ 23 ] ? "(" : "" ) + irpParts[ 17 ] + ( !choices[ 23 ] ? "(" : "" );
    }
    else
    {
      irp += "(";
    }
    brackets++;
    
    if ( choices[ 0 ] )
    {
      irp += choices[ 5 ] ? irpParts[ 5 ] : irpParts[ 0 ];
      lenAfterLeadIn = irp.length();
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
    
    if ( choices[ 23 ] || choices[ 12 ] || minRpts > 0 )
    {
      // repeat on key held, and/or has mandatory repeats
      if ( choices[ 9 ] )
      {
        // has halved-style repeats, so needs new IRstream
        if ( !firstFrame )
        {
          // when not first frame, only the repeats are sent
          irp = "";
          brackets--;
        }
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
        int r = firstFrame ? minRpts : minRpts+1;  // when not first frame, only halved-style repeats are sent
        irp += r > 1 ? r : "";
        irp += choices[ 12 ] ? minRpts > 0 ? "+," : "*," : ",";
      }
      else
      {
        // repeat current IRstream, or if choices[ 23 ] then entire signal block
        boolean leadinExcluded = false;
        if ( choices[ 0 ] && choices[ 8 ] )
        {
          // exclude the lead-in from the repeat
          irp = irp.substring( 0, lenAfterLeadIn ) + "(" + irp.substring( lenAfterLeadIn );
          // need at least one occurrence of remainder of frame after lead-in
          leadinExcluded = true;
          brackets++;
        }
        // replace final comma by bracket
        irp = irp.substring( 0, irp.length()-1 ) + ")";
        // for mandatory repeats, add numeric repeat count
        irp += minRpts > 0 ? ( minRpts + 1 ) : "";
        // if mandatory repeats, add "+"
        // if repeat while key held, add "*"
        // if mandatory repeats not continuing while key held, no further marker
        irp += choices[ 12 ] || choices[ 23 ] ? minRpts > 0 || leadinExcluded || brackets == 1 ? "+," : "*," : ",";
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
    if ( choices[ 17 ] && !choices[ 20 ] )
    {
      irp += irpParts[ 17 ];
    }
    if ( choices[ 24 ] && ( irp.endsWith( "+," ) || irp.endsWith( "*," ) ) )
    {
      irpStruct.comments.add( "only keys in repeating group repeat" );
    }

    firstFrame = false;   // Set "not first frame" for subsequent calls
    return irp;
  }
  
  private List< Integer > createCodePaths( int start, int limit )
  {
    int last = start + 1;
    List< String > labelsUsed = new ArrayList< String >();
    String source = completeItemList.get( start ).getLabel();
    List< Integer > validTypes = null;
    if ( source.startsWith( "PB" ) || limit > 0 )
    {
      // valid types for protocol block
      // limit only used when analyzing part of protocol block
      validTypes = Arrays.asList( -1,1,2,3,4,8,9,14,15 );
    }
    else
    {
      // valid types for signal block
      validTypes = Arrays.asList( -1,1,2,3,4,7,8,9,10,11,12,13,16,17 );  // 6 removed, not sure why it was allowed
    }

    if ( limit == 0 )
    {
      limit = completeItemList.size() - 1;
    }
    
    for ( int i = start+1; i < limit; i++ )
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
//      int type = item.get_Type();
//      if ( source.startsWith( "PB" ) )
//      {
//        if ( /*( type == 3 || type == 4 ) &&*/ validTypes.contains( type ) )
//        {
//          last = i + 1;
//        }
//      }
//      else
      {
        // for signal block, continue till instruction preceding END
        if ( item.getOperation().equals( "END" ) )
        {
          last = i;
          break;
        }
        last = i + 1;
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
          prb.errors.add( "There is a jump into timing region from outside" );
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
        if ( n.branch[ 1 ] > 0 && n.branch[ 1 ] < n.branch[ 0 ] 
            || n.branch[ 2 ] > 0 && n.branch[ 2 ] < n.branch[ 0 ] )
        {
          prb.description += "\nBackward jump at " + n.branch[ 0 ] + " (" + n.branch[ 1 ] + ", " + n.branch[ 2 ] + ")\n";
          return paths;
        }
        
        if ( n.branch[0] == -1 )
        {
          return paths;
        }
        
        boolean added = false;
        int branchType = completeItemList.get( n.branch[0] ).get_Type();
        if ( branchType == 7 || branchType == 13 || branchType == 16 )
        {
          // branching on test of first frame (7) or key held (13) or repeat required (16)
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

//    if ( !paths.isEmpty() )
//    {
//      prb.description += "\nPaths: ";
//      for ( int p : paths )
//      {
//        prb.description += Integer.toString( p, 4 ) + " ";
//      }
//      prb.description += "\n";
//    }
    
    return paths;
  }
  
  private int createPathSequence( int start, int p, int index, List< Integer > list )
  {
    /*
//     *   Index values:
//     *   0 = first frame
//     *   1 = not first frame, key held
//     *   2 = not first frame, key released
//     *   
     *   Return values
     *   0 = not changed
     *   1 = changed at hey held branch
     *   2 = changed at first frame branch
     */
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
      return 0;
    }

    int type = completeItemList.get( n.branch[ 0 ] ).get_Type();
    int ret = type==7 ? 2 : 1;   
    p &= (1<<(2*level))-1;      // remove the final 3
    p += index<<(2*level);      // replace it by index
    List< Integer > cp = n.branch[index] > 0 ? createCodePaths( n.branch[index]-1, 0 ) : null;
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
    return ret;
  }

  private String getPFdescription( int pfn, int start, boolean[] choices )
  {
    String desc = "";
    if ( choices == null )
    {
      choices = new boolean[ 30 ];
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
              choices[ 24 ] = true;
              break;
            case 3:
              desc = "Send One-ON in place of first repeat, nothing on later repeats";
              e.errors.add( "Send One-ON in place of first repeat is not yet supported" );
              break;
          }
          break;
        case 0:
        case 6:
        case 7:
          choices[ 0 ] = choices[ 5 ] = choices[ 8 ] = choices[ 9 ] = false;
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
      choices[ 20 ] = ( val & 0x80 ) != 0;
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
     * 9 = Branch to/return from subroutine
     *10 = Minimum repeat change
     *11 = Suppress IR transmission
     *12 = Resume IR transmission
     *13 = Branch on test of key held
     *14 = Indexed/indirect data move
     *15 = DBNZ instruction
     *16 = Branch on test of repeat required (function 55)
     *17 = Change of TXB/TXD data
     */
    
    
    if ( ts == null )
    {
      ts = new TimingStruct();
    }
    freqFlags[ 1 ] = false;  // set for instruction that changes frequency
    String opName = item.getOperation();
    if ( opName.equals( "END" ) || opName.equals( "NOP" ) )
    {
      return -1;
    }
    List< String > opList = Arrays.asList( "MOV", "AND", "OR", "BRA", 
        "MOVN", "MOVW", "CARRIER", "TIMING", "CALL", "XOR", "BSR", "DBNZ", "RTS" );
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
      String pre = i == 0 ? "\\(?" : i == 1 ? "Z,.*, " : "NZ,.*, ";
      if ( Pattern.matches( pre + "(Fix|Var|Calc|Tmp).*", args ) )
      {
        itemType = i == 0 ? 1 : 2;
        if ( itemType == 1 && opIndex == 0
            && ( args.contains( "[" ) || args.contains( "(" ) ) )
        {
          itemType = 14;  // indexed/indirect move
        }
        else if ( itemType == 1 && opName.equals( "DBBC" ) )
        {
          itemType = 0;
        }
        break;
      }
//      else if ( Pattern.matches( pre + "Tmp.*", args ) )
//      {
//        itemType = -1;
//        break;
//      }
    }
    
    if ( opIndex < 0 )
    {
      return itemType;
    }

    if ( opIndex == 11 )
    {
      // DBNZ instruction
      itemType = 15;
    }
    else if ( opIndex == 3 )
    {
      if ( Pattern.matches( "L\\d*", args ) )
      {
        // Unconditional branch
        itemType = 2;
      }
      else if ( Pattern.matches( "(T|F),.*", args ) )
      {
        // Branch on true/false test
        int fnCode = item.getHex().getData()[ 2 ];
        itemType = fnCode == 0x54 ? 13 : fnCode == 0x55 ? 16 : fnCode > 0x55 ? 0 : 8;
      }
    }
    else if ( opIndex == 10 || opIndex == 12 )
    {
      // Subroutine branch/return
      itemType = 9;
    }
    
    if ( itemType == 13 || itemType == 16 )
    {
      int rptFlags = ts.pf[ 1 ] & 0x30;
      if ( ( rptFlags & 0x20 ) != 0 )
      {
        e.errors.add( "Repeat policy not supported" );
      }
      if ( choices[ 16 ] )
      {
        // after repeats, re-execute current signal block
        choices[ 23 ] = itemType == 13 || rptFlags == 0x10;
      }
      else
      {
        e.errors.add( "Repeat policy not supported" );
      }
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
      else if ( str.startsWith( "Suppress" ) )
      {
        itemType = 11;
        choices[ 21 ] = true;
      }
      else if ( str.startsWith( "Resume" ) )
      {
        itemType = 12;
        choices[ 21 ] = false;
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
    else if ( opIndex == 0 && args.startsWith( "TXDcount, #" ) )
    {
      itemType = 17;
      int val = getImmValue( args );
      txdCount = val;
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
  
  private String translateTXBytes( Hex hex, List< String > srcList )
  {
    Arrays.fill( txd, null );
    Arrays.fill( txb, 0 );
    Arrays.fill( txdIndex, -1 );
    txdCount = hex == null ? 0 : hex.length();

    if ( txdCount == 0 )
    {
      return " <none>\n";
    }
    
    String txStr = "";
    for ( int i = 0; i < txdCount; i++ )
    {
      short val = hex.getData()[ i ];
      txb[ i ] = ( ( val >> 4 ) & 0x07 ) + 1;
      boolean flag = ( val & 0x80 ) > 0;
      txdIndex[ i ] = val & 0x0F;
      int addr = 0xD0 + txdIndex[ i ];
      String label = getZeroLabel( addr );
      txStr += " " + ( flag ? "~" : "" ) + label + ":" + txb[ i ];
      String irpLabel = irpLabel( addr );
      if ( srcList != null && !srcList.contains( irpLabel ) )
      {
        srcList.add( irpLabel );
      }
      txd[ i ] = new OpTree( ( flag ? "~" : "" ) + irpLabel );
    }
    return txStr + "\n";
  }

  private void analyzeTXBytes( int[] togData )
  {
    int togIndex = 0x100;
    int togPos = 0x100;
    int togEnd = 0x100;
    int togCount = 0;
    if ( txdCount == 0 )
    {
      irpParts[ 11 ] = null;
      return;
    }
    irpParts[ 11 ] = "";
    int bitCount = 0;
    int mid = 0x100;

    if ( ( pf[ 4 ] & 0x80 ) != 0 )
    {
      mid = pf[ 4 ] & 0x7F;
    }
    for ( int i = 0; i < txdCount; i++ )
    {
      int n = txb[ i ];
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
      if ( txd == null || txd[ i ] == null )
      {
        // Error situation, so abort analysis
        irpParts[ 11 ] = null;
        return;
      }
      
      String valStr = txd[ i ].lsbEvaluate();
      boolean flag = valStr.startsWith( "~" );
      boolean togComp = togData != null ? ( togData[ 5 ] & 0x08 ) > 0 : false;
      togComp = togCount == 1 ? false : flag && !togComp || !flag && togComp;    
      int togBits = Math.max( Math.min( togEnd+1, n ) - togPos, 0 );
      if ( n > 0 )
      {
        if ( togIndex == txdIndex[ i ] && togPos < n )
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
      }
      if ( rem >= 0 )
      {      
        if ( choices[ 3 ] )
        {
          List< String > warn = new ArrayList< String >();
          List< Integer > irpVals = new ArrayList< Integer >();
          int[] limits = new int[]{0,0};
          getTimingItem( 6, limits, irpVals, warn );
          for ( int m : irpVals )
          {
            irpParts[ 11 ] += m + ",";
          }
          if ( prb != null )
          {
            prb.warnings.addAll( warn );
          }
        }

        int togRem = togCount - togBits;
        if ( togIndex == txdIndex[ i ] && togEnd + 1 > n && togPos < n+rem )
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
    return;
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
  
  private String irpLabel( int addr )
  {
    AssemblerItem item = new AssemblerItem();
    if ( item.setZeroLabel( proc, addr, new ArrayList< Integer >(), "" ) )
    {
      String zLabel = item.getLabel();
      String iLabel = irpLabel( zLabel );
      return iLabel != null ? iLabel : zLabel;
    }
    return String.format( "$%02X", addr );
  }
  
  
  private boolean testIRPstruct( IRPstruct irps )
  {
    LinkedHashMap< String, String > map = new LinkedHashMap< String, String >();
    for ( String str : irps.comments )
    {
      int ndx = str.indexOf( '=' );
      if ( ndx >= 0 )
      {
        String v = str.substring( 0, ndx );
        String a = str.substring( ndx+1 );
        if ( map.get( v ) != null && !map.get( v ).equals( a ) )
        {
          return false;
        }
        map.put( v, a );
      }
    }
    return true;
  }
  
  private String interpretPB( int start )
  {
    String ret = "";
    int last = 0;
    int startTiming = 0;
    int lastTiming = 0;
    List< String > labelsUsed = new ArrayList< String >();

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
      int type = item.get_Type();
      if ( type == 3 || type == 4 )
      {
        if ( startTiming == 0 )
        {
          startTiming = i;
        }
        lastTiming = i;
      }
      if ( item.getOperation().equals( "END" ) )
      {
        last = i;
        break;
      }
    }

    if ( startTiming > 0 )
    {
      // There are timing instructions, all between startTiming and lastTiming
      // inclusive.  Extend this to smallest self-contained block.
      int[] range = findSelfContained( startTiming, lastTiming + 1 );
      startTiming = range[ 0 ];
      lastTiming = range[ 1 ] - 1;

      // Now see if there are branch instructions preceding this that form part of
      // the timing block.

      for ( int j = startTiming-1; j > start; j-- )
      {
        AssemblerItem item = completeItemList.get( j );
        int type = item.get_Type();
        if ( type == 2 )
        {
          range = findSelfContained( j, j + 1 );
          if ( range[ 0 ] != j || range[ 1 ] > lastTiming + 1 )
          {
            break;
          }
          else
          {
            startTiming = j;
          }
        }
        else
        {
          break;
        }
      }
      
      // Now test the instructions preceding timing block to see if it is self-contained.
      range = findSelfContained( start, startTiming );
      if ( range[ 0 ] < start )
      {
        e.errors.add( "PB jumps into earlier block" );
      }
      if ( range[ 1 ] > startTiming )
      {
        // Prefix block not self-contained, so need to include it in timing block
        startTiming = start + 1;
      }
      
      // Now test the instructions following timing block to see if it is self-contained.
      range = findSelfContained( lastTiming + 1, last );
      if ( range[ 0 ] < lastTiming + 1 )
      {
        // Postfix block not self-contained, so need to include it in timing block
        lastTiming = last - 1;
      }
      if ( range[ 1 ] > last )
      {
        e.errors.add( "Postfix block jumps outside PB" );
      }
    }
    
    // Now test if there are instructions other than branch, timing and format in timing block
    if ( startTiming > 0 )
    {
      for ( int i = startTiming; i <= lastTiming; i++ )
      {
        AssemblerItem item = completeItemList.get( i );
        int type = item.get_Type();
        if ( type != 2 && type != 3 && type != 4 && type != -1 )
        {
          prb.timingBlockHasGaps = true;
          prb.errors.add( "Timing instructions not consecutive" );
          break;
        }
      }
    }

//    System.err.println( e.names.get( 0 ) );
    prb.startTiming = startTiming;
    prb.lastTiming = lastTiming;
    
    int limit = startTiming == 0 ? last : startTiming;
    LinkedHashMap< Integer, Node > savedNodeList = nodeList;
    List< Integer > comboPaths = new ArrayList< Integer >();
    LinkedHashMap< String, OpTree > varMap = new LinkedHashMap< String, OpTree >();
    List< String > refList = new ArrayList< String >();
    
    for ( String label : functionIndex.keySet() )
    {
      Function f = functionIndex.get( label );
      nodeList = new LinkedHashMap< Integer, Node >();
      int fnStart = labelIndex.get( label );
      comboPaths.clear();
      treeRoot = fnStart;
      comboPaths = createCodePaths( fnStart-1, last );
      treeRoot = 0;
      varMap.clear();
      refList.clear();
      loopDone.clear();
      f.code = makeCTree( null, nodeList, varMap, refList, fnStart );
      prb.functions.add( f );
    }
    
    nodeList = new LinkedHashMap< Integer, Node >();
    comboPaths.clear();
    varMap.clear();
    refList.clear();
    loopDone.clear();
    comboPaths = createCodePaths( start , limit );
    prb.preamble = makeCTree( null, nodeList, varMap, refList, start + 1 );
    
    if ( lastTiming > 0 )
    {
      nodeList = new LinkedHashMap< Integer, Node >();
      comboPaths.clear();
      varMap.clear();
      refList.clear();
      loopDone.clear();
      comboPaths = createCodePaths( lastTiming, last );
      prb.postamble = makeCTree( null, nodeList, varMap, refList, lastTiming + 1 );
      
      // If no gaps, see if postamble can be run before timing block
      if ( !prb.timingBlockHasGaps && prb.postamble != null )
      {
        nodeList = new LinkedHashMap< Integer, Node >();
        comboPaths.clear();
        varMap.clear();
        refList.clear();
        loopDone.clear();
        comboPaths = createCodePaths( startTiming - 1, lastTiming + 1 );
        CodeTree timingTree = makeCTree( null, nodeList, varMap, refList, startTiming );
        List< String > timingVars = timingTree.getAllSources();
        List< String > postambleVars = prb.postamble.getAllDests();
        for ( String var :timingVars )
        {
          if ( postambleVars.contains( var ) )
          {
            prb.postambleCommutable = false;
            // convert postamble to function
            Function f = new Function( "Proc" + functionIndex.size() );
            f.code = prb.postamble;
            prb.functions.add( f );
            break;
          }
        }
      }
    }
    nodeList = savedNodeList;
    return ret;  
  }
  
  private CodeTree makeCTree( CodeTree parent, LinkedHashMap< Integer, Node > list, LinkedHashMap< String, OpTree > varMap, 
      List< String > refList, int start )
  {    
    CodeTree cTree = new CodeTree( parent );
    Node n = list.get( start );
    if ( n == null )
    {
      return null;
    }

    cTree.node = n;
    if ( n.branchType == 11 && loopDone.get( n.start ) == null )
    {
      loopDone.put( n.start, cTree );
    }
    int[] b = n.branch;
    List< String > loopDestList = null;
    List< String > destList = new ArrayList< String >();
    List< String > srcList = new ArrayList< String >();
    LinkedHashMap< String, OpTree > nextVarMap = varMap;
    LinkedHashMap< String, OpTree > incrementals = new LinkedHashMap< String, OpTree >();
    
    for ( int i = n.start; i <= b[0]; i++ )
    {
      AssemblerItem item = completeItemList.get( i );
      getReferenced( item, destList, srcList, false );
      if ( isAssignmentItem( item ) )
      {
        interpretAssignmentItem( item, varMap, refList );
      }
      else if ( isLoopItem( item ) )
      {
        interpretLoopItem( item, varMap, refList, incrementals );
      }
      else
      {
        continue;
      }
    }
    for ( String key : incrementals.keySet() )
    {
      varMap.remove( key );
      varMap.put( key,incrementals.get( key ) );
    }
    
    for ( int i = n.start; i <= b[0]; i++ )
    {
      AssemblerItem item = completeItemList.get( i );
      getReferenced( item, cTree.thisDests, cTree.thisSources, true );
    }
    
    if ( n.getComments( 0, "" ).equals( "loop" )
        || n.getComments( 0, "" ).equals( "while" ) )
    {
      nextVarMap = new LinkedHashMap< String, OpTree >();
      
//      List< String > destList2 = new ArrayList< String >();
//      List< String > srcList2 = new ArrayList< String >();
//      loopDestList = new ArrayList< String >();
      for ( int i = b[ 1 ]; i < b[ 2 ]; i++ )
      {
        AssemblerItem item = completeItemList.get( i );
        String args = item.getArgumentText();
        int refPos = -1;
        int pos = args.indexOf( ", (" );
        if ( pos >= 0 )
        {
          refPos = pos + 3;
        }
        else
        {
          pos = args.indexOf( "(" );
          if ( pos >= 0 )
          {
            refPos = pos + 1;
          }
        }
        if ( refPos > 0 )
        {
          String refVar = args.substring( refPos, args.indexOf( ")" ) );
          refList.add( refVar );
//          OpTree val = varMap.get( refVar );
//          if ( val == null )
//          {
//            val = new OpTree( refVar );
//          }
////          if ( val != null )
////          {
////            if ( refList.contains( refVar ) )
////            {
//              val = val.doOp( "REV", null );
////            }
//            OpTree ot = val.doOp( "SUB", new OpTree( "11" ) );
//            varMap.put( refVar, ot );
//            cTree.assignments.put( refVar, ot.lsbEvaluate() );
////          }
//        }
//        
//        getReferenced( item, destList2, srcList2, false );
//        if ( item.getOperation().equals( "MOVI" ) )
//        {
//          String indexVar = destList2.get( 1 );
//          destList2.remove( indexVar );
//        }
//        for ( String var : destList2 )
//        {
////          nextVarMap.remove( var );
//          if ( !loopDestList.contains( var ) )
//          {
//            loopDestList.add( var );
//          }
        }
      }
    }
    else if ( n.getComments( 0, "" ).equals( "call" ) )
    {
      nextVarMap = new LinkedHashMap< String, OpTree >();
    }

    for ( String var : varMap.keySet() )
    {      
      cTree.assignments.put( var, varMap.get( var ).lsbEvaluate() );
    }
    cTree.branch[ 0 ] = n.getComments( 0, "" );
    cTree.branch[ 1 ] = n.getComments( 1, "" );
    
    if ( cTree.branch[ 0 ] != null 
        && ( cTree.branch[ 0 ].equals( "loop" ) || cTree.branch[ 0 ].equals( "next" ) ) )
    {
      cTree.loop[ 0 ] = n.branchVar;
      OpTree base = varMap.get( n.branchVar ) != null ? varMap.get( n.branchVar ) : new OpTree( n.branchVar );
      if ( refList.contains( n.branchVar ) )
      {
        base = base.doOp( "REV", null );
      }
      OpTree ot = base.doOp( "SUB", new OpTree( "128" ) );
      cTree.loop[ 1 ] = ot.msbEvaluate();
    }
    else if ( cTree.branch[ 0 ] != null 
        && ( cTree.branch[ 0 ].equals( "while" ) ) )
    {
      cTree.loop[ 0 ] = nodeList.get( b[ 1 ] ).getComments( 1, "" );
    }

    if ( b[ 1 ] > 0 )
    {
      if ( loopDone.get( b[1]) == null )
      {
        LinkedHashMap< String, OpTree > newVarMap = new LinkedHashMap< String, OpTree >();
        for ( String s : nextVarMap.keySet() )
        {
          newVarMap.put(  s, nextVarMap.get( s ).clone() );
        }
        cTree.next[ 0 ] = makeCTree( cTree, list, newVarMap, refList, b[ 1 ] );
      }
      else
      {
        cTree.next[0] = loopDone.get( b[1]);
      }
    }
    if ( b[ 2 ] > 0 )
    {
      if ( loopDone.get( b[2]) == null )
      {
        LinkedHashMap< String, OpTree > newVarMap = new LinkedHashMap< String, OpTree >();
        for ( String s : nextVarMap.keySet() )
        {
          newVarMap.put(  s, nextVarMap.get( s ).clone() );
        }
        if ( n.getComments( 0, "" ).equals( "loop" ) )
        {
          // a DBNZ loop finishes with the loop variable being 0
          newVarMap.put( n.branchVar, new OpTree( "0" ) );
        }
        cTree.next[ 1 ] = makeCTree( cTree, list, newVarMap, refList, b[ 2 ] );
      }
      else
      {
        cTree.next[1] = loopDone.get( b[2]);
      }
    }
    return cTree;
  }
  
  private void interpretLoopItem( AssemblerItem item, LinkedHashMap< String, OpTree > varMap, List< String > refList, 
      LinkedHashMap< String, OpTree > incrementals )
  {
    List< String > comboOps = Arrays.asList( "LSL", "LSR", "AND", "OR", "XOR",
        "ADD", "SUB" );
    String op = item.getOperation();
    String args = item.getArgumentText();
    StringTokenizer st = new StringTokenizer( args, ",()", true );
    int argNum = 0;
    boolean ref = false;
    String source = null;
    String dest = null;
    OpTree ot = new OpTree();
    while ( st.hasMoreTokens() )
    {
      String token = st.nextToken().trim();
      if ( token.isEmpty() )
      {
        continue;
      }
      if ( token.equals( "," ) )
      {
        argNum++;
        continue;
      }
      if ( token.equals( "(" ) )
      {
        ref = true;
        continue;
      }
      if ( token.equals( ")" ) )
      {
        ref = false;
        continue;
      }
      if ( argNum == 0 )
      {
        dest = irpLabel( token );
        dest = dest != null ? dest : token;
        if ( ref )
        {
          dest = irpLabel( 0xD0 ) + "[" + dest+ "-208]";
        }
      }
      else if ( argNum == 1 )
      {
        source = irpLabel( token );
        source = source != null ? source : token;
        if ( ref )
        {
          ot.op = "VAR";
          ot.opArgs = new ArrayList< OpTree >();
          ot.opArgs.add( new OpTree( "11" ) );
          
          OpTree val = varMap.get( source );
          if ( val == null )
          {
            val = new OpTree( source );
          }
          val = val.doOp( "REV", null );
          OpTree ot3 = val.doOp( "SUB", new OpTree( "11" ) );

          if ( op.equals( "MOVI" ) )
          {
//            ot3 = ot3.doOp( "ADD", ( new OpTree( loopVar ) ).doOp( "REV", null ) );
            OpTree ot4 = val.doOp( "ADD", new OpTree( "128" ) ).doOp( "REV", null );
            incrementals.put( source, ot4 );
          }
          ot3 = ot3.doOp( "REV", null );
          ot.opArgs.add( ot3 );            
          source += "[]";  // prevent it from being in refList
        }
        else
        {
          ot = varMap.get( source ) != null ? varMap.get( source ) : new OpTree( source );
        }
      }
    }
    
    if ( comboOps.contains( op ) )
    {
      OpTree currVal = varMap.get( dest );
      if ( currVal == null )
      {
        currVal = new OpTree( dest );
      }
      if ( refList.contains( dest ) )
      {
        currVal = currVal.doOp( "REV", null );
      }
      ot = currVal.doOp( op, ot );
    }
    
    if ( refList.contains( source ) && !refList.contains( dest ) 
        || !refList.contains( source ) && refList.contains( dest ))
    {
      ot = ot.doOp( "REV", null );
    }
    varMap.remove( dest );
    varMap.put( dest, ot );
  }
  
  private void interpretAssignmentItem( AssemblerItem item, LinkedHashMap< String, OpTree > varMap, List< String > refList )
  {
    for ( int i= 0; i < 10; i++ )
    {
      refList.add( "Tmp"+i);
    }
    
    
    List< String > argList = new ArrayList< String >();
    List< String > argList2 = new ArrayList< String >();
    List< String > comboOps = Arrays.asList( "LSL", "LSR", "AND", "OR", "XOR",
        "ADD", "SUB", "MULT" );
    String args = item.getArgumentText();
    StringTokenizer st = new StringTokenizer( args, ",[]" );
    while ( st.hasMoreTokens() )
    {
      String token = st.nextToken().trim();
      if ( token.startsWith( "#$" ) )
      {
        // All values, symbolic or numerical, are passed in bit-reversed form
        int val = Integer.parseInt( token.substring( 2 ), 16 );
        int[] rev = new int[ 4 ];
        reverseByte( val & 0xFF, rev );
        argList.add( "" + rev[ 0 ] );
        reverseByte( (val>>8) & 0xFF, rev );
        argList2.add( "" + rev[ 0 ] );
      }
      else
      {
        String label = irpLabel( token );
        argList.add( label != null ? label : token );
        argList2.add( "" );
      }
    }
    
    String op = item.getOperation();
    if ( op.equals( "MOV" ) && !args.contains( "[" ) && !args.contains( "(" ) )
    {
      String dest = argList.get(0);
      String source = argList.get(1);
      OpTree currVal = varMap.get( source );
      OpTree newVal = currVal != null ? currVal : new OpTree( source );
      if ( refList.contains( source ) && !refList.contains( dest ) 
          || !refList.contains( source ) && refList.contains( dest ) )
      {
        newVal = newVal.doOp( "REV", null );
      }
      varMap.remove( dest );
      varMap.put( dest, newVal );
    }
    else if ( op.equals( "MOVW" ) && args.contains( "#$" ) )
    {
      int dest = item.getHex().getData()[ 1 ];
      String var1 = argList.get(0);
      String var2 = irpLabel( getZeroLabel( dest + 1 ) );
      OpTree ot1 = new OpTree( argList.get(1) );
      OpTree ot2 = new OpTree( argList2.get(1));
      if ( refList.contains( var1 ) )
      {
        ot1 = ot1.doOp( "REV",  null );
      }
      if ( refList.contains( var2 ) )
      {
        ot2 = ot2.doOp( "REV",  null );
      }
      varMap.remove( var1 );
      varMap.remove( var2 );
      varMap.put( var1, ot1 );  
      varMap.put( var2, ot2 );
    }
    else if ( op.equals( "MOVN" ) )
    {
      int dest = item.getHex().getData()[ 1 ];
      int src = item.getHex().getData()[ 2 ];
      int n = item.getHex().getData()[ 3 ];
      for ( int i = 0; i < n; i++ )
      {
        String var1 = irpLabel( dest + i );
        String var2 = irpLabel( src + i );
        OpTree currVal = varMap.get( var2 );
        OpTree newVal = currVal != null ? currVal : new OpTree( var2 );
        if ( refList.contains( var1 ) &&  !refList.contains( var2 )
          || !refList.contains( var1 ) &&  refList.contains( var2 ) )
        {
          newVal = newVal.doOp( "REV", null );
        }
        varMap.remove( var1 );
        varMap.put( var1, newVal );
      }
    }
    else if ( op.equals( "XOR" ) && args.contains( "#$FF" ) )
    {
      // Complement
      String dest = argList.get(0);
      String source = argList.get(1);
      OpTree currVal = varMap.get( source );
      currVal = currVal != null ? currVal : new OpTree( source );
      OpTree newVal = currVal.doOp( "CPL", null );
      if ( refList.contains( source ) && !refList.contains( dest ) 
          || !refList.contains( source ) && refList.contains( dest ) )
      {
        newVal = newVal.doOp( "REV", null );
      }
      varMap.remove( dest );
      varMap.put( dest, newVal );
    }
    else if ( comboOps.contains( op ) )
    {
      String dest = argList.get(0);
      String source = argList.get(1);
      String argStr = argList.get(2);
      OpTree currVal = varMap.get( source );
      currVal = currVal != null ? currVal : new OpTree( source );
      if ( refList.contains( source ) )
      {
        currVal = currVal.doOp( "REV", null );
      }
      OpTree currArg = varMap.get( argStr );
      currArg = currArg != null ? currArg : new OpTree( argStr );
      if ( refList.contains( argStr ) )
      {
        currArg = currArg.doOp( "REV", null );
      }

      OpTree newVal = currVal.doOp( op, currArg );
      if ( refList.contains( dest ) )
      {
        newVal = newVal.doOp( "REV", null );
      }
      varMap.remove( dest );
      varMap.put( dest, newVal );
    }
    else if ( op.equals( "SWAP" ) )
    {
      String dest = argList.get(0);
      String source = argList.get(1);
      OpTree currVal = varMap.get( source );
      currVal = currVal != null ? currVal : new OpTree( source );
      OpTree newVal = currVal.doOp( "SWAP", null );
      if ( refList.contains( source ) && !refList.contains( dest ) 
          || !refList.contains( source ) && refList.contains( dest ) )
      {
        newVal = newVal.doOp( "REV", null );
      }
      varMap.remove( dest );
      varMap.put( dest, newVal ); 
    }
    else if ( op.equals( "MOV" ) && args.contains( "[" ) )
    {
      String dest = argList.get(0);
      String base = argList.get(2);
      OpTree currVal = varMap.get( base );
      currVal = currVal != null ? currVal : new OpTree( base );
      if ( refList.contains( base ) )
      {
        currVal = currVal.doOp( "REV", null );
      }
      String str = argList.get( 1 ) + "[";
      String valStr = currVal.msbEvaluate();
      str += valStr != null ? valStr : "???";
      str += "]";
      OpTree ot = new OpTree( str );
      if ( refList.contains( dest ) )
      {
        ot = ot.doOp( "REV", null );
      }
      varMap.remove( dest );
      varMap.put( dest, ot );
    }
  }
  
  private boolean getReferenced( AssemblerItem item, List< String > destList, List<  String > srcList,
      boolean checkDest )
  { 
    if ( item.getHex() == null )
    {
      return false;
    }
    
    short[] hex = item.getHex().getData();
    int op = hex[ 0 ];

    String str = null;
    if ( ( op >= 0x01 && op <= 0x09 ) || op == 0x53 )
    {
      str = irpLabel( hex[ 3 ] );
      if ( !srcList.contains( str ) && ( !checkDest || !destList.contains( str ) ) )
      {
        srcList.add(  str );
      }
    }
    
    if ( op == 0x09 || op == 0x19 )
    {
      str = irpLabel( hex[ 2 ] + 1 );
      if ( !srcList.contains( str ) && ( !checkDest || !destList.contains( str ) ) )
      {
        srcList.add(  str );
      }
    }

    if ( op == 0x54 || op == 0x55 || op ==0x56 || op == 0x58 )
    {
      for ( int i = op == 0x54 ? 1 : 2; i < 3; i++ )
      {
        str = irpLabel( hex[ i ] );
        if ( !srcList.contains( str ) && ( !checkDest || !destList.contains( str ) ) )
        {
          srcList.add(  str );
        }
      }
    }
    
    if ( op < 0x4C || op == 0x53 || op == 0x50 || op == 0x51 )
    {
      if ( op != 0x10 && op != 0x30 /*&& op != 0x41*/ )
      {
        str = irpLabel( hex[ 2 ] );
//        if ( op == 0x40 )
//        {
//          str = irpLabel( 0xD0 ) + "[" + irpLabel( hex[ 2 ] ) + "-208]";
//        }
        if ( !srcList.contains( str ) && ( !checkDest || !destList.contains( str ) ) )
        {
          srcList.add(  str );
        }
      }
      
      str = irpLabel( hex[ 1 ] );
      if ( !destList.contains( str ) )
      {
        str = irpLabel( hex[ 1 ] );
        destList.add( str );
      }
      List< Integer > wordOps = Arrays.asList( 0x08, 0x09, 0x18, 0x19,0x20, 0x30 );
      if ( wordOps.contains( op ) )
      {
        str = irpLabel( hex[ 1 ] + 1 );
        if ( !destList.contains( str ) )
        {
          destList.add(  str );
        }
      }
      if ( op == 0x50 )  // DBBC
      {
        str = irpLabel( hex[ 3 ] );
        if ( !destList.contains( str ) )
        {
          destList.add(  str );
        }
      }
    }
    
    if ( op == 0x52 )
    {
      for ( int i = 0; i < hex[ 3 ]; i++ )
      {
        str = irpLabel( hex[ 2 ]+ i );
        if ( !srcList.contains( str ) && ( !checkDest || !destList.contains( str ) ) )
        {
          srcList.add(  str );
        }
      }
      for ( int i = 0; i < hex[ 3 ]; i++ )
      {
        str = irpLabel( hex[ 1 ]+ i );
        if ( !destList.contains( str ) )
        {
          destList.add(  str );
        }
      }
    }
    
    if ( /*op == 0x41 ||*/ op == 0x58 )
    {
      str = irpLabel( hex[ 2 ] );
      if ( !destList.contains( str ) )
      {
        destList.add(  str );
      }
    }
    
    if ( op == 0x4C )
    {
      str = irpLabel( 0xD0 ) + "[" + irpLabel( hex[ 1 ] ) + "]";
      if ( !destList.contains( str ) )
      {
        destList.add( str );
      }
      str = irpLabel( hex[ 2 ] );
      if ( !srcList.contains( str ) && ( !checkDest || !destList.contains( str ) ) )
      {
        srcList.add(  str );
      }
    }
    
    if ( op == 0x59 || op == 0x4F )
    {
      String label = op == 0x59 ? item.getArgumentText() : item.getLabel();
      Function fn = functionIndex.get(  label );
      if ( fn != null && fn.code != null && fn.code.node != null )
      {
//        if ( fn.code == null || fn.code.getAllSources() == null )
//        {
//          int x = 0;
//        }
        for ( String var : fn.code.getAllSources() )
        {
          if ( !srcList.contains( var ) )
          {
            srcList.add( var );
          }
        }
        for ( String var : fn.code.getAllDests() )
        {
          if ( !destList.contains( var ) )
          {
            destList.add( var );
          }
        }
      }
    }
    
    return true;
  }

  
  private int[] findSelfContained( int start, int end )
  {
    boolean changed = true;
    while ( changed )
    {
      changed = false;
      for ( int i = start; i < end; i++ )
      {
        AssemblerItem item = completeItemList.get( i );
        String op = item.getOperation();
        String args = item.getArgumentText();
        if ( !op.equals( "BRA" ) && !op.equals( "DBNZ" ) )
        {
          continue;
        }
        else
        {
          StringTokenizer st = new StringTokenizer( args, "," );
          String label = null;
          while ( st.hasMoreTokens() )
          {
            String token = st.nextToken().trim();
            if ( Pattern.matches( "L\\d*", token ) )
            {
              label = token;
              break;
            }
          }
          if ( label == null )
          {
            return null;  // Error situation, label should not be null
          }
          int dest = labelIndex.get( label );
          if ( dest < start )
          {
            start = dest;
            changed = true;
          }
          if ( dest > end )
          {
            end = dest;
            changed = true;
          }

        }
      }
    }
    return new int[]{ start, end }; 
  }
  
  private boolean isAssignmentItem( AssemblerItem item )
  {
    int t = item.get_Type();
    if( t == 3 || t == 4 || t == 5 )
    {
      return false;
    }
    String op = item.getOperation();
    String args = item.getArgumentText();
    List< String > comboOps = Arrays.asList( "MOV", "LSL", "LSR", "AND", "OR", 
        "XOR", "MOVN", "SWAP", "ADD",  "SUB", "MULT", "NOP" );
    return ( comboOps.contains( op )  && !args.contains( "(" )
        || op.equals( "MOVW" ) && args.contains( "#$" ) );
  }

  private boolean isLoopItem( AssemblerItem item )
  {
    if ( isAssignmentItem( item ) )
    {
      return true;
    }
    String args = item.getArgumentText();
    if ( Pattern.matches( ".*\\((Fix|Var|Calc|Tmp)\\d+\\).*", args ) ) 
    {
      // instruction with indirect addressing
      return true;
    }
    return false;
  }
  
  private List< AssemblerItem > expandLabels( List< AssemblerItem > in )
  {
    List< AssemblerItem > out = new ArrayList< AssemblerItem >();
    for ( AssemblerItem ai : in )
    {
      int ad = ai.getAddress();
      String label = labels.get( ad );
      if ( label != null )
      {
        AssemblerItem newItem = new AssemblerItem( ad, new Hex( new short[]{ 0x4F, 0, 0, 0 } ) );
        newItem.setLabel( label );
        newItem.setOperation( "NOP" );
        newItem.setType( -1 );
        out.add( newItem );
      }
      if ( !ai.getLabel().startsWith( "PB" ) && !ai.getLabel().startsWith( "SB" ) )
      {
        ai.setLabel( "" );
      }
//      labelIndex.put(  label, i );

//      if ( item.getLabel().startsWith( "L" ) )
//      {
//        AssemblerItem newItem = new AssemblerItem( item.getAddress(), new Hex( new short[]{ 0x4F, 0, 0, 0 } ) );
//        newItem.setLabel( item.getLabel() );
//        newItem.setOperation( "NOP" );
//        out.add( newItem );
//        item.setLabel( "" );
//      }
      out.add( ai );
    }
    return out;
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
    { "???", "Nil" },                  { "NOP", "Nil" },
    
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
    
    { "TestRecordKey", "50", "Test if Record key" },  // Test if current key is Record
    { "TestPowerKey", "51", "Test if Power key" },   // Test if current key is Power
    { "TestRepeatingKey", "52", "Test if repeating key" },  // Test if current key is a repeating one, Vol+/-, Ch+/-, FF/Rew, SkipFwd/Back
    { "TestVolKey", "53", "Test if volume key" },     // Test if current key is Vol+/-
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
