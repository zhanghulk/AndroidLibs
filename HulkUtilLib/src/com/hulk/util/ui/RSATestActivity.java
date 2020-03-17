package com.hulk.util.ui;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.logging.Logger;

import com.hulk.util.R;
import com.hulk.util.rsa.RSAUtils;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class RSATestActivity extends Activity {
	// 字符串公钥，可以直接保存在客户端
	public static final String PUBLIC_KEY_STR = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArDq+LD0E5aRw9O6oElL2jvb7OGxOACxdcZZnvwN4L+Pv3aM4KSGl4Q7zDSAj/ViaQDC6Y0f3GXiAPIoGPcUnIcm/mpiNZ85NHgoYtgwpP4o0nAEarEUu/YPfdzYAVF7ku+azVJPxelbgxQV0tlamKk0H1COHi3nIdgbusaAvEarMZfFMk25MKB03LrWBjJ9ydDFOjvfokigdxvBDmFhyTsgU1QlEsDPKNFqRS+nrDx6z6j5Xpfeq3P59sQJLE3Hd6YGbUxJB4eVDua5KWS6Fw/5mFWfGBQmdMqm4dUEXlCAYr1U6GVtJJ+amSfzwP1U2D5KD7xCy8N3MJRlgsN2iFwIDAQAB";
	// 字符串密钥，通常保存在服务器，这里为了方便演示，直接保存在客户端
	public static final String PRIVATE_KEY_STR = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCsOr4sPQTlpHD07qgSUvaO9vs4bE4ALF1xlme/A3gv4+/dozgpIaXhDvMNICP9WJpAMLpjR/cZeIA8igY9xSchyb+amI1nzk0eChi2DCk/ijScARqsRS79g993NgBUXuS75rNUk/F6VuDFBXS2VqYqTQfUI4eLech2Bu6xoC8Rqsxl8UyTbkwoHTcutYGMn3J0MU6O9+iSKB3G8EOYWHJOyBTVCUSwM8o0WpFL6esPHrPqPlel96rc/n2xAksTcd3pgZtTEkHh5UO5rkpZLoXD/mYVZ8YFCZ0yqbh1QReUIBivVToZW0kn5qZJ/PA/VTYPkoPvELLw3cwlGWCw3aIXAgMBAAECggEABrWPHPgPjcaXI+N8JqKWukECzlLhwv33cepTBkzjTLJLcM3f7TJDXP4RF8zNuhvOfnundyChjpt0G2ehEJzyhk1uql4Q/B88P9RS3ByjKrd+jyk32cgkKXoOpX00DBVaQbud9siAmqxxuxsYTdYYSQORL4Fm0VcgKQDiIYdE7iIx0G+CTO8ClWKNwQsY82GdEd1DizGVz7p747k5doSiSi6Bu7YHXk9d5kiFeGhBRXO2KQt2ZfyVsRbDuKdyWvpMeRKFE8dsSvgEC1Cli8ThGjPM1PLJYmkWRGwFu+Rorua04u6ss6zqEam08pOm0qzfoKJ7ZvaiIhbecjadRC9qeQKBgQDYBeDxPQu1IwA92KtcazSCGXCk4cf3IqlDnlT/kVTdy5RsVa93mq2KAYSlTOq+6b58qPP5RlNx0kbWZUo4eyqy3s7GHcDI9kkSgljUKUboFNvtD4ROMgJ8f8xauEsKb1MOkS940JTJ4OfdzHfzOLj/DTfyxFl58AJGfUyi7hfJQwKBgQDMGiPvzJPFzvOL+jQPbF3B+ttlJLOAmHpgzlkqlWTD3EQC7EW9AZiuIlk0mgxXMWkULvpn2sem3/RwTbUp6omaz2/vWZE9UXUvLXAMWy44zNNaXUP/rROxvpFXuvD63N2BevHzL4t2GDCO54yrXq5vNkjqRBTee8sfqxpOLP68nQKBgQCqIh8h/6Eb3OAQ1XdIh0pIeH7F7OhPVGYY0jdBPJWpRO+1TtquCQ1KFp4Ajg6Ho5IZnfrgRSntB94wdn+48hAT5fTWBZLS811jjXMmTQgCOoNnNgROjYZ1xTUN8f1vz3OLkn7f2O6F/HLAtYt27CKPBTseINQTfBpep8pWu8vR/wKBgBWB46uPSTsc9bkYYogFiVO5lYjw9yFj7/FnjSnZmEazXU9ZinfCRU6EPBY47Xf6svH3iVeMTGGfU+jJp3+FQX7YwRjdvVpSzSBtj1MeAJ7nppXtIg89M8gVJsex4VbuE0FjrT9NEUsefW9xovckAQmjFMfq6LARJ3Rs2VbHkwhZAoGBANOj4V5tJzcZmoav14WfNTs5EPq+W8ZR73NDaffTq5oWytqYaQoG/haISANySHL5mF+PIZ5lKBRHnzO6u2tk+ir/LjmMqJT5WzhtQqAv8jkogkxzD8nyXtiIyRyf8s/oI2UQwdNWIxqQKLIrqGQ2HCuSC1QquZD1EmuIYjE6/w5j";

	public static final String PUBLIC_KEY_STR2 = "-----BEGIN PUBLIC KEY-----\\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwvnNjh51piNnhiu4+40P\\nwwFYIWOXjel3BXxx6s1UJ/VKsT3jI0uRDOm5L0OeCxtsH1DiGUvp/q0LiTHLvkOZ\\n7iZj4elBDG6wVpXckUda4T94w+tburFrF/u28ZLlysx5kTDv5LFQGRacCBZB2kzC\\n3XyRHDEYeQvn0pe9BWpI0inlgdgs8bWJnml8MOLdPr2tiIRFTTwzeNJ+LtGxnuPz\\nlf9uL4PjmpKjW6yjcW2pATWleZOjbZLrj2AhiBKl9X8ImkB+7uc+tRiazN9+QPsL\\n1Lv4tO+G7cCz1FEGvRU/EXNBc3kE2UikSQyo7vU73wOFXIRPT8Bju4dURA4zS6FM\\nywIDAQAB\\n-----END PUBLIC KEY-----\\n+40PwwFYIWOXjel3BXxx6s1UJ/VKsT3jI0uRDOm5L0OeCxtsH1DiGUvp/q0LiTHLvkOZ7iZj4elBDG6wVpXckUda4T94w+tburFrF/u28ZLlysx5kTDv5LFQGRacCBZB2kzC3XyRHDEYeQvn0pe9BWpI0inlgdgs8bWJnml8MOLdPr2tiIRFTTwzeNJ+LtGxnuPzlf9uL4PjmpKjW6yjcW2pATWleZOjbZLrj2AhiBKl9X8ImkB+7uc+tRiazN9+QPsL1Lv4tO+G7cCz1FEGvRU/EXNBc3kE2UikSQyo7vU73wOFXIRPT8Bju4dURA4zS6FMywIDAQAB";
	private static final String TAG = null;
	TextView msg_tv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		msg_tv = (TextView) findViewById(R.id.msg_tv);
		//testEncAndDec();
		
		PublicKey publicKey = RSAUtils.keyStrToPublicKey(PUBLIC_KEY_STR2, true);
		msg_tv.setText(publicKey == null ? "" : publicKey.toString());
	}
	
	void testEncAndDec() {
		// 获取公钥
		PublicKey publicKey = RSAUtils.keyStrToPublicKey(PUBLIC_KEY_STR, true);
		// 获取私钥
		PrivateKey privateKey = RSAUtils.keyStrToPrivate(PRIVATE_KEY_STR, true);

		// 需要加密的数据
		String clearText01 = "Hello, I am zhanghao. This is a RAS Demo";

		// 公钥加密结果
		String publicEncryptedResult = RSAUtils.encryptDataByPublicKey(clearText01.getBytes(), publicKey);
		// 私钥解密结果
		String privateDecryptedResult = RSAUtils.decryptedToStrByPrivate(publicEncryptedResult, privateKey);

		msg_tv.setText("例子一：\n公钥加密，私钥解密测试：\n" + "原文:\n" + clearText01 + "\n" + "公钥加密结果:\n" + publicEncryptedResult + "\n"
				+ "私钥解密结果:\n" + privateDecryptedResult);

		// 需要加密的数据
		String clearText02 = "这是Android平台RAS加解密例子，好好学习天天向上！";

		// 私钥加密结果
		String privateEncryptedResult = RSAUtils.encryptDataByPrivateKey(clearText02.getBytes(), privateKey);
		// 公钥解密结果
		String publicDecryptedResult = RSAUtils.decryptedToStrByPublicKey(privateEncryptedResult, publicKey);

		msg_tv.append("\n\n例子二：\n私钥加密，公钥解密测试：\n" + "原文：\n" + clearText02 + "\n" + "私钥加密结果:\n" + privateEncryptedResult + "\n"
				+ "公钥解密结果:\n" + publicDecryptedResult);
	}
}
