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

import nl.knokko.util.bits.BitInput;
import nl.knokko.util.protocol.BitProtocol;
import nl.knokko.util.socket.client.TCPClientSocket;
import nl.knokko.bo.server.profile.ProfileTCPClient;
import nl.knokko.bo.server.profile.ProfileTCPClient.State;
import nl.knokko.bo.server.protocol.AuthProfileCode.StC.RefuseStart;

/**
 *
 * @author 20182191
 */
public class ProtocolDenyStart implements BitProtocol<TCPClientSocket<ProfileTCPClient.State>> {

    @Override
    public void process(BitInput input, TCPClientSocket<ProfileTCPClient.State> handler) {
        if(handler.getState().getAuthState() == State.AUTH_STATE_WAITING || handler.getState().getAuthState() == State.AUTH_STATE_START){
            System.out.println("The auth server refused my start");
            long reason = input.readNumber(RefuseStart.BITCOUNT, false);
            String string;
            if(reason == RefuseStart.WRONG_PASSWORD){
                string = "password for auth server is wrong";
            }
            else if(reason == RefuseStart.ALREADY_STARTED){
                string = "there is already an active profile server registered at the auth server";
            }
            else if(reason == RefuseStart.WRONG_IP){
                string = "the auth server expects us from another ip address";
            }
            else if(reason == RefuseStart.NO_PROFILE_SERVER){
                string = "the auth server doesn't have a profile server yet";
            }
            else {
                string = "unknown reason";
            }
            handler.close("TCP connection with auth server has been refused because " + string);
        }
        else {
            handler.close("The auth server sent TCPCode.StC.DENY_START at the wrong moment");
        }
    }
}