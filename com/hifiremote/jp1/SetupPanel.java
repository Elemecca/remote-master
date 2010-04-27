package com.hifiremote.jp1;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.SwingPropertyChangeSupport;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;

// TODO: Auto-generated Javadoc
/**
 * The Class SetupPanel.
 */
public class SetupPanel extends KMPanel implements ActionListener, ItemListener, PropertyChangeListener,
    DocumentListener, FocusListener
{
  /**
   * Instantiates a new setup panel.
   * 
   * @param deviceUpgrade
   *          the device upgrade
   */
  public SetupPanel( DeviceUpgrade deviceUpgrade )
  {
    super( "Setup", deviceUpgrade );

    protocolHolder = new JPanel( new BorderLayout() );
    Border border = BorderFactory.createTitledBorder( "Protocol Parameters" );
    protocolHolder.setBorder( border );

    Insets insets = border.getBorderInsets( protocolHolder );
    double bt = insets.top;
    double bl = insets.left + 10;
    double br = insets.right;
    double bb = insets.bottom;
    double b = 10; // space around border
    double i = 5; // space between rows
    double v = 20; // space between groupings
    double c = 10; // space between columns
    double f = TableLayout.FILL;
    double p = TableLayout.PREFERRED;
    double size[][] =
    {
        {
            b, bl, p, b, p, br, c, f, b
        }, // cols
        {
            b, p, v, p, i, p, v, bt, p, bb, f, b
        }
    // rows
    };
    tl = new TableLayout( size );
    setLayout( tl );

    JLabel label = new JLabel( "Setup Code:", SwingConstants.RIGHT );
    add( label, "2, 1" );
    setupCode = new JTextField();
    SetupCodeFilter filter = new SetupCodeFilter( setupCode );
    ( ( AbstractDocument )setupCode.getDocument() ).setDocumentFilter( filter );
    // setupCode.addPropertyChangeListener( "value", this );
    setupCode.getDocument().addDocumentListener( this );
    FocusSelector.selectOnFocus( setupCode );
    label.setLabelFor( setupCode );
    setupCode.setToolTipText( "Enter the desired setup code (between 0 and " + SetupCode.getMax()
        + ") for the device upgrade." );

    add( setupCode, "4, 1" );

    label = new JLabel( "Protocol:", SwingConstants.RIGHT );
    add( label, "2, 3" );

    protocolList = new JComboBox();
    protocolList.addActionListener( this );
    label.setLabelFor( protocolList );
    protocolList.setMaximumRowCount( 25 );
    protocolList.setToolTipText( "Select the protocol to be used for this device upgrade from the drop-down list." );
    add( protocolList, "4, 3" );

    label = new JLabel( "Protocol ID:", SwingConstants.RIGHT );
    add( label, "2, 5" );

    protocolID = new JTextField();
    label.setLabelFor( protocolID );
    protocolID.setEditable( false );
    protocolID.setToolTipText( "This is the protocol ID that corresponds to the selected protocol." );
    add( protocolID, "4, 5" );

    add( protocolHolder, "1, 7, 5, 9" );
    label = new JLabel( "Fixed Data:", SwingConstants.RIGHT );
    add( label, "2, 8" );

    fixedData = new JTextField();
    fixedData.setEditable( false );
    add( fixedData, "4, 8" );

    notes = new JTextArea( 5, 50 );
    notes.setToolTipText( "Enter any notes about this device upgrade." );
    notes.setLineWrap( true );
    notes.setWrapStyleWord( true );
    JScrollPane scrollPane = new JScrollPane( notes );
    scrollPane.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createTitledBorder( "Upgrade Notes" ),
        scrollPane.getBorder() ) );
    notes.getDocument().addDocumentListener( this );
    new TextPopupMenu( notes );
    add( scrollPane, "7, 1, 7, 9" );

    protocolNotes = new JEditorPane();
    protocolNotes.setBackground( label.getBackground() );
    protocolNotes.setToolTipText( "Notes about the selected protocol." );
    protocolNotes.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
    protocolNotes.setEditable( false );
    // protocolNotes.setLineWrap( true );
    // protocolNotes.setWrapStyleWord( true );
    scrollPane = new JScrollPane( protocolNotes );
    scrollPane.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createTitledBorder( "Protocol Notes" ),
        scrollPane.getBorder() ) );
    add( scrollPane, "1, 10, 7, 10" );
  } // SetupPanel

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.KMPanel#update()
   */
  public void update()
  {
    if ( deviceUpgrade == null )
      return;
    updateInProgress = true;
    Protocol p = deviceUpgrade.getProtocol();
    Remote remote = deviceUpgrade.getRemote();
    setupCode.setText( SetupCode.toString( deviceUpgrade.getSetupCode() ) );
    setupCode.setToolTipText( "Enter the desired setup code (between 0 and " + SetupCode.getMax()
        + ") for the device upgrade." );
    java.util.List< Protocol > protocols = ProtocolManager.getProtocolManager().getProtocolsForRemote( remote );
    if ( !protocols.contains( p ) )
    {
      // ??? There should be a better way to handle this (the current protocol is
      // incompatible with the current remote), but this way is at least better than
      // the old way of displaying the first compatible protocol.
      protocols = new ArrayList< Protocol >( protocols );
      protocols.add( p );
    }

    Value[] vals = deviceUpgrade.getParmValues();
    p.setDeviceParms( vals );
    updateParameters();
    protocolList.setModel( new DefaultComboBoxModel( protocols.toArray() ) );
    protocolList.setSelectedItem( p );
    protocolID.setText( p.getID( remote ).toString() );
    notes.setText( deviceUpgrade.getNotes() );
    fixedData.setText( p.getFixedData( vals ).toString() );

    updateProtocolNotes( p.getNotes() );

    updateInProgress = false;
  }

  /**
   * Update parameters.
   */
  public void updateParameters()
  {
    DeviceParameter[] newParameters = deviceUpgrade.getProtocol().getDeviceParameters();
    if ( parameters != newParameters )
    {
      if ( parameters != null )
      {
        for ( int i = 0; i < parameters.length; i++ )
        {
          parameters[ i ].removeListener( this );
          remove( parameters[ i ].getLabel() );
          remove( parameters[ i ].getComponent() );
          tl.deleteRow( 8 );
          tl.deleteRow( 8 );
        }
      }
      parameters = newParameters;
      if ( parameters != null )
      {
        int row = 8;
        for ( int i = 0; i < parameters.length; i++ )
        {
          parameters[ i ].addListener( this );
          tl.insertRow( row, TableLayout.PREFERRED );
          add( parameters[ i ].getLabel(), "2, " + row );
          add( parameters[ i ].getComponent(), "4, " + row );
          row++ ;
          tl.insertRow( row++ , 5 );
        }
        TableLayoutConstraints tlc = tl.getConstraints( protocolHolder );
        remove( protocolHolder );
        add( protocolHolder, tlc );
      }
    }
  }

  /**
   * Update fixed data.
   */
  public void updateFixedData()
  {
    Protocol p = deviceUpgrade.getProtocol();
    p.initializeParms();
    deviceUpgrade.setParmValues( p.getDeviceParmValues() );
    fixedData.setText( p.getFixedData( deviceUpgrade.getParmValues() ).toString() );
  }

  // ActionListener Methods
  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed( ActionEvent e )
  {
    Object source = e.getSource();

    if ( source == protocolList )
    {
      Protocol newProtocol = getSelectedProtocol();
      Protocol oldProtocol = deviceUpgrade.getProtocol();
      if ( newProtocol != oldProtocol )
      {
        if ( deviceUpgrade.setProtocol( newProtocol ) )
        {
          protocolID.setText( newProtocol.getID( deviceUpgrade.getRemote() ).toString() );
          updateParameters();
          fixedData.setText( newProtocol.getFixedData( newProtocol.getDeviceParmValues() ).toString() );
          updateProtocolNotes( newProtocol.getNotes() );
          revalidate();

          deviceUpgrade.checkSize();
          propertyChangeSupport.firePropertyChange( "protocol", oldProtocol, newProtocol );
        }
        else
        {
          protocolList.removeActionListener( this );
          protocolList.setSelectedItem( oldProtocol );
          protocolList.addActionListener( this );
        }
      }
    }
    else
      // must be a protocol parameter
      updateFixedData();
  } // actionPerformed

  protected void updateProtocolNotes( String text )
  {
    String contentType = "text/plain";
    if ( text != null && text.startsWith( "<" ) )
    {
      contentType = "text/html";
    }
    EditorKit kit = protocolNotes.getEditorKitForContentType( contentType );
    protocolNotes.setEditorKit( kit );
    protocolNotes.setText( text );
    protocolNotes.setCaretPosition( 0 );
    protocolNotes.revalidate();
  }

  /**
   * Gets the selected protocol.
   * 
   * @return the selected protocol
   */
  public Protocol getSelectedProtocol()
  {
    Protocol protocol = ( Protocol )protocolList.getSelectedItem();
    return protocol;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.KMPanel#commit()
   */
  public void commit()
  {
    deviceUpgrade.getProtocol().updateFunctions( deviceUpgrade.getFunctions() );
  }

  /**
   * Update notes.
   */
  private void updateNotes()
  {
    deviceUpgrade.setNotes( notes.getText() );
  }

  /**
   * Update setup code.
   */
  private void updateSetupCode()
  {
    String text = setupCode.getText();
    if ( text.equals( "" ) )
    {
      return;
    }
    int val = Integer.parseInt( setupCode.getText() );
    int oldSetupCode = deviceUpgrade.getSetupCode();
    deviceUpgrade.setSetupCode( val );
    propertyChangeSupport.firePropertyChange( "setupCode", oldSetupCode, val );
  }

  public void release()
  {
    for ( int i = 0; i < parameters.length; i++ )
    {
      parameters[ i ].removeListener( this );
    }
  }
  /**
   * Doc changed.
   * 
   * @param e
   *          the e
   */
  private void docChanged( DocumentEvent e )
  {
    if ( !updateInProgress )
    {
      Document doc = e.getDocument();
      if ( doc == notes.getDocument() )
        updateNotes();
      else if ( doc == setupCode.getDocument() )
        updateSetupCode();
      else
        updateFixedData();
    }
  }

  // DocumentListener
  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
   */
  public void changedUpdate( DocumentEvent e )
  {
    docChanged( e );
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
   */
  public void insertUpdate( DocumentEvent e )
  {
    docChanged( e );
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
   */
  public void removeUpdate( DocumentEvent e )
  {
    docChanged( e );
  }

  // FocusListener
  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
   */
  public void focusGained( FocusEvent e )
  {
    JP1Frame.clearMessage( controlWithFocus );
    controlWithFocus = ( JTextComponent )e.getSource();
    JP1Frame.clearMessage( controlWithFocus );
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
   */
  public void focusLost( FocusEvent e )
  {}

  // ItemListener
  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
   */
  public void itemStateChanged( ItemEvent e )
  {
    if ( !updateInProgress )
      updateFixedData();
  }

  // PropertyChangeListener methods
  /*
   * (non-Javadoc)
   * 
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  public void propertyChange( PropertyChangeEvent e )
  {
    if ( !updateInProgress )
    {
      updateSetupCode();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.Container#addPropertyChangeListener(java.beans.PropertyChangeListener)
   */
  public void addPropertyChangeListener( PropertyChangeListener listener )
  {
    if ( ( propertyChangeSupport != null ) && ( listener != null ) )
      propertyChangeSupport.addPropertyChangeListener( listener );
  }

  /** The setup code. */
  private JTextField setupCode = null;

  /** The protocol list. */
  private JComboBox protocolList = null;

  /** The protocol id. */
  private JTextField protocolID = null;

  /** The notes. */
  private JTextArea notes = null;

  /** The protocol holder. */
  private JPanel protocolHolder = null;

  /** The fixed data. */
  private JTextField fixedData = null;

  /** The protocol notes. */
  private JEditorPane protocolNotes = null;

  /** The parameters. */
  private DeviceParameter[] parameters = null;

  /** The tl. */
  private TableLayout tl;

  /** The update in progress. */
  private boolean updateInProgress = false;

  /** The control to select all. */
  private JTextComponent controlWithFocus = null;

  /** The property change support. */
  private SwingPropertyChangeSupport propertyChangeSupport = new SwingPropertyChangeSupport( this );
}
