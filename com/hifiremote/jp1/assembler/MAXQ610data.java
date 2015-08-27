package com.hifiremote.jp1.assembler;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.hifiremote.jp1.Hex;
import com.hifiremote.jp1.MAXQProcessor;
import com.hifiremote.jp1.Processor;
import com.hifiremote.jp1.Protocol;
import com.hifiremote.jp1.ProtocolManager;

public class MAXQ610data
{
  private boolean show = false;
  private PrintWriter pw = null;
  private String s = null;
  private Processor proc = null;
  private int carrier = 0;
  private int altCarrier = 0;
  private short[] tbLengths = null;
  private int[] tbDurations = null;
  
  private boolean hasNativeCode = false;
  private boolean has2usOff = false;
  
  public void analyze()
  {
    proc = new MAXQProcessor( "MAXQ610" );
    try
    {
      FileWriter fw = new FileWriter( "MAXQprotocols.txt" );
      pw = new PrintWriter( fw );
      ProtocolManager pm = ProtocolManager.getProtocolManager();
      for ( String name : pm.getNames() )
      {
        for ( Protocol p : pm.findByName( name ) )
        {
          show = false;
          Hex hex = p.getCode().get( "MAXQ610");
          if ( hex != null )
          {
            s = p.getName() + ": PID=" + p.getID().toString().replaceAll( "\\s", "" );
            String var = p.getVariantName();
            if ( var != null && !var.isEmpty() )
            {
              s += "." + var;
            }
            s += "\n";
            analyzeExecutor( hex );
            s += "\n";
            show = true;
            if ( show )
            {
              pw.print( s.replaceAll( "\\n", System.getProperty("line.separator" ) ) );
            }
          }
        }
      }
      pw.close();
      fw.close();
    }
    catch ( IOException e )
    {
      e.printStackTrace();
    }
  }
  
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
  
  private void analyzeSingleExec( Hex hex )
  {
    short[] data = hex.getData();
    analyzeHeader( hex.subHex( 0,  3 ) );
    int pos = 0;
    pos += 3; // skip header
    
    int tbHeader = data[ pos ];
    boolean tbHasSpec = ( tbHeader & 0x80 ) != 0;
    int tbSize = ( tbHeader & 0x3F ) + ( tbHasSpec ? 3 : 1 );
    analyzeTimingBlock( hex.subHex( pos, tbSize ));
    pos += tbSize;
    
    int dbHeader = data[ pos++ ];
    int dbSize = dbHeader & 0x07;
    hasNativeCode = ( dbHeader & 0x80 ) != 0;
    has2usOff = ( dbHeader & 0x40 ) != 0;
    boolean hasAltExec = ( dbHeader & 0x20 ) != 0;
    int altCount = !hasAltExec && dbSize > 1 ? data[ pos ] & 0x0F : 0;
    pos += dbSize;  // pos points to first (or only) protocol block
    for ( int i = 0; i <= altCount; i++ )
    {
      int pbOptionsSize = data[ pos ] & 0x0F;
      int pbOffset = pbOptionsSize > 0 ? data[ pos + 1 ] : 0;
      if ( altCount > 0 )
      {
        s += "Protocol block " + ( i + 1 ) + "\n";
      }
      if ( pbOffset > 0 && i < altCount )
      {
        analyzeProtocolBlock( hex.subHex( pos, pbOffset ) );
        pos += pbOffset;
      }
      else
      {
        analyzeProtocolBlock( hex.subHex( pos ) );
        if ( i < altCount )
        {
          s += "*** Fewer than " + ( altCount + 1 ) + " protocol blocks ***\n";
        }
        break;
      } 
    }
  }
  
  private void analyzeHeader( Hex hex )
  {
    short[] data = hex.getData();
    int on = data[ 0 ];
    int off = data[ 1 ];
    if ( on == 0 && off == 0 )
    {
      on = 6;
      off = 6;
      carrier = on + off;
      s += "Carrier is unmodulated\n";
    }
    else
    {
      on++;
      off++;
      carrier = on + off;
      s += String.format( "%.2fkHz, duty cycle %.1f", 6000.0 / carrier, ( 100.0 * on )/ carrier ) + "%\n";
    }
    int args = data[ 2 ];
    s += "" + ( args >> 4 ) + " fixed, " + (args & 0x0F ) + " variable bytes\n";
  }
  
  private void analyzeTimingBlock( Hex hex )
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
  
  private String getTimingItem( int n )
  {
    int pos = 0;
    int[] sizes = new int[]{ 2, 2, 1, 2, 1, 2, 2, 1 };
    int itemCarrier = carrier;
    if ( n == 1 && altCarrier != 0 )
    {
      itemCarrier = altCarrier;
    }
    int mult = has2usOff ? 12 : itemCarrier;
    String str = "";
    for ( int i = 0; i < n; i++ )
    {
      pos += tbLengths[ i ] * sizes[ i ];
    }
    if ( ( pos + tbLengths[ n ] * sizes[ n ] ) > tbDurations.length )
    {
      return str;
    }
    for ( int i = 0; i < tbLengths[ n ]; i++ )
    {
      if ( sizes[ n ] == 2 )
      {
        int val = ( tbDurations[ pos++ ]*itemCarrier + 3 )/6;  // convert carrier cycles to us
        if ( val > 0 )
        {
          str += str.isEmpty() ? "" : ",";
          str += "+" + val;
        }
        val = ( tbDurations[ pos++ ]*mult + 3 )/6;  // convert carrier cycles to us
        if ( val > 0 )
        {
          str += str.isEmpty() ? "" : ",";
          str += "-" + val;
        }
        if ( !str.isEmpty() && i == tbLengths[ n ] - 1 )
        {
          str += "\n";
        }
      }
      else
      {
        int val = 0;
        for ( ; i < tbLengths[ n ]; i++ )
        {
          val = ( val << 16 ) + tbDurations[ pos++ ];
        }
        if ( n != 7 )
        {
          val = ( val * mult + 3 )/6;
          if ( val > 0 )
          {
            str += "-" + val + "\n";
          }
        }
        else if ( val > 0 )
        {
          altCarrier = ( val & 0xFF ) + ( ( val >> 8 ) & 0xFF ) + 2;
          str += "+" + ( val & 0xFF ) + ",-" + ( ( val >> 8 ) & 0xFF ) + "\n";
        }
      }
    }
    return str;
  }
  
  private void analyzeProtocolBlock( Hex hex )
  {
    int pos = 0;
    short[] data = hex.getData();
    int pbHeader = data[ pos++ ];
    int pbOptSize = pbHeader & 0x0F;
    int pbSwitchSize = pbOptSize > 2 ? data[ pos + 1 ] & 0x0F : 0;
    int codeSpec = pbOptSize > 3 ? data[ pos + 3 ] : 0;
    int toggle = pbOptSize > 5 ? hex.get( pos + 4 ) : 0;
    int codeSelector = codeSpec & 0x0F;
    
    if ( ( codeSpec & 0x10 ) != 0 )
    {
      // Set altCarrier
      getTimingItem( 7 );
    }
    else
    {
      altCarrier = 0;
    }
    
    if ( codeSelector != 1 )
    {
      String str = getTimingItem( 0 );
      if ( !str.isEmpty() )
      {
        s += "1-bursts (us): " + str;
      }
      str = getTimingItem( 1 );
      if ( !str.isEmpty() )
      {
        s += "0-bursts (us): " + str;
      }
    }
    else
    {
      int size = ( tbLengths[ 0 ] + tbLengths[ 1 ] ) > 7 ? 2 : 1;
      int mult = has2usOff ? 12 : carrier;
      int n = 0;

      for ( int i = 0; i < 4; i++ )
      {
        String str = "";
        for ( int j = 0; j < size; j++ )
        {
          int val = ( tbDurations[ n++ ]*carrier + 3 )/6;  // convert carrier cycles to us
          if ( val > 0 )
          {
            str += str.isEmpty() ? "" : ",";
            str += "+" + val;
          }
          val = ( tbDurations[ n++ ]*mult + 3 )/6;  // convert carrier cycles to us
          if ( val > 0 )
          {
            str += str.isEmpty() ? "" : ",";
            str += "-" + val;
          }
          if ( j == size - 1 )
          {
            str += "\n";
          }
        }
        s += "Bursts for bit-pair " + ( new String[]{ "00", "01", "10", "11" } )[ i ] + " (us): " + str;
      }
    }

    String str = getTimingItem( 2 );
    if ( !str.isEmpty() )
    {
      s += "lead-out (us): " + str;
    }
    str = getTimingItem( 3 );
    if ( !str.isEmpty() )
    {
      s += "lead-in (us): " + str;
    }
    str = getTimingItem( 4 );
    if ( !str.isEmpty() )
    {
      s += "alternate lead-out (us): " + str;
    }
    str = getTimingItem( 5 );
    if ( !str.isEmpty() )
    {
      s += "alternate lead-in (us): " + str;
    }
    str = getTimingItem( 6 );
    if ( !str.isEmpty() )
    {
      s += "mid-frame burst (us): " + str;
    }
    if ( ( codeSpec & 0x10 ) != 0 )
    {
      str = getTimingItem( 7 );
      if ( !str.isEmpty() )
      {
        s += "0-burst carrier times (units of 1/6us): " + str;
      }
    }

    pos += pbOptSize;   // pos points to code block if present, else signal block
    boolean pbHasCode = ( pbHeader & 0x80 ) != 0;
    if ( pbHasCode )
    {
      int cbSize = data[ pos++ ];
      pos += cbSize;
    }

    // pos now points to signal block
    boolean more = true;
    while ( more )
    {
      int sigPtr = pos;
      if ( pos >= data.length )
      {
        s += "*** Signal block missing ***\n";
        return;
      }
      int pf0 = data[ pos++ ];
      int formatLen = pf0 & 0x07;
      int pf1 = formatLen > 0 ? data[ sigPtr + 1 ] : 0;
      int pf2 = formatLen > 1 ? data[ sigPtr + 2 ] : 0;
      int pf3 = formatLen > 2 ? data[ sigPtr + 3 ] : 0;
      int pf4 = formatLen > 3 ? data[ sigPtr + 4 ] : 0;
      if ( ( pf1 & 0x40 ) != 0 )
      {
        s += "Uses " + ( ( pf1 & 0x01 ) != 0 ? "alternate" : "normal" ) + " lead-in\n";
      }
      else
      {
        s += "There is no lead-in\n";
      }
      if ( ( pf4 & 0x80 ) != 0 )
      {
        s += "Mid-frame burst follows first " + ( pf4 & 0x7F ) + " data bits\n";
      }
      if ( ( pf1 & 0x08 ) != 0 )
      {
        s += "Data followed by ";
        s += ( pf3 & 0x40 ) != 0 ? "alternate lead-in " : "normal lead-in ";
        s += "as end-frame burst\n";
      }
      if ( ( pf1 & 0x04 ) != 0 )
      {
        s += "One-ON precedes lead-out\n";
      }
      if ( ( pf0 & 0x40 ) != 0 )
      {
        s += "Uses " + ( ( pf1 & 0x02 ) != 0 ? "alternate" : "normal" ) + " lead-out\n";
        s += "Lead-out is " + ( ( ( pf0 & 0x02 ) != 0 ) ? "total time\n" : "gap time\n" );
      }
      else
      {
        s += "There is no lead-out\n";
      }
      
     

      boolean sbHasCode = ( pf0 & 0x80 ) != 0;

      
      int txCount = pf2 & 0x1F;
      pos += formatLen;
      for ( int i = 0; i <= pbSwitchSize; i++ )
      {
        Hex txBytes = hex.subHex( pos, txCount );
        s += "TX bytes";
        if ( pbSwitchSize > 0 )
        {
          s += " " + i;
        }     
        s += ": " + txBytes + "\n";
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
          s += "*** Excess data in protocol block ***\n";
        }
        return;
      }
      int pf5 = formatLen > 4 ? data[ sigPtr + 5 ] : 0;
      more = ( pf5 & 0xF0 ) != 0;
      if ( more )
      {
        s += "*** Alternate signal block selected according to PF5 ***";
        continue;
      }
      if ( sbHasCode )
      {
        int cbSize = data[ pos++ ];
        pos += cbSize;
      }
      more = !hasNativeCode && pos < data.length;
      if ( more )
      {
        if ( pos == data.length - 1 )
        {
          s += "*** Apparent spurious extra byte at end of protocol block ***\n";
          more = false;
        }
        else
        {
          s += "Alternate signal block selected according to PF2 bits 5-7\n";
        }
      }
    }
    if ( hasNativeCode )
    {
      s += "Native code block:\n";
      Hex nCode = hex.subHex( pos );
      s += nCode.toString() + "\n";
      pos += nCode.length();
    }
    if ( pos != data.length )
    {
      s += "**** Parsing error ****\n";
    }
  }
  
  
}
