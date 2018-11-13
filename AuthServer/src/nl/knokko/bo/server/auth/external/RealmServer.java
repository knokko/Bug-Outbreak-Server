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
package nl.knokko.bo.server.auth.external;

import nl.knokko.util.bits.BitInput;
import nl.knokko.util.bits.BitOutput;

public class RealmServer extends ExternalServer {

	private final String name;

	private final long createdAt;

	// doesn't need saving

	private int onlinePlayers;
	private int maxPlayers;
	private int totalPlayers;

	public RealmServer(BitInput input) {
		super(input);
		name = input.readJavaString();
		createdAt = input.readLong();
	}

	public RealmServer(String name, byte[] ip) {
		super(ip);
		this.name = name;
		this.createdAt = System.currentTimeMillis();
	}

	public void save(BitOutput output) {
		output.addJavaString(name);
		output.addLong(createdAt);
	}

	public String getName() {
		return name;
	}

	public long getCreatedAt() {
		return createdAt;
	}

	public int getOnlinePlayers() {
		return onlinePlayers;
	}

	public int getMaxPlayers() {
		return maxPlayers;
	}

	public int getTotalPlayers() {
		return totalPlayers;
	}

	public void updateState(int online, int max, int total) {
		onlinePlayers = online;
		maxPlayers = max;
		totalPlayers = total;
	}
}