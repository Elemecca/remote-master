package com.hifiremote.jp1;

import java.util.*;

// TODO: Auto-generated Javadoc
/**
 * The Class ButtonAssignments.
 */
public class ButtonAssignments
{
  
  /**
   * Instantiates a new button assignments.
   */
  public ButtonAssignments(){};

  /**
   * Clear.
   */
  public void clear()
  {
    for ( int i = 0; i < assignedFunctions.length; ++i )
    {
      Function f = assignedFunctions[ i ];
      if ( f != null )
      {
        for ( Iterator< Function.User > it = f.getUsers().iterator(); it.hasNext(); )
        {
          it.next();
          it.remove();
        }

        assignedFunctions[ i ] = null;
      }
    }
  }

  /**
   * Assign.
   * 
   * @param b the b
   * @param f the f
   */
  public void assign( Button b, Function f )
  {
    assign( b, f, Button.NORMAL_STATE );
  }

  /**
   * Assign.
   * 
   * @param b the b
   * @param f the f
   * @param state the state
   */
  public void assign( Button b, Function f, int state )
  {
    short keyCode = b.getKeyCode( state );
    Function oldFunction = assignedFunctions[ keyCode ];
    if ( oldFunction != null )
      oldFunction.removeReference( b, state );

    assignedFunctions[ keyCode ] = f;
    if ( f != null )
      f.addReference( b, state );
  }

  /**
   * Gets the assignment.
   * 
   * @param b the b
   * 
   * @return the assignment
   */
  public Function getAssignment( Button b )
  {
    return getAssignment( b, Button.NORMAL_STATE );
  }

  /**
   * Gets the assignment.
   * 
   * @param b the b
   * @param state the state
   * 
   * @return the assignment
   */
  public Function getAssignment( Button b, int state )
  {
    if ( b.getIsNormal() && ( state != Button.NORMAL_STATE ) && !b.allowsKeyMove( state ))
      return null;
    if ( !b.getIsNormal() && ( state != Button.NORMAL_STATE ))
      return null;
    return assignedFunctions[ b.getKeyCode( state )];
  }

  /**
   * Checks if is empty.
   * 
   * @return true, if is empty
   */
  public boolean isEmpty()
  {
    for ( int i = 0; i < assignedFunctions.length; ++i )
    {
      if ( assignedFunctions[ i ] != null )
        return false;
    }
    return true;
  }

  /** The assigned functions. */
  private Function[] assignedFunctions = new Function[ 256 ];
}
