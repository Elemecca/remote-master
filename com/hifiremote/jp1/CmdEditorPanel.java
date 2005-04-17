package com.hifiremote.jp1;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.text.*;

public class CmdEditorPanel
  extends HexEditorPanel
{
  public CmdEditorPanel()
  {
    super( "Command Parameters", "Command", "Enter the default command for this protocol, in hex.",
           "Add command parameters to allow the user to specify the contents of the command." );
  }
}
