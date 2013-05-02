package com.hifiremote.jp1;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.Document;
import javax.swing.text.NumberFormatter;

import com.hifiremote.jp1.RemoteMaster.IconImage;
import com.hifiremote.jp1.RemoteConfiguration.Icon;

public class FavoritesPanel extends RMPanel implements ActionListener, 
  ListSelectionListener, DocumentListener
{
  public FavoritesPanel()
  {
    super();
    favModel = new FavScanTableModel();
    favModel.setPanel( this );
    deviceBoxPanel = new JPanel( new WrapLayout( FlowLayout.LEFT ) );
    deviceButtonBox = new JComboBox();
    deviceButtonBox.addActionListener( this );
    Dimension d = deviceButtonBox.getPreferredSize();
    d.width = 100;
    deviceButtonBox.setPreferredSize( d );
    deviceBoxPanel.add( new JLabel( "Channel change device: " ) );
    deviceBoxPanel.add( deviceButtonBox );
    
    NumberFormatter formatter = new NumberFormatter( new DecimalFormat( "0.0" ) );
    formatter.setValueClass( Float.class );
    duration = new JFormattedTextField( formatter ){
      @Override
      protected void processFocusEvent( FocusEvent e ) 
      {
        super.processFocusEvent( e );
        if ( e.getID() == FocusEvent.FOCUS_GAINED )
        {  
          selectAll();
        }  
      }
    };
//    duration.setFocusLostBehavior( JFormattedTextField.PERSIST );
    duration.setColumns( 4 );
    duration.addActionListener( this );
    deviceBoxPanel.add( Box.createHorizontalStrut( 20 ) );
    deviceBoxPanel.add( new JLabel( "Interdigit pause: " ) );
    deviceBoxPanel.add( duration );
    deviceBoxPanel.add( new JLabel( " secs") );
    
    addFinal = new JCheckBox( "Send final key?" );
    addFinal.addActionListener( this );
    addFinal.setToolTipText( "Send a key such as Enter or OK after each macro?" );
    deviceBoxPanel.add( Box.createHorizontalStrut( 20 ) );
    deviceBoxPanel.add( addFinal );
    
    finalKey = new JTextField( 12 );
    finalKeyLabel = new JLabel( "Key: " );
    finalKey.setEditable( false );
    finalKey.setToolTipText( "Double-click to edit." );
    deviceBoxPanel.add( Box.createHorizontalStrut( 5 ) );
    deviceBoxPanel.add( finalKeyLabel );
    deviceBoxPanel.add( finalKey );
    
    JPanel panel = new JPanel( new BorderLayout() );
    panel.setBorder( BorderFactory.createTitledBorder( " Favorites Macros " ) );
    panel.add( deviceBoxPanel, BorderLayout.PAGE_START );
    favTable = new JP1Table( favModel );
    favTable.setSelectionMode( ListSelectionModel.SINGLE_INTERVAL_SELECTION );
    JScrollPane scrollPane = new JScrollPane( favTable );
    panel.add( scrollPane, BorderLayout.CENTER );
    
    JPanel buttonPanel = new JPanel( new WrapLayout( FlowLayout.CENTER, 5, 0 ) );
    buttonPanel.setBorder( BorderFactory.createEmptyBorder( 3, 0, 0, 0 ) );

    editButton = new JButton( "Edit" );
    editButton.addActionListener( this );
    editButton.setToolTipText( "Edit the selected item." );
    editButton.setEnabled( false );
    buttonPanel.add( editButton );

    newButton = new JButton( "New" );
    newButton.addActionListener( this );
    newButton.setToolTipText( "Add a new item." );
    buttonPanel.add( newButton );

    cloneButton = new JButton( "Clone" );
    cloneButton.addActionListener( this );
    cloneButton.setToolTipText( "Add a copy of the selected item." );
    cloneButton.setEnabled( false );
    buttonPanel.add( cloneButton );

    deleteButton = new JButton( "Delete" );
    deleteButton.addActionListener( this );
    deleteButton.setToolTipText( "Delete the selected item." );
    deleteButton.setEnabled( false );
    buttonPanel.add( deleteButton );

    upButton = new JButton( "Up" );
    upButton.addActionListener( this );
    upButton.setToolTipText( "Move the selected item up in the list." );
    upButton.setEnabled( false );
    buttonPanel.add( upButton );

    downButton = new JButton( "Down" );
    downButton.addActionListener( this );
    downButton.setToolTipText( "Move the selected item down in the list." );
    downButton.setEnabled( false );
    buttonPanel.add( downButton );
    
    iconImage = new IconImage();
    buttonPanel.add( Box.createHorizontalStrut( 10 ) );
    buttonPanel.add( iconImage );

    panel.add( buttonPanel, BorderLayout.PAGE_END );
    
    profilesPanel = new JPanel( new BorderLayout() );
    profilesPanel.setBorder( BorderFactory.createTitledBorder( " Profiles " ) );
    profilesPanel.add( new JScrollPane( profiles ), BorderLayout.CENTER );
    profiles.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
    profiles.addListSelectionListener( this );
    upperPane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, profilesPanel, panel);
    upperPane.setResizeWeight( 0.5 );
    
    panel = new JPanel( new WrapLayout() );
    panel.add( allButton );
    panel.add( profileButton);
    panel.setSize( new Dimension( 40, 1 ) );
    ButtonGroup grp = new ButtonGroup();
    grp.add( allButton );
    grp.add( profileButton );
    allButton.addActionListener( this );
    profileButton.addActionListener( this );
    allButton.setSelected( true );
    profilesPanel.add( panel, BorderLayout.PAGE_START );

    panel = new JPanel( new BorderLayout() );
    profilesPanel.add( panel, BorderLayout.PAGE_END );
    
    JPanel p = new JPanel( new FlowLayout( FlowLayout.LEFT, 5, 0 ) );
    profileField = new JTextField( 10 );
    profileField.getDocument().addDocumentListener( this );
    profileField.setToolTipText( "Edit this value to rename selected profile" );
    p.add( new JLabel( "Selected: " ) );
    p.add( profileField );
    profileIcon = new IconImage();
    p.add(  Box.createHorizontalStrut( 10 ) );
    p.add( profileIcon );
    panel.add( p, BorderLayout.PAGE_START );

    buttonPanel = new JPanel( new WrapLayout() );
    panel.add( buttonPanel, BorderLayout.PAGE_END );
    newProfile = new JButton( "New" );
    newProfile.addActionListener( this );
    newProfile.setToolTipText( "Add a new profile." );
    buttonPanel.add( newProfile );

    deleteProfile = new JButton( "Delete" );
    deleteProfile.addActionListener( this );
    deleteProfile.setToolTipText( "Delete the selected profile." );
    deleteProfile.setEnabled( false );
    buttonPanel.add( deleteProfile );

    upProfile = new JButton( "Up" );
    upProfile.addActionListener( this );
    upProfile.setToolTipText( "Move the selected profile up in the list." );
    upProfile.setEnabled( false );
    buttonPanel.add( upProfile );

    downProfile = new JButton( "Down" );
    downProfile.addActionListener( this );
    downProfile.setToolTipText( "Move the selected profile down in the list." );
    downProfile.setEnabled( false );
    buttonPanel.add( downProfile );

    add( upperPane, BorderLayout.PAGE_START ); 
    
    groupPanel = new JPanel( new BorderLayout() );
    groupPanel.setBorder( BorderFactory.createTitledBorder( " Favorites Group Assignments " ) );
    activityGroupTable = new JP1Table( activityGroupModel );
    activityGroupTable.setCellEditorModel( activityGroupModel );
    activityGroupTable.setSelectionMode( ListSelectionModel.SINGLE_INTERVAL_SELECTION );
    scrollPane = new JScrollPane( activityGroupTable );
    groupPanel.add( scrollPane, BorderLayout.CENTER );
    add( groupPanel, BorderLayout.CENTER );
    
    d = favTable.getPreferredSize();
    d.height = 12 * favTable.getRowHeight();
    favTable.setPreferredScrollableViewportSize( d );
    favTable.getSelectionModel().addListSelectionListener( this );
    favTable.addFocusListener( new FocusAdapter()
    {
      @Override
      public void focusGained( FocusEvent e )
      {
        activeTable = favTable;
        setHighlightAction( favTable );
      }
    } );
    activityGroupTable.addFocusListener( new FocusAdapter()
    {
      @Override
      public void focusGained( FocusEvent e )
      {
        activeTable = activityGroupTable;
        setHighlightAction( activityGroupTable );
      }
    } );
    
    duration.addFocusListener( new FocusAdapter()
    {
      @Override
      public void focusLost( FocusEvent e )
      {
        float f = ( Float )duration.getValue();
        try
        {
          duration.commitEdit();
        }
        catch ( ParseException e1 )
        {
          duration.setValue( f );
          return;
        }
        // minimum duration is 0.1 secs
        f = ( Float )duration.getValue();
        int val = Math.max( ( int )( 10.0 * f + 0.5 ), 1 );
        remoteConfig.setFavPause( val );
        duration.setValue( val/10.0 );
        try
        {
          duration.commitEdit();
        }
        catch ( ParseException e1 ) {}
        propertyChangeSupport.firePropertyChange( "data", null, null );
      }
    } );
    
    openEditor = new MouseAdapter()
    {
      @Override
      public void mouseClicked( MouseEvent e )
      {
        if ( e.getClickCount() != 2 )
        {
          return;
        }
        Component source = e.getComponent();
        if ( source == finalKey )
        {
          Remote remote = remoteConfig.getRemote();
          Button btn = remote.getButton( finalKey.getText() );
          if ( btn == null )
          {
            btn = remote.getButtons().get( 0 );
          }
          Integer result = KeyChooser.showDialog( finalKey, remote, ( int )btn.getKeyCode() );
          if ( result != null )
          {
            btn = remote.getButton( result );
            finalKey.setText( btn.getName() );
            remoteConfig.setFavFinalKey( btn );
            propertyChangeSupport.firePropertyChange( "data", null, null );
          }
          return;
        }
        int row = favTable.getSelectedRow();
        if ( row == -1 )
        {
          return;
        }
        if ( !favTable.isCellEditable( row, favTable.columnAtPoint( e.getPoint() ) ) )
        {
          editRowObject( row );
        }
      }
    };
    favTable.addMouseListener( openEditor );
    finalKey.addMouseListener( openEditor );
    
    activeTable = favTable;
  }

  @Override
  public void addPropertyChangeListener( PropertyChangeListener listener )
  {
    if ( listener != null )
    {
      if ( favModel != null )
      {
        favModel.addPropertyChangeListener( listener );
      }
      if ( activityGroupModel != null )
      {
        activityGroupModel.addPropertyChangeListener( listener );
      }
      propertyChangeSupport.addPropertyChangeListener( listener );
    }
  }

  @Override
  public void set( RemoteConfiguration remoteConfig )
  {
    this.remoteConfig = remoteConfig;
    Remote remote = remoteConfig.getRemote();
    DefaultComboBoxModel comboModel = new DefaultComboBoxModel( remote.getDeviceButtons() );
    favModel.set( remoteConfig );
    favTable.initColumns( favModel );
    activityGroupTable.setVisible( false );
    profilesPanel.setVisible( remote.hasProfiles() );
    favBtn = remote.getButtonByStandardName( "Favorites" );
    newButton.setEnabled( favBtn != null );
    duration.setValue( new Float( remoteConfig.getFavPause() / 10.0 ) );
    if ( favBtn != null )
    {
      profileButton.setEnabled( profilesModel.size() > 0 );
      if ( profilesModel.size() == 0 )
      {
        allButton.setSelected( true );
      }
      activityGroupModel.set( favBtn, remoteConfig, getActivity() );
      activityGroupTable.initColumns( activityGroupModel );
      deviceButtonBox.setModel( comboModel );
      deviceButtonBox.setSelectedItem( remoteConfig.getFavKeyDevButton() ); 
    }
    Button favFinalKey = remoteConfig.getFavFinalKey();
    boolean showFinal = favFinalKey != null;
    addFinal.setSelected( showFinal );
    finalKey.setVisible( showFinal );
    finalKeyLabel.setVisible( showFinal );
    if ( showFinal )
    {
      finalKey.setText( favFinalKey.getName() );
    }
    else
    {
      finalKey.setText( remote.getUpgradeButtons()[ 0 ].getName() );
    }
    if ( remote.hasProfiles() )
    {
      profilesModel.clear();
      for ( Activity activity : remote.getFavKey().getProfiles() )
      {
        profilesModel.addElement( activity );
      }
      profiles.setModel( profilesModel );
    }
    upperPane.resetToPreferredSizes();
    iconImage.setImage( null );
  }
  
  public void finishEditing()
  {
    if ( favTable.getCellEditor() != null )
    {
      favTable.getCellEditor().stopCellEditing();
    }
    if ( activityGroupTable.getCellEditor() != null )
    {
      activityGroupTable.getCellEditor().stopCellEditing();
    }
  }
  
  private void setHighlightAction( JP1Table table )
  {
    remoteConfig.getOwner().highlightAction.setEnabled( table.getSelectedRowCount() > 0 );
  }
  
  public JP1Table getActiveTable()
  {
    return activeTable;
  }
  
  @Override
  public void actionPerformed( ActionEvent event )
  {
    Object source = event.getSource();
    List< FavScan > favScans = remoteConfig.getFavScans();
    finishEditing();
    int row = 0;
    Remote remote = remoteConfig.getRemote();
    if ( source.getClass() == JButton.class )
    {
      row = favTable.getSelectedRow();
    }

    if ( source == deviceButtonBox )
    {
      DeviceButton deviceButton = ( DeviceButton )deviceButtonBox.getSelectedItem();
      if ( deviceButton != remoteConfig.getFavKeyDevButton() )
      {
        remoteConfig.setFavKeyDevButton( deviceButton );
      }
      propertyChangeSupport.firePropertyChange( "data", null, null );
    }
    else if ( source == addFinal )
    {
      boolean checked = addFinal.isSelected();
      finalKey.setVisible( checked );
      finalKeyLabel.setVisible( checked );
      Button btn = checked ? remote.getButton( finalKey.getText() ) : null;
      remoteConfig.setFavFinalKey( btn );
      propertyChangeSupport.firePropertyChange( "data", null, null );
    }
    else if ( source == upButton || source == downButton )
    {
      FavScan favScan = favScans.get( row );
      favScans.remove( row );
      int toRow = ( source == upButton ) ? row - 1 : row + 1;
      favScans.add( toRow, favScan );      
      favModel.fireTableRowsUpdated( Math.min( row, toRow ), Math.max( row, toRow ));
      favTable.setRowSelectionInterval( toRow, toRow );
    }
    else if ( source == deleteButton )
    {
      int[] rows = favTable.getSelectedRows();
      Arrays.sort( rows );
      for ( int i = rows.length - 1; i >= 0; i-- )
      {
        favScans.remove( rows[ i ] );
      }
      favModel.fireTableRowsDeleted( rows[ 0 ], rows[ rows.length - 1 ] );
    }
    else if ( source == newButton )
    {
      newRowObject();

    }
    else if ( source == cloneButton )
    {
      FavScan orig = favScans.get( row );
      FavScan favScan = new FavScan( orig );
      favScan.setSegmentFlags( orig.getSegmentFlags() );
      favScans.add( favScan );
      row = favScans.size() - 1;
      favModel.fireTableRowsInserted( row, row );
      favTable.setRowSelectionInterval( row, row );
    }
    else if ( source == editButton )
    {
      editRowObject( row );
    }
    else if ( source == allButton )
    {
      favModel.fireTableStructureChanged();
      favTable.initColumns( favModel );
      groupPanel.setBorder( BorderFactory.createTitledBorder( " Favorites Group Assignments " ) );
      activityGroupModel.set( favBtn, remoteConfig, getActivity() );
      activityGroupTable.initColumns( activityGroupModel );
      repaint();
    }
    else if ( source == profileButton )
    {
      if ( profilesModel.size() > 0 && profiles.getSelectedIndex() < 0 )
      {
        profiles.setSelectedIndex( 0 );
      }
      favModel.fireTableStructureChanged();
      favTable.initColumns( favModel );
      activityGroupModel.set( favBtn, remoteConfig, getActivity() );
      activityGroupTable.initColumns( activityGroupModel );
      groupPanel.setBorder( BorderFactory.createTitledBorder( " Profile Group Assignments " ) );
      repaint();
    }
    else if ( source == newProfile )
    {
      List< Integer > indices = new ArrayList< Integer >();
      for ( Activity a: remote.getFavKey().getProfiles() )
      {
        if ( !indices.contains( a.getProfileIndex() ) );
        {
          indices.add( a.getProfileIndex() );
        }
      }
      Collections.sort( indices );
      for ( int i = 0; ; i++ )
      {
        if ( !indices.contains( i ) )
        {
          Activity activity = remote.getFavKey().createProfile( "New profile", i, remote );
          profilesModel.addElement( activity );
          profiles.setModel( profilesModel );
          profiles.setSelectedValue( activity, true );
          break;
        }
      }
    }
    else if ( source == deleteProfile )
    {
      Activity activity = ( Activity )profiles.getSelectedValue();
      int i = profiles.getSelectedIndex();
      int index = activity.getProfileIndex();
      for ( FavScan favScan : remoteConfig.getFavScans() )
      {
        favScan.getProfileIndices().remove( ( Integer )index );
      }
      remote.getFavKey().getProfiles().remove( activity );
      profilesModel.removeElement( activity );
      profiles.setModel( profilesModel );
      if ( !profilesModel.isEmpty() )
      {
        profiles.setSelectedIndex( i < profilesModel.size() ? i : i - 1 );
      }
    }
    else if ( source == upProfile || source == downProfile )
    {
      Activity activity = ( Activity )profiles.getSelectedValue();
      int i = profiles.getSelectedIndex();
      profilesModel.remove( i );
      remote.getFavKey().getProfiles().remove( i );
      int toRow = ( source == upProfile ) ? i - 1 : i + 1;
      profilesModel.add( toRow, activity );
      remote.getFavKey().getProfiles().add( toRow, activity );
      profiles.setModel( profilesModel );
      profiles.setSelectedIndex( toRow );
    }
    activityGroupTable.setVisible( favTable.getModel().getRowCount() > 0 );
  }
  
  @Override
  public void valueChanged( ListSelectionEvent e )
  {
    Object source = e.getSource();
    if ( source == favTable.getSelectionModel() )
    {
      if ( !e.getValueIsAdjusting() )
      {
        if ( favTable.getSelectedRowCount() == 1 )
        {
          int row = favTable.getSelectedRow();
          boolean selected = row != -1;
          upButton.setEnabled( row > 0 );
          downButton.setEnabled( selected && row < favTable.getRowCount() - 1 );
          cloneButton.setEnabled( true );
          editButton.setEnabled( true );
          Integer iconRef = remoteConfig.getFavScans().get( row ).getIconref();
          BufferedImage image = null;
          if ( remoteConfig.getUserIcons() != null && iconRef != null && iconRef >= 127 )
          {
            Icon icon = remoteConfig.getUserIcons().get( iconRef );
            image = icon != null ? icon.image : null;
          }
          iconImage.setImage( image );
        }
        else
        {
          upButton.setEnabled( false );
          downButton.setEnabled( false );
          cloneButton.setEnabled( false );
          editButton.setEnabled( false );
          iconImage.setImage( null );
        }
        repaint();
        deleteButton.setEnabled( favTable.getSelectedRowCount() > 0 );
      }
    }
    else if ( source == profiles )
    {
      Activity a = ( Activity )profiles.getSelectedValue();
      int index = profiles.getSelectedIndex();
      if ( a != null )
      {
        profileField.setText( a.getName() );
        upProfile.setEnabled( index > 0 );
        downProfile.setEnabled( index < profilesModel.getSize() - 1 );
        deleteProfile.setEnabled( true );
        Integer iconref = a.getIconref();
        BufferedImage image = null;
        if ( iconref != null && remoteConfig.getUserIcons() != null && iconref >= 127 )
        {
          Icon icon = remoteConfig.getUserIcons().get( iconref );
          image = icon != null ? icon.image : null;
        }
        profileIcon.setImage( image );
      }
      else
      {
        profileField.setText( "" );
        upProfile.setEnabled( false );
        downProfile.setEnabled( false );
        deleteProfile.setEnabled( false );
        profileIcon.setImage( null );
      }
      if ( profileButton.isSelected() )
      {
        activityGroupModel.set( favBtn, remoteConfig, a );
        activityGroupTable.initColumns( activityGroupModel );
      }
      repaint();
    }
  }
  
  public Activity getActivity()
  {
    if ( profileButton.isSelected() )
    {
      return ( Activity )profiles.getSelectedValue();
    }
    else
    {
      Remote remote = remoteConfig.getRemote();
      return remoteConfig.getActivities().get( remote.getButtonByStandardName( "Favorites"  ) );
    }
  }
  
  public boolean showProfile()
  {
    return profileButton.isSelected();
  }
  
  private void editRowObject( int row )
  {
    List< FavScan > favScans = remoteConfig.getFavScans();
    FavScan favScan = FavScanDialog.showDialog( this, favScans.get( row ), remoteConfig );
    if ( favScan != null )
    {
      favScans.set( row, favScan );
      favModel.fireTableRowsUpdated( row, row );
      propertyChangeSupport.firePropertyChange( "data", null, null );
    }
  }
  
  private void documentChanged( DocumentEvent e )
  {
    Document doc = e.getDocument();
    if ( doc == profileField.getDocument() )
    {
      Activity activity = ( Activity )profiles.getSelectedValue();
      if ( activity != null )
      {
        activity.setName( profileField.getText() );
      }
    }
  }
  
  @Override
  public void changedUpdate( DocumentEvent e )
  {
    documentChanged( e );
  }
  
  @Override
  public void insertUpdate( DocumentEvent e )
  {
    documentChanged( e );
  }

  @Override
  public void removeUpdate( DocumentEvent e )
  {
    documentChanged( e );
  }
  
  private void newRowObject()
  {
    List< FavScan > favScans = remoteConfig.getFavScans();
    FavScan favScan = FavScanDialog.showDialog( this, null, remoteConfig );
    if ( favScan != null )
    {
      if ( remoteConfig.getRemote().isSSD() )
      {
        List< Integer > serials = new ArrayList< Integer >();
        for ( FavScan fs : favScans )
        {
          if ( !serials.contains( fs.getSerial() ) )
          {
            serials.add( fs.getSerial() );
          }
        }
        Collections.sort( serials );
        for ( int i = 0; ; i++ )
        {
          if ( !serials.contains( i ) )
          {
            favScan.setSerial( i );
            break;
          }
        }
        favScan.setProfileIndices( new ArrayList< Integer >() );
      }
      favScan.setSegmentFlags( 0xFF );
      favScans.add( favScan );
      int row = favScans.size() - 1;
      if ( favTable.getSelectedRowCount() == 0 )
      {
        favTable.setColumnSelectionInterval( 1, 1 );
      }
      favModel.fireTableRowsInserted( row, row );
      favTable.setRowSelectionInterval( row, row );
      favTable.requestFocusInWindow();
    }
  }
  
  public JList getProfiles()
  {
    return profiles;
  }
  
//  public class IconImage extends Component
//  {               
//    private BufferedImage image = null;
//
//    public void setImage( BufferedImage image )
//    {
//      this.image = image;
//    }
//
//    public void paint( Graphics g ) 
//    {
//      int y = image == null ? 0 : ( 40 - image.getHeight() ) / 2;
//      g.drawImage( image, 0, Math.max( 0, y ), null ); 
//    }     
//
//    public Dimension getPreferredSize() 
//    {                     
//      return new Dimension( 100, 40 );        
//    }
//  }

  private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport( this );
  private RemoteConfiguration remoteConfig = null;
  public MouseListener openEditor = null;
  private JP1Table favTable = null;
  private JP1Table activeTable = null;
  private FavScanTableModel favModel = null;
  private JP1Table activityGroupTable = null;
  private ActivityGroupTableModel activityGroupModel = new ActivityGroupTableModel();
  private Button favBtn = null;
  private JPanel deviceBoxPanel = null;
  private JFormattedTextField duration = null;
  private JComboBox deviceButtonBox = null;
  private JButton editButton = null;
  private JButton newButton = null;
  private JButton cloneButton = null;
  private JButton deleteButton = null;
  private JButton upButton = null;
  private JButton downButton = null;
  private JButton newProfile = null;
  private JButton deleteProfile = null;
  private JButton upProfile = null;
  private JButton downProfile = null;
  private JCheckBox addFinal = null;
  private JTextField finalKey = null;
  private JLabel finalKeyLabel = null;
  private JPanel profilesPanel = null;
  private JPanel groupPanel = null;
  private JList profiles = new JList();
  private DefaultListModel profilesModel = new DefaultListModel();
  private JSplitPane upperPane = null;
  private JRadioButton allButton = new JRadioButton( "Show all favorites" );
  private JRadioButton profileButton = new JRadioButton( "Show selected profile" );
  private JTextField profileField = null;
  private IconImage iconImage = null;
  private IconImage profileIcon = null;
}
