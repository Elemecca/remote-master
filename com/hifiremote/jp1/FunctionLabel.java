package com.hifiremote.jp1;

import java.awt.Insets;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.awt.Color;
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
    setBorder(
      BorderFactory.createCompoundBorder(
        BorderFactory.createBevelBorder( BevelBorder.RAISED ),
        BorderFactory.createEmptyBorder( 2, 5, 3, 5 )));
    if ( function == null )
      setToolTipText( "Drag or double-click this function to clear the function performed by a button." );
    else
    {
//      if ( function.getNotes() != null )
        setToolTipText( function.getNotes());
//      else
//        setToolTipText( "" );
//      setToolTipText( "Drag or double-click this function to set the function performed by a button." );
    }

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
  }

  public Function getFunction(){ return function; }

  public void showAssigned()
  {
    setForeground( Color.black );
  }

  public void showUnassigned()
  {
    setForeground( Color.red );
  }

  private Function function = null;
  private static Insets insets = null;
  private static MouseMotionAdapter ml = null;
  private static TransferHandler th = null;
}

