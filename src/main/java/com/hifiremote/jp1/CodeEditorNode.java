package com.hifiremote.jp1;

import java.io.PrintWriter;
import java.util.*;

// TODO: Auto-generated Javadoc
/**
 * The Class CodeEditorNode.
 */
public class CodeEditorNode
  extends ProtocolEditorNode
{
  
  /**
   * Instantiates a new code editor node.
   */
  public CodeEditorNode()
  {
     super( "Protocol Code", false );
  }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.ProtocolEditorNode#getEditingPanel()
   */
  public ProtocolEditorPanel getEditingPanel()
  {
     if ( editorPanel == null )
      editorPanel = new CodeEditorPanel();
    return editorPanel;
  }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.ProtocolEditorNode#print(java.io.PrintWriter)
   */
  public void print( PrintWriter pw )
  {
    for ( Iterator< String > i = getKeys().iterator(); i.hasNext(); )
    {
      String key = ( String )i.next();
      Hex hex = getCode( key );
      if (( hex != null ) && ( hex.length() != 0 ))
        pw.println( "Code." + key + '=' + hex );
    }
  }

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
    codes.put( processor, code );
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
    return codes.get( processor );
  }
  
  /**
   * Removes the code.
   * 
   * @param processor the processor
   */
  public void removeCode( String processor )
  {
    codes.remove( processor );
  }
  
  /** The editor panel. */
  private static CodeEditorPanel editorPanel = null;
  
  /** The codes. */
  private HashMap< String, Hex > codes = new HashMap< String, Hex >( 6 );
}
