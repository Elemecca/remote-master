package com.hifiremote.jp1;


// TODO: Auto-generated Javadoc
/**
 * The Class FixedDataEditorNode.
 */
public class FixedDataEditorNode
  extends HexEditorNode
{
  
  /**
   * Instantiates a new fixed data editor node.
   * 
   * @param length the length
   */
  public FixedDataEditorNode( int length )
  {
    super( "Device Parameters", null, "FixedData=", "DevParms=", "DeviceTranslator=" );
 }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.ProtocolEditorNode#createChild()
   */
  public ProtocolEditorNode createChild()
  {
    return new DevParmEditorNode();
  }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.ProtocolEditorNode#getEditingPanel()
   */
  public ProtocolEditorPanel getEditingPanel()
  {
    if ( editingPanel == null )
      editingPanel = new FixedDataEditorPanel( 1 );
    return editingPanel;
  }

  /**
   * Sets the length.
   * 
   * @param length the new length
   */
  public void setLength( int length )
  {
    if ( editingPanel != null )
      editingPanel.setLength( length );
  }
  
  /** The editing panel. */
  private static FixedDataEditorPanel editingPanel = null;
}
