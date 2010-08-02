package com.hifiremote.jp1;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

public class RMSetterDialog< T > extends JDialog
{
  
  public < C extends JComponent & RMSetter< T > > 
  T showDialog( Component locationComp, Remote remote, Class< C > panelClass, T value )
  {
    if ( dialog == null || remote != dialog.getRemote() )
    {  
      dialog = new SetterDialog< C >( locationComp, panelClass );
    }
    dialog.setRemote( remote );
    dialog.setValue( value );
    dialog.pack();
    dialog.setLocationRelativeTo( locationComp );
    dialog.setVisible( true );

    return this.value;
  }
  
  private class SetterDialog< C extends JComponent & RMSetter< T > > 
  extends JDialog implements ActionListener
  {

    private SetterDialog( Component c, Class< C > panelClass ) 
    {
      super(( JFrame )SwingUtilities.getRoot( c ));
      setTitle( title );
      setModal( true );

      JButton cancelButton = new JButton( "Cancel" );
      cancelButton.addActionListener( this );

      JButton setButton = new JButton( "Set" );
      setButton.setActionCommand( "Set" );
      setButton.addActionListener( this );
      getRootPane().setDefaultButton( setButton );

      KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
      getRootPane().registerKeyboardAction( this, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW );

      JPanel buttonPanel = new JPanel( new FlowLayout( FlowLayout.TRAILING ));
      buttonPanel.add( setButton );
      buttonPanel.add( cancelButton );

      Container contentPane = getContentPane();

      mainPanel = null;
      try
      {
        mainPanel = panelClass.newInstance();
      }
      catch ( Exception e )
      {
        e.printStackTrace();
      }

      contentPane.add( mainPanel, BorderLayout.PAGE_START );
      contentPane.add( buttonPanel, BorderLayout.PAGE_END );

      pack();

    }

    public Remote getRemote()
    {
      return remote;
    }

    public void setRemote( Remote remote )
    {
      this.remote = remote;
    }

    @Override
    public void actionPerformed( ActionEvent e )
    {
      if ( "Set".equals( e.getActionCommand()))
      {  
        value = mainPanel.getValue();
        if ( value == null )
        {
          return;
        }
      }  
      else
      {
        value = null;
      }

      setVisible( false );   
    }

    public void setValue( T value )
    { 
      mainPanel.setValue( value ); 
    }

    private C mainPanel;
    private Remote remote;

  }

  public void setTitle( String title )
  {
    this.title = title;
  }
  
  private String title = null;
  private  T value = null;
  private  SetterDialog< ? > dialog = null;
}
