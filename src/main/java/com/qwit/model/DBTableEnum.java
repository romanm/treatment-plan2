package com.qwit.model;

/**
 * @author Simon Klebeck
 *
 */
public enum DBTableEnum {
	patient		("patient"),
	concept		("protocol"),
	schema		("task"),
	drug		("drug"),
	finding		("finding"),
	diagnose	("diagnose"),
	labor		("labor");
	
	private  String tableName;
	
	public String getTableName()
	{
		return tableName;
	}
	
	public void setTableName(String name)
	{
		DBTableEnum[] n = DBTableEnum.values();
		tableName = "undefined";
		for (DBTableEnum e : n)
		{
			if(e.getTableName().equals(name))
				tableName = name;
		}
	}
	
	
    private DBTableEnum(String name)
    {
    	tableName = name;
    }
   
}
