package com.hifiremote.jp1;

import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

public class FunctionLabel
  extends JLabel
{
  public FunctionLabel( Function function )
  {
    if ( function == null )
      setText( "- none -" );
    else
    {
      setText( function.getName());
      if ( function.assigned() )
        showAssigned();
      else
        showUnassigned();
    }
    setHorizontalAlignment( SwingConstants.CENTER );
    Border border = 
      BorderFactory.createCompoundBorder(
        BorderFactory.createBevelBorder( BevelBorder.RAISED ),
        BorderFactory.createEmptyBorder( 2, 5, 3, 5 ));
    setBorder( border );

    if ( th == null )
    {
      th = new TransferHandler()
      {
        protected Transferable createTransferable( JComponent c )
        {
          return new LocalObjectTransferable((( FunctionLabel )c ).function );
        }

        public int getSourceActions( JComponent c )
        {
          return TransferHandler.COPY_OR_MOVE;
        }
      };
    }
    setTransferHandler( th );

    if ( ml == null )
    {
      ml = new MouseMotionAdapter()
      {
        public void mouseDragged( MouseEvent e )
        {
          JComponent c = ( JComponent )e.getSource();
          TransferHandler th = c.getTransferHandler();
          th.exportAsDrag( c, e, TransferHandler.COPY );
        }
      };
    }

    addMouseMotionListener( ml );

    this.function = function;
    updateToolTipText();
  }

  public Function getFunction(){ return function; }

  public void updateToolTipText()
  {
    StringBuilder buff = new StringBuilder( 400 );
    buff.append( "<html>" );
    if ( function == null )
      buff.append( "&nbsp;Drag or double-click this function to<br>&nbsp;clear the function performed by a button." );
    else
    {
      if ( function.getNotes() != null )
        buff.append( "&nbsp;" + function.getNotes());
      else
        buff.append( "&nbsp;Drag or double-click this function to<br>&nbsp;set the function performed by a button." );
      Enumeration e = function.getUsers();
      if ( e.hasMoreElements())
      {
        buff.append( "<br><hr>&nbsp;" + function.getName() + " is assigned to: " );
        boolean first = true;
        while ( e.hasMoreElements())
        {
          if ( first )
            first = false;
          else
            buff.append( ", " );
          Function.User user = ( Function.User )e.nextElement();
          Button b = user.button;
          int state = user.state;
          if ( state == Button.NORMAL_STATE )
            buff.append( b.getName());
          else if ( state == Button.SHIFTED_STATE )
            buff.append( b.getShiftedName());
          else if ( state == Button.XSHIFTED_STATE )
            buff.append( b.getXShiftedName());
        }
      }
    }
    buff.append( "</html>" );
    setToolTipText( buff.toString());
  }

  public void showAssigned()
  {
    setForeground( Color.black );
  }

  public void showUnassigned()
  {
    setForeground( Color.red );
  }

  public Dimension getPreferredSize()
  {
    Dimension d = super.getPreferredSize();
    if ( d.width > 180 )
      d.width = 180;
    return d;
  }

  private Function function = null;
  private static MouseMotionAdapter ml = null;
  private static TransferHandler th = null;
}

