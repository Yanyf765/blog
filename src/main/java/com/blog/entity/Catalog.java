package com.blog.entity;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;

@Entity
public class Catalog implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@NotEmpty(message="名称不能为空")
	@Size(min=2,max=30)
	@Column(nullable=false)
	private String name;
	
	@OneToOne(cascade=CascadeType.DETACH,fetch=FetchType.LAZY)
	@JoinColumn(name="user_id")
	private User user;
	
	

	protected Catalog() {
	}
	

	public Catalog(String name, User user) {
		super();
		this.name = name;
		this.user = user;
	}


	public void setName(String name) {
		this.name = name;
	}




	public String getName() {
		return name;
	}


	public Long getId() {
		return id;
	}



	public void setId(Long id) {
		this.id = id;
	}



	public User getUser() {
		return user;
	}



	public void setUser(User user) {
		this.user = user;
	}



	

}
