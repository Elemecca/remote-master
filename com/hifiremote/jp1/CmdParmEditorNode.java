package com.hifiremote.jp1;

import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Vector;

public class CmdParmEditorNode
  extends HexParmEditorNode
{
  public ProtocolEditorPanel getEditingPanel()
  {
     if ( editorPanel == null )
      editorPanel = new CmdParmEditorPanel();
    return editorPanel;
  }

  private static CmdParmEditorPanel editorPanel = null;
}
