package com.hifiremote.jp1;

public abstract class AdvancedCode
{
  public AdvancedCode( int keyCode, Hex data, String notes )
  {
    this.keyCode = keyCode;
    this.data = data;
    this.notes = notes;
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
  
  public abstract Object getValue();
  public abstract void setValue( Object value );

  protected Hex data;
  public Hex getData(){ return data; }
  public void setData( Hex hex )
  {
    if (( data != hex ) && !data.equals( hex ))
      data = hex;
  }

  private String notes;
  public String getNotes(){ return notes; }
  public void setNotes( String notes )
  {
    if (( notes != this.notes ) && !notes.equals( this.notes ))
      this.notes = notes;
  }
}
