package com.hifiremote.jp1;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;


public class FocusSelector implements FocusListener, Runnable
{
  public static void selectOnFocus( JTextComponent comp )
  {
    comp.addFocusListener( focusSelector );
  }
  
  @Override
  public void focusGained( FocusEvent e )
  {
    JP1Frame.clearMessage( gotFocus );
    gotFocus = ( JTextComponent )e.getSource();
    JP1Frame.clearMessage( gotFocus );
    SwingUtilities.invokeLater( this );
  }

  @Override
  public void focusLost( FocusEvent e )
  {
    // Do nothing
  }

  // Runnable
  /* (non-Javadoc)
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
    gotFocus.selectAll();
  }

  private static FocusSelector focusSelector = new FocusSelector();
  private JTextComponent gotFocus = null;
}
