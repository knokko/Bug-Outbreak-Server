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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import nl.knokko.util.bits.BitInput;
import nl.knokko.util.bits.BitOutput;
import nl.knokko.util.bits.BitInputStream;
import nl.knokko.util.bits.BitOutputStream;

import nl.knokko.bo.server.profile.ProfileServer;

public class ProfileDataManager {
    
    private final File baseDirectory;
    private final File userDirectory;
    private final File passwordDirectory;
    
    private final UsersFile[] loadedFiles;
    
    private int[] authPassword;
    
    private boolean shouldSave;
    
    public ProfileDataManager(){
        baseDirectory = new File("profile");
        userDirectory = new File(baseDirectory + "/users");
        passwordDirectory = new File(baseDirectory + "/passwords");
        baseDirectory.mkdirs();
        userDirectory.mkdir();
        passwordDirectory.mkdir();
        loadedFiles = new UsersFile[256];
        shouldSave = true;
    }
    
    public synchronized void save(){
        if(!shouldSave){
            ProfileServer.getConsole().println("Saving has been skipped");
            return;
        }
        userDirectory.mkdirs();
        for(int index = 0; index < loadedFiles.length; index++){
            try {
                if(loadedFiles[index] != null){
                    BitOutput output = new BitOutputStream(new FileOutputStream(getUsersFile(index)));
                    loadedFiles[index].save(output);
                    output.terminate();
                }
            } catch(IOException ioex){
                ProfileServer.getConsole().println("An IO error occured while saving users with index " + index + ": " + ioex.getMessage());
            }
        }
        passwordDirectory.mkdirs();
        try {
            BitOutput output = new BitOutputStream(new FileOutputStream(getAuthPasswordFile()));
            output.addIntArray(authPassword);
            output.terminate();
        } catch(IOException ioex){
            ProfileServer.getConsole().println("An IO error occured while saving the password for the auth server: " + ioex.getMessage());
        }
    }
    
    public synchronized void load(){
        for(int index = 0; index < loadedFiles.length; index++){
            File file = getUsersFile(index);
            if(file.exists()){
                try {
                    BitInput input = new BitInputStream(new FileInputStream(file));
                    loadedFiles[index] = new UsersFile(input);
                    input.terminate();
                } catch(IOException ioex){
                    ProfileServer.getConsole().println("An IO error occured while loading users with index " + index + ": " + ioex.getMessage());
                }
            }
        }
        try {
            BitInput input = new BitInputStream(new FileInputStream(getAuthPasswordFile()));
            authPassword = input.readIntArray();
            input.terminate();
        } catch(IOException ioex){
            ProfileServer.getConsole().println("An IO error occured while loading the password for the auth server: " + ioex.getMessage());
            ProfileServer.getConsole().println("Copy the password from the auth server to the profile server and start the profile server again.");
            try {
                ProfileServer.getConsole().println("IP should be " + InetAddress.getLocalHost().getHostAddress());
            } catch (UnknownHostException ex) {
                ProfileServer.getConsole().println("The host address of the server socket is unknown");
            }
            shouldSave = false;
            ProfileServer.stop();
            return;
        }
    }
    
    public UserData getUserData(long id){
        return getUsersData((byte) id).getUser(id);
    }
    
    private File getUsersFile(int index){
        return new File(userDirectory + "/" + index + ".users");
    }
    
    private File getAuthPasswordFile(){
        return new File(passwordDirectory + "/auth.pw");
    }
    
    private synchronized UsersFile getUsersData(byte hash){
        int index = hash & 0xFF;
        UsersFile uf = loadedFiles[index];
        if(uf != null) return uf;
        File file = getUsersFile(index);
        try {
            BitInput input = new BitInputStream(new FileInputStream(file));
            uf = new UsersFile(input);
            input.terminate();
            loadedFiles[index] = uf;
            return uf;
        } catch(IOException ioex){
            ProfileServer.getConsole().println("Creating new data for users with index " + index);
            uf = new UsersFile();
            loadedFiles[index] = uf;
            return uf;
        }
    }
    
    public int[] getAuthPassword(){
        return authPassword;
    }
}