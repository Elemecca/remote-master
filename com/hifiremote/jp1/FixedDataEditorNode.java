package com.hifiremote.jp1;

import java.beans.*;
import java.io.PrintWriter;
import java.util.Enumeration;

public class FixedDataEditorNode
  extends ProtocolEditorNode
{
  public FixedDataEditorNode()
  {
     super( "Device Parameters", true );
     fixedData = nullHex;
  }

  public ProtocolEditorPanel getEditingPanel()
  {
     if ( editorPanel == null )
      editorPanel = new FixedDataEditorPanel();
    return editorPanel;
  }

  private Hex fixedData = null;
  public Hex getFixedData(){ return fixedData; }
  public void setFixedData( Hex newData )
  { 
    if ( newData == null )
      newData = nullHex;
    Hex oldData = fixedData;
    fixedData = newData; 
    firePropertyChange( "fixedData", oldData, newData );
  }

  public void print( PrintWriter pw )
  {
    if ( fixedData != nullHex )
      pw.println( "FixedData=" + fixedData.toString());

    // Print the device parameters
    Enumeration children = children();
    if ( !children.hasMoreElements() )
      return;

    pw.print( "DevParms=" );
    while( children.hasMoreElements() )
    {
      DevParmEditorNode node = ( DevParmEditorNode )children.nextElement();
      node.print( pw );
      if ( children.hasMoreElements())
        pw.print( ',' );
    } 
    pw.println();

    // Print the device translators
    pw.print( "DeviceTranslator=" );
    children = children();
    int devParmNumber = 0;
    while( children.hasMoreElements() )
    {
      DevParmEditorNode devNode = ( DevParmEditorNode )children.nextElement();
      Enumeration xlators = devNode.children();
      while ( xlators.hasMoreElements())
      {
        TranslatorEditorNode xlator = ( TranslatorEditorNode )xlators.nextElement();
        pw.print( "Translator(" + devParmNumber + ',' );
        xlator.print( pw );
        pw.print( ')' );
        if ( xlators.hasMoreElements())
          pw.print( ' ' );
      }
      if ( children.hasMoreElements())
        pw.print( ' ' );  
      devParmNumber++;
    } 
    pw.println();
  }

  public boolean canAddChildren()
  {
    if (( fixedData == null ) || ( fixedData == nullHex ) || ( fixedData.length() == 0 ))
      return false;
    return true;
  }

  public ProtocolEditorNode createChild()
  {
    return new DevParmEditorNode();
  }

  private static FixedDataEditorPanel editorPanel = null;
}
