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
package nl.knokko.bo.server.auth.protocol.tcp;

import java.util.Arrays;

import nl.knokko.bo.server.auth.AuthServer;
import nl.knokko.bo.server.auth.AuthTCPServer;
import nl.knokko.bo.server.auth.AuthTCPServer.State;
import nl.knokko.bo.server.protocol.AuthProfileCode.StC;
import nl.knokko.util.bits.BitInput;
import nl.knokko.util.bits.BitOutput;
import nl.knokko.util.protocol.BitProtocol;

public class ProtocolProfileStart implements BitProtocol<AuthTCPServer.Handler> {

	@Override
	public void process(BitInput input, AuthTCPServer.Handler handler) {
		if(handler.getState().getState() == State.STATE_DEFAULT){
                    System.out.println("The profile server would like to start");
			synchronized(AuthServer.getDataManager().getProfileLock()){
				if(AuthServer.getDataManager().getProfileServer() != null){
					if(Arrays.equals(AuthServer.getDataManager().getProfileServer().getIP(), handler.getClient().getInetAddress().getAddress())){
						if(!AuthServer.getDataManager().getProfileServer().isOnline()){
							int[] tempHasher = AuthServer.getRandom().nextInts(9);
							BitOutput output = handler.createOutput();
							output.addNumber(StC.VERIFY, StC.BITCOUNT, false);
							output.addInts(tempHasher);
                                                        handler.getState().setState(State.STATE_PROFILE_LOGGING_IN);
							handler.getState().setTempHasher(tempHasher);
							output.terminate();
						}
						else {
							denyStart(handler, StC.RefuseStart.ALREADY_STARTED);
						}
					}
					else {
						denyStart(handler, StC.RefuseStart.WRONG_IP);
					}
				}
				else {
					denyStart(handler, StC.RefuseStart.NO_PROFILE_SERVER);
				}
			}
		}
		else {
			handler.stop("Tried profile login from wrong state");
		}
	}
	
	static void denyStart(AuthTCPServer.Handler handler, byte reason){
		BitOutput output = handler.createOutput();
		output.addNumber(StC.DENY_START, StC.BITCOUNT, false);
		output.addNumber(reason, StC.RefuseStart.BITCOUNT, false);
		output.terminate();
	}
}