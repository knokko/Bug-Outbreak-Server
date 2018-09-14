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
package nl.knokko.bo.server.profile.protocol.web;

import nl.knokko.bo.server.game.model.entity.EntityModel;
import nl.knokko.bo.server.game.model.entity.InvalidModelException;
import nl.knokko.bo.server.profile.ProfileServer;
import nl.knokko.bo.server.profile.ProfileWebServer;
import nl.knokko.bo.server.profile.data.ProfileEntityModel;
import nl.knokko.bo.server.profile.data.UserData;
import nl.knokko.bo.server.profile.protocol.web.WebCode.StC;
import nl.knokko.util.bits.BitInput;
import nl.knokko.util.bits.BitOutput;
import nl.knokko.util.protocol.BitProtocol;

public class ProtocolChangeModel implements BitProtocol<ProfileWebServer.Handler> {

    @Override
    public void process(BitInput input, ProfileWebServer.Handler handler) {
        if(handler.getState().isLoggedIn()){
            UserData data = ProfileServer.getDataManager().getUserData(handler.getState().getUserID());
            synchronized(data){
                ProfileEntityModel model = data.getModel(input.readJavaString(100));
                if(model != null){
                    try {
                        model.setModel(new EntityModel(input));
                        BitOutput output = handler.createOutput();
                        output.addNumber(StC.CHANGED_MODEL, StC.BITCOUNT, false);
                        output.terminate();
                    } catch(InvalidModelException ime){
                        handler.uglyStop("Changed a model to make it invalid");
                    }
                }
                else {
                    handler.uglyStop("Tried to change a model that doesn't exist");
                }
            }
        }
        else {
            handler.uglyStop("Tried to change a model before logging in");
        }
    }
}