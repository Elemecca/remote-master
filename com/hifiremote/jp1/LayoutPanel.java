package com.hifiremote.jp1;

import java.awt.*;
import javax.swing.border.*;
import javax.swing.*;
import java.text.*;
import javax.swing.text.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.event.*;
import java.util.*;
import info.clearthought.layout.*;

public class LayoutPanel
  extends KMPanel
{
  public LayoutPanel( DeviceUpgrade devUpgrade )
  {
    super( devUpgrade );

    imagePanel = new JPanel()
    {
      public void paint( Graphics g )
      {
        Graphics2D g2 = ( Graphics2D ) g;
        g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON );
        Remote r = deviceUpgrade.getRemote();
        ImageIcon icon = r.getImageIcon();
        if ( icon != null )
          g2.drawImage( icon.getImage(), null, null );

        g2.setPaint( Color.blue );

        DeviceType devType = deviceUpgrade.getDeviceType();
        ButtonMap map = devType.getButtonMap();
        // Button[] buttons = r.getButtons();
        // for ( int i = 0; i < buttons.length; i++ )
        // {
          // Button b = buttons[ i ];
          // if ( !map.isPresent( b ))
          // {
            // Shape shape = b.getShape();
            // if ( shape != null )
              // g2.fill( shape );
          // }
        // }

        if ( currentButton != null )
        {
          g2.setPaint( Color.white );
          g2.fill( currentButton.getShape());
        }
        g2.setPaint( Color.yellow );
        g2.setStroke( new BasicStroke( 2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND ));
        for ( int i = 0; i < map.size(); i++ )
        {
          Button b = map.get( i );
          Shape s = b.getShape();
          if ( s != null )
            g2.draw( s );
          else
            System.err.println( "No shape for button " + b );
        }
      }
    };
    add( imagePanel );

    imagePanel.addMouseListener( new MouseAdapter()
    {
      public void mousePressed( MouseEvent e )
      {
        Point p = e.getPoint();
        Button savedButton = currentButton;
        currentButton = null;
        Button[] buttons = deviceUpgrade.getRemote().getButtons();
        for ( int i = 0; i < buttons.length; i++ )
        {
          Button b = buttons[ i ];
          Shape s = b.getShape();
          if (( s != null ) && s.contains( p ))
          {
            currentButton = b;
            break;
          }
        }
        if ( currentButton != savedButton )
          doRepaint();
      }

      public void mouseReleased( MouseEvent e )
      {
        ;
      }
    });
  }

  public void update()
  {
    ImageIcon icon = deviceUpgrade.getRemote().getImageIcon();
    if ( icon != null )
    {
      int w = icon.getIconWidth();
      int h = icon.getIconHeight();
      Dimension size = new Dimension( w, h );
      imagePanel.setPreferredSize( size );
      imagePanel.setMaximumSize( size );
      imagePanel.setMinimumSize( size );
      imagePanel.setSize( w, h );
    }
    Button[] buttons = deviceUpgrade.getRemote().getButtons();
    boolean found = false;
    for ( int i = 0; i < buttons.length; i++ )
    {
      if ( currentButton == buttons[ i ])
      {
        found = true;
        break;
      }
    }
    if ( !found )
      currentButton = null;

    validate();
    doRepaint();
  }

  private void doRepaint()
  {
    imagePanel.repaint( 0L, 0, 0, imagePanel.getWidth(), imagePanel.getHeight());
  }

  private Button currentButton = null;
  private JPanel imagePanel = null;
}
