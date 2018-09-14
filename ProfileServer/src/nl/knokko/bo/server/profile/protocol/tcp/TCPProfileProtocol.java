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

import nl.knokko.util.protocol.DomainBitProtocol;
import nl.knokko.bo.server.profile.ProfileTCPClient;
import nl.knokko.bo.server.protocol.AuthProfileCode.StC;
import nl.knokko.util.socket.client.TCPClientSocket;

/**
 *
 * @author 20182191
 */
public class TCPProfileProtocol extends DomainBitProtocol<TCPClientSocket<ProfileTCPClient.State>> {
    
    public TCPProfileProtocol(){
        super(StC.AMOUNT, StC.BITCOUNT);
        register(StC.VERIFY, new ProtocolVerify());
        register(StC.APPROVE_START, new ProtocolApproveStart());
        register(StC.DENY_START, new ProtocolDenyStart());
        register(StC.ALLOW_LOGIN, new ProtocolAllowLogin());
    }
}