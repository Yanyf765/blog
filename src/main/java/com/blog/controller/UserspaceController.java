package com.blog.controller;

import java.util.List;

import javax.validation.ConstraintViolationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.blog.entity.Blog;
import com.blog.entity.Catalog;
import com.blog.entity.User;
import com.blog.entity.Vote;
import com.blog.service.BlogService;
import com.blog.service.CatalogService;
import com.blog.service.UserService;
import com.blog.util.ConstraintViolationExceptionHandler;
import com.blog.vo.Response;


@Controller
@RequestMapping("/u")
public class UserspaceController {
	
	@Autowired
	private UserDetailsService userDetailsService;
	
	@Autowired
	private BlogService blogService;
	
	@Autowired
	UserService userService;
	
	@Autowired
	CatalogService catalogService;
	
	@GetMapping("/{username}")
	public String userSpace(@PathVariable("username") String username, Model model) {
		User  user = (User)userDetailsService.loadUserByUsername(username);
		model.addAttribute("user", user);
		return "redirect:/u/" + username + "/blogs";
	}
	
	@GetMapping("/{username}/profile")
	@PreAuthorize("authentication.name.equals(#username)") 
	public ModelAndView profile(@PathVariable("username") String username, Model model) {
		User  user = (User)userDetailsService.loadUserByUsername(username);
		model.addAttribute("user", user);
		return new ModelAndView("/userspace/profile", "userModel", model);
	}
	
	@GetMapping("/{username}/blogs")
	public String listBlogsByOrder(
			@PathVariable("username")String username,
			@RequestParam(value="order",required=false,defaultValue="new")String order,
			@RequestParam(value="category",required=false)Long category,
			@RequestParam(value="keyword",required=false,defaultValue="")String keyword,
			@RequestParam(value="async",required=false)boolean async,
			@RequestParam(value="pageIndex",required=false,defaultValue="0")int pageIndex,
			@RequestParam(value="pageSize",required=false,defaultValue="10")int pageSize,
			Model model){
		System.out.println("username="+username);
		
		User user = (User)userDetailsService.loadUserByUsername(username);
		model.addAttribute("user", user);
		
		if(category !=null){
			System.out.println("category:"+category);
			System.out.print("selflink:" + "redirect:/u/"+ username +"/blogs?category="+category);
			return "/u";
			
		}
		Page<Blog> page=null;
		if(order.equals("hot")){
			//最热查询
			Sort sort = new Sort(Direction.DESC,"reading","comments","likes");
			Pageable pageable = new PageRequest(pageIndex, pageSize);
			page = blogService.listBlogsByTitleVoteAndSort(user, keyword, pageable);
		}
		
		if (order.equals("new")) { // 最新查询
			Pageable pageable = new PageRequest(pageIndex, pageSize);
			page = blogService.listBlogsByTitleVote(user, keyword, pageable);
		}
 
		
		List<Blog> list = page.getContent();	// 当前所在页面数据列表
		
		model.addAttribute("order", order);
		model.addAttribute("page", page);
		model.addAttribute("blogList", list);
		return (async==true?"/userspace/u :: #mainContainerRepleace":"/userspace/u");
	}
	
	/*
	 * 获取头像修改页面
	 * <p>Title: avatar</p>  
	 * <p>Description: </p>  
	 * @param username
	 * @param model
	 * @return
	 */
	@GetMapping("/{username}/avatar")
	@PreAuthorize("authentication.name.equals(#username)")
	public ModelAndView avatar(@PathVariable("username")String username,Model model){
		User user = (User)userDetailsService.loadUserByUsername(username);
		model.addAttribute("user", user);
		return new ModelAndView("/userspace/avatar","userModel",model);
	}
	
	/*
	 * 保存用户头像
	 * <p>Title: saveAvatar</p>  
	 * <p>Description: </p>  
	 * @param username
	 * @param user
	 * @return
	 */
	@PostMapping("/{username}/avatar")
	@PreAuthorize("authentication.name.equals(#username)")
	public ResponseEntity<Response> saveAvatar(@PathVariable("username")String username,
			@RequestBody User user){
		String avatarUrl = user.getAvatar();
		
		User originalUser = userService.getUserById(user.getId());
		originalUser.setAvatar(avatarUrl);
		userService.saveUser(originalUser);
		
		return ResponseEntity.ok().body(new Response(true,"处理成功",avatarUrl));
	}
	
	/**
	 * 获取博客展示界面
	 * @param id
	 * @param model
	 * @return
	 */
	@GetMapping("/{username}/blogs/{id}")
	public String getBlogById(@PathVariable("username") String username,@PathVariable("id") Long id, Model model) {
		User principal = null;
		Blog blog = blogService.getBlogById(id);
		
		// 每次读取，简单的可以认为阅读量增加1次
		blogService.readingIncrease(id);

		// 判断操作用户是否是博客的所有者
		boolean isBlogOwner = false;
		if (SecurityContextHolder.getContext().getAuthentication() !=null && SecurityContextHolder.getContext().getAuthentication().isAuthenticated()
				 &&  !SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString().equals("anonymousUser")) {
			principal = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal(); 
			if (principal !=null && username.equals(principal.getUsername())) {
				isBlogOwner = true;
			} 
		}
		
		// 判断操作用户的点赞情况
		List<Vote> votes = blog.getVotes();
		Vote currentVote = null; // 当前用户的点赞情况
		
		if (principal !=null) {
			for (Vote vote : votes) {
				if(vote.getUser().getUsername().equals(principal.getUsername())){
					currentVote = vote;
					break;
				}
			}
		}

		model.addAttribute("isBlogOwner", isBlogOwner);
		model.addAttribute("blogModel",blog);
		model.addAttribute("currentVote",currentVote);
		
		return "/userspace/blog";
	}
	
	/**
	 * 删除博客
	 * @param id
	 * @param model
	 * @return
	 */
	@DeleteMapping("/{username}/blogs/{id}")
	@PreAuthorize("authentication.name.equals(#username)") 
	public ResponseEntity<Response> deleteBlog(@PathVariable("username") String username,@PathVariable("id") Long id) {
		try {
			blogService.removeBlog(id);
		} catch (Exception e) {
			return ResponseEntity.ok().body(new Response(false, e.getMessage()));
		}
		
		String redirectUrl = "/u/" + username + "/blogs";
		return ResponseEntity.ok().body(new Response(true, "处理成功", redirectUrl));
	}
	
	/**
	 * 获取新增博客的界面
	 * @param model
	 * @return
	 */
	@GetMapping("/{username}/blogs/edit")
	public ModelAndView createBlog(@PathVariable("username") String username,Model model) {
		User user = (User)userDetailsService.loadUserByUsername(username);
		List<Catalog> catalogs = catalogService.listCatalogs(user);
		
		model.addAttribute("blog", new Blog(null, null, null));
		model.addAttribute("catalogs", catalogs);
		return new ModelAndView("/userspace/blogedit", "blogModel", model);
	}
	
	/**
	 * 获取编辑博客的界面
	 * @param model
	 * @return
	 */
	@GetMapping("/{username}/blogs/edit/{id}")
	public ModelAndView editBlog(@PathVariable("username") String username,@PathVariable("id") Long id, Model model) {
		// 获取用户分类列表
		User user = (User)userDetailsService.loadUserByUsername(username);
		List<Catalog> catalogs = catalogService.listCatalogs(user);
		
		model.addAttribute("blog", blogService.getBlogById(id));
		model.addAttribute("catalogs", catalogs);
		return new ModelAndView("/userspace/blogedit", "blogModel", model);
	}
	
	/**
	 * 保存博客
	 * @param username 
	 * @param blog
	 * @return
	 */
	@PostMapping("/{username}/blogs/edit")
	@PreAuthorize("authentication.name.equals(#username)") 
	public ResponseEntity<Response> saveBlog(@PathVariable("username") String username, 
			@RequestBody Blog blog) {
		//对catalog进行空处理
		if(blog.getCatalog().getId() == null){
			return ResponseEntity.ok().body(new Response(false, "未选择分类"));
		}
		try {
			//判断是修改还是新增
			if(blog.getId()!=null){
				//修改
				Blog orignalBlog = blogService.getBlogById(blog.getId());
				orignalBlog.setTitle(blog.getTitle());
				orignalBlog.setContent(blog.getContent());
				orignalBlog.setSummary(blog.getSummary());
				orignalBlog.setCatalog(blog.getCatalog());
				orignalBlog.setTags(blog.getTags());
				blogService.saveBlog(orignalBlog);
			}else{
				//新增
				User user = (User)userDetailsService.loadUserByUsername(username);
				blog.setUser(user);
				blogService.saveBlog(blog);
			}
			
		}catch(ConstraintViolationException e){
			return ResponseEntity.ok().body(new Response(false,ConstraintViolationExceptionHandler.getMessage(e)));	
		} catch (Exception e) {
			return ResponseEntity.ok().body(new Response(false, e.getMessage()));
		}
		
		String redirectUrl = "/u/" + username + "/blogs/" + blog.getId();
		return ResponseEntity.ok().body(new Response(true, "处理成功", redirectUrl));
	}
	

	
}
