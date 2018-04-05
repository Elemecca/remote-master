package com.hifiremote.jp1;

// TODO: Auto-generated Javadoc
/**
 * The Class CmdParmEditorNode.
 */
public class CmdParmEditorNode
  extends HexParmEditorNode
{
  
  /* (non-Javadoc)
   * @see com.hifiremote.jp1.ProtocolEditorNode#getEditingPanel()
   */
  public ProtocolEditorPanel getEditingPanel()
  {
     if ( editorPanel == null )
      editorPanel = new CmdParmEditorPanel();
    return editorPanel;
  }

  /** The editor panel. */
  private static CmdParmEditorPanel editorPanel = null;
}
