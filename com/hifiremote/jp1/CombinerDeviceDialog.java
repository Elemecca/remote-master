package com.hifiremote.jp1;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

// TODO: Auto-generated Javadoc
/**
 * The Class CombinerDeviceDialog.
 */
public class CombinerDeviceDialog
  extends JDialog
  implements ActionListener, ItemListener, DocumentListener
{
  
  /**
   * Instantiates a new combiner device dialog.
   * 
   * @param owner the owner
   * @param dev the dev
   * @param r the r
   */
  public CombinerDeviceDialog( JFrame owner, CombinerDevice dev, Remote r )
  {
    super( owner, "Combiner Device", true );
    createGui( owner, dev, r );
  }

  /**
   * Instantiates a new combiner device dialog.
   * 
   * @param owner the owner
   * @param dev the dev
   * @param r the r
   */
  public CombinerDeviceDialog( JDialog owner, CombinerDevice dev, Remote r )
  {
    super( owner, "Combiner Device", true );
    createGui( owner, dev, r );
  }

  /**
   * Creates the gui.
   * 
   * @param owner the owner
   * @param dev the dev
   * @param r the r
   */
  private void createGui( Component owner, CombinerDevice dev, Remote r )
  {
    setLocationRelativeTo( owner );
    Container contentPane = getContentPane();
    remote = r;

    protocolHolder = new JPanel( new BorderLayout());
    protocolHolder.setBorder( BorderFactory.createTitledBorder( "Protocol Parameters" ));

    Insets insets = protocolHolder.getInsets();
    double bt = insets.top;
    double bl = insets.left + 10;
    double br = insets.right;
    double bb = insets.bottom;
    double b = 10;       // space around border
    double i = 5;        // space between rows
    double v = 20;       // space between groupings
    double f = TableLayout.FILL;
    double p = TableLayout.PREFERRED;
    double size[][] =
    {// 0  1   2  3  4  5   6  7   8  9  10 11
      { b, bl, p, b, p, br, b, f, b },             // cols
      { b, p,  i, p, v, bt, p, bb, i, f, b }    // rows
    };
    tl = new TableLayout( size );
    mainPanel = new JPanel( tl );
    contentPane.add( mainPanel, BorderLayout.CENTER );

    JLabel label = new JLabel( "Protocol:", SwingConstants.RIGHT );
    mainPanel.add( label, "2, 1" );

    boolean allowUpgrades =  r.getProcessor().getEquivalentName().equals( "S3C80" );
    java.util.List< Protocol > allProtocols =
      ProtocolManager.getProtocolManager().getProtocolsForRemote( r, allowUpgrades );
    java.util.List< Protocol > protocols = new ArrayList< Protocol >();
    if ( allowUpgrades )
      protocols.add( new ManualProtocol( null, null ));
    for ( Protocol protocol : allProtocols )
    {
      if ( protocol.getDefaultCmd().length() == 1 )
        protocols.add( protocol );
    }
    if ( dev == null )
      device = new CombinerDevice(( Protocol )protocols.get( 1 ), new Value[ 0 ]);
    else
      device = new CombinerDevice( dev );

    device.getProtocol().reset();

    protocolList = new JComboBox( protocols.toArray());
    protocolList.addActionListener( this );
    label.setLabelFor( protocolList );
    protocolList.setToolTipText( "Select the protocol to be used for this device upgrade from the drop-down list." );
    mainPanel.add( protocolList, "4, 1" );

    deviceNotes = new JTextField();
    new TextPopupMenu( deviceNotes );
    deviceNotes.getDocument().addDocumentListener( this );
    JPanel temp = new JPanel( new BorderLayout());
    temp.add( deviceNotes, BorderLayout.CENTER );
    temp.setBorder( BorderFactory.createTitledBorder( "Notes" ));

    mainPanel.add( temp, "7, 1, 7, 3" );

    label = new JLabel( "Protocol ID:", SwingConstants.RIGHT );
    mainPanel.add( label, "2, 3" );

    protocolID = new JTextField();
    label.setLabelFor( protocolID );
    protocolID.setEditable( false );
    protocolID.setToolTipText( "This is the protocol ID that corresponds to the selected protocol." );
    mainPanel.add( protocolID, "4, 3" );

    label = new JLabel( "Fixed Data:", SwingConstants.RIGHT );
    mainPanel.add( label, "2, 6" );

    fixedData = new JTextField( " " );
    fixedData.setEditable( false );
    mainPanel.add( fixedData, "4, 6" );

    boolean flag = ( device.getProtocol().getClass() == ManualProtocol.class );
    fixedData.setEditable( flag );
    protocolID.setEditable( flag );

    mainPanel.add( protocolHolder, "1, 5, 5, 7" );

    protocolNotes = new JTextArea( 15, 60 );
    protocolNotes.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ));
    protocolNotes.setText( protocols.get( 0 ).getNotes());
    protocolNotes.setBackground( label.getBackground());
    protocolNotes.setToolTipText( "Notes about the selected protocol." );
    protocolNotes.setEditable( false );
    protocolNotes.setLineWrap( true );
    protocolNotes.setWrapStyleWord( true );
    JScrollPane scrollPane = new JScrollPane( protocolNotes );
    scrollPane.setBorder(
      BorderFactory.createCompoundBorder(
        BorderFactory.createTitledBorder( "Protocol Notes" ),
        scrollPane.getBorder()));
    mainPanel.add( scrollPane, "1, 9, 7, 9" );

    JPanel panel = new JPanel( new FlowLayout( FlowLayout.RIGHT ));
    contentPane.add( panel, BorderLayout.SOUTH );

    okButton = new JButton( "OK" );
    okButton.addActionListener( this );
    panel.add( okButton );

    cancelButton = new JButton( "Cancel" );
    cancelButton.addActionListener( this );
    panel.add( cancelButton );

    update();
    pack();

    Rectangle rect = getBounds();
    int x = rect.x - rect.width / 2;
    int y = rect.y - rect.height / 2;
    setLocation( x, y );
  }

  /**
   * Update.
   */
  public void update()
  {
    Protocol p = device.getProtocol();
    p.setDeviceParms( device.getValues());
    updateParameters();
    protocolList.setSelectedItem( p );
    if ( protocolList.getSelectedItem() != p )
    {
      protocolList.addItem( p );
      protocolList.setSelectedItem( p );

    }
    fixedData.getDocument().removeDocumentListener( this );
    protocolID.getDocument().removeDocumentListener( this );
    deviceNotes.getDocument().removeDocumentListener( this );

    fixedData.setText( device.getFixedData().toString());
    Hex id = p.getID( remote );
    if ( id != null )
      protocolID.setText( id.toString());
    else
      protocolID.setText( null );
    deviceNotes.setText( device.getNotes());

    fixedData.getDocument().addDocumentListener( this );
    protocolID.getDocument().addDocumentListener( this );
    deviceNotes.getDocument().addDocumentListener( this );

    protocolNotes.setText( p.getNotes());
    protocolNotes.setCaretPosition( 0 );
  }

  /**
   * Update parameters.
   */
  public void updateParameters()
  {
    DeviceParameter[] newParameters = device.getProtocol().getDeviceParameters();
    if ( parameters != newParameters )
    {
      removeParameters( parameters );
      parameters = newParameters;
      addParameters( parameters );
    }
  }

  /**
   * Removes the parameters.
   * 
   * @param parameters the parameters
   */
  private void removeParameters( DeviceParameter[] parameters )
  {
    if ( parameters != null )
    {
      for ( int i = 0; i < parameters.length; i++ )
      {
        parameters[ i ].removeListener( this );
        mainPanel.remove( parameters[ i ].getLabel());
        mainPanel.remove( parameters[ i ].getComponent());
        tl.deleteRow( 6 );
        tl.deleteRow( 6 );
      }
    }
  }

  /**
   * Adds the parameters.
   * 
   * @param parameters the parameters
   */
  private void addParameters( DeviceParameter[] parameters )
  {
    if ( parameters != null )
    {
      int row = 6;
      for ( int i = 0; i < parameters.length; i++ )
      {
        parameters[ i ].addListener( this );
        tl.insertRow( row, TableLayout.PREFERRED );
        mainPanel.add( parameters[ i ].getLabel(), "2, " + row );
        mainPanel.add( parameters[ i ].getComponent() , "4, " + row );
        row++;
        tl.insertRow( row++, 5 );
      }
      TableLayoutConstraints tlc = tl.getConstraints( protocolHolder );
      mainPanel.remove( protocolHolder );
      mainPanel.add( protocolHolder, tlc );
    }
  }


  /**
   * Update fixed data.
   */
  public void updateFixedData()
  {
    Protocol p = device.getProtocol();
    device.setValues( p.getDeviceParmValues());
    fixedData.getDocument().removeDocumentListener( this );
    fixedData.setText( device.getFixedData().toString());
    fixedData.getDocument().addDocumentListener( this );
  }

  // ActionListener Methods
  /* (non-Javadoc)
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed( ActionEvent e )
  {
    Object source = e.getSource();

    if ( source == protocolList )
    {
      Protocol newProtocol = ( Protocol )protocolList.getSelectedItem();
      Protocol oldProtocol = device.getProtocol();
      if ( newProtocol != oldProtocol )
      {
        oldProtocol.reset();
        protocolID.getDocument().removeDocumentListener( this );
        Hex id = newProtocol.getID( remote );
        if ( id != null )
          protocolID.setText( id.toString());
        else
          protocolID.setText( null );
        protocolID.getDocument().addDocumentListener( this );
        device.setProtocol( newProtocol );
        updateParameters();
        fixedData.getDocument().removeDocumentListener( this );
        fixedData.setText( newProtocol.getFixedData( newProtocol.getDeviceParmValues()).toString());
        fixedData.getDocument().addDocumentListener( this );
        boolean flag = ( newProtocol.getClass() == ManualProtocol.class );
        fixedData.setEditable( flag );
        protocolID.setEditable( flag );
        validate();
        protocolNotes.setText( newProtocol.getNotes());
        protocolNotes.setCaretPosition( 0 );
        protocolNotes.revalidate();
      }
    }
    if ( source == cancelButton )
    {
      userAction = JOptionPane.CANCEL_OPTION;
      setVisible( false );
      removeParameters( parameters );
      device.getProtocol().reset();
      dispose();
    }
    else if ( source == okButton )
    {
      userAction = JOptionPane.OK_OPTION;
      setVisible( false );
      removeParameters( parameters );
      device.getProtocol().reset();
      dispose();
    }
    else // must be a protocol parameter
      updateFixedData();
  } // actionPerformed

  // DocumentListener
  /* (non-Javadoc)
   * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
   */
  public void changedUpdate( DocumentEvent e )
  {
    docUpdated( e );
  }

  /* (non-Javadoc)
   * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
   */
  public void insertUpdate( DocumentEvent e )
  {
    docUpdated( e );
  }

  /* (non-Javadoc)
   * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
   */
  public void removeUpdate( DocumentEvent e )
  {
    docUpdated( e );
  }

  /**
   * Doc updated.
   * 
   * @param e the e
   */
  private void docUpdated( DocumentEvent e )
  {
    Document doc = e.getDocument();
    if ( doc == protocolID.getDocument())
      (( ManualProtocol )device.getProtocol()).setID( new Hex( protocolID.getText()));
    else if ( doc == fixedData.getDocument())
      (( ManualProtocol )device.getProtocol()).setRawHex( new Hex( fixedData.getText()));
    else if ( doc == deviceNotes.getDocument())
      device.setNotes( deviceNotes.getText());
    else
      updateFixedData();
  }

  // ItemListener
  /* (non-Javadoc)
   * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
   */
  public void itemStateChanged( ItemEvent e )
  {
    updateFixedData();
  }

  /**
   * Gets the user action.
   * 
   * @return the user action
   */
  public int getUserAction()
  {
    return userAction;
  }

  /**
   * Gets the combiner device.
   * 
   * @return the combiner device
   */
  public CombinerDevice getCombinerDevice()
  {
    return device;
  }

  /** The device. */
  private CombinerDevice device = null;
  
  /** The remote. */
  private Remote remote = null;
  
  /** The main panel. */
  private JPanel mainPanel = null;
  
  /** The protocol holder. */
  private JPanel protocolHolder = null;
  
  /** The protocol list. */
  private JComboBox protocolList = null;
  
  /** The device notes. */
  private JTextField deviceNotes = null;
  
  /** The protocol id. */
  private JTextField protocolID = null;
  
  /** The fixed data. */
  private JTextField fixedData = null;
  
  /** The protocol notes. */
  private JTextArea protocolNotes = null;
  
  /** The tl. */
  private TableLayout tl = null;
  
  /** The parameters. */
  private DeviceParameter[] parameters = null;
  
  /** The ok button. */
  private JButton okButton = null;
  
  /** The cancel button. */
  private JButton cancelButton = null;

  /** The user action. */
  private int userAction = JOptionPane.CANCEL_OPTION;
}
