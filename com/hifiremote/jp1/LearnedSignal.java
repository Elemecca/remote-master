package com.hifiremote.jp1;

public class LearnedSignal
{
  public LearnedSignal( int keyCode, int deviceButtonIndex, Hex data, String notes )
  {
    this.keyCode = keyCode;
    this.deviceButtonIndex = deviceButtonIndex;
    this.data = data;
    this.notes = notes;
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
