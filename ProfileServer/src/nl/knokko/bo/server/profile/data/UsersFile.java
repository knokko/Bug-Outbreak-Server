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
package nl.knokko.bo.server.profile.data;

import nl.knokko.util.bits.BitInput;
import nl.knokko.util.bits.BitOutput;

public class UsersFile {
    
    private static final byte ENCODING_1 = 0;
    
    private UserData[] users;
    
    UsersFile(){
        users = new UserData[0];
    }
    
    UsersFile(BitInput input){
        byte encoding = input.readByte();
        if(encoding == ENCODING_1){
            load1(input);
        }
        else {
            throw new IllegalArgumentException("Unknown encoding: " + encoding);
        }
    }

    private void load1(BitInput input){
        users = new UserData[input.readInt()];
        for(int index = 0; index < users.length; index++){
            users[index] = UserData.load1(input);
        }
    }
    
    private void save1(BitOutput output){
        output.addInt(users.length);
        for(UserData data : users)
            data.save1(output);
    }
    
    public synchronized void save(BitOutput output){
        output.addByte(ENCODING_1);
        save1(output);
    }
    
    public synchronized UserData getUser(long id){
        for(int index = 0; index < users.length; index++)
            if(users[index].getID() == id)
                return users[index];
        UserData user = new UserData(id);
        addUser(user);
        return user;
    }
    
    public synchronized void addUser(UserData user){
        UserData[] newUsers = new UserData[users.length + 1];
        System.arraycopy(users, 0, newUsers, 0, users.length);
    }
}