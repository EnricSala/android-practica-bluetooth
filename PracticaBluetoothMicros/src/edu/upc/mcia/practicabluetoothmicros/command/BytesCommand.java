package edu.upc.mcia.practicabluetoothmicros.command;

import java.util.Arrays;

public class BytesCommand {

	public byte[] array;

	@Override
	public String toString() {
		return "[array=" + Arrays.toString(array) + "]";
	}

}
