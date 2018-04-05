package com.hifiremote.jp1;

import java.io.PrintWriter;
import java.util.*;

// TODO: Auto-generated Javadoc
/**
 * The Class GeneralEditorNode.
 */
public class GeneralEditorNode
  extends ProtocolEditorNode
{
  
  /**
   * Instantiates a new general editor node.
   */
  public GeneralEditorNode()
  {
     super( "General Settings", false );
     if ( nullHex == null )
       nullHex = new Hex();

     id = nullHex;
     altId = nullHex;
  }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.ProtocolEditorNode#getEditingPanel()
   */
  public ProtocolEditorPanel getEditingPanel()
  {
     if ( editorPanel == null )
      editorPanel = new GeneralEditorPanel();
    return editorPanel;
  }

  /** The name. */
  private String name = null;
  
  /* (non-Javadoc)
   * @see com.hifiremote.jp1.ProtocolEditorNode#getName()
   */
  public String getName(){ return name; }
  
  /* (non-Javadoc)
   * @see com.hifiremote.jp1.ProtocolEditorNode#setName(java.lang.String)
   */
  public void setName( String newName ){ name = newName; }

  /** The old names. */
  private String oldNames = null;
  
  /**
   * Gets the old names.
   * 
   * @return the old names
   */
  public String getOldNames(){ return oldNames; }
  
  /**
   * Sets the old names.
   * 
   * @param newNames the new old names
   */
  public void setOldNames( String newNames ){ oldNames = newNames; }

  /** The id. */
  private Hex id = null;
  
  /**
   * Gets the id.
   * 
   * @return the id
   */
  public Hex getId(){ return id; }
  
  /**
   * Sets the id.
   * 
   * @param newId the new id
   */
  public void setId( Hex newId )
  {
    if ( newId == null )
      newId = nullHex;
    id = newId; 
  } 
  
  /** The alt id. */
  private Hex altId = null;
  
  /**
   * Gets the alt id.
   * 
   * @return the alt id
   */
  public Hex getAltId(){ return altId; }
  
  /**
   * Sets the alt id.
   * 
   * @param newId the new alt id
   */
  public void setAltId( Hex newId )
  {
    if ( newId == null )
      newId = nullHex;
    altId = newId; 
  } 

  /** The codes. */
  private HashMap< String, Hex > codes = new HashMap< String, Hex >( 6 );

  /**
   * Gets the keys.
   * 
   * @return the keys
   */
  public Set< String > getKeys(){ return codes.keySet(); }
  
  /**
   * Adds the code.
   * 
   * @param processor the processor
   * @param code the code
   */
  public void addCode( String processor, Hex code )
  {
    Hex oldCode = getCode( processor );
    codes.put( processor, code );
    firePropertyChange( "Code", oldCode, code );
  }

  /**
   * Gets the code.
   * 
   * @param processor the processor
   * 
   * @return the code
   */
  public Hex getCode( String processor )
  {
    return ( Hex )codes.get( processor );
  }
  
  /**
   * Removes the code.
   * 
   * @param processor the processor
   */
  public void removeCode( String processor )
  {
    if (( codes.size() == 1 ) && codes.containsKey( processor ))
    {
      Hex oldValue = getCode( processor );
      codes.remove( processor );
      firePropertyChange( "Code", oldValue, null );
    }
  }
  
  /* (non-Javadoc)
   * @see com.hifiremote.jp1.ProtocolEditorNode#print(java.io.PrintWriter)
   */
  public void print( PrintWriter pw )
  {
    pw.println( "[" + name + "]" );
    if (( oldNames != null ) && !oldNames.equals( "" ))
      pw.println( "OldNames=" + oldNames );
    pw.println( "PID=" + id.toString());
    if (( altId != null ) && ( altId.length() > 0 ))
      pw.println( "AlternatePID=" + altId.toString());

    for ( Iterator< String > i = getKeys().iterator(); i.hasNext(); )
    {
      String key = ( String )i.next();
      Hex hex = getCode( key );
      if (( hex != null ) && ( hex.length() != 0 ))
        pw.println( "Code." + key + '=' + hex );
    }
  }
  
  /** The editor panel. */
  private static GeneralEditorPanel editorPanel = null;
  
  /** The null hex. */
  private static Hex nullHex = null;
}
