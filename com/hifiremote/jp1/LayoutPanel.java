package com.hifiremote.jp1;

import java.awt.*;
import javax.swing.border.*;
import javax.swing.*;
import java.text.*;
import javax.swing.text.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.event.*;
import java.util.*;
import java.awt.datatransfer.*;
import info.clearthought.layout.*;

public class LayoutPanel
  extends KMPanel
  implements ActionListener
{
  public LayoutPanel( DeviceUpgrade devUpgrade )
  {
    super( devUpgrade );
    setLayout( new BorderLayout());
    imagePanel = new JPanel()
    {
      public void paint( Graphics g )
      {
        Graphics2D g2 = ( Graphics2D ) g;
        g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON );
        Remote r = deviceUpgrade.getRemote();
        ImageIcon icon = r.getImageIcon();
        if ( icon != null )
          g2.drawImage( icon.getImage(), null, null );

        g2.setPaint( Color.blue );

        if ( currentButton != null )
        {
          g2.setPaint( Color.white );
          g2.fill( currentButton.getShape());
        }
        g2.setPaint( Color.yellow );
        g2.setStroke( new BasicStroke( 2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND ));

        DeviceType devType = deviceUpgrade.getDeviceType();
        ButtonMap map = devType.getButtonMap();

        for ( int i = 0; i < map.size(); i++ )
        {
          Button b = map.get( i );
          Shape s = b.getShape();
          if ( s != null )
            g2.draw( s );
          else
            System.err.println( "No shape for button " + b );
        }
      }
    };
    JPanel fPanel = new JPanel();
    fPanel.add( imagePanel );

    add( new JScrollPane( fPanel ), BorderLayout.WEST );
    JPanel infoPanel = new JPanel( new GridLayout( 2, 3 ));

    infoPanel.add( new JLabel( "Button:" ));
    infoPanel.add( new JLabel( "Function:" ));
    infoPanel.add( new JLabel( "Shifted:" ));
    buttonName = new JTextField();
    buttonName.setEditable( false );
    infoPanel.add( buttonName );

    function = new JTextField();
    function.setEditable( false );
    infoPanel.add( function );

    shifted = new JTextField();
    shifted.setEditable( false );
    infoPanel.add( shifted );

    add( infoPanel, BorderLayout.NORTH );

    JPanel panel = new JPanel( new BorderLayout());
    JLabel label = new JLabel( "Available Functions:" );
    label.setBorder( BorderFactory.createEmptyBorder( 2, 2, 3, 2 ));
    panel.add( label, BorderLayout.NORTH );
    add( panel, BorderLayout.CENTER );

    JPanel outerPanel = new JPanel( new BorderLayout());
    functionPanel = new JPanel( new GridLayout( 0, 4 ));
    
    outerPanel.add( new JScrollPane( functionPanel ), BorderLayout.NORTH );
    panel.add( outerPanel, BorderLayout.CENTER );

    MouseInputAdapter mia = new MouseInputAdapter()
    {
      public void mousePressed( MouseEvent e )
      {
        Point p = e.getPoint();
        Button savedButton = currentButton;
        currentButton = getButtonAtPoint( p );
        if ( currentButton != savedButton )
        {
          setButtonText( currentButton );
          doRepaint();
        }
        showPopup( e );
      }

      public void mouseReleased( MouseEvent e )
      {
        showPopup( e );
      }

      private void showPopup( MouseEvent e )
      {
        if ( e.isPopupTrigger() )
        {
          Button b = getButtonAtPoint( e.getPoint());
          if ( b != null )
          {
            currentButton = b;
            doRepaint();
            popup.show( imagePanel, e.getX(), e.getY());
          }
        }
      }

      public void mouseDragged( MouseEvent e )
      {
        buttonUnderMouse = getButtonAtPoint( e.getPoint());
        System.err.println( "mouseDragged: buttonUnderMouse is " + buttonUnderMouse );
      }
    };
    imagePanel.addMouseListener( mia );
    imagePanel.addMouseMotionListener( mia );

    TransferHandler th = new TransferHandler()
    {
      public boolean canImport( JComponent comp, DataFlavor[] flavors )
      {
        return ( buttonUnderMouse != null );
      }

      public boolean importData( JComponent c, Transferable t )
      {
        boolean rc = false;
        if ( buttonUnderMouse != null )
        {
          try
          {
            Function f = ( Function )t.getTransferData( LocalObjectTransferable.getFlavor());
            buttonUnderMouse.setFunction( f );
          }
          catch ( Exception e )
          {
            rc = false;
            System.err.println( "ButtonPanel.importData() caught an exception!" );
            e.printStackTrace( System.err );
          }
        }
        else
          rc = false;

        return rc;
      }
    };
  }

  private void addFunction( Function f )
  {
    if (( f == null ) ||
        (( f.getHex() != null ) && ( f.getName() != null ) && (f.getName().length() > 0 )))
    {
      FunctionLabel l;
      if ( f == null )
        l = new FunctionLabel( null );
      else
        l = f.getLabel();
      l.addMouseListener( doubleClickListener );
      functionPanel.add( l );

      FunctionItem item;
      if ( f == null )
        item = new FunctionItem( null );
      else
        item = f.getItem();
      item.addActionListener( this );
      popup.add( item );
    }
  }

  private void setFunctions()
  {
    popup = new JPopupMenu();
    popup.setLayout( new GridLayout( 0, 3 ));
    FunctionItem item = null;

    functionPanel.removeAll();
    FunctionLabel label = null;
    Function function = null;

    Vector funcs = deviceUpgrade.getFunctions();
    for ( int i = 0; i < funcs.size(); i++ )
    {
      function = ( Function )funcs.elementAt( i );
      addFunction( function );
    }
    funcs = deviceUpgrade.getExternalFunctions();
    for ( int i = 0; i < funcs.size(); i++ )
    {
      function = ( Function )funcs.elementAt( i );
      addFunction( function );
    }
    addFunction( null );
  }

  public void update()
  {
    ImageIcon icon = deviceUpgrade.getRemote().getImageIcon();
    if ( icon != null )
    {
      int w = icon.getIconWidth();
      int h = icon.getIconHeight();
      Dimension size = new Dimension( w, h );
      imagePanel.setPreferredSize( size );
      imagePanel.setMaximumSize( size );
      imagePanel.setMinimumSize( size );
      imagePanel.setSize( w, h );
    }
    Button[] buttons = deviceUpgrade.getRemote().getButtons();
    boolean found = false;
    for ( int i = 0; i < buttons.length; i++ )
    {
      if ( currentButton == buttons[ i ])
      {
        found = true;
        break;
      }
    }
    if ( !found )
      currentButton = null;

    setButtonText( currentButton );

    setFunctions();
    validate();
    doRepaint();
  }

  private void doRepaint()
  {
    imagePanel.repaint( 0L, 0, 0, imagePanel.getWidth(), imagePanel.getHeight());
  }

  private void setButtonText( Button b )
  {
    if ( b != null )
    {
      buttonName.setText( b.getName());
      Function f = b.getFunction();
      if ( f != null )
        function.setText( f.getName());
      else
        function.setText( "" );
      f = b.getShiftedFunction();
      if ( f != null )
        shifted.setText( f.getName());
      else
        shifted.setText( "" );
    }
    else
    {
      buttonName.setText( "" );
      function.setText( "" );
      shifted.setText( "" );
    }
  }

  public Button getButtonAtPoint( Point p )
  {
    Button rc = null;
    Button[] buttons = deviceUpgrade.getRemote().getButtons();
    for ( int i = 0; i < buttons.length; i++ )
    {
      Button b = buttons[ i ];
      Shape s = b.getShape();
      if (( s != null ) && s.contains( p ))
      {
        rc = b;
        break;
      }
    }
    return rc;
  }

// From interface ActionListener
  public void actionPerformed( ActionEvent e )
  {
    Object source = e.getSource();
    if ( currentButton != null )
    {
      Function function = (( FunctionItem )source ).getFunction();
      if (( e.getModifiers() & ActionEvent.SHIFT_MASK ) == 0 )
        currentButton.setFunction( function );
      else
        currentButton.setShiftedFunction( function );
      setButtonText( currentButton );
    }
  }

  class DoubleClickListener
    extends MouseAdapter
  {
    public void mouseClicked( MouseEvent e )
    {
      if (( currentButton == null ) || ( e.getClickCount() < 2 ))
        e.consume();
      else
      {
        FunctionLabel label = ( FunctionLabel )e.getSource();
        if (( e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK ) == 0 )
          currentButton.setFunction( label.getFunction());
        else
          currentButton.setShiftedFunction( label.getFunction());
        setButtonText( currentButton );
      }
    }
  }

  private Button currentButton = null;
  private Button buttonUnderMouse = null;
  private JPanel imagePanel = null;
  private JTextField buttonName = null;
  private JTextField function = null;
  private JTextField shifted = null;
  private JPopupMenu popup = null;
  private JPanel functionPanel = null;
  private DoubleClickListener doubleClickListener = new DoubleClickListener();
}
