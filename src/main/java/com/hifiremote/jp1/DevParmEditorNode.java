package com.hifiremote.jp1;

// TODO: Auto-generated Javadoc
/**
 * The Class DevParmEditorNode.
 */
public class DevParmEditorNode
  extends HexParmEditorNode
{
  
  /* (non-Javadoc)
   * @see com.hifiremote.jp1.ProtocolEditorNode#getEditingPanel()
   */
  public ProtocolEditorPanel getEditingPanel()
  {
     if ( editorPanel == null )
      editorPanel = new DevParmEditorPanel();
    return editorPanel;
  }

  /** The editor panel. */
  private static DevParmEditorPanel editorPanel = null;
}
