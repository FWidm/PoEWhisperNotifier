package de.poe.notifier.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;

public class Utility {
	/**
	 * Returns the last line from a given text file. This method is particularly
	 * well suited for very large text files that contain millions of text lines
	 * since it will just seek the end of the text file and seek the last line
	 * indicator. Please use only for large sized text files.
	 * 
	 * @param file
	 *            A file on disk
	 * @return The last line if available or an empty string if nothing was
	 *         found
	 */
	public static String getLastLineFast(final File file) {

		// file needs to exist
		if (file.exists() == false || file.isDirectory()) {
			return "";
		}

		// avoid empty files
		if (file.length() <= 2) {
			return "";
		}

		// open the file for read-only mode
		try (RandomAccessFile fileAccess = new RandomAccessFile(file, "r")) {
			char breakLine = '\n';
			// offset of the current filesystem block - start with the last one
			long blockStart = (file.length() - 1) / 4096 * 4096;
			// hold the current block
			byte[] currentBlock = new byte[(int) (file.length() - blockStart)];
			// later (previously read) blocks
			List<byte[]> laterBlocks = new ArrayList<byte[]>();

			while (blockStart >= 0) {

				fileAccess.seek(blockStart);
				fileAccess.readFully(currentBlock);
				// ignore the last 2 bytes of the block if it is the first one
				int lengthToScan = currentBlock.length - (laterBlocks.isEmpty() ? 2 : 0);
				for (int i = lengthToScan - 1; i >= 0; i--) {
					if (currentBlock[i] == breakLine) {
						// we found our end of line!
						StringBuilder result = new StringBuilder();
						// RandomAccessFile#readLine uses ISO-8859-1, therefore
						// we do here too EDIT: I DO NOT - this would display
						// some UTF8 Chars wrong, might make problems tho.
						result.append(new String(currentBlock, i + 1, currentBlock.length - (i + 1), "UTF-8"));
						for (byte[] laterBlock : laterBlocks) {
							result.append(new String(laterBlock, "UTF-8"));
						}
						// maybe we had a newline at end of file? Strip it.
						if (result.charAt(result.length() - 1) == breakLine) {
							// newline can be \r\n or \n, so check which one to
							// strip
							int newlineLength = result.charAt(result.length() - 2) == '\r' ? 2 : 1;
							result.setLength(result.length() - newlineLength);
						}
						return result.toString();
					}
				}
				// no end of line found - we need to read more
				laterBlocks.add(0, currentBlock);
				blockStart -= 4096;
				currentBlock = new byte[4096];
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		// oops, no line break found or some exception happened
		return "";
	}

	public static String tail2(File file, int lines) {
		java.io.RandomAccessFile fileHandler = null;
		try {
			fileHandler = new java.io.RandomAccessFile(file, "r");
			long fileLength = fileHandler.length() - 1;
			StringBuilder sb = new StringBuilder();
			int line = 0;

			for (long filePointer = fileLength; filePointer != -1; filePointer--) {
				fileHandler.seek(filePointer);
				int readByte = fileHandler.readByte();

				if (readByte == 0xA) {
					if (filePointer < fileLength) {
						line = line + 1;
					}
				} else if (readByte == 0xD) {
					if (filePointer < fileLength - 1) {
						line = line + 1;
					}
				}
				if (line >= lines) {
					break;
				}
				sb.append((char) readByte);
			}

			String lastLine = sb.reverse().toString();
			return lastLine;
		} catch (java.io.FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (java.io.IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (fileHandler != null)
				try {
					fileHandler.close();
				} catch (IOException e) {
				}
		}
	}

	/**
	 * Returns the current window title with a given maximum length, uses JNA to
	 * get the information.
	 * 
	 * @param length
	 * @return
	 * @throws Exception
	 */
	public static String returnWindowTitle(int length) throws Exception {
		char[] buffer = new char[length * 2];
		HWND hwnd = User32.INSTANCE.GetForegroundWindow();

		User32.INSTANCE.GetWindowText(hwnd, buffer, length);
		char[] className = new char[length*2];
		User32.INSTANCE.GetClassName(hwnd,className,0);
		return Native.toString(buffer);
	}

	// http://stackoverflow.com/a/19005828/3764804

	/**
	 * Checks whether a specific process can be found inside the windows task list.
	 * @param processName
	 * @return true if process is found, else false
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static boolean isProcessRunning(String processName) throws IOException, InterruptedException
	{
		ProcessBuilder processBuilder = new ProcessBuilder("tasklist.exe");
		Process process = processBuilder.start();
		String tasksList = toString(process.getInputStream());

		return tasksList.contains(processName);
	}

	// http://stackoverflow.com/a/5445161/3764804

	/**
	 * Takes an inputstream and converts it's content to one string.
	 * @param inputStream
	 * @return string representation of the input stream
	 */
	private static String toString(InputStream inputStream)
	{
		Scanner scanner = new Scanner(inputStream, "UTF-8").useDelimiter("\\A");
		String string = scanner.hasNext() ? scanner.next() : "";
		scanner.close();

		return string;
	}
}
