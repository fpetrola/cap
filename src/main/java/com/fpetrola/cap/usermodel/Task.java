package com.fpetrola.cap.usermodel;
 
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;                     
            

@Entity()
@Table(name = "Task")
public class Task  { 
            
	@ManyToOne() @JoinColumn(name = "title1")
	protected String title1; 
	protected String title3233;

	protected String title3233a;
	protected String title3233b;
  
  
	@ManyToOne() @JoinColumn(name = "d2")
	protected String d2;
 

	@ManyToOne() @JoinColumn(name = "status")
	protected String status;

	public Task() {
		// TODO Auto-generated constructor stub
	}
}
