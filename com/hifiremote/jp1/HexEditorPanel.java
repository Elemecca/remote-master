package com.hifiremote.jp1;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.text.*;

public class HexEditorPanel
  extends ProtocolEditorPanel
  implements PropertyChangeListener
{
  public HexEditorPanel( String title, String name, String toolTipText, String directions )
  {
    super( title );
    SpringLayout layout = new SpringLayout();
    JPanel panel = new JPanel( layout );
    add( panel, BorderLayout.CENTER );
    JLabel label = new JLabel( name );
    //label.setAlignmentY( Component.TOP_ALIGNMENT );
    hex = new JFormattedTextField( new HexFormatter()); 
    hex.setToolTipText( toolTipText );
    hex.addPropertyChangeListener( this );
    Dimension d = hex.getMaximumSize();
    d.height = hex.getPreferredSize().height;
    hex.setMaximumSize( d );
    hex.setAlignmentY( Component.TOP_ALIGNMENT );
    new TextPopupMenu( hex );
    label.setLabelFor( hex );
    panel.add( label );
    panel.add( hex );
    
        // Lay out the panel.
    SpringUtilities.makeCompactGrid( panel,
                                     1, 2,  // rows, cols 
                                     5, 5,      // initX, initY
                                     5, 5 );    // xPad, yPad

    
    setText( directions );
  }

  public void commit(){;}
  public void update( ProtocolEditorNode newNode )
  {
    node = ( HexEditorNode )newNode;
    hex.removePropertyChangeListener( this );
    hex.setValue( node.getHex());
    hex.addPropertyChangeListener( this );
  }

  // PropertyChangeListener methods
  public void propertyChange( PropertyChangeEvent e )
  {
    Object source = e.getSource();
    if ( source == hex )
    {
      if ( node != null )
        node.setHex(( Hex )hex.getValue());
    }
}

  private HexEditorNode node = null;
  private JFormattedTextField hex = null;
}
