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

import nl.knokko.bo.server.game.model.entity.EntityModel;
import nl.knokko.bo.server.game.model.entity.InvalidModelException;
import nl.knokko.util.bits.BitInput;
import nl.knokko.util.bits.BitOutput;

public class ProfileEntityModel {

	static ProfileEntityModel load1(BitInput input) {
		try {
			return new ProfileEntityModel(new EntityModel(input), input.readJavaString(), input.readLong(),
					input.readLong());
		} catch (InvalidModelException ime) {
			throw new IllegalArgumentException(ime);
		}
	}

	private EntityModel model;

	private String name;

	private final long createdAt;
	private long lastModified;

	public ProfileEntityModel(EntityModel model, String name, long createdAt, long lastModified) {
		this.model = model;
		this.name = name;
		this.createdAt = createdAt;
		this.lastModified = lastModified;
	}

	public void save1(BitOutput output) {
		model.save(output);
		output.addJavaString(name);
		output.addLong(createdAt);
		output.addLong(lastModified);
	}

	public EntityModel getModel() {
		return model;
	}

	public String getName() {
		return name;
	}

	public long getCreatedAt() {
		return createdAt;
	}

	public long getLastModified() {
		return lastModified;
	}

	public void setModel(EntityModel newModel) {
		model = newModel;
		lastModified = System.currentTimeMillis();
	}

	public void setName(String newName) {
		name = newName;
	}
}