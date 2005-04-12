package com.hifiremote.jp1;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.text.*;

public class FixedDataEditorPanel
  extends HexEditorPanel
{
  public FixedDataEditorPanel()
  {
    super( "Device Parameters", "Fixed data", "Enter the fixed data for this protocol, in hex.",
           "If the protocol uses any fixed data bytes, enter them below.\n\nOnce that has been done, add device parameters to allow the user to specify the contents of the Fixed Data." );
  }
}
