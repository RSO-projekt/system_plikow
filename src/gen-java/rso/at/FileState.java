/**
 * Autogenerated by Thrift Compiler (0.9.1)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package rso.at;


import java.util.Map;
import java.util.HashMap;
import org.apache.thrift.TEnum;

public enum FileState implements org.apache.thrift.TEnum {
  IDLE(0),
  READ(1),
  PREMODIFIED(2),
  MODIFIED(3);

  private final int value;

  private FileState(int value) {
    this.value = value;
  }

  /**
   * Get the integer value of this enum value, as defined in the Thrift IDL.
   */
  public int getValue() {
    return value;
  }

  /**
   * Find a the enum type by its integer value, as defined in the Thrift IDL.
   * @return null if the value is not found.
   */
  public static FileState findByValue(int value) { 
    switch (value) {
      case 0:
        return IDLE;
      case 1:
        return READ;
      case 2:
        return PREMODIFIED;
      case 3:
        return MODIFIED;
      default:
        return null;
    }
  }
}
