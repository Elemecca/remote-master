package com.hifiremote.jp1;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class KeyChooser
  extends JDialog
  implements ActionListener
{
  private static KeyChooser dialog;
  private static Integer value; 
  private JComboBox buttonBox = new JComboBox();
  private JCheckBox shiftBox = new JCheckBox();
  private JCheckBox xShiftBox = new JCheckBox();

  public static Integer showDialog( Component locationComp,
                                    Remote remote,
                                    Integer initialKeyCode )
  {
    if ( dialog == null )
      dialog = new KeyChooser( locationComp );
    
    dialog.setRemote( remote );
    dialog.setKeyCode( initialKeyCode );
    dialog.setLocationRelativeTo( locationComp );
                              
    dialog.setVisible( true );
    return value;
  }

  private KeyChooser( Component c ) 
  {
    super(( JFrame )SwingUtilities.getRoot( c ));
    setTitle( "Key Chooser" );
    setModal( true );

    JButton cancelButton = new JButton( "Cancel" );
    cancelButton.addActionListener( this );

    JButton setButton = new JButton( "Set" );
    setButton.setActionCommand( "Set" );
    setButton.addActionListener( this );
    getRootPane().setDefaultButton( setButton );
    
    KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
    getRootPane().registerKeyboardAction( this, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW );

    JPanel panel = new JPanel();
    panel.add( new JLabel( "Key:" ));
    panel.add( buttonBox );
    panel.add( shiftBox );
    panel.add( xShiftBox );

    buttonBox.addActionListener( this );
    shiftBox.addActionListener( this );
    xShiftBox.addActionListener( this );

    JPanel buttonPanel = new JPanel( new FlowLayout( FlowLayout.TRAILING ));
    buttonPanel.add( setButton );
    buttonPanel.add( cancelButton );

    //Put everything together, using the content pane's BorderLayout.
    Container contentPane = getContentPane();
    contentPane.add( panel, BorderLayout.NORTH );
    contentPane.add( buttonPanel, BorderLayout.PAGE_END );

    pack();
  }

  private Remote remote = null;
  public void setRemote( Remote remote )
  {
    this.remote = remote;
    buttonBox.setModel( new DefaultComboBoxModel( remote.getUpgradeButtons()));
    shiftBox.setText( remote.getShiftLabel());
    xShiftBox.setText( remote.getXShiftLabel());
    xShiftBox.setVisible( remote.getXShiftEnabled());
    pack();
  }

  public Integer getKeyCode()
  {
    Button b = ( Button )buttonBox.getSelectedItem();
    int code = b.getKeyCode();
    if ( shiftBox.isSelected())
      code |= remote.getShiftMask();
    else if ( xShiftBox.isSelected())
      code |= remote.getXShiftMask();

    return new Integer( code );
  }

  public void setKeyCode( Integer keyCode )
  {
    value = keyCode;
    int code = keyCode.intValue();
    Button b = remote.getButton( code );
    if ( b == null )
    {
      int base = code & 0x3F;
      if ( base != 0 )
      {
        b = remote.getButton( base );
        if (( base | remote.getShiftMask()) == code )
          shiftBox.setSelected( true );
        if (( base | remote.getXShiftMask()) == code )
          xShiftBox.setSelected( true );
      }
      else
      {
        b = remote.getButton( code & ~remote.getShiftMask());
        if ( b != null )
          shiftBox.setSelected( true );
        else
        {
          b = remote.getButton( code ^ ~remote.getXShiftMask());
          if ( b != null )
            xShiftBox.setSelected( true );
        }
      }
    }
      
    shiftBox.setEnabled( b.allowsShiftedKeyMove());
    xShiftBox.setEnabled( b.allowsXShiftedKeyMove());

    if ( b.getIsXShifted())
      xShiftBox.setSelected( true );      
    else if ( b.getIsShifted())
      shiftBox.setSelected( true );

    buttonBox.removeActionListener( this );
    buttonBox.setSelectedItem( b );  
    buttonBox.addActionListener( this );
  }

  public void actionPerformed( ActionEvent e )
  {
    Object source = e.getSource();
    if ( source == buttonBox )
    {
      Button b = ( Button )buttonBox.getSelectedItem();
      shiftBox.setEnabled( b.allowsShiftedKeyMove());
      if ( !b.allowsShiftedKeyMove())
        shiftBox.setSelected( false );
      if ( b.getIsShifted())
        shiftBox.setSelected( true );

      xShiftBox.setEnabled( b.allowsXShiftedKeyMove());
      if ( !b.allowsXShiftedKeyMove())
        xShiftBox.setSelected( false );
      if ( b.getIsXShifted())
        xShiftBox.setSelected( true );
    }
    else if ( source == shiftBox )
    {
      if ( shiftBox.isSelected())
        xShiftBox.setSelected( false );
    }
    else if ( source == xShiftBox )
    {
      if ( xShiftBox.isSelected())
        shiftBox.setSelected( false );
    }
    else
    {
      if ( "Set".equals( e.getActionCommand())) 
        value = getKeyCode();
      else
        value = null;

      setVisible( false );
    }
  }
}

