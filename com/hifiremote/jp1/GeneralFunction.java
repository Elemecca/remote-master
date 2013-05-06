package com.hifiremote.jp1;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import com.hifiremote.jp1.RemoteConfiguration.RMIcon;

public class GeneralFunction
{
  public GeneralFunction() {};
  
  public GeneralFunction( String name )
  {
    this.name = name;
  }
  
  /**
   * The Class User.
   */
  public static class User
  {

    public User( Button b, int state )
    {
      button = b;
      this.state = state;
      db = null;
    }
    
    public User( DeviceButton db, Button button )
    {
      this.db = db;
      this.button = button;
      this.state = Button.NORMAL_STATE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals( Object o )
    {
      User u = ( User )o;
      if ( db != u.db )
        return false;
      if ( button != u.button )
        return false;
      if ( state != u.state )
        return false;
      return true;
    }

    public Button button;
    public int state;
    public DeviceButton db;
  }
 
  public static class IconRenderer extends DefaultTableCellRenderer
  {
    private static final JLabel label = new JLabel();
    private static final JCheckBox check = new JCheckBox();
    
    @Override
    public Component getTableCellRendererComponent( JTable table, Object value, 
        boolean isSelected, boolean hasFocus,
        int row, int col )
    {
      Color color = UIManager.getColor("Table.selectionBackground");
      check.setBackground( isSelected ? color : Color.WHITE );
      check.setHorizontalAlignment( SwingConstants.CENTER );
      RMIcon icon = ( RMIcon )value;
      check.setSelected( icon != null && icon.image != null );
      return icon != null ? check : label;
    }
  }
  
  
  public static class IconPanel extends JPanel implements ActionListener, RMSetter< RMIcon >
  {
    public IconPanel()
    {
      super();
      setLayout( new BorderLayout() );
      iconPanel = new JPanel( new GridLayout( 0, 5, -1, -1 ) );
      selected = new JLabel();
      add( iconPanel, BorderLayout.CENTER );
      JPanel panel = new JPanel();
      panel.add( Box.createVerticalStrut( 40 ) );
      panel.add( new JLabel( "Selected: ") );
      panel.add( selected );
      add( panel, BorderLayout.PAGE_END );
      setBorder( BorderFactory.createTitledBorder( " Icon selector: " ) );
      map = new LinkedHashMap< ImageIcon, RMIcon >();
    }
    
    @Override
    public RMIcon getValue()
    {
      return value;
    }

    @Override
    public void setValue( RMIcon value )
    {
      this.value = value;
      List< RMIcon > list = new ArrayList< RMIcon >();
      nullIcon = new RMIcon( value );
      nullIcon.image = null;
      nullIcon.ref = 0;
      list.add( nullIcon );
      for ( RMIcon icon : config.getSysIcons().values() )
      {
        if ( icon.type == value.type )
        {
          list.add( icon );
          map.put( icon.image, icon );
        }
      }
      for ( RMIcon icon : config.getUserIcons().values() )
      {
        if ( icon.type == value.type )
        {
          list.add( icon );
          map.put( icon.image, icon );
        }
      }
      JButton b = null;
      for ( RMIcon icon : list )
      {
        JButton button = new JButton( icon.image );
        if ( icon.ref == value.ref )
        {
          b = button;
        }
        button.setContentAreaFilled(false);   
        button.setOpaque(false);
        button.setBorder( BorderFactory.createLineBorder( Color.BLACK ) );
        button.addActionListener( this );
        iconPanel.add( button );
      }
      selected.setIcon( value.image );
      selected.setText( value.image == null ? "<none>" : null );
      if ( b != null )
      {
        b.requestFocusInWindow();
      }
    }
    
    @Override
    public void setRemoteConfiguration( RemoteConfiguration config )
    {
      this.config = config;
    }
    
    @Override
    public void actionPerformed( ActionEvent e )
    {
      JButton b = ( JButton )e.getSource();
      ImageIcon image = ( ImageIcon )b.getIcon();
      RMIcon icon = null;
      if ( image == null )
      {
        selected.setIcon( null );
        selected.setText( "<none>" );
        icon = nullIcon;
      }
      else
      {
        selected.setIcon( image );
        selected.setText( null );
        icon = map.get( image );
      }
      value = new RMIcon( icon );
    }
    
    private RemoteConfiguration config = null;
    private RMIcon value = null;
    private JPanel iconPanel = null;
    private JLabel selected = null;
    private LinkedHashMap< ImageIcon, RMIcon > map = null;
    private RMIcon nullIcon = null;
  }

  public int getDeviceButtonIndex()
  {
    return deviceButtonIndex;
  }
  
  public void setDeviceButtonIndex( int deviceButtonIndex )
  {
    this.deviceButtonIndex = deviceButtonIndex;
  }
  
  public Hex getData()
  {
    return data;
  }

  public void setData( Hex hex )
  {
    data = hex;
  }

  public String getNotes()
  {
    return notes;
  }
  
  public String getName()
  {
    return name;
  }
  
  public String getDisplayName()
  {
    String s = "";
    if ( this instanceof Macro && !( ( ( Macro )this).isSystemMacro() ) )
    {
      s = "Macro: ";
    }
//    else if ( this instanceof KeyMove )
//    {
//      s = "KM: ";
//    }
    else if ( this instanceof LearnedSignal )
    {
      s = "Learn: ";
    }
    return s += name;
  }
  
  public boolean assigned()
  {
    return ( !users.isEmpty() );
  }
  
  public boolean assigned( DeviceButton db )
  {
    // A learned signal hides anything underneath, so treat as unassigned
    // if all assignments are hidden
    if ( db != null && db.getUpgrade() != null && db.getUpgrade().getRemote().isSSD() )
    {
      for ( User u : users )
      {
        LinkedHashMap< Integer, LearnedSignal > learnedMap = db.getUpgrade().getLearnedMap();
        if ( ( this instanceof Function || u.db == db )
            && ( this instanceof LearnedSignal || learnedMap.get( ( int )u.button.getKeyCode()) == null ) )
        {
          return true;
        }
      }
      return false;
    }
    else
    {
      return assigned();
    }
  }


  public List< User > getUsers()
  {
    return users;
  }
  

  public void addReference( DeviceButton db, Button b )
  {
    users.add( new User( db, b ) );
    if ( label != null )
    {
      label.showAssigned();
      label.updateToolTipText();
    }
  }
  

  public void removeReference( DeviceButton db, Button b )
  {
    users.remove( new User( db, b ) );
    if ( label != null )
    {
      label.showAssigned( db );
      label.updateToolTipText();
    }
  }
  

  public void removeReferences()
  {
    users.clear();
    if ( label != null )
    {
      label.showUnassigned();
      label.updateToolTipText();
    }
  }
  
  public FunctionLabel getLabel()
  {
    if ( label == null )
    {
      label = new FunctionLabel( this );
      label.updateToolTipText();
      if ( assigned() )
        label.showAssigned();
    }
    return label;
  }
  
  public FunctionItem getFunctionItem()
  {
    if ( item == null )
      item = new FunctionItem( this );
    return item;
  }
  
  public boolean hasData()
  {
    if ( data != null )
    {
      return true;
    }
    else if ( !(this instanceof AdvancedCode ) )
    {
      return false;
    }
    else
    {
      AdvancedCode a = ( AdvancedCode )this;
      return a.getItems() != null;
    }
  }
  
  public int getSerial()
  {
    return serial;
  }

  public void setSerial( int serial )
  {
    this.serial = serial;
  }

  
  public DeviceUpgrade getUpgrade( Remote remote )
  {
    if ( this instanceof Function )
    {
      return upgrade;
    }
    DeviceButton db = remote.getDeviceButton( deviceButtonIndex );
    if ( db == null )
    {
      return null;
    }
    return db.getUpgrade();
  }

  public void setUpgrade( DeviceUpgrade upgrade )
  {
    this.upgrade = upgrade;
  }
  
  public boolean accept()
  {
    if ( this instanceof Function )
    {
      Function f = ( Function )this;
      return f.accept();
    }
    else if ( this instanceof Macro )
    {
      Macro m = ( Macro )this;
      return !m.isSystemMacro() && m.getActivity() == null;
    }
    else return true;
    
  }

  public Integer getIconref()
  {
    // This works, but the equivalent conditional expression does not
    if ( icon == null )
    {
      return iconref;
    }
    else
    {
      return icon.ref;
    }
  }

  public void setIconref( Integer iconref )
  {
    this.iconref = iconref;
  }
  
  @Override
  public String toString()
  {
    if ( this instanceof Function )
    {
      return name;
    }
    else if ( this instanceof Macro )
    {
      return "Macro: " + name;
    }
    else if ( this instanceof LearnedSignal )
    {
      return "Learn: " + name;
    }
    else
    {
      // GeneralFunctions for Selector buttons have no subtype
      return "Button: " + name;
    }
  }

  protected Hex data;
  protected String name = null;
  protected int deviceButtonIndex = 0;
  protected int serial = -1;   // signifies unset
  protected String notes = null;
  protected List< User > users = new ArrayList< User >();
  protected FunctionLabel label = null;
  protected FunctionItem item = null;
  protected DeviceUpgrade upgrade = null;
  protected Integer iconref = null;
  protected RMIcon icon = null;

}
