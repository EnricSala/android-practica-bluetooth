package edu.upc.mcia.practicabluetoothmicros;

import junit.framework.TestCase;
import edu.upc.mcia.practicabluetoothmicros.command.BytesCommand;

public class BytesCommandTest extends TestCase {

	private final int[] testValues = new int[] { 0x55, 6, 34, 92, 179 };
	
	public void testIsComplete() {
		BytesCommand command = new BytesCommand(testValues.length + 1);
		for (int i = 0; i < testValues.length; i++) {
			command.addValue(testValues[i]);
		}
		assertEquals(false, command.isComplete());
		command.addValue(251);
		assertEquals(true, command.isComplete());
	}

	public void testAddValue() {
		BytesCommand command = new BytesCommand(testValues.length);
		for (int i = 0; i < testValues.length; i++) {
			command.addValue(testValues[i]);
		}
		assertEquals(true, command.isComplete());
	}

	public void testToByteArray() {
		BytesCommand command = new BytesCommand(testValues);
		assertEquals(true, command.isComplete());
		byte[] bytes = command.toByteArray();
		for (int i = 0; i < testValues.length; i++) {
			assertEquals(testValues[i], 0xFF & ((int) bytes[i]));
		}
	}
}
