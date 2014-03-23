package com.qwit.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.qwit.model.TreeManager;

public class Prefactor {
	static Pattern absDayMinus = Pattern.compile("-?(\\d+)");
public static void main(String[] args) {
	String absDayVal = "43";
	System.out.println("-----------  "+absDayVal);
	Matcher m_absDayMinus = absDayMinus.matcher(absDayVal);
	System.out.println("-----------"+m_absDayMinus);
	if(m_absDayMinus.find()){
		System.out.println("---------w--");
		String group = m_absDayMinus.group();
		System.out.println("----------- "+group);
		System.out.println("----------- "+m_absDayMinus.pattern());
		
	}
	if(true)
		return;
	System.out.println("-----------");
	Prefactor pf = new Prefactor();
	long f20 = pf.factorial(20);
	System.out.println("20!="+f20);
	long f19 = pf.factorial(19);
	System.out.println("19!="+f19);
	System.out.println("20!/19!="+(f20/f19));
	System.out.println("-----------");
	int x = 2012;
	long st = Calendar.getInstance().getTimeInMillis();
	pf.findPrefactor(x);
	long t1 = Calendar.getInstance().getTimeInMillis()-st;
	pf.findPrefactor2(x);
//	pf.findPrimeNumber(x);
	long t2 = Calendar.getInstance().getTimeInMillis()-st;
	System.out.println(t1+"/"+(t2-t1)+"/"+t2);
//	pf.sumAB(x);
	pf.multipAB(x);
}


private long factorial(int x) {
	if(0==x)
		return 1;
	else
		return x*factorial(x-1);
}
private void multipAB(int x) {
	for (int a = 0; a < x; a++) 
		for (int b = a; b < x; b++) 
			if(a*b==x)
				System.out.println(a+"*"+b+"="+(a*b));
}

private void sumAB(int x) {
	for (int a = 0; a < x; a++) 
		for (int b = a; b < x; b++) 
			if(a+b==x)
				System.out.println(a+"+"+b+"="+(a+b));
}

private void findPrimeNumber(int x) {
	int npn = 0;
	String pns = "";
	for (int i = 2; i <=x; i++)
		if(isSimpleNr(i))
		{
			pns+=(pns.length()>0?",":"")+i;
			npn++;
			if(npn%20==0){
				pns+="\n";
			}
		}
	System.out.println("("+npn+")");
	System.out.println(pns+".");
	System.out.println("("+npn+")");
}

List<Integer> l;
Map<Integer,Integer> m;
private void findPrefactor2(int x) {
	l=new ArrayList<Integer>();
	m=new HashMap<Integer,Integer>();
	findPrefactor3(x);
	String s="";
	for(Integer i:l)
		s=(i+"^"+m.get(i)+"*")+s;
	if(s.length()>0)
		s=s.substring(0,s.length()-1);
	System.out.println(s+"="+x);
}

private void findPrefactor3(int x) {
	for (int i = 2; i <= x; i++)
		if(x%i==0&&isSimpleNr(i)){
			if(m.containsKey(i))
				m.put(i, m.get(i)+1);
			else{
				l.add(i);
				m.put(i, 1);
			}
			findPrefactor3(x/i);
			break;
		}
}
private void findPrefactor(int x) {
	System.out.println("Prefactor of "+x);
	String pc = "";
	for (int i = 2; i < x; i++)
		if(x%i==0&&isSimpleNr(i))
			pc+=(pc.length()>0?",":"")+i;
	System.out.println(pc+".");
}


private boolean isSimpleNr(int x) {
	for (int i = 2; i < x; i++) if(x%i==0) return false;
	return true;
}

//public static byte[] getBytes(TreeManager docMtl) throws java.io.IOException{
public static byte[] getBytes(TreeManager docMtl){
	java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
	try {
		java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
		oos.writeObject(docMtl);
		oos.flush();
		oos.close();
		bos.close();
	} catch (IOException ioe) {;
		log.fatal(ioe.getMessage());
	}
	byte [] data = bos.toByteArray();
	return data;
}
public static TreeManager toObject(byte[] bytes){ 
	TreeManager docMtl = null;
	java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(bytes);
	try{
		java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bis);
		docMtl = (TreeManager) ois.readObject();
		ois.close();
		bis.close();
	}catch(java.io.IOException ioe){
		log.fatal(ioe.getMessage());
	}catch(java.lang.ClassNotFoundException cnfe){
		log.fatal(cnfe.getMessage());
	}
	return docMtl;
}
private final static Log log = LogFactory.getLog(Prefactor.class);
}
