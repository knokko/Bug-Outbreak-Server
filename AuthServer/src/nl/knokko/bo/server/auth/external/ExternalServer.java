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
package nl.knokko.bo.server.auth.external;

import nl.knokko.util.bits.BitInput;
import nl.knokko.util.bits.BitOutput;

public class ExternalServer {

	private byte[] ip;
	private int port;
	
	private int[] password;

	public ExternalServer(byte[] ip) {
		this.ip = ip;
		this.port = -1;
	}
	
	public ExternalServer(BitInput input){
		ip = input.readByteArray();
                this.port = -1;
	}
	
	public void save(BitOutput output){
		output.addByteArray(ip);
	}
	
	public void savePassword(BitOutput output){
		output.addIntArray(password);
	}
	
	public void loadPassword(BitInput input){
		password = input.readIntArray();
	}
	
	public void setOnline(int port){
		this.port = port;
	}
	
	public void setOffline(){
		port = -1;
	}
	
	public byte[] getIP(){
		return ip;
	}
	
	public void setIP(byte[] ip){
		this.ip = ip;
	}
	
	public int getPort(){
		return port;
	}
	
	public boolean isOnline(){
		return port != -1;
	}
	
	public int[] getPassword(){
		return password;
	}
}
