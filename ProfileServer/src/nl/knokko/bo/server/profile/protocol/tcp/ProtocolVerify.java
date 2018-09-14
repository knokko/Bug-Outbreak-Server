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
package nl.knokko.bo.server.profile.protocol.tcp;

import nl.knokko.bo.server.profile.ProfileServer;
import nl.knokko.bo.server.profile.ProfileTCPClient;
import nl.knokko.bo.server.profile.ProfileTCPClient.State;
import nl.knokko.util.bits.BitInput;
import nl.knokko.util.bits.BitOutput;
import nl.knokko.util.hashing.HashResult;
import nl.knokko.util.hashing.ServerHasher;
import nl.knokko.util.protocol.BitProtocol;
import nl.knokko.util.socket.client.TCPClientSocket;
import nl.knokko.bo.server.protocol.AuthProfileCode.CtS;

/**
 *
 * @author 20182191
 */
public class ProtocolVerify implements BitProtocol<TCPClientSocket<ProfileTCPClient.State>>{

    @Override
    public void process(BitInput input, TCPClientSocket<ProfileTCPClient.State> handler) {
        if(handler.getState().getAuthState() == State.AUTH_STATE_START){
            System.out.println("The auth server asks me to verify myself");
            int[] tempHasher = input.readInts(9);
            HashResult toServer = ServerHasher.tempHash(ProfileServer.getDataManager().getAuthPassword(), tempHasher);
            BitOutput output = handler.createOutput();
            output.addNumber(CtS.VERIFY, CtS.BITCOUNT, false);
            output.addInts(toServer.get());
            output.addChar((char) ProfileServer.getWebPort());
            handler.getState().setAuthState(State.AUTH_STATE_WAITING);
            output.terminate();
        }
        else {
            handler.close("Received TCPCode.StC.VERIFY code at wrong moment");
        }
    }
}