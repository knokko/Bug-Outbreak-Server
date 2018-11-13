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

public class IPData {

	private final byte[] ip;

	private byte createdAccounts;

	// fields that don't need saving

	private int failedAttempts;

	public IPData(byte[] ip) {
		this.ip = ip;
	}

	public IPData(BitInput input) {
		this.ip = input.readByteArray();
		createdAccounts = input.readByte();
	}

	public synchronized void save(BitOutput output) {
		output.addByteArray(ip);
		output.addByte(createdAccounts);
	}

	public byte[] getIP() {
		return Arrays.copyOf(ip, ip.length);
	}

	/**
	 * @return The amount of failed login attempts for today
	 */
	public synchronized int getFailedLoginAttempts() {
		return failedAttempts;
	}

	public synchronized void increaseFailedAttempts() {
		if (failedAttempts < Integer.MAX_VALUE)// overflow won't help attackers
			failedAttempts++;
	}

	public synchronized byte getCreatedAccounts() {
		return createdAccounts;
	}

	public synchronized void increaseCreatedAccounts() {
		if (createdAccounts < Byte.MAX_VALUE)// just to be sure
			createdAccounts++;
	}
}