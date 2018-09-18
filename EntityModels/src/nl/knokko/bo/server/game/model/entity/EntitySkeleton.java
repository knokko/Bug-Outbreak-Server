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

public class EntitySkeleton {
	
	private static final byte ENCODING_PART_LIST_1 = -128;
	
	private Part[] parts;

	public EntitySkeleton(BitInput input) throws InvalidModelException {
		byte encoding = input.readByte();
		if(encoding == ENCODING_PART_LIST_1){
			loadPartList1(input);
		}
		else {
			throw new InvalidModelException("Unknown skeleton encoding: " + encoding);
		}
	}
	
	private void loadPartList1(BitInput input) throws InvalidModelException {
		int partLength = input.readByte() + 1;//to make sure parts[Byte.MAX_VALUE] will be accessible
		if(partLength <= 0) throw new InvalidModelException("Too few skeleton parts: " + partLength);
		parts = new Part[partLength];
		for(int index = 0; index < partLength; index++)
			parts[index] = Part.load1(input, index);
	}
	
	public void save(BitOutput output){
		savePartList1(output);
	}
	
	private void savePartList1(BitOutput output){
		output.addByte(ENCODING_PART_LIST_1);
		output.addByte((byte) (parts.length - 1));
		for(Part part : parts)
			part.save1(output);
	}
	
	public int getAmount(){
		return parts.length;
	}
	
	public static class Part {
		
		public static Part load1(BitInput input, int ownIndex) throws InvalidModelException {
			byte ordinal = input.readByte();
                        byte parentIndex = input.readByte();
                        if(parentIndex >= ownIndex) throw new InvalidModelException("parentIndex (" + parentIndex + ") must be smaller than ownIndex (" + ownIndex + ")");
                        if(parentIndex < -1) throw new InvalidModelException("parentIndex (" + parentIndex + ") is smaller than -1");
			if(ordinal <= 0) throw new InvalidModelException("Negative animation ordinal: " + ordinal);
			AnimationType[] values = AnimationType.values();
			if(ordinal >= values.length) throw new InvalidModelException("Invalid animation ordinal: " + ordinal);
			AnimationType animation = values[ordinal];
			int x = input.readInt();
			int y = input.readInt();
                        int z = input.readInt();
			int pitch = input.readInt();
			int yaw = input.readInt();
			int roll = input.readInt();
			int param1 = 0;
			int param2 = 0;
			if(animation.parameterCount >= 1){
                            param1 = input.readInt();
                            if(animation.parameterCount >= 2)
				param2 = input.readInt();
			}
			return new Part(parentIndex, x, y, z, pitch, yaw, roll, animation, param1, param2);
		}
		
		private final AnimationType animation;
                private final byte parentIndex;
		
		private final int param1;
		private final int param2;
		
		private final int x;
		private final int y;
		private final int z;
		
		private final int pitch;
		private final int yaw;
		private final int roll;
		
		private Part(byte parentIndex, int x, int y, int z, int pitch, int yaw, int roll, AnimationType animationType, int param1, int param2){
                    this.parentIndex = parentIndex;
                    this.x = x;
                    this.y = y;
                    this.z = z;
                    this.pitch = pitch;
                    this.yaw = yaw;
                    this.roll = roll;
                    animation = animationType;
                    this.param1 = param1;
                    this.param2 = param2;
		}
		
		public void save1(BitOutput output){
                    output.addByte(parentIndex);
			output.addInts(x, y, z, pitch, yaw, roll);
			output.addByte((byte) animation.ordinal());
			if(animation.parameterCount >= 1){
				output.addInt(param1);
				if(animation.parameterCount >= 2){
					output.addInt(param2);
				}
			}
		}
	}
	
	public static enum AnimationType {
		
		NONE(0),
		MOVING_PITCH(2),
		MOVING_YAW(2),
		MOVING_ROLL(2),
		ATTACK_PITCH(2),
		ATTACK_YAW(2),
		ATTACK_ROLL(2),
		RANDOM_PITCH(2),
		RANDOM_YAW(2),
		RANDOM_ROLL(2);
		
		
		
		private final int parameterCount;
		
		AnimationType(int parameterCount){
			this.parameterCount = parameterCount;
			//There is no point in doing anything more than defining the amount of parameters because the server won't render it anyway
		}
		
		public int getParameterCount(){
			return parameterCount;
		}
	}
}