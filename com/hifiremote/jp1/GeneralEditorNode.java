package com.hifiremote.jp1;

import java.io.PrintWriter;

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

  public void print( PrintWriter pw )
  {
    pw.println( "[" + name + "]" );
    if (( oldNames != null ) && !oldNames.equals( "" ))
      pw.println( "OldNames=" + oldNames );
    pw.println( "PID=" + id.toString());
    if (( altId != null ) && ( altId.length() > 0 ))
      pw.println( "AlternatePID=" + altId.toString());
  }
  
  private static GeneralEditorPanel editorPanel = null;
  private static Hex nullHex = null;
}
