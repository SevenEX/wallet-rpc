package com.ztuo.bc.wallet.aes;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;


public class AESUtils {
    /**
     * 注意key和加密用到的字符串是不一样的 加密还要指定填充的加密模式和填充模式 AES密钥可以是128或者256，加密模式包括ECB, CBC等
     * ECB模式是分组的模式，CBC是分块加密后，每块与前一块的加密结果异或后再加密 第一块加密的明文是与IV变量进行异或
     */
    public static final String KEY_ALGORITHM = "AES";
    public static final String ECB_CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";
    public static final String CBC_CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    public static final String PLAIN_TEXT = "MANUTD is the greatest club in the world";
    
    /**
     * IV(Initialization Value)是一个初始值，对于CBC模式来说，它必须是随机选取并且需要保密的
     * 而且它的长度和密码分组相同(比如：对于AES 128为128位，即长度为16的byte类型数组)
     * 
     */
    public static final byte[] IVPARAMETERS = new byte[] { 1, 2, 3, 4, 5, 6, 7,
            8, 9, 10, 11, 12, 13, 14, 15, 16 };

    public static void main(String[] arg) throws Exception {
//       // 账户AES密钥：加密私钥的密钥
//       String aes = generateAESSecretKey();
//       // 加密账户AES密钥的密钥
//       String aes1 = generateAESSecretKey();
//       System.out.println(aes);
//       System.out.println(aes1);
//       // 用户私钥
//       String priKey = "uoipdfjasfklasdgusdiuaioj;erakfljas;kdlhjfadsio'algjqioj;mfldsf";
//       // 加密私钥
//       String enCodePriKey = AESUtils.encryptForCoupons(priKey, aes);
//       // 加密后私钥
//       System.out.println(enCodePriKey);
//       // 加密账户AES密钥
//       String enCodeAesKey = AESUtils.enCode(aes, aes1);
//       // 加密后AES密钥
//       System.out.println(enCodeAesKey);
       // 解密AES密钥
       String deAesKey= AESUtils.deCode("74fdc62890040d440b312a17d3c64926b2706b2102e0503aaf355e5191e48318537c3c031c48a5266a78ac0865469181", "d6efc78c997c8b595a7a3dd5dc3c1f6a");
       System.out.println("deAesKey:" + deAesKey);
       System.out.println(AESUtils.decryptForCoupons("21c9d43c1fe5e59480d6e29fee05978822fc5af870c20b2fa6c91cde7a069a434e06c38307794d2893bbdf21ae2c65f77e316608627e0e4c147e7a19bf33ad2e071a9917454ae2b3d7fac538c8c45c74", deAesKey));
       // 解密用户私钥
//       String dePriKey = AESUtils.decryptForCoupons(enCodePriKey, deAesKey);
//       System.out.println("dePriKey:" + dePriKey);
    }

    /** 
     * 加密（ECB模式）
     * @Title: enCode 
     * @param data 加密明文
     * @param key 密钥
     * @return String    返回类型 
     * @lastModify 2018年4月21日
     */
	public static String enCode(String data, String key) {
		// 还原密钥
		SecretKey secretKey = restoreSecretKey(hexStringToBytes(key));
		// 加密
		byte[] encodedText = AesEcbEncode(data.getBytes(), secretKey);
		// 返回结果
		return bytesToHexString(encodedText);
	}
    
    /** 
     * 解密（ECB模式）
     * @Title: deCode 
     * @param data 加密密文
     * @param key  密钥
     * @return String    返回类型 
     * @lastModify 2018年4月21日
     */
    public static String deCode(String data, String key) {
    	// 还原密钥
    	 SecretKey secretKey = restoreSecretKey(hexStringToBytes(key));
    	 // 
         return AesEcbDecode(hexStringToBytes(data),secretKey);
    }
    
    /** 
     * 根据密钥进行加密 CBC模式
     * @Title: encryptForCoupons 
     * @param data
     * @param key
     * @return String    返回类型 
     * @lastModify 2018年4月21日
     */
    public static String encryptForCoupons(String data, String key) {
		try {
			Cipher cipher = Cipher.getInstance(CBC_CIPHER_ALGORITHM);
			int blockSize = cipher.getBlockSize();

			byte[] dataBytes = data.getBytes("UTF-8");
			int plaintextLength = dataBytes.length;
			if (plaintextLength % blockSize != 0) {
				plaintextLength = plaintextLength
						+ (blockSize - (plaintextLength % blockSize));
			}

			byte[] plaintext = new byte[plaintextLength];
			System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);

			SecretKey keyspec = restoreSecretKey(hexStringToBytes(key));
			IvParameterSpec ivspec = new IvParameterSpec(IVPARAMETERS);

			cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
			byte[] encrypted = cipher.doFinal(plaintext);
			return bytesToHexString(encrypted);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
    
	/** 
	 * 解密 CBC模式
	 * @Title: decryptForCoupons 
	 * @param data 密文
	 * @param key 密钥
	 * @throws Exception 设定文件 
	 * @return String    返回类型 
	 * @lastModify 2018年4月21日
	 */
	public static String decryptForCoupons(String data, String key) throws Exception {
		try {

			byte[] encrypted1 = hexStringToBytes(data);

			Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
			SecretKey keyspec = restoreSecretKey(hexStringToBytes(key));
			IvParameterSpec ivspec = new IvParameterSpec(IVPARAMETERS);

			cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);

			byte[] original = cipher.doFinal(encrypted1);
			String originalString = new String(original, "UTF-8");
			return originalString.trim();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
    /**
     * 使用ECB模式进行加密。 加密过程三步走： 1. 传入算法，实例化一个加解密器 2. 传入加密模式和密钥，初始化一个加密器 3.
     * 调用doFinal方法加密
     * 
     * @param plainText
     * @return
     */
	private static byte[] AesEcbEncode(byte[] plainText, SecretKey key) {

        try {

            Cipher cipher = Cipher.getInstance(ECB_CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(plainText);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException
                | InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 使用ECB解密，三步走，不说了
     * 
     * @param decodedText
     * @param key
     * @return
     */
    private static String AesEcbDecode(byte[] decodedText, SecretKey key) {
        try {
            Cipher cipher = Cipher.getInstance(ECB_CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            return new String (cipher.doFinal(decodedText));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException
                | InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     * 1.创建一个KeyGenerator 2.调用KeyGenerator.generateKey方法
     * 由于某些原因，这里只能是128，如果设置为256会报异常，原因在下面文字说明
     * 
     * @return
     */
    public static String generateAESSecretKey() {
        KeyGenerator keyGenerator;
        try {
            keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM);
             keyGenerator.init(128);
//            return Base64.encodeBase64String(keyGenerator.generateKey().getEncoded());
            return bytesToHexString(keyGenerator.generateKey().getEncoded());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

 // 后台测试签名的时候 要和前台保持一致，所以需要将结果转换
    public static String bytesToHexString(byte[] bytes) {
        StringBuilder string = new StringBuilder();
        for (byte b : bytes) {
            String hexString = Integer.toHexString(0x00FF & b);
            string.append(hexString.length() == 1 ? "0" + hexString : hexString);
        }
        return string.toString();
    }

    // 前台的签名结果是将byte中的一些 负数转换成了正数，
    // 但是后台验证的方法需要的又必须是转换之前的
    public static byte[] hexStringToBytes(String data) {
        int k = 0;
        byte[] results = new byte[data.length() / 2];
        for (int i = 0; i + 1 < data.length(); i += 2, k++) {
            results[k] = (byte) (Character.digit(data.charAt(i), 16) << 4);
            results[k] += (byte) (Character.digit(data.charAt(i + 1), 16));
        }
        return results;
    }
   
    /**
     * 还原密钥
     * 
     * @param secretBytes
     * @return
     */
    public static SecretKey restoreSecretKey(byte[] secretBytes) {
        SecretKey secretKey = new SecretKeySpec(secretBytes, KEY_ALGORITHM);
        return secretKey;
    }
}