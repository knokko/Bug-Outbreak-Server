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
package nl.knokko.bo.server.game.model.entity;

import nl.knokko.util.bits.BitInput;
import nl.knokko.util.bits.BitOutput;

public class EntityTexture {
	
	private static final byte ENCODING_CCB_RGB = -128;
	
	public static final int MIN_SIZE = 1;
	public static final int MAX_SIZE = 1000000;
	
	private byte[] data;
	
	private char width;
	private char height;

	public EntityTexture(BitInput input) throws InvalidModelException {
		byte encoding = input.readByte();
		if(encoding == ENCODING_CCB_RGB){
			loadCCB_RGB(input);
		}
		else {
			throw new InvalidModelException("Invalid texture encoding");
		}
	}
	
	private void loadCCB_RGB(BitInput input) throws InvalidModelException {
		width = input.readChar();
		height = input.readChar();
		int size = width * height;
		if(size < MIN_SIZE) throw new InvalidModelException("Texture too small (" + width + " x " + height + ")");
		if(size > MAX_SIZE) throw new InvalidModelException("Texture too large (" + width + " x " + height + ")");
		data = new byte[size * 3];
		input.readBytes(data);
	}
	
	public void save(BitOutput output){
		saveCCB_RGB(output);
	}
	
	private void saveCCB_RGB(BitOutput output){
		output.addChar(width);
		output.addChar(height);
		output.addBytes(data);
	}
	
	public int getWidth(){
		return width;
	}
	
	public int getHeight(){
		return height;
	}
}