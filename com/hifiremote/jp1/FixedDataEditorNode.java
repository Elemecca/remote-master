package com.hifiremote.jp1;

import java.beans.*;
import java.io.PrintWriter;
import java.util.Enumeration;

public class FixedDataEditorNode
  extends HexEditorNode
{
  public FixedDataEditorNode( int length )
  {
    super( "Device Parameters", null, "FixedData=", "DevParms=", "DeviceTranslator=" );
    this.length = length;
  }

  public ProtocolEditorNode createChild()
  {
    return new DevParmEditorNode();
  }

  public ProtocolEditorPanel getEditingPanel()
  {
    if ( editingPanel == null )
      editingPanel = new FixedDataEditorPanel( 1 );
    return editingPanel;
  }

  public void setLength( int length )
  {
    this.length = length;
    if ( editingPanel != null )
      editingPanel.setLength( length );
  }
  
  private static FixedDataEditorPanel editingPanel = null;
  private int length = 0;
}
