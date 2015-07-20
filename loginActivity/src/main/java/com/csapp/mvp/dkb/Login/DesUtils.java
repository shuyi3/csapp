package com.csapp.mvp.dkb.Login;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

public class DesUtils {
	public static final String ALGORITHM_DES = "DES/CBC/PKCS5Padding";
    private static final String HEXES = "0123456789ABCDEF";
    /**
	 * DES算法，加密
	 * 
	 * @param data
	 *            待加密字符串
	 * @param key
	 *            加密私钥，长度不能够小于8位
	 * @return 加密后的字节数组，一般结合Base64编码使用
	 * @throws CryptException
	 *             异常
	 */

    @NotNull
    static byte[] Keys = { 0x46, 0x65, 0x6E, 0x67, (byte) 0x79, (byte) 0x75,
			(byte) 0x6E, (byte) 0x54 };

    @NotNull
    public static String encode( @NotNull String key,  @NotNull String data) throws Exception {
		return encode(key, data.getBytes());
	}

	/**
	 * DES算法，加密
	 * 
	 * @param data
	 *            待加密字符串
	 * @param key
	 *            加密私钥，长度不能够小于8位
	 * @return 加密后的字节数组，一般结合Base64编码使用
	 * @throws CryptException
	 *             异常
	 */
    @NotNull
    private static String encode( @NotNull String key, byte[] data) throws Exception {
		try {
			DESKeySpec dks = new DESKeySpec(key.getBytes());
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			// key的长度不能够小于8位字节
			Key secretKey = keyFactory.generateSecret(dks);
			Cipher cipher = Cipher.getInstance(ALGORITHM_DES);
			IvParameterSpec iv = new IvParameterSpec(Keys);
			AlgorithmParameterSpec paramSpec = iv;
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, paramSpec);
			byte[] bytes = cipher.doFinal(data);
			return bytesToHex(bytes);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	/**
	 * DES算法，解密
	 * 
	 * @param data
	 *            待解密字符串
	 * @param key
	 *            解密私钥，长度不能够小于8位
	 * @return 解密后的字节数组
	 * @throws Exception
	 *             异常
	 */
	private static byte[] decode( @NotNull String key, byte[] data) throws Exception {
		try {
			DESKeySpec dks = new DESKeySpec(key.getBytes());
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			// key的长度不能够小于8位字节
			Key secretKey = keyFactory.generateSecret(dks);
			Cipher cipher = Cipher.getInstance(ALGORITHM_DES);
			IvParameterSpec iv = new IvParameterSpec(Keys);
			AlgorithmParameterSpec paramSpec = iv;
			cipher.init(Cipher.DECRYPT_MODE, secretKey, paramSpec);
			return cipher.doFinal(data);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	/**
	 * 解密.
	 * 
	 * @param key
	 * @param data
	 * @return
	 * @throws Exception
	 */
	@Nullable
    public static String decode( String key,  @NotNull String data) {
		byte[] datas;
		String value = null;
		try {
			if (System.getProperty("os.name") != null
					&& (System.getProperty("os.name").equalsIgnoreCase("sunos") || System
							.getProperty("os.name").equalsIgnoreCase("linux"))) {
				datas = decode(key, decode(data.toString()));
				System.out.println("key=" + key + " data=" + data.toString());
			} else {
				datas = decode(key, decode(data.toString()));
				System.out.println("key=" + key + " data=" + data.toString());
			}
			value = new String(datas);
		} catch (Exception e) {
			value = "";
			e.printStackTrace();
		}
		return value;
	}

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] decode(String hex){

        String[] list=hex.split("(?<=\\G.{2})");
        ByteBuffer buffer= ByteBuffer.allocate(list.length);
        for(String str: list)
            buffer.put(Byte.parseByte(str,16));

        return buffer.array();
    }


}