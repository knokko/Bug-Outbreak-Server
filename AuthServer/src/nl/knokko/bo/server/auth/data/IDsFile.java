/*******************************************************************************
 * The MIT License
 *
 * Copyright (c) 2018 knokko
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *  
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *******************************************************************************/
package nl.knokko.bo.server.auth.data;

import java.util.Arrays;

import nl.knokko.util.bits.BitInput;
import nl.knokko.util.bits.BitOutput;

public class IDsFile {

	public static final int AMOUNT = 65536;

	private final int index;

	private String[] idMap;

	public IDsFile(int index) {
		this.index = index;
		idMap = new String[0];
	}

	public IDsFile(int index, BitInput input) {
		this.index = index;
		idMap = new String[input.readChar()];
		for (int id = 0; id < idMap.length; id++)
			idMap[id] = input.readJavaString();
	}

	public synchronized void save(BitOutput output) {
		output.addChar((char) idMap.length);
		for (String username : idMap)
			output.addJavaString(username);
	}

	public synchronized String getUsernameForID(long id) {
		long idIndex = id - index * AMOUNT;
		if (idIndex < 0 || idIndex >= AMOUNT)
			throw new IllegalArgumentException("id " + id + " for index " + index);
		if (idIndex >= idMap.length)
			return null;
		return idMap[(int) idIndex];
	}

	public synchronized void register(long id, String username) {
		long idIndex = id - index * AMOUNT;
		if (idIndex < 0 || idIndex >= AMOUNT)
			throw new IllegalArgumentException("id " + id + " for index " + index);
		if (idIndex >= idMap.length)
			idMap = Arrays.copyOf(idMap, (int) (idIndex + 1));
		idMap[(int) idIndex] = username;
	}

	public int getIndex() {
		return index;
	}
}