package com.hifiremote.jp1;

public class ButtonAssignment
{
  public ButtonAssignment( Button button,
                          Function function,
                          Function shiftedFunction )
  {
    this.button = button;
    this.function = function;
    this.shiftedFunction = function;
  }

  public ButtonAssignment( Button button )
  {
    this.button = button;
  }

  public Button getButton(){ return button; }
  public ButtonAssignment setFunction( Function function )
  {
    this.function = function;
    return this;
  }
  public Function getFunction(){ return function; }
  public ButtonAssignment setShiftedFunction( Function function )
  {
    this.shiftedFunction = function;
    return this;
  }
  public Function getShiftedFunction(){ return shiftedFunction; }

  private Button button = null;
  private Function function = null;
  private Function shiftedFunction = null;
}
