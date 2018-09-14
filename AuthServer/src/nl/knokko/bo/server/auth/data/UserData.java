/* 
 * The MIT License
 *
 * Copyright 2018 20182191.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
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
 */
package nl.knokko.bo.server.auth.data;

import java.net.InetAddress;

import nl.knokko.util.bits.BitInput;
import nl.knokko.util.bits.BitOutput;
import nl.knokko.util.hashing.HashResult;

public class UserData {
	
	private String username;
	private String salt;
	
	private int[] encryptedHash;
	
	private long id;
	private final byte[] creationIP;
	private boolean op;
	
	//fields that don't need saving
	
	private boolean loggedIn;
	private int failedAttempts;
	
	public UserData(BitInput input){
		username = input.readJavaString();
		salt = input.readJavaString();
		encryptedHash = input.readInts(20);
		creationIP = input.readByteArray();
		id = input.readLong();
		op = input.readBoolean();
	}

	public UserData(String username, String salt, HashResult encryptedHashResult, InetAddress address) {
		this.username = username;
		this.salt = salt;
		this.encryptedHash = encryptedHashResult.get();
		this.creationIP = address.getAddress();
		this.id = -1;
	}
	
	public synchronized void save(BitOutput output){
		output.addJavaString(username);
		output.addJavaString(salt);
		output.addInts(encryptedHash);
		output.addByteArray(creationIP);
		if(id == -1) throw new IllegalStateException("The account " + username + " doesn't have an id!");
		output.addLong(id);
		output.addBoolean(op);
	}
	
	public synchronized void assignID(long id){
		if(this.id != -1) throw new IllegalStateException("Can't change the id of " + username + " because it already has an id");
		this.id = id;
	}
	
	public synchronized long getID(){
		return id;
	}
	
	public synchronized String getUsername(){
		return username;
	}
	
	public synchronized String getSalt(){
		return salt;
	}
	
	public synchronized HashResult getEncryptedHash(){
		return new HashResult(encryptedHash);
	}
	
	/**
	 * @return The IP address where the account was created
	 */
	public byte[] getCreationIP(){
		return creationIP;
	}
	
	public synchronized boolean isLoggedIn(){
		return loggedIn;
	}
	
	/**
	 * Mark this UserData as logged in.
	 * @return true if the user was not yet logged in or false if the user was already logged in
	 */
	public synchronized void setLoggedIn(){
		loggedIn = true;
	}
	
	public synchronized void setLoggedOut(){
		loggedIn = false;
	}
	
	/**
	 * @return The amount of failed login attempts that happened today
	 */
	public synchronized int getFailedAttempts(){
		return failedAttempts;
	}
	
	public synchronized void increaseFailedAttempts(){
		if(failedAttempts < Integer.MAX_VALUE)//Failing too often won't help attackers
			failedAttempts++;
	}
	
	public synchronized boolean isOP(){
		return op;
	}
	
	public synchronized void setOP(boolean newOP){
		op = newOP;
	}
}