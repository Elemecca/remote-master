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
  public FixedDataEditorPanel( int hexLength )
  {
    super( "Device Parameters", "Fixed data", "Enter the default fixed data for this protocol, in hex.",
           "Enter the default fixed data below.", hexLength );
  }
}
