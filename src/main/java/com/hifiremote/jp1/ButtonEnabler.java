package com.hifiremote.jp1;

public interface ButtonEnabler
{
  public void enableButtons( Button baseButton, MacroDefinitionBox box );
  
  public boolean isAvailable( Button baseButton );
}
