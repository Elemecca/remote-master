package com.hifiremote.jp1;

import java.util.*;

public class ButtonAssignments
{
  public ButtonAssignments(){};
  
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
  
  public void assign( Button b, Function f )
  {
    assign( b, f, Button.NORMAL_STATE );
  }
  
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
  
  public Function getAssignment( Button b )
  {
    return getAssignment( b, Button.NORMAL_STATE );
  }
  
  public Function getAssignment( Button b, int state )
  {
    return assignedFunctions[ b.getKeyCode( state )];
  }
  
  private Function[] assignedFunctions = new Function[ 256 ];
}
