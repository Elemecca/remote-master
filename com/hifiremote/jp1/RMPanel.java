package com.hifiremote.jp1;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;

// TODO: Auto-generated Javadoc
/**
 * The Class RMPanel.
 */
public abstract class RMPanel extends JPanel
{

  /**
   * Instantiates a new rM panel.
   */
  public RMPanel()
  {
    super( new BorderLayout() );
  }
  
  public abstract void set( RemoteConfiguration remoteConfig );

  protected void refresh()
  {
  }

  public abstract void addRMPropertyChangeListener( PropertyChangeListener listener );
  
  public static class ButtonKeyAdapter extends KeyAdapter
  {
    private JButton delButton = null;
    private JButton cloneButton = null;
    
    public ButtonKeyAdapter( JButton delButton, JButton cloneButton )
    {
      super();
      this.delButton = delButton;
      this.cloneButton = cloneButton;
    }
    
    @Override
    public void keyPressed( KeyEvent e )
    {
      if ( e.getKeyCode() == KeyEvent.VK_DELETE )
      {
        if ( delButton != null && delButton.isVisible() && delButton.isEnabled() )
        {
          delButton.doClick();
        }
        e.consume();
      }
      else if ( ( e.getKeyCode() == KeyEvent.VK_D ) && ( ( e.getModifiers() & KeyEvent.CTRL_MASK ) != 0 )
          && cloneButton != null && cloneButton.isVisible() && cloneButton.isEnabled() )
      {
        cloneButton.doClick();
        e.consume();
      }
    }
  };
  
  public static class ButtonFocusAdapter implements FocusListener
  {
    private Component cpt = null;
    private JButton delButton = null;
    private JButton cloneButton = null;
    
    public ButtonFocusAdapter( Component cpt, JButton delButton, JButton cloneButton )
    {
      this.cpt = cpt;
      this.delButton = delButton;
      this.cloneButton = cloneButton;
    }
    
    @Override
    public void focusGained( FocusEvent e )
    {
      boolean selected = false;
      if ( cpt instanceof JP1Table )
      {
        selected = ( ( JP1Table )cpt ).getSelectedRow() >= 0;
      }
      else if ( cpt instanceof JList )
      {
        selected = ( ( JList )cpt ).getSelectedIndex() >= 0;
      }
      
      if ( delButton != null )
      {
        delButton.setEnabled( selected );
      }
      if ( cloneButton != null )
      {
        cloneButton.setEnabled( selected );
      }
    }

    @Override
    public void focusLost( FocusEvent e )
    {
      if ( delButton != null )
      {
        delButton.setEnabled( false );
      }
      if ( cloneButton != null )
      {
        cloneButton.setEnabled( false );
      }
    }
  }
  
  public void setButtonKeys( JPanel panel, Component cpt, JButton delButton, JButton cloneButton )
  {
    if ( delButton != null )
    {
      delButton.setFocusable( false );
    }
    if ( cloneButton != null )
    {
      cloneButton.setFocusable( false );
    }
    cpt.addKeyListener( new ButtonKeyAdapter( delButton, cloneButton ) );
    if ( panel instanceof RMPanel )
    {
      cpt.addFocusListener( new ButtonFocusAdapter( cpt, delButton, cloneButton ) );
    }
  }
  
  public void setButtonKeys( JPanel panel, Component cpt, JButton delButton )
  {
    setButtonKeys( panel, cpt, delButton, null );
  }
}
