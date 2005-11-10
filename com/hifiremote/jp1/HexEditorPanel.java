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
  public HexEditorPanel( String title, String name, String toolTipText, String directions, int length )
  {
    super( title );
    SpringLayout layout = new SpringLayout();
    JPanel panel = new JPanel( layout );
    add( panel, BorderLayout.CENTER );
    JLabel label = new JLabel( name );
    //label.setAlignmentY( Component.TOP_ALIGNMENT );
    hexFormatter = new HexFormatter( length );
    hex = new JFormattedTextField( hexFormatter );
    hex.setFocusLostBehavior( JFormattedTextField.COMMIT_OR_REVERT );
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

  public void setLength( int length )
  {
    Hex current = ( Hex )hex.getValue();
    Hex newHex = new Hex( length );
    if ( current != null )
    {
      int[] currentData = current.getData();
      int[] newData = newHex.getData();
      int len = Math.min( currentData.length, newData.length );
      for ( int i = 0; i < len; i++ )
        newData[ i ] = currentData[ i ];
    }
    hexFormatter.setLength( length );
    hex.setValue( newHex );
  }

  private HexEditorNode node = null;
  private HexFormatter hexFormatter = null;
  private JFormattedTextField hex = null;
}
