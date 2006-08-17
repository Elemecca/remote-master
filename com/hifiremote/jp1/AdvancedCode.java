package com.hifiremote.jp1;

import java.util.Properties;

public abstract class AdvancedCode
{
  public AdvancedCode( int keyCode, Hex data, String notes )
  {
    this.keyCode = keyCode;
    this.data = data;
    this.notes = notes;
  }
  
  public AdvancedCode( Properties props )
  {
    keyCode = Integer.parseInt( props.getProperty( "KeyCode" ));
    data = new Hex( props.getProperty( "Data" ));
    notes = props.getProperty( "Notes" );
  }

  private int keyCode;
  public int getKeyCode(){ return keyCode; }
  public void setKeyCode( int keyCode )
  {
    if ( this.keyCode != keyCode )
    {
      this.keyCode = keyCode;
    }
  }
  
  public abstract String getValueString( RemoteConfiguration remoteConfig );

  protected Hex data;
  public Hex getData(){ return data; }
  public void setData( Hex hex )
  {
    if (( data != hex ) && !data.equals( hex ))
      data = hex;
  }

  private String notes = null;
  public String getNotes(){ return notes; }
  public void setNotes( String notes )
  {
    if (( notes != this.notes ) && !notes.equals( this.notes ))
      this.notes = notes;
  }
  
  public void store( PropertyWriter pw )
  {
    pw.print( "KeyCode", keyCode );
    pw.print( "Data", data );
    if (( notes != null ) && ( notes.length() > 0 ))
      pw.print( "Notes", notes );
  }
}
