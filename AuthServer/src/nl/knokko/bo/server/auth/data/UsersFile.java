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

public class UsersFile {

	private final byte hash;

	private UserData[] users;

	public UsersFile(byte hash) {
		this.hash = hash;
		this.users = new UserData[0];
	}

	public UsersFile(byte hash, BitInput input) {
		this.hash = hash;
		this.users = new UserData[input.readInt()];
		for (int index = 0; index < users.length; index++)
			users[index] = new UserData(input);
	}

	public synchronized void save(BitOutput output) {
		output.addInt(users.length);
		for (UserData user : users)
			user.save(output);
	}

	/**
	 * @return The common hash codes of all usernames in this file that are cast to
	 *         byte
	 */
	public byte getHash() {
		return hash;
	}

	public synchronized UserData getUser(String username) {
		for (UserData user : users)
			if (user.getUsername().equals(username))
				return user;
		return null;
	}

	/**
	 * Adds a user to the users of this UsersFile. This method makes sure that there
	 * is no user with the same name already, but doesn't provide any other checks
	 * 
	 * @param user
	 * @return True if the account was successfully created, false if there was
	 *         another account with the same name
	 */
	public synchronized boolean addUser(UserData user) {
		if (getUser(user.getUsername()) != null)
			return false;
		UserData[] newUsers = Arrays.copyOf(users, users.length + 1);
		newUsers[users.length] = user;
		users = newUsers;
		return true;
	}
}