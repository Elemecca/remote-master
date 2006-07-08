package com.hifiremote.jp1;

import java.io.PrintWriter;
import java.util.*;

public class GeneralEditorNode
  extends ProtocolEditorNode
{
  public GeneralEditorNode()
  {
     super( "General Settings", false );
     if ( nullHex == null )
       nullHex = new Hex();

     id = nullHex;
     altId = nullHex;
  }

  public ProtocolEditorPanel getEditingPanel()
  {
     if ( editorPanel == null )
      editorPanel = new GeneralEditorPanel();
    return editorPanel;
  }

  private String name = null;
  public String getName(){ return name; }
  public void setName( String newName ){ name = newName; }

  private String oldNames = null;
  public String getOldNames(){ return oldNames; }
  public void setOldNames( String newNames ){ oldNames = newNames; }

  private Hex id = null;
  public Hex getId(){ return id; }
  public void setId( Hex newId )
  {
    if ( newId == null )
      newId = nullHex;
    id = newId; 
  } 
  
  private Hex altId = null;
  public Hex getAltId(){ return altId; }
  public void setAltId( Hex newId )
  {
    if ( newId == null )
      newId = nullHex;
    altId = newId; 
  } 

  private HashMap< String, Hex > codes = new HashMap< String, Hex >( 6 );

  public Set getKeys(){ return codes.keySet(); }
  public void addCode( String processor, Hex code )
  {
    Hex oldCode = getCode( processor );
    codes.put( processor, code );
    firePropertyChange( "Code", oldCode, code );
  }

  public Hex getCode( String processor )
  {
    return ( Hex )codes.get( processor );
  }
  
  public void removeCode( String processor )
  {
    if (( codes.size() == 1 ) && codes.containsKey( processor ))
    {
      Hex oldValue = getCode( processor );
      codes.remove( processor );
      firePropertyChange( "Code", oldValue, null );
    }
  }
  
  public void print( PrintWriter pw )
  {
    pw.println( "[" + name + "]" );
    if (( oldNames != null ) && !oldNames.equals( "" ))
      pw.println( "OldNames=" + oldNames );
    pw.println( "PID=" + id.toString());
    if (( altId != null ) && ( altId.length() > 0 ))
      pw.println( "AlternatePID=" + altId.toString());

    for ( Iterator i = getKeys().iterator(); i.hasNext(); )
    {
      String key = ( String )i.next();
      Hex hex = getCode( key );
      if (( hex != null ) && ( hex.length() != 0 ))
        pw.println( "Code." + key + '=' + hex );
    }
  }
  
  private static GeneralEditorPanel editorPanel = null;
  private static Hex nullHex = null;
}
