package com.hifiremote.jp1;

import java.beans.*;
import java.io.PrintWriter;
import java.util.Enumeration;

public class CmdEditorNode
  extends HexEditorNode
{
  public CmdEditorNode()
  {
    super( "Command Parameters", new Hex( 1 ), "DefaultCmd=", "CmdParms=", "CmdTranslator=" );
  }

  public ProtocolEditorNode createChild()
  {
    return new CmdParmEditorNode();
  }

  public ProtocolEditorPanel getEditingPanel()
  {
    if ( cmdEditorPanel == null )
      cmdEditorPanel = new CmdEditorPanel();
    return cmdEditorPanel;
  }

  private static CmdEditorPanel cmdEditorPanel = null;
}
