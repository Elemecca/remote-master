package com.hifiremote.jp1;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

public class GeneralFunction
{
  public GeneralFunction() {};
  
  public GeneralFunction( String name )
  {
    this.name = name;
  }
  
  public GeneralFunction( Properties props )
  {
    name = props.getProperty( "Name" );
    notes = props.getProperty( "Notes" );
    String temp = props.getProperty( "Data" );
    if ( temp != null )
    {
      data = new Hex( temp );
      notes = props.getProperty( "Notes" );
    }
  }
  
  public static class User
  {

    public User( Button b, int state )
    {
      button = b;
      this.state = state;
      db = DeviceButton.noButton;
    }
    
    public User( DeviceButton db, Button button )
    {
      this.db = db != null ? db : DeviceButton.noButton;
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
      if ( button.getKeyCode( state ) != u.button.getKeyCode( u.state ) )
      {
        return false;
      }
//      if ( button != u.button )
//        return false;
//      if ( state != u.state )
//        return false;
      return true;
    }
    
    public static String getUserNames( List< User > users )
    {
      boolean first = true;
      if ( users.isEmpty() )
      {
        return "<none>";
      }
      StringBuilder buff = new StringBuilder();
      for ( User user : users )
      {
        if ( first )
          first = false;
        else
          buff.append( ", " );
        Button b = user.button;
        DeviceButton db = user.db;
        int state = user.state;
        String name = null;
        if ( db != null && db != DeviceButton.noButton && !( name = db.getName().trim() ).isEmpty() )
        {
          buff.append( name + "/" );
        }
        if ( state == Button.NORMAL_STATE )
          buff.append( b.getName());
        else if ( state == Button.SHIFTED_STATE )
          buff.append( b.getShiftedName());
        else if ( state == Button.XSHIFTED_STATE )
          buff.append( b.getXShiftedName());
      }
      return buff.toString();
    }

    public Button button;
    public int state;
    public DeviceButton db;
  }
 
  public static class RMIcon
  {
    /* Icon types appear to be as follows:
     * 05 = System icons (monochrome), used for Devices and Activities
     * 06 = Favorites icons (for a particular channel)
     * 08 = Profiles icons (nine standard ones, numbered 1-9, for standard
     *      profile names Custom, Mom, Dad, Kids, Guest, Movies, News, 
     *      Sports, Comedy
     * 09 = Function icons used on soft buttons
     * 
     * Types 08 and 09 seem to have a standard reference in the intro field,
     * as the actual icon numbers vary.
     */
    int type = 0;
    Hex intro = new Hex( new short[]{ 0,0,0,0,0,0,0,0 } );
    ImageIcon image = null;
    int ref = 0;
    
    public RMIcon(){};
    
    public RMIcon( int type )
    {
      this.type = type;
    }
    
    public RMIcon( RMIcon icon )
    {
      copy( icon );
    }
    
    public void copy( RMIcon icon )
    {
      type = icon.type;
      intro = icon.intro == null ? null : new Hex( icon.intro );
      image = icon.image;
      ref = icon.ref;
    }
    
    public String toString()
    {
      return Integer.toString( ref );
    }
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
    private static final int COLS = 6;
    
    public IconPanel()
    {
      super();
      setLayout( new BorderLayout() );
      showAllButton = new JRadioButton( "Show all" );
      showUnusedButton = new JRadioButton( "Show unused" );
      ButtonGroup grp = new ButtonGroup();
      grp.add( showAllButton );
      grp.add( showUnusedButton );
      showAllButton.addActionListener( this );
      showAllButton.setSelected( true );
      showUnusedButton.addActionListener( this );
      showAllButton.setFocusable( false );
      showUnusedButton.setFocusable( false );
      selected = new JLabel();
      JPanel panel = new JPanel( new FlowLayout( WrapLayout.CENTER, 5, 0 ) );
      panel.add( showAllButton );
      panel.add( showUnusedButton );
      panel.add( Box.createVerticalStrut( 40 ) );
      panel.add( Box.createHorizontalStrut( 10 ) );
      panel.add( new JLabel( "Selected: ") );
      panel.add( selected );
      add( panel, BorderLayout.PAGE_START );
      iconPanel = new JPanel( new GridLayout( 0, COLS, -1, -1 ) );
      scrollPane = new JScrollPane( iconPanel );
      scrollPane.setHorizontalScrollBarPolicy( ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER ); 
      add( scrollPane, BorderLayout.CENTER );
      panel = new JPanel( new GridLayout( 3, 1 ) );
      importButton = new JButton( "Import new icon from file" );
      importButton.setToolTipText( "<html>Import icon from .png file to icon set of the remote.<br>"
          + "Import occurs even if you later press Cancel.</html>" );
      exportButton = new JButton( "Export selected icon to file" );
      exportButton.setToolTipText( "<html>Export icon to .png file.</html>" );
      deleteButton = new JButton( "Delete selected (unused) icon" );
      deleteButton.setToolTipText( "<html>Delete icon from icon set of the remote.  Deletion<br>"
          + " occurs even if you later press Cancel</html>" );
      importButton.addActionListener( this );
      exportButton.addActionListener( this );
      deleteButton.addActionListener( this );
      importButton.setFocusable( false );
      exportButton.setFocusable( false );
      deleteButton.setFocusable( false );
      panel.add( importButton );
      panel.add( exportButton );
      panel.add( deleteButton );
      add( panel, BorderLayout.PAGE_END );
      setBorder( BorderFactory.createTitledBorder( " Icon selector: " ) );
      map = new LinkedHashMap< ImageIcon, RMIcon >();
      usedImages = new ArrayList< ImageIcon >();
      nullIcon = new RMIcon();
    }
    
    private class IconButton extends JButton
    {
      public IconButton( ImageIcon image )
      {
        super( image );
        setContentAreaFilled( false );   
        setOpaque( false );
        setBorder( BorderFactory.createLineBorder( Color.BLACK ) );
      }
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
      iconPanel.removeAll();
      nullIcon.type = value.type;
      list.add( nullIcon );
      map.clear();
      if ( value.type == 5 )
      {
        showUnusedButton.setEnabled( false );
        showAllButton.setSelected( true );
        usedImages.clear();
      }
      else
      {
        showUnusedButton.setEnabled( true );
        setUsedImages();
      }
      for ( RMIcon icon : config.getSysIcons().values() )
      {
        if ( icon.type == value.type && ( showAll || !usedImages.contains( icon.image ) ) )
        {
          list.add( icon );
          map.put( icon.image, icon );
        }
      }
      for ( RMIcon icon : config.getUserIcons().values() )
      {
        if ( icon.type == value.type && ( showAll || !usedImages.contains( icon.image ) ) )
        {
          list.add( icon );
          map.put( icon.image, icon );
        }
      }
      for ( RMIcon icon : list )
      {
        JButton button = new IconButton( icon.image );
        // Ensure that empty button gets selected if no other match
        if ( icon.ref == 0 || icon.ref == value.ref )
        {
          selectedButton = button;
        }
        button.addActionListener( this );
        iconPanel.add( button );
      }
      
      GridLayout grid = ( GridLayout )iconPanel.getLayout();
      Dimension d = grid.preferredLayoutSize( iconPanel );
      d.width += ( (Integer )UIManager.get( "ScrollBar.width" ) ).intValue() + 10; 
      d.height += 10;
      scrollPane.setPreferredSize( d );
      selected.setIcon( value.image );
      selected.setText( value.image == null ? "<none>" : null );
      importButton.setEnabled( value.type > 5 );
      deleteButton.setEnabled( false );
      if ( selectedButton != null )
      {
        selectedButton.requestFocusInWindow();
      }
    }
    
    private void setUsedImages()
    {
      usedImages.clear();
      for ( FavScan fav : config.getFavScans() )
      {
        if ( fav.icon.image != null )
        {
          usedImages.add( fav.icon.image );
        }
      }
      for ( Activity activity : config.getRemote().getFavKey().getProfiles() )
      {
        if ( activity.icon.image != null )
        {
          usedImages.add( activity.icon.image );
        }
      }
      for ( DeviceUpgrade dev : config.getDeviceUpgrades() )
      {
        for ( Function f : dev.getFunctions() )
        {
          if ( f.icon.image != null )
          {
            usedImages.add( f.icon.image );
          }
        }
      }
    }

    @Override
    public void setRemoteConfiguration( RemoteConfiguration config )
    {
      this.config = config;
      propertyChangeSupport.addPropertyChangeListener( config.getOwner() );
    }

    @Override
    public void actionPerformed( ActionEvent e )
    {
      Object source = e.getSource();
      if ( source instanceof JRadioButton )
      {
        showAll = showAllButton.isSelected();
        RMIcon icon = value;
        setValue( icon );
        validate();
        repaint();
        return;
      }
      JButton b = ( JButton )e.getSource();
      if ( b == importButton )
      {
        load();
      }
      else if ( b == exportButton )
      {
        save();
      }
      else if ( b == deleteButton )
      {
        ImageIcon imageIcon = ( ImageIcon )selectedButton.getIcon();
        RMIcon icon = map.get( imageIcon );
        config.getUserIcons().remove( icon.ref );
        propertyChangeSupport.firePropertyChange( "icons", null, null );
        setValue( nullIcon );
        validate();
        repaint();
      }
      else
      {
        selectedButton = b;
        ImageIcon image = ( ImageIcon )b.getIcon();
        RMIcon icon = null;
        if ( image == null )
        {
          selected.setIcon( null );
          selected.setText( "<none>" );
          icon = nullIcon;
          deleteButton.setEnabled( false );
        }
        else
        {
          selected.setIcon( image );
          selected.setText( null );
          icon = map.get( image );
          deleteButton.setEnabled( icon.type > 5 && !usedImages.contains( image ) );
        }
//        value = new RMIcon( icon );
        value = icon;  // *** not sure why it needed to be copied
      }
    }
    
    public void load()
    {
      File file = null;
      PropertyFile properties = RemoteMaster.getProperties();
      File dir = properties.getFileProperty( "IconPath" );
      if ( dir == null )
      {
        dir = properties.getFileProperty( "IRPath" );
      }
      RMFileChooser chooser = new RMFileChooser( dir );
      String[] endings =
      {
        ".png"
      };
      EndingFileFilter filter = new EndingFileFilter( "Graphics files (*.png)", endings );
//      chooser.addChoosableFileFilter( filter );
      chooser.setFileFilter( filter );
      RemoteMaster rm = ( RemoteMaster )SwingUtilities.getAncestorOfClass( RemoteMaster.class, this );
      while ( true )
      {
        if ( chooser.showOpenDialog( rm ) == RMFileChooser.APPROVE_OPTION )
        {
          file = chooser.getSelectedFile();

          if ( !file.exists() )
          {
            JOptionPane.showMessageDialog( rm, file.getName() + " doesn't exist.", "File doesn't exist.",
                JOptionPane.ERROR_MESSAGE );
          }
          else if ( file.isDirectory() )
          {
            JOptionPane.showMessageDialog( rm, file.getName() + " is a directory.", "File doesn't exist.",
                JOptionPane.ERROR_MESSAGE );
          }
          else
          {
            break;
          }
        }
        else
        {
          return;
        }
      }
      properties.setProperty( "IconPath", file.getParentFile() );
      MediaTracker tracker = new MediaTracker( this );
      ImageIcon imageIcon = new ImageIcon( file.getAbsolutePath() );
      Image image = imageIcon.getImage();
      tracker.addImage( image, 1 );
      try
      {
        tracker.waitForID( 1 );
      }
      catch ( InterruptedException e )
      {
        e.printStackTrace();
      }
      int h = image.getHeight( null );
      int w = image.getWidth( null );
      if ( h > 34 )
      {
        w = ( int )( ( w * 34.0 ) / h + 0.5 );
        h = 34;
      }
      BufferedImage bi = new BufferedImage( w, h, BufferedImage.TYPE_INT_ARGB );  
      Graphics g = bi.createGraphics();  
      g.drawImage( image, 0, 0, w, h, null );
      imageIcon = new ImageIcon( bi );
      RMIcon icon = new RMIcon( value );
      icon.image = imageIcon;
      icon.ref = 257;  // ensures no clash with existing icons
      config.getUserIcons().put( icon.ref, icon );
      map.put( imageIcon, icon );
      JButton button = new IconButton( icon.image );
      button.addActionListener( this );
      iconPanel.add( button );
      iconPanel.validate();
      // firing property change reallocates the icon ref values
      propertyChangeSupport.firePropertyChange( "icons", null, null );
    }
    
    public void save()
    {
      PropertyFile properties = RemoteMaster.getProperties();
      File dir = properties.getFileProperty( "IconPath" );
      if ( dir == null )
      {
        dir = properties.getFileProperty( "IRPath" );
      } 
      RMFileChooser chooser = new RMFileChooser( dir );
      String[] endings =
      {
        ".png"
      };
      chooser.setFileFilter( new EndingFileFilter( "Graphics files (*.png)", endings ) );
      RemoteMaster rm = ( RemoteMaster )SwingUtilities.getAncestorOfClass( RemoteMaster.class, this );
      int returnVal = chooser.showSaveDialog( rm );
      if ( returnVal == RMFileChooser.APPROVE_OPTION )
      {
        File file = chooser.getSelectedFile();
        properties.setProperty( "IconPath", file.getParentFile() );
        String name = file.getAbsolutePath();
        if ( !name.toLowerCase().endsWith( ".png" ) )
        {
          name = name + ".png";
        }
        file = new File( name );
        int rc = JOptionPane.YES_OPTION;
        if ( file.exists() )
        {
          rc = JOptionPane.showConfirmDialog( rm, file.getName() + " already exists.  Do you want to replace it?",
              "Replace existing file?", JOptionPane.YES_NO_OPTION );
        }
        if ( rc == JOptionPane.YES_OPTION )
        {
          BufferedImage image = ( BufferedImage) value.image.getImage();
          try
          {
            ImageIO.write(image, "PNG", file );
          }
          catch ( IOException ex )
          {
            ex.printStackTrace();
          }
        }
      }
    }
    
    private RemoteConfiguration config = null;
    private RMIcon value = null;
    private JPanel iconPanel = null;
    private JScrollPane scrollPane = null;
    private boolean showAll = true;
    private JLabel selected = null;
    private JButton selectedButton = null;
    private LinkedHashMap< ImageIcon, RMIcon > map = null;
    private RMIcon nullIcon = null;
    private JButton importButton = null;
    private JButton exportButton = null;
    private JButton deleteButton = null;
    private JRadioButton showAllButton = null;
    private JRadioButton showUnusedButton = null;
    private List< ImageIcon > usedImages = null;
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport( this );
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
  
  public String getDisplayName( Remote remote )
  {
    if ( this instanceof Macro && !( ( ( Macro )this).isSystemMacro() ) )
    {
      return "Macro: " + name;
    }
    else if ( this instanceof LearnedSignal )
    {
      if ( name != null )
      {
        return "Learn: " + name;
      }
      else
      {
        Button btn = remote.getButton( ( ( LearnedSignal )this ).getKeyCode() );
        return "Learn: " + btn.getName();
      }
    }
    return name;
  }
  
  public boolean assigned()
  {
    return ( !getUsers().isEmpty() );
  }
  
  public boolean assigned( DeviceButton db )
  {
    // A learned signal hides anything underneath, so show as unassigned
    // if all assignments are hidden
    DeviceUpgrade upg = null;
    if ( db != null && db != DeviceButton.noButton && ( upg = db.getUpgrade() ) != null && upg.getRemote().usesEZRC() )
    {
      for ( User u : users )
      {
        LinkedHashMap< Integer, LearnedSignal > learnedMap = db.getUpgrade().getLearnedMap();
        if ( ( /* this instanceof Function || */ u.db == db )
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
    User user = new User( db, b );
    if ( !users.contains( user ) )
    {
      users.add( user );
    }
    if ( label != null )
    {
      label.showAssigned( db );
      label.updateToolTipText();
    }
    Function f = null;
    if ( this instanceof LearnedSignal && db.getUpgrade() != null && ( f = db.getUpgrade().getAssignments().getAssignment( b ) ) != null )
    {
      f.removeReference( db, b );
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
    Function f = null;
    if ( this instanceof LearnedSignal && ( f = db.getUpgrade().getAssignments().getAssignment( b ) ) != null )
    {
      f.addReference( db, b );
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

  public void store( PropertyWriter pw )
  {
    if ( name != null )
    {
      pw.print( "Name", name );
    }
    if ( data != null )
    {
      pw.print( "Data", data );
    }
    if ( notes != null && !notes.trim().isEmpty() )
    {
      pw.print( "Notes", notes );
    }
    if ( icon != null && icon.ref > 0 )
    {
      pw.print(  "Iconref", icon.ref );
    }
  }
  
  protected Hex data = null;
  protected String name = null;
  protected int deviceButtonIndex = 0;
  protected int serial = -1;   // signifies unset
  protected String notes = null;
  protected List< User > users = new ArrayList< User >();
  protected FunctionLabel label = null;
  protected FunctionItem item = null;
  protected DeviceUpgrade upgrade = null;
  protected RMIcon icon = null;

}
