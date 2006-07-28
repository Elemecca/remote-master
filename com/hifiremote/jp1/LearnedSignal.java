package com.hifiremote.jp1;

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
    pw.print( "DeviceButtonINdex", deviceButtonIndex );
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
}
