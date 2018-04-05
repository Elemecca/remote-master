package com.hifiremote.jp1;


// TODO: Auto-generated Javadoc
/**
 * The Class FixedDataEditorPanel.
 */
public class FixedDataEditorPanel
  extends HexEditorPanel
{
  
  /**
   * Instantiates a new fixed data editor panel.
   * 
   * @param hexLength the hex length
   */
  public FixedDataEditorPanel( int hexLength )
  {
    super( "Device Parameters", "Fixed data", "Enter the default fixed data for this protocol, in hex.",
           "Enter the default fixed data below.", hexLength );
  }
}
