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

import java.util.Arrays;

import nl.knokko.util.bits.BitInput;
import nl.knokko.util.bits.BitOutput;

public class IPsFile {
	
	private final byte hash;
	
	private IPData[] ips;

	public IPsFile(byte hash) {
		this.hash = hash;
		this.ips = new IPData[0];
	}
	
	public IPsFile(byte hash, BitInput input){
		this.hash = hash;
		this.ips = new IPData[input.readInt()];
		for(int index = 0; index < ips.length; index++)
			ips[index] = new IPData(input);
	}
	
	public synchronized void save(BitOutput output){
		output.addInt(ips.length);
		for(IPData ip : ips)
			ip.save(output);
	}
	
	public byte getHash(){
		return hash;
	}
	
	public synchronized IPData getIP(byte[] ip){
		for(IPData data : ips)
			if(Arrays.equals(ip, data.getIP()))
				return data;
		return null;
	}
	
	/**
	 * @param ip The IPData to add
	 * @return true if the IP was added, false if it was not added because it already existed
	 */
	public synchronized boolean addIP(IPData ip){
		if(getIP(ip.getIP()) != null) return false;
		IPData[] newIps = Arrays.copyOf(ips, ips.length + 1);
		newIps[ips.length] = ip;
		return true;
	}
}