import java.io.File;
import java.util.ArrayList;

public class test_byteArray {

	final static String test_string = "fassial";
	final static int test_int = 0xFFFFFFFF;
	final static short test_short = (short) 0xFFFF;

	public static void main(String[] args) {
		byte[] test_stringByteArray, test_intByteArray, test_shortByteArray;

		System.out.println("test string: ");
		test_stringByteArray = virtualDisk.string2ByteArray(test_byteArray.test_string);
		System.out.printf("the length of test_string: %d\nthe length of test_stringByteArray: %d\nthe length of test_string_convert: %d\n", test_string.length(), test_stringByteArray.length, virtualDisk.byteArray2String(test_stringByteArray).length());

		System.out.println("test int: ");
		test_intByteArray = virtualDisk.int2ByteArray(test_byteArray.test_int);
		System.out.printf("the value of test_int: %x\nthe value of test_intByteArray: %x %x %x %x\nthe value of test_int_convert: %x\n", test_int, test_intByteArray[3], test_intByteArray[2], test_intByteArray[1], test_intByteArray[0], virtualDisk.byteArray2Int(test_intByteArray));

		System.out.println("test short: ");
		test_shortByteArray = virtualDisk.short2ByteArray(test_byteArray.test_short);
		System.out.printf("the value of test_short: %x\nthe value of test_shortByteArray: %x %x\nthe value of test_short_convert: %x\n", test_short, test_intByteArray[1], test_intByteArray[0], virtualDisk.byteArray2Short(test_shortByteArray));

		System.out.println("test unsigned short: ");
		System.out.printf("the value of test_short: %x\nthe value of test_short2Int: %x\nthe value of test_short_convert: %x\n", test_byteArray.test_short, virtualDisk.unsignedShort2Int(test_byteArray.test_short), virtualDisk.int2UnsignedShort(virtualDisk.unsignedShort2Int(test_byteArray.test_short)));
	}
}

