package com.hifiremote.jp1;

import java.beans.*;
import java.io.PrintWriter;
import java.util.Enumeration;

public class CmdEditorNode
  extends HexEditorNode
{
  public CmdEditorNode( int length )
  {
    super( "Command Parameters", null, "DefaultCmd=", "CmdParms=", "CmdTranslator=" );
    this.length = length;
  }

  public ProtocolEditorNode createChild()
  {
    return new CmdParmEditorNode();
  }

  public ProtocolEditorPanel getEditingPanel()
  {
    if ( cmdEditorPanel == null )
      cmdEditorPanel = new CmdEditorPanel( length );
    return cmdEditorPanel;
  }

  public void setLength( int length )
  {
    this.length = length;
    if ( cmdEditorPanel != null )
      cmdEditorPanel.setLength( length );
  }

  private static CmdEditorPanel cmdEditorPanel = null;
  private int length = 0;
}
