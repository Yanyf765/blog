package com.blog.entity;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;

import com.github.rjeschke.txtmark.Processor;

@Entity
public class Blog implements Serializable{
	private static final long serialVersionUID = 1L;
	
	@Id // 主键
	@GeneratedValue(strategy = GenerationType.IDENTITY) // 自增长策略
	private Long id; // 用户的唯一标识
	
	@NotEmpty(message = "标题不能为空")
	@Size(min=2, max=50)
	@Column(nullable = false, length = 50) // 映射为字段，值不能为空
	private String title;
	
	@NotEmpty(message = "摘要不能为空")
	@Size(min=2, max=300)
	@Column(nullable = false) // 映射为字段，值不能为空
	private String summary;

	@Lob  // 大对象，映射 MySQL 的 Long Text 类型
	@Basic(fetch=FetchType.LAZY) // 懒加载
	@NotEmpty(message = "content内容不能为空")
	@Size(min=2)
	@Column(nullable = false) // 映射为字段，值不能为空
	private String content;
	
	@Lob  // 大对象，映射 MySQL 的 Long Text 类型
	@Basic(fetch=FetchType.LAZY) // 懒加载
	@NotEmpty(message = "htmlContent内容不能为空")
	@Size(min=2)
	@Column(nullable = false) // 映射为字段，值不能为空
	private String htmlContent; // 将 md 转为 html
	
	@OneToOne(cascade=CascadeType.DETACH)
	@JoinColumn(name="user_id")
	private User user;
	
	@Column(nullable = false) // 映射为字段，值不能为空
	@org.hibernate.annotations.CreationTimestamp  // 由数据库自动创建时间
	private Timestamp createTime;

	@Column(name="reading")
	private Long reading = 0L; // 访问量、阅读量
	 
	@Column(name="comments")
	private Long commentSize = 0L;  // 评论量

	@Column(name="likes")
	private Long likes = 0L;  // 点赞量

	public Blog() {

	}

	public Blog(String title, String summary, String content) {
		super();
		this.title = title;
		this.summary = summary;
		this.content = content;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
		this.htmlContent = Processor.process(content);
	}

	public String getHtmlContent() {
		return htmlContent;
	}

	public void setHtmlContent(String htmlContent) {
		this.htmlContent = htmlContent;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public Long getReading() {
		return reading;
	}

	public void setReading(Long reading) {
		this.reading = reading;
	}

	public Long getCommentSize() {
		return commentSize;
	}

	public void setComments(Long commentSize) {
		this.commentSize = commentSize;
	}

	public Long getLikes() {
		return likes;
	}

	public void setLikes(Long likes) {
		this.likes = likes;
	}
	
	public void addComment(Comment comment){
		this.comments.add(comment);
	}
	
}
