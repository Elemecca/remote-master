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
  public LayoutPanel( DeviceUpgrade deviceUpgrade )
  {
    super( deviceUpgrade );
    image = new ImageIcon( "15-1994.jpg" );
  }

  public void paint(Graphics g)
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

    g2.setPaint( Color.yellow );
    g2.setStroke( new BasicStroke( 2.0f ));
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

  private ImageIcon image = null;
}
