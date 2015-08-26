package com.hifiremote.jp1.assembler;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.hifiremote.jp1.Hex;
import com.hifiremote.jp1.Protocol;
import com.hifiremote.jp1.ProtocolManager;

public class MAXQ610data
{
  private boolean show = false;
  private PrintWriter pw = null;
  private String s = null;
  
  boolean hasNativeCode = false;
  boolean has2usOff = false;
  
  public void analyze()
  {
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
    int pos = 0;
    pos += 3; // skip header
    
    int tbHeader = data[ pos++ ];
    boolean tbHasSpec = ( tbHeader & 0x80 ) != 0;
    int tbSize = tbHeader & 0x3F;
    pos += ( tbHasSpec ? 2 : 0 ) + tbSize;
    
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
  
  private void analyzeProtocolBlock( Hex hex )
  {
    int pos = 0;
    short[] data = hex.getData();
    int pbHeader = data[ pos++ ];
    int pbOptSize = pbHeader & 0x0F;
    int pbSwitchSize = pbOptSize > 2 ? data[ pos + 1 ] & 0x0F : 0;
    
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
      boolean sbHasCode = ( pf0 & 0x80 ) != 0;
      int formatLen = pf0 & 0x07;
      int pf2 = formatLen > 1 ? data[ sigPtr + 2 ] : 0;
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
