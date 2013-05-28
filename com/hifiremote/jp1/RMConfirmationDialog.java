package com.hifiremote.jp1;

import javax.swing.JOptionPane;

public class RMConfirmationDialog
{

  public static boolean show(String title, String message, int trueValue )
  {
    return show( title, message, trueValue, null, false, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE );
  }

  public static boolean show(String title, String message, int trueValue, String suppressProperty )
  {
    return show( title, message, trueValue, suppressProperty, false, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE );
  }

  public static boolean show(String title, String message, int trueValue, String suppressProperty, boolean suppressDefault, int optionType, int messageType )
  {
    // first check if this one is being suppressed by user option
    if ( suppressProperty != null && Boolean.parseBoolean( JP1Frame.getProperties().getProperty( suppressProperty, Boolean.toString( suppressDefault ) ) ) )
      return true;

    // show the prompt and return
    return ( JOptionPane.showConfirmDialog( null, message, title, optionType, messageType ) == trueValue );
  }
}
