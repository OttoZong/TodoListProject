package com.ofir.model;

public class Item
{
	private int id;
	private String userEmail;
	private String title;
	private String content;
	private Status status;
	
    public enum Status
	{
		ready,
		inProgress,
		finish;
	}
    
    public Item()
    {}
    
	public Item(String title, String content, Status status, String userMail)
	{
		super();
		setTitle(title);
		setContent(content);
		setStatus(status);
		setUserEmail(userMail);
	}
	
	public Item(int id,String title, String content, Status status, String userMail)
	{
		super();
    	setId(id);
		setTitle(title);
		setContent(content);
		setStatus(status);
		setUserEmail(userMail);
	}
	
	public int getId()
	{
		return id;
	}
	
	public void setId(int id)
	{
		this.id = id;
	}
	
	public String getUserEmail()
	{
		return userEmail;
	}
	
	public void setUserEmail(String userEmail)
	{
		this.userEmail = userEmail;
	}
	
	public String getTitle()
	{
		return title;
	}
	
	public void setTitle(String title)
	{
		this.title = title;
	}
	
	public String getContent()
	{
		return content;
	}
	
	public void setContent(String content)
	{
		this.content = content;
	}
	
	public Status getStatus()
	{
		return status;
	}
	
	public void setStatus(Status status)
	{
		this.status = status;
	}
	
	@Override
	public String toString()
	{
		return id + "," + title + "," + content + "," + status;
	}
}
