package com.hifiremote.jp1;

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
