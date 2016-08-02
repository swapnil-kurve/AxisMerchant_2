package com.nxg.axismerchant.classes;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.util.Log;

public class EncryptDecrypt
{

	String newtxt;;
	String iv,encrpttext,finalString,decrypttext="";
	CryptLib _crypt;
	SharedPreferences preferences;
	@SuppressWarnings("static-access")
	public String encrypt(String txt)
	{
		try
		{
			_crypt = new CryptLib();
			iv = _crypt.generateRandomIV(16);
//			String c = Constants.MPIN+Constants.IMEI;
//			String key = CryptLib.SHA256(Constants.MPIN+Constants.IMEI, 32);   // require to change
//			String c = mpin+imei;
			String key = CryptLib.SHA256(Constants.MPIN+Constants.IMEI, 32);   // require to change
			String s = _crypt.encrypt(txt,key, iv);
			encrpttext = s.replace("\n", "");
			finalString = encrpttext+"~"+iv;
		}
		catch (Exception e)
		{
			Log.e("Error while encryption", String.valueOf(e.getMessage()));
		}
		return finalString;

	}

	@SuppressLint("NewApi")
	@SuppressWarnings("static-access")
	public String decrypt(String txt)
	{
		try
		{
			String arr1[]=txt.split("~");
			for(int i=0;i<arr1.length;i++)
			{
				if(i==0)
				{
					newtxt=arr1[0];
				}
				else
				{
					iv=arr1[1];
				}
			}

			_crypt=new CryptLib();
//			String s=Constants.MPIN+Constants.IMEI;
//			String key = CryptLib.SHA256(Constants.MPIN+Constants.IMEI, 32);
			String key = CryptLib.SHA256(Constants.MPIN+Constants.IMEI, 32);
			decrypttext = _crypt.decrypt(newtxt,key, iv);
		}
		catch (Exception e) {
			Log.e("Error while decyption",String.valueOf(e.getMessage()));
			return "";
		}

		if(decrypttext==null)
			 decrypttext="";

		return decrypttext;

	}


	@SuppressWarnings("static-access")
	public String encryptDatabase(String txt)
	{
		try
		{
			_crypt=new CryptLib();
			iv=_crypt.generateRandomIV(27);
			String key = CryptLib.SHA512(Constants.secretekeyDatabase, 36);
			String s = _crypt.encrypt(txt,key, iv);
			encrpttext = s.replace("\n", "");
			finalString=encrpttext+"~"+iv;
		}
		catch (Exception e)
		{
			// TODO: handle exception
			Log.e("Error while encryption", String.valueOf(e.getMessage()));
		}
		return finalString;

	}

	@SuppressLint("NewApi")
	@SuppressWarnings("static-access")
	public String decryptDatabase(String txt)
	{

		try
		{


			String arr1[]=txt.split("~");
			for(int i=0;i<arr1.length;i++)
			{
				if(i==0)
				{
					newtxt=arr1[0];
				}
				else
				{
					iv=arr1[1];
				}
			}

			_crypt=new CryptLib();
			String key = CryptLib.SHA512(Constants.secretekeyDatabase, 36);
			decrypttext = _crypt.decrypt(newtxt,key, iv);
		}
		catch (Exception e) {
			// TODO: handle exception
			Log.e("Error while decyption",String.valueOf(e.getMessage()));
			return "";
		}


		if(decrypttext==null)
			 decrypttext="";

		return decrypttext;

	}

	/*@SuppressWarnings("static-access")
	public String encryptkey(String txt)
	{
		try
		{
			_crypt=new CryptLib();
			iv=_crypt.generateRandomIV(16);
			encrpttext = _crypt.encrypt(txt, CryptLib.key, iv);
			finalString=encrpttext+"~"+iv;
		}
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return finalString;
	}
	
	@SuppressWarnings("static-access")
	public String decryptkey(String txt)
	{
		try
		{
			_crypt=new CryptLib();
			iv=_crypt.generateRandomIV(16);
			decrypttext = _crypt.decrypt(txt, CryptLib.key, iv);
			finalString=decrypttext+"~"+iv;
		}
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return finalString;
		
	}*/

}
