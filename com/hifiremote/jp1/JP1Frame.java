package com.hifiremote.jp1;

import java.awt.*;
import javax.swing.*;

// TODO: Auto-generated Javadoc
/**
 * The Class JP1Frame.
 */
public class JP1Frame extends JFrame
{
  
  /**
   * Instantiates a new j p1 frame.
   * 
   * @param title the title
   * @param properties the properties
   */
  public JP1Frame( String title, PropertyFile properties )
  {
    super( title );
    this.properties = properties;
    Container contentPane = super.getContentPane();
    contentPane.add( newContentPane, BorderLayout.CENTER );
    messageArea.setForeground( Color.red );
    contentPane.add( messageArea, BorderLayout.SOUTH );
  }

  /**
   * Gets the properties.
   * 
   * @return the properties
   */
  public static PropertyFile getProperties(){ return properties; }

  /* (non-Javadoc)
   * @see javax.swing.JFrame#getContentPane()
   */
  public Container getContentPane()
  {
    return newContentPane;
  }

  /**
   * Show message.
   * 
   * @param message the message
   */
  public void showMessage( String message )
  {
    messageArea.setText( message );
    Toolkit.getDefaultToolkit().beep();
  }

  /**
   * Show message.
   * 
   * @param message the message
   * @param c the c
   */
  public static void showMessage( String message, Component c )
  {
    JP1Frame frame = ( JP1Frame )SwingUtilities.getAncestorOfClass( JP1Frame.class, c );
    if ( frame != null )
      frame.showMessage( message );
  }

  /**
   * Clear message.
   */
  public void clearMessage()
  {
    messageArea.setText( "" );
  }

  /**
   * Clear message.
   * 
   * @param c the c
   */
  public static void clearMessage( Component c )
  {
    JP1Frame frame = ( JP1Frame )SwingUtilities.getAncestorOfClass( JP1Frame.class, c );
    if ( frame != null )
      frame.clearMessage();
  }

  /** The message area. */
  private JLabel messageArea = new JLabel( "" );
  
  /** The new content pane. */
  private JPanel newContentPane = new JPanel( new BorderLayout());
  
  /** The properties. */
  protected static PropertyFile properties = null;
}
