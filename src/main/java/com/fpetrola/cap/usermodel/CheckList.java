package com.fpetrola.cap.usermodel;

import javax.persistence.Entity;
import javax.persistence.Table;

import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;




@Entity()
@Table(name = "CheckList")
public class CheckList {
 
	@ManyToOne() @JoinColumn(name = "todo")
	protected String todo;
}
