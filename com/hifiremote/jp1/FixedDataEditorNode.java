package com.hifiremote.jp1;

import java.beans.*;
import java.io.PrintWriter;
import java.util.Enumeration;

public class FixedDataEditorNode
  extends HexEditorNode
{
  public FixedDataEditorNode()
  {
    super( "Device Parameters", null, "FixedData=", "DevParms=", "DeviceTranslator=" );
  }

  public ProtocolEditorNode createChild()
  {
    return new DevParmEditorNode();
  }

  public ProtocolEditorPanel getEditingPanel()
  {
    if ( editingPanel == null )
      editingPanel = new FixedDataEditorPanel();
    return editingPanel;
  }
  
  private static FixedDataEditorPanel editingPanel = null;
}
