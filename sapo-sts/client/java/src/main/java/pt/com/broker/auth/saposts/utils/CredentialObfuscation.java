package pt.com.broker.auth.saposts.utils;

import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CredentialObfuscation
{
	private static byte[] shuffle(byte[] data)
	{
		final int BLOCK_SIZE = 32;
		if ((data.length % BLOCK_SIZE) != 0)
			return null;

		byte[] clone = data.clone();

		int blocks = clone.length / BLOCK_SIZE;

		for (int base = 0; base != blocks; ++base)
		{
			int smallBlock = BLOCK_SIZE / 2;
			for (int i = 0; i != smallBlock; ++i)
			{
				int firstIndex = (BLOCK_SIZE * base) + i;
				int secondIndex = (BLOCK_SIZE * base) + BLOCK_SIZE - 1 - i;

				byte aux = (byte) (clone[firstIndex] ^ ((byte) 0xff));

				clone[firstIndex] = (byte) (clone[secondIndex] ^ ((byte) 0xff));

				clone[secondIndex] = aux;
			}
		}

		return clone;
	}

	public static String obfuscate(String plaintext)
	{
		try
		{
			byte[] plainData = plaintext.getBytes();

			KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
			keyGenerator.init(128);
			SecretKey secretKey = keyGenerator.generateKey();

			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);

			byte[] protectedData = cipher.doFinal(plainData);

			byte[] iv = cipher.getIV();
			byte[] encodedKey = secretKey.getEncoded();

			byte[] allData = new byte[iv.length + encodedKey.length + protectedData.length];

			System.arraycopy(iv, 0, allData, 0, iv.length);
			System.arraycopy(encodedKey, 0, allData, iv.length, encodedKey.length);
			System.arraycopy(protectedData, 0, allData, iv.length + encodedKey.length, protectedData.length);

			byte[] shuffleData = shuffle(allData);

			String encodedShuffleData = Base64.encodeBytes(shuffleData);

			return encodedShuffleData;
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}

		return null;
	}

	public static String deObfuscate(String obfuscatedData)
	{
		try
		{
			byte[] shuffleData = Base64.decode(obfuscatedData);

			byte[] allData = shuffle(shuffleData);

			byte[] iv = new byte[16];
			System.arraycopy(allData, 0, iv, 0, iv.length);

			byte[] encodedKey = new byte[16];
			System.arraycopy(allData, iv.length, encodedKey, 0, encodedKey.length);

			byte[] protectedData = new byte[shuffleData.length - (iv.length + encodedKey.length)];

			System.arraycopy(allData, iv.length + encodedKey.length, protectedData, 0, shuffleData.length - (iv.length + encodedKey.length));

			SecretKey secretKey = new SecretKeySpec(encodedKey, "AES");

			AlgorithmParameterSpec ivParameterSpec = new IvParameterSpec(iv);

			Cipher decipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			decipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);

			byte[] plaindata = decipher.doFinal(protectedData);

			String plaintext = new String(plaindata);
			return plaintext;
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
		return null;
	}
}
