package com.hifiremote.jp1;

import java.awt.*;
import javax.swing.*;

public class JP1Frame extends JFrame
{
  public JP1Frame( String title, PropertyFile properties )
  {
    super( title );
    this.properties = properties;
    Container contentPane = super.getContentPane();
    contentPane.add( newContentPane, BorderLayout.CENTER );
    messageArea.setForeground( Color.red );
    contentPane.add( messageArea, BorderLayout.SOUTH );
  }

  public static PropertyFile getProperties(){ return properties; }

  public Container getContentPane()
  {
    return newContentPane;
  }

  public void showMessage( String message )
  {
    messageArea.setText( message );
    Toolkit.getDefaultToolkit().beep();
  }

  public static void showMessage( String message, Component c )
  {
    JP1Frame frame = ( JP1Frame )SwingUtilities.getAncestorOfClass( JP1Frame.class, c );
    if ( frame != null )
      frame.showMessage( message );
  }

  public void clearMessage()
  {
    messageArea.setText( "" );
  }

  public static void clearMessage( Component c )
  {
    JP1Frame frame = ( JP1Frame )SwingUtilities.getAncestorOfClass( JP1Frame.class, c );
    if ( frame != null )
      frame.clearMessage();
  }

  private JLabel messageArea = new JLabel( "" );
  private JPanel newContentPane = new JPanel( new BorderLayout());
  protected static PropertyFile properties = null;
}
