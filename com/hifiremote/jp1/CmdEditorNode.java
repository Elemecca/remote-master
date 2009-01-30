package com.hifiremote.jp1;

// TODO: Auto-generated Javadoc
/**
 * The Class CmdEditorNode.
 */
public class CmdEditorNode
  extends HexEditorNode
{
  
  /**
   * Instantiates a new cmd editor node.
   * 
   * @param length the length
   */
  public CmdEditorNode( int length )
  {
    super( "Command Parameters", null, "DefaultCmd=", "CmdParms=", "CmdTranslator=" );
    this.length = length;
  }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.ProtocolEditorNode#createChild()
   */
  public ProtocolEditorNode createChild()
  {
    return new CmdParmEditorNode();
  }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.ProtocolEditorNode#getEditingPanel()
   */
  public ProtocolEditorPanel getEditingPanel()
  {
    if ( cmdEditorPanel == null )
      cmdEditorPanel = new CmdEditorPanel( length );
    return cmdEditorPanel;
  }

  /**
   * Sets the length.
   * 
   * @param length the new length
   */
  public void setLength( int length )
  {
    this.length = length;
    if ( cmdEditorPanel != null )
      cmdEditorPanel.setLength( length );
  }

  /** The cmd editor panel. */
  private static CmdEditorPanel cmdEditorPanel = null;
  
  /** The length. */
  private int length = 0;
}
