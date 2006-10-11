package com.hifiremote.jp1;

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
