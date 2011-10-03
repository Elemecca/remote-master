package com.hifiremote.jp1;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

// TODO: Auto-generated Javadoc
/**
 * The Class JP1Frame.
 */
public class JP1Frame extends JFrame implements HyperlinkListener
{

  /**
   * Instantiates a new jp1 frame.
   * 
   * @param title
   *          the title
   * @param properties
   *          the properties
   */
  public JP1Frame( String title, PropertyFile properties )
  {
    super( title );
    JP1Frame.properties = properties;
    Container contentPane = super.getContentPane();
    contentPane.add( newContentPane, BorderLayout.CENTER );
    messageArea.setForeground( Color.red );
    contentPane.add( messageArea, BorderLayout.SOUTH );

    if ( Desktop.isDesktopSupported() )
    {
      desktop = Desktop.getDesktop();
    }
  }

  /**
   * Gets the properties.
   * 
   * @return the properties
   */
  public static PropertyFile getProperties()
  {
    return properties;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.JFrame#getContentPane()
   */
  public Container getContentPane()
  {
    return newContentPane;
  }

  /**
   * Show message.
   * 
   * @param message
   *          the message
   */
  public void showMessage( String message )
  {
    messageArea.setText( message );
    Toolkit.getDefaultToolkit().beep();
  }

  /**
   * Show message.
   * 
   * @param message
   *          the message
   * @param c
   *          the c
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
   * @param c
   *          the c
   */
  public static void clearMessage( Component c )
  {
    JP1Frame frame = ( JP1Frame )SwingUtilities.getAncestorOfClass( JP1Frame.class, c );
    if ( frame != null )
      frame.clearMessage();
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.HyperlinkListener#hyperlinkUpdate(javax.swing.event.HyperlinkEvent)
   */
  @Override
  public void hyperlinkUpdate( HyperlinkEvent event )
  {
    if ( event.getEventType() != HyperlinkEvent.EventType.ACTIVATED )
    {
      return;
    }

    if ( desktop != null )
    {
      try
      {
        desktop.browse( event.getURL().toURI() );
      }
      catch ( IOException e )
      {
        e.printStackTrace( System.err );
      }
      catch ( URISyntaxException e )
      {
        e.printStackTrace( System.err );
      }
    }
  }

  /** The message area. */
  private JLabel messageArea = new JLabel( "" );

  /** The new content pane. */
  private JPanel newContentPane = new JPanel( new BorderLayout() );

  /** The properties. */
  protected static PropertyFile properties = null;

  protected Desktop desktop = null;
}
