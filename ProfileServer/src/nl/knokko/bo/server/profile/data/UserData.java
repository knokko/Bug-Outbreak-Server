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
import nl.knokko.bo.server.game.model.entity.EntityModel;

public class UserData {
    
    public static final int MAX_MODELS = 10;
    
    static UserData load1(BitInput input){
        long id = input.readLong();
        ProfileEntityModel[] models = new ProfileEntityModel[input.readInt()];
        for(int index = 0; index < models.length; index++)
            models[index] = ProfileEntityModel.load1(input);
        return new UserData(id, models);
    }
    
    private final long id;
    
    private ProfileEntityModel[] models;
    
    //doesn't need saving
    
    private boolean isLoggedIn;
    
    UserData(long id){
        this.id = id;
        this.models = new ProfileEntityModel[0];
    }
    
    private UserData(long id, ProfileEntityModel[] models){
        this.id = id;
        this.models = models;
    }
    
    public long getID(){
        return id;
    }
    
    public synchronized void save1(BitOutput output){
        output.addLong(id);
        output.addInt(models.length);
        for(ProfileEntityModel model : models)
            model.save1(output);
    }
    
    public synchronized ProfileEntityModel getModel(String name){
        for(ProfileEntityModel model : models)
            if(model.getName().equals(name))
                return model;
        return null;
    }
    
    /**
     * Do NOT modify this array!
     * @return The current array where the profile models of this user are stored
     */
    public ProfileEntityModel[] getModels(){
        return models;
    }
    
    public int getModelAmount(){
        return models.length;
    }
    
    public synchronized boolean deleteModel(String name){
        for(int index = 0; index < models.length; index++){
            if(models[index].getName().equals(name)){
                ProfileEntityModel[] newModels = new ProfileEntityModel[models.length - 1];
                System.arraycopy(models, 0, newModels, 0, index);
                System.arraycopy(models, index + 1, newModels, index, newModels.length - index);
                models = newModels;
                return true;
            }
        }
        return false;
    }
    
    public synchronized boolean changeModel(String name, EntityModel newModel){
        for(ProfileEntityModel model : models){
            if(model.getName().equals(name)){
                model.setModel(newModel);
                return true;
            }
        }
        return false;
    }
    
    public synchronized void addModel(ProfileEntityModel model){
        ProfileEntityModel[] newModels = new ProfileEntityModel[models.length + 1];
        System.arraycopy(models, 0, newModels, 0, models.length);
        newModels[models.length] = model;
        models = newModels;
    }
    
    public boolean isLoggedIn(){
        return isLoggedIn;
    }
    
    public void setLoggedIn(){
        isLoggedIn = true;
    }
    
    public void setLoggedOut(){
        isLoggedIn = false;
    }
}