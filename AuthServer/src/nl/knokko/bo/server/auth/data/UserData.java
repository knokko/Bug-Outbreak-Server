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

import java.net.InetAddress;

import nl.knokko.util.bits.BitInput;
import nl.knokko.util.bits.BitOutput;

public class UserData {

	private String username;
	private final String salt;

	private final byte[] testPayload;
	private final int[] serverStartSeed;
	private final int[] clientSessionSeed;
	private final int[] serverSessionSeed;

	private long id;
	private final byte[] creationIP;
	private boolean op;

	// fields that don't need saving

	private boolean loggedIn;
	private int failedAttempts;

	public UserData(BitInput input) {
		username = input.readString();
		salt = input.readString();
		testPayload = input.readBytes(300);
		serverStartSeed = input.readInts(24);
		clientSessionSeed = input.readInts(24);
		serverSessionSeed = input.readInts(24);

		creationIP = input.readByteArray();
		id = input.readLong();
		op = input.readBoolean();
	}

	public UserData(String username, String salt, byte[] testPayload, int[] serverStartSeed, int[] clientSessionSeed,
			int[] serverSessionSeed, InetAddress address) {
		this.username = username;
		this.salt = salt;
		this.testPayload = testPayload;
		this.serverStartSeed = serverStartSeed;
		this.clientSessionSeed = clientSessionSeed;
		this.serverSessionSeed = serverSessionSeed;

		this.creationIP = address.getAddress();
		this.id = -1;
	}

	public void save(BitOutput output) {
		output.addString(username);
		output.addString(salt);
		output.addBytes(testPayload);
		output.addInts(serverStartSeed);
		output.addInts(clientSessionSeed);
		output.addInts(serverSessionSeed);
		output.addByteArray(creationIP);
		if (id == -1)
			throw new IllegalStateException("The account " + username + " doesn't have an id!");
		output.addLong(id);
		output.addBoolean(op);
	}

	public void assignID(long id) {
		if (this.id != -1)
			throw new IllegalStateException("Can't change the id of " + username + " because it already has an id");
		this.id = id;
	}

	public long getID() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public String getSalt() {
		return salt;
	}

	public byte[] getTestPayload() {
		return testPayload;
	}
	
	public int[] getServerStartSeed() {
		return serverStartSeed;
	}
	
	public int[] getClientSessionSeed() {
		return clientSessionSeed;
	}
	
	public int[] getServerSessionSeed() {
		return serverSessionSeed;
	}

	/**
	 * @return The IP address where the account was created
	 */
	public byte[] getCreationIP() {
		return creationIP;
	}

	public boolean isLoggedIn() {
		return loggedIn;
	}

	/**
	 * Mark this UserData as logged in.
	 * 
	 * @return true if the user was not yet logged in or false if the user was
	 *         already logged in
	 */
	public void setLoggedIn() {
		loggedIn = true;
	}

	public void setLoggedOut() {
		loggedIn = false;
	}

	/**
	 * @return The amount of failed login attempts that happened today
	 */
	public int getFailedAttempts() {
		return failedAttempts;
	}

	public void increaseFailedAttempts() {
		if (failedAttempts < Integer.MAX_VALUE)// Failing too often won't help attackers
			failedAttempts++;
	}

	public boolean isOP() {
		return op;
	}

	public void setOP(boolean newOP) {
		op = newOP;
	}
}