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

public enum TransactionType implements org.apache.thrift.TEnum {
  READ(0),
  WRITE(1);

  private final int value;

  private TransactionType(int value) {
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
  public static TransactionType findByValue(int value) { 
    switch (value) {
      case 0:
        return READ;
      case 1:
        return WRITE;
      default:
        return null;
    }
  }
}
