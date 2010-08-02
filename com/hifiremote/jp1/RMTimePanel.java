package com.hifiremote.jp1;

import java.awt.event.FocusEvent;
import java.text.ParseException;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.text.DateFormatter;

public class RMTimePanel extends JPanel implements RMSetter< RMTime >
{
  public RMTimePanel()
  {
    super();
    timeField = new TimeField( new DateFormatter( RMTime.timeFormat ) );
    add( timeField );
    setBorder( BorderFactory.createTitledBorder( "Time Setting" ) );
  }
  
  @Override
  public void setValue( RMTime time )
  {
    timeField.setValue( time.get() );
  }
  
  @Override
  public RMTime getValue()
  {
    if ( ! timeField.isEditValid() )
    {
      showWarning( "The time you have specified is not valid.  It should be\n" + 
          "a 24-hour clock value of the form \"hh:mm\"." );
      return null;
    }
    try
    {
      timeField.commitEdit();
    }
    catch ( ParseException e )
    {
      e.printStackTrace();
    }
    return new RMTime( (Date)timeField.getValue() );
  }
  
  private void showWarning( String message )
  {
    JOptionPane.showMessageDialog( this, message, "Invalid Format", JOptionPane.ERROR_MESSAGE );
  }
  
  public class TimeField extends JFormattedTextField
  {
    public TimeField(DateFormatter df)
    {
      super(df);
      setColumns( 6 );
      setFocusLostBehavior( JFormattedTextField.PERSIST );
    }
    protected void processFocusEvent(FocusEvent e) 
    {
      super.processFocusEvent(e);
      if ( e.getID() == FocusEvent.FOCUS_GAINED )
      {  
        selectAll();
      }  
    }
  }
  
  private JFormattedTextField timeField = null;

}
