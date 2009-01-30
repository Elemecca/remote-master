package com.hifiremote.jp1;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

// TODO: Auto-generated Javadoc
/**
 * The Class HexEditorPanel.
 */
public class HexEditorPanel
  extends ProtocolEditorPanel
  implements PropertyChangeListener
{
  
  /**
   * Instantiates a new hex editor panel.
   * 
   * @param title the title
   * @param name the name
   * @param toolTipText the tool tip text
   * @param directions the directions
   * @param length the length
   */
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

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.ProtocolEditorPanel#commit()
   */
  public void commit(){;}
  
  /* (non-Javadoc)
   * @see com.hifiremote.jp1.ProtocolEditorPanel#update(com.hifiremote.jp1.ProtocolEditorNode)
   */
  public void update( ProtocolEditorNode newNode )
  {
    node = ( HexEditorNode )newNode;
    hex.removePropertyChangeListener( this );
    hex.setValue( node.getHex());
    hex.addPropertyChangeListener( this );
  }

  // PropertyChangeListener methods
  /* (non-Javadoc)
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  public void propertyChange( PropertyChangeEvent e )
  {
    Object source = e.getSource();
    if ( source == hex )
    {
      if ( node != null )
        node.setHex(( Hex )hex.getValue());
    }
  }

  /**
   * Sets the length.
   * 
   * @param length the new length
   */
  public void setLength( int length )
  {
    Hex current = ( Hex )hex.getValue();
    Hex newHex = new Hex( length );
    if ( current != null )
    {
      short[] currentData = current.getData();
      short[] newData = newHex.getData();
      int len = Math.min( currentData.length, newData.length );
      for ( int i = 0; i < len; i++ )
        newData[ i ] = currentData[ i ];
    }
    hexFormatter.setLength( length );
    hex.setValue( newHex );
  }

  /** The node. */
  private HexEditorNode node = null;
  
  /** The hex formatter. */
  private HexFormatter hexFormatter = null;
  
  /** The hex. */
  private JFormattedTextField hex = null;
}
