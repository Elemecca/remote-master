package com.hifiremote.jp1;

import java.io.PrintWriter;
import java.util.*;

public class CodeEditorNode
  extends ProtocolEditorNode
{
  public CodeEditorNode()
  {
     super( "Protocol Code", false );
  }

  public ProtocolEditorPanel getEditingPanel()
  {
     if ( editorPanel == null )
      editorPanel = new CodeEditorPanel();
    return editorPanel;
  }

  public void print( PrintWriter pw )
  {
    for ( Iterator i = getKeys().iterator(); i.hasNext(); )
    {
      String key = ( String )i.next();
      Hex hex = getCode( key );
      if (( hex != null ) && ( hex.length() != 0 ))
        pw.println( "Code." + key + '=' + hex );
    }
  }

  public Set getKeys(){ return codes.keySet(); }
  public void addCode( String processor, Hex code )
  {
    codes.put( processor, code );
  }

  public Hex getCode( String processor )
  {
    return codes.get( processor );
  }
  
  public void removeCode( String processor )
  {
    codes.remove( processor );
  }
  
  private static CodeEditorPanel editorPanel = null;
  private HashMap< String, Hex > codes = new HashMap< String, Hex >( 6 );
}
