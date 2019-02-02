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
package nl.knokko.bo.server.game.model.entity;

import nl.knokko.util.bits.BitInput;
import nl.knokko.util.bits.BitOutput;

public class RawEntityModel {

	/**
	 * Amount of vertices is stored as int. Positions are stored in an int array.
	 * TextureCoords are stored in a char array. Matrices are stored in a byte array.
	 */
	private static final byte ENCODING_IICB = -128;

	public static final int MIN_VERTICES = 40;
	public static final int MAX_VERTICES = 1000;// TODO change those properties after some testing
	public static final int MIN_TRIANGLES = 20;
	public static final int MAX_TRIANGLES = 1000;

	private int[] positions;
	private char[] textureCoords;
	private byte[] matrices;

	private char[] indices;

	public RawEntityModel(BitInput input, EntitySkeleton skeleton, EntityTexture texture) throws InvalidModelException {
		byte encoding = input.readByte();
		if (encoding == ENCODING_IICB) {
			loadIICB(input, skeleton, texture);
		} else {
			throw new InvalidModelException("Invalid model encoding");
		}
	}

	private void loadIICB(BitInput input, EntitySkeleton skeleton, EntityTexture texture) throws InvalidModelException {
		int vertices = input.readInt();
		if (vertices < MIN_VERTICES)
			throw new InvalidModelException("Too few vertices (" + vertices + ")");
		if (vertices > MAX_VERTICES)
			throw new InvalidModelException("Too many vertices (" + vertices + ")");
		input.increaseCapacity(vertices * (3 * 4 + 2 * 2 + 1 * 1));
		positions = new int[vertices * 3];
		textureCoords = new char[vertices * 2];
		matrices = new byte[vertices];
		for (int index = 0; index < positions.length; index++)
			positions[index] = input.readDirectInt();
		int textureIndex = 0;
		for (int index = 0; index < vertices; index++) {
			char x = input.readDirectChar();
			if (x >= texture.getWidth())
				throw new InvalidModelException("Invalid texture X: " + x);
			char y = input.readDirectChar();
			if (y >= texture.getHeight())
				throw new InvalidModelException("Invalid texture Y: " + y);
			textureCoords[textureIndex++] = x;
			textureCoords[textureIndex++] = y;
		}
		for (int index = 0; index < vertices; index++) {
			byte matrix = input.readDirectByte();
			if (matrix < 0)
				throw new InvalidModelException("Invalid matrix: " + matrix);
			if (matrix >= skeleton.getAmount())
				throw new InvalidModelException("Invalid matrix: " + matrix);
			matrices[index] = matrix;
		}
		int triangles = input.readInt();
		if (triangles < MIN_TRIANGLES)
			throw new InvalidModelException("Too few triangles (" + triangles + ")");
		if (triangles > MAX_TRIANGLES)
			throw new InvalidModelException("Too many triangles (" + triangles + ")");
		indices = new char[triangles * 3];
		input.readChars(indices);
	}

	public void save(BitOutput output) {
		saveIICB(output);
	}

	private void saveIICB(BitOutput output) {
		output.ensureExtraCapacity(8 + 32 + matrices.length * (3 * 4 + 2 * 2 + 1 * 1));
		output.addDirectByte(ENCODING_IICB);
		output.addDirectInt(matrices.length);
		for (int pos : positions)
			output.addDirectInt(pos);
		for (char coord : textureCoords)
			output.addDirectChar(coord);
		for (byte matrix : matrices)
			output.addDirectByte(matrix);
	}
}