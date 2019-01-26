/*******************************************************************************
 * The MIT License
 *
 * Copyright (c) 2018 knokko
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
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
 *******************************************************************************/
package nl.knokko.bo.server.profile.data;

import nl.knokko.util.bits.BitInput;
import nl.knokko.util.bits.BitOutput;
import nl.knokko.bo.server.game.model.entity.EntityModel;
import nl.knokko.usermanager.UserData;

public class ProfileUserData extends UserData {

	public static final int MAX_MODELS = 10;
	
	private static final byte ENCODING_1 = 0;

	private ProfileEntityModel[] models;

	// Fields that do not need saving

	private boolean isLoggedIn;
	
	private final Object modelsLock;

	public ProfileUserData(long id) {
		super(id);
		this.models = new ProfileEntityModel[0];
		
		this.isLoggedIn = false;
		this.modelsLock = new Object();
	}
	
	@Override
	protected void saveData(BitOutput output) {
		output.addByte(ENCODING_1);
		save1(output);
	}
	
	@Override
	protected void loadData(BitInput input) {
		byte encoding = input.readByte();
		if (encoding == ENCODING_1) {
			load1(input);
		} else {
			throw new IllegalArgumentException("Unknown encoding: " + encoding);
		}
	}

	private void save1(BitOutput output) {
		
		// Prevent annoying concurrency problems
		ProfileEntityModel[] models = this.models;
		output.addInt(models.length);
		for (ProfileEntityModel model : models)
			model.save1(output);
	}
	
	private void load1(BitInput input) {
		models = new ProfileEntityModel[input.readInt()];
		for (int index = 0; index < models.length; index++)
			models[index] = ProfileEntityModel.load1(input);
	}

	public ProfileEntityModel getModel(String name) {
		ProfileEntityModel[] models = this.models;
		for (ProfileEntityModel model : models)
			if (model.getName().equals(name))
				return model;
		return null;
	}

	/**
	 * Do NOT modify this array!
	 * 
	 * @return The current array where the profile models of this user are stored
	 */
	public ProfileEntityModel[] getModels() {
		return models;
	}

	public int getModelAmount() {
		return models.length;
	}

	public boolean deleteModel(String name) {
		synchronized (modelsLock) {
			ProfileEntityModel[] models = this.models;
			for (int index = 0; index < models.length; index++) {
				if (models[index].getName().equals(name)) {
					ProfileEntityModel[] newModels = new ProfileEntityModel[models.length - 1];
					System.arraycopy(models, 0, newModels, 0, index);
					System.arraycopy(models, index + 1, newModels, index, newModels.length - index);
					models = newModels;
					return true;
				}
			}
			return false;
		}
	}

	public boolean changeModel(String name, EntityModel newModel) {
		for (ProfileEntityModel model : models) {
			if (model.getName().equals(name)) {
				model.setModel(newModel);
				return true;
			}
		}
		return false;
	}

	public void addModel(ProfileEntityModel model) {
		synchronized (modelsLock) {
			ProfileEntityModel[] models = this.models;
			ProfileEntityModel[] newModels = new ProfileEntityModel[models.length + 1];
			System.arraycopy(models, 0, newModels, 0, models.length);
			newModels[models.length] = model;
			this.models = newModels;
		}
	}

	public boolean isLoggedIn() {
		return isLoggedIn;
	}

	public void setLoggedIn() {
		isLoggedIn = true;
	}

	public void setLoggedOut() {
		isLoggedIn = false;
	}
}