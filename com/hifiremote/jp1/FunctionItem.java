package com.hifiremote.jp1;

import java.awt.Insets;
import java.awt.event.*;
import java.awt.datatransfer.*;
import javax.swing.*;
import javax.swing.event.*;

public class FunctionItem
  extends JMenuItem
{
  public FunctionItem( Function function )
  {
    this.function = function;
    if ( function == null )
      setText( "- none -" );
    else
      setText( function.toString());
  }

  public Function getFunction(){ return function; }
  private Function function = null;
}

