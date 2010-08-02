package com.hifiremote.jp1;

public interface ButtonEnabler
{
  public void enableButtons( Button baseButton );
  
  public boolean isAvailable( Button baseButton );
}
