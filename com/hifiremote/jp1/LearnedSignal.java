package com.hifiremote.jp1;

import com.hifiremote.decodeir.*;
import java.io.*;
import java.util.*;

public class LearnedSignal
{
  public LearnedSignal( int keyCode, int deviceButtonIndex, Hex data, String notes )
  {
    this.keyCode = keyCode;
    this.deviceButtonIndex = deviceButtonIndex;
    this.data = data;
    this.notes = notes;
  }

  public LearnedSignal( Properties properties )
  {
    keyCode = Integer.parseInt( properties.getProperty( "KeyCode" ));
    deviceButtonIndex = Integer.parseInt( properties.getProperty( "DeviceButtonIndex" ));
    data = new Hex( properties.getProperty( "Data" ));
    notes = properties.getProperty( "Notes" );
  }

  public void store( PropertyWriter pw )
  {
    pw.print( "KeyCode", keyCode );
    pw.print( "DeviceButtonIndex", deviceButtonIndex );
    pw.print( "Data", data );
    if (( notes != null ) && !notes.equals( "" ));
      pw.print( "Notes", notes );
  }

  private int keyCode;
  public int getKeyCode(){ return keyCode; }
  public void setKeyCode( int code ){ keyCode = code; }

  private int deviceButtonIndex;
  public int getDeviceButtonIndex(){ return deviceButtonIndex; }
  public void setDeviceButtonIndex( int newIndex )
  {
    deviceButtonIndex = newIndex;
  }

  private Hex data = null;
  public Hex getData(){ return data; }
  public void setData( Hex hex ){ data = hex; }

  private String notes = null;
  public String getNotes(){ return notes; }
  public void setNotes( String text ){ notes = text; }

  private UnpackLearned unpackLearned = null;
  public UnpackLearned getUnpackLearned()
  {
    if ( unpackLearned == null )
      unpackLearned = new UnpackLearned( data );
    return unpackLearned;
  }

  private ArrayList< LearnedSignalDecode > decodes = null;
  public ArrayList< LearnedSignalDecode > getDecodes()
  {
    if ( decodes == null )
    {
      UnpackLearned ul = getUnpackLearned();
      if ( !ul.ok )
        return null;
      getDecodeIR();
      decodeIR.setBursts( ul.durations, ul.repeat );
      decodeIR.setFrequency( ul.frequency );
      decodeIR.initDecoder();
      decodes = new ArrayList< LearnedSignalDecode >();
      while ( decodeIR.decode())
        decodes.add( new LearnedSignalDecode( decodeIR ));
    }
    return decodes;
  }

  public static DecodeIRCaller getDecodeIR()
  {
    if ( decodeIR == null )
      decodeIR = new DecodeIRCaller( new File( System.getProperty( "user.dir" )));

    return decodeIR;
  }

  private static DecodeIRCaller decodeIR = null;
}
