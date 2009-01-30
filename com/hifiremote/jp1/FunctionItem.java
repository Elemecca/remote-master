package com.hifiremote.jp1;

import javax.swing.JMenuItem;

// TODO: Auto-generated Javadoc
/**
 * The Class FunctionItem.
 */
public class FunctionItem
  extends JMenuItem
{
  
  /**
   * Instantiates a new function item.
   * 
   * @param function the function
   */
  public FunctionItem( Function function )
  {
    this.function = function;
    if ( function == null )
      setText( "- none -" );
    else
    {
      setText( function.getName());
      setToolTipText( function.getNotes());
    }
  }

  /**
   * Gets the function.
   * 
   * @return the function
   */
  public Function getFunction(){ return function; }
  
  /** The function. */
  private Function function = null;
}

