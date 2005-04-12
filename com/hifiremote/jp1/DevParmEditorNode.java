package com.hifiremote.jp1;

import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Vector;

public class DevParmEditorNode
  extends HexParmEditorNode
{
  public ProtocolEditorPanel getEditingPanel()
  {
     if ( editorPanel == null )
      editorPanel = new DevParmEditorPanel();
    return editorPanel;
  }

  private static DevParmEditorPanel editorPanel = null;
}
