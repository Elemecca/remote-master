package com.hifiremote.jp1;

import java.util.Properties;

// TODO: Auto-generated Javadoc
/**
 * The Class AdvancedCode.
 */
public abstract class AdvancedCode
{
  
  /**
   * Instantiates a new advanced code.
   * 
   * @param keyCode the key code
   * @param data the data
   * @param notes the notes
   */
  public AdvancedCode( int keyCode, Hex data, String notes )
  {
    this.keyCode = keyCode;
    this.data = data;
    this.notes = notes;
  }
  
  /**
   * Instantiates a new advanced code.
   * 
   * @param props the props
   */
  public AdvancedCode( Properties props )
  {
    keyCode = Integer.parseInt( props.getProperty( "KeyCode" ));
    data = new Hex( props.getProperty( "Data" ));
    notes = props.getProperty( "Notes" );
  }

  /** The key code. */
  private int keyCode;
  
  /**
   * Gets the key code.
   * 
   * @return the key code
   */
  public int getKeyCode(){ return keyCode; }
  
  /**
   * Sets the key code.
   * 
   * @param keyCode the new key code
   */
  public void setKeyCode( int keyCode )
  {
    if ( this.keyCode != keyCode )
    {
      this.keyCode = keyCode;
    }
  }
  
  /**
   * Gets the value string.
   * 
   * @param remoteConfig the remote config
   * 
   * @return the value string
   */
  public abstract String getValueString( RemoteConfiguration remoteConfig );

  /** The data. */
  protected Hex data;
  
  /**
   * Gets the data.
   * 
   * @return the data
   */
  public Hex getData(){ return data; }
  
  /**
   * Sets the data.
   * 
   * @param hex the new data
   */
  public void setData( Hex hex )
  {
    if (( data != hex ) && !data.equals( hex ))
      data = hex;
  }

  /** The notes. */
  private String notes = null;
  
  /**
   * Gets the notes.
   * 
   * @return the notes
   */
  public String getNotes(){ return notes; }
  
  /**
   * Sets the notes.
   * 
   * @param notes the new notes
   */
  public void setNotes( String notes )
  {
    if (( notes != this.notes ) && !notes.equals( this.notes ))
      this.notes = notes;
  }
  
  /**
   * Store.
   * 
   * @param pw the pw
   */
  public void store( PropertyWriter pw )
  {
    pw.print( "KeyCode", keyCode );
    pw.print( "Data", data );
    if (( notes != null ) && ( notes.length() > 0 ))
      pw.print( "Notes", notes );
  }
}
