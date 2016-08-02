package com.nxg.axismerchant.classes;

import android.annotation.SuppressLint;
import android.util.Log;


public class EncryptDecryptRegister
{
 
	String newtxt;;
	String iv,encrpttext,finalString,decrypttext="";
	CryptLib _crypt;
	@SuppressWarnings("static-access")
	public String encrypt(String txt)
	{
		try
		{
			_crypt=new CryptLib();
			iv=_crypt.generateRandomIV(16);
			String key = CryptLib.SHA256("my secret key", 32);
			String s = _crypt.encrypt(txt,key, iv);
			encrpttext = s.replace("\n", "");
			finalString=encrpttext+"~"+iv;
		}
		catch (Exception e) 
		{
			// TODO: handle exception
			Log.e("Error while encyption", String.valueOf(e.getMessage()));
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
			String key = CryptLib.SHA256("my secret key", 32);
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
	
}
