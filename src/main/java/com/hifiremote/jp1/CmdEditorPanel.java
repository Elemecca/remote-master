package com.hifiremote.jp1;

// TODO: Auto-generated Javadoc
/**
 * The Class CmdEditorPanel.
 */
public class CmdEditorPanel
  extends HexEditorPanel
{
  
  /**
   * Instantiates a new cmd editor panel.
   * 
   * @param length the length
   */
  public CmdEditorPanel( int length )
  {
    super( "Command Parameters", "Command", "Enter the default command for this protocol, in hex.",
           "Add command parameters to allow the user to specify the contents of the command.", length );
  }
}
